/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export type IdentifierField = 'classPK' | 'externalReferenceCode';

export enum EMappingMode {
	AUTO_RESOLVED = 'autoResolved',
	CONTENT = 'content',
	CONTEXT = 'context',
	LITERAL = 'literal',
}

export interface IContentMappedTokenValue {
	className: string;
	classPK: string;
	externalReferenceCode: string;
	fieldId: IdentifierField | '';
	mappingMode: EMappingMode.CONTENT;
	scopeExternalReferenceCode?: string;
	title?: string;
}

export interface IContextMappedTokenValue {
	fieldId: IdentifierField | '';
	mappingMode: EMappingMode.CONTEXT;
}

export interface IAutoResolvedTokenValue {
	mappingMode: EMappingMode.AUTO_RESOLVED;
}

export type IMappedTokenValue =
	| IAutoResolvedTokenValue
	| IContentMappedTokenValue
	| IContextMappedTokenValue;

export type TokenMapping = string | IMappedTokenValue;

export interface IAPIURLTokenDescriptor {
	autoResolvedTokenNames: string[];
	externalReferenceCode?: string;
	tokenNames: string[];
	unresolvedTokenNames: string[];
}

export function isMappedTokenValue(
	value: TokenMapping
): value is IMappedTokenValue {
	return typeof value === 'object' && value !== null;
}

export function isAutoResolved(
	value: TokenMapping
): value is IAutoResolvedTokenValue {
	return (
		isMappedTokenValue(value) &&
		value.mappingMode === EMappingMode.AUTO_RESOLVED
	);
}

export function isContentMapped(
	value: TokenMapping
): value is IContentMappedTokenValue {
	return (
		isMappedTokenValue(value) && value.mappingMode === EMappingMode.CONTENT
	);
}

export function isContextMapped(
	value: TokenMapping
): value is IContextMappedTokenValue {
	return (
		isMappedTokenValue(value) && value.mappingMode === EMappingMode.CONTEXT
	);
}

export function getMappingMode(value: TokenMapping): EMappingMode {
	if (!isMappedTokenValue(value)) {
		return EMappingMode.LITERAL;
	}

	return value.mappingMode;
}

export function isTokenMapped(
	apiURLTokenDescriptor: IAPIURLTokenDescriptor,
	tokenMappings: Record<string, TokenMapping>,
	tokenName: string
): boolean {
	const tokenMapping = tokenMappings[tokenName];

	if (tokenMapping === undefined) {
		return apiURLTokenDescriptor.autoResolvedTokenNames.includes(tokenName);
	}

	if (typeof tokenMapping === 'string') {
		return !!tokenMapping.trim().length;
	}

	if (isAutoResolved(tokenMapping)) {
		return true;
	}

	if (!tokenMapping.fieldId) {
		return false;
	}

	if (isContextMapped(tokenMapping)) {
		return true;
	}

	return tokenMapping.fieldId === 'externalReferenceCode'
		? !!tokenMapping.externalReferenceCode
		: !!tokenMapping.classPK;
}

export function isTokenMappingCompleted(
	apiURLTokenDescriptor: IAPIURLTokenDescriptor,
	tokenMappings: Record<string, TokenMapping>
): boolean {
	return apiURLTokenDescriptor.tokenNames.every((tokenName) =>
		isTokenMapped(apiURLTokenDescriptor, tokenMappings, tokenName)
	);
}
