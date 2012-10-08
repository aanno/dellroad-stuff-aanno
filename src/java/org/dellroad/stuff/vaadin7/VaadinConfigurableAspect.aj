
/*
 * Copyright (C) 2011 Archie L. Cobbs and other authors. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.stuff.vaadin7;

import java.io.ObjectStreamException;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.dellroad.stuff.spring.AbstractConfigurableAspect;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.aspectj.AbstractDependencyInjectionAspect;
import org.springframework.beans.factory.wiring.BeanConfigurerSupport;
import org.springframework.beans.factory.wiring.BeanWiringInfoResolver;
import org.springframework.web.context.ConfigurableWebApplicationContext;

/**
 * Aspect that autowires classes marked with the {@link VaadinConfigurable @VaadinConfigurable} annotation to a
 * {@link BaseSpringContextApplication} application context.
 *
 * <p>
 * This aspect does the same thing that Spring's {@code AnnotationBeanConfigurerAspect} aspect does,
 * except that this aspect configures beans using the application context associated with the current
 * {@link BaseSpringContextApplication} Vaadin application rather than the one associated with the overall
 * servlet context (i.e., its parent).
 * </p>
 *
 * <p>
 * As a result, objects so annotated will be configured for their specific {@link BaseSpringContextApplication}
 * instance, and therefore can only be instantiated by threads associated with a current {@link BaseSpringContextApplication}
 * (see {@link BaseContextApplication#get}). This will be the case when executing within a {@link BaseSpringContextApplication}
 * Vaadin application HTTP request or an invocation of {@link BaseContextApplication#invoke} on same.
 * </p>
 *
 * <p>
 * This implementation is derived from Spring's {@code AnnotationBeanConfigurerAspect} implementation
 * and therefore shares its license (also Apache).
 * </p>
 *
 * @see BaseSpringContextApplication
 * @see BaseContextApplication#get
 * @see VaadinConfigurable
 */
public aspect VaadinConfigurableAspect extends AbstractConfigurableAspect {

// Stuff copied from AbstractInterfaceDrivenDependencyInjectionAspect.aj

    public pointcut beanConstruction(Object bean) :
        initialization(VaadinConfigurableObject+.new(..)) && this(bean);

    public pointcut beanDeserialization(Object bean) :
        execution(Object VaadinConfigurableDeserializationSupport+.readResolve()) &&
        this(bean);

    public pointcut leastSpecificSuperTypeConstruction() : initialization(VaadinConfigurableObject.new(..));

    declare parents: 
        VaadinConfigurableObject+ && Serializable+ implements VaadinConfigurableDeserializationSupport;

    static interface VaadinConfigurableDeserializationSupport extends Serializable {
    }

    public Object VaadinConfigurableDeserializationSupport.readResolve() throws ObjectStreamException {
        return this;
    }

// Stuff copied from AnnotationBeanConfigurerAspect.aj

    public pointcut inConfigurableBean() : @this(VaadinConfigurable);

    public pointcut preConstructionConfiguration() : preConstructionConfigurationSupport(*);

    declare parents: @VaadinConfigurable * implements VaadinConfigurableObject;

    private pointcut preConstructionConfigurationSupport(VaadinConfigurable c) : @this(c) && if(c.preConstruction());

	declare parents: @VaadinConfigurable Serializable+ implements VaadinConfigurableDeserializationSupport;

// Our implementation

    @Override
    protected BeanFactory getBeanFactory(Object bean) {

        // Get application context
        // ConfigurableWebApplicationContext context = BaseSpringContextApplication.get().getApplicationContext();
        // ConfigurableWebApplicationContext context = BaseSpringContextApplication.get().getApplicationContext(
        // 		BaseSpringContextApplication.getCurrentUI().getUIId());
    	final BaseSpringContextApplication bsca = BaseSpringContextApplication.get();
        ConfigurableWebApplicationContext context = bsca.getCurrentlyConstructedContext();
        if (context == null) {
        	final HttpServletRequest request = BaseSpringContextApplication.currentRequest();
        	bsca.initSession(request);
        	context = bsca.getApplicationContext(SessionIdUtils.retrieveSessionId(request.getSession()));
        	assert context != null;
        }
    	
        // Logging
        if (this.log.isTraceEnabled())
            this.log.trace("using application context " + context + " to configure @VaadinConfigurable bean " + bean);

        // Return associated bean factory
        return context.getBeanFactory();
    }

    @Override
    protected BeanWiringInfoResolver getBeanWiringInfoResolver(Object bean) {
        return new VaadinConfigurableBeanWiringInfoResolver();
    }
}

