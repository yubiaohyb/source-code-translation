/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.web.servlet.mvc;

import javax.servlet.http.HttpServletRequest;

/**
 * 支持记录请求的最后修改时间来辅助实现内容缓存。
 * 用法和Servlet API的getLastModified方法一样。
 * Supports last-modified HTTP requests to facilitate content caching.
 * Same contract as for the Servlet API's {@code getLastModified} method.
 *
 * 方法调用会交由HandlerAdapter#getLastModified实现。
 * 默认，spring的默认框架中任何的Controller或HttpRequestHandler可以实现此接口开启最后修改时间检查。
 * <p>Delegated to by a {@link org.springframework.web.servlet.HandlerAdapter#getLastModified}
 * implementation. By default, any Controller or HttpRequestHandler within Spring's
 * default framework can implement this interface to enable last-modified checking.
 *
 * 注意：不同的处理器实现方式，最后修改时间的实现风格不同。
 * 例如，在spring2.5注解控制器方式（使用RequestMapping）通过WebRequest#checkNotModified方法提供最后修改时间支持，可以在主处理器方法中进行最后修改时间校验。
 * <p><b>Note:</b> Alternative handler implementation approaches have different
 * last-modified handling styles. For example, Spring 2.5's annotated controller
 * approach (using {@code @RequestMapping}) provides last-modified support
 * through the {@link org.springframework.web.context.request.WebRequest#checkNotModified}
 * method, allowing for last-modified checking within the main handler method.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see javax.servlet.http.HttpServlet#getLastModified
 * @see Controller
 * @see SimpleControllerHandlerAdapter
 * @see org.springframework.web.HttpRequestHandler
 * @see HttpRequestHandlerAdapter
 */
public interface LastModified {

	/**
	 * HttpServlet中getLastModified方法作用一致。
	 * 在请求处理前调用。
	 * 返回值将作为Last-Modified头信息发送给HTTP客户端，并于客户端发回来的If-Modified-Since的头信息进行比较。
	 * Same contract as for HttpServlet's {@code getLastModified} method.
	 * Invoked <b>before</b> request processing.
	 * <p>The return value will be sent to the HTTP client as Last-Modified header,
	 * and compared with If-Modified-Since headers that the client sends back.
	 * The content will only get regenerated if there has been a modification.
	 * @param request current HTTP request
	 * @return the time the underlying resource was last modified, or -1
	 * meaning that the content must always be regenerated
	 * @see org.springframework.web.servlet.HandlerAdapter#getLastModified
	 * @see javax.servlet.http.HttpServlet#getLastModified
	 */
	long getLastModified(HttpServletRequest request);

}
