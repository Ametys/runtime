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
 * Set the admin password
 */

Ext.define('Ametys.plugins.core.administration.Password', {
	singleton: true,
	
	/**
	 * @property {String} pluginName The plugin loading this class
	 * @private
	 */
	
	/**
	 * @property {Boolean} _initialized Is the dialog box initialized?
	 * @private
	 */
	_initialized: false,
	
	/**
	 * Do the job (open the dialog box and so on)
	 * @param {String} pluginName The plugin declaring the function
	 * @param {Object} The params. None are currently used
	 */
	act: function(pluginName, params)
	{
		this.pluginName = pluginName;

		if (!this.delayedInitialize())
	      return false;
		
		this.box.show();
		this._initForm();
	},

	/**
	 * @private
	 * Initialize the dialog box.
	 */
	delayedInitialize: function ()
	{
		if (this._initialized)
			return true;
		
	    this._form = new Ext.FormPanel({
			id : 'form-password',
			
			border :false,
			bodyStyle :'padding:10px 10px 0',
			
			defaultType :'textfield',
			
			items:[ 
			       	// Old password
			        new Ext.form.field.Text({
			        	fieldLabel :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_OLD"/>",
			        	name: 'oldPassword',
			        	
						labelWidth: 175,
						labelAlign: 'right',
						labelSeparator: '',
						width: 206 + 175,
						
						allowBlank: false,
				        inputType:"password",
				        msgTarget: 'side',
				        blankText: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR_MANDATORY"/>"
			        }), 
			        
			        // New password
			        new Ext.form.field.Text({
			        	fieldLabel :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_NEW"/>",
			        	name :'newPassword',

						labelWidth: 175,
						labelAlign: 'right',
						labelSeparator: '',
			        	width: 206 + 175,
			        	
			        	allowBlank: false,
			        	inputType:"password",
			        	msgTarget: 'side',
			        	blankText: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR_MANDATORY"/>"
			        }), 
			        
			        // Confirm password
			        new Ext.form.field.Text({
						fieldLabel: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CONFIRM"/>",
						name: 'confirmPassword',

						labelWidth: 175,
						labelAlign: 'right',
						labelSeparator: '',
						width: 206 + 175,

						allowBlank: false,
				        inputType: "password",
				        msgTarget: 'side',
				        blankText: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR_MANDATORY"/>"
					})
	
			 ]
		});
	    	
	    this.box = new Ametys.window.DialogBox({
	    	
	    	title :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_DIALOG_CAPTION"/>",
	    	icon : Ametys.getPluginResourcesPrefix(this.pluginName) + "/img/administrator/password/password_16.png",
	    	
	    	layout :'fit',
			width :430,
			height :190,
				
			items : [ this._form ],
			
			defaultButton: this._form.getForm().findField('oldPassword'),
			closeAction: 'hide',
			buttons : [ {
					text :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_DIALOG_OK"/>",
					handler : Ext.bind(this.ok, this)
				}, {
					text :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_DIALOG_CANCEL"/>",
					handler : Ext.bind(this.cancel, this)
				} 
			]
		});
	    
	    this._initialized = true;
	    return true;
	},
	
	/**
	 * @private
	 * Initialize the box form
	 */
	_initForm: function ()
	{
		var form = this._form.getForm();
		form.findField('oldPassword').setValue("");
		form.findField('newPassword').setValue("");
		form.findField('confirmPassword').setValue("");
		
		form.findField('oldPassword').clearInvalid();
		form.findField('newPassword').clearInvalid();
		form.findField('confirmPassword').clearInvalid();
		
	    return true;
	},
	
	/**
	 * The ok button handler
	 */
	ok: function()
	{
		// VERIFICATIONS
		var form = this._form.getForm();
		
		var oldPassword = form.findField("oldPassword");
		var newPassword =  form.findField("newPassword");
		var confirmPassword =  form.findField("confirmPassword");

		if (oldPassword.getValue() == "" || newPassword.getValue() == "" || confirmPassword.getValue() == "")
		{
			Ext.Msg.show ({
	    		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
	    		msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR_WRONG_EMPTY"/>",
	    		buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.ERROR
			});
			return;
		}

		if (newPassword.getValue() != confirmPassword.getValue())
		{
			confirmPassword.markInvalid("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR_WRONG_CONFIRM"/>");
			return;
		}
		
		// ENVOIE DES DONNEES
		var result = Ametys.data.ServerComm.send({
			plugin: this.pluginName, 
			url: "/administrator/password/set", 
			parameters: { oldPassword: oldPassword.getValue(), newPassword: newPassword.getValue(), confirmPassword: confirmPassword.getValue() }, 
			priority: Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, 
			callback: null, 
			responseType: null
		});
	    if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR_FATAL"/>", result, "this.ok"))
	    {
	       return;
	    }

	    if (Ext.dom.Query.selectValue("*/result", result) != "SUCCESS")
	    {
	    	Ext.Msg.show ({
	    		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
	    		msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR"/>",
	    		buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.ERROR
			});
	    	
	        return;
	    }

		this.box.hide();
		
		Ext.Msg.show ({
			title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_LABEL"/>",
			msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_OK"/>",
			buttons: Ext.Msg.OK,
			icon: Ext.MessageBox.INFO
		});
	},

	/**
	 * The cancel button handler
	 */
	cancel: function()
	{
		this.box.hide();
	}
});
