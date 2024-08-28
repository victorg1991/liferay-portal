/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import copyItem from '../actions/copyItem';
import pasteItem from '../actions/pasteItem';
import {COPY_ITEM, PASTE_ITEM} from '../actions/types';

export const INITIAL_STATE = [];

export default function copyFragmentItemIdsReducer(
	itemIds: string[] = INITIAL_STATE,
	action: ReturnType<typeof copyItem> | ReturnType<typeof pasteItem>
) {
	switch (action.type) {
		case COPY_ITEM:
			return action.itemIds;

		case PASTE_ITEM:
			return INITIAL_STATE;

		default:
			return itemIds;
	}
}
