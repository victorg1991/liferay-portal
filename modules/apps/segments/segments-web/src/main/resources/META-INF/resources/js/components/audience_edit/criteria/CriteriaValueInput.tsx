/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClayInput, ClaySelectWithOption} from '@clayui/form';
import React from 'react';

import {AudienceRule, PropertyField} from '../types';

interface Props {
	onChange: (value: AudienceRule['value']) => void;
	property: PropertyField | undefined;
	value: AudienceRule['value'];
}

export default function CriteriaValueInput({onChange, property, value}: Props) {
	if (!property) {
		return (
			<ClayInput
				aria-label={Liferay.Language.get('value')}
				onChange={(event) => onChange(event.target.value)}
				type="text"
				value={String(value ?? '')}
			/>
		);
	}

	if (property.options && !!property.options.length) {
		return (
			<ClaySelectWithOption
				aria-label={property.label}
				onChange={(event) => onChange(event.target.value)}
				options={property.options}
				value={String(value ?? '')}
			/>
		);
	}

	if (property.type === 'boolean') {
		return (
			<ClaySelectWithOption
				aria-label={property.label}
				onChange={(event) => onChange(event.target.value === 'true')}
				options={[
					{label: Liferay.Language.get('true'), value: 'true'},
					{label: Liferay.Language.get('false'), value: 'false'},
				]}
				value={String(value)}
			/>
		);
	}

	if (property.type === 'integer' || property.type === 'double') {
		return (
			<ClayInput
				aria-label={property.label}
				onChange={(event) => onChange(Number(event.target.value))}
				step={property.type === 'integer' ? 1 : 0.01}
				type="number"
				value={String(value ?? 0)}
			/>
		);
	}

	return (
		<ClayInput
			aria-label={property.label}
			onChange={(event) => onChange(event.target.value)}
			type={property.type === 'date' ? 'datetime-local' : 'text'}
			value={String(value ?? '')}
		/>
	);
}
