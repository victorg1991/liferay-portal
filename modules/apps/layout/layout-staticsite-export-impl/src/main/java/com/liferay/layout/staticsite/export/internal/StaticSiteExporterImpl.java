/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.staticsite.export.internal;

import com.liferay.asset.display.page.model.AssetDisplayPageEntry;
import com.liferay.asset.display.page.service.AssetDisplayPageEntryLocalService;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.asset.kernel.service.persistence.AssetEntryQuery;
import com.liferay.friendly.url.model.FriendlyURLEntry;
import com.liferay.friendly.url.service.FriendlyURLEntryLocalService;
import com.liferay.info.item.InfoItemReference;
import com.liferay.layout.page.template.constants.LayoutPageTemplateEntryTypeConstants;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalService;
import com.liferay.layout.renderer.LayoutHTMLRenderer;
import com.liferay.layout.staticsite.export.StaticSiteExportResult;
import com.liferay.layout.staticsite.export.StaticSiteExporter;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.FriendlyURLResolver;
import com.liferay.portal.kernel.portlet.FriendlyURLResolverRegistryUtil;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.servlet.ServletContextPool;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.zip.ZipWriter;
import com.liferay.portal.kernel.zip.ZipWriterFactory;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Víctor Galán
 */
@Component(service = StaticSiteExporter.class)
public class StaticSiteExporterImpl implements StaticSiteExporter {

	@Override
	public StaticSiteExportResult exportSite(
			long groupId, HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, Locale locale)
		throws PortalException {

		StaticSiteExportResult staticSiteExportResult =
			new StaticSiteExportResult();

		Map<String, String> pageHTMLs = new LinkedHashMap<>();

		User user = _userLocalService.getGuestUser(
			_portal.getCompanyId(httpServletRequest));

		for (Layout layout : _getExportableLayouts(groupId)) {
			String friendlyURL = layout.getFriendlyURL(locale);

			try {
				pageHTMLs.put(
					friendlyURL,
					_layoutHTMLRenderer.renderHTML(
						httpServletRequest, httpServletResponse, layout, locale,
						null, user));

				_addExportedPage(friendlyURL, groupId, staticSiteExportResult);
			}
			catch (Exception exception) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Unable to render " + friendlyURL + " as guest",
						exception);
				}

				staticSiteExportResult.addSkippedPage(
					friendlyURL, exception.getMessage());
			}
		}

		_renderDisplayPages(
			groupId, httpServletRequest, httpServletResponse, locale, pageHTMLs,
			staticSiteExportResult, user);

		ZipWriter zipWriter = _zipWriterFactory.getZipWriter();

		_writeResources(
			httpServletRequest, httpServletResponse, pageHTMLs.values(),
			staticSiteExportResult, zipWriter);

		StaticSiteURLRewriter staticSiteURLRewriter =
			new StaticSiteURLRewriter();

		String portalURL = _portal.getPortalURL(httpServletRequest);

		for (Map.Entry<String, String> entry : pageHTMLs.entrySet()) {
			try {
				zipWriter.addEntry(
					_getPageFileName(entry.getKey()),
					staticSiteURLRewriter.rewrite(
						entry.getValue(),
						staticSiteExportResult.getExportedPageFileNames(),
						staticSiteExportResult.getResourceFileNames(),
						portalURL));
			}
			catch (Exception exception) {
				staticSiteExportResult.addFailure(
					entry.getKey(), exception.getMessage());
			}
		}

		try {
			zipWriter.addEntry(
				"export-report.json", _getReportJSON(staticSiteExportResult));

			staticSiteExportResult.setFile(zipWriter.getFile());
		}
		catch (Exception exception) {
			throw new PortalException(
				"Unable to write the static site export", exception);
		}

		return staticSiteExportResult;
	}

	private void _addExportedPage(
		String friendlyURL, long groupId,
		StaticSiteExportResult staticSiteExportResult) {

		String fileName = _getPageFileName(friendlyURL);

		staticSiteExportResult.addExportedPage(friendlyURL, fileName);

		Group group = _groupLocalService.fetchGroup(groupId);

		if (group == null) {
			return;
		}

		staticSiteExportResult.addExportedPage(
			StringBundler.concat(
				_portal.getPathFriendlyURLPublic(), group.getFriendlyURL(),
				friendlyURL),
			fileName);
	}

	private void _appendFailures(
		StringBundler sb, List<StaticSiteExportResult.Failure> failures) {

		for (int i = 0; i < failures.size(); i++) {
			StaticSiteExportResult.Failure failure = failures.get(i);

			if (i > 0) {
				sb.append(", ");
			}

			sb.append("{\"url\": \"");
			sb.append(_escapeJSON(failure.getURL()));
			sb.append("\", \"message\": \"");
			sb.append(_escapeJSON(failure.getMessage()));
			sb.append("\"}");
		}
	}

	private String _escapeJSON(String value) {
		if (value == null) {
			return StringPool.BLANK;
		}

		value = StringUtil.replace(value, CharPool.BACK_SLASH, "\\\\");

		return StringUtil.replace(value, CharPool.QUOTE, "\\\"");
	}

	private String _getDisplayPageFriendlyURL(
		long classNameId, long classPK, Locale locale) {

		String className = _portal.getClassName(classNameId);

		FriendlyURLResolver friendlyURLResolver = null;

		for (FriendlyURLResolver curFriendlyURLResolver :
				FriendlyURLResolverRegistryUtil.
					getFriendlyURLResolversAsCollection(
						_portal.getDefaultCompanyId())) {

			if (className.equals(curFriendlyURLResolver.getKey())) {
				friendlyURLResolver = curFriendlyURLResolver;

				break;
			}
		}

		if (friendlyURLResolver == null) {
			return null;
		}

		try {
			FriendlyURLEntry friendlyURLEntry =
				_friendlyURLEntryLocalService.getMainFriendlyURLEntry(
					classNameId, classPK);

			String urlTitle = friendlyURLEntry.getUrlTitle(
				LocaleUtil.toLanguageId(locale));

			if (Validator.isNull(urlTitle)) {
				return null;
			}

			return friendlyURLResolver.getDefaultURLSeparator() + urlTitle;
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug("Unable to get the friendly URL", exception);
			}

			return null;
		}
	}

	private List<long[]> _getDisplayPageItems(
		long groupId, LayoutPageTemplateEntry layoutPageTemplateEntry) {

		List<long[]> classNameIdsAndClassPKs = new ArrayList<>();

		for (AssetDisplayPageEntry assetDisplayPageEntry :
				_assetDisplayPageEntryLocalService.
					getAssetDisplayPageEntriesByLayoutPageTemplateEntryId(
						layoutPageTemplateEntry.
							getLayoutPageTemplateEntryId())) {

			classNameIdsAndClassPKs.add(
				new long[] {
					assetDisplayPageEntry.getClassNameId(),
					assetDisplayPageEntry.getClassPK()
				});
		}

		if (!classNameIdsAndClassPKs.isEmpty() ||
			!layoutPageTemplateEntry.isDefaultTemplate()) {

			return classNameIdsAndClassPKs;
		}

		AssetEntryQuery assetEntryQuery = new AssetEntryQuery();

		assetEntryQuery.setClassNameIds(
			new long[] {layoutPageTemplateEntry.getClassNameId()});

		if (layoutPageTemplateEntry.getClassTypeId() > 0) {
			assetEntryQuery.setClassTypeIds(
				new long[] {layoutPageTemplateEntry.getClassTypeId()});
		}

		assetEntryQuery.setGroupIds(new long[] {groupId});
		assetEntryQuery.setVisible(Boolean.TRUE);

		for (AssetEntry assetEntry :
				_assetEntryLocalService.getEntries(assetEntryQuery)) {

			classNameIdsAndClassPKs.add(
				new long[] {
					assetEntry.getClassNameId(), assetEntry.getClassPK()
				});
		}

		return classNameIdsAndClassPKs;
	}

	private List<Layout> _getExportableLayouts(long groupId) {
		List<Layout> layouts = new ArrayList<>();

		for (Layout layout : _layoutLocalService.getLayouts(groupId, false)) {
			if (layout.isHidden() || layout.isSystem() ||
				(layout.getStatus() != WorkflowConstants.STATUS_APPROVED) ||
				!_supportedLayoutTypes.contains(layout.getType())) {

				continue;
			}

			layouts.add(layout);
		}

		return layouts;
	}

	private String _getPageFileName(String friendlyURL) {
		if (Validator.isNull(friendlyURL) ||
			friendlyURL.equals(StringPool.SLASH)) {

			return _INDEX_FILE_NAME;
		}

		return StringUtil.removeFirst(friendlyURL, StringPool.SLASH) + ".html";
	}

	private String _getReportJSON(
		StaticSiteExportResult staticSiteExportResult) {

		StringBundler sb = new StringBundler();

		Set<String> pageFileNames = new HashSet<>(
			staticSiteExportResult.getExportedPageFileNames(
			).values());

		sb.append("{\"deployAtWebServerRoot\": true, \"pages\": ");
		sb.append(pageFileNames.size());
		sb.append(", \"resources\": ");
		sb.append(
			staticSiteExportResult.getResourceFileNames(
			).size());
		sb.append(", \"skippedPages\": [");

		_appendFailures(sb, staticSiteExportResult.getSkippedPages());

		sb.append("], \"failures\": [");

		_appendFailures(sb, staticSiteExportResult.getFailures());

		sb.append("]}");

		return sb.toString();
	}

	private String _getResourceFileName(String url) {
		String path = url;
		String queryString = null;

		int index = url.indexOf(CharPool.QUESTION);

		if (index != -1) {
			path = url.substring(0, index);
			queryString = url.substring(index + 1);
		}

		path = StringUtil.removeFirst(path, StringPool.SLASH);

		path = StringUtil.replace(
			path,
			new char[] {CharPool.OPEN_PARENTHESIS, CharPool.CLOSE_PARENTHESIS},
			new String[] {StringPool.BLANK, StringPool.BLANK});

		if (Validator.isNull(queryString)) {
			return path;
		}

		String digest = StringUtil.toHexString(queryString.hashCode());

		int extensionIndex = path.lastIndexOf(CharPool.PERIOD);

		if (extensionIndex == -1) {
			return path + StringPool.PERIOD + digest;
		}

		return StringBundler.concat(
			path.substring(0, extensionIndex), StringPool.PERIOD, digest,
			path.substring(extensionIndex));
	}

	private void _renderDisplayPages(
		long groupId, HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse, Locale locale,
		Map<String, String> pageHTMLs,
		StaticSiteExportResult staticSiteExportResult, User user) {

		for (LayoutPageTemplateEntry layoutPageTemplateEntry :
				_layoutPageTemplateEntryLocalService.
					getLayoutPageTemplateEntries(groupId)) {

			if (layoutPageTemplateEntry.getType() !=
					LayoutPageTemplateEntryTypeConstants.DISPLAY_PAGE) {

				continue;
			}

			Layout layout = _layoutLocalService.fetchLayout(
				layoutPageTemplateEntry.getPlid());

			if (layout == null) {
				continue;
			}

			for (long[] classNameIdAndClassPK :
					_getDisplayPageItems(groupId, layoutPageTemplateEntry)) {

				String friendlyURL = _getDisplayPageFriendlyURL(
					classNameIdAndClassPK[0], classNameIdAndClassPK[1], locale);

				if (Validator.isNull(friendlyURL) ||
					pageHTMLs.containsKey(friendlyURL)) {

					continue;
				}

				try {
					pageHTMLs.put(
						friendlyURL,
						_layoutHTMLRenderer.renderHTML(
							httpServletRequest, httpServletResponse,
							new InfoItemReference(
								_portal.getClassName(classNameIdAndClassPK[0]),
								classNameIdAndClassPK[1]),
							layout, locale, user));

					_addExportedPage(
						friendlyURL, groupId, staticSiteExportResult);
				}
				catch (Exception exception) {
					if (_log.isWarnEnabled()) {
						_log.warn(
							"Unable to render " + friendlyURL + " as guest",
							exception);
					}

					staticSiteExportResult.addSkippedPage(
						friendlyURL, exception.getMessage());
				}
			}
		}
	}

	private void _writeResources(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse, Iterable<String> pageHTMLs,
		StaticSiteExportResult staticSiteExportResult, ZipWriter zipWriter) {

		ServletContext servletContext = ServletContextPool.get(
			_portal.getServletContextName());

		StaticSiteResourceFetcher staticSiteResourceFetcher =
			new StaticSiteResourceFetcher(
				httpServletRequest, httpServletResponse, servletContext);

		StaticSiteResourceHarvester staticSiteResourceHarvester =
			new StaticSiteResourceHarvester();

		Deque<String> urls = new ArrayDeque<>();
		Set<String> visitedURLs = new HashSet<>();

		for (String pageHTML : pageHTMLs) {
			urls.addAll(staticSiteResourceHarvester.harvestHTML(pageHTML));
		}

		while (!urls.isEmpty()) {
			String url = urls.removeFirst();

			if (!visitedURLs.add(url)) {
				continue;
			}

			byte[] bytes = null;

			try {
				bytes = staticSiteResourceFetcher.fetch(url);
			}
			catch (Exception exception) {
				if (_log.isDebugEnabled()) {
					_log.debug("Unable to fetch " + url, exception);
				}
			}

			if (ArrayUtil.isEmpty(bytes)) {
				staticSiteExportResult.addFailure(url, "Unable to fetch");

				continue;
			}

			String fileName = _getResourceFileName(url);

			try {
				zipWriter.addEntry(fileName, bytes);
			}
			catch (Exception exception) {
				staticSiteExportResult.addFailure(url, exception.getMessage());

				continue;
			}

			staticSiteExportResult.addResource(url, fileName);

			if (StringUtil.endsWith(url, ".css") || url.contains(".css?")) {
				urls.addAll(
					staticSiteResourceHarvester.harvestCSS(
						new String(bytes), url));
			}
		}
	}

	private static final String _INDEX_FILE_NAME = "index.html";

	private static final Log _log = LogFactoryUtil.getLog(
		StaticSiteExporterImpl.class);

	private static final Set<String> _supportedLayoutTypes = new HashSet<>(
		Arrays.asList(
			LayoutConstants.TYPE_CONTENT, LayoutConstants.TYPE_EMBEDDED,
			LayoutConstants.TYPE_PANEL, LayoutConstants.TYPE_PORTLET));

	@Reference
	private AssetDisplayPageEntryLocalService
		_assetDisplayPageEntryLocalService;

	@Reference
	private AssetEntryLocalService _assetEntryLocalService;

	@Reference
	private FriendlyURLEntryLocalService _friendlyURLEntryLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private LayoutHTMLRenderer _layoutHTMLRenderer;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private LayoutPageTemplateEntryLocalService
		_layoutPageTemplateEntryLocalService;

	@Reference
	private Portal _portal;

	@Reference
	private UserLocalService _userLocalService;

	@Reference
	private ZipWriterFactory _zipWriterFactory;

}