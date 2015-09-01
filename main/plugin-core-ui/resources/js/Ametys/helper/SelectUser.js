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
	 * @property {Boolean} initialized Determine if the plugin have been already initialized or not
	 */
	initialized: false,
	
	/**
	 * @private
	 * @property {String} usersManagerRole The currently selected UsersManager to use to list the user. The string is the role name on the server-side. Null/Empty means the default users manager. The current user manager role registered by the {@link #act} call.
	 */
	usersManagerRole: null,
	
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
	 * @property {Ext.form.field.Text} criteria The field of the dialog box that displays the filter field
	 */
	/**
	 * @private
	 * @property {Ametys.window.DialogBox} box The re-usable dialog box
	 */
	
	/**
	 * @private
	 * This method is called to initialize the dialog box. Only the first call will be taken in account.
	 */
	delayedInitialize: function ()
	{
		if (this.initialized)
		{
			return true;
		}
		this.initialized = true;

		var plugin = this.pluginName;

		this.criteria = new Ext.form.TextField ({
			 listeners: {'keyup': Ext.bind(this._reload, this)},
			 fieldLabel: "<i18n:text i18n:key='PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_FIND'/>",
			 name: "criteria",
			 
			 region: 'north',
			 
			 labelWidth :70,
			 width: 210,
			 
			 enableKeyEvents: true,
			 value: ""
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
	        proxy: {
	        	type: 'ametys',
	        	plugin: 'core',
				url: 'users/search.json',
	        	reader: {
	        		type: 'json',
	        		rootProperty: 'users'
	        	}
	        },
	        
	        listeners: {
	        	'beforeload': Ext.bind(this._onBeforeLoad, this),
	        	'load': Ext.bind(this._onLoad, this)
	        }
		});
		
		this.userList = new Ext.grid.Panel({
			region: 'center',
			store : store,
			hideHeaders : true,
			columns: [{header: "Label", width : 240, menuDisabled : true, sortable: true, dataIndex: 'fullname'}]
		});	
		
		var warning = new Ext.Component({
			region: 'south',
			height: 26,
			cls: 'select-user-warning',
			
			html: "<i18n:text i18n:key='PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_WARN100'/>"
		});
		
		this.box = new Ametys.window.DialogBox({
			title :"<i18n:text i18n:key='PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_CAPTION'/>",
			layout :'border',
			width: 280,
			height: 340,
			icon: Ametys.getPluginResourcesPrefix('core') + '/img/users/user_16.png',
			items : [this.criteria, this.userList, warning],
			
			defaultButton: this.criteria,
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
	 * Display the dialog box to select a user
	 * @param {Object} config The configuration for the box
	 * @param {Function} config.callback The callback function that is called when the user has been selected
	 * @param {Object} config.callback.users A Map String-String of the selected users. The key is the user identifier and the value is the associated user name.
	 * @param {Function} config.cancelCallback The callback function if the user cancel the dialog box. Can be null.
	 * @param {String} config.usersManagerRole the avalon role of the users manager which will be called to get the user list, or null to call the default users manager.
	 * @param {Boolean} config.allowMultiselection True to authorize multiple selection of users. Default value is true.
	 * @param {String} config.plugin The plugin to use for the request. Default value is 'core'.
	 */
	act: function (config)
	{
		config = config || {};
		
		this.delayedInitialize();
		this.callback = config.callback || function () {};
		this.cancelCallback = config.cancelCallback || function () {};
	    this.usersManagerRole = config.usersManagerRole || '';
	    this.allowMultiselection = config.allowMultiselection || true;
	    this.pluginName = config.plugin || 'core';
		
		this.criteria.setValue("");
		this.userList.getSelectionModel().setSelectionMode(this.allowMultiselection ? 'SIMPLE' : 'SINGLE');

		this.box.show();
		
		this.load();
	},
	
	/**
	 * This method is called to apply the current filter immediately
	 * @private
	 */
	load: function ()
	{
		this._reloadTimer = null;

		var criteria = this.criteria.getValue();
		this.userList.getStore().load();
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
			usersManagerRole: this.usersManagerRole,
			criteria: this.criteria.getValue(),
			limit: this.RESULT_LIMIT
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
		this._reloadTimer = window.setTimeout(Ext.bind(this.load, this), 500);
	},
	
	/**
	 * @private
	 * The method called when the user push the ok button of the dialog box
	 */
	ok: function ()
	{
		var addedusers = {}
		
		var selection = this.userList.getSelectionModel().getSelection();
		if (selection.length == 0)
		{
			Ametys.Msg.show({
				   title: "<i18n:text i18n:key='PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_CAPTION'/>",
				   msg: "<i18n:text i18n:key='PLUGINS_CORE_UI_USERS_SELECTUSER_DIALOG_ERROR_EMPTY'/>",
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.INFO
				});
			return;
		}

		this.box.hide();
		
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
		this.box.hide();

		this.cancelCallback();
	}
});
