/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.layout.page.template.internal.info.item.provider;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.exception.NoSuchEntryException;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.info.exception.NoSuchInfoItemException;
import com.liferay.info.field.InfoField;
import com.liferay.info.field.InfoFieldSet;
import com.liferay.info.field.InfoFieldValue;
import com.liferay.info.field.type.CategoriesInfoFieldType;
import com.liferay.info.field.type.URLInfoFieldType;
import com.liferay.info.item.field.reader.InfoItemFieldReaderFieldSetProvider;
import com.liferay.info.localized.InfoLocalizedValue;
import com.liferay.info.type.KeyLocalizedLabelPair;
import com.liferay.layout.page.template.constants.LayoutPageTemplateEntryTypeConstants;
import com.liferay.layout.page.template.info.item.provider.DisplayPageInfoItemFieldSetProvider;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Eudaldo Alonso
 */
@Component(service = DisplayPageInfoItemFieldSetProvider.class)
public class DisplayPageInfoItemFieldSetProviderImpl
	implements DisplayPageInfoItemFieldSetProvider {

	@Override
	public InfoFieldSet getInfoFieldSet(
		String itemClassName, String infoItemFormVariationKey,
		long scopeGroupId) {

		return _getDisplayPageInfoFieldSet(
			_getDisplayPages(
				itemClassName, infoItemFormVariationKey, scopeGroupId));
	}

	private void _getDisplayPages(
		String itemClassName, String infoItemFormVariationKey, long scopeGroupId) {

		List<LayoutPageTemplateEntry> layoutPageTemplateEntries =
			_layoutPageTemplateEntryService.getLayoutPageTemplateEntries(
				scopeGroupId, _portal.getClassNameId(itemClassName),
				GetterUtil.getLong(infoItemFormVariationKey),
				LayoutPageTemplateEntryTypeConstants.TYPE_DISPLAY_PAGE);

		for (LayoutPageTemplateEntry layoutPageTemplateEntry :
				layoutPageTemplateEntries) {

			jsonArray.put(
				JSONUtil.put(
					"default", layoutPageTemplateEntry.isDefaultTemplate()
				).put(
					"key",
					layoutPageTemplateEntry.getLayoutPageTemplateEntryKey()
				).put(
					"name", layoutPageTemplateEntry.getName()
				));
		}
	}

	private InfoFieldSet _getInfoFieldSet(
		String itemClassName, String infoItemFormVariationKey,
		long scopeGroupId) {

		List<LayoutPageTemplateEntry> layoutPageTemplateEntries =
			_layoutPageTemplateEntryService.getLayoutPageTemplateEntries(
				scopeGroupId, _portal.getClassNameId(itemClassName),
				GetterUtil.getLong(infoItemFormVariationKey),
				LayoutPageTemplateEntryTypeConstants.TYPE_DISPLAY_PAGE);

		return InfoFieldSet.builder(
		).infoFieldSetEntry(
			unsafeConsumer -> layoutPageTemplateEntries.forEach(
				layoutPageTemplateEntry -> unsafeConsumer.accept(
					InfoField.builder(
					).infoFieldType(
						URLInfoFieldType.INSTANCE
					).uniqueId(
						AssetVocabulary.class.getSimpleName() +
						StringPool.UNDERLINE +
						assetVocabulary.getVocabularyId()
					).name(
						layoutPageTemplateEntry.getName()
					).labelInfoLocalizedValue(
						InfoLocalizedValue.localize(getClass(), "display-page")
					).build()))
		).labelInfoLocalizedValue(
			InfoLocalizedValue.localize(getClass(), "display-page")
		).name(
			"display-page"
		).build();
	}

	@Override
	public List<InfoFieldValue<Object>> getInfoFieldValues(
		AssetEntry assetEntry) {

		List<InfoFieldValue<Object>> infoFieldValues = new ArrayList<>();

		infoFieldValues.add(
			new InfoFieldValue<>(
				_displayPageURLInfoField,
				() -> _getDisplayPages(
					assetEntry.getClassName(), assetEntry.getClassPK())));

		return infoFieldValues;
	}

	@Override
	public List<InfoFieldValue<Object>> getInfoFieldValues(
			String itemClassName, long itemClassPK)
		throws NoSuchInfoItemException {

		AssetRendererFactory<?> assetRendererFactory =
			AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(
				itemClassName);

		try {
			return getInfoFieldValues(
				assetRendererFactory.getAssetEntry(itemClassName, itemClassPK));
		}
		catch (NoSuchEntryException noSuchEntryException) {
			throw new NoSuchInfoItemException(
				StringBundler.concat(
					"Unable to get asset entry with class name ", itemClassName,
					" and class PK ", itemClassPK),
				noSuchEntryException);
		}
		catch (PortalException portalException) {
			throw new RuntimeException(portalException);
		}
	}

	private static final InfoField<URLInfoFieldType> _displayPageURLInfoField =
		InfoField.builder(
		).infoFieldType(
			URLInfoFieldType.INSTANCE
		).namespace(
		).name(
			"displayPageURL"
		).labelInfoLocalizedValue(
			InfoLocalizedValue.localize(
				"com.liferay.asset.info.display.impl", "display-page-url")
		).build();

	private InfoFieldSet _getDisplayPageInfoFieldSet() {
		return InfoFieldSet.builder(
		).infoFieldSetEntry(
			_displayPageURLInfoField
		).labelInfoLocalizedValue(
			InfoLocalizedValue.localize(getClass(), "display-page")
		).name(
			"display-page"
		).build();
	}

	@Reference
	private InfoItemFieldReaderFieldSetProvider
		_infoItemFieldReaderFieldSetProvider;

	@Reference
	private Portal _portal;

	@Reference
	private LayoutPageTemplateEntryService _layoutPageTemplateEntryService;

}