/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.renderer;

import com.liferay.info.item.InfoItemReference;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Locale;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Víctor Galán
 */
@ProviderType
public interface LayoutHTMLRenderer {

	public String renderHTML(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse,
			InfoItemReference infoItemReference, Layout layout, Locale locale,
			User user, boolean wholePage)
		throws Exception;

	public String renderHTML(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, Layout layout,
			Locale locale, String segmentsExperienceKey, User user,
			boolean wholePage)
		throws Exception;

}