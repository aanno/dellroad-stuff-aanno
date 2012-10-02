package org.dellroad.stuff.vaadin7;

import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public abstract class SpringUI extends UI {

	/**
     * Creates a new empty UI without a caption. This UI will have a
     * {@link VerticalLayout} with margins enabled as its content.
     */
     public SpringUI() {
    	super();
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
    }

	@Override
	protected final void init(VaadinRequest request) {
		final BaseSpringContextApplication app = BaseSpringContextApplication.get();
		app.initApplication(ContextApplication.currentRequest());
		initSpringUI(request, app.getApplicationContext(getUIId()));
	}

    /**
     * Initialize the application. Sub-classes of {@link BaseSpringContextApplication} must implement this method.
     *
     * @param context the associated {@link WebApplicationContext} just created and refreshed
     * @see #destroySpringApplication
     */
    protected abstract void initSpringUI(VaadinRequest request, ConfigurableWebApplicationContext context);

}