/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.content.page.editor.web.internal.portlet.action;

import com.liferay.layout.content.page.editor.constants.ContentPageEditorPortletKeys;
import com.liferay.layout.content.page.editor.web.internal.util.layout.structure.LayoutStructureUtil;
import com.liferay.layout.util.structure.LayoutStructureItem;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.Arrays;
import java.util.Collections;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Víctor Galán
 */
@Component(
	property = {
		"javax.portlet.name=" + ContentPageEditorPortletKeys.CONTENT_PAGE_EDITOR_PORTLET,
		"mvc.command.name=/layout_content_page_editor/change_item_deletion_status"
	},
	service = MVCActionCommand.class
)
public class ChangeItemDeletionStatusMVCActionCommand
	extends BaseContentPageEditorTransactionalMVCActionCommand {

	@Override
	protected JSONObject doTransactionalCommand(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		long segmentsExperienceId = ParamUtil.getLong(
			actionRequest, "segmentsExperienceId");

		JSONArray movedItemsJSONArray = _jsonFactory.createJSONArray(
			ParamUtil.getString(actionRequest, "movedItemIds"));

		String itemId = ParamUtil.getString(actionRequest, "itemId");

		JSONObject configJSONObject = _jsonFactory.createJSONObject(
			ParamUtil.getString(actionRequest, "config"));

		String[] addedItemIds = ParamUtil.getStringValues(
			actionRequest, "addedItemIds");

		String[] removedItemIds = ParamUtil.getStringValues(
			actionRequest, "removedItemIds");

		return JSONUtil.put(
			"layoutData",
			LayoutStructureUtil.updateLayoutPageTemplateData(
				themeDisplay.getScopeGroupId(), segmentsExperienceId,
				themeDisplay.getPlid(),
				layoutStructure -> {
					LayoutStructureItem layoutStructureItem =
						layoutStructure.getLayoutStructureItem(itemId);

					layoutStructureItem.updateItemConfig(configJSONObject);

					for (int i = 0; i < movedItemsJSONArray.length(); i++) {
						JSONObject jsonObject =
							movedItemsJSONArray.getJSONObject(i);

						layoutStructure.moveLayoutStructureItem(
							jsonObject.getString("itemId"),
							jsonObject.getString("parentId"), -1);
					}

					layoutStructure.markLayoutStructureItemForDeletion(
						Arrays.asList(removedItemIds), Collections.emptyList());

					for (String restoredItemId : addedItemIds) {
						layoutStructure.unmarkLayoutStructureItemForDeletion(
							restoredItemId);
					}
				}));
	}

	@Reference
	private JSONFactory _jsonFactory;

}