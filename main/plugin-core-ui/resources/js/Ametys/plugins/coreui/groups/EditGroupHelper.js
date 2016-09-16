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
Ext.define('Ametys.plugins.coreui.groups.EditGroupHelper', {
	singleton: true,
	
    /**
     * @property {Boolean} _chooseGroupDirectoryInitialized True if the dialog box for choosing group directory is initialized.
     * @private
     */
	/**
	 * @property {Boolean} _initialized True if the dialog box creation process has been done.
	 * @private
	 */
	/**
	 * @property {String} _mode The current edition mode ('new' or 'edit')
	 * @private
	 */
    /**
     * @property {String[]} _contexts The contexts for the group directories to display in the combobox.
     * @private
     */
	/**
	 * @property {String} _groupMessageTargetType The type of message target for group
	 * @private
	 */
    /**
     * @property {Ametys.window.DialogBox} _chooseGroupDirectoryDialog The dialog box for choosing the group directory before creating a group.
     * @private
     */
    /**
     * @property {Ametys.window.DialogBox} _box The dialog box for creating/renaming a group.
     * @private
     */
	
	/**
	 * Open dialog box to create a new group
     * @param {String[]} contexts The contexts for the group directories to display in the combobox.
	 * @param {String} [groupMessageTargetType=group] the type of group message target
	 * @param {Function} [callback] the callback function. Parameters are:
	 * @param {Object} callback.group The group's properties
	 */
	add: function (contexts, groupMessageTargetType, callback)
	{
		this._mode = 'new';
        this._contexts = contexts;
		this._groupMessageTargetType = groupMessageTargetType;
		this._callback = callback;
		
		//this._open (null);
        
        if (!this._chooseGroupDirectoryInitialized)
        {
            this._chooseGroupDirectoryDialog = Ext.create('Ametys.window.DialogBox', {
                title: "{{i18n PLUGINS_CORE_UI_GROUPS_DIALOG_ADD_TITLE}}",
                iconCls: 'ametysicon-multiple25 decorator-ametysicon-agenda3',
                
                layout: {
                    type: 'vbox',
                    align : 'stretch',
                    pack  : 'start'
                },
                width: 450,
                
                defaultFocus: 'groupDirectories',
                items: [
                    {
                        xtype: 'component',
                        html: "{{i18n PLUGINS_CORE_UI_GROUPS_DIALOG_CHOOSE_GROUP_DIRECTORY_HINT}}",
                        height: 25
                    },
                    {
                        xtype: 'form',
                        itemId: 'form',
                        defaults: {
                            xtype: 'combobox',
                            cls: 'ametys',
                            labelWidth: 150,
                            displayField: 'label',
                            queryMode: 'local',
                            forceSelection: true,
                            triggerAction: 'all',
                            allowBlank: false
                        },
                        items: [{
                            fieldLabel: "{{i18n PLUGINS_CORE_UI_TOOL_GROUPS_GROUP_DIRECTORY_COMBOBOX}}",
                            name: "groupDirectories",
                            itemId: "groupDirectories",
                            store: {
                                fields: ['id', 'label'],
                                proxy: {
                                    type: 'ametys',
                                    plugin: 'core-ui',
                                    url: 'modifiable-group-directories.json',
                                    reader: {
                                        type: 'json',
                                        rootProperty: 'groupDirectories'
                                    }
                                },
                                listeners: {
                                    'beforeload': Ext.bind(function(store, operation) {
                                        operation.setParams( Ext.apply(operation.getParams() || {}, {
                                            contexts: this._contexts
                                        }));
                                    }, this),
                                    'load': Ext.bind(function(store, records) {
                                        if (records.length == 0)
                                        {
                                            this._chooseGroupDirectoryDialog.close();
                                            Ext.Msg.show({
                                                title: "{{i18n PLUGINS_CORE_UI_GROUPS_DIALOG_NO_MODIFIABLE_DIRECTORY_WARNING_TITLE}}",
                                                msg: "{{i18n PLUGINS_CORE_UI_GROUPS_DIALOG_NO_MODIFIABLE_DIRECTORY_WARNING_MSG}}",
                                                buttons: Ext.Msg.OK,
                                                icon: Ext.MessageBox.INFO
                                            });
                                        }
                                    }, this)
                                }
                            },
                            valueField: 'id'
                        }]
                    }
                ],
                
                closeAction: 'hide',
                
                referenceHolder: true,
                defaultButton: 'next',
                
                buttons : [{
                	reference: 'next',
                    text: "{{i18n PLUGINS_CORE_UI_GROUPS_DIALOG_NEXT}}",
                    handler: Ext.bind(function() {
                        if (!this._chooseGroupDirectoryDialog.down('#form').isValid())
                        {
                            return;
                        }
                        var groupDirectoryId = this._chooseGroupDirectoryDialog.down('#groupDirectories').getValue();
                        this._open(groupDirectoryId, null);
                        this._chooseGroupDirectoryDialog.close();
                    },this)
                }, {
                    text: "{{i18n PLUGINS_CORE_UI_GROUPS_DIALOG_CANCEL}}",
                    handler: Ext.bind(function() {this._chooseGroupDirectoryDialog.close();}, this)
                }]
            });
            
            this._chooseGroupDirectoryInitialized = true;
        }
        
        this._chooseGroupDirectoryDialog.show();
        this._chooseGroupDirectoryDialog.down('#groupDirectories').getStore().load({
            scope: this,
            callback: function(records) {
                var selectSuccess = false;
                // If GroupsTool opened, try to select the same value in the combobox
                var tool = Ametys.tool.ToolsManager.getTool("uitool-groups");
                if (tool != null)
                {
                    var groupDirectoryId = tool.getDirectoryComboValue();
                    if (this._chooseGroupDirectoryDialog.down('#groupDirectories').getStore().getById(groupDirectoryId) != null)
                    {
                        this._chooseGroupDirectoryDialog.down('#groupDirectories').select(groupDirectoryId);
                        selectSuccess = true;
                    }
                }
                // Otherwise, select the first data
                if (records.length > 0 && !selectSuccess)
                {
                    this._chooseGroupDirectoryDialog.down('#groupDirectories').select(records[0].get('id'));
                }
            }
        });
	},
	
	/**
	 * Open dialog box to to edit group's information
     * @param {String} groupDirectoryId The id of the group directory.
	 * @param {String} id the id of group to edit
	 * @param {String} [groupMessageTargetType=group] the type of group message target
	 * @param {Function} [callback] the callback function. Parameters are:
	 * @param {Object} callback.group The group's properties
	 */
	edit: function (groupDirectoryId, id, groupMessageTargetType, callback)
	{
		this._mode = 'edit';
		this._groupMessageTargetType = groupMessageTargetType;
		this._callback = callback;
		
		this._open (groupDirectoryId, id);
	},
	
	/**
	 * @private
	 * Show dialog box for group edition
	 * @param {String} groupDirectoryId The id of the group directory.
	 * @param {String} [id] The group's id. Can be null in 'new' mode
	 */
	_open: function (groupDirectoryId, id)
	{
		this._delayedInitialize(groupDirectoryId);
		this._initForm (groupDirectoryId, id);
	},
	
	/**
	 * @private
	 * Creates the dialog if needed.
     * @param {String} groupDirectoryId The id of the group directory.
	 */
	_delayedInitialize: function (groupDirectoryId)
	{
		// Initialize only once.
		if (!this._initialized)
		{
			this._form = Ext.create('Ext.form.FormPanel', {
				border: false,
				bodyPadding: 5,
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
					fieldLabel: "{{i18n PLUGINS_CORE_UI_GROUPS_DIALOG_NAME}}",
					name: 'name',
					itemId: 'name',
					allowBlank: false
				}, {
					xtype: 'hidden',
					name: 'id'
				}]
			});
			
			this._box = Ext.create('Ametys.window.DialogBox', {
				title: this._mode == 'new' ? "{{i18n PLUGINS_CORE_UI_GROUPS_DIALOG_ADD_TITLE}}" : "{{i18n PLUGINS_CORE_UI_GROUPS_DIALOG_EDIT_TITLE}}",
				iconCls: 'ametysicon-multiple25 ' + (this._mode == 'new' ? 'decorator-ametysicon-add64' : 'decorator-ametysicon-edit45'),
				
				width: 450,
				maxHeight: 500,
				
				items: [ this._form ],
				
				closeAction: 'hide',
				
				referenceHolder: true,
				defaultButton: 'validate',
				
				defaultFocus: 'name',
				selectDefaultFocus: true,
				
				buttons : [{
					reference: 'validate',
					text: "{{i18n PLUGINS_CORE_UI_GROUPS_DIALOG_OK}}",
					handler: Ext.bind(this._validate, this, [groupDirectoryId])
				}, {
					text: "{{i18n PLUGINS_CORE_UI_GROUPS_DIALOG_CANCEL}}",
					handler: Ext.bind(function() {this._box.hide();}, this)
				}]
			});
			
			this._initialized = true;
		}
		else
		{
			this._box.setTitle(this._mode == 'new' ? "{{i18n PLUGINS_CORE_UI_GROUPS_DIALOG_ADD_TITLE}}" : "{{i18n PLUGINS_CORE_UI_GROUPS_DIALOG_EDIT_TITLE}}");
			this._box.setIconCls('ametysicon-multiple25 ' + (this._mode == 'new' ? 'decorator-ametysicon-add64' : 'decorator-ametysicon-edit45'));
		}
	},
	
	/**
	 * @private
	 * Initialize the form
     * @param {String} groupDirectoryId The id of the group directory.
	 * @param {String} [id] The group's id. Can not be null in edition mode.
	 */
	_initForm: function (groupDirectoryId, id)
	{
		if (this._mode == 'new')
		{
			this._form.getForm().reset();
			this._box.show();
		}
		else
		{
			Ametys.plugins.core.groups.GroupsDAO.getGroup([groupDirectoryId, id], this._getGroupCb, {scope: this});
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
		this._box.show();
	},
	
	/**
	 * @private
	 * Validates the dialog box.
	 * Creates or edits group.
     * @param {String} groupDirectoryId the id of the group directory
	 */
	_validate: function(groupDirectoryId)
	{
		if (!this._form.isValid())
		{
			return;
		}
		
		var values = this._form.getValues();
		if (this._mode == 'new')
		{
			Ametys.plugins.core.groups.GroupsDAO.addGroup([groupDirectoryId, values.name, this._groupMessageTargetType], this._editGroupCb, {scope:this, waitMessage: {target: this._box}});
		}
		else
		{
			Ametys.plugins.core.groups.GroupsDAO.renameGroup([groupDirectoryId, values.id, values.name, this._groupMessageTargetType], this._editGroupCb, {scope:this, waitMessage: {target: this._box}});
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
                title: "{{i18n PLUGINS_CORE_UI_GROUPS_UNKNOWN_GROUP_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_UI_GROUPS_UNKNOWN_GROUP_ERROR}}",
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
