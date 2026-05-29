/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClayInput} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import React from 'react';

import {
	useAudienceDispatch,
	useAudienceState,
} from '../contexts/AudienceEditContext';

interface Props {
	defaultLanguageId: string;
	portletNamespace: string;
}

export default function AudienceHeader({
	defaultLanguageId,
	portletNamespace,
}: Props) {
	const state = useAudienceState();
	const dispatch = useAudienceDispatch();

	return (
		<header className="audience-header">
			<input
				name={`${portletNamespace}defaultLanguageId`}
				type="hidden"
				value={defaultLanguageId}
			/>

			<input
				name={`${portletNamespace}name_${defaultLanguageId}`}
				type="hidden"
				value={state.name}
			/>

			<ClayInput
				aria-label={Liferay.Language.get('audience-name')}
				className="audience-header__title"
				onChange={(event) =>
					dispatch({
						payload: {name: event.target.value},
						type: 'SET_NAME',
					})
				}
				placeholder={Liferay.Language.get('new-audience')}
				type="text"
				value={state.name}
			/>

			<div className="audience-header__erc">
				<label
					className="audience-header__erc-label"
					htmlFor={`${portletNamespace}erc`}
				>
					{Liferay.Language.get('erc')}

					<span
						className="lfr-portal-tooltip ml-1"
						title={Liferay.Language.get(
							'external-reference-code-helper'
						)}
					>
						<ClayIcon symbol="info-circle" />
					</span>
				</label>

				<ClayInput
					id={`${portletNamespace}erc`}
					name={`${portletNamespace}segmentsEntryKey`}
					onChange={(event) =>
						dispatch({
							payload: {erc: event.target.value},
							type: 'SET_ERC',
						})
					}
					readOnly
					value={state.erc}
				/>
			</div>
		</header>
	);
}
