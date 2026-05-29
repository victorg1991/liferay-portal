/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClayButtonWithIcon} from '@clayui/button';
import {useDragAndDrop} from '@liferay/layout-js-components-web';
import React, {useCallback, useEffect, useRef, useState} from 'react';
import {useDrop} from 'react-dnd';

import {useAudienceDispatch} from '../contexts/AudienceEditContext';
import {ACCEPTING_TYPES} from '../drag_and_drop/constants/acceptingTypes';
import {AudienceRule, PropertyField} from '../types';
import {createRule} from '../utils/criteriaTree';
import CriteriaOperatorSelect from './CriteriaOperatorSelect';
import CriteriaValueInput from './CriteriaValueInput';

interface DnDItem {
	id: string;
	name: string;
}

interface PropertyDragItem {
	payload: PropertyField;
	type: string;
}

type DropPosition = 'bottom' | 'middle' | 'top' | null;

interface Props {
	index: number;
	items: DnDItem[];
	onReorder: (
		items: DnDItem[],
		targetId: string,
		position: DropPosition,
		sourceId: string
	) => void;
	property: PropertyField | undefined;
	rule: AudienceRule;
}

const MIDDLE_TOP_RATIO = 0.3;
const MIDDLE_BOTTOM_RATIO = 0.7;

function getRelativePosition(
	element: HTMLElement,
	clientY: number
): DropPosition {
	const rect = element.getBoundingClientRect();
	const offset = clientY - rect.top;

	if (offset < rect.height * MIDDLE_TOP_RATIO) {
		return 'top';
	}

	if (offset > rect.height * MIDDLE_BOTTOM_RATIO) {
		return 'bottom';
	}

	return 'middle';
}

export default function CriteriaRow({
	index,
	items,
	onReorder,
	property,
	rule,
}: Props) {
	const dispatch = useAudienceDispatch();

	const dragHandlerRef = useRef<HTMLButtonElement>(null);
	const dropItemRef = useRef<HTMLDivElement | null>(null);

	const [propertyDropPosition, setPropertyDropPosition] =
		useState<DropPosition>(null);

	const {
		handleKeyboardDragAndDrop,
		isDropBottomPosition,
		isDropMiddlePosition,
		isDropTopPosition,
	} = useDragAndDrop<DnDItem>({
		allowMiddleDrop: true,
		dragHandlerRef,
		dropItemRef,
		item: items[index],
		itemIndex: index,
		items,
		onDrop: onReorder,
	});

	const [{isPropertyOver}, propertyDrop] = useDrop<
		PropertyDragItem,
		void,
		{isPropertyOver: boolean}
	>({
		accept: ACCEPTING_TYPES.PROPERTY,
		collect: (monitor) => ({
			isPropertyOver: monitor.isOver({shallow: true}),
		}),
		drop(item, monitor) {
			if (!dropItemRef.current) {
				return;
			}

			const clientOffset = monitor.getClientOffset();

			if (!clientOffset) {
				return;
			}

			const position = getRelativePosition(
				dropItemRef.current,
				clientOffset.y
			);

			const newRule = createRule({
				attribute: item.payload.name,
				entityName: item.payload.entityName,
				type: item.payload.type,
			});

			if (position === 'middle') {
				dispatch({payload: newRule, type: 'APPEND_NODE'});
				dispatch({
					payload: {sourceId: newRule.id, targetId: rule.id},
					type: 'MERGE_NODES',
				});
			}
			else {
				dispatch({
					payload: {
						newNode: newRule,
						position: position === 'top' ? 'before' : 'after',
						targetId: rule.id,
					},
					type: 'INSERT_SIBLING',
				});
			}

			setPropertyDropPosition(null);
		},
		hover(_item, monitor) {
			if (!dropItemRef.current || !monitor.isOver({shallow: true})) {
				setPropertyDropPosition(null);

				return;
			}

			const clientOffset = monitor.getClientOffset();

			if (!clientOffset) {
				return;
			}

			setPropertyDropPosition(
				getRelativePosition(dropItemRef.current, clientOffset.y)
			);
		},
	});

	const setRef = useCallback(
		(node: HTMLDivElement | null) => {
			dropItemRef.current = node;
			propertyDrop(node);
		},
		[propertyDrop]
	);

	if (!isPropertyOver && propertyDropPosition !== null) {
		setPropertyDropPosition(null);
	}

	const effectiveMiddle =
		isDropMiddlePosition || propertyDropPosition === 'middle';
	const effectiveTop = isDropTopPosition || propertyDropPosition === 'top';
	const effectiveBottom =
		isDropBottomPosition || propertyDropPosition === 'bottom';

	useEffect(() => {
		const wrapper =
			dropItemRef.current?.parentElement?.parentElement ?? null;

		if (!wrapper) {
			return;
		}

		wrapper.classList.toggle(
			'audience-row-wrapper--drop-bottom',
			effectiveBottom
		);
		wrapper.classList.toggle(
			'audience-row-wrapper--drop-middle',
			effectiveMiddle
		);
		wrapper.classList.toggle(
			'audience-row-wrapper--drop-top',
			effectiveTop
		);

		return () => {
			wrapper.classList.remove('audience-row-wrapper--drop-bottom');
			wrapper.classList.remove('audience-row-wrapper--drop-middle');
			wrapper.classList.remove('audience-row-wrapper--drop-top');
		};
	}, [effectiveBottom, effectiveMiddle, effectiveTop]);

	return (
		<div className="audience-row" ref={setRef}>
			<ClayButtonWithIcon
				aria-label={Liferay.Language.get('drag')}
				borderless
				className="audience-row__grip"
				displayType="secondary"
				onKeyDown={handleKeyboardDragAndDrop}
				ref={dragHandlerRef}
				size="sm"
				symbol="drag"
				title={Liferay.Language.get('drag')}
			/>

			<div className="audience-row__attribute">
				{property?.label ?? rule.attribute}
			</div>

			<CriteriaOperatorSelect
				onChange={(operation) =>
					dispatch({
						payload: {
							partial: {operation},
							ruleId: rule.id,
						},
						type: 'UPDATE_RULE',
					})
				}
				property={property}
				value={rule.operation}
			/>

			<div className="audience-row__value">
				<CriteriaValueInput
					onChange={(value) =>
						dispatch({
							payload: {
								partial: {value},
								ruleId: rule.id,
							},
							type: 'UPDATE_RULE',
						})
					}
					property={property}
					value={rule.value}
				/>
			</div>
		</div>
	);
}
