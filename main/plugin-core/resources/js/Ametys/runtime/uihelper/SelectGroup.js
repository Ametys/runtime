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
 * 		Ametys.runtime.uihelper.SelectGroup.act({
 *			callback: Ext.bind(function (groups) { console.info(groups); }, this), 
 *			allowMultiselection: false,
 *		});	
 */
Ext.define('Ametys.runtime.uihelper.SelectGroup', {
	singleton: true,
	
	/**
	 * @private
	 * @property {Boolean} initialized Determine if the plugin have been already initialized or not
	 */
	initialized: false,
	
	/**
	 * @private
	 * @property {String} groupsManagerRole The currently selected groupsManager to use to list the group. The string is the role name on the server-side. Null/Empty means the default groups manager. The current group manager role registered by the {@link #act} call.
	 */
	groupsManagerRole: null,
	
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
	delayed_initialize: function ()
	{
		if (this.initialized)
		{
			return true;
		}
		this.initialized = true;

		var plugin = this.pluginName;

		this.criteria = new Ext.form.TextField ({
			 listeners: {'keyup': Ext.bind(this.reload, this)},
			 fieldLabel: "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_FIND'/>",
			 name: "criteria",
			 
			 region: 'north',
			 
			 labelWidth :70,
			 width: 210,
			 
			 enableKeyEvents: true,
			 value: ""
		});
		
		var model = Ext.define('Ametys.runtime.uihelper.SelectGroup.Groups', {
		    extend: 'Ext.data.Model',
		    fields: [
		        {name: 'id',  type: 'string'},
		        {name: 'label',  type: 'string'}
		    ]
		});

		var store = Ext.create('Ext.data.Store', {
			model: 'Ametys.runtime.uihelper.SelectGroup.Groups',
	        data: { groups: []},
	        proxy: {
	        	type: 'memory',
	        	reader: {
	        		type: 'json',
	        		rootProperty: 'groups'
	        	}
	        }
		});
		
		this.listview = new Ext.grid.Panel({
			region: 'center',
			store : store,
			hideHeaders : true,
			columns: [{header: "Label", width : 240, menuDisabled : true, sortable: true, dataIndex: 'label'}]
		});	
		
		var warning = new Ext.Component({
			region: 'south',
			height: 26,
			cls: 'select-group-warning',
			
			html: "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_WARN100'/>"
		});
		
		this.box = new Ametys.window.DialogBox({
			title :"<i18n:text i18n:key='PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_CAPTION'/>",
			layout :'border',
			width: 280,
			height: 340,
			icon: Ametys.getPluginResourcesPrefix('core') + '/img/groups/icon_small.png',
			items : [this.criteria, this.listview, warning],
			
			defaultButton: this.criteria,
			closeAction: 'hide',
			
			buttons : [ {
				text :"<i18n:text i18n:key='PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_OK'/>",
				handler : Ext.bind(this.ok, this)
			}, {
				text :"<i18n:text i18n:key='PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_CANCEL'/>",
				handler : Ext.bind(this.cancel, this)
			} ]
		});
	},
	
	/**
	 * Display the dialog box to select a group
	 * @param {Object} config The configuration for the box
	 * @param {Function} config.callback The callback function that is called when the group has been selected
	 * @param {Object} config.callback.groups A Map String-String of the selected groups. The key is the group identifier and the value is the associated group name.
	 * @param {Function} config.cancelCallback The callback function if the group cancel the dialog box. Can be null.
	 * @param {String} config.groupsManagerRole the avalon role of the groups manager which will be called to get the group list, or null to call the default groups manager.
	 * @param {Boolean} config.allowMultiselection True to authorize multiple selection of users. Default value is true.
	 * @param {String} config.plugin The plugin to use for the request. Default value is 'core'.
	 */
	act: function (config)
	{
		this.delayed_initialize();
		this.callback = config.callback || function () {};
		this.cancelCallback = config.cancelCallback || function () {};
	    this.groupsManagerRole = config.groupsManagerRole || '';
	    this.allowMultiselection = config.allowMultiselection || true;
	    this.pluginName = config.plugin || 'core';
		
		this.criteria.setValue("");
		this.listview.getSelectionModel().setSelectionMode(this.allowMultiselection ? 'SIMPLE' : 'SINGLE');
		
		this.box.show();
		
		this.load();
	},
	
	/**
	 * This method is called to apply the current filter immediately
	 * @private
	 */
	load: function ()
	{
		this.reloadTimer = null;

		var criteria = this.criteria.getValue();

		// Get the group list from the UsersManager.
		var params = { criteria: criteria, limit: 100, start: 0, groupsManagerRole: this.groupsManagerRole };
		
		var result = Ametys.data.ServerComm.send({
			plugin: this.pluginName, 
			url: "groups/search.xml", 
			parameters: params, 
			priority: Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, 
			callback: null, 
			responseType: null
		});
	    if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key='PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_ERROR_LISTING'/>", result, "Ametys.runtime.uihelper.SelectGroup.load"))
	    {
	       return;
	    }

		this.listview.getStore().removeAll();
		
		var groups = Ext.dom.Query.select("Search/groups/group", result);

		if (groups.length == 0)
	    {
			Ametys.Msg.show({
				   title: "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_CAPTION'/>",
				   msg: "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_NORESULT'/>",
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.INFO
				});
			return;
	    }

		for (var i=0; i < groups.length; i++)
		{
			var label = Ext.dom.Query.selectValue('label', groups[i]);
			var fullname = label + " (" + groups[i].getAttribute('id') + ")";
			
			var group = Ext.create('Ametys.runtime.uihelper.SelectGroup.Groups', {
				id: groups[i].getAttribute('id'),
				label: fullname
			});
			this.listview.getStore().addSorted(group);
		}
	},
	
	/**
	 * This method is called to apply the current filter but in a delayed time.
	 * This is a listener method on filter modificaiton.
	 * Every modification will not be directly applyed. Consecutive modifications (separated by less than 500 ms) will be applyed at once.
	 * @private
	 */
	reload: function (field, newValue, oldValue)
	{
		if (this.reloadTimer != null)
		{
			window.clearTimeout(this.reloadTimer);
		}
		this.reloadTimer = window.setTimeout(Ext.bind(this.load, this), 500);
	},
	
	/**
	 * @private
	 * The method called when the group push the ok button of the dialog box
	 */
	ok: function ()
	{
		var addedgroups = {}
		
		var selection = this.listview.getSelectionModel().getSelection();
		if (selection.length == 0)
		{
			Ametys.Msg.show({
				   title: "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_CAPTION'/>",
				   msg: "<i18n:text i18n:key='PLUGINS_CORE_GROUPS_SELECTGROUP_DIALOG_ERROR_EMPTY'/>",
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.INFO
				});
			return;
		}

		this.box.hide();
		
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
		this.box.hide();

		this.cancelCallback();
	}
});
