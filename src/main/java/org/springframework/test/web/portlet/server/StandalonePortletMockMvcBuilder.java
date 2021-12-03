package org.springframework.test.web.portlet.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.portlet.HandlerInterceptor;
import org.springframework.web.portlet.mvc.annotation.AnnotationMethodHandlerAdapter;
import org.springframework.web.portlet.mvc.annotation.DefaultAnnotationHandlerMapping;

public class StandalonePortletMockMvcBuilder extends DefaultPortletMockMvcBuilder {
	
	private Object[] controllers;
	
	private Validator validator;
	
	private List<HandlerInterceptor> interceptors = new ArrayList<HandlerInterceptor>();

	private ConversionService conversionService;
	
	public StandalonePortletMockMvcBuilder(Object... controllers) {
		Assert.isTrue(!ObjectUtils.isEmpty(controllers), "At least one controller is required");
		this.controllers = controllers;
	}
	
	public StandalonePortletMockMvcBuilder setValidator(Validator validator) {
		this.validator = validator;
		return this;
	}
	
	public StandalonePortletMockMvcBuilder addInteceptors(HandlerInterceptor... interceptors) {
		this.interceptors.addAll(Arrays.asList(interceptors));
		return this;
	}

	public StandalonePortletMockMvcBuilder setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
		return this;
	}

	protected ApplicationContext initApplicationContext() {
		GenericWebApplicationContext wac = new GenericWebApplicationContext();

		addControllers(wac);
		addHandlerInterceptors(wac);

		RootBeanDefinition mappingDef = new RootBeanDefinition(DefaultAnnotationHandlerMapping.class);
		mappingDef.getPropertyValues().add("interceptors", interceptors);
		wac.registerBeanDefinition("handlerMapping", mappingDef);

		RootBeanDefinition adapterDef = new RootBeanDefinition(AnnotationMethodHandlerAdapter.class);

		if (this.validator != null || this.conversionService != null) {
			ConfigurableWebBindingInitializer webBindingInitializer = new ConfigurableWebBindingInitializer();
			if (this.validator != null) {
				webBindingInitializer.setValidator(this.validator);
			}
			if (this.conversionService != null) {
				webBindingInitializer.setConversionService(this.conversionService);
			}
			adapterDef.getPropertyValues().add("webBindingInitializer", webBindingInitializer);
		}

		wac.registerBeanDefinition("handlerAdapter", adapterDef);

		AnnotationConfigUtils.registerAnnotationConfigProcessors(wac);

		wac.refresh();

		return wac;
	}
	
	private void addHandlerInterceptors(GenericWebApplicationContext wac) {
		for (int i = 0; i < interceptors.size(); i++) {
			wac.getBeanFactory().registerSingleton("interceptor" + i, interceptors.get(i));
		}
	}
	
	private void addControllers(GenericWebApplicationContext wac) {
		for (int i = 0; i < controllers.length; i++) {
			wac.getBeanFactory().registerSingleton("controller" + i, controllers[i]);
		}
	}
	
	public PortletMockMvc build() {
		setApplicationContext(initApplicationContext());
		
		return super.build();
	}

}
