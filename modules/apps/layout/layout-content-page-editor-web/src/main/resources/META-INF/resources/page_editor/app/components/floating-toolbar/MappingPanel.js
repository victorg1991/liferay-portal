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

import React from 'react';

import MappingSelector from '../../../common/components/MappingSelector';
import {getEditableItemPropTypes} from '../../../prop-types/index';
import {BACKGROUND_IMAGE_FRAGMENT_ENTRY_PROCESSOR} from '../../config/constants/backgroundImageFragmentEntryProcessor';
import {EDITABLE_FRAGMENT_ENTRY_PROCESSOR} from '../../config/constants/editableFragmentEntryProcessor';
import {EDITABLE_TYPES} from '../../config/constants/editableTypes';
import selectEditableValue from '../../selectors/selectEditableValue';
import selectEditableValues from '../../selectors/selectEditableValues';
import selectSegmentsExperienceId from '../../selectors/selectSegmentsExperienceId';
import {useDispatch, useSelector} from '../../store/index';
import updateEditableValues from '../../thunks/updateEditableValues';
import isMapped from '../fragment-content/isMapped';

export function MappingPanel({item}) {
	const {editableId, editableType, fragmentEntryLinkId} = item;

	const dispatch = useDispatch();
	const segmentsExperienceId = useSelector(selectSegmentsExperienceId);

	const editableValue = useSelector(
		(state) =>
			selectEditableValue(
				state,
				fragmentEntryLinkId,
				editableId,
				processoryKey
			),
		[fragmentEntryLinkId, editableId, processoryKey]
	);

	const editableValues = useSelector(
		(state) => selectEditableValues(state, fragmentEntryLinkId),
		[fragmentEntryLinkId]
	);

	const processoryKey =
		editableType === EDITABLE_TYPES.backgroundImage
			? BACKGROUND_IMAGE_FRAGMENT_ENTRY_PROCESSOR
			: EDITABLE_FRAGMENT_ENTRY_PROCESSOR;

	const updateEditableValue = (newEditableValue) => {
		const nextEditableValues = {
			...editableValues,
			[processoryKey]: {
				...editableValues[processoryKey],
				[editableId]: {
					config: isMapped(newEditableValue)
						? {...editableValue.config, alt: ''}
						: editableValue.config,
					defaultValue: editableValue.defaultValue,
					...newEditableValue,
				},
			},
		};

		dispatch(
			updateEditableValues({
				editableValues: nextEditableValues,
				fragmentEntryLinkId,
				segmentsExperienceId,
			})
		);
	};

	return (
		<MappingSelector
			fieldType={editableType}
			mappedItem={editableValue}
			onMappingSelect={updateEditableValue}
		/>
	);
}

MappingPanel.propTypes = {
	item: getEditableItemPropTypes(),
};
