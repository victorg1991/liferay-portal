/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.staticsite.export.internal;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Víctor Galán
 */
public class StaticSiteResourceHarvesterTest {

	@ClassRule
	public static LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_staticSiteResourceHarvester = new StaticSiteResourceHarvester();
	}

	@Test
	public void testHarvestCSSImport() {
		Set<String> urls = _staticSiteResourceHarvester.harvestCSS(
			"@import \"/o/other-web/base.css\";", "/o/my-web/css/main.css");

		Assert.assertTrue(
			urls.toString(), urls.contains("/o/other-web/base.css"));
	}

	@Test
	public void testHarvestCSSRelativeURL() {
		Set<String> urls = _staticSiteResourceHarvester.harvestCSS(
			"a { background: url(../images/aui/loading.gif); }",
			"/o/classic-theme/css/clay.css");

		Assert.assertTrue(
			urls.toString(),
			urls.contains("/o/classic-theme/images/aui/loading.gif"));
	}

	@Test
	public void testHarvestCSSSkipsDataURI() {
		Set<String> urls = _staticSiteResourceHarvester.harvestCSS(
			"a { background: url(data:image/gif;base64,AAAA); }",
			"/o/classic-theme/css/clay.css");

		Assert.assertTrue(urls.toString(), urls.isEmpty());
	}

	@Test
	public void testHarvestHTMLImportMap() {
		Set<String> urls = _staticSiteResourceHarvester.harvestHTML(
			"<html><head><script type=\"importmap\">{\"imports\": " +
				"{\"react\": \"/o/frontend-js-react-web/exports/react.js\"}}" +
					"</script></head><body></body></html>");

		Assert.assertTrue(
			urls.toString(),
			urls.contains("/o/frontend-js-react-web/exports/react.js"));
	}

	@Test
	public void testHarvestHTMLInlineStyleURL() {
		Set<String> urls = _staticSiteResourceHarvester.harvestHTML(
			"<html><body><style>.a { background: " +
				"url('/o/my-web/images/a.png'); }</style></body></html>");

		Assert.assertTrue(
			urls.toString(), urls.contains("/o/my-web/images/a.png"));
	}

	@Test
	public void testHarvestHTMLLinkAndScript() {
		Set<String> urls = _staticSiteResourceHarvester.harvestHTML(
			"<html><head><link href=\"/o/classic-theme/css/clay.css\" " +
				"rel=\"stylesheet\"><script src=\"/o/my-web/js/main.js\">" +
					"</script></head><body></body></html>");

		Assert.assertTrue(
			urls.toString(), urls.contains("/o/classic-theme/css/clay.css"));
		Assert.assertTrue(
			urls.toString(), urls.contains("/o/my-web/js/main.js"));
	}

	@Test
	public void testHarvestHTMLSkipsExternalAndPageURLs() {
		Set<String> urls = _staticSiteResourceHarvester.harvestHTML(
			"<html><body><a href=\"/home\">Home</a>" +
				"<a href=\"https://liferay.com\">Liferay</a>" +
					"<img src=\"/documents/20126/0/a.png\"></body></html>");

		Assert.assertEquals(urls.toString(), 1, urls.size());
		Assert.assertTrue(
			urls.toString(), urls.contains("/documents/20126/0/a.png"));
	}

	@Test
	public void testHarvestHTMLSrcset() {
		Set<String> urls = _staticSiteResourceHarvester.harvestHTML(
			"<source srcset=\"/o/adaptive-media/image/1/Thumbnail/a.png?t=1 " +
				"2x, /o/adaptive-media/image/1/Preview/a.png?t=1 1x\">");

		Assert.assertTrue(
			urls.toString(),
			urls.contains("/o/adaptive-media/image/1/Thumbnail/a.png?t=1"));
		Assert.assertTrue(
			urls.toString(),
			urls.contains("/o/adaptive-media/image/1/Preview/a.png?t=1"));
	}

	@Test
	public void testHarvestHTMLStripsFragment() {
		Set<String> urls = _staticSiteResourceHarvester.harvestHTML(
			"<html><body><svg><use href=\"/o/classic-theme/images/icons.svg" +
				"#user\"/></svg></body></html>");

		Assert.assertTrue(
			urls.toString(),
			urls.contains("/o/classic-theme/images/icons.svg"));
	}

	@Test
	public void testIsHarvestableURL() {
		Assert.assertTrue(
			_staticSiteResourceHarvester.isHarvestableURL("/o/a/b.css"));
		Assert.assertTrue(
			_staticSiteResourceHarvester.isHarvestableURL("/documents/1/2/a"));
		Assert.assertFalse(
			_staticSiteResourceHarvester.isHarvestableURL("/home"));
		Assert.assertFalse(
			_staticSiteResourceHarvester.isHarvestableURL(
				"https://liferay.com/o/a.css"));
		Assert.assertFalse(_staticSiteResourceHarvester.isHarvestableURL(null));
	}

	private StaticSiteResourceHarvester _staticSiteResourceHarvester;

}