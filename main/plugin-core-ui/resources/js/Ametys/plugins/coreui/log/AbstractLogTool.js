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
 * An abstract tool that defines a grid linked to a detail view. It can easily be extended to display logs by providing data to the grid and custom formating rules. 
 */
Ext.define('Ametys.plugins.coreui.log.AbstractLogTool', {
    extend: "Ametys.tool.Tool",
    
    /**
     * @cfg {Boolean} vertical-panel-layout=true Change the position of the view details. If true, the details are behing the grid, if false, they are on the right of the grid. 
     */
    
    /**
     * @cfg {String} noselect-detail-message The message displayed in the details view when no log is selected in the grid. 
     */
    
    /**
     * @property {Number} _scrollPos The current scroll position of the grid 
     * @private
     */
    
    constructor: function(config)
    {
        this.callParent(arguments);
        
        this.noselectDetailMessage = this.getInitialConfig("noselect-detail-message") || "{{i18n PLUGINS_CORE_UI_LOGTOOL_DEFAULT_DETAIL_MESSAGE}}";
        this.verticalPanelLayout = this.getInitialConfig("vertical-panel-layout") != null ? this.getInitialConfig("vertical-panel-layout") : true;
    },
    
    getMBSelectionInteraction: function()
    {
        return Ametys.tool.Tool.MB_TYPE_NOSELECTION;
    },
    
    /**
     * @protected
     * @template
     * Determine a row CSS class depending upon the record
     * @param {Ext.data.Model} record The record
     * @return {String} The CSS classname to apply
     */
    getRowClass: function(record)
    {
        return "";
    },
    
    createPanel: function()
    {
        this.grid = Ext.create("Ext.grid.Panel", {
            stateful: true,
            stateId: this.self.getName() + "$grid",
            store: this.getStore(),
            scrollable: true,
            border: true,
            flex: 0.5,
            minWidth: this.verticalPanelLayout ? 200 : 100,
            minHeight: this.verticalPanelLayout ? 100 : 200,
            cls: 'uitool-log',
            plugins: 'gridfilters',
            
            columns: this.getColumns(),
            
            dockedItems: this.getDockedItems(),
            
            bbar: {
                xtype: 'statusbar',
                statusAlign: 'right'
            },
            
            viewConfig: { 
                getRowClass: Ext.bind(this.getRowClass, this)
            }, 
            
            listeners: {
                'selectionchange': Ext.bind(this._onSelectLog, this) 
            }                
        });
        
        this.detailsComponent = Ext.create("Ext.Component", {
            stateful: true,
            stateId: this.self.getName() + "$details",
            scrollable: true,
            minWidth: this.verticalPanelLayout ? 200 : 100,
            minHeight: this.verticalPanelLayout ? 100 : 200,
            split: true,
            border: true,
            flex: 0.5,
            ui: 'panel',
            cls: 'a-panel-text',
            html: this.noselectDetailMessage
        });
        
         this.gridContainer = Ext.create("Ext.container.Container", {
            layout: { 
                type: this.verticalPanelLayout != "false" ? 'vbox' : 'hbox',
                align: 'stretch'
            },
            cls: 'uitool-logtool',
            items: [ this.grid, this.detailsComponent ]
        });
         
        this._updateStatusBar();
        this.grid.store.on("add", this._updateStatusBar, this);
        this.grid.store.on("add", this._loadScrollPosition, this);
        this.grid.store.on("remove", this._updateStatusBar, this);
        this.grid.store.on("filterchange", this._updateStatusBar, this);
        this.grid.store.on("clear", this._updateStatusBar, this);
        
        this.grid.on("viewready", this._onViewReady, this);
         
        return this.gridContainer;
    },
    
    /**
     * Create the initial store used by the log grid.
     * @return {Ext.data.Store/String/Object} store used by the log grid.
     */
    getStore: function()
    {
        throw "unimplemented method #getStore";
    },
    
    /**
     * Configure the columns of the log grid.
     * @return {Object[]} The columns configuration
     */
    getColumns: function()
    {
        throw "unimplemented method #getColumns";
    },
    
    /**
     * Configure the docked items of the log grid.
     * @return {Object[]} The docked items.
     */
    getDockedItems: function()
    {
        return [];
    },
    
    /**
     * Listener on the grid for the select change event. Update the details view.
     * @param {Ext.grid.Panel} panel The grid panel.
     * @param {Ext.data.Model[]} records The selected records.
     * @param {Object} eOpts Options passed when configuring the listener.
     * @private
     */
    _onSelectLog: function(panel, records, eOpts)
    {
        if (records.length > 0)
        {
            this.detailsComponent.update(this.getDetailsText(records));
        }
        else
        {
            this.detailsComponent.update(this.noselectDetailMessage);
        }
    },
    
    /**
     * Format and return the text displayed in the details for the current selected records.
     * @param {Ext.data.Model[]} records The selected records.
     * @return {String} The formated text to display.
     */
    getDetailsText: function(records)
    {
        throw "unimplemented method #getDetailsText";
    },
    
    /**
     * Listener on the store count update. Automatically update the status bar.
     * @private
     */
    _updateStatusBar: function()
    {
        var statusbar = this.grid.getDockedItems('statusbar[dock="bottom"]')[0];
        var count = this.grid.getStore().getCount();
        if (count > 0)
        {
            statusbar.setStatus(Ext.String.format("{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_GRID_STATUSBAR}}", count));
        }
        else
        {
            statusbar.setStatus("{{i18n PLUGINS_CORE_UI_TOOLS_SERVERLOGS_GRID_STATUSBAR_EMPTY}}");
        }
    },
    
    /**
     * Listener on the view ready. Add an interceptor to the scroll move event to save its position
     */
    _onViewReady: function(grid)
    {
        var onScrollMove = Ext.bind(this._saveScrollPosition, this);
        grid.view.onScrollMove = grid.view.onScrollMove ? Ext.Function.createInterceptor(grid.view.onScrollMove, onScrollMove, grid) : onScrollMove;
        
        // save the initial scroll position
        this._saveScrollPosition();
    },
    
    /**
     * This listener saves the current scroll position of the grid, to be able to restore it on store modifications.
     * @private
     */
    _saveScrollPosition: function()
    {
        if (this.grid.getView() == null || this.grid.getView().getEl() == null || this.grid.getView().getEl().down("*") == null)
        {
            this._scrollPos = null;
            return;
        }
        
        var gridEl = this.grid.getView().getEl();
        var tableEl = gridEl.down("*"); 
        
        if (gridEl.getScrollTop() == 0)
        {
            this._scrollPos = "top";
        }
        else if (Math.abs(tableEl.getBottom() - gridEl.getBottom()) <= 1)  
        {
            this._scrollPos = "bottom";
        }
        else
        {
            this._scrollPos = null;
        }
    },
    
    /**
     * Load the scroll position of the grid with the position returned by the {#saveScrollPosition}.
     * @private
     */
    _loadScrollPosition: function()
    {
        if (this._scrollPos == null)
        {
            return;
        }
        
        var gridEl = this.grid.getView().getEl();
        
        if (this._scrollPos == "top")
        {
            this.grid.getView().scrollTo(0, 0, false);
        }
        
        if (this._scrollPos == "bottom")
        {
            this.grid.getView().scrollBy(0, gridEl.down("*").getBottom(), false);
        }
    },
    
    onClose: function()
    {
        this.grid.store.un("add", this._updateStatusBar, this);
        this.grid.store.un("add", this._loadScrollPosition, this);
        this.grid.store.un("remove", this._updateStatusBar, this);
        this.grid.store.un("filterchange", this._updateStatusBar, this);
        this.grid.store.un("clear", this._updateStatusBar, this);
        
        this.callParent(arguments);
    }
    
});