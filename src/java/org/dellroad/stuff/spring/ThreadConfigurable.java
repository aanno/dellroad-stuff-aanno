
/*
 * Copyright (C) 2011 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.stuff.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Autowire;

/**
 * Indicates that the class is a candidate for configuration using the {@code ThreadConfigurableAspect} aspect.
 *
 * <p>
 * Works just like Spring's {@link org.springframework.beans.factory.annotation.Configurable @Configurable} annotation,
 * but whereas {@link org.springframework.beans.factory.annotation.Configurable @Configurable} autowires using a fixed
 * bean factory stored in a static variable, {@link ThreadConfigurable @ThreadConfigurable} autowires using a bean
 * factory that is configurable on a per-thread basis. This allows the same
 * {@link ThreadConfigurable @ThreadConfigurable}-annotated beans to be instantiated and autowired by different
 * application contexts at the same time, where the application context chosen depends on the thread in which
 * the instantiation is occurring. This is useful when setting up "parallel universes" within the same application.
 * </p>
 *
 * <p>
 * The configuring bean factory is determined by the singleton {@link ThreadLocalBeanFactory} instance, i.e., the value
 * returned by invoking <code>{@link ThreadLocalBeanFactory#getInstance}.{@link ThreadLocalBeanFactory#get get()}</code>.
 * Note that {@link ThreadConfigurable @ThreadConfigurable} requires that the {@link org.springframework.beans.factory.BeanFactory}
 * configured in the {@link ThreadLocalBeanFactory} actually be a
 * {@link org.springframework.beans.factory.config.ConfigurableListableBeanFactory}.
 * </p>
 *
 * <p>
 * With {@link ThreadLocalBeanFactory} the configured bean factory is inherited by spawned child threads,
 * so typically this configuration need only be done once when starting new some process or operation,
 * even if that operation creates multiple threads.
 * </p>
 *
 * <p>
 * For example:
 *  <blockquote><pre>
 *  new Thread("context startup") {
 *      &#64;Override
 *      public void run() {
 *
 *          // Setup the context used by &#64;ThreadConfigurable beans created in this thread;
 *          // to be safe, configure the ThreadLocalBeanFactory before refreshing the context.
 *          ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(
 *            new String[] { "applicationContext.xml" }, false);
 *          ThreadLocalBeanFactory.getInstance().set(context.getBeanFactory());
 *          context.refresh();
 *
 *          // Now &#64;ThreadConfigurable beans will use "context" for autowiring, but only
 *          // if they are created in this thread (or one of its child threads).
 *          new SomeThreadConfigurableBean() ...
 *      }
 *  }.start();
 *  </pre></blockquote>
 *
 * <p>
 * Note: to make this annotation behave like Spring's
 * {@link org.springframework.beans.factory.annotation.Configurable @Configurable} annotation, simply include the
 * {@link ThreadLocalBeanFactory} singleton instance in your bean factory:
 * <blockquote><pre>
 *     &lt;bean class="org.dellroad.stuff.spring.ThreadLocalBeanFactory" factory-method="getInstance"/&gt;
 * </pre></blockquote>
 * This will set the containing bean factory as the default. This definition should be listed prior to any other
 * bean definitions that could result in {@link ThreadConfigurable @ThreadConfigurable}-annotated beans being
 * created during bean factory startup.
 * </p>
 *
 * <p>
 * Note: if a {@link ThreadConfigurable @ThreadConfigurable}-annotated bean is constructed and no bean factory
 * has been configured for the current thread, there is no default set either, then no configuration is performed
 * and a debug message is logged (to logger {@code org.dellroad.stuff.spring.ThreadConfigurableAspect}); this consistent
 * with the behavior of Spring's {@link org.springframework.beans.factory.annotation.Configurable @Configurable}.
 * </p>
 *
 * @see ThreadLocalBeanFactory
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface ThreadConfigurable {

    /**
     * Configuration bean definition template name, if any.
     */
    String value() default "";

    /**
     * Whether and how to automatically autowire dependencies.
     */
    Autowire autowire() default Autowire.NO;

    /**
     * Whether to enable dependency checking.
     */
    boolean dependencyCheck() default false;

    /**
     * Whether to inject dependencies prior to constructor execution.
     */
    boolean preConstruction() default false;
}

