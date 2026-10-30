/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.categories.admin.web.internal.info.item.provider.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.test.util.AssetTestUtil;
import com.liferay.info.item.ERCInfoItemIdentifier;
import com.liferay.info.item.InfoItemDetails;
import com.liferay.info.item.InfoItemReference;
import com.liferay.info.item.InfoItemServiceRegistry;
import com.liferay.info.item.provider.InfoItemDetailsProvider;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Víctor Galán
 */
@RunWith(Arquillian.class)
public class AssetVocabularyInfoItemDetailsProviderTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testGetInfoItemDetails() throws Exception {
		Group group = GroupTestUtil.addGroup();

		AssetVocabulary assetVocabulary = AssetTestUtil.addVocabulary(
			group.getGroupId());

		InfoItemDetailsProvider<AssetVocabulary> infoItemDetailsProvider =
			_infoItemServiceRegistry.getFirstInfoItemService(
				InfoItemDetailsProvider.class, AssetVocabulary.class.getName());

		InfoItemDetails infoItemDetails =
			infoItemDetailsProvider.getInfoItemDetails(assetVocabulary);

		Assert.assertEquals(
			AssetVocabulary.class.getName(), infoItemDetails.getClassName());
		Assert.assertEquals(
			new InfoItemReference(
				AssetVocabulary.class.getName(),
				assetVocabulary.getVocabularyId()),
			infoItemDetails.getInfoItemReference());

		infoItemDetails = infoItemDetailsProvider.getInfoItemDetails(
			group.getGroupId(), ERCInfoItemIdentifier.class, assetVocabulary);

		Assert.assertEquals(
			new InfoItemReference(
				AssetVocabulary.class.getName(),
				new ERCInfoItemIdentifier(
					assetVocabulary.getExternalReferenceCode())),
			infoItemDetails.getInfoItemReference());

		infoItemDetails = infoItemDetailsProvider.getInfoItemDetails(
			RandomTestUtil.randomLong(), ERCInfoItemIdentifier.class,
			assetVocabulary);

		Assert.assertEquals(
			new InfoItemReference(
				AssetVocabulary.class.getName(),
				new ERCInfoItemIdentifier(
					assetVocabulary.getExternalReferenceCode(),
					group.getExternalReferenceCode())),
			infoItemDetails.getInfoItemReference());
	}

	@Inject
	private InfoItemServiceRegistry _infoItemServiceRegistry;

}