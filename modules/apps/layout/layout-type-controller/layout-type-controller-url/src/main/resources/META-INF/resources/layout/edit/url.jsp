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

<%@ include file="/layout/init.jsp" %>

<%
String url = StringPool.BLANK;

if (selLayout != null) {
	UnicodeProperties typeSettingsProperties = selLayout.getTypeSettingsProperties();

	url = typeSettingsProperties.getProperty("url", StringPool.BLANK);
}
%>

<aui:field-wrapper cssClass="lfr-input-text-container" label="url">
	<liferay-ui:input-localized
		cssClass="lfr-input-text"
		fieldPrefix="TypeSettingsProperties"
		fieldPrefixSeparator="--"
		name="url"
		xml="<%= StringPool.BLANK %>"
	/>
</aui:field-wrapper>

<aui:script use="liferay-form">
	Liferay.componentReady('<portlet:namespace />editLayoutFm').then(() => {
		const form = Liferay.Form.get('<portlet:namespace />editLayoutFm');

		form.addRule('<portlet:namespace />url', 'required');
	});
</aui:script>