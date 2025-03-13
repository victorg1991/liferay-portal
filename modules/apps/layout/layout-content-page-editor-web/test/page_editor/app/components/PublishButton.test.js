/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom/extend-expect';
import {act, fireEvent, render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import PublishButton from '../../../../src/main/resources/META-INF/resources/page_editor/app/components/PublishButton';
import useCheckFormsValidity from '../../../../src/main/resources/META-INF/resources/page_editor/app/utils/useCheckFormsValidity';
import useSaveEditableChanges from '../../../../src/main/resources/META-INF/resources/page_editor/app/utils/useSaveEditableChanges';

jest.mock(
	'../../../../src/main/resources/META-INF/resources/page_editor/app/config/index',
	() => ({
		config: {
			pending: false,
			portletNamespace: 'portletNamespace',
			publishURL: 'publishURL',
			redirectURL: 'redirectURL',
		},
	})
);

jest.mock(
	'../../../../src/main/resources/META-INF/resources/page_editor/app/components/FormValidationModal',
	() => ({FormValidationModal: () => 'Form Validation Modal'})
);

jest.mock(
	'../../../../src/main/resources/META-INF/resources/page_editor/app/utils/useCheckFormsValidity',
	() => jest.fn()
);

jest.mock(
	'../../../../src/main/resources/META-INF/resources/page_editor/app/utils/useSaveEditableChanges',
	() => jest.fn()
);

const renderComponent = ({onPublish = () => {}, canPublish = true} = {}) => {
	const ref = React.createRef();

	return render(
		<PublishButton
			canPublish={canPublish}
			formRef={ref}
			label="publish"
			onPublish={onPublish}
		/>
	);
};

describe('PublishButton', () => {
	beforeAll(() => {
		useCheckFormsValidity.mockImplementation(() => () =>
			Promise.resolve(true)
		);

		useSaveEditableChanges.mockImplementation(() => () =>
			Promise.resolve(true)
		);
	});

	afterAll(() => {
		useCheckFormsValidity.mockClear();

		useSaveEditableChanges.mockClear();
	});

	it('renders PublishButton component', () => {
		renderComponent();

		expect(screen.getByLabelText('publish')).toBeInTheDocument();
	});

	it('calls onPublish when the button is clicked', async () => {
		const onPublish = jest.fn(() => {});

		renderComponent({onPublish});

		const button = screen.getByLabelText('publish');

		userEvent.click(button);

		await waitFor(() => expect(onPublish).toHaveBeenCalled());
	});

	it('does not allow to publish if canPublish is false', async () => {
		const onPublish = jest.fn(() => {});

		renderComponent({
			canPublish: false,
			onPublish,
		});

		const button = screen.getByLabelText('publish');

		fireEvent.click(button);

		await waitFor(() => expect(onPublish).not.toHaveBeenCalled());

		expect(button).toBeDisabled();
	});

	it('does not call onPublish when some form is invalid', async () => {
		useCheckFormsValidity.mockImplementation(() => () =>
			Promise.resolve(false)
		);

		const onPublish = jest.fn(() => {});

		renderComponent({onPublish});

		const button = screen.getByLabelText('publish');

		await act(async () => {
			fireEvent.click(button);
		});

		await waitFor(() => expect(onPublish).not.toHaveBeenCalled());
	});
});
