个人解读路线
====
>#### RequestMappingHandlerMapping是如何完成处理器方法的的扫描注册的？ ####
    RequestMappingHandlerMapping在完成bean的构造注入后，会调用生命周期的afterPropertiesSet方法。
    方法首先会对配置属性进行一系列的初始化（包括UrlPathHelper和PathMatcher等）；
    然后调用super.afterPropertiesSet()进一步地配置。
    
    父类RequestMappingInfoHandlerMapping并没有对此方法进行覆写，而是直接调用了祖先类（爷爷类）AbstractHandlerMethodMapping的实现。
    方法实现会在初始化阶段进行处理器方法（HttpMethod）的扫描，并将其缓存在MappingRegistry中。
    具体实现：
    根据配置从应用上下文中获取所有的bean名称，然后根据bean名称去获取bean的类型，并判断bean是否是处理器类型（是否有@Controller或@RequestMapping）注解；
    如果是，则根据处理器的用户定义的原始类型检查获取对应的HttpMethod/RequestCondition；
    最后将经aop处理后的结果连同处理器一起添加到MappingRegistry中，方便以后使用。
