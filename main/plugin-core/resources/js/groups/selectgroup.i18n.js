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
RUNTIME_Plugin_Runtime_SelectGroup.initialized = false;
// --------------------------------
RUNTIME_Plugin_Runtime_SelectGroup.delayed_initialize = function ()
{
	if (RUNTIME_Plugin_Runtime_SelectGroup.initialized)
		return true;
	RUNTIME_Plugin_Runtime_SelectGroup.initialized = true;

	var plugin = RUNTIME_Plugin_Runtime_SelectGroup.plugin;

	RUNTIME_Plugin_Runtime_SelectGroup.criteria = new Ext.form.TextField ({
		 listeners: {'keyup': RUNTIME_Plugin_Runtime_SelectGroup.reload},
		 fieldLabel: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_FIND"/>",
		 name: "criteria",
		 width: 140,
		 enableKeyEvents: true,
		 value: ""
	});
	
	var form = new Ext.FormPanel( {
		formId : 'select-group-form',
		labelWidth :70,
		border: false,
		items: [RUNTIME_Plugin_Runtime_SelectGroup.criteria]
	});
	
	RUNTIME_Plugin_Runtime_SelectGroup.listview = new org.ametys.ListView({
	    store : new Ext.data.SimpleStore({
			id:0,
	        fields: [
	           {name: 'label'}
	        ]
	    }),
	    hideHeaders : true,
	    columns: [
	        {header: "Nom", width : 240, menuDisabled : true, sortable: true, dataIndex: 'label'}
	    ],
		id: 'select-group-list',
		baseCls: 'select-group-list',
		autoScroll: true,
	    height:200
	});	
	RUNTIME_Plugin_Runtime_SelectGroup.listview.setMultipleSelection(true);
	
	var warning = new org.ametys.HtmlContainer ({
		html: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_WARN100"/>",
		cls: 'select-group-warning'
	});
	
	RUNTIME_Plugin_Runtime_SelectGroup.box = new org.ametys.DialogBox({
		title :"<i18n:text i18n:key="PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_CAPTION"/>",
		layout :'anchor',
		width :280,
		height : 340,
		cls : 'select-group-box',
		icon: getPluginResourcesUrl('core') + '/img/groups/icon_small.png',
		items : [form, RUNTIME_Plugin_Runtime_SelectGroup.listview, warning ],
		closeAction: 'hide',
		buttons : [ {
			text :"<i18n:text i18n:key="PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_OK"/>",
			handler : function() {
			RUNTIME_Plugin_Runtime_SelectGroup.ok();
			}
		}, {
			text :"<i18n:text i18n:key="PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_CANCEL"/>",
			handler : function() {
			RUNTIME_Plugin_Runtime_SelectGroup.cancel();
			}
		} ]
	});
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
	
	RUNTIME_Plugin_Runtime_SelectGroup.criteria.setValue("");
	RUNTIME_Plugin_Runtime_SelectGroup.load();

	RUNTIME_Plugin_Runtime_SelectGroup.box.show();
	try
	{
		RUNTIME_Plugin_Runtime_SelectGroup.criteria.focus();
	} catch (e) {}
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectGroup.reload = function (field, newValue, oldValue)
{
	if (RUNTIME_Plugin_Runtime_SelectGroup.reloadTimer != null)
		window.clearTimeout(RUNTIME_Plugin_Runtime_SelectGroup.reloadTimer);
	RUNTIME_Plugin_Runtime_SelectGroup.reloadTimer = window.setTimeout(RUNTIME_Plugin_Runtime_SelectGroup.load, 500);
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectGroup.load = function ()
{
	RUNTIME_Plugin_Runtime_SelectGroup.reloadTimer = null;

	var criteria = RUNTIME_Plugin_Runtime_SelectGroup.criteria.getValue();

	var result = Tools.postFromUrl(getPluginDirectUrl(RUNTIME_Plugin_Runtime_SelectGroup.plugin) + "/groups/selectgroup/search.xml", "criteria=" + criteria + "&amp;count=100" + "&amp;offset=0");
	if (result == null)
	{
		alert("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_ERROR_LISTING"/>")
		return;
	}	

	RUNTIME_Plugin_Runtime_SelectGroup.listview.getStore().removeAll();
	
	var groups = result.selectNodes("/Search/groups/group");

	for (var i=0; i &lt; groups.length; i++)
	{
		var label = groups[i].selectSingleNode('label')[Tools.xmlTextContent] + " (" + groups[i].getAttribute('id') + ")";
		RUNTIME_Plugin_Runtime_SelectGroup.listview.addElement(groups[i].getAttribute('id'), {label: label});
	}
	if (groups.length == 0)
    {
       alert("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_NORESULT"/>");
    }
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

		RUNTIME_Plugin_Runtime_SelectGroup.box.hide();
		
		for (var i=0; i &lt; selection.length; i++)
		{
			var opt = selection[i];
			addedgroups[opt.id] = opt.get('name');
		}
	
		RUNTIME_Plugin_Runtime_SelectGroup.callback(addedgroups);
	}
	else
	{
		RUNTIME_Plugin_Runtime_SelectGroup.box.hide();
	}
}
	
// --------------------------------
RUNTIME_Plugin_Runtime_SelectGroup.cancel = function ()
{
	RUNTIME_Plugin_Runtime_SelectGroup.box.hide();

	if (RUNTIME_Plugin_Runtime_SelectGroup.cancelCallback != null)
	{
		RUNTIME_Plugin_Runtime_SelectGroup.cancelCallback();
	}
}
