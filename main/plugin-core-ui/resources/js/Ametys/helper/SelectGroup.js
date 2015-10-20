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
 * This is a helper to select one or more groups from the group manager. See {@link #act} method
 * 
 * 		Ametys.helper.SelectGroup.act({
 *			callback: Ext.bind(function (groups) { console.info(groups); }, this), 
 *			allowMultiselection: false,
 *		});	
 */
Ext.define('Ametys.helper.SelectGroup', {
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
	 * @property {Boolean} _initialized Determine if the plugin have been already initialized or not
	 */
	_initialized: false,
	
	/**
	 * @property {Boolean} allowMultiselection Property to enable or disable multiselection
	 */
	
	/**
	 * @property {String} groupsManagerRole The currently selected groupsManager to use to list the group. The string is the role name on the server-side. Null/Empty means the default groups manager. The current group manager role registered by the {@link #act} call.
	 */
	groupsManagerRole: null,
	
	/**
	 * @property {String} pluginName=core The name of the currently selected plugin to use for requests. Selected by the {@link #act} call.
	 */
	/**
	 * @property {String} url=groups/search.json The url of the currently selected plugin to use for requests. Selected by the {@link #act} call.
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
	 * @property {Ext.form.field.Text} _searchField The field of the dialog box that displays the filter field
	 */
	/**
	 * @private
	 * @property {Ext.grid.Panel} _groupList The grid of result groups
	 */
	/**
	 * @private
	 * @property {Ametys.window.DialogBox} _box The re-usable dialog box
	 */
	
	/**
	 * Open the dialog box to select a group
	 * @param {Object} config The configuration options:
	 * @param {Function} config.callback The callback function that is called when the group has been selected
	 * @param {Object} config.callback.groups A Map String-String of the selected groups. The key is the group identifier and the value is the associated group name.
	 * @param {Function} config.cancelCallback The callback function if the group cancel the dialog box. Can be null.
	 * @param {String} [config.groupsManagerRole] the avalon role of the groups manager which will be called to get the group list, or null to call the default groups manager.
	 * @param {Boolean} [config.allowMultiselection=true] Set to false to disable multiple selection of users.
	 * @param {String} [config.plugin=core] The plugin to use for search request.
	 * @param {String} [config.url=groups/search.json] The url to use for search request.
	 */
	act: function (config)
	{
		config = config || {};
		
		this.callback = config.callback || function () {};
		this.cancelCallback = config.cancelCallback || function () {};
	    this.groupsManagerRole = config.groupsManagerRole || '';
	    this.allowMultiselection = config.allowMultiselection || true;
	    this.pluginName = config.plugin || 'core';
	    this.url = config.url || 'groups/search.json';
	    
		this.delayedInitialize();
		
		this._searchField.setValue("");
		this._groupList.getSelectionModel().setSelectionMode(this.allowMultiselection ? 'SIMPLE' : 'SINGLE');
		this._groupList.getSelectionModel().deselectAll();
		this._groupList.getStore().setProxy({
			type: 'ametys',
			reader: {
				type: 'json',
				rootProperty: 'groups'
			},
			plugin: this.pluginName,
			url: this.url,
			
			extraParams: {
				groupsManagerRole: this.groupsManagerRole,
				limit: this.RESULT_LIMIT
			}
		});
		
		this._box.show();
		this.load();
	},
	
	/**
	 * @private
	 * This method is called to initialize the dialog box. Only the first call will be taken in account.
	 */
	delayedInitialize: function ()
	{
		if (this._initialized)
		{
			return true;
		}
		this._initialized = true;

		this._searchField = Ext.create('Ext.form.TextField', {
			cls: 'ametys',
			labelWidth :70,
			width: 210,
			
			fieldLabel: "<i18n:text i18n:key='PLUGINS_CORE_UI_GROUPS_SELECTGROUP_DIALOG_FIND'/>",
			name: "criteria",
			value: "",
			 
			enableKeyEvents: true,
			listeners: {'keyup': Ext.bind(this._reload, this)},
		});
		
		var model = Ext.define('Ametys.helper.SelectGroup.Group', {
		    extend: 'Ext.data.Model',
		    fields: [
	     		{name: 'id'},
	     		{name: 'label', sortType: Ext.data.SortTypes.asNonAccentedUCString}
	     	]
		});

		var store = Ext.create('Ext.data.Store', {
			model: 'Ametys.helper.SelectGroup.Group',
			sortOnLoad: true,
			sorters: [{property: 'label', direction:'ASC'}],
			
			listeners: {
	        	'beforeload': Ext.bind(this._onBeforeLoad, this),
	        	'load': Ext.bind(this._onLoad, this)
	        }
		});
		
		this._groupList = Ext.create('Ext.grid.Panel', {
			flex: 1,
			store : store,
			hideHeaders : true,
			columns: [{header: "Label", width : 240, menuDisabled : true, sortable: true, dataIndex: 'label'}]
		});	
		
		this._box = Ext.create('Ametys.window.DialogBox', {
			title :"<i18n:text i18n:key='PLUGINS_CORE_UI_GROUPS_SELECTGROUP_DIALOG_CAPTION'/>",
			layout: {
			    type: 'vbox',
			    align : 'stretch',
			    pack  : 'start'
			},
			width: 280,
			height: 340,
			icon: Ametys.getPluginResourcesPrefix('core') + '/img/groups/group_16.png',
			
			items : [
			         this._searchField, 
			         this._groupList, 
			         {
			        	 xtype: 'container',
			        	 style: {
			        		 textAlign: 'center'
			        	 },
			        	 cls: 'a-text-warning',
			        	 html: "<i18n:text i18n:key='PLUGINS_CORE_UI_GROUPS_SELECTGROUP_DIALOG_WARN100'/>"
			         }
			],
			
			defaultFocus: this._searchField,
			closeAction: 'hide',
			
			buttons : [ {
				text :"<i18n:text i18n:key='PLUGINS_CORE_UI_GROUPS_SELECTGROUP_DIALOG_OK'/>",
				handler : Ext.bind(this.ok, this)
			}, {
				text :"<i18n:text i18n:key='PLUGINS_CORE_UI_GROUPS_SELECTGROUP_DIALOG_CANCEL'/>",
				handler : Ext.bind(this.cancel, this)
			} ]
		});
	},
	
	/**
	 * This method is called to apply the current filter immediately
	 * @private
	 */
	load: function ()
	{
		this._reloadTimer = null;
		this._groupList.getStore().load();
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
			criteria: this._searchField.getValue(),
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
			   title: "<i18n:text i18n:key='PLUGINS_CORE_UI_GROUPS_SELECTGROUP_DIALOG_CAPTION'/>",
			   msg: "<i18n:text i18n:key='PLUGINS_CORE_UI_GROUPS_SELECTGROUP_DIALOG_NORESULT'/>",
			   buttons: Ext.Msg.OK,
			   icon: Ext.MessageBox.INFO
			});
		}
	},
	
	/**
	 * This method is called to apply the current filter but in a delayed time.
	 * This is a listener method on filter modificaiton.
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
	 * The method called when the group push the ok button of the dialog box
	 */
	ok: function ()
	{
		var addedgroups = {}
		
		var selection = this._groupList.getSelectionModel().getSelection();
		if (selection.length == 0)
		{
			Ametys.Msg.show({
				   title: "<i18n:text i18n:key='PLUGINS_CORE_UI_GROUPS_SELECTGROUP_DIALOG_CAPTION'/>",
				   msg: "<i18n:text i18n:key='PLUGINS_CORE_UI_GROUPS_SELECTGROUP_DIALOG_ERROR_EMPTY'/>",
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.INFO
				});
			return;
		}

		this._box.hide();
		
		for (var i=0; i < selection.length; i++)
		{
			var opt = selection[i];
			addedgroups[opt.get('id')] = opt.get('label');
		}
	
		this.callback(addedgroups);
	},

	/**
	 * @private
	 * The method called when the group cancel the dialog box
	 */
	cancel: function ()
	{
		this._box.hide();

		this.cancelCallback();
	}
});
