/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton, {ClayButtonWithIcon} from '@clayui/button';
import ClayIcon from '@clayui/icon';
import React from 'react';

import {
	useAudienceDispatch,
	useAudienceState,
} from '../contexts/AudienceEditContext';

interface Props {
	portletNamespace: string;
	selectScopeURL: string;
}

interface ScopeSelection {
	groupId: string;
	value: string;
}

export default function ScopeCard({portletNamespace, selectScopeURL}: Props) {
	const state = useAudienceState();
	const dispatch = useAudienceDispatch();

	const openSelector = () => {
		const openSelectionModal = (
			Liferay as unknown as {
				Util: {
					openSelectionModal: (config: {
						onSelect: (selectedItem: ScopeSelection) => void;
						selectEventName: string;
						title: string;
						url: string;
					}) => void;
				};
			}
		).Util.openSelectionModal;

		openSelectionModal({
			onSelect: (selectedItem) => {
				dispatch({
					payload: {
						scope: {
							groupId: selectedItem.groupId,
							name: selectedItem.value,
						},
					},
					type: 'SET_SCOPE',
				});
			},
			selectEventName: `${portletNamespace}selectScope`,
			title: Liferay.Language.get('select-scope'),
			url: selectScopeURL,
		});
	};

	return (
		<section className="audience-scope-card card">
			<header className="audience-scope-card__header">
				<h3>{Liferay.Language.get('scope')}</h3>

				<ClayButtonWithIcon
					aria-label={Liferay.Language.get('collapse-scope')}
					borderless
					displayType="secondary"
					size="sm"
					symbol="angle-down"
				/>
			</header>

			<div className="audience-scope-card__body">
				<div className="audience-scope-card__row">
					<p className="text-secondary">
						{Liferay.Language.get(
							'if-no-site-is-selected-the-audience-is-going-to-be-available-for-all-sites'
						)}
					</p>

					<ClayButton
						displayType="secondary"
						onClick={openSelector}
						size="sm"
					>
						<ClayIcon className="mr-1" symbol="plus" />

						{Liferay.Language.get('select-scope')}
					</ClayButton>
				</div>

				{state.scope ? (
					<div className="audience-scope-card__selection">
						<span>{state.scope.name}</span>

						<ClayButtonWithIcon
							aria-label={Liferay.Language.get('clear-scope')}
							borderless
							displayType="secondary"
							onClick={() =>
								dispatch({
									payload: {scope: null},
									type: 'SET_SCOPE',
								})
							}
							size="sm"
							symbol="times-circle"
						/>
					</div>
				) : null}

				<input
					name={`${portletNamespace}groupId`}
					type="hidden"
					value={state.scope?.groupId ?? ''}
				/>
			</div>
		</section>
	);
}
