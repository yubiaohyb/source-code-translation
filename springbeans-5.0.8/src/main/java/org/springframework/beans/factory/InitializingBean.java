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

package org.springframework.beans.factory;

/**
 * bean实现此接口用于在BeanFactory设置完所有属性后立即作出响应。
 * 例如：执行自定义初始化或者存粹地检查所有强制属性是否都已设置。
 * Interface to be implemented by beans that need to react once all their
 * properties have been set by a BeanFactory: for example, to perform custom
 * initialization, or merely to check that all mandatory properties have been set.
 *
 * 实现InitializingBean的替代方案就是指定一个自定义的初始化方法，好比在xml bean定义中。
 * bean生命周期所有方法列表可以查看BeanFactory文档。
 * <p>An alternative to implementing InitializingBean is specifying a custom
 * init-method, for example in an XML bean definition.
 * For a list of all bean lifecycle methods, see the
 * {@link BeanFactory BeanFactory javadocs}.
 *
 * @author Rod Johnson
 * @see BeanNameAware
 * @see BeanFactoryAware
 * @see BeanFactory
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getInitMethodName
 * @see org.springframework.context.ApplicationContextAware
 */
public interface InitializingBean {

	/**
	 * BeanFactory在完成所有bean属性设置后调用（同时也实现了BeanFactoryAware和ApplicationContextAware）。
	 * 本方法只有bean方法完成所有属性设置后才会执行，在发生配置缺失时抛出异常。
	 * Invoked by a BeanFactory after it has set all bean properties supplied
	 * (and satisfied BeanFactoryAware and ApplicationContextAware).
	 * <p>This method allows the bean instance to perform initialization only
	 * possible when all bean properties have been set and to throw an
	 * exception in the event of misconfiguration.
	 * @throws Exception in the event of misconfiguration (such
	 * as failure to set an essential property) or if initialization fails.
	 */
	void afterPropertiesSet() throws Exception;

}
