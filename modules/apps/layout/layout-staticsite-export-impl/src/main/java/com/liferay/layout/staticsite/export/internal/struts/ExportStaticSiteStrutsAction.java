/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.layout.staticsite.export.internal.struts;

import com.liferay.layout.staticsite.export.StaticSiteBuilder;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.permission.GroupPermissionUtil;
import com.liferay.portal.kernel.servlet.ServletResponseUtil;
import com.liferay.portal.kernel.struts.StrutsAction;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.zip.ZipWriter;
import com.liferay.portal.kernel.zip.ZipWriterFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Víctor Galán
 */
@Component(
	property = "path=/portal/layout_staticsite_export/export_static_site",
	service = StrutsAction.class
)
public class ExportStaticSiteStrutsAction implements StrutsAction {

	@Override
	public String execute(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws Exception {

		long groupId = ParamUtil.getLong(httpServletRequest, "groupId");

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		if (!GroupPermissionUtil.contains(
				permissionChecker, groupId, ActionKeys.MANAGE_LAYOUTS)) {

			throw new PrincipalException.MustHavePermission(
				permissionChecker, Group.class.getName(), groupId,
				ActionKeys.MANAGE_LAYOUTS);
		}

		long[] plids = ParamUtil.getLongValues(httpServletRequest, "plids");

		try {
			ZipWriter zipWriter = _zipWriterFactory.getZipWriter();

			_staticSiteBuilder.build(
				groupId, _portal.getLocale(httpServletRequest), plids,
				zipWriter::addEntry);

			File file = zipWriter.getFile();

			Group group = _groupLocalService.getGroup(groupId);

			try (InputStream inputStream = new FileInputStream(file)) {
				ServletResponseUtil.sendFile(
					httpServletRequest, httpServletResponse,
					group.getGroupKey() + "-static-site.zip", inputStream,
					file.length(), ContentTypes.APPLICATION_ZIP);
			}
		}
		catch (Exception exception) {
			_portal.sendError(
				exception, httpServletRequest, httpServletResponse);
		}

		return null;
	}

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private Portal _portal;

	@Reference
	private StaticSiteBuilder _staticSiteBuilder;

	@Reference
	private ZipWriterFactory _zipWriterFactory;

}