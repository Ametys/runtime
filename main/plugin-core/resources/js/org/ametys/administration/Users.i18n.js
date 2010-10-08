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

Ext.namespace('org.ametys.administration.Users');

org.ametys.administration.Users = function ()
{
}

org.ametys.administration.Users._modifiable;

org.ametys.administration.Users.initialize = function (pluginName)
{
	org.ametys.administration.Users.pluginName = pluginName;
}

org.ametys.administration.Users.createPanel = function ()
{
	// The users store
	var store = new Ext.data.SimpleStore({
		id:0,
        fields: [
           {name: 'login'},
           {name: 'display'},
           {name: 'firstname'},
           {name: 'lastname'},
           {name: 'email'}
        ]});
	
	org.ametys.administration.Users._listView = new org.ametys.ListView({
		region: 'center',
		
		id: 'detailView',
		baseCls : 'user-list',
		
		store : store,
		
		viewConfig: {
	        forceFit: true
	    },
	    
	    columns: [
	        {header: "<i18n:text i18n:key="PLUGINS_CORE_USERS_COL_NAME"/>", width : 200, menuDisabled : true, sortable: true, dataIndex: 'display'},
	        {header: "<i18n:text i18n:key="PLUGINS_CORE_USERS_COL_EMAIL"/>", width : 240, menuDisabled : true, sortable: true, dataIndex: 'email'}
	    ]
	});		
	
	org.ametys.administration.Users._listView.addListener ('rowclick', org.ametys.administration.Users._selectUser, this);
	org.ametys.administration.Users._listView.addListener ('mouseup', org.ametys.administration.Users._unSelectUser, this);
	
	
	org.ametys.administration.Users._contextualPanel = new org.ametys.HtmlContainer({
		region:'east',
	
		cls : 'admin-right-panel',
		border: false,
		width: 277,
	    
		items: [org.ametys.administration.Users._drawSearchPanel ()]
	});
	
	org.ametys.administration.Users._contextualPanel.add (org.ametys.administration.Users._drawActionsPanel ());
	org.ametys.administration.Users._contextualPanel.add (org.ametys.administration.Users._drawHelpPanel ());
	
	return new Ext.Panel({
		region: 'center',
		
		baseCls: 'transparent-panel',
		border: false,
		layout: 'border',
		
		autoScroll: true,
		
		items: [org.ametys.administration.Users._listView, 
		        org.ametys.administration.Users._contextualPanel]
	});
}


org.ametys.administration.Users._selectUser = function  (listview, rowindex, e)
{
	if (org.ametys.administration.Users._modifiable)
	{
		org.ametys.administration.Users._actions.showElt(2);
		org.ametys.administration.Users._actions.showElt(3);
		org.ametys.administration.Users._actions.showElt(4);
	}
	else
	{
		org.ametys.administration.Users._actions.show();
	}
}

org.ametys.administration.Users._unSelectUser = function  (e)
{
	if (org.ametys.administration.Users._modifiable)
	{
		org.ametys.administration.Users._actions.hideElt(2);
		org.ametys.administration.Users._actions.hideElt(3);
		org.ametys.administration.Users._actions.hideElt(4);
	}
	else
	{
		org.ametys.administration.Users._actions.hide();
	}
}

/**
 * Draw the search panel
 * @return {org.ametys.ActionsPanel} The search panel
 * @private
 */
org.ametys.administration.Users._drawSearchPanel = function ()
{
	org.ametys.administration.Users._search = new org.ametys.ActionsPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCH"/>"});
	
	org.ametys.administration.Users._searchForm = new Ext.form.FormPanel ({
		baseCls: 'search',
		
        items: [ new Ext.form.TextField ({
    		hideLabel: true,
            name: 'searchField',
            
            anchor:'100%',
            value: '<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCH_CRITERIA"/>',
            
            onFocus : function () {if (this.getValue() == '<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCH_CRITERIA"/>') this.setValue(''); },
            onBlur : function () {if (this.getValue() == '') this.setValue('<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCH_CRITERIA"/>'); }
    	})],
       	
       	buttonAlign: 'right',
        buttons: [{
            text: '<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCH_BUTTON"/>',
			handler : org.ametys.administration.Users.search	            
        }]
	});					        
	
	org.ametys.administration.Users._search.add(org.ametys.administration.Users._searchForm);
	
	// Quit
	org.ametys.administration.Users._search.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_QUIT"/>", 
				getPluginResourcesUrl(org.ametys.administration.Users.pluginName) + '/img/users/quit.png', 
				org.ametys.administration.Users.goBack);
	
	return org.ametys.administration.Users._search;
}

/**
 * Draw the actions panel.
 * @return {org.ametys.ActionsPanel} The action panel
 * @private
 */
org.ametys.administration.Users._drawActionsPanel = function ()
{
	org.ametys.administration.Users._actions = new org.ametys.ActionsPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE"/>"});

	if (org.ametys.administration.Users._modifiable)
	{
		// Add user
		org.ametys.administration.Users._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_NEW"/>",
				getPluginResourcesUrl(org.ametys.administration.Users.pluginName) + '/img/users/add_user.png', 
				org.ametys.administration.Users.add);
	}
	
	// Edit 
	org.ametys.administration.Users._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_IMPERSONATE"/>",
			getPluginResourcesUrl(org.ametys.administration.Users.pluginName) + '/img/users/impersonate_user.png', 
			org.ametys.administration.Users.impersonate);

	if (org.ametys.administration.Users._modifiable)
	{
		// Edit 
		org.ametys.administration.Users._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_EDIT"/>",
				getPluginResourcesUrl(org.ametys.administration.Users.pluginName) + '/img/users/edit_user.png', 
				org.ametys.administration.Users.edit);
	}
	
	if (org.ametys.administration.Users._modifiable)
	{
		// Delete 
		org.ametys.administration.Users._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_DEL"/>",
				getPluginResourcesUrl(org.ametys.administration.Users.pluginName) + '/img/users/delete.png', 
				org.ametys.administration.Users.remove);
	}	
	
	if (org.ametys.administration.Users._modifiable)
	{
		org.ametys.administration.Users._actions.hideElt(2);
		org.ametys.administration.Users._actions.hideElt(3);
		org.ametys.administration.Users._actions.hideElt(4);
	}
	else
	{
		org.ametys.administration.Users._actions.hide();
	}
	
	return org.ametys.administration.Users._actions;
}

/**
 * Draw the help panel.
 * @return {org.ametys.TextPanel} The help panel
 * @private
 */
org.ametys.administration.Users._drawHelpPanel = function ()
{
	var helpPanel = new org.ametys.TextPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_USERS_HELP"/>"});
	helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_USERS_HELP_TEXT_READ"/>");
	
	return helpPanel;
}

/**
 * Add an user
 */
org.ametys.administration.Users.impersonate = function ()
{
	var elt = org.ametys.administration.Users._listView.getSelection()[0];

	
	var serverMessage = new org.ametys.servercomm.ServerMessage("core", "administrator/users/impersonate", { login: elt.data.login }, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
	var result = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);

    if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_USERS_IMPERSONATE_ERROR"/>", (result == null || org.ametys.servercomm.ServerComm.handleResponse(result, "error") != "") ? null : result, "org.ametys.administration.Users.impersonate"))
    {
       return;
    }
    else
    {
    	var login = org.ametys.servercomm.ServerComm.handleResponse(result, "login");
    	var name = org.ametys.servercomm.ServerComm.handleResponse(result, "name");
		Ext.Msg.show ({
    		title: "<i18n:text i18n:key='PLUGINS_CORE_USERS_IMPERSONATE_SUCCESS_TITLE'/>",
    		msg: "<i18n:text i18n:key='PLUGINS_CORE_USERS_IMPERSONATE_SUCCESS'/> " + name + " (" + login + ")&lt;br/&gt;&lt;br/&gt;<i18n:text i18n:key='PLUGINS_CORE_USERS_IMPERSONATE_SUCCESS_2'/>",
    		buttons: Ext.Msg.OK,
			icon: Ext.MessageBox.INFO,
			fn: function() { window.open(context.contextPath + "/"); }
    	});
    }
}

/**
 * Add an user
 */
org.ametys.administration.Users.add = function ()
{
	RUNTIME_Plugin_Runtime_EditUser.act({"mode": "new"}, org.ametys.administration.Users._addCb);
} 

org.ametys.administration.Users._addCb = function (login, firstname, lastname, email)
{
	org.ametys.administration.Users._addElement (login, firstname, lastname, email);
}
/**
 * Edit an user
 */
org.ametys.administration.Users.edit = function ()
{
	var elt = org.ametys.administration.Users._listView.getSelection()[0];
    RUNTIME_Plugin_Runtime_EditUser.act({"mode": "edit", "login" : elt.data.login}, org.ametys.administration.Users._editCb);
} 

org.ametys.administration.Users._editCb = function (login, firstname, lastname, email)
{
	var elt = org.ametys.administration.Users._listView.getSelection()[0];          
	org.ametys.administration.Users._updateElement (elt, login, firstname, lastname, email);
}

/**
 * Delete an user
 */
org.ametys.administration.Users.remove = function ()
{
	Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_DEL_TITLE"/>", 
					 "<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_DEL_PROMPT"/>", 
					 org.ametys.administration.Users.doRemove);
} 

org.ametys.administration.Users.doRemove = function (answer)
{
	if (answer == 'yes')
	{
        var elt = org.ametys.administration.Users._listView.getSelection()[0];              
        var url = getPluginDirectUrl(org.ametys.administration.Users.pluginName) + "/users/delete";
        var args = "login=" + encodeURIComponent(elt.data.login);
        
        if (200 != org.ametys.servercomm.DirectComm.getInstance().sendSynchronousRequest(url, args).status)
        {
        	Ext.Msg.show ({
            		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
            		msg: "<i18n:text i18n:key="PLUGINS_CORE_USERS_DELETE_ERROR"/>",
            		buttons: Ext.Msg.OK,
   					icon: Ext.MessageBox.ERROR
            });
            return;
        }
        
        org.ametys.administration.Users._listView.removeElement(elt);    
    }    
}

/**
 * Search
 */
org.ametys.administration.Users.search = function ()
{
	// Effacer tout
	org.ametys.administration.Users._listView.getStore().removeAll();
	
	if (org.ametys.administration.Users._modifiable)
	{
		org.ametys.administration.Users._actions.hideElt(2);
		org.ametys.administration.Users._actions.hideElt(3);
		org.ametys.administration.Users._actions.hideElt(4);
	}
	else
	{
		org.ametys.administration.Users._actions.hide();
	}

	
	var searchField = org.ametys.administration.Users._searchForm.getForm().findField("searchField");
	var searchValue = searchField.getValue() == '<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCH_CRITERIA"/>' ? '' : searchField.getValue();
	
	var serverMessage = new org.ametys.servercomm.ServerMessage(org.ametys.administration.Users.pluginName, "users/search.xml", { criteria : searchValue }, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
	var result = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);

    if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCHING_ERROR"/>", result, "org.ametys.administration.Users.search"))
    {
       return;
    }

    // Afficher les resultats
    var nodes = result.selectNodes("Search/users/user");
    for (var i = 0; i &lt; nodes.length; i++)
    {
        var firstnameNode = nodes[i].selectSingleNode("firstname");
        var firstname = firstnameNode != null ? firstnameNode[org.ametys.servercomm.ServerComm.xmlTextContent] : "";
        
        var lastname = nodes[i].selectSingleNode("lastname")[org.ametys.servercomm.ServerComm.xmlTextContent];
        var login = nodes[i].getAttribute("login");
        var email = nodes[i].selectSingleNode("email")[org.ametys.servercomm.ServerComm.xmlTextContent];
        
        org.ametys.administration.Users._addElement(login, firstname, lastname, email);
    }
    
    if (nodes.length == 0)
    {
    	Ext.Msg.show ({
        		title: "<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCHING"/>",
        		msg: "<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCHING_NORESULT"/>",
        		buttons: Ext.Msg.OK,
					icon: Ext.MessageBox.INFO
        });
        return;
    }
}

/**
 * Back to the administration home page
 */
org.ametys.administration.Users.goBack = function ()
{
    document.location.href = context.workspaceContext;
}   

/**
 * @private
 */
org.ametys.administration.Users._addElement = function (login, firstname, lastname, email)
{
	org.ametys.administration.Users._listView.addElement(login, {
			'login': login,
			'display': firstname + " " + lastname + " (" + login + ")",
			'firstname': firstname,
			'lastname': lastname,
			'email': email 
		});	
}

/**
 * @private
 */
org.ametys.administration.Users._updateElement = function (element, login, firstname, lastname, email)
{
	element.set('firstname', firstname);
	element.set('lastname', lastname);
	element.set('email', email);
	element.set('display', firstname + ' ' + lastname + ' (' + login + ')');
}

org.ametys.administration.Users.addInputField = function (type, name, label, description)
{
	switch (type) {
		case 'double':
			return org.ametys.administration.Users._createDoubleField (name, label, description);
			break;
		case 'long':
			return org.ametys.administration.Users._createLongField (name, label, description);
			break;
		case 'password':
			return org.ametys.administration.Users._createPasswordField (name, label, description);
			break;
		case 'date':
			return org.ametys.administration.Users._createDateField (name, label, description);
			break;
		case 'boolean':
			return org.ametys.administration.Users._createBooleanField (name, label, description);
			break;
		default:
			return org.ametys.administration.Users._createTextField (name, label, description);
			break;
	}
}

org.ametys.administration.Users._createDoubleField = function (name, label, description)
{
	return new org.ametys.form.DoubleField ({
		name: name,
        fieldLabel: label,
        desc: description,
        
        width: 205
	});
}

org.ametys.administration.Users._createLongField = function (name, label, description)
{
	return new org.ametys.form.LongField ({
		name: name,
		fieldLabel: label,
        desc: description,
        
        width: 205
	});
}

org.ametys.administration.Users._createPasswordField = function (name, label, description)
{
	return new org.ametys.form.PasswordCreationField ({
		name: name,
		
	    fieldLabel: label,
	    desc: description,
	    
	    width: 205
	});
}

org.ametys.administration.Users._createDateField = function (name, label, description)
{
	return new org.ametys.form.DateField ({
		name: name,
		 
        fieldLabel: label,
        desc: description,
        
        width: 205
	});
}

org.ametys.administration.Users._createBooleanField = function (name, label, description)
{
	return new org.ametys.form.BooleanField ({
		name: name,
		 
        fieldLabel: label,
        desc: description,
        
        checked: (value == "true")
        
	});
}

org.ametys.administration.Users._createTextField = function (name, label, description)
{
	return new org.ametys.form.TextField ({
		name: name,
		
        fieldLabel: label,
        desc: description,
        
        width: 205
	});
}
