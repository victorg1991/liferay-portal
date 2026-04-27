/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.audience.web.internal.servlet;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.segments.criteria.Criteria;
import com.liferay.segments.criteria.CriteriaSerializer;
import com.liferay.segments.criteria.contributor.SegmentsCriteriaContributor;
import com.liferay.segments.criteria.contributor.SegmentsCriteriaContributorRegistry;
import com.liferay.segments.criteria.mapper.SegmentsCriteriaJSONObjectMapper;
import com.liferay.segments.model.SegmentsEntry;
import com.liferay.segments.service.SegmentsEntryLocalService;

import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Víctor Galán
 */
@Component(
	property = {
		"osgi.http.whiteboard.context.path=/audiences",
		"osgi.http.whiteboard.servlet.name=com.liferay.audience.web.internal.servlet.GetAudiencesServlet",
		"osgi.http.whiteboard.servlet.pattern=/audiences/*"
	},
	service = Servlet.class
)
public class GetAudiencesServlet extends HttpServlet {

	@Override
	protected void doGet(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws IOException {

		httpServletResponse.setContentType(ContentTypes.APPLICATION_JSON);
		httpServletResponse.setStatus(HttpServletResponse.SC_OK);

		JSONArray audiencesJSONArray = _jsonFactory.createJSONArray();

		long groupId = ParamUtil.getLong(httpServletRequest, "groupId");

		if (groupId <= 0) {
			_write(httpServletResponse, audiencesJSONArray);

			return;
		}

		SegmentsCriteriaContributor contextContributor =
			_getContextContributor();

		if (contextContributor == null) {
			_write(httpServletResponse, audiencesJSONArray);

			return;
		}

		try {
			List<SegmentsEntry> segmentsEntries =
				_segmentsEntryLocalService.getSegmentsEntries(
					groupId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);

			for (SegmentsEntry segmentsEntry : segmentsEntries) {
				String source = segmentsEntry.getSource();

				if ((source == null) || !source.startsWith(_AUDIENCE_SOURCE)) {
					continue;
				}

				Criteria criteria = CriteriaSerializer.deserialize(
					segmentsEntry.getCriteria());

				JSONObject criteriaJSONObject =
					_segmentsCriteriaJSONObjectMapper.toJSONObject(
						criteria, contextContributor);

				JSONObject audienceJSONObject = _toAudienceJSONObject(
					segmentsEntry, criteriaJSONObject.getJSONObject("query"));

				if (audienceJSONObject != null) {
					audiencesJSONArray.put(audienceJSONObject);
				}
			}
		}
		catch (Exception exception) {
			_log.error(exception);

			httpServletResponse.setStatus(
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

			return;
		}

		_write(httpServletResponse, audiencesJSONArray);
	}

	private SegmentsCriteriaContributor _getContextContributor() {
		for (SegmentsCriteriaContributor contributor :
				_segmentsCriteriaContributorRegistry.
					getSegmentsCriteriaContributors()) {

			if (Objects.equals(contributor.getKey(), "context")) {
				return contributor;
			}
		}

		return null;
	}

	private String _getRetentionType(String source) {
		int index = source.indexOf(':');

		if (index < 0) {
			return "SESSION";
		}

		return StringUtil.toUpperCase(source.substring(index + 1));
	}

	private JSONObject _toAudienceJSONObject(
		SegmentsEntry segmentsEntry, JSONObject queryJSONObject) {

		if (queryJSONObject == null) {
			return null;
		}

		String combinator = "AND";

		JSONArray rulesJSONArray = _jsonFactory.createJSONArray();

		if (queryJSONObject.has("items")) {
			String conjunctionName = queryJSONObject.getString(
				"conjunctionName");

			if (Validator.isNotNull(conjunctionName)) {
				combinator = StringUtil.toUpperCase(conjunctionName);
			}

			JSONArray itemsJSONArray = queryJSONObject.getJSONArray("items");

			for (int i = 0; i < itemsJSONArray.length(); i++) {
				JSONObject ruleJSONObject = _toRuleJSONObject(
					itemsJSONArray.getJSONObject(i));

				if (ruleJSONObject != null) {
					rulesJSONArray.put(ruleJSONObject);
				}
			}
		}
		else {
			JSONObject leafJSONObject = _toRuleJSONObject(queryJSONObject);

			if (leafJSONObject == null) {
				return null;
			}

			rulesJSONArray.put(leafJSONObject);
		}

		return _jsonFactory.createJSONObject(
		).put(
			"combinator", combinator
		).put(
			"id", segmentsEntry.getSegmentsEntryKey()
		).put(
			"retentionType", _getRetentionType(segmentsEntry.getSource())
		).put(
			"rules", rulesJSONArray
		);
	}

	private JSONObject _toRuleJSONObject(JSONObject jsonObject) {
		if (jsonObject == null) {
			return null;
		}

		if (jsonObject.has("items")) {
			String conjunctionName = jsonObject.getString("conjunctionName");

			JSONArray itemsJSONArray = jsonObject.getJSONArray("items");

			JSONArray rulesJSONArray = _jsonFactory.createJSONArray();

			for (int i = 0; i < itemsJSONArray.length(); i++) {
				JSONObject childJSONObject = _toRuleJSONObject(
					itemsJSONArray.getJSONObject(i));

				if (childJSONObject != null) {
					rulesJSONArray.put(childJSONObject);
				}
			}

			return _jsonFactory.createJSONObject(
			).put(
				"combinator",
				Validator.isNotNull(conjunctionName) ?
					StringUtil.toUpperCase(conjunctionName) : "AND"
			).put(
				"rules", rulesJSONArray
			);
		}

		if (!jsonObject.has("propertyName")) {
			return null;
		}

		String operatorName = jsonObject.getString("operatorName");

		return _jsonFactory.createJSONObject(
		).put(
			"attr", jsonObject.getString("propertyName")
		).put(
			"op",
			Validator.isNotNull(operatorName) ?
				StringUtil.replace(operatorName, '-', '_') : null
		).put(
			"val", jsonObject.get("value")
		);
	}

	private void _write(
			HttpServletResponse httpServletResponse, JSONArray jsonArray)
		throws IOException {

		PrintWriter printWriter = httpServletResponse.getWriter();

		printWriter.write(jsonArray.toString());
	}

	private static final String _AUDIENCE_SOURCE = "AUDIENCE";

	private static final Log _log = LogFactoryUtil.getLog(
		GetAudiencesServlet.class);

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private SegmentsCriteriaContributorRegistry
		_segmentsCriteriaContributorRegistry;

	@Reference(target = "(segments.criteria.mapper.key=odata)")
	private SegmentsCriteriaJSONObjectMapper _segmentsCriteriaJSONObjectMapper;

	@Reference
	private SegmentsEntryLocalService _segmentsEntryLocalService;

}