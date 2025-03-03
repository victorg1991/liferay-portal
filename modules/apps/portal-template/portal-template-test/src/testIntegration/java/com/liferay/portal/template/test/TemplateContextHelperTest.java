/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.template.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.template.engine.TemplateContextHelper;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Istvan Sajtos
 */
@RunWith(Arquillian.class)
public class TemplateContextHelperTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testURLtoByteArray() throws Exception {
		TemplateContextHelper templateContextHelper =
			new TemplateContextHelper();

		Map<String, Object> helperUtilities =
			templateContextHelper.getHelperUtilities(false);

		Http http = (Http)helperUtilities.get("httpUtil");

		Http.Options options = new Http.Options();

		options.setLocation("http://www.google.com");

		http.URLtoByteArray(options);

		Assert.assertFalse(options.isFollowRedirects());
	}

	@Test
	public void testURLtoInputStream() throws Exception {
		TemplateContextHelper templateContextHelper =
			new TemplateContextHelper();

		Map<String, Object> helperUtilities =
			templateContextHelper.getHelperUtilities(false);

		Http http = (Http)helperUtilities.get("httpUtil");

		Http.Options options = new Http.Options();

		options.setLocation("http://www.google.com");

		http.URLtoInputStream(options);

		Assert.assertFalse(options.isFollowRedirects());
	}

	@Test
	public void testURLtoString() throws Exception {
		TemplateContextHelper templateContextHelper =
			new TemplateContextHelper();

		Map<String, Object> helperUtilities =
			templateContextHelper.getHelperUtilities(false);

		Http http = (Http)helperUtilities.get("httpUtil");

		Http.Options options = new Http.Options();

		options.setLocation("http://www.google.com");

		http.URLtoString(options);

		Assert.assertFalse(options.isFollowRedirects());
	}

}