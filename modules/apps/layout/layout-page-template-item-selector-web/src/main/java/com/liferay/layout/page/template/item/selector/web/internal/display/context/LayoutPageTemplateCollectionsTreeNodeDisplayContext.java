package com.liferay.layout.page.template.item.selector.web.internal.display.context;

import com.liferay.layout.page.template.constants.LayoutPageTemplateCollectionTypeConstants;
import com.liferay.layout.page.template.model.LayoutPageTemplateCollection;
import com.liferay.layout.page.template.service.LayoutPageTemplateCollectionLocalServiceUtil;
import com.liferay.portal.kernel.bean.BeanParamUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

public class LayoutPageTemplateCollectionsTreeNodeDisplayContext {

	public LayoutPageTemplateCollectionsTreeNodeDisplayContext(
		HttpServletRequest httpServletRequest, ThemeDisplay themeDisplay) {
		_httpServletRequest = httpServletRequest;
		_themeDisplay = themeDisplay;
	}

	public JSONArray getLayoutPageTemplateCollectionJSONArray(long groupId, long layoutPageTemplateCollectionId) {
		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		List<LayoutPageTemplateCollection> layoutPageTemplateCollections = LayoutPageTemplateCollectionLocalServiceUtil.getLayoutPageTemplateCollections(
			groupId, layoutPageTemplateCollectionId);

		for (LayoutPageTemplateCollection layoutPageTemplateCollection : layoutPageTemplateCollections) {
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

			JSONArray childrenJSONArray = getLayoutPageTemplateCollectionJSONArray(
				groupId, layoutPageTemplateCollection.getLayoutPageTemplateCollectionId());

			if (childrenJSONArray.length() > 0) {
				jsonObject.put("children", childrenJSONArray);
			}

			jsonObject.put(
				"id", layoutPageTemplateCollection.getLayoutPageTemplateCollectionId()
			).put(
				"name", layoutPageTemplateCollection.getName()
			);

			jsonArray.put(jsonObject);
		}

		return jsonArray;
	}

	public long getParentLayoutPageTemplateCollectionId() {
		if (_parentLayoutPageTemplateCollectionId != null) {
			return _parentLayoutPageTemplateCollectionId;
		}

		_parentLayoutPageTemplateCollectionId = _layoutPageTemplateCollection.getParentLayoutPageTemplateCollectionId();

		return _parentLayoutPageTemplateCollectionId;
	}

	public long getLayoutPageTemplateCollectionId() {
		if (_LayoutPageTemplateCollectionId != null) {
			return _LayoutPageTemplateCollectionId;
		}

		_LayoutPageTemplateCollectionId = BeanParamUtil.getLong(
			getLayoutPageTemplateCollection(), _httpServletRequest, "layoutPageTemplateCollectionId",
			LayoutPageTemplateCollectionTypeConstants.BASIC);

		return _LayoutPageTemplateCollectionId;
	}

	public LayoutPageTemplateCollection getLayoutPageTemplateCollection() {
		if (_layoutPageTemplateCollection != null) {
			return _layoutPageTemplateCollection;
		}

		_layoutPageTemplateCollection = LayoutPageTemplateCollectionLocalServiceUtil.fetchLayoutPageTemplateCollection(
			ParamUtil.getLong(_httpServletRequest, "layoutPageTemplateCollectionId"));

		return _layoutPageTemplateCollection;
	}


	private Long _LayoutPageTemplateCollectionId;

	private Long _parentLayoutPageTemplateCollectionId;

	private LayoutPageTemplateCollection _layoutPageTemplateCollection;

	private final HttpServletRequest _httpServletRequest;

	private final ThemeDisplay _themeDisplay;
}
