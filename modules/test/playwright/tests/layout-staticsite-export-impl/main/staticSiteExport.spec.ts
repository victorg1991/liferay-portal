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

const TAB_TITLES = ['Overview', 'How It Works', 'Limitations'];

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

			const paragraphFragmentDefinition = getFragmentDefinition({
				fragmentFields: [
					{
						id: 'element-text',
						value: {
							text: {
								value_i18n: {
									en_US:
										'A paragraph on both pages, whose' +
										' fragment ships its own stylesheet.',
								},
							},
						},
					},
				],
				id: getRandomString(),
				key: 'BASIC_COMPONENT-paragraph',
			});

			// Home page: two headings, a real image, and a tabs fragment whose
			// panels only become visible when its own JavaScript runs

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
										url: {
											value_i18n: {en_US: documentURL},
										},
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
					getFragmentDefinition({
						fragmentConfig: {
							numberOfTabs: TAB_TITLES.length,
							persistSelectedTab: true,
						},
						fragmentFields: TAB_TITLES.map((tabTitle, index) => ({
							id: `title${index + 1}`,
							value: {
								text: {value_i18n: {en_US: tabTitle}},
							},
						})),
						id: getRandomString(),
						key: 'BASIC_COMPONENT-tabs',
						pageElements: TAB_TITLES.map((tabTitle, index) => ({
							definition: {
								fragmentDropZoneId: String(index + 1),
							},
							id: getRandomString(),
							pageElements: [
								getFragmentDefinition({
									fragmentFields: [
										{
											id: 'element-text',
											value: {
												text: {
													value_i18n: {
														en_US: `${tabTitle} tab heading`,
													},
												},
											},
										},
									],
									id: getRandomString(),
									key: 'BASIC_COMPONENT-heading',
								}),
							],
							type: 'FragmentDropZone',
						})) as never,
					}),
					paragraphFragmentDefinition,
				]),
				siteId: site.id,
				title: 'home',
			});

			// Index page: one button per article, linking to its display page

			await apiHelpers.headlessDelivery.createSitePage({
				pageDefinition: getPageDefinition(
					webContentURLPaths
						.map((webContentURLPath, index) =>
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
													en_US: webContentTitles[
														index
													],
												},
											},
										},
									},
								] as never,
								id: getRandomString(),
								key: 'BASIC_COMPONENT-button',
							})
						)
						.concat([paragraphFragmentDefinition])
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

			const report = JSON.parse(
				fs.readFileSync(
					path.join(exportDirPath, 'export-report.json'),
					'utf8'
				)
			);

			expect(fileNames).toContain('index.html');
			expect(fileNames).toContain('home.html');
			expect(fileNames).toContain('export-report.json');

			expect(
				fileNames.filter((fileName) => fileName.endsWith('.html'))
					.length
			).toBe(report.pages);

			for (const webContentURLPath of webContentURLPaths) {
				expect(fileNames).toContain(
					`${webContentURLPath.slice(1)}.html`
				);
			}

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

			await page.goto(`${staticSiteServer.baseURL}/home.html`);

			// The whole page is exported, not just its content: the theme's
			// header, navigation and footer are all served statically

			await expect(page.locator('#banner')).toBeVisible();
			await expect(page.locator('#footer')).toBeVisible();

			const logo = page.locator('#banner img').first();

			await expect(logo).toBeVisible();

			expect(
				await logo.evaluate(
					(element: HTMLImageElement) =>
						element.complete && element.naturalWidth > 0
				)
			).toBe(true);

			const navigationLinks = page.locator('.navbar-site a.nav-link');

			await expect(navigationLinks).toHaveCount(2);

			// The tabs fragment's own JavaScript runs: every panel is hidden
			// until it reveals one, and clicking a tab switches which

			const tabs = page.locator('.component-tabs .nav-link');

			await expect(tabs).toHaveCount(TAB_TITLES.length);

			for (let i = 0; i < TAB_TITLES.length; i++) {
				await tabs.nth(i).click();

				await expect(
					page.locator('.tab-panel-item:not(.d-none)')
				).toHaveCount(1);

				await expect(
					page.locator('.tab-panel-item:not(.d-none)')
				).toContainText(`${TAB_TITLES[i]} tab heading`);
			}

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

			// A fragment's CSS is emitted once per request, so exporting many
			// pages on one request wrote it onto whichever page rendered
			// first. Both pages carry a paragraph, whose fragment ships a
			// stylesheet, so both must carry that stylesheet.

			for (const pageFileName of ['home.html', 'index.html']) {
				await page.goto(`${staticSiteServer.baseURL}/${pageFileName}`);

				const hasParagraphCSS = await page
					.locator('style')
					.evaluateAll((styles) =>
						styles.some((style) =>
							(style.textContent || '').includes(
								'.component-paragraph'
							)
						)
					);

				expect(hasParagraphCSS).toBe(true);
			}

			// Every display page was rendered for the article it was bound
			// to, which its Open Graph title reflects, and is served as its
			// own document

			for (let i = 0; i < WEB_CONTENT_COUNT; i++) {
				await page.goto(`${staticSiteServer.baseURL}/index.html`);

				await page.locator('.component-button a').nth(i).click();

				expect(
					await page
						.locator('meta[property="og:title"]')
						.first()
						.getAttribute('content')
				).toBe(webContentTitles[i]);

				await expect(page.locator('#main-content')).toBeAttached();
				await expect(page.locator('#banner')).toBeVisible();
				await expect(page.locator('#footer')).toBeVisible();
			}

			// The theme's navigation reaches the other exported pages

			await page.goto(`${staticSiteServer.baseURL}/index.html`);

			await page.locator('.navbar-site a.nav-link').first().click();

			await expect(page.locator('#main-content')).toBeAttached();

			expect(new URL(page.url()).pathname).toBe('/home.html');

			// Every resource the export bundles is served, and no URL kept
			// the broken host the synthetic theme display used to produce

			expect(
				failedResponses.filter((failedResponse) =>
					failedResponse.includes('/documents/')
				)
			).toEqual([]);

			expect(
				staticSiteServer.requestedPaths.filter((requestedPath) =>
					requestedPath.includes('null')
				)
			).toEqual([]);

			expect(failedResponses).toEqual([]);
		}
	);
});
