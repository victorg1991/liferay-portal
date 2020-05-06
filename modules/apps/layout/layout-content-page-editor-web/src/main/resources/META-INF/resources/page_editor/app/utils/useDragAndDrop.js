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

import {throttle} from 'frontend-js-web';
import React, {
	useCallback,
	useContext,
	useEffect,
	useMemo,
	useReducer,
	useRef,
} from 'react';
import {useDrag, useDrop} from 'react-dnd';
import {getEmptyImage} from 'react-dnd-html5-backend';

import {LAYOUT_DATA_ITEM_TYPES} from '../config/constants/layoutDataItemTypes';

const LAYOUT_DATA_ALLOWED_CHILDREN_TYPES = {
	[LAYOUT_DATA_ITEM_TYPES.root]: [
		LAYOUT_DATA_ITEM_TYPES.collection,
		LAYOUT_DATA_ITEM_TYPES.dropZone,
		LAYOUT_DATA_ITEM_TYPES.container,
		LAYOUT_DATA_ITEM_TYPES.row,
		LAYOUT_DATA_ITEM_TYPES.fragment,
	],
	[LAYOUT_DATA_ITEM_TYPES.collection]: [],
	[LAYOUT_DATA_ITEM_TYPES.collectionItem]: [
		LAYOUT_DATA_ITEM_TYPES.row,
		LAYOUT_DATA_ITEM_TYPES.fragment,
	],
	[LAYOUT_DATA_ITEM_TYPES.dropZone]: [],
	[LAYOUT_DATA_ITEM_TYPES.container]: [
		LAYOUT_DATA_ITEM_TYPES.collection,
		LAYOUT_DATA_ITEM_TYPES.dropZone,
		LAYOUT_DATA_ITEM_TYPES.row,
		LAYOUT_DATA_ITEM_TYPES.fragment,
	],
	[LAYOUT_DATA_ITEM_TYPES.row]: [LAYOUT_DATA_ITEM_TYPES.column],
	[LAYOUT_DATA_ITEM_TYPES.column]: [
		LAYOUT_DATA_ITEM_TYPES.collection,
		LAYOUT_DATA_ITEM_TYPES.dropZone,
		LAYOUT_DATA_ITEM_TYPES.row,
		LAYOUT_DATA_ITEM_TYPES.fragment,
	],
	[LAYOUT_DATA_ITEM_TYPES.fragment]: [],
	[LAYOUT_DATA_ITEM_TYPES.fragmentDropZone]: [
		LAYOUT_DATA_ITEM_TYPES.collection,
		LAYOUT_DATA_ITEM_TYPES.dropZone,
		LAYOUT_DATA_ITEM_TYPES.container,
		LAYOUT_DATA_ITEM_TYPES.row,
		LAYOUT_DATA_ITEM_TYPES.fragment,
	],
};

const BORDER_SIZE = 40;

export const TARGET_POSITION = {
	BOTTOM: 'bottom',
	MIDDLE: 'middle',
	TOP: 'top',
};

const DRAG_DROP_TARGET_TYPE = {
	DRAGGING_TO_ITSELF: 'itself',
	ELEVATE: 'elevate',
	INITIAL: 'initial',
	INSIDE: 'inside',
};

const initialDragDrop = {
	dispatch: null,

	state: {

		/**
		 * Item that is being dragged
		 */
		dropItem: null,

		/**
		 * Target item where the item is being dragged true.
		 * If elevate is true, dropTargetItem is the sibling
		 * of drop item, otherwise is it's parent.
		 */
		dropTargetItem: null,

		/**
		 * When false, an "invalid drop" advise should be shown
		 * to users.
		 */
		droppable: true,

		/**
		 * If true, dropTargetItem is the sibling of dropItem
		 * and targetPosition determines the item index.
		 */
		elevate: false,

		/**
		 * Vertical position relative to dropTargetItem
		 * (bottom, middle, top)
		 */
		targetPositionWithMiddle: null,

		/**
		 * Vertical position relative to dropTargetItem
		 * (bottom, top)
		 */
		targetPositionWithoutMiddle: null,

		/**
		 * Source of the Drag and Drop status
		 * (where the drag and drop status have been generated)
		 */
		type: DRAG_DROP_TARGET_TYPE.INITIAL,
	},
};

const DragAndDropContext = React.createContext(initialDragDrop);

export function useDragItem(sourceItem, onDragEnd) {
	const getSourceItem = useCallback(() => sourceItem, [sourceItem]);
	const {dispatch, layoutData, state} = useContext(DragAndDropContext);
	const sourceRef = useRef(null);

	const [{isDraggingSource}, handlerRef, previewRef] = useDrag({
		collect: (monitor) => ({
			isDraggingSource: monitor.isDragging(),
		}),

		end() {
			computeDrop({dispatch, layoutData, onDragEnd, state});
		},

		item: {
			getSourceItem,
			id: sourceItem.itemId,
			type: sourceItem.type,
		},
	});

	useEffect(() => {
		previewRef(getEmptyImage(), {captureDraggingState: true});
	}, [previewRef]);

	return {
		handlerRef,
		isDraggingSource,
		sourceRef,
	};
}

export function useDragSymbol({label, type}, onDragEnd) {
	const sourceItem = useMemo(() => ({isSymbol: true, itemId: label, type}), [
		label,
		type,
	]);

	const {handlerRef, isDraggingSource, sourceRef} = useDragItem(
		sourceItem,
		onDragEnd
	);

	const symbolRef = (element) => {
		sourceRef.current = element;
		handlerRef(element);
	};

	return {
		isDraggingSource,
		sourceRef: symbolRef,
	};
}

export function useDropTarget(targetItem) {
	const {dispatch, layoutData, state} = useContext(DragAndDropContext);
	const isOverTarget = state.dropTargetItem === targetItem;
	const targetRef = useRef(null);

	const [, setDropTargetRef] = useDrop({
		accept: Object.values(LAYOUT_DATA_ITEM_TYPES),

		hover({getSourceItem}, monitor) {
			computeHover({
				dispatch,
				layoutData,
				monitor,
				sourceItem: getSourceItem(),
				targetItem,
				targetRef,
			});
		},
	});

	useEffect(() => {
		if (!isOverTarget) {
			dispatch(initialDragDrop.state);
		}
	}, [dispatch, isOverTarget]);

	const setTargetRef = useCallback(
		(element) => {
			setDropTargetRef(element);

			targetRef.current = element;
		},
		[setDropTargetRef]
	);

	return {
		canDropOverTarget: state.droppable,
		isOverTarget,
		sourceItem: state.dropItem,
		targetPosition: state.targetPositionWithMiddle,
		targetRef: setTargetRef,
	};
}

export const DragAndDropContextProvider = ({children, layoutData}) => {
	const [state, reducerDispatch] = useReducer(
		(state, nextState) =>
			Object.keys(state).some((key) => state[key] !== nextState[key])
				? nextState
				: state,
		initialDragDrop.state
	);

	const dispatch = useMemo(() => {
		return throttle(reducerDispatch, 100);
	}, [reducerDispatch]);

	return (
		<DragAndDropContext.Provider value={{dispatch, layoutData, state}}>
			{children}
		</DragAndDropContext.Provider>
	);
};

function computeDrop({dispatch, layoutData, onDragEnd, state}) {
	if (state.droppable && state.dropItem && state.dropTargetItem) {
		if (state.elevate) {
			const parentItem = layoutData.items[state.dropTargetItem.parentId];

			const siblingPosition = parentItem.children.indexOf(
				state.dropTargetItem.itemId
			);

			const position = Math.min(
				parentItem.children.includes(state.dropItem.itemId)
					? parentItem.children.length - 1
					: parentItem.children.length,
				state.targetPositionWithoutMiddle === TARGET_POSITION.BOTTOM
					? siblingPosition + 1
					: siblingPosition
			);

			onDragEnd(parentItem.itemId, position);
		}
		else {
			onDragEnd(
				state.dropTargetItem.itemId,
				state.dropTargetItem.children.length
			);
		}
	}

	dispatch(initialDragDrop.state);
}

function computeHover({
	dispatch,
	layoutData,
	monitor,
	siblingItem = null,
	sourceItem,
	targetItem,
	targetRef,
}) {

	// Not dragging over direct child
	// We do not want to alter state here,
	// as dnd generate extra hover events when
	// items are being dragged over nested children

	if (!monitor.isOver({shallow: true})) {
		return;
	}

	// Dragging over itself or a descendant

	if (dropTargetIsAncestor(sourceItem, layoutData, targetItem.itemId)) {
		return dispatch({
			...initialDragDrop.state,
			type: DRAG_DROP_TARGET_TYPE.DRAGGING_TO_ITSELF,
		});
	}

	// Apparently valid drag, calculate vertical position and
	// nesting validation

	const [
		targetPositionWithMiddle,
		targetPositionWithoutMiddle,
	] = getTargetPosition(monitor, targetRef);

	const isAllowedChild = LAYOUT_DATA_ALLOWED_CHILDREN_TYPES[
		targetItem.type
	].includes(sourceItem.type);

	// Drop inside target

	if (targetPositionWithMiddle === TARGET_POSITION.MIDDLE) {
		return dispatch({
			dropItem: sourceItem,
			dropTargetItem: targetItem,
			droppable: isAllowedChild,
			elevate: null,
			targetPositionWithMiddle,
			targetPositionWithoutMiddle,
			type: DRAG_DROP_TARGET_TYPE.INSIDE,
		});
	}

	// Valid elevation
	// dropItem should be child of dropTargetItem
	// dropItem should be sibling of siblingItem

	if (siblingItem && isAllowedChild) {
		return dispatch({
			dropItem: sourceItem,
			dropTargetItem: siblingItem,
			droppable: true,
			elevate: true,
			targetPositionWithMiddle,
			targetPositionWithoutMiddle,
			type: DRAG_DROP_TARGET_TYPE.ELEVATE,
		});
	}

	// Try to elevate to some valid ancestor
	// using dropTargetItem parent as target and
	// dropTargetItem as sibling

	if (targetItem.parentId) {
		return computeHover({
			dispatch,
			layoutData,
			monitor,
			siblingItem: targetItem,
			sourceItem,
			targetItem: layoutData.items[targetItem.parentId],
			targetRef,
		});
	}
}

function dropTargetIsAncestor(dropItem, layoutData, dropTargetId) {
	const dropTarget = layoutData.items[dropTargetId];

	if (dropTarget) {
		return dropTarget.itemId !== dropItem.itemId
			? dropTargetIsAncestor(dropItem, layoutData, dropTarget.parentId)
			: true;
	}

	return false;
}

/**
 * When dragging downwards, only move when the cursor is below 50%
 * When dragging upwards, only move when the cursor is above 50%
 *
 * Checking if is dragging over middle:
 * The calculation to identify when the mouse position is over the middle of the element,
 * the middle region is evaluated according to BORDER_SIZE
 */
function getTargetPosition(monitor, targetRef) {
	if (!targetRef.current) {
		return [null, null];
	}

	const hoverBoundingRect = targetRef.current.getBoundingClientRect();

	const clientOffsetY = monitor.getClientOffset().y;
	const hoverMiddleY = hoverBoundingRect.top + hoverBoundingRect.height / 2;

	const targetPositionWithoutMiddle =
		clientOffsetY < hoverMiddleY
			? TARGET_POSITION.TOP
			: TARGET_POSITION.BOTTOM;

	const targetPositionWithMiddle =
		clientOffsetY < hoverBoundingRect.bottom - BORDER_SIZE &&
		clientOffsetY > hoverBoundingRect.top + BORDER_SIZE
			? TARGET_POSITION.MIDDLE
			: targetPositionWithoutMiddle;

	return [targetPositionWithMiddle, targetPositionWithoutMiddle];
}
