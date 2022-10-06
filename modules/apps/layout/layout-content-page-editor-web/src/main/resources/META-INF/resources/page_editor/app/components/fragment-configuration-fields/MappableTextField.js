/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import ClayForm, {ClaySelectWithOption} from '@clayui/form';
import PropTypes from 'prop-types';
import React, {useEffect, useState} from 'react';

import MappingSelectorWrapper from '../../../common/components/MappingSelector';
import {useId} from '../../../core/hooks/useId';
import {ConfigurationFieldPropTypes} from '../../../prop-types/index';
import isMapped from '../../utils/editable-value/isMapped';
import {TextField} from './TextField';

const SOURCE_OPTION_DIRECT = 'direct';
const SOURCE_OPTION_MAPPING = 'mapping';

const SOURCE_OPTIONS = [
	{
		label: Liferay.Language.get('direct'),
		value: SOURCE_OPTION_DIRECT,
	},
	{
		label: Liferay.Language.get('mapping'),
		value: SOURCE_OPTION_MAPPING,
	},
];

export default function MappableTextField({field, onValueSelect, value}) {
	const [source, setSource] = useState(SOURCE_OPTION_DIRECT);

	useEffect(() => {
		if (isMapped(value)) {
			setSource(SOURCE_OPTION_MAPPING);
		}
		else if (value.url) {
			setSource(SOURCE_OPTION_DIRECT);
		}
	}, [value]);

	const sourceInputId = useId();

	const handleSourceChange = (event) => {
		onValueSelect(field.name, {});
		setSource(event.target.value);
	};

	return (
		<>
			<ClayForm.Group small>
				<label htmlFor={sourceInputId}>{field.label}</label>

				<ClaySelectWithOption
					id={sourceInputId}
					onChange={handleSourceChange}
					options={SOURCE_OPTIONS}
					value={source}
				/>
			</ClayForm.Group>
			{source === SOURCE_OPTION_DIRECT && (
				<TextField
					field={field}
					onValueSelect={(value) =>
						onValueSelect(field.name, {url: value})
					}
					value={value}
				/>
			)}
			{source === SOURCE_OPTION_MAPPING && (
				<MappingSelectorWrapper
					fieldType="text"
					mappedItem={value}
					onMappingSelect={(value) =>
						onValueSelect(field.name, value)
					}
				/>
			)}
		</>
	);
}

MappableTextField.propTypes = {
	field: PropTypes.shape(ConfigurationFieldPropTypes).isRequired,
	onValueSelect: PropTypes.func.isRequired,
	value: PropTypes.oneOfType([
		PropTypes.shape({
			classNameId: PropTypes.string,
			classPK: PropTypes.string,
			fieldId: PropTypes.string,
		}),

		PropTypes.shape({
			href: PropTypes.string,
		}),
	]),
};
