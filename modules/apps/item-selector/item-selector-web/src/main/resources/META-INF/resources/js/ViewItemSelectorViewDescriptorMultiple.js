/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import getOpener from 'frontend-js-web';

export default function ({
	itemSelectorSelectedEvent,
	itemSelectorReturnType,
	portletNamespace
}){
	const searchContainer = Liferay.SearchContainer.get(
		`${portletNamespace}entries`
	);

	searchContainer.on('rowToggled', (event) => {
		const searchContainerItems = event.elements.allSelectedElements;

		let arr = [];

		searchContainerItems.each(function () {
			let domElement = this.ancestor('li');

			if (domElement == null) {
				domElement = this.ancestor('tr');
			}

			if (domElement == null) {
				domElement = this.ancestor('dd');
			}

			if (domElement != null) {
				const itemValue = domElement.getDOM().dataset.value;

				arr.push(itemValue);
			}
		});

		const openerWindow = getOpener();

		openerWindow.Liferay.fire(itemSelectorSelectedEvent, {
			data: {
				returnType: itemSelectorReturnType,
				value: arr
			},
		});
	});
}