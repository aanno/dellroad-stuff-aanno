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
	
	private ConfigurableWebApplicationContext context;

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

    /**
     * Creates a new empty UI with the given caption. This UI will have a
     * {@link VerticalLayout} with margins enabled as its content.
     * 
     * @param caption
     *            the caption of the UI, used as the page title if there's
     *            nothing but the application on the web page
     * 
     * @see #setCaption(String)
     */
    public SpringUI(String caption) {
    	super(caption);
    	UI.setCurrent(this);
    }

    /**
     * Creates a new UI with the given caption and content.
     * 
     * @param caption
     *            the caption of the UI, used as the page title if there's
     *            nothing but the application on the web page
     * @param content
     *            the content container to use as this UIs content.
     * 
     * @see #setContent(ComponentContainer)
     * @see #setCaption(String)
     */
    public SpringUI(String caption, ComponentContainer content) {
    	super(caption, content);
    	UI.setCurrent(this);
    }

	@Override
	protected final void init(VaadinRequest request) {
		final BaseSpringContextApplication app = BaseSpringContextApplication.get();
		app.initApplication(request);
		final String so = SessionIdUtils.retrieveSessionId(request.getWrappedSession());
		initSpringUI(request, app.getApplicationContext(so));
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
    	return context;
    }

}
