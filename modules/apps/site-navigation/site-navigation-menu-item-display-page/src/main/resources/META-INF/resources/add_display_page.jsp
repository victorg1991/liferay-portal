<%--
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
--%>

<%@ include file="/init.jsp" %>

<%
DisplayPageSiteNavigationMenuTypeDisplayContext displayPageSiteNavigationMenuTypeDisplayContext = new DisplayPageSiteNavigationMenuTypeDisplayContext(request);

Map<String, String[]> itemSelectorParameterMap = displayPageSiteNavigationMenuTypeDisplayContext.getItemSelectorParameterMap();
%>

<c:choose>
	<c:when test="<%= itemSelectorParameterMap.isEmpty() %>">
		<div class="alert alert-info">
			<%= LanguageUtil.get(resourceBundle, "selection-is-not-available") %>
		</div>
	</c:when>
	<c:otherwise>
		<c:if test="<%= itemSelectorParameterMap.size() > 1 %>">

		</c:if>

		<c:choose>
			<c:when test='<%= ParamUtil.getBoolean(request, "showGroupSelector") %>'>
				<liferay-item-selector:group-selector />
			</c:when>
			<c:otherwise>

				<%
				ItemSelectorView<ItemSelectorCriterion> itemSelectorView = displayPageSiteNavigationMenuTypeDisplayContext.getSelectedItemSelectorView();

				itemSelectorView.renderHTML(request, response, displayPageSiteNavigationMenuTypeDisplayContext.getItemSelectorCriterion(), displayPageSiteNavigationMenuTypeDisplayContext.getPortletURL(currentURLObj, liferayPortletResponse), "selectCoso", false);
				%>

			</c:otherwise>
		</c:choose>
	</c:otherwise>
</c:choose>