/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';
import * as fs from 'fs';
import * as os from 'os';
import * as path from 'path';

import {dataApiHelpersTest} from '../../../fixtures/dataApiHelpersTest';
import {isolatedSiteTest} from '../../../fixtures/isolatedSiteTest';
import {loginTest} from '../../../fixtures/loginTest';
import {pagesAdminPagesTest} from '../../../fixtures/pagesAdminPagesTest';
import getPageDefinition from '../../layout-content-page-editor-web/main/utils/getPageDefinition';
import {extractZip} from './utils/staticSiteServer';

const test = mergeTests(
	dataApiHelpersTest,
	isolatedSiteTest,
	loginTest(),
	pagesAdminPagesTest
);

const PAGE_TITLES = ['alpha', 'beta', 'gamma'];

test.describe('Static site export from Pages admin', () => {
	let temporaryDirPath: string;

	test.afterEach(() => {
		if (temporaryDirPath) {
			fs.rmSync(temporaryDirPath, {force: true, recursive: true});
		}
	});

	test(
		'Can export only the pages selected in the modal',
		{tag: '@LPD-103696'},
		async ({apiHelpers, page, pagesAdminPage, site}) => {
			for (const title of PAGE_TITLES) {
				await apiHelpers.headlessDelivery.createSitePage({
					pageDefinition: getPageDefinition([]),
					siteId: site.id,
					title,
				});
			}

			await pagesAdminPage.goto(site.friendlyUrlPath);

			// The export lives in the portlet's options menu

			await page.getByLabel('Options').first().click();

			const exportItem = page.getByRole('menuitem', {
				name: 'Export Static Site',
			});

			await expect(exportItem).toBeVisible();

			await exportItem.click();

			// The modal lists every page the export can write, in a tree

			const modal = page.locator('#exportStaticSiteModal');

			await expect(modal).toBeVisible();

			await expect(modal.getByRole('tree')).toBeVisible();

			const checkboxes = modal.locator('[data-testid="pageCheckbox"]');

			await expect(checkboxes).toHaveCount(PAGE_TITLES.length);

			// Every page starts selected; leave one out

			for (let i = 0; i < PAGE_TITLES.length; i++) {
				await expect(checkboxes.nth(i)).toBeChecked();
			}

			await checkboxes.last().uncheck();

			const [download] = await Promise.all([
				page.waitForEvent('download', {timeout: 300000}),
				modal
					.getByRole('button', {exact: true, name: 'Export'})
					.click(),
			]);

			temporaryDirPath = fs.mkdtempSync(
				path.join(os.tmpdir(), 'static-site-export-ui-')
			);

			const zipFilePath = path.join(temporaryDirPath, 'export.zip');

			await download.saveAs(zipFilePath);

			const fileNames = await extractZip(
				zipFilePath,
				path.join(temporaryDirPath, 'site')
			);

			// Only the selected pages were written

			const htmlFileNames = fileNames.filter((fileName) =>
				fileName.endsWith('.html')
			);

			expect(htmlFileNames).toContain(`${PAGE_TITLES[0]}.html`);
			expect(htmlFileNames).toContain(`${PAGE_TITLES[1]}.html`);
			expect(htmlFileNames).not.toContain(`${PAGE_TITLES[2]}.html`);
		}
	);
});
