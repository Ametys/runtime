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
// 				SELECT USER 
// ----------------------------------------------------------------
function RUNTIME_Plugin_Runtime_SelectUser()
{
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectUser.usersManagerRole = null;
RUNTIME_Plugin_Runtime_SelectUser.initialized = false;
// --------------------------------
RUNTIME_Plugin_Runtime_SelectUser.delayed_initialize = function ()
{
	if (RUNTIME_Plugin_Runtime_SelectUser.initialized)
		return true;
	RUNTIME_Plugin_Runtime_SelectUser.initialized = true;

	var plugin = RUNTIME_Plugin_Runtime_SelectUser.plugin;

	RUNTIME_Plugin_Runtime_SelectUser.criteria = new Ext.form.TextField ({
		 listeners: {'keyup': RUNTIME_Plugin_Runtime_SelectUser.reload},
		 fieldLabel: "<i18n:text i18n:key="PLUGINS_CORE_USERS_SELECTUSER_DIALOG_FIND"/>",
		 name: "criteria",
		 width: 140,
		 enableKeyEvents: true,
		 value: ""
	});
	
	var form = new Ext.FormPanel( {
		region: 'north',
		height: 37,
		
		formId : 'select-user-form',
		labelWidth :70,
		border: false,
		items: [RUNTIME_Plugin_Runtime_SelectUser.criteria]
	});
	
	RUNTIME_Plugin_Runtime_SelectUser.listview = new org.ametys.ListView({
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
	RUNTIME_Plugin_Runtime_SelectUser.listview.setMultipleSelection(false);
	
	var warning = new org.ametys.HtmlContainer ({
		region: 'south',
		height: 26,
		cls: 'select-user-warning',
		
		html: "<i18n:text i18n:key="PLUGINS_CORE_USERS_SELECTUSER_DIALOG_WARN100"/>"
	});
	
	RUNTIME_Plugin_Runtime_SelectUser.box = new org.ametys.DialogBox({
		title :"<i18n:text i18n:key="PLUGINS_CORE_USERS_SELECTUSER_DIALOG_CAPTION"/>",
		layout :'border',
		width :280,
		height : 340,
		cls : 'select-user-box',
		icon: getPluginResourcesUrl('core') + '/img/users/icon_small.png',
		items : [form, RUNTIME_Plugin_Runtime_SelectUser.listview, warning],
		closeAction: 'hide',
		buttons : [ {
			text :"<i18n:text i18n:key="PLUGINS_CORE_USERS_SELECTUSER_DIALOG_OK"/>",
			handler : function() {
			RUNTIME_Plugin_Runtime_SelectUser.ok();
			}
		}, {
			text :"<i18n:text i18n:key="PLUGINS_CORE_USERS_SELECTUSER_DIALOG_CANCEL"/>",
			handler : function() {
			RUNTIME_Plugin_Runtime_SelectUser.cancel();
			}
		} ]
	});
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectUser.initialize = function (plugin)
{
	RUNTIME_Plugin_Runtime_SelectUser.plugin = plugin;
}
// --------------------------------
/**
 * {Function}
 * {Function}
 * {String} the avalon role of the users manager which will be called to get the user list, or null to call the default users manager.
 */
RUNTIME_Plugin_Runtime_SelectUser.act = function (callback, cancelCallback, usersManagerRole)
{
	RUNTIME_Plugin_Runtime_SelectUser.delayed_initialize();
	RUNTIME_Plugin_Runtime_SelectUser.callback = callback;
	RUNTIME_Plugin_Runtime_SelectUser.cancelCallback = cancelCallback;
    RUNTIME_Plugin_Runtime_SelectUser.usersManagerRole = usersManagerRole || '';
	
	RUNTIME_Plugin_Runtime_SelectUser.criteria.setValue("");

	RUNTIME_Plugin_Runtime_SelectUser.box.show();
	
	RUNTIME_Plugin_Runtime_SelectUser.load();
	
	try
	{
		RUNTIME_Plugin_Runtime_SelectUser.criteria.focus();
	} catch (e) {}
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectUser.reload = function (field, newValue, oldValue)
{
	if (RUNTIME_Plugin_Runtime_SelectUser.reloadTimer != null)
		window.clearTimeout(RUNTIME_Plugin_Runtime_SelectUser.reloadTimer);
	RUNTIME_Plugin_Runtime_SelectUser.reloadTimer = window.setTimeout(RUNTIME_Plugin_Runtime_SelectUser.load, 500);
}
// --------------------------------
RUNTIME_Plugin_Runtime_SelectUser.load = function ()
{
	RUNTIME_Plugin_Runtime_SelectUser.reloadTimer = null;

	var criteria = RUNTIME_Plugin_Runtime_SelectUser.criteria.getValue();

	// Get the user list from the UsersManager.
	var params = { criteria: criteria, count: 100, offset: 0, usersManagerRole: RUNTIME_Plugin_Runtime_SelectUser.usersManagerRole };
	var serverMessage = new org.ametys.servercomm.ServerMessage(RUNTIME_Plugin_Runtime_SelectUser.plugin, "users/search.xml", params, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
	var result = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);

    if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_USERS_SELECTUSER_DIALOG_ERROR_LISTING"/>", result, "RUNTIME_Plugin_Runtime_SelectUser.load"))
    {
       return;
    }

	RUNTIME_Plugin_Runtime_SelectUser.listview.getStore().removeAll();
	
	var users = result.selectNodes("Search/users/user");

	for (var i=0; i &lt; users.length; i++)
	{
		var fullname = (users[i].selectSingleNode('firstname') != null ? users[i].selectSingleNode('firstname')[org.ametys.servercomm.ServerComm.xmlTextContent] + " " + users[i].selectSingleNode('lastname')[org.ametys.servercomm.ServerComm.xmlTextContent] : users[i].selectSingleNode('lastname')[org.ametys.servercomm.ServerComm.xmlTextContent]) + " (" + users[i].getAttribute('login') + ")";
		RUNTIME_Plugin_Runtime_SelectUser.listview.addElement(users[i].getAttribute('login'), {name: fullname});
	}
	if (users.length == 0)
    {
		Ext.Msg.show({
			   title: "<i18n:text i18n:key="PLUGINS_CORE_USERS_SELECTUSER_DIALOG_CAPTION"/>",
			   msg: "<i18n:text i18n:key="PLUGINS_CORE_USERS_SELECTUSER_DIALOG_NORESULT"/>",
			   buttons: Ext.Msg.OK,
			   icon: Ext.MessageBox.INFO
			});
    }
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
			Ext.Msg.show({
				   title: "<i18n:text i18n:key="PLUGINS_CORE_USERS_SELECTUSER_DIALOG_CAPTION"/>",
				   msg: "<i18n:text i18n:key="PLUGINS_CORE_USERS_SELECTUSER_DIALOG_ERROR_EMPTY"/>",
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.INFO
				});
			return;
		}

		RUNTIME_Plugin_Runtime_SelectUser.box.hide();
		
		for (var i=0; i &lt; selection.length; i++)
		{
			var opt = selection[i];
			addedusers[opt.id] = opt.get('name');
		}
	
		RUNTIME_Plugin_Runtime_SelectUser.callback(addedusers);
	}
	else
	{
		RUNTIME_Plugin_Runtime_SelectUser.box.hide();
	}
}
	
// --------------------------------
RUNTIME_Plugin_Runtime_SelectUser.cancel = function ()
{
	RUNTIME_Plugin_Runtime_SelectUser.box.hide();

	if (RUNTIME_Plugin_Runtime_SelectUser.cancelCallback != null)
	{
		RUNTIME_Plugin_Runtime_SelectUser.cancelCallback();
	}
}
