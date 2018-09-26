/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.core.env;

/**
 * 组件实现此接口暴露环境信息
 * Interface indicating a component that contains and exposes an {@link Environment} reference.
 *
 * 所有的spring应用上下文都是和环境相关的，而此接口主要就是用于在接收bean工厂实例（可能是应用上下文或不是）的框架方法中执行instanceof检查，
 * 在客观需要时与环境进行交互。
 * <p>All Spring application contexts are EnvironmentCapable, and the interface is used primarily
 * for performing {@code instanceof} checks in framework methods that accept BeanFactory
 * instances that may or may not actually be ApplicationContext instances in order to interact
 * with the environment if indeed it is available.
 *
 * 正如上面提到的，ApplicationContext接口继承了EnvironmentCapable，并提供getEnvironment()方法实现；
 * 然而ConfigurableApplicationContext接口重新定义了自己的getEnvironment()方法，窄化了方法签名的返回类型，
 * 返回ConfigurableEnvironment。这样做的目的就是只允许通过ConfigurableApplicationContext进行访问并配置环境对象，
 * 否则只可读。
 * <p>As mentioned, {@link org.springframework.context.ApplicationContext ApplicationContext}
 * extends EnvironmentCapable, and thus exposes a {@link #getEnvironment()} method; however,
 * {@link org.springframework.context.ConfigurableApplicationContext ConfigurableApplicationContext}
 * redefines {@link org.springframework.context.ConfigurableApplicationContext#getEnvironment
 * getEnvironment()} and narrows the signature to return a {@link ConfigurableEnvironment}.
 * The effect is that an Environment object is 'read-only' until it is being accessed from
 * a ConfigurableApplicationContext, at which point it too may be configured.
 *
 * @author Chris Beams
 * @since 3.1
 * @see Environment
 * @see ConfigurableEnvironment
 * @see org.springframework.context.ConfigurableApplicationContext#getEnvironment()
 */
public interface EnvironmentCapable {

	/**
	 * 返回组件环境
	 * Return the {@link Environment} associated with this component.
	 */
	Environment getEnvironment();

}
