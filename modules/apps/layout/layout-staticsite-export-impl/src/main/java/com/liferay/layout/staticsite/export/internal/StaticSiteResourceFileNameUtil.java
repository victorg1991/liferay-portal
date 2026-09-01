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

/**
 * Names the file a harvested resource is written to.
 *
 * @author Víctor Galán
 */
public class StaticSiteResourceFileNameUtil {

	/**
	 * Returns the path, relative to the root of the static site, of the file
	 * the resource at the given URL is written to.
	 *
	 * <p>
	 * The URL's own path is kept, so that a stylesheet reaching a font beside
	 * itself still reaches it. Two departures from that are necessary. A query
	 * string cannot be part of a file name, so a digest of it is folded in,
	 * which also keeps two resources that differ only there apart. And a file
	 * whose name states no type is given the one its URL states further up,
	 * because a web server has nothing but the extension to describe what it is
	 * serving.
	 * </p>
	 */
	public static String getFileName(String url) {
		String path = url;
		String queryString = null;

		int index = url.indexOf(CharPool.QUESTION);

		if (index != -1) {
			path = url.substring(0, index);
			queryString = url.substring(index + 1);
		}

		path = _addInheritedExtension(
			StringUtil.removeFirst(path, StringPool.SLASH));

		if (Validator.isNull(queryString)) {
			return path;
		}

		String digest = StringUtil.toHexString(queryString.hashCode());

		int extensionIndex = path.lastIndexOf(CharPool.PERIOD);

		// A period in a directory name is not this file's extension, and
		// folding the digest in there would name a different directory

		if (extensionIndex <= path.lastIndexOf(CharPool.SLASH)) {
			return path + StringPool.PERIOD + digest;
		}

		return StringBundler.concat(
			path.substring(0, extensionIndex), StringPool.PERIOD, digest,
			path.substring(extensionIndex));
	}

	/**
	 * Returns the given path with the extension its nearest ancestor states,
	 * when the file itself states none.
	 *
	 * <p>
	 * A document URL ends in an identifier rather than a file name, and names
	 * the type one segment up:
	 * <code>/documents/1/2/emblem.svg/4bca3313-75ce</code>. Written as it
	 * stands, the file has no extension, so a static server offers it as
	 * <code>application/octet-stream</code> and a browser declines to render
	 * it.
	 * </p>
	 */
	private static String _addInheritedExtension(String path) {
		int slashIndex = path.lastIndexOf(CharPool.SLASH);

		if (path.indexOf(CharPool.PERIOD, slashIndex + 1) != -1) {
			return path;
		}

		String extension = null;

		int end = slashIndex;

		while (end > 0) {
			int begin = path.lastIndexOf(CharPool.SLASH, end - 1);

			String segment = path.substring(begin + 1, end);

			int periodIndex = segment.lastIndexOf(CharPool.PERIOD);

			if (periodIndex != -1) {
				extension = segment.substring(periodIndex + 1);

				break;
			}

			end = begin;
		}

		if (!_isExtension(extension)) {
			return path;
		}

		return path + StringPool.PERIOD + extension;
	}

	private static boolean _isExtension(String extension) {
		if (Validator.isNull(extension) ||
			(extension.length() > _EXTENSION_MAX_LENGTH)) {

			return false;
		}

		for (char c : extension.toCharArray()) {
			if (!Character.isLetterOrDigit(c)) {
				return false;
			}
		}

		return true;
	}

	private static final int _EXTENSION_MAX_LENGTH = 5;

}