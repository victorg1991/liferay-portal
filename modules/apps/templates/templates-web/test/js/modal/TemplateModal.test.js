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

import {act, cleanup, fireEvent, render} from '@testing-library/react';
import userEvent from '@testing-library/user-event';

import '@testing-library/jest-dom/extend-expect';
import React from 'react';

import TemplateModal from '../../../src/main/resources/META-INF/resources/js/modal/TemplateModal';
import addTemplate from '../../../src/main/resources/META-INF/resources/js/modal/addTemplate';

jest.mock(
	'../../../src/main/resources/META-INF/resources/js/modal/addTemplate',
	() => jest.fn(() => Promise.resolve())
);

const ADD_TEMPLATE_URL = 'ADD_TEMPLATE_URL';

const renderTemplateModal = (itemTypes = []) => {
	jest.useFakeTimers();

	const renderData = render(
		<TemplateModal
			addTemplateURL={ADD_TEMPLATE_URL}
			itemTypes={itemTypes}
			namespace=""
			onModalClose={() => {}}
		/>
	);

	// Modal is displayed after a delay, so force it to display right away

	act(() => {
		jest.runAllTimers();
	});

	return renderData;
};

describe('TemplateModal', () => {
	beforeEach(cleanup);

	it('renders', () => {
		const {getByText} = renderTemplateModal();

		expect(getByText('add-template')).toBeInTheDocument();
	});

	it('shows an error when trying to submit without entering a name', () => {
		const {getByText} = renderTemplateModal();

		userEvent.click(getByText('save'));

		expect(getByText('this-field-is-required')).toBeInTheDocument();
	});

	it('does not show subtype label if there is not any subtypes', () => {
		const itemTypes = [
			{label: 'Web Content Article', subtypes: [], value: '1'},
			{
				label: 'Categories',
				subtypes: [],
				value: '3',
			},
		];

		const {
			getByLabelText,
			getByText,
			queryByLabelText,
		} = renderTemplateModal(itemTypes);

		const itemTypeSelect = getByLabelText('item-type');

		fireEvent.change(itemTypeSelect, {target: {value: 0}});

		expect(getByText('Web Content Article'));
		expect(queryByLabelText('itemSubtype')).not.toBeInTheDocument();
	});

	it('show subtype label if there are any subtypes', () => {
		const itemTypes = [
			{
				label: 'Web Content Article',
				subtypes: [{label: 'Basic Web Content', value: '2'}],
				value: '1',
			},
			{
				label: 'Categories',
				subtypes: [],
				value: '3',
			},
		];

		const {getByLabelText} = renderTemplateModal(itemTypes);

		const itemTypeSelect = getByLabelText('item-type');

		fireEvent.change(itemTypeSelect, {target: {value: 0}});

		expect(getByLabelText('item-subtype')).toBeInTheDocument();
	});

	it('only show subtypes when there is only one item type', () => {
		const itemTypes = [
			{
				label: 'Web Content Article',
				subtypes: [{label: 'Basic Web Content', value: '2'}],
				value: '1',
			},
		];

		const {queryByText} = renderTemplateModal(itemTypes);

		expect(queryByText('Web Content Article')).not.toBeInTheDocument();
		expect(queryByText('Basic Web Content')).toBeInTheDocument();
	});

	it('send values to the backend', async () => {
		const itemTypes = [
			{
				label: 'Web Content Article',
				subtypes: [{label: 'Basic Web Content', value: '2'}],
				value: '1',
			},
		];

		const {getByLabelText, getByText} = renderTemplateModal(itemTypes);

		userEvent.type(getByLabelText('name'), 'name');

		await act(async () => userEvent.click(getByText('save')));

		expect(addTemplate).toHaveBeenCalledWith(
			expect.objectContaining({
				addTemplateURL: ADD_TEMPLATE_URL,
				body: {
					itemSubtype: '2',
					itemType: '1',
					name: 'name',
				},
			})
		);
	});

	it('send additional values  to the backend', async () => {
		const itemTypes = [
			{
				additionalFields: {
					classNameId: 'classNameId',
					classPK: 'classPK',
				},
				label: 'Web Content Article',
				subtypes: [{label: 'Basic Web Content', value: '2'}],
				value: '1',
			},
		];

		const {getByLabelText, getByText} = renderTemplateModal(itemTypes);

		userEvent.type(getByLabelText('name'), 'name');

		await act(async () => userEvent.click(getByText('save')));

		expect(addTemplate).toHaveBeenCalledWith(
			expect.objectContaining({
				addTemplateURL: ADD_TEMPLATE_URL,
				body: {
					classNameId: 'classNameId',
					classPK: 'classPK',
					itemSubtype: '2',
					itemType: '1',
					name: 'name',
				},
			})
		);
	});
});
