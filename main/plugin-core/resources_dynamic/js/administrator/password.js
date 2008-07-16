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
	if (RUNTIME_Plugin_Runtime_Administrator_Password.initialized == true)
		return;
  
    var plugin = RUNTIME_Plugin_Runtime_Administrator_Password.plugin;
	
	if (!Tools.loadHTML(getPluginDirectUrl(plugin) + "/administrator/password/dialogs.html", "<i18n:text i18n:key="KERNEL_DIALOG_ERRORREADINGURL" i18n:catalogue="kernel"/>"))
      return false;

	var config = new SDialog.Config()
	config.innerTableClass = "dialog";
  
	RUNTIME_Plugin_Runtime_Administrator_Password.box = new SDialog("RUNTIME_Plugin_Runtime_Administrator_Password", 
								"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_DIALOG_CAPTION"/>", 
								getPluginResourcesUrl(RUNTIME_Plugin_Runtime_Administrator_Password.plugin) + "/img/administrator/password/icon_small.gif", 
								380, 135, config, RUNTIME_Plugin_Runtime_Administrator_Password);
	RUNTIME_Plugin_Runtime_Administrator_Password.box.paint();
	Tools.loadStyle(RUNTIME_Plugin_Runtime_Administrator_Password.box.ui.iframe.contentWindow.document, context.contextPath + "/kernel/resources/css/dialog.css");

    RUNTIME_Plugin_Runtime_Administrator_Password.initialized = true;
    
    return true;
}

RUNTIME_Plugin_Runtime_Administrator_Password.act = function(plugin, params)
{
	if (!RUNTIME_Plugin_Runtime_Administrator_Password.delayedInitialize())
      return false;
	
	var _document = RUNTIME_Plugin_Runtime_Administrator_Password.box.ui.iframe.contentWindow.document;
	
	var oldPassword = _document.getElementById("oldPassword");
    oldPassword.value = "";

	_document.getElementById("newPassword").value = "";
	_document.getElementById("confirmPassword").value = "";
	
	RUNTIME_Plugin_Runtime_Administrator_Password.box.showModal();
	
	try { oldPassword.focus(); } catch (e) {}
  
    return true;
}

RUNTIME_Plugin_Runtime_Administrator_Password.ok = function()
{
	// VERIFICATIONS
	var _document = RUNTIME_Plugin_Runtime_Administrator_Password.box.ui.iframe.contentWindow.document;
	
	var oldPassword = _document.getElementById("oldPassword");
	var newPassword = _document.getElementById("newPassword");
	var confirmPassword = _document.getElementById("confirmPassword");

	if (oldPassword.value == "" || newPassword.value == "" || confirmPassword.value == "")
	{
		alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR_WRONG_EMPTY"/>");
		return;
	}

	if (newPassword.value != confirmPassword.value)
	{
		alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_ERROR_WRONG_CONFIRM"/>");
		return;
	}
	
	// ENVOIE DES DONNEES
	var url = getPluginDirectUrl(RUNTIME_Plugin_Runtime_Administrator_Password.plugin) + "/administrator/password/set"
	var args = "oldPassword=" + encodeURIComponent(oldPassword.value) 
				+ "&amp;newPassword=" + encodeURIComponent(newPassword.value)
				+ "&amp;confirmPassword=" + encodeURIComponent(confirmPassword.value);
				
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

	RUNTIME_Plugin_Runtime_Administrator_Password.box.close();
	
	alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PASSWORD_CHANGE_OK"/>");
}

RUNTIME_Plugin_Runtime_Administrator_Password.cancel = function()
{
	RUNTIME_Plugin_Runtime_Administrator_Password.box.close();
}
