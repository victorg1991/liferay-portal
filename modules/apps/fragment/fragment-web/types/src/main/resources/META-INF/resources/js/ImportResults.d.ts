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

/// <reference types="react" />

interface ImportResult {
	message: string;
	name: string;
	type: 'fragment' | 'composition';
}
export interface ImportResultsData {
	'imported': ImportResult[];
	'imported-draft': ImportResult[];
	'invalid': ImportResult[];
}
interface Props {
	fileName: string | null;
	importResults: ImportResultsData;
}
declare function ImportResults({fileName, importResults}: Props): JSX.Element;
export default ImportResults;
