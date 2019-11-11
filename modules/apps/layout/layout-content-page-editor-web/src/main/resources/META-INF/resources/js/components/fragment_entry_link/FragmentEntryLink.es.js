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

import Component from 'metal-component';
import Soy from 'metal-soy';
import {Config} from 'metal-state';

import '../floating_toolbar/fragment_configuration/FloatingToolbarFragmentConfigurationPanel.es';

import './FragmentEntryLinkContent.es';
import {
	MOVE_FRAGMENT_ENTRY_LINK,
	UPDATE_SELECTED_SIDEBAR_PANEL_ID
} from '../../actions/actions.es';
import {duplicateFragmentEntryLinkAction} from '../../actions/duplicateFragmentEntryLink.es';
import {removeFragmentEntryLinkAction} from '../../actions/removeFragmentEntryLinks.es';
import {
	disableSavingChangesStatusAction,
	enableSavingChangesStatusAction,
	updateLastSaveDateAction
} from '../../actions/saveChanges.es';
import {updateActiveItemAction} from '../../actions/updateActiveItem.es';
import {getConnectedComponent} from '../../store/ConnectedComponent.es';
import {
	shouldUpdatePureComponent,
	onPropertiesChanged
} from '../../utils/FragmentsEditorComponentUtils.es';
import {
	getFragmentColumn,
	getFragmentRowIndex,
	getItemMoveDirection,
	getItemPath,
	getTargetBorder,
	getWidget,
	itemIsInPath
} from '../../utils/FragmentsEditorGetUtils.es';
import {
	moveItem,
	moveRow,
	removeItem
} from '../../utils/FragmentsEditorUpdateUtils.es';
import {computeConfigurationEditableValue} from '../../utils/computeValues.es';
import {
	FLOATING_TOOLBAR_BUTTONS,
	FRAGMENTS_EDITOR_ITEM_TYPES,
	FRAGMENTS_EDITOR_ROW_TYPES,
	FREEMARKER_FRAGMENT_ENTRY_PROCESSOR
} from '../../utils/constants';
import {isDropZone} from '../../utils/isDropZone.es';
import FloatingToolbar from '../floating_toolbar/FloatingToolbar.es';
import templates from './FragmentEntryLink.soy';

/**
 * FragmentEntryLink
 * @review
 */
class FragmentEntryLink extends Component {
	/**
	 * @inheritdoc
	 */
	created() {
		this._handleFloatingToolbarButtonClicked = this._handleFloatingToolbarButtonClicked.bind(
			this
		);

		onPropertiesChanged(
			this,
			['_isActive', 'hasUpdatePermissions', 'fragmentEntryLinkId'],
			() => {
				if (this._isActive && this._hasUpdatePermissions) {
					this._createFloatingToolbar();
				} else {
					this._disposeFloatingToolbar();
				}
			}
		);
	}

	/**
	 * @inheritdoc
	 */
	disposed() {
		this._disposeFloatingToolbar();
	}

	/**
	 * @inheritdoc
	 * @param {object} state
	 * @return {object}
	 * @review
	 */
	prepareStateForRender(state) {
		return {
			...state,
			itemType: FRAGMENTS_EDITOR_ITEM_TYPES.fragment
		};
	}

	/**
	 * @inheritdoc
	 * @return {boolean}
	 * @review
	 */
	shouldUpdate(changes) {
		delete changes._configuration;
		delete changes._configurationValues;
		delete changes._defaultConfigurationValues;

		return shouldUpdatePureComponent(changes);
	}

	/**
	 * Creates a new instance of the floating toolbar.
	 * @private
	 */
	_createFloatingToolbar() {
		const config = {
			anchorElement: this.element,
			buttons: this._getFloatingToolbarButtons(),
			events: {
				buttonClicked: this._handleFloatingToolbarButtonClicked
			},
			item: {
				configuration: this._configuration,
				configurationValues: this._configurationValues,
				defaultConfigurationValues: this._defaultConfigurationValues,
				fragmentEntryLinkId: this.fragmentEntryLinkId
			},
			itemId: this.fragmentEntryLinkId,
			itemType: FRAGMENTS_EDITOR_ITEM_TYPES.fragment,
			portalElement: document.body,
			store: this.store
		};

		if (this._floatingToolbar) {
			this._floatingToolbar.setState(config);
		} else {
			this._floatingToolbar = new FloatingToolbar(config);
		}
	}

	/**
	 * Duplicate this fragmentEntryLink
	 * @private
	 */
	_duplicateFragmentEntryLink() {
		this.store
			.dispatch(enableSavingChangesStatusAction())
			.dispatch(
				duplicateFragmentEntryLinkAction(
					this.fragmentEntryLinkId,
					this.rowType
				)
			)
			.dispatch(updateLastSaveDateAction())
			.dispatch(disableSavingChangesStatusAction());
	}

	/**
	 * Disposes of an existing floating toolbar instance.
	 * @private
	 */
	_disposeFloatingToolbar() {
		if (this._floatingToolbar) {
			this._floatingToolbar.dispose();

			this._floatingToolbar = null;
		}
	}

	/**
	 * @private
	 * @return {object[]} Floating toolbar buttons
	 * @review
	 */
	_getFloatingToolbarButtons() {
		const buttons = [];

		const widget =
			this._fragmentEntryLink.portletId &&
			getWidget(this.widgets, this._fragmentEntryLink.portletId);

		if (
			!isDropZone(this._fragmentEntryLink) ||
			(widget && widget.instanceable)
		) {
			buttons.push(FLOATING_TOOLBAR_BUTTONS.duplicateFragment);
		}

		if (this._shouldShowConfigPanel()) {
			buttons.push(FLOATING_TOOLBAR_BUTTONS.fragmentConfiguration);
		}

		return buttons;
	}

	/**
	 * Callback executed when an floating toolbar button is clicked
	 * @param {Event} event
	 * @param {Object} data
	 * @private
	 */
	_handleFloatingToolbarButtonClicked(event, data) {
		const {panelId} = data;

		if (panelId === FLOATING_TOOLBAR_BUTTONS.duplicateFragment.panelId) {
			event.preventDefault();

			this._duplicateFragmentEntryLink();
		}
	}

	/**
	 * Handle fragment keyup event so it can emit when it
	 * should be moved or selected.
	 * @param {KeyboardEvent} event
	 * @private
	 * @review
	 */
	_handleFragmentKeyUp(event) {
		event.stopPropagation();

		const direction = getItemMoveDirection(event.keyCode);
		const {fragmentEntryLinkRowType} = event.delegateTarget.dataset;

		if (direction) {
			if (
				fragmentEntryLinkRowType ===
				FRAGMENTS_EDITOR_ROW_TYPES.sectionRow
			) {
				moveRow(
					direction,
					getFragmentRowIndex(
						this.layoutData.structure,
						this.fragmentEntryLinkId
					),
					this.store,
					this.layoutData.structure
				);
			} else {
				const column = getFragmentColumn(
					this.layoutData.structure,
					this.fragmentEntryLinkId
				);
				const fragmentIndex = column.fragmentEntryLinkIds.indexOf(
					this.fragmentEntryLinkId
				);
				const targetFragmentEntryLinkId =
					column.fragmentEntryLinkIds[fragmentIndex + direction];

				if (direction && targetFragmentEntryLinkId) {
					const moveItemPayload = {
						fragmentEntryLinkId: this.fragmentEntryLinkId,
						targetBorder: getTargetBorder(direction),
						targetItemId: targetFragmentEntryLinkId,
						targetItemType: FRAGMENTS_EDITOR_ITEM_TYPES.fragment
					};

					moveItem(
						this.store,
						MOVE_FRAGMENT_ENTRY_LINK,
						moveItemPayload
					);
				}
			}
		}
	}

	/**
	 * @private
	 * @review
	 */
	_handleFragmentCommentsButtonClick() {
		this.store.dispatch(
			updateActiveItemAction(
				this.fragmentEntryLinkId,
				FRAGMENTS_EDITOR_ITEM_TYPES.fragment
			)
		);

		this.store.dispatch({
			type: UPDATE_SELECTED_SIDEBAR_PANEL_ID,
			value: 'comments'
		});
	}

	/**
	 * Callback executed when the fragment remove button is clicked.
	 * @param {Object} event
	 * @private
	 */
	_handleFragmentRemoveButtonClick(event) {
		event.stopPropagation();

		removeItem(
			this.store,
			removeFragmentEntryLinkAction(this.fragmentEntryLinkId)
		);
	}

	/**
	 * Returns wether the config panel should be shown or not
	 * @private
	 * @review
	 */
	_shouldShowConfigPanel() {
		const fieldSetsExist =
			this._configuration &&
			Array.isArray(this._configuration.fieldSets) &&
			this._configuration.fieldSets.length > 0;

		return fieldSetsExist && this._isActive;
	}
}

/**
 * State definition.
 * @review
 * @static
 * @type {!Object}
 */
FragmentEntryLink.STATE = {
	_configuration: Config.object()
		.internal()
		.value(null),
	_configurationValues: Config.object()
		.internal()
		.value(null),
	_defaultConfigurationValues: Config.object()
		.internal()
		.value(null),
	_dropTargetBorder: Config.string()
		.internal()
		.value(null),
	_floatingToolbar: Config.object()
		.internal()
		.value(null),
	_fragmentEntryLink: Config.object()
		.internal()
		.value({}),
	_hasUpdatePermissions: Config.bool()
		.internal()
		.value(true),
	_isActive: Config.bool()
		.internal()
		.value(false),
	_isComponentRow: Config.bool()
		.internal()
		.value(false),
	_isHovered: Config.bool()
		.internal()
		.value(false),
	_showComments: Config.bool()
		.internal()
		.value(false),
	_spritemap: Config.string()
		.internal()
		.value(''),

	fragmentEntryLinkId: Config.string().required(),
	name: Config.string().value(''),
	rowType: Config.string().required(),
	showControlBar: Config.bool().value(true),
	styleModifier: Config.string().value('')
};

const ConnectedFragmentEntryLink = getConnectedComponent(
	FragmentEntryLink,
	[
		'activeItemId',
		'activeItemType',
		'dropTargetItemId',
		'dropTargetItemType',
		'dropTargetBorder',
		'fragmentEntryLinks',
		'hasUpdatePermissions',
		'hoveredItemId',
		'hoveredItemType',
		'layoutData',
		'segmentsExperienceId',
		'sidebarPanels',
		'spritemap',
		'widgets'
	],
	(state, props) => {
		const fragmentEntryLink =
			state.fragmentEntryLinks[props.fragmentEntryLinkId];

		let configurationValues =
			fragmentEntryLink.editableValues[
				FREEMARKER_FRAGMENT_ENTRY_PROCESSOR
			];

		const {configuration, defaultConfigurationValues} = fragmentEntryLink;

		if (configurationValues) {
			const segmentedConfigurationValues = computeConfigurationEditableValue(
				configurationValues,
				{selectedExperienceId: state.segmentsExperienceId}
			);

			configurationValues = {
				...defaultConfigurationValues,
				...segmentedConfigurationValues
			};
		}

		return {
			_configuration: configuration,
			_configurationValues: configurationValues,
			_defaultConfigurationValues: defaultConfigurationValues,
			_dropTargetBorder:
				state.dropTargetItemId === props.fragmentEntryLinkId &&
				state.dropTargetItemType ===
					FRAGMENTS_EDITOR_ITEM_TYPES.fragment
					? state.dropTargetBorder
					: null,
			_fragmentEntryLink: fragmentEntryLink,
			_hasUpdatePermissions: state.hasUpdatePermissions,
			_isActive:
				state.activeItemId === props.fragmentEntryLinkId &&
				state.activeItemType === FRAGMENTS_EDITOR_ITEM_TYPES.fragment,
			_isComponentRow:
				props.rowType === FRAGMENTS_EDITOR_ROW_TYPES.componentRow,
			_isHovered: itemIsInPath(
				getItemPath(
					state.hoveredItemId,
					state.hoveredItemType,
					state.layoutData.structure
				),
				props.fragmentEntryLinkId,
				FRAGMENTS_EDITOR_ITEM_TYPES.fragment
			),
			_showComments: state.sidebarPanels.some(
				sidebarPanel => sidebarPanel.sidebarPanelId === 'comments'
			),
			_spritemap: state.spritemap
		};
	}
);

Soy.register(ConnectedFragmentEntryLink, templates);

export {ConnectedFragmentEntryLink, FragmentEntryLink};

export default ConnectedFragmentEntryLink;
