/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import React from 'react';

import {Conjunction} from '../types';

interface Props {
	conjunction: Conjunction;
	onChange: (conjunction: Conjunction) => void;
}

export default function ConjunctionDivider({conjunction, onChange}: Props) {
	const label =
		conjunction === 'AND'
			? Liferay.Language.get('and')
			: Liferay.Language.get('or');

	const toggled: Conjunction = conjunction === 'AND' ? 'OR' : 'AND';

	return (
		<div className="audience-conjunction-divider">
			<ClayButton
				className="audience-conjunction-divider__button"
				displayType="secondary"
				onClick={() => onChange(toggled)}
				size="sm"
				title={Liferay.Language.get('click-to-toggle-and-or')}
			>
				{label.toUpperCase()}
			</ClayButton>
		</div>
	);
}
