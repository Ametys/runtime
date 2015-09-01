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
Ext.define('Ametys.plugins.core.users.EditUserHelper', {
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
	 * @property {String} _usersManagerRole The role of the users manager
	 * @private
	 */
	/**
	 * @property {String} _usersMessageTargetType The type of message target for user
	 * @private
	 */
	
	/**
	 * Open dialog box to create a new user
	 * @param {String} usersManagerRole the role of users manager. If null the default users manager will be used.
	 * @param {String} [userMessageTargetType=user] the type of user message target
	 * @param {Function} [callback] the callback function. Parameters are:
	 * @param {Object} login callback.user The user's properties
	 */
	add: function (usersManagerRole, userMessageTargetType, callback)
	{
		this._mode = 'new';
		this._usersManagerRole = usersManagerRole;
		this._userMessageTargetType = userMessageTargetType;
		this._callback = callback;
		
		this._open (null);
	},
	
	/**
	 * Open dialog box to to edit user's information
	 * @param {String} login the login of user to edit
	 * @param {String} usersManagerRole the role of users manager. If null default manager will be used.
	 * @param {String} [userMessageTargetType=user] the type of user message target
	 * @param {Function} [callback] the callback function. Parameters are:
	 * @param {Object} login callback.user The user's properties
	 */
	edit: function (login, usersManagerRole, userMessageTargetType, callback)
	{
		this._mode = 'edit';
		this._usersManagerRole = usersManagerRole;
		this._userMessageTargetType = userMessageTargetType;
		this._callback = callback;
		
		this._open (login);
	},
	
	/**
	 * @private
	 * Show dialog box for user edition
	 * @param {String} [login] The user's login. Can be null in 'new' mode
	 */
	_open: function (login)
	{
		var me = this;
		function configureCallback (success)
		{
			if (success)
			{
				me._box.show();
				me._initForm (login);
				
				if (me._box.defaultFocus)
				{
					me._box.defaultFocus.focus(true);
				}
			}
		}
		
		// Create dialog box if needed
		this._createDialogBox(configureCallback);
	},
	
	/**
	 * @private
	 * Creates the dialog if needed.
	 * @param {Function} callback Function to called after drawing box.
	 */
	_createDialogBox: function (callback)
	{
		// Initialize only once.
		if (!this._initialized)
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
				title: this._mode == 'new' ? "<i18n:text i18n:key='PLUGINS_CORE_USERS_DIALOG_ADD_TITLE' i18n:catalogue='plugin.core'/>" : "<i18n:text i18n:key='PLUGINS_CORE_USERS_DIALOG_EDIT_TITLE' i18n:catalogue='plugin.core'/>",
				icon: Ametys.getPluginResourcesPrefix('core') + '/img/users/' + (this._mode == 'new' ? 'add_16.png' : 'modify_16.png'),
				
				width: 450,
				maxHeight: 500,
				
				items: [ this._form ],
				
				closeAction: 'hide',
				buttons : [{
					text: "<i18n:text i18n:key='PLUGINS_CORE_USERS_DIALOG_OK' i18n:catalogue='plugin.core'/>",
					handler: Ext.bind(this._validate, this)
				}, {
					text: "<i18n:text i18n:key='PLUGINS_CORE_USERS_DIALOG_CANCEL' i18n:catalogue='plugin.core'/>",
					handler: Ext.bind(function() {this._box.hide();}, this)
				}]
			});
			
			this._configureForm(callback);
		}
		else
		{
			this._box.setTitle(this._mode == 'new' ? "<i18n:text i18n:key='PLUGINS_CORE_USERS_DIALOG_ADD_TITLE' i18n:catalogue='plugin.core'/>" : "<i18n:text i18n:key='PLUGINS_CORE_USERS_DIALOG_EDIT_TITLE' i18n:catalogue='plugin.core'/>");
			this._box.setIcon(Ametys.getPluginResourcesPrefix('core') + '/img/users/' + (this._mode == 'new' ? 'add_16.png' : 'modify_16.png'));
			callback(true);
		}
	},
	
	/**
	 * @private
	 * Configures the user edition form.
	 * @param {Function} callback Function to called after drawing box.
	 */
	_configureForm: function (callback)
	{
		Ametys.data.ServerComm.callMethod({
			role: "org.ametys.plugins.core.user.UserDAO",
			methodName: "getEditionModel",
			parameters: [this._usersManagerRole],
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
		this._initialized = true;
		
		args[0](true);
	},
	
	/**
	 * @private
	 * Initialize the form
	 * @param {String} [login] The user's login. Can not be null in edition mode.
	 */
	_initForm: function (login)
	{
		if (this._mode == 'new')
		{
			this._form.setValues({values: {}, comments: {}, repeaters: []});
			this._form.getForm().findField('login').enable();
			this._form.getForm().findField('login').focus(true);
		}
		else
		{
			Ametys.plugins.core.users.UsersDAO.getUser([login, this._usersManagerRole], this._getUserCb, {scope: this});
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
		this._form.setValues({values: user, comments: {}, repeaters: []});
		this._form.getForm().findField('login').disable();
		
		// TODO focus first field != changepasswordfield
	},
	
	/**
	 * @private
	 * Validates the dialog box.
	 * Creates or edits user.
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
			Ametys.plugins.core.users.UsersDAO.addUser([values, this._usersManagerRole, this._userMessageTargetType], this._editUserCb, {scope:this, waitMessage: {target: this._box}});
		}
		else
		{
			values.login = this._form.getForm().findField('login').getValue(); // disable field
			Ametys.plugins.core.users.UsersDAO.editUser([values, this._usersManagerRole, this._userMessageTargetType], this._editUserCb, {scope:this, waitMessage: {target: this._box}});
		}
	},

	/**
	 * @private
	 * Callback function invoked after group creation/edition process is over.
	 * @param {Object} group the added/edited group or the errors
	 * @param {Object} args the callback arguments
	 */
	_editUserCb: function (group, args)
	{
		if (group.error == 'unknown-group')
		{
			Ametys.message
			var errors = user.errors;
			Ext.Array.forEach(errors, function(error) {
				var fd = this._form.findField(error);
				if (fd)
				{
					fd.markInvalid("<i18n:text i18n:key='PLUGINS_CORE_USERS_DIALOG_INVALID_FIELD' i18n:catalogue='plugin.core'/>");
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
