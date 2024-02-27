
import {TreeView as ClayTreeView} from '@clayui/core';
import ClayIcon from "@clayui/icon";
import ClayEmptyState from "@clayui/empty-state";
import React from "react";

export default function SelectLayoutPageTemplateCollection({
	layoutPageTemplateCollections,
}){
	return (
		<>
			<ClayTreeView
				items={layoutPageTemplateCollections}
				showExpanderOnHover={false}
			>
				{(item) => (
					<ClayTreeView.Item>
						<ClayTreeView.ItemStack
							onClick={(event) => onClick(event, item)}
							onKeyUp={(event) => onKeyUp(event, item)}
						>
							<ClayIcon symbol="folder"/>

							{item.name}
						</ClayTreeView.ItemStack>

						<ClayTreeView.Group items={item.children}>
							{(item) => (
								<ClayTreeView.Item
									onClick={(event) => onClick(event, item)}
									onKeyUp={(event) => onKeyUp(event, item)}
								>
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
				description={Liferay.Language.get(
					'try-again-with-a-different-search'
				)}
				imgSrc={`${themeDisplay.getPathThemeImages()}/states/search_state.gif`}
				small
				title={Liferay.Language.get('no-results-found')}
			/>
			);
		</>
	)}