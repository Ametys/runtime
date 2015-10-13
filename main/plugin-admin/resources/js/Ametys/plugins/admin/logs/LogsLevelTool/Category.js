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
 * @private
 * The model for the {@link Ametys.plugins.admin.logs.LogsLevelTool} tree tool. 
 */
Ext.define('Ametys.plugins.admin.logs.LogsLevelTool.Category', { 
    extend: 'Ext.data.TreeModel', 
    fields: [ 
        { name: 'id', type: 'string' }, 
        { name: 'parentLevel', type: 'string'},
        { name: 'icon', type: 'string', calculate: function(data) {
                return Ametys.getPluginResourcesPrefix("admin") + "/img/logs/loglevel_" + (data.level.toLowerCase() == "inherit" ? data.parentLevel.toLowerCase() + "-inherit": data.level.toLowerCase()) + ".png"
            } 
        }, 
        { name: 'text', type: 'string', mapping: 'name', sortType: Ext.data.SortTypes.asNonAccentedUCString},
        { name: 'category', type: 'string', mapping: 'fullname'},
        { name: 'level', type: 'string', mapping: 'level', sortType: Ext.data.SortTypes.asNonAccentedUCString},
    ],
    
    /**
     * Get the current log level of a tree node.
     * @param {Ametys.plugins.admin.logs.LogsLevelTool.Category} node the node from which resolve the level
     * @return {String} The log level (info, warn...) of the node. Inherit will be resolved by getting the parents node value.
     */
    getResolvedLevel: function(node)
    {
    	var node = node != null ? node : this;
    	if (node.get('level') != 'inherit')
        {
            return node.get('level');
        }
        else
        {
            return this.getResolvedLevel(node.parentNode);
        }
    },
}); 
