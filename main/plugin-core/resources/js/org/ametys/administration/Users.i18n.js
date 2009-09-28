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
	
	if (org.ametys.administration.Users._modifiable)
	{
		org.ametys.administration.Users._listView.addListener ('rowclick', org.ametys.administration.Users._selectUser, this);
		org.ametys.administration.Users._listView.addListener ('mouseup', org.ametys.administration.Users._unSelectUser, this);
	}
	
	
	org.ametys.administration.Users._contextualPanel = new org.ametys.HtmlContainer({
		region:'east',
	
		cls : 'admin-right-panel',
		border: false,
		width: 277,
	    
		items: [org.ametys.administration.Users._drawSearchPanel ()]
	});
	
	if (org.ametys.administration.Users._modifiable)
	{
		org.ametys.administration.Users._contextualPanel.add (org.ametys.administration.Users._drawActionsPanel ());
	}
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
	org.ametys.administration.Users._actions.showElt(2);
	org.ametys.administration.Users._actions.showElt(3);
}

org.ametys.administration.Users._unSelectUser = function  (e)
{
	org.ametys.administration.Users._actions.hideElt(2);
	org.ametys.administration.Users._actions.hideElt(3);
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
            value: 'Nom ou identifiant',
            
            onFocus : function () {this.setValue('');}
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
	if (!org.ametys.administration.Users._modifiable)
		return null;
	
	org.ametys.administration.Users._actions = new org.ametys.ActionsPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE"/>"});
	
	// Add user
	org.ametys.administration.Users._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_NEW"/>",
			getPluginResourcesUrl(org.ametys.administration.Users.pluginName) + '/img/users/add_user.png', 
			org.ametys.administration.Users.add);
	
	// Edit 
	org.ametys.administration.Users._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_EDIT"/>",
			getPluginResourcesUrl(org.ametys.administration.Users.pluginName) + '/img/users/edit_user.png', 
			org.ametys.administration.Users.edit);
	
	// Delete 
	org.ametys.administration.Users._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_DEL"/>",
			getPluginResourcesUrl(org.ametys.administration.Users.pluginName) + '/img/users/delete.png', 
			org.ametys.administration.Users.remove);
	
	
	org.ametys.administration.Users._actions.hideElt(2);
	org.ametys.administration.Users._actions.hideElt(3);
	
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
	Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_DELETE"/>", 
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
        
        if (200 != Tools.postUrlStatusCode(url, args))
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
	}
	
	var searchValue = org.ametys.administration.Users._searchForm.getForm().findField("searchField").getValue();
    
    var url = getPluginDirectUrl(org.ametys.administration.Users.pluginName) + "/users/search.xml";
    var arg = "criteria=" + encodeURIComponent(searchValue);
    
    var result = Tools.postFromUrl(url, arg);
    if (result == null)
    {
    	Ext.Msg.show ({
        		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
        		msg: "<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCHING_ERROR"/>",
        		buttons: Ext.Msg.OK,
					icon: Ext.MessageBox.ERROR
        });
        return;
    }

    // Afficher les resultats
    var nodes = result.selectNodes("/Search/users/user");
    for (var i = 0; i &lt; nodes.length; i++)
    {
        var firstnameNode = nodes[i].selectSingleNode("firstname");
        var firstname = firstnameNode != null ? firstnameNode[Tools.xmlTextContent] : "";
        
        var lastname = nodes[i].selectSingleNode("lastname")[Tools.xmlTextContent];
        var login = nodes[i].getAttribute("login");
        var email = nodes[i].selectSingleNode("email")[Tools.xmlTextContent];
        
        org.ametys.administration.Users._addElement(firstname, lastname, login, email);
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
        
        width: 250
	});
}

org.ametys.administration.Users._createLongField = function (name, label, description)
{
	return new org.ametys.form.LongField ({
		name: name,
		fieldLabel: label,
        desc: description,
        
        width: 250
	});
}

org.ametys.administration.Users._createPasswordField = function (name, label, description)
{
	return new org.ametys.form.PasswordWidget ({
		name: name,
		
	    fdLabel: label,
	    desc: description,
	    
	    fdLabelWidth :230
	});
}

org.ametys.administration.Users._createDateField = function (name, label, description)
{
	return new org.ametys.form.DateField ({
		name: name,
		 
        fieldLabel: label,
        desc: description,
        
        width: 250
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
        
        width: 250
	});
}
