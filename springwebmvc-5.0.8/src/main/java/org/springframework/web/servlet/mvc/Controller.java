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

package org.springframework.web.servlet.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;
import org.springframework.web.servlet.ModelAndView;

/**
 * 基本的控制器接口，实现此接口表示这是一个组件。
 * 像HttpServlet一样负责接收HttpServletRequest和HttpServletResponse，但是可以参与MVC工作流。
 * 类似于Struts中Action的概念。
 * Base Controller interface, representing a component that receives
 * {@code HttpServletRequest} and {@code HttpServletResponse}
 * instances just like a {@code HttpServlet} but is able to
 * participate in an MVC workflow. Controllers are comparable to the
 * notion of a Struts {@code Action}.
 *
 * Controller的实现要求是可复用/线程安全的类，在应用程序生命周期中能够处理并发HTTP请求。
 * 为了让Controller的配置简化，Controller实现类提倡采用JavaBeans方式，通常就是这么做的。
 * <p>Any implementation of the Controller interface should be a
 * <i>reusable, thread-safe</i> class, capable of handling multiple
 * HTTP requests throughout the lifecycle of an application. To be able to
 * configure a Controller easily, Controller implementations are encouraged
 * to be (and usually are) JavaBeans.
 *
 * 工作流
 * <h3><a name="workflow">Workflow</a></h3>
 *
 * 在分发器接收到请求，且解析完本地化/主题等组件后，会尝试使用HandlerMapping解析Controller。
 * 当找到处理请求的Controller后，将调用其handleRequest(HttpServletRequest, HttpServletResponse)方法；
 * 找到的Controller负责请求的实际处理；如果可以的话，会返回相应的ModelAndView。
 * 因此，实际上，此方法是分发器分发给控制器的主要入口。
 * <p>After a {@code DispatcherServlet} has received a request and has
 * done its work to resolve locales, themes, and suchlike, it then tries
 * to resolve a Controller, using a
 * {@link org.springframework.web.servlet.HandlerMapping HandlerMapping}.
 * When a Controller has been found to handle the request, the
 * {@link #handleRequest(HttpServletRequest, HttpServletResponse) handleRequest}
 * method of the located Controller will be invoked; the located Controller
 * is then responsible for handling the actual request and &mdash; if applicable
 * &mdash; returning an appropriate
 * {@link org.springframework.web.servlet.ModelAndView ModelAndView}.
 * So actually, this method is the main entry point for the
 * {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlet}
 * which delegates requests to controllers.
 *
 * 因此基本上所有的Controller实现只是处理HttpServletRequests，义务性地返回一个ModelAndView，交由分发器做进一步地处理。
 * 其他额外的功能例如校验/表单处理等可以通过继承AbstractController或其子类实现。
 * <p>So basically any <i>direct</i> implementation of the {@code Controller} interface
 * just handles HttpServletRequests and should return a ModelAndView, to be further
 * interpreted by the DispatcherServlet. Any additional functionality such as
 * optional validation, form handling, etc. should be obtained through extending
 * {@link org.springframework.web.servlet.mvc.AbstractController AbstractController}
 * or one of its subclasses.
 *
 * 设计和测试时的一些注意事项
 * <h3>Notes on design and testing</h3>
 *
 * Controller接口在设计上像HttpServlet显然就是为了操作HttpServletRequest和HttpServletResponse对象。
 * 接口的设计并不是简单地与Servlet API解耦，例如WebWork/JSF/Tapestry，在可以充分使用servlet api的强大能力的同时，
 * Controllers的使用可以更加的通用化：不仅可以处理web端用户接口请求，还可以处理远程协议或根据指令生成报表。
 * <p>The Controller interface is explicitly designed to operate on HttpServletRequest
 * and HttpServletResponse objects, just like an HttpServlet. It does not aim to
 * decouple itself from the Servlet API, in contrast to, for example, WebWork, JSF or Tapestry.
 * Instead, the full power of the Servlet API is available, allowing Controllers to be
 * general-purpose: a Controller is able to not only handle web user interface
 * requests but also to process remoting protocols or to generate reports on demand.
 *
 * 通过对HttpServletRequest和HttpServletResponse传入mock对象就可以对Controllers进行简单的测试。
 * 正因如此，Spring开发了一组Servlet API 模拟集合，可以测试任何web组件，尤其适合测试spring web控制器。
 * 相较于Struts Action，spring mvc不需要模拟ActionServlet或者其他的基础组件，只需要模拟HttpServletRequest和HttpServletResponse。
 * <p>Controllers can easily be tested by passing in mock objects for the
 * HttpServletRequest and HttpServletResponse objects as parameters to the
 * {@link #handleRequest(HttpServletRequest, HttpServletResponse) handleRequest}
 * method. As a convenience, Spring ships with a set of Servlet API mocks
 * that are suitable for testing any kind of web components, but are particularly
 * suitable for testing Spring web controllers. In contrast to a Struts Action,
 * there is no need to mock the ActionServlet or any other infrastructure;
 * mocking HttpServletRequest and HttpServletResponse is sufficient.
 *
 * 如果Controllers需要引用特定的环境变量，只需要像spring（web）应用上下文中的其他bean一样选择实现特定的感知接口。例如：
 * ApplicationContextAware/ResourceLoaderAware/ServletContextAware
 * <p>If Controllers need to be aware of specific environment references, they can
 * choose to implement specific awareness interfaces, just like any other bean in a
 * Spring (web) application context can do, for example:
 * <ul>
 * <li>{@code org.springframework.context.ApplicationContextAware}</li>
 * <li>{@code org.springframework.context.ResourceLoaderAware}</li>
 * <li>{@code org.springframework.web.context.ServletContextAware}</li>
 * </ul>
 *
 * 在测试环境中，通过在相应的感知接口中定义settter方法，环境变量可以很容易的传递进去。通常，推荐尽可能窄化对感知接口的依赖。
 * 例如，如果只需要加载资源，那就只要实现ResourceLoaderAware就好。
 * 换言之，从WebApplicationObjectSupport的基类中去取，虽然由于继承关系，你可以取到所有环境变量，但带来的问题是在初始化时，你就需要对它进行赋值。
 * <p>Such environment references can easily be passed in testing environments,
 * through the corresponding setters defined in the respective awareness interfaces.
 * In general, it is recommended to keep the dependencies as minimal as possible:
 * for example, if all you need is resource loading, implement ResourceLoaderAware only.
 * Alternatively, derive from the WebApplicationObjectSupport base class, which gives
 * you all those references through convenient accessors but requires an
 * ApplicationContext reference on initialization.
 *
 * Controllers可以选择是否实现LastModified接口。
 * <p>Controllers can optionally implement the {@link LastModified} interface.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see LastModified
 * @see SimpleControllerHandlerAdapter
 * @see AbstractController
 * @see org.springframework.mock.web.MockHttpServletRequest
 * @see org.springframework.mock.web.MockHttpServletResponse
 * @see org.springframework.context.ApplicationContextAware
 * @see org.springframework.context.ResourceLoaderAware
 * @see org.springframework.web.context.ServletContextAware
 * @see org.springframework.web.context.support.WebApplicationObjectSupport
 */
@FunctionalInterface
public interface Controller {

	/**
	 * 处理请求，返回分发器负责渲染的ModelAndView。
	 * 当返回值为null时，并不意味着这是一个系统异常：只是暗示处理器自身已经完成了请求处理工作，不需要再通过ModelAndView进行渲染了。
	 * Process the request and return a ModelAndView object which the DispatcherServlet
	 * will render. A {@code null} return value is not an error: it indicates that
	 * this object completed request processing itself and that there is therefore no
	 * ModelAndView to render.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render, or {@code null} if handled directly
	 * @throws Exception in case of errors
	 */
	@Nullable
	ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
