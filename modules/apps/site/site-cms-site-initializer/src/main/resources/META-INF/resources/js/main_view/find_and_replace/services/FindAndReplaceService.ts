/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ApiHelper from '../../../common/services/ApiHelper';
import {ISearchAssetObjectEntry} from '../../../common/types/AssetType';
import {StickerConfig} from '../../../common/types/StickerConfig';
import {getFileMimeTypeObjectDefinitionStickerValue} from '../../props_transformer/utils/transformViewsItemProps';
import {ReplaceItem} from '../contexts/FindAndReplaceContext';

function enrichItem({
	fdsItem,
	replaceItem,
	stickerConfig,
}: {
	fdsItem: ISearchAssetObjectEntry;
	replaceItem: ReplaceItem;
	stickerConfig: StickerConfig;
}) {
	const {
		fileMimeTypeCssClasses,
		fileMimeTypeIcons,
		objectDefinitionCssClasses,
		objectDefinitionIcons,
	} = stickerConfig;

	const stickerClassName = getFileMimeTypeObjectDefinitionStickerValue(
		fileMimeTypeCssClasses,
		objectDefinitionCssClasses,
		fdsItem
	);

	const stickerSymbol =
		getFileMimeTypeObjectDefinitionStickerValue(
			fileMimeTypeIcons,
			objectDefinitionIcons,
			fdsItem
		) || 'documents-and-media';

	return {
		...replaceItem,
		className: fdsItem.entryClassName,
		stickerClassName,
		stickerSymbol,
	};
}

async function getReplaceItems({
	fdsItems,
	stickerConfig,
}: {
	fdsItems: ISearchAssetObjectEntry[];
	stickerConfig: StickerConfig;
}) {
	const formData = new FormData();

	const objectEntries = fdsItems.map((item) => ({
		className: item.entryClassName,
		objectEntryId: item.embedded.id,
	}));

	formData.append('objectEntries', JSON.stringify(objectEntries));

	const response = await ApiHelper.postFormData<ReplaceItem[]>(
		formData,
		`${Liferay.ThemeDisplay.getPathMain()}/cms/get_object_entries_values`
	);

	if (response.error || !response.data) {
		return response;
	}

	const fdsItemsMap = new Map<string, ISearchAssetObjectEntry>();

	for (const fdsItem of fdsItems) {
		fdsItemsMap.set(String(fdsItem.embedded.id), fdsItem);
	}

	const data = response.data.map((replaceItem) => {
		const fdsItem = fdsItemsMap.get(replaceItem.id);

		if (!fdsItem) {
			return replaceItem;
		}

		return enrichItem({fdsItem, replaceItem, stickerConfig});
	});

	return {
		...response,
		data,
	};
}

export default {getReplaceItems};
