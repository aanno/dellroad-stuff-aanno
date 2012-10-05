
/*
 * Copyright (C) 2011 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.stuff.vaadin7;

import javax.servlet.http.HttpServletRequest;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServiceSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

/**
 * Vaadin 7 version of {@link org.dellroad.stuff.vaadin7.BaseContextApplication}.
 *
 * @see org.dellroad.stuff.vaadin7.BaseContextApplication
 */
@SuppressWarnings("serial")
public class ContextApplication extends org.dellroad.stuff.vaadin7.BaseContextApplication {

    /**
     * Initialize the application. In Vaadin 7 overriding this method is optional.
     *
     * <p>
     * The implementation in {@link ContextApplication} does nothing.
     */
    @Override
    protected void initApplication(VaadinRequest request) {
    }
    
    @Override
    public void close() {
    }

// Error handling

    @Override
    public void showError(String title, String description) {
        ContextApplication.showError(UI.getCurrent(), this.getNotificationDelay(), title, description);
    }

    static void showError(UI root, int notificationDelay, String title, String description) {
        // Show error
        Notification notification = new Notification(title, description, Notification.TYPE_ERROR_MESSAGE);
        notification.setStyleName("warning");
        notification.setDelayMsec(notificationDelay);
        notification.show(root.getPage());
    }

    @Override
    public void invoke(Runnable action) {
        final VaadinServiceSession previous = VaadinServiceSession.getCurrent();
        /*
        if (previous != null && previous != this)
            throw new IllegalStateException("there is already a current application for this thread (according to Vaadin)");
        VaadinSession.setCurrent(this);
         */
        checkSession(previous);
        try {
            super.invoke(action);
        } finally {
        	VaadinServiceSession.setCurrent(previous);
        }
    }
}

