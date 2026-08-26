/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.internal.renderer;

import com.liferay.info.constants.InfoDisplayWebKeys;
import com.liferay.info.item.InfoItemDetails;
import com.liferay.info.item.InfoItemReference;
import com.liferay.info.item.InfoItemServiceRegistry;
import com.liferay.info.item.provider.InfoItemDetailsProvider;
import com.liferay.layout.constants.LayoutWebKeys;
import com.liferay.layout.display.page.LayoutDisplayPageObjectProvider;
import com.liferay.layout.display.page.LayoutDisplayPageProvider;
import com.liferay.layout.display.page.LayoutDisplayPageProviderRegistry;
import com.liferay.layout.display.page.constants.LayoutDisplayPageWebKeys;
import com.liferay.layout.renderer.LayoutHTMLRenderer;
import com.liferay.layout.util.LayoutServiceContextHelper;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.LayoutSet;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.servlet.DynamicServletRequest;
import com.liferay.portal.kernel.servlet.ServletContextPool;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.theme.ThemeUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.webserver.WebServerServletToken;
import com.liferay.portal.struts.Definition;
import com.liferay.portal.struts.TilesUtil;
import com.liferay.segments.SegmentsEntryRetriever;
import com.liferay.segments.constants.SegmentsExperienceConstants;
import com.liferay.segments.constants.SegmentsWebKeys;
import com.liferay.segments.context.RequestContextMapper;
import com.liferay.segments.model.SegmentsExperience;
import com.liferay.segments.processor.SegmentsExperienceRequestProcessorRegistry;
import com.liferay.segments.service.SegmentsExperienceLocalService;
import com.liferay.segments.service.SegmentsExperienceService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Víctor Galán
 */
@Component(service = LayoutHTMLRenderer.class)
public class LayoutHTMLRendererImpl implements LayoutHTMLRenderer {

	@Override
	public String renderHTML(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse,
			InfoItemReference infoItemReference, Layout layout, Locale locale,
			User user, boolean wholePage)
		throws Exception {

		LayoutDisplayPageProvider<?> layoutDisplayPageProvider =
			_layoutDisplayPageProviderRegistry.
				getLayoutDisplayPageProviderByClassName(
					layout.getCompanyId(), infoItemReference.getClassName());

		if (layoutDisplayPageProvider == null) {
			throw new PortalException(
				"No layout display page provider for " +
					infoItemReference.getClassName());
		}

		LayoutDisplayPageObjectProvider<?> layoutDisplayPageObjectProvider =
			layoutDisplayPageProvider.getLayoutDisplayPageObjectProvider(
				infoItemReference);

		if (layoutDisplayPageObjectProvider == null) {
			throw new PortalException(
				"Unable to get the display object for " + infoItemReference);
		}

		Object infoItem = layoutDisplayPageObjectProvider.getDisplayObject();

		InfoItemDetailsProvider<Object> infoItemDetailsProvider =
			_infoItemServiceRegistry.getFirstInfoItemService(
				InfoItemDetailsProvider.class,
				layoutDisplayPageObjectProvider.getClassName());

		InfoItemDetails infoItemDetails = null;

		if (infoItemDetailsProvider != null) {
			infoItemDetails = infoItemDetailsProvider.getInfoItemDetails(
				infoItem);
		}

		return _renderHTML(
			httpServletRequest, httpServletResponse, infoItem, infoItemDetails,
			layout, layoutDisplayPageObjectProvider, locale, null, user,
			wholePage);
	}

	@Override
	public String renderHTML(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, Layout layout,
			Locale locale, String segmentsExperienceKey, User user,
			boolean wholePage)
		throws Exception {

		return _renderHTML(
			httpServletRequest, httpServletResponse, null, null, layout, null,
			locale, segmentsExperienceKey, user, wholePage);
	}

	private String _getCompanyLogo(Company company, String imagePath) {
		String companyLogo = imagePath + "/company_logo";

		long companyLogoId = company.getLogoId();

		if (companyLogoId <= 0) {
			return companyLogo;
		}

		return StringBundler.concat(
			companyLogo, "?img_id=", companyLogoId, "&t=",
			_webServerServletToken.getToken(companyLogoId));
	}

	private String _getLayoutSetLogo(
		Company company, String imagePath, LayoutSet layoutSet) {

		if (!company.isSiteLogo() || !layoutSet.isLogo()) {
			return null;
		}

		long logoId = layoutSet.getLogoId();

		if (logoId == 0) {
			logoId = layoutSet.getLiveLogoId();
		}

		if (logoId <= 0) {
			return null;
		}

		return StringBundler.concat(
			imagePath, "/layout_set_logo?img_id=", logoId, "&t=",
			_webServerServletToken.getToken(logoId));
	}

	private SegmentsExperience _getSegmentsExperience(
			HttpServletRequest httpServletRequest, Layout layout,
			String segmentsExperienceKey, User user)
		throws Exception {

		if (Validator.isNull(segmentsExperienceKey)) {
			return _getUserSegmentsExperience(httpServletRequest, layout, user);
		}

		return _segmentsExperienceService.fetchSegmentsExperience(
			layout.getGroupId(), segmentsExperienceKey, layout.getPlid());
	}

	private SegmentsExperience _getUserSegmentsExperience(
			HttpServletRequest httpServletRequest, Layout layout, User user)
		throws Exception {

		long[] segmentsExperienceIds =
			_segmentsExperienceRequestProcessorRegistry.
				getSegmentsExperienceIds(
					httpServletRequest, null, layout.getGroupId(),
					layout.getPlid(),
					_segmentsEntryRetriever.getSegmentsEntryIds(
						layout.getGroupId(), user.getUserId(),
						_requestContextMapper.map(httpServletRequest)));

		if (ArrayUtil.isEmpty(segmentsExperienceIds)) {
			return _segmentsExperienceLocalService.fetchSegmentsExperience(
				layout.getGroupId(), SegmentsExperienceConstants.KEY_DEFAULT,
				layout.getPlid());
		}

		return _segmentsExperienceLocalService.getSegmentsExperience(
			segmentsExperienceIds[0]);
	}

	private String _renderHTML(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, Object infoItem,
			InfoItemDetails infoItemDetails, Layout layout,
			LayoutDisplayPageObjectProvider<?> layoutDisplayPageObjectProvider,
			Locale locale, String segmentsExperienceKey, User user,
			boolean wholePage)
		throws Exception {

		try (AutoCloseable autoCloseable =
				_layoutServiceContextHelper.getServiceContextAutoCloseable(
					layout, user)) {

			ServiceContext serviceContext =
				ServiceContextThreadLocal.getServiceContext();

			httpServletRequest = _portal.getOriginalServletRequest(
				httpServletRequest);

			httpServletRequest = DynamicServletRequest.addQueryString(
				httpServletRequest, "p_l_id=" + layout.getPlid(), false);

			serviceContext.setRequest(httpServletRequest);

			ThemeDisplay themeDisplay = serviceContext.getThemeDisplay();

			String portalURL = _portal.getPortalURL(httpServletRequest);

			themeDisplay.setLanguageId(LocaleUtil.toLanguageId(locale));
			themeDisplay.setLocale(locale);
			themeDisplay.setPortalDomain(
				HttpComponentsUtil.getDomain(portalURL));
			themeDisplay.setPortalURL(portalURL);
			themeDisplay.setRequest(httpServletRequest);
			themeDisplay.setSecure(
				_portal.isForwardedSecure(httpServletRequest));
			themeDisplay.setServerName(
				_portal.getForwardedHost(httpServletRequest));
			themeDisplay.setServerPort(
				_portal.getForwardedPort(httpServletRequest));

			httpServletRequest.setAttribute(WebKeys.LAYOUT, layout);
			httpServletRequest.setAttribute(WebKeys.LOCALE, locale);

			themeDisplay.setLayout(layout);
			themeDisplay.setPlid(layout.getPlid());

			if (layoutDisplayPageObjectProvider != null) {
				httpServletRequest.setAttribute(
					InfoDisplayWebKeys.INFO_ITEM, infoItem);
				httpServletRequest.setAttribute(
					InfoDisplayWebKeys.INFO_ITEM_DETAILS, infoItemDetails);
				httpServletRequest.setAttribute(
					LayoutDisplayPageWebKeys.
						LAYOUT_DISPLAY_PAGE_OBJECT_PROVIDER,
					layoutDisplayPageObjectProvider);
			}

			SegmentsExperience segmentsExperience = _getSegmentsExperience(
				httpServletRequest, layout, segmentsExperienceKey, user);

			if (segmentsExperience != null) {
				httpServletRequest.setAttribute(
					SegmentsWebKeys.SEGMENTS_EXPERIENCE_IDS,
					new long[] {segmentsExperience.getSegmentsExperienceId()});
			}

			httpServletRequest.removeAttribute(WebKeys.LAYOUT_CONTENT);
			httpServletRequest.removeAttribute(LayoutWebKeys.LAYOUT_STRUCTURE);

			if (wholePage) {
				_setWholePageThemeDisplay(layout, themeDisplay);

				httpServletRequest.setAttribute(
					TilesUtil.DEFINITION,
					new Definition(
						StringPool.BLANK,
						HashMapBuilder.put(
							"content", _PATH_PORTAL_LAYOUT
						).put(
							"selectable", Boolean.TRUE.toString()
						).build()));
			}

			layout.includeLayoutContent(
				httpServletRequest, httpServletResponse);

			LayoutSet layoutSet = layout.getLayoutSet();

			String html = ThemeUtil.include(
				ServletContextPool.get(StringPool.BLANK), httpServletRequest,
				httpServletResponse, "portal_normal.ftl", layoutSet.getTheme(),
				false);

			if (wholePage) {
				return html;
			}

			StringBundler sb = (StringBundler)httpServletRequest.getAttribute(
				WebKeys.LAYOUT_CONTENT);

			Document document = Jsoup.parse(html);

			Element bodyElement = document.body();

			if (sb == null) {
				bodyElement.html(StringPool.BLANK);
			}
			else {
				bodyElement.html(sb.toString());
			}

			return document.html();
		}
	}

	/**
	 * Fills in what a page needs only when it is rendered whole: the logo the
	 * theme shows and the layouts it builds its navigation from.
	 */
	private void _setWholePageThemeDisplay(
			Layout layout, ThemeDisplay themeDisplay)
		throws Exception {

		Company company = _companyLocalService.getCompany(
			layout.getCompanyId());

		String imagePath = _portal.getPathImage();

		themeDisplay.setPathImage(imagePath);

		String layoutSetLogo = _getLayoutSetLogo(
			company, imagePath, layout.getLayoutSet());

		if (layoutSetLogo == null) {
			themeDisplay.setCompanyLogo(_getCompanyLogo(company, imagePath));
		}
		else {
			themeDisplay.setCompanyLogo(layoutSetLogo);
			themeDisplay.setLayoutSetLogo(layoutSetLogo);
		}

		themeDisplay.setLayouts(
			_layoutLocalService.getLayouts(
				layout.getGroupId(), layout.isPrivateLayout(),
				LayoutConstants.DEFAULT_PARENT_LAYOUT_ID));
	}

	private static final String _PATH_PORTAL_LAYOUT = "/portal/layout.jsp";

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private InfoItemServiceRegistry _infoItemServiceRegistry;

	@Reference
	private LayoutDisplayPageProviderRegistry
		_layoutDisplayPageProviderRegistry;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private LayoutServiceContextHelper _layoutServiceContextHelper;

	@Reference
	private Portal _portal;

	@Reference
	private RequestContextMapper _requestContextMapper;

	@Reference
	private SegmentsEntryRetriever _segmentsEntryRetriever;

	@Reference
	private SegmentsExperienceLocalService _segmentsExperienceLocalService;

	@Reference
	private SegmentsExperienceRequestProcessorRegistry
		_segmentsExperienceRequestProcessorRegistry;

	@Reference
	private SegmentsExperienceService _segmentsExperienceService;

	@Reference
	private WebServerServletToken _webServerServletToken;

}