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

import ClayForm, {ClayInput} from '@clayui/form';
import PropTypes from 'prop-types';
import React, {useState} from 'react';

import {ConfigurationFieldPropTypes} from '../../../prop-types/index';

export const TextField = ({field, onValueSelect}) => {
	const [errorMessage, setErrorMessage] = useState('');

	return (
		<ClayForm.Group className={errorMessage ? 'has-error' : ''}>
			<label htmlFor={field.name}>{field.label}</label>

			<ClayInput
				defaultValue={field.defaultValue}
				id={field.name}
				onChange={event => {
					if (field.typeOptions && field.typeOptions.validation) {
						const regexp = new RegExp(
							field.typeOptions.validation.regexp,
							'g'
						);

						if (regexp.test(event.target.value)) {
							setErrorMessage('');

							onValueSelect(field.name, event.target.value);
						}
						else {
							setErrorMessage(
								field.typeOptions.validation.errorMessage ||
									Liferay.Language.get(
										'you-have-entered-invalid-data'
									)
							);
						}
					}
					else {
						onValueSelect(field.name, event.target.value);
					}
				}}
				placeholder={
					field.typeOptions ? field.typeOptions.placeholder : ''
				}
				sizing="sm"
				type="text"
			/>

			{errorMessage && (
				<ClayForm.FeedbackGroup>
					<ClayForm.FeedbackItem>
						<ClayForm.FeedbackIndicator symbol="check-circle-full" />
						{errorMessage}
					</ClayForm.FeedbackItem>
				</ClayForm.FeedbackGroup>
			)}
		</ClayForm.Group>
	);
};

TextField.propTypes = {
	field: PropTypes.shape(ConfigurationFieldPropTypes).isRequired,
	onValueSelect: PropTypes.func.isRequired,
	value: PropTypes.string
};
