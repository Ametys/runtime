/*
 *  Copyright 2012 Anyware Services
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
 * Edit a user
 */
Ext.define('Ametys.plugins.core.administration.UserEdit', {
	singleton: true,
	
	/**
	 * @property {String} plugin The plugin declaring this class
	 * @private
	 */
	initialized: false,
	/**
	 * @property {Boolean} initialized Is the dialog box already created?
	 * @private
	 */
	/**
	 * @private
	 * @property {Ext.form.field.Field[]} _editForms The items to build the user edition box
	 */
	_editForms: [],

	/**
	 * Initialize the fields
	 * @param {String} plugin The name of the plugin that will be used for request, images...
	 * @param {Number} fieldsNum The number of normally sized items in the form (big widgets should count for 2 as password...)
	 */
	initialize: function(plugin, fieldsNum)
	{
		this.plugin = plugin;
	    this.fieldsNum = fieldsNum;
	},

	/**
	 * Initialize for good the dialog box. Only the first call will be effective (since the {@link #initialize} property will be set.
	 * @private
	 */
	delayedInitialize: function()
	{
		if (this.initialized)
		{
			return true;
		}
		
		var formPanel = new Ext.FormPanel( {
			formId: 'edit-user-form',
			bodyStyle: 'padding:10px',
			border: false,
			labelWidth: 100,
			defaultType: 'textfield',
			defaults: {
				msgTarget: 'side'
			}
		});
		
		for (var i in this._editForms)
		{
			formPanel.add(this._editForms[i]);
		}
		
		this.form = formPanel.getForm();
		
		this.box = new Ametys.window.DialogBox({
			
			title: "<i18n:text i18n:key='PLUGINS_CORE_USERS_HANDLE_NEW'/>",
			icon: Ametys.getPluginResourcesPrefix('core') + '/img/users/icon_small.png',
			
			width : 430,
			height : (85 + 32 * this.fieldsNum),
			autoScroll: true,
			
			items : [ formPanel ],
			
			defaultButton: formPanel.items.get(0),
			closeAction: 'hide',
			buttons : [ {
				text :"<i18n:text i18n:key='PLUGINS_CORE_USERS_DIALOG_OK'/>",
				handler : Ext.bind(this.ok, this)
			}, {
				text :"<i18n:text i18n:key='PLUGINS_CORE_USERS_DIALOG_CANCEL'/>",
				handler : Ext.bind(this.cancel, this)
			} ]
		});
		
		if (this.params['mode'] == 'new')
		{
			this.box.setTitle("<i18n:text i18n:key='PLUGINS_CORE_USERS_HANDLE_NEW'/>", 'new-user-icon-box');
		}
		else
		{
			this.box.setTitle("<i18n:text i18n:key='PLUGINS_CORE_USERS_DIALOG_LABEL'/>", 'edit-user-icon-box');
		}
		
		this.initialized = true;
		
	    return true;
	},

	/**
	 * Effectively opens the dialog box
	 * @param {Object} params Necessary key is 'mode' that can be 'new' or 'edit'. If 'new', all fields will be clear else 'login' field will be grayed and all fields value will be taken from the {@link #plugin} pipeline '/users/info'.
	 * @param {Function} callback The callback function when everything is fine.
	 * @param {String} callback.login The login of the user.
	 * @param {String} callback.firstname The firstname of the user. Can be empty.
	 * @param {String} callback.lastname The lastname of the user.
	 * @param {String} callback.email The email of the user. Can be empty.
	 */
	act: function(params, callback)
	{
		this.params = params;
		this.callback = callback;
		
		if (!this.delayedInitialize())
	        return;
		
		this.form.reset();
	
	    var params = this.params;
	    
	    if (params['mode'] == 'new')
	    {
	    	this.form.findField('field_login').setDisabled(false);
	    }
	    else
	    {
	    	this.form.findField('field_login').setDisabled(true);
	      
	    	
	    	var nodes = Ametys.data.ServerComm.send({
	    		plugin: this.plugin, 
	    		url: "/administrator/users/info", 
	    		parameters: { login: params['login'] }, 
	    		priority: Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, 
	    		callback: null, 
	    		responseType: null
	    	});
	        if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key='PLUGINS_CORE_USERS_DIALOG_ERROR'/>", nodes, "Ametys.plugins.core.administration.UserEdit.act"))
	        {
	           return;
	        }
	
		    var userInfo = Ext.dom.Query.selectNode("users-info/users/user[@login='" + params['login'] + "']", nodes);
		      
		    this.form.findField('field_login').setValue(userInfo.getAttribute("login"));
		   
		    var fields = Ext.dom.Query.select('*', userInfo);
		    for (var i = 0; i < fields.length; i++)
		    {
		    	var field = fields[i];
		        var fieldName = fields[i].nodeName;
		        var fieldValue = fields[i].firstChild ? fields[i].firstChild.nodeValue : "";
		        
		        var elt = this.form.findField('field_' + fieldName);
		        elt.setValue(fieldValue);
		    }
	    }
	    
	    this.box.show();
	},

	/**
	 * Handler when dialog box is validated
	 */
	ok: function ()
	{
		var form = this.form;
		if (!form.isValid())
		{
			return;
		}
		
		var args = form.getValues();
	    args['field_login'] = form.findField('field_login').getValue();
	    args['mode'] = this.params['mode'];

    	var result = Ametys.data.ServerComm.send({
    		plugin: this.plugin, 
    		url: "/administrator/users/edit", 
    		parameters: args, 
    		priority: Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, 
    		callback: null, 
    		responseType: null
    	});
	    if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key='PLUGINS_CORE_USERS_DIALOG_ERROR'/>", result, "Ametys.plugins.core.administration.UserEdit.ok"))
	    {
	       return;
	    }
	  
	    // passe les erreurs en rouges
		var fieldsString = Ext.dom.Query.selectValue("*/error", result);
	    if (fieldsString != null && fieldsString.length > 0)
	    {
	      var fields = fieldsString.split(",");
	      for (var i = 0; i < fields.length; i++)
	      {
	        var field = fields[i];
	        
	        if (field.length > 0)
	        {
	          var elt = this._editForms[field];
	          if (elt != null)
	          {
	            elt.markInvalid("<i18n:text i18n:key='PLUGINS_CORE_USERS_DIALOG_INVALID_FIELD'/>");
	          }
	        }
	      }
	      return;
	    }	
	  
	    // mise à jour graphique
	    function getValue(name)
	    {
	    	var e = form.findField("field_" + name);
	    	if (e == null)
	    		return null;
	    	else
	    		return e.getValue();
	    }

	    var firstname = getValue("firstname");
	    var lastname = getValue("lastname");
	    var login = getValue("login");
	    var email = getValue("email");

	    if (typeof this.callback == 'function')
	    {
	    	this.callback (login, firstname, lastname, email);
	    }
		
		this.box.hide();
	},

	/**
	 * Handler when dialog box is canceled
	 */
	cancel: function ()
	{
		this.box.hide();
	},
	
	/**
	 * Add an input field to the creation form
	 * @param {String} id Internal id for the field
	 * @param {String} type Can be 'double', 'long', 'password', 'date', 'boolean', or 'text' (default value).
	 * @param {String} name The name of the field
	 * @param {String} label The label of the field
	 * @param {String} description The description tooltip for the field
	 * @return {Ext.form.field.Field} The newly created field
	 */
	addInputField: function (id, type, name, label, description)
	{
		var input;
		switch (type) 
		{
			case 'double':
				input = this._createDoubleField (name, label, description);
				break;
			case 'long':
				input = this._createLongField (name, label, description);
				break;
			case 'password':
				input = this._createPasswordField (name, label, description);
				break;
			case 'date':
				input = this._createDateField (name, label, description);
				break;
			case 'boolean':
				input = this._createBooleanField (name, label, description);
				break;
			default:
				input = this._createTextField (name, label, description);
				break;
		}
		this._editForms[id] = input;
	},
	
	/**
	 * @private
	 * Create a field of type 'double'
	 * @param {String} name The name of the field
	 * @param {String} label The label of the field
	 * @param {String} description The description tooltip for the field
	 * @return {Ext.form.field.Field} The newly created field
	 */
	_createDoubleField: function (name, label, description)
	{
		return new Ext.form.field.Double ({
			name: name,
	        fieldLabel: label,
	        labelSeparator: '',
	        labelAlign: 'right',
	        ametysDescription: description,
	        
	        width: 225 + 100
		});
	},
	
	/**
	 * @private
	 * Create a field of type 'long'
	 * @param {String} name The name of the field
	 * @param {String} label The label of the field
	 * @param {String} description The description tooltip for the field
	 * @return {Ext.form.field.Field} The newly created field
	 */
	_createLongField: function (name, label, description)
	{
		return new Ext.form.field.Long ({
			name: name,
			fieldLabel: label,
	        labelSeparator: '',
	        labelAlign: 'right',
			ametysDescription: description,
	        
	        width: 225 + 100
		});
	},
	
	/**
	 * @private
	 * Create a field of type 'password'
	 * @param {String} name The name of the field
	 * @param {String} label The label of the field
	 * @param {String} description The description tooltip for the field
	 * @return {Ext.form.field.Field} The newly created field
	 */
	_createPasswordField: function (name, label, description)
	{
		return new Ametys.form.field.ChangePassword ({
			name: name,
			
		    fieldLabel: label,
	        labelSeparator: '',
	        labelAlign: 'right',
		    ametysDescription: description,
		    
		    width: 225 + 100
		});
	},
	
	/**
	 * @private
	 * Create a field of type 'date'
	 * @param {String} name The name of the field
	 * @param {String} label The label of the field
	 * @param {String} description The description tooltip for the field
	 * @return {Ext.form.field.Field} The newly created field
	 */
	_createDateField: function (name, label, description)
	{
		return new  Ext.form.field.Date ({
			name: name,
			 
	        fieldLabel: label,
	        labelSeparator: '',
	        labelAlign: 'right',
	        ametysDescription: description,
	        
	        width: 225 + 100
		});
	},
	
	/**
	 * @private
	 * Create a field of type 'boolean'
	 * @param {String} name The name of the field
	 * @param {String} label The label of the field
	 * @param {String} description The description tooltip for the field
	 * @return {Ext.form.field.Field} The newly created field
	 */
	_createBooleanField: function (name, label, description)
	{
		return new  Ext.form.field.Boolean ({
			name: name,
			 
	        fieldLabel: label,
	        labelSeparator: '',
	        labelAlign: 'right',
	        ametysDescription: description,
	        
	        checked: false
	        
		});
	},
	
	/**
	 * @private
	 * Create a field of type 'text'
	 * @param {String} name The name of the field
	 * @param {String} label The label of the field
	 * @param {String} description The description tooltip for the field
	 * @return {Ext.form.field.Field} The newly created field
	 */
	_createTextField: function (name, label, description)
	{
		return new  Ext.form.field.Text ({
			name: name,
			
	        fieldLabel: label,
	        labelSeparator: '',
	        labelAlign: 'right',
	        ametysDescription: description,
	        
	        width: 225 + 100
		});
	}
});