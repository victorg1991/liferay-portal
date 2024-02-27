<%@ page import="com.liferay.portal.kernel.util.HashMapBuilder" %>
<%@ page
	import="com.liferay.layout.page.template.item.selector.web.internal.display.context.LayoutPageTemplateCollectionsTreeNodeDisplayContext" %><%--
/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
	LayoutPageTemplateCollectionsTreeNodeDisplayContext
		layoutPageTemplateCollectionsTreeNodeDisplayContext = new LayoutPageTemplateCollectionsTreeNodeDisplayContext(httpServletRequest, themeDisplay);
%>

<react:component
	module="{SelectLayoutPageTemplateCollection} from layout-page-template-item-selector-web"
	props='<%=
		HashMapBuilder.<String, Object>put(
			"layoutPageTemplateCollections", layoutPageTemplateCollectionsTreeNodeDisplayContext.getLayoutPageTemplateCollectionJSONArray(themeDisplay.getScopeGroupId(), 0)
		).build()
	%>'
/>