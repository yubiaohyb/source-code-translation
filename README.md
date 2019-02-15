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
        根据配置从应用上下文中获取所有的bean名称，然后根据bean名称去获取bean的类型，并判断bean是否是处理器类型（是否有@Controller或@RequestMapping）；
        如果是，则根据处理器的用户定义的原始类型检查获取对应的HttpMethod/RequestCondition；
        最后将经aop处理后的结果连同处理器一起添加到MappingRegistry中，方便以后使用。
    
>#### DispatcherServlet的service处理基本流程？ ####
    HttpServlet
    虽然为web容器提供了init/service/destroy三个生命周期接口，但是作为抽象虚拟超类是没有办法直接使用的。
    service在接收到HTTP请求后，会转由保护方法doGet、doPost、doPut、doDelete等处理，而doXXX默认实现会返回异常信息。
    这样做的好处是子类可以根据需要对特定请求方式的请求进行处理，限制请求接收的方式类型，定制化一些东西。
    
    FrameworkServlet
    在继承HttpServlet后，对service以及各个doXXX方法进行了覆写。
    定义了一个通用化的processRequest(request, response)方法供service以及各个doXXX方法处理请求。
    service方法在接收到请求后，首先判断如果请求方式为PATCH或空，是则直接调用processRequest，否则调用super.service转而去调用各个覆写的doXXX方法。
    processRequest的处理过程：
        注册异步处理管理器，根据本地化上下文和请求属性初始化上下文容器；
        调用doService(request, response)（抽象方法）；
        重置上下文容器，清理请求属性；
        发布请求已处理事件。
    
    DispatcherServlet
    覆写doService实现：
        Include请求进行属性快照；
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
        否则，触发处理器拦截器afterConcurrentHandlingStarted回调。
    processDispatchResult实现：
        异常检查，并做相应ModelAndView处理；
        判断是否进行视图渲染；
        如果发生跳转时并发处理，则返回；
        负责尝试触发处理器拦截器AfterCompletion回调。
        
    

>#### DispatcherServlet的handler/adapter是如何获取的？handler又是如何执行的？ ####
```java
protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
    if (this.handlerMappings != null) {
        for (HandlerMapping hm : this.handlerMappings) {
            HandlerExecutionChain handler = hm.getHandler(request);
            if (handler != null) {
                return handler;
            }
        }
    }
    return null;
}
```
	如上，DispatcherServlet会根据handlerMappings遍历获取匹配的处理器（执行链）。
	这里我们以RequestMappingHandlerMapping举例：
	本身RequestMappingHandlerMapping是没有实现getHandler方法的，而继承了来自AbstractHandlerMapping的实现。
	这里，我们简单看一下继承/实现关系，并稍作说明：
	继承/实现关系：
	HandlerMapping --> AbstractHandlerMapping --> AbstractHandlerMethodMapping --> RequestMappingInfoHandlerMapping --> RequestMappingHandlerMapping
    
    AbstractHandlerMapping的getHandler实现过程：
    调用getHandlerInternal尝试获取处理器；
    如果为空，则取默认处理器；
    如果处理器值为字符串类型，则需要从应用上下文中按名获取；
    获取处理器执行链；
    如果跨域，做相应处理；
    返回处理器执行链。
    
---
spring boot升级

	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-properties-migrator</artifactId>
		<scope>runtime</scope>
	</dependency>

tail -f datacenter-biz.log | grep 'takeEffectAdaptOptionJob_Worker-1'


借助spring-boot-dependencies构建自己的parent pom
	<dependencyManagement>
		<dependencies>
			<!-- Override Spring Data release train provided by Spring Boot -->
			<dependency>
				<groupId>org.springframework.data</groupId>
				<artifactId>spring-data-releasetrain</artifactId>
				<version>Fowler-SR2</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
			<dependency>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-dependencies</artifactId>
				<version>2.1.2.RELEASE</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

使用spring-boot-maven-plugin打包、运行程序
	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>

Starters集合
	官方命名格式：spring-boot-starter-*，都是在org.springframework.boot组下面
	三方命名推荐格式：thirdpartyproject-spring-boot-starter

	spring-boot-starter-actuator用于监控、管理应用

结构化代码
	避免java类使用默认包；
	mian类推荐使用包的根位置，方便@SpringBootApplication进行自动配置、组件扫描
	
配置类
	多个配置类的组合方式：
		@Import、@ImportResource；
		@ComponentScan配合多个@Configuration
		
	@Import的三种使用方式
		Configuration，ImportSelector，ImportBeanDefinitionRegistrar
		
替换自动配置
	携带--debug参数启动程序，查看自动配置
	关闭目标自动配置
		@Configuration
		@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
		public class MyConfiguration {
		}
	
	两种关闭特定自动配置方式(两者可以配合使用)：
		@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})或者@EnableAutoConfiguration(excludeName={"xxx.xxx.xxx"})
		配置文件配置项spring.autoconfigure.exclude
		
注入对象使用final修饰

@SpringBootApplication等效替换
	@EnableAutoConfiguration
	@ComponentScan
	@Configuration
	
程序开启远程debug
		$ java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n \
       -jar target/myapplication-0.0.1-SNAPSHOT.jar

devtools简化开发配置   
<dependencies>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-devtools</artifactId>
		<optional>true</optional>
	</dependency>
</dependencies>

SpringApplication事件、监听器（区别于spring框架的事件）
    
    
