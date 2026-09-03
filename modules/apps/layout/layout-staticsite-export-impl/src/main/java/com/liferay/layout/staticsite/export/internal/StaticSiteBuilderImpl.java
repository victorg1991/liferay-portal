/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.staticsite.export.internal;

import com.liferay.asset.display.page.model.AssetDisplayPageEntry;
import com.liferay.asset.display.page.service.AssetDisplayPageEntryLocalService;
import com.liferay.info.item.InfoItemReference;
import com.liferay.layout.display.page.LayoutDisplayPageObjectProvider;
import com.liferay.layout.display.page.LayoutDisplayPageProvider;
import com.liferay.layout.display.page.LayoutDisplayPageProviderRegistry;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalService;
import com.liferay.layout.renderer.LayoutHTMLRenderer;
import com.liferay.layout.staticsite.export.StaticSiteBuilder;
import com.liferay.layout.staticsite.export.StaticSiteExportResult;
import com.liferay.layout.staticsite.export.StaticSiteWriter;
import com.liferay.layout.util.LayoutServiceContextHelper;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.servlet.DummyHttpServletResponse;
import com.liferay.portal.kernel.servlet.ServletContextPool;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.nio.charset.StandardCharsets;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Víctor Galán
 */
@Component(service = StaticSiteBuilder.class)
public class StaticSiteBuilderImpl implements StaticSiteBuilder {

	@Override
	public StaticSiteExportResult build(
			long groupId, long[] plids, StaticSiteWriter staticSiteWriter)
		throws PortalException {

		Group group = _groupLocalService.getGroup(groupId);

		StaticSiteExportResult staticSiteExportResult =
			new StaticSiteExportResult();

		// The build renders pages and reads resources the way a request would,
		// so it runs against a service context whether or not a request is what
		// asked for it

		try (AutoCloseable autoCloseable =
				_layoutServiceContextHelper.getServiceContextAutoCloseable(
					_companyLocalService.getCompany(group.getCompanyId()))) {

			ServiceContext serviceContext =
				ServiceContextThreadLocal.getServiceContext();

			HttpServletRequest httpServletRequest = serviceContext.getRequest();

			HttpServletResponse httpServletResponse =
				new DummyHttpServletResponse();

			// Every file goes through the writer, so recording there is what
			// makes the inventory complete rather than a list maintained
			// alongside the code that writes

			StaticSiteWriter recordingStaticSiteWriter = (fileName, bytes) -> {
				staticSiteWriter.write(fileName, bytes);

				staticSiteExportResult.addWrittenFile(fileName, bytes);
			};

			String portalURL = _getPortalURL(group);

			User user = _userLocalService.getGuestUser(group.getCompanyId());

			Locale siteDefaultLocale = _portal.getSiteDefaultLocale(groupId);

			Map<Locale, Map<String, String>> pageHTMLsByLocale =
				new LinkedHashMap<>();

			for (Locale locale : _getExportedLocales(siteDefaultLocale)) {
				pageHTMLsByLocale.put(
					locale,
					_renderPages(
						groupId, httpServletRequest, httpServletResponse,
						locale, plids, portalURL, siteDefaultLocale,
						staticSiteExportResult, user));
			}

			// Every locale draws on one set of resources, so they are gathered
			// across all of them at once and written once

			List<String> pageHTMLs = new ArrayList<>();

			for (Map<String, String> localePageHTMLs :
					pageHTMLsByLocale.values()) {

				pageHTMLs.addAll(localePageHTMLs.values());
			}

			_writeResources(
				httpServletRequest, httpServletResponse, pageHTMLs, portalURL,
				staticSiteExportResult, recordingStaticSiteWriter);

			for (Map.Entry<Locale, Map<String, String>> entry :
					pageHTMLsByLocale.entrySet()) {

				_writePages(
					entry.getKey(), groupId, entry.getValue(), portalURL,
					siteDefaultLocale, staticSiteExportResult,
					recordingStaticSiteWriter);
			}

			// Written through the raw writer rather than the recording one,
			// since a report cannot carry a digest of itself

			staticSiteWriter.write(
				"export-report.json",
				_getBytes(_getReportJSON(staticSiteExportResult)));
		}
		catch (PortalException portalException) {
			throw portalException;
		}
		catch (Exception exception) {
			throw new PortalException(
				"Unable to build the static site", exception);
		}

		return staticSiteExportResult;
	}

	@Override
	public List<Layout> getExportableLayouts(long groupId) {
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

	private void _addExportedPage(
		Locale locale, String friendlyURL, long groupId,
		Locale siteDefaultLocale,
		StaticSiteExportResult staticSiteExportResult) {

		String fileName = _getPageFileName(
			locale, friendlyURL, siteDefaultLocale);

		staticSiteExportResult.addExportedPage(locale, friendlyURL, fileName);

		Group group = _groupLocalService.fetchGroup(groupId);

		if (group == null) {
			return;
		}

		// A site reached through a virtual host is addressed without the
		// friendly URL servlet mapping, so both forms name the same page and
		// both have to be answered for

		boolean defaultPage = friendlyURL.equals(
			_getDefaultFriendlyURL(groupId));

		for (String siteURL :
				Arrays.asList(
					_portal.getPathFriendlyURLPublic() + group.getFriendlyURL(),
					group.getFriendlyURL())) {

			staticSiteExportResult.addExportedPage(
				locale, siteURL + friendlyURL, fileName);

			// The theme names a page in another locale by prefixing its
			// language, which is how the alternate links reach their siblings.
			// Those are answered for from every locale rather than only from
			// this one

			staticSiteExportResult.addTranslatedPage(
				StringBundler.concat(
					StringPool.SLASH, LocaleUtil.toLanguageId(locale), siteURL,
					friendlyURL),
				fileName);
			staticSiteExportResult.addTranslatedPage(
				StringBundler.concat(
					StringPool.SLASH, locale.getLanguage(), siteURL,
					friendlyURL),
				fileName);

			if (defaultPage) {
				staticSiteExportResult.addExportedPage(
					locale, siteURL, fileName);
				staticSiteExportResult.addExportedPage(
					locale, siteURL + StringPool.SLASH, fileName);
				staticSiteExportResult.addTranslatedPage(
					StringPool.SLASH + locale.getLanguage() + siteURL,
					fileName);
			}
		}
	}

	private byte[] _getBytes(String s) {
		return s.getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * Finds the display page links a page makes, by looking for the separators
	 * the registered display page providers publish rather than for a fixed
	 * set of them.
	 */
	private String _getDefaultFriendlyURL(long groupId) {
		Layout layout = _layoutLocalService.fetchFirstLayout(
			groupId, false, LayoutConstants.DEFAULT_PARENT_LAYOUT_ID);

		if (layout == null) {
			return null;
		}

		return layout.getFriendlyURL();
	}

	/**
	 * Finds the display page links a page makes, by looking for the separators
	 * the registered display page providers publish rather than for a fixed
	 * set of them.
	 */
	private Set<String> _getDisplayPageFriendlyURLs(String pageHTML) {
		Set<String> friendlyURLs = new LinkedHashSet<>();

		for (String urlSeparator : _getURLSeparators()) {
			Matcher matcher = Pattern.compile(
				Pattern.quote(urlSeparator) + "([-\\w%.]+)"
			).matcher(
				pageHTML
			);

			while (matcher.find()) {
				friendlyURLs.add(urlSeparator + matcher.group(1));
			}
		}

		return friendlyURLs;
	}

	private Layout _getDisplayPageLayout(
		long groupId,
		LayoutDisplayPageObjectProvider<?> layoutDisplayPageObjectProvider) {

		AssetDisplayPageEntry assetDisplayPageEntry =
			_assetDisplayPageEntryLocalService.fetchAssetDisplayPageEntry(
				groupId, layoutDisplayPageObjectProvider.getClassNameId(),
				layoutDisplayPageObjectProvider.getClassPK());

		LayoutPageTemplateEntry layoutPageTemplateEntry = null;

		if (assetDisplayPageEntry != null) {
			layoutPageTemplateEntry =
				_layoutPageTemplateEntryLocalService.
					fetchLayoutPageTemplateEntry(
						assetDisplayPageEntry.getLayoutPageTemplateEntryId());
		}

		if (layoutPageTemplateEntry == null) {
			layoutPageTemplateEntry =
				_layoutPageTemplateEntryLocalService.
					fetchDefaultLayoutPageTemplateEntry(
						groupId,
						layoutDisplayPageObjectProvider.getClassNameId(),
						layoutDisplayPageObjectProvider.getClassTypeId());
		}

		if (layoutPageTemplateEntry == null) {
			return null;
		}

		return _layoutLocalService.fetchLayout(
			layoutPageTemplateEntry.getPlid());
	}

	/**
	 * Returns the file a page is written to. The site default locale sits at
	 * the root of the archive and every other locale under its language code,
	 * matching how the portal addresses them.
	 */
	private Set<Locale> _getExportedLocales(Locale siteDefaultLocale) {
		Set<Locale> locales = new LinkedHashSet<>();

		locales.add(siteDefaultLocale);

		Collections.addAll(locales, _PROVISIONAL_LOCALES);

		return locales;
	}

	private JSONArray _getFailuresJSONArray(
		List<StaticSiteExportResult.Failure> failures) {

		JSONArray jsonArray = _jsonFactory.createJSONArray();

		for (StaticSiteExportResult.Failure failure : failures) {
			jsonArray.put(
				JSONUtil.put(
					"message", failure.getMessage()
				).put(
					"url", failure.getURL()
				));
		}

		return jsonArray;
	}

	private LayoutDisplayPageProvider<?> _getLayoutDisplayPageProvider(
		String urlSeparator) {

		for (LayoutDisplayPageProvider<?> layoutDisplayPageProvider :
				_layoutDisplayPageProviderRegistry.
					getLayoutDisplayPageProviders()) {

			if (urlSeparator.equals(
					layoutDisplayPageProvider.getURLSeparator())) {

				return layoutDisplayPageProvider;
			}
		}

		return null;
	}

	private String _getPageFileName(
		Locale locale, String friendlyURL, Locale siteDefaultLocale) {

		String fileName = _INDEX_FILE_NAME;

		if (Validator.isNotNull(friendlyURL) &&
			!friendlyURL.equals(StringPool.SLASH)) {

			fileName =
				StringUtil.removeFirst(friendlyURL, StringPool.SLASH) + ".html";
		}

		if (Objects.equals(locale, siteDefaultLocale)) {
			return fileName;
		}

		return locale.getLanguage() + StringPool.SLASH + fileName;
	}

	private String _getPortalURL(Group group) throws PortalException {
		Company company = _companyLocalService.getCompany(group.getCompanyId());

		return _portal.getPortalURL(
			company.getVirtualHostname(), _portal.getPortalServerPort(false),
			false);
	}

	/**
	 * Returns the report written beside the site, which is what one export can
	 * be compared against another with.
	 *
	 * <p>
	 * The file inventory carries a path, a digest and a size for everything
	 * written. Diffing two reports says which files an export gained, which it
	 * lost, and which kept their path while their contents changed, none of
	 * which can be recovered from the archives themselves once one has been
	 * unzipped over the other.
	 * </p>
	 */
	private String _getReportJSON(StaticSiteExportResult staticSiteExportResult)
		throws Exception {

		JSONObject filesJSONObject = _jsonFactory.createJSONObject();

		for (Map.Entry<String, StaticSiteExportResult.WrittenFile> entry :
				staticSiteExportResult.getWrittenFiles(
				).entrySet()) {

			StaticSiteExportResult.WrittenFile writtenFile = entry.getValue();

			filesJSONObject.put(
				entry.getKey(),
				JSONUtil.put(
					"digest", writtenFile.getDigest()
				).put(
					"size", writtenFile.getSize()
				));
		}

		return JSONUtil.put(
			"deployAtWebServerRoot", true
		).put(
			"failures",
			_getFailuresJSONArray(staticSiteExportResult.getFailures())
		).put(
			"fileCount", filesJSONObject.length()
		).put(
			"files", filesJSONObject
		).put(
			"locales",
			JSONUtil.toJSONArray(
				staticSiteExportResult.getExportedLocales(),
				LocaleUtil::toLanguageId)
		).put(
			"pages", staticSiteExportResult.getExportedPageCount()
		).put(
			"resources",
			staticSiteExportResult.getResourceFileNames(
			).size()
		).put(
			"resourceURLs",
			JSONUtil.toJSONArray(
				staticSiteExportResult.getResourceFileNames(
				).keySet(),
				resourceURL -> resourceURL)
		).put(
			"skippedPages",
			_getFailuresJSONArray(staticSiteExportResult.getSkippedPages())
		).toString();
	}

	private List<Layout> _getSelectedLayouts(long groupId, long[] plids) {
		List<Layout> layouts = getExportableLayouts(groupId);

		if (ArrayUtil.isEmpty(plids)) {
			return layouts;
		}

		List<Layout> selectedLayouts = new ArrayList<>();

		for (Layout layout : layouts) {
			if (ArrayUtil.contains(plids, layout.getPlid())) {
				selectedLayouts.add(layout);
			}
		}

		return selectedLayouts;
	}

	private Set<String> _getURLSeparators() {
		Set<String> urlSeparators = new LinkedHashSet<>();

		for (LayoutDisplayPageProvider<?> layoutDisplayPageProvider :
				_layoutDisplayPageProviderRegistry.
					getLayoutDisplayPageProviders()) {

			String urlSeparator = layoutDisplayPageProvider.getURLSeparator();

			if (Validator.isNotNull(urlSeparator)) {
				urlSeparators.add(urlSeparator);
			}
		}

		return urlSeparators;
	}

	/**
	 * Expands the specifiers written against each import map prefix, against
	 * every location that prefix was given. A bilingual site states the same
	 * specifier twice, once per locale, and both locations hold files the
	 * archive needs.
	 */
	private Set<String> _harvestModuleSpecifiers(
		StaticSiteResourceHarvester staticSiteResourceHarvester, String content,
		Map<String, Set<String>> importMapPrefixes) {

		Set<String> urls = new LinkedHashSet<>();

		for (Map.Entry<String, Set<String>> entry :
				importMapPrefixes.entrySet()) {

			for (String prefixURL : entry.getValue()) {
				urls.addAll(
					staticSiteResourceHarvester.harvestModuleSpecifiers(
						content,
						Collections.singletonMap(entry.getKey(), prefixURL)));
			}
		}

		return urls;
	}

	private String _removeDynamicScripts(String html) {
		Matcher matcher = _scriptPattern.matcher(html);

		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			String script = matcher.group();

			boolean dynamic = false;

			for (String moduleName : _DYNAMIC_MODULE_NAMES) {
				if (script.contains(moduleName)) {
					dynamic = true;

					break;
				}
			}

			matcher.appendReplacement(
				sb,
				dynamic ? StringPool.BLANK : Matcher.quoteReplacement(script));
		}

		matcher.appendTail(sb);

		return sb.toString();
	}

	private String _renderDisplayPage(
		String friendlyURL, long groupId, HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse, Locale locale,
		String portalURL, StaticSiteExportResult staticSiteExportResult,
		User user) {

		int index = friendlyURL.indexOf(CharPool.SLASH, 1);

		String urlSeparator = friendlyURL.substring(0, index + 1);

		LayoutDisplayPageProvider<?> layoutDisplayPageProvider =
			_getLayoutDisplayPageProvider(urlSeparator);

		if (layoutDisplayPageProvider == null) {
			return null;
		}

		String urlTitle = friendlyURL.substring(index + 1);

		LayoutDisplayPageObjectProvider<?> layoutDisplayPageObjectProvider =
			layoutDisplayPageProvider.getLayoutDisplayPageObjectProvider(
				groupId, urlTitle);

		if (layoutDisplayPageObjectProvider == null) {
			staticSiteExportResult.addSkippedPage(
				friendlyURL, "Unable to get the item behind the link");

			return null;
		}

		Layout layout = _getDisplayPageLayout(
			groupId, layoutDisplayPageObjectProvider);

		if (layout == null) {
			staticSiteExportResult.addSkippedPage(
				friendlyURL, "No display page for the item behind the link");

			return null;
		}

		try {
			return _removeDynamicScripts(
				StringUtil.removeSubstring(
					_layoutHTMLRenderer.renderHTML(
						httpServletRequest, httpServletResponse,
						new InfoItemReference(
							layoutDisplayPageObjectProvider.getClassName(),
							layoutDisplayPageObjectProvider.getClassPK()),
						layout, locale, user, true),
					portalURL));
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to render " + friendlyURL + " as guest", exception);
			}

			staticSiteExportResult.addSkippedPage(
				friendlyURL, exception.getMessage());

			return null;
		}
	}

	/**
	 * Renders a display page for every item the exported pages link to, and for
	 * every item those pages link to in turn, so that what leaves the portal is
	 * what the site actually reaches rather than everything it holds.
	 */
	private void _renderDisplayPages(
		long groupId, HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse, Locale locale,
		Map<String, String> pageHTMLs, String portalURL,
		Locale siteDefaultLocale, StaticSiteExportResult staticSiteExportResult,
		User user) {

		Deque<String> friendlyURLs = new ArrayDeque<>(pageHTMLs.values());

		Set<String> visitedFriendlyURLs = new HashSet<>();

		while (!friendlyURLs.isEmpty()) {
			for (String friendlyURL :
					_getDisplayPageFriendlyURLs(friendlyURLs.removeFirst())) {

				if (!visitedFriendlyURLs.add(friendlyURL) ||
					pageHTMLs.containsKey(friendlyURL)) {

					continue;
				}

				String pageHTML = _renderDisplayPage(
					friendlyURL, groupId, httpServletRequest,
					httpServletResponse, locale, portalURL,
					staticSiteExportResult, user);

				if (pageHTML == null) {
					continue;
				}

				pageHTMLs.put(friendlyURL, pageHTML);

				_addExportedPage(
					locale, friendlyURL, groupId, siteDefaultLocale,
					staticSiteExportResult);

				friendlyURLs.add(pageHTML);
			}
		}
	}

	/**
	 * Renders the selected pages in one locale, follows the display pages they
	 * reach, and returns the HTML of each keyed by its friendly URL.
	 */
	private Map<String, String> _renderPages(
		long groupId, HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse, Locale locale, long[] plids,
		String portalURL, Locale siteDefaultLocale,
		StaticSiteExportResult staticSiteExportResult, User user) {

		Map<String, String> pageHTMLs = new LinkedHashMap<>();

		for (Layout layout : _getSelectedLayouts(groupId, plids)) {
			String friendlyURL = layout.getFriendlyURL(locale);

			try {
				pageHTMLs.put(
					friendlyURL,
					_removeDynamicScripts(
						StringUtil.removeSubstring(
							_layoutHTMLRenderer.renderHTML(
								httpServletRequest, httpServletResponse, layout,
								locale, null, user, true),
							portalURL)));

				_addExportedPage(
					locale, friendlyURL, groupId, siteDefaultLocale,
					staticSiteExportResult);
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
			portalURL, siteDefaultLocale, staticSiteExportResult, user);

		return pageHTMLs;
	}

	private void _writePages(
		Locale locale, long groupId, Map<String, String> pageHTMLs,
		String portalURL, Locale siteDefaultLocale,
		StaticSiteExportResult staticSiteExportResult,
		StaticSiteWriter staticSiteWriter) {

		StaticSiteURLRewriter staticSiteURLRewriter =
			new StaticSiteURLRewriter();

		String defaultFriendlyURL = _getDefaultFriendlyURL(groupId);

		for (Map.Entry<String, String> entry : pageHTMLs.entrySet()) {
			String friendlyURL = entry.getKey();

			try {
				String pageHTML = staticSiteURLRewriter.rewrite(
					entry.getValue(),
					staticSiteExportResult.getExportedPageFileNames(locale),
					staticSiteExportResult.getResourceFileNames(),
					staticSiteExportResult.getTranslatedPageFileNames(),
					portalURL);

				staticSiteWriter.write(
					_getPageFileName(locale, friendlyURL, siteDefaultLocale),
					_getBytes(pageHTML));

				// The archive answers for its root with the default locale's
				// own default page

				if (Objects.equals(locale, siteDefaultLocale) &&
					Objects.equals(friendlyURL, defaultFriendlyURL)) {

					staticSiteWriter.write(
						_INDEX_FILE_NAME, _getBytes(pageHTML));
				}
			}
			catch (Exception exception) {
				staticSiteExportResult.addFailure(
					friendlyURL, exception.getMessage());
			}
		}
	}

	private void _writeResources(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse, Iterable<String> pageHTMLs,
		String portalURL, StaticSiteExportResult staticSiteExportResult,
		StaticSiteWriter staticSiteWriter) {

		ServletContext servletContext = ServletContextPool.get(
			_portal.getServletContextName());

		StaticSiteBundleResourceResolver staticSiteBundleResourceResolver =
			new StaticSiteBundleResourceResolver(
				FrameworkUtil.getBundle(
					StaticSiteBuilderImpl.class
				).getBundleContext());

		StaticSiteResourceFetcher staticSiteResourceFetcher =
			new StaticSiteResourceFetcher(
				httpServletRequest, httpServletResponse, portalURL,
				servletContext, staticSiteBundleResourceResolver);

		StaticSiteResourceHarvester staticSiteResourceHarvester =
			new StaticSiteResourceHarvester();

		Deque<String> urls = new ArrayDeque<>();
		Set<String> visitedURLs = new HashSet<>();

		// One specifier resolves against a different location in each locale,
		// so every location it is given is kept rather than the last one

		Map<String, Set<String>> importMapPrefixes = new LinkedHashMap<>();

		for (String pageHTML : pageHTMLs) {
			urls.addAll(staticSiteResourceHarvester.harvestHTML(pageHTML));
			urls.addAll(
				staticSiteResourceHarvester.harvestLoaderModules(pageHTML));

			for (Map.Entry<String, String> entry :
					staticSiteResourceHarvester.harvestImportMapPrefixes(
						pageHTML
					).entrySet()) {

				Set<String> prefixURLs = importMapPrefixes.computeIfAbsent(
					entry.getKey(), specifier -> new LinkedHashSet<>());

				prefixURLs.add(entry.getValue());
			}
		}

		for (String pageHTML : pageHTMLs) {
			urls.addAll(
				_harvestModuleSpecifiers(
					staticSiteResourceHarvester, pageHTML, importMapPrefixes));
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

			String fileName = StaticSiteResourceFileNameUtil.getFileName(url);

			try {
				staticSiteWriter.write(fileName, bytes);
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
			else if (StringUtil.endsWith(url, ".js") || url.contains(".js?")) {
				String js = new String(bytes);

				urls.addAll(staticSiteResourceHarvester.harvestJS(js, url));
				urls.addAll(
					staticSiteResourceHarvester.harvestLoaderModules(js));
				urls.addAll(
					_harvestModuleSpecifiers(
						staticSiteResourceHarvester, js, importMapPrefixes));

				urls.addAll(
					staticSiteBundleResourceResolver.resolveSiblingModuleURLs(
						url));
			}
		}
	}

	private static final String[] _DYNAMIC_MODULE_NAMES = {
		"/o/audiences/bootstrap", "frontend-js-audiences-web",
		"frontend-js-spa-web"
	};

	private static final String _INDEX_FILE_NAME = "index.html";

	private static final Locale[] _PROVISIONAL_LOCALES = {
		LocaleUtil.US, LocaleUtil.SPAIN
	};

	private static final Log _log = LogFactoryUtil.getLog(
		StaticSiteBuilderImpl.class);

	private static final Pattern _scriptPattern = Pattern.compile(
		"<script\\b[^>]*>.*?</script>", Pattern.DOTALL);
	private static final Set<String> _supportedLayoutTypes = new HashSet<>(
		Arrays.asList(
			LayoutConstants.TYPE_CONTENT, LayoutConstants.TYPE_EMBEDDED,
			LayoutConstants.TYPE_PANEL, LayoutConstants.TYPE_PORTLET));

	@Reference
	private AssetDisplayPageEntryLocalService
		_assetDisplayPageEntryLocalService;

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private LayoutDisplayPageProviderRegistry
		_layoutDisplayPageProviderRegistry;

	@Reference
	private LayoutHTMLRenderer _layoutHTMLRenderer;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private LayoutPageTemplateEntryLocalService
		_layoutPageTemplateEntryLocalService;

	@Reference
	private LayoutServiceContextHelper _layoutServiceContextHelper;

	@Reference
	private Portal _portal;

	@Reference
	private UserLocalService _userLocalService;

}