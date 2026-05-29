/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayIcon from '@clayui/icon';
import classNames from 'classnames';
import React from 'react';

import useDragSource from '../../../hooks/useDragSource';
import {ACCEPTING_TYPES} from '../drag_and_drop/constants/acceptingTypes';
import {PropertyField} from '../types';

interface Props {
	property: PropertyField;
}

const TYPE_ICONS: Record<string, string> = {
	boolean: 'asterisk',
	date: 'calendar',
	double: 'hashtag',
	id: 'user',
	integer: 'hashtag',
	string: 'text',
};

export default function AudienceSidebarItem({property}: Props) {
	const {handlerRef, isDragging} = useDragSource({
		item: {
			payload: property,
			type: ACCEPTING_TYPES.PROPERTY,
		},
	});

	return (
		<div
			className={classNames(
				'audience-sidebar-item align-items-center d-flex',
				{
					'audience-sidebar-item--dragging': isDragging,
				}
			)}
			ref={handlerRef}
		>
			<ClayIcon
				className="audience-sidebar-item__grip text-secondary"
				symbol="drag"
			/>

			<span
				className={classNames(
					'audience-sidebar-item__type-icon',
					`audience-sidebar-item__type-icon--${property.type}`
				)}
			>
				<ClayIcon
					symbol={
						property.icon ?? TYPE_ICONS[property.type] ?? 'text'
					}
				/>
			</span>

			<span className="audience-sidebar-item__label">
				{property.label}
			</span>
		</div>
	);
}
