
/*
 * Copyright (C) 2011 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.stuff.vaadin7;

import java.util.HashMap;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import com.vaadin.ui.UI;
import com.vaadin.ui.UI.CleanupEvent;

/**
 * A Spring custom {@link Scope} for Vaadin UI lifetime.
 *
 * <p>
 * This works for applications that subclass {@link BaseContextApplication}; objects will be scoped to each
 * {@link UI} instance. Spring {@linkplain org.springframework.beans.factory.DisposableBean#destroy destroy-methods}
 * will be invoked when the {@link UI} is closed.
 * </p>
 *
 * <p>
 * To enable this scope, simply add this bean to your application context as a singleton (it will register itself):
 * <blockquote><pre>
 *  &lt;!-- Enable the "vaadinUI" custom scope --&gt;
 *  &lt;bean class="org.dellroad.stuff.vaadin.VaadinUIScope"/&gt;
 * </pre></blockquote>
 * Then declare scoped beans normally using the scope name {@code "vaadinUI"}.
 * </p>
 */
public class VaadinUIScope implements Scope, BeanFactoryPostProcessor, UI.CleanupListener {

    /**
     * Key to the current application instance. For use by {@link #resolveContextualObject}.
     */
    public static final String APPLICATION_KEY = "ui";

    /**
     * The name of this scope (i.e., <code>{@value}</code>).
     */
    public static final String SCOPE_NAME = "vaadinUI";

    private final HashMap<UIScopeKey, ApplicationBeanHolder> beanHolders
      = new HashMap<UIScopeKey, ApplicationBeanHolder>();

// BeanFactoryPostProcessor methods

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        beanFactory.registerScope(VaadinUIScope.SCOPE_NAME, this);
    }

// ContextApplication.CloseListener methods

    @Override
    public void cleanup(CleanupEvent event) {
    	final UI ui = event.getUI();
    	final UIScopeKey key = new UIScopeKey(getContextByUI(ui), ui.getUIId());
    	
        ApplicationBeanHolder beanHolder;
        synchronized (this) {
            beanHolder = this.beanHolders.remove(key);
        }
        if (beanHolder != null)
            beanHolder.close();
    }
    
    private ConfigurableWebApplicationContext getContextByUI(UI ui) {
    	if (ui instanceof SpringUI) {
    		final SpringUI springUI = (SpringUI) ui;
    		final ConfigurableWebApplicationContext context = springUI.getContext();
    		return context;
    	}
    	else {
    		throw new IllegalStateException();
    	}
    }

// Scope methods

    @Override
    public synchronized Object get(String name, ObjectFactory<?> objectFactory) {
        return this.getApplicationBeanHolder(true).getBean(name, objectFactory);
    }

    @Override
    public synchronized Object remove(String name) {
        ApplicationBeanHolder beanHolder = this.getApplicationBeanHolder(false);
        return beanHolder != null ? beanHolder.remove(name) : null;
    }

    @Override
    public synchronized void registerDestructionCallback(String name, Runnable callback) {
        this.getApplicationBeanHolder(true).registerDestructionCallback(name, callback);
    }

    @Override
    public String getConversationId() {
        UI ui = BaseContextApplication.getCurrentUI();
        if (ui == null)
            return null;
        return ui.getClass().getName() + "@" + System.identityHashCode(ui);
    }

    @Override
    public Object resolveContextualObject(String key) {
        if (APPLICATION_KEY.equals(key))
            return BaseContextApplication.getCurrentUI();
        return null;
    }

// Internal methods

    private synchronized ApplicationBeanHolder getApplicationBeanHolder(boolean create) {
        final UI ui = BaseContextApplication.getCurrentUI();
        final UIScopeKey key = new UIScopeKey(getContextByUI(ui), ui.getUIId());
        // application.addListener(this);
        ApplicationBeanHolder beanHolder = this.beanHolders.get(key);
        if (beanHolder == null && create) {
            beanHolder = new ApplicationBeanHolder(key);
            this.beanHolders.put(key, beanHolder);
        }
        return beanHolder;
    }
    
// Scope key
    
    private static class UIScopeKey {
    	
    	private final ConfigurableWebApplicationContext context;
    	
    	private final int uiId;
    	
    	private UIScopeKey(ConfigurableWebApplicationContext context, int uiId) {
    		if (context == null) {
    			throw new NullPointerException();
    		}
    		this.context = context;
    		this.uiId = uiId;
    	}
    	
    	public ConfigurableWebApplicationContext getContext() {
    		return context;
    	}
    	
    	public int getUIId() {
    		return uiId;
    	}
    	
    	public boolean equals(Object o) {
    		if (o == this) return true;
    		if (!(o instanceof UIScopeKey)) return false;
    		final UIScopeKey other = (UIScopeKey) o;
    		return context.equals(other.context) && uiId == other.uiId;
    	}
    	
    	public int hashCode() {
    		int result = 3 * context.hashCode() + 11;
    		result += 7 * uiId;
    		return result;
    	}
    	
    }

// Bean holder class corresponding to a single Application instance

    private static class ApplicationBeanHolder {

        private final HashMap<String, Object> beans = new HashMap<String, Object>();
        private final HashMap<String, Runnable> destructionCallbacks = new HashMap<String, Runnable>();
        private final UIScopeKey key;

        public ApplicationBeanHolder(UIScopeKey key) {
            this.key = key;
        }

        public Object getBean(String name, ObjectFactory<?> objectFactory) {
            Object bean = this.beans.get(name);
            if (bean == null) {
                bean = objectFactory.getObject();
                this.beans.put(name, bean);
            }
            return bean;
        }

        public Object remove(String name) {
            this.destructionCallbacks.remove(name);
            return this.beans.remove(name);
        }

        public void registerDestructionCallback(String name, Runnable callback) {
            this.destructionCallbacks.put(name, callback);
        }

        public void close() {
            for (Runnable callback : this.destructionCallbacks.values())
                callback.run();
            this.beans.clear();
            this.destructionCallbacks.clear();
        }

        public boolean isEmpty() {
            return this.beans.isEmpty();
        }
    }
}

