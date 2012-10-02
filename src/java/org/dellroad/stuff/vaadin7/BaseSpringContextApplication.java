
/*
 * Copyright (C) 2011 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.stuff.vaadin7;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dellroad.stuff.vaadin.ContextApplication.CloseListener;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;
import com.vaadin.ui.UI.CleanupEvent;

/**
 * Vaadin application implementation that manages an associated Spring {@link WebApplicationContext}.
 *
 * <h3>Overview</h3>
 *
 * <p>
 * Each Vaadin application instance is given its own Spring application context, and all such
 * application contexts share the same parent context, which is the one associated with the overal servlet web context
 * (i.e., the one created by Spring's {@link org.springframework.web.context.ContextLoaderListener ContextLoaderListener}).
 * A context is created when a new Vaadin application instance is initialized, and destroyed when it is closed.
 * </p>
 *
 * <p>
 * This setup is analogous to how Spring's {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlet}
 * creates per-servlet application contexts that are children of the overall servlet web context.
 * </p>
 *
 * <h3>Application Context XML File Location</h3>
 *
 * <p>
 * For each Vaadin application {@code com.example.FooApplication} that subclasses this class, there should exist an XML
 * file named {@code FooApplication.xml} in the {@code WEB-INF/} directory that defines the per-Vaadin application Spring
 * application context. This naming scheme {@linkplain #getApplicationName can be overriden}, or explicit config file
 * location(s) can be specified by setting the {@link #VAADIN_CONTEXT_LOCATION_PARAMETER} servlet parameter.
 * </p>
 *
 * <h3>Vaadin Application as BeanFactory singleton</h3>
 *
 * <p>
 * This {@link BaseSpringContextApplication} instance can itself be exposed in, and configured by, the associated Spring
 * application context. Simply create a bean definition that invokes {@link BaseContextApplication#get}:
 * <blockquote><pre>
 *  &lt;bean id="myVaadinApplication" class="org.dellroad.stuff.vaadin.ContextApplication" factory-method="get"/&gt;
 * </pre></blockquote>
 *
 * <p>
 * This then allows you to autowire the {@link BaseSpringContextApplication} and other UI components together, e.g.:
 * <blockquote><pre>
 *  public class MyApplication extends SpringContextApplication {
 *
 *      &#64;Autowired
 *      private MainPanel mainPanel;
 *
 *      &#64;Override
 *      public void initSpringApplication(ConfigurableWebApplicationContext context) {
 *          Window mainWindow = new Window("MyApplication", this.mainPanel);
 *          this.setMainWindow(mainWindow);
 *      }
 *
 *      ...
 *  }
 *
 *  &#64;Component
 *  public class MainPanel extends VerticalLayout {
 *
 *      &#64;Autowired
 *      private MyApplication application;
 *
 *      ...
 *  }
 * </pre></blockquote>
 * </p>
 *
 * <p>
 * Even if you don't explicitly define the {@link BaseSpringContextApplication} bean in your Spring application context,
 * it will still be available as a dependency for autowiring into other beans (this is accomplished using
 * {@link ConfigurableListableBeanFactory#registerResolvableDependency
 * ConfigurableListableBeanFactory.registerResolvableDependency()}). Of course, in this case the
 * {@link BaseSpringContextApplication} bean won't itself be autowired or configured.
 * </p>
 *
 * <h3><code>@VaadinConfigurable</code> Beans</h3>
 *
 * <p>
 * It is also possible to configure beans outside of this application context using AOP, so that any invocation of
 * {@code new FooBar()}, where the class {@code FooBar} is marked {@link VaadinConfigurable @VaadinConfigurable},
 * will automagically cause the new {@code FooBar} object to be configured by the application context associated with
 * the {@linkplain BaseContextApplication#get() currently running application instance}. In effect, this does for
 * Vaadin application beans what Spring's {@link org.springframework.beans.factory.annotation.Configurable @Configurable}
 * does for regular beans.
 * </p>
 *
 * <p>
 * Note however that Spring {@linkplain org.springframework.beans.factory.DisposableBean#destroy destroy methods}
 * will not be invoked on application close for these beans, since their lifecycle is controlled outside of the
 * Spring application context (this is also the case with
 * {@link org.springframework.beans.factory.annotation.Configurable @Configurable} beans). Instead, these beans
 * can register as a {@link BaseContextApplication.CloseListener} for shutdown notification.
 * </p>
 *
 * <p>
 * For the this annotation to do anything, {@link VaadinConfigurable @VaadinConfigurable} classes must be woven
 * (either at build time or runtime) using the
 * <a href="http://www.eclipse.org/aspectj/doc/released/faq.php#compiler">AspectJ compiler</a> with the
 * {@code VaadinConfigurableAspect} aspect (included in the <code>dellroad-stuff</code> JAR file).
 * </p>
 *
 * @see BaseContextApplication#get
 * @see <a href="https://github.com/archiecobbs/dellroad-stuff-vaadin-spring-demo3">Example Code on GitHub</a>
 */
@SuppressWarnings("serial")
public abstract class BaseSpringContextApplication extends BaseContextApplication {

    /**
     * Optional servlet initialization parameter (<code>{@value #VAADIN_CONTEXT_LOCATION_PARAMETER}</code>) used to specify
     * the location(s) of the application context XML file(s). Multiple XML files may be separated by whitespace.
     * If this parameter is not defined, then the XML file location is built using {@code /WEB-INF/} as a prefix,
     * the string from {@link #getApplicationName}, and {@code .xml} as a suffix.
     */
    public static final String VAADIN_CONTEXT_LOCATION_PARAMETER = "vaadinContextConfigLocation";
    
    public static final String VAADIN_APPLICATION = "application";

    private static final AtomicLong UNIQUE_INDEX = new AtomicLong();

    /**
     * UI id -> ConfigurableWebApplicationContext.
     */
    private transient Map<Integer,ConfigurableWebApplicationContext> uiId2Context = 
    		new ConcurrentHashMap<Integer, ConfigurableWebApplicationContext>();
    
    public BaseSpringContextApplication() {
    }

    @Override
    protected final void doOnRequestStart(HttpServletRequest request, HttpServletResponse response) {
    	
    }

    @Override
    protected final void doOnRequestEnd(HttpServletRequest request, HttpServletResponse response) {
    }
    
    private void setApplicationContext(int uiId, ConfigurableWebApplicationContext context) {
    	uiId2Context.put(uiId, context);
    }
    
    /**
     * Get this instance's associated Spring application context.
     */
    public ConfigurableWebApplicationContext getApplicationContext(int uiId) {
        return uiId2Context.get(uiId2Context);
    }

    /**
     * Get this instance's associated Spring application context.
     */
    public ConfigurableWebApplicationContext removeApplicationContext(int uiId) {
        return uiId2Context.remove(uiId2Context);
    }

    /**
     * Get the {@link BaseSpringContextApplication} instance associated with the current thread or throw an exception if there is none.
     *
     * <p>
     * Works just like {@link BaseContextApplication#get()} but returns this narrower type.
     * </p>
     *
     * @return the {@link BaseSpringContextApplication} associated with the current thread
     * @throws IllegalStateException if the current thread is not servicing a Vaadin web request
     *  or the current Vaadin {@link com.vaadin.Application} is not a {@link BaseSpringContextApplication}
     */
    public static BaseSpringContextApplication get() {
        return BaseContextApplication.get(BaseSpringContextApplication.class);
    }

    /**
     * Initializes the associated {@link ConfigurableWebApplicationContext}.
     *
     * <p>
     * After initializing the associated Spring application context, this method delegates to {@link #initSpringApplication}.
     */
    @Override
    protected final void initApplication(HttpServletRequest request) {
    	final Integer uiId = getUIId(request);
    	if (uiId != null) {
    		ConfigurableWebApplicationContext context = uiId2Context.get(uiId);
    		if (context != null) {
    			// context already initialized
    			return;
    		}
    	
    		// Load the context
            context = this.loadContext();
            
            // Initialize subclass
            this.initSpringApplication(context);
            
    		setApplicationContext(uiId, context);
    	
    		// Get notified of context shutdown so we can shut down the context as well
    		final ContextCloseListener listener = new ContextCloseListener(uiId);
    		this.addListener(listener);
    		
    		// Get notified of UI shutdown so we can shut down the context as well
    		final UI ui = getUI(request, uiId);
    		ui.addCleanupListener(listener);
    		
            log.info("context created: " + context + "for uiId " + uiId 
            		+ ", new UI-context size: " + uiId2Context.size());
            return;
    	}
    }

    /**
     * Initialize the application. Sub-classes of {@link BaseSpringContextApplication} must implement this method.
     *
     * @param context the associated {@link WebApplicationContext} just created and refreshed
     * @see #destroySpringApplication
     */
    protected abstract void initSpringApplication(ConfigurableWebApplicationContext context);

    /**
     * Close this instance.
     *
     * <p>
     * The implementation in {@link BaseContextApplication} first delegates to the superclass and then
     * notifies any registered {@link CloseListener}s.
     * </p>
     */
    @Override
    public void close() {
    	int uiId = getUIId();
    	close(uiId);
    }
    
    private void close(int uiId) {
    	removeApplicationContext(uiId);
        log.info("closing application context associated with Vaadin ui "  + " " + uiId
                + " " + BaseSpringContextApplication.this.getApplicationName());
        final ConfigurableWebApplicationContext context = removeApplicationContext(uiId);
        if (context != null) {
        	context.close();
        }
        log.info("UI-context size: " + uiId2Context.size());
        destroySpringApplication();
    }
    
    /**
     * Perform any application-specific shutdown work. This will be invoked at shutdown after this Vaadin application and the
     * associated {@link WebApplicationContext} have both been closed.
     *
     * <p>
     * The implementation in {@link BaseSpringContextApplication} does nothing. Subclasses may override as necessary.
     * </p>
     *
     * <p>
     * Note that if a {@link BaseSpringContextApplication} instance is exposed in the application context and configured
     * with a Spring {@linkplain org.springframework.beans.factory.DisposableBean#destroy destroy method}, then that
     * method will also be invoked when the application is closed. In such cases overriding this method is not necessary.
     * </p>
     *
     * @see #initSpringApplication
     */
    protected void destroySpringApplication() {
    }

    /**
     * Post-process the given {@link WebApplicationContext} after initial creation but before the initial
     * {@link org.springframework.context.ConfigurableApplicationContext#refresh refresh()}.
     *
     * <p>
     * The implementation in {@link BaseSpringContextApplication} does nothing. Subclasses may override as necessary.
     * </p>
     *
     * @param context the associated {@link WebApplicationContext} just refreshed
     * @see #onRefresh
     * @see ConfigurableWebApplicationContext#refresh()
     */
    protected void postProcessWebApplicationContext(ConfigurableWebApplicationContext context) {
    }

    /**
     * Perform any application-specific work after a successful application context refresh.
     *
     * <p>
     * The implementation in {@link BaseSpringContextApplication} does nothing. Subclasses may override as necessary.
     * </p>
     *
     * @param context the associated {@link WebApplicationContext} just refreshed
     * @see #postProcessWebApplicationContext
     * @see org.springframework.context.ConfigurableApplicationContext#refresh
     */
    protected void onRefresh(ApplicationContext context) {
    }

    /**
     * Get the name for this application. This is used as the name of the XML file in {@code WEB-INF/} that
     * defines the Spring application context associated with this instance, unless the
     * {@link #VAADIN_CONTEXT_LOCATION_PARAMETER} servlet parameter is set.
     *
     * <p>
     * The implementation in {@link BaseSpringContextApplication} returns this instance's class'
     * {@linkplain Class#getSimpleName simple name}.
     * </p>
     */
    protected String getApplicationName() {
        return this.getClass().getSimpleName();
    }

// ApplicationContext setup

    private ConfigurableWebApplicationContext loadContext() {
    	ConfigurableWebApplicationContext context;
    	
        // Logging
        this.log.info("loading application context for Vaadin application " + this.getApplicationName());

        // Find the application context associated with the servlet; it will be the parent
        ServletContext servletContext;
        HttpServletRequest request = BaseContextApplication.currentRequest();
        try {
            // getServletContext() is a servlet AIP 3.0 method, so don't freak out if it's not there
            servletContext = (ServletContext)HttpServletRequest.class.getMethod("getServletContext").invoke(request);
        } catch (Exception e) {
            servletContext = ContextLoader.getCurrentWebApplicationContext().getServletContext();
        }
        WebApplicationContext parent = WebApplicationContextUtils.getWebApplicationContext(servletContext);

        // Create and configure a new application context for this Application instance
        context = new XmlWebApplicationContext();
        context.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX
          + servletContext.getContextPath() + "/" + this.getApplicationName() + "-"
          + BaseSpringContextApplication.UNIQUE_INDEX.incrementAndGet());
        context.setParent(parent);
        context.setServletContext(servletContext);
        //context.setServletConfig(??);
        context.setNamespace(this.getApplicationName());

        // Set explicit config location(s) if set by web.xml init-param
        String configLocationValue = this.getInitParameter(VAADIN_CONTEXT_LOCATION_PARAMETER);
        if (configLocationValue == null) {
        	configLocationValue = this.getInitParameter(VAADIN_APPLICATION);
        }
        if (configLocationValue == null) {
        	configLocationValue = getApplicationName();
        }
        // Set config location from application name (if there is no web.xml init-param
        if (configLocationValue != null) {
            context.setConfigLocation(configLocationValue);
        }

        // Register listener so we can notify subclass on refresh events
        context.addApplicationListener(new SourceFilteringListener(context, new RefreshListener()));

        // Register this instance as an implicitly resolvable dependency
        context.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {
            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
                beanFactory.registerResolvableDependency(VaadinServlet.class, BaseSpringContextApplication.this);
            }
        });

        // Invoke any subclass setup
        this.postProcessWebApplicationContext(context);

        // Refresh context
        context.refresh();

        return context;
    }

// Serialization

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject();
        final Set<Integer> uiIds = (Set<Integer>) input.readObject();
        for (Integer uiId: uiIds) {
        	final ConfigurableWebApplicationContext context = loadContext();
        	setApplicationContext(uiId, context);
        }
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
    	out.defaultWriteObject();
    	out.writeObject(new HashSet<Integer>(uiId2Context.keySet()));
    }

// Nested classes

    // My refresh listener
    private class RefreshListener implements ApplicationListener<ContextRefreshedEvent>, Serializable {
        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            BaseSpringContextApplication.this.onRefresh(event.getApplicationContext());
        }
    }

    // My close listener
    private class ContextCloseListener implements CloseListener, UI.CleanupListener, Serializable, Cloneable {
    	
    	private final int uiId;
    	
    	private ContextCloseListener(int uiId) {
    		this.uiId = uiId;
    	}
    	
        @Override
        public void applicationClosed(CloseEvent closeEvent) {
        	log.info("close for uiId " + uiId + " triggered by context close");
        	close(uiId);
        }
        
		@Override
		public void cleanup(CleanupEvent event) {
        	log.info("close for uiId " + uiId + " triggered by UI cleanup");
			close(uiId);
		}
		
    }
}
