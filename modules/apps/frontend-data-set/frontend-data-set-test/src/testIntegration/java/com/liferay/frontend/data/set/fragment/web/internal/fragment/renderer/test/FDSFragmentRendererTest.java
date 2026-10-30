/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.frontend.data.set.fragment.web.internal.fragment.renderer.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.test.util.AssetTestUtil;
import com.liferay.fragment.renderer.FragmentRenderer;
import com.liferay.layout.test.util.LayoutTestUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Víctor Galán
 */
@RunWith(Arquillian.class)
public class FDSFragmentRendererTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testGetTokenValue() throws Exception {
		Group group = GroupTestUtil.addGroup();

		AssetVocabulary assetVocabulary = AssetTestUtil.addVocabulary(
			group.getGroupId());

		AssetCategory assetCategory = AssetTestUtil.addCategory(
			group.getGroupId(), assetVocabulary.getVocabularyId());

		Assert.assertEquals(
			String.valueOf(assetCategory.getCategoryId()),
			_getTokenValue(
				group,
				JSONUtil.put(
					"className", AssetCategory.class.getName()
				).put(
					"classPK", RandomTestUtil.randomLong()
				).put(
					"externalReferenceCode",
					assetCategory.getExternalReferenceCode()
				).put(
					"fieldId", "classPK"
				).put(
					"mappingMode", "content"
				)));

		long classPK = RandomTestUtil.randomLong();

		Assert.assertEquals(
			String.valueOf(classPK),
			_getTokenValue(
				group,
				JSONUtil.put(
					"className", AssetCategory.class.getName()
				).put(
					"classPK", classPK
				).put(
					"fieldId", "classPK"
				).put(
					"mappingMode", "content"
				)));
	}

	@Test
	public void testGetTokenValueWithScopeExternalReferenceCode()
		throws Exception {

		Group group1 = GroupTestUtil.addGroup();

		AssetVocabulary assetVocabulary1 = AssetTestUtil.addVocabulary(
			group1.getGroupId());

		AssetCategory assetCategory1 = AssetTestUtil.addCategory(
			group1.getGroupId(), assetVocabulary1.getVocabularyId());

		Group group2 = GroupTestUtil.addGroup();

		AssetVocabulary assetVocabulary2 = AssetTestUtil.addVocabulary(
			group2.getGroupId());

		AssetCategory assetCategory2 = AssetTestUtil.addCategory(
			assetCategory1.getExternalReferenceCode(), group2.getGroupId(),
			assetVocabulary2.getVocabularyId());

		Assert.assertEquals(
			String.valueOf(assetCategory2.getCategoryId()),
			_getTokenValue(
				group1,
				JSONUtil.put(
					"className", AssetCategory.class.getName()
				).put(
					"classPK", assetCategory1.getCategoryId()
				).put(
					"externalReferenceCode",
					assetCategory1.getExternalReferenceCode()
				).put(
					"fieldId", "classPK"
				).put(
					"mappingMode", "content"
				).put(
					"scopeExternalReferenceCode",
					group2.getExternalReferenceCode()
				)));

		Assert.assertNull(
			_getTokenValue(
				group1,
				JSONUtil.put(
					"className", AssetCategory.class.getName()
				).put(
					"classPK", assetCategory1.getCategoryId()
				).put(
					"externalReferenceCode",
					assetCategory1.getExternalReferenceCode()
				).put(
					"fieldId", "classPK"
				).put(
					"mappingMode", "content"
				).put(
					"scopeExternalReferenceCode", RandomTestUtil.randomString()
				)));
	}

	private String _getTokenValue(Group group, JSONObject mappingJSONObject)
		throws Exception {

		HttpServletRequest httpServletRequest = new MockHttpServletRequest();

		httpServletRequest.setAttribute(
			WebKeys.LAYOUT, LayoutTestUtil.addTypeContentLayout(group));

		return ReflectionTestUtil.invoke(
			_fragmentRenderer, "_getTokenValue",
			new Class<?>[] {
				JSONObject.class, HttpServletRequest.class, String.class
			},
			JSONUtil.put("token", mappingJSONObject), httpServletRequest,
			"token");
	}

	@Inject(
		filter = "component.name=com.liferay.frontend.data.set.fragment.web.internal.fragment.renderer.FDSFragmentRenderer"
	)
	private FragmentRenderer _fragmentRenderer;

}