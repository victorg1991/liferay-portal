/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React, {useEffect, useState} from 'react';

export function JSModuleField({field, onValueSelect, value}) {
	const {module} = field.typeOptions || {};

	const [Component, setComponent] = useState(null);

	console.log(React);

	useEffect(() => {
		import(module).then((module) => {
			setComponent(() => module.ConfigurationComponent);
		});
	}, [module]);

	if (!Component) {
		return null;
	}

	return (
		<Component field={field} onValueSelect={onValueSelect} value={value} />
	);
}
