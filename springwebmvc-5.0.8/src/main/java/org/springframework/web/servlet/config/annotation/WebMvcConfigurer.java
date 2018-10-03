/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.web.servlet.config.annotation;

import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * 定义回调接口来为spring MVC自定义基于java的配置。
 * 为Spring MVC开启java配置模式使用@EnableWebMvc注解。
 * Defines callback methods to customize the Java-based configuration for
 * Spring MVC enabled via {@code @EnableWebMvc}.
 *
 * @EnableWebMvc注解的配置类可以实现此接口提供回调，并且可以自定义覆盖默认配置。
 * <p>{@code @EnableWebMvc}-annotated configuration classes may implement
 * this interface to be called back and given a chance to customize the
 * default configuration.
 *
 * @author Rossen Stoyanchev
 * @author Keith Donald
 * @author David Syer
 * @since 3.1
 */
public interface WebMvcConfigurer {

	/**
	 * 用于配置HandlerMappings路径匹配选项，例如反斜杠匹配，后缀注册，路径匹配器以及路径辅助器。
	 * 配置的路径匹配器/路径辅助器会有以下接口实例共享：
	 * RequestMappings/ViewControllerMappings/ResourcesMappings
	 * Helps with configuring HandlerMappings path matching options such as trailing slash match,
	 * suffix registration, path matcher and path helper.
	 * Configured path matcher and path helper instances are shared for:
	 * <ul>
	 * <li>RequestMappings</li>
	 * <li>ViewControllerMappings</li>
	 * <li>ResourcesMappings</li>
	 * </ul>
	 * @since 4.0.3
	 */
	default void configurePathMatch(PathMatchConfigurer configurer) {
	}

	/**
	 * 配置内容协商选项
	 * Configure content negotiation options.
	 */
	default void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
	}

	/**
	 * 配置异步请求处理选项
	 * Configure asynchronous request handling options.
	 */
	default void configureAsyncSupport(AsyncSupportConfigurer configurer) {
	}

	/**
	 * 配置处理器将未处理的请求转发给servlet容器的默认servlet。
	 * 常见的用法就是当DispatcherServlet的映射路径是"/"，覆写类servlet容器对静态资源的默认处理。
	 * Configure a handler to delegate unhandled requests by forwarding to the
	 * Servlet container's "default" servlet. A common use case for this is when
	 * the {@link DispatcherServlet} is mapped to "/" thus overriding the
	 * Servlet container's default handling of static resources.
	 */
	default void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
	}

	/**
	 * 在默认注册之外，再添加一些Converter和Formatter
	 * Add {@link Converter}s and {@link Formatter}s in addition to the ones
	 * registered by default.
	 */
	default void addFormatters(FormatterRegistry registry) {
	}

	/**
	 * 添加Spring MVC生命周期拦截器对控制器方法调用进行前置或后置处理。拦截器可以注册作用于所有的请求，也可以限制作用于一组格式的URL。
	 * 注意：这里注册的拦截器只作用于控制器，而不是资源处理器请求。
	 * 如果需要拦截静态资源请求，可以声明一个MappedInterceptor的bean或者通过继承WebMvcConfigurationSupport切换到高级配置模式，然后覆盖resourceHandlerMapping。
	 * Add Spring MVC lifecycle interceptors for pre- and post-processing of
	 * controller method invocations. Interceptors can be registered to apply
	 * to all requests or be limited to a subset of URL patterns.
	 * <p><strong>Note</strong> that interceptors registered here only apply to
	 * controllers and not to resource handler requests. To intercept requests for
	 * static resources either declare a
	 * {@link org.springframework.web.servlet.handler.MappedInterceptor MappedInterceptor}
	 * bean or switch to advanced configuration mode by extending
	 * {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
	 * WebMvcConfigurationSupport} and then override {@code resourceHandlerMapping}.
	 */
	default void addInterceptors(InterceptorRegistry registry) {
	}

	/**
	 * 添加静态资源处理器，作用于来自web应用程序根路径/类路径/或其他的特定位置的图片/js/css文件。
	 * Add handlers to serve static resources such as images, js, and, css
	 * files from specific locations under web application root, the classpath,
	 * and others.
	 */
	default void addResourceHandlers(ResourceHandlerRegistry registry) {
	}

	/**
	 * 配置跨域请求的处理
	 * Configure cross origin requests processing.
	 * @since 4.2
	 */
	default void addCorsMappings(CorsRegistry registry) {
	}

	/**
	 * 配置简单自动化的控制器，预配置类响应状态码或用于渲染响应体的视图。
	 * 当不需要自定义控制器逻辑时非常有用——例如，渲染主页/执行简单的站点URL重定向/使用HTML返回404状态/没有任何内容的204等等。
	 * Configure simple automated controllers pre-configured with the response
	 * status code and/or a view to render the response body. This is useful in
	 * cases where there is no need for custom controller logic -- e.g. render a
	 * home page, perform simple site URL redirects, return a 404 status with
	 * HTML content, a 204 with no content, and more.
	 */
	default void addViewControllers(ViewControllerRegistry registry) {
	}

	/**
	 * 配置视图解析器，用于将控制器返回的字符串视图名转换成复杂视图Configure view resolvers to translate String-based view names returned from
	 * controllers into concrete {@link org.springframework.web.servlet.View}
	 * implementations to perform rendering with.
	 * @since 4.1
	 */
	default void configureViewResolvers(ViewResolverRegistry registry) {
	}

	/**
	 * 添加解析器支持自定义控制器方法参数类型。
	 * 修改内置支持，可以直接配置RequestMappingHandlerAdapter
	 * Add resolvers to support custom controller method argument types.
	 * <p>This does not override the built-in support for resolving handler
	 * method arguments. To customize the built-in support for argument
	 * resolution, configure {@link RequestMappingHandlerAdapter} directly.
	 * @param resolvers initially an empty list
	 */
	default void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
	}

	/**
	 * 添加处理器用于支持自定义的控制器方法返回值类型。
	 * 这里的配置不会覆盖框架中内置的对处理返回值的支持。
	 * 要自定义内置的处理返回值，可以直接配置RequestMappingHandlerAdapter。
	 * Add handlers to support custom controller method return value types.
	 * <p>Using this option does not override the built-in support for handling
	 * return values. To customize the built-in support for handling return
	 * values, configure RequestMappingHandlerAdapter directly.
	 * @param handlers initially an empty list
	 */
	default void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
	}

	/**
	 * Configure the {@link HttpMessageConverter}s to use for reading or writing
	 * to the body of the request or response. If no converters are added, a
	 * default list of converters is registered.
	 * <p><strong>Note</strong> that adding converters to the list, turns off
	 * default converter registration. To simply add a converter without impacting
	 * default registration, consider using the method
	 * {@link #extendMessageConverters(java.util.List)} instead.
	 * @param converters initially an empty list of converters
	 */
	default void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
	}

	/**
	 * A hook for extending or modifying the list of converters after it has been
	 * configured. This may be useful for example to allow default converters to
	 * be registered and then insert a custom converter through this method.
	 * @param converters the list of configured converters to extend.
	 * @since 4.1.3
	 */
	default void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
	}

	/**
	 * 配置异常解析器
	 * 如果起始为空，框架则配置默认解析器结合；不为空，则使用给定的值进行配置。
	 * 另外，可以通过extendHandlerExceptionResolvers对默认的异常解析器列表进行扩展/调整。
	 * Configure exception resolvers.
	 * <p>The given list starts out empty. If it is left empty, the framework
	 * configures a default set of resolvers, see
	 * {@link WebMvcConfigurationSupport#addDefaultHandlerExceptionResolvers(List)}.
	 * Or if any exception resolvers are added to the list, then the application
	 * effectively takes over and must provide, fully initialized, exception
	 * resolvers.
	 * <p>Alternatively you can use
	 * {@link #extendHandlerExceptionResolvers(List)} which allows you to extend
	 * or modify the list of exception resolvers configured by default.
	 * @param resolvers initially an empty list
	 * @see #extendHandlerExceptionResolvers(List)
	 * @see WebMvcConfigurationSupport#addDefaultHandlerExceptionResolvers(List)
	 */
	default void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
	}

	/**
	 * 扩充或修正默认配置的异常解析器列表。
	 * 可以用于插入自定义的异常解析器，调整默认设置
	 * Extending or modify the list of exception resolvers configured by default.
	 * This can be useful for inserting a custom exception resolver without
	 * interfering with default ones.
	 * @param resolvers the list of configured resolvers to extend
	 * @since 4.3
	 * @see WebMvcConfigurationSupport#addDefaultHandlerExceptionResolvers(List)
	 */
	default void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
	}

	/**
	 * 提供自定义Validator替换默认校验器
	 * 假设类路径上存在JSR-303实现，则默认实现为OptionalValidatorFactoryBean。
	 * 默认返回null
	 * Provide a custom {@link Validator} instead of the one created by default.
	 * The default implementation, assuming JSR-303 is on the classpath, is:
	 * {@link org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean}.
	 * Leave the return value as {@code null} to keep the default.
	 */
	@Nullable
	default Validator getValidator() {
		return null;
	}

	/**
	 * 提供自定义MessageCodesResolver，根据数据绑定和错误校验码生成消息码
	 * Provide a custom {@link MessageCodesResolver} for building message codes
	 * from data binding and validation error codes. Leave the return value as
	 * {@code null} to keep the default.
	 */
	@Nullable
	default MessageCodesResolver getMessageCodesResolver() {
		return null;
	}

}
