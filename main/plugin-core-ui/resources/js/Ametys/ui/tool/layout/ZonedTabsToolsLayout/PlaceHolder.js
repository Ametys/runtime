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
 * The place holder in the layout for the tabpanel
 * @private
 */
Ext.define("Ametys.ui.tool.layout.ZonedTabsToolsLayout.ZoneTabsToolsPanelPlaceHolder", 
    {
        extend: "Ext.Container",
        
        xtype: 'zonetabpanel-placeholder',
        ui: 'tool-placeholder',
        
        hidden: true,
        
        /**
         * @private
         * @property {Boolean} _panelCollapsed=false Is the panel collapsed (even if floating)?
         */
        _panelCollapsed: false,
        
        /**
         * @private
         * @property {String} collapsedCls The CSS class added to this component when it is collapsed
         */
        collapsedCls: "x-collapsed",
        
        /**
         * @private
         * @property {Number} _originialMinSize The min width or height before collapsing
         */
        /**
         * @private
         * @property {Number} _originialMaxSize The max width or height before collapsing
         */
        /**
         * @private
         * @property {Number} _originalFlex The flex value before collapsing 
         */
        
        constructor: function(config)
        {
            this.callParent([config]);
            
            this.on('beforeshow', this._onShow, this);

            this.on('resize', this._doLayoutPanel, this);
            this.on('afterlayout', this._doLayoutPanel, this);

            // For first show we cannot simply listen to show event, because is not fired since we -its parent- is not visible
            this.getPanel().show = Ext.Function.createSequence(Ext.bind(this.show, this), Ext.bind(this.getPanel().show, this.getPanel()));
            this.getPanel().hide = Ext.Function.createSequence(Ext.bind(this.hide, this), Ext.bind(this.getPanel().hide, this.getPanel()));
            
            this.getPanel().on('collapse', this._onCollapse, this);
            this.getPanel().on('expand', this._onExpand, this);
        },
        
        /**
         * @private
         * Get the corresponding splitter
         * @return {Ext.resizer.Splitter} The splitter
         */
        getSplitter: function()
        {
            if (this.getPanel().location == "b" || this.getPanel().location == "r")
            {
                return this.prev();
            }
            else
            {
                return this.next();
            }
        },
        
        /**
         * @private
         * Listener on underlying collapse to change size
         */
        _onCollapse: function()
        {
            if (!this.getPanel()._floating)
            {
                var size = this.getPanel().getHeader().getSize();
                if (this.minWidth)
                {
                    this._originialMinSize = this.minWidth; 
                    this._originialMaxSize = this.maxWidth; 
                    this.minWidth = size.width;
                    this.maxWidth = size.width;
                }
                if (this.minHeight)
                {
                    this._originialMinSize = this.minHeight; 
                    this._originialMaxSize = this.maxHeight; 
                    this.minHeight = size.height;
                    this.maxHeight = size.height;
                }
                this._originalFlex = this.flex;
                this.flex = 0;
                this._panelCollapsed = true;
                this.addCls(this.collapsedCls);
                this.getSplitter().addCls(this.collapsedCls);
                this.ownerCt.updateLayout();
            }
            else
            {
                this.getPanel()._floating = false;
                if (this.getPanel().collapseDirection == "right" || this.getPanel().collapseDirection == "bottom")
                {
                    this.getPanel().setPagePosition(this.getPosition());
                }
            }
        },
        
        /**
         * @private
         * Listener on underlying collapse to change size
         */
        _onExpand: function()
        {
            if (!this.getPanel()._floating)
            {
                if (this.minWidth)
                {
                    this.minWidth = this._originialMinSize;
                    this.maxWidth = this._originialMaxSize;
                }
                if (this.minHeight)
                {
                    this.minHeight = this._originialMinSize;
                    this.maxHeight = this._originialMaxSize;
                }
                this.flex = this._originalFlex;
                this._panelCollapsed = false;
                this.removeCls(this.collapsedCls);
                this.getSplitter().removeCls(this.collapsedCls);
                this.ownerCt.updateLayout();
            }
            else
            {
                if (this.getPanel().collapseDirection == "right" || this.getPanel().collapseDirection == "bottom")
                {
                    this.getPanel().alignTo(this, "br-br");
                }
            }
        },
        
        getState: function()
        {
            if (this._panelCollapsed)
            {
                this.flex = this._originalFlex;
            }
            
            var state = this.callParent();
            
            if (this._panelCollapsed)
            {
                this.flex = 0;
            }
            
            return state;
        },
        
        /**
         * @private
         * Adapt underlying panel size and position to the current wrapper
         */
        _doLayoutPanel: function()
        {
            if (this.getPanel()._floating)
            {
                // The panel is expanded but not the placeholder, we cannot resize it to the placeholder size
                
                // We compute the diff betweend headersize and placeholder size
                var size = this.getPanel().getHeader().getSize();
                var futureSize = this.getSize();
                var diffSize = { 
                    width : futureSize.width - size.width,
                    height : futureSize.height - size.height
                };
                
                // We apply this difference to the whole panel
                var currentPanelSize = this.getPanel().getSize();
                this.getPanel().setSize({
                    width: currentPanelSize.width + diffSize.width,
                    height: currentPanelSize.height + diffSize.height
                });
            }
            else
            {
                this.getPanel().setSize(this.getSize());
            }
            
            this.getPanel().setPagePosition(this.getPosition());
        },
        
        /**
         * Get the underlying panel
         * @return {Ametys.ui.tool.layout.ZonedTabsToolsLayout.ZoneTabsToolsPanel} The wrapped panel
         */
        getPanel: function()
        {
            return this.floatingItems.items[0];
        },
        
        /**
         * @private
         * Listener on show
         * @param {Ametys.ui.tool.layout.ZonedTabsToolsLayout.ZoneTabsToolsPanel} me This component
         */
        _onShow: function(me)
        {
            this._doLayoutPanel();
            
            if (!this.flex)
            {
                var brother;
                if (brother = this.nextSibling("zoned-container") || this.previousSibling("zoned-container"))
                {
                    this.flex = brother.flex / 3;
                }
            }
        }        
    }
);
        
        