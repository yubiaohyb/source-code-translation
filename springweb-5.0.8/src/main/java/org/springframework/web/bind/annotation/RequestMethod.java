/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.web.bind.annotation;

/**
 * java 5 HTTP请求方式枚举。
 * 用于注解RequestMapping的method()属性。
 * Java 5 enumeration of HTTP request methods. Intended for use with the
 * {@link RequestMapping#method()} attribute of the {@link RequestMapping} annotation.
 *
 * 注意：DispatcherServlet默认只支持GET, HEAD, POST, PUT, PATCH and DELETE。
 * 除非显式指定DispatcherServlet也分发TRACE和OPTIONS，否则对于这两个请求方式的处理将采用默认的HttpServlet行为：
 * 检查dispatchOptionsRequest和dispatchTraceRequest属性，如果需要则开启。
 * <p>Note that, by default, {@link org.springframework.web.servlet.DispatcherServlet}
 * supports GET, HEAD, POST, PUT, PATCH and DELETE only. DispatcherServlet will
 * process TRACE and OPTIONS with the default HttpServlet behavior unless explicitly
 * told to dispatch those request types as well: Check out the "dispatchOptionsRequest"
 * and "dispatchTraceRequest" properties, switching them to "true" if necessary.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see RequestMapping
 * @see org.springframework.web.servlet.DispatcherServlet#setDispatchOptionsRequest
 * @see org.springframework.web.servlet.DispatcherServlet#setDispatchTraceRequest
 */
public enum RequestMethod {

	GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE

}
