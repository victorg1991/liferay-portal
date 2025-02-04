/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayAlert from '@clayui/alert';
import {TreeView as ClayTreeView} from '@clayui/core';
import ClayIcon from '@clayui/icon';
import classNames from 'classnames';
import React, {useCallback, useEffect, useMemo, useState} from 'react';

import {fromControlsId} from '../../../../../app/components/layout_data_items/Collection';
import {ITEM_ACTIVATION_ORIGINS} from '../../../../../app/config/constants/itemActivationOrigins';
import {ITEM_TYPES} from '../../../../../app/config/constants/itemTypes';
import {
	ARROW_DOWN_KEY_CODE,
	ARROW_LEFT_KEY_CODE,
	ARROW_RIGHT_KEY_CODE,
	ARROW_UP_KEY_CODE,
	ENTER_KEY_CODE,
	SPACE_KEY_CODE,
} from '../../../../../app/config/constants/keyboardCodes';
import {LAYOUT_DATA_ITEM_TYPES} from '../../../../../app/config/constants/layoutDataItemTypes';
import {LAYOUT_TYPES} from '../../../../../app/config/constants/layoutTypes';
import {config} from '../../../../../app/config/index';
import {
	useHoverItem,
	useHoveredItemId,
	useSelectItem, useActiveItemId,
} from '../../../../../app/contexts/ControlsContext';

import {
	useDispatch,
	useSelector,
	useSelectorRef,
} from '../../../../../app/contexts/StoreContext';
import selectCanUpdateEditables from '../../../../../app/selectors/selectCanUpdateEditables';
import selectCanUpdateItemConfiguration from '../../../../../app/selectors/selectCanUpdateItemConfiguration';
import selectCanUpdatePageStructure from '../../../../../app/selectors/selectCanUpdatePageStructure';
import {DragAndDropContextProvider} from '../../../../../app/utils/drag_and_drop/useDragAndDrop';
import getFirstControlsId from '../../../../../app/utils/getFirstControlsId';
import usePageContents from '../../../../../app/utils/usePageContents';
import StructureTreeNode from './StructureTreeNode';
import StructureTreeNodeActions from './StructureTreeNodeActions';
import VisibilityButton from './VisibilityButton';
import visit from './visit';

export default function StructureTreeContent({expandedKeys, setExpandedKeys}) {
	const activeItemId = useActiveItemId();
	const canUpdateEditables = useSelector(selectCanUpdateEditables);
	const canUpdateItemConfiguration = useSelector(
		selectCanUpdateItemConfiguration
	);
	const fragmentEntryLinks = useSelector((state) => state.fragmentEntryLinks);
	const layoutData = useSelector((state) => state.layoutData);

	const pageContents = usePageContents();
	const hoverItem = useHoverItem();
	const hoveredItemId = useHoveredItemId();
	const selectItem = useSelectItem();

	const mappingFields = useSelector((state) => state.mappingFields);
	const masterLayoutData = useSelector(
		(state) => state.masterLayout?.masterLayoutData
	);

	const restrictedItemIds = useSelector((state) => state.restrictedItemIds);

	const selectedViewportSize = useSelector(
		(state) => state.selectedViewportSize
	);
	const layoutDataRef = useSelectorRef((store) => store.layoutData);

	const [dragAndDropHoveredItemId, setDragAndDropHoveredItemId] = useState(
		null
	);

	const [editingNodeId, setEditingNodeId] = useState(null);

	const isMasterPage = config.layoutType === LAYOUT_TYPES.master;

	const data = masterLayoutData || layoutData;

	const onHoverNode = useCallback((itemId) => {
		setDragAndDropHoveredItemId(itemId);
	}, []);

	const nodes = useMemo(
		() =>
			visit(data.items[data.rootItems.main], data.items, {
				activeItemId,
				canUpdateEditables,
				canUpdateItemConfiguration,
				editingNodeId,
				fragmentEntryLinks,
				hoveredItemId,
				isMasterPage,
				layoutData,
				layoutDataRef,
				mappingFields,
				masterLayoutData,
				onHoverNode,
				pageContents,
				restrictedItemIds,
				selectedViewportSize,
			}).children,

		[
			activeItemId,
			canUpdateEditables,
			canUpdateItemConfiguration,
			data.items,
			data.rootItems.main,
			editingNodeId,
			fragmentEntryLinks,
			hoveredItemId,
			isMasterPage,
			layoutData,
			layoutDataRef,
			mappingFields,
			masterLayoutData,
			pageContents,
			restrictedItemIds,
			onHoverNode,
			selectedViewportSize,
		]
	);

	const setExpandedNodes = (expandedNodes) => {
		setExpandedKeys(Array.from(expandedNodes));
	};

	const handleNodeFocus = () => {
		const focusedItem = document.activeElement?.querySelector(
			'[data-item-id]'
		);

		if (focusedItem) {
			hoverItem(focusedItem.dataset.itemId);
		}
	};

	const handleButtonsKeyDown = (event) => {
		if (
			[
				ARROW_DOWN_KEY_CODE,
				ARROW_LEFT_KEY_CODE,
				ARROW_RIGHT_KEY_CODE,
				ARROW_UP_KEY_CODE,
			].includes(event.nativeEvent.code)
		) {
			document.activeElement
				.closest('.page-editor__page-structure__clay-tree-node')
				?.focus();
		}
		else {
			event.stopPropagation();
		}
	};

	const ItemActions = ({item}) => {
		const activeItemId = useActiveItemId();
		const dispatch = useDispatch();
		const hoveredItemId = useHoveredItemId();
		const isSelected = item.id === fromControlsId(activeItemId);
		const isHovered = item.id === fromControlsId(hoveredItemId);
		const canUpdatePageStructure = useSelector(
			selectCanUpdatePageStructure
		);
		const showOptions =
			canUpdatePageStructure &&
			item.itemType !== ITEM_TYPES.editable &&
			item.type !== LAYOUT_DATA_ITEM_TYPES.dropZone &&
			item.activable &&
			!item.isMasterItem;

		if (item.editingName) {
			return null;
		}

		return (
			<div
				className={classNames('autofit-row w-auto', {
					'page-editor__page-structure__tree-node__buttons--hidden':
						item.hidden || item.hiddenAncestor,
				})}
				onFocus={(event) => event.stopPropagation()}
				onKeyDown={handleButtonsKeyDown}
			>
				{(item.hidable || item.hidden) && (
					<VisibilityButton
						className="ml-0"
						dispatch={dispatch}
						node={item}
						selectedViewportSize={selectedViewportSize}
						visible={item.hidden || isHovered || isSelected}
					/>
				)}

				{showOptions && (
					<StructureTreeNodeActions
						item={item}
						setEditingNodeId={setEditingNodeId}
						visible={item.hidden || isHovered || isSelected}
					/>
				)}
			</div>
		);
	};

	useEffect(() => {
		if (dragAndDropHoveredItemId) {
			setExpandedKeys((previousExpanedKeys) => [
				...new Set([
					...previousExpanedKeys,
					...[dragAndDropHoveredItemId],
				]),
			]);
		}
	}, [dragAndDropHoveredItemId, setExpandedKeys]);

	const onKeyDown = (event, item) => {
		const {code} = event.nativeEvent;

		if (![ENTER_KEY_CODE, SPACE_KEY_CODE].includes(code)) {
			return;
		}

		const itemId = getFirstControlsId({
			item,
			layoutData: layoutDataRef.current,
		});

		if (item.activable) {
			selectItem(itemId, {
				itemType: item.itemType,
				origin: ITEM_ACTIVATION_ORIGINS.sidebar,
			});

			hoverItem(null);
		}
	};

	return (
		<div
			className="overflow-auto page-editor__page-structure__structure-tree pt-4"
			onFocus={handleNodeFocus}
		>
			{!nodes.length && (
				<ClayAlert
					aria-live="polite"
					displayType="info"
					title={Liferay.Language.get('info')}
				>
					{Liferay.Language.get('there-is-no-content-on-this-page')}
				</ClayAlert>
			)}

			<DragAndDropContextProvider>
				<ClayTreeView
					displayType="light"
					expandDoubleClick={false}
					expandedKeys={new Set(expandedKeys)}
					expanderIcons={{
						close: <ClayIcon symbol="hr" />,
						open: <ClayIcon symbol="plus" />,
					}}
					items={nodes}
					onExpandedChange={setExpandedNodes}
					onItemsChange={() => {}}
					showExpanderOnHover={false}
				>
					{(item) => (
						<ClayTreeView.Item
							actions={<ItemActions item={item} />}
							active={item.active && item.activable}
						>
							<ClayTreeView.ItemStack
								className={classNames(
									'page-editor__page-structure__clay-tree-node',
									{
										'page-editor__page-structure__clay-tree-node--active':
											item.active && item.activable,
										'page-editor__page-structure__clay-tree-node--hovered':
										item.hovered,
										'page-editor__page-structure__clay-tree-node--mapped':
										item.mapped,
										'page-editor__page-structure__clay-tree-node--master-item':
										item.isMasterItem,
									}
								)}
								data-qa-id={item.tooltipTitle}
								data-title={
									item.isMasterItem || !item.activable
										? ''
										: item.tooltipTitle
								}
								data-tooltip-align={
									item.isMasterItem || !item.activable
										? ''
										: 'right'
								}
								onKeyDown={(event) => onKeyDown(event, item)}
								onMouseLeave={(event) => {
									if (item.hovered) {
										event.stopPropagation();
										hoverItem(null);
									}
								}}
								onMouseOver={(event) => {
									event.stopPropagation();
									hoverItem(item.id);
								}}
							>
								<span className="sr-only">{item.name}</span>

								<StructureTreeNode
									node={item}
									setEditingNodeId={setEditingNodeId}
								/>
							</ClayTreeView.ItemStack>

							<ClayTreeView.Group items={item.children}>
								{(item) => (
									<ClayTreeView.Item
										actions={<ItemActions item={item} />}
									>
										<span className="sr-only">
											{item.name}
										</span>

										<StructureTreeNode
											node={item}
											setEditingNodeId={setEditingNodeId}
										/>
									</ClayTreeView.Item>
								)}
							</ClayTreeView.Group>
						</ClayTreeView.Item>
					)}
				</ClayTreeView>
			</DragAndDropContextProvider>
		</div>
	);
}
