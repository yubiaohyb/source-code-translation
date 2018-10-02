/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.context;

import org.springframework.beans.factory.Aware;

/**
 * 对象实现此接口获取来自自身所处消息源（通常是应用程序上下文）的通知。
 * Interface to be implemented by any object that wishes to be notified
 * of the MessageSource (typically the ApplicationContext) that it runs in.
 *
 * 注意：MessageSource通常也可以作为bean引用传递过来（任意的bean属性或构造器参数），因为在应用程序上下文中会定义一个名称为messageSource的bean。
 * <p>Note that the MessageSource can usually also be passed on as bean
 * reference (to arbitrary bean properties or constructor arguments), because
 * it is defined as bean with name "messageSource" in the application context.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 1.1.1
 * @see ApplicationContextAware
 */
public interface MessageSourceAware extends Aware {

	/**
	 * 设置对象运行所处的数据源。
	 * 在正常bean属性设置后，初始化回调例如InitializingBean的afterPropertiesSet或自定义的初始化方法之前调用。
	 * 在ApplicationContextAware的setApplicationContext之前调用。
	 * Set the MessageSource that this object runs in.
	 * <p>Invoked after population of normal bean properties but before an init
	 * callback like InitializingBean's afterPropertiesSet or a custom init-method.
	 * Invoked before ApplicationContextAware's setApplicationContext.
	 * @param messageSource message sourceto be used by this object
	 */
	void setMessageSource(MessageSource messageSource);

}
