/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';
import * as fs from 'fs';
import * as os from 'os';
import * as path from 'path';

import {dataApiHelpersTest} from '../../../fixtures/dataApiHelpersTest';
import {featureFlagsTest} from '../../../fixtures/featureFlagsTest';
import {isolatedSiteTest} from '../../../fixtures/isolatedSiteTest';
import {loginTest} from '../../../fixtures/loginTest';
import {liferayConfig} from '../../../liferay.config';
import getRandomString from '../../../utils/getRandomString';
import getBasicWebContentStructureId from '../../../utils/structured-content/getBasicWebContentStructureId';
import getFragmentDefinition from '../../layout-content-page-editor-web/main/utils/getFragmentDefinition';
import getPageDefinition from '../../layout-content-page-editor-web/main/utils/getPageDefinition';
import {
	StaticSiteServer,
	extractZip,
	startStaticSiteServer,
} from './utils/staticSiteServer';

const test = mergeTests(
	dataApiHelpersTest,
	featureFlagsTest({
		'LPS-178052': {enabled: true},
	}),
	isolatedSiteTest,
	loginTest()
);

const HEADINGS = ['Static Export Overview', 'How The Export Works'];

const WEB_CONTENT_COUNT = 5;

test.describe('Static site export', () => {
	let staticSiteServer: StaticSiteServer;
	let temporaryDirPath: string;

	test.afterEach(async () => {
		if (staticSiteServer) {
			await staticSiteServer.stop();
		}

		if (temporaryDirPath) {
			fs.rmSync(temporaryDirPath, {force: true, recursive: true});
		}
	});

	test(
		'Can export a site and serve it from an unrelated web server',
		{tag: '@LPD-103696'},
		async ({apiHelpers, page, site}, testInfo) => {
			const document = await apiHelpers.headlessDelivery.postDocument(
				site.id,
				fs.createReadStream(
					path.join(
						testInfo.project.testDir,
						'dependencies',
						'liferay.png'
					)
				),
				{
					description: getRandomString(),
					externalReferenceCode: getRandomString(),
					fileName: 'liferay.png',
					title: 'Static Export Logo',
					viewableBy: 'Anyone',
				}
			);

			const documentURL = new URL(
				document.contentUrl,
				liferayConfig.environment.baseUrl
			).pathname;

			// Five web contents, each reachable only through a display page

			const contentStructureId =
				await getBasicWebContentStructureId(apiHelpers);

			const webContentTitles: string[] = [];
			const webContentURLPaths: string[] = [];

			const className =
				await apiHelpers.jsonWebServicesClassName.fetchClassName(
					'com.liferay.journal.model.JournalArticle'
				);

			const layoutPageTemplateEntry =
				await apiHelpers.jsonWebServicesLayoutPageTemplateEntry.addDisplayPageLayoutPageTemplateEntry(
					{
						classNameId: className.classNameId,
						groupId: site.id,
						name: getRandomString(),
					}
				);

			for (let i = 1; i <= WEB_CONTENT_COUNT; i++) {
				const title = `Static Export Article ${i}`;

				const structuredContent =
					await apiHelpers.headlessDelivery.postStructuredContent({
						contentFields: [
							{
								contentFieldValue: {
									data: `<p>Body of article ${i} exported as a static file.</p>`,
								},
								name: 'content',
							},
						],
						contentStructureId,
						datePublished: new Date().toISOString(),
						siteId: site.id,
						title,
						viewableBy: 'Anyone',
					});

				await apiHelpers.jsonWebServicesAssetDisplayPageEntry.addAssetDisplayPageEntry(
					{
						classNameId: className.classNameId,
						classPK: String(structuredContent.id),
						groupId: site.id,
						layoutPageTemplateEntryId: String(
							layoutPageTemplateEntry.layoutPageTemplateEntryId
						),
					}
				);

				webContentTitles.push(title);
				webContentURLPaths.push(
					`/w/${structuredContent.friendlyUrlPath}`
				);
			}

			// Home page: two headings and a real image

			await apiHelpers.headlessDelivery.createSitePage({
				pageDefinition: getPageDefinition([
					getFragmentDefinition({
						fragmentFields: [
							{
								id: 'element-text',
								value: {
									text: {value_i18n: {en_US: HEADINGS[0]}},
								},
							},
						],
						id: getRandomString(),
						key: 'BASIC_COMPONENT-heading',
					}),
					getFragmentDefinition({
						fragmentFields: [
							{
								id: 'image-square',
								value: {
									fragmentImage: {
										title: {value: 'Static Export Logo'},
										url: {value: documentURL},
									},
								},
							},
						] as never,
						id: getRandomString(),
						key: 'BASIC_COMPONENT-image',
					}),
					getFragmentDefinition({
						fragmentFields: [
							{
								id: 'element-text',
								value: {
									text: {value_i18n: {en_US: HEADINGS[1]}},
								},
							},
						],
						id: getRandomString(),
						key: 'BASIC_COMPONENT-heading',
					}),
				]),
				siteId: site.id,
				title: 'home',
			});

			// Index page: one button per article, linking to its display page

			await apiHelpers.headlessDelivery.createSitePage({
				pageDefinition: getPageDefinition(
					webContentURLPaths.map((webContentURLPath, index) =>
						getFragmentDefinition({
							fragmentFields: [
								{
									id: 'link',
									value: {
										fragmentLink: {
											value: {
												href: {
													value: webContentURLPath,
												},
												target: 'Self',
											},
										},
										text: {
											value_i18n: {
												en_US: webContentTitles[index],
											},
										},
									},
								},
							] as never,
							id: getRandomString(),
							key: 'BASIC_COMPONENT-button',
						})
					)
				),
				siteId: site.id,
				title: 'index',
			});

			// Export the site, then forget the portal exists

			const response = await page.request.get(
				`${liferayConfig.environment.baseUrl}/c/portal/layout_staticsite_export/export_static_site?groupId=${site.id}`
			);

			expect(response.status()).toBe(200);

			temporaryDirPath = fs.mkdtempSync(
				path.join(os.tmpdir(), 'static-site-export-')
			);

			const zipFilePath = path.join(temporaryDirPath, 'export.zip');

			fs.writeFileSync(zipFilePath, await response.body());

			const exportDirPath = path.join(temporaryDirPath, 'site');

			const fileNames = await extractZip(zipFilePath, exportDirPath);

			expect(fileNames).toContain('index.html');
			expect(fileNames).toContain('export-report.json');

			for (const webContentURLPath of webContentURLPaths) {
				expect(fileNames).toContain(
					`${webContentURLPath.slice(1)}.html`
				);
			}

			const report = JSON.parse(
				fs.readFileSync(
					path.join(exportDirPath, 'export-report.json'),
					'utf8'
				)
			);

			expect(report.skippedPages).toEqual([]);
			expect(report.resources).toBeGreaterThan(0);

			staticSiteServer = await startStaticSiteServer(exportDirPath);

			const failedResponses: string[] = [];

			page.on('response', (response) => {
				if (response.status() >= 400) {
					failedResponses.push(
						`${response.status()} ${response.url()}`
					);
				}
			});

			// The home page renders its headings and its image, served
			// entirely by the static server

			await page.goto(`${staticSiteServer.baseURL}/index.html`);

			for (const heading of HEADINGS) {
				await expect(
					page.locator(`text=${heading}`).first()
				).toBeVisible();
			}

			const image = page.locator('.component-image img').first();

			await expect(image).toBeVisible();

			expect(
				await image.evaluate(
					(element: HTMLImageElement) =>
						element.complete && element.naturalWidth > 0
				)
			).toBe(true);

			// Every display page renders the article it was bound to

			for (let i = 0; i < WEB_CONTENT_COUNT; i++) {
				await page.goto(
					`${staticSiteServer.baseURL}${webContentURLPaths[i]}.html`
				);

				await expect(
					page.locator(`text=Body of article ${i + 1}`).first()
				).toBeVisible();
			}

			// Nothing 404ed, and no URL kept the broken host the synthetic
			// theme display used to produce

			expect(failedResponses).toEqual([]);

			expect(
				staticSiteServer.requestedPaths.filter((requestedPath) =>
					requestedPath.includes('null')
				)
			).toEqual([]);
		}
	);
});
