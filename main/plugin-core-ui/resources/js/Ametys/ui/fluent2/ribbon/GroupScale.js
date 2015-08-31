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
 * This class is the "group" container, that you can see in each tab to host buttons and other components
 */
Ext.define(
    "Ametys.ui.fluent.ribbon.GroupScale",
    {
        extend: "Ext.panel.Panel",
        alias: 'widget.ametys.ribbon-groupscale',
        
        /**
         * @cfg {Object} layout Doesn't apply to ribbon element. The value HAS TO be the default value.
         * @private
         */
        
        /**
         * @cfg {String} defaultType Doesn't apply to ribbon element. The value HAS TO be the default value.
         * @private
         */
        defaultType: 'ametys.ribbon-group-scale-part',
        
        constructor: function(config)
        {
            config = config || {};
            config.layout = {
                type: 'hbox',
                align: 'stretch'
            };
            this.callParent([config]);
        }
    }
);
    