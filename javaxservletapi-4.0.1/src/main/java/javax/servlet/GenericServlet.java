/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 *
 * 定义了一个通用，与协议无关的servlet。
 * 如果想写一个Web上使用的HTTP协议的servlet，可以继承HttpServlet。
 * Defines a generic, protocol-independent
 * servlet. To write an HTTP servlet for use on the
 * Web, extend {@link javax.servlet.http.HttpServlet} instead.
 *
 * GenericServlet实现了Servlet和ServletConfig接口。
 * 虽然继承一个指定协议的子类例如HttpServlet更常见，但是直接继承GenericServlet也是可以的。
 * <p><code>GenericServlet</code> implements the <code>Servlet</code>
 * and <code>ServletConfig</code> interfaces. <code>GenericServlet</code>
 * may be directly extended by a servlet, although it's more common to extend
 * a protocol-specific subclass such as <code>HttpServlet</code>.
 *
 * GenericServlet简化了servlet的编写，提供了servlet接口生命周期方法以及servletConfig接口方法的简化版本，
 * 同时还实现了在servletContext声明的log方法。
 * <p><code>GenericServlet</code> makes writing servlets
 * easier. It provides simple versions of the lifecycle methods 
 * <code>init</code> and <code>destroy</code> and of the methods 
 * in the <code>ServletConfig</code> interface. <code>GenericServlet</code>
 * also implements the <code>log</code> method, declared in the
 * <code>ServletContext</code> interface. 
 *
 * 写一个通用的servlet，现在你只需要简单覆盖抽象方法service就可以了。
 * <p>To write a generic servlet, you need only
 * override the abstract <code>service</code> method. 
 *
 *
 * @author 	Various
 */

 
public abstract class GenericServlet 
    implements Servlet, ServletConfig, java.io.Serializable
{
    private static final String LSTRING_FILE = "javax.servlet.LocalStrings";
    private static ResourceBundle lStrings =
        ResourceBundle.getBundle(LSTRING_FILE);

    private transient ServletConfig config;
    

    /**
     *
     * 什么都不做。servlet的所有初始化工作由init方法完成。
     * Does nothing. All of the servlet initialization
     * is done by one of the <code>init</code> methods.
     *
     */
    public GenericServlet() { }
    
    
    /**
     * servlet容器调用此方法将servlet移出服务
     * Called by the servlet container to indicate to a servlet that the
     * servlet is being taken out of service.  See {@link Servlet#destroy}.
     *
     * 
     */
    public void destroy() {
    }
    
    
    /**
     * 根据名称获取初始化参数值
     * Returns a <code>String</code> containing the value of the named
     * initialization parameter, or <code>null</code> if the parameter does
     * not exist.  See {@link ServletConfig#getInitParameter}.
     *
     * 提供这个方法是为了简化从servletConfig获取初始化参数值
     * <p>This method is supplied for convenience. It gets the
     * value of the named parameter from the servlet's 
     * <code>ServletConfig</code> object.
     *
     * @param name 		a <code>String</code> specifying the name 
     *				of the initialization parameter
     *
     * @return String 		a <code>String</code> containing the value
     *				of the initialization parameter
     *
     */ 
    public String getInitParameter(String name) {
        ServletConfig sc = getServletConfig();
        if (sc == null) {
            throw new IllegalStateException(
                lStrings.getString("err.servlet_config_not_initialized"));
        }

        return sc.getInitParameter(name);
    }
    
    
   /**
    * 返回初始化参数名集合
    * Returns the names of the servlet's initialization parameters
    * as an <code>Enumeration</code> of <code>String</code> objects,
    * or an empty <code>Enumeration</code> if the servlet has no
    * initialization parameters.  See {@link
    * ServletConfig#getInitParameterNames}.
    *
    * 同样是为了简化访问
    * <p>This method is supplied for convenience. It gets the
    * parameter names from the servlet's <code>ServletConfig</code> object. 
    *
    *
    * @return Enumeration 	an enumeration of <code>String</code>
    *				objects containing the names of 
    *				the servlet's initialization parameters
    */
    public Enumeration<String> getInitParameterNames() {
        ServletConfig sc = getServletConfig();
        if (sc == null) {
            throw new IllegalStateException(
                lStrings.getString("err.servlet_config_not_initialized"));
        }

        return sc.getInitParameterNames();
    }   
     

    /**
     * 返回servlet的ServletConfig
     * Returns this servlet's {@link ServletConfig} object.
     *
     * @return ServletConfig 	the <code>ServletConfig</code> object
     *				that initialized this servlet
     */    
    public ServletConfig getServletConfig() {
	return config;
    }
 
    
    /**
     * 返回servlet运行所处的运行上下文
     * Returns a reference to the {@link ServletContext} in which this servlet
     * is running.  See {@link ServletConfig#getServletContext}.
     *
     * 简化从ServletConfig处对ServletContext的获取
     * <p>This method is supplied for convenience. It gets the
     * context from the servlet's <code>ServletConfig</code> object.
     *
     *
     * @return ServletContext 	the <code>ServletContext</code> object
     *				passed to this servlet by the <code>init</code>
     *				method
     */
    public ServletContext getServletContext() {
        ServletConfig sc = getServletConfig();
        if (sc == null) {
            throw new IllegalStateException(
                lStrings.getString("err.servlet_config_not_initialized"));
        }

        return sc.getServletContext();
    }


    /**
     * 返回servlet默认信息，例如作者、版本、版权。
     * 默认返回空字符串，覆盖此方法返回一个有意义的值。
     * Returns information about the servlet, such as
     * author, version, and copyright. 
     * By default, this method returns an empty string.  Override this method
     * to have it return a meaningful value.  See {@link
     * Servlet#getServletInfo}.
     *
     *
     * @return String 		information about this servlet, by default an
     * 				empty string
     */    
    public String getServletInfo() {
	return "";
    }


    /**
     * servlet容器调用此方法配置提供服务的servlet
     * Called by the servlet container to indicate to a servlet that the
     * servlet is being placed into service.  See {@link Servlet#init}.
     *
     * 实现中保存了ServletConfig对象，方便后续需要的时候使用。
     * 覆写方法时调用super.init(config)
     * <p>This implementation stores the {@link ServletConfig}
     * object it receives from the servlet container for later use.
     * When overriding this form of the method, call 
     * <code>super.init(config)</code>.
     *
     * @param config 			the <code>ServletConfig</code> object
     *					that contains configuration
     *					information for this servlet
     *
     * @exception ServletException 	if an exception occurs that
     *					interrupts the servlet's normal
     *					operation
     * 
     * @see 				UnavailableException
     */
    public void init(ServletConfig config) throws ServletException {
	      this.config = config;
	      this.init();
    }


    /**
     * 方法可被覆写，简化对super.init(config)调用
     * A convenience method which can be overridden so that there's no need
     * to call <code>super.init(config)</code>.
     *
     * 简单地覆盖这个方法，方法会被GenericServlet.init(ServletConfig config)调用，而不是去覆写init(ServletConfig)。
     * ServletConfig对象保存了下来，我们仍然可以通过getServletConfig方法取得。
     * <p>Instead of overriding {@link #init(ServletConfig)}, simply override
     * this method and it will be called by
     * <code>GenericServlet.init(ServletConfig config)</code>.
     * The <code>ServletConfig</code> object can still be retrieved via {@link
     * #getServletConfig}. 
     *
     * @exception ServletException 	if an exception occurs that
     *					interrupts the servlet's
     *					normal operation
     */
    public void init() throws ServletException {

    }
    

    /**
     * 添加指定信息到servlet日志文件中，以servlet名称开头。
     * Writes the specified message to a servlet log file, prepended by the
     * servlet's name.  See {@link ServletContext#log(String)}.
     *
     * @param msg 	a <code>String</code> specifying
     *			the message to be written to the log file
     */     
    public void log(String msg) {
	getServletContext().log(getServletName() + ": "+ msg);
    }
   
   
    /**
     * 为异常添加解释性信息和栈路径到servlet日志文件中，以servlet名称开头。
     * Writes an explanatory message and a stack trace
     * for a given <code>Throwable</code> exception
     * to the servlet log file, prepended by the servlet's name.
     * See {@link ServletContext#log(String, Throwable)}.
     *
     *
     * @param message 		a <code>String</code> that describes
     *				the error or exception
     *
     * @param t			the <code>java.lang.Throwable</code> error
     * 				or exception
     */   
    public void log(String message, Throwable t) {
	      getServletContext().log(getServletName() + ": " + message, t);
    }
    
    
    /**
     * servlet容器会调用此方法允许servlet响应请求
     * Called by the servlet container to allow the servlet to respond to
     * a request.  See {@link Servlet#service}.
     * 
     * 本方法被声明为抽象，因此子类如HttpServlet必须覆盖它。
     * <p>This method is declared abstract so subclasses, such as
     * <code>HttpServlet</code>, must override it.
     *
     * @param req 	the <code>ServletRequest</code> object
     *			that contains the client's request
     *
     * @param res 	the <code>ServletResponse</code> object
     *			that will contain the servlet's response
     *
     * @exception ServletException 	if an exception occurs that
     *					interferes with the servlet's
     *					normal operation occurred
     *
     * @exception IOException 		if an input or output
     *					exception occurs
     */

    public abstract void service(ServletRequest req, ServletResponse res)
	throws ServletException, IOException;
    

    /**
     * 返回servlet实例名称
     * Returns the name of this servlet instance.
     * See {@link ServletConfig#getServletName}.
     *
     * @return          the name of this servlet instance
     */
    public String getServletName() {
        ServletConfig sc = getServletConfig();
        if (sc == null) {
            throw new IllegalStateException(
                lStrings.getString("err.servlet_config_not_initialized"));
        }

        return sc.getServletName();
    }
}
