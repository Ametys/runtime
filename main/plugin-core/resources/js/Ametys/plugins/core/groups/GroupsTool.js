/*
 *  Copyright 2013 Anyware Services
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
 * This tool displays the list of groups, and allow a CMS user perform several actions such as :
 * 
 * - rename / delete a group
 * - add / delete users in a group
 * 
 * @private
 */
Ext.define('Ametys.plugins.core.groups.GroupsTool', {
	extend: 'Ametys.tool.Tool',
	
	/**
	 * @property {Ext.grid.GridPanel} _groupGrid The grid with the group records
	 * See {@link Ametys.plugins.cms.group.GroupsTool.GroupEntry}
	 * @private
	 */
	/**
	 * @property {Ext.grid.GridPanel} _userGrid The grid with the user records of the current selected group
	 * See {@link Ametys.plugins.cms.group.GroupsTool.GroupUserEntry}
	 * @private
	 */
	/**
	 * @property {String} _groupsManagerRole The role of the groups manager
	 * @private
	 */
	/**
	 * @property {String} _usersManagerRole The role of the users manager
	 * @private
	 */
	/**
	 * @property {String} [_userTargetType=user] The target type for users
	 * @private
	 */
	/**
	 * @property {String} [_groupTargetType=group] The target type for groups
	 * @private
	 */
	
	/**
	 * @inheritdoc
	 */
	getMBSelectionInteraction: function() 
	{
		return Ametys.tool.Tool.MB_TYPE_ACTIVE;
	},
	
	constructor: function(config)
	{
		this.callParent(arguments);
		
		// Set the role and the message target type
		this._groupsManagerRole =  config['groups-manager-role'];
		this._usersManagerRole = config['users-manager-role'];
		this._groupTargetType = config['group-target-type'] || Ametys.message.MessageTarget.GROUP;
		this._userTargetType = config['user-target-type'] || Ametys.message.MessageTarget.USER;
		
		// Bus messages listeners
		Ametys.message.MessageBus.on(Ametys.message.Message.CREATED, this._onMessageCreated, this);
		Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, this._onMessageEdited, this);
		Ametys.message.MessageBus.on(Ametys.message.Message.DELETED, this._onMessageDeleted, this);
	},
	
	setParams: function(params)
	{
    	// Register the tool on the history tool
		var role = this.getFactory().getRole();
	    var toolParams = this.getParams();

        Ametys.navhistory.HistoryDAO.addEntry({
			id: this.getId(),
			label: this.getTitle(),
			description: this.getDescription(),
			iconSmall: this.getSmallIcon(),
			iconMedium: this.getMediumIcon(),
			iconLarge: this.getLargeIcon(),
			type: Ametys.navhistory.HistoryDAO.TOOL_TYPE,
			action: Ext.bind(Ametys.tool.ToolsManager.openTool, Ametys.tool.ToolsManager, [role, toolParams], false)
        });
        
        this.callParent(arguments); 
	},
	
	createPanel: function()
	{
		/* WEST PANEL (GROUPS) */
		var groupStore = this._createGroupStore();
		
		this._groupGrid = Ext.create('Ext.grid.Panel', {
			region: 'west',
			border: false,
			width: 350,
			cls: 'mask-below-menu',
			
			store: groupStore,
			columns: [{header: "<i18n:text i18n:key='PLUGINS_CORE_UITOOL_GROUPS_LABEL' i18n:catalogue='plugin.core'/>", width: 330, dataIndex: 'label', renderer: this._renderGroupName, hideable: false}],
			
			listeners: {
				selectionchange: {fn: this._onGroupSelectionChange, scope: this}
			}
		});
		
		/* CENTER PANEL (USERS) */
		var userStore = this._createUserStore();
		
		this._userGrid = Ext.create('Ext.grid.Panel', {
			region: 'center',
			border: false,
			cls: 'mask-below-menu',
			
			store: userStore,
			selModel: {
				mode: 'MULTI'
			},
			
			columns: [{header: "<i18n:text i18n:key='PLUGINS_CORE_UITOOL_GROUPS_USERSGROUP'/>", width: 350, dataIndex: 'name', renderer: this._renderUserName, hideable: false}],
			
			listeners: {
				selectionchange: {fn: this._onUserSelectionChange, scope: this}
			}
		});
		
		return Ext.create('Ext.panel.Panel', {
			defaults: {
				split: true
			},
			layout: 'border',
			items: [this._groupGrid, this._userGrid]
		});
	},
	
	/**
	 * @private
	 * Function to render the name of a group
	 * @param {Object} value The data value
	 * @param {Object} metaData A collection of data about the current cell
	 * @param {Ext.data.Model} record The record
	 * @return {String} The html value to render.
	 */
	_renderGroupName: function(value, metaData, record)
	{
		return '<img src="' + Ametys.getPluginResourcesPrefix('core') + '/img/groups/group_16.png' + '" style="float: left; margin-right: 3px"/>' + record.get('label') + ' (' + record.get('id') + ')';
	},
	
	/**
	 * @private
	 * Function to render the name of an user
	 * @param {Object} value The data value
	 * @param {Object} metaData A collection of data about the current cell
	 * @param {Ext.data.Model} record The record
	 * @return {String} The html value to render.
	 */
	_renderUserName: function(value, metaData, record)
	{
		return '<img src="' + Ametys.getPluginResourcesPrefix('core') + '/img/users/user_16.png' + '" style="float: left; margin-right: 3px"/>' + record.get('name') + ' (' + record.get('login') + ')';
	},
	
	/**
	 * @private
	 * Create the group store that the grid should use as its data source.
	 * @return {Ext.data.Store} The created store
	 */
	_createGroupStore: function ()
	{
		// Merge default store configuration with inherited configuration (provided by #getStoreConfig).
		var storeConfig = Ext.merge({
			remoteSort: false,
			autoLoad: true,
			
			model: 'Ametys.plugins.core.groups.GroupsTool.Group',
			proxy: {
				type: 'ametys',
				reader: {
					type: 'json',
					rootProperty: 'groups'
				}
			},
			
			sortOnLoad: true,
			sorters: [{property: 'label', direction:'ASC'}]
			
		}, this.getGroupStoreConfig());
		
		return Ext.create('Ext.data.Store', storeConfig);
	},
	
	/**
	 * Returns the elements of configuration of group store to be overridden.
	 * Override this function if you want to override the group store configuration.
	 * @return {Object} The elements of store configuration to be overridden
	 */
	getGroupStoreConfig: function()
	{
		return {
			proxy: {
				plugin: 'core',
				url: 'groups/search.json',
				
				extraParams: {
					groupsManagerRole: this._groupsManagerRole,
					limit: null // No pagination
				}
			}
		};
	},
	
	/**
	 * @private
	 * Create the user store that the user grid will use as its data source.
	 * @return {Ext.data.Store} The created store
	 */
	_createUserStore: function ()
	{
		// Merge default store configuration with inherited configuration (provided by #getStoreConfig).
		var storeConfig = Ext.merge({
			remoteSort: false,
			autoLoad: false,
			
			model: 'Ametys.plugins.core.groups.GroupsTool.UserGroup',
			proxy: {
				type: 'ametys',
				reader: {
					type: 'json',
					rootProperty: 'users'
				}
			},
			
			sortOnLoad: true,
			sorters: [{property: 'name', direction:'ASC'}],
			
			listeners: {
				beforeload: {fn: this._onBeforeLoadUsers, scope: this}
			}
			
		}, this.getUserStoreConfig());
		
		return Ext.create('Ext.data.Store', storeConfig);
	},
	
	/**
	 * Returns the elements of configuration of user store to be overridden.
	 * Override this function if you want to override the user store configuration.
	 * @return {Object} The elements of store configuration to be overridden
	 */
	getUserStoreConfig: function()
	{
		return {
			proxy: {
				plugin: 'core',
				url: 'group/users',
				
				extraParams: {
					groupsManagerRole: this._groupsManagerRole,
					usersManagerRole: this._usersManagerRole,
					limit: null // No pagination
				}
			}
		};
	},
	
	/**
	 * Function called before loading the user store
	 * @param {Ext.data.Store} store The store
	 * @param {Ext.data.operation.Operation} operation The object that will be passed to the Proxy to load the store
	 * @private
	 */
	_onBeforeLoadUsers: function (store, operation)
	{
		operation.setParams(operation.getParams() || {});
		
		if (!operation.getParams().groupID)
		{
			var group = this._groupGrid.getSelectionModel().getSelection()[0];
			if (group)
			{
				operation.setParams(Ext.apply(operation.getParams(), {
					groupID: group.getId()
				}));
			}
		}
	},
	
	/**
	 * Fires a event of selection on message bus, from the selected contents in the grid.
	 * Add load the user store given for the selected group.
	 * @param {Ext.selection.Model} model The selection model
	 * @param {Ext.data.Model[]} selected The selected records
	 * @param {Object} eOpts Event options
	 * @private
	 */
	_onGroupSelectionChange: function(model, selected, eOpts)
	{
		this._userGrid.getStore().removeAll();
		
		var group = selected[0];
		if (group)
		{
			this._userGrid.getStore().load({
				params: {
					groupID: group.getId(),
					groupsManagerRole: this._groupsManagerRole,
					usersManagerRole: this._usersManagerRole
				}
			});
		}
		
		this.sendCurrentSelection();
	},
	
	/**
	 * Called when selection change in the user grid
	 * @param {Ext.selection.Model} model The selection model
	 * @param {Ext.data.Model[]} selected The selected records
	 * @param {Object} eOpts Event options
	 * @private
	 */
	_onUserSelectionChange: function(model, selected, eOpts)
	{
		this.sendCurrentSelection();
	},
	
	/**
	 * @inheritdoc
	 */
	sendCurrentSelection: function()
	{
		var targets = [];
		
		var users = this._userGrid.getSelectionModel().getSelection();
		var userTargets = [];
		
		var me = this;
		Ext.Array.forEach(users, function(user) {
			userTarget = Ext.create('Ametys.message.MessageTarget', {
				type: me._userTargetType,
				parameters: {login: user.getId()}
			});
			
			userTargets.push(userTarget);
		});
		
		
		var group = this._groupGrid.getSelectionModel().getSelection()[0];
		if (group)
		{
			var groupTarget = {
				type: this._groupTargetType,
				parameters: {
					id: group.getId()
				},
				subtargets: userTargets
			};
			
			targets.push(groupTarget);
		}
		
		Ext.create('Ametys.message.Message', {
			type: Ametys.message.Message.SELECTION_CHANGED,
			targets: targets
		});
	},
	
	/**
	 * Listener when a Ametys.message.Message#CREATED message was received
	 * @param {Ametys.message.Message} message The received message
	 * @private
	 */
	_onMessageCreated: function(message)
	{
		var groupTarget = message.getTarget(new RegExp('^' + this._groupTargetType + '$'), 1);
		if (groupTarget)
		{
			var id = groupTarget.getParameters().id;
			var group = this._groupGrid.getStore().getById(id);
			if (group)
			{
				Ametys.plugins.core.groups.GroupsDAO.getGroup([id, this._groupsManagerRole], this._updateGroup, {scope: this});
			}
			else
			{
				Ametys.plugins.core.groups.GroupsDAO.getGroup([id, this._groupsManagerRole], this._addGroup, {scope: this});
			}
		}
	},
	
	/**
	 * Listener when a Ametys.message.Message#MODIFIED message was received
	 * @param {Ametys.message.Message} message The received message
	 * @private
	 */
	_onMessageEdited: function(message)
	{
		var groupTarget = message.getTarget(this._groupTargetType);
		if (groupTarget != null)
		{
			var id = groupTarget.getParameters().id;
			if (message.getParameters().major)
			{
				Ametys.plugins.core.groups.GroupsDAO.getGroup([id, this._groupsManagerRole], this._updateGroup, {scope: this});
			}
			else
			{
				// Reload users' group
				this._userGrid.getStore().load({
					params: {
						groupID: id,
						groupsManagerRole: this._groupsManagerRole,
						usersManagerRole: this._usersManagerRole
					}
				});
				
			}
		}
	},
	
	/**
	 * Listener when a Ametys.message.Message#DELETED message was received
	 * @param {Ametys.message.Message} message The received message
	 * @private
	 */
	_onMessageDeleted: function(message)
	{
		var groupTargets = message.getTargets(this._groupTargetType);
		
		var store = this._groupGrid.getStore();
		Ext.Array.forEach(groupTargets, function(target) {
			var group = store.getById(target.getParameters().id);
			if (group)
			{
				store.remove(group);
			}
		}, this);
	},
	
	
	/**
	 * @private
	 * Add a group record 
	 * @param {Object} group The group's properties
	 */
	_addGroup: function (group)
	{
		if (group && group.id)
		{
			var record = Ext.create('Ametys.plugins.core.groups.GroupsTool.Group', group);
			this._groupGrid.getStore().addSorted(record);
			this._groupGrid.getSelectionModel().select([record]);
		}
	},
	
	/**
	 * @private
	 * Update a group record 
	 * @param {Object} group The group's properties
	 */
	_updateGroup: function (group)
	{
		if (group && group.id)
		{
			var store = this._groupGrid.getStore();
			var record = store.getById(group.id);
			
			record.beginEdit();
			record.set('label', group.label);
			record.endEdit();
			
			// commit changes (record is not marked as dirty anymore)
			record.commit();
			
			// re-sort
			store.sort();
		}
	}
});
