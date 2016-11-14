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
         * @cfg {String/Object} layout The layout to use
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
                    'show': this._changeTitlePositionOrWidth,
                    'hide': this._changeTitlePositionOrWidth,
                    'resize': this._changeTitlePositionOrWidth,
                    scope: this
                }
            });
        },
        
        /*afterComponentLayout: function(width, height, oldWidth, oldHeight) 
        {
            this.callParent(arguments);
            this._changeTitlePositionOrWidth();
        },*/
        
        /**
         * @private
         * This method will determine if title should be set at the left or at the right of this container ; and will also compute its width
         */
        _changeTitlePositionOrWidth: function()
        {
            var tabPanel = this.ownerCt.ownerCt.getPanel();
            var title = this.ownerCt.getTitle();
            var separator = this.ownerCt.getComponent('separator');
            
            // look for last non-contextual tab
            var lastNonContextualTabStrip = null;
            var lastTabStrip = null;
            for (var i = 0; i < tabPanel.items.length; i++)
            {
                var tabEl = tabPanel.getTabBar().items.get(i);
                if (tabEl.isVisible())
                {
                    lastTabStrip = tabEl;
                    
                    var panel = tabPanel.items.get(i); 
                    if (panel.contextualTab == null)
                    {
                        lastNonContextualTabStrip = tabEl;
                    }
                }
            }

            if (lastNonContextualTabStrip != lastTabStrip)
            {
                // The fixed width of title will be the right position of the last non-contextual tab - the left position of this title
                var globalBox = this.ownerCt.getBox(true);
                var availableLeftWidth = lastNonContextualTabStrip.getBox().right - (separator.previousSibling() ? separator.previousSibling().getPosition()[0] : globalBox.left);
                var availableRightWidth = (this.nextSibling() ? this.nextSibling().getBox().left : globalBox.right) - lastTabStrip.getBox().right;

                if (availableLeftWidth > availableRightWidth)
                {
                    this.ownerCt.moveBefore(title, this);

                    title.flex = undefined;
                    title.setWidth(availableLeftWidth);
                    separator.hide();
                }                
                else
                {
                    this.add(title);
                    
                    title.flex = 1;
                    title.setWidth(null);
                    separator.setWidth(availableLeftWidth);
                    separator.show();
                }
                this.show();
            }
            else
            {
                title.flex = 1;
                title.setWidth(null);
                separator.hide();
                this.ownerCt.moveBefore(title, this);
                this.hide();
            }
        }
    }
);
