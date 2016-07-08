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
 * Helper for the edition of a SQL data source
 * @private
 */
Ext.define('Ametys.plugins.admin.datasource.EditLDAPDataSourceHelper', {
	singleton: true,
	
	/**
	 * @private
	 * @property {Boolean} _initialized True if the dialog box creation process is finished
	 */
	/**
	 * @private
	 * @property {String} _mode The current edition mode ('new' or 'edit')
	 */
	/**
	 * @private
	 * @property {Ametys.window.DialogBox} _box The dialog box
	 */
	/**
	 * @private
	 * @property {Ext.form.Panel} _form The form panel
	 */
	/**
	 * @private
	 * @property {Function} _callback the callback function
	 */
	
	/**
	 * Open dialog box to create a new LDAP data source
	 * @param {Function} callback a callback function to invoke after the form is validated, can be null
	 */
	add: function (callback)
	{
		this._callback = callback;
		this._mode = 'new';
		this._open ();
	},
	
	/**
	 * Open dialog box to edit a LDAP data source
	 * @param {String} id the id of the selected data source
	 * @param {Function} [callback] a callback function to invoke after the data source was edited.
	 */
	edit: function (id, callback)
	{
		this._callback = callback;
		this._mode = 'edit';
		this._open (id);
	},
	
	/**
	 * @private
	 * Show dialog box for LDAP data source edition
	 * @param {String} id the id of the selected data source in 'edit' mode, null in 'new' mode
	 */
	_open: function (id)
	{
		var me = this;
		function configureCallback (success)
		{
			if (success)
			{
				me._initForm (id);
				me._box.show();
			}
		}
		
		// Create dialog box if needed
		this._createDialogBox(configureCallback);
	},
	
	/**
	 * @private
	 * Create the dialog box if it is not already the case
	 * @param {Function} callback function invoked when the dialog box's drawing is finished
	 */
	_createDialogBox: function (callback)
	{
		if (!this._initialized)
		{
			this._form = Ext.create('Ametys.form.ConfigurableFormPanel', {
				testURL: Ametys.getPluginDirectPrefix('admin') + '/datasource/test'
			});
			
			this._box = Ext.create('Ametys.window.DialogBox', {
				title: this._mode == 'new' ? "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_ADD_TITLE}}" : "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_EDIT_TITLE}}",
				iconCls: "ametysmisc-agenda3 " + (this._mode == 'new' ? "decorator-ametysmisc-add64" : "decorator-ametysmisc-edit45"),
				
				layout: 'fit',
				width: 700,
				items: [ this._form ],
				
				closeAction: 'hide',
				defaultFocus: this._form,
				buttons : [{
					text: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_OK}}",
					handler: Ext.bind(this._ok, this)
				}, {
					text: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_CANCEL}}",
					handler: Ext.bind(function() {this._box.hide();}, this)
				}]
			});
			
			this._configureForm(callback);
		}
		else
		{
			this._box.setTitle(this._mode == 'new' ? "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_ADD_TITLE}}" : "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_EDIT_TITLE}}");
			this._box.setIconCls("ametysmisc-agenda3 " + (this._mode == 'new' ? "decorator-ametysmisc-add64" : "decorator-ametysmisc-edit45"));
			callback(true);
		}
	},
	
	/**
	 * @private
	 * Callback for the form configuration process. Configures the {@link Ametys.form.ConfigurableFormPanel} of the dialog box
	 * @param {Function} args.callback the callback function
	 */
	_configureForm: function(callback)
	{
		var configuration = {};
		
		// Enumerations
		var authenticationMethodEnumeration = [];
		var aliasDereferencingEnumeration = [];
		
		// Anonymous
		authenticationMethodEnumeration.push({label: "{{i18n PLUGINS_ADMIN_DATASOURCE_CORE_LDAP_CONFIG_AUTH_METHOD_ENUM_NONE}}", value: 'none'});
		// Simple
		authenticationMethodEnumeration.push({label: "{{i18n PLUGINS_ADMIN_DATASOURCE_CORE_LDAP_CONFIG_AUTH_METHOD_ENUM_SIMPLE}}", value: 'simple'});
		
		// Always
		aliasDereferencingEnumeration.push({label: "{{i18n PLUGINS_ADMIN_DATASOURCE_CORE_LDAP_CONFIG_ALIAS_DEREF_ENUM_NEVER}}", value: 'never'});
		// Never
		aliasDereferencingEnumeration.push({label: "{{i18n PLUGINS_ADMIN_DATASOURCE_CORE_LDAP_CONFIG_ALIAS_DEREF_ENUM_ALWAYS}}", value: 'always'});
		// Finding
		aliasDereferencingEnumeration.push({label: "{{i18n PLUGINS_ADMIN_DATASOURCE_CORE_LDAP_CONFIG_ALIAS_DEREF_ENUM_FINDING}}", value: 'finding'});
		// Searching
		aliasDereferencingEnumeration.push({label: "{{i18n PLUGINS_ADMIN_DATASOURCE_CORE_LDAP_CONFIG_ALIAS_DEREF_ENUM_SEARCHING}}", value: 'searching'});
		
		// Id field
		configuration.id = {
			hidden: true,
			type: 'string'
		};
		
		// Name field
		configuration.name = {
			type: 'string',
	        label: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_NAME}}",
	        description: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_NAME_DESCRIPTION}}",
	        validation: {
	        	mandatory: true
	        }
		};
		
		// Description field
		configuration.description = {
			type: 'string',
			widget: 'edition.textarea',
			'widget-params': {
				charCounter: false
			},
	        label: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_DESCRIPTION}}",
	        description: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_DESCRIPTION_DESCRIPTION}}"
		};
		
		// Base URL field 
		configuration.baseURL = {
			type: 'string',
			label: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_URL}}",
			description: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_URL_DESCRIPTION}}",
			'default-value': 'ldap://server:389',
	        validation: {
	        	mandatory: true,
	        	regexp: new RegExp('^ldaps?://[\\w\\-.]*(:\d+)?'),
	        	regexText: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_URL_REGEXP_TEXT}}"
	        }
		};
		
		// Use SSL field
		configuration.useSSL = {
			type: 'boolean',
			widget: 'edition.checkbox',
			label: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_USESSL}}",
			description: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_USESSL_DESCRIPTION}}",
	        validation: {
	        	mandatory: true
	        }
		};

		// Base DN field
		configuration.baseDN = {
			type: 'string',
			label: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_BASEDN}}",
			description: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_BASEDN_DESCRIPTION}}",
			'default-value': 'dc=company,dc=com'
		};
		
		// Alias dereferencing mode field
		configuration.aliasDereferencing = {
			type: 'string',
			enumeration: aliasDereferencingEnumeration,
			label: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_ALIASDEREFERENCING}}",
			description: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_ALIASDEREFERENCING_DESCRIPTION}}",
			'default-value': 'always'
		};
		
		// Follow referrals field
		configuration.followReferrals = {
			type: 'boolean',
			widget: 'edition.checkbox',
			label: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_FOLLOWREFERRALS}}",
			description: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_FOLLOWREFERRALS_DESCRIPTION}}"
		};
		
		// Authentication method field
		configuration.authenticationMethod = {
			type: 'string',
			enumeration: authenticationMethodEnumeration,
			label: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_AUTHENTICATIONMETHOD}}",
			description: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_AUTHENTICATIONMETHOD_DESCRIPTION}}",
			'default-value': 'none'
		};
		
		// Administrator domain name field
		configuration.adminDN = {
			type: 'string',
			label: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_ADMINDN}}",
			description: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_ADMINDN_DESCRIPTION}}",
			disableCondition: {condition: [{id: 'authenticationMethod', operator: 'eq', value: 'none'}]},
			'default-value': 'cn=admin'
		};
		
		// Administrator password field
		configuration.adminPassword = {
			type: 'password',
			widget: 'edition.password',
			label: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_ADMINPASSWORD}}",
			description: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_ADMINPASSWORD_DESCRIPTION}}",
			disableCondition: {condition: [{id: 'authenticationMethod', operator: 'eq', value: 'none'}]}
		};
		
		// Private field
		configuration['private'] = {
			type: 'boolean',
			widget: 'edition.checkbox',
			label: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_PRIVATE}}",
			description: "{{i18n PLUGINS_ADMIN_DATASOURCES_DIALOG_LDAP_FIELD_PRIVATE_DESCRIPTION}}",
		};
		
		// Global parameter checker
		configuration['field-checker'] = {
			id: 'ldap-connection-checker-datasource',
			'icon-glyph': 'ametysmisc-agenda3',
			'linked-fields': ['id', 'baseURL', 'baseDN', 'useSSL', 'followReferrals', 'authenticationMethod', 'adminDN', 'adminPassword'],
			label: "{{i18n plugin.core-impl:PLUGINS_CORE_LDAP_CONNECTION_CHECKER_LABEL}}",
			description: "{{i18n plugin.core-impl:PLUGINS_CORE_LDAP_CONNECTION_CHECKER_DESC}}"
		};
		
		this._form.configure(configuration);
		callback(true);
	},
	
 	/**
 	 * @private
 	 * Initialize the fields of the form
 	 * @param {String} id the id of the data source if in 'edit' mode, null in 'new' mode
 	 */
	_initForm: function (id)
 	{
		var form = this._form.getForm();
		
		// Reset the parameter checker status to 'not tested' and reset the warnings
		if (this._initialized)
		{
			this._form.reset();
		}
		
		this._initialized = true;
		
 		if (id == null) 
        {
 			// FIXME chrome automatically sets the user and password fields...
 			this._form.setValues({});
 			form.reset();
        }
 		else
 		{
            var me = this;
            Ametys.plugins.core.datasource.DataSourceDAO.getDataSource(["LDAP", id], function (datasource) {
                me._form.setValues({values: datasource});
            });
 		}
 	},
 	
 	/**
 	 * @private
 	 * Check if the data source is valid
 	 */
 	_ok: function()
 	{
 		if (!this._form.isValid())
		{
 			return;
		}
 		
 		var fieldCheckersManager = this._form._fieldCheckersManager;
 		var ldapConnectionChecker = fieldCheckersManager._fieldCheckers[0];
		if (ldapConnectionChecker.getStatus() == Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_SUCCESS)
		{
			this._okCb();
		}
		else if (ldapConnectionChecker.getStatus() == Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_FAILURE)
		{
			this._displayInvalidDataSourceDialog();
		}
		else
		{
			// Test the data source
			Ext.getBody().mask("{{i18n plugin.core-ui:PLUGINS_CORE_UI_LOADMASK_DEFAULT_MESSAGE}}");
	 	    fieldCheckersManager.check(null,
					   true, 
			           Ext.bind(function(success) 
		      		   { 
			              Ext.getBody().unmask(); 
			              if (success) 
			              { 
			            	  this._okCb();
			              }
			              else
		            	  {
			            	  this._displayInvalidDataSourceDialog();
		            	  }
		      	  		}, this), false);  
		}
 	},
 	
 	/**
 	 * Display the warning dialog for the saving of invalid data sources
 	 */
 	_displayInvalidDataSourceDialog: function()
 	{
 		Ametys.Msg.show({
			title: "{{i18n PLUGINS_ADMIN_UITOOL_DATASOURCE_INVALID_CREATION_TITLE}}",
			message: "{{i18n PLUGINS_ADMIN_UITOOL_DATASOURCE_INVALID_CREATION_MSG}}",
			icon: Ext.Msg.WARNING,
			buttons: Ext.Msg.YESNO,
			scope: this,
			fn : function(btn) {
				if (btn == 'yes') {
					this._okCb();
				}
			}
		});
 	},
 	
 	/**
 	 * @private
 	 * Submit the form's values
 	 */
	_okCb: function()
	{
		var values = this._form.getValues();
 		values['private'] = this._form.getForm().findField('private').getValue();
 		values['useSSL'] = this._form.getForm().findField('useSSL').getValue();
 		values['followReferrals'] = this._form.getForm().findField('followReferrals').getValue();
 		
 		if (this._mode == 'new')
		{
 			Ametys.plugins.core.datasource.DataSourceDAO.addDataSource(["LDAP", values], this._addOrEditDataSourceCb, {scope: this, waitMessage: {target: this._box}});
		}
 		else
		{
 			Ametys.plugins.core.datasource.DataSourceDAO.editDataSource(["LDAP", values], this._addOrEditDataSourceCb, {scope: this, waitMessage: {target: this._box}});
		}
	},
    
    /**
     * Callback function after adding or editing SQL data source
     * @param {Object} datasource the object representation of the added/edited datasource
     */
    _addOrEditDataSourceCb: function (datasource)
    {
        this._box.hide();
        if (Ext.isFunction(this._callback))
        {
            this._callback(datasource);
        }
    }
    
});