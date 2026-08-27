/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.staticsite.export.internal;

import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Víctor Galán
 */
public class StaticSiteURLRewriterTest {

	@ClassRule
	public static LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_staticSiteURLRewriter = new StaticSiteURLRewriter();
	}

	@Test
	public void testRewriteEscapedResourceURL() {
		String html = _staticSiteURLRewriter.rewrite(
			"<script src=\"/o/a/b.js?x=1&amp;y=2\"></script>",
			Collections.emptyMap(),
			HashMapBuilder.put(
				"/o/a/b.js?x=1&y=2", "o/a/b.abcd1234.js"
			).build(),
			null);

		Assert.assertEquals(
			"<script src=\"/o/a/b.abcd1234.js\"></script>", html);
	}

	@Test
	public void testRewriteHashedResourceURL() {
		String html = _staticSiteURLRewriter.rewrite(
			"<link href=\"/o/my-web/css/main.(abc123).css\">",
			Collections.emptyMap(),
			HashMapBuilder.put(
				"/o/my-web/css/main.(abc123).css",
				"o/my-web/css/main.d41d8cd9.css"
			).build(),
			null);

		Assert.assertEquals(
			"<link href=\"/o/my-web/css/main.d41d8cd9.css\">", html);
	}

	@Test
	public void testRewriteLongestResourceURLFirst() {
		String html = _staticSiteURLRewriter.rewrite(
			"<link href=\"/o/a/b.css?t=1\">", Collections.emptyMap(),
			HashMapBuilder.put(
				"/o/a/b.css", "o/a/b.css"
			).put(
				"/o/a/b.css?t=1", "o/a/b.abcd1234.css"
			).build(),
			null);

		Assert.assertEquals("<link href=\"/o/a/b.abcd1234.css\">", html);
	}

	@Test
	public void testRewritePageLink() {
		String html = _staticSiteURLRewriter.rewrite(
			"<a href=\"/home\">Home</a><a href=\"/about-us\">About</a>",
			HashMapBuilder.put(
				"/about-us", "about-us.html"
			).put(
				"/home", "index.html"
			).build(),
			Collections.emptyMap(), null);

		Assert.assertEquals(
			"<a href=\"/index.html\">Home</a>" +
				"<a href=\"/about-us.html\">About</a>",
			html);
	}

	@Test
	public void testRewritePageLinkDoesNotTouchLongerPath() {
		String html = _staticSiteURLRewriter.rewrite(
			"<a href=\"/newsletter\">Newsletter</a>",
			HashMapBuilder.put(
				"/news", "news.html"
			).build(),
			Collections.emptyMap(), null);

		Assert.assertEquals("<a href=\"/newsletter\">Newsletter</a>", html);
	}

	@Test
	public void testRewriteStripsAlternateLinks() {
		String html = _staticSiteURLRewriter.rewrite(
			"<link href=\"/es/web/site/home\" hreflang=\"es-ES\" " +
				"rel=\"alternate\"><title>Kept</title>",
			Collections.emptyMap(), Collections.emptyMap(), null);

		Assert.assertEquals("<title>Kept</title>", html);
	}

	@Test
	public void testRewriteStripsEscapedPortalURL() {
		String html = _staticSiteURLRewriter.rewrite(
			"currentURL: 'http\\x3a\\x2f\\x2flocalhost\\x3a8080/home'",
			Collections.emptyMap(), Collections.emptyMap(),
			"http://localhost:8080");

		Assert.assertEquals("currentURL: '/home'", html);
	}

	@Test
	public void testRewriteStripsPortalURL() {
		String html = _staticSiteURLRewriter.rewrite(
			"<img src=\"http://localhost:8080/documents/1/a.png\">",
			Collections.emptyMap(), Collections.emptyMap(),
			"http://localhost:8080");

		Assert.assertEquals("<img src=\"/documents/1/a.png\">", html);
	}

	private StaticSiteURLRewriter _staticSiteURLRewriter;

}