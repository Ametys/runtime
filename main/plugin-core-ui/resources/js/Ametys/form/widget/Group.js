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
 * This widget is registered for fields of type Ametys.form.WidgetManager#TYPE_STRING.<br>
 */
Ext.define('Ametys.form.widget.Group', {
    extend: 'Ametys.form.AbstractQueryableComboBox',
	
	valueField: 'value',
	displayField: 'displayName',
	
    getStore: function()
    {
        if (!Ext.data.schema.Schema.get('default').hasEntity('Ametys.form.widget.Group.Groups')) {
            Ext.define("Ametys.form.widget.Group.Groups", {
                extend: 'Ext.data.Model',
                
                fields: [
                     {name: 'label', mapping: 'label', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString},
                     {name: 'groupId', mapping: 'id'},
                     {name: 'groupDirectory', mapping: 'groupDirectory'},
                     {name: 'groupDirectoryLabel', mapping: 'groupDirectoryLabel', sortType: Ext.data.SortTypes.asNonAccentedUCString},
                     {
                        name: 'value', 
                        type: 'string',
                        calculate: function(data)
                        {
                            return data.groupId + '#' + data.groupDirectory;
                        }
                     },
                     {
                         name: 'displayName',
                         type: 'string',
                         sortType: Ext.data.SortTypes.asNonAccentedUCString,
                         calculate: function(data)
                         {
                            return data.label + ' (' + data.groupId + ', ' + data.groupDirectoryLabel + ')';
                         }
                     }
                ]
            });
        }

        return Ext.create('Ext.data.Store', {
            model: 'Ametys.form.widget.Group.Groups',
            proxy: {
                type: 'ametys',
                plugin: 'core',
    			url: "groups/search.json", 
                reader: {
                    type: 'json',
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
     * @param {Ext.data.operation.Operation} operation The Ext.data.Operation object that will be passed to the Proxy to load the Store.
     * @private
     */
    _onStoreBeforeLoad: function(store, operation)
    {
        operation.setParams(Ext.apply(operation.getParams() || {}, {
            criteria: operation.getParams().query,
            id: operation.getParams().id ? operation.getParams().id.split(',') : null,
            count: this.maxResult, 
            offset: 0,
            contexts: Ametys.getAppParameter('populationContexts')
        }));
    },
    
    getValue: function ()
    {
    	var value = this.callParent(arguments),
    		convertedValues = [];
    	
    	value = Ext.Array.from(value);
    	
    	Ext.Array.forEach (value, function (val) {
    		var parts = val.split('#');
    		convertedValues.push({
    			groupId: parts[0],
    			groupDirectory: parts[1]
    		});
    	});
    	
    	return this.multiple ? convertedValues : convertedValues[0];
    },
    
    setValue: function (value)
    {
    	value = Ext.Array.from(value);
    	
    	var convertedValues = [];
    	Ext.Array.forEach (value, function (item) {
    		if (Ext.isObject(item))
    		{
    			convertedValues.push(item.id + '#' + item.groupDirectory);
    		}
    		else
    		{
    			convertedValues.push(item);
    		}
    	});
    	
    	this.callParent([convertedValues]);
    },
    
    getSubmitValue: function ()
    {
    	var value = this.getValue();
    	return value != null ? Ext.encode(this.getValue()) : null;
    },
    
    getLabelTpl: function ()
    {
    	var tpl = [];
    	tpl.push('<img width="16" height="16" src="' + Ametys.getPluginResourcesPrefix('core-ui') + '/img/groups/group_16.png"/>');
    	tpl.push('{' + this.displayField + '}');
    	return tpl;
    }
});
