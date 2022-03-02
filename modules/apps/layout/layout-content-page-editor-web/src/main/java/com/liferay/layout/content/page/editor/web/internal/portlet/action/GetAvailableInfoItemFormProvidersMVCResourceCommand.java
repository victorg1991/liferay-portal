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

package com.liferay.layout.content.page.editor.web.internal.portlet.action;

import com.liferay.info.form.InfoForm;
import com.liferay.info.item.InfoItemServiceTracker;
import com.liferay.info.item.provider.InfoItemFormProvider;
import com.liferay.layout.content.page.editor.constants.ContentPageEditorPortletKeys;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.List;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Víctor Galán
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + ContentPageEditorPortletKeys.CONTENT_PAGE_EDITOR_PORTLET,
		"mvc.command.name=/layout_content_page_editor/get_available_info_item_form_providers"
	},
	service = MVCResourceCommand.class
)
public class GetAvailableInfoItemFormProvidersMVCResourceCommand
	extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		List<InfoItemFormProvider<?>> infoItemFormProviders =
			_infoItemServiceTracker.getAllInfoItemServices(
				(Class<InfoItemFormProvider<?>>)
					(Class<?>)InfoItemFormProvider.class);

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		for (InfoItemFormProvider<?> infoItemFormProvider :
				infoItemFormProviders) {

			InfoForm infoForm = infoItemFormProvider.getInfoForm();

			if (Validator.isNotNull(infoForm.getName())) {
				jsonArray.put(
					JSONUtil.put(
						"label", infoForm.getLabel(resourceRequest.getLocale())
					).put(
						"value", PortalUtil.getClassNameId(infoForm.getName())
					));
			}
		}

		JSONPortletResponseUtil.writeJSON(
			resourceRequest, resourceResponse, jsonArray);
	}

	@Reference
	private InfoItemServiceTracker _infoItemServiceTracker;

}