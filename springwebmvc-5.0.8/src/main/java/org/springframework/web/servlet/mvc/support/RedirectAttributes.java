/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.web.servlet.mvc.support;

import java.util.Collection;
import java.util.Map;

import org.springframework.lang.Nullable;
import org.springframework.ui.Model;
import org.springframework.web.servlet.FlashMap;

/**
 * 对Model接口进行了扩展，控制器可以在重定向时选择属性。
 * 因为添加重定向的目的是非常明确的。
 * 例如，在重定向URL使用时，属性值可能需要格式化为字符串，从而能够在RedirectView中添加作为查询参数甚至时作为URI变量。
 * A specialization of the {@link Model} interface that controllers can use to
 * select attributes for a redirect scenario. Since the intent of adding
 * redirect attributes is very explicit --  i.e. to be used for a redirect URL,
 * attribute values may be formatted as Strings and stored that way to make
 * them eligible to be appended to the query string or expanded as URI
 * variables in {@code org.springframework.web.servlet.view.RedirectView}.
 *
 * 接口同时还支持添加flash属性。对于flash属性的大概介绍查看FlashMap。
 * 我们可以使用RedirectAttributes存储flash属性，它们将自动传递给当前请求的FlashMap的output属性。
 * <p>This interface also provides a way to add flash attributes. For a
 * general overview of flash attributes see {@link FlashMap}. You can use
 * {@link RedirectAttributes} to store flash attributes and they will be
 * automatically propagated to the "output" FlashMap of the current request.
 *
 * 下面是在控制器中的一个使用示例：
 * <p>Example usage in an {@code @Controller}:
 * <pre class="code">
 * &#064;RequestMapping(value = "/accounts", method = RequestMethod.POST)
 * public String handle(Account account, BindingResult result, RedirectAttributes redirectAttrs) {
 *   if (result.hasErrors()) {
 *     return "accounts/new";
 *   }
 *   // Save account ...
 *   redirectAttrs.addAttribute("id", account.getId()).addFlashAttribute("message", "Account created!");
 *   return "redirect:/accounts/{id}";
 * }
 * </pre>
 *
 * 除非方法返回重定向视图名或RedirectView对象，否则调用了方法，且从未使用RedirectAttributes模型对象的话，则为空。
 * <p>A RedirectAttributes model is empty when the method is called and is never
 * used unless the method returns a redirect view name or a RedirectView.
 *
 * 完成重定向后，flash属性会自动到目标URL对应控制器的模型对象上。
 * <p>After the redirect, flash attributes are automatically added to the model
 * of the controller that serves the target URL.
 *
 * @author Rossen Stoyanchev
 * @since 3.1
 */
public interface RedirectAttributes extends Model {

	@Override
	RedirectAttributes addAttribute(String attributeName, @Nullable Object attributeValue);

	@Override
	RedirectAttributes addAttribute(Object attributeValue);

	@Override
	RedirectAttributes addAllAttributes(Collection<?> attributeValues);

	@Override
	RedirectAttributes mergeAttributes(Map<String, ?> attributes);

	/**
	 * Add the given flash attribute.
	 * @param attributeName the attribute name; never {@code null}
	 * @param attributeValue the attribute value; may be {@code null}
	 */
	RedirectAttributes addFlashAttribute(String attributeName, @Nullable Object attributeValue);

	/**
	 * Add the given flash storage using a
	 * {@link org.springframework.core.Conventions#getVariableName generated name}.
	 * @param attributeValue the flash attribute value; never {@code null}
	 */
	RedirectAttributes addFlashAttribute(Object attributeValue);

	/**
	 * Return the attributes candidate for flash storage or an empty Map.
	 */
	Map<String, ?> getFlashAttributes();
}
