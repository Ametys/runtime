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
 *			callback: Ext.bind(function (groups) { console.info(groups[0].id + ', ' + groups[0].groupDirectory); }, this), 
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
	 * @property {String} pluginName=core The name of the currently selected plugin to use for requests. Selected by the {@link #act} call.
	 */
	/**
	 * @property {String} url=groups/search.json The url of the currently selected plugin to use for requests. Selected by the {@link #act} call.
	 */
    /**
     * @property {String} [context] The context for the group directories to display in the combobox. All group directories will be displayed if not provided.
     */
    /**
     * @private
     * @property {Ext.form.field.ComboBox} _groupDirectoriesField The combobox of the dialog box that displays the group directories where to search
     */
    /**
     * @property {Boolean} _enableAllDirectoriesOption True to add an option in the group directories combobx for searching over all the directories.
     * @private
     */
    /**
     * @property {Boolean} _allDirectoriesOptionId The id of the 'all directories' options.
     * @private
     * @readonly
     */
    _allDirectoriesOptionId: '#all',
	
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
	 * @param {Object[]} config.callback.groups An array of groups.
	 * @param {String} config.callback.groups.id The id of the group
	 * @param {String} config.callback.groups.groupDirectory The group directory id of the group
	 * @param {String} config.callback.groups.label The name of the group
	 * @param {String} config.callback.groups.groupDirectoryName The name of the group directory
	 * @param {Function} config.cancelCallback The callback function if the group cancel the dialog box. Can be null.
	 * @param {Boolean} [config.allowMultiselection=true] Set to false to disable multiple selection of users.
	 * @param {String} [config.plugin=core] The plugin to use for search request.
	 * @param {String} [config.url=groups/search.json] The url to use for search request.
     * @param {String} [config.context] The context for the group directories to display in the combobox. Default to the current context.
     * @param {Boolean} [config.enableAllDirectoriesOption=true] True to add an option in the directory combobx for searching over all the directories.
	 */
	act: function (config)
	{
		config = config || {};
		
		this.callback = config.callback || function () {};
		this.cancelCallback = config.cancelCallback || function () {};
	    this.allowMultiselection = config.allowMultiselection || true;
	    this.pluginName = config.plugin || 'core';
	    this.url = config.url || 'groups/search.json';
        this.context = config.context != null ? config.context : Ametys.getAppParameter('context');
        this._enableAllDirectoriesOption = config.enableAllDirectoriesOption !== false;
	    
		this.delayedInitialize();
		
        this._groupDirectoriesField.clearValue();
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
				limit: this.RESULT_LIMIT
			}
		});
		
		this._box.show();
		this._loadDirectories();
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
        
        this._groupDirectoriesField = Ext.create('Ext.form.field.ComboBox', {
            xtype: 'combobox',
            fieldLabel: "{{i18n PLUGINS_CORE_UI_GROUPS_SELECTGROUP_DIALOG_DIRECTORY}}",
            name: "groupDirectories",
            cls: 'ametys',
            labelWidth: 150,
            
            store: {
                fields: ['id', {name: 'label', sortType: Ext.data.SortTypes.asNonAccentedUCString}],
                proxy: {
                    type: 'ametys',
                    plugin: 'core-ui',
                    url: 'group-directories.json',
                    reader: {
                        type: 'json',
                        rootProperty: 'groupDirectories'
                    }
                },
                sorters: [{property: 'label', direction: 'ASC'}],
                listeners: {
                    'beforeload': {fn: this._onBeforeLoadDirectories, scope: this},
                    'load': {fn: this._onLoadDirectories, scope: this}
                }
            },
            valueField: 'id',
            displayField: 'label',
            queryMode: 'local',
            forceSelection: true,
            triggerAction: 'all',
            
            listeners: {change: Ext.bind(this._onGroupDirectoryChange, this)}
        });

		this._searchField = Ext.create('Ext.form.TextField', {
			cls: 'ametys',
			labelWidth: 70,
			width: 210,
			
			fieldLabel: "{{i18n PLUGINS_CORE_UI_GROUPS_SELECTGROUP_DIALOG_FIND}}",
			name: "criteria",
			value: "",
			 
			enableKeyEvents: true,
			listeners: {'keyup': Ext.bind(this._reload, this)}
		});
		
		var model = Ext.define('Ametys.helper.SelectGroup.Group', {
		    extend: 'Ext.data.Model',
		    fields: [
	     		{name: 'id'},
	     		{name: 'label', sortType: Ext.data.SortTypes.asNonAccentedUCString},
	     		{name: 'groupDirectory'},
	     		{name: 'groupDirectoryLabel', sortType: Ext.data.SortTypes.asNonAccentedUCString},
                {
                    name: 'displayName',
                    sortType: Ext.data.SortTypes.asNonAccentedUCString,
                    calculate: function(data)
                    {
                        return data.label + ' (' + data.id + ', ' + data.groupDirectoryLabel + ')';
                    }
                }
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
			store: store,
			hideHeaders: true,
			columns: [{header: "Label", flex: 1, menuDisabled : true, sortable: true, dataIndex: 'displayName'}]
		});	
		
		this._box = Ext.create('Ametys.window.DialogBox', {
			title: this.allowMultiselection ? "{{i18n PLUGINS_CORE_UI_GROUPS_SELECTGROUPS_DIALOG_CAPTION}}" : "{{i18n PLUGINS_CORE_UI_GROUPS_SELECTGROUP_DIALOG_CAPTION}}",
			layout: {
			    type: 'vbox',
			    align : 'stretch',
			    pack  : 'start'
			},
			width: 450,
			height: 600,
			//icon: Ametys.getPluginResourcesPrefix('core-ui') + '/img/groups/group_16.png',
            iconCls: 'ametysicon-multiple25',
			
			items: [
                     this._groupDirectoriesField,
			         this._searchField, 
			         this._groupList, 
			         {
			        	 xtype: 'container',
			        	 style: {
			        		 textAlign: 'center'
			        	 },
			        	 cls: 'a-text-warning',
			        	 html: "{{i18n PLUGINS_CORE_UI_GROUPS_SELECTGROUP_DIALOG_WARN100}}"
			         }
			],
			
			defaultFocus: this._searchField,
			closeAction: 'hide',
			
			referenceHolder: true,
			defaultButton: 'validate',
			
			buttons: [{
				reference: 'validate',
				text: "{{i18n PLUGINS_CORE_UI_GROUPS_SELECTGROUP_DIALOG_OK}}",
				handler: Ext.bind(this.ok, this)
			}, {
				text: "{{i18n PLUGINS_CORE_UI_GROUPS_SELECTGROUP_DIALOG_CANCEL}}",
				handler: Ext.bind(this.cancel, this)
			} ]
		});
	},
    
    /**
     * @private
     * Load the groups when the current group directory has changed 
     */
    _onGroupDirectoryChange: function()
    {
        this.load();
    },
    
    /**
     * Function called before loading the group directory store
     * @param {Ext.data.Store} store The store
     * @param {Ext.data.operation.Operation} operation The object that will be passed to the Proxy to load the store
     * @private
     */
    _onBeforeLoadDirectories: function(store, operation)
    {
        operation.setParams( Ext.apply(operation.getParams() || {}, {
            context: this.context
        }));
    },
    
    /**
     * @private
     * Listener invoked after loading group directories
     * @param {Ext.data.Store} store The store
     * @param {Ext.data.Model[]} records The records of the store
     */
    _onLoadDirectories: function(store, records)
    {
        if (this._enableAllDirectoriesOption)
        {
            // Add an option in the directories combobox for searching over all the directories
            store.add({
                id: this._allDirectoriesOptionId,
                label: "{{i18n PLUGINS_CORE_UI_GROUPS_SELECTGROUP_DIALOG_DIRECTORY_OPTION_ALL}}"
            });
        }
    },
    
    /**
     * @private
     * Load the group directories
     */
    _loadDirectories: function()
    {
        this._groupDirectoriesField.getStore().load({
            scope: this,
            callback: function(records) {
                // When store loaded, select the 'all' option if it is available
                if (this._enableAllDirectoriesOption)
                {
                    this._groupDirectoriesField.select(this._allDirectoriesOptionId);
                }
                // Otherwise select the fist data
                else if (records.length > 0)
                {
                    this._groupDirectoriesField.select(records[0].get('id'));
                }
                // If there is one and only one directory, hide the combobox
                this._groupDirectoriesField.setHidden(records.length == 1);
            }
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
        // If the directory combobox value is invalid, cancel the loading
        if (this._groupDirectoriesField.getValue() == null)
        {
            return false;
        }
        
        // 'all' option is selected
        if (this._groupDirectoriesField.getValue() == this._allDirectoriesOptionId)
        {
            operation.setParams( Ext.apply(operation.getParams() || {}, {
                context: this.context,
                criteria: this._searchField.getValue()
            }));
            return true;
        }
        
        operation.setParams( Ext.apply(operation.getParams() || {}, {
            groupDirectoryId: this._groupDirectoriesField.getValue(),
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
			   title: this.allowMultiselection ? "{{i18n PLUGINS_CORE_UI_GROUPS_SELECTGROUPS_DIALOG_CAPTION}}" : "{{i18n PLUGINS_CORE_UI_GROUPS_SELECTGROUP_DIALOG_CAPTION}}",
			   msg: "{{i18n PLUGINS_CORE_UI_GROUPS_SELECTGROUP_DIALOG_NORESULT}}",
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
		var addedgroups = [];
		
		var selection = this._groupList.getSelectionModel().getSelection();
		if (selection.length == 0)
		{
			Ametys.Msg.show({
				   title: this.allowMultiselection ? "{{i18n PLUGINS_CORE_UI_GROUPS_SELECTGROUPS_DIALOG_CAPTION}}" : "{{i18n PLUGINS_CORE_UI_GROUPS_SELECTGROUP_DIALOG_CAPTION}}",
				   msg: "{{i18n PLUGINS_CORE_UI_GROUPS_SELECTGROUP_DIALOG_ERROR_EMPTY}}",
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.INFO
				});
			return;
		}

		this._box.hide();
		
		for (var i=0; i < selection.length; i++)
		{
			var opt = selection[i];
			addedgroups.push({
               id: opt.get('id'),
               groupDirectory: opt.get('groupDirectory'),
               label: opt.get('label'),
               groupDirectoryName: opt.get('groupDirectoryLabel')
            });
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
