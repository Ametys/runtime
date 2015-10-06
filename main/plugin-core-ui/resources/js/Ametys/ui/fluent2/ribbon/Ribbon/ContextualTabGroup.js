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
 * This class display the label of a group of contextual tabs
 * @private
 */
Ext.define(
    "Ametys.ui.fluent.ribbon.Ribbon.ContextualTabGroup",
    {
        extend: "Ext.Component",
        alias: 'widget.ametys.ribbon-contextualtabgroup',

        /**
         * @cfg ui=ribbon-header-contextualgroup @inheritdoc
         */
        ui: 'ribbon-header-contextualgroup',
        
        /**
         * @property {Ametys.ui.fluent.ribbon.Panel[]} _tabs The contextual panel associated to this group
         */
        
        constructor: function(config)
        {
            this._tabs = [];
            
            this.callParent(arguments);  
            
            this.on('click', this._onClick, this);
        },
        
        /**
         * Add a contextual panel to this group
         * @param {Ametys.ui.fluent.ribbon.Panel} panel The panel to add
         */
        associateContextualPanel: function(panel)
        {
            this._tabs.push(panel);
            
            var tabPanel = this._getTabPanel();
            var index = tabPanel.items.indexOf(panel);
            var tabEl = tabPanel.getTabBar().items.get(index);
            tabEl.on('show', this._onAssociatedTabVisibleChange, this);
            tabEl.on('hide', this._onAssociatedTabVisibleChange, this);
        },
        
        /**
         * Get the tab panel
         * @return {Ametys.ui.fluent.ribbon.TabPanel} The tab panel instance of the ribbon
         */
        _getTabPanel: function()
        {
            return this.ownerCt.ownerCt.ownerCt.getPanel();
        },
        
        /**
         * @private
         * Get the visible contextual tabs in the contextual group of the ribbon panel
         * @return {Ametys.ui.fluent.ribbon.Panel[]} The tab selection (not the strip panel) that are visible in the contextual group
         */
        _getVisibleContextualTabs: function()
        {
            var tabPanel = this._getTabPanel();
            
            var visibles = [];
            for (var i = 0; i < this._tabs.length; i++)
            {
                var tab = this._tabs[i];
                var index = tabPanel.items.indexOf(tab);
                var tabEl = tabPanel.getTabBar().items.get(index);
                if (tabEl.isVisible())
                {
                    visibles.push(tab);
                }
            }
            return visibles;
        },  

        /**
         * @private
         * Listener when clicking on the contextual tab group header
         */
        _onClick: function()
        {
            this._getTabPanel().setActiveTab(this._getVisibleContextualTabs()[0]);
        },
        
        /**
         * @private
         * Set the width of the contextual group to the sum of the width of the button of the tabs of the given ribbon panel (contextual group label)
         * Others ribbonPanels positions are modified
         */
        _onAssociatedTabVisibleChange: function()
        {
            var tabs = this._getVisibleContextualTabs();
            
            // Any tab visible ? show the group label and size it
            if (tabs.length > 0)
            {
                var tabPanel = this._getTabPanel();
                
                var sum = 0;
                for (var i = 0; i < tabs.length; i++)
                {
                    var panel = tabs[i];
                    var index = tabPanel.items.indexOf(panel);
                    var tabEl = tabPanel.getTabBar().items.get(index);

                    sum += tabEl.getWidth();
                }
            
                this.setWidth(sum + 2*(tabs.length - 1));
                this.ownerCt.show(); // this is required for the first call, because as ownerCt has never been rendered: its listeners are not active
                this.show();
            }
            // No contextual tab is visible: hide it
            else
            {
                this.hide();
            }
        }
    }
);
