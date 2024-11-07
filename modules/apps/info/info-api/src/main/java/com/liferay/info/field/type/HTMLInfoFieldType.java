/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.info.field.type;

/**
 * @author Pablo Molina
 */
public class HTMLInfoFieldType implements InfoFieldType {

	public static final HTMLInfoFieldType INSTANCE = new HTMLInfoFieldType();

	public static final Attribute<LongTextInfoFieldType, Boolean> LOCALIZABLE =
		new Attribute<>();

	@Override
	public String getName() {
		return "html";
	}

	private HTMLInfoFieldType() {
	}

}