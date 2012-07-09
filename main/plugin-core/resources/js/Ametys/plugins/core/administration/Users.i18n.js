/*
 *  Copyright 2012 Anyware Services
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

/**
 * Set the admin password
 */

Ext.define('Ametys.plugins.core.administration.Users', {
	singleton: true,
	
	/**
	 * @property {String} pluginName The plugin declaring this class
	 * @private
	 */
	/**
	 * @property {Boolean} _modifiable Is the current user manager modifiable ?
	 * @private
	 */
	/**
	 * @private
	 * @property {Ext.Container} _contextualPanel The right panel
	 */
	/**
	 * @private
	 * @property {Ext.grid.Panel} _listView The list of users
	 */
	/**
	 * @private
	 * @property {Ametys.workspace.admin.rightpanel.ActionPanel} _actions The action panel
	 */
	/**
	 * @private
	 * @property {Ametys.workspace.admin.rightpanel.ActionPanel} _search The search panel
	 */
	/**
	 * @private
	 * @property {Ametys.workspace.admin.rightpanel.ActionPanel} _searchForm The search panel
	 */

	/**
	 * Initialize
	 * @params {String} pluginName The plugin declaring this class
	 */
	initialize: function (pluginName)
	{
		this.pluginName = pluginName;
	},
	
	/**
	 * Creates the panel that rules the screen
	 * @returns {Ext.Panel} The screen
	 */
	createPanel: function ()
	{
		// The users
		Ext.define('Ametys.plugins.core.administration.Users.Users', {
		    extend: 'Ext.data.Model',
		    fields: [
			           {name: 'login'},
			           {name: 'display'},
			           {name: 'firstname'},
			           {name: 'lastname'},
			           {name: 'email'}
		    ]
		});
		
		this._listView = new Ext.grid.Panel({
			region: 'center',
			
			id: 'detailView',
			allowDeselect: true,
			
			height: '100%',
			
		    store : Ext.create('Ext.data.Store', {
				model: 'Ametys.plugins.core.administration.Users.Users',
		        data: { users: []},
		        
		        sortOnLoad: true,
		        sorters: [ { property: 'lang', direction: "ASC" } ],
		        
		        proxy: {
		        	type: 'memory',
		        	reader: {
		        		type: 'json',
		        		root: 'users'
		        	}
		        }
			}), 
		    	
		    columns: [
				        {header: "<i18n:text i18n:key="PLUGINS_CORE_USERS_COL_NAME"/>", width : 200, menuDisabled : true, sortable: true, dataIndex: 'display'},
				        {header: "<i18n:text i18n:key="PLUGINS_CORE_USERS_COL_EMAIL"/>", width : 240, menuDisabled : true, sortable: true, dataIndex: 'email'}
		    ],
			
		    listeners: {
		    	'select': Ext.bind(this._selectUser, this),
		    	'deselect': Ext.bind(this._unSelectUser, this)
		    }
		});	
		
		this._contextualPanel = new Ext.Container({
			region:'east',
		
			cls : 'admin-right-panel',
			border: false,
			width: 277,
		    
			items: [this._drawSearchPanel ()]
		});
		
		this._contextualPanel.add (this._drawActionsPanel ());
		this._contextualPanel.add (this._drawHelpPanel ());
		
		return new Ext.Panel({
			region: 'center',
			
			baseCls: 'transparent-panel',
			border: false,
			layout: 'border',
			
			items: [this._listView, 
			        this._contextualPanel]
		});
	},

	/**
	 * Listener when a user is selected
	 * @param  {Ext.selection.RowModel} me
	 * @param {Ext.data.Model} record
	 * @param {Number} index
	 * @param {Object} eOpts 
	 */
	_selectUser: function (me, record, index, eOpts)
	{
		if (this._modifiable)
		{
			this._actions.showElt(2);
			this._actions.showElt(3);
			this._actions.showElt(4);
		}
		else
		{
			this._actions.show();
		}
	},

	/**
	 * Listener when a user is deselected
	 * @param  {Ext.selection.RowModel} me
	 * @param {Ext.data.Model} record
	 * @param {Number} index
	 * @param {Object} eOpts 
	 */
	_unSelectUser: function (me, record, index, eOpts)
	{
		if (this._modifiable)
		{
			this._actions.hideElt(2);
			this._actions.hideElt(3);
			this._actions.hideElt(4);
		}
		else
		{
			this._actions.hide();
		}
	},

	/**
	 * Draw the search panel
	 * @return {Ametys.workspace.admin.rightpanel.ActionPanel} The search panel
	 * @private
	 */
	_drawSearchPanel: function ()
	{
		this._search = new Ametys.workspace.admin.rightpanel.ActionPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCH"/>"});
		
		this._searchForm = new Ext.form.Panel ({
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
				handler : Ext.bind(this.search, this)	            
	        }]
		});					        
		
		this._search.add(this._searchForm);
		
		// Quit
		this._search.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_QUIT"/>", 
					Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/users/quit.png', 
					Ext.bind(this.goBack, this));
		
		return this._search;
	},

	/**
	 * Draw the actions panel.
	 * @return {Ametys.workspace.admin.rightpanel.ActionPanel} The action panel
	 * @private
	 */
	_drawActionsPanel: function ()
	{
		this._actions = new Ametys.workspace.admin.rightpanel.ActionPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE"/>"});

		if (this._modifiable)
		{
			// Add user
			this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_NEW"/>",
					Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/users/add_user.png', 
					Ext.bind(this.add, this));
		}
		
		// Edit 
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_IMPERSONATE"/>",
					Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/users/impersonate_user.png', 
					Ext.bind(this.impersonate, this));

		if (this._modifiable)
		{
			// Edit 
			this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_EDIT"/>",
					Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/users/edit_user.png', 
					Ext.bind(this.edit, this));
		}
		
		if (this._modifiable)
		{
			// Delete 
			this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_DEL"/>",
					Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/users/delete.png', 
					Ext.bind(this.remove, this));
		}	
		
		if (this._modifiable)
		{
			this._actions.hideElt(2);
			this._actions.hideElt(3);
			this._actions.hideElt(4);
		}
		else
		{
			this._actions.hide();
		}
		
		return this._actions;
	},

	/**
	 * Draw the help panel.
	 * @return {Ametys.workspace.admin.rightpanel.TextPanel} The help panel
	 * @private
	 */
	_drawHelpPanel: function ()
	{
		var helpPanel = new Ametys.workspace.admin.rightpanel.TextPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_USERS_HELP"/>"});
		helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_USERS_HELP_TEXT_READ"/>");
		
		return helpPanel;
	},

	/**
	 * Impersone the current selected user
	 */
	impersonate: function ()
	{
		var elt = this._listView.getSelectionModel().getSelection()[0];

		
		var result = Ametys.data.ServerComm.send("core", "administrator/users/impersonate", { login: elt.data.login }, Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
	    if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_USERS_IMPERSONATE_ERROR"/>", (result == null || Ext.dom.Query.selectValue("*/errror", result) != "") ? null : result, "Ametys.plugins.core.administration.Users.impersonate"))
	    {
	       return;
	    }
	    else
	    {
	    	var login = Ext.dom.Query.selectValue("*/login", result);
	    	var name = Ext.dom.Query.selectValue("*/name", result);
			Ext.Msg.show ({
	    		title: "<i18n:text i18n:key='PLUGINS_CORE_USERS_IMPERSONATE_SUCCESS_TITLE'/>",
	    		msg: "<i18n:text i18n:key='PLUGINS_CORE_USERS_IMPERSONATE_SUCCESS'/> " + name + " (" + login + ")&lt;br/&gt;&lt;br/&gt;<i18n:text i18n:key='PLUGINS_CORE_USERS_IMPERSONATE_SUCCESS_2'/>",
	    		buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.INFO,
				fn: function() { window.open(Ametys.CONTEXT_PATH + "/"); }
	    	});
	    }
	},

	/**
	 * Add a new user
	 */
	add: function ()
	{
		RUNTIME_Plugin_Runtime_EditUser.act({"mode": "new"}, Ext.bind(this._addCb, this));
	},

	/**
	 * Callback for the user creation
	 * @param {String} login The new login
	 * @param {String} firstname The new firstname
	 * @param {String} lastname The new lastname
	 * @param {String} email The new email
	 * @private
	 */
	_addCb: function (login, firstname, lastname, email)
	{
		this._addElement (login, firstname, lastname, email);
	},
	
	/**
	 * Edit a user
	 */
	edit: function ()
	{
		var elt = this._listView.getSelectionModel().getSelection()[0];
	    RUNTIME_Plugin_Runtime_EditUser.act({"mode": "edit", "login" : elt.data.login}, Ext.bind(this._editCb, this));
	}, 

	/**
	 * Callback for the user edition
	 * @param {String} login The new login
	 * @param {String} firstname The new firstname
	 * @param {String} lastname The new lastname
	 * @param {String} email The new email
	 * @private
	 */
	_editCb: function (login, firstname, lastname, email)
	{
		var elt = this._listView.getSelectionModel().getSelection()[0];
		this._updateElement (elt, login, firstname, lastname, email);
	},

	/**
	 * Delete a user
	 */
	remove: function ()
	{
		Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_DEL_TITLE"/>", 
						 "<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_DEL_PROMPT"/>", 
						 Ext.bind(this.doRemove, true));
	},

	/**
	 * Callback for a user deletion
	 * @param {String} anwser Will effectively delete if the answer is 'yes'.
	 */
	doRemove: function (answer)
	{
		if (answer == 'yes')
		{
	        var elt = this._listView.getSelectionModel().getSelection()[0];              
	        var url = Ametys.getPluginResourcesPrefix(this.pluginName) + "/users/delete";
	        var args = "login=" + encodeURIComponent(elt.data.login);
	        
	        if (200 != Ext.Ajax.request({url: url, params: args, async: false}).status)
	        {
	        	Ext.Msg.show ({
	            		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
	            		msg: "<i18n:text i18n:key="PLUGINS_CORE_USERS_DELETE_ERROR"/>",
	            		buttons: Ext.Msg.OK,
	   					icon: Ext.MessageBox.ERROR
	            });
	            return;
	        }
	        
	        this._listView.getStore().remove(elt);
	    }    
	},

	/**
	 * Search
	 */
	search: function ()
	{
		// Effacer tout
		this._listView.getStore().removeAll();
		
		if (this._modifiable)
		{
			this._actions.hideElt(2);
			this._actions.hideElt(3);
			this._actions.hideElt(4);
		}
		else
		{
			this._actions.hide();
		}

		
		var searchField = this._searchForm.getForm().findField("searchField");
		var searchValue = searchField.getValue() == '<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCH_CRITERIA"/>' ? '' : searchField.getValue();
		
		var result = Ametys.data.ServerComm.send(this.pluginName, "users/search.xml", { criteria : searchValue }, Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);

	    if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCHING_ERROR"/>", result, "Ametys.plugins.core.administration.Users.search"))
	    {
	       return;
	    }

	    // Afficher les resultats
	    var nodes = Ext.dom.Query.select("Search/users/user", result);
	    for (var i = 0; i &lt; nodes.length; i++)
	    {
	        var firstname = Ext.dom.Query.selectValue("firstname", nodes[i], "");
	        
	        var lastname = Ext.dom.Query.selectValue("lastname", nodes[i]);
	        var login = nodes[i].getAttribute("login");
	        var email = Ext.dom.Query.selectValue("email", nodes[i]);
	        
	        this._addElement(login, firstname, lastname, email);
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
	},

	/**
	 * Back to the administration home page
	 */
	goBack: function ()
	{
	    document.location.href = Ametys.WORKSPACE_URI;
	},   

	/**
	 * Add an element to the grid
	 * @param {String} login The new login
	 * @param {String} firstname The new firstname
	 * @param {String} lastname The new lastname
	 * @param {String} email The new email
	 * @private
	 */
	_addElement: function (login, firstname, lastname, email)
	{
		this._listView.getStore().addSorted(Ext.create('Ametys.plugins.core.administration.Users.Users', {
			'login': login,
			'display': firstname + " " + lastname + " (" + login + ")",
			'firstname': firstname,
			'lastname': lastname,
			'email': email 
		}));
	},

	/**
	 * @private
	 * Update an existing element
	 * @param {Ext.data.Model} element The element to update
	 * @param {String} login The new login
	 * @param {String} firstname The new firstname
	 * @param {String} lastname The new lastname
	 * @param {String} email The new email
	 */
	_updateElement: function (element, login, firstname, lastname, email)
	{
		element.set('firstname', firstname);
		element.set('lastname', lastname);
		element.set('email', email);
		element.set('display', firstname + ' ' + lastname + ' (' + login + ')');
		element.commit();
	},

	/**
	 * Add an input field to the creation form
	 * @param {String} type Can be 'double', 'long', 'password', 'date', 'boolean', or 'text' (default value).
	 * @param {String} name The name of the field
	 * @param {String} label The label of the field
	 * @param {String} description The description tooltip for the field
	 * @return {Ext.form.field.Field} The newly created field
	 */
	addInputField: function (type, name, label, description)
	{
		switch (type) 
		{
			case 'double':
				return this._createDoubleField (name, label, description);
				break;
			case 'long':
				return this._createLongField (name, label, description);
				break;
			case 'password':
				return this._createPasswordField (name, label, description);
				break;
			case 'date':
				return this._createDateField (name, label, description);
				break;
			case 'boolean':
				return this._createBooleanField (name, label, description);
				break;
			default:
				return this._createTextField (name, label, description);
				break;
		}
	},

	/**
	 * @private
	 * Create a field of type 'double'
	 * @param {String} name The name of the field
	 * @param {String} label The label of the field
	 * @param {String} description The description tooltip for the field
	 * @return {Ext.form.field.Field} The newly created field
	 */
	_createDoubleField: function (name, label, description)
	{
		return new Ext.form.field.Double ({
			name: name,
	        fieldLabel: label,
	        ametysDescription: description,
	        
	        width: 205
		});
	},

	/**
	 * @private
	 * Create a field of type 'long'
	 * @param {String} name The name of the field
	 * @param {String} label The label of the field
	 * @param {String} description The description tooltip for the field
	 * @return {Ext.form.field.Field} The newly created field
	 */
	_createLongField: function (name, label, description)
	{
		return new Ext.form.field.Long ({
			name: name,
			fieldLabel: label,
			ametysDescription: description,
	        
	        width: 205
		});
	},

	/**
	 * @private
	 * Create a field of type 'password'
	 * @param {String} name The name of the field
	 * @param {String} label The label of the field
	 * @param {String} description The description tooltip for the field
	 * @return {Ext.form.field.Field} The newly created field
	 */
	_createPasswordField: function (name, label, description)
	{
		return new Ametys.form.field.ChangePassword ({
			name: name,
			
		    fieldLabel: label,
		    ametysDescription: description,
		    
		    width: 205
		});
	},

	/**
	 * @private
	 * Create a field of type 'date'
	 * @param {String} name The name of the field
	 * @param {String} label The label of the field
	 * @param {String} description The description tooltip for the field
	 * @return {Ext.form.field.Field} The newly created field
	 */
	_createDateField: function (name, label, description)
	{
		return new  Ext.form.field.Date ({
			name: name,
			 
	        fieldLabel: label,
	        ametysDescription: description,
	        
	        width: 205
		});
	},

	/**
	 * @private
	 * Create a field of type 'boolean'
	 * @param {String} name The name of the field
	 * @param {String} label The label of the field
	 * @param {String} description The description tooltip for the field
	 * @return {Ext.form.field.Field} The newly created field
	 */
	_createBooleanField: function (name, label, description)
	{
		return new  Ext.form.field.Boolean ({
			name: name,
			 
	        fieldLabel: label,
	        ametysDescription: description,
	        
	        checked: (value == "true")
	        
		});
	},

	/**
	 * @private
	 * Create a field of type 'text'
	 * @param {String} name The name of the field
	 * @param {String} label The label of the field
	 * @param {String} description The description tooltip for the field
	 * @return {Ext.form.field.Field} The newly created field
	 */
	_createTextField: function (name, label, description)
	{
		return new  Ext.form.field.Text ({
			name: name,
			
	        fieldLabel: label,
	        ametysDescription: description,
	        
	        width: 205
		});
	}
});
