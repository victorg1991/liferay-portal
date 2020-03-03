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

package com.liferay.fragment.internal.list.retriever;

import com.liferay.info.list.provider.DefaultInfoListProviderContext;
import com.liferay.info.list.provider.InfoListProvider;
import com.liferay.info.list.provider.InfoListProviderContext;
import com.liferay.info.list.provider.InfoListProviderTracker;
import com.liferay.info.list.provider.item.selector.criterion.InfoListProviderItemSelectorReturnType;
import com.liferay.info.pagination.Pagination;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GroupThreadLocal;
import com.liferay.portal.kernel.util.Validator;

import java.util.Collections;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Eudaldo Alonso
 */
@Component(immediate = true, service = LayoutListRetriever.class)
public class InfoListProviderLayoutListRetriever
	implements LayoutListRetriever<InfoListProviderItemSelectorReturnType> {

	public List getList(Object key) {
		String infoListProviderKey = StringPool.BLANK;

		if (key instanceof JSONObject) {
			JSONObject keyJSONObject = (JSONObject)key;

			infoListProviderKey = keyJSONObject.getString("classPK");
		}

		if (Validator.isNull(infoListProviderKey)) {
			return Collections.emptyList();
		}

		InfoListProvider infoListProvider =
			_infoListProviderTracker.getInfoListProvider(infoListProviderKey);

		Group group = _groupLocalService.fetchGroup(
			GroupThreadLocal.getGroupId());

		User user = _userLocalService.fetchUser(
			PrincipalThreadLocal.getUserId());

		InfoListProviderContext infoListProviderContext =
			new DefaultInfoListProviderContext(group, user);

		return infoListProvider.getInfoList(
			infoListProviderContext,
			Pagination.of(QueryUtil.ALL_POS, QueryUtil.ALL_POS), null);
	}

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private InfoListProviderTracker _infoListProviderTracker;

	@Reference
	private UserLocalService _userLocalService;

}