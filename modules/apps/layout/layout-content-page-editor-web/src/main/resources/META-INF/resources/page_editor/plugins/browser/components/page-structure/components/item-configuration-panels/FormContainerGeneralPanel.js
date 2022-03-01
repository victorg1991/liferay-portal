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

import React, {useCallback, useEffect} from 'react';

import {SelectField} from '../../../../../../app/components/fragment-configuration-fields/SelectField';
import {COMMON_STYLES_ROLES} from '../../../../../../app/config/constants/commonStylesRoles';
import {FREEMARKER_FRAGMENT_ENTRY_PROCESSOR} from '../../../../../../app/config/constants/freemarkerFragmentEntryProcessor';
import {
	useDispatch,
	useSelector,
	useSelectorCallback,
} from '../../../../../../app/contexts/StoreContext';
import InfoItemService from '../../../../../../app/services/InfoItemService';
import {getResponsiveConfig} from '../../../../../../app/utils/getResponsiveConfig';
import updateConfigurationValue from '../../../../../../app/utils/updateConfigurationValue';
import Collapse from '../../../../../../common/components/Collapse';
import {CommonStyles} from './CommonStyles';

export default function FormContainerGeneralPanel({item}) {
	const [availableItemTypes, setItemTypes] = React.useState([]);

	const selectedViewportSize = useSelector(
		(state) => state.selectedViewportSize
	);

	const languageId = useSelector((state) => state.languageId);

	const dispatch = useDispatch();

	const fragmentEntryLink = useSelectorCallback(
		(state) => state.fragmentEntryLinks[item.config.fragmentEntryLinkId],
		[item.config.fragmentEntryLinkId]
	);

	useEffect(() => {
		InfoItemService.getAvailableInfoItemFormProviders().then(
			(itemTypes) => {
				setItemTypes([
					{
						label: Liferay.Language.get('none'),
						value: '',
					},
					...itemTypes,
				]);
			}
		);
	}, []);

	const onValueSelect = useCallback(
		(name, value) => {
			updateConfigurationValue({
				dispatch,
				fragmentEntryLink,
				languageId,
				name,
				value,
			});
		},
		[dispatch, fragmentEntryLink, languageId]
	);

	const itemConfig = getResponsiveConfig(item.config, selectedViewportSize);

	const configurationValues =
		fragmentEntryLink.editableValues[FREEMARKER_FRAGMENT_ENTRY_PROCESSOR] ||
		{};

	return (
		<>
			<Collapse
				label={Liferay.Language.get('form-container-options')}
				open
			>
				<div className="mb-3">
					{availableItemTypes.length > 0 && (
						<SelectField
							disabled={availableItemTypes.length === 0}
							field={{
								label: Liferay.Language.get('item-type'),
								name: 'itemType',
								typeOptions: {
									validValues: availableItemTypes,
								},
							}}
							onValueSelect={onValueSelect}
							value={configurationValues.itemType}
						/>
					)}
				</div>
			</Collapse>

			<CommonStyles
				commonStylesValues={itemConfig}
				item={item}
				role={COMMON_STYLES_ROLES.general}
			/>
		</>
	);
}
