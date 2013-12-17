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
// 				SELECT GROUPS 
// ----------------------------------------------------------------
function RUNTIME_Plugin_Runtime_SelectGroups()
{
}
RUNTIME_Plugin_Runtime_SelectGroups.groupsManagerRole = null;
RUNTIME_Plugin_Runtime_SelectGroups.initialized = false;
// --------------------------------
RUNTIME_Plugin_Runtime_SelectGroups.delayed_initialize = function ()
{
	if (RUNTIME_Plugin_Runtime_SelectGroups.initialized)
		return true;
	RUNTIME_Plugin_Runtime_SelectGroups.initialized = true;

	var plugin = RUNTIME_Plugin_Runtime_SelectGroups.plugin;

	RUNTIME_Plugin_Runtime_SelectGroups.listview = new org.ametys.ListView({
		region: 'center',
		
	    store : new Ext.data.SimpleStore({
			id:0,
	        fields: [
	           {name: 'name'}
	        ]
	    }),
	    hideHeaders : true,
	    columns: [
	        {header: "Nom", width : 240, menuDisabled : true, sortable: true, dataIndex: 'name'}
	    ],
		id: 'select-group-list',
		baseCls: 'select-group-list',
		autoScroll: true
	});	
	RUNTIME_Plugin_Runtime_SelectGroups.listview.getSelectionModel().addListener('selectionchange', RUNTIME_Plugin_Runtime_SelectGroups._onSelectGroups, this);
	
	RUNTIME_Plugin_Runtime_SelectGroups.buttons = new Ext.Panel({
		region: 'east',
		cls: 'buttons',
		border: false,
		width: 100,
		items: [
			new Ext.Button({
				width: 80,
				text: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_SELECTGROUPS_BTN_ADDGROUP"/>",
				handler : RUNTIME_Plugin_Runtime_SelectGroups._addGroup
			}),
			new Ext.Button({
				width: 80,
				text: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_SELECTGROUPS_BTN_DELETEGROUPS"/>",
				handler : RUNTIME_Plugin_Runtime_SelectGroups._deleteGroups,
				disabled: true
			})
		]
	});
	RUNTIME_Plugin_Runtime_SelectGroups.listview.setMultipleSelection(true);
	
	RUNTIME_Plugin_Runtime_SelectGroups.box = new org.ametys.DialogBox({
		title :"<i18n:text i18n:key="PLUGINS_CORE_GROUPS_SELECTGROUPS_DIALOG_CAPTION"/>",
		layout :'border',
		width :360,
		height : 340,
		cls : 'select-user-box',
		icon: getPluginResourcesUrl('core') + '/img/users/icon_small.png',
		items : [RUNTIME_Plugin_Runtime_SelectGroups.listview, RUNTIME_Plugin_Runtime_SelectGroups.buttons],
		closeAction: 'hide',
		buttons : [ {
			text :"<i18n:text i18n:key="PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_OK"/>",
			handler : function() {
				RUNTIME_Plugin_Runtime_SelectGroups.ok();
			}
		}, {
			text :"<i18n:text i18n:key="PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_CANCEL"/>",
			handler : function() {
				RUNTIME_Plugin_Runtime_SelectGroups.cancel();
			}
		} ]
	});
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectGroups.initialize = function (plugin)
{
	RUNTIME_Plugin_Runtime_SelectGroups.plugin = plugin;
}

RUNTIME_Plugin_Runtime_SelectGroups._onSelectGroups = function (sm)
{
	var selection = RUNTIME_Plugin_Runtime_SelectGroups.listview.getSelection();
	RUNTIME_Plugin_Runtime_SelectGroups.buttons.items.get(1).setDisabled(selection.length == 0);
}

RUNTIME_Plugin_Runtime_SelectGroups._addGroup = function ()
{
	RUNTIME_Plugin_Runtime_SelectGroup.initialize('core');
	RUNTIME_Plugin_Runtime_SelectGroup.act(RUNTIME_Plugin_Runtime_SelectGroups._addGroupCb, null, RUNTIME_Plugin_Runtime_SelectGroups.groupsManagerRole, true);	
}
RUNTIME_Plugin_Runtime_SelectGroups._addGroupCb = function (groups)
{
	if (groups.length == 0)
	{
		return;
	}
	
	var records = [];
	for (var i in groups)
	{
		var record = new Ext.data.Record();
		record.id = i;
		record.data.name = groups[i];
		records.push(record);
	}
	
	RUNTIME_Plugin_Runtime_SelectGroups.listview.getStore().add(records);
}
RUNTIME_Plugin_Runtime_SelectGroups._deleteGroups = function ()
{
	var selection = RUNTIME_Plugin_Runtime_SelectGroups.listview.getSelection();
	for (var i=0; i &lt; selection.length; i++)
	{
		RUNTIME_Plugin_Runtime_SelectGroups.listview.getStore().remove(selection[i]);
	}
}
// --------------------------------
/**
 * {Function}
 * {Function}
 * {String} the avalon role of the groups manager which will be called to get the group list, or null to call the default groups manager.
 */
RUNTIME_Plugin_Runtime_SelectGroups.act = function (callback, cancelCallback, groupsManagerRole, groups)
{
	RUNTIME_Plugin_Runtime_SelectGroups.delayed_initialize();
	RUNTIME_Plugin_Runtime_SelectGroups.callback = callback;
	RUNTIME_Plugin_Runtime_SelectGroups.cancelCallback = cancelCallback;
    RUNTIME_Plugin_Runtime_SelectGroups.groupsManagerRole = groupsManagerRole || '';
	
    RUNTIME_Plugin_Runtime_SelectGroups.init(groups);
    
	RUNTIME_Plugin_Runtime_SelectGroups.box.show();
}
//---------------------------------
RUNTIME_Plugin_Runtime_SelectGroups.init = function (groups)
{
	RUNTIME_Plugin_Runtime_SelectGroups.listview.getStore().removeAll();
	
	var records = [];
	for (var i in groups)
	{
		var record = new Ext.data.Record();
		record.id = i;
		record.data.name = groups[i];
		records.push(record);
	}
	
	RUNTIME_Plugin_Runtime_SelectGroups.listview.getStore().add(records);
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectGroups.ok = function ()
{
	if (RUNTIME_Plugin_Runtime_SelectGroups.callback != null)
	{
		var addedgroups = {}
		
		var store = RUNTIME_Plugin_Runtime_SelectGroups.listview.getStore();
		var records = store.data.items;
		
		RUNTIME_Plugin_Runtime_SelectGroups.box.hide();
		
		for (var i=0; i &lt; records.length; i++)
		{
			var opt = records[i];
			addedgroups[opt.id] = opt.get('name');
		}
	
		RUNTIME_Plugin_Runtime_SelectGroups.callback(addedgroups);
	}
	else
	{
		RUNTIME_Plugin_Runtime_SelectGroups.box.hide();
	}
}
	
// --------------------------------
RUNTIME_Plugin_Runtime_SelectGroups.cancel = function ()
{
	RUNTIME_Plugin_Runtime_SelectGroups.box.hide();

	if (RUNTIME_Plugin_Runtime_SelectGroups.cancelCallback != null)
	{
		RUNTIME_Plugin_Runtime_SelectGroups.cancelCallback();
	}
}
