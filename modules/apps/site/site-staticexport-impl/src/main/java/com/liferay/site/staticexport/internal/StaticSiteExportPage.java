/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.staticexport.internal;

import java.util.Locale;
import java.util.Set;

/**
 * @author Víctor Galán
 */
public class StaticSiteExportPage {

	public StaticSiteExportPage(
		Set<String> friendlyURLs, String key, Locale locale, String path,
		long plid, StaticSiteExportDocument staticSiteExportDocument) {

		_friendlyURLs = friendlyURLs;
		_key = key;
		_locale = locale;
		_path = path;
		_plid = plid;
		_staticSiteExportDocument = staticSiteExportDocument;
	}

	public Set<String> getFriendlyURLs() {
		return _friendlyURLs;
	}

	public String getKey() {
		return _key;
	}

	public Locale getLocale() {
		return _locale;
	}

	public String getPath() {
		return _path;
	}

	public long getPlid() {
		return _plid;
	}

	public StaticSiteExportDocument getStaticSiteExportDocument() {
		return _staticSiteExportDocument;
	}

	private final Set<String> _friendlyURLs;
	private final String _key;
	private final Locale _locale;
	private final String _path;
	private final long _plid;
	private final StaticSiteExportDocument _staticSiteExportDocument;

}