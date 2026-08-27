/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.staticsite.internal;

import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.exportimport.staticsite.StaticSiteExporter;
import com.liferay.layout.staticsite.export.StaticSiteBuilder;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.zip.ZipWriter;
import com.liferay.portal.kernel.zip.ZipWriterFactory;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Answers the export framework's request for a static site with the archive the
 * layout modules build, using the page selection the export process resolved.
 *
 * @author Víctor Galán
 */
@Component(service = StaticSiteExporter.class)
public class StaticSiteExporterImpl implements StaticSiteExporter {

	@Override
	public File export(PortletDataContext portletDataContext) throws Exception {
		Group group = _groupLocalService.getGroup(
			portletDataContext.getGroupId());

		ZipWriter zipWriter = _zipWriterFactory.getZipWriter();

		_staticSiteBuilder.build(
			group.getGroupId(),
			_portal.getSiteDefaultLocale(group.getGroupId()),
			_getPlids(
				group.getGroupId(), portletDataContext.isPrivateLayout(),
				portletDataContext.getLayoutIds()),
			zipWriter::addEntry);

		return zipWriter.getFile();
	}

	/**
	 * Returns the pages the export process selected, as the identifiers the
	 * builder takes. An empty selection is the process saying every page.
	 */
	private long[] _getPlids(
		long groupId, boolean privateLayout, long[] layoutIds) {

		List<Long> plids = new ArrayList<>();

		for (long layoutId : layoutIds) {
			Layout layout = _layoutLocalService.fetchLayout(
				groupId, privateLayout, layoutId);

			if (layout != null) {
				plids.add(layout.getPlid());
			}
		}

		long[] selectedPlids = new long[plids.size()];

		for (int i = 0; i < selectedPlids.length; i++) {
			selectedPlids[i] = plids.get(i);
		}

		return selectedPlids;
	}

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private Portal _portal;

	@Reference
	private StaticSiteBuilder _staticSiteBuilder;

	@Reference
	private ZipWriterFactory _zipWriterFactory;

}