/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import {TreeView as ClayTreeView} from '@clayui/core';
import {ClayCheckbox} from '@clayui/form';
import ClayModal from '@clayui/modal';
import {openModal, openToast} from 'frontend-js-components-web';
import {fetch} from 'frontend-js-web';
import PropTypes from 'prop-types';
import React, {useCallback, useMemo, useState} from 'react';

function collectPlids(pages) {
	return pages.map((page) => page.plid);
}

function PageItem({item, onToggle, selectedPlids}) {
	return (
		<div
			className={
				'align-items-center d-flex c-ml-' + Math.min(item.depth * 3, 9)
			}
		>
			<ClayCheckbox
				checked={selectedPlids.has(item.plid)}
				containerProps={{className: 'mb-0 mr-2 mt-0'}}
				data-plid={item.plid}
				data-testid="pageCheckbox"
				onChange={(event) => onToggle(item, event.target.checked)}
				tabIndex={-1}
			/>

			<span>{item.name}</span>

			<span className="c-ml-2 text-muted">{item.friendlyURL}</span>
		</div>
	);
}

PageItem.propTypes = {
	item: PropTypes.object.isRequired,
	onToggle: PropTypes.func.isRequired,
	selectedPlids: PropTypes.object.isRequired,
};

function ExportStaticSiteModal({closeModal, exportURL, groupId, pages}) {
	const allPlids = useMemo(() => collectPlids(pages), [pages]);

	const [exporting, setExporting] = useState(false);
	const [selectedPlids, setSelectedPlids] = useState(() => new Set(allPlids));

	const onToggle = useCallback((item, checked) => {
		setSelectedPlids((previousSelectedPlids) => {
			const nextSelectedPlids = new Set(previousSelectedPlids);

			if (checked) {
				nextSelectedPlids.add(item.plid);
			}
			else {
				nextSelectedPlids.delete(item.plid);
			}

			return nextSelectedPlids;
		});
	}, []);

	const onExport = useCallback(async () => {
		setExporting(true);

		try {
			const searchParams = new URLSearchParams();

			searchParams.append('groupId', groupId);

			selectedPlids.forEach((plid) => searchParams.append('plids', plid));

			const response = await fetch(exportURL + '?' + searchParams);

			if (!response.ok) {
				throw new Error(response.statusText);
			}

			const anchor = document.createElement('a');

			anchor.download = 'static-site.zip';
			anchor.href = URL.createObjectURL(await response.blob());

			document.body.appendChild(anchor);

			anchor.click();

			document.body.removeChild(anchor);

			URL.revokeObjectURL(anchor.href);

			closeModal();
		}
		catch (error) {
			openToast({
				message: Liferay.Language.get('an-unexpected-error-occurred'),
				type: 'danger',
			});
		}
		finally {
			setExporting(false);
		}
	}, [closeModal, exportURL, groupId, selectedPlids]);

	return (
		<>
			<ClayModal.Header>
				{Liferay.Language.get('export-static-site')}
			</ClayModal.Header>

			<ClayModal.Body>
				<p>
					{Liferay.Language.get(
						'each-selected-page-is-rendered-and-written-with-everything-it-references'
					)}
				</p>

				<ClayCheckbox
					checked={selectedPlids.size === allPlids.length}
					indeterminate={
						selectedPlids.size > 0 &&
						selectedPlids.size < allPlids.length
					}
					label={Liferay.Language.get('select-all')}
					onChange={(event) =>
						setSelectedPlids(
							event.target.checked ? new Set(allPlids) : new Set()
						)
					}
				/>

				<hr />

				<ClayTreeView defaultItems={pages} showExpanderOnHover={false}>
					{(item) => (
						<ClayTreeView.Item>
							<PageItem
								item={item}
								onToggle={onToggle}
								selectedPlids={selectedPlids}
							/>
						</ClayTreeView.Item>
					)}
				</ClayTreeView>
			</ClayModal.Body>

			<ClayModal.Footer
				last={
					<ClayButton.Group spaced>
						<ClayButton
							disabled={exporting}
							displayType="secondary"
							onClick={closeModal}
						>
							{Liferay.Language.get('cancel')}
						</ClayButton>

						<ClayButton
							disabled={exporting || !selectedPlids.size}
							displayType="primary"
							onClick={onExport}
						>
							{exporting
								? Liferay.Language.get('exporting')
								: Liferay.Language.get('export')}
						</ClayButton>
					</ClayButton.Group>
				}
			/>
		</>
	);
}

ExportStaticSiteModal.propTypes = {
	closeModal: PropTypes.func.isRequired,
	exportURL: PropTypes.string.isRequired,
	groupId: PropTypes.string.isRequired,
	pages: PropTypes.array.isRequired,
};

export default function ExportStaticSite({action, exportURL, groupId, pages}) {
	Liferay.Util.setPortletConfigurationIconAction(action, () =>
		openModal({
			contentComponent: ({closeModal}) => (
				<ExportStaticSiteModal
					closeModal={closeModal}
					exportURL={exportURL}
					groupId={groupId}
					pages={pages}
				/>
			),
			id: 'exportStaticSiteModal',
			size: 'md',
		})
	);
}
