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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Víctor Galán
 */
public class StaticSiteURLRewriter {

	/**
	 * Returns the given page with every reference to the portal replaced by one
	 * to the copy of what it named.
	 *
	 * <p>
	 * Substitution runs over the whole page rather than over parsed attributes,
	 * because the theme states as many of its URLs in script and in style as it
	 * does in markup.
	 * </p>
	 *
	 * @param pageFileNames the pages of this page's own locale, which is what
	 *        its navigation and its content link to
	 * @param translatedPageFileNames the pages of every locale, addressed the
	 *        way the theme addresses another locale, which is what the
	 *        alternate links point at
	 */
	public String rewrite(
		String html, Map<String, String> pageFileNames,
		Map<String, String> resourceFileNames,
		Map<String, String> translatedPageFileNames, String portalURL) {

		if (Validator.isNotNull(portalURL)) {
			html = StringUtil.removeSubstring(html, portalURL);
			html = StringUtil.removeSubstring(html, _escapeJS(portalURL));
		}

		for (Map.Entry<String, String> entry :
				_sortByKeyLengthDescending(resourceFileNames)) {

			String url = entry.getKey();
			String fileName = StringPool.SLASH + entry.getValue();

			if (url.equals(fileName)) {
				continue;
			}

			html = StringUtil.replace(html, url, fileName);
			html = StringUtil.replace(
				html, StringUtil.replace(url, CharPool.AMPERSAND, "&amp;"),
				fileName);
		}

		// A locale prefixed URL is longer than the same page without one, so
		// the alternates are replaced first and the plain forms cannot consume
		// their prefixes

		html = _rewritePageURLs(html, translatedPageFileNames);

		return _removeDanglingAlternateLinks(
			_rewritePageURLs(html, pageFileNames));
	}

	private String _escapeJS(String url) {
		StringBundler sb = new StringBundler();

		for (char c : url.toCharArray()) {
			if ((c == CharPool.COLON) || (c == CharPool.SLASH)) {
				sb.append("\\x");
				sb.append(Integer.toHexString(c));
			}
			else {
				sb.append(c);
			}
		}

		return sb.toString();
	}

	/**
	 * Returns the given page without the alternate links whose locale the build
	 * did not write. A site serves every locale the instance has, so a page
	 * names alternates the archive has no page for, and a reference to a
	 * translation that does not exist is worse than no reference at all.
	 */
	private String _removeDanglingAlternateLinks(String html) {
		Matcher matcher = _alternateLinkPattern.matcher(html);

		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			String alternateLink = matcher.group();

			matcher.appendReplacement(
				sb,
				alternateLink.contains(".html\"") ?
					Matcher.quoteReplacement(alternateLink) : StringPool.BLANK);
		}

		matcher.appendTail(sb);

		return sb.toString();
	}

	/**
	 * Replaces each page URL with its file, but only where the URL is bounded
	 * the way a reference to a page is bounded. Without that, a page whose
	 * friendly URL is a prefix of another's would be replaced inside it.
	 */
	private String _rewritePageURLs(
		String html, Map<String, String> pageFileNames) {

		for (Map.Entry<String, String> entry :
				_sortByKeyLengthDescending(pageFileNames)) {

			String friendlyURL = entry.getKey();

			String replacement = StringPool.SLASH + entry.getValue();

			for (char quote : _URL_QUOTES) {
				char[] terminators = {CharPool.POUND, CharPool.QUESTION, quote};

				for (char terminator : terminators) {
					String prefix = String.valueOf(quote);
					String suffix = String.valueOf(terminator);

					html = StringUtil.replace(
						html, prefix + friendlyURL + suffix,
						prefix + replacement + suffix);
				}
			}
		}

		return html;
	}

	private List<Map.Entry<String, String>> _sortByKeyLengthDescending(
		Map<String, String> map) {

		List<Map.Entry<String, String>> entries = new ArrayList<>(
			map.entrySet());

		entries.sort(
			(entry1, entry2) -> {
				String key1 = entry1.getKey();
				String key2 = entry2.getKey();

				return key2.length() - key1.length();
			});

		return entries;
	}

	private static final char[] _URL_QUOTES = {
		CharPool.QUOTE, CharPool.APOSTROPHE
	};

	private static final Pattern _alternateLinkPattern = Pattern.compile(
		"<link[^>]+rel=\"alternate\"[^>]*>");

}