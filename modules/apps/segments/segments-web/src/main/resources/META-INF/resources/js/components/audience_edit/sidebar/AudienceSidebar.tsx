/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import {ClayInput, ClaySelectWithOption} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import {sub} from 'frontend-js-web';
import React, {useMemo, useState} from 'react';

import {
	useAudienceDispatch,
	useAudienceState,
} from '../contexts/AudienceEditContext';
import {PropertyField, PropertyGroup, RetentionType} from '../types';
import AudienceSidebarItem from './AudienceSidebarItem';

interface Props {
	propertyGroups: PropertyGroup[];
}

type TabKey = 'default' | 'custom';

const RETENTION_TYPES: Array<{label: string; value: RetentionType}> = [
	{label: Liferay.Language.get('session'), value: 'SESSION'},
	{label: Liferay.Language.get('persistent'), value: 'PERSISTENT'},
];

export default function AudienceSidebar({propertyGroups}: Props) {
	const [activeTab, setActiveTab] = useState<TabKey>('default');
	const [searchTerm, setSearchTerm] = useState('');

	const state = useAudienceState();
	const dispatch = useAudienceDispatch();

	const properties = useMemo<PropertyField[]>(() => {
		const groups = propertyGroups.filter((group) => {
			if (activeTab === 'default') {
				return group.propertyKey === 'context';
			}

			return group.propertyKey !== 'context';
		});

		const all = groups.flatMap((group) => group.properties);

		if (!searchTerm) {
			return all;
		}

		const lower = searchTerm.toLowerCase();

		return all.filter((property) =>
			property.label.toLowerCase().includes(lower)
		);
	}, [activeTab, propertyGroups, searchTerm]);

	return (
		<aside className="audience-sidebar card">
			<div className="audience-sidebar__header">
				<h2 className="audience-sidebar__title">
					{Liferay.Language.get('audience-criteria')}
				</h2>

				<label className="audience-sidebar__field-label">
					{Liferay.Language.get('retention-type')}

					<span
						className="lfr-portal-tooltip ml-1"
						title={Liferay.Language.get(
							'session-audiences-evaluate-per-visit-persistent-audiences-persist-the-cookie'
						)}
					>
						<ClayIcon symbol="info-circle" />
					</span>
				</label>

				<ClaySelectWithOption
					aria-label={Liferay.Language.get('retention-type')}
					onChange={(event) =>
						dispatch({
							payload: {
								retentionType: event.target
									.value as RetentionType,
							},
							type: 'SET_RETENTION_TYPE',
						})
					}
					options={RETENTION_TYPES}
					value={state.retentionType}
				/>
			</div>

			<ul className="audience-sidebar__tabs nav nav-tabs">
				<li className="nav-item">
					<ClayButton
						borderless
						className={`nav-link ${
							activeTab === 'default' ? 'active' : ''
						}`}
						displayType="unstyled"
						onClick={() => setActiveTab('default')}
					>
						{Liferay.Language.get('default')}
					</ClayButton>
				</li>

				<li className="nav-item">
					<ClayButton
						borderless
						className={`nav-link ${
							activeTab === 'custom' ? 'active' : ''
						}`}
						displayType="unstyled"
						onClick={() => setActiveTab('custom')}
					>
						{Liferay.Language.get('custom')}
					</ClayButton>
				</li>
			</ul>

			<div className="audience-sidebar__search">
				<ClayInput.Group>
					<ClayInput.GroupItem>
						<ClayInput
							aria-label={Liferay.Language.get('search')}
							onChange={(event) =>
								setSearchTerm(event.target.value)
							}
							placeholder={Liferay.Language.get('search')}
							type="text"
							value={searchTerm}
						/>

						<ClayInput.GroupInsetItem after tag="span">
							<ClayIcon symbol="search" />
						</ClayInput.GroupInsetItem>
					</ClayInput.GroupItem>
				</ClayInput.Group>
			</div>

			<div className="audience-sidebar__list">
				{!properties.length ? (
					<p className="p-3 text-center text-secondary">
						{sub(
							Liferay.Language.get('no-results-for-x'),
							searchTerm || ''
						)}
					</p>
				) : (
					properties.map((property) => (
						<AudienceSidebarItem
							key={`${property.entityName}.${property.name}`}
							property={property}
						/>
					))
				)}
			</div>
		</aside>
	);
}
