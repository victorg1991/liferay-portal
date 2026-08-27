/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.staticsite.export.internal;

import com.liferay.petra.function.UnsafeSupplier;
import com.liferay.petra.io.StreamUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.BufferCacheServletResponse;
import com.liferay.portal.kernel.servlet.DirectRequestDispatcherFactoryUtil;
import com.liferay.portal.kernel.servlet.DynamicServletRequest;
import com.liferay.portal.kernel.servlet.HttpMethods;
import com.liferay.portal.kernel.servlet.ServletContextPool;
import com.liferay.portal.kernel.util.Validator;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.ByteBuffer;

import java.util.Arrays;

/**
 * @author Víctor Galán
 */
public class StaticSiteResourceFetcher {

	public StaticSiteResourceFetcher(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse, String portalURL,
		ServletContext servletContext,
		StaticSiteBundleResourceResolver staticSiteBundleResourceResolver) {

		_httpServletRequest = httpServletRequest;
		_httpServletResponse = httpServletResponse;
		_portalURL = portalURL;
		_servletContext = servletContext;
		_staticSiteBundleResourceResolver = staticSiteBundleResourceResolver;
	}

	/**
	 * Returns the bytes of the resource at the given URL, from the bundle that
	 * owns it, from the servlet that generates it, or from the portal over
	 * HTTP, whichever answers first.
	 *
	 * <p>
	 * Each source is tried independently, because a source that fails outright
	 * says nothing about the ones after it and must not deny them their turn.
	 * </p>
	 */
	public byte[] fetch(String url) throws Exception {
		int index = url.indexOf(CharPool.QUESTION);

		String path = (index == -1) ? url : url.substring(0, index);

		String unhashedURL = _unhash(url);

		for (UnsafeSupplier<byte[], Exception> unsafeSupplier :
				Arrays.<UnsafeSupplier<byte[], Exception>>asList(
					() -> _staticSiteBundleResourceResolver.resolve(path),
					() -> _fetch(url),
					() -> unhashedURL.equals(url) ? null : _fetch(unhashedURL),
					() -> _request(url))) {

			byte[] bytes = null;

			try {
				bytes = unsafeSupplier.get();
			}
			catch (Exception exception) {
				if (_log.isDebugEnabled()) {
					_log.debug("Unable to fetch " + url, exception);
				}
			}

			if ((bytes != null) && (bytes.length > 0)) {
				return bytes;
			}
		}

		return null;
	}

	private byte[] _fetch(String url) throws Exception {
		if (_httpServletRequest == null) {
			return null;
		}

		String path = url;
		String queryString = null;

		int index = url.indexOf(CharPool.QUESTION);

		if (index != -1) {
			path = url.substring(0, index);
			queryString = url.substring(index + 1);
		}

		ServletContext servletContext = _servletContext;

		String dispatchPath = path;

		if (path.startsWith(_MODULE_PATH_PREFIX)) {
			int slashIndex = path.indexOf(
				CharPool.SLASH, _MODULE_PATH_PREFIX.length());

			if (slashIndex != -1) {
				ServletContext moduleServletContext = ServletContextPool.get(
					path.substring(_MODULE_PATH_PREFIX.length(), slashIndex));

				if (moduleServletContext != null) {
					servletContext = moduleServletContext;

					dispatchPath = path.substring(slashIndex);
				}
			}
		}

		RequestDispatcher requestDispatcher =
			DirectRequestDispatcherFactoryUtil.getRequestDispatcher(
				servletContext, dispatchPath);

		if (requestDispatcher == null) {
			return null;
		}

		HttpServletRequest httpServletRequest = _httpServletRequest;

		if (Validator.isNotNull(queryString)) {
			httpServletRequest = DynamicServletRequest.addQueryString(
				httpServletRequest, queryString, false);
		}

		BufferCacheServletResponse bufferCacheServletResponse =
			new BufferCacheServletResponse(_httpServletResponse);

		requestDispatcher.include(
			new ResourcePathHttpServletRequestWrapper(
				httpServletRequest, dispatchPath),
			bufferCacheServletResponse);

		ByteBuffer byteBuffer = bufferCacheServletResponse.getByteBuffer();

		byte[] bytes = new byte[byteBuffer.remaining()];

		byteBuffer.get(bytes);

		return bytes;
	}

	/**
	 * Asks the portal for a resource it generates per request rather than
	 * stores, which is the only way left to obtain one.
	 */
	private byte[] _request(String url) throws Exception {
		if (Validator.isNull(_portalURL)) {
			return null;
		}

		HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(
			_portalURL + url
		).openConnection();

		try {
			httpURLConnection.setConnectTimeout(_TIMEOUT);
			httpURLConnection.setInstanceFollowRedirects(true);
			httpURLConnection.setReadTimeout(_TIMEOUT);
			httpURLConnection.setRequestMethod(HttpMethods.GET);

			if (httpURLConnection.getResponseCode() !=
					HttpServletResponse.SC_OK) {

				return null;
			}

			try (InputStream inputStream = httpURLConnection.getInputStream()) {
				return StreamUtil.toByteArray(inputStream);
			}
		}
		finally {
			httpURLConnection.disconnect();
		}
	}

	private String _unhash(String url) {
		int index = url.indexOf(".(");

		if (index == -1) {
			return url;
		}

		int endIndex = url.indexOf(")", index);

		if (endIndex == -1) {
			return url;
		}

		return url.substring(0, index) + url.substring(endIndex + 1);
	}

	private static final String _MODULE_PATH_PREFIX = "/o/";

	private static final int _TIMEOUT = 20000;

	private static final Log _log = LogFactoryUtil.getLog(
		StaticSiteResourceFetcher.class);

	private final HttpServletRequest _httpServletRequest;
	private final HttpServletResponse _httpServletResponse;
	private final String _portalURL;
	private final ServletContext _servletContext;
	private final StaticSiteBundleResourceResolver
		_staticSiteBundleResourceResolver;

	private static class ResourcePathHttpServletRequestWrapper
		extends HttpServletRequestWrapper {

		public ResourcePathHttpServletRequestWrapper(
			HttpServletRequest httpServletRequest, String path) {

			super(httpServletRequest);

			_path = path;
		}

		@Override
		public String getPathInfo() {
			int index = _path.indexOf(CharPool.SLASH, 1);

			if (index == -1) {
				return null;
			}

			return _path.substring(index);
		}

		@Override
		public String getRequestURI() {
			return _path;
		}

		@Override
		public String getServletPath() {
			int index = _path.indexOf(CharPool.SLASH, 1);

			if (index == -1) {
				return _path;
			}

			return _path.substring(0, index);
		}

		private final String _path;

	}

}