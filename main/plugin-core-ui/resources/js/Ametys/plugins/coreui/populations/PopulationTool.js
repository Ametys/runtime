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
 * Tool for displaying users populations of the application in a grid.
 */
Ext.define('Ametys.plugins.coreui.populations.PopulationTool', {
    extend: "Ametys.tool.Tool",
    
    /**
     * @property {Ext.data.ArrayStore} _store The store with the populations
     * @private
     */
    
    /**
     * @property {Ext.grid.Panel} _grid The grid panel displaying the populations
     * @private
     */
    
    /**
     * @property {String} _selectionAfterRefresh If not null, the id of the population to select when the next refresh will be finished.
     * @private
     */
    
    constructor: function(config)
    {
        this.callParent(arguments);
        
        Ametys.message.MessageBus.on(Ametys.message.Message.CREATED, this._onMessageCreated, this);
        Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, this._onMessageModified, this);
        Ametys.message.MessageBus.on(Ametys.message.Message.DELETED, this._onMessageDeleted, this);
    },
    
    createPanel: function()
    {
        this._store = this._createStore();
        
        this._grid = Ext.create("Ext.grid.Panel", { 
            store: this._store,
            stateful: true,
            stateId: this.self.getName() + "$grid",
            
            // Grouping by enabled/disabled state
            features: [
                {
                    ftype: 'grouping',
                    enableGroupingMenu: false,
                    groupHeaderTpl: [
                        '{name:this.formatEnabled}', 
                        {
	                        formatEnabled: function(name) {
	                            return name == "true" ? "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_TOOL_GROUP_ENABLED_LABEL}}" : "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_TOOL_GROUP_DISABLED_LABEL}}"
	                        }
                        }
                    ]
                } 
            ],
            
            columns: [
                 {stateId: 'grid-title', header: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_TOOL_COLUMN_TITLE}}", flex: 1, sortable: true, dataIndex: 'label', renderer: this._renderLabel},
                 {stateId: 'grid-id', header: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_TOOL_COLUMN_ID}}", width: 200, dataIndex: 'id'},
                 {stateId: 'grid-user-directories', header: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_TOOL_COLUMN_USER_DIRECTORIES}}", width: 350, dataIndex: 'userDirectories', renderer: this._renderUserDirectories},
                 {stateId: 'grid-credential-providers', header: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_TOOL_COLUMN_CREDENTIAL_PROVIDERS}}", width: 350, dataIndex: 'credentialProviders', renderer: this._renderArray},
                 {stateId: 'grid-is-in-use', header: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_TOOL_COLUMN_IS_IN_USE}}", width: 80, dataIndex: 'isInUse', align: 'center', renderer: this._renderBoolean}
            ],
            
            listeners: {'selectionchange': Ext.bind(this.sendCurrentSelection, this)}
        });
        
        return this._grid;
    },
    
    /**
     * @private
     * Create the store for populations.
     * @return {Ext.data.Store} The store
     */
    _createStore: function ()
    {
        return Ext.create('Ext.data.Store', {
            autoDestroy: true,
            model: 'Ametys.plugins.coreui.populations.PopulationTool.PopulationEntry',
            proxy: {
                type: 'ametys',
                plugin: 'core-ui',
                url: 'populations.json',
                reader: {
                    type: 'json',
                    rootProperty: 'userPopulations'
                },
                extraParams: {
                    showDisabled: true
                }
             },
             
             groupField: 'enabled',
             sortOnLoad: true,
             sorters: [{property: 'label', direction:'ASC'}]
        });
    },
    
    getMBSelectionInteraction: function() 
    {
        return Ametys.tool.Tool.MB_TYPE_ACTIVE;
    },
    
    setParams: function (params)
    {
        this.callParent(arguments);
        this.showOutOfDate();
    },
    
    refresh: function ()
    {
        this.showRefreshing();
        
        function callback()
        {
            this.showRefreshed();
            var record = this._store.getById(this._selectionAfterRefresh);
            if (record != null)
            {
                this._grid.getSelectionModel().select([record]);
                this._selectionAfterRefresh = null;
            }
        };
        this._store.load({callback: callback, scope: this});
    },
    
    sendCurrentSelection: function()
    {
        var selection = this._grid.getSelection();
        
        var targets = [];
        Ext.Array.forEach(selection, function(population) {
            targets.push({
                id: Ametys.message.MessageTarget.USER_POPULATION,
                parameters: {
                    id: population.get('id'),
                    valid: population.get('valid')
                }
            });
        }, this);
        
        Ext.create('Ametys.message.Message', {
            type: Ametys.message.Message.SELECTION_CHANGED,
            targets: targets
        });
    },
    
    /**
     * @private
     * Renderer for the population's title
     * @param {String} value the title
     * @param {Object} metadata A collection of metadata about the current cell
     * @param {Ext.data.Model} record The record for the current row
     * @return {String} the html string used for the rendering of the url
     */
    _renderLabel: function(value, metadata, record)
    {
        var decorator = record.get('valid') ? '' : ' decorator-ametysicon-caution9 population-warning';
        return '<span class="a-grid-glyph ametysicon-agenda3' + decorator + '"></span>' + value;
    },
    
    /**
     * Renders an array.
     * @param {Array} value The data value for the current cell.
     * @return {String} The html representation
     * @private
     */
    _renderArray: function(value)
    {
        var result = "";
        Ext.Array.forEach(value, function(item, index) {
            if (index > 0)
            {
	            result += ", ";
            }
            result += item;
        }, this);
        
        return result;
    },
    
    /**
     * Renders an array of usersdirectory
     * @param {Array} value The data value for the current cell.
     * @return {String} The html representation
     * @private
     */
    _renderUserDirectories: function(value)
    {
        var result = "";
        Ext.Array.forEach(value, function(item, index) {
            if (index > 0)
            {
                result += ", ";
            }
            result += item.label;
        }, this);
        
        return result;
    },
    
    /**
     * Renders a boolean value.
     * @param {Boolean} value The data value for the current cell.
     * @return {String} The html representation
     * @private
     */
    _renderBoolean: function(value)
    {
        var isTrue = Ext.isBoolean(value) ? value : value == 'true';
        if (isTrue)
        {
            return '<span class="a-grid-glyph ametysicon-check34"/>';
        }
        else
        {
            return "";
        }
    },
    
    /**
     * Listener on creation message.
     * @param {Ametys.message.Message} message The edition message.
     * @private
     */
    _onMessageCreated: function(message)
    {
        var targets = message.getTargets(Ametys.message.MessageTarget.USER_POPULATION);
        if (targets.length > 0)
        {
            this._selectionAfterRefresh = targets[0].getParameters().id;
            this.showOutOfDate();
        }
    },
    
    /**
     * Listener on edition message.
     * @param {Ametys.message.Message} message The edition message.
     * @private
     */
    _onMessageModified: function(message)
    {
        var targets = message.getTargets(Ametys.message.MessageTarget.USER_POPULATION);
        if (targets.length > 0)
        {
            this.showOutOfDate();
        }
    },
    
    /**
     * Listener on deletion message.
     * @param {Ametys.message.Message} message The deletion message.
     * @private
     */
    _onMessageDeleted: function(message)
    {
        var targets = message.getTargets(Ametys.message.MessageTarget.USER_POPULATION);
        Ext.Array.forEach(targets, function(target) {
            var record = this._store.getById(target.getParameters().id);
            this._grid.getSelectionModel().deselect([record]);
            this._store.remove(record);
        }, this);
    }
    
});

/**
 * This class is the model for entries in the grid of the population tool
 * @private
 */
Ext.define("Ametys.plugins.coreui.populations.PopulationTool.PopulationEntry", {
    extend: 'Ext.data.Model',
    
    fields: [
        {name: 'id'},    
        {name: 'label', sortType: Ext.data.SortTypes.asNonAccentedUCString},
        {name: 'enabled'},
        {name: 'valid', type: 'boolean'},
        {name: 'isInUse'},
        {name: 'userDirectories'},
        {name: 'credentialProviders'}
    ]
});

Ext.define("Ametys.message.PopulationMessageTarget",{
    override: "Ametys.message.MessageTarget",
    statics: 
    {
        /**
         * @member Ametys.message.MessageTarget
         * @readonly
         * @property {String} USER_POPULATION The target of the message is a user populations
         * @property {String} USER_POPULATION.id The id of the user population
         * @property {Boolean} [USER_POPULATION.valid] true if the user population is valid
         */
        USER_POPULATION: "userPopulation"
    }
});