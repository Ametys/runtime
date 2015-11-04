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

Ext.define("Ametys.ui.tool.layout.ZonedTabsToolsLayout.Container", 
    {
        extend: "Ext.container.Container",
        xtype: "zoned-container",
        
        flex: 1,
        
        split: true,
        layout: {
            type: 'hbox',
            align: 'stretch'
        },
        
        getState: function()
        {
            var state = this.callParent(arguments);
            
            state = this.addPropertyToState(state, 'flex');
            
            return state;
        }
    }
);
