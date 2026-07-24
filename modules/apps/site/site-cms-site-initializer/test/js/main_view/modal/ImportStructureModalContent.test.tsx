/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {fireEvent, render, screen, waitFor} from '@testing-library/react';
import React from 'react';

import ApiHelper from '../../../../src/main/resources/META-INF/resources/js/common/services/ApiHelper';
import ImportStructureModalContent from '../../../../src/main/resources/META-INF/resources/js/main_view/modal/ImportStructureModalContent';

jest.mock(
	'../../../../src/main/resources/META-INF/resources/js/common/services/ApiHelper'
);

const mockGet = ApiHelper.get as jest.MockedFunction<typeof ApiHelper.get>;
const mockPostFormData = ApiHelper.postFormData as jest.MockedFunction<
	typeof ApiHelper.postFormData
>;

const mockCloseModal = jest.fn();
const mockLoadData = jest.fn();

const DEFAULT_PROPS = {
	apiURL: '/o/object-admin/v1.0/object-definitions/by-external-reference-code/',
	closeModal: mockCloseModal,
	importURL: '/o/site-cms-site-initializer/import',
	loadData: mockLoadData,
};

const renderComponent = (props = DEFAULT_PROPS) => {
	return render(<ImportStructureModalContent {...props} />);
};

const boundObjectDefinitionsFile = new File(
	[
		JSON.stringify([
			{
				externalReferenceCode: 'STRUCTURE1',
				objectFolderExternalReferenceCode: 'L_CMS_CONTENT_STRUCTURES',
			},
			{
				externalReferenceCode: 'REPEATABLEGROUP1',
				objectFolderExternalReferenceCode:
					'L_CMS_STRUCTURE_REPEATABLE_GROUPS',
			},
		]),
	],
	'structure.json',
	{type: 'application/json'}
);

const selectFile = async (container: HTMLElement, file: File) => {
	const input =
		container.querySelector<HTMLInputElement>('input[type="file"]')!;

	fireEvent.change(input, {
		target: {files: [file]},
	});

	await waitFor(() => {
		expect(screen.getByText('import')).toBeEnabled();
	});
};

describe('ImportStructureModalContent', () => {
	beforeEach(() => {
		jest.clearAllMocks();

		mockGet.mockResolvedValue({
			data: null,
			error: 'Not found',
			status: null,
		});

		mockPostFormData.mockResolvedValue({
			data: {},
			error: null,
			status: null,
		});
	});

	it('renders the modal header, file field, and buttons', () => {
		renderComponent();

		expect(
			screen.getByText('import-and-override-content-structure')
		).toBeInTheDocument();
		expect(screen.getByLabelText('json-file')).toBeInTheDocument();
		expect(screen.getByText('cancel')).toBeInTheDocument();

		const importButton = screen.getByText('import');

		expect(importButton).toBeInTheDocument();
		expect(importButton).toBeDisabled();
	});

	it('imports directly when the structure does not exist', async () => {
		const {container} = renderComponent();

		await selectFile(container, boundObjectDefinitionsFile);

		fireEvent.click(screen.getByText('import'));

		await waitFor(() => {
			expect(mockPostFormData).toHaveBeenCalledTimes(1);
		});

		expect(mockGet).toHaveBeenCalledWith(
			`${DEFAULT_PROPS.apiURL}STRUCTURE1`
		);
		expect(mockCloseModal).toHaveBeenCalledTimes(1);
		expect(mockLoadData).toHaveBeenCalledTimes(1);
	});

	it('posts the bound definitions as an array', async () => {
		const {container} = renderComponent();

		await selectFile(container, boundObjectDefinitionsFile);

		fireEvent.click(screen.getByText('import'));

		await waitFor(() => {
			expect(mockPostFormData).toHaveBeenCalledTimes(1);
		});

		const formData = mockPostFormData.mock.calls[0][0];

		const objectDefinitions = JSON.parse(
			formData.get('objectDefinitions') as string
		);

		expect(objectDefinitions).toHaveLength(2);
		expect(formData.get('objectDefinitionJSON')).toBeNull();
		expect(formData.get('objectFolderExternalReferenceCode')).toBeNull();
	});

	it('warns and lists the structures that will be overwritten', async () => {
		mockGet.mockResolvedValue({
			data: {
				externalReferenceCode: 'STRUCTURE1',
				name: 'My Existing Structure',
			} as any,
			error: null,
			status: null,
		});

		const {container} = renderComponent();

		await selectFile(container, boundObjectDefinitionsFile);

		fireEvent.click(screen.getByText('import'));

		await waitFor(() => {
			expect(
				screen.getByText(
					'do-you-want-to-proceed-with-the-import-process'
				)
			).toBeInTheDocument();
		});

		expect(screen.getByText('My Existing Structure')).toBeInTheDocument();

		expect(mockGet).toHaveBeenCalledTimes(1);

		expect(mockPostFormData).not.toHaveBeenCalled();

		fireEvent.click(screen.getByText('continue'));

		await waitFor(() => {
			expect(mockPostFormData).toHaveBeenCalledTimes(1);
		});

		expect(mockCloseModal).toHaveBeenCalledTimes(1);
	});

	it('keeps the modal open and re-disables import when it fails', async () => {
		mockPostFormData.mockResolvedValue({
			data: null,
			error: 'Error importing structure',
			status: 'ERROR',
		});

		const {container} = renderComponent();

		await selectFile(container, boundObjectDefinitionsFile);

		fireEvent.click(screen.getByText('import'));

		await waitFor(() => {
			expect(mockPostFormData).toHaveBeenCalledTimes(1);
		});

		await waitFor(() => {
			expect(screen.getByText('import').closest('button')).toBeDisabled();
		});

		expect(mockCloseModal).not.toHaveBeenCalled();
	});
});
