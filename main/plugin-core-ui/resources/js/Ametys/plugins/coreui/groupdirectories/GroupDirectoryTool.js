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
 * Tool for displaying group directories of the application in a grid.
 * @private
 */
Ext.define('Ametys.plugins.coreui.groupdirectories.GroupDirectoryTool', {
    extend: "Ametys.tool.Tool",
    
    /**
     * @property {Ext.data.ArrayStore} _store The store with the directories
     * @private
     */
    
    /**
     * @property {Ext.grid.Panel} _grid The grid panel displaying the directories
     * @private
     */
    
    constructor: function(config)
    {
        this.callParent(arguments);
        
        Ametys.message.MessageBus.on(Ametys.message.Message.CREATED, this._onMessageCreatedOrModified, this);
        Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, this._onMessageCreatedOrModified, this);
        Ametys.message.MessageBus.on(Ametys.message.Message.DELETED, this._onMessageDeleted, this);
    },
    
    createPanel: function()
    {
        this._store = this._createStore();
        
        this._grid = Ext.create("Ext.grid.Panel", { 
            store: this._store,
            stateful: true,
            stateId: this.self.getName() + "$grid",
            
            columns: [
                 {stateId: 'grid-title', header: "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_TOOL_COLUMN_TITLE}}", flex: 1, sortable: true, dataIndex: 'label', renderer: this._renderLabel},
                 {stateId: 'grid-id', header: "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_TOOL_COLUMN_ID}}", width: 250, dataIndex: 'id'},
                 {stateId: 'grid-type', header: "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_TOOL_COLUMN_TYPE}}", width: 350, dataIndex: 'type'}
            ],
            
            listeners: {'selectionchange': Ext.bind(this.sendCurrentSelection, this)}
        });
        
        return this._grid;
    },
    
    /**
     * @private
     * Create the store for directories.
     * @return {Ext.data.Store} The store
     */
    _createStore: function ()
    {
        return Ext.create('Ext.data.Store', {
            autoDestroy: true,
            model: 'Ametys.plugins.coreui.groupdirectories.GroupDirectoryTool.DirectoryEntry',
            proxy: {
                type: 'ametys',
                plugin: 'core-ui',
                url: 'group-directories.json',
                reader: {
                    type: 'json',
                    rootProperty: 'groupDirectories'
                }
             },
             
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
        this._store.load({callback: this.showRefreshed, scope: this});
    },
    
    sendCurrentSelection: function()
    {
        var selection = this._grid.getSelection();
        
        var targets = [];
        Ext.Array.forEach(selection, function(directory) {
            targets.push({
                id: Ametys.message.MessageTarget.GROUP_DIRECTORY,
                parameters: {
                    id: directory.get('id')
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
     * Renderer for the group directory's label
     * @param {String} value the label
     * @param {Object} metadata A collection of metadata about the current cell
     * @param {Ext.data.Model} record The record for the current row
     * @return {String} the group directory's label with its icon
     */
    _renderLabel: function(value, metadata, record)
    {
        return '<span class="a-grid-glyph ametysicon-multiple25 decorator-ametysicon-agenda3"></span>' + value;
    },
    
    /**
     * Listener on creation or edition message.
     * @param {Ametys.message.Message} message The edition message.
     * @private
     */
    _onMessageCreatedOrModified: function(message)
    {
        var targets = message.getTargets(Ametys.message.MessageTarget.GROUP_DIRECTORY);
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
        var targets = message.getTargets(Ametys.message.MessageTarget.GROUP_DIRECTORY);
        Ext.Array.forEach(targets, function(target) {
            var record = this._store.getById(target.getParameters().id);
            this._grid.getSelectionModel().deselect([record]);
            this._store.remove(record);
        }, this);
    }
    
});

/**
 * This class is the model for entries in the grid of the GroupDirectory tool
 * @private
 */
Ext.define("Ametys.plugins.coreui.groupdirectories.GroupDirectoryTool.DirectoryEntry", {
    extend: 'Ext.data.Model',
    
    fields: [
        {name: 'id'},    
        {name: 'label', sortType: Ext.data.SortTypes.asNonAccentedUCString},
        {name: 'type', mapping: 'modelLabel', sortType: Ext.data.SortTypes.asNonAccentedUCString}
    ]
});

Ext.define("Ametys.message.GroupDirectoryMessageTarget",{
    override: "Ametys.message.MessageTarget",
    statics: 
    {
        /**
         * @member Ametys.message.MessageTarget
         * @readonly
         * @property {String} GROUP_DIRECTORY The target of the message is a group directory
         * @property {String} GROUP_DIRECTORY.id The id of the group directory
         */
        GROUP_DIRECTORY: "groupDirectory"
    }
});