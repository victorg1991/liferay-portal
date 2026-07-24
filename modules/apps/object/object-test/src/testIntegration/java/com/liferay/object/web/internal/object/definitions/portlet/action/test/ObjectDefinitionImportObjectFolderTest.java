/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.web.internal.object.definitions.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.object.admin.rest.dto.v1_0.ObjectDefinition;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.model.ObjectFolder;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectFolderLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.object.web.internal.BaseExportImportTestCase;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionResponse;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Víctor Galán
 */
@FeatureFlag("LPD-17564")
@RunWith(Arquillian.class)
public class ObjectDefinitionImportObjectFolderTest
	extends BaseExportImportTestCase {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@After
	public void tearDown() throws Exception {
		_deleteObjectDefinition("TESTOBJECTDEFINITION1", "objectRelationship1");
		_deleteObjectDefinition("TESTOBJECTDEFINITION2", null);

		_deleteObjectFolder("TESTIMPORTFOLDER1");
		_deleteObjectFolder("TESTIMPORTFOLDER2");
	}

	@Test
	public void testImportBoundObjectDefinitionsPreservesObjectFolders()
		throws Exception {

		ObjectFolder objectFolder1 = _addObjectFolder("TESTIMPORTFOLDER1");
		ObjectFolder objectFolder2 = _addObjectFolder("TESTIMPORTFOLDER2");

		JSONArray jsonArray = JSONUtil.putAll(
			_toObjectDefinitionJSONObject(
				"TESTOBJECTDEFINITION1", "TestObjectDefinition1",
				objectFolder1.getExternalReferenceCode()
			).put(
				"objectRelationships",
				JSONUtil.put(
					createOneToManyObjectRelationship(
						"TESTOBJECTDEFINITION1", "TESTOBJECTDEFINITION2",
						"TestObjectDefinition2",
						ObjectDefinitionConstants.SCOPE_COMPANY,
						"objectRelationship1"))
			),
			_toObjectDefinitionJSONObject(
				"TESTOBJECTDEFINITION2", "TestObjectDefinition2",
				objectFolder2.getExternalReferenceCode()));

		importJSON(
			"TESTOBJECTDEFINITION1", jsonArray.toString(),
			"TestObjectDefinition1");

		com.liferay.object.model.ObjectDefinition objectDefinition1 =
			_fetchObjectDefinition("TESTOBJECTDEFINITION1");
		com.liferay.object.model.ObjectDefinition objectDefinition2 =
			_fetchObjectDefinition("TESTOBJECTDEFINITION2");

		Assert.assertEquals(
			objectFolder1.getObjectFolderId(),
			objectDefinition1.getObjectFolderId());
		Assert.assertEquals(
			objectFolder2.getObjectFolderId(),
			objectDefinition2.getObjectFolderId());

		Assert.assertNotNull(
			_objectRelationshipLocalService.
				fetchObjectRelationshipByObjectDefinitionId(
					objectDefinition1.getObjectDefinitionId(),
					"objectRelationship1"));
	}

	@Test
	public void testImportRemovingEdgeRelationshipFails() throws Exception {
		ObjectFolder objectFolder1 = _addObjectFolder("TESTIMPORTFOLDER1");
		ObjectFolder objectFolder2 = _addObjectFolder("TESTIMPORTFOLDER2");

		JSONArray jsonArray = JSONUtil.putAll(
			_toObjectDefinitionJSONObject(
				"TESTOBJECTDEFINITION1", "TestObjectDefinition1",
				objectFolder1.getExternalReferenceCode()
			).put(
				"objectRelationships",
				JSONUtil.put(
					createOneToManyObjectRelationship(
						"TESTOBJECTDEFINITION1", "TESTOBJECTDEFINITION2",
						"TestObjectDefinition2",
						ObjectDefinitionConstants.SCOPE_COMPANY,
						"objectRelationship1"))
			),
			_toObjectDefinitionJSONObject(
				"TESTOBJECTDEFINITION2", "TestObjectDefinition2",
				objectFolder2.getExternalReferenceCode()));

		importJSON(
			"TESTOBJECTDEFINITION1", jsonArray.toString(),
			"TestObjectDefinition1");

		MockLiferayPortletActionResponse mockLiferayPortletActionResponse =
			importJSON(
				"TESTOBJECTDEFINITION1",
				_toObjectDefinitionJSONObject(
					"TESTOBJECTDEFINITION1", "TestObjectDefinition1",
					objectFolder1.getExternalReferenceCode()
				).toString(),
				"TestObjectDefinition1");

		MockHttpServletResponse mockHttpServletResponse =
			(MockHttpServletResponse)
				mockLiferayPortletActionResponse.getHttpServletResponse();

		Assert.assertNotEquals(
			"{}", mockHttpServletResponse.getContentAsString());

		com.liferay.object.model.ObjectDefinition objectDefinition1 =
			_fetchObjectDefinition("TESTOBJECTDEFINITION1");

		Assert.assertNotNull(
			_objectRelationshipLocalService.
				fetchObjectRelationshipByObjectDefinitionId(
					objectDefinition1.getObjectDefinitionId(),
					"objectRelationship1"));
	}

	@Override
	protected ClassLoader getClassLoader() {
		return ObjectDefinitionImportObjectFolderTest.class.getClassLoader();
	}

	@Override
	protected Class<?> getClazz() {
		return getClass();
	}

	@Override
	protected long getId(String name) throws Exception {
		Page<ObjectDefinition> objectDefinitionsPage =
			objectDefinitionResource.getObjectDefinitionsPage(
				name, null, null, Pagination.of(1, 1), null);

		List<ObjectDefinition> objectDefinitions =
			(List<ObjectDefinition>)objectDefinitionsPage.getItems();

		ObjectDefinition objectDefinition = objectDefinitions.get(0);

		return objectDefinition.getId();
	}

	@Override
	protected String getIdentifierName() {
		return "objectDefinitionId";
	}

	@Override
	protected String getJSONName() {
		return "objectDefinitionJSON";
	}

	@Override
	protected MVCActionCommand getMVCActionCommand() {
		return _mvcActionCommand;
	}

	@Override
	protected MVCResourceCommand getMVCResourceCommand() {
		return _mvcResourceCommand;
	}

	private ObjectFolder _addObjectFolder(String externalReferenceCode)
		throws Exception {

		return _objectFolderLocalService.addObjectFolder(
			externalReferenceCode, user.getUserId(),
			Collections.singletonMap(
				LocaleUtil.US, RandomTestUtil.randomString()),
			RandomTestUtil.randomString());
	}

	private void _deleteObjectDefinition(
			String objectDefinitionExternalReferenceCode,
			String objectRelationshipName)
		throws Exception {

		com.liferay.object.model.ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					objectDefinitionExternalReferenceCode,
					TestPropsValues.getCompanyId());

		if (objectDefinition == null) {
			return;
		}

		if (Validator.isNull(objectRelationshipName)) {
			_objectDefinitionLocalService.deleteObjectDefinition(
				objectDefinition.getObjectDefinitionId());

			return;
		}

		ObjectRelationship objectRelationship =
			_objectRelationshipLocalService.
				fetchObjectRelationshipByObjectDefinitionId(
					objectDefinition.getObjectDefinitionId(),
					objectRelationshipName);

		if (objectRelationship != null) {
			_objectRelationshipLocalService.updateObjectRelationship(
				objectRelationship.getExternalReferenceCode(),
				objectRelationship.getObjectRelationshipId(),
				objectRelationship.getParameterObjectFieldId(),
				objectRelationship.getDeletionType(), false,
				objectRelationship.getLabelMap(), null);
		}

		_objectDefinitionLocalService.deleteObjectDefinition(
			objectDefinition.getObjectDefinitionId());
	}

	private void _deleteObjectFolder(String externalReferenceCode)
		throws Exception {

		ObjectFolder objectFolder =
			_objectFolderLocalService.fetchObjectFolderByExternalReferenceCode(
				externalReferenceCode, TestPropsValues.getCompanyId());

		if (objectFolder != null) {
			_objectFolderLocalService.deleteObjectFolder(
				objectFolder.getObjectFolderId());
		}
	}

	private com.liferay.object.model.ObjectDefinition _fetchObjectDefinition(
			String externalReferenceCode)
		throws Exception {

		return _objectDefinitionLocalService.
			fetchObjectDefinitionByExternalReferenceCode(
				externalReferenceCode, TestPropsValues.getCompanyId());
	}

	private JSONObject _toObjectDefinitionJSONObject(
			String externalReferenceCode, String name,
			String objectFolderExternalReferenceCode)
		throws Exception {

		return jsonFactory.createJSONObject(
			defaultObjectDefinitionJSON
		).put(
			"externalReferenceCode", externalReferenceCode
		).put(
			"name", name
		).put(
			"objectFolderExternalReferenceCode",
			objectFolderExternalReferenceCode
		);
	}

	@Inject(
		filter = "mvc.command.name=/object_definitions/import_object_definition"
	)
	private MVCActionCommand _mvcActionCommand;

	@Inject(
		filter = "mvc.command.name=/object_definitions/export_bound_object_definitions"
	)
	private MVCResourceCommand _mvcResourceCommand;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectFolderLocalService _objectFolderLocalService;

	@Inject
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

}