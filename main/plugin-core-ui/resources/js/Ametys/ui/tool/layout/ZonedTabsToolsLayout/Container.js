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
 * This class is a container of ZoneTabsToolPanel to group them
 * @private
 */
Ext.define("Ametys.ui.tool.layout.ZonedTabsToolsLayout.Container", 
    {
        extend: "Ext.container.Container",
        xtype: "zoned-container",
        
        flex: 1,
        ui: 'tool-container',
        
        layout: {
            type: 'hbox',
            align: 'stretch'
        },
        
        constructor: function(config)
        {
            this.callParent(arguments);
            
            this.on('resize', this.saveBrotherState, this);
        },
        
        /**
         * @private
         * Due to a ExtJS bug (RUNTIME-1680) when the container is resized, only the concerned lateral zone state is also saved
         * but (especially at startup) the other lateral zone may be influenced and need to be saved
         */
        saveBrotherState: function()
        {
            var brother;
            
            brother = this.nextSibling("zonetabpanel-placeholder");
            if (brother)
            {
                brother.saveState();
            }
            
            brother = this.previousSibling("zonetabpanel-placeholder");
            if (brother)
            {
                brother.saveState();
            }
        }
    }
);
