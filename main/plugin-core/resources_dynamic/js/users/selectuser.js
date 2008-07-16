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
// ----------------------------------------------------------------
// 				SELECT USER 
// ----------------------------------------------------------------
function RUNTIME_Plugin_Runtime_SelectUser()
{
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectUser.initialized = false;
// --------------------------------
RUNTIME_Plugin_Runtime_SelectUser.delayed_initialize = function ()
{
	if (RUNTIME_Plugin_Runtime_SelectUser.initialized)
		return true;
	RUNTIME_Plugin_Runtime_SelectUser.initialized = true;

	var plugin = RUNTIME_Plugin_Runtime_SelectUser.plugin;

	// Recupere les boites de dialogue
    if (!Tools.loadHTML(getPluginDirectUrl(plugin) + "/users/selectuser/dialog.html", "<i18n:text i18n:key="KERNEL_DIALOG_ERRORREADINGURL" i18n:catalogue="kernel"/>"))
        return false;
        
	var config = new SDialog.Config()
	config.innerTableClass = "dialog";
	RUNTIME_Plugin_Runtime_SelectUser.box = new SDialog("RUNTIME_Plugin_Runtime_SelectUser", 
								"<i18n:text i18n:key="PLUGINS_CORE_USERS_SELECTUSER_DIALOG_CAPTION"/>", 
								getPluginResourcesUrl(plugin) + "/img/users/add_user.gif", 
								280, 305, config, RUNTIME_Plugin_Runtime_SelectUser);
	RUNTIME_Plugin_Runtime_SelectUser.box.paint();
	
	var _document = RUNTIME_Plugin_Runtime_SelectUser.box.ui.iframe.contentWindow.document;
	Tools.loadStyle(_document, context.contextPath + "/kernel/resources/css/dialog.css");

	RUNTIME_Plugin_Runtime_SelectUser.listview = new SListView ("select", RUNTIME_Plugin_Runtime_SelectUser.box.ui.iframe.contentWindow.document, null);
	RUNTIME_Plugin_Runtime_SelectUser.listview.setView("detail");
	RUNTIME_Plugin_Runtime_SelectUser.listview.addColumn (null, "", null, "280px", null);
	RUNTIME_Plugin_Runtime_SelectUser.listview.showHeaders(false);
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectUser.initialize = function (plugin)
{
	RUNTIME_Plugin_Runtime_SelectUser.plugin = plugin;
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectUser.act = function (callback, cancelCallback)
{
	RUNTIME_Plugin_Runtime_SelectUser.delayed_initialize();
	RUNTIME_Plugin_Runtime_SelectUser.callback = callback;
	RUNTIME_Plugin_Runtime_SelectUser.cancelCallback = cancelCallback;
	
	var _document = RUNTIME_Plugin_Runtime_SelectUser.box.ui.iframe.contentWindow.document;
	_document.getElementById('criteria').value = "";

	RUNTIME_Plugin_Runtime_SelectUser.load();

	RUNTIME_Plugin_Runtime_SelectUser.box.showModal();
	try
	{
		_document.getElementById('criteria').focus();
	} catch (e) {}
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectUser.reload = function ()
{
	if (RUNTIME_Plugin_Runtime_SelectUser.reloadTimer != null)
		window.clearTimeout(RUNTIME_Plugin_Runtime_SelectUser.reloadTimer);
	RUNTIME_Plugin_Runtime_SelectUser.reloadTimer = window.setTimeout(RUNTIME_Plugin_Runtime_SelectUser.load, 500);
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectUser.load = function ()
{
	RUNTIME_Plugin_Runtime_SelectUser.reloadTimer = null;

	var _document = RUNTIME_Plugin_Runtime_SelectUser.box.ui.iframe.contentWindow.document;
	var criteria = _document.getElementById('criteria').value;

	RUNTIME_Plugin_Runtime_SelectUser.listview.elements = new Array();
	RUNTIME_Plugin_Runtime_SelectUser.listview.selection = new Array();

	// Recupere la liste des users 
	var result = Tools.postFromUrl(getPluginDirectUrl(RUNTIME_Plugin_Runtime_SelectUser.plugin) + "/users/search.xml", "criteria=" + criteria + "&amp;count=100" + "&amp;offset=0");
	if (result == null)
	{
		alert("<i18n:text i18n:key="PLUGINS_CORE_USERS_SELECTUSER_DIALOG_ERROR_LISTING"/>")
		return;
	}	

	var users = result.selectNodes("/Search/users/user");

	for (var i=0; i &lt; users.length; i++)
	{
		RUNTIME_Plugin_Runtime_SelectUser.listview.addElement((users[i].selectSingleNode('firstname') != null ? users[i].selectSingleNode('firstname')[Tools.xmlTextContent] + " " + users[i].selectSingleNode('lastname')[Tools.xmlTextContent] : users[i].selectSingleNode('lastname')[Tools.xmlTextContent]) + " (" + users[i].getAttribute('login') + ")", 
						getPluginResourcesUrl(RUNTIME_Plugin_Runtime_SelectUser.plugin) + "/img/users/icon_small.gif", getPluginResourcesUrl(RUNTIME_Plugin_Runtime_SelectUser.plugin) + "/img/users/icon_medium.gif", getPluginResourcesUrl(RUNTIME_Plugin_Runtime_SelectUser.plugin) + "/img/users/icon_large.gif", 
						{id: users[i].getAttribute('login')});
	}
	if (users.length == 0)
    {
       alert("<i18n:text i18n:key="PLUGINS_CORE_USERS_SELECTUSER_DIALOG_NORESULT"/>");
    }
  
	RUNTIME_Plugin_Runtime_SelectUser.listview.paint();
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectUser.ok = function ()
{
	if (RUNTIME_Plugin_Runtime_SelectUser.callback != null)
	{
		var addedusers = {}
		
		var selection = RUNTIME_Plugin_Runtime_SelectUser.listview.getSelection();
		if (selection.length == 0)
		{
			alert("<i18n:text i18n:key="PLUGINS_CORE_USERS_SELECTUSER_DIALOG_ERROR_EMPTY"/>");
			return;
		}

		RUNTIME_Plugin_Runtime_SelectUser.box.close();
		
		for (var i=0; i &lt; selection.length; i++)
		{
			var opt = selection[i];

			addedusers[opt.properties.id] = opt.name;
		}
	
		RUNTIME_Plugin_Runtime_SelectUser.callback(addedusers);
	}
	else
	{
		RUNTIME_Plugin_Runtime_SelectUser.box.close();
	}
}
	
// --------------------------------
RUNTIME_Plugin_Runtime_SelectUser.cancel = function ()
{
	RUNTIME_Plugin_Runtime_SelectUser.box.close();

	if (RUNTIME_Plugin_Runtime_SelectUser.cancelCallback != null)
	{
		RUNTIME_Plugin_Runtime_SelectUser.cancelCallback();
	}
}
