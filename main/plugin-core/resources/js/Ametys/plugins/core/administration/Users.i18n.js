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
	 * @property {Ametys.admin.rightpanel.ActionPanel} _actions The action panel
	 */
	/**
	 * @private
	 * @property {Ametys.admin.rightpanel.ActionPanel} _search The search panel
	 */
	/**
	 * Initialize
	 * @param {String} pluginName The plugin declaring this class
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
		        sorters: [ { property: 'display', direction: "ASC" } ],
		        
		        proxy: {
		        	type: 'memory',
		        	reader: {
		        		type: 'json',
		        		root: 'users'
		        	}
		        }
			}), 
		    	
		    columns: [
				        {header: "<i18n:text i18n:key='PLUGINS_CORE_USERS_COL_NAME'/>", flex: 0.4, menuDisabled : true, sortable: true, dataIndex: 'display'},
				        {header: "<i18n:text i18n:key='PLUGINS_CORE_USERS_COL_EMAIL'/>", flex: 0.6, menuDisabled : true, sortable: true, dataIndex: 'email'}
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
		    
			items: [this._drawSearchPanel()]
		});
		
		this._contextualPanel.add (this._drawActionsPanel ());
		this._contextualPanel.add (this._drawHelpPanel ());
		
		return new Ext.Panel({
			region: 'center',
			
			cls: 'transparent-panel',
			border: false,
			layout: 'border',
			
			items: [this._listView, 
			        this._contextualPanel]
		});
	},

	/**
	 * Listener when a user is selected
	 * @param {Ext.selection.RowModel} sm The row selection model
	 * @param {Ext.data.Model} record The selected record
	 * @param {Number} index The index of selected record
	 * @param {Object} eOpts The event options
	 * @private
	 */
	_selectUser: function (sm, record, index, eOpts)
	{
		if (this._modifiable)
		{
			this._actions.showElt(1);
			this._actions.showElt(2);
			this._actions.showElt(3);
		}
		else
		{
			this._actions.show();
		}
	},

	/**
	 * Listener when a user is unselected
	 * @param {Ext.selection.RowModel} sm The row selection model
	 * @param {Ext.data.Model} record The unselected record
	 * @param {Number} index The index of unselected record
	 * @param {Object} eOpts The event options
	 * @private
	 */
	_unSelectUser: function (sm, record, index, eOpts)
	{
		if (this._modifiable)
		{
			this._actions.hideElt(1);
			this._actions.hideElt(2);
			this._actions.hideElt(3);
		}
		else
		{
			this._actions.hide();
		}
	},

	/**
	 * Draw the search panel
	 * @return {Ametys.admin.rightpanel.ActionPanel} The search panel
	 * @private
	 */
	_drawSearchPanel: function ()
	{
		this._search = new Ametys.admin.rightpanel.ActionPanel({title: "<i18n:text i18n:key='PLUGINS_CORE_USERS_SEARCH'/>"});
		
		var ct = new Ext.Container({
			layout: 'column',
			items: [
				new Ext.form.field.Text({
					hideLabel: true,
				    id: 'searchField',
				    
				    style: {
				    	marginLeft: '10px',
				    },
				    
				    width: 182,
				    
				    value: '<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCH_CRITERIA"/>',
				    onFocus : function () {if (this.getValue() == '<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCH_CRITERIA"/>') this.setValue(''); },
				    onBlur : function () {if (this.getValue() == '') this.setValue('<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCH_CRITERIA"/>'); }
				}),

				new Ext.button.Button ({
		            icon: Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/users/search_16.png',
		            tooltip: "<i18n:text i18n:key='PLUGINS_CORE_USERS_SEARCH_BUTTON'/>",
					handler : Ext.bind(this.search, this)	            
				})
			]
		});
		
		this._search.add(ct);

		// Quit
		this._search.addAction("<i18n:text i18n:key='PLUGINS_CORE_USERS_HANDLE_QUIT'/>", 
					null,
					Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/users/quit.png', 
					Ext.bind(this.goBack, this));
		
		return this._search;
	},

	/**
	 * Draw the actions panel.
	 * @return {Ametys.admin.rightpanel.ActionPanel} The action panel
	 * @private
	 */
	_drawActionsPanel: function ()
	{
		this._actions = new Ametys.admin.rightpanel.ActionPanel({title: "<i18n:text i18n:key='PLUGINS_CORE_USERS_HANDLE'/>"});

		if (this._modifiable)
		{
			// Add user
			this._actions.addAction("<i18n:text i18n:key='PLUGINS_CORE_USERS_HANDLE_NEW'/>",
					null,
					Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/users/add_user.png', 
					Ext.bind(this.add, this));
		}
		
		// Edit 
		this._actions.addAction("<i18n:text i18n:key='PLUGINS_CORE_USERS_IMPERSONATE'/>",
					null,
					Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/users/impersonate_user.png', 
					Ext.bind(this.impersonate, this));

		if (this._modifiable)
		{
			// Edit 
			this._actions.addAction("<i18n:text i18n:key='PLUGINS_CORE_USERS_HANDLE_EDIT'/>",
					null,
					Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/users/edit_user.png', 
					Ext.bind(this.edit, this));
		}
		
		if (this._modifiable)
		{
			// Delete 
			this._actions.addAction("<i18n:text i18n:key='PLUGINS_CORE_USERS_HANDLE_DEL'/>",
					null,
					Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/users/delete.png', 
					Ext.bind(this.remove, this));
		}	
		
		if (this._modifiable)
		{
			this._actions.hideElt(1);
			this._actions.hideElt(2);
			this._actions.hideElt(3);
		}
		else
		{
			this._actions.hide();
		}
		
		return this._actions;
	},

	/**
	 * Draw the help panel.
	 * @return {Ametys.admin.rightpanel.TextPanel} The help panel
	 * @private
	 */
	_drawHelpPanel: function ()
	{
		var helpPanel = new Ametys.admin.rightpanel.TextPanel({title: "<i18n:text i18n:key='PLUGINS_CORE_USERS_HELP'/>"});
		helpPanel.addText("<i18n:text i18n:key='PLUGINS_CORE_USERS_HELP_TEXT_READ'/>");
		
		return helpPanel;
	},

	/**
	 * Impersone the current selected user
	 */
	impersonate: function ()
	{
		var elt = this._listView.getSelectionModel().getSelection()[0];

		var result = Ametys.data.ServerComm.send({
			plugin: "core", 
			url: "administrator/users/impersonate", 
			parameters: { login: elt.data.login }, 
			priority: Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, 
			callback: null, 
			responseType: null
		});
	    if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key='PLUGINS_CORE_USERS_IMPERSONATE_ERROR'/>", (result == null || Ext.dom.Query.selectValue("*/error", result, "") != "") ? null : result, "Ametys.plugins.core.administration.Users.impersonate"))
	    {
	       return;
	    }
	    else
	    {
	    	var login = Ext.dom.Query.selectValue("*/login", result);
	    	var name = Ext.dom.Query.selectValue("*/name", result);
			Ext.Msg.show ({
	    		title: "<i18n:text i18n:key='PLUGINS_CORE_USERS_IMPERSONATE_SUCCESS_TITLE'/>",
	    		msg: "<i18n:text i18n:key='PLUGINS_CORE_USERS_IMPERSONATE_SUCCESS'/> " + name + " (" + login + ")<br/><br/><i18n:text i18n:key='PLUGINS_CORE_USERS_IMPERSONATE_SUCCESS_2'/>",
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
		Ametys.plugins.core.administration.UserEdit.act({"mode": "new"}, Ext.bind(this._addCb, this));
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
	    Ametys.plugins.core.administration.UserEdit.act({"mode": "edit", "login" : elt.data.login}, Ext.bind(this._editCb, this));
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
		Ext.Msg.confirm ("<i18n:text i18n:key='PLUGINS_CORE_USERS_HANDLE_DEL_TITLE'/>", 
						 "<i18n:text i18n:key='PLUGINS_CORE_USERS_HANDLE_DEL_PROMPT'/>", 
						 Ext.bind(this.doRemove, this));
	},

	/**
	 * Callback for a user deletion
	 * @param {String} answer Will effectively delete if the answer is 'yes'.
	 */
	doRemove: function (answer)
	{
		if (answer == 'yes')
		{
	        var elt = this._listView.getSelectionModel().getSelection()[0];              
	        var url = Ametys.getPluginDirectPrefix(this.pluginName) + "/administrator//users/delete";
	        var args = "login=" + encodeURIComponent(elt.data.login);
	        
	        if (200 != Ext.Ajax.request({url: url, params: args, async: false}).status)
	        {
	        	Ext.Msg.show ({
	            		title: "<i18n:text i18n:key='PLUGINS_CORE_ERROR_DIALOG_TITLE'/>",
	            		msg: "<i18n:text i18n:key='PLUGINS_CORE_USERS_DELETE_ERROR'/>",
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
			this._actions.hideElt(1);
			this._actions.hideElt(2);
			this._actions.hideElt(3);
		}
		else
		{
			this._actions.hide();
		}

		
		var searchField = Ext.getCmp("searchField");
		var searchValue = searchField.getValue() == '<i18n:text i18n:key="PLUGINS_CORE_USERS_SEARCH_CRITERIA"/>' ? '' : searchField.getValue();
		
		var result = Ametys.data.ServerComm.send({
			plugin: this.pluginName, 
			url: "users/search.xml", 
			parameters: { criteria : searchValue }, 
			priority: Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, 
			callback: null, 
			responseType: null
		});

	    if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key='PLUGINS_CORE_USERS_SEARCHING_ERROR'/>", result, "Ametys.plugins.core.administration.Users.search"))
	    {
	       return;
	    }

	    // Afficher les resultats
	    var nodes = Ext.dom.Query.select("Search/users/user", result);
	    for (var i = 0; i < nodes.length; i++)
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
	        		title: "<i18n:text i18n:key='PLUGINS_CORE_USERS_SEARCHING'/>",
	        		msg: "<i18n:text i18n:key='PLUGINS_CORE_USERS_SEARCHING_NORESULT'/>",
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
	}
});
