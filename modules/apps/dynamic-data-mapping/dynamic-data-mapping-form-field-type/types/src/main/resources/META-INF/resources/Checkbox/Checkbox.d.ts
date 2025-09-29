/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React from 'react';
import type {FieldChangeEventHandler} from '../types';
declare const Main: React.FC<IProps>;
interface IProps extends ICheckboxProps {
	errorMessage?: string;
	id?: string;
	predefinedValue?: boolean | String[];
	readOnly?: boolean;
	showAsSwitcher?: boolean;
	showMaximumRepetitionsInfo?: boolean;
	systemSettingsURL: string;
	tip?: string;
	value?: boolean;
	visible?: boolean;
}
interface ICheckboxProps {
	accessibleProps: {
		'aria-describedby'?: string;
		'aria-errormessage'?: string;
		'aria-required': boolean;
	};
	checked: boolean;
	disabled?: boolean;
	label?: string;
	name: string;
	onChange: FieldChangeEventHandler<boolean>;
	required?: boolean;
	showLabel?: boolean;
}
export {Main};
export default Main;
