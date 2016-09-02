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
 * This class provides a widget to query and select one or more users.
 * 
 * This widget is the default widget registered for fields of type Ametys.form.WidgetManager#TYPE_USER.<br>
 */
Ext.define('Ametys.form.widget.User', {
    extend: 'Ametys.form.AbstractQueryableComboBox',
  
	valueField: 'value',
	displayField: 'displayName',
    
    /**
     * @private
     * @property {String[]} _contexts The contexts for the populations where to retrieve users
     */
    /**
     * @cfg {String[]} [contexts] The contexts for the populations where to retrieve users. Default to the default ametys contexts
     */
    
    constructor: function(config)
    {
        config = config || {};
        this._contexts = config.contexts || Ametys.getAppParameter('populationContexts');
        
        this.callParent(arguments);
    },
	
    getStore: function()
    {
    	if (!Ext.data.schema.Schema.get('default').hasEntity('Ametys.form.widget.User.Users')) {
    		Ext.define("Ametys.form.widget.User.Users", {
    			extend: 'Ext.data.Model',

	    		fields: [
	    		         {name: 'firstname', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString},
	    		         {name: 'lastname', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString},
	    		         {name: 'login', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString},
	    		         {name: 'population', type: 'string'},
	    		         {name: 'populationLabel', type: 'string'},
	    		         {
                            name: 'value', 
                            type: 'string',
                            calculate: function(data)
                            {
                                return data.login + '#' + data.population;
                            }
                         },
	    		         {name: 'fullname', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString},
	    		         {name: 'sortablename', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString},
	    		         {
	    		             name: 'displayName',
	    		             type: 'string',
	    		             sortType: Ext.data.SortTypes.asNonAccentedUCString,
	    		             calculate: function (data)
	    		             {
                                return Ametys.plugins.core.users.UsersDAO.renderUser(data.login, data.populationLabel, data.sortablename);
                             }
	    		         }
	    		]
    		});
    	}

        return Ext.create('Ext.data.Store', {
            model: 'Ametys.form.widget.User.Users',
            proxy: {
                type: 'ametys',
                plugin: 'core',
    			url: 'users/search.json', 
                reader: {
                    type: 'json',
                    rootProperty: 'users'
                }
            },
            
            pageSize: this.maxResult,
            sortOnLoad: true,
            sorters: [{property: 'displayName', direction:'ASC'}],
            
            listeners: {
                beforeload: {fn: this._onStoreBeforeLoad, scope: this}
            }
        });
    },
    
    /**
     * Set the request parameters before loading the store.
     * @param {Ext.data.Store} store The store.
     * @param {Ext.data.operation.Operation} operation The Ext.data.Operation object that will be passed to the Proxy to load the Store.
     * @protected
     */
    _onStoreBeforeLoad: function(store, operation)
    {
        var params = operation.getParams() || {};
        operation.setParams(Ext.apply(params, {
            criteria: params.query,
            login: params.login ? params.login.split(',') : null,
            count: this.maxResult, 
            offset: 0 ,
            contexts: this._contexts
        }));
    },
    
    getLabelTpl: function ()
    {
    	var tpl = [];
        tpl.push('<img src="' + Ametys.getPluginDirectPrefix('core-ui') + '/user/{population}/{login}/image_16" style="float: left; margin-right: 3px; width: 16px; height: 16px;"/>');
        
    	tpl.push('{displayName}');
    	return tpl;
    }
});
