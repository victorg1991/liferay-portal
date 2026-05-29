/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayIcon from '@clayui/icon';
import classNames from 'classnames';
import React, {useCallback} from 'react';
import {useDrop} from 'react-dnd';

import {useAudienceDispatch} from '../contexts/AudienceEditContext';
import {ACCEPTING_TYPES} from '../drag_and_drop/constants/acceptingTypes';
import {PropertyField} from '../types';
import {createRule} from '../utils/criteriaTree';

interface DragItem {
	payload: PropertyField | {id: string};
	type: string;
}

export default function CriteriaEmptyDropZone() {
	const dispatch = useAudienceDispatch();

	const [{isOver}, drop] = useDrop<DragItem, void, {isOver: boolean}>({
		accept: [ACCEPTING_TYPES.PROPERTY, ACCEPTING_TYPES.RULE],
		collect: (monitor) => ({isOver: monitor.isOver({shallow: true})}),
		drop(item) {
			if (item.type === ACCEPTING_TYPES.PROPERTY) {
				const property = item.payload as PropertyField;

				const newRule = createRule({
					attribute: property.name,
					entityName: property.entityName,
					type: property.type,
				});

				dispatch({payload: newRule, type: 'APPEND_NODE'});
			}
		},
	});

	const setRef = useCallback(
		(node: HTMLDivElement | null) => {
			drop(node);
		},
		[drop]
	);

	return (
		<div
			className={classNames('audience-empty-drop-zone', {
				'audience-empty-drop-zone--over': isOver,
			})}
			ref={setRef}
		>
			<ClayIcon symbol="drag" />

			<span className="ml-2">
				{Liferay.Language.get(
					'drag-criteria-from-the-sidebar-to-begin'
				)}
			</span>
		</div>
	);
}
