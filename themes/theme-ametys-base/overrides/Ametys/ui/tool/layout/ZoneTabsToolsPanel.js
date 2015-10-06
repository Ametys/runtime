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

Ext.define('Ametys.theme.ametysbase.ui.tool.layout.ZonedTabsToolsLayout.ZoneTabsToolsPanel', {
    override: 'Ametys.ui.tool.layout.ZonedTabsToolsLayout.ZoneTabsToolsPanel',
    
    // Automatic resize tabs
    minTabWidth: 36, // Size to display an icon with correct margins
    maxTabWidth: 250,
    
    plain: true,
    
    tabBar: {
        defaults:{
            // Will try to fill all available space.
            flex:1,
            textAlign: 'left'
        }
    }
});
