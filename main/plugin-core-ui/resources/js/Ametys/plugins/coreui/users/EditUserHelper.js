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
 * Helper for editing a user
 * @private
 */
Ext.define('Ametys.plugins.coreui.users.EditUserHelper', {
	singleton: true,
	
	/**
	 * @property {Boolean} _chooseUserDirectoryInitialized True if the dialog box for choosing user directory is initialized.
	 * @private
	 */
	/**
	 * @property {String} _mode The current edition mode ('new' or 'edit')
	 * @private
	 */
	/**
	 * @property {String} _usersMessageTargetType The type of message target for user
	 * @private
	 */
    /**
     * @property {Ametys.window.DialogBox} _chooseUserDirectoryDialog The dialog box for choosing the population and the user directory before creating a user.
     * @private
     */
    /**
     * @property {Ametys.window.DialogBox} _box The dialog box for creating/editing a user.
     * @private
     */
	
	/**
	 * Open dialog box to create a new user
	 * @param {String} context The context for the populations to display in the combobox.
	 * @param {String} [userMessageTargetType=user] the type of user message target
	 * @param {String} userToolRole The role of users tool
	 * @param {Function} [callback] the callback function. Parameters are:
	 * @param {Object} callback.user The user's properties
	 */
	add: function (context, userMessageTargetType, userToolRole, callback) //Ametys appParameter
	{
		this._mode = 'new';
		this._userMessageTargetType = userMessageTargetType;
		this._callback = callback;
        
        if (!this._chooseUserDirectoryInitialized)
        {
		    this._chooseUserDirectoryDialog = Ext.create('Ametys.window.DialogBox', {
		        title: "{{i18n PLUGINS_CORE_UI_USERS_DIALOG_ADD_TITLE}}",
		        icon: Ametys.getPluginResourcesPrefix('core-ui') + '/img/users/add_16.png',
		        
		        layout: {
		            type: 'vbox',
		            align : 'stretch',
		            pack  : 'start'
		        },
		        width: 450,
                
		        defaultFocus: 'userPopulations',
		        items: [
		            {
		                xtype: 'component',
                        html: "{{i18n PLUGINS_CORE_UI_USERS_DIALOG_CHOOSE_USER_DIRECTORY_HINT}}",
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
		                items: [
			                {
			                    fieldLabel: "{{i18n PLUGINS_CORE_UI_TOOL_USERS_POPULATION_FIELD}}",
					            name: "userPopulations",
					            itemId: "userPopulations",
		                        store: {
					                fields: ['id', 'label'],
					                proxy: {
					                    type: 'ametys',
					                    plugin: 'core-ui',
					                    url: 'modifiable-populations.json',
					                    reader: {
					                        type: 'json',
					                        rootProperty: 'userPopulations'
					                    }
					                },
					                listeners: {
					                    'beforeload': Ext.bind(function(store, operation) {
		                                    operation.setParams( Ext.apply(operation.getParams() || {}, {
									            context: context
									        }));
		                                }, this),
                                        'load': Ext.bind(function(store, records) {
                                            if (records.length == 0)
                                            {
                                                this._chooseUserDirectoryDialog.close();
                                                Ext.Msg.show({
									                title: "{{i18n PLUGINS_CORE_UI_USERS_DIALOG_NO_MODIFIABLE_POPULATION_WARNING_TITLE}}",
									                msg: "{{i18n PLUGINS_CORE_UI_USERS_DIALOG_NO_MODIFIABLE_POPULATION_WARNING_MSG}}",
									                buttons: Ext.Msg.OK,
									                icon: Ext.MessageBox.INFO
									            });
                                            }
                                        }, this)
					                }
					            },
		                        valueField: 'id',
		                        
		                        listeners: {
					                'change': Ext.bind(function(combo, newValue, oldValue) {
					                    var data = [];
					                    var record = combo.getStore().getById(newValue);
					                    Ext.Array.forEach(record.get('userDirectories'), function(userDirectory, index) {
					                        data.push({
					                            index: userDirectory.index,
					                            label: userDirectory.label
					                        });
					                    }, this);
					                     this._chooseUserDirectoryDialog.down('#userDirectories').getStore().loadData(data, false);
					                }, this)
					            }
			                },
			                {
			                    fieldLabel: "{{i18n PLUGINS_CORE_UI_TOOL_USERS_USER_DIRECTORY_FIELD}}",
					            name: "userDirectories",
					            itemId: "userDirectories",
		                        store: {
					                fields: ['index', 'label'],
					                data: [],
					                listeners: {
					                    'datachanged': Ext.bind(function(store) {
					                        this._chooseUserDirectoryDialog.down('#userDirectories').clearValue();
					                        this._chooseUserDirectoryDialog.down('#userDirectories').setValue(store.getRange()[0]);
					                    }, this)
					                }
					            },
		                        valueField: 'index'
			                }
		                ]
		            }
		        ],
		        
		        closeAction: 'hide',
		        buttons : [{
		            text: "{{i18n PLUGINS_CORE_UI_USERS_DIALOG_NEXT}}",
		            handler: Ext.bind(function() {
                        if (!this._chooseUserDirectoryDialog.down('#form').isValid())
                        {
                            return;
                        }
		                var populationId = this._chooseUserDirectoryDialog.down('#userPopulations').getValue();
		                var udIndex = this._chooseUserDirectoryDialog.down('#userDirectories').getValue();
		                this._open(populationId, udIndex, null);
                        this._chooseUserDirectoryDialog.close();
		            },this)
		        }, {
		            text: "{{i18n PLUGINS_CORE_UI_USERS_DIALOG_CANCEL}}",
		            handler: Ext.bind(function() {this._chooseUserDirectoryDialog.close();}, this)
		        }]
		    });
            
            this._chooseUserDirectoryInitialized = true;
        }
        
        this._chooseUserDirectoryDialog.show();
        this._chooseUserDirectoryDialog.down('#userPopulations').getStore().load({
            scope: this,
            callback: function(records) {
                var selectSuccess = false;
                // If UserTool opened, try to select the same values in the comboboxes
                if (userToolRole)
		        {
		            var tool = Ametys.tool.ToolsManager.getTool(userToolRole);
		            if (tool != null)
		            {
		                var populationId = tool.getPopulationComboValue();
                        if (this._chooseUserDirectoryDialog.down('#userPopulations').getStore().getById(populationId) != null)
                        {
                            this._chooseUserDirectoryDialog.down('#userPopulations').select(populationId);
                            
	                        var userDirectoryIndex = tool.getUserDirectoryComboValue();
                            if (this._chooseUserDirectoryDialog.down('#userDirectories').getStore().findExact('index', userDirectoryIndex) >= 0) // test if the value selected in the tool is present in the dialog box
                            {
		                        this._chooseUserDirectoryDialog.down('#userDirectories').setValue(userDirectoryIndex);
                            }
                            selectSuccess = true;
                        }
		            }
		        }
                // Otherwise, select the first data
                if (records.length > 0 && !selectSuccess)
                {
                    this._chooseUserDirectoryDialog.down('#userPopulations').select(records[0].get('id'));
                }
            }
        });
	},
	
	/**
	 * Open dialog box to to edit user's information
	 * @param {String} login The login of user to edit
	 * @param {String} populationId The id of the population of the user
	 * @param {String} [userMessageTargetType=user] the type of user message target
	 * @param {Function} [callback] the callback function. Parameters are:
	 * @param {Object} callback.user The user's properties
	 */
	edit: function (login, populationId, userMessageTargetType, callback)
	{
		this._mode = 'edit';
		this._userMessageTargetType = userMessageTargetType;
		this._callback = callback;
		
		this._open (populationId, null, login);
	},
	
	/**
	 * @private
	 * Show dialog box for user edition
	 * @param {String} populationId The id of the population of the user. Cannot be null
	 * @param {String} userDirectoryIndex The index of the user directory in its population. Can be null in 'edit' mode
	 * @param {String} login The user's login. Can be null in 'new' mode
	 */
	_open: function (populationId, userDirectoryIndex, login)
	{
		var me = this;
		function configureCallback (success)
		{
			if (success)
			{
				me._box.show();
				me._initForm (login, populationId);
			}
		}
		
		// Create dialog box if needed
		this._createDialogBox(login, populationId, userDirectoryIndex, configureCallback);
	},
	
	/**
	 * @private
	 * Creates the dialog if needed.
     * @param {String} login The user's login. Can be null in 'new' mode.
     * @param {String} populationId The id of the population of the user. Cannot be null.
     * @param {String} userDirectoryIndex The index of the user directory in its population. Can be null in 'edit' mode
	 * @param {Function} callback Function to called after drawing box.
	 */
	_createDialogBox: function (login, populationId, userDirectoryIndex, callback)
	{
		this._form = Ext.create('Ametys.form.ConfigurableFormPanel', {
			'tab-policy-mode': 'default',
            'fieldNamePrefix': '',
			showAmetysComments: false,
			
			defaultFieldConfig: {
				labelWidth: 100
			}
		});
		
		this._box = Ext.create('Ametys.window.DialogBox', {
			title: this._mode == 'new' ? "{{i18n PLUGINS_CORE_UI_USERS_DIALOG_ADD_TITLE}}" : "{{i18n PLUGINS_CORE_UI_USERS_DIALOG_EDIT_TITLE}}",
			icon: Ametys.getPluginResourcesPrefix('core-ui') + '/img/users/' + (this._mode == 'new' ? 'add_16.png' : 'modify_16.png'),
			
			layout: 'fit',
			width: 500,
			maxHeight: 500,
			
			items: [ this._form ],
			
			closeAction: 'hide',
			buttons : [{
				text: "{{i18n PLUGINS_CORE_UI_USERS_DIALOG_OK}}",
				handler: Ext.bind(this._validate, this, [populationId, userDirectoryIndex])
			}, {
				text: "{{i18n PLUGINS_CORE_UI_USERS_DIALOG_CANCEL}}",
				handler: Ext.bind(function() {this._box.hide();}, this)
			}]
		});
		
		this._configureForm(login, populationId, userDirectoryIndex, callback);
	},
	
	/**
	 * @private
	 * Configures the user edition form.
     * @param {String} login The user's login. Can be null in 'new' mode.
     * @param {String} populationId The id of the population of the user. Cannot be null.
     * @param {Number} userDirectoryIndex The index of the user directory in its population. Can be null in 'edit' mode
	 * @param {Function} callback Function to called after drawing box.
	 */
	_configureForm: function (login, populationId, userDirectoryIndex, callback)
	{
        var parameters = this._mode == 'new' ? [populationId, userDirectoryIndex] : [login, populationId];
        
		Ametys.data.ServerComm.callMethod({
			role: "org.ametys.plugins.core.user.UserDAO",
			methodName: "getEditionModel",
			parameters: parameters,
			callback: {
				handler: this._getEditionModelCb,
				scope: this,
				arguments: [callback]
			},
			errorMessage: true
		});
	},
	
	/**
	 * @private
	 * Callback function after retrieving user's edition model. Configure the form.
	 * @param {Object} model the user's edition model
	 * @param {Object[]} args the callback arguments.
	 */
	_getEditionModelCb: function (model, args)
	{
		model['password'].widget = 'edition.changepassword';
		this._form.configure(model);
		
		args[0](true);
	},
	
	/**
	 * @private
	 * Initialize the form
	 * @param {String} [login] The user's login. Can not be null in edition mode.
	 * @param {String} [populationId] The id of the population of the user. Can not be null in edition mode.
	 */
	_initForm: function (login, populationId)
	{
		if (this._mode == 'new')
		{
			this._form.reset();
			this._form.getForm().findField('login').enable();
			this._form.getForm().findField('login').focus(true);
		}
		else
		{
			Ametys.plugins.core.users.UsersDAO.getUser([login, populationId], this._getUserCb, {scope: this});
		}
	},
	
	/**
	 * @private
	 * Callback function invoked after retrieving user's properties
	 * Initialize the form
	 * @param {Object} user the user's properties
	 */
	_getUserCb: function (user)
	{
	    user.password = "PASSWORD" ;
		this._form.setValues({values: user, comments: {}, repeaters: []});
		this._form.getForm().findField('login').disable();
		
		// focus first field != changepasswordfield
        this._form.getForm().getFields().each(function(item, index, length) {
            if (item.isDisabled() || item.type == 'password')
            {
                return true;
            }
            else
            {
                item.focus(true);
                return false;
            }
        });
	},
	
	/**
	 * @private
	 * Validates the dialog box.
	 * Creates or edits user.
     * @param {String} populationId The id of the population the user belongs to ('edit' mode) or where the user has to be created ('new' mode)
     * @param {Number} userDirectoryIndex The index of the user directory in its population. Can be null in 'edit' mode
	 */
	_validate: function(populationId, userDirectoryIndex)
	{
		if (!this._form.isValid())
		{
			return;
		}
		
		var values = this._form.getValues();
		if (this._mode == 'new')
		{
			Ametys.plugins.core.users.UsersDAO.addUser([populationId, userDirectoryIndex, values, this._userMessageTargetType], this._editUserCb, {scope: this, waitMessage: {target: this._box}});
		}
		else
		{
			values.login = this._form.getForm().findField('login').getValue(); // disabled field
			Ametys.plugins.core.users.UsersDAO.editUser([populationId, values, this._userMessageTargetType], this._editUserCb, {scope:this, waitMessage: {target: this._box}});
		}
	},

	/**
	 * @private
	 * Callback function invoked after group creation/edition process is over.
	 * @param {Object} user the added/edited user or the errors
	 * @param {Object} args the callback arguments
	 */
	_editUserCb: function (user, args)
	{
		if (user.errors)
		{
			var errors = user.errors;
			Ext.Array.forEach(errors, function(error) {
				var fd = this._form.findField(error);
				if (fd)
				{
					fd.markInvalid("{{i18n PLUGINS_CORE_UI_USERS_DIALOG_INVALID_FIELD}}");
				}
			}, this);
			
			return;
		}
		
		this._box.hide();
		
		if (Ext.isFunction (this._callback))
		{
			this._callback (user)
		}
	}
});
