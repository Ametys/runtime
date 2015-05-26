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
 * A panel to display a tool in the tools layout.
 */
Ext.define("Ametys.ui.tool.ToolPanel", { 
    extend: "Ext.panel.Panel",
    
    alias: 'widget.ametys.toolpanel',
    
    statics:
    {
        /**
         * @readonly
         * @property {Number} TOOLTYPE_0 Constant for a kind of tool #cfg-type.
         */
        TOOLTYPE_0: 0,
        /**
         * @readonly
         * @property {Number} TOOLTYPE_10 Constant for a kind of tool #cfg-type.
         */
        TOOLTYPE_10: 10,
        /**
         * @readonly
         * @property {Number} TOOLTYPE_20 Constant for a kind of tool #cfg-type.
         */
        TOOLTYPE_20: 20,
        /**
         * @readonly
         * @property {Number} TOOLTYPE_30 Constant for a kind of tool #cfg-type.
         */
        TOOLTYPE_30: 30
    },
        
    config: {
        /** @cfg {String} The description of the tool. Can be a HTML string. null if no description is available. */
        description: null,
        
        /** @cfg {Boolean}  dirtyState=false The dirty state of the tool. Are they any running modifications? */
        dirtyState: false,
        
        /** @cfg {String} smallIcon The path to the small icon. Size depends on theme but is usually a 16x16 icon. */
        smallIcon: null,
        /** @cfg {String} mediumIcon The path to the small icon. Size depends on theme but is usually a 32x32 icon. */
        mediumIcon: null,
        /** @cfg {String} largeIcon The path to the small icon. Size depends on theme but is usually a 48x48 icon. */
        largeIcon: null,
        
        /** @cfg {String} helpId The help identifier. */
        helpId: null
    },
    
    /** 
     * @cfg {Number} type=TOOLTYPE_0 The type of tool. Have to be one of the TOOLTUPE_ constants. A tool type may have a different rendering look and also an adapted policy (tools of same kind can be grouped together automatically) 
     */
    type: 0,
    
    /**
     * Get the type of the tool
     * @return {Number} The type value configured at #cfg-type.
     */
    getType: function()
    {
        return this.type;
    },
    
    /**
     * @event toolopen Fired when the tool is opened.
     * @param {Ametys.ui.tool.ToolPanel} this
     */
    /**
     * @event toolclose Fired when the tool is closed.
     * @param {Ametys.ui.tool.ToolPanel} this
     * @param {Boolean} wasFocused Was the tool focused?
     */
    /**
     * @event toolfocus Fired when the tool is receiving the UI focus.
     * @param {Ametys.ui.tool.ToolPanel} this
     */
    /**
     * @event toolblur Fired when the tool is loosing the UI focus.
     * @param {Ametys.ui.tool.ToolPanel} this
     */
    /**
     * @event toolactivate Fired when the tool is activated (visible in its zone).
     * @param {Ametys.ui.tool.ToolPanel} this
     */
    /**
     * @event tooldeactivate Fired when the tool is deactivated (invisible in its zone).
     * @param {Ametys.ui.tool.ToolPanel} this
     */
    /**
     * @event toolmoved Fired when the tool is moved from a zone to another.
     * @param {Ametys.ui.tool.ToolPanel} this
     * @param {String} newLocation The location of the new zone
     */
    
    /**
     * @property {Ametys.ui.tool.ToolsLayout} _toolsLayout The parent layout.
     */
    
    constructor: function(config)
    {
        // Let's avoid to call info changing too often
        this._infoChanging = Ext.Function.createThrottled(this._infoChanging, 10);
        
        this.callParent(arguments);
    },
    
    applyTitle: function(title)
    {
        this._infoChanging();
        return this.callParent(arguments);
    },
    
    applyDescription: function(description)
    {
        this._infoChanging();
        return description;
    },
    
    applyDirtyState: function(dirtyState)
    {
        this._infoChanging();
        return dirtyState;
    },
    
    applySmallIcon: function(smallIcon)
    {
        this._infoChanging();
        return smallIcon;
    },
    
    applyMediumIcon: function(mediumIcon)
    {
        this._infoChanging();
        return mediumIcon;
    },
    
    applyLargeIcon: function(largeIcon)
    {
        this._infoChanging();
        return largeIcon;
    },
    
    /**
     * @private
     * Called when infos are about to change.
     * This method is throttled so effect will not be applied immediately to avoid overload when called many times in a row
     */
    _infoChanging: function()
    {
        if (this._toolsLayout != null)
        {
            this._toolsLayout.onToolInfoChanged(this);
        }
    }
});
