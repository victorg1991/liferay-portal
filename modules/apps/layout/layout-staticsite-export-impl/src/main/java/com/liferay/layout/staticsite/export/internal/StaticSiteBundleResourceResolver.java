/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.staticsite.export.internal;

import com.liferay.petra.io.StreamUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.InputStream;

import java.net.URL;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Reads resources published under <code>/o</code> straight from the bundle that
 * owns them, since the OSGi HTTP pipeline cannot be reached by a nested
 * dispatch.
 *
 * @author Víctor Galán
 */
public class StaticSiteBundleResourceResolver {

	public StaticSiteBundleResourceResolver(BundleContext bundleContext) {
		for (Bundle bundle : bundleContext.getBundles()) {
			String webContextPath = bundle.getHeaders(
				StringPool.BLANK
			).get(
				"Web-ContextPath"
			);

			if (Validator.isNotNull(webContextPath)) {
				_bundles.put(
					StringUtil.removeFirst(webContextPath, StringPool.SLASH),
					bundle);
			}
		}
	}

	public byte[] resolve(String path) throws Exception {
		if (!path.startsWith(_MODULE_PATH_PREFIX)) {
			return null;
		}

		int index = path.indexOf(CharPool.SLASH, _MODULE_PATH_PREFIX.length());

		if (index == -1) {
			return null;
		}

		Bundle bundle = _bundles.get(
			path.substring(_MODULE_PATH_PREFIX.length(), index));

		if (bundle == null) {
			return null;
		}

		URL url = _getEntryURL(bundle, path.substring(index));

		if (url == null) {
			return null;
		}

		try (InputStream inputStream = url.openStream()) {
			return StreamUtil.toByteArray(inputStream);
		}
	}

	private URL _getEntryURL(Bundle bundle, String path) {
		for (String prefix : _ENTRY_PREFIXES) {
			URL url = bundle.getEntry(prefix + path);

			if (url != null) {
				return url;
			}
		}

		String unhashedPath = _unhash(path);

		for (String prefix : _ENTRY_PREFIXES) {
			URL url = bundle.getEntry(prefix + unhashedPath);

			if (url != null) {
				return url;
			}
		}

		int index = unhashedPath.lastIndexOf(CharPool.SLASH);

		String fileName = unhashedPath.substring(index + 1);

		for (String prefix : _ENTRY_PREFIXES) {
			String dirPath = prefix + unhashedPath.substring(0, index);

			if (Validator.isNull(dirPath)) {
				dirPath = StringPool.SLASH;
			}

			Enumeration<URL> enumeration = bundle.findEntries(
				dirPath, StringPool.STAR, false);

			while ((enumeration != null) && enumeration.hasMoreElements()) {
				URL url = enumeration.nextElement();

				String urlPath = url.getPath();

				String candidateFileName = urlPath.substring(
					urlPath.lastIndexOf(CharPool.SLASH) + 1);

				if (fileName.equals(_unhash(candidateFileName))) {
					return url;
				}
			}
		}

		return null;
	}

	private String _unhash(String path) {
		int index = path.indexOf(".(");

		if (index == -1) {
			return path;
		}

		int endIndex = path.indexOf(CharPool.CLOSE_PARENTHESIS, index);

		if (endIndex == -1) {
			return path;
		}

		return path.substring(0, index) + path.substring(endIndex + 1);
	}

	private static final String[] _ENTRY_PREFIXES = {
		"META-INF/resources", StringPool.BLANK
	};

	private static final String _MODULE_PATH_PREFIX = "/o/";

	private final Map<String, Bundle> _bundles = new HashMap<>();

}