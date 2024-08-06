/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React, {useCallback, useEffect, useState} from 'react';

import {
	useActiveItemId,
} from '../../../../../app/contexts/ControlsContext';
import {useMovementTarget} from '../../../../../app/contexts/KeyboardMovementContext';
import {
	useSelector,
} from '../../../../../app/contexts/StoreContext';
import StructureTreeContent from './StructureTreeContent';

export default function PageStructureSidebar() {
	const activeItemId = useActiveItemId();
	const layoutData = useSelector((state) => state.layoutData);
	const masterLayoutData = useSelector(
		(state) => state.masterLayout?.masterLayoutData
	);
	const [expandedKeys, setExpandedKeys] = useState([]);

	const {itemId: keyboardMovementTargetId} = useMovementTarget();

	const getAncestorsIds = useCallback(
		(layoutDataItem, data) => {
			if (!layoutDataItem.parentId) {
				const itemInMasterLayout =
					masterLayoutData?.items[layoutDataItem.itemId];
				if (
					!itemInMasterLayout &&
					masterLayoutData?.rootItems?.dropZone
				) {
					const dropZoneItem =
						masterLayoutData.items[
							masterLayoutData.rootItems.dropZone
						];

					return [
						...[layoutDataItem.itemId],
						...getAncestorsIds(
							masterLayoutData.items[dropZoneItem.parentId],
							masterLayoutData
						),
					];
				}
				else {
					return [layoutDataItem.itemId];
				}
			}

			return [
				...[layoutDataItem.itemId],
				...getAncestorsIds(data.items[layoutDataItem.parentId], data),
			];
		},
		[masterLayoutData]
	);

	useEffect(() => {
		if (activeItemId) {
			const layoutDataActiveItem = layoutData.items[activeItemId];

			if (!layoutDataActiveItem) {
				return;
			}

			setExpandedKeys((previousExpanedKeys) => [
				...new Set([
					...previousExpanedKeys,
					...getAncestorsIds(layoutDataActiveItem, layoutData),
				]),
			]);
		}
	}, [activeItemId, getAncestorsIds, layoutData, masterLayoutData]);

	useEffect(() => {
		if (keyboardMovementTargetId) {
			const layoutDataTargetItem =
				layoutData.items[keyboardMovementTargetId];

			if (!layoutDataTargetItem) {
				return;
			}

			setExpandedKeys((previousExpanedKeys) => [
				...new Set([
					...previousExpanedKeys,
					...getAncestorsIds(layoutDataTargetItem, layoutData),
				]),
			]);
		}
	}, [getAncestorsIds, keyboardMovementTargetId, layoutData]);

	return (
		<StructureTreeContent
			expandedKeys={expandedKeys}
			setExpandedKeys={setExpandedKeys}
		/>
	);

}
