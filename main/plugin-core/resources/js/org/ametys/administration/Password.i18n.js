/*
 *  Copyright 2009 Anyware Services
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

Ext.namespace('org.ametys.administration.Password');

/**
 * Set Password
 */
org.ametys.administration.Password = function ()
{
}

org.ametys.administration.Password._initialized;

org.ametys.administration.Password.delayedInitialize = function ()
{
	if (org.ametys.administration.Password._initialized)
		return true;
	
    org.ametys.administration.Password._form = new Ext.FormPanel({
		id : 'form-password',
		
		border :false,
		bodyStyle :'padding:10px 10px 0',
		
		labelWidth :175,
		defaultType :'textfield',
		
		items:[ 
		       	// Old password
		        new org.ametys.form.TextField({
		        	fieldLabel :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_OLD"/>",
		        	name :'oldPassword',
					width :190,
					allowBlank: false,
			        inputType:"password",
			        msgTarget: 'side',
			        blankText: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR_MANDATORY"/>"
		        }), 
		        
		        // New password
		        new org.ametys.form.TextField({
		        	fieldLabel :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_NEW"/>",
		        	name :'newPassword',
		        	width :190,
		        	allowBlank: false,
		        	inputType:"password",
		        	msgTarget: 'side',
		        	blankText: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR_MANDATORY"/>"
		        }), 
		        
		        // Confirm password
		        new org.ametys.form.TextField({
					fieldLabel: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CONFIRM"/>",
					name :'confirmPassword',
					width :190,
					allowBlank: false,
			        inputType:"password",
			        msgTarget: 'side',
			        blankText: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR_MANDATORY"/>"
				})

		 ]
	});
    	
    org.ametys.administration.Password.box = new org.ametys.DialogBox({
    	
    	title :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_DIALOG_CAPTION"/>",
    	icon : getPluginResourcesUrl(org.ametys.administration.Password.pluginName) + "/img/administrator/password/password_16.png",
    	
    	layout :'fit',
		width :430,
		height :190,
			
		items : [ org.ametys.administration.Password._form ],
		
		closeAction: 'hide',
		buttons : [ {
				text :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_DIALOG_OK"/>",
				handler : org.ametys.administration.Password.ok
			}, {
				text :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_DIALOG_CANCEL"/>",
				handler : org.ametys.administration.Password.cancel
			} 
		]
	});
    
    org.ametys.administration.Password._initialized = true;
    return true;
}

org.ametys.administration.Password.act = function(pluginName, params)
{
	org.ametys.administration.Password.pluginName = pluginName;

	if (!org.ametys.administration.Password.delayedInitialize())
      return false;
	
	org.ametys.administration.Password.box.show();
	org.ametys.administration.Password._initForm ();
	
}

org.ametys.administration.Password._initForm = function ()
{
	var form = org.ametys.administration.Password._form.getForm();
	form.findField('oldPassword').setValue("");
	form.findField('newPassword').setValue("");
	form.findField('confirmPassword').setValue("");
	
	form.findField('oldPassword').clearInvalid();
	form.findField('newPassword').clearInvalid();
	form.findField('confirmPassword').clearInvalid();
	
	//try { oldPassword.focus(); } catch (e) {}
	
    return true;
}

org.ametys.administration.Password.ok = function()
{
	// VERIFICATIONS
	var form = org.ametys.administration.Password._form.getForm();
	
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
	var serverMessage = new org.ametys.servercomm.ServerMessage(org.ametys.administration.Password.pluginName, "/administrator/password/set", { oldPassword: oldPassword.getValue(), newPassword: newPassword.getValue(), confirmPassword: confirmPassword.getValue() }, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
	var result = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);

    if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR_FATAL"/>", result, "org.ametys.administration.Password.ok"))
    {
       return;
    }
	
    if (Tools.getFromXML(result, "result") != "SUCCESS")
    {
    	Ext.Msg.show ({
    		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
    		msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR"/>",
    		buttons: Ext.Msg.OK,
			icon: Ext.MessageBox.ERROR
		});
    	
        return;
    }

	org.ametys.administration.Password.box.hide();
	
	Ext.Msg.show ({
		title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_LABEL"/>",
		msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_OK"/>",
		buttons: Ext.Msg.OK,
		icon: Ext.MessageBox.INFO
	});
}

org.ametys.administration.Password.cancel = function()
{
	org.ametys.administration.Password.box.hide();
}
