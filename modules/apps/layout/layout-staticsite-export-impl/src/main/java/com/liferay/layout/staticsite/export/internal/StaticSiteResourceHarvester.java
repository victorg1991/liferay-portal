/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.staticsite.export.internal;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Víctor Galán
 */
public class StaticSiteResourceHarvester {

	public Set<String> harvestCSS(String css, String cssURL) {
		Set<String> urls = new LinkedHashSet<>();

		Matcher matcher = _cssURLPattern.matcher(css);

		while (matcher.find()) {
			String url = matcher.group(1);

			if (url == null) {
				url = matcher.group(2);
			}

			_addURL(urls, _resolve(_unquote(url), cssURL));
		}

		return urls;
	}

	public Set<String> harvestHTML(String html) {
		Set<String> urls = new LinkedHashSet<>();

		Document document = Jsoup.parse(html);

		for (String attributeName : _ATTRIBUTE_NAMES) {
			Elements elements = document.select("[" + attributeName + "]");

			for (Element element : elements) {
				_addURL(urls, element.attr(attributeName));
			}
		}

		Elements elements = document.select("[srcset]");

		for (Element element : elements) {
			for (String candidate :
					StringUtil.split(element.attr("srcset"), CharPool.COMMA)) {

				_addURL(
					urls,
					StringUtil.split(StringUtil.trim(candidate), CharPool.SPACE)
						[0]);
			}
		}

		Matcher matcher = _cssURLPattern.matcher(html);

		while (matcher.find()) {
			String url = matcher.group(1);

			if (url == null) {
				url = matcher.group(2);
			}

			_addURL(urls, _unquote(url));
		}

		urls.addAll(_harvestImportMap(document));

		return urls;
	}

	public Set<String> harvestJS(String js) {
		Set<String> urls = new LinkedHashSet<>();

		Matcher matcher = _jsModulePathPattern.matcher(js);

		while (matcher.find()) {
			_addURL(urls, matcher.group(1));
		}

		return urls;
	}

	public boolean isHarvestableURL(String url) {
		if (Validator.isNull(url)) {
			return false;
		}

		for (String prefix : _RESOURCE_PREFIXES) {
			if (url.startsWith(prefix)) {
				return true;
			}
		}

		return false;
	}

	private void _addURL(Set<String> urls, String url) {
		if (Validator.isNull(url)) {
			return;
		}

		url = StringUtil.trim(url);

		int index = url.indexOf(CharPool.POUND);

		if (index != -1) {
			url = url.substring(0, index);
		}

		if (isHarvestableURL(url)) {
			urls.add(url);
		}
	}

	private Set<String> _harvestImportMap(Document document) {
		Set<String> urls = new LinkedHashSet<>();

		Elements elements = document.select("script[type=importmap]");

		for (Element element : elements) {
			Matcher matcher = _jsonStringValuePattern.matcher(element.data());

			while (matcher.find()) {
				_addURL(urls, matcher.group(1));
			}
		}

		return urls;
	}

	private String _resolve(String url, String baseURL) {
		if (Validator.isNull(url) || url.startsWith(StringPool.SLASH) ||
			url.startsWith("data:") || url.startsWith("http")) {

			return url;
		}

		int index = baseURL.lastIndexOf(CharPool.SLASH);

		if (index == -1) {
			return url;
		}

		String path = baseURL.substring(0, index + 1);

		while (url.startsWith("../")) {
			url = url.substring(3);

			path = path.substring(0, path.length() - 1);

			int lastIndex = path.lastIndexOf(CharPool.SLASH);

			if (lastIndex == -1) {
				break;
			}

			path = path.substring(0, lastIndex + 1);
		}

		if (url.startsWith("./")) {
			url = url.substring(2);
		}

		return path + url;
	}

	private String _unquote(String url) {
		if (Validator.isNull(url)) {
			return url;
		}

		url = StringUtil.trim(url);

		return StringUtil.unquote(url);
	}

	private static final String[] _ATTRIBUTE_NAMES = {"href", "poster", "src"};

	private static final String[] _RESOURCE_PREFIXES = {
		"/combo", "/documents/", "/image/", "/o/", "/webserver/"
	};

	private static final Pattern _cssURLPattern = Pattern.compile(
		"url\\(([^)]+)\\)|@import\\s+[\"']([^\"']+)[\"']");
	private static final Pattern _jsModulePathPattern = Pattern.compile(
		"[\"']([-@$/.\\w()]*?/o/[-@$/.\\w()]+\\.(?:css|js))[\"']");
	private static final Pattern _jsonStringValuePattern = Pattern.compile(
		":\\s*\"([^\"]+)\"");

}