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
// 				SELECT GROUP 
// ----------------------------------------------------------------
function RUNTIME_Plugin_Runtime_SelectGroup()
{
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectGroup.listener = {};
RUNTIME_Plugin_Runtime_SelectGroup.initialized = false;
// --------------------------------
RUNTIME_Plugin_Runtime_SelectGroup.listener.ok = function() 
{
	RUNTIME_Plugin_Runtime_SelectGroup.ok();
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectGroup.cancel = function() 
{
	RUNTIME_Plugin_Runtime_SelectGroup.cancel();
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectGroup.delayed_initialize = function ()
{
	if (RUNTIME_Plugin_Runtime_SelectGroup.initialized)
		return true;
	RUNTIME_Plugin_Runtime_SelectGroup.initialized = true;

	var plugin = RUNTIME_Plugin_Runtime_SelectGroup.plugin;

	// Recupere les boites de dialogue
    if (!Tools.loadHTML(getPluginDirectUrl(plugin) + "/groups/selectgroup/dialog.html", "<i18n:text i18n:key="KERNEL_DIALOG_ERRORREADINGURL" i18n:catalogue="kernel"/>"))
        return false;
        
	var config = new SDialog.Config()
	config.innerTableClass = "dialog";
	RUNTIME_Plugin_Runtime_SelectGroup.box = new SDialog("RUNTIME_Plugin_Runtime_SelectGroup", 
								"<i18n:text i18n:key="PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_CAPTION"/>", 
								getPluginResourcesUrl(plugin) + "/img/groups/new.gif", 
								280, 305, config, RUNTIME_Plugin_Runtime_SelectGroup.listener);
	RUNTIME_Plugin_Runtime_SelectGroup.box.paint();
	
	var _document = RUNTIME_Plugin_Runtime_SelectGroup.box.ui.iframe.contentWindow.document;
	Tools.loadStyle(_document, context.contextPath + "/kernel/resources/css/dialog.css");

	RUNTIME_Plugin_Runtime_SelectGroup.listview = new SListView ("select", RUNTIME_Plugin_Runtime_SelectGroup.box.ui.iframe.contentWindow.document, null);
	RUNTIME_Plugin_Runtime_SelectGroup.listview.setView("detail");
	RUNTIME_Plugin_Runtime_SelectGroup.listview.addColumn (null, "", null, "280px", null);
	RUNTIME_Plugin_Runtime_SelectGroup.listview.showHeaders(false);
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectGroup.initialize = function (plugin)
{
	RUNTIME_Plugin_Runtime_SelectGroup.plugin = plugin;
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectGroup.act = function (callback, cancelCallback)
{
	RUNTIME_Plugin_Runtime_SelectGroup.delayed_initialize();
	RUNTIME_Plugin_Runtime_SelectGroup.callback = callback;
	RUNTIME_Plugin_Runtime_SelectGroup.cancelCallback = cancelCallback;
	
	var _document = RUNTIME_Plugin_Runtime_SelectGroup.box.ui.iframe.contentWindow.document;
	_document.getElementById('criteria').value = "";

	RUNTIME_Plugin_Runtime_SelectGroup.load();

	RUNTIME_Plugin_Runtime_SelectGroup.box.showModal();
	try
	{
		_document.getElementById('criteria').focus();
	} catch (e) {}
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectGroup.reload = function ()
{
	if (RUNTIME_Plugin_Runtime_SelectGroup.reloadTimer != null)
		window.clearTimeout(RUNTIME_Plugin_Runtime_SelectGroup.reloadTimer);
	RUNTIME_Plugin_Runtime_SelectGroup.reloadTimer = window.setTimeout(RUNTIME_Plugin_Runtime_SelectGroup.load, 500);
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectGroup.load = function ()
{
	RUNTIME_Plugin_Runtime_SelectGroup.reloadTimer = null;

	var _document = RUNTIME_Plugin_Runtime_SelectGroup.box.ui.iframe.contentWindow.document;
	var criteria = _document.getElementById('criteria').value;

	RUNTIME_Plugin_Runtime_SelectGroup.listview.elements = new Array();
	RUNTIME_Plugin_Runtime_SelectGroup.listview.selection = new Array();

	// Recupere la liste des groups 
	var result = Tools.postFromUrl(getPluginDirectUrl(RUNTIME_Plugin_Runtime_SelectGroup.plugin) + "/groups/selectgroup/search.xml", "criteria=" + criteria + "&amp;count=100" + "&amp;offset=0");
	if (result == null)
	{
		alert("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_ERROR_LISTING"/>")
		return;
	}	

	var groups = result.selectNodes("/Search/groups/group");

	for (var i=0; i &lt; groups.length; i++)
	{
		RUNTIME_Plugin_Runtime_SelectGroup.listview.addElement(groups[i].selectSingleNode('label')[Tools.xmlTextContent] + " (" + groups[i].getAttribute('id') + ")", 
						getPluginResourcesUrl(RUNTIME_Plugin_Runtime_SelectGroup.plugin) + "/img/groups/icon_small.gif", getPluginResourcesUrl(RUNTIME_Plugin_Runtime_SelectGroup.plugin) + "/img/groups/icon_medium.gif", getPluginResourcesUrl(RUNTIME_Plugin_Runtime_SelectGroup.plugin) + "/img/groups/icon_large.gif", 
						{id: groups[i].getAttribute('id')});
	}
	if (groups.length == 0)
    {
       alert("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_NORESULT"/>");
    }
  
	RUNTIME_Plugin_Runtime_SelectGroup.listview.paint();
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectGroup.ok = function ()
{
	if (RUNTIME_Plugin_Runtime_SelectGroup.callback != null)
	{
		var addedgroups = {}
		
		var selection = RUNTIME_Plugin_Runtime_SelectGroup.listview.getSelection();
		if (selection.length == 0)
		{
			alert("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_ERROR_EMPTY"/>");
			return;
		}

		RUNTIME_Plugin_Runtime_SelectGroup.box.close();
		
		for (var i=0; i &lt; selection.length; i++)
		{
			var opt = selection[i];

			addedgroups[opt.properties.id] = opt.name;
		}
	
		RUNTIME_Plugin_Runtime_SelectGroup.callback(addedgroups);
	}
	else
	{
		RUNTIME_Plugin_Runtime_SelectGroup.box.close();
	}
}
	
// --------------------------------
RUNTIME_Plugin_Runtime_SelectGroup.cancel = function ()
{
	RUNTIME_Plugin_Runtime_SelectGroup.box.close();

	if (RUNTIME_Plugin_Runtime_SelectGroup.cancelCallback != null)
	{
		RUNTIME_Plugin_Runtime_SelectGroup.cancelCallback();
	}
}
