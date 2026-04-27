/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClaySelectWithOption} from '@clayui/form';
import PropTypes from 'prop-types';
import React, {useState} from 'react';

import {propertyGroupShape} from '../../utils/types.es';
import CriteriaSidebarCollapse from './CriteriaSidebarCollapse';
import CriteriaSidebarSearchBar from './CriteriaSidebarSearchBar';

const RETENTION_TYPE_OPTIONS = [
	{label: Liferay.Language.get('session'), value: 'session'},
	{label: Liferay.Language.get('page'), value: 'page'},
];

export default function CriteriaSidebar({
	initialRetentionType = 'session',
	onTitleClicked,
	portletNamespace = '',
	propertyGroups,
	propertyKey,
}) {
	const [searchValue, setSearchValue] = useState('');
	const [retentionType, setRetentionType] = useState(initialRetentionType);

	return (
		<div
			aria-label={Liferay.Language.get('segments-contributors-panel')}
			className="criteria-sidebar-root d-flex flex-column"
			role="tabpanel"
			tabIndex={-1}
		>
			<div className="sidebar-header">
				{Liferay.Language.get('properties')}
			</div>

			<div className="sidebar-search">
				<CriteriaSidebarSearchBar
					onChange={(value) => setSearchValue(value)}
					searchValue={searchValue}
				/>
			</div>

			<div className="c-px-4 c-py-2">
				<label htmlFor="retentionType">
					{Liferay.Language.get('retention-type')}
				</label>

				<ClaySelectWithOption
					className="form-control-sm"
					id={`${portletNamespace}retentionType`}
					name={`${portletNamespace}retentionType`}
					onChange={(event) => setRetentionType(event.target.value)}
					options={RETENTION_TYPE_OPTIONS}
					value={retentionType}
				/>
			</div>

			<div className="c-p-4 position-relative sidebar-collapse">
				<CriteriaSidebarCollapse
					onCollapseClick={onTitleClicked}
					propertyGroups={propertyGroups}
					propertyKey={propertyKey}
					searchValue={searchValue}
				/>
			</div>
		</div>
	);
}

CriteriaSidebar.propTypes = {
	initialRetentionType: PropTypes.string,
	onTitleClicked: PropTypes.func,
	portletNamespace: PropTypes.string,
	propertyGroups: PropTypes.arrayOf(propertyGroupShape),
	propertyKey: PropTypes.string,
};
