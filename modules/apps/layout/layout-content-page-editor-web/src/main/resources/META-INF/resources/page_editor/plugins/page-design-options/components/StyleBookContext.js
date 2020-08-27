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

import React, {useContext, useState} from 'react';

import {config} from '../../../app/config/index';

const StyleBookDispatchContext = React.createContext(() => {});

const StyleBookStateContext = React.createContext({
	frontendTokens: [],
	label: '',
	styleBookEntryId: '',
});

export const StyleBookContextProvider = ({children}) => {
	const [state, setState] = useState({
		frontendTokens: config.frontendTokens,
		label: config.defaultStyleBookEntryName,
		styleBookEntryId: config.styleBookEntryId,
	});

	return (
		<StyleBookDispatchContext.Provider value={setState}>
			<StyleBookStateContext.Provider value={state}>
				{children}
			</StyleBookStateContext.Provider>
		</StyleBookDispatchContext.Provider>
	);
};

export const useSetStyleBook = () => {
	return useContext(StyleBookDispatchContext);
};

export const useStyleBookEntryId = () => {
	return useContext(StyleBookStateContext).styleBookEntryId;
};

export const useStyleBookFrontendTokens = () => {
	return useContext(StyleBookStateContext).frontendTokens;
};

export const useStyleBookLabel = () => {
	return useContext(StyleBookStateContext).label;
};
