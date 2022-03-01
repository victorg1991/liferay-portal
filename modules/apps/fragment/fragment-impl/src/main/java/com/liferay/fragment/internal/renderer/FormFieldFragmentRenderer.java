/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.fragment.internal.renderer;

import com.liferay.fragment.constants.FragmentConfigurationFieldDataType;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.renderer.FragmentRenderer;
import com.liferay.fragment.renderer.FragmentRendererContext;
import com.liferay.fragment.util.configuration.FragmentEntryConfigurationParser;
import com.liferay.info.field.InfoField;
import com.liferay.info.field.InfoFieldSetEntry;
import com.liferay.info.field.type.InfoFieldType;
import com.liferay.info.form.InfoForm;
import com.liferay.info.item.InfoItemServiceTracker;
import com.liferay.info.item.provider.InfoItemFormProvider;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.Tuple;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.ratings.taglib.servlet.taglib.RatingsTag;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * @author Pavel Savinov
 */
@Component(service = FragmentRenderer.class)
public class FormFieldFragmentRenderer
	implements FragmentRenderer {

	@Override
	public String getCollectionKey() {
		return "form-components";
	}

	@Override
	public String getIcon() {
		return "container";
	}

	@Override
	public String getLabel(Locale locale) {
		return "Form input";
	}

	@Override
	public void render(
		FragmentRendererContext fragmentRendererContext,
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse) {

		FragmentEntryLink fragmentEntryLink =
			fragmentRendererContext.getFragmentEntryLink();

		String className =
			(String) _fragmentEntryConfigurationParser.getConfigurationFieldValue(
				fragmentEntryLink.getEditableValues(), "className",
				FragmentConfigurationFieldDataType.STRING);

		if (Validator.isNull(className)) {
			printInvalidInfo(httpServletResponse);

			return;
		}


		InfoItemFormProvider<?> infoItemFormProvider =
			_infoItemServiceTracker.getFirstInfoItemService(
				InfoItemFormProvider.class, className);

		String field =
			(String) _fragmentEntryConfigurationParser.getConfigurationFieldValue(
				fragmentEntryLink.getEditableValues(), "field",
				FragmentConfigurationFieldDataType.STRING);

		if (Validator.isNull(field)) {
			printInvalidInfo(httpServletResponse);

			return;
		}

		InfoForm infoForm = infoItemFormProvider.getInfoForm();

		InfoField<?> infoField =
			(InfoField<?>) infoForm.getInfoFieldSetEntry(field);



	}

	private void printInvalidInfo(HttpServletResponse httpServletResponse) {
		try {
			StringBundler stringBundler = new StringBundler();

			stringBundler.append(
				"<div class=\"alert alert-info\" role=\"alert\">");
			stringBundler.append(
				"<strong class=\"lead\">Error:</strong>Not field selected");
			stringBundler.append("</div>");

			PrintWriter writer = httpServletResponse.getWriter();

			writer.write(stringBundler.toString());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Reference
	private InfoItemServiceTracker _infoItemServiceTracker;

	@Reference
	private FragmentEntryConfigurationParser _fragmentEntryConfigurationParser;

	private static final Log _log = LogFactoryUtil.getLog(
		FormFieldFragmentRenderer.class);

}