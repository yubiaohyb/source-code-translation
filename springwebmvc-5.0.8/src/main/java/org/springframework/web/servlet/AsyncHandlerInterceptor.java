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

import org.springframework.web.method.HandlerMethod;

/**
 * 扩展HandlerInterceptor添加来一个在异步请求处理开始后执行的回调。
 * Extends {@code HandlerInterceptor} with a callback method invoked after the
 * start of asynchronous request handling.
 *
 * 当处理器开启一个异步处理请求，分发器不会像往常处理同步请求异常再去调用postHandle和afterCompletion方法，
 * 因为请求处理的结果（例如ModelAndView）还没有就绪，会从另一个线程中并发生成。
 * 这种场景下，会转去执行afterConcurrentHandlingStarted方法，在线程释放回servlet容器前清除线程绑定的一些属性。
 * <p>When a handler starts an asynchronous request, the {@link DispatcherServlet}
 * exits without invoking {@code postHandle} and {@code afterCompletion} as it
 * normally does for a synchronous request, since the result of request handling
 * (e.g. ModelAndView) is likely not yet ready and will be produced concurrently
 * from another thread. In such scenarios, {@link #afterConcurrentHandlingStarted}
 * is invoked instead, allowing implementations to perform tasks such as cleaning
 * up thread-bound attributes before releasing the thread to the Servlet container.
 *
 * 在异步处理完成后，请求会被分发给容器做进一步地处理。
 * 在这个阶段，分发器调用preHandle/postHandle/afterCompletion方法，
 * 通过使用拦截器检查ServletRequest的DispatcherType是REQUEST还是ASYNC，
 * 来区分初始请求和异步处理完成后子分发请求。
 * <p>When asynchronous handling completes, the request is dispatched to the
 * container for further processing. At this stage the {@code DispatcherServlet}
 * invokes {@code preHandle}, {@code postHandle}, and {@code afterCompletion}.
 * To distinguish between the initial request and the subsequent dispatch
 * after asynchronous handling completes, interceptors can check whether the
 * {@code javax.servlet.DispatcherType} of {@link javax.servlet.ServletRequest}
 * is {@code "REQUEST"} or {@code "ASYNC"}.
 *
 * 注意：当由于网络问题导致异步请求超时或完成时，处理器拦截器可能需要相应的处理。
 * 在这种情况下，servlet容器不会继续分发请求，因此后续的postHandle和afterCompletion也就得不到调用。
 * 相反地，可以通过在WebAsyncManager上使用registerCallbackInterceptor或registerDeferredResultInterceptor方法来注册跟踪异步请求。
 * 我们可以主动地在每个请求上去这么做，不用管是否开启异步请求处理。
 * <p>Note that {@code HandlerInterceptor} implementations may need to do work
 * when an async request times out or completes with a network error. For such
 * cases the Servlet container does not dispatch and therefore the
 * {@code postHandle} and {@code afterCompletion} methods will not be invoked.
 * Instead, interceptors can register to track an asynchronous request through
 * the {@code registerCallbackInterceptor} and {@code registerDeferredResultInterceptor}
 * methods on {@link org.springframework.web.context.request.async.WebAsyncManager
 * WebAsyncManager}. This can be done proactively on every request from
 * {@code preHandle} regardless of whether async request processing will start.
 *
 * @author Rossen Stoyanchev
 * @since 3.2
 * @see org.springframework.web.context.request.async.WebAsyncManager
 * @see org.springframework.web.context.request.async.CallableProcessingInterceptor
 * @see org.springframework.web.context.request.async.DeferredResultProcessingInterceptor
 */
public interface AsyncHandlerInterceptor extends HandlerInterceptor {

	/**
	 * 当处理器并发执行时替代postHandle和afterCompletion方法的调用。
	 * Called instead of {@code postHandle} and {@code afterCompletion}
	 * when the handler is being executed concurrently.
	 * 实现中可能会使用到请求和响应，但是应该避免对其改动，因为可能会跟处理器的并发执行冲突。本方法的通常用作清理线程本地变量。<p>Implementations may use the provided request and response but should
	 * avoid modifying them in ways that would conflict with the concurrent
	 * execution of the handler. A typical use of this method would be to
	 * clean up thread-local variables.
	 * @param request the current request
	 * @param response the current response
	 * @param handler the handler (or {@link HandlerMethod}) that started async
	 * execution, for type and/or instance examination
	 * @throws Exception in case of errors
	 */
	default void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response,
			Object handler) throws Exception {
	}

}
