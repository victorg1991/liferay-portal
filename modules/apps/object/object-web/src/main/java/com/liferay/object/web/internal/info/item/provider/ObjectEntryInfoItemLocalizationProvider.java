/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.web.internal.info.item.provider;

import com.liferay.info.item.provider.InfoItemCategorizationProvider;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;

/**
 * @author Rubén Pulido
 */
public class ObjectEntryInfoItemLocalizationProvider
	implements InfoItemCategorizationProvider<ObjectEntry> {

	public ObjectEntryInfoItemLocalizationProvider(
		ObjectDefinition objectDefinition) {

		_objectDefinition = objectDefinition;
	}

	@Override
	public boolean supportsCategorization() {
		return _objectDefinition.isEnableLocalization();
	}

	private final ObjectDefinition _objectDefinition;

}