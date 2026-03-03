/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton, {ClayButtonWithIcon} from '@clayui/button';
import ClayIcon from '@clayui/icon';
import ClayList from '@clayui/list';
import ClayModal from '@clayui/modal';
import ClaySticker from '@clayui/sticker';
import {
	EConfigInURLBehavior,
	FrontendDataSet,
} from '@liferay/frontend-data-set-web';
import classNames from 'classnames';
import {openToast} from 'frontend-js-components-web';
import {sub} from 'frontend-js-web';
import React, {useContext, useEffect, useRef, useState} from 'react';

import AsyncButton from '../../../structure_builder/components/AsyncButton';
import {
	FindAndReplaceContext,
	ReplaceItem,
	useDiscard,
} from '../contexts/FindAndReplaceContext';
import FindAndReplaceService from '../services/FindAndReplaceService';

export function Summary() {
	const {closeModal, dataSetId, items, replacement, search, setView} =
		useContext(FindAndReplaceContext);

	const discard = useDiscard();

	function applyChanges() {
		if (!items) {
			return;
		}

		FindAndReplaceService.performBulkReplace({
			dataSetId,
			items,
			replacement,
			search,
		});

		closeModal();
	}

	return (
		<>
			<ClayModal.Header
				closeButtonAriaLabel={Liferay.Language.get('close')}
			>
				<div className="align-items-center c-gap-3 d-flex">
					<ClayButtonWithIcon
						aria-label={Liferay.Language.get('back')}
						borderless
						className="text-secondary"
						displayType="unstyled"
						monospaced
						onClick={() => setView('setup')}
						size="sm"
						symbol="angle-left"
					/>

					{Liferay.Language.get('review-changes')}
				</div>
			</ClayModal.Header>

			<ClayModal.Body>
				<FrontendDataSet
					configInURLBehavior={EConfigInURLBehavior.OFF}
					id="findAndReplaceFds"
					items={items ?? []}
					pagination={{
						deltas: [{label: 10}, {label: 20}],
						initialDelta: 10,
					}}
					showPagination
					style="fluid"
					views={[
						{
							component: ({items}: {items: ReplaceItem[]}) => (
								<ClayList>
									{items.map((item) => (
										<ListItem item={item} key={item.id} />
									))}
								</ClayList>
							),
						},
					]}
				/>
			</ClayModal.Body>

			<ClayModal.Footer
				last={
					<ClayButton.Group spaced>
						<ClayButton displayType="secondary" onClick={discard}>
							{Liferay.Language.get('cancel')}
						</ClayButton>

						<ClayButton onClick={applyChanges}>
							{Liferay.Language.get(
								'apply-changes-to-all-assets'
							)}
						</ClayButton>
					</ClayButton.Group>
				}
			/>
		</>
	);
}

function ListItem({item}: {item: ReplaceItem}) {
	const {removeItem, replacement, search} = useContext(FindAndReplaceContext);

	const [status, setStatus] = useState<'applied' | 'applying' | 'idle'>(
		'idle'
	);

	const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

	const title = getTitle(item);

	useEffect(() => {
		return () => {
			if (timeoutRef.current) {
				clearTimeout(timeoutRef.current);
			}
		};
	}, []);

	async function applySingleItem() {
		setStatus('applying');

		const {error} = await FindAndReplaceService.performSingleReplace({
			item,
			replacement,
			search,
		});

		if (error) {
			setStatus('idle');

			openToast({
				message: Liferay.Language.get('an-unexpected-error-occurred'),
				type: 'danger',
			});

			return;
		}

		setStatus('applied');

		openToast({
			message: sub(Liferay.Language.get('changes-applied-to-x'), title),
			type: 'success',
		});

		timeoutRef.current = setTimeout(() => {
			removeItem(item.id);
		}, 5000);
	}

	return (
		<ClayList.Item
			className={classNames('align-items-center', {
				'list-item__success': status === 'applied',
			})}
			flex
		>
			<ClayList.ItemField>
				<ClaySticker
					className={classNames('inline-item', item.stickerClassName)}
				>
					<ClayIcon symbol={item.stickerSymbol} />
				</ClaySticker>
			</ClayList.ItemField>

			<ClayList.ItemField expand>
				<ClayList.ItemTitle>{title}</ClayList.ItemTitle>

				<ClayList.ItemText>
					{sub(Liferay.Language.get('x-changes'), item.fields.length)}
				</ClayList.ItemText>
			</ClayList.ItemField>

			<ClayList.ItemField>
				<AsyncButton
					disabled={status === 'applied'}
					displayType="secondary"
					label={Liferay.Language.get('apply-changes')}
					onClick={applySingleItem}
					status={status === 'applying' ? 'loading' : 'idle'}
				/>
			</ClayList.ItemField>

			<ClayList.ItemField>
				<ClayButtonWithIcon
					aria-label={sub(
						Liferay.Language.get('discard-changes-to-x'),
						title
					)}
					borderless
					displayType="secondary"
					monospaced
					onClick={() => removeItem(item.id)}
					size="sm"
					symbol="times-circle"
				/>
			</ClayList.ItemField>
		</ClayList.Item>
	);
}

function getTitle(item: ReplaceItem) {
	const field = item.fields.find((field) => field.name === 'title');

	return field?.value_i18n![Liferay.ThemeDisplay.getDefaultLanguageId()];
}
