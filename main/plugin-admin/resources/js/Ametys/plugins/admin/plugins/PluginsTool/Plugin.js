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
 * The model for the {@link Ametys.plugins.admin.plugins.PluginsTool} tree tool. 
 */
Ext.define('Ametys.plugins.admin.plugins.PluginsTool.Plugin', { 
    extend: 'Ext.data.TreeModel', 
    fields: [ 
       { name: 'icon', type: 'string' }, 
       { name: 'text', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString}, 
       { name: 'type', type: 'string' }, // can be 'plugin', 'extension', 'feature', 'component', 'extension-point'
       { name: 'active', type: 'boolean' }, // true if its an active feature
       { name: 'cause', type: 'string' }, // if inactive, the cause of inactivation
       { name: 'isMultiple', type: 'string' }, // is it a multiple extension point 
       { name: 'pluginName', type: 'string' }, // the name of the plugin bringing the element
       { name: 'featureName', type: 'string' }, // the name of the feature bringing the lement
       { name: 'componentName', type: 'string' }, // the name of the component
       { name: 'extensionPointName', type: 'string'}, // the extension point if
       { name: 'extensionId', type: 'string'}, // the extension if
       { name: 'leaf', type: 'boolean'} 
    ] 
}); 