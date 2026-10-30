/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.categories.admin.web.internal.info.item.provider;

import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetVocabularyLocalService;
import com.liferay.info.exception.NoSuchInfoItemException;
import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.ERCInfoItemIdentifier;
import com.liferay.info.item.InfoItemIdentifier;
import com.liferay.info.item.provider.BaseInfoItemObjectProvider;
import com.liferay.info.item.provider.InfoItemObjectProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Víctor Galán
 */
@Component(
	property = {
		"info.item.identifier=com.liferay.info.item.ClassPKInfoItemIdentifier",
		"info.item.identifier=com.liferay.info.item.ERCInfoItemIdentifier",
		"item.class.name=com.liferay.asset.kernel.model.AssetVocabulary",
		"service.ranking:Integer=100"
	},
	service = InfoItemObjectProvider.class
)
public class AssetVocabularyInfoItemObjectProvider
	extends BaseInfoItemObjectProvider<AssetVocabulary> {

	@Override
	protected AssetVocabulary doGetInfoItem(
			long groupId, InfoItemIdentifier infoItemIdentifier)
		throws NoSuchInfoItemException {

		if (!(infoItemIdentifier instanceof ClassPKInfoItemIdentifier) &&
			!(infoItemIdentifier instanceof ERCInfoItemIdentifier)) {

			throw new NoSuchInfoItemException(
				"Unsupported info item identifier " + infoItemIdentifier);
		}

		AssetVocabulary assetVocabulary = null;

		if (infoItemIdentifier instanceof ClassPKInfoItemIdentifier) {
			ClassPKInfoItemIdentifier classPKInfoItemIdentifier =
				(ClassPKInfoItemIdentifier)infoItemIdentifier;

			assetVocabulary = _assetVocabularyLocalService.fetchAssetVocabulary(
				classPKInfoItemIdentifier.getClassPK());
		}
		else {
			ERCInfoItemIdentifier ercInfoItemIdentifier =
				(ERCInfoItemIdentifier)infoItemIdentifier;

			assetVocabulary =
				_assetVocabularyLocalService.
					fetchAssetVocabularyByExternalReferenceCode(
						ercInfoItemIdentifier.getExternalReferenceCode(),
						groupId);
		}

		if (assetVocabulary == null) {
			throw new NoSuchInfoItemException(
				"Unable to get asset vocabulary with info item identifier " +
					infoItemIdentifier);
		}

		return assetVocabulary;
	}

	@Reference
	private AssetVocabularyLocalService _assetVocabularyLocalService;

}