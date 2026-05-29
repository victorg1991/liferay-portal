/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React, {
	Dispatch,
	createContext,
	useContext,
	useMemo,
	useReducer,
} from 'react';

import {
	AudienceCriteria,
	AudienceNode,
	AudienceRule,
	AudienceScope,
	Conjunction,
	RetentionType,
} from '../types';
import {
	appendNode,
	duplicateNode,
	emptyCriteria,
	insertSibling,
	mergeNodes,
	removeNode,
	updateNode,
} from '../utils/criteriaTree';

export interface AudienceEditState {
	criteria: AudienceCriteria;
	erc: string;
	name: string;
	retentionType: RetentionType;
	scope: AudienceScope | null;
}

export type Action =
	| {payload: AudienceNode; type: 'APPEND_NODE'}
	| {payload: {id: string}; type: 'REMOVE_NODE'}
	| {payload: {id: string}; type: 'DUPLICATE_NODE'}
	| {
			payload: {
				newNode: AudienceNode;
				position: 'after' | 'before';
				targetId: string;
			};
			type: 'INSERT_SIBLING';
	  }
	| {payload: {sourceId: string; targetId: string}; type: 'MERGE_NODES'}
	| {
			payload: {groupId: string | null; rules: AudienceNode[]};
			type: 'REPLACE_RULES';
	  }
	| {
			payload: {
				partial: Partial<AudienceRule>;
				ruleId: string;
			};
			type: 'UPDATE_RULE';
	  }
	| {
			payload: {conjunction: Conjunction; groupId: string};
			type: 'UPDATE_GROUP_CONJUNCTION';
	  }
	| {payload: {conjunction: Conjunction}; type: 'SET_ROOT_CONJUNCTION'}
	| {type: 'CLEAR_ALL'}
	| {payload: {erc: string}; type: 'SET_ERC'}
	| {payload: {name: string}; type: 'SET_NAME'}
	| {payload: {retentionType: RetentionType}; type: 'SET_RETENTION_TYPE'}
	| {payload: {scope: AudienceScope | null}; type: 'SET_SCOPE'};

function reducer(state: AudienceEditState, action: Action): AudienceEditState {
	switch (action.type) {
		case 'APPEND_NODE':
			return {
				...state,
				criteria: appendNode(state.criteria, action.payload),
			};

		case 'REMOVE_NODE':
			return {
				...state,
				criteria: removeNode(state.criteria, action.payload.id),
			};

		case 'DUPLICATE_NODE':
			return {
				...state,
				criteria: duplicateNode(state.criteria, action.payload.id),
			};

		case 'INSERT_SIBLING':
			return {
				...state,
				criteria: insertSibling(
					state.criteria,
					action.payload.targetId,
					action.payload.newNode,
					action.payload.position
				),
			};

		case 'MERGE_NODES':
			return {
				...state,
				criteria: mergeNodes(
					state.criteria,
					action.payload.sourceId,
					action.payload.targetId
				),
			};

		case 'REPLACE_RULES': {
			const {groupId, rules} = action.payload;

			if (groupId === null) {
				return {
					...state,
					criteria: {...state.criteria, rules},
				};
			}

			return {
				...state,
				criteria: updateNode(state.criteria, groupId, (node) => ({
					...node,
					rules,
				})),
			};
		}

		case 'UPDATE_RULE':
			return {
				...state,
				criteria: updateNode(
					state.criteria,
					action.payload.ruleId,
					(node) => ({...node, ...action.payload.partial})
				),
			};

		case 'UPDATE_GROUP_CONJUNCTION':
			return {
				...state,
				criteria: updateNode(
					state.criteria,
					action.payload.groupId,
					(node) => ({
						...node,
						conjunction: action.payload.conjunction,
					})
				),
			};

		case 'SET_ROOT_CONJUNCTION':
			return {
				...state,
				criteria: {
					...state.criteria,
					conjunction: action.payload.conjunction,
				},
			};

		case 'CLEAR_ALL':
			return {...state, criteria: emptyCriteria()};

		case 'SET_ERC':
			return {...state, erc: action.payload.erc};

		case 'SET_NAME':
			return {...state, name: action.payload.name};

		case 'SET_RETENTION_TYPE':
			return {...state, retentionType: action.payload.retentionType};

		case 'SET_SCOPE':
			return {...state, scope: action.payload.scope};

		default:
			return state;
	}
}

const StateContext = createContext<AudienceEditState | null>(null);
const DispatchContext = createContext<Dispatch<Action> | null>(null);

export function useAudienceState(): AudienceEditState {
	const state = useContext(StateContext);

	if (!state) {
		throw new Error(
			'useAudienceState must be used inside AudienceEditProvider'
		);
	}

	return state;
}

export function useAudienceDispatch(): Dispatch<Action> {
	const dispatch = useContext(DispatchContext);

	if (!dispatch) {
		throw new Error(
			'useAudienceDispatch must be used inside AudienceEditProvider'
		);
	}

	return dispatch;
}

interface ProviderProps {
	children: React.ReactNode;
	initialState: AudienceEditState;
}

export function AudienceEditProvider({children, initialState}: ProviderProps) {
	const [state, dispatch] = useReducer(reducer, initialState);

	const memoizedState = useMemo(() => state, [state]);

	return (
		<StateContext.Provider value={memoizedState}>
			<DispatchContext.Provider value={dispatch}>
				{children}
			</DispatchContext.Provider>
		</StateContext.Provider>
	);
}
