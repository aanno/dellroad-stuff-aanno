package org.dellroad.stuff.vaadin7.mvp;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.web.context.ConfigurableWebApplicationContext;

public class UIEventBus implements ApplicationEventMulticaster, ApplicationContextAware {
	
	private final ApplicationEventMulticaster wrapped = new SimpleApplicationEventMulticaster();
	
	private ConfigurableWebApplicationContext context;
	
	public UIEventBus() {
	}

	@Override
	public void addApplicationListener(ApplicationListener listener) {
		wrapped.addApplicationListener(listener);
	}

	@Override
	public void addApplicationListenerBean(String listenerBeanName) {
		wrapped.addApplicationListenerBean(listenerBeanName);
	}

	@Override
	public void removeApplicationListener(ApplicationListener listener) {
		wrapped.removeApplicationListener(listener);
	}

	@Override
	public void removeApplicationListenerBean(String listenerBeanName) {
		wrapped.removeApplicationListenerBean(listenerBeanName);
	}

	@Override
	public void removeAllListeners() {
		wrapped.removeAllListeners();
	}

	@Override
	public void multicastEvent(ApplicationEvent event) {
		wrapped.multicastEvent(event);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = (ConfigurableWebApplicationContext) applicationContext;
	}

}
