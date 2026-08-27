/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.staticsite.export;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Layout;

import java.util.List;
import java.util.Locale;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Builds a static copy of a site: renders the chosen pages, follows everything
 * they reference, and writes both as files whose references point at each
 * other rather than at the portal.
 *
 * @author Víctor Galán
 */
@ProviderType
public interface StaticSiteBuilder {

	/**
	 * Builds the given pages of the given site, handing every file to the given
	 * writer, and returns what was written and what could not be.
	 *
	 * @param plids the pages to build, or an empty array for every page the
	 *        build can write
	 */
	public StaticSiteExportResult build(
			long groupId, Locale locale, long[] plids,
			StaticSiteWriter staticSiteWriter)
		throws PortalException;

	/**
	 * Returns the pages the build can write, which is what a caller offering a
	 * choice of pages has to choose from.
	 */
	public List<Layout> getExportableLayouts(long groupId);

}