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

import {cleanup, render} from '@testing-library/react';
import React from 'react';

import '@testing-library/jest-dom/extend-expect';

import IssueDetail from '../../../src/main/resources/META-INF/resources/js/components/IssueDetail';
import {StoreContextProvider} from '../../../src/main/resources/META-INF/resources/js/context/StoreContext';

const mockIssueWithTitleAndSections = {
	description: 'Page displays images with incorrect aspect ratio.',
	failingElements: [
		{
			actualAspectRatio: 'element-1-actual-aspect-ratio',
			displayedAspectRatio: 'element-1-displayed-aspect-ratio',
			doRatiosMatch: false,
			url: 'element-1-url',
		},
		{
			actualAspectRatio: 'element-2-actual-aspect-ratio',
			displayedAspectRatio: 'element-2-displayed-aspect-ratio',
			doRatiosMatch: false,
			url: 'element-2-url',
		},
	],
	key: 'incorrect-image-aspect-ratios',
	tips: 'Incorrect image aspect ratios can be caused by...',
	title: 'Incorrect image aspect ratios',
	total: '2',
};

const mockIssueWithContent = {
	description: 'When multiple pages have similar content, search engines...',
	failingElements: [
		{
			content: 'If the problem is that the canonical URL does...',
		},
	],
	key: 'invalid-canonical-url',
	tips: 'In a Liferay site, canonical URLs are automatically generated...',
	title: 'Invalid Canonical URL',
	total: '1',
};

const renderIssueDetail = (selectedIssue) =>
	render(
		<StoreContextProvider
			value={{
				selectedIssue,
			}}
		>
			<IssueDetail />
		</StoreContextProvider>
	);

describe('IssuesList', () => {
	afterEach(cleanup);

	it('renders description, tips and failing elements sections', () => {
		const {getByText} = renderIssueDetail(mockIssueWithTitleAndSections);

		expect(getByText('description')).toBeInTheDocument();
		expect(getByText('tips')).toBeInTheDocument();
		expect(getByText('failing-elements')).toBeInTheDocument();
	});
});
