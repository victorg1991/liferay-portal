/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {UPDATE_FORM_ITEM_CONFIG} from './types';

import type {
	DeletedLayoutDataItem,
	LayoutData,
} from '../../types/layout_data/LayoutData';
import type {FragmentEntryLinkMap} from './addFragmentEntryLinks';

export default function updateFormItemConfig({
	addedFragmentEntryLinks = null,
	addedItemIds = [],
	deletedItems = [],
	isMapping,
	itemIds,
	layoutData,
	movedItemIds = [],
	removedItemIds = [],
}: {
	addedFragmentEntryLinks?: FragmentEntryLinkMap | null;
	addedItemIds: string[];
	deletedItems?: DeletedLayoutDataItem[];
	isMapping: boolean;
	itemIds: string[];
	layoutData: LayoutData;
	movedItemIds: {itemId: string; parentId: string}[];
	removedItemIds?: string[];
}) {
	return {
		addedFragmentEntryLinks,
		addedItemIds,
		deletedItems,
		isMapping,
		itemIds,
		layoutData,
		movedItemIds,
		removedItemIds,
		type: UPDATE_FORM_ITEM_CONFIG,
	} as const;
}
