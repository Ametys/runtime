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
     * @private
     * @property {Ametys.form.field.SelectUserDirectory} _directoryField The main field
     */
	
	/**
	 * Open dialog box to create a new user
	 * @param {String[]} populationContexts The contexts for the populations to display in the combobox.
	 * @param {String} [userMessageTargetType=user] the type of user message target
	 * @param {String} userToolRole The role of users tool
	 * @param {Function} [callback] the callback function. Parameters are:
	 * @param {Object} callback.user The user's properties
	 */
	add: function (populationContexts, userMessageTargetType, userToolRole, callback)
	{
		this._mode = 'new';
		this._userMessageTargetType = userMessageTargetType;
		this._callback = callback;
        
        if (!this._chooseUserDirectoryInitialized)
        {
            this._directoryField = Ext.create("Ametys.form.field.SelectUserDirectory", { 
                itemId: 'userDirectories',
                labelWidth: 150,
                allowBlank: false,
                
                onlyModifiable: true,
                showLabels: true,
                layout: {
                    type: 'vbox',
                    align : 'stretch',
                    pack  : 'start'                    
                }
            });
            this._directoryField.getStore().on('load', function(store, records) {
                if (records.length == 0)
                {
                    this._chooseUserDirectoryDialog.close();
                    Ametys.Msg.show({
                        title: "{{i18n PLUGINS_CORE_UI_USERS_DIALOG_NO_MODIFIABLE_POPULATION_WARNING_TITLE}}",
                        msg: "{{i18n PLUGINS_CORE_UI_USERS_DIALOG_NO_MODIFIABLE_POPULATION_WARNING_MSG}}",
                        buttons: Ext.Msg.OK,
                        icon: Ext.MessageBox.INFO
                    });
                }
                else
                {
                    this._directoryField._userPopulations.select(records[0].get('id'));
                    
                    // If UserTool opened, try to select the same values in the comboboxes
                    if (userToolRole)
                    {
                        var tool = Ametys.tool.ToolsManager.getTool(userToolRole);
                        if (tool != null)
                        {
                            var value = tool.getPopulationComboValue();
                            if (value && value != '#all')
                            {
                                this._directoryField.setValue(value); // First set the first combobox to update the second one
                                
                                var userDirectoryId = tool.getUserDirectoryComboValue();
                                if (this._directoryField._userDirectories.getStore().findExact('index', userDirectoryId) >= 0) // test if the value selected in the tool is present in the dialog box
                                {
                                    value += "#" + userDirectoryId;
                                    this._directoryField.setValue(value);
                                }
                            }
                        }
                    }
                }
            }, this);
            
		    this._chooseUserDirectoryDialog = Ext.create('Ametys.window.DialogBox', {
		        title: "{{i18n PLUGINS_CORE_UI_USERS_DIALOG_ADD_TITLE}}",
		        icon: Ametys.getPluginResourcesPrefix('core-ui') + '/img/users/add_16.png',
		        
		        layout: {
		            type: 'vbox',
		            align : 'stretch',
		            pack  : 'start'
		        },
		        width: 450,
                
		        defaultFocus: 'userDirectories',
		        items: [
		            {
		                xtype: 'component',
                        html: "{{i18n PLUGINS_CORE_UI_USERS_DIALOG_CHOOSE_USER_DIRECTORY_HINT}}",
                        height: 25
		            },
                    this._directoryField
		        ],
		        
		        closeAction: 'hide',
		        
		        referenceHolder: true,
		        defaultButton: 'next',
		        
		        buttons : [{
		        	reference: 'next',
		            text: "{{i18n PLUGINS_CORE_UI_USERS_DIALOG_NEXT}}",
		            handler: Ext.bind(function() {
                        var userDirectoryField = this._chooseUserDirectoryDialog.items.getByKey('userDirectories');
                        if (!userDirectoryField.isValid())
                        {
                            return;
                        }
                        var userDirectoryValue = userDirectoryField.getValue().split('#', 2);
		                this._open(userDirectoryValue[0], userDirectoryValue[1], null);
                        this._chooseUserDirectoryDialog.close();
		            },this)
		        }, {
		            text: "{{i18n PLUGINS_CORE_UI_USERS_DIALOG_CANCEL}}",
		            handler: Ext.bind(function() {this._chooseUserDirectoryDialog.close();}, this)
		        }]
		    });
            
            this._chooseUserDirectoryInitialized = true;
        }

        this._directoryField.getStore().load();
        
        this._chooseUserDirectoryDialog.show();
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
	 * @param {String} userDirectoryId The id of the user directory in its population. Can be null in 'edit' mode
	 * @param {String} login The user's login. Can be null in 'new' mode
	 */
	_open: function (populationId, userDirectoryId, login)
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
		this._createDialogBox(login, populationId, userDirectoryId, configureCallback);
	},
	
	/**
	 * @private
	 * Creates the dialog if needed.
     * @param {String} login The user's login. Can be null in 'new' mode.
     * @param {String} populationId The id of the population of the user. Cannot be null.
     * @param {String} userDirectoryId The index of the user directory in its population. Can be null in 'edit' mode
	 * @param {Function} callback Function to called after drawing box.
	 */
	_createDialogBox: function (login, populationId, userDirectoryId, callback)
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
			iconCls: 'ametysicon-black302' + (this._mode == 'new' ? ' ametysicon-add64' : ' ametysicon-edit45'),
			
			layout: 'fit',
			width: 500,
			maxHeight: 500,
			
			items: [ this._form ],
			
			closeAction: 'hide',
			
			referenceHolder: true,
			defaultButton: 'validate',
			
			buttons : [{
				reference: 'validate',
				text: "{{i18n PLUGINS_CORE_UI_USERS_DIALOG_OK}}",
				handler: Ext.bind(this._validate, this, [populationId, userDirectoryId])
			}, {
				text: "{{i18n PLUGINS_CORE_UI_USERS_DIALOG_CANCEL}}",
				handler: Ext.bind(function() {this._box.hide();}, this)
			}]
		});
		
		this._configureForm(login, populationId, userDirectoryId, callback);
	},
	
	/**
	 * @private
	 * Configures the user edition form.
     * @param {String} login The user's login. Can be null in 'new' mode.
     * @param {String} populationId The id of the population of the user. Cannot be null.
     * @param {Number} userDirectoryId The id of the user directory in its population. Can be null in 'edit' mode
	 * @param {Function} callback Function to called after drawing box.
	 */
	_configureForm: function (login, populationId, userDirectoryId, callback)
	{
		Ametys.data.ServerComm.callMethod({
			role: "org.ametys.plugins.core.user.UserDAO",
			methodName: this._mode == "new" ? "getEditionModelForDirectory" : "getEditionModelForUSer",
			parameters: this._mode == "new" ? [populationId, userDirectoryId] : [login, populationId],
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
			this._form.getForm().findField('login').focus();
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
     * @param {String} userDirectoryId The index of the user directory in its population. Can be null in 'edit' mode
	 */
	_validate: function(populationId, userDirectoryId)
	{
		if (!this._form.isValid())
		{
			return;
		}
		
		var values = this._form.getValues();
		if (this._mode == 'new')
		{
			Ametys.plugins.core.users.UsersDAO.addUser([populationId, userDirectoryId, values, this._userMessageTargetType], this._editUserCb, {scope: this, waitMessage: {target: this._box}});
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
