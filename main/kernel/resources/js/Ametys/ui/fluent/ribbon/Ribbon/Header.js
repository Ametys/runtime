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
 * The header for the ribbon panel.
 * Always add the application title to the given title
 * @private
 */
Ext.define(
    "Ametys.ui.fluent.ribbon.Ribbon.Header",
    {
        extend: "Ext.panel.Header",
        alias: 'widget.ametys.ribbon-header',
        
        headerCls: 'x-fluent-tab-panel-header',
        height: 26,

        /**
         * @cfg {String} applicationTitle The application title. Cannot be changed after configuration. Can contains HTML tags.
         */
        
        /**
         * Reduce the width for the title
         * @param {String/Number} width The width in pixel or auto.
         */
        setTitleWidth: function(width)
        {
            if (width == "auto")
            {
                this.getTitle().flex = 1;
                this.getTitle().ownerCt.doLayout();
            }
            else
            {
                this.getTitle().flex = undefined;
                this.getTitle().setWidth(width);
            }
        },
        
        /**
         * Add a group label in the header
         * @param {String} label The label of the group
         * @param {Number} color The color to use (code between 1 and 6)
         * @returns {Ext.dom.Element} The created element (hidden)
         */
        addGroupLabel: function(label, color)
        {
            var ribbonPanel = this.getTitle().getEl().parent().createChild ({ 
                cls: "x-fluent-tab-contextuallabel x-fluent-tab-contextuallabel-" + color, 
                cn: [{ 
                         cls: "x-fluent-tab-contextuallabel-wrapper", 
                         cn: [{ 
                                  cls: "x-fluent-tab-contextuallabel-body", 
                                  html: label, 
                                  title: label
                              }
                         ] 
                    }
                ] 
            });
            ribbonPanel.setVisibilityMode(Ext.Element.DISPLAY);
            ribbonPanel.hide();
            return ribbonPanel;
        }
    }
);
