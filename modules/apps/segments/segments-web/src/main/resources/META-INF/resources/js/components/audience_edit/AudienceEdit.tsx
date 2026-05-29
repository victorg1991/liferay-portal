/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {DragAndDropContextProvider} from '@liferay/layout-js-components-web';
import React from 'react';

import {
	AudienceEditProvider,
	useAudienceState,
} from './contexts/AudienceEditContext';
import CriteriaCard from './criteria/CriteriaCard';
import AudienceHeader from './header/AudienceHeader';
import ScopeCard from './scope/ScopeCard';
import AudienceSidebar from './sidebar/AudienceSidebar';
import {AudienceCriteria, PropertyGroup, RetentionType} from './types';
import {emptyCriteria} from './utils/criteriaTree';

import './AudienceEdit.scss';

export interface AudienceEditProps {
	defaultLanguageId: string;
	initialCriteria?: AudienceCriteria | null;
	initialERC: string;
	initialName: string;
	initialRetentionType?: RetentionType;
	initialScope?: {groupId: string; name: string} | null;
	portletNamespace: string;
	propertyGroups: PropertyGroup[];
	selectScopeURL: string;
}

function HiddenCriteriaInput({portletNamespace}: {portletNamespace: string}) {
	const state = useAudienceState();

	return (
		<input
			name={`${portletNamespace}audienceCriteriaJSON`}
			type="hidden"
			value={JSON.stringify(state.criteria)}
		/>
	);
}

function HiddenRetentionInput({portletNamespace}: {portletNamespace: string}) {
	const state = useAudienceState();

	return (
		<input
			name={`${portletNamespace}retentionType`}
			type="hidden"
			value={state.retentionType}
		/>
	);
}

export default function AudienceEdit({
	defaultLanguageId,
	initialCriteria,
	initialERC,
	initialName,
	initialRetentionType = 'SESSION',
	initialScope = null,
	portletNamespace,
	propertyGroups,
	selectScopeURL,
}: AudienceEditProps) {
	return (
		<DragAndDropContextProvider>
			<AudienceEditProvider
				initialState={{
					criteria: initialCriteria ?? emptyCriteria(),
					erc: initialERC,
					name: initialName,
					retentionType: initialRetentionType,
					scope: initialScope,
				}}
			>
				<div className="audience-edit">
					<AudienceSidebar propertyGroups={propertyGroups} />

					<div className="audience-edit__main">
						<AudienceHeader
							defaultLanguageId={defaultLanguageId}
							portletNamespace={portletNamespace}
						/>

						<ScopeCard
							portletNamespace={portletNamespace}
							selectScopeURL={selectScopeURL}
						/>

						<CriteriaCard propertyGroups={propertyGroups} />
					</div>

					<HiddenCriteriaInput portletNamespace={portletNamespace} />

					<HiddenRetentionInput portletNamespace={portletNamespace} />
				</div>
			</AudienceEditProvider>
		</DragAndDropContextProvider>
	);
}
