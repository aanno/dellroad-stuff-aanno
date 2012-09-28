
/*
 * Copyright (C) 2011 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.stuff.vaadin7;

import com.vaadin.Application;
import com.vaadin.server.VaadinSession;

import org.springframework.web.context.ConfigurableWebApplicationContext;

/**
 * Vaadin 7 version of {@link org.dellroad.stuff.vaadin7.BaseSpringContextApplication}.
 *
 * @see org.dellroad.stuff.vaadin7.BaseSpringContextApplication
 */
@SuppressWarnings("serial")
public class SpringContextApplication extends org.dellroad.stuff.vaadin7.BaseSpringContextApplication {

    /**
     * Initialize the application. In Vaadin 7 overriding this method is optional.
     *
     * <p>
     * The implementation in {@link ContextApplication} does nothing.
     */
    @Override
    protected void initSpringApplication(ConfigurableWebApplicationContext context) {
    }

// Error handling

    @Override
    public void showError(String title, String description) {
        ContextApplication.showError(this.getRoots(), this.getNotificationDelay(), title, description);
    }

    @Override
    public void invoke(Runnable action) {
        final VaadinSession previous = VaadinSession.getCurrent();
        if (previous != null && previous != this)
            throw new IllegalStateException("there is already a current application for this thread (according to Vaadin)");
        VaadinSession.setCurrent(this);
        try {
            super.invoke(action);
        } finally {
        	VaadinSession.setCurrent(previous);
        }
    }
}

