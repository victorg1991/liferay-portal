/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {TreeView as ClayTreeView} from '@clayui/core';
import ClayIcon from "@clayui/icon";
import ClayEmptyState from "@clayui/empty-state";
import React from "react";

export default function SelectLayoutPageTemplateCollection({
	layoutPageTemplateCollections,
}){

	return layoutPageTemplateCollections ? (
		<ClayTreeView
			items={layoutPageTemplateCollections}
			showExpanderOnHover={false}
		>
			{(item) => (
				<ClayTreeView.Item>
					<ClayTreeView.ItemStack>
						<ClayIcon symbol="folder"/>

						{item.name}
					</ClayTreeView.ItemStack>

					<ClayTreeView.Group items={item.children}>
						{(item) => (
							<ClayTreeView.Item>
								<ClayIcon symbol="folder"/>

								{item.name}
							</ClayTreeView.Item>
						)}
					</ClayTreeView.Group>
				</ClayTreeView.Item>
			)}
		</ClayTreeView>
	) : (
		<ClayEmptyState
			imgSrc={`${themeDisplay.getPathThemeImages()}/states/search_state.gif`}
			small
			title={Liferay.Language.get('no-results-found')}
		/>
	)
}