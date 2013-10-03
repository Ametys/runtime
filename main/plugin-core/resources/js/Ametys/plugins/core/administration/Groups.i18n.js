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

Ext.define('Ametys.plugins.core.administration.Groups', {
	singleton: true,
	
	/**
	 * @property {Boolean} _modifiable Is the current group manager modifiable?
	 * @private
	 */
	/**
	 * @property {Boolean} _hasChanges Has pending changes?
	 * @private
	 */
	/**
	 * @property {String} _currentGroup If there are pending changes, this is the id of the concerned group
	 * @private
	 */
	/**
	 * @private
	 * @property {Ametys.workspace.admin.rightpanel.ActionPanel} _usersActions The action panel for users actions
	 */
	/**
	 * @private
	 * @property {Ametys.workspace.admin.rightpanel.ActionPanel} _groupsActions The action panel for groups actions
	 */
	/**
	 * @private
	 * @property {Ext.Container} _contextualPanel The right panel
	 */
	/**
	 * @private
	 * @property {Ext.panel.Panel} _groupsPanel The center panel (with both list views)
	 */
	/**
	 * @private
	 * @property {Ext.grid.Panel} _listViewGp The list view for groups
	 */
	/**
	 * @private
	 * @property {Ext.grid.Panel} _listViewU The list view for users
	 */
	
	/**
	 * Initialize the elements
	 * @param {String} pluginName The name of the plugin to use for ajax request...
	 */
	initialize: function (pluginName)
	{
		this.pluginName = pluginName;
		window.onbeforeunload = Ext.bind(this.checkBeforeQuit, this);
	},

	/**
	 * Create the main screen
	 * @returns {Ext.panel.Panel} The created panel
	 */
	createPanel: function ()
	{
		Ext.define('Ametys.plugins.core.administration.Groups.Group', {
		    extend: 'Ext.data.Model',
		    fields: [
			           {name: 'id'},
			           {name: 'name', sortType: Ext.data.SortTypes.asUCString}
		    ]
		});
		Ext.define('Ametys.plugins.core.administration.Groups.Users', {
		    extend: 'Ext.data.Model',
		    fields: [
			           {name: 'user', sortType: Ext.data.SortTypes.asUCString},
			           {name: 'type'}
		    ]
		});
		
		this._listViewGp = new Ext.grid.Panel({
			region: 'north',
			
			id: 'list-view-groups',
			
			baseCls2: 'group-list',
			height: 200,
			minSize: 100,
			maxSize: 310,
			
			title : "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_LABEL'/>",
			hideHeaders: true,
			border: false,
			autoScroll: true,
			
		    store: Ext.create('Ext.data.Store', {
				model: 'Ametys.plugins.core.administration.Groups.Group',
		        data: { groups: []},
		        
				allowDeselect: true,
		        sortOnLoad: true,
		        sorters: [ { property: 'name', direction: "ASC" } ],
		        
		    	proxy: {
		        	type: 'memory',
		        	reader: {
		        		type: 'json',
		        		root: 'groups'
		        	}
		        }
			}),  
			
			selType: 'cellmodel',
			plugins: [
			          Ext.create('Ext.grid.plugin.CellEditing', {  })
			],
			
		    columns: [
				        {dataIndex: 'name', flex: 1, width: 1, editor: { xtype: 'textfield', allowBlank: false }}
		    ],
			
		    listeners: {
		    	'select': Ext.bind(this._selectGroup, this),
		    	'deselect': Ext.bind(this._unselectGroup, this),
		    	'edit': Ext.bind(this._editGroupLabel, this),
		    	'validateedit':  Ext.bind(this._validateEdit, this)
		    }
		});	
			
			
		this._listViewU = new Ext.grid.Panel({
			region: 'center',
			
			id: 'list-view-user',
			allowDeselect: true,
			
			baseCls3: 'user-list',
			border: false,
			autoScroll: true,
			
			title : "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_COLUMN'/>",
			hideHeaders: true,
			multiSelect: true,
			
			store : Ext.create('Ext.data.Store', {
				model: 'Ametys.plugins.core.administration.Groups.Users',
		        data: { users: []},
		        
		        sortOnLoad: true,
		        sorters: [ { property: 'user', direction: "ASC" } ],
		        
				hideHeaders: true,

				proxy: {
		        	type: 'memory',
		        	reader: {
		        		type: 'json',
		        		root: 'users'
		        	}
		        }
			}), 
		    	
		    columns: [
				        {dataIndex: 'user', flex: 1, width: 1}
		    ]
		});	
		
		this._groupsPanel = new Ext.Panel({
			region: 'center',
			
			defaults: {
			    split: true
			},
			layout: 'border',
			
			cls: 'transparent-panel',
			border: false,
			autoScroll: false,
			margins: '0 20 0 0',
			
			items: [this._listViewGp, 
			        this._listViewU]
		});

		
		this._contextualPanel = new Ext.Container({
			region:'east',
		
			cls : 'admin-right-panel',
			border: false,
			width: 277,
		    
			html : ''
		});
		
		this._contextualPanel.add (this._drawGroupsActionsPanel ());
		if (this._modifiable)
		{
			this._contextualPanel.add (this._drawUsersActionsPanel ());
		}
		this._contextualPanel.add (this._drawHelpPanel ());
		
		return new Ext.Panel({
			region: 'center',
			
			cls: 'transparent-panel',
			border: false,
			layout: 'border',
			
			autoScroll: false,
			items: [this._groupsPanel, 
			        this._contextualPanel]
		});
	},

	/**
	 * This function is call when a group is unselected
	 * @private
	 * @param  {Ext.selection.RowModel} me The row selection model 
	 * @param {Ext.data.Model} record The record selected
	 * @param {Number} index The row index
	 * @param {Object} eOpts Events options
	 */
	_unselectGroup: function (me, record, index, eOpts)
	{
    	if (this._modifiable)
    	{
    		this._usersActions.setVisible(false);
    		this._groupsActions.showElt(0);
    		this._groupsActions.hideElt(1);
    		this._groupsActions.hideElt(2);
    	}
	},
	
	/**
	 * This function is call when a group is selected
	 * @private
	 * @param  {Ext.selection.RowModel} me The row selection model 
	 * @param {Ext.data.Model} record The record selected
	 * @param {Number} index The row index
	 * @param {Object} eOpts Events options
	 */
	_selectGroup: function (me, record, index, eOpts)
	{
		if (this._currentGroup != null && this._hasChanges)
		{
			var objects = "";
			var users = this._listViewU.getStore().data.items;
			for (var i=0; i < users.length; i++)
			{
				var objectId = users[i].get('id');
				objects += objectId + '/';
			}
			
			Ext.Msg.confirm ("<i18n:text i18n:key='PLUGINS_CORE_SAVE_DIALOG_TITLE'/>", 
							 "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_MODIFY_CONFIRM'/>", 
							 Ext.bind(this._saveConfirm, this, [this._currentGroup, objects], true)
			);
		}
		
		if (this._modifiable)
		{
			this._groupsActions.showElt(1);
			this._groupsActions.showElt(2);
			
			this._usersActions.setVisible(true);
			this._usersActions.showElt(0);
			this._usersActions.showElt(1);
			this._usersActions.hideElt(2);
		}
		
		this._listViewU.getStore().removeAll();
		
		this._currentGroup = record; 

		var result = Ametys.data.ServerComm.send({
			plugin: this.pluginName, 
			url: "/administrator/groups/members", 
			parameters: { groupID: this._currentGroup.get('id') }, 
			priority: Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, 
			callback: null, 
			responseType: null
		});
	    if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key='PLUGINS_CORE_GROUPS_LIST_ERROR'/>", result, "Ametys.plugins.core.administration.Groups._selectGroup"))
	    {
	       throw "this._selectGroup request failed";
	    }
	   
		var members = Ext.dom.Query.select("GroupMembers/User", result);
		for (var i=0; i < members.length; i++)
		{
			var fullname = Ext.dom.Query.selectValue("FullName", members[i]);
			var login =  members[i].getAttribute("login");

			var newEntry = Ext.create('Ametys.plugins.core.administration.Groups.Users', {
				'user': fullname + " (" + login + ")",
				'id': login
			}); 
			this._listViewU.getStore().addSorted(newEntry);
		}
	},

	/**
	 * @private
	 * Listener on save button
	 * @param {String} button If 'yes' will save effectively by calling {@link #save}
	 * @param {String} text undefined
	 * @param {Object} opt Options given as arfs
	 * @param {Ext.data.Model} group The group optionnaly for save
	 * @param {String} objects The objects optionnaly for save
	 */
	_saveConfirm: function (button, text, opt, group, objects)
	{
		if (button == 'yes')
		{
			this.save(group, objects);
		}
		else
		{
			this._hasChanges = false;
		}
	},

	/**
	 * Listener to accept edition
	 * @private
	 */
	_validateEdit: function ()
	{
		return true;
	},

	/**
	 * Create or rename a group
     * - grid - The grid
     * - record - The record that was edited
     * - field - The field name that was edited
     * - value - The value being set
     * - originalValue - The original value for the field, before the edit.
     * - row - The grid table row
     * - column - The grid {@link Ext.grid.column.Column Column} defining the column that was edited.
     * - rowIdx - The row index that was edited
     * - colIdx - The column index that was edited
     * @param {Ext.grid.plugin.CellEditing} editor The editor plugin
     * @param {Object} e The editing context with the following properties:
     *  @param {Ext.grid.Panel}         e.grid The owning grid Panel.
     *  @param {Ext.data.Model}         e.record The record being edited.
     *  @param {String}                 e.field The name of the field being edited.
     *  @param {Mixed}                  e.value The field's current value.
     *  @param {HTMLElement}            e.row The grid row element.
     *  @param {Ext.grid.column.Column} e.column The Column being edited.
     *  @param {Number}                 e.rowIdx The index of the row being edited.
     *  @param {Number}                 e.colIdx The index of the column being edited.
     * @param {Object} eOpts The options object passed to Ext.util.Observable.addListener.
	 * @private
	 */
	_editGroupLabel: function (editor, e, eOpts)
	{
		// Rename
		var result = Ametys.data.ServerComm.send({
			plugin: this.pluginName, 
			url: "/administrator/groups/rename", 
			parameters: { id: e.record.data.id, name: e.record.get('name') }, 
			priority: Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, 
			callback: null, 
			responseType: null
		});
	    if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key='PLUGINS_CORE_GROUPS_RENAME_ERROR'/>", result, "Ametys.plugins.core.administration.Groups._editGroupLabel"))
	    {
	       // nothing
	    }
		else
		{
			var state = Ext.dom.Query.selectValue("*/message", result); 
			if (state != null && state == "missing")
			{
				Ext.Msg.show ({
            		title: "<i18n:text i18n:key='PLUGINS_CORE_ERROR_DIALOG_TITLE'/>",
            		msg: "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_RENAME_MISSING_ERROR'/>",
            		buttons: Ext.Msg.OK,
   					icon: Ext.MessageBox.ERROR
            	});
				this._listViewGp.getStore().remove(e.record);
			}
		}
		e.record.commit();
		// Sort
		this._listViewGp.getStore().sort('name', 'ASC');
	},

	/**
	 * Back to the administration home page
	 */
	goBack: function ()
	{
		document.location.href = Ametys.WORKSPACE_URI;
	},   

	/**
	 * Add a user
	 */
	addUser: function () 
	{
		function cb (users)
		{
			var selectedElements = new Array();
			
			for (var i in users)
			{
				var e = this._listViewU.getStore().findExact('id', i);
				if (e == -1)
				{
					e = Ext.create('Ametys.plugins.core.administration.Groups.Users', {
						'user': users[i],
						'id': i
					}); 
					this._listViewU.getStore().addSorted(e);
				}
				selectedElements.push(e);
			}
			this._needSave();
		}

		Ametys.runtime.uihelper.SelectUser.act({
			callback: Ext.bind(cb, this), 
			cancelCallback: null, 
			usersManagerRole: null, 
			allowMultiselection: true, 
			plugin: this.pluginName
		});							
	},

	/**
	 * Delete selected users
	 */
	deleteUsers: function () 
	{
	    var elts = this._listViewU.getSelectionModel().getSelection();
		for (var i = 0; i < elts.length; i++)
		{
			this._listViewU.getStore().remove(elts[i]);
		}
		this._needSave();    
	},

	/**
	 * Save modifications
	 * @param {Ext.data.Model} group The record to save. Can be null to save the current record
	 * @param {String} objects The users id '/' separated. Optionnal
	 */
	save: function (group, objects)
	{
		group = group || this._currentGroup || this._listViewGp.getSelection()[0]; 
		
		if (objects == null)
		{
			objects = "";
			var users = this._listViewU.getStore().data.items;
			for (var i=0; i < users.length; i++)
			{
				var objectId = users[i].get('id');
				objects += objectId + '/';
			}
		}
		
		// Send
		var result = Ametys.data.ServerComm.send({
			plugin: this.pluginName, 
			url: "/administrator/groups/modify", 
			parameters: { id: group.get('id'), objects: objects }, 
			priority: Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, 
			callback: null, 
			responseType: null
		});
	    if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key='PLUGINS_CORE_GROUPS_MODIFY_ERROR'/>", result, "Ametys.plugins.core.administration.Groups.save"))
	    {
	        // Just display the message.
	    }
		else
		{
			var state = Ext.dom.Query.selectValue("*/message", result); 
			if (state != null && state == "missing")
			{
				Ext.Msg.show ({
	            		title: "<i18n:text i18n:key='PLUGINS_CORE_ERROR_DIALOG_TITLE'/>",
	            		msg: "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_MODIFY_MISSING_ERROR'/>",
	            		buttons: Ext.Msg.OK,
	   					icon: Ext.MessageBox.ERROR
	            });
				this._listViewGp.getStore().remove(group);
			}
			else
			{
				this._usersActions.hideElt(2);
			}
		}
		
		this._hasChanges = false;
	},

	/**
	 * @private
	 * Handle to mark the form as dirty
	 */
	_needSave: function (field, newValue, oldValue)
	{
		this._usersActions.showElt(2);
		this._hasChanges = true;
	},

	/**
	 * Draw the actions panel for groups.
	 * @return {Ametys.workspace.admin.rightpanel.ActionPanel} The action panel
	 * @private
	 */
	_drawGroupsActionsPanel: function ()
	{
		this._groupsActions = new Ametys.workspace.admin.rightpanel.ActionPanel({title: "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_HANDLE_CATEGORY'/>"});

		if (this._modifiable)
		{
			// New group
			this._groupsActions.addAction("<i18n:text i18n:key='PLUGINS_CORE_GROUPS_HANDLE_CREATE'/>",
					 null,
					 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/groups/add_group.png', 
					 Ext.bind(this.add, this));
			
			// Rename goup
			this._groupsActions.addAction("<i18n:text i18n:key='PLUGINS_CORE_GROUPS_HANDLE_RENAME'/>",
					 null,
					 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/groups/rename.png', 
					 Ext.bind(this.rename, this));
			
			// Delete goup
			this._groupsActions.addAction("<i18n:text i18n:key='PLUGINS_CORE_GROUPS_HANDLE_DELETE'/>",
					 null,
					 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/groups/delete.png', 
					 Ext.bind(this.remove, this));
		}
		
		// Quit
		this._groupsActions.addAction("<i18n:text i18n:key='PLUGINS_CORE_GROUPS_HANDLE_QUIT'/>",
				 null,
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/groups/quit.png', 
				 Ext.bind(this.goBack, this));
		
		if (this._modifiable)
		{
			this._groupsActions.showElt(0);
			this._groupsActions.hideElt(1);
			this._groupsActions.hideElt(2);
		}
		
		return this._groupsActions;
	},

	/**
	 * Draw the actions panel.
	 * @return {Ametys.workspace.admin.rightpanel.ActionPanel} The action panel
	 * @private
	 */
	_drawUsersActionsPanel: function ()
	{
		if (!this._modifiable)
			return null;
		
		this._usersActions = new Ametys.workspace.admin.rightpanel.ActionPanel({title: "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_USERS_HANDLE_CATEGORY'/>"});

		// Add user
		this._usersActions.addAction("<i18n:text i18n:key='PLUGINS_CORE_GROUPS_HANDLE_ADDUSER'/>",
				 null,
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/groups/add_user.png', 
				 Ext.bind(this.addUser, this));
		
		// Delete user
		this._usersActions.addAction("<i18n:text i18n:key='PLUGINS_CORE_GROUPS_HANDLE_DELETEUSER'/>", 
				 null,
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/groups/delete.png', 
				 Ext.bind(this.deleteUsers, this));
		
		// Validate modification
		this._usersActions.addAction("<i18n:text i18n:key='PLUGINS_CORE_GROUPS_HANDLE_VALIDATE'/>", 
				 null,
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/groups/validate.png', 
				 Ext.bind(this.save, this, []));
		
		this._usersActions.setVisible(false);
		
		return this._usersActions;
	},


	/**
	 * Draw the help panel.
	 * @return {Ametys.workspace.admin.rightpanel.TextPanel} The help panel
	 * @private
	 */
	_drawHelpPanel: function ()
	{
		var helpPanel = new Ametys.workspace.admin.rightpanel.TextPanel({title: "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_HELP_CATEGORY'/>"});
		helpPanel.addText("<i18n:text i18n:key='PLUGINS_CORE_GROUPS_HANDLE_HELP_HINT'/>");
		
		return helpPanel;
	},

	/**
	 * Add a new group
	 */
	add: function ()
	{
		// Create group
		var result = Ametys.data.ServerComm.send({
			pluginOrWorkspace: this.pluginName, 
			url: "/administrator/groups/create", 
			parameters: { name: "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_NEWGROUP'/>" }, 
			priority: Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, 
			callback: null, 
			responseType: null
		});
		
	    if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key='PLUGINS_CORE_GROUPS_NEW_ERROR'/>", result, "Ametys.plugins.core.administration.Groups.add"))
	    {
	       return false;
	    }
	    
	    var id = Ext.dom.Query.selectValue("*/id", result);
	    
		var newEntry = Ext.create('Ametys.plugins.core.administration.Groups.Group', {
			'name': "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_NEWGROUP'/>",
			'id': id
		}); 

		
		this._listViewU.getStore().removeAll();
		this._listViewGp.getStore().addSorted(newEntry);
		this._listViewGp.getSelectionModel().setCurrentPosition({row: this._listViewGp.getStore().indexOf(newEntry), column: 0});
		
		this.rename ();
	},

	/**
	 * @private
	 * Handler of the rename action
	 */
	rename: function ()
	{
		var record = this._listViewGp.getSelectionModel().getSelection()[0];
		this._listViewGp.plugins[0].startEdit(record, 0);
	},

	/**
	 * @private
	 * Handler of the remove button
	 */
	remove: function () 
	{
		Ext.Msg.confirm ("<i18n:text i18n:key='PLUGINS_CORE_GROUPS_HANDLE_DELETE'/>", 
						 "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_DELETE_CONFIRM'/>", 
						 Ext.bind(this.doRemove, this));
	},
	
	/**
	 * Effectively remove the current group selection
	 * @param {String} answer If 'yes' the group will be removed
	 * @private
	 */
	doRemove: function (answer)
	{
		if (answer == 'yes')
		{
			var elt = this._listViewGp.getSelectionModel().getSelection()[0];
			this._hasChanges = false;
			
	    	if (200 == Ext.Ajax.request({url: Ametys.getPluginDirectPrefix(this.pluginName) + "/administrator/groups/delete", params: "id=" + elt.get('id'), async: false}).status)
			{
	    		this._listViewGp.getStore().remove(elt);
	    		this._unselectGroup();
			}
			else
			{
				Ext.Msg.show ({
	        		title: "<i18n:text i18n:key='PLUGINS_CORE_ERROR_DIALOG_TITLE'/>",
	        		msg: "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_DELETE_ERROR'/>",
	        		buttons: Ext.Msg.OK,
					icon: Ext.MessageBox.ERROR
	        	});
			}
	    }
	},

	/**
	 * This is a handler when quitting the screen, to propose to save
	 * @private
	 */
	checkBeforeQuit: function()
    {
        if (this._currentGroup != null && this._hasChanges)
        {
            if (confirm("<i18n:text i18n:key='PLUGINS_CORE_GROUPS_MODIFY_CONFIRM'/>"))
            {
                this.save();
            }
        }
    }	
});
