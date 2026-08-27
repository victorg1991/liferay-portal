<%--
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
ExportStaticSiteDisplayContext exportStaticSiteDisplayContext = (ExportStaticSiteDisplayContext)request.getAttribute(ExportStaticSiteDisplayContext.class.getName());
%>

<liferay-frontend:component
	componentId='<%= liferayPortletResponse.getNamespace() + "ExportStaticSite" %>'
	context="<%= exportStaticSiteDisplayContext.getContext() %>"
	module="{ExportStaticSite} from layout-admin-web"
	servletContext="<%= application %>"
/>