/*
 *  Copyright 2013 Anyware Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * Helper for editing a group
 * @private
 */
Ext.define('Ametys.plugins.core.groups.EditGroupHelper', {
	singleton: true,
	
	/**
	 * @property {Boolean} _initialized True if the dialog box creation process has been done.
	 * @private
	 */
	/**
	 * @property {String} _mode The current edition mode ('new' or 'edit')
	 * @private
	 */
	/**
	 * @property {String} _groupsManagerRole The role of the groups manager
	 * @private
	 */
	/**
	 * @property {String} _groupMessageTargetType The type of message target for group
	 * @private
	 */
	
	/**
	 * Open dialog box to create a new group
	 * @param {String} groupsManagerRole the role of groups manager. If null the default groups manager will be used.
	 * @param {String} [groupMessageTargetType=group] the type of group message target
	 * @param {Function} [callback] the callback function. Parameters are:
	 * @param {Object} login callback.group The group's properties
	 */
	add: function (groupsManagerRole, groupMessageTargetType, callback)
	{
		this._mode = 'new';
		this._groupsManagerRole = groupsManagerRole;
		this._groupMessageTargetType = groupMessageTargetType;
		this._callback = callback;
		
		this._open (null);
	},
	
	/**
	 * Open dialog box to to edit group's information
	 * @param {String} id the id of group to edit
	 * @param {String} groupsManagerRole the role of groups manager. If null default manager will be used.
	 * @param {String} [groupMessageTargetType=group] the type of group message target
	 * @param {Function} [callback] the callback function. Parameters are:
	 * @param {Object} login callback.group The group's properties
	 */
	edit: function (id, groupsManagerRole, groupMessageTargetType, callback)
	{
		this._mode = 'edit';
		this._groupsManagerRole = groupsManagerRole;
		this._groupMessageTargetType = groupMessageTargetType;
		this._callback = callback;
		
		this._open (id);
	},
	
	/**
	 * @private
	 * Show dialog box for group edition
	 * @param {String} [id] The group's id. Can be null in 'new' mode
	 */
	_open: function (id)
	{
		this._delayedInitialize();
		this._box.show();
		this._initForm (id);
	},
	
	/**
	 * @private
	 * Creates the dialog if needed.
	 */
	_delayedInitialize: function ()
	{
		// Initialize only once.
		if (!this._initialized)
		{
			this._form = Ext.create('Ext.form.FormPanel', {
				border: false,
				scrollable: true,
				defaults: {
					cls: 'ametys',
					msgTarget: 'side',
					anchor: '90%',
					labelAlign: 'right',
					labelSeparator: '',
					labelWidth: 100
				},
				
				items: [{
					xtype: 'textfield',
					fieldLabel: "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_DIALOG_NAME'  i18n:catalogue='plugin.core'/>",
					name: 'name',
					allowBlank: false
				}, {
					xtype: 'hidden',
					name: 'id'
				}]
			});
			
			this._box = Ext.create('Ametys.window.DialogBox', {
				title: this._mode == 'new' ? "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_DIALOG_ADD_TITLE' i18n:catalogue='plugin.core'/>" : "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_DIALOG_EDIT_TITLE' i18n:catalogue='plugin.core'/>",
				icon: Ametys.getPluginResourcesPrefix('core') + '/img/groups/' + (this._mode == 'new' ? 'add_16.png' : 'rename_16.png'),
				
				width: 450,
				maxHeight: 500,
				
				items: [ this._form ],
				
				closeAction: 'hide',
				buttons : [{
					text: "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_DIALOG_OK' i18n:catalogue='plugin.core'/>",
					handler: Ext.bind(this._validate, this)
				}, {
					text: "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_DIALOG_CANCEL' i18n:catalogue='plugin.core'/>",
					handler: Ext.bind(function() {this._box.hide();}, this)
				}]
			});
			
			this._initialized = true;
		}
		else
		{
			this._box.setTitle(this._mode == 'new' ? "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_DIALOG_ADD_TITLE' i18n:catalogue='plugin.core'/>" : "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_DIALOG_EDIT_TITLE' i18n:catalogue='plugin.core'/>");
			this._box.setIcon(Ametys.getPluginResourcesPrefix('core') + '/img/groups/' + (this._mode == 'new' ? 'add_16.png' : 'rename_16.png'));
		}
	},
	
	/**
	 * @private
	 * Initialize the form
	 * @param {String} [id] The group's id. Can not be null in edition mode.
	 */
	_initForm: function (id)
	{
		if (this._mode == 'new')
		{
			this._form.getForm().reset();
			this._form.getForm().findField('name').focus(true);
		}
		else
		{
			Ametys.plugins.core.groups.GroupsDAO.getGroup([id, this._groupsManagerRole], this._getGroupCb, {scope: this});
		}
	},
	
	/**
	 * @private
	 * Callback function invoked after retrieving group's properties
	 * Initialize the form
	 * @param {Object} group the group's properties
	 */
	_getGroupCb: function (group)
	{
		this._form.getForm().findField('id').setValue(group.id);
		this._form.getForm().findField('name').setValue(group.label);
		this._form.getForm().findField('name').focus(true);
	},
	
	/**
	 * @private
	 * Validates the dialog box.
	 * Creates or edits group.
	 */
	_validate: function()
	{
		if (!this._form.isValid())
		{
			return;
		}
		
		var values = this._form.getValues();
		if (this._mode == 'new')
		{
			Ametys.plugins.core.groups.GroupsDAO.addGroup([values.name, this._groupsManagerRole, this._groupMessageTargetType], this._editGroupCb, {scope:this, waitMessage: {target: this._box}});
		}
		else
		{
			Ametys.plugins.core.groups.GroupsDAO.renameGroup([values.id, values.name, this._groupsManagerRole, this._groupMessageTargetType], this._editGroupCb, {scope:this, waitMessage: {target: this._box}});
		}
	},

	/**
	 * @private
	 * Callback function invoked after group creation/edition process is over.
	 * @param {Object} group the added/edited group or the errors
	 * @param {Object} args the callback arguments
	 */
	_editGroupCb: function (group, args)
	{
		if (group.errors)
		{
			Ametys.Msg.show ({
                title: "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_UNKNOWN_GROUP_TITLE'/>",
                msg: "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_UNKNOWN_GROUP_ERROR'/>",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
			});
			return;
		}
		
		this._box.hide();
		
		if (Ext.isFunction (this._callback))
		{
			this._callback (group)
		}
	}
});
