/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {AudienceCriteria, AudienceNode, AudienceRule, isGroup} from '../types';
import {nextId} from './ids';

export function emptyCriteria(): AudienceCriteria {
	return {conjunction: 'AND', rules: []};
}

function defaultOperationFor(type: string): string {
	if (type === 'date') {
		return 'gt';
	}

	return 'eq';
}

function defaultValueFor(type: string): AudienceRule['value'] {
	if (type === 'boolean') {
		return true;
	}

	if (type === 'integer' || type === 'double') {
		return 0;
	}

	return '';
}

export function createRule({
	attribute,
	entityName,
	type,
}: {
	attribute: string;
	entityName: string;
	type: string;
}): AudienceRule {
	return {
		attribute,
		entityName,
		id: nextId('rule'),
		operation: defaultOperationFor(type),
		value: defaultValueFor(type),
	};
}

export function appendNode(
	criteria: AudienceCriteria,
	node: AudienceNode
): AudienceCriteria {
	return {...criteria, rules: [...criteria.rules, node]};
}

export function updateNode(
	criteria: AudienceCriteria,
	id: string,
	updater: (node: AudienceNode) => AudienceNode
): AudienceCriteria {
	return {
		...criteria,
		rules: updateInList(criteria.rules, id, updater),
	};
}

function updateInList(
	nodes: AudienceNode[],
	id: string,
	updater: (node: AudienceNode) => AudienceNode
): AudienceNode[] {
	return nodes.map((node) => {
		if (node.id === id) {
			return updater(node);
		}

		if (isGroup(node)) {
			return {
				...node,
				rules: updateInList(node.rules, id, updater),
			};
		}

		return node;
	});
}

export function removeNode(
	criteria: AudienceCriteria,
	id: string
): AudienceCriteria {
	return {
		...criteria,
		rules: removeInList(criteria.rules, id),
	};
}

function removeInList(nodes: AudienceNode[], id: string): AudienceNode[] {
	return nodes
		.filter((node) => node.id !== id)
		.map((node) => {
			if (isGroup(node)) {
				return {...node, rules: removeInList(node.rules, id)};
			}

			return node;
		});
}

export function duplicateNode(
	criteria: AudienceCriteria,
	id: string
): AudienceCriteria {
	return {
		...criteria,
		rules: duplicateInList(criteria.rules, id),
	};
}

function duplicateInList(nodes: AudienceNode[], id: string): AudienceNode[] {
	const result: AudienceNode[] = [];

	for (const node of nodes) {
		result.push(node);

		if (node.id === id) {
			result.push(cloneNode(node));
		}
		else if (isGroup(node)) {
			result[result.length - 1] = {
				...node,
				rules: duplicateInList(node.rules, id),
			};
		}
	}

	return result;
}

export function findNode(
	criteria: AudienceCriteria,
	id: string
): AudienceNode | null {
	for (const node of criteria.rules) {
		const found = findInNode(node, id);

		if (found) {
			return found;
		}
	}

	return null;
}

function findInNode(node: AudienceNode, id: string): AudienceNode | null {
	if (node.id === id) {
		return node;
	}

	if (isGroup(node)) {
		for (const child of node.rules) {
			const found = findInNode(child, id);

			if (found) {
				return found;
			}
		}
	}

	return null;
}

export function insertSibling(
	criteria: AudienceCriteria,
	targetId: string,
	newNode: AudienceNode,
	position: 'after' | 'before'
): AudienceCriteria {
	return {
		...criteria,
		rules: insertSiblingInList(criteria.rules, targetId, newNode, position),
	};
}

function insertSiblingInList(
	nodes: AudienceNode[],
	targetId: string,
	newNode: AudienceNode,
	position: 'after' | 'before'
): AudienceNode[] {
	const result: AudienceNode[] = [];

	let handled = false;

	for (const node of nodes) {
		if (node.id === targetId) {
			if (position === 'before') {
				result.push(newNode, node);
			}
			else {
				result.push(node, newNode);
			}

			handled = true;
		}
		else if (isGroup(node)) {
			const innerRules = insertSiblingInList(
				node.rules,
				targetId,
				newNode,
				position
			);

			if (innerRules !== node.rules) {
				handled = true;
			}

			result.push({...node, rules: innerRules});
		}
		else {
			result.push(node);
		}
	}

	return handled ? result : nodes;
}

export function mergeNodes(
	criteria: AudienceCriteria,
	sourceId: string,
	targetId: string
): AudienceCriteria {
	if (sourceId === targetId) {
		return criteria;
	}

	const source = findNode(criteria, sourceId);

	if (!source) {
		return criteria;
	}

	const withoutSource = removeNode(criteria, sourceId);

	return updateNode(withoutSource, targetId, (target) => ({
		conjunction: 'AND',
		id: nextId('group'),
		rules: [target, source],
	}));
}

function cloneNode(node: AudienceNode): AudienceNode {
	if (isGroup(node)) {
		return {
			...node,
			id: nextId('group'),
			rules: node.rules.map(cloneNode),
		};
	}

	return {...node, id: nextId('rule')};
}
