/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../../fixtures/apiHelpersTest';
import {loginTest} from '../../../fixtures/loginTest';

export const test = mergeTests(apiHelpersTest, loginTest());

test('Teardown: Delete site with required data for Echo tests', async ({
	apiHelpers,
	page,
}) => {
	await page.goto('/');

	const response = await apiHelpers.headlessSite.deleteSiteByERC(
		'echo-site-erc'
	);

	await expect(response).toBeOK();
});
