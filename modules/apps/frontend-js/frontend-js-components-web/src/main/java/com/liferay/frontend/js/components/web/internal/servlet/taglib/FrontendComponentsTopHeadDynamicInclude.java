/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.frontend.js.components.web.internal.servlet.taglib;

import com.liferay.frontend.js.components.web.internal.configuration.FFFrontendJSComponentsConfiguration;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.servlet.taglib.BaseDynamicInclude;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.url.builder.AbsolutePortalURLBuilder;
import com.liferay.portal.url.builder.AbsolutePortalURLBuilderFactory;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Julien Castelain
 */
@Component(
	configurationPid = "com.liferay.frontend.js.components.web.internal.configuration.FFFrontendJSComponentsConfiguration",
	immediate = true, service = DynamicInclude.class
)
public class FrontendComponentsTopHeadDynamicInclude extends BaseDynamicInclude {

	@Override
	public void include(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, String key)
		throws IOException {

		if (!_ffFrontendJSComponentsConfiguration.enableClayTreeView()) {
			return;
		}

		PrintWriter printWriter = httpServletResponse.getWriter();

		StringBundler sb = new StringBundler();
		
		sb.append("<script>if (!Liferay) { Liferay = {} }}");
		sb.append("if (!Liferay.__FF__) { Liferay.__FF__ = {} }}");
		sb.append("Liferay.__FF__.clayTreeViewEnabled = ");
		sb.append(_ffFrontendJSComponentsConfiguration.enableClayTreeView());
		sb.append(";</script>");

		printWriter.println(sb.toString());
	}

	@Override
	public void register(DynamicIncludeRegistry dynamicIncludeRegistry) {
		dynamicIncludeRegistry.register("/html/common/themes/top_head.jsp#pre");
	}

	@Activate
	@Modified
	protected void activate(
			BundleContext bundleContext, ComponentContext componentContext,
			Map<String, Object> properties)
		throws Exception {

		_bundleContext = bundleContext;

		_lastModified = System.currentTimeMillis();

		_ffFrontendJSComponentsConfiguration = ConfigurableUtil.createConfigurable(
			FFFrontendJSComponentsConfiguration.class, properties);
	}

	@Reference
	private AbsolutePortalURLBuilderFactory _absolutePortalURLBuilderFactory;

	private volatile BundleContext _bundleContext;
	private volatile FFFrontendJSComponentsConfiguration _ffFrontendJSComponentsConfiguration;
	private volatile long _lastModified;

	@Reference
	private Portal _portal;

}
