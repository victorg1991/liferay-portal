/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

let counter = 0;

export function nextId(prefix: string): string {
	counter += 1;

	return `${prefix}-${counter}-${Math.random().toString(36).slice(2, 8)}`;
}
