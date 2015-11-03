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
 * This tool displays the list of users
 * @private
 */
Ext.define('Ametys.plugins.core.users.UsersTool', {
	extend: 'Ametys.tool.Tool',
	
	statics: {
		/**
		 * @property {Number} RESULT_LIMIT
		 * @readonly
		 * @static
		 * The maximum number of records to search for
		 */
		RESULT_LIMIT: 100
	},

	/**
	 * @property {Ext.data.Store} _store The store with the user records
	 * See {@link Ametys.plugins.cms.user.UsersTool.UserEntry}
	 * @private
	 */
	/**
	 * @property {Ext.grid.Panel} _grid The grid panel displaying the users
	 * @private
	 */
	/**
	 * @property {Ext.form.field.Text} _searchField The search fields filter
	 * @private
	 */
	/**
	 * @property {String} _previousSearchValue The value of the search field used during the last load request.
	 * @private
	 */
	/**
	 * @property {String} _usersManagerRole the role of the users manager
	 * @private
	 */
	/**
	 * @property {String} _userTargetType the message target type for users
	 * @private
	 */

	constructor: function(config)
	{
		this.callParent(arguments);
		
		// Set the role and the message target type
		this._usersManagerRole =  config['users-manager-role'];
		this._userTargetType = config['message-target-type'] || Ametys.message.MessageTarget.USER;
		
		// Listening to some bus messages.
		Ametys.message.MessageBus.on(Ametys.message.Message.CREATED, this._onMessageCreated, this);
		Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, this._onMessageEdited, this);
		Ametys.message.MessageBus.on(Ametys.message.Message.DELETED, this._onMessageDeleted, this);
	},
	
	getMBSelectionInteraction: function() 
	{
		return Ametys.tool.Tool.MB_TYPE_ACTIVE;
	},
	
	createPanel: function()
	{
		this._store = this.createUserStore();
		
		this._grid = Ext.create("Ext.grid.Panel", {
			border: false,
			cls: 'mask-below-menu',
			
			store: this._store,
			scrollable: true,
			
			columns: [
				{header: "<i18n:text i18n:key='PLUGINS_CORE_UITOOL_USERS_COL_NAME' i18n:catalogue='plugin.core'/>", width: 250, sortable: true, dataIndex: 'displayName', renderer: this._renderDisplayName, hideable: false},
				{header: "<i18n:text i18n:key='PLUGINS_CORE_UITOOL_USERS_COL_EMAIL' i18n:catalogue='plugin.core'/>", width: 350, sortable: true, dataIndex: 'email'}
			],
			
			selModel : {
				mode: 'MULTI'
			},
			
			dockedItems: [
				// Search field
				{
					xtype: 'toolbar',
					cls: 'users-toolbar',
					dock: 'top',
					items: [{
						xtype: 'textfield',
						cls: 'ametys',
						emptyText: "<i18n:text i18n:key='PLUGINS_CORE_UITOOL_USERS_SEARCH_EMPTY_TEXT'/>",
						hideLabel: true,
						width: 200,
						enableKeyEvents: true,
						listeners: {
							keyup: { fn: this._onKeyUp, scope: this}
						}
					},
					'',
					{
						html: "<i18n:text i18n:key='PLUGINS_CORE_UITOOL_USERS_SEARCH_LIMIT_HELPTEXT'/>",
						xtype: 'component',
						cls: 'hint'
					}]
				}
			],
			
			listeners: {
				selectionchange: {fn: this._onSelectionChange, scope: this}
			}
		});
		
		return this._grid;
	},
	
	/**
	 * @private
	 * Renderer for user's full name
	 * @param {Object} value The data value
	 * @param {Object} metaData A collection of data about the current cell
	 * @param {Ext.data.Model} record The record
	 * @return {String} The html value to render.
	 */
	_renderDisplayName: function(value, metaData, record)
	{
		return '<img src="' + Ametys.getPluginResourcesPrefix('core') + '/img/users/user_16.png' + '" style="float: left; margin-right: 3px"/>' + value;
	},
	
	/**
	 * Get the user store
	 * @return {Ext.data.Store} The user store
	 */
	getStore: function ()
	{
		return this._store;
	},
	
	/**
	 * Create the user store that the grid should use as its data source.
	 * @return {Ext.data.Store} The created store
	 */
	createUserStore: function ()
	{
		// Merge default store configuration with inherited configuration (provided by #getStoreConfig).
		var storeConfig = Ext.merge({
			autoLoad: false,
			
			model: 'Ametys.plugins.core.users.UsersTool.User',
			proxy: {
				type: 'ametys',
				reader: {
					type: 'json',
					rootProperty: 'users'
				}
			},
			
			remoteSort: false,
			sortOnLoad: true,
			sorters: [{property: 'displayName', direction:'ASC'}],
			
			listeners: {
				beforeload: {fn: this._onBeforeLoad, scope: this}
			}
			
		}, this.getStoreConfig());
		
		return Ext.create('Ext.data.Store', storeConfig);
	},
	
	/**
	 * Returns the elements of configuration of user store to be overridden.
	 * Override this function if you want to override the user store configuration.
	 * @return {Object} The elements of store configuration to be overridden
	 */
	getStoreConfig: function()
	{
		return {
			proxy: {
				plugin: 'core',
				url: 'users/search.json'
			}
		};
	},
	
	sendCurrentSelection: function()
	{
		var selection = this._grid.getSelectionModel().getSelection();
		var targets = [];
		
		var me = this;
		Ext.Array.forEach(selection, function(record) {
			targets.push({
				type: me._userTargetType,
				parameters: {id: record.getId()}
			})
		});
		
		Ext.create('Ametys.message.Message', {
			type: Ametys.message.Message.SELECTION_CHANGED,
			targets: targets
		});
	},
	
	/**
	 * Reload store after research
	 * @param {Ext.form.field.Text} field The search text field
	 * @private
	 */
	_onKeyUp: function (field)
	{
		if (this._sendTask)
		{
			window.clearTimeout(this._sendTask);
		}
		this._sendTask = window.setTimeout(Ext.bind(this.search, this), 500);
	},

	/**
	 * Load the user store
	 */
	search: function()
	{
		this._sendTask = null;
		this._store.load();
	},
	
	/**
	 * Get the value of search text field
	 * @return {String} The value of search text field
	 * @private
	 */
	_getSearchFieldValue : function ()
	{
		this._searchField = this._searchField || this._grid.getDockedItems('toolbar[dock="top"]')[0].down('textfield');
		return this._searchField ? this._searchField.getValue() : '';
	},

	setParams: function(params)
	{
		this.callParent(arguments);
		this.search();
		
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
	},
	
	/**
	 * Function called before loading the store
	 * @param {Ext.data.Store} store The store
	 * @param {Ext.data.operation.Operation} operation The object that will be passed to the Proxy to load the store
	 * @param {Object} eOpts Event options
	 * @private
	 */
	_onBeforeLoad: function(store, operation, eOpts)
	{
		operation.setParams( operation.getParams() || {} );
		operation.setParams( Ext.apply(operation.getParams(), {
			usersManagerRole: this._usersManagerRole,
			criteria: this._getSearchFieldValue(),
			limit: this.self.RESULT_LIMIT
		}));
	},
	
	/**
	 * Fires a event of selection on message bus, from the selected contents in the grid.
     * @param {Ext.selection.Model} model The selection model
     * @param {Ext.data.Model[]} selected The selected records
	 * @param {Object} eOpts Event options
	 * @private
     */
	_onSelectionChange: function(model, selected, eOpts)
	{
		this.sendCurrentSelection();
	},
	
	/**
	 * Listener when a Ametys.message.Message#CREATED message was received
	 * @param {Ametys.message.Message} message The received message
	 * @private
	 */
	_onMessageCreated: function(message)
	{
		var userTarget = message.getTarget(new RegExp('^' + this._userTargetType + '$'), 1);
		if (userTarget)
		{
			var login = userTarget.getParameters().id;
			var user = this._store.getById(login);
			if (user)
			{
				Ametys.plugins.core.users.UsersDAO.getUser([login, this._usersManagerRole], this._updateUser, {scope: this});
			}
			else
			{
				Ametys.plugins.core.users.UsersDAO.getUser([login, this._usersManagerRole], this._addUser, {scope: this});
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
		if (message != null && message.getParameters().major === true)
		{
			var userTarget = message.getTarget(new RegExp('^' + this._userTargetType + '$'), 1);
			if (userTarget)
			{
				Ametys.plugins.core.users.UsersDAO.getUser([userTarget.getParameters().id, this._usersManagerRole], this._updateUser, {scope: this});
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
		var userTargets = message.getTargets(new RegExp('^' + this._userTargetType + '$'), 1);
		
		var me = this;
		Ext.Array.forEach(userTargets, function(target) {
			var user = me._store.getById(target.getParameters().id);
			if (user)
			{
				me._store.remove(user);
			}
		}, this);
	},
	
	/**
	 * @private
	 * Add a user record 
	 * @param {Object} user The user's properties
	 */
	_addUser: function (user)
	{
		if (user && user.login)
		{
			user.id = user.login;
			var record = Ext.create('Ametys.plugins.core.users.UsersTool.User', user);
			this._store.addSorted(record);
		}
	},
	
	/**
	 * @private
	 * Update a user record 
	 * @param {Object} user The user's properties
	 */
	_updateUser: function (user)
	{
		if (user && user.login)
		{
			var record = this._store.getById(user.login);
			
			record.beginEdit();
			record.set('lastname', user.lastname);
			record.set('firstname', user.firstname);
			record.set('email', user.email);
			record.set('fullname', user.fullname);
			record.set('sortablename', user.sortablename);
			record.endEdit();
			
			// commit changes (record is not marked as dirty anymore)
			record.commit();
			
			// re-sort
			this._store.sort();
		}
	}
});
