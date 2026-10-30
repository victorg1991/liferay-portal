/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.categories.admin.web.internal.info.item.provider.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.test.util.AssetTestUtil;
import com.liferay.info.exception.NoSuchInfoItemException;
import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.ERCInfoItemIdentifier;
import com.liferay.info.item.InfoItemServiceRegistry;
import com.liferay.info.item.provider.InfoItemObjectProvider;
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
public class AssetVocabularyInfoItemObjectProviderTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testGetInfoItem() throws Exception {
		Group group = GroupTestUtil.addGroup();

		AssetVocabulary assetVocabulary = AssetTestUtil.addVocabulary(
			group.getGroupId());

		InfoItemObjectProvider<AssetVocabulary> infoItemObjectProvider =
			_infoItemServiceRegistry.getFirstInfoItemService(
				InfoItemObjectProvider.class, AssetVocabulary.class.getName());

		Assert.assertEquals(
			assetVocabulary,
			infoItemObjectProvider.getInfoItem(
				group.getGroupId(),
				new ClassPKInfoItemIdentifier(
					assetVocabulary.getVocabularyId())));
		Assert.assertEquals(
			assetVocabulary,
			infoItemObjectProvider.getInfoItem(
				group.getGroupId(),
				new ERCInfoItemIdentifier(
					assetVocabulary.getExternalReferenceCode())));
		Assert.assertEquals(
			assetVocabulary,
			infoItemObjectProvider.getInfoItem(
				RandomTestUtil.randomLong(),
				new ERCInfoItemIdentifier(
					assetVocabulary.getExternalReferenceCode(),
					group.getExternalReferenceCode())));
	}

	@Test(expected = NoSuchInfoItemException.class)
	public void testGetInfoItemWithMissingExternalReferenceCode()
		throws Exception {

		Group group = GroupTestUtil.addGroup();

		InfoItemObjectProvider<AssetVocabulary> infoItemObjectProvider =
			_infoItemServiceRegistry.getFirstInfoItemService(
				InfoItemObjectProvider.class, AssetVocabulary.class.getName());

		infoItemObjectProvider.getInfoItem(
			group.getGroupId(),
			new ERCInfoItemIdentifier(RandomTestUtil.randomString()));
	}

	@Inject
	private InfoItemServiceRegistry _infoItemServiceRegistry;

}