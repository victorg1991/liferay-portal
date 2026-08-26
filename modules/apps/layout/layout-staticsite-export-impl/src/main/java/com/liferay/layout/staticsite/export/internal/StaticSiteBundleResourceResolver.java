/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.staticsite.export.internal;

import com.liferay.petra.io.StreamUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.InputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
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

	/**
	 * Lists every module a runtime loader can build a URL for under the
	 * directory the given module sits in, which no page or script states
	 * literally.
	 */
	public List<String> resolveSiblingModuleURLs(String path) {
		if (!path.startsWith(_MODULE_PATH_PREFIX)) {
			return Collections.emptyList();
		}

		int index = path.indexOf(CharPool.SLASH, _MODULE_PATH_PREFIX.length());

		if (index == -1) {
			return Collections.emptyList();
		}

		String webContextPath = path.substring(
			_MODULE_PATH_PREFIX.length(), index);

		Bundle bundle = _bundles.get(webContextPath);

		if (bundle == null) {
			return Collections.emptyList();
		}

		String bundlePath = path.substring(index);

		int fileNameIndex = bundlePath.lastIndexOf(CharPool.SLASH);

		if (fileNameIndex <= 0) {
			return Collections.emptyList();
		}

		String fileName = bundlePath.substring(fileNameIndex + 1);

		int moduleNameIndex = bundlePath.lastIndexOf(
			CharPool.SLASH, fileNameIndex - 1);

		if (moduleNameIndex == -1) {
			return Collections.emptyList();
		}

		String moduleName = bundlePath.substring(
			moduleNameIndex + 1, fileNameIndex);

		if (!fileName.equals(moduleName + _MODULE_FILE_NAME_SUFFIX)) {
			return Collections.emptyList();
		}

		String dirPath = bundlePath.substring(0, moduleNameIndex);

		List<String> urls = new ArrayList<>();

		for (String prefix : _ENTRY_PREFIXES) {
			Enumeration<URL> enumeration = bundle.findEntries(
				prefix + dirPath, StringPool.STAR + _MODULE_FILE_NAME_SUFFIX,
				true);

			while ((enumeration != null) && enumeration.hasMoreElements()) {
				URL url = enumeration.nextElement();

				String siblingModuleName = _getSiblingModuleName(
					url.getPath(), prefix + dirPath);

				if (siblingModuleName == null) {
					continue;
				}

				String modulePath = StringBundler.concat(
					_MODULE_PATH_PREFIX, webContextPath, dirPath,
					StringPool.SLASH, siblingModuleName);

				urls.add(
					StringBundler.concat(
						modulePath, StringPool.SLASH, siblingModuleName,
						_MODULE_FILE_NAME_SUFFIX));

				String skinPath = StringBundler.concat(
					dirPath, StringPool.SLASH, siblingModuleName,
					_SKIN_DIR_NAME, siblingModuleName, ".css");

				if (bundle.getEntry(prefix + skinPath) != null) {
					urls.add(
						StringBundler.concat(
							_MODULE_PATH_PREFIX, webContextPath, skinPath));
				}
			}

			if (!urls.isEmpty()) {
				return urls;
			}
		}

		return urls;
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

	private String _getSiblingModuleName(String urlPath, String dirPath) {
		int index = urlPath.indexOf(dirPath + StringPool.SLASH);

		if (index == -1) {
			return null;
		}

		String relativePath = urlPath.substring(index + dirPath.length() + 1);

		int slashIndex = relativePath.indexOf(CharPool.SLASH);

		if (slashIndex == -1) {
			return null;
		}

		String moduleName = relativePath.substring(0, slashIndex);

		if (!relativePath.equals(
				StringBundler.concat(
					moduleName, StringPool.SLASH, moduleName,
					_MODULE_FILE_NAME_SUFFIX))) {

			return null;
		}

		return moduleName;
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

	private static final String _MODULE_FILE_NAME_SUFFIX = "-min.js";

	private static final String _MODULE_PATH_PREFIX = "/o/";

	private static final String _SKIN_DIR_NAME = "/assets/skins/sam/";

	private final Map<String, Bundle> _bundles = new HashMap<>();

}