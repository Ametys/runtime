<!--+
    | Copyright (c) 2007 Anyware Technologies and others.
    | All rights reserved. This program and the accompanying materials
    | are made available under the terms of the Eclipse Public License v1.0
    | which accompanies this distribution, and is available at
    | http://www.opensource.org/licenses/eclipse-1.0.php
    | 
    | Contributors:
    |     Anyware Technologies - initial API and implementation
    +-->
/**
 * Set Password
 */
function RUNTIME_Plugin_Runtime_Administrator_Password()
{
}

RUNTIME_Plugin_Runtime_Administrator_Password.initialize = function(plugin)
{
	RUNTIME_Plugin_Runtime_Administrator_Password.plugin = plugin;
}

RUNTIME_Plugin_Runtime_Administrator_Password.delayedInitialize = function()
{
    var plugin = RUNTIME_Plugin_Runtime_Administrator_Password.plugin;
    if (!RUNTIME_Plugin_Runtime_Administrator_Password.box)
    {
    	RUNTIME_Plugin_Runtime_Administrator_Password.form = new Ext.FormPanel( {
    		id : 'form-password',
    		labelWidth :175,
    		defaultType :'textfield',
    		border :false,
    		bodyStyle :'padding:10px 10px 0',
    		items :[ new org.ametys.form.TextField({
    			fieldLabel :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_OLD"/>",
    			name :'oldPassword',
    			width :190,
    			allowBlank: false,
    	        inputType:"password",
    	        msgTarget: 'side',
    	        blankText: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR_MANDATORY"/>"
    		}), new org.ametys.form.TextField({
    			fieldLabel :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_NEW"/>",
    			name :'newPassword',
    			width :190,
    			allowBlank: false,
    	        inputType:"password",
    	        msgTarget: 'side',
    	        blankText: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR_MANDATORY"/>"
    		}), new org.ametys.form.TextField({
    			fieldLabel: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CONFIRM"/>",
    			name :'confirmPassword',
    			width :190,
    			allowBlank: false,
    	        inputType:"password",
    	        msgTarget: 'side',
    	        blankText: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR_MANDATORY"/>"
    		})]
    	});
    	
	    RUNTIME_Plugin_Runtime_Administrator_Password.box = new org.ametys.DialogBox( {
			title :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_DIALOG_CAPTION"/>",
			layout :'fit',
			width :430,
			height :190,
			icon : getPluginResourcesUrl('core') + "/img/administrator/password/password_16.png",
			items : [ RUNTIME_Plugin_Runtime_Administrator_Password.form ],
			closeAction: 'hide',
			buttons : [ {
				text :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_DIALOG_OK"/>",
				handler : function() {
					RUNTIME_Plugin_Runtime_Administrator_Password.ok();
				}
			}, {
				text :"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_DIALOG_CANCEL"/>",
				handler : function() {
				RUNTIME_Plugin_Runtime_Administrator_Password.cancel();
				}
			} ]
		});
    }
    
    return true;
}

RUNTIME_Plugin_Runtime_Administrator_Password.act = function(plugin, params)
{
	if (!RUNTIME_Plugin_Runtime_Administrator_Password.delayedInitialize())
      return false;
	
	RUNTIME_Plugin_Runtime_Administrator_Password.box.show();
	
	var form = RUNTIME_Plugin_Runtime_Administrator_Password.form.getForm();
	var oldPassword = form.findField('oldPassword');
	oldPassword.setValue("");
	form.findField('newPassword').setValue("");
	form.findField('confirmPassword').setValue("");
	
	form.findField('oldPassword').clearInvalid();
	form.findField('newPassword').clearInvalid();
	form.findField('confirmPassword').clearInvalid();
	
	//try { oldPassword.focus(); } catch (e) {}
	
    return true;
}

RUNTIME_Plugin_Runtime_Administrator_Password.ok = function()
{
	// VERIFICATIONS
	var form = RUNTIME_Plugin_Runtime_Administrator_Password.form.getForm();
	
	var oldPassword = form.findField("oldPassword");
	var newPassword =  form.findField("newPassword");
	var confirmPassword =  form.findField("confirmPassword");

	if (oldPassword.getValue() == "" || newPassword.getValue() == "" || confirmPassword.getValue() == "")
	{
		alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR_WRONG_EMPTY"/>");
		return;
	}

	if (newPassword.getValue() != confirmPassword.getValue())
	{
		confirmPassword.markInvalid("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR_WRONG_CONFIRM"/>");
		return;
	}
	
	// ENVOIE DES DONNEES
	var url = getPluginDirectUrl(RUNTIME_Plugin_Runtime_Administrator_Password.plugin) + "/administrator/password/set"
	var args = "oldPassword=" + encodeURIComponent(oldPassword.getValue()) 
				+ "&amp;newPassword=" + encodeURIComponent(newPassword.getValue())
				+ "&amp;confirmPassword=" + encodeURIComponent(confirmPassword.getValue());
				
    var result = Tools.postFromUrl(url, args);
	if (result == null)
	{
		alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR_FATAL"/>");
		return;
	}
    if (Tools.getFromXML(result, "result") != "SUCCESS")
    {
        alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR"/>");
        return;
    }

	RUNTIME_Plugin_Runtime_Administrator_Password.box.hide();
	
	alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_OK"/>");
}

RUNTIME_Plugin_Runtime_Administrator_Password.cancel = function()
{
	RUNTIME_Plugin_Runtime_Administrator_Password.box.hide();
}
