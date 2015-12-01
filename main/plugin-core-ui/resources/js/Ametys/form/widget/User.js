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
    extend: 'Ametys.form.widget.AbstractQueryableComboBox',
  
    /**
	 * @private
	 * @property {String} usersManagerRole The currently selected UsersManager to use to list the user. The string is the role name on the server-side. Null/Empty means the default users manager.
	 */
	usersManagerRole: null,
	
	valueField: 'login',
	displayField: 'displayName',
	
    getStore: function()
    {
    	if (!Ext.data.schema.Schema.get('default').hasEntity('Ametys.form.widget.User.Users')) {
    		Ext.define("Ametys.form.widget.User.Users", {
    			extend: 'Ext.data.Model',

	    		fields: [
	    		         {name: 'firstname', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString},
	    		         {name: 'lastname', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString},
	    		         {name: 'login', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString},
	    		         {name: 'fullname', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString},
	    		         {name: 'sortablename', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString},
	    		         {
	    		             name: 'displayName',
	    		             type: 'string',
	    		             sortType: Ext.data.SortTypes.asNonAccentedUCString,
	    		             depends: ['sortablename', 'login'],
	    		             calculate: function (data)
	    		             {
	    		                 if (data.sortablename != data.login)
	    		                 {
	    		                     return data.sortablename + ' (' + data.login + ')';
	    		                 }
	    		                 
	    		                 return data.login;
	    		             }
	    		         }
	    		],
	
	    		idProperty: 'login'
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
     * @private
     */
    _onStoreBeforeLoad: function(store, operation)
    {
        operation.setParams(Ext.apply(operation.getParams() || {}, {
            criteria: operation.getParams().query,
            login: operation.getParams().login ? operation.getParams().login.split(',') : null,
            count: this.maxResult, 
            offset: 0, 
            usersManagerRole: this.usersManagerRole
        }));
    },
    
    getLabelTpl: function ()
    {
    	var tpl = [];
        if (!this.usersManagerRole || this.usersManagerRole == 'org.ametys.core.user.UsersManager.ROLE')
        {
            tpl.push('<img src="' + Ametys.getPluginDirectPrefix('core-ui') + '/user/{login}/image_16" style="float: left; margin-right: 3px; width: 16px; height: 16px;"/>');
        }
        else
        {
            tpl.push('<img src="' + Ametys.getPluginDirectPrefix('core-ui') + '/user/default-image_16" style="float: left; margin-right: 3px; width: 16px; height: 16px;"/>');
        }
        
    	tpl.push('{fullname}');
    	return tpl;
    }
});
