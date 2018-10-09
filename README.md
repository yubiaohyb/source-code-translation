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



>#### DispatcherServlet的handler/adapter是如何获取的？handler又是如何执行的？ ####
