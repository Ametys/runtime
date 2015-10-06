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
 * This class is the container of all "contextual tabs groups" in the ribbon header
 * @private
 */
Ext.define(
    "Ametys.ui.fluent.ribbon.Ribbon.ContextualTabGroupContainer",
    {
        extend: "Ext.container.Container",
        alias: 'widget.ametys.ribbon-contextualtabgroups',

        /** 
         * @cfg {String/Object} layout
         * @private
         */
        layout: 'hbox',
        
        /**
         * @property {String} contextualTabGroupPrefixCls The CSS classname to set on the button of group of contextual tabs as prefix for colors
         * @readonly
         * @private
         */
        contextualTabGroupPrefixCls: 'a-fluent-header-tabsgroup-',
        
        /**
         * Add a new (hidden) group in the header
         * @param {String} label The label of the group
         * @param {Number} color The color of the group to use (will be transmisted to CSS classname)
         * @returns {Ext.Component} The created hidden component
         */
        addGroup: function(label, color)
        {
            return this.add({
                xtype: 'ametys.ribbon-contextualtabgroup',
                
                cls: this.contextualTabGroupPrefixCls + color,

                html: label,
                hidden: true,
                listeners: {
                    'show': this._toggleVisibleState,
                    'hide': this._toggleVisibleState,
                    scope: this
                }
            });
        },

        /**
         * @private
         * On show, we have to change header title width
         */
        onShow: function()
        {
            this.callParent(arguments);
            
            var tabPanel = this.ownerCt.ownerCt.getPanel();
            var title = this.ownerCt.getTitle();
            
            // look for last non-contextual tab
            var lastNonContextualTabStrip = null;
            for (var i = 0; i < tabPanel.items.length; i++)
            {
                var panel = tabPanel.items.get(i); 
                if (panel.contextualTab == null)
                {
                    var tabEl = tabPanel.getTabBar().items.get(i);
                    if (tabEl.isVisible())
                    {
                        lastNonContextualTabStrip = tabEl;
                    }
                }
            }

            if (lastNonContextualTabStrip != null)
            {
                // The fixed width of title will be the right position of the last non-contextual tab - the left position of this title
                var computeWidth = lastNonContextualTabStrip.getBox().right - title.getPosition()[0];
                
                title.flex = undefined;
                title.setWidth(computeWidth);
            }            
        },
        
        /**
         * @private
         * On hide, we have to change header title width
         */
        onHide: function()
        {
            var title = this.ownerCt.getTitle();
            
            title.flex = 1;
            title.setWidth(null);
        },
        
        /**
         * @private
         * The contextual tab group container may need to be shown or hidden. Let's look at its children
         */
        _toggleVisibleState: function()
        {
            var allInvisibles = true;
            
            this.items.each(function(item) {
                return allInvisibles = !item.isVisible();
            });

            this.setVisible(!allInvisibles);
        },        
    }
);
