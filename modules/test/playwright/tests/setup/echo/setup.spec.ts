/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';
import {createReadStream} from 'fs';
import {resolve} from 'path';

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

	const file = resolve(__dirname, 'site-initializer.zip');

	const stream = createReadStream(file);

	const response = await page.request.post(
		`http://localhost:8080/o/headless-site/v1.0/sites`,
		{
			headers: {
				'Accept': '*/*',
				'Content-Type': 'multipart/form-data',
				'x-csrf-token': authToken,
			},

			multipart: {
				file: stream,
				site: {
					buffer: Buffer.from('{"name": "Papa"}', 'utf-8'),
					mimeType: 'application/json',
					name: 'site',
				},
			},
		}
	);

	console.log(response);

	const site = await response.json();

	console.log(site);

	expect(site).toHaveProperty('externalReferenceCode', 'echo-site-erc');
});
