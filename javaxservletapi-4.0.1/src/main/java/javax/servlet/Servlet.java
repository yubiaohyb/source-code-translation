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


/**
 * 这个接口定义了所有servlet必须实现的方法。
 * Defines methods that all servlets must implement.
 *
 * servlet是运行在web服务器中的java小程序，通常通过HTTP协议接收、响应来自web客户端的请求。
 * <p>A servlet is a small Java program that runs within a Web server.
 * Servlets receive and respond to requests from Web clients,
 * usually across HTTP, the HyperText Transfer Protocol. 
 *
 * 我们可以通过继承GenericServlet或HttpServlet来实现这个接口。
 * <p>To implement this interface, you can write a generic servlet
 * that extends
 * <code>javax.servlet.GenericServlet</code> or an HTTP servlet that
 * extends <code>javax.servlet.http.HttpServlet</code>.
 *
 * 接口定义的方法包括对servlet的初始化、请求处理以及将servlet移出服务器。
 * 这些所谓的生命周期方法会按照以下顺序被调用：
 * 构造servlet时，调用init方法进行初始化；
 * 稍后，所有来自客户端的请求会service方法来处理；
 * servlet停止服务后，将会调用destroy方法进行销毁，然后就是垃圾收集及最终处理结束。
 * <p>This interface defines methods to initialize a servlet,
 * to service requests, and to remove a servlet from the server.
 * These are known as life-cycle methods and are called in the
 * following sequence:
 * <ol>
 * <li>The servlet is constructed, then initialized with the <code>init</code> method.
 * <li>Any calls from clients to the <code>service</code> method are handled.
 * <li>The servlet is taken out of service, then destroyed with the 
 * <code>destroy</code> method, then garbage collected and finalized.
 * </ol>
 *
 * 除了生命周期方法，接口另外还提供了getServletConfig方法来获取启动信息，getServletInfo返回servlet自身信息，如作者、版本、版权。
 * <p>In addition to the life-cycle methods, this interface
 * provides the <code>getServletConfig</code> method, which the servlet 
 * can use to get any startup information, and the <code>getServletInfo</code>
 * method, which allows the servlet to return basic information about itself,
 * such as author, version, and copyright.
 *
 * @author 	Various
 *
 * @see 	GenericServlet
 * @see 	javax.servlet.http.HttpServlet
 *
 */


public interface Servlet {

    /**
     * servlet容器调用此方法指定提供服务的servlet
     * Called by the servlet container to indicate to a servlet that the
     * servlet is being placed into service.
     *
     * servlet实例化后，servlet容器会调用init方法一次进行初始化配置，
     * 且在servlet接收请求前init方法必须成功完成。
     * <p>The servlet container calls the <code>init</code>
     * method exactly once after instantiating the servlet.
     * The <code>init</code> method must complete successfully
     * before the servlet can receive any requests.
     *
     * 如果调用init方法抛出了一个ServletException或者在调用超时，
     * 则servlet容器不应该使用此servlet提供服务。
     * <p>The servlet container cannot place the servlet into service
     * if the <code>init</code> method
     * <ol>
     * <li>Throws a <code>ServletException</code>
     * <li>Does not return within a time period defined by the Web server
     * </ol>
     *
     *
     * @param config			a <code>ServletConfig</code> object 
     *					containing the servlet's
     * 					configuration and initialization parameters
     *
     * @exception ServletException 	if an exception has occurred that
     *					interferes with the servlet's normal
     *					operation
     *
     * @see 				UnavailableException
     * @see 				#getServletConfig
     *
     */

    public void init(ServletConfig config) throws ServletException;
    
    

    /**
     *
     * 返回servlet配置对象，其中包含了servlet的初始化和启动信息，也就是传递给init方法的那个对象。
     * Returns a {@link ServletConfig} object, which contains
     * initialization and startup parameters for this servlet.
     * The <code>ServletConfig</code> object returned is the one 
     * passed to the <code>init</code> method. 
     *
     * 实现本方法的前提是保存了servlet的配置对象，在GenericServlet类中已经对此做了实现。
     * <p>Implementations of this interface are responsible for storing the
     * <code>ServletConfig</code> object so that this 
     * method can return it. The {@link GenericServlet}
     * class, which implements this interface, already does this.
     *
     * @return		the <code>ServletConfig</code> object
     *			that initializes this servlet
     *
     * @see 		#init
     *
     */

    public ServletConfig getServletConfig();
    
    

    /**
     * servlet容器会调用此方法允许servlet响应请求
     * Called by the servlet container to allow the servlet to respond to
     * a request.
     *
     * 只有servlet容器成功完成了对servlet的init方法的调用，才会调用此方法。
     * <p>This method is only called after the servlet's <code>init()</code>
     * method has completed successfully.
     * 
     * servlet抛出或发送错误信息始终也应该设置响应状态码。
     * <p>  The status code of the response always should be set for a servlet
     * that throws or sends an error.
     *
     * 
     * servlet通常运行于可以并发处理多个请求的多线程servlet容器中。
     * 开发者必须意识到，除了servlet的类和实例变量，对于其他的共享资源例如文件、网络连接的访问也需要进行同步化处理。
     * 更多关于java多线程编程的信息可以查看the Java tutorial on multi-threaded programming。
     * 链接：http://java.sun.com/Series/Tutorial/java/threads/multithreaded.html
     * <p>Servlets typically run inside multithreaded servlet containers
     * that can handle multiple requests concurrently. Developers must 
     * be aware to synchronize access to any shared resources such as files,
     * network connections, and as well as the servlet's class and instance 
     * variables. 
     * More information on multithreaded programming in Java is available in 
     * <a href="http://java.sun.com/Series/Tutorial/java/threads/multithreaded.html">
     * the Java tutorial on multi-threaded programming</a>.
     *
     *
     * @param req 	the <code>ServletRequest</code> object that contains
     *			the client's request
     *
     * @param res 	the <code>ServletResponse</code> object that contains
     *			the servlet's response
     *
     * @exception ServletException 	if an exception occurs that interferes
     *					with the servlet's normal operation 
     *
     * @exception IOException 		if an input or output exception occurs
     *
     */

    public void service(ServletRequest req, ServletResponse res)
	throws ServletException, IOException;
	
	

    /**
     * 返回servlet信息，例如作者、版本、版权
     * 返回的字符串应该是纯文本，而不是什么标记语言字符串。
     * Returns information about the servlet, such
     * as author, version, and copyright.
     * 
     * <p>The string that this method returns should
     * be plain text and not markup of any kind (such as HTML, XML,
     * etc.).
     *
     * @return 		a <code>String</code> containing servlet information
     *
     */

    public String getServletInfo();
    
    

    /**
     *
     * servlet容器调用此方法将servlet移出服务。
     * 只有当所有工作线程都完成service方法调用或超时时，本方法才能被调用，而且就这么一次。
     * 调用本方法后，servelt容器不会再调用servlet的service方法提供服务。
     * Called by the servlet container to indicate to a servlet that the
     * servlet is being taken out of service.  This method is
     * only called once all threads within the servlet's
     * <code>service</code> method have exited or after a timeout
     * period has passed. After the servlet container calls this 
     * method, it will not call the <code>service</code> method again
     * on this servlet.
     *
     * 方法提供了资源释放的能力（例如内存、文件处理、线程）。
     * 需要确保内存中servlet的当前状态与持久化状态保持同步。<p>This method gives the servlet an opportunity
     * to clean up any resources that are being held (for example, memory,
     * file handles, threads) and make sure that any persistent state is
     * synchronized with the servlet's current state in memory.
     *
     */

    public void destroy();
}
