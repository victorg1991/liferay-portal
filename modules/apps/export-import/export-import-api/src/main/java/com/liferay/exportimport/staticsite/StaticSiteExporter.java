/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.staticsite;

import com.liferay.exportimport.kernel.lar.PortletDataContext;

import java.io.File;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Serializes the layouts selected by an export process as a tree of static HTML
 * files and the assets they reference, and returns the archive holding them.
 *
 * <p>
 * The archive is the implementation's own rather than the one the export
 * process assembles for a LAR, because a static site is laid out for a web
 * server rather than for another portal, and nothing else in the process
 * contributes to it.
 * </p>
 *
 * <p>
 * Implementations reuse the page selection resolved by the export process. They
 * do not reuse the staged model data handlers, which serialize models rather
 * than rendered output.
 * </p>
 *
 * @author Alejandro Tardín
 */
@ProviderType
public interface StaticSiteExporter {

	public File export(PortletDataContext portletDataContext) throws Exception;

}