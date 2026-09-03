/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.staticsite.export;

import com.liferay.portal.kernel.util.StringUtil;

import java.io.File;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author Víctor Galán
 */
public class StaticSiteExportResult {

	/**
	 * Records that the page addressed by the given URL, in the given locale, is
	 * written to the given file.
	 */
	public void addExportedPage(
		Locale locale, String friendlyURL, String fileName) {

		Map<String, String> exportedPageFileNames =
			_exportedPageFileNamesByLocale.computeIfAbsent(
				locale, exportedLocale -> new LinkedHashMap<>());

		exportedPageFileNames.put(friendlyURL, fileName);
	}

	public void addFailure(String url, String message) {
		_failures.add(new Failure(url, message));
	}

	public void addResource(String url, String fileName) {
		_resourceFileNames.put(url, fileName);
	}

	public void addSkippedPage(String friendlyURL, String message) {
		_skippedPages.add(new Failure(friendlyURL, message));
	}

	/**
	 * Records a page URL that names its own locale, such as the ones the theme
	 * emits as alternates. These are answered for from every locale, because a
	 * page in one locale links to its siblings in the others.
	 */
	public void addTranslatedPage(String friendlyURL, String fileName) {
		_translatedPageFileNames.put(friendlyURL, fileName);
	}

	/**
	 * Records a file the build wrote, by the path it was written to and a
	 * digest of its bytes.
	 *
	 * <p>
	 * Two exports of the same site are compared through these: a path present
	 * in one and not the other was added or removed, and a path in both whose
	 * digest differs was rewritten. Neither is visible from the file names
	 * alone, and neither survives unzipping over a previous export.
	 * </p>
	 */
	public void addWrittenFile(String fileName, byte[] bytes) {
		_writtenFiles.put(
			fileName, new WrittenFile(_getDigest(bytes), bytes.length));
	}

	public Set<Locale> getExportedLocales() {
		return _exportedPageFileNamesByLocale.keySet();
	}

	public int getExportedPageCount() {
		Set<String> fileNames = new HashSet<>();

		for (Map<String, String> exportedPageFileNames :
				_exportedPageFileNamesByLocale.values()) {

			fileNames.addAll(exportedPageFileNames.values());
		}

		return fileNames.size();
	}

	public Map<String, String> getExportedPageFileNames(Locale locale) {
		return _exportedPageFileNamesByLocale.getOrDefault(
			locale, Collections.emptyMap());
	}

	public List<Failure> getFailures() {
		return _failures;
	}

	public File getFile() {
		return _file;
	}

	public Map<String, String> getResourceFileNames() {
		return _resourceFileNames;
	}

	public List<Failure> getSkippedPages() {
		return _skippedPages;
	}

	public Map<String, String> getTranslatedPageFileNames() {
		return _translatedPageFileNames;
	}

	public Map<String, WrittenFile> getWrittenFiles() {
		return _writtenFiles;
	}

	public boolean hasResource(String url) {
		return _resourceFileNames.containsKey(url);
	}

	public void setFile(File file) {
		_file = file;
	}

	public static class Failure {

		public Failure(String url, String message) {
			_url = url;
			_message = message;
		}

		public String getMessage() {
			return _message;
		}

		public String getURL() {
			return _url;
		}

		private final String _message;
		private final String _url;

	}

	public static class WrittenFile {

		public WrittenFile(String digest, int size) {
			_digest = digest;
			_size = size;
		}

		public String getDigest() {
			return _digest;
		}

		public int getSize() {
			return _size;
		}

		private final String _digest;
		private final int _size;

	}

	private String _getDigest(byte[] bytes) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");

			return StringUtil.bytesToHexString(messageDigest.digest(bytes));
		}
		catch (NoSuchAlgorithmException noSuchAlgorithmException) {
			throw new IllegalStateException(noSuchAlgorithmException);
		}
	}

	private final Map<Locale, Map<String, String>>
		_exportedPageFileNamesByLocale = new LinkedHashMap<>();
	private final List<Failure> _failures = new ArrayList<>();
	private File _file;
	private final Map<String, String> _resourceFileNames =
		new LinkedHashMap<>();
	private final List<Failure> _skippedPages = new ArrayList<>();
	private final Map<String, String> _translatedPageFileNames =
		new LinkedHashMap<>();
	private final Map<String, WrittenFile> _writtenFiles =
		new LinkedHashMap<>();

}