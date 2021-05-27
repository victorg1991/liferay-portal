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

package com.liferay.site.navigation.menu.item.display.page.internal.display.context;

import com.liferay.item.selector.ItemSelector;
import com.liferay.item.selector.ItemSelectorCriterion;
import com.liferay.item.selector.ItemSelectorRendering;
import com.liferay.item.selector.ItemSelectorView;
import com.liferay.item.selector.ItemSelectorViewRenderer;
import com.liferay.item.selector.criteria.InfoItemItemSelectorReturnType;
import com.liferay.item.selector.criteria.info.item.criterion.InfoItemItemSelectorCriterion;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.PortletURLUtil;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactoryUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.site.navigation.menu.item.display.page.internal.constants.SiteNavigationMenuItemTypeDisplayPageWebKeys;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Jürgen Kappler
 */
public class DisplayPageSiteNavigationMenuTypeDisplayContext {

	public DisplayPageSiteNavigationMenuTypeDisplayContext(
		HttpServletRequest httpServletRequest) {

		_httpServletRequest = httpServletRequest;

		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public InfoItemItemSelectorCriterion getItemSelectorCriterion() {
		InfoItemItemSelectorCriterion itemSelectorCriterion =
			new InfoItemItemSelectorCriterion();

		itemSelectorCriterion.setDesiredItemSelectorReturnTypes(
			new InfoItemItemSelectorReturnType());

		return itemSelectorCriterion;
	}

	public Map<String, String[]> getItemSelectorParameterMap() {
		return HashMapBuilder.put(
			"0_json",
			() -> {
				JSONObject jsonObject = JSONUtil.put(
					"desiredItemSelectorReturnTypes", "infoitem"
				).put(
					"status", WorkflowConstants.STATUS_APPROVED
				);

				return new String[] {jsonObject.toJSONString()};
			}
		).put(
			"criteria",
			new String[] {InfoItemItemSelectorCriterion.class.getName()}
		).put(
			"itemSelectedEventName", new String[] {"selectInfoItem"}
		).build();
	}

	public PortletURL getPortletURL(
		PortletURL currentURLObj,
		LiferayPortletResponse liferayPortletResponse) {

		try {
			return PortletURLUtil.clone(currentURLObj, liferayPortletResponse);
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(exception, exception);
			}

			return liferayPortletResponse.createRenderURL();
		}
	}

	public ItemSelectorView<ItemSelectorCriterion>
		getSelectedItemSelectorView() {

		Map<String, ItemSelectorView<ItemSelectorCriterion>>
			itemSelectorViewMap = _getItemSelectorViewMap();

		Set<String> strings = itemSelectorViewMap.keySet();

		return itemSelectorViewMap.get(strings.toArray()[0]);
	}

	private ItemSelectorRendering _getItemSelectorRendering() {
		if (_itemSelectorRendering != null) {
			return _itemSelectorRendering;
		}

		ItemSelector itemSelector =
			(ItemSelector)_httpServletRequest.getAttribute(
				SiteNavigationMenuItemTypeDisplayPageWebKeys.ITEM_SELECTOR);

		ThemeDisplay themeDisplay =
			(ThemeDisplay)_httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		_itemSelectorRendering = itemSelector.getItemSelectorRendering(
			RequestBackedPortletURLFactoryUtil.create(_httpServletRequest),
			getItemSelectorParameterMap(), themeDisplay);

		return _itemSelectorRendering;
	}

	private Map<String, ItemSelectorView<ItemSelectorCriterion>>
		_getItemSelectorViewMap() {

		if (_itemSelectorViewMap != null) {
			return _itemSelectorViewMap;
		}

		Map<String, ItemSelectorView<ItemSelectorCriterion>>
			itemSelectorViewMap = new HashMap<>();

		ItemSelectorRendering itemSelectorRendering =
			_getItemSelectorRendering();

		for (ItemSelectorViewRenderer itemSelectorViewRenderer :
				itemSelectorRendering.getItemSelectorViewRenderers()) {

			ItemSelectorView<ItemSelectorCriterion> itemSelectorView =
				itemSelectorViewRenderer.getItemSelectorView();

			itemSelectorViewMap.put(
				itemSelectorView.getTitle(_themeDisplay.getLocale()),
				itemSelectorView);
		}

		_itemSelectorViewMap = itemSelectorViewMap;

		return itemSelectorViewMap;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DisplayPageSiteNavigationMenuTypeDisplayContext.class);

	private final HttpServletRequest _httpServletRequest;
	private ItemSelectorRendering _itemSelectorRendering;
	private Map<String, ItemSelectorView<ItemSelectorCriterion>>
		_itemSelectorViewMap;
	private final ThemeDisplay _themeDisplay;

}