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

package com.liferay.site.navigation.menu.item.display.page.internal.type;

import com.liferay.frontend.taglib.servlet.taglib.util.JSPRenderer;
import com.liferay.item.selector.ItemSelector;
import com.liferay.item.selector.criteria.InfoItemItemSelectorReturnType;
import com.liferay.item.selector.criteria.info.item.criterion.InfoItemItemSelectorCriterion;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.site.navigation.menu.item.display.page.internal.constants.SiteNavigationMenuItemTypeDisplayPageWebKeys;
import com.liferay.site.navigation.menu.item.layout.constants.SiteNavigationMenuItemTypeConstants;
import com.liferay.site.navigation.model.SiteNavigationMenuItem;
import com.liferay.site.navigation.type.SiteNavigationMenuItemType;

import java.io.IOException;

import java.util.Locale;

import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jürgen Kappler
 */
@Component(
	immediate = true,
	property = "site.navigation.menu.item.type=" + SiteNavigationMenuItemTypeConstants.DISPLAY_PAGE,
	service = SiteNavigationMenuItemType.class
)
public class DisplayPageSiteNavigationMenuItemType
	implements SiteNavigationMenuItemType {

	@Override
	public String getIcon() {
		return "page";
	}

	@Override
	public String getItemSelectorURL(HttpServletRequest httpServletRequest) {
		RenderResponse renderResponse =
			(RenderResponse)httpServletRequest.getAttribute(
				JavaConstants.JAVAX_PORTLET_RESPONSE);

		InfoItemItemSelectorCriterion itemSelectorCriterion =
			new InfoItemItemSelectorCriterion();

		itemSelectorCriterion.setDesiredItemSelectorReturnTypes(
			new InfoItemItemSelectorReturnType());

		PortletURL infoItemSelectorURL = _itemSelector.getItemSelectorURL(
			RequestBackedPortletURLFactoryUtil.create(httpServletRequest),
			renderResponse.getNamespace() + "selectItem",
			itemSelectorCriterion);

		if (infoItemSelectorURL == null) {
			return StringPool.BLANK;
		}

		return infoItemSelectorURL.toString();
	}

	@Override
	public String getLabel(Locale locale) {
		return LanguageUtil.get(locale, "display-page");
	}

	@Override
	public String getRegularURL(
		HttpServletRequest httpServletRequest,
		SiteNavigationMenuItem siteNavigationMenuItem) {

		return StringPool.BLANK;
	}

	@Override
	public String getSubtitle(
		SiteNavigationMenuItem siteNavigationMenuItem, Locale locale) {

		return StringPool.BLANK;
	}

	@Override
	public String getTarget(SiteNavigationMenuItem siteNavigationMenuItem) {
		UnicodeProperties typeSettingsUnicodeProperties =
			new UnicodeProperties();

		typeSettingsUnicodeProperties.fastLoad(
			siteNavigationMenuItem.getTypeSettings());

		boolean useNewTab = GetterUtil.getBoolean(
			typeSettingsUnicodeProperties.getProperty(
				"useNewTab", Boolean.FALSE.toString()));

		if (!useNewTab) {
			return StringPool.BLANK;
		}

		return "target=\"_blank\"";
	}

	@Override
	public String getTitle(
		SiteNavigationMenuItem siteNavigationMenuItem, Locale locale) {

		return StringPool.BLANK;
	}

	@Override
	public String getType() {
		return SiteNavigationMenuItemTypeConstants.DISPLAY_PAGE;
	}

	@Override
	public boolean isBrowsable(SiteNavigationMenuItem siteNavigationMenuItem) {
		return true;
	}

	@Override
	public boolean isItemSelector() {
		return true;
	}

	@Override
	public void renderAddPage(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws IOException {

		httpServletRequest.setAttribute(
			SiteNavigationMenuItemTypeDisplayPageWebKeys.ITEM_SELECTOR,
			_itemSelector);

		_jspRenderer.renderJSP(
			_servletContext, httpServletRequest, httpServletResponse,
			"/add_display_page.jsp");
	}

	@Override
	public void renderEditPage(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse,
		SiteNavigationMenuItem siteNavigationMenuItem) {
	}

	@Reference
	private ItemSelector _itemSelector;

	@Reference
	private JSPRenderer _jspRenderer;

	@Reference
	private Portal _portal;

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.site.navigation.menu.item.display.page)",
		unbind = "-"
	)
	private ServletContext _servletContext;

}