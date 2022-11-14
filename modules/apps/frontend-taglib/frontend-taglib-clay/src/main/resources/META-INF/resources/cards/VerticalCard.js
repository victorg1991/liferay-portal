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

import {ClayCardWithInfo} from '@clayui/card';
import ClayIcon from '@clayui/icon';
import ClaySticker from '@clayui/sticker';
import React, {useCallback, useEffect, useMemo, useRef, useState} from 'react';

import {EVENT_MANAGEMENT_TOOLBAR_TOGGLE_ALL_ITEMS} from '../constants';
import getDataAttributes from '../get_data_attributes';
import normalizeDropdownItems from '../normalize_dropdown_items';

export default function VerticalCard({
	actions,
	additionalProps: _additionalProps,
	componentId: _componentId,
	cssClass,
	description,
	disabled,
	displayType,
	flushHorizontal,
	flushVertical,
	href,
	imageAlt,
	imageSrc,
	inputName = '',
	inputValue = '',
	labels = [],
	locale: _locale,
	portletId: _portletId,
	portletNamespace: _portletNamespace,
	selectable,
	selected: initialSelected,
	showSticker,
	stickerCssClass,
	stickerIcon,
	stickerImageAlt,
	stickerImageSrc,
	stickerLabel,
	stickerShape,
	stickerStyle,
	symbol,
	title,
	...otherProps
}) {
	const normalizedActions = useMemo(() => normalizeDropdownItems(actions), [
		actions,
	]);

	const cardRef = useRef();

	const [selected, setSelected] = useState(initialSelected);

	const stickerProps = useMemo(() => {
		const stickerProps = {
			children: stickerLabel,
			className: stickerCssClass,
			displayType: stickerStyle,
			shape: stickerShape,
		};

		if (stickerImageSrc) {
			stickerProps.children = (
				<ClaySticker.Image
					alt={stickerImageAlt}
					src={stickerImageSrc}
				/>
			);
		}
		else if (stickerIcon) {
			stickerProps.children = <ClayIcon symbol={stickerIcon} />;
		}

		return stickerProps;
	}, [
		stickerCssClass,
		stickerIcon,
		stickerImageAlt,
		stickerImageSrc,
		stickerLabel,
		stickerShape,
		stickerStyle,
	]);

	const handleToggleAllItems = useCallback(
		({checked}) => {
			setSelected(checked);
		},
		[setSelected]
	);

	useEffect(() => {
		Liferay.on(
			EVENT_MANAGEMENT_TOOLBAR_TOGGLE_ALL_ITEMS,
			handleToggleAllItems
		);

		return () => {
			Liferay.detach(
				EVENT_MANAGEMENT_TOOLBAR_TOGGLE_ALL_ITEMS,
				handleToggleAllItems
			);
		};
	}, [handleToggleAllItems]);

	useEffect(() => {
		const checkSelection = () => {
			const searchContainerContent = document.querySelector(
				'.searchcontainer-content'
			);

			if (!searchContainerContent?.dataset.selectionData) {
				return;
			}

			const selectionData = JSON.parse(
				searchContainerContent.dataset.selectionData
			);

			const state = selectionData.find(
				(data) => data.value === inputValue
			);

			if (state?.checked) {
				setSelected(true);
			}
		};

		// Here the AUI component may be loaded or not, in practice it was only loaded when the last card loaded
		// this is why we check if the searchcontainer is loaded, otherwise we listen for the event

		const searchContainer = document.querySelector('.searchcontainer')?.id;

		if (searchContainer) {
			checkSelection();
		}
		else {
			Liferay.on('search-container:registered', () => {
				requestAnimationFrame(checkSelection);
			});
		}

		checkSelection();

		return () =>
			Liferay.detach('search-container:registered', checkSelection);
	}, [inputValue]);

	return (
		<ClayCardWithInfo
			actions={normalizedActions}
			checkboxProps={{
				name: inputName ?? '',
				value: inputValue ?? '',
			}}
			className={cssClass}
			description={description}
			disabled={disabled}
			displayType={displayType}
			flushHorizontal={flushHorizontal}
			flushVertical={flushVertical}
			href={href}
			imgProps={imageSrc && {alt: imageAlt, src: imageSrc}}
			labels={labels?.map(
				({
					closeable: _closeable,
					data,
					label,
					style: _style,
					...rest
				}) => {
					const dataAttributes = getDataAttributes(data);

					return {
						value: label,
						...dataAttributes,
						...rest,
					};
				}
			)}
			onSelectChange={
				selectable
					? (selected) => {
							setSelected(selected);
					  }
					: null
			}
			ref={cardRef}
			selectable={selectable}
			selected={selected}
			stickerProps={showSticker ? stickerProps : null}
			symbol={symbol}
			title={title}
			{...otherProps}
		/>
	);
}
