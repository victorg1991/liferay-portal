/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {useCallback} from 'react';

import {EDITABLE_FRAGMENT_ENTRY_PROCESSOR} from '../config/constants/editableFragmentEntryProcessor';
import {
	useEditableProcessorUniqueId,
	useEditorInstance,
} from '../contexts/EditableProcessorContext';
import {useDispatch, useSelector} from '../contexts/StoreContext';
import selectLanguageId from '../selectors/selectLanguageId';
import updateEditableValues from '../thunks/updateEditableValues';
import {setIn} from './setIn';

export default function useSaveEditableChanges() {
	const dispatch = useDispatch();
	const editor = useEditorInstance();
	const editableUniqueId = useEditableProcessorUniqueId();
	const fragmentEntryLinks = useSelector((state) => state.fragmentEntryLinks);
	const languageId = useSelector(selectLanguageId);

	return useCallback(async () => {
		if (!editor) {
			return;
		}

		const [fragmentEntryLinkId, editableId] = extractIds(editableUniqueId);

		const fragment = fragmentEntryLinks[fragmentEntryLinkId];

		const value = editor.get('nativeEditor').getData();

		return dispatch(
			updateEditableValues({
				editableValues: setIn(
					fragment.editableValues,
					[EDITABLE_FRAGMENT_ENTRY_PROCESSOR, editableId, languageId],
					value
				),
				fragmentEntryLinkId,
			})
		);
	}, [dispatch, editableUniqueId, editor, fragmentEntryLinks, languageId]);
}

function extractIds(editableUniqueId) {
	const separatorIndex = editableUniqueId.indexOf('-');
	const fragmentEntryLinkId = editableUniqueId.slice(0, separatorIndex);
	const editableId = editableUniqueId.slice(separatorIndex + 1);

	return [fragmentEntryLinkId, editableId];
}
