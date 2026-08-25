/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.internal.renderer;

import com.liferay.layout.renderer.LayoutHTMLRenderer;
import com.liferay.layout.util.LayoutServiceContextHelper;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutSet;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.servlet.DynamicServletRequest;
import com.liferay.portal.kernel.servlet.ServletContextPool;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.theme.ThemeUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
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
			HttpServletResponse httpServletResponse, Layout layout,
			Locale locale, String segmentsExperienceKey, User user)
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

			httpServletRequest.setAttribute(WebKeys.LOCALE, locale);

			SegmentsExperience segmentsExperience = _getSegmentsExperience(
				httpServletRequest, layout, segmentsExperienceKey, user);

			if (segmentsExperience != null) {
				httpServletRequest.setAttribute(
					SegmentsWebKeys.SEGMENTS_EXPERIENCE_IDS,
					new long[] {segmentsExperience.getSegmentsExperienceId()});
			}

			layout.includeLayoutContent(
				httpServletRequest, httpServletResponse);

			StringBundler sb = (StringBundler)httpServletRequest.getAttribute(
				WebKeys.LAYOUT_CONTENT);

			LayoutSet layoutSet = layout.getLayoutSet();

			Document document = Jsoup.parse(
				ThemeUtil.include(
					ServletContextPool.get(StringPool.BLANK),
					httpServletRequest, httpServletResponse,
					"portal_normal.ftl", layoutSet.getTheme(), false));

			Element bodyElement = document.body();

			bodyElement.html(sb.toString());

			return document.html();
		}
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

}