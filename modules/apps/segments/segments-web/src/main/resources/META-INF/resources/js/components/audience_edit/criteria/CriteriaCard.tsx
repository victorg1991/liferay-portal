/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton, {ClayButtonWithIcon} from '@clayui/button';
import ClayIcon from '@clayui/icon';
import {RowBuilder} from '@liferay/layout-js-components-web';
import React, {useCallback, useMemo, useRef} from 'react';
import {useDrop} from 'react-dnd';

import {
	useAudienceDispatch,
	useAudienceState,
} from '../contexts/AudienceEditContext';
import {ACCEPTING_TYPES} from '../drag_and_drop/constants/acceptingTypes';
import {AudienceNode, PropertyField, PropertyGroup, isRule} from '../types';
import {createRule} from '../utils/criteriaTree';
import ConjunctionDivider from './ConjunctionDivider';
import CriteriaEmptyDropZone from './CriteriaEmptyDropZone';
import CriteriaGroup from './CriteriaGroup';
import CriteriaRow from './CriteriaRow';

interface DragItem {
	payload: PropertyField | {id: string};
	type: string;
}

interface DnDItem {
	id: string;
	name: string;
}

interface Props {
	propertyGroups: PropertyGroup[];
}

export default function CriteriaCard({propertyGroups}: Props) {
	const state = useAudienceState();
	const dispatch = useAudienceDispatch();

	const propertyLookup = useMemo(() => {
		const map = new Map<string, PropertyField>();

		propertyGroups.forEach((group) => {
			group.properties.forEach((property) => {
				map.set(`${property.entityName}.${property.name}`, property);
			});
		});

		return (entityName: string, attribute: string) =>
			map.get(`${entityName}.${attribute}`);
	}, [propertyGroups]);

	const rootNodes = state.criteria.rules;

	const dndItems = useMemo<DnDItem[]>(
		() =>
			rootNodes.map((node) => ({
				id: node.id,
				name: isRule(node)
					? propertyLookup(node.entityName, node.attribute)?.label ??
						node.attribute
					: Liferay.Language.get('group'),
			})),
		[propertyLookup, rootNodes]
	);

	const handleReorder = useCallback(
		(
			newItems: DnDItem[],
			targetId: string,
			position: 'bottom' | 'middle' | 'top' | null,
			sourceId: string
		) => {
			if (position === 'middle') {
				dispatch({
					payload: {sourceId, targetId},
					type: 'MERGE_NODES',
				});

				return;
			}

			const nodesById = new Map<string, AudienceNode>(
				rootNodes.map((node) => [node.id, node])
			);

			const newNodes = newItems
				.map((item) => nodesById.get(item.id))
				.filter((node): node is AudienceNode => Boolean(node));

			dispatch({
				payload: {groupId: null, rules: newNodes},
				type: 'REPLACE_RULES',
			});
		},
		[dispatch, rootNodes]
	);

	const cardBodyRef = useRef<HTMLDivElement | null>(null);

	const [, drop] = useDrop<DragItem, void, unknown>({
		accept: ACCEPTING_TYPES.PROPERTY,
		drop(item, monitor) {
			if (!monitor.isOver({shallow: true})) {
				return;
			}

			if (item.type !== ACCEPTING_TYPES.PROPERTY) {
				return;
			}

			const property = item.payload as PropertyField;

			dispatch({
				payload: createRule({
					attribute: property.name,
					entityName: property.entityName,
					type: property.type,
				}),
				type: 'APPEND_NODE',
			});
		},
	});

	const setBodyRef = useCallback(
		(node: HTMLDivElement | null) => {
			cardBodyRef.current = node;
			drop(node);
		},
		[drop]
	);

	const empty = !rootNodes.length;

	return (
		<section className="audience-criteria-card card">
			<header className="audience-criteria-card__header">
				<h3>{Liferay.Language.get('criteria')}</h3>
			</header>

			<div className="audience-criteria-card__body" ref={setBodyRef}>
				{empty ? (
					<CriteriaEmptyDropZone />
				) : (
					<RowBuilder<DnDItem>
						createItem={() => ({id: '', name: ''})}
						hideAddButton
						items={dndItems}
						labels={{
							addedAnnouncement:
								Liferay.Language.get('criterion-added'),
							delete: Liferay.Language.get('delete'),
							deletedAnnouncement:
								Liferay.Language.get('criterion-deleted'),
							list: Liferay.Language.get('criteria'),
						}}
						renderItem={({index}) => {
							const node = rootNodes[index];

							if (isRule(node)) {
								return (
									<CriteriaRow
										index={index}
										items={dndItems}
										onReorder={handleReorder}
										property={propertyLookup(
											node.entityName,
											node.attribute
										)}
										rule={node}
									/>
								);
							}

							return (
								<CriteriaGroup
									group={node}
									propertyGroups={propertyGroups}
									propertyLookup={propertyLookup}
								/>
							);
						}}
						renderItemActions={({index}) => {
							const node = rootNodes[index];

							if (!isRule(node)) {
								return null;
							}

							return (
								<ClayButtonWithIcon
									aria-label={Liferay.Language.get(
										'duplicate'
									)}
									borderless
									displayType="secondary"
									onClick={() =>
										dispatch({
											payload: {id: node.id},
											type: 'DUPLICATE_NODE',
										})
									}
									size="sm"
									symbol="copy"
									title={Liferay.Language.get('duplicate')}
								/>
							);
						}}
						renderItemSeparator={() => (
							<ConjunctionDivider
								conjunction={state.criteria.conjunction}
								onChange={(conjunction) =>
									dispatch({
										payload: {conjunction},
										type: 'SET_ROOT_CONJUNCTION',
									})
								}
							/>
						)}
						setItems={(newItems) => {
							const remainingIds = new Set(
								newItems.map((item) => item.id)
							);

							const removed = rootNodes.find(
								(node) => !remainingIds.has(node.id)
							);

							if (removed) {
								dispatch({
									payload: {id: removed.id},
									type: 'REMOVE_NODE',
								});
							}
						}}
					/>
				)}

				{!empty ? (
					<>
						<div className="audience-criteria-card__actions">
							<ClayButton
								borderless
								displayType="secondary"
								onClick={() => dispatch({type: 'CLEAR_ALL'})}
								size="sm"
							>
								<ClayIcon
									className="mr-1"
									symbol="times-circle"
								/>

								{Liferay.Language.get('clear-all')}
							</ClayButton>
						</div>

						<p className="audience-criteria-card__hint text-secondary">
							<ClayIcon className="mr-1" symbol="layers" />

							{Liferay.Language.get(
								'drag-and-drop-over-an-existing-criteria-to-form-groups'
							)}
						</p>
					</>
				) : null}
			</div>
		</section>
	);
}
