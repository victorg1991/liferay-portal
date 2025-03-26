/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.template.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayInputStream;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.template.engine.TemplateContextHelper;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.io.InputStream;

import java.net.URL;

import java.util.Map;
import java.util.Objects;

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
	public void testFollowRedirectDisabled() throws Exception {
		TemplateContextHelper templateContextHelper =
			new TemplateContextHelper();

		Map<String, Object> helperUtilities =
			templateContextHelper.getHelperUtilities(false);

		Http http = (Http)helperUtilities.get("httpUtil");

		String expectedString = "FollowRedirectDisabled";

		byte[] expectedByteArray = expectedString.getBytes();

		InputStream expectedInputStream = new UnsyncByteArrayInputStream(
			expectedByteArray);

		ReflectionTestUtil.setFieldValue(
			http, "_http",
			ProxyUtil.newProxyInstance(
				TemplateContextHelperTest.class.getClassLoader(),
				new Class<?>[] {Http.class},
				(proxy, method, args) -> {
					String methodName = method.getName();

					if (Objects.equals(methodName, "URLtoByteArray") ||
						Objects.equals(methodName, "URLtoInputStream") ||
						Objects.equals(methodName, "URLtoString")) {

						Assert.assertEquals(1, args.length);

						Assert.assertTrue(args[0] instanceof Http.Options);

						Http.Options options = (Http.Options)args[0];

						Assert.assertFalse(options.isFollowRedirects());

						if (Objects.equals(methodName, "URLtoByteArray")) {
							return expectedByteArray;
						}
						else if (Objects.equals(
									methodName, "URLtoInputStream")) {

							return expectedInputStream;
						}

						return expectedString;
					}

					return null;
				}));

		Http.Options options = new Http.Options();

		options.setLocation("http://www.google.com");

		Assert.assertSame(expectedString, http.URLtoString(options));

		Assert.assertSame(
			expectedString, http.URLtoString("http://www.google.com"));
		Assert.assertSame(
			expectedString, http.URLtoString("http://www.google.com", false));
		Assert.assertSame(
			expectedString, http.URLtoString(new URL("http://www.google.com")));

		Assert.assertSame(expectedByteArray, http.URLtoByteArray(options));
		Assert.assertSame(
			expectedByteArray, http.URLtoByteArray("http://www.google.com"));
		Assert.assertSame(
			expectedByteArray,
			http.URLtoByteArray("http://www.google.com", false));

		Assert.assertSame(expectedInputStream, http.URLtoInputStream(options));
		Assert.assertSame(
			expectedInputStream,
			http.URLtoInputStream("http://www.google.com"));
		Assert.assertSame(
			expectedInputStream,
			http.URLtoInputStream("http://www.google.com", false));
	}

}