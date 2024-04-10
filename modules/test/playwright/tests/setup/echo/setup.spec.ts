/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../../fixtures/apiHelpersTest';
import {loginTest} from '../../../fixtures/loginTest';

export const test = mergeTests(apiHelpersTest, loginTest());

test('Setup: Create site with required data for Echo tests', async ({
	apiHelpers,
	page,
}) => {
	await page.goto('/');

	// const site = await apiHelpers.headlessSite.createSite({
	// 	externalReferenceCode: 'echo-site-erc',
	// 	name: getRandomString(),
	// });

	const authToken = await page.evaluate(() => Liferay.authToken);

	const response = await page.request.post(
		`${apiHelpers.baseUrl}headless-site/v1.0/sites`,
		{
			form: {
				file: './site-initializer.zip',
				site: `{name: 'Papa'}`,
			},
			headers: {
				'Content-Type': 'multipart/form-data',
				'x-csrf-token': authToken,
			},
		}
	);

	const site = await response.json();

	expect(site).toHaveProperty('externalReferenceCode', 'echo-site-erc');
});
