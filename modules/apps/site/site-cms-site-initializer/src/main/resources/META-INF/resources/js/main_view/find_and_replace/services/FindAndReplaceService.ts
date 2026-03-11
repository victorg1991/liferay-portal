/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ApiHelper from '../../../common/services/ApiHelper';
import {ISearchAssetObjectEntry} from '../../../common/types/AssetType';
import {StickerConfig} from '../../../common/types/StickerConfig';
import {triggerAssetBulkAction} from '../../props_transformer/actions/triggerAssetBulkAction';
import {getFileMimeTypeObjectDefinitionStickerValue} from '../../props_transformer/utils/transformViewsItemProps';
import {ReplaceItem, ReplaceItemField} from '../contexts/FindAndReplaceContext';

type Values = Record<
	string,
	ReplaceItemField['value'] | ReplaceItemField['value_i18n']
>;

function replaceFieldValues(
	fields: ReplaceItemField[],
	search: string,
	replacement: string
) {
	const values: Values = {};

	for (const field of fields) {
		if (field.value_i18n) {
			values[`${field.name}_i18n`] = Object.fromEntries(
				Object.entries(field.value_i18n).map(([localeId, value]) => [
					localeId,
					value.replaceAll(search, replacement),
				])
			);
		}
		else if (field.value) {
			values[field.name] = field.value.replaceAll(search, replacement);
		}
	}

	return values;
}

function getBulkReplaceData({
	replaceItems,
	replacement,
	search,
}: {
	replaceItems: ReplaceItem[];
	replacement: string;
	search: string;
}) {
	const items: Array<{className: string; id: number}> = [];
	const values: Values = {};

	for (const replaceItem of replaceItems) {
		items.push({
			className: replaceItem.className,
			id: Number(replaceItem.id),
		});

		const itemValues = replaceFieldValues(
			replaceItem.fields,
			search,
			replacement
		);

		const relatedValues: Record<string, Array<Values>> = {};

		for (const relatedItem of replaceItem.related || []) {
			relatedValues[relatedItem.name] = [
				{
					externalReferenceCode: relatedItem.externalReferenceCode,
					...replaceFieldValues(
						relatedItem.fields,
						search,
						replacement
					),
				},
			];
		}

		values[replaceItem.id] = {
			...itemValues,
			...relatedValues,
		};
	}

	return {
		items,
		values,
	};
}

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

function performBulkReplace({
	dataSetId,
	items: replaceItems,
	replacement,
	search,
}: {
	dataSetId: string;
	items: ReplaceItem[];
	replacement: string;
	search: string;
}) {
	const {items, values} = getBulkReplaceData({
		replaceItems,
		replacement,
		search,
	});

	triggerAssetBulkAction<'UpdateObjectValuesBulkSelectionAction'>({
		additionalData: {
			replacement,
			search,
		},
		apiURL: '/o/bulk/v1.0/bulk-action',
		dataSetId,
		keyValues: {
			values,
		},
		resetSearch: true,
		selectedData: {
			items: items as unknown as ISearchAssetObjectEntry[],
			selectAll: false,
		},
		type: 'UpdateObjectValuesBulkSelectionAction',
	});
}

function performSingleReplace({
	item,
	replacement,
	search,
}: {
	item: ReplaceItem;
	replacement: string;
	search: string;
}) {
	const {items, values} = getBulkReplaceData({
		replaceItems: [item],
		replacement,
		search,
	});

	return ApiHelper.post('/o/bulk/v1.0/bulk-action', {
		bulkActionItems: items.map(({className, id}) => ({
			className,
			classPK: id,
		})),
		type: 'UpdateObjectValuesBulkSelectionAction',
		values,
	});
}

export default {getReplaceItems, performBulkReplace, performSingleReplace};
