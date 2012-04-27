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

Ext.namespace('org.ametys.administration.Groups');

org.ametys.administration.Groups = function ()
{
}

org.ametys.administration.Groups._modifiable;
org.ametys.administration.Groups._hasChanges;
org.ametys.administration.Groups._currentGroup;

org.ametys.administration.Groups.initialize = function (pluginName)
{
	org.ametys.administration.Groups.pluginName = pluginName;
}

org.ametys.administration.Groups.createPanel = function ()
{
	var cm = new Ext.grid.ColumnModel([{
        id:'name',
        
        header: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_LABEL"/>",
        dataIndex: 'name',
        
        width: 500,
        editor: new Ext.form.TextField({
            allowBlank: false
        })
    }]);
	
	org.ametys.administration.Groups._listViewGp = new org.ametys.EditorListView ({
		id: 'list-view-groups',
		region: 'north',
		
		title : "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_LABEL"/>",
		
		baseCls: 'group-list',
		height: 200,
		minSize: 100,
		maxSize: 310,
		autoScroll: true,
		
	    store : new Ext.data.SimpleStore({
	    	listeners: {'update': org.ametys.administration.Groups._editGroupLabel},
			id:0,
	        fields: [
	        	{name: 'id'},
	        	{name: 'name'}
	        ],
	        sortInfo: {field : 'name', direction: "ASC"}
		}),
		
		
		hideHeaders: true,
		sm: new Ext.grid.CellSelectionModel({singleSelect:true}),
	   	cm: cm,
	   	clicksToEdit:2,
		
		listeners: {'rowclick': org.ametys.administration.Groups._selectGroup, 
					'validateedit': org.ametys.administration.Groups._validateEdit}
	});	
	
	org.ametys.administration.Groups._listViewU = new org.ametys.ListView({
		region: 'center',
		id: 'list-view-user',
		
		baseCls: 'user-list',
		autoScroll: true,
		
		title : "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_COLUMN"/>",
	    
		store : new Ext.data.SimpleStore({
				id:0,
	        	fields: [
	        	         {name: 'user'},
	        	         {name: 'type'}
	           	],
	           	sortInfo: {field : 'user', direction: "ASC"}
		}),
		
		hideHeaders: true,
	    columns: [
	        {width : 500, menuDisabled : true, sortable: true, dataIndex: 'user'}
	    ]
	});						
	org.ametys.administration.Groups._listViewU.setMultipleSelection(true);
	
	
	org.ametys.administration.Groups._contextualPanel = new org.ametys.HtmlContainer({
		region:'east',
	
		cls : 'admin-right-panel',
		border: false,
		width: 277,
	    
		html : ''
	});
	
	org.ametys.administration.Groups._contextualPanel.add (org.ametys.administration.Groups._drawGroupsActionsPanel ());
	if (org.ametys.administration.Groups._modifiable)
	{
		org.ametys.administration.Groups._contextualPanel.add (org.ametys.administration.Groups._drawUsersActionsPanel ());
	}
	org.ametys.administration.Groups._contextualPanel.add (org.ametys.administration.Groups._drawHelpPanel ());
	
	
	org.ametys.administration.Groups._groupsPanel = new Ext.Panel({
		region: 'center',
		
		defaults: {
		    split: true
		},
		layout: 'border',
		
		baseCls: 'transparent-panel',
		border: false,
		autoScroll: false,
		margins: '0 20 0 0',
		
		items: [org.ametys.administration.Groups._listViewGp, 
		        org.ametys.administration.Groups._listViewU]
	});
	
	return new Ext.Panel({
		region: 'center',
		
		baseCls: 'transparent-panel',
		border: false,
		layout: 'border',
		
		autoScroll: false,
		items: [org.ametys.administration.Groups._groupsPanel, 
		        org.ametys.administration.Groups._contextualPanel]
	});
}

/**
 * This function is call when a group is selected
 */
org.ametys.administration.Groups._selectGroup = function (grid, rowindex, e)
{
	if (org.ametys.administration.Groups._currentGroup != null &amp;&amp; org.ametys.administration.Groups._hasChanges)
	{
		Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_SAVE_DIALOG_TITLE"/>", 
						 "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_MODIFY_CONFIRM"/>", 
						 function (button) {
							org.ametys.administration.Groups._saveConfirm (button, org.ametys.administration.Groups._currentGroup)
						}
		);
	}
	
	if (org.ametys.administration.Groups._modifiable)
	{
		org.ametys.administration.Groups._groupsActions.showElt(1);
		org.ametys.administration.Groups._groupsActions.showElt(2);
		
		org.ametys.administration.Groups._usersActions.setVisible(true);
		org.ametys.administration.Groups._usersActions.showElt(4);
		org.ametys.administration.Groups._usersActions.showElt(5);
		org.ametys.administration.Groups._usersActions.hideElt(6);
	}
	
	org.ametys.administration.Groups._listViewU.getStore().removeAll();
	
	var group = org.ametys.administration.Groups._listViewGp.getStore().getAt(rowindex);
	org.ametys.administration.Groups._currentGroup = group; 
    
	var serverMessage = new org.ametys.servercomm.ServerMessage(org.ametys.administration.Groups.pluginName, "/groups/members", { groupID: group.get('id'), sitename: context.siteName }, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
	var result = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);

    if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CMS_PROFILES_ERROR_TITLE"/>", result, "org.ametys.administration.Groups._selectGroup"))
    {
       throw "org.ametys.administration.Groups._selectGroup request failed";
    }
   
	var members = result.selectNodes("GroupMembers/User");
	for (var i=0; i &lt; members.length; i++)
	{
		var fullname = members[i].selectSingleNode("FullName")[org.ametys.servercomm.ServerComm.xmlTextContent];
		var login =  members[i].getAttribute("login");
		org.ametys.administration.Groups._listViewU.addElement(login, {user: fullname + "(" + login + ")"}, true);
	}
}


org.ametys.administration.Groups._saveConfirm = function (button, group)
{
	if (button == 'yes')
	{
		org.ametys.administration.Groups.save(group);
	}
	else
	{
		org.ametys.administration.Groups._hasChanges = false;
	}
}

org.ametys.administration.Groups._validateEdit  = function (e)
{
	return true;
}


/**
 * Create or rename a group
 */
org.ametys.administration.Groups._editGroupLabel = function (store, record, operation)
{
	if (operation == Ext.data.Record.EDIT)
	{
		// Rename
		var serverMessage = new org.ametys.servercomm.ServerMessage(org.ametys.administration.Groups.pluginName, "/groups/rename", { id: record.data.id, name: record.get('name') }, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
		var result = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);

	    if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_RENAME_ERROR"/>", result, "org.ametys.administration.Groups._editGroupLabel"))
	    {
	       // nothing
	    }
		else
		{
			var state = org.ametys.servercomm.ServerComm.handleResponse(result, "message"); 
			if (state != null &amp;&amp; state == "missing")
			{
				Ext.Msg.show ({
            		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
            		msg: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_RENAME_MISSING_ERROR"/>",
            		buttons: Ext.Msg.OK,
   					icon: Ext.MessageBox.ERROR
            	});
				org.ametys.administration.Groups._listViewGp.removeElement(record);
			}
		}
		record.commit();
		// Sort
		org.ametys.administration.Groups._listViewGp.getStore().sort('name', 'ASC');
	}
}

/**
 * Back to the administration home page
 */
org.ametys.administration.Groups.goBack = function ()
{
    document.location.href = context.workspaceContext;
}   

/**
 * Add an user
 */
org.ametys.administration.Groups.addUser = function () 
{
	function cb (users)
	{
		function seek (arr, id)
		{
			for (var i=0; i&lt;arr.length; i++)
			{
				if (arr[i].id == id)
					return arr[i];
			}
			return null;
		}
	
		var selectedElements = new Array();
		var existingElements = org.ametys.administration.Groups._listViewU.getElements();
		
		for (var i in users)
		{
			var e = seek(existingElements, i);
			
			if (e == null)
				e = org.ametys.administration.Groups._listViewU.addElement(i, {"user": users[i], "type": "user"}, true);
			selectedElements.push(e);
		}
		org.ametys.administration.Groups._needSave();
	}
	
	RUNTIME_Plugin_Runtime_SelectUser.act(cb);							
}

/**
 * Delete users
 */
org.ametys.administration.Groups.deleteUsers = function () 
{
    var elts = org.ametys.administration.Groups._listViewU.getSelection();
	for (var i = 0; i &lt; elts.length; i++)
	{
		org.ametys.administration.Groups._listViewU.removeElement(elts[i]);
	}
	org.ametys.administration.Groups._needSave();    
}	

/**
 * Save modifications
 */
org.ametys.administration.Groups.save = function (group)
{
	if (group == null)
	{
		group = org.ametys.administration.Groups._listViewGp.getSelection()[0];
	}
	
	var objects = "";
	var users = org.ametys.administration.Groups._listViewU.getStore().data.items;
	for (var i=0; i &lt; users.length; i++)
	{
		var objectId = users[i].id;
		objects += objectId + '/';
	}
	
	// Send
	var ok = false;
	while (!ok)
	{
		var serverMessage = new org.ametys.servercomm.ServerMessage(org.ametys.administration.Groups.pluginName, "/groups/modify", { id: group.id, objects: objects }, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
		var result = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);

	    if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_MODIFY_ERROR"/>", result, "org.ametys.administration.Groups.save"))
	    {
	       // nothing
	    }
		else 
		{
			var state = org.ametys.servercomm.ServerComm.handleResponse(result, "message"); 
			if (state != null &amp;&amp; state == "missing")
			{
				Ext.Msg.show ({
                		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
                		msg: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_MODIFY_MISSING_ERROR"/>",
                		buttons: Ext.Msg.OK,
	   					icon: Ext.MessageBox.ERROR
                });
				org.ametys.administration.Groups._listViewGp.removeElement(group);
			}
			else
			{
				ok = true;
				org.ametys.administration.Groups._usersActions.hideElt(6);
			}
		}
	}
	
	org.ametys.administration.Groups._hasChanges = false;
}

org.ametys.administration.Groups._needSave = function (field, newValue, oldValue)
{
	org.ametys.administration.Groups._usersActions.showElt(6);
	org.ametys.administration.Groups._hasChanges = true;
}

/**
 * Draw the actions panel.
 * @return {org.ametys.ActionsPanel} The action panel
 * @private
 */
org.ametys.administration.Groups._drawGroupsActionsPanel = function ()
{
	org.ametys.administration.Groups._groupsActions = new org.ametys.ActionsPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_CATEGORY"/>"});

	if (org.ametys.administration.Groups._modifiable)
	{
		// New group
		org.ametys.administration.Groups._groupsActions.addAction("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_CREATE"/>", 
				 getPluginResourcesUrl(org.ametys.administration.Groups.pluginName) + '/img/groups/add_group.png', 
				 org.ametys.administration.Groups.add);
		
		// Rename goup
		org.ametys.administration.Groups._groupsActions.addAction("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_RENAME"/>", 
				 getPluginResourcesUrl(org.ametys.administration.Groups.pluginName) + '/img/groups/rename.png', 
				 org.ametys.administration.Groups.rename);
		
		// Delete goup
		org.ametys.administration.Groups._groupsActions.addAction("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_DELETE"/>", 
				 getPluginResourcesUrl(org.ametys.administration.Groups.pluginName) + '/img/groups/delete.png', 
				 org.ametys.administration.Groups.remove);
	}
	
	// Quit
	org.ametys.administration.Groups._groupsActions.addAction("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_QUIT"/>", 
			 getPluginResourcesUrl(org.ametys.administration.Groups.pluginName) + '/img/groups/quit.png', 
			 org.ametys.administration.Groups.goBack);
	
	if (org.ametys.administration.Groups._modifiable)
	{
		org.ametys.administration.Groups._groupsActions.showElt(0);
		org.ametys.administration.Groups._groupsActions.hideElt(1);
		org.ametys.administration.Groups._groupsActions.hideElt(2);
	}
	
	return org.ametys.administration.Groups._groupsActions;
}

/**
 * Draw the actions panel.
 * @return {org.ametys.ActionsPanel} The action panel
 * @private
 */
org.ametys.administration.Groups._drawUsersActionsPanel = function ()
{
	if (!org.ametys.administration.Groups._modifiable)
		return null;
	
	org.ametys.administration.Groups._usersActions = new org.ametys.ActionsPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_USERS_HANDLE_CATEGORY"/>"});

	// Add user
	org.ametys.administration.Groups._usersActions.addAction("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_ADDUSER"/>", 
			 getPluginResourcesUrl(org.ametys.administration.Groups.pluginName) + '/img/groups/add_user.png', 
			 org.ametys.administration.Groups.addUser);
	
	// Delete user
	org.ametys.administration.Groups._usersActions.addAction("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_DELETEUSER"/>", 
			 getPluginResourcesUrl(org.ametys.administration.Groups.pluginName) + '/img/groups/delete.png', 
			 org.ametys.administration.Groups.deleteUsers);
	
	// Validate modification
	org.ametys.administration.Groups._usersActions.addAction("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_VALIDATE"/>", 
			 getPluginResourcesUrl(org.ametys.administration.Groups.pluginName) + '/img/groups/validate.png', 
			 org.ametys.administration.Groups.save);
	
	org.ametys.administration.Groups._usersActions.setVisible(false);
	
	return org.ametys.administration.Groups._usersActions;
}


/**
 * Draw the help panel.
 * @return {org.ametys.TextPanel} The help panel
 * @private
 */
org.ametys.administration.Groups._drawHelpPanel = function ()
{
	var helpPanel = new org.ametys.TextPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HELP_CATEGORY"/>"});
	helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_HELP_HINT"/>");
	
	return helpPanel;
}

/**
 * Add a new group
 */
org.ametys.administration.Groups.add = function ()
{
	// Create group
	var serverMessage = new org.ametys.servercomm.ServerMessage(org.ametys.administration.Groups.pluginName, "/groups/create", { name: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_NEWGROUP"/>" }, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
	var result = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);

    if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_NEW_ERROR"/>", result, "org.ametys.administration.Groups._editGroupLabel"))
    {
       return false;
    }
    
    var id = org.ametys.servercomm.ServerComm.handleResponse(result, "id");
    
	var record = org.ametys.administration.Groups._listViewGp.getStore().recordType;
	
	var newEntry = new record({
		name: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_NEWGROUP"/>",
		id: id
	}, id);
	
	org.ametys.administration.Groups._listViewGp.getStore().addSorted(newEntry);
	org.ametys.administration.Groups._listViewU.getStore().removeAll();
	
	if(org.ametys.administration.Groups._listViewGp.getStore().getCount() &gt; 0)
	{
		var index = org.ametys.administration.Groups._listViewGp.getStore().indexOfId(id);
		org.ametys.administration.Groups._listViewGp.getSelectionModel().select(index, 0);
	}
	else
	{
		org.ametys.administration.Groups._listViewGp.getSelectionModel().select(0, 0);
	}
	
	org.ametys.administration.Groups.rename ();

}

org.ametys.administration.Groups.rename = function ()
{
	var cell = org.ametys.administration.Groups._listViewGp.getSelectionModel().getSelectedCell();
	org.ametys.administration.Groups._listViewGp.startEditing(cell[0],cell[1])
}

org.ametys.administration.Groups.remove = function () 
{
	Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_HANDLE_DELETE"/>", 
					 "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_DELETE_CONFIRM"/>", 
					 org.ametys.administration.Groups.doRemove);
}
org.ametys.administration.Groups.doRemove = function (answer)
{
	if (answer == 'yes')
	{
		var elt = org.ametys.administration.Groups._listViewGp.getSelection()[0];
    	if (200 == org.ametys.servercomm.DirectComm.getInstance().sendSynchronousRequest(getPluginDirectUrl(org.ametys.administration.Groups.pluginName) + "/groups/delete", "id=" + elt.get('id')).status)
		{
    		org.ametys.administration.Groups._listViewGp.removeElement(elt);
		}
		else
		{
			Ext.Msg.show ({
        		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
        		msg: "<i18n:text i18n:key="PLUGINS_CORE_GROUPS_DELETE_ERROR"/>",
        		buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.ERROR
        	});
		}
		
    	org.ametys.administration.Groups._listViewU.getStore().removeAll();
    	
    	if (org.ametys.administration.Groups._modifiable)
    	{
    		org.ametys.administration.Groups._usersActions.setVisible(false);
    		org.ametys.administration.Groups._groupsActions.showElt(0);
    		org.ametys.administration.Groups._groupsActions.hideElt(1);
    		org.ametys.administration.Groups._groupsActions.hideElt(2);
    	}
    }
}
