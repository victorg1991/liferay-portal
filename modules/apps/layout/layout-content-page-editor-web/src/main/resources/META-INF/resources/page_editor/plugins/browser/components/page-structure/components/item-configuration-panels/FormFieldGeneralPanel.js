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

import ClayAlert from '@clayui/alert';
import ClayForm, {ClaySelect} from '@clayui/form';
import classNames from 'classnames';
import React, {useCallback, useEffect, useMemo, useState} from 'react';

import {TextField} from '../../../../../../app/components/fragment-configuration-fields/TextField';
import {COMMON_STYLES_ROLES} from '../../../../../../app/config/constants/commonStylesRoles';
import {FORM_CONTAINER_FRAGMENT_KEY} from '../../../../../../app/config/constants/formContainerFragmentKey';
import {FREEMARKER_FRAGMENT_ENTRY_PROCESSOR} from '../../../../../../app/config/constants/freemarkerFragmentEntryProcessor';
import {
	useDispatch,
	useSelector,
} from '../../../../../../app/contexts/StoreContext';
import InfoItemService from '../../../../../../app/services/InfoItemService';
import {getResponsiveConfig} from '../../../../../../app/utils/getResponsiveConfig';
import updateConfigurationValue from '../../../../../../app/utils/updateConfigurationValue';
import Collapse from '../../../../../../common/components/Collapse';
import {CommonStyles} from './CommonStyles';

function loadMappingFields(classNameId) {
	return InfoItemService.getAvailableStructureMappingFields({
		classNameId,
		classTypeId: 0,
		onNetworkStatus: () => {},
	}).then((response) => {
		if (Array.isArray(response)) {
			return response;
		}

		return [];
	});
}

function findFormContainerFragment(layoutData, fragmentEntryLinks, item) {
	const findFormContainer = (itemId) => {
		const item = layoutData.items[itemId];

		if (!item) {
			return null;
		}

		const fragmentEntryLink =
			fragmentEntryLinks[item.config.fragmentEntryLinkId];

		if (
			!fragmentEntryLink ||
			fragmentEntryLink.fragmentEntryKey !== FORM_CONTAINER_FRAGMENT_KEY
		) {
			return findFormContainer(item.parentId);
		}
		else {
			return fragmentEntryLink;
		}
	};

	return findFormContainer(item.parentId);
}

const UNMAPPED_OPTION = {
	label: `-- ${Liferay.Language.get('unmapped')} --`,
	value: 'unmapped',
};

function MappingFieldSelect({fields, onValueSelect, value}) {
	return (
		<ClayForm.Group className={classNames('mt-3')} small>
			<label htmlFor="mappingSelectorFieldSelect">
				{Liferay.Language.get('field')}
			</label>

			<ClaySelect
				aria-label={Liferay.Language.get('field')}
				disabled={!(fields && !!fields.length)}
				id="mappingSelectorFieldSelectId"
				onChange={onValueSelect}
				value={value}
			>
				{fields && !!fields.length && (
					<>
						<ClaySelect.Option
							label={UNMAPPED_OPTION.label}
							value={UNMAPPED_OPTION.value}
						/>

						{fields.map((fieldSet, index) => {
							const key = `${fieldSet.label || ''}${index}`;

							const Wrapper = ({children, ...props}) =>
								fieldSet.label ? (
									<ClaySelect.OptGroup {...props}>
										{children}
									</ClaySelect.OptGroup>
								) : (
									<React.Fragment key={key}>
										{children}
									</React.Fragment>
								);

							return (
								<Wrapper key={key} label={fieldSet.label}>
									{fieldSet.fields.map((field) => (
										<ClaySelect.Option
											key={field.key}
											label={field.label}
											value={field.key}
										/>
									))}
								</Wrapper>
							);
						})}
					</>
				)}
			</ClaySelect>
		</ClayForm.Group>
	);
}

export function FormFieldGeneralPanel({item}) {
	const selectedViewportSize = useSelector(
		(state) => state.selectedViewportSize
	);

	const languageId = useSelector((state) => state.languageId);

	const dispatch = useDispatch();

	const fragmentEntryLinks = useSelector((state) => state.fragmentEntryLinks);

	const fragmentEntryLink =
		fragmentEntryLinks[item.config.fragmentEntryLinkId];

	const layoutData = useSelector((state) => state.layoutData);

	const formContainerFragment = useMemo(
		() => findFormContainerFragment(layoutData, fragmentEntryLinks, item),
		[fragmentEntryLinks, layoutData, item]
	);

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
	const [fields, setFields] = useState([]);

	const itemConfig = getResponsiveConfig(item.config, selectedViewportSize);

	const formContainerFragmentConfiguration =
		formContainerFragment?.editableValues[
			FREEMARKER_FRAGMENT_ENTRY_PROCESSOR
		] || {};

	const formFieldFragmentConfiguration =
		fragmentEntryLink?.editableValues[
			FREEMARKER_FRAGMENT_ENTRY_PROCESSOR
		] || {};

	useEffect(() => {
		if (
			formContainerFragment &&
			formContainerFragmentConfiguration.classNameId &&
			fields.length === 0
		) {
			onValueSelect(
				'classNameId',
				formContainerFragmentConfiguration.classNameId
			);

			loadMappingFields(
				formContainerFragmentConfiguration.classNameId
			).then((response) => {
				setFields(response);
			});
		}
	}, [
		formContainerFragment,
		formContainerFragmentConfiguration.classNameId,
		onValueSelect,
		fields.length,
	]);

	if (
		!formContainerFragment ||
		!formContainerFragmentConfiguration.className
	) {
		return (
			<ClayAlert
				className="m-3"
				displayType="info"
				title={Liferay.Language.get('info')}
			>
				This fragment is not inside a form container
			</ClayAlert>
		);
	}

	return (
		<>
			<Collapse
				label={Liferay.Language.get('form-container-options')}
				open
			>
				<div className="mb-3">
					{formContainerFragmentConfiguration.className}
				</div>

				<TextField
					field={{label: 'label', name: 'label'}}
					onValueSelect={onValueSelect}
					value={formFieldFragmentConfiguration.label}
				/>

				<TextField
					field={{label: 'placeholder', name: 'placeholder'}}
					onValueSelect={onValueSelect}
					value={formFieldFragmentConfiguration.placeholder}
				/>

				<MappingFieldSelect
					fields={fields}
					onValueSelect={(event) =>
						onValueSelect('field', event.target.value)
					}
					value={formFieldFragmentConfiguration.field}
				/>
			</Collapse>

			<CommonStyles
				commonStylesValues={itemConfig}
				item={item}
				role={COMMON_STYLES_ROLES.general}
			/>
		</>
	);
}
