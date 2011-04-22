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

// ----------------------------------------------------------------
// 				SELECT GROUP 
// ----------------------------------------------------------------
function RUNTIME_Plugin_Runtime_SelectGroup()
{
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectGroup.groupsManagerRole = null;
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
		defaultButton: RUNTIME_Plugin_Runtime_SelectGroup.criteria,
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
/**
 * {Function}
 * {Function}
 * {String} the avalon role of the users manager which will be called to get the user list, or null to call the default users manager.
 */
RUNTIME_Plugin_Runtime_SelectGroup.act = function (callback, cancelCallback, groupsManagerRole)
{
	RUNTIME_Plugin_Runtime_SelectGroup.delayed_initialize();
	RUNTIME_Plugin_Runtime_SelectGroup.callback = callback;
	RUNTIME_Plugin_Runtime_SelectGroup.cancelCallback = cancelCallback;
    RUNTIME_Plugin_Runtime_SelectGroup.groupsManagerRole = groupsManagerRole;
	
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

    // Get the group list from the GroupsManager.
    var params = { criteria: criteria, count: 100, offset: 0, groupsManagerRole: RUNTIME_Plugin_Runtime_SelectGroup.groupsManagerRole };
	var serverMessage = new org.ametys.servercomm.ServerMessage(RUNTIME_Plugin_Runtime_SelectGroup.plugin, "groups/selectgroup/search.xml", params, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
	var responseXML = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);

    if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_ERROR_LISTING"/>", responseXML, "RUNTIME_Plugin_Runtime_SelectGroup.load"))
    {
       return;
    }

	RUNTIME_Plugin_Runtime_SelectGroup.listview.getStore().removeAll();
	
	var groups = responseXML.selectNodes("Search/groups/group");

	for (var i=0; i &lt; groups.length; i++)
	{
		var label = groups[i].selectSingleNode('label')[org.ametys.servercomm.ServerComm.xmlTextContent] + " (" + groups[i].getAttribute('id') + ")";
		RUNTIME_Plugin_Runtime_SelectGroup.listview.addElement(groups[i].getAttribute('id'), {label: label});
	}
	if (groups.length == 0)
    {
		Ext.Msg.show ({
    		title: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_FIND"/>",
    		msg: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_NORESULT"/>",
    		buttons: Ext.Msg.OK,
			icon: Ext.MessageBox.INFO
		});
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
			Ext.Msg.show ({
        		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
        		msg: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_ERROR_EMPTY"/>",
        		buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.ERROR
        	});
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
