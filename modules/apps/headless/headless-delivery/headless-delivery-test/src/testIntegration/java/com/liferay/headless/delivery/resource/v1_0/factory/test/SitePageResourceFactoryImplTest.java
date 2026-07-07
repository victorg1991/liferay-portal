/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.resource.v1_0.factory.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.headless.delivery.dto.v1_0.SitePage;
import com.liferay.headless.delivery.resource.v1_0.SitePageResource;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.TransactionConfig;
import com.liferay.portal.kernel.transaction.TransactionInvokerUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Victor Galan
 */
@RunWith(Arquillian.class)
public class SitePageResourceFactoryImplTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testPostSiteSitePageWithMenuDisplayFragmentPageElement()
		throws Throwable {

		Group group = GroupTestUtil.addGroup();

		SitePageResource.Builder builder = _sitePageResourceFactory.create();

		SitePageResource sitePageResource = builder.user(
			TestPropsValues.getUser()
		).build();

		SitePage sitePage = (SitePage)TransactionInvokerUtil.invoke(
			TransactionConfig.Factory.create(
				Propagation.REQUIRED, new Class<?>[] {Exception.class}),
			() -> sitePageResource.postSiteSitePage(
				group.getGroupId(),
				SitePage.toDTO(
					StringBundler.concat(
						"{\"pageDefinition\": {\"pageElement\": {",
						"\"pageElements\": [{\"definition\": {\"fragment\": ",
						"{\"key\": \"com.liferay.fragment.renderer.menu.",
						"display.internal.MenuDisplayFragmentRenderer\"}, ",
						"\"fragmentConfig\": {\"sublevels\": -1}}, \"type\": ",
						"\"Fragment\"}], \"type\": \"Root\"}}, \"title\": \"",
						RandomTestUtil.randomString(), "\"}"))));

		Assert.assertNotNull(sitePage.getId());

		_groupLocalService.deleteGroup(group);
	}

	@Inject
	private GroupLocalService _groupLocalService;

	@Inject
	private SitePageResource.Factory _sitePageResourceFactory;

}
