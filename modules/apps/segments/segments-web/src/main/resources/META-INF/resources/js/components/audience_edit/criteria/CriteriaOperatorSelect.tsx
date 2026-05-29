/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClaySelectWithOption} from '@clayui/form';
import React from 'react';

import {PropertyField} from '../types';

const STRING_OPERATIONS = [
	{label: Liferay.Language.get('equals'), value: 'eq'},
	{label: Liferay.Language.get('not-equals'), value: 'ne'},
	{label: Liferay.Language.get('contains'), value: 'contains'},
	{label: Liferay.Language.get('does-not-contain'), value: 'not_contains'},
];

const NUMBER_OPERATIONS = [
	{label: Liferay.Language.get('is-equal-to'), value: 'eq'},
	{label: Liferay.Language.get('is-not-equal-to'), value: 'ne'},
	{label: Liferay.Language.get('is-greater-than'), value: 'gt'},
	{label: Liferay.Language.get('is-less-than'), value: 'lt'},
];

const DATE_OPERATIONS = [
	{label: Liferay.Language.get('is-after'), value: 'gt'},
	{label: Liferay.Language.get('is-before'), value: 'lt'},
];

const BOOLEAN_OPERATIONS = [
	{label: Liferay.Language.get('is'), value: 'eq'},
	{label: Liferay.Language.get('is-not'), value: 'ne'},
];

function getOperations(type: string | undefined) {
	switch (type) {
		case 'integer':
		case 'double':
			return NUMBER_OPERATIONS;

		case 'date':
			return DATE_OPERATIONS;

		case 'boolean':
			return BOOLEAN_OPERATIONS;

		default:
			return STRING_OPERATIONS;
	}
}

interface Props {
	onChange: (operation: string) => void;
	property: PropertyField | undefined;
	value: string;
}

export default function CriteriaOperatorSelect({
	onChange,
	property,
	value,
}: Props) {
	return (
		<ClaySelectWithOption
			aria-label={Liferay.Language.get('operator')}
			className="audience-row__operator"
			onChange={(event) => onChange(event.target.value)}
			options={getOperations(property?.type)}
			value={value}
		/>
	);
}
