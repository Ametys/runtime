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
Ext.define('Ametys.plugins.coreui.users.UsersTool', {
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
     * See {@link Ametys.plugins.cms.user.UsersTool.User}
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
     * @property {String} _userTargetId the message target type for users
     * @private
     */
    /**
     * @property {String[]} _contexts The contexts for the populations to display in the combobox.
     * @private
     */
    /**
     * @property {Boolean} _enableAllPopulationsOption True to add an option in the populations combobox for searching over all the populations.
     * @private
     */
    /**
     * @property {String} _allPopulationsOptionId The id of the 'all populations' options.
     * @private
     * @readonly
     */
    _allPopulationsOptionId: '#all',
    /**
     * @property {Boolean} _showDirectoryCombobox True to show the user directory combobox field
     * @private
     */
    /**
     * @property {Boolean} _showDirectoryColumn True to show the user directory column
     * @private
     */

    constructor: function(config)
    {
        this.callParent(arguments);
        
        // Set the role and the message target type
        this._userTargetId = config['message-target-id'] || Ametys.message.MessageTarget.USER;
        
        // Listening to some bus messages.
        Ametys.message.MessageBus.on(Ametys.message.Message.CREATED, this._onMessageCreated, this);
        Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, this._onMessageEdited, this);
        Ametys.message.MessageBus.on(Ametys.message.Message.DELETED, this._onMessageDeleted, this);
    },
    
    getMBSelectionInteraction: function() 
    {
        return Ametys.tool.Tool.MB_TYPE_ACTIVE;
    },
    
    /**
     * @inheritdoc
     * @param {Object[]} [params.selectedUsers] The users to initially select
     * @param {String[]} [params.contexts] The contexts for the populations to display in the combobox. Default to the current contexts.
     * @param {Boolean/String} [params.enableAllPopulationsOption=false] True to add an option in the populations combobox for searching over all the populations.
     * @param {Boolean/String} [params.showDirectoryColumn=false] True to show the user directory column
     * @param {Boolean/String} [params.showDirectoryCombobox=false] True to show the user directory combobox field
     */
    setParams: function(params)
    {
        this.callParent(arguments);
        
        this._initialSelectedUsers = params.selectedUsers || [];
        this._contexts = Ext.Array.from(params.contexts || Ametys.getAppParameter('populationContexts'));
        this._enableAllPopulationsOption = Ext.isBoolean(params.enableAllPopulationsOption) ? params.enableAllPopulationsOption : params.enableAllPopulationsOption == "true";
        this._showDirectoryColumn = Ext.isBoolean(params.showDirectoryColumn) ? params.showDirectoryColumn : params.showDirectoryColumn == "true";
        this._grid.down('[dataIndex=directory]').setVisible(this._showDirectoryColumn);
        this._showDirectoryCombobox = Ext.isBoolean(params.showDirectoryCombobox) ? params.showDirectoryCombobox : params.showDirectoryCombobox == "true";
        
        this.showOutOfDate();
    },
    
    refresh: function()
    {
        this._userDirectoriesField.setVisible(this._showDirectoryCombobox);
        
        this.showRefreshing();
        this._loadPopulations(Ext.bind(this.showRefreshed, this));
    },
    
    createPanel: function()
    {
        this._userPopulationsField = Ext.create('Ext.form.field.ComboBox', this._getPopulationComboboxCfg());
        this._userDirectoriesField = Ext.create('Ext.form.field.ComboBox', this._getUserDirectoryComboboxCfg());
        // Search input
        this._searchField = Ext.create('Ext.form.TextField', {
            xtype: 'textfield',
            cls: 'ametys',
            maxWidth: 250,
            flex: 1,
            emptyText: "{{i18n PLUGINS_CORE_UI_TOOL_USERS_SEARCH_EMPTY_TEXT}}",
            listeners: {change: Ext.Function.createBuffered(this._search, 500, this)}
        });
        
        this._store = this.createUserStore();
        
        this._grid = Ext.create("Ext.grid.Panel", {
            store: this._store,
            scrollable: true,
            
            columns: [
                {header: "{{i18n PLUGINS_CORE_UI_TOOL_USERS_COL_NAME}}", width: 250, sortable: true, dataIndex: 'displayName', renderer: Ext.bind(this._renderDisplayName, this), hideable: false},
                {header: "{{i18n PLUGINS_CORE_UI_TOOL_USERS_COL_LOGIN}}", width: 200, sortable: true, dataIndex: 'login'},
                {header: "{{i18n PLUGINS_CORE_UI_TOOL_USERS_COL_POPULATION}}", width: 200, sortable: true, dataIndex: 'populationLabel'},
                {header: "{{i18n PLUGINS_CORE_UI_TOOL_USERS_COL_EMAIL}}", flex: 1, sortable: true, dataIndex: 'email'},
                {header: "{{i18n PLUGINS_CORE_UI_TOOL_USERS_COL_DIRECTORY}}", width: 300, sortable: true, dataIndex: 'directory', hidden: true} // hidden by default, will be shown if told
            ],
            
            selModel : {
                mode: 'MULTI'
            },
            
            dockedItems: [
                {
                    xtype: 'toolbar',
                    layout: { 
                        type: 'hbox',
                        align: 'stretch'
                    },
                    dock: 'top',
                    items: [
                        this._userPopulationsField, 
                        this._userDirectoriesField
                    ],
                    
                    listeners: {
                        'resize': function(toolbar, width, height) {
                            if (width > 450)
                            {
                                this._userPopulationsField.setHideLabel(false);
                                this._userDirectoriesField.setHideLabel(false);
                                this._userPopulationsField.setMaxWidth(this._userPopulationsField.getInitialConfig('maxWidth'));
                                this._userDirectoriesField.setMaxWidth(this._userDirectoriesField.getInitialConfig('maxWidth'));
                                
                                toolbar.getLayout().setVertical(false);
                            }
                            else if (width > 300)
                            {
                                this._userPopulationsField.setHideLabel(false);
                                this._userDirectoriesField.setHideLabel(false);
                                this._userPopulationsField.setMaxWidth(null);
                                this._userDirectoriesField.setMaxWidth(null);
                                
                                toolbar.getLayout().setVertical(true);
                            }
                            else
                            {
                                this._userPopulationsField.setHideLabel(true);
                                this._userDirectoriesField.setHideLabel(true);
                                this._userPopulationsField.setMaxWidth(null);
                                this._userDirectoriesField.setMaxWidth(null);
                                
                                toolbar.getLayout().setVertical(true);
                            }
                        },
                        scope: this
                    }
                }, {
                    xtype: 'toolbar',
                    layout: { 
                        type: 'hbox',
                        align: 'stretch'
                    },
                    dock: 'top',
                    items: [
                        this._searchField,
                        {
                            flex: 1,
                            html: "<span title=\"{{i18n PLUGINS_CORE_UI_TOOL_USERS_SEARCH_LIMIT_HELPTEXT}}\">{{i18n PLUGINS_CORE_UI_TOOL_USERS_SEARCH_LIMIT_HELPTEXT}}</span>",
                            xtype: 'component',
                            cls: 'a-toolbar-text'
                        }
                    ],
                    
                    
                    listeners: {
                        'resize': function(toolbar, width, height) {
                            toolbar.getLayout().setVertical(width <= 200);
                        },
                        scope: this
                    }
                }
            ],
            
            listeners: {
                selectionchange: {fn: this._onSelectionChange, scope: this}
            },
            
            viewConfig: {
                plugins: {
                    ptype: 'ametysgridviewdragdrop',
                    dragTextField: 'login',
                    setAmetysDragInfos: Ext.bind(this.getDragInfo, this),
                    setAmetysDropZoneInfos: Ext.emptyFn 
                }
            }
        });
        
        return this._grid;
    },
    
    sendCurrentSelection: function()
    {
        var selection = this._grid.getSelectionModel().getSelection();
        var targets = [];
        
        var me = this;
        Ext.Array.forEach(selection, function(record) {
            targets.push({
                id: me._userTargetId,
                parameters: {
                    id: record.get('login'),
                    population: record.get('population')
                }
            })
        }, this);
        
        Ext.create('Ametys.message.Message', {
            type: Ametys.message.Message.SELECTION_CHANGED,
            targets: targets
        });
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
            
            model: 'Ametys.plugins.coreui.users.UsersTool.User',
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
                beforeload: {fn: this._onBeforeLoad, scope: this},
                load: {fn: this._onLoad, scope: this}
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
    
    /**
     * @private
     * Get the configuration object for creating the combobox for the user populations
     * @return {Object} The configuration
     */
    _getPopulationComboboxCfg: function()
    {
        return {
            xtype: 'combobox',
            fieldLabel: "{{i18n PLUGINS_CORE_UI_TOOL_USERS_POPULATION_FIELD}}",
            name: "userPopulations",
            cls: 'ametys',
            labelWidth: 75,
            maxWidth: 275,
            flex: 275,
            
            store: {
                fields: ['id', {name: 'label', sortType: Ext.data.SortTypes.asNonAccentedUCString}],
                proxy: {
                    type: 'ametys',
                    plugin: 'core-ui',
                    url: 'populations.json',
                    reader: {
                        type: 'json',
                        rootProperty: 'userPopulations'
                    }
                },
                sorters: [{property: 'label', direction: 'ASC'}],
                listeners: {
                    'beforeload': {fn: this._onBeforeLoadPopulations, scope: this},
                    'load': {fn: this._onLoadPopulations, scope: this}
                }
            },
            valueField: 'id',
            displayField: 'label',
            queryMode: 'local',
            forceSelection: true,
            triggerAction: 'all',
            
            listeners: {
                'change': {fn: this._onChangePopulation, scope: this}
            }
        };
    },
    
    /**
     * @private
     * Get the configuration object for creating the combobox for the user directories
     * @return {Object} The configuration
     */
    _getUserDirectoryComboboxCfg: function()
    {
        return {
            fieldLabel: "{{i18n PLUGINS_CORE_UI_TOOL_USERS_USER_DIRECTORY_FIELD}}",
            name: "userDirectories",
            cls: 'ametys',
            labelWidth: 150,
            maxWidth: 350,
            flex: 350,
            hidden: true, // hidden by default, will be shown if told
            
            store: {
                fields: ['id', {name: 'label', sortType: Ext.data.SortTypes.asNonAccentedUCString}],
                data: [],
                sorters: [{property: 'label', direction: 'ASC'}],
                listeners: {
                    'datachanged': Ext.bind(function(store) {
                        this._userDirectoriesField.clearValue();
                        this._userDirectoriesField.setValue("-");
                    }, this)
                }
            },
            valueField: 'id',
            displayField: 'label',
            queryMode: 'local',
            forceSelection: true,
            triggerAction: 'all',
            
            listeners: {
                'change': Ext.Function.createBuffered(this._search, 500, this)
            }
        };
    },
    
    /**
     * @private
     * Add the 'source' of the drag.
     * @param {Object} item The default drag data that will be transmitted. You have to add a 'source' item in it: 
     * @param {Ametys.relation.RelationPoint} item.source The source (in the relation way) of the drag operation. 
     */
    getDragInfo: function(item)
    {
        var targets = [];
        
        Ext.Array.each(item.records, function(record) {
            targets.push({
                id: this._userTargetId,
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
     * Get the user store
     * @return {Ext.data.Store} The user store
     */
    getStore: function ()
    {
        return this._store;
    },
    
    /**
     * Function called before loading the population store
     * @param {Ext.data.Store} store The store
     * @param {Ext.data.operation.Operation} operation The object that will be passed to the Proxy to load the store
     * @private
     */
    _onBeforeLoadPopulations: function(store, operation)
    {
        operation.setParams( Ext.apply(operation.getParams() || {}, {
            contexts: this._contexts
        }));
    },
    
    /**
     * @private
     * Listener invoked after loading populations
     * @param {Ext.data.Store} store The store
     * @param {Ext.data.Model[]} records The records of the store
     */
    _onLoadPopulations: function(store, records)
    {
        if (this._enableAllPopulationsOption)
        {
            // Add an option in the populations combobox for searching over all the populations
            store.add({
                id: this._allPopulationsOptionId,
                label: "{{i18n PLUGINS_CORE_UI_TOOL_USERS_POPULATION_FIELD_OPTION_ALL}}"
            });
        }
    },
    
    /**
     * @private
     * Function called when the value of the population combobox field changed.
     * @param {Ext.form.field.ComboBox} combo The combobox
     * @param {String} newValue The new value
     * @param {String} oldValue The original value
     */
    _onChangePopulation: function(combo, newValue, oldValue)
    {
        if (newValue == this._allPopulationsOptionId)
        {
            // Search over all the populations
            this._userDirectoriesField.setDisabled(true);
            Ext.defer(this._search, 500, this);
            return;
        }
        else
        {
            this._userDirectoriesField.setDisabled(false);
        }
        
        // Populate the user directories combobox store
        var data = [{
            id: '-',
            label: "{{i18n PLUGINS_CORE_UI_TOOL_USERS_USER_DIRECTORY_FIELD_OPTION_ALL}}"
        }];
        var record = combo.getStore().getById(newValue);
        if (record != null)
        {
            Ext.Array.forEach(record.get('userDirectories'), function(item, index) {
                data.push({
                    id: item.id,
                    label: item.label
                });
            }, this);
        }
        this._userDirectoriesField.getStore().loadData(data, false);
    },
    
    /**
     * @private
     * Load the store of the populations combobox.
     * @param {Function} [callback] The callback function
     */
    _loadPopulations: function(callback)
    {
        this._userPopulationsField.getStore().load({
            scope: this,
            callback: function(records) {
                // When store loaded, select the 'all' option if it is available AND there are more than one population
                if (this._enableAllPopulationsOption && records.length > 1)
                {
                    this._userPopulationsField.select(this._allPopulationsOptionId);
                }
                // Otherwise select the fist data
                else if (records.length > 0)
                {
                    this._userPopulationsField.select(records[0].get('id'));
                }
                // If there is one and only one population, hide the combobox
                var hide = records.length == 1;
                this._userPopulationsField.setHidden(hide);
                // What's more, if this population has one directory, hide the user directory combobox
                if (hide && this._userDirectoriesField.getStore().getRange().length == 2)
                {
                    this._userDirectoriesField.setHidden(true);
                }
                
                // Callback function
                if (Ext.isFunction(callback))
                {
                    callback();
                }
            }
        });
    },
    
    /**
     * @private
     * Load the user store
     */
    _search: function()
    {
        this._store.load();
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
     * Gets the id of the population selected in the user population combobox of this tool.
     * @return {String} The id of the population selected in the user population combobox
     */
    getPopulationComboValue: function()
    {
        return this._userPopulationsField.getValue();
    },
    
    /**
     * Gets the id of the user directory selected in the user directory combobox of this tool.
     * @return {String} The id of the user directory selected in the user directory combobox
     */
    getUserDirectoryComboValue: function()
    {
        return this._userDirectoriesField.getValue();
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
        // If one of the two comboboxes is invalid, cancel the loading
        if (this.getPopulationComboValue() == null 
            || this.getPopulationComboValue() != this._allPopulationsOptionId && this.getUserDirectoryComboValue() == null)
        {
            return false;
        }
        
        // 'all' option is selected
        if (this.getPopulationComboValue() == this._allPopulationsOptionId)
        {
            operation.setParams( Ext.apply(operation.getParams() || {}, {
                contexts: this._contexts,
                criteria: this._searchField.getValue(),
                limit: this.self.RESULT_LIMIT
            }));
            return true;
        }
        
        operation.setParams( Ext.apply(operation.getParams() || {}, {
            userPopulationId: this.getPopulationComboValue(),
            userDirectoryId: this.getUserDirectoryComboValue(),
            criteria: this._searchField.getValue(),
            limit: this.self.RESULT_LIMIT
        }));
    },
    
    /**
     * @private
     * Listener invoked after loading users
     * @param {Ext.data.Store} store The store
     * @param {Ext.data.Model[]} records The records of the store
     */
    _onLoad: function(store, records)
    {
        if (this._initialSelectedUsers.length > 0)
        {
            var records = [];
            var sm = this._grid.getSelectionModel();
            var store = this._grid.getStore();
            
            Ext.Array.each (this._initialSelectedUsers, function (login) {
                var id = store.find("id", login); 
                if (id != '-')
                {
                    records.push(store.getAt(id));
                }
            });
            
            sm.select(records);
            
            this._initialSelectedUsers = []; // reset
        }
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
        // Case creation of a population
        var populationTargets = message.getTargets(Ametys.message.MessageTarget.USER_POPULATION);
        if (populationTargets.length > 0)
        {
            this.showOutOfDate();
        }
        
        // Case creation of a user
        var userTarget = message.getTarget(new RegExp('^' + this._userTargetId + '$'), 1);
        if (userTarget)
        {
            var login = userTarget.getParameters().id;
            var population = userTarget.getParameters().population;
            if (this.getPopulationComboValue() == population || this.getPopulationComboValue() == this._allPopulationsOptionId)
            {
                // The tool is concerned by the message
                this._search();
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
        // Case edition of a population
        var populationTargets = message.getTargets(Ametys.message.MessageTarget.USER_POPULATION);
        if (populationTargets.length > 0)
        {
            this.showOutOfDate();
        }
        
        // Case edition of a user
        if (message != null && message.getParameters().major === true)
        {
            var userTarget = message.getTarget(new RegExp('^' + this._userTargetId + '$'), 1);
            if (userTarget)
            {
                var login = userTarget.getParameters().id;
                var population = userTarget.getParameters().population;
                if ((this.getPopulationComboValue() == population || this.getPopulationComboValue() == this._allPopulationsOptionId) && this._findUserRecord(login, population))
                {
                    // The tool is concerned by the message
                    this._search();
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
        // Case deletion of a population
        var populationTargets = message.getTargets(Ametys.message.MessageTarget.USER_POPULATION);
        if (populationTargets.length > 0)
        {
            this.showOutOfDate();
        }
        
        // Case deletion of a user
        var userTargets = message.getTargets(new RegExp('^' + this._userTargetId + '$'), 1);
        
        var me = this;
        Ext.Array.forEach(userTargets, function(target) {
            var user = me._findUserRecord(target.getParameters().id, target.getParameters().population);
            if (user && (target.getParameters().population == this.getPopulationComboValue() || this._allPopulationsOptionId == this.getPopulationComboValue()))
            {
                me._store.remove(user);
            }
        }, this);
    },
    
    /**
     * @private
     * Gets the first record from the user store that matches the given login and population id.
     * @param {String} login The user login
     * @param {String} population The population id
     * @return {Ext.data.Model} the found record, or null if not found.
     */
    _findUserRecord: function(login, population)
    {
        var foundRecord;
        
        Ext.Array.each(this._store.getRange(), function(record) {
            if (record.get('login') == login && record.get('population') == population)
            {
                foundRecord = record;
                return false;
            }
        }, this);
        
        return foundRecord;
    }
});
