/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import * as fs from 'fs';
import * as http from 'http';
import * as path from 'path';
import {open} from 'yauzl';

const CONTENT_TYPES: Record<string, string> = {
	'.css': 'text/css',
	'.gif': 'image/gif',
	'.html': 'text/html',
	'.ico': 'image/x-icon',
	'.jpeg': 'image/jpeg',
	'.jpg': 'image/jpeg',
	'.js': 'text/javascript',
	'.json': 'application/json',
	'.mjs': 'text/javascript',
	'.png': 'image/png',
	'.svg': 'image/svg+xml',
	'.woff': 'font/woff',
	'.woff2': 'font/woff2',
};

/**
 * Extracts every entry of a zip into a directory, preserving binary content
 * and nested paths.
 */
export async function extractZip(
	zipFilePath: string,
	targetDirPath: string
): Promise<string[]> {
	return new Promise((resolve, reject) => {
		const fileNames: string[] = [];

		open(zipFilePath, {lazyEntries: true}, (error, zipFile) => {
			if (error || !zipFile) {
				return reject(error);
			}

			zipFile.readEntry();

			zipFile.on('entry', (entry) => {
				if (/\/$/.test(entry.fileName)) {
					zipFile.readEntry();

					return;
				}

				const filePath = path.join(targetDirPath, entry.fileName);

				fs.mkdirSync(path.dirname(filePath), {recursive: true});

				zipFile.openReadStream(entry, (error, readStream) => {
					if (error || !readStream) {
						zipFile.close();

						return reject(error);
					}

					const writeStream = fs.createWriteStream(filePath);

					readStream.pipe(writeStream);

					writeStream.on('close', () => {
						fileNames.push(entry.fileName);

						zipFile.readEntry();
					});
				});
			});

			zipFile.on('end', () => {
				zipFile.close();

				resolve(fileNames);
			});

			zipFile.on('error', (error) => {
				zipFile.close();

				reject(error);
			});
		});
	});
}

export type StaticSiteServer = {
	baseURL: string;
	requestedPaths: string[];
	stop: () => Promise<void>;
};

/**
 * Serves a directory over HTTP on an ephemeral port, standing in for the
 * unrelated web server the export is meant to be deployed to.
 */
export async function startStaticSiteServer(
	rootDirPath: string
): Promise<StaticSiteServer> {
	const requestedPaths: string[] = [];

	const server = http.createServer((request, response) => {
		const requestPath = decodeURIComponent(
			(request.url || '/').split('?')[0]
		);

		requestedPaths.push(requestPath);

		const filePath = path.join(
			rootDirPath,
			requestPath === '/' ? 'index.html' : requestPath
		);

		if (
			!path.resolve(filePath).startsWith(path.resolve(rootDirPath)) ||
			!fs.existsSync(filePath) ||
			!fs.statSync(filePath).isFile()
		) {
			response.writeHead(404);
			response.end('Not found');

			return;
		}

		response.writeHead(200, {
			'content-type':
				CONTENT_TYPES[path.extname(filePath).toLowerCase()] ||
				'application/octet-stream',
		});

		fs.createReadStream(filePath).pipe(response);
	});

	await new Promise<void>((resolve) =>
		server.listen(0, '127.0.0.1', () => resolve())
	);

	const address = server.address();

	if (!address || typeof address === 'string') {
		throw new Error('Unable to determine the static site server port');
	}

	return {
		baseURL: `http://127.0.0.1:${address.port}`,
		requestedPaths,
		stop: () =>
			new Promise<void>((resolve, reject) =>
				server.close((error) => (error ? reject(error) : resolve()))
			),
	};
}
