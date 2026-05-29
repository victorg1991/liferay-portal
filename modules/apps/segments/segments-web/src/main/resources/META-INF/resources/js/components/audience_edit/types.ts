/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export type Conjunction = 'AND' | 'OR';

export type RetentionType = 'SESSION' | 'PERSISTENT';

export interface AudienceRule {
	attribute: string;
	entityName: string;
	id: string;
	operation: string;
	value: string | string[] | number | boolean;
}

export interface AudienceGroup {
	conjunction: Conjunction;
	id: string;
	rules: Array<AudienceRule | AudienceGroup>;
}

export type AudienceNode = AudienceRule | AudienceGroup;

export interface AudienceCriteria {
	conjunction: Conjunction;
	rules: AudienceNode[];
}

export function isGroup(node: AudienceNode): node is AudienceGroup {
	return (node as AudienceGroup).rules !== undefined;
}

export function isRule(node: AudienceNode): node is AudienceRule {
	return !isGroup(node);
}

export interface PropertyField {
	entityName: string;
	icon?: string;
	label: string;
	name: string;
	options?: Array<{label: string; value: string}>;
	type: 'string' | 'integer' | 'double' | 'boolean' | 'date' | 'id';
}

export interface PropertyGroup {
	entityName: string;
	name: string;
	properties: PropertyField[];
	propertyKey: string;
}

export interface AudienceScope {
	groupId: string;
	name: string;
}
