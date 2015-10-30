/*
 *  Copyright 2015 Anyware Services
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
 * This is a helper to select one or more users from the user manager. See {@link #act} method
 * 
 * 		Ametys.helper.SelectUser.act({
 *			callback: Ext.bind(function (users) { console.info(users); }, this), 
 *			allowMultiselection: true,
 *		});	
 */
Ext.define('Ametys.helper.SelectUser', {
	singleton: true,
	
	/**
	 * @property {Number} RESULT_LIMIT
	 * @readonly
	 * @static
	 * The maximum number of records to search for
	 */
	RESULT_LIMIT: 100,
	
	/**
	 * @private
	 * @property {Boolean} _initialized Determines if the dialog box have been already initialized or not
	 */
	_initialized: false,
	
	/**
	 * @property {Boolean} allowMultiselection Property to enable or disable multiselection
	 */
	
	/**
	 * @property {String} usersManagerRole The currently selected UsersManager to use to list the user. The string is the role name on the server-side. Null/Empty means the default users manager. The current user manager role registered by the {@link #act} call.
	 */
	usersManagerRole: null,
	/**
	 * @property {String} pluginName=core The name of the currently selected plugin to use for requests. Selected by the {@link #act} call.
	 */
	/**
	 * @property {String} url=users/search.json The url of the currently selected plugin to use for requests. Selected by the {@link #act} call.
	 */
	
	/**
	 * @private
	 * @property {Function} callBack The current callback function registered by the {@link #act} call
	 */
	/**
	 * @private
	 * @property {Function} cancelCallback The current cancel callback function registered by the {@link #act} call
	 */
	/**
	 * @private
	 * @property {String} pluginName The name of the currently selected plugin to use for requests. Selected by the {@link #act} call.
	 */
	/**
	 * @private
	 * @property {Ext.form.field.Text} _searchField The field of the dialog box that displays the filter field
	 */
	/**
	 * @private
	 * @property {Ametys.window.DialogBox} _box The re-usable dialog box
	 */
	/**
	 * @private
	 * @property {Ext.grid.Panel} _userList The grid of result users
	 */
	
	/**
	 * Open the dialog box to select a user
	 * @param {Object} config The configuration options:
	 * @param {Function} config.callback The callback function to call when user(s) has(have) been selected
	 * @param {Object} config.callback.users A Map String-String of the selected users. The key is the user identifier and the value is the associated user name.
	 * @param {Function} config.cancelCallback The callback function if the user cancel the dialog box. Can be null.
	 * @param {String} [config.usersManagerRole] the avalon role of the users manager which will be called to get the user list, or null to call the default users manager.
	 * @param {Boolean} [config.allowMultiselection=true] Set to false to disable multiple selection of users.
	 * @param {String} [config.plugin=core] The plugin to use for search request.
	 * @param {String} [config.url=users/search.json] The url to use for search request.
	 */
	act: function (config)
	{
		config = config || {};
		
		this.callback = config.callback || function () {};
		this.cancelCallback = config.cancelCallback || function () {};
	    this.usersManagerRole = config.usersManagerRole || '';
	    this.allowMultiselection = config.allowMultiselection || true;
	    this.pluginName = config.plugin || 'core';
	    this.url = config.url || 'users/search.json';
		
	    this._delayedInitialize();
	    
		this._searchField.setValue("");
		this._userList.getSelectionModel().setSelectionMode(this.allowMultiselection ? 'SIMPLE' : 'SINGLE');
		this._userList.getSelectionModel().deselectAll();
		this._userList.getStore().setProxy ({
        	type: 'ametys',
        	plugin: this.pluginName,
			url: this.url,
        	reader: {
        		type: 'json',
        		rootProperty: 'users'
        	},
        	extraParams: {
				usersManagerRole: this.usersManagerRole,
				limit: this.RESULT_LIMIT
			}
        });
		
		this._box.show();
		this.loadUsers();
	},
	
	/**
	 * @private
	 * This method is called to initialize the dialog box. Only the first call will be taken in account.
	 */
	_delayedInitialize: function ()
	{
		if (this._initialized)
		{
			return true;
		}
		this._initialized = true;

		this._searchField = Ext.create('Ext.form.TextField', {
			 fieldLabel: "<i18n:text i18n:key='PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_FIND'/>",
			 name: "criteria",
			 cls: 'ametys',
			 region: 'north',
			 
			 labelWidth :70,
			 width: 210,
			 
			 value: "",
			 
			 enableKeyEvents: true,
			 listeners: {'keyup': Ext.bind(this._reload, this)}
		});
		
		var model = Ext.define('Ametys.helper.SelectUser.Users', {
		    extend: 'Ext.data.Model',
		    fields: [
		        {name: 'id', mapping: 'login'},
				{name: 'login'},
				{name: 'lastname', sortType: Ext.data.SortTypes.asNonAccentedUCString},
				{name: 'firstname', sortType: Ext.data.SortTypes.asNonAccentedUCString},
				{
					name: 'fullname',
					sortType: Ext.data.SortTypes.asNonAccentedUCString,
					depends: ['id', 'login', 'lastname', 'firstname'],
					calculate: function (data)
					{
						return [data.lastname, data.firstname || '', '(' + data.id + ')'].join(' ');
					}
				}
		    ]
		});

		var store = Ext.create('Ext.data.Store', {
			model: 'Ametys.helper.SelectUser.Users',
	        data: { users: []},
	        listeners: {
	        	'beforeload': Ext.bind(this._onBeforeLoad, this),
	        	'load': Ext.bind(this._onLoad, this)
	        }
		});
		
		this._userList = Ext.create('Ext.grid.Panel', {
			flex: 1,
			store : store,
			hideHeaders : true,
			columns: [{header: "Label", width : 240, menuDisabled : true, sortable: true, dataIndex: 'fullname'}]
		});	
		
		this._box = Ext.create('Ametys.window.DialogBox', {
			title : this.allowMultiselection ? "<i18n:text i18n:key='PLUGINS_CORE_UI_USERS_SELECTUSERS_DIALOG_CAPTION'/>" : "<i18n:text i18n:key='PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_CAPTION'/>",
			icon: Ametys.getPluginResourcesPrefix('core') + '/img/users/user_16.png',
			
			layout: {
			    type: 'vbox',
			    align : 'stretch',
			    pack  : 'start'
			},
			width: 280,
			height: 340,
			
			items : [
			         this._searchField, 
			         this._userList, 
			         {
			        	 xtype: 'container',
			        	 style: {
			        		 textAlign: 'center'
			        	 },
			        	 cls: 'a-text-warning',
			        	 html: "<i18n:text i18n:key='PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_WARN100'/>"
			         }
			],
			
			defaultFocus: this._searchField,
			closeAction: 'hide',
			
			buttons : [ {
				text :"<i18n:text i18n:key='PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_OK'/>",
				handler : Ext.bind(this.ok, this)
			}, {
				text :"<i18n:text i18n:key='PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_CANCEL'/>",
				handler : Ext.bind(this.cancel, this)
			} ]
		});
	},
	
	/**
	 * This method is called to apply the current filter immediately
	 * @private
	 */
	loadUsers: function ()
	{
		this._reloadTimer = null;
		this._userList.getStore().load();
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
			criteria: this._searchField.getValue()
		}));
	},
	
	/**
	 * Function called after loading the store
	 * @param {Ext.data.Store} store The store
	 * @param {Ext.data.Model[]} records The loaded records
	 */
	_onLoad: function (store, records)
	{
		if (records.length == 0)
		{
			Ametys.Msg.show({
			   title: "<i18n:text i18n:key='PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_CAPTION'/>",
			   msg: "<i18n:text i18n:key='PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_NORESULT'/>",
			   buttons: Ext.Msg.OK,
			   icon: Ext.MessageBox.INFO
			});
		}
	},
	
	/**
	 * This method is called to apply the current filter but in a delayed time.
	 * This is a listener method on filter modification.
	 * Every modification will not be directly applyed. Consecutive modifications (separated by less than 500 ms) will be applyed at once.
	 * @private
	 */
	_reload: function (field, newValue, oldValue)
	{
		if (this._reloadTimer != null)
		{
			window.clearTimeout(this._reloadTimer);
		}
		this._reloadTimer = window.setTimeout(Ext.bind(this.loadUsers, this), 500);
	},
	
	/**
	 * @private
	 * The method called when the user push the ok button of the dialog box
	 */
	ok: function ()
	{
		var addedusers = {}
		
		var selection = this._userList.getSelectionModel().getSelection();
		if (selection.length == 0)
		{
			Ametys.Msg.show({
				   title: this.allowMultiselection ? "<i18n:text i18n:key='PLUGINS_CORE_UI_USERS_SELECTUSERS_DIALOG_CAPTION'/>" : "<i18n:text i18n:key='PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_CAPTION'/>",
				   msg: "<i18n:text i18n:key='PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_ERROR_EMPTY'/>",
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.INFO
				});
			return;
		}

		this._box.hide();
		
		for (var i=0; i < selection.length; i++)
		{
			var opt = selection[i];
			addedusers[opt.get('login')] = opt.get('name');
		}
	
		this.callback(addedusers);
	},

	/**
	 * @private
	 * The method called when the user cancel the dialog box
	 */
	cancel: function ()
	{
		this._box.hide();
		this.cancelCallback();
	}
});
