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
// 				SELECT USERS 
// ----------------------------------------------------------------
function RUNTIME_Plugin_Runtime_SelectUsers()
{
}
RUNTIME_Plugin_Runtime_SelectUsers.usersManagerRole = null;
RUNTIME_Plugin_Runtime_SelectUsers.initialized = false;
// --------------------------------
RUNTIME_Plugin_Runtime_SelectUsers.delayed_initialize = function ()
{
	if (RUNTIME_Plugin_Runtime_SelectUsers.initialized)
		return true;
	RUNTIME_Plugin_Runtime_SelectUsers.initialized = true;

	var plugin = RUNTIME_Plugin_Runtime_SelectUsers.plugin;

	RUNTIME_Plugin_Runtime_SelectUsers.listview = new org.ametys.ListView({
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
		id: 'select-user-list',
		baseCls: 'select-user-list',
		autoScroll: true
	});	
	RUNTIME_Plugin_Runtime_SelectUsers.listview.getSelectionModel().addListener('selectionchange', RUNTIME_Plugin_Runtime_SelectUsers._onSelectUsers, this);
	
	RUNTIME_Plugin_Runtime_SelectUsers.buttons = new Ext.Panel({
		region: 'east',
		cls: 'buttons',
		border: false,
		width: 100,
		items: [
			new Ext.Button({
				width: 80,
				text: "<i18n:text i18n:key="PLUGINS_CORE_USERS_SELECTUSERS_BTN_ADDUSER"/>",
				handler : RUNTIME_Plugin_Runtime_SelectUsers._addUser
			}),
			new Ext.Button({
				width: 80,
				text: "<i18n:text i18n:key="PLUGINS_CORE_USERS_SELECTUSERS_BTN_DELETEUSERS"/>",
				handler : RUNTIME_Plugin_Runtime_SelectUsers._deleteUsers,
				disabled: true
			})
		]
	});
	RUNTIME_Plugin_Runtime_SelectUsers.listview.setMultipleSelection(true);
	
	RUNTIME_Plugin_Runtime_SelectUsers.box = new org.ametys.DialogBox({
		title :"<i18n:text i18n:key="PLUGINS_CORE_USERS_SELECTUSERS_DIALOG_CAPTION"/>",
		layout :'border',
		width :360,
		height : 340,
		cls : 'select-user-box',
		icon: getPluginResourcesUrl('core') + '/img/users/icon_small.png',
		items : [RUNTIME_Plugin_Runtime_SelectUsers.listview, RUNTIME_Plugin_Runtime_SelectUsers.buttons],
		closeAction: 'hide',
		buttons : [ {
			text :"<i18n:text i18n:key="PLUGINS_CORE_USERS_SELECTUSER_DIALOG_OK"/>",
			handler : function() {
				RUNTIME_Plugin_Runtime_SelectUsers.ok();
			}
		}, {
			text :"<i18n:text i18n:key="PLUGINS_CORE_USERS_SELECTUSER_DIALOG_CANCEL"/>",
			handler : function() {
				RUNTIME_Plugin_Runtime_SelectUsers.cancel();
			}
		} ]
	});
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectUsers.initialize = function (plugin)
{
	RUNTIME_Plugin_Runtime_SelectUsers.plugin = plugin;
}

RUNTIME_Plugin_Runtime_SelectUsers._onSelectUsers = function (sm)
{
	var selection = RUNTIME_Plugin_Runtime_SelectUsers.listview.getSelection();
	RUNTIME_Plugin_Runtime_SelectUsers.buttons.items.get(1).setDisabled(selection.length == 0);
}

RUNTIME_Plugin_Runtime_SelectUsers._addUser = function ()
{
	RUNTIME_Plugin_Runtime_SelectUser.initialize('core');
	RUNTIME_Plugin_Runtime_SelectUser.act(RUNTIME_Plugin_Runtime_SelectUsers._addUserCb, null, RUNTIME_Plugin_Runtime_SelectUsers.usersManagerRole);	
}
RUNTIME_Plugin_Runtime_SelectUsers._addUserCb = function (users)
{
	if (users.length == 0)
	{
		return;
	}
	
	var records = [];
	for (var i in users)
	{
		var record = new Ext.data.Record();
		record.id = i;
		record.data.name = users[i];
		records.push(record);
	}
	
	RUNTIME_Plugin_Runtime_SelectUsers.listview.getStore().add(records);
}
RUNTIME_Plugin_Runtime_SelectUsers._deleteUsers = function ()
{
	var selection = RUNTIME_Plugin_Runtime_SelectUsers.listview.getSelection();
	for (var i=0; i &lt; selection.length; i++)
	{
		RUNTIME_Plugin_Runtime_SelectUsers.listview.getStore().remove(selection[i]);
	}
}
// --------------------------------
/**
 * {Function}
 * {Function}
 * {String} the avalon role of the users manager which will be called to get the user list, or null to call the default users manager.
 */
RUNTIME_Plugin_Runtime_SelectUsers.act = function (callback, cancelCallback, usersManagerRole, users)
{
	RUNTIME_Plugin_Runtime_SelectUsers.delayed_initialize();
	RUNTIME_Plugin_Runtime_SelectUsers.callback = callback;
	RUNTIME_Plugin_Runtime_SelectUsers.cancelCallback = cancelCallback;
    RUNTIME_Plugin_Runtime_SelectUsers.usersManagerRole = usersManagerRole || '';
	
    RUNTIME_Plugin_Runtime_SelectUsers.init(users);
    
	RUNTIME_Plugin_Runtime_SelectUsers.box.show();
}
//---------------------------------
RUNTIME_Plugin_Runtime_SelectUsers.init = function (users)
{
	RUNTIME_Plugin_Runtime_SelectUsers.listview.getStore().removeAll();
	
	var records = [];
	for (var i in users)
	{
		var record = new Ext.data.Record();
		record.id = i;
		record.data.name = users[i];
		records.push(record);
	}
	
	RUNTIME_Plugin_Runtime_SelectUsers.listview.getStore().add(records);
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectUsers.ok = function ()
{
	if (RUNTIME_Plugin_Runtime_SelectUsers.callback != null)
	{
		var addedusers = {}
		
		var store = RUNTIME_Plugin_Runtime_SelectUsers.listview.getStore();
		var records = store.data.items;
		
		RUNTIME_Plugin_Runtime_SelectUsers.box.hide();
		
		for (var i=0; i &lt; records.length; i++)
		{
			var opt = records[i];
			addedusers[opt.id] = opt.get('name');
		}
	
		RUNTIME_Plugin_Runtime_SelectUsers.callback(addedusers);
	}
	else
	{
		RUNTIME_Plugin_Runtime_SelectUsers.box.hide();
	}
}
	
// --------------------------------
RUNTIME_Plugin_Runtime_SelectUsers.cancel = function ()
{
	RUNTIME_Plugin_Runtime_SelectUsers.box.hide();

	if (RUNTIME_Plugin_Runtime_SelectUsers.cancelCallback != null)
	{
		RUNTIME_Plugin_Runtime_SelectUsers.cancelCallback();
	}
}
