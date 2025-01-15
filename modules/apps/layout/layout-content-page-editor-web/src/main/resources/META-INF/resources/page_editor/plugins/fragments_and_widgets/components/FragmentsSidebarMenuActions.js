/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayDropDown from '@clayui/drop-down';
import ClayIcon from '@clayui/icon';
import {FocusScope} from '@clayui/shared';
import {sub} from 'frontend-js-web';
import React, {useCallback, useMemo, useRef, useState} from 'react';
import {flushSync} from 'react-dom';

import {FRAGMENTS_DISPLAY_STYLES} from '../../../app/config/constants/fragmentsDisplayStyles';
import {ReorderSetsModal} from './reorder_sets_modal/ReorderSetsModal';

export default function FragmentsSidebarMenuActions({
	displayStyle,
	displayStyleButtonDisabled,
	setDisplayStyle,
}) {
	const [active, setActive] = useState(false);

	const alignElementRef = useRef();
	const dropdownRef = useRef();

	const updateActive = useCallback((nextActive) => {
		flushSync(() => {
			setActive(nextActive);
		});

		if (nextActive) {
			dropdownRef.current?.querySelector('button')?.focus();
		}
		else {
			alignElementRef.current?.focus();
		}
	}, []);

	const [showReorderModal, setShowReorderModal] = useState(false);

	return (
		<>
			<ClayButton
				aria-expanded={active}
				aria-haspopup="true"
				aria-label={Liferay.Language.get('components-options')}
				className="ml-2 page-editor__sidebar__fragments-widgets-panel-menu-actions position-relative"
				displayType="unstyled"
				onClick={(event) => {
					event.stopPropagation();
					updateActive(!active);
				}}
				ref={alignElementRef}
				size="sm"
				title={Liferay.Language.get('components-options')}
			>
				{active ? (
					<div
						className="position-absolute"
						style={{
							height: '50px',
							transform: 'translateX(-10px, -10px)',
							width: '50px',
						}}
					/>
				) : null}

				<ClayIcon symbol="ellipsis-v" />
			</ClayButton>

			<ClayDropDown.Menu
				active={active}
				alignElementRef={alignElementRef}
				containerProps={{
					className: 'cadmin',
				}}
				hasLeftSymbols
				onActiveChange={updateActive}
				ref={dropdownRef}
			>
				{active && (
					<ActionList
						displayStyle={displayStyle}
						displayStyleButtonDisabled={displayStyleButtonDisabled}
						setActive={setActive}
						setDisplayStyle={setDisplayStyle}
						setShowReorderModal={setShowReorderModal}
					/>
				)}
			</ClayDropDown.Menu>

			{showReorderModal && (
				<ReorderSetsModal
					onCloseModal={() => setShowReorderModal(false)}
				/>
			)}
		</>
	);
}

const ActionList = ({
	displayStyle,
	displayStyleButtonDisabled,
	setActive,
	setDisplayStyle,
	setShowReorderModal,
}) => {
	const dropdownItems = useMemo(() => {
		const items = [];

		items.push({
			action: () => {
				setShowReorderModal(true);
			},
			icon: 'order-arrow',
			label: Liferay.Language.get('reorder-sets'),
		});

		items.push({
			action: () => {
				setDisplayStyle(
					displayStyle === FRAGMENTS_DISPLAY_STYLES.LIST
						? FRAGMENTS_DISPLAY_STYLES.CARDS
						: FRAGMENTS_DISPLAY_STYLES.LIST
				);
			},
			disabled: displayStyleButtonDisabled,
			icon:
				displayStyleButtonDisabled ||
				displayStyle === FRAGMENTS_DISPLAY_STYLES.LIST
					? 'cards2'
					: 'list',
			label: sub(
				Liferay.Language.get('switch-to-x-view'),
				displayStyle === FRAGMENTS_DISPLAY_STYLES.LIST
					? Liferay.Language.get('card')
					: Liferay.Language.get('list[noun]')
			),
		});

		return items;
	}, [
		displayStyle,
		displayStyleButtonDisabled,
		setDisplayStyle,
		setShowReorderModal,
	]);

	return (
		<FocusScope>
			<div>
				<ClayDropDown.ItemList items={dropdownItems}>
					{(item) => (
						<ClayDropDown.Item
							aria-label={item.label}
							disabled={item.disabled}
							onClick={() => {
								setActive(false);

								item.action();
							}}
							symbolLeft={item.icon}
						>
							{item.label}
						</ClayDropDown.Item>
					)}
				</ClayDropDown.ItemList>
			</div>
		</FocusScope>
	);
};
