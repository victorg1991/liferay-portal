/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.internal.upgrade.v5_0_0;

import com.liferay.portal.dao.orm.common.SQLTransformer;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.db.IndexMetadata;
import com.liferay.portal.kernel.dao.db.IndexMetadataFactoryUtil;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentEntryUpgradeProcess extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		_updateDraftIdentifiers();
		_updateIndexes();
	}

	private void _updateDraftIdentifiers() throws Exception {
		Map<Long, String> identifiers = new HashMap<>();

		try (PreparedStatement selectPreparedStatement =
				connection.prepareStatement(
					SQLTransformer.transform(
						"select fragmentEntryId, ctCollectionId from " +
							"FragmentEntry where head = [$FALSE$] and headId " +
								"!= fragmentEntryId"));
			ResultSet resultSet = selectPreparedStatement.executeQuery();
			PreparedStatement updatePreparedStatement =
				AutoBatchPreparedStatementUtil.autoBatch(
					connection,
					"update FragmentEntry set uuid_ = ?, " +
						"externalReferenceCode = ? where fragmentEntryId = ? " +
							"and ctCollectionId = ?")) {

			while (resultSet.next()) {
				long fragmentEntryId = resultSet.getLong("fragmentEntryId");

				String identifier = identifiers.computeIfAbsent(
					fragmentEntryId, key -> PortalUUIDUtil.generate());

				updatePreparedStatement.setString(1, identifier);
				updatePreparedStatement.setString(2, identifier);
				updatePreparedStatement.setLong(3, fragmentEntryId);
				updatePreparedStatement.setLong(
					4, resultSet.getLong("ctCollectionId"));

				updatePreparedStatement.addBatch();
			}

			updatePreparedStatement.executeBatch();
		}
	}

	private void _updateIndexes() throws Exception {
		DB db = DBManagerUtil.getDB();

		List<IndexMetadata> dropIndexMetadatas = new ArrayList<>();

		for (String sql : _OLD_INDEXES) {
			IndexMetadata indexMetadata =
				IndexMetadataFactoryUtil.createIndexMetadata(
					sql.replaceAll("\\[\\$COLUMN_LENGTH:\\d+\\$\\]", ""));

			if (hasIndex("FragmentEntry", indexMetadata.getIndexName())) {
				dropIndexMetadatas.add(indexMetadata);
			}
		}

		for (IndexMetadata indexMetadata : dropIndexMetadatas) {
			runSQL(indexMetadata.getDropSQL());
		}

		List<IndexMetadata> addIndexMetadatas = new ArrayList<>();

		for (String sql : _NEW_INDEXES) {
			IndexMetadata indexMetadata =
				IndexMetadataFactoryUtil.createIndexMetadata(
					sql.replaceAll("\\[\\$COLUMN_LENGTH:\\d+\\$\\]", ""));

			if (!hasIndex("FragmentEntry", indexMetadata.getIndexName())) {
				addIndexMetadatas.add(indexMetadata);
			}
		}

		db.addIndexes(connection, addIndexMetadatas);
	}

	private static final String[] _NEW_INDEXES = {
		"create index IX_2095DA5 on FragmentEntry (fragmentCollectionId)",
		"create unique index IX_3B4DA4D9 on FragmentEntry (groupId, externalReferenceCode[$COLUMN_LENGTH:75$], ctCollectionId)",
		"create unique index IX_F5386A5 on FragmentEntry (groupId, fragmentEntryKey[$COLUMN_LENGTH:75$], head, ctCollectionId)",
		"create unique index IX_AE3E4F50 on FragmentEntry (groupId, uuid_[$COLUMN_LENGTH:75$], ctCollectionId)"
	};

	private static final String[] _OLD_INDEXES = {
		"create index IX_DFBAF515 on FragmentEntry (externalReferenceCode[$COLUMN_LENGTH:75$])",
		"create index IX_ADC3EFB9 on FragmentEntry (fragmentCollectionId, head)",
		"create index IX_8B622592 on FragmentEntry (groupId, fragmentCollectionId, head, name[$COLUMN_LENGTH:75$])",
		"create index IX_24159CF8 on FragmentEntry (groupId, fragmentCollectionId, head, status, name[$COLUMN_LENGTH:75$])",
		"create index IX_C522C3BC on FragmentEntry (groupId, fragmentCollectionId, head, status, type_)",
		"create index IX_47674E62 on FragmentEntry (groupId, fragmentCollectionId, head, type_)",
		"create index IX_7F3F0EB3 on FragmentEntry (groupId, fragmentEntryKey[$COLUMN_LENGTH:75$])",
		"create unique index IX_3EEFF605 on FragmentEntry (groupId, head, externalReferenceCode[$COLUMN_LENGTH:75$], ctCollectionId)",
		"create unique index IX_1676C265 on FragmentEntry (groupId, head, fragmentEntryKey[$COLUMN_LENGTH:75$], ctCollectionId)",
		"create unique index IX_A420787C on FragmentEntry (groupId, head, uuid_[$COLUMN_LENGTH:75$], ctCollectionId)"
	};

}
