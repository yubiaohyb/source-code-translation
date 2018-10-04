/*
 * Copyright 2002-2018 the original author or authors.
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

import java.util.Arrays;

import org.springframework.web.cors.CorsConfiguration;

/**
 * 根据URL路径格式辅助完成CorsConfiguration的创建
 * Assists with the creation of a {@link CorsConfiguration} instance for a given
 * URL path pattern.
 *
 * @author Sebastien Deleuze
 * @author Rossen Stoyanchev
 * @author Sam Brannen
 * @since 4.2
 * @see CorsConfiguration
 * @see CorsRegistry
 */
public class CorsRegistration {

	private final String pathPattern;

	private final CorsConfiguration config;


	public CorsRegistration(String pathPattern) {
		this.pathPattern = pathPattern;
		// Same implicit default values as the @CrossOrigin annotation + allows simple methods
		this.config = new CorsConfiguration().applyPermitDefaultValues();
	}


	/**
	 * 指定允许的请求源，例如http://domain1.com或者使用*允许所有请求源。
	 * 匹配的请求源会出现在预留实际CORS请求的Access-Control-Allow-Origin响应头中。
	 * 默认允许所有请求。
	 * 注意：CORS检查使用来自Forwarded/X-Forwarded-Host/X-Forwarded-Port/X-Forwarded-Proto头信息的值。
	 * 如果出现，则用于来反射来自客户端的地址。
	 * 为了选择是否从配置中心抽取使用或丢弃这样的请求头信息，可以使用ForwardedHeaderFilter。
	 * 有关这个filter的更多信息查看框架说明。
	 * The list of allowed origins that be specific origins, e.g.
	 * {@code "http://domain1.com"}, or {@code "*"} for all origins.
	 * <p>A matched origin is listed in the {@code Access-Control-Allow-Origin}
	 * response header of preflight actual CORS requests.
	 * <p>By default, all origins are allowed.
	 * <p><strong>Note:</strong> CORS checks use values from "Forwarded"
	 * (<a href="http://tools.ietf.org/html/rfc7239">RFC 7239</a>),
	 * "X-Forwarded-Host", "X-Forwarded-Port", and "X-Forwarded-Proto" headers,
	 * if present, in order to reflect the client-originated address.
	 * Consider using the {@code ForwardedHeaderFilter} in order to choose from a
	 * central place whether to extract and use, or to discard such headers.
	 * See the Spring Framework reference for more on this filter.
	 */
	public CorsRegistration allowedOrigins(String... origins) {
		this.config.setAllowedOrigins(Arrays.asList(origins));
		return this;
	}


	/**
	 * 设置允许的HTTP方式，例如GET，POST等。使用*允许所有请求方式。
	 * 默认simple方式，允许如GET，HEAD，POST
	 * Set the HTTP methods to allow, e.g. {@code "GET"}, {@code "POST"}, etc.
	 * The special value {@code "*"} allows all methods.
	 * <p>By default "simple" methods, i.e. {@code GET}, {@code HEAD}, and
	 * {@code POST} are allowed.
	 */
	public CorsRegistration allowedMethods(String... methods) {
		this.config.setAllowedMethods(Arrays.asList(methods));
		return this;
	}

	/**
	 * 设置预发请求中允许用于实际请求的请求头信息列表。
	 * 使用*允许使用所有请求头信息。
	 * CORS定义强制出现Cache-Control/Content-Language/Expires/Last-Modified/Pragma请求头信息，其他的可选。
	 * 默认允许所有请求头信息
	 * Set the list of headers that a preflight request can list as allowed
	 * for use during an actual request. The special value {@code "*"} may be
	 * used to allow all headers.
	 * <p>A header name is not required to be listed if it is one of:
	 * {@code Cache-Control}, {@code Content-Language}, {@code Expires},
	 * {@code Last-Modified}, or {@code Pragma} as per the CORS spec.
	 * <p>By default all headers are allowed.
	 */
	public CorsRegistration allowedHeaders(String... headers) {
		this.config.setAllowedHeaders(Arrays.asList(headers));
		return this;
	}

	/**
	 * 设置返回的响应头信息
	 * 注意：当前属性暂时不支持谁在*。
	 * 默认属性不设置。
	 * Set the list of response headers other than "simple" headers, i.e.
	 * {@code Cache-Control}, {@code Content-Language}, {@code Content-Type},
	 * {@code Expires}, {@code Last-Modified}, or {@code Pragma}, that an
	 * actual response might have and can be exposed.
	 * <p>Note that {@code "*"} is not supported on this property.
	 * <p>By default this is not set.
	 */
	public CorsRegistration exposedHeaders(String... headers) {
		this.config.setExposedHeaders(Arrays.asList(headers));
		return this;
	}

	/**
	 * 浏览器是否需要发送凭证，例如跨域请求携带cookies发送到注解的站点。
	 * 相应的值配置在预发请求的Access-Control-Allow-Credentials请求头信息上。
	 * 注意：当前配置在与域名建立了高度信任的同时，也增加了web应用程序受到攻击的风险。
	 * Whether the browser should send credentials, such as cookies along with
	 * cross domain requests, to the annotated endpoint. The configured value is
	 * set on the {@code Access-Control-Allow-Credentials} response header of
	 * preflight requests.
	 * <p><strong>NOTE:</strong> Be aware that this option establishes a high
	 * level of trust with the configured domains and also increases the surface
	 * attack of the web application by exposing sensitive user-specific
	 * information such as cookies and CSRF tokens.
	 * <p>By default this is not set in which case the
	 * {@code Access-Control-Allow-Credentials} header is also not set and
	 * credentials are therefore not allowed.
	 */
	public CorsRegistration allowCredentials(boolean allowCredentials) {
		this.config.setAllowCredentials(allowCredentials);
		return this;
	}

	/**
	 * 配置客户端持有预发请求响应的时间，单位秒。
	 * 默认30分钟。
	 * Configure how long in seconds the response from a pre-flight request
	 * can be cached by clients.
	 * <p>By default this is set to 1800 seconds (30 minutes).
	 */
	public CorsRegistration maxAge(long maxAge) {
		this.config.setMaxAge(maxAge);
		return this;
	}

	protected String getPathPattern() {
		return this.pathPattern;
	}

	protected CorsConfiguration getCorsConfiguration() {
		return this.config;
	}

}
