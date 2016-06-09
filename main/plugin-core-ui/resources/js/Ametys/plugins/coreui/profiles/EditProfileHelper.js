/*
 *  Copyright 2015 Anyware Services
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
 * @private
 * Helper for editing a profile
 */
Ext.define('Ametys.plugins.coreui.profiles.EditProfileHelper', {
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
	 * Open dialog box to create a new profile
	 * @param {String} context The context. Can be null.
	 * @param {Function} [callback] the callback function. Parameters are:
	 * @param {Object} callback.profile The profile's properties
	 */
	add: function (context, callback)
	{
		this._mode = 'new';
		this._callback = callback;
		this._context = context;
		
		this._open (null);
	},
	
	/**
	 * Open dialog box to to edit profile's information
	 * @param {String} id the id of profile to edit
	 * @param {Function} [callback] the callback function. Parameters are:
	 * @param {Object} callback.profile The profile's properties
	 */
	edit: function (id, callback)
	{
		this._mode = 'edit';
		this._callback = callback;
		
		this._open (id);
	},
	
	/**
	 * @private
	 * Show dialog box for profile edition
	 * @param {String} [id] The profile's id. Can be null in 'new' mode
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
					fieldLabel: "{{i18n PLUGINS_CORE_UI_PROFILES_DIALOG_NAME}}",
					name: 'name',
					allowBlank: false
				}, {
					xtype: 'hidden',
					name: 'id'
				}]
			});
			
			this._box = Ext.create('Ametys.window.DialogBox', {
				title: this._mode == 'new' ? "{{i18n PLUGINS_CORE_UI_PROFILES_DIALOG_ADD_TITLE}}" : "{{i18n PLUGINS_CORE_UI_PROFILES_DIALOG_EDIT_TITLE}}",
				icon: Ametys.getPluginResourcesPrefix('core-ui') + '/img/profiles/' + (this._mode == 'new' ? 'add_16.gif' : 'rename_16.gif'),
				
				width: 450,
				
				items: [ this._form ],
				
				closeAction: 'hide',
				buttons : [{
					text: "{{i18n PLUGINS_CORE_UI_PROFILES_DIALOG_OK}}",
					handler: Ext.bind(this._validate, this)
				}, {
					text: "{{i18n PLUGINS_CORE_UI_PROFILES_DIALOG_CANCEL}}",
					handler: Ext.bind(function() {this._box.hide();}, this)
				}]
			});
			
			this._initialized = true;
		}
		else
		{
			this._box.setTitle(this._mode == 'new' ? "{{i18n PLUGINS_CORE_UI_PROFILES_DIALOG_ADD_TITLE}}" : "{{i18n PLUGINS_CORE_UI_PROFILES_DIALOG_EDIT_TITLE}}");
			this._box.setIcon(Ametys.getPluginResourcesPrefix('core-ui') + '/img/profiles/' + (this._mode == 'new' ? 'add_16.gif' : 'rename_16.gif'));
		}
	},
	
	/**
	 * @private
	 * Initialize the form
	 * @param {String} [id] The profile's id. Can not be null in edition mode.
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
			Ametys.plugins.core.profiles.ProfilesDAO.getProfile([id], this._getProfileCb, {scope: this});
		}
	},
	
	/**
	 * @private
	 * Callback function invoked after retrieving profile's properties
	 * Initialize the form
	 * @param {Object} profile the profile's properties
	 */
	_getProfileCb: function (profile)
	{
		this._form.getForm().findField('id').setValue(profile.id);
		this._form.getForm().findField('name').setValue(profile.label);
		this._form.getForm().findField('name').focus(true);
	},
	
	/**
	 * @private
	 * Validates the dialog box.
	 * Creates or edits profile.
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
			Ametys.plugins.core.profiles.ProfilesDAO.addProfile([values.name, this._context], this._editProfileCb, {scope:this, waitMessage: {target: this._box}});
		}
		else
		{
			Ametys.plugins.core.profiles.ProfilesDAO.renameProfile([values.id, values.name], this._editProfileCb, {scope:this, waitMessage: {target: this._box}});
		}
	},

	/**
	 * @private
	 * Callback function invoked after profile creation/edition process is over.
	 * @param {Object} profile the added/edited profile or the errors
	 * @param {Object} args the callback arguments
	 */
	_editProfileCb: function (profile, args)
	{
		if (profile.errors)
		{
			Ametys.Msg.show ({
                title: "{{i18n PLUGINS_CORE_UI_PROFILES_UNKNOWN_PROFILE_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_UI_PROFILES_UNKNOWN_PROFILE_ERROR}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
			});
			return;
		}
		
		this._box.hide();
		
		if (Ext.isFunction (this._callback))
		{
			this._callback (profile)
		}
	}
});
