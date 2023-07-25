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

import ClayIcon from '@clayui/icon';
import ClayLayout from '@clayui/layout';
import ClayPanel from '@clayui/panel';
import {sub} from 'frontend-js-web';
import React, {useState} from 'react';

interface ImportResult {
	message: string;
	name: string;
	type: 'fragment' | 'composition';
}

export interface ImportResultsData {
	'imported': ImportResult[];
	'imported-draft': ImportResult[];
	'invalid': ImportResult[];
}

interface Props {
	fileName: string | null;
	importResults: ImportResultsData;
}

function ImportResults({fileName, importResults}: Props) {
	const [expanded, setExpanded] = useState(
		!importResults['imported-draft'] && !importResults.invalid
	);

	return (
		<>
			{importResults.imported && (
				<ClayLayout.Sheet size="lg">
					<ClayPanel
						collapsable
						displayTitle={
							<ClayPanel.Title>
								<ClayIcon
									className="text-4 text-success"
									symbol="check-circle-full"
								/>

								<span className="c-ml-3 font-weight-semi-bold text-4 text-success">
									{sub(
										Liferay.Language.get(
											'x-items-were-imported'
										),
										importResults.imported.length
									)}
								</span>
							</ClayPanel.Title>
						}
						expanded={expanded}
						onExpandedChange={() => {
							setExpanded(!expanded);
						}}
						showCollapseIcon
					>
						<ClayPanel.Body>
							<ul className="list-group sidebar-list-group">
								{importResults.imported.map((result, index) => (
									<li
										className="list-group-item list-group-item-flex p-0"
										key={index}
									>
										<ClayLayout.ContentCol expand>
											<div className="list-group-title">
												{result.name}
											</div>
										</ClayLayout.ContentCol>
									</li>
								))}
							</ul>
						</ClayPanel.Body>
					</ClayPanel>
				</ClayLayout.Sheet>
			)}

			{importResults['imported-draft'] && (
				<ClayLayout.Sheet size="lg">
					<ClayPanel
						displayTitle={
							<ClayPanel.Title>
								<ClayIcon
									className="text-4 text-warning"
									symbol="warning-full"
								/>

								<span className="c-ml-3 font-weight-semi-bold text-4 text-warning">
									{sub(
										Liferay.Language.get(
											'x-items-were-imported-with-warnings'
										),
										importResults['imported-draft'].length
									)}
								</span>
							</ClayPanel.Title>
						}
					>
						<ClayPanel.Body>
							<ul className="list-group sidebar-list-group">
								{importResults['imported-draft'].map(
									(result, index) => (
										<li
											className="list-group-item list-group-item-flex p-0"
											key={index}
										>
											<ClayLayout.ContentCol expand>
												<div className="list-group-title">
													{result.name}
												</div>

												<div className="list-group-subtext text-warning">
													{result.message}
												</div>
											</ClayLayout.ContentCol>

											<ClayLayout.ContentCol>
												<div className="list-group-subtitle">
													{fileName}
												</div>
											</ClayLayout.ContentCol>
										</li>
									)
								)}
							</ul>
						</ClayPanel.Body>
					</ClayPanel>
				</ClayLayout.Sheet>
			)}

			{importResults.invalid && (
				<ClayLayout.Sheet size="lg">
					<ClayPanel
						displayTitle={
							<ClayPanel.Title>
								<ClayIcon
									className="text-4 text-danger"
									symbol="exclamation-full"
								/>

								<span className="c-ml-3 font-weight-semi-bold text-4 text-danger">
									{sub(
										Liferay.Language.get(
											'x-items-could-not-be-imported'
										),
										importResults.invalid.length
									)}
								</span>
							</ClayPanel.Title>
						}
					>
						<ClayPanel.Body>
							<ul className="list-group sidebar-list-group">
								{importResults.invalid.map((result, index) => (
									<li
										className="list-group-item list-group-item-flex p-0"
										key={index}
									>
										<ClayLayout.ContentCol expand>
											<div className="list-group-title">
												{result.name}
											</div>

											<div className="list-group-subtext text-danger">
												{result.message
													? result.message
													: Liferay.Language.get(
															'the-definition-of-the-item-is-invalid'
													  )}
											</div>
										</ClayLayout.ContentCol>

										<ClayLayout.ContentCol>
											<div className="list-group-subtitle">
												{fileName}
											</div>
										</ClayLayout.ContentCol>
									</li>
								))}
							</ul>
						</ClayPanel.Body>
					</ClayPanel>
				</ClayLayout.Sheet>
			)}
		</>
	);
}

export default ImportResults;
