/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import useKeyboardDragAndDrop from './useKeyboardDragAndDrop';
import usePointerDragAndDrop from './usePointerDragAndDrop';

import './useDragAndDrop.scss';

export type DropPosition = 'bottom' | 'middle' | 'top' | null;

interface Props<T> {
	allowMiddleDrop?: boolean;
	dragHandlerRef: React.RefObject<HTMLElement>;
	dropItemRef: React.RefObject<HTMLElement>;
	item: T;
	itemIndex: number;
	items: T[];
	onDrop: (
		items: T[],
		targetId: string,
		position: DropPosition,
		sourceId: string
	) => void;
}

export default function useDragAndDrop<T extends {id: string; name: string}>({
	allowMiddleDrop = false,
	dragHandlerRef,
	dropItemRef,
	item,
	itemIndex,
	items,
	onDrop,
}: Props<T>) {
	const {
		isPointerDragging,
		isPointerDropBottomPosition,
		isPointerDropMiddlePosition,
		isPointerDropTopPosition,
	} = usePointerDragAndDrop<T>({
		allowMiddleDrop,
		dragHandlerRef,
		dropItemRef,
		items,
		onDrop,
		targetItem: item,
	});

	const {
		handleKeyboardDragAndDrop,
		isKeyboardDragging,
		isKeyboardDropBottomPosition,
		isKeyboardDropMiddlePosition,
		isKeyboardDropTopPosition,
	} = useKeyboardDragAndDrop<T>({
		allowMiddleDrop,
		draggedItem: item,
		draggedItemIndex: itemIndex,
		items,
		onDrop,
	});

	return {
		handleKeyboardDragAndDrop,
		isDragging: isPointerDragging || isKeyboardDragging,
		isDropBottomPosition:
			isPointerDropBottomPosition || isKeyboardDropBottomPosition,
		isDropMiddlePosition:
			isPointerDropMiddlePosition || isKeyboardDropMiddlePosition,
		isDropTopPosition:
			isPointerDropTopPosition || isKeyboardDropTopPosition,
		isKeyboardDragging,
	};
}
