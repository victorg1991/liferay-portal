/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.admin.web.internal.display.context;

import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Portal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Víctor Galán
 */
public class ExportStaticSiteDisplayContext {

	public ExportStaticSiteDisplayContext(
		String action, long groupId, List<Layout> layouts, Locale locale,
		Portal portal) {

		_action = action;
		_groupId = groupId;
		_layouts = layouts;
		_locale = locale;
		_portal = portal;
	}

	public Map<String, Object> getContext() {
		return HashMapBuilder.<String, Object>put(
			"action", _action
		).put(
			"exportURL",
			_portal.getPathMain() +
				"/portal/layout_staticsite_export/export_static_site"
		).put(
			"groupId", String.valueOf(_groupId)
		).put(
			"pages", _getPages()
		).build();
	}

	private int _getDepth(Layout layout, Map<Long, Layout> layoutsMap) {
		int depth = 0;

		Layout parentLayout = layoutsMap.get(layout.getParentLayoutId());

		while ((parentLayout != null) && (depth < _MAX_DEPTH)) {
			depth++;

			parentLayout = layoutsMap.get(parentLayout.getParentLayoutId());
		}

		return depth;
	}

	private List<Map<String, Object>> _getPages() {
		List<Map<String, Object>> pages = new ArrayList<>();

		Map<Long, Layout> layoutsMap = new HashMap<>();

		for (Layout layout : _layouts) {
			layoutsMap.put(layout.getLayoutId(), layout);
		}

		for (Layout layout : _layouts) {
			pages.add(
				HashMapBuilder.<String, Object>put(
					"depth", _getDepth(layout, layoutsMap)
				).put(
					"friendlyURL", layout.getFriendlyURL(_locale)
				).put(
					"id", String.valueOf(layout.getPlid())
				).put(
					"name", layout.getName(_locale)
				).put(
					"plid", String.valueOf(layout.getPlid())
				).build());
		}

		return pages;
	}

	private static final int _MAX_DEPTH = 10;

	private final String _action;
	private final long _groupId;
	private final List<Layout> _layouts;
	private final Locale _locale;
	private final Portal _portal;

}