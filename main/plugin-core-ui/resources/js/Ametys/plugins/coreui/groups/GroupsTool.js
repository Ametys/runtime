/*
 *  Copyright 2016 Anyware Services
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
Ext.define('Ametys.plugins.coreui.groups.GroupsTool', {
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
	 * @property {String} [_userTargetId=user] The target for users
	 * @private
	 */
	/**
	 * @property {String} [_groupTargetId=group] The target for groups
	 * @private
	 */
    /**
     * @property {String[]} _contexts The contexts for the group directories to display in the combobox.
     * @private
     */
    /**
     * @property {Boolean} _enableAllDirectoriesOption True to add an option in the group directories combobox for searching over all the directories.
     * @private
     */
    /**
     * @property {Boolean} _allDirectoriesOptionId The id of the 'all directories' options.
     * @private
     * @readonly
     */
    _allDirectoriesOptionId: '#all',
	
	getMBSelectionInteraction: function() 
	{
		return Ametys.tool.Tool.MB_TYPE_ACTIVE;
	},
    
    /**
     * @inheritdoc
     * @param {String} [params.contexts] The contexts for the group directories to display in the combobox. Default to the current contexts.
     * @param {Boolean/String} [params.enableAllDirectoriesOption=false] True to add an option in the group directories combobox for searching over all the directories.
     */
    setParams: function(params)
    {
        this.callParent(arguments);
        
        this._contexts = Ext.Array.from(params.contexts || Ametys.getAppParameter('populationContexts'));
        this._enableAllDirectoriesOption = Ext.isBoolean(params.enableAllDirectoriesOption) ? params.enableAllDirectoriesOption : params.enableAllDirectoriesOption == "true";
        this._loadDirectories();
    },
	
	constructor: function(config)
	{
		this.callParent(arguments);
		
		// Set the role and the message target type
		this._groupTargetId = config['group-target-id'] || Ametys.message.MessageTarget.GROUP;
		this._userTargetId = config['user-target-id'] || Ametys.message.MessageTarget.USER;
		
		// Bus messages listeners
		Ametys.message.MessageBus.on(Ametys.message.Message.CREATED, this._onMessageCreated, this);
		Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, this._onMessageEdited, this);
		Ametys.message.MessageBus.on(Ametys.message.Message.DELETED, this._onMessageDeleted, this);
	},
	
	createPanel: function()
	{
        this._groupDirectoriesField = Ext.create('Ext.form.field.ComboBox', this._getGroupDirectoryComboboxCfg());
        
		var groupStore = this._createGroupStore();
		this._groupGrid = Ext.create('Ext.grid.Panel', {
			scrollable: true,
			
			stateful: true,
			stateId: this.self.getName() + "$grid",
			
			style: {
                borderRightStyle: 'solid',  
                borderRightWidth: '1px'  
            },
			
			flex: 0.4,
            
			store: groupStore,
			columns: [
                {header: "{{i18n PLUGINS_CORE_UI_TOOL_GROUPS_LABEL}}", width: 150, dataIndex: 'label', renderer: this._renderGroupName, hideable: false},
                {header: "{{i18n PLUGINS_CORE_UI_TOOL_GROUPS_ID}}", width: 100, dataIndex: 'groupId', hideable: false},
                {header: "{{i18n PLUGINS_CORE_UI_TOOL_GROUPS_DIRECTORY}}", flex: 1, dataIndex: 'groupDirectoryLabel'}
            ],
			
			listeners: {
				selectionchange: {fn: this._onGroupSelectionChange, scope: this}
			},
			
            viewConfig: {
                plugins: {
                    ptype: 'ametysgridviewdragdrop',
                    setAmetysDragInfos: Ext.emptyFn,
                    setAmetysDropZoneInfos: Ext.bind(this.getGroupDropInfo, this) 
                }
            }
		});
		
		var userStore = this._createUserStore();
		this._userGrid = Ext.create('Ext.grid.Panel', {
			scrollable: true,
			
			flex: 1,
			minWidth: 350,
			style: {
                borderLeftStyle: 'solid',  
                borderLeftWidth: '1px'  
            },
			split: true,
			
			store: userStore,
			selModel: {
				mode: 'MULTI'
			},
			
			columns: [
                {header: "{{i18n PLUGINS_CORE_UI_TOOL_GROUPS_USER_GRID_NAME}}", width: 250, dataIndex: 'displayName', renderer: Ext.bind(this._renderUserName, this), hideable: false, sortable: true},
	            {header: "{{i18n PLUGINS_CORE_UI_TOOL_GROUPS_USER_GRID_LOGIN}}", width: 200, sortable: true, dataIndex: 'login'},
	            {header: "{{i18n PLUGINS_CORE_UI_TOOL_GROUPS_USER_GRID_POPULATION}}", width: 200, sortable: true, dataIndex: 'populationLabel'},
	            {header: "{{i18n PLUGINS_CORE_UI_TOOL_GROUPS_USER_GRID_EMAIl}}", flex: 1, sortable: true, dataIndex: 'email'}
            ],
			
			listeners: {
				selectionchange: {fn: this._onUserSelectionChange, scope: this}
			},
			
            viewConfig: {
                plugins: {
                    ptype: 'ametysgridviewdragdrop',
                    dragTextField: 'login',
                    setAmetysDragInfos: Ext.bind(this.getUserDragInfo, this),
                    setAmetysDropZoneInfos: Ext.bind(this.getUserDropInfo, this) 
                }
            }
		});
		
		return Ext.create('Ext.panel.Panel', {
			layout: {
				type: 'hbox',
				pack: 'start',
				align: 'stretch'
			},
			scrollable: 'horizontal',
			items: [this._groupGrid, this._userGrid],
            
            dockedItems: [{
                xtype: 'toolbar',
                layout: { 
                    type: 'hbox',
                    align: 'stretch'
                },
                dock: 'top',
                items: [this._groupDirectoriesField]
            }]
		});
	},
	
	sendCurrentSelection: function()
	{
		var targets = [];
		
		var users = this._userGrid.getSelectionModel().getSelection();
		var userTargets = [];
		
		var me = this;
		Ext.Array.forEach(users, function(user) {
			userTarget = Ext.create('Ametys.message.MessageTarget', {
				id: me._userTargetId,
				parameters: {
                    id: user.get('login'),
                    population: user.get('population')
                }
			});
			
			userTargets.push(userTarget);
		});
		
		
		var group = this._groupGrid.getSelectionModel().getSelection()[0];
		if (group)
		{
			var groupTarget = {
				id: this._groupTargetId,
				parameters: {
					id: group.get('groupId'),
                    groupDirectory: group.get('groupDirectory')
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
     * @private
     * Get the configuration object for creating the combobox for the group directories
     * @return {Object} The configuration
     */
    _getGroupDirectoryComboboxCfg: function()
    {
        return {
            xtype: 'combobox',
            fieldLabel: "{{i18n PLUGINS_CORE_UI_TOOL_GROUPS_GROUP_DIRECTORY_COMBOBOX}}",
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
        };
    },
    
    /**
     * @private
     * Load the groups when the current group directory has changed 
     */
    _onGroupDirectoryChange: function()
    {
        this._groupGrid.getStore().load();
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
            contexts: this._contexts
        }));
    },
    
    /**
     * Listener invoked after loading group directories
     * @param {Ext.data.Store} store The store
     * @param {Ext.data.Model[]} records The records of the store
     * @private
     */
    _onLoadDirectories: function(store, records)
    {
        if (this._enableAllDirectoriesOption)
        {
            // Add an option in the directories combobox for searching over all the directories
            store.add({
                id: this._allDirectoriesOptionId,
                label: "{{i18n PLUGINS_CORE_UI_TOOL_GROUPS_GROUP_DIRECTORY_COMBOBOX_OPTION_ALL}}"
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
	 * @private
	 * Create the group store that the grid should use as its data source.
	 * @return {Ext.data.Store} The created store
	 */
	_createGroupStore: function ()
	{
		// Merge default store configuration with inherited configuration (provided by #getStoreConfig).
		var storeConfig = Ext.merge({
			remoteSort: false,
			autoLoad: false,
			
			model: 'Ametys.plugins.coreui.groups.GroupsTool.Group',
			proxy: {
				type: 'ametys',
				reader: {
					type: 'json',
					rootProperty: 'groups'
				}
			},
			
			sortOnLoad: true,
			sorters: [{property: 'label', direction:'ASC'}],
            
            listeners: {
                beforeload: {fn: this._onBeforeLoadGroups, scope: this}
            }
			
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
			
			model: 'Ametys.plugins.coreui.groups.GroupsTool.UserGroup',
			proxy: {
				type: 'ametys',
				reader: {
					type: 'json',
					rootProperty: 'users'
				}
			},
			
			sortOnLoad: true,
			sorters: [{property: 'displayName', direction:'ASC'}],
			
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
					limit: null // No pagination
				}
			}
		};
	},
	
	/**
	 * Create the target of the drop operation relation.
	 * @param {Ext.data.Model[]} groups The target group of the drop operation.
	 * @param {Object} item The default drag data that will be transmitted. You have to add a 'target' item in it: 
	 * @param {Object} item.target The target (in the relation way) of the drop operation. A Ametys.relation.RelationPoint config. 
	 */	
	getGroupDropInfo: function(groups, item)
	{
		if (groups.length > 0)
		{
			var me = this;
			
			item.target = {
				relationTypes: [Ametys.relation.Relation.REFERENCE], 
				targets: {
					type: me._groupTargetId,
					parameters: { 
						id: groups[0].get('groupId'),
                        groupDirectory: groups[0].get('groupDirectory')
					}
				}
			};
		}
	},
	
    /**
     * @private
     * Add the 'source' of the drag.
     * @param {Object} item The default drag data that will be transmitted. You have to add a 'source' item in it: 
     * @param {Ametys.relation.RelationPoint} item.source The source (in the relation way) of the drag operation. 
     */
    getUserDragInfo: function(item)
    {
        var targets = [];
        
        Ext.Array.each(item.records, function(record) {
        	targets.push({
        		type: this._userTargetId,
        		parameters: {
        			id: record.get('login'),
                    population: record.get('population')
        		}
        	});
        }, this);
    
        if (targets.length > 0)
        {
            item.source = {
                relationTypes: [Ametys.relation.Relation.REFERENCE], 
                targets: targets
            };
        }
    },
	
	/**
	 * Create the target of the drop operation relation.
	 * @param {Object} target The target of the drop operation.
	 * @param {Object} item The default drag data that will be transmitted. You have to add a 'target' item in it: 
	 * @param {Object} item.target The target (in the relation way) of the drop operation. A Ametys.relation.RelationPoint config. 
	 */	
	getUserDropInfo: function(target, item)
	{
		var selectedGroup = this._groupGrid.getSelectionModel().getSelection()[0];
		if (selectedGroup)
		{
			var me = this;
			item.target = {
				relationTypes: [Ametys.relation.Relation.REFERENCE], 
				targets: {
					type: me._groupTargetId,
					parameters: { 
						id: selectedGroup.get('groupId'),
                        groupDirectory: selectedGroup.get('groupDirectory')
					}
				}
			};
		}
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
		return '<span class="a-grid-glyph ametysicon-multiple25"></span>' + value;
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
	    if (this._userTargetId == Ametys.message.MessageTarget.USER)
        {
	        return '<img src="' + Ametys.getPluginDirectPrefix('core-ui') + '/user/' + record.get('population') + '/' + record.get('login') + '/image_16" class="a-grid-icon a-grid-icon-user"/>' + value;
        }
	    else
        {
	        return '<img src="' + Ametys.getPluginDirectPrefix('core-ui') + '/user/default-image_16" class="a-grid-icon a-grid-icon-user"/>' + value;
        }
	},
    
    /**
     * Gets the id of the group directory selected in the group directory combobox of this tool.
     * @return {Number} The id of the group directory selected in the group directory combobox
     */
    getDirectoryComboValue: function()
    {
        return this._groupDirectoriesField.getValue();
    },
    
    /**
     * Function called before loading the group store
     * @param {Ext.data.Store} store The store
     * @param {Ext.data.operation.Operation} operation The object that will be passed to the Proxy to load the store
     * @private
     */
    _onBeforeLoadGroups: function(store, operation)
    {
        // If the directory combobox value is invalid, cancel the loading
        if (this.getDirectoryComboValue() == null)
        {
            return false;
        }
        
        // 'all' option is selected
        if (this.getDirectoryComboValue() == this._allDirectoriesOptionId)
        {
            operation.setParams( Ext.apply(operation.getParams() || {}, {
                contexts: this._contexts
            }));
            return true;
        }
        
        operation.setParams( Ext.apply(operation.getParams() || {}, {
            groupDirectoryId: this.getDirectoryComboValue()
        }));
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
		
		if (!operation.getParams().groupID || !operation.getParams().groupDirectoryId)
		{
			var group = this._groupGrid.getSelectionModel().getSelection()[0];
			if (group)
			{
				operation.setParams(Ext.apply(operation.getParams(), {
                    groupDirectoryId: group.get('groupDirectory'),
					groupID: group.get('groupId')
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
					groupID: group.get('groupId')
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
	 * Listener when a Ametys.message.Message#CREATED message was received
	 * @param {Ametys.message.Message} message The received message
	 * @private
	 */
	_onMessageCreated: function(message)
	{
		var groupTarget = message.getTarget(new RegExp('^' + this._groupTargetId + '$'), 1);
		if (groupTarget)
		{
			var groupDirectoryId = groupTarget.getParameters().groupDirectory;
            if (this.getDirectoryComboValue() == this._allDirectoriesOptionId || this.getDirectoryComboValue() == groupDirectoryId)
            {
                // The group grid is concerned by the message
                this._groupGrid.getStore().load();
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
            var groupDirectoryId = groupTarget.getParameters().groupDirectory;
            if (this.getDirectoryComboValue() == this._allDirectoriesOptionId || this.getDirectoryComboValue() == groupDirectoryId)
            {
				if (message.getParameters().major)
				{
					this._groupGrid.getStore().load();
				}
				else
				{
					// Reload users' group
					this._userGrid.getStore().load({
						params: {
							groupID: id
						}
					});
				}
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
            var groupId = target.getParameters().id;
            var groupDirectory = target.getParameters().groupDirectory;
            var group = this._findGroupRecord(groupId, groupDirectory);
			if (group)
			{
				store.remove(group);
			}
		}, this);
	},
    
    /**
     * @private
     * Gets the first record from the group store that matches the given group id and directory id.
     * @param {String} groupId The group id
     * @param {String} groupDirectory The group directory id
     * @return {Ext.data.Model} the found record, or null if not found.
     */
    _findGroupRecord: function(groupId, groupDirectory)
    {
        var foundRecord;
        
        Ext.Array.each(this._groupGrid.getStore().getRange(), function(record) {
            if (record.get('groupId') == groupId && record.get('groupDirectory') == groupDirectory)
            {
                foundRecord = record;
                return false;
            }
        }, this);
        
        return foundRecord;
    }
});
