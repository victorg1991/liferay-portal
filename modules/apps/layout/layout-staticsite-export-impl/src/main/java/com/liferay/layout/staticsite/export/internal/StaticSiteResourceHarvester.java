/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.staticsite.export.internal;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

			url = _unquote(url);

			if (url.startsWith(StringPool.POUND)) {
				continue;
			}

			_addURL(urls, _resolve(url, cssURL));
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

		urls.addAll(_harvestModuleStubs(html));

		urls.addAll(harvestJS(html, StringPool.BLANK));

		return urls;
	}

	/**
	 * Returns the module specifier prefixes the import map maps to a location
	 * rather than to a file, which only become URLs once a specifier is
	 * appended to them.
	 */
	public Map<String, String> harvestImportMapPrefixes(String html) {
		Map<String, String> prefixes = new LinkedHashMap<>();

		Document document = Jsoup.parse(html);

		Elements elements = document.select("script[type=importmap]");

		for (Element element : elements) {
			Matcher matcher = _jsonStringEntryPattern.matcher(element.data());

			while (matcher.find()) {
				String specifier = matcher.group(1);
				String url = matcher.group(2);

				if (specifier.endsWith(StringPool.SLASH) &&
					url.endsWith(StringPool.SLASH)) {

					prefixes.put(specifier, url);
				}
			}
		}

		return prefixes;
	}

	public Set<String> harvestJS(String js, String jsURL) {
		Set<String> urls = new LinkedHashSet<>();

		Matcher matcher = _jsModulePathPattern.matcher(js);

		while (matcher.find()) {
			_addURL(urls, StringPool.SLASH + matcher.group(1));
		}

		String strippedJS = _stripBlockComments(js);

		matcher = _jsRelativeModulePathPattern.matcher(strippedJS);

		while (matcher.find()) {
			_addURL(urls, _resolve(matcher.group(1), jsURL));
		}

		matcher = _jsRelativeStylesheetPathPattern.matcher(strippedJS);

		while (matcher.find()) {
			_addURL(urls, _resolve(matcher.group(1), jsURL));
		}

		return urls;
	}

	/**
	 * Collects the modules a legacy loader configuration declares, whose URLs
	 * are a group base joined to a relative path and so never appear whole.
	 */
	public Set<String> harvestLoaderModules(String js) {
		Set<String> urls = new LinkedHashSet<>();

		List<Integer> indexes = new ArrayList<>();
		List<String> expressions = new ArrayList<>();

		Matcher matcher = _loaderBasePattern.matcher(js);

		while (matcher.find()) {
			indexes.add(matcher.end());
			expressions.add(matcher.group(1));
		}

		for (int i = 0; i < indexes.size(); i++) {
			String base = _getLoaderBase(
				js, indexes.get(i), expressions.get(i));

			if (base == null) {
				continue;
			}

			int endIndex = js.length();

			if ((i + 1) < indexes.size()) {
				endIndex = indexes.get(i + 1);
			}

			Matcher pathMatcher = _loaderPathPattern.matcher(
				js.substring(indexes.get(i), endIndex));

			while (pathMatcher.find()) {
				_addURL(urls, base + pathMatcher.group(1));
			}
		}

		return urls;
	}

	public Set<String> harvestModuleSpecifiers(
		String content, Map<String, String> prefixes) {

		Set<String> urls = new LinkedHashSet<>();

		for (Map.Entry<String, String> entry : prefixes.entrySet()) {
			Matcher matcher = Pattern.compile(
				"[\"'`]" + Pattern.quote(entry.getKey()) + "([-/.\\w]+)[\"'`]"
			).matcher(
				content
			);

			while (matcher.find()) {
				_addURL(urls, entry.getValue() + matcher.group(1));
			}
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

	private String _getLoaderBase(String js, int index, String expression) {
		StringBundler sb = new StringBundler();

		for (String operand : StringUtil.split(expression, CharPool.PLUS)) {
			operand = operand.trim();

			Matcher matcher = _quotedLiteralPattern.matcher(operand);

			if (matcher.matches()) {
				sb.append(matcher.group(1));

				continue;
			}

			if (!_identifierPattern.matcher(
					operand
				).matches()) {

				continue;
			}

			matcher = Pattern.compile(
				"\\b(?:const|let|var)\\s+" + Pattern.quote(operand) +
					"\\s*=\\s*[\"']([^\"']+)[\"']"
			).matcher(
				js
			);

			String value = null;

			while (matcher.find() && (matcher.end() <= index)) {
				value = matcher.group(1);
			}

			if (value != null) {
				sb.append(value);
			}
		}

		String base = sb.toString();

		if (!base.startsWith(_MODULE_PATH_PREFIX) ||
			!base.endsWith(StringPool.SLASH)) {

			return null;
		}

		return base;
	}

	private Set<String> _harvestImportMap(Document document) {
		Set<String> urls = new LinkedHashSet<>();

		Elements elements = document.select("script[type=importmap]");

		for (Element element : elements) {
			Matcher matcher = _jsonStringValuePattern.matcher(element.data());

			while (matcher.find()) {
				String url = matcher.group(1);

				if (url.endsWith(StringPool.SLASH)) {
					continue;
				}

				_addURL(urls, url);
			}
		}

		return urls;
	}

	/**
	 * Collects the modules a page reaches through a stub, which builds its URL
	 * when something calls it and so never states one. The bundle it reaches
	 * for is an argument to the stub, and that is a literal.
	 */
	private Set<String> _harvestModuleStubs(String html) {
		Set<String> urls = new LinkedHashSet<>();

		Matcher matcher = _moduleStubPattern.matcher(html);

		while (matcher.find()) {
			_addURL(
				urls,
				StringBundler.concat(
					_MODULE_PATH_PREFIX, matcher.group(1),
					"/__liferay__/index.js"));
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

	/**
	 * Removes block comments, so that an import written inside documentation
	 * is not mistaken for one the script actually makes.
	 */
	private String _stripBlockComments(String js) {
		return _blockCommentPattern.matcher(
			js
		).replaceAll(
			StringPool.BLANK
		);
	}

	private String _unquote(String url) {
		if (Validator.isNull(url)) {
			return url;
		}

		url = StringUtil.trim(url);

		url = StringUtil.unquote(url);

		// A data URI can carry the parenthesis that ends a url(), leaving the
		// value cut short and its opening quote behind, so trim any quote the
		// pair above could not.

		while (!url.isEmpty() &&
			   ((url.charAt(0) == CharPool.QUOTE) ||
				(url.charAt(0) == CharPool.APOSTROPHE))) {

			url = url.substring(1);
		}

		return url;
	}

	private static final String[] _ATTRIBUTE_NAMES = {
		"href", "poster", "src", "xlink:href"
	};

	private static final String _MODULE_PATH_PREFIX = "/o/";

	private static final String _RESOURCE_EXTENSIONS =
		"css|gif|ico|jpeg|jpg|js|json|png|svg|webp|woff|woff2";

	private static final String[] _RESOURCE_PREFIXES = {
		"/combo", "/documents/", "/image/", "/o/", "/webserver/"
	};

	private static final Pattern _blockCommentPattern = Pattern.compile(
		"/\\*.*?\\*/", Pattern.DOTALL);
	private static final Pattern _cssURLPattern = Pattern.compile(
		"url\\(([^)]+)\\)|@import\\s+[\"']([^\"']+)[\"']");
	private static final Pattern _identifierPattern = Pattern.compile(
		"[$_A-Za-z][$_\\w]*");
	private static final Pattern _jsModulePathPattern = Pattern.compile(
		"[\"'`](?:\\$\\{[^}]*\\})?/?(o/[-@$/.\\w()]+\\.(?:" +
			_RESOURCE_EXTENSIONS + "))[\"'`]");
	private static final Pattern _jsonStringEntryPattern = Pattern.compile(
		"\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"");
	private static final Pattern _jsonStringValuePattern = Pattern.compile(
		":\\s*\"([^\"]+)\"");
	private static final Pattern _jsRelativeModulePathPattern = Pattern.compile(
		"(?:from|import)\\s*\\(?\\s*[\"'`](\\.{1,2}/[-@$/.\\w()]+" +
			"\\.(?:css|js))[\"'`]");

	/**
	 * A module reaches its own stylesheet by naming it relative to itself and
	 * resolving it against its own URL, rather than by importing it, so there
	 * is no import to recognize it by.
	 */
	private static final Pattern _jsRelativeStylesheetPathPattern =
		Pattern.compile("[\"'](\\.{1,2}/[-@$/.\\w()]+\\.css)[\"']");

	private static final Pattern _loaderBasePattern = Pattern.compile(
		"\\bbase:\\s*([^,]+)");
	private static final Pattern _loaderPathPattern = Pattern.compile(
		"\\bpath:\\s*[\"']([^\"']+)[\"']");
	private static final Pattern _moduleStubPattern = Pattern.compile(
		"buildESMStub\\(\\s*[\"']([-\\w.]+)[\"']");
	private static final Pattern _quotedLiteralPattern = Pattern.compile(
		"[\"']([^\"']*)[\"']");

}