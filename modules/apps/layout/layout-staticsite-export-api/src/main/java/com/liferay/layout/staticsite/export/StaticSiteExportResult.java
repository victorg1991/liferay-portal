/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.staticsite.export;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Víctor Galán
 */
public class StaticSiteExportResult {

	public void addExportedPage(String friendlyURL, String fileName) {
		_exportedPageFileNames.put(friendlyURL, fileName);
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

	public Map<String, String> getExportedPageFileNames() {
		return _exportedPageFileNames;
	}

	public List<Failure> getFailures() {
		return _failures;
	}

	public Map<String, String> getResourceFileNames() {
		return _resourceFileNames;
	}

	public List<Failure> getSkippedPages() {
		return _skippedPages;
	}

	public boolean hasResource(String url) {
		return _resourceFileNames.containsKey(url);
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

	private final Map<String, String> _exportedPageFileNames =
		new LinkedHashMap<>();
	private final List<Failure> _failures = new ArrayList<>();
	private final Map<String, String> _resourceFileNames =
		new LinkedHashMap<>();
	private final List<Failure> _skippedPages = new ArrayList<>();

}