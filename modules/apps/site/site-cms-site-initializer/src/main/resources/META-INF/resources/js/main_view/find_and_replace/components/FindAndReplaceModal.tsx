/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayModal, {useModal} from '@clayui/modal';
import {Observer} from '@clayui/modal/src/types';
import {Locale} from 'frontend-js-components-web';
import React, {useContext, useState} from 'react';

import {ISearchAssetObjectEntry} from '../../../common/types/AssetType';
import {StickerConfig} from '../../../common/types/StickerConfig';
import {
	FindAndReplaceContext,
	FindAndReplaceContextProvider,
	View,
} from '../contexts/FindAndReplaceContext';
import {Discard} from './Discard';
import {Loading} from './Loading';
import {NoMatches} from './NoMatches';
import {Setup} from './Setup';
import {Summary} from './Summary';

type Props = {
	availableLocales: Locale[];
	dataSetId: string;
	fdsItems: ISearchAssetObjectEntry[];
	search: string;
	stickerConfig: StickerConfig;
};

export default function Wrapper({
	availableLocales,
	dataSetId,
	fdsItems,
	search,
	stickerConfig,
}: Props) {
	const [visible, setVisible] = useState(true);

	const {observer, onClose: closeModal} = useModal({
		onClose: () => setVisible(false),
	});

	if (!visible) {
		return;
	}

	return (
		<FindAndReplaceContextProvider
			availableLocales={availableLocales}
			closeModal={closeModal}
			dataSetId={dataSetId}
			fdsItems={fdsItems}
			search={search}
			stickerConfig={stickerConfig}
		>
			<FindAndReplaceModal observer={observer} />
		</FindAndReplaceContextProvider>
	);
}

function FindAndReplaceModal({observer}: {observer: Observer}) {
	const {view} = useContext(FindAndReplaceContext);

	const size = getSize(view);
	const status = getStatus(view);

	return (
		<ClayModal
			center={size !== 'full-screen'}
			className="cms-find-and-replace-modal"
			disableAutoClose
			observer={observer}
			size={size}
			status={status}
		>
			{view === 'loading' && <Loading />}

			{view === 'no-matches' && <NoMatches />}

			{view === 'setup' && <Setup />}

			{view === 'summary' && <Summary />}

			{view === 'discard' && <Discard />}
		</ClayModal>
	);
}

function getSize(view: View) {
	if (view === 'summary') {
		return 'full-screen';
	}

	return undefined;
}

function getStatus(view: View) {
	if (view === 'no-matches' || view === 'discard') {
		return 'danger';
	}

	return undefined;
}
