/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.staticsite.export.internal;

import com.liferay.layout.staticsite.export.StaticSiteExportResult;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Víctor Galán
 */
public class StaticSiteExportResultTest {

	@ClassRule
	public static LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_staticSiteExportResult = new StaticSiteExportResult();
	}

	@Test
	public void testAddWrittenFileRecordsPathDigestAndSize() {
		_staticSiteExportResult.addWrittenFile(
			"index.html", "<html></html>".getBytes());

		Map<String, StaticSiteExportResult.WrittenFile> writtenFiles =
			_staticSiteExportResult.getWrittenFiles();

		Assert.assertEquals(writtenFiles.toString(), 1, writtenFiles.size());

		StaticSiteExportResult.WrittenFile writtenFile = writtenFiles.get(
			"index.html");

		Assert.assertEquals(13, writtenFile.getSize());
		Assert.assertEquals(
			"6ea9bf1e2c1e4e14e1d5c1b7b0d3e0e6a5a1e8dc".length(),
			writtenFile.getDigest(
			).length());
	}

	@Test
	public void testDigestDiffersWhenContentsDiffer() {

		// A page that keeps its path while its contents change is the case the
		// file names alone cannot report

		_staticSiteExportResult.addWrittenFile("a.html", "one".getBytes());

		StaticSiteExportResult staticSiteExportResult =
			new StaticSiteExportResult();

		staticSiteExportResult.addWrittenFile("a.html", "two".getBytes());

		Map<String, StaticSiteExportResult.WrittenFile> writtenFiles =
			_staticSiteExportResult.getWrittenFiles();

		Assert.assertNotEquals(
			writtenFiles.get(
				"a.html"
			).getDigest(),
			staticSiteExportResult.getWrittenFiles(
			).get(
				"a.html"
			).getDigest());
	}

	@Test
	public void testDigestMatchesWhenContentsMatch() {
		_staticSiteExportResult.addWrittenFile("a.html", "same".getBytes());

		StaticSiteExportResult staticSiteExportResult =
			new StaticSiteExportResult();

		staticSiteExportResult.addWrittenFile("b.html", "same".getBytes());

		Map<String, StaticSiteExportResult.WrittenFile> writtenFiles =
			_staticSiteExportResult.getWrittenFiles();

		Assert.assertEquals(
			writtenFiles.get(
				"a.html"
			).getDigest(),
			staticSiteExportResult.getWrittenFiles(
			).get(
				"b.html"
			).getDigest());
	}

	@Test
	public void testWrittenFilesKeepInsertionOrder() {
		_staticSiteExportResult.addWrittenFile("z.html", "z".getBytes());
		_staticSiteExportResult.addWrittenFile("a.html", "a".getBytes());

		Assert.assertArrayEquals(
			new String[] {"z.html", "a.html"},
			_staticSiteExportResult.getWrittenFiles(
			).keySet(
			).toArray(
				new String[0]
			));
	}

	private StaticSiteExportResult _staticSiteExportResult;

}