个人解读路线
====
>#### DispatcherServlet的init过程是怎样的？ ####
    DispatcherServlet继承关系如下：
    HttpServlet ==> HttpServletBean ==> FrameworkServlet ==> DispatcherServlet
    
    HttpServlet为web容器提供了init/service/destroy三个生命周期接口。
    我们可以覆写doXXX实现相应请求的处理，也可以通过ServletConfig属性获取容器环境（servletContext）或者进行servlet初始化配置等；
    
    HttpServletBean开始以JavaBean的方式处理属性设置，会在ServletConfig中的查找必需的初始化参数，自动进行类型转换并配置。
    实现了EnvironmentCapable和EnvironmentAware接口，可用于根据系统环境参数等进行相应的一些调整。
    
    FrameworkServlet是spring web框架的基础servlet。
    实现上开始提供应用上下文的处理。

    DispatcherServlet实现了FrameworkServlet的doService方法，负责最终的请求分发/处理/返回。
    
    web容器启动过程中会对其下配置的servlet进行初始化，首先在web.xml中的初始化参数和容器信息会被封装在ServletConfig中，
    servlet初始化时，会先调用HttpServletBean的init方法完成对基本初始化参数和上下文环境的配置，然后来到FrameworkServlet中执行覆写的initServletBean方法。
    初始化web应用上下文：
    然后再额外进行一些其他的配置，默认什么都不做。


>#### RequestMappingHandlerMapping是如何完成处理器方法的扫描注册的？ ####
    RequestMappingHandlerMapping在完成bean的构造注入后，会调用生命周期的afterPropertiesSet方法。
    方法首先会对配置属性进行一系列的初始化（包括UrlPathHelper和PathMatcher等）；
    然后调用super.afterPropertiesSet()进一步地配置。
    
    父类RequestMappingInfoHandlerMapping并没有对此方法进行覆写，而是直接调用了祖先类（爷爷类）AbstractHandlerMethodMapping的实现。
    方法实现会在初始化阶段进行处理器方法（HttpMethod）的扫描，并将其缓存在MappingRegistry中。
    具体实现：
        根据配置从应用上下文中获取所有的bean名称，然后根据bean名称去获取bean的类型，并判断bean是否是处理器类型（是否有@Controller或@RequestMapping）注解；
        如果是，则根据处理器的用户定义的原始类型检查获取对应的HttpMethod/RequestCondition；
        最后将经aop处理后的结果连同处理器一起添加到MappingRegistry中，方便以后使用。
    
>#### DispatcherServlet的service处理基本流程？ ####
    HttpServlet虽然为web容器提供了init/service/destroy三个生命周期接口，但是作为抽象虚拟超类是没有办法直接使用的。
    在设计，service在接收到HTTP请求后，会交由保护方法doGet、doPost、doPut、doDelete等处理。
    doXXX默认实现会返回异常信息，这样做的好处是子类可以根据需要对特定请求方式的请求进行处理，限制请求接收的方式类型，定制化一些东西。
    
    FrameworkServlet在继承HttpServlet后，对service以及各个doXXX方法进行了覆写。
    FrameworkServlet定义了一个通用化的processRequest(request, response)方法供service以及各个doXXX方法处理请求。
    FrameworkServlet的service方法在接收到请求后，首先判断如果请求方式为PATCH或空，是则直接调用processRequest，否则调用super.service转而去调用各个覆写的doXXX方法。
    processRequest的处理过程：
        注册异步处理管理器，根据本地化上下文和请求属性初始化上下文容器；
        调用doService(request, response)（抽象方法）；
        重置上下文容器，清理请求属性；
        发布请求已处理事件。
    
    DispatcherServlet覆写doService方法：
        Include请求拍属性快照；
        请求属性添加DispatcherServlet属性（web应用上下文/主题解析器/本地化解析器/闪存）；
        调用doDispatch(request, response)；
        恢复请求属性快照。
    
    doDispatch(request, response)的执行过程：
        检查请求是否含文件上传，并做处理；
        获取处理器（执行链），并做后备处理；
        获取处理器适配器；
        处理最后修改请求头；
        拦截器预处理（preHandle）；
        处理器处理；
        如果是异步处理则跳出处理等待最后的回调；
        如果返回的ModelAndView的视图未知，则设置默认视图；
        拦截器后置处理（postHandle）；
        调用processDispatchResult进行最终的视图渲染的相关工作；
        无论此过程都会调用triggerAfterCompletion回调处理器拦截器的afterCompletion方法；
        最后如果请求是同步处理，则对上传文件请求释放所占用的一些资源；
        否则，触发处理器拦截器afterConcurrentHandlingStarted回调；
    

>#### DispatcherServlet的handler/adapter是如何获取的？handler又是如何执行的？ ####
