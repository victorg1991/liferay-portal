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

import {useEffect, useMemo} from 'react';

import {useStyleBookFrontendTokens} from './StyleBookContext';

export const StyleBookRenderer = () => {
	const frontendTokens = useStyleBookFrontendTokens();
	const wrapper = useMemo(() => document.getElementById('wrapper'), []);

	useEffect(() => {
		if (frontendTokens && wrapper) {
			frontendTokens.forEach((token) => {
				wrapper.style.setProperty(
					`--${token.cssVariable}`,
					token.value
				);
			});
		}

		return () => {
			if (frontendTokens && wrapper) {
				frontendTokens.forEach((token) => {
					wrapper.style.removeProperty(`--${token.cssVariable}`);
				});
			}
		};
	}, [frontendTokens, wrapper]);

	return null;
};
