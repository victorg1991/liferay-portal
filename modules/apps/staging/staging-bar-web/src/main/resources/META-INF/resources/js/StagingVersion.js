/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {fetch, openConfirmModal, openToast, openWindow} from 'frontend-js-web';

const MAP_CMD_REVISION = {
	redo: 'redo_layout_revision',
	undo: 'undo_layout_revision',
};

const MAP_TEXT_REVISION = {
	redo: Liferay.Language.get(
		'are-you-sure-you-want-to-redo-your-last-changes'
	),
	undo: Liferay.Language.get(
		'are-you-sure-you-want-to-undo-your-last-changes'
	),
};

export default function stagingVersion({
	layoutRevisionStatusURL,
	markAsReadyForPublicationURL,
	namespace,
}){
	const StagingBar = Liferay.StagingBar;
	let eventHandles = [];

	const redo = document.getElementById(`${namespace}redoLink`);
	redo.addEventListener('click', onRedoRevisionChange);
	eventHandles.push({ detach: () => redo.removeEventListener('click', onRevisionChange)});

	const undo = document.getElementById(`${namespace}undoLink`);
	undo.addEventListener('click', onUndoRevisionChange);
	eventHandles.push({ detach: () => undo.removeEventListener('click', onRevisionChange)});

	const submit = document.getElementById(`${namespace}submit`);
	submit.addEventListener('click', onSubmit);
	eventHandles.push({ detach: () => submit.removeEventListener('click', onSubmit)});

	const viewHistory = document.getElementById(`${namespace}viewHistoryLink`);
	viewHistory.addEventListener('click', onViewHistory);
	eventHandles.push({ detach: () => viewHistory.removeEventListener('click', onViewHistory)});

	const layoutRevisionDetails = document.getElementById(
		`${namespace}layoutRevisionDetails`);

	if (layoutRevisionDetails) {
		eventHandles.push(
			Liferay.after('updatedLayout', () => {
				fetch(markAsReadyForPublicationURL)
					.then((response) => response.text())
					.then((response) => {

						layoutRevisionDetails.innerHTML = response;

						Liferay.fire('updatedStatus');
					})
					.catch(() => {
						layoutRevisionDetails.innerHTML =
							Liferay.Language.get(
								'there-was-an-unexpected-error.-please-refresh-the-current-page');
					});
			})
		);
	}

	const layoutRevisionStatus = document.getElementById(
		`${namespace}layoutRevisionStatus`);

	if (layoutRevisionStatus) {
		Liferay.after('updatedStatus', () => {
			fetch(layoutRevisionStatusURL)
				.then((response) => response.text())
				.then((response) => {

					layoutRevisionStatus.innerHTML = response;
				})
				.catch(() => {
					layoutRevisionStatus.innerHTML = Liferay.Language.get(
						'there-was-an-unexpected-error.-please-refresh-the-current-page');
				});
		});
	}

	const onUndoRevisionChange = (event) => {

		const cmd = MAP_CMD_REVISION["undo"];
		const confirmText = MAP_TEXT_REVISION["undo"];

		openConfirmModal({
			message: confirmText,
			onConfirm: (isConfirmed) => {
				if (isConfirmed) {
					updateRevision(
						cmd,
						event.layoutRevisionId,
						event.layoutSetBranchId
					);
				}
			},
		});
	};

	const onRedoRevisionChange = (event) => {

		const cmd = MAP_CMD_REVISION["redo"];
		const confirmText = MAP_TEXT_REVISION["redo"];

		openConfirmModal({
			message: confirmText,
			onConfirm: (isConfirmed) => {
				if (isConfirmed) {
					updateRevision(
						cmd,
						event.layoutRevisionId,
						event.layoutSetBranchId
					);
				}
			},
		});
	};

	const onSubmit = (event) => {

		const layoutRevisionDetails = document.getElementById(`${namespace}layoutRevisionDetails`);

		const layoutRevisionInfo = layoutRevisionDetails.querySelector('.layout-revision-info');

		if (layoutRevisionInfo) {
			layoutRevisionInfo.classList.add('loading');
		}

		const submitLink = document.getElementById(`${namespace}submitLink`);

		if (submitLink) {
			submitLink.innerHTML = Liferay.Language.get('loading') + '...';
		}

		fetch(event.publishURL)
			.then(() => {
				if (event.incomplete) {
					location.href = event.currentURL;
				}
				else {
					Liferay.fire('updatedLayout');
				}
			})
			.catch(() => {
				layoutRevisionDetails.classList.add('alert alert-danger');

				layoutRevisionDetails.innerHTML = Liferay.Language.get('there-was-an-unexpected-error.-please-refresh-the-current-page');
			});
	};

	const onViewHistory = () => {
		openWindow({
			dialog: {
				after: {
					destroy() {
						window.location.reload();
					},
				},
				destroyOnHide: true,
			},
			title: Liferay.Language.get('history'),
			uri: StagingBar.viewHistoryURL,
		});
	};

	const updateRevision = (cmd, layoutRevisionId, layoutSetBranchId) => {
		const updateLayoutData = {
			cmd,
			layoutRevisionId,
			layoutSetBranchId,
			p_auth: Liferay.authToken,
			p_l_id: themeDisplay.getPlid(),
			p_v_l_s_g_id: themeDisplay.getSiteGroupId(),
		};

		fetch(
			themeDisplay.getPathMain() + '/portal/update_layout',
			{
				body: Liferay.Util.objectToFormData(updateLayoutData),
				method: 'POST',
			}
		)
			.then(() => {
				window.location.reload();
			})
			.catch(() => {
				openToast({
					message: Liferay.Language.get(
						'there-was-an-unexpected-error.-please-refresh-the-current-page'
					),
					toastProps: {
						autoClose: 10000,
					},
					type: 'warning',
				});
			});
	};

	return {
		dispose() {
			eventHandlers.forEach(eventHandler => eventHandler.detach());
		},
	};

};
