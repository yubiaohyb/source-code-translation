/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet.view;

import java.util.Locale;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.View;

/**
 * ViewResolver接口的实现类，根据指定的资源位置，专门配置视图定义的xml文件读取bean定义。
 * 文件通常位于WEB-INF目录下，默认为/WEB-INF/views.xml。
 * A {@link org.springframework.web.servlet.ViewResolver} implementation that uses
 * bean definitions in a dedicated XML file for view definitions, specified by
 * resource location. The file will typically be located in the WEB-INF directory;
 * the default is "/WEB-INF/views.xml".
 *
 * 当前实现类不支国际化，如果有需要，可以考虑使用ResourceBundleViewResolver。
 * <p>This {@code ViewResolver} does not support internationalization at the level
 * of its definition resources. Consider {@link ResourceBundleViewResolver} if you
 * need to apply different view resources per locale.
 *
 * 注意：该视图解析器实现了Ordered接口，用于灵活配置在视图解析器链中位置。
 * 例如，有些特殊的视图可以通过当前视图解析器定义（order值赋值为0），而剩余的视图会由UrlBasedViewResolver负责解析。
 * <p>Note: This {@code ViewResolver} implements the {@link Ordered} interface
 * in order to allow for flexible participation in {@code ViewResolver} chaining.
 * For example, some special views could be defined via this {@code ViewResolver}
 * (giving it 0 as "order" value), while all remaining views could be resolved by
 * a {@link UrlBasedViewResolver}.
 *
 * @author Juergen Hoeller
 * @since 18.06.2003
 * @see org.springframework.context.ApplicationContext#getResource
 * @see ResourceBundleViewResolver
 * @see UrlBasedViewResolver
 */
public class XmlViewResolver extends AbstractCachingViewResolver
		implements Ordered, InitializingBean, DisposableBean {

	/** Default if no other location is supplied */
	public static final String DEFAULT_LOCATION = "/WEB-INF/views.xml";


	@Nullable
	private Resource location;

	@Nullable
	private ConfigurableApplicationContext cachedFactory;

	private int order = Ordered.LOWEST_PRECEDENCE;  // default: same as non-Ordered


	/**
	 * Set the location of the XML file that defines the view beans.
	 * <p>The default is "/WEB-INF/views.xml".
	 * @param location the location of the XML file.
	 */
	public void setLocation(Resource location) {
		this.location = location;
	}

	/**
	 * Specify the order value for this ViewResolver bean.
	 * <p>The default value is {@code Ordered.LOWEST_PRECEDENCE}, meaning non-ordered.
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	/**
	 * 根据xml文件预初始化工厂。
	 * 只有缓存开启时才有用
	 * Pre-initialize the factory from the XML file.
	 * Only effective if caching is enabled.
	 */
	@Override
	public void afterPropertiesSet() throws BeansException {
		if (isCache()) {
			initFactory();
		}
	}


	/**
	 * 实现只是返回视图名，因为XmlViewResolver并不支持本地化解析。
	 * This implementation returns just the view name,
	 * as XmlViewResolver doesn't support localized resolution.
	 */
	@Override
	protected Object getCacheKey(String viewName, Locale locale) {
		return viewName;
	}

	@Override
	protected View loadView(String viewName, Locale locale) throws BeansException {
		BeanFactory factory = initFactory();
		try {
			return factory.getBean(viewName, View.class);
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Allow for ViewResolver chaining...
			return null;
		}
	}

	/**
	 * 根据xml文件初始化视图bean工厂
	 * 同步是因为并发访问
	 * Initialize the view bean factory from the XML file.
	 * Synchronized because of access by parallel threads.
	 * @throws BeansException in case of initialization errors
	 */
	protected synchronized BeanFactory initFactory() throws BeansException {
		if (this.cachedFactory != null) {
			return this.cachedFactory;
		}

		ApplicationContext applicationContext = obtainApplicationContext();

		Resource actualLocation = this.location;
		if (actualLocation == null) {
			actualLocation = applicationContext.getResource(DEFAULT_LOCATION);
		}

		// Create child ApplicationContext for views.
		GenericWebApplicationContext factory = new GenericWebApplicationContext();
		factory.setParent(applicationContext);
		factory.setServletContext(getServletContext());

		// Load XML resource with context-aware entity resolver.
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
		reader.setEnvironment(applicationContext.getEnvironment());
		reader.setEntityResolver(new ResourceEntityResolver(applicationContext));
		reader.loadBeanDefinitions(actualLocation);

		factory.refresh();

		if (isCache()) {
			this.cachedFactory = factory;
		}
		return factory;
	}


	/**
	 * 上下文停止时，关闭视图bean工厂
	 * Close the view bean factory on context shutdown.
	 */
	@Override
	public void destroy() throws BeansException {
		if (this.cachedFactory != null) {
			this.cachedFactory.close();
		}
	}

}
