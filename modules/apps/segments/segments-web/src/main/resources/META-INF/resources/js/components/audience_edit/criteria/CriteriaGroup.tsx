/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClayButtonWithIcon} from '@clayui/button';
import {RowBuilder} from '@liferay/layout-js-components-web';
import React, {useMemo} from 'react';

import {useAudienceDispatch} from '../contexts/AudienceEditContext';
import {
	AudienceGroup,
	AudienceNode,
	PropertyField,
	PropertyGroup,
	isRule,
} from '../types';
import ConjunctionDivider from './ConjunctionDivider';
import CriteriaRow from './CriteriaRow';

interface DnDItem {
	id: string;
	name: string;
}

interface Props {
	group: AudienceGroup;
	propertyGroups: PropertyGroup[];
	propertyLookup: (
		entityName: string,
		attribute: string
	) => PropertyField | undefined;
}

export default function CriteriaGroup({
	group,
	propertyGroups,
	propertyLookup,
}: Props) {
	const dispatch = useAudienceDispatch();

	const dndItems = useMemo<DnDItem[]>(
		() =>
			group.rules.map((node) => ({
				id: node.id,
				name: isRule(node)
					? propertyLookup(node.entityName, node.attribute)?.label ??
						node.attribute
					: Liferay.Language.get('group'),
			})),
		[group.rules, propertyLookup]
	);

	const handleReorder = (
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

		const rulesById = new Map<string, AudienceNode>(
			group.rules.map((node) => [node.id, node])
		);

		const newRules = newItems
			.map((item) => rulesById.get(item.id))
			.filter((node): node is AudienceNode => Boolean(node));

		dispatch({
			payload: {groupId: group.id, rules: newRules},
			type: 'REPLACE_RULES',
		});
	};

	return (
		<div className="audience-group">
			<div className="audience-group__delete">
				<ClayButtonWithIcon
					aria-label={Liferay.Language.get('delete-group')}
					borderless
					displayType="secondary"
					onClick={() =>
						dispatch({
							payload: {id: group.id},
							type: 'REMOVE_NODE',
						})
					}
					size="sm"
					symbol="times-circle"
					title={Liferay.Language.get('delete-group')}
				/>
			</div>

			<div className="audience-group__body">
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
						const child = group.rules[index];

						if (isRule(child)) {
							return (
								<CriteriaRow
									index={index}
									items={dndItems}
									onReorder={handleReorder}
									property={propertyLookup(
										child.entityName,
										child.attribute
									)}
									rule={child}
								/>
							);
						}

						return (
							<CriteriaGroup
								group={child}
								propertyGroups={propertyGroups}
								propertyLookup={propertyLookup}
							/>
						);
					}}
					renderItemActions={({index}) => {
						const child = group.rules[index];

						if (!isRule(child)) {
							return null;
						}

						return (
							<ClayButtonWithIcon
								aria-label={Liferay.Language.get('duplicate')}
								borderless
								displayType="secondary"
								onClick={() =>
									dispatch({
										payload: {id: child.id},
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
							conjunction={group.conjunction}
							onChange={(conjunction) =>
								dispatch({
									payload: {
										conjunction,
										groupId: group.id,
									},
									type: 'UPDATE_GROUP_CONJUNCTION',
								})
							}
						/>
					)}
					setItems={(newItems) => {
						const remainingIds = new Set(
							newItems.map((item) => item.id)
						);

						const removed = group.rules.find(
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
			</div>
		</div>
	);
}
