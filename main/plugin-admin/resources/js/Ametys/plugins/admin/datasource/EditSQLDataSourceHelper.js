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
 * Helper to add or edit a SQL data source
 * @private
 */
Ext.define('Ametys.plugins.admin.datasource.EditSQLDataSourceHelper', {
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
	 * @property {Ametys.form.ConfigurableFormPanel} _form The form panel
	 */
	/**
	 * @private
	 * @property {Object} _driverTemplates the mapping of driver value with their associated template
	 */
	/**
	 * @private
	 * @property {Function} _callback the callback function
	 */
	
	/**
	 * Open dialog box to create a new SQL data source
	 * @param {Function} [callback] a callback function to invoke after the data source was created.
	 */
	add: function (callback)
	{
		this._callback = callback;
		this._mode = 'new';
		this._open ();
	},
	
	/**
	 * Open dialog box to edit a SQL data source
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
	 * Show dialog box for SQL data source edition
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
				title: this._mode == 'new' ? "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_ADD_TITLE'/>" : "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_EDIT_TITLE'/>",
				iconCls: "flaticon-data110 decorator-flaticon-add64",
				
				layout: 'fit',
				width: 700,
				items: [ this._form ],
				
				closeAction: 'hide',
				buttons : [{
					text: "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_OK'/>",
					handler: Ext.bind(this._ok, this)
				}, {
					text: "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_CANCEL'/>",
					handler: Ext.bind(function() {this._box.hide();}, this)
				}]
			});
			
			this._configureForm(callback);
		}
		else
		{
			this._box.setTitle(this._mode == 'new' ? "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_ADD_TITLE'/>" : "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_EDIT_TITLE'/>");
			this._box.setIconCls("flaticon-data110 " + (this._mode == 'new' ? "decorator-flaticon-add64" : "decorator-flaticon-edit45"));
			callback(true);
		}
	},
	
	/**
	 * @private
	 * Configure the {@link Ametys.form.ConfigurableFormPanel}
	 * @param {Function} callback function invoked when the form configuration is finished
	 */
	_configureForm: function(callback)
	{
		// Retrieve the handled database types
		Ametys.data.ServerComm.callMethod({
			role: "org.ametys.core.datasource.dbtype.SQLDatabaseTypeManager",
			methodName: "getSQLDatabaseTypes",
			parameters: [],
			callback: {
				handler: this._getSQLDatabaseTypesCb,
				arguments: {
					callback: callback
				},
				scope: this
			},
			errorMessage: {
				category: this.self.getName(),
				msg: "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_GET_DRIVERS_ERROR'/>"
			}
		});
	},
	
	/**
	 * @private
	 * Callback for the form configuration process. Configures the {@link Ametys.form.ConfigurableFormPanel} of the dialog box
	 * @param {Object} response the server's response
	 * @param {Array} response.databaseTypes the array of database types
	 * @param {String} response.databaseTypes.label the label of the current database type
	 * @param {String} response.databaseTypes.value the value of the current database type
	 * @param {String} response.databaseTypes.template the template associated to the current database type 
	 * @param {Object} args the callback arguments
	 * @param {Function} args.callback the callback function
	 */
	_getSQLDatabaseTypesCb: function(response, args)
	{
		var me = this;
		
		var databaseTypes = response.databaseTypes;
		
		var driverEnumeration = []; // The list of label-value pairs that will be displayed in the form
		var templateMapping = {}; // The mapping of driver value and associated template
		
		Ext.Array.each(databaseTypes, function(databaseType){
			driverEnumeration.push({label: databaseType.label, value: databaseType.value});
			
			templateMapping[databaseType.value] = databaseType.template;
		});
			
		this._driverTemplates = templateMapping;
		var configuration = this._getFormConfiguration (driverEnumeration);
		this._form.configure(configuration);
		
		args.callback(true);
	},
    
    /**
     * @private
     * Get the form configuration
     * @param {Object[]} driverEnumeration the drivers' enumeration
     * @return {Object} the form configuration
     */
    _getFormConfiguration: function (driverEnumeration)
    {
        return {
            // Data source id (for edition only)
            'id': {
	            hidden: true,
	            type: 'string'
	        },
            // Data source type
            'type': {
	            hidden: true,
	            type: 'string',
	            'default-value': 'SQL'
            },
            // Name
            'name': {
                type: 'string',
                label: "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_FIELD_NAME'/>",
                description: "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_FIELD_NAME_DESCRIPTION'/>",
                validation: {
                    mandatory: true
                }
            },
            // Description
            'description': {
                type: 'string',
                widget: 'edition.textarea',
                'widget-params': {
                    charCounter: false
                },
                label: "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_FIELD_DESCRIPTION'/>",
                description: "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_FIELD_DESCRIPTION_DESCRIPTION'/>"
            },
            // Driver 
            'driver': {
	            type: 'string',
	            enumeration: driverEnumeration,
	            label: "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_FIELD_DRIVER'/>",
	            description: "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_FIELD_DRIVER_DESCRIPTION'/>",
	            validation: {
	                mandatory: true
	            }
            },
            // Server url
            'url': {
	            type: 'string',
	            label: "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_FIELD_URL'/>",
	            description: "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_FIELD_URL_DESCRIPTION'/>",
	            validation: {
	                mandatory: true
	            }
	        },
            // Username
            'user': {
	            type: 'string',
	            label: "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_FIELD_USER'/>",
	            description: "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_FIELD_USER_DESCRIPTION'/>"
	        },
            // Password
            'password': {
                type: 'password',
	            widget: 'edition.password',
	            label: "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_FIELD_PASSWORD'/>",
	            description: "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_FIELD_PASSWORD_DESCRIPTION'/>"
            },
            // Is private ?
            'private': {
                type: 'boolean',
	            widget: 'edition.checkbox',
	            label: "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_FIELD_PRIVATE'/>",
	            description: "<i18n:text i18n:key='PLUGINS_ADMIN_DATASOURCES_DIALOG_SQL_FIELD_PRIVATE_DESCRIPTION'/>",
	            'param-checker': {
	                id: 'sql-connection-checker-datasource',
	                'small-icon-path': '/plugins/admin/resources/img/datasource/test_sqlconnect_16.png',
	                'medium-icon-path': '/plugins/admin/resources/img/datasource/test_sqlconnect_32.png',
	                'large-icon-path': '/plugins/admin/resources/img/datasource/test_sqlconnect_48.png',
	                'linked-params': ['driver', 'url', 'user', 'password'],
	                'param-ref': 'private',
	                label: "<i18n:text i18n:key='PLUGINS_CORE_SQL_CONNECTION_CHECKER_LABEL' i18n:catalogue='plugin.core-impl'/>",
	                description: "<i18n:text i18n:key='PLUGINS_CORE_SQL_CONNECTION_CHECKER_DESC' i18n:catalogue='plugin.core-impl'/>",
	                order: 1
	            }
            }
        }
    },
	
 	/**
 	 * @private
 	 * Initialize the fields of the form
 	 * @param {String} id the id of the data source if in 'edit' mode, null in 'new' mode
 	 */
	_initForm: function (id)
 	{
		var form = this._form.getForm(); 
		if (!this._initialized)
		{
			form.findField('driver').on('change', Ext.bind(this._onDriverChange, this));
		}
        else
        {
            // Reset the parameter checker status to 'not tested' and reset the warnings
            this._form.reset();
        }
		
 		if (id == null) 
        {
 			// FIXME chrome automatically sets the user and password fields... see CMS-6913
 	 		this._form.setValues({});
 	 		form.reset();
        }
 		else
 		{
            var me = this;
 			Ametys.plugins.core.datasource.DataSourceDAO.getDataSource(["SQL", id], function (datasource) {
                me._form.setValues({values: datasource});
            });
 		}
        
        this._initialized = true;
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
 		
 		// Test the data source
 		var paramCheckersDAO = this._form._paramCheckersDAO;
 		
 		Ext.getBody().mask("<i18n:text i18n:key='PLUGINS_CORE_UI_LOADMASK_DEFAULT_MESSAGE' i18n:catalogue='plugin.core-ui'/>");
 		paramCheckersDAO.check(paramCheckersDAO._paramCheckers,
 							   true, 
					           Ext.bind(function(success) 
				      		   { 
					              Ext.getBody().unmask(); 
					              if (success) 
					              { 
					            	  this._okCb();
					              }
				      	  		}, this), false); 
 	},
 	
 	/**
 	 * @private
 	 * Submit the form's values
 	 */
	_okCb: function()
	{
    	var values = this._form.getValues();
 		values['private'] = this._form.getForm().findField('private').getValue();
    	
		if (this._mode == 'new')
		{
			Ametys.plugins.core.datasource.DataSourceDAO.addDataSource(["SQL", values], 
				this._addOrEditDataSourceCb, 
				{scope: this, waitMessage: {target: this._box}}
			);
		}
		else
		{
			Ametys.plugins.core.datasource.DataSourceDAO.editDataSource(["SQL", values], this._addOrEditDataSourceCb, {scope: this, waitMessage: {target: this._box}});
		}
	},
    
    /**
     * Callback function after adding or editing SQL data source
     * @param {} datasource
     */
    _addOrEditDataSourceCb: function (datasource)
    {
        this._box.hide();
        if (Ext.isFunction(this._callback))
        {
            this._callback(datasource);
        }
    },
 	
	/**
	 * @private
     * Function invoked when the value of the driver field changes
     * @param {Ametys.form.widget.ComboBox} combo the combobox
     * @param {String} newValue the new value of the field
     * @param {String} oldValue the previous value of the field
	 */
	_onDriverChange: function(combo, newValue, oldValue)
	{
		if (oldValue != newValue)
		{
			var form = this._form.getForm();
			form.findField('url').setValue(this._driverTemplates[newValue]);
		}
	}
});