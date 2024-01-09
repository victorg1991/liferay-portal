/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
import ClayModal, {useModal} from "@clayui/modal";
import ClayLoadingIndicator from "@clayui/loading-indicator";
import {useIsMounted} from '@liferay/frontend-js-react-web';
import {fetch, runScriptsInElement} from 'frontend-js-web';
import React, {useEffect, useState} from "react";
import ClayButton from "@clayui/button";

export default function PermissionsModal({permissionsURL, onPublishButtonClick}) {
	const {observer, onClose} = useModal();
	const [loading, setLoading] = useState(true);
	const [content, setContent] = useState('');
	const isMounted = useIsMounted();

	useEffect(() => {
		fetch(permissionsURL)
			.then((response) => response.text())
			.then((content) => {
				if (isMounted()) {
					setContent(content);
					setLoading(false);
				}
			})
			.catch((error) => {
				if (process.env.NODE_ENV === 'development') {
					console.error(error);
				}
			});
	}, [isMounted, permissionsURL]);

	return <ClayModal
		size="lg"
		observer={observer}
	>
		<ClayModal.Header> {Liferay.Language.get('publish-web-content')} </ClayModal.Header>

		<ClayModal.Body>
			<p>Confirm the web content visibility before publishing.</p>
			{loading ? (
				<ClayLoadingIndicator />
			) : (
				<PermissionsModalBody content={content} />
			)}
		</ClayModal.Body>

		<ClayModal.Footer
			last={
				<ClayButton.Group spaced>
					<ClayButton displayType="secondary" onClick={onClose}>
						{Liferay.Language.get('cancel')}
					</ClayButton>

					<ClayButton
						displayType="primary"
						onClick={onPublishButtonClick}
						type="submit"
					>
						{Liferay.Language.get('publish')}
					</ClayButton>
				</ClayButton.Group>
			}
		/>
	</ClayModal>
}

class PermissionsModalBody extends React.Component {
	constructor(props) {
		super(props);

		this._ref = React.createRef();
	}

	componentDidMount() {
		if (this._ref.current) {
			runScriptsInElement(this._ref.current);

			this._ref.current.addEventListener('change', this._handleOnChange);
		}
	}
	shouldComponentUpdate() {
		return false;
	}

	render() {
		return (
			<div
				dangerouslySetInnerHTML={{__html: this.props.content}}
				ref={this._ref}
			/>
		);
	}
}