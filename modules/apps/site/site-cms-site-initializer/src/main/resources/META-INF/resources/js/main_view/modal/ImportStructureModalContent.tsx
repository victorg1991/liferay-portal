/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayModal from '@clayui/modal';
import {openToast} from 'frontend-js-components-web';
import React, {useState} from 'react';

import {FieldFile} from '../../common/components/forms';
import ApiHelper from '../../common/services/ApiHelper';

const JSON_EXTENSION = '.json';

const REPEATABLE_GROUPS_FOLDER = 'L_CMS_STRUCTURE_REPEATABLE_GROUPS';

function readJSONFile(file: File): Promise<any> {
	return new Promise((resolve, reject) => {
		const fileReader = new FileReader();

		fileReader.onerror = () => reject();

		fileReader.onload = () => {
			try {
				resolve(JSON.parse(fileReader.result as string));
			}
			catch (error) {
				reject(error);
			}
		};

		fileReader.readAsText(file);
	});
}

export default function ImportStructureModalContent({
	apiURL,
	closeModal,
	importURL,
	loadData,
}: {
	apiURL: string;
	closeModal: () => void;
	importURL: string;
	loadData?: () => void;
}) {
	const [jsonFile, setJsonFile] = useState<File | null>(null);
	const [objectDefinitions, setObjectDefinitions] = useState<any>(null);
	const [existingStructureNames, setExistingStructureNames] = useState<
		string[]
	>([]);
	const [errorMessage, setErrorMessage] = useState('');
	const [loading, setLoading] = useState(false);
	const [showWarning, setShowWarning] = useState(false);

	const onFileChange = (file: File | null) => {
		if (!file) {
			setErrorMessage('');
		}

		setJsonFile(file);
	};

	const importStructure = async (
		importedObjectDefinitions = objectDefinitions
	) => {
		setLoading(true);

		const formData = new FormData();

		formData.append('keepActive', 'true');

		if (Array.isArray(importedObjectDefinitions)) {
			formData.append(
				'objectDefinitions',
				JSON.stringify(importedObjectDefinitions)
			);
		}
		else if (jsonFile) {
			formData.append('objectDefinitionJSON', new Blob([jsonFile]));
		}

		const {error} = await ApiHelper.postFormData(formData, importURL);

		setLoading(false);

		if (error) {
			setShowWarning(false);
			setErrorMessage(error);

			return;
		}

		closeModal();

		openToast({
			message: Liferay.Language.get(
				'the-content-structure-was-successfully-imported-and-the-existing-content-structure-was-overwritten'
			),
			type: 'success',
		});

		loadData?.();
	};

	const onImportButtonClick = async () => {
		if (!jsonFile) {
			return;
		}

		setLoading(true);

		let parsedFile;

		try {
			parsedFile = await readJSONFile(jsonFile);
		}
		catch (error) {
			setLoading(false);
			setErrorMessage(
				Liferay.Language.get('the-structure-failed-to-import')
			);

			return;
		}

		setObjectDefinitions(parsedFile);

		const definitions = Array.isArray(parsedFile)
			? parsedFile
			: [parsedFile];

		const existingNames: string[] = [];

		for (const definition of definitions) {
			if (
				!definition?.externalReferenceCode ||
				definition.objectFolderExternalReferenceCode ===
					REPEATABLE_GROUPS_FOLDER
			) {
				continue;
			}

			const {data, error} = await ApiHelper.get(
				`${apiURL}${definition.externalReferenceCode}`
			);

			if (!error && data) {
				existingNames.push((data as {name: string}).name);
			}
		}

		setLoading(false);

		if (existingNames.length) {
			setExistingStructureNames(existingNames);
			setShowWarning(true);

			return;
		}

		await importStructure(parsedFile);
	};

	if (showWarning) {
		return (
			<>
				<ClayModal.Header
					closeButtonAriaLabel={Liferay.Language.get('close')}
				>
					{Liferay.Language.get(
						'import-and-override-content-structure'
					)}
				</ClayModal.Header>

				<ClayModal.Body>
					<p>
						{Liferay.Language.get(
							'import-and-override-content-structure-warning-message'
						)}
					</p>

					<ul>
						{existingStructureNames.map((name, index) => (
							<li key={index}>
								<strong>{name}</strong>
							</li>
						))}
					</ul>

					<p>
						{Liferay.Language.get(
							'do-you-want-to-proceed-with-the-import-process'
						)}
					</p>
				</ClayModal.Body>

				<ClayModal.Footer
					last={
						<ClayButton.Group spaced>
							<ClayButton
								displayType="secondary"
								onClick={closeModal}
								type="button"
							>
								{Liferay.Language.get('cancel')}
							</ClayButton>

							<ClayButton
								disabled={loading}
								displayType="warning"
								onClick={() => importStructure()}
							>
								{Liferay.Language.get('continue')}
							</ClayButton>
						</ClayButton.Group>
					}
				/>
			</>
		);
	}

	return (
		<>
			<ClayModal.Header
				closeButtonAriaLabel={Liferay.Language.get('close')}
			>
				{Liferay.Language.get('import-and-override-content-structure')}
			</ClayModal.Header>

			<ClayModal.Body>
				<FieldFile
					errorMessage={errorMessage}
					fieldId="jsonFileId"
					label={Liferay.Language.get('json-file')}
					onFileChange={onFileChange}
					validExtensions={JSON_EXTENSION}
				/>
			</ClayModal.Body>

			<ClayModal.Footer
				last={
					<ClayButton.Group spaced>
						<ClayButton
							displayType="secondary"
							onClick={closeModal}
							type="button"
						>
							{Liferay.Language.get('cancel')}
						</ClayButton>

						<ClayButton
							disabled={!jsonFile || !!errorMessage || loading}
							displayType="primary"
							onClick={onImportButtonClick}
						>
							{Liferay.Language.get('import')}
						</ClayButton>
					</ClayButton.Group>
				}
			/>
		</>
	);
}
