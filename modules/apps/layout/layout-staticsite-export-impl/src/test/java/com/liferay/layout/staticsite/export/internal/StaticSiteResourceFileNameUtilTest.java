/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.staticsite.export.internal;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Víctor Galán
 */
public class StaticSiteResourceFileNameUtilTest {

	@ClassRule
	public static LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetFileNameFoldsQueryStringIntoName() {
		String fileName = StaticSiteResourceFileNameUtil.getFileName(
			"/o/layout-common-styles/main.css?plid=1&t=2");

		Assert.assertTrue(
			fileName, fileName.startsWith("o/layout-common-styles/main."));
		Assert.assertTrue(fileName, fileName.endsWith(".css"));
	}

	@Test
	public void testGetFileNameKeepsFingerprintedName() {
		Assert.assertEquals(
			"o/my-web/css/main.(abc123).css",
			StaticSiteResourceFileNameUtil.getFileName(
				"/o/my-web/css/main.(abc123).css"));
	}

	@Test
	public void testGetFileNameKeepsPath() {
		Assert.assertEquals(
			"o/classic-theme/images/favicon.ico",
			StaticSiteResourceFileNameUtil.getFileName(
				"/o/classic-theme/images/favicon.ico"));
	}

	@Test
	public void testGetFileNameQueryStringDoesNotRenameDirectory() {

		// The only period is in a directory name, so folding the digest in
		// there would write the file into a directory nothing asks for

		String fileName = StaticSiteResourceFileNameUtil.getFileName(
			"/documents/1/2/emblem.svg/4bca3313?download=true");

		Assert.assertTrue(
			fileName, fileName.startsWith("documents/1/2/emblem.svg/4bca3313"));
	}

	@Test
	public void testGetFileNameTakesExtensionFromAncestor() {

		// A document URL ends in an identifier and names the type one segment
		// up. Without the extension a static server offers the file as
		// application/octet-stream and the browser will not render it.

		Assert.assertEquals(
			"documents/1/2/emblem.15479fd9.svg/4bca3313-75ce.svg",
			StaticSiteResourceFileNameUtil.getFileName(
				"/documents/1/2/emblem.15479fd9.svg/4bca3313-75ce"));
	}

	@Test
	public void testGetFileNameWhenNoAncestorNamesAnExtension() {
		Assert.assertEquals(
			"o/js/language/en_US/fragment-impl/all",
			StaticSiteResourceFileNameUtil.getFileName(
				"/o/js/language/en_US/fragment-impl/all"));
	}

	@Test
	public void testGetFileNameWhenTheAncestorIsNotAnExtension() {

		// "tax-office-rerun" is a name, not a type, and guessing it as one
		// would produce a file no server can describe either

		Assert.assertEquals(
			"web/tax-office-rerun/inicio",
			StaticSiteResourceFileNameUtil.getFileName(
				"/web/tax-office-rerun/inicio"));
	}

}