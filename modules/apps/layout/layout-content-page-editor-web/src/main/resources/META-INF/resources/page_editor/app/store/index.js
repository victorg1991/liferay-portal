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

import React, {
	useCallback,
	useContext,
	useEffect,
	useMemo,
	useReducer,
	useRef,
	useState,
} from 'react';

import useThunk from '../../core/hooks/useThunk';
import useUndo from '../components/undo/useUndo';

const StoreContext = React.createContext(null);

/**
 * Although StoreContextProvider creates a full functional store,
 * sometimes mocking dispatchs and/or store state may be necessary
 * for testing purposes.
 *
 * This component wraps it's children with an usable StoreContext
 * that calls dispatch and getState methods instead of using a real
 * reducer internally.
 */
export const StoreAPIContextProvider = ({
	children,
	dispatch = () => {},
	getState = () => ({}),
}) => {
	const subscribers = useRef([]);

	const subscribe = useCallback((subscriber) => {
		subscribers.current = [...subscribers.current, subscriber];
	}, []);

	const unsubscribe = useCallback((subscriber) => {
		subscribers.current = subscribers.current.filter(
			(_subscriber) => _subscriber !== subscriber
		);
	}, []);

	useEffect(() => {
		storeRef.current.dispatch = dispatch;
	}, [dispatch]);

	useEffect(() => {
		storeRef.current.getState = getState;
		subscribers.current.forEach((subscriber) => subscriber());
	}, [getState]);

	const storeRef = useRef({
		dispatch,
		getState,
		subscribe,
		unsubscribe,
	});

	return (
		<StoreContext.Provider value={storeRef}>
			{children}
		</StoreContext.Provider>
	);
};

/**
 * StoreContext is a black box for components: they should
 * get information from state and dispatch actions by using
 * given useSelector and useDispatch hooks.
 *
 * That's why we only provide a custom StoreContextProvider instead
 * of the raw React context.
 */
export const StoreContextProvider = ({children, initialState, reducer}) => {
	const [state, dispatch] = useThunk(
		useUndo(useReducer(reducer, initialState))
	);

	const getState = useCallback(() => state, [state]);

	return (
		<StoreAPIContextProvider dispatch={dispatch} getState={getState}>
			{children}
		</StoreAPIContextProvider>
	);
};

/**
 * @see https://react-redux.js.org/api/hooks#usedispatch
 */
export const useDispatch = () => {
	const storeRef = useContext(StoreContext);

	if (process.env.NODE_ENV === 'test' && !storeRef) {
		throw new Error('StoreContextProvider was not found');
	}

	return storeRef.current.dispatch;
};

/**
 * Default compare function used inside useSelector, supports comparing:
 * - Primitive types
 * - Array elements
 * - Object properties
 * Both array and object properties are compared only to first level.
 * @param a
 * @param b
 * @returns {boolean}
 */
const defaultCompareEqual = (a, b) => {
	if (a === b) {
		return true;
	}

	if (a instanceof Array && b instanceof Array) {
		if (a.length !== b.length) {
			return false;
		}

		return a.every((item, index) => {
			return item === b[index];
		});
	}

	if (a && b && typeof a === 'object' && typeof b === 'object') {
		if (Object.keys(a).length !== Object.keys(b).length) {
			return false;
		}

		return Object.entries(a).every(([key, value]) => {
			return b[key] === value;
		});
	}

	return false;
}

/**
 * @param {function} selector Pure function that receives state and returns
 *  any content generated from it.
 * @param {Array} [dependencies=[]] List of external dependencies that are
 *  being used inside selector, so it refresh when these change.
 * @param {function} [compareEqual=defaultCompareEqual] Function to check if
 *  the generated content is equals or not.
 * @see https://react-redux.js.org/api/hooks#useselector
 */
export const useSelector = (
	selector,
	dependencies = [],
	compareEqual = defaultCompareEqual
) => {
	const storeRef = useContext(StoreContext);

	if (process.env.NODE_ENV === 'test' && !storeRef) {
		throw new Error('StoreContextProvider was not found');
	}

	const initialState = useMemo(
		() => selector(storeRef.current.getState()),

		// We really want to call selector here just on component mount.
		// This provides an initial value that will be recalculated when
		// store suscription has been called.
		// eslint-disable-next-line
		[]
	);

	const selectorCallback = useCallback(selector, dependencies);
	const [selectorState, setSelectorState] = useState(initialState);

	useEffect(() => {
		const store = storeRef.current;

		const updateSelectorState = () => {
			const nextState = selectorCallback(storeRef.current.getState());

			if (!compareEqual(selectorState, nextState)) {
				setSelectorState(nextState);
			}
		}

		const onStoreChange = () => {
			updateSelectorState();
		};

		store.subscribe(onStoreChange);
		updateSelectorState();

		return () => {
			store.unsubscribe(onStoreChange);
		};
	}, [selectorState, storeRef, selectorCallback, compareEqual]);

	return selectorState;
};
