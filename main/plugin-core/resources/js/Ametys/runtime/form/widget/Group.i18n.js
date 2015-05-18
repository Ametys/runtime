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
 * This class provides a widget to query and select one or more groups.
 * 
 * This widget is registered for fields of type Ametys.runtime.form.WidgetManager#TYPE_STRING.<br>
 */
Ext.define('Ametys.runtime.form.widget.Group', {
    
    extend: 'Ametys.runtime.form.widget.AbstractQueryableComboBox',
  
    /**
	 * @private
	 * @property {String} groupsManagerRole The currently selected GroupsManager to use to list the group. The string is the role name on the server-side. Null/Empty means the default groups manager.
	 */
    groupsManagerRole: null,
	
	valueField: 'id',
	displayField: 'label',
	
    getStore: function()
    {
    	Ext.define("Ametys.runtime.form.widget.Group.Groups", {
    		extend: 'Ext.data.Model',
    		
    	    fields: [
    	             {name: 'label', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString},
    	             {name: 'id', mapping: '@id'}
    	    ],
    	    
    	    idProperty: 'id'
    	});

        return Ext.create('Ext.data.Store', {
            model: 'Ametys.runtime.form.widget.Group.Groups',
            proxy: {
                type: 'ametys',
                plugin: 'core',
    			url: "groups/search.xml", 
                reader: {
                    type: 'xml',
                    record: 'group',
                    rootProperty: 'groups'
                }
            },
            
            pageSize: this.maxResult,
            sortOnLoad: true,
            sorters: [{property: 'label', direction:'ASC'}],
            
            listeners: {
                beforeload: {fn: this._onStoreBeforeLoad, scope: this}
            }
        });
    },
    
    /**
     * Set the request parameters before loading the store.
     * @param {Ext.data.Store} store The store.
     * @param {Ext.data.Operation} operation The Ext.data.Operation object that will be passed to the Proxy to load the Store.
     * @private
     */
    _onStoreBeforeLoad: function(store, operation)
    {
        operation.setParams( operation.getParams() || {} );
        
        operation.setParams( Ext.apply(operation.getParams(), {
        	criteria: operation.getParams().query,
        	id: operation.getParams().id ? operation.getParams().id.split(',') : null,
        	count: this.maxResult, 
        	offset: 0, 
        	groupsManagerRole: this.groupsManagerRole
        }));
    },
    
    getLabelTpl: function ()
    {
    	var tpl = [];
    	tpl.push('<img width="16" height="16" src="' + Ametys.getPluginResourcesPrefix('cms') + '/img/groups/group_16.png"/>');
    	tpl.push('{[values.label]}');
    	return tpl;
    }
});
