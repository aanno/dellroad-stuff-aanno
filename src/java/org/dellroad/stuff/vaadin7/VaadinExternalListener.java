
/*
 * Copyright (C) 2011 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.stuff.vaadin7;


/**
 * Support superclass customized for use by listeners that are part of a Vaadin application when listening
 * to non-Vaadin ("external") event sources.
 *
 * <p>
 * Listeners that are part of a Vaadin application should use this superclass if they are going to be registered
 * with non-Vaadin event sources, and use the methods {@link #register()} and {@link #unregister()} to control listener
 * registration. Subclasses implement {@link #register(Object) register(S)} and {@link #unregister(Object) register(S)}
 * to perform the actual listener registration/unregister operations.
 *
 * <p>
 * Use of this class will ensure two things:
 * <ul>
 *  <li>Events can be delivered {@linkplain BaseContextApplication#invoke in the proper Vaadin application context}; and</li>
 *  <li>The listener is automatically unregistered from the external event source when the Vaadin application is closed;
 *      this avoids a memory leak</li>
 * </ul>
 * </p>
 *
 * <p>
 * Subclass listener methods should use {@link #handleEvent handleEvent()} to handle events.
 * </p>
 *
 * <p>
 * Note: when listening to event sources that are scoped to specific Vaadin application instances and already originate events
 * within the proper Vaadin application context (i.e., event sources that are not external to the Vaadin application),
 * then the use of this superclass is not necessary (however, it also doesn't hurt to use it anyway).
 * </p>
 *
 * @param <S> The type of the event source
 * @see BaseContextApplication#invoke
 * @see VaadinApplicationScope
 * @see VaadinApplicationListener
 * @see BaseSpringContextApplication
 */
public abstract class VaadinExternalListener<S> {

    private final S eventSource;
    private final BaseContextApplication application;
    private final CloseListener closeListener = new CloseListener();

    /**
     * Convenience constructor. Equivalent to:
     * <blockquote>
     *  {@link #VaadinExternalListener(Object, BaseContextApplication) VaadinExternalListener(eventSource, ContextApplication.get())}
     * </blockquote>
     *
     * @param eventSource the event source on which this listener will register
     * @throws IllegalArgumentException if {@code eventSource} is null
     * @throws IllegalStateException if there is no {@link BaseContextApplication} associated with the current thread
     */
    protected VaadinExternalListener(S eventSource) {
        this(eventSource, BaseContextApplication.get());
    }

    /**
     * Primary constructor.
     *
     * @param eventSource the event source on which this listener will register
     * @param application the associated Vaadin application
     * @throws IllegalArgumentException if either parameter is null
     */
    protected VaadinExternalListener(S eventSource, BaseContextApplication application) {
        if (eventSource == null)
            throw new IllegalArgumentException("null eventSource");
        if (application == null)
            throw new IllegalArgumentException("null application");
        this.eventSource = eventSource;
        this.application = application;
    }

    /**
     * Register as a listener on configured event source.
     *
     * <p>
     * This also registers a {@link BaseContextApplication.CloseListener} on the
     * {@linkplain #getApplication configured Vaadin application} so that when the application
     * closes we can unregister this instance from the event source to avoid a memory leak.
     */
    public void register() {
        this.application.addListener(this.closeListener);
        this.register(this.eventSource);
    }

    /**
     * Un-register as a listener on configured event source.
     *
     * <p>
     * This also unregisters the {@link BaseContextApplication.CloseListener} registered by {@link #register}.
     */
    public void unregister() {
        this.application.removeListener(this.closeListener);
        this.unregister(this.eventSource);
    }

    /**
     * Get the {@link BaseContextApplication} with which this instance is associated.
     */
    public final BaseContextApplication getApplication() {
        return this.application;
    }

    /**
     * Get the event source with which this instance is (or was) registered as a listener.
     */
    public final S getEventSource() {
        return this.eventSource;
    }

    /**
     * Execute the given action using the {@link BaseContextApplication} with which this instance is associated.
     * Subclass listener methods should handle events using this method to ensure they are handled
     * {@linkplain BaseContextApplication#invoke in the proper Vaadin application context}.
     *
     * @param action action to perform
     */
    protected void handleEvent(Runnable action) {
        this.getApplication().invoke(action);
    }

    /**
     * Register as a listener on the given event source.
     *
     * <p>
     * Subclass must implement this to perform the actual listener registration.
     *
     * @param eventSource event source, never null
     */
    protected abstract void register(S eventSource);

    /**
     * Register as a listener from the given event source.
     *
     * <p>
     * Subclass must implement this to perform the actual listener registration.
     *
     * @param eventSource event source, never null
     */
    protected abstract void unregister(S eventSource);

// Application close listener

    private final class CloseListener implements BaseContextApplication.CloseListener {

        @Override
        public void applicationClosed(BaseContextApplication.CloseEvent closeEvent) {
            VaadinExternalListener.this.unregister();
        }
    }
}

