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

package org.springframework.web.servlet;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ConfigurableWebEnvironment;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.context.support.ServletRequestHandledEvent;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.util.NestedServletException;
import org.springframework.web.util.WebUtils;

/**
 * spring web框架的基础servlet，提供了基于javabean集成spring上下文的完整解决方案。
 *
 * 提供了以下的一些功能特性：
 * 每个servlet管理一个自己的应用上下文，配置由这个servlet命名空间下bean来决定。
 * 无论请求是否处理成功，在处理过程中会发布（触发）事件。
 *
 * 子类必须实现doService()方法处理请求。
 * 由于类是继承于HttpServletBean而不是直接继承HttpServlet，bean的属性会自动映射到servlet上。
 * 子类可以覆写initFrameworkServlet()方法实现一些自定义的初始化。
 *
 * 在servelt的init-param层级检测contextClass参数，如果没有找到则使用默认上下文参数XmlWebApplicationContext。
 * 注意默认情况下，自定义的上下文类需要实现ConfigurableWebApplicationContext SPI。
 *
 * contextInitializerClasses作为servlet的初始化参数，是一个可选项。可以指定一到多个ApplicationContextInitializer类。
 * 通过这种方式可以完成对应用上下文进行一些额外的编程式配置，例如通过ConfigurableApplicationContext#getEnvironment()来添加一些属性资源或者激活运行环境。
 * ContextLoader对于根web应用上下文也同样支持上下文初始化类。
 *
 * contextConfigLocation用于指定配置文件位置，可以指定多个，中间可以使用逗号或空格分开，如："test-servlet.xml, myServlet.xml"。
 * 如果没有显式指定，默认会从servelt命名空间进行读取。
 *
 * 注意：如果有多个配置文件时，稍后的文件中的bean定义会覆盖前面的相同bean的定义，至少在使用spring的默认上下文实现时是这样的。
 * 也就是说，我们可以通过额外的xml文件来覆盖某些bean定义。
 *
 * 默认的命名格式是"'servlet-name'-servlet"，例如"test-servlet"是servlet名为test的格式名（在XmlWebApplicationContext中默认指向/WEB-INF/test-servlet.xml）。
 * 命名格式也可以通过namespace初始化参数进行指定。
 *
 * 从spring3.1开始，FrameworkServlet对应用上下文采用注入方式，而不再是自己内部创建。
 * 这样做有助于在servlet3.0及以后的版本中实现servlet实例的编程式注册。
 * 详见FrameworkServlet(WebApplicationContext)。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Chris Beams
 * @author Rossen Stoyanchev
 * @author Phillip Webb
 * @see #doService
 * @see #setContextClass
 * @see #setContextConfigLocation
 * @see #setContextInitializerClasses
 * @see #setNamespace
 */
@SuppressWarnings("serial")
public abstract class FrameworkServlet extends HttpServletBean implements ApplicationContextAware {

	/**
	 * web应用上下文命名空间后缀。如果当前类的servlet实例名为test，则它的命名空间为test-servlet。
	 */
	public static final String DEFAULT_NAMESPACE_SUFFIX = "-servlet";

	/**
	 * FrameworkServlet的默认上下文。
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 */
	public static final Class<?> DEFAULT_CONTEXT_CLASS = XmlWebApplicationContext.class;

	/**
	 * web应用上下文中servlet上下文属性前缀。
	 * The completion is the servlet name.
	 */
	public static final String SERVLET_CONTEXT_PREFIX = FrameworkServlet.class.getName() + ".CONTEXT.";

	/**
	 * 单初始化参数多值字符串分隔符。
	 */
	private static final String INIT_PARAM_DELIMITERS = ",; \t\n";


	/** 用于查找web应用上下文的servlet上下文属性 */
	@Nullable
	private String contextAttribute;

	/** 要创建的web应用上下文实现类 */
	private Class<?> contextClass = DEFAULT_CONTEXT_CLASS;

	/** web应用上下文id设置值 */
	@Nullable
	private String contextId;

	/** servlet命名空间 */
	@Nullable
	private String namespace;

	/** 上下文显式配置位置 */
	@Nullable
	private String contextConfigLocation;

	/** 实际应用于上下文的应用上下文初始化器实例 */
	private final List<ApplicationContextInitializer<ConfigurableApplicationContext>> contextInitializers =
			new ArrayList<>();

	/** 通过初始化参数配置的逗号分隔应用上下文初始化器类名称 */
	@Nullable
	private String contextInitializerClasses;

	/** 是否将此上下文发布为servlet上下文属性 */
	private boolean publishContext = true;

	/** 是否在每个请求的最后触发ServletRequestHandledEvent事件 */
	private boolean publishEvents = true;

	/** 是否将本地上下文和请求属性暴露出去，让子线程继承 */
	private boolean threadContextInheritable = false;

	/** 是否将HTTP OPTIONS请求分发给doService方法 */
	private boolean dispatchOptionsRequest = false;

	/** 是否将HTTP TRACE请求分发给doService方法 */
	private boolean dispatchTraceRequest = false;

	/** servlet的web应用上下文 */
	@Nullable
	private WebApplicationContext webApplicationContext;

	/** 标记web应用上下文是否已通过WebApplicationContext方法设置了 */
	private boolean webApplicationContextInjected = false;

	/** 标记onRefresh方法是否已调用 */
	private boolean refreshEventReceived = false;


	/**
	 * 创建FrameworkServlet实例，基于默认上下文以及servlet初始化参数提供的参数，在内部自己创建web应用上下文。
	 * 通常用于servlet2.5以及更早的版本，servlet注册的唯一方式就是通过web.xml配置使用无参构造函数。
	 * 调用setContextConfigLocation（对应contextConfigLocation初始化参数）指定默认上下文类要加载的xml文件。
	 * 调用setContextClass（对应contextClass初始化参数）指定上下文类覆盖默认的XmlWebApplicationContext，例如AnnotationConfigWebApplicationContext。
	 * 调用setContextInitializerClasses（对应上下文初始器类），在刷新之前进一步配置内部应用上下文。
	 * @see #FrameworkServlet(WebApplicationContext)
	 */
	public FrameworkServlet() {
	}

	/**
	 * 使用给定web应用上下文创建FrameworkServlet
	 * 用于在servlet3.0及以后的版本中，基于实例通过ServletContext#addServlet API注册servlet。
	 * 使用这个构造函数也就意味着contextClass/contextConfigLocation/contextAttribute/namespace这些以往的属性或初始化参数会被忽略掉。
	 * 
	 * 如果web应用上下文可能或者尚未调用ConfigurableApplicationContext#refresh()进行刷新。
	 * 如果满足上下文是ConfigurableWebApplicationContext的实现类，且还没有刷新（推荐方式？？？），将会发生以下动作：
	 * 如果还没有指定parent，那么根应用上下文将会设置为其parent；
	 * 如果还没有设置id，那么将会设置一个值给它；
	 * ServletContext和ServletConfig将会传给应用上下文；
	 * 将会调用postProcessWebApplicationContext；
	 * 所有通过contextInitializerClasses初始化参数或者通过setContextInitializers设置的上下文初始化器将会被应用；
	 * 最后调用ConfigurableApplicationContext#refresh进行上下文刷新。
	 * 
	 * 如果上下文已刷新或者没有实现ConfigurableWebApplicationContext，则无论用户是否执行自定义需求行为都不会出现上述动作。
	 * 查看 {@link org.springframework.web.WebApplicationInitializer} 用例。
	 * @param webApplicationContext the context to use
	 * @see #initWebApplicationContext
	 * @see #configureAndRefreshWebApplicationContext
	 * @see org.springframework.web.WebApplicationInitializer
	 */
	public FrameworkServlet(WebApplicationContext webApplicationContext) {
		this.webApplicationContext = webApplicationContext;
	}


	/**
	 * 设置用于读取web应用上下文的servlet上下文属性名称
	 */
	public void setContextAttribute(@Nullable String contextAttribute) {
		this.contextAttribute = contextAttribute;
	}

	/**
	 * 返回                               
	 */
	@Nullable
	public String getContextAttribute() {
		return this.contextAttribute;
	}

	/**
	 * 设置自定义上下文类。设置类必须是WebApplicationContext的子类。
	 * 当使用默认FrameworkServlet实现时，该上下文类也必须实现ConfigurableWebApplicationContext接口。
	 * @see #createWebApplicationContext
	 */
	public void setContextClass(Class<?> contextClass) {
		this.contextClass = contextClass;
	}

	/**
	 * 返回
	 */
	public Class<?> getContextClass() {
		return this.contextClass;
	}

	/**
	 * 自定义应用上下文id
	 */
	public void setContextId(@Nullable String contextId) {
		this.contextId = contextId;
	}

	/**
	 * 返回，如果有
	 */
	@Nullable
	public String getContextId() {
		return this.contextId;
	}

	/**
	 * 设置servlet自定义命名空间
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * 返回命名空间，如果未指定，则按照习惯进行返回。
	 */
	public String getNamespace() {
		return (this.namespace != null ? this.namespace : getServletName() + DEFAULT_NAMESPACE_SUFFIX);
	}

	/**
	 * 显式设置上下文配置位置，覆盖默认，可以由多个文件位置组成。
	 */
	public void setContextConfigLocation(@Nullable String contextConfigLocation) {
		this.contextConfigLocation = contextConfigLocation;
	}

	/**
	 * 返回显式文件配置位置，如果有的话
	 */
	@Nullable
	public String getContextConfigLocation() {
		return this.contextConfigLocation;
	}

	/**
	 * 指定FrameworkServlet需要使用的应用上下文初始化器
	 * @see #configureAndRefreshWebApplicationContext
	 * @see #applyInitializers
	 */
	@SuppressWarnings("unchecked")
	public void setContextInitializers(@Nullable ApplicationContextInitializer<?>... initializers) {
		if (initializers != null) {
			for (ApplicationContextInitializer<?> initializer : initializers) {
				this.contextInitializers.add((ApplicationContextInitializer<ConfigurableApplicationContext>) initializer);
			}
		}
	}

	/**
	 * 设置ApplicationContextInitializer类全限定名集合
	 * @see #configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext)
	 * @see #applyInitializers(ConfigurableApplicationContext)
	 */
	public void setContextInitializerClasses(String contextInitializerClasses) {
		this.contextInitializerClasses = contextInitializerClasses;
	}

	/**
	 * 设置当前servlet上下文是否作为ServletContext属性提供给web容器中的所有对象。默认值为true。
	 * 尽管一直存在争议，这样却非常方便测试，其他的应用对象通过这种方式访问到上下文。
	 */
	public void setPublishContext(boolean publishContext) {
		this.publishContext = publishContext;
	}

	/**
	 * 设置当前servlet在请求处理后是否触发ServletRequestHandledEvent，默认为true。
	 * 如果ApplicationListener依赖这类事件，可通过关闭此选项来获取少许的性能提升。
	 * @see org.springframework.web.context.support.ServletRequestHandledEvent
	 */
	public void setPublishEvents(boolean publishEvents) {
		this.publishEvents = publishEvents;
	}

	/**
	 * 设置是否暴露LocaleContext和RequestAttributes给子线程继承（使用InheritableThreadLocal）。
	 * 默认为false，避免对生成的后台线程产生影响。
	 * 设置为true，将允许在请求处理过程中后台生成的自定义子线程继承这些属性，且仅限于此请求使用。
	 * 也就是说，在请求的初始任务完成后，这些线程不再复用。
	 * 警告：如果你正在使用线程池，请不要开启子线程继承，因为这将导致继承的上下文会暴露给一个线程池线程，这是非常危险的。
	 */
	public void setThreadContextInheritable(boolean threadContextInheritable) {
		this.threadContextInheritable = threadContextInheritable;
	}

	/**
	 * 设置是否将HTTP OPTIONS请求分发给doService方法。
	 * 默认为false，应用HttpServlet的默认行为（也就是遍历所有的标准HTTP请求方法去响应OPTIONS请求）。
	 * 注意然而从4.3开始，DispatcherServlet默认已开启，因为它内置了对OPTIONS请求的支持。
	 * 如果你希望像处理其他HTTP请求一样，可以开启此项将OPTIONS请求加入请求分发链。
	 * 这也就意味着你的controller会接受到这些请求，不过这里需要保证实际可以处理这些OPTIONS请求。
	 * 注意当请求一个OPTIONS响应时，如果你的controller恰巧没有设置Allow头，HttpServlet的默认OPTIONS处理过程将会被应用。
	 */
	public void setDispatchOptionsRequest(boolean dispatchOptionsRequest) {
		this.dispatchOptionsRequest = dispatchOptionsRequest;
	}

	/**
	 * 设置是否将HTTP TRACE请求分发给doService方法。
	 * 默认为false，应用HttpServlet的默认行为。
	 * 如果你希望像处理其他HTTP请求一样，可以开启此项将OPTIONS请求加入请求分发链。
	 * 这也就意味着你的controller会接受到这些请求，不过这里需要保证实际可以处理这些TRACE请求。
	 * 注意当请求一个TRACE响应时，如果你的controller恰巧没有设置content type 'message/http'，HttpServlet的默认TRACE处理过程将会被应用。
	 */
	public void setDispatchTraceRequest(boolean dispatchTraceRequest) {
		this.dispatchTraceRequest = dispatchTraceRequest;
	}

	/**
	 * 通过实现ApplicationContextAware，spring将会调用此方法注入当前应用上下文。Called by Spring via {@link ApplicationContextAware} to inject the current
	 * FrameworkServlets将会像spring bean一样被注册到一个已存在的web应用上下文当中，而不是通过findWebApplicationContext()方法查找ContextLoaderListener引导获取上下文。
	 * 主要用于支持内嵌servlet容器。
	 * @since 4.0
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		if (this.webApplicationContext == null && applicationContext instanceof WebApplicationContext) {
			this.webApplicationContext = (WebApplicationContext) applicationContext;
			this.webApplicationContextInjected = true;
		}
	}


	/**
	 * 覆写HttpServletBean的方法，在所有bean属性设置后调用，用于创建当前servlet的web应用上下文。
	 */
	@Override
	protected final void initServletBean() throws ServletException {
		getServletContext().log("Initializing Spring FrameworkServlet '" + getServletName() + "'");
		if (this.logger.isInfoEnabled()) {
			this.logger.info("FrameworkServlet '" + getServletName() + "': initialization started");
		}
		long startTime = System.currentTimeMillis();

		try {
			this.webApplicationContext = initWebApplicationContext();
			initFrameworkServlet();
		}
		catch (ServletException | RuntimeException ex) {
			this.logger.error("Context initialization failed", ex);
			throw ex;
		}

		if (this.logger.isInfoEnabled()) {
			long elapsedTime = System.currentTimeMillis() - startTime;
			this.logger.info("FrameworkServlet '" + getServletName() + "': initialization completed in " +
					elapsedTime + " ms");
		}
	}

	/**
	 * 为当前servlet初始化并发布web应用上下文。
	 * 交由createWebApplicationContext()实际创建上下文。可在子类中进行覆写。
	 * @return the WebApplicationContext instance
	 * @see #FrameworkServlet(WebApplicationContext)
	 * @see #setContextClass
	 * @see #setContextConfigLocation
	 */
	protected WebApplicationContext initWebApplicationContext() {
		WebApplicationContext rootContext =
				WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		WebApplicationContext wac = null;

		if (this.webApplicationContext != null) {
			// 构造时注入了上下文实例的话就直接使用它
			wac = this.webApplicationContext;
			if (wac instanceof ConfigurableWebApplicationContext) {
				ConfigurableWebApplicationContext cwac = (ConfigurableWebApplicationContext) wac;
				if (!cwac.isActive()) {
					// 如果上下文还没有刷新，那就会执行设置父上下文/设置上下文id等操作。
					if (cwac.getParent() == null) {
						// 如果上下文实例没有注入显式id，则设置根应用上下文（如果存在，也可能为空）为父上下文。
						cwac.setParent(rootContext);
					}
					configureAndRefreshWebApplicationContext(cwac);
				}
			}
		}
		if (wac == null) {
			// 如果在构造时没有注入上下文实例，则查看servlet上下文中是否有注册。
			// 如果存在，我们默认它已经指定了父上下文，并且已经初始化例如设置上下文id。
			wac = findWebApplicationContext();
		}
		if (wac == null) {
			// 如果在servlet中没有找到，那就创建一个本地上下文。
			wac = createWebApplicationContext(rootContext);
		}

		if (!this.refreshEventReceived) {
			// 如果上下文没有实现ConfigurableApplicationContext没有refresh支持或者上下文在构造注入时已经刷新过了，则在这里需要手动触发初始化刷新
			onRefresh(wac);
		}

		if (this.publishContext) {
			// 发布当前上下文作为servlet上下文属性。
			String attrName = getServletContextAttributeName();
			getServletContext().setAttribute(attrName, wac);
			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Published WebApplicationContext of servlet '" + getServletName() +
						"' as ServletContext attribute with name [" + attrName + "]");
			}
		}

		return wac;
	}

	/**
	 * 使用setContextAttribute配置的名称从ServletContext获取web应用上下文。
	 * 在servlet被初始化或调用前，web应用上下文必须已经加载并存储到servletContext中。
	 * 子类可以根据需要覆写这个方法重新定义获取方式。
	 * @return the WebApplicationContext for this servlet, or {@code null} if not found
	 * @see #getContextAttribute()
	 */
	@Nullable
	protected WebApplicationContext findWebApplicationContext() {
		String attrName = getContextAttribute();
		if (attrName == null) {
			return null;
		}
		WebApplicationContext wac =
				WebApplicationContextUtils.getWebApplicationContext(getServletContext(), attrName);
		if (wac == null) {
			throw new IllegalStateException("No WebApplicationContext found: initializer not registered?");
		}
		return wac;
	}

	/**
	 * 通过默认的XmlWebApplicationContext或者通过setContextClass方法设置的自定义的上下文类为servlet初始化web应用上下文。
	 * 当然，如果是自定义上下文的话，需要实现ConfigurableWebApplicationContext接口。
	 * 可以在子类中覆写此方法。
	 * 不要忘记将此servlet作为应用程序监听器注册到创建的上下文上（因为后续在返回上下文实例前，需要触发onRefresh回调，并调用ConfigurableApplicationContext的refresh方法）。
	 * @param parent the parent ApplicationContext to use, or {@code null} if none
	 * @return the WebApplicationContext for this servlet
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 */
	protected WebApplicationContext createWebApplicationContext(@Nullable ApplicationContext parent) {
		Class<?> contextClass = getContextClass();
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Servlet with name '" + getServletName() +
					"' will try to create custom WebApplicationContext context of class '" +
					contextClass.getName() + "'" + ", using parent context [" + parent + "]");
		}
		if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
			throw new ApplicationContextException(
					"Fatal initialization error in servlet with name '" + getServletName() +
					"': custom WebApplicationContext class [" + contextClass.getName() +
					"] is not of type ConfigurableWebApplicationContext");
		}
		ConfigurableWebApplicationContext wac =
				(ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);

		wac.setEnvironment(getEnvironment());
		wac.setParent(parent);
		String configLocation = getContextConfigLocation();
		if (configLocation != null) {
			wac.setConfigLocation(configLocation);
		}
		configureAndRefreshWebApplicationContext(wac);

		return wac;
	}

	protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext wac) {
		if (ObjectUtils.identityToString(wac).equals(wac.getId())) {
			// The application context id is still set to its original default value
			// -> assign a more useful id based on available information
			if (this.contextId != null) {
				wac.setId(this.contextId);
			}
			else {
				// Generate default id...
				wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX +
						ObjectUtils.getDisplayString(getServletContext().getContextPath()) + '/' + getServletName());
			}
		}

		wac.setServletContext(getServletContext());
		wac.setServletConfig(getServletConfig());
		wac.setNamespace(getNamespace());
		wac.addApplicationListener(new SourceFilteringListener(wac, new ContextRefreshListener()));

		// 在上下文刷新前，初始化web应用上下文环境的属性资源。
		// 在这里提前调用是为了后续的两个方法可以访问到。
		ConfigurableEnvironment env = wac.getEnvironment();
		if (env instanceof ConfigurableWebEnvironment) {
			((ConfigurableWebEnvironment) env).initPropertySources(getServletContext(), getServletConfig());
		}

		postProcessWebApplicationContext(wac);
		applyInitializers(wac);
		wac.refresh();
	}

	/**
	 * 通过默认的XmlWebApplicationContext或者通过setContextClass方法设置的自定义的上下文类为servlet初始化web应用上下文。
	 * 当然，如果是自定义上下文的话，需要实现ConfigurableWebApplicationContext接口。
	 * 可以在子类中覆写此方法。
	 * Delegates to #createWebApplicationContext(ApplicationContext).
	 * @param parent the parent WebApplicationContext to use, or {@code null} if none
	 * @return the WebApplicationContext for this servlet
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 * @see #createWebApplicationContext(ApplicationContext)
	 */
	protected WebApplicationContext createWebApplicationContext(@Nullable WebApplicationContext parent) {
		return createWebApplicationContext((ApplicationContext) parent);
	}

	/**
	 * 在web应用上下文刷新正式成为当前servlet的上下文之前，进行后置处理。
	 * 默认实现为空。refresh()方法将在此方法返回后自动调用。
	 * 注意此方法被设计用于子类修改应用上下文，尽管已经提供了initWebApplicationContext()供终端用户使用ApplicationContextInitializer进行修改。
	 * @param wac the configured WebApplicationContext (not refreshed yet)
	 * @see #createWebApplicationContext
	 * @see #initWebApplicationContext
	 * @see ConfigurableWebApplicationContext#refresh()
	 */
	protected void postProcessWebApplicationContext(ConfigurableWebApplicationContext wac) {
	}

	/**
	 * 在web应用上下文刷新之前，应用contextInitializerClasses指定初始化器进行配置。
	 * 也可以查看postProcessWebApplicationContext方法，面向终端用户的子类修改应用上下文，并且在此方法之前被立即调用。
	 * @param wac the configured WebApplicationContext (not refreshed yet)
	 * @see #createWebApplicationContext
	 * @see #postProcessWebApplicationContext
	 * @see ConfigurableApplicationContext#refresh()
	 */
	protected void applyInitializers(ConfigurableApplicationContext wac) {
		String globalClassNames = getServletContext().getInitParameter(ContextLoader.GLOBAL_INITIALIZER_CLASSES_PARAM);
		if (globalClassNames != null) {
			for (String className : StringUtils.tokenizeToStringArray(globalClassNames, INIT_PARAM_DELIMITERS)) {
				this.contextInitializers.add(loadInitializer(className, wac));
			}
		}

		if (this.contextInitializerClasses != null) {
			for (String className : StringUtils.tokenizeToStringArray(this.contextInitializerClasses, INIT_PARAM_DELIMITERS)) {
				this.contextInitializers.add(loadInitializer(className, wac));
			}
		}

		AnnotationAwareOrderComparator.sort(this.contextInitializers);
		for (ApplicationContextInitializer<ConfigurableApplicationContext> initializer : this.contextInitializers) {
			initializer.initialize(wac);
		}
	}

	@SuppressWarnings("unchecked")
	private ApplicationContextInitializer<ConfigurableApplicationContext> loadInitializer(
			String className, ConfigurableApplicationContext wac) {
		try {
			Class<?> initializerClass = ClassUtils.forName(className, wac.getClassLoader());
			Class<?> initializerContextClass =
					GenericTypeResolver.resolveTypeArgument(initializerClass, ApplicationContextInitializer.class);
			if (initializerContextClass != null && !initializerContextClass.isInstance(wac)) {
				throw new ApplicationContextException(String.format(
						"Could not apply context initializer [%s] since its generic parameter [%s] " +
						"is not assignable from the type of application context used by this " +
						"framework servlet: [%s]", initializerClass.getName(), initializerContextClass.getName(),
						wac.getClass().getName()));
			}
			return BeanUtils.instantiateClass(initializerClass, ApplicationContextInitializer.class);
		}
		catch (ClassNotFoundException ex) {
			throw new ApplicationContextException(String.format("Could not load class [%s] specified " +
					"via 'contextInitializerClasses' init-param", className), ex);
		}
	}

	/**
	 * 返回servlet的web应用上下文的servlet上下文属性名称。
	 * 默认实现返回 SERVLET_CONTEXT_PREFIX + servlet name
	 * @see #SERVLET_CONTEXT_PREFIX
	 * @see #getServletName
	 */
	public String getServletContextAttributeName() {
		return SERVLET_CONTEXT_PREFIX + getServletName();
	}

	/**
	 * Return this servlet's WebApplicationContext.
	 */
	@Nullable
	public final WebApplicationContext getWebApplicationContext() {
		return this.webApplicationContext;
	}


	/**
	 * 在所有bean属性已设置，且web应用上下文已加载时调用，默认实现为空。
	 * 子类可以覆写此方法指定自定义初始化需求。
	 * @throws ServletException in case of an initialization exception
	 */
	protected void initFrameworkServlet() throws ServletException {
	}

	/**
	 * 可以理解为servlet是有状态的，需要对servlet的上下文进行刷新
	 * @see #getWebApplicationContext()
	 * @see org.springframework.context.ConfigurableApplicationContext#refresh()
	 */
	public void refresh() {
		WebApplicationContext wac = getWebApplicationContext();
		if (!(wac instanceof ConfigurableApplicationContext)) {
			throw new IllegalStateException("WebApplicationContext does not support refresh: " + wac);
		}
		((ConfigurableApplicationContext) wac).refresh();
	}

	/**
	 * 收到来自servlet的web应用上下文刷新事件后进行回调。
	 * 触发servlet的上下文依赖状态刷新，将会调用默认实现onRefresh。
	 * @param event the incoming ApplicationContext event
	 */
	public void onApplicationEvent(ContextRefreshedEvent event) {
		this.refreshEventReceived = true;
		onRefresh(event.getApplicationContext());
	}

	/**
	 * 这是一个模版方法，可以被覆写添加一些servlet相关的刷新工作。
	 * 上下文刷新后调用。
	 * 默认实现为空。
	 * @param context the current WebApplicationContext
	 * @see #refresh()
	 */
	protected void onRefresh(ApplicationContext context) {
		// 供子类使用: 默认什么都不干
	}

	/**
	 * 关闭servlet的web应用上下文。
	 * @see org.springframework.context.ConfigurableApplicationContext#close()
	 */
	@Override
	public void destroy() {
		getServletContext().log("Destroying Spring FrameworkServlet '" + getServletName() + "'");
		// Only call close() on WebApplicationContext if locally managed...
		if (this.webApplicationContext instanceof ConfigurableApplicationContext && !this.webApplicationContextInjected) {
			((ConfigurableApplicationContext) this.webApplicationContext).close();
		}
	}


	/**
	 * 覆写父类方法实现拦截PATCH请求。
	 */
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpMethod httpMethod = HttpMethod.resolve(request.getMethod());
		if (httpMethod == HttpMethod.PATCH || httpMethod == null) {
			processRequest(request, response);
		}
		else {
			super.service(request, response);
		}
	}

	/**
	 * 将GET请求传给processRequest/doService方法。
	 * 也会被HttpServlet的doHead的默认实现调用（只有内容长度，没有响应体）。
	 * @see #doService
	 * @see #doHead
	 */
	@Override
	protected final void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		processRequest(request, response);
	}

	/**
	 * 将POST请求传给processRequest。
	 * @see #doService
	 */
	@Override
	protected final void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		processRequest(request, response);
	}

	/**
	 * 将PUT请求传给processRequest。
	 * @see #doService
	 */
	@Override
	protected final void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		processRequest(request, response);
	}

	/**
	 * 将DELETE请求传给processRequest。
	 * @see #doService
	 */
	@Override
	protected final void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		processRequest(request, response);
	}

	/**
	 * 如果需要的话，将OPTIONS请求传给processRequest。
	 * 如果分发时仍然没有设置Allow头，HttpServlet的标准OPTIONS处理过程仍将生效。
	 * @see #doService
	 */
	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (this.dispatchOptionsRequest || CorsUtils.isPreFlightRequest(request)) {
			processRequest(request, response);
			if (response.containsHeader("Allow")) {
				// 恰当处理OPTIONS请求，框架已经做了
				return;
			}
		}

		// 使用响应封装添加PATCH的允许方法中。
		super.doOptions(request, new HttpServletResponseWrapper(response) {
			@Override
			public void setHeader(String name, String value) {
				if ("Allow".equals(name)) {
					value = (StringUtils.hasLength(value) ? value + ", " : "") + HttpMethod.PATCH.name();
				}
				super.setHeader(name, value);
			}
		});
	}

	/**
	 * 如果需要，将TRACE请求传给processRequest。
	 * 否则应用HttpServlet的标准TRACE处理过程。
	 * @see #doService
	 */
	@Override
	protected void doTrace(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (this.dispatchTraceRequest) {
			processRequest(request, response);
			if ("message/http".equals(response.getContentType())) {
				// Proper TRACE response coming from a handler - we're done.
				return;
			}
		}
		super.doTrace(request, response);
	}

	/**
	 * 处理请求，无论结果如何，发布事件。Process this request, publishing an event regardless of the outcome.
	 * 实际的事件处理是由抽象模板方法doService执行的。
	 */
	protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		long startTime = System.currentTimeMillis();
		Throwable failureCause = null;

		LocaleContext previousLocaleContext = LocaleContextHolder.getLocaleContext();
		LocaleContext localeContext = buildLocaleContext(request);

		RequestAttributes previousAttributes = RequestContextHolder.getRequestAttributes();
		ServletRequestAttributes requestAttributes = buildRequestAttributes(request, response, previousAttributes);

		WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
		asyncManager.registerCallableInterceptor(FrameworkServlet.class.getName(), new RequestBindingInterceptor());

		initContextHolders(request, localeContext, requestAttributes);

		try {
			doService(request, response);
		}
		catch (ServletException | IOException ex) {
			failureCause = ex;
			throw ex;
		}
		catch (Throwable ex) {
			failureCause = ex;
			throw new NestedServletException("Request processing failed", ex);
		}

		finally {
			resetContextHolders(request, previousLocaleContext, previousAttributes);
			if (requestAttributes != null) {
				requestAttributes.requestCompleted();
			}

			if (logger.isDebugEnabled()) {
				if (failureCause != null) {
					this.logger.debug("Could not complete request", failureCause);
				}
				else {
					if (asyncManager.isConcurrentHandlingStarted()) {
						logger.debug("Leaving response open for concurrent processing");
					}
					else {
						this.logger.debug("Successfully completed request");
					}
				}
			}

			publishRequestHandledEvent(request, response, startTime, failureCause);
		}
	}

	/**
	 * 为给定请求构造本地信息上下文，将请求的初始本地信息作为当前本地信息暴露出去。
	 * @param request current HTTP request
	 * @return the corresponding LocaleContext, or {@code null} if none to bind
	 * @see LocaleContextHolder#setLocaleContext
	 */
	@Nullable
	protected LocaleContext buildLocaleContext(HttpServletRequest request) {
		return new SimpleLocaleContext(request.getLocale());
	}

	/**
	 * 为给定请求构造servlet请求属性（也可能还带有一个响应的引用），处理之前绑定属性及它们的类型。
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param previousAttributes pre-bound RequestAttributes instance, if any
	 * @return the ServletRequestAttributes to bind, or {@code null} to preserve
	 * the previously bound instance (or not binding any, if none bound before)
	 * @see RequestContextHolder#setRequestAttributes
	 */
	@Nullable
	protected ServletRequestAttributes buildRequestAttributes(HttpServletRequest request,
			@Nullable HttpServletResponse response, @Nullable RequestAttributes previousAttributes) {

		if (previousAttributes == null || previousAttributes instanceof ServletRequestAttributes) {
			return new ServletRequestAttributes(request, response);
		}
		else {
			return null;  // 可以考虑保存之前绑定请求属性实例
		}
	}

	private void initContextHolders(HttpServletRequest request,
			@Nullable LocaleContext localeContext, @Nullable RequestAttributes requestAttributes) {

		if (localeContext != null) {
			LocaleContextHolder.setLocaleContext(localeContext, this.threadContextInheritable);
		}
		if (requestAttributes != null) {
			RequestContextHolder.setRequestAttributes(requestAttributes, this.threadContextInheritable);
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Bound request context to thread: " + request);
		}
	}

	private void resetContextHolders(HttpServletRequest request,
			@Nullable LocaleContext prevLocaleContext, @Nullable RequestAttributes previousAttributes) {

		LocaleContextHolder.setLocaleContext(prevLocaleContext, this.threadContextInheritable);
		RequestContextHolder.setRequestAttributes(previousAttributes, this.threadContextInheritable);
		if (logger.isTraceEnabled()) {
			logger.trace("Cleared thread-bound request context: " + request);
		}
	}

	private void publishRequestHandledEvent(HttpServletRequest request, HttpServletResponse response,
			long startTime, @Nullable Throwable failureCause) {

		if (this.publishEvents && this.webApplicationContext != null) {
			// 无论请求是否处理成功，发布请求已处理事件
			long processingTime = System.currentTimeMillis() - startTime;
			this.webApplicationContext.publishEvent(
					new ServletRequestHandledEvent(this,
							request.getRequestURI(), request.getRemoteAddr(),
							request.getMethod(), getServletConfig().getServletName(),
							WebUtils.getSessionId(request), getUsernameForRequest(request),
							processingTime, failureCause, response.getStatus()));
		}
	}

	/**
	 * 获取给定请求用户名。
	 * 默认取UserPrincipal的名称。
	 * 可由子类覆写。
	 * @param request current HTTP request
	 * @return the username, or {@code null} if none found
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	@Nullable
	protected String getUsernameForRequest(HttpServletRequest request) {
		Principal userPrincipal = request.getUserPrincipal();
		return (userPrincipal != null ? userPrincipal.getName() : null);
	}


	/**
	 * 子类必须实现此方法处理请求，以此为中心接收GET, POST, PUT and DELETE的回调。
	 * HttpServlet的doGet和doPost的方法覆写规则基本一致。
	 * 当前类拦截调用保证异常处理和事件发布正常。
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws Exception in case of any kind of processing failure
	 * @see javax.servlet.http.HttpServlet#doGet
	 * @see javax.servlet.http.HttpServlet#doPost
	 */
	protected abstract void doService(HttpServletRequest request, HttpServletResponse response)
			throws Exception;


	/**
	 * 监听器
	 * 通过FrameworkServlet的onApplicationEvent建立关联，只接收来自当前servlet的web应用上下文的事件。
	 */
	private class ContextRefreshListener implements ApplicationListener<ContextRefreshedEvent> {

		@Override
		public void onApplicationEvent(ContextRefreshedEvent event) {
			FrameworkServlet.this.onApplicationEvent(event);
		}
	}


	/**
	 * 回调处理拦截器实现
	 * 初始化/重置FrameworkServlet的上下文缓存。
	 */
	private class RequestBindingInterceptor implements CallableProcessingInterceptor {

		@Override
		public <T> void preProcess(NativeWebRequest webRequest, Callable<T> task) {
			HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
			if (request != null) {
				HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
				initContextHolders(request, buildLocaleContext(request),
						buildRequestAttributes(request, response, null));
			}
		}
		@Override
		public <T> void postProcess(NativeWebRequest webRequest, Callable<T> task, Object concurrentResult) {
			HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
			if (request != null) {
				resetContextHolders(request, null, null);
			}
		}
	}

}
