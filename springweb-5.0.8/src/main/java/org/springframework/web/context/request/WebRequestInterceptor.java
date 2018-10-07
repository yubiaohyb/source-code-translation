/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.web.context.request;

import org.springframework.lang.Nullable;
import org.springframework.ui.ModelMap;

/**
 * web请求拦截器通用接口。在这层抽象基础之上，Servlet和Portlet请求环境均适用。
 * Interface for general web request interception. Allows for being applied
 * to Servlet request as well as Portlet request environments, by building
 * on the {@link WebRequest} abstraction.
 *
 * 接口用于mvc风格的请求处理：处理器执行，返回一组数据对象，然后据此渲染视图。
 * 当然，当处理器完整处理完请求后，视图渲染不会执行。
 * <p>This interface assumes MVC-style request processing: A handler gets executed,
 * exposes a set of model objects, then a view gets rendered based on that model.
 * Alternatively, a handler may also process the request completely, with no
 * view to be rendered.
 *
 * 说了异步处理的流程。
 * <p>In an async processing scenario, the handler may be executed in a separate
 * thread while the main thread exits without rendering or invoking the
 * {@code postHandle} and {@code afterCompletion} callbacks. When concurrent
 * handler execution completes, the request is dispatched back in order to
 * proceed with rendering the model and all methods of this contract are invoked
 * again. For further options and comments see
 * {@code org.springframework.web.context.request.async.AsyncWebRequestInterceptor}
 *
 * <p>This interface is deliberately minimalistic to keep the dependencies of
 * generic request interceptors as minimal as feasible.
 *
 * 注意：尽管拦截器适用于servlet环境的请求处理，但默认只作用于portlet环境的render块用于预处理渲染portlet视图。
 * 为了action块中也可以使用，可以设置HandlerMapping的applyWebRequestInterceptorsToRenderPhaseOnly为false。
 * 否则，考虑使用portlet专有的HandlerInterceptor。
 * <p><b>NOTE:</b> While this interceptor is applied to the entire request processing
 * in a Servlet environment, it is by default only applied to the <i>render</i> phase
 * in a Portlet environment, preparing and rendering a Portlet view. To apply
 * WebRequestInterceptors to the <i>action</i> phase as well, set the HandlerMapping's
 * "applyWebRequestInterceptorsToRenderPhaseOnly" flag to "false". Alternatively,
 * consider using the Portlet-specific HandlerInterceptor mechanism for such needs.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see ServletWebRequest
 * @see org.springframework.web.servlet.DispatcherServlet
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping#setInterceptors
 * @see org.springframework.web.servlet.HandlerInterceptor
 */
public interface WebRequestInterceptor {

	/**
	 * Intercept the execution of a request handler <i>before</i> its invocation.
	 * <p>Allows for preparing context resources (such as a Hibernate Session)
	 * and expose them as request attributes or as thread-local objects.
	 * @param request the current web request
	 * @throws Exception in case of errors
	 */
	void preHandle(WebRequest request) throws Exception;

	/**
	 * Intercept the execution of a request handler <i>after</i> its successful
	 * invocation, right before view rendering (if any).
	 * <p>Allows for modifying context resources after successful handler
	 * execution (for example, flushing a Hibernate Session).
	 * @param request the current web request
	 * @param model the map of model objects that will be exposed to the view
	 * (may be {@code null}). Can be used to analyze the exposed model
	 * and/or to add further model attributes, if desired.
	 * @throws Exception in case of errors
	 */
	void postHandle(WebRequest request, @Nullable ModelMap model) throws Exception;

	/**
	 * Callback after completion of request processing, that is, after rendering
	 * the view. Will be called on any outcome of handler execution, thus allows
	 * for proper resource cleanup.
	 * <p>Note: Will only be called if this interceptor's {@code preHandle}
	 * method has successfully completed!
	 * @param request the current web request
	 * @param ex exception thrown on handler execution, if any
	 * @throws Exception in case of errors
	 */
	void afterCompletion(WebRequest request, @Nullable Exception ex) throws Exception;

}
