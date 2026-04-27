/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.audience.web.internal.portlet.action;

import com.liferay.audience.web.internal.constants.AudiencePortletKeys;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.segments.service.SegmentsEntryRoleLocalService;

import jakarta.portlet.ActionRequest;
import jakarta.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Drew Brokke
 */
@Component(
	property = {
		"jakarta.portlet.name=" + AudiencePortletKeys.AUDIENCE,
		"mvc.command.name=/segments/update_segments_entry_site_roles"
	},
	service = MVCActionCommand.class
)
public class UpdateSegmentsEntrySiteRolesMVCActionCommand
	extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		long segmentsEntryId = ParamUtil.getLong(
			actionRequest, "segmentsEntryId");

		long[] siteRoleIds = ParamUtil.getLongValues(
			actionRequest, "siteRoleIds");

		_segmentsEntryRoleLocalService.setSegmentsEntrySiteRoles(
			segmentsEntryId, siteRoleIds,
			ServiceContextFactory.getInstance(actionRequest));
	}

	@Reference
	private SegmentsEntryRoleLocalService _segmentsEntryRoleLocalService;

}