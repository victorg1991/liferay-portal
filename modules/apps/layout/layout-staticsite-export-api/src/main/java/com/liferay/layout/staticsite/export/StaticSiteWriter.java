/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.staticsite.export;

/**
 * Receives the files a static site is made of, so that a caller decides where
 * they land. The build is the same whether they end up in an archive of its
 * own or in one the export framework is already assembling.
 *
 * @author Víctor Galán
 */
@FunctionalInterface
public interface StaticSiteWriter {

	public void write(String fileName, byte[] bytes) throws Exception;

}