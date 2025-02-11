/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {applicationsMenuPageTest} from '../../fixtures/applicationsMenuPageTest';
import {captchaConfigPageTest} from '../../fixtures/captchaConfigPageTest';
import {loginTest} from '../../fixtures/loginTest';
import {liferayConfig} from '../../liferay.config';
import getRandomString from '../../utils/getRandomString';

export const test = mergeTests(
	captchaConfigPageTest,
	applicationsMenuPageTest,
	loginTest
);

test.beforeEach(
	'Disable create account CAPTCHA',
	async ({captchaConfigPage, page}) => {
		await page.goto(liferayConfig.environment.baseUrl);

		if (await page.getByRole('button', {name: 'Sign In'}).isVisible()) {
			await captchaConfigPage.performLogin();
		}

		await captchaConfigPage.goTo();

		await captchaConfigPage.disableCreateAccountCaptcha();
	}
);

test.afterEach(
	'Reset CAPTCHA configuration',
	async ({captchaConfigPage, page}) => {
		await page.goto('/');

		if (await page.getByRole('button', {name: 'Sign In'}).isVisible()) {
			await captchaConfigPage.performLogin();
		}

		await captchaConfigPage.goTo();

		await captchaConfigPage.resetCaptchaConfiguration();

		await page.goto('/');
	}
);

test('LPD-44960 Create account using duplicate email address', async ({
	page,
}) => {
	await page.getByLabel('Test Test User Profile').click();

	await page.getByRole('menuitem', {name: 'Sign Out'}).click();

	await page.goto(liferayConfig.environment.baseUrl);

	await page.getByRole('button', {name: 'Sign In'}).click();

	await page.getByText('Create Account').click();

	await page.getByRole('heading', {name: 'User Display Data'}).waitFor();

	await page.getByLabel('Screen Name').fill(getRandomString());

	await page.getByLabel('Email Address').fill('test@liferay.com');

	await page.getByLabel('First Name').fill(getRandomString());

	await page.getByLabel('Last Name').fill(getRandomString());

	const password = getRandomString();

	await page.getByLabel('Password Required', {exact: true}).fill(password);

	await page.getByLabel('Reenter Password Required').fill(password);

	await page.getByRole('button', {name: 'Save'}).click();

	await expect(
		page.getByText(
			'Thank you for creating an account. Use your password to log in.'
		)
	).toBeVisible();

	await expect(
		page.getByText('Error:Your request failed to complete.')
	).toBeHidden();
});

test('LPD-44960 Create account using duplicate email address with email address verification', async ({
	applicationsMenuPage,
	captchaConfigPage,
	page,
}) => {
	await applicationsMenuPage.goToInstanceSettings();

	await page.getByRole('link', {name: 'User Authentication'}).click();

	const strangersVerify = page.getByText(
		'Require strangers to verify their email address?'
	);

	await expect(strangersVerify).toBeVisible();

	await strangersVerify.check();
	await expect(strangersVerify).toBeChecked();

	await captchaConfigPage.saveConfiguration();

	await page.getByLabel('Test Test User Profile').click();

	await page.getByRole('menuitem', {name: 'Sign Out'}).click();

	await page.goto(liferayConfig.environment.baseUrl);

	await page.getByRole('button', {name: 'Sign In'}).click();

	await page.getByText('Create Account').click();

	await page.getByLabel('Screen Name').fill(getRandomString());

	await page.getByLabel('Email Address').fill('test@liferay.com');

	await page.getByLabel('First Name').fill(getRandomString());

	await page.getByLabel('Last Name').fill(getRandomString());

	const password = getRandomString();

	await page.getByLabel('Password Required', {exact: true}).fill(password);

	await page.getByLabel('Reenter Password Required').fill(password);

	await page.getByRole('button', {name: 'Save'}).click();

	await expect(
		page.getByText(
			'Thank you for creating an account. Your email verification code was sent to test@liferay.com. Use your password to log in.'
		)
	).toBeVisible();

	await expect(
		page.getByText('Error:Your request failed to complete.')
	).toBeHidden();

	await captchaConfigPage.performLogin();

	await applicationsMenuPage.goToInstanceSettings();

	await page.getByRole('link', {name: 'User Authentication'}).click();

	await expect(strangersVerify).toBeVisible();

	await strangersVerify.uncheck();
	await expect(strangersVerify).not.toBeChecked();

	await captchaConfigPage.saveConfiguration();
});
