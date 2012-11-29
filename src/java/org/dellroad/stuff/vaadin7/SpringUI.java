package org.dellroad.stuff.vaadin7;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@VaadinConfigurable
public abstract class SpringUI extends UI implements ApplicationContextAware {
	
	private transient ConfigurableWebApplicationContext context;

	/**
     * Creates a new empty UI without a caption. This UI will have a
     * {@link VerticalLayout} with margins enabled as its content.
     */
     public SpringUI() {
    	super();
    	UI.setCurrent(this);
    }

    /**
     * Creates a new UI with the given component container as its content.
     * 
     * @param content
     *            the content container to use as this UIs content.
     * 
     * @see #setContent(ComponentContainer)
     */
    public SpringUI(ComponentContainer content) {
    	super(content);
    	UI.setCurrent(this);
    }

	@Override
	protected final void init(VaadinRequest request) {
		initSpringUI(request, SpringVaadinSession.getApplicationContext());
	}

    /**
     * Initialize the application. Sub-classes of {@link BaseSpringContextApplication} must implement this method.
     *
     * @param context the associated {@link WebApplicationContext} just created and refreshed
     * @see #destroySpringApplication
     */
    protected abstract void initSpringUI(VaadinRequest request, ConfigurableWebApplicationContext context);
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    	this.context = (ConfigurableWebApplicationContext) applicationContext;
    }
    
    public ConfigurableWebApplicationContext getContext() {
        if (context == null) {
            context = SpringVaadinSession.getApplicationContext();
        }
    	return context;
    }

}
