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

package org.springframework.web.servlet.view.document;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import org.springframework.web.servlet.view.AbstractView;

/**
 * 传统XLS格式Excel文档视图的实用超类，兼容Apache POI 3.5或更高版本。
 * Convenient superclass for Excel document views in traditional XLS format.
 * Compatible with Apache POI 3.5 and higher.
 *
 * 子类中workbook的具体使用，查看Apache's POI官网。
 * <p>For working with the workbook in the subclass, see
 * <a href="http://poi.apache.org">Apache's POI site</a>
 *
 * @author Juergen Hoeller
 * @since 4.2
 */
public abstract class AbstractXlsView extends AbstractView {

	/**
	 * Default Constructor.
	 * Sets the content type of the view to "application/vnd.ms-excel".
	 */
	public AbstractXlsView() {
		setContentType("application/vnd.ms-excel");
	}


	@Override
	protected boolean generatesDownloadContent() {
		return true;
	}

	/**
	 * 根据model渲染Excel视图
	 * Renders the Excel view, given the specified model.
	 */
	@Override
	protected final void renderMergedOutputModel(
			Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

		//新创建一个workbook
		// Create a fresh workbook instance for this render step.
		Workbook workbook = createWorkbook(model, request);

		//委托给应用程序
		// Delegate to application-provided document code.
		buildExcelDocument(model, workbook, request, response);

		// Set the content type.
		response.setContentType(getContentType());

		// Flush byte array to servlet output stream.
		renderWorkbook(workbook, response);
	}


	/**
	 * Template method for creating the POI {@link Workbook} instance.
	 * <p>The default implementation creates a traditional {@link HSSFWorkbook}.
	 * Spring-provided subclasses are overriding this for the OOXML-based variants;
	 * custom subclasses may override this for reading a workbook from a file.
	 * @param model the model Map
	 * @param request current HTTP request (for taking the URL or headers into account)
	 * @return the new {@link Workbook} instance
	 */
	protected Workbook createWorkbook(Map<String, Object> model, HttpServletRequest request) {
		return new HSSFWorkbook();
	}

	/**
	 * The actual render step: taking the POI {@link Workbook} and rendering
	 * it to the given response.
	 * @param workbook the POI Workbook to render
	 * @param response current HTTP response
	 * @throws IOException when thrown by I/O methods that we're delegating to
	 */
	protected void renderWorkbook(Workbook workbook, HttpServletResponse response) throws IOException {
		ServletOutputStream out = response.getOutputStream();
		workbook.write(out);
		workbook.close();
	}

	/**
	 * 应用程序子类实现此方法，根据model填充Excel workbook文档。
	 * Application-provided subclasses must implement this method to populate
	 * the Excel workbook document, given the model.
	 * @param model the model Map
	 * @param workbook the Excel workbook to populate
	 * @param request in case we need locale etc. Shouldn't look at attributes.
	 * @param response in case we need to set cookies. Shouldn't write to it.
	 */
	protected abstract void buildExcelDocument(
			Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response)
			throws Exception;

}
