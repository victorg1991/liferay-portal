/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayForm, {ClayInput} from '@clayui/form';
import React, {useState} from 'react';

export function ConfigurationComponent({
	onValueSelect,
}: {
	onValueSelect: (name: string, value: string) => void;
}) {
	const [nextValue1, setNextValue1] = useState('');
	const [nextValue2, setNextValue2] = useState('');

	return (
		<>
			<ClayForm.Group>
				<label>Value 1</label>

				<ClayInput
					onChange={(event) => {
						setNextValue1(event.target.value);

						onValueSelect('value1', event.target.value);
					}}
					sizing="sm"
					type="text"
					value={nextValue1 || ''}
				/>
			</ClayForm.Group>

			<ClayForm.Group>
				<label>Value 2</label>

				<ClayInput
					onChange={(event) => {
						setNextValue2(event.target.value);

						onValueSelect('value2', event.target.value);
					}}
					sizing="sm"
					type="text"
					value={nextValue2 || ''}
				/>
			</ClayForm.Group>
		</>
	);
}
