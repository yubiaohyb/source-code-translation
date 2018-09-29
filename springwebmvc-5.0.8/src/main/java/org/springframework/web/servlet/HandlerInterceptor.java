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

package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;

/**
 * 这是一个工作流接口，可以用于自定义处理器执行链。
 * 应用程序可以为某些分组的处理器注册任意多个已有或自定义的拦截器，用于在不改变现有处理器实现的前提下，添加一些通用预处理行为。
 * Workflow interface that allows for customized handler execution chains.
 * Applications can register any number of existing or custom interceptors
 * for certain groups of handlers, to add common preprocessing behavior
 * without needing to modify each handler implementation.
 *
 * 处理器拦截器会在处理器适配器触发处理器自我执行前被调用。
 * 这种预处理机制可以应用于很多方面。例如：权限校验、通用处理器行为如本地化，主题切换。
 * 它的主要目的是用于重构剥离出重复性的处理器代码。
 * <p>A HandlerInterceptor gets called before the appropriate HandlerAdapter
 * triggers the execution of the handler itself. This mechanism can be used
 * for a large field of preprocessing aspects, e.g. for authorization checks,
 * or common handler behavior like locale or theme changes. Its main purpose
 * is to allow for factoring out repetitive handler code.
 *
 * 在异步处理场景中，处理器可能由另外一个线程执行，而主线程仍然存在只是尚未执行渲染或调用postHandle和afterCompletion回调方法。
 * 当并发处理器执行完成，请求会再次被分发回来继续执行渲染及其他规定的方法调用。
 * 更多选项和细节可以查看AsyncHandlerInterceptor。
 * <p>In an asynchronous processing scenario, the handler may be executed in a
 * separate thread while the main thread exits without rendering or invoking the
 * {@code postHandle} and {@code afterCompletion} callbacks. When concurrent
 * handler execution completes, the request is dispatched back in order to
 * proceed with rendering the model and all methods of this contract are invoked
 * again. For further options and details see
 * {@code org.springframework.web.servlet.AsyncHandlerInterceptor}
 *
 * 通常每个处理器映射bean单独定义一个拦截器链。
 * 为了将特定拦截器链应用到一组处理器上，开发人员需要通过处理器映射bean映射到这些处理器上。
 * 拦截器本身作为bean定义在应用程序上下文中，处理器映射bean可以通过interceptors属性进行引用。
 * （在XML中使用<list/>、<ref/>标签）
 * <p>Typically an interceptor chain is defined per HandlerMapping bean,
 * sharing its granularity. To be able to apply a certain interceptor chain
 * to a group of handlers, one needs to map the desired handlers via one
 * HandlerMapping bean. The interceptors themselves are defined as beans
 * in the application context, referenced by the mapping bean definition
 * via its "interceptors" property (in XML: a &lt;list&gt; of &lt;ref&gt;).
 *
 * 处理器拦截器基本上类似于servlet过滤器，但相较于后者拦截器只允许自定义预处理（可以选择禁止处理器本身的执行）和自定义后置处理。
 * 处理器更加的强大，例如可以替换拦截器链传下来的请求和响应对象。
 * 注意：过滤器是在web.xml中配置的，而处理器拦截器是在应用上下文中（应该是xml配置思维ORZ...）。
 * <p>HandlerInterceptor is basically similar to a Servlet Filter, but in
 * contrast to the latter it just allows custom pre-processing with the option
 * of prohibiting the execution of the handler itself, and custom post-processing.
 * Filters are more powerful, for example they allow for exchanging the request
 * and response objects that are handed down the chain. Note that a filter
 * gets configured in web.xml, a HandlerInterceptor in the application context.
 *
 * 基本准则就是，处理器粒度相关的预处理任务可以考虑使用处理器拦截器使用。
 * 另一方面，过滤器非常适合于请求内容和视图内容的处理，例如文件上传表单和GZIP压缩。
 * 通常当开发人员需要将过滤器作用于特定的内容类型（例如图片）或所有的请求时会得到展现。
 * <p>As a basic guideline, fine-grained handler-related preprocessing tasks are
 * candidates for HandlerInterceptor implementations, especially factored-out
 * common handler code and authorization checks. On the other hand, a Filter
 * is well-suited for request content and view content handling, like multipart
 * forms and GZIP compression. This typically shows when one needs to map the
 * filter to certain content types (e.g. images), or to all requests.
 *
 * @author Juergen Hoeller
 * @since 20.06.2003
 * @see HandlerExecutionChain#getInterceptors
 * @see org.springframework.web.servlet.handler.HandlerInterceptorAdapter
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping#setInterceptors
 * @see org.springframework.web.servlet.handler.UserRoleAuthorizationInterceptor
 * @see org.springframework.web.servlet.i18n.LocaleChangeInterceptor
 * @see org.springframework.web.servlet.theme.ThemeChangeInterceptor
 * @see javax.servlet.Filter
 */
public interface HandlerInterceptor {

	/**
	 * 在处理器适配器调用处理器之前，拦截处理器的执行。
	 * 分发器会在一个由多个拦截器，结尾是处理器本身的执行链中进行处理器调用。
	 * 使用这个方法，每个拦截器都可以决定跳出执行链，通常是通过发送一个HTTP Error或者写一个自定义的响应。
	 * 注意：异步请求处理需要另外考虑。详细查看AsyncHandlerInterceptor。
	 * 默认实现返回true。
	 * Intercept the execution of a handler. Called after HandlerMapping determined
	 * an appropriate handler object, but before HandlerAdapter invokes the handler.
	 * <p>DispatcherServlet processes a handler in an execution chain, consisting
	 * of any number of interceptors, with the handler itself at the end.
	 * With this method, each interceptor can decide to abort the execution chain,
	 * typically sending a HTTP error or writing a custom response.
	 * <p><strong>Note:</strong> special considerations apply for asynchronous
	 * request processing. For more details see
	 * {@link org.springframework.web.servlet.AsyncHandlerInterceptor}.
	 * <p>The default implementation returns {@code true}.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler chosen handler to execute, for type and/or instance evaluation
	 * @return {@code true} if the execution chain should proceed with the
	 * next interceptor or the handler itself. Else, DispatcherServlet assumes
	 * that this interceptor has already dealt with the response itself.
	 * @throws Exception in case of errors
	 */
	default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		return true;
	}

	/**
	 * 在处理器适配器调用处理器后，分发器渲染视图之前，拦截处理器的执行。
	 * 可以通过已有的ModelAndView添加额外的模型对象提供给视图。
	 * 分发器会在一个由多个拦截器，结尾是处理器本身的执行链中进行处理器调用。
	 * 处理器执行完后，每个拦截器会按照在倒序执行本方法。
	 * 异步请求处理需要特殊考虑使用。详细查看AsyncHandlerInterceptor。
	 * Intercept the execution of a handler. Called after HandlerAdapter actually
	 * invoked the handler, but before the DispatcherServlet renders the view.
	 * Can expose additional model objects to the view via the given ModelAndView.
	 * <p>DispatcherServlet processes a handler in an execution chain, consisting
	 * of any number of interceptors, with the handler itself at the end.
	 * With this method, each interceptor can post-process an execution,
	 * getting applied in inverse order of the execution chain.
	 * <p><strong>Note:</strong> special considerations apply for asynchronous
	 * request processing. For more details see
	 * {@link org.springframework.web.servlet.AsyncHandlerInterceptor}.
	 * <p>The default implementation is empty.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler handler (or {@link HandlerMethod}) that started asynchronous
	 * execution, for type and/or instance examination
	 * @param modelAndView the {@code ModelAndView} that the handler returned
	 * (can also be {@code null})
	 * @throws Exception in case of errors
	 */
	default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable ModelAndView modelAndView) throws Exception {
	}

	/**
	 * 方法会在请求处理完成后调用，也就是说在视图渲染结束后。
	 * 无论处理器执行结果如何都会调用，因此特别适合来做一些资源清理工作。
	 * 注意：只有当前拦截器的preHandle调用成功并返回true后，才会被调用。
	 * 和postHandle方法一样，第一个拦截器将会最后一个被调用。
	 * 异步处理需要另外考虑。
	 * Callback after completion of request processing, that is, after rendering
	 * the view. Will be called on any outcome of handler execution, thus allows
	 * for proper resource cleanup.
	 * <p>Note: Will only be called if this interceptor's {@code preHandle}
	 * method has successfully completed and returned {@code true}!
	 * <p>As with the {@code postHandle} method, the method will be invoked on each
	 * interceptor in the chain in reverse order, so the first interceptor will be
	 * the last to be invoked.
	 * <p><strong>Note:</strong> special considerations apply for asynchronous
	 * request processing. For more details see
	 * {@link org.springframework.web.servlet.AsyncHandlerInterceptor}.
	 * <p>The default implementation is empty.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler handler (or {@link HandlerMethod}) that started asynchronous
	 * execution, for type and/or instance examination
	 * @param ex exception thrown on handler execution, if any
	 * @throws Exception in case of errors
	 */
	default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable Exception ex) throws Exception {
	}

}
