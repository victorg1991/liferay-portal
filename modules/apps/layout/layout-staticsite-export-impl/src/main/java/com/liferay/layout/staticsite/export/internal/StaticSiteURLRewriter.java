/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
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
import java.util.regex.Pattern;

/**
 * @author Víctor Galán
 */
public class StaticSiteURLRewriter {

	public String rewrite(
		String html, Map<String, String> pageFileNames,
		Map<String, String> resourceFileNames, String portalURL) {

		html = _alternateLinkPattern.matcher(
			html
		).replaceAll(
			StringPool.BLANK
		);

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

		for (Map.Entry<String, String> entry :
				_sortByKeyLengthDescending(pageFileNames)) {

			String friendlyURL = entry.getKey();

			String replacement = StringPool.SLASH + entry.getValue();

			for (char delimiter : _URL_DELIMITERS) {
				html = StringUtil.replace(
					html, "\"" + friendlyURL + delimiter,
					"\"" + replacement + delimiter);
			}
		}

		return html;
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

	private static final char[] _URL_DELIMITERS = {
		CharPool.POUND, CharPool.QUESTION, CharPool.QUOTE
	};

	private static final Pattern _alternateLinkPattern = Pattern.compile(
		"<link[^>]+rel=\"alternate\"[^>]*>");

}