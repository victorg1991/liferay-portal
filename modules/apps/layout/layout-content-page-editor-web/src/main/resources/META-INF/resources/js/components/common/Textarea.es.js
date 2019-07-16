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

/* eslint no-unused-vars: "warn" */

import React, {useState} from 'react';

const Textarea = props => {
	const [empty, setEmpty] = useState(true);

	const _handleChange = event => {
		if (props._handleChange) {
			props._handleChange(event);
		}

		if (event.target) {
			setEmpty(!event.target.value);
		}
	};

	return (
		<textarea
			className={`form-control fragments-editor__textarea ${
				empty ? 'fragments-editor__textarea--empty' : ''
			}`}
			onChange={_handleChange}
			{...props}
		/>
	);
};

export {Textarea};
export default Textarea;
