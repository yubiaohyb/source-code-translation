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

package org.springframework.web.servlet.config.annotation;

import org.springframework.lang.Nullable;
import org.springframework.util.PathMatcher;
import org.springframework.web.util.UrlPathHelper;

/**
 * 用于配置HandlerMappings路径匹配选项，例如反斜杠匹配，后缀注册，路径匹配器和路径辅助器。
 * Helps with configuring HandlerMappings path matching options such as trailing
 * slash match, suffix registration, path matcher and path helper.
 *
 * 配置的路径匹配器和路径辅助器实例由RequestMappings/ViewControllerMappings/ResourcesMappings共享。
 * <p>Configured path matcher and path helper instances are shared for:
 * <ul>
 * <li>RequestMappings</li>
 * <li>ViewControllerMappings</li>
 * <li>ResourcesMappings</li>
 * </ul>
 *
 * @author Brian Clozel
 * @since 4.0.3
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
 * @see org.springframework.web.servlet.handler.SimpleUrlHandlerMapping
 */
public class PathMatchConfigurer {

	@Nullable
	private Boolean suffixPatternMatch;

	@Nullable
	private Boolean trailingSlashMatch;

	@Nullable
	private Boolean registeredSuffixPatternMatch;

	@Nullable
	private UrlPathHelper urlPathHelper;

	@Nullable
	private PathMatcher pathMatcher;


	/**
	 * 当使用格式匹配请求时，是否使用后缀格式匹配（.*）。
	 * 如果可以，映射到/users的方法，同样也匹配/users.*。
	 * 默认为真
	 * Whether to use suffix pattern match (".*") when matching patterns to
	 * requests. If enabled a method mapped to "/users" also matches to "/users.*".
	 * <p>By default this is set to {@code true}.
	 * @see #registeredSuffixPatternMatch
	 */
	public PathMatchConfigurer setUseSuffixPatternMatch(Boolean suffixPatternMatch) {
		this.suffixPatternMatch = suffixPatternMatch;
		return this;
	}

	/**
	 * 不管是否存在反斜杠，是否匹配URL。
	 * 如果可以，映射到/users的方法，同样也匹配/users/。
	 * Whether to match to URLs irrespective of the presence of a trailing slash.
	 * If enabled a method mapped to "/users" also matches to "/users/".
	 * <p>The default value is {@code true}.
	 */
	public PathMatchConfigurer setUseTrailingSlashMatch(Boolean trailingSlashMatch) {
		this.trailingSlashMatch = trailingSlashMatch;
		return this;
	}

	/**
	 * 是否只有通过WebMvcConfigurer#configureContentNegotiation配置内容协商显式注册的路径扩展名，后缀格式匹配才会工作。
	 * 此时通常建议避免因为其他原因在路径中出现"."，减少不必要的歧义。
	 * 默认设置为假
	 * Whether suffix pattern matching should work only against path extensions
	 * explicitly registered when you
	 * {@link WebMvcConfigurer#configureContentNegotiation configure content
	 * negotiation}. This is generally recommended to reduce ambiguity and to
	 * avoid issues such as when a "." appears in the path for other reasons.
	 * <p>By default this is set to "false".
	 * @see WebMvcConfigurer#configureContentNegotiation
	 */
	public PathMatchConfigurer setUseRegisteredSuffixPatternMatch(Boolean registeredSuffixPatternMatch) {
		this.registeredSuffixPatternMatch = registeredSuffixPatternMatch;
		return this;
	}

	/**
	 * 设置用于路径查询解析的UrlPathHelper
	 * 一般用于使用自定义子类覆盖默认UrlPathHelper实现，或者跨多个HandlerMappings和MethodNameResolvers共享通用UrlPathHelper设置。
	 * Set the UrlPathHelper to use for resolution of lookup paths.
	 * <p>Use this to override the default UrlPathHelper with a custom subclass,
	 * or to share common UrlPathHelper settings across multiple HandlerMappings
	 * and MethodNameResolvers.
	 */
	public PathMatchConfigurer setUrlPathHelper(UrlPathHelper urlPathHelper) {
		this.urlPathHelper = urlPathHelper;
		return this;
	}

	/**
	 * 设置PathMatcher实现，用于根据已注册的URL格式匹配URL路径
	 * Set the PathMatcher implementation to use for matching URL paths
	 * against registered URL patterns. Default is AntPathMatcher.
	 * @see org.springframework.util.AntPathMatcher
	 */
	public PathMatchConfigurer setPathMatcher(PathMatcher pathMatcher) {
		this.pathMatcher = pathMatcher;
		return this;
	}


	@Nullable
	public Boolean isUseSuffixPatternMatch() {
		return this.suffixPatternMatch;
	}

	@Nullable
	public Boolean isUseTrailingSlashMatch() {
		return this.trailingSlashMatch;
	}

	@Nullable
	public Boolean isUseRegisteredSuffixPatternMatch() {
		return this.registeredSuffixPatternMatch;
	}

	@Nullable
	public UrlPathHelper getUrlPathHelper() {
		return this.urlPathHelper;
	}

	@Nullable
	public PathMatcher getPathMatcher() {
		return this.pathMatcher;
	}

}
