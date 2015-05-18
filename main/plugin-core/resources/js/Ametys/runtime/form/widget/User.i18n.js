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
 * This widget is the default widget registered for fields of type Ametys.runtime.form.WidgetManager#TYPE_USER.<br>
 */
Ext.define('Ametys.runtime.form.widget.User', {
    
    extend: 'Ametys.runtime.form.widget.AbstractQueryableComboBox',
  
    /**
	 * @private
	 * @property {String} usersManagerRole The currently selected UsersManager to use to list the user. The string is the role name on the server-side. Null/Empty means the default users manager.
	 */
	usersManagerRole: null,
	
	valueField: 'login',
	displayField: 'fullname',
	
    getStore: function()
    {
    	Ext.define("Ametys.runtime.form.widget.User.Users", {
    		extend: 'Ext.data.Model',
    		
    	    fields: [
    	             {name: 'firstname', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString},
    	             {name: 'lastname', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString},
    	             {name: 'login', mapping: '@login', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString},
    	             {name: 'fullname', convert: function (v, record) {
    	                 return record.get('lastname') + ' ' + record.get('firstname') + ' (' + record.get('login') + ')';
    	             }, type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString}
    	    ],
    	    
    	    idProperty: 'login'
    	});

        return Ext.create('Ext.data.Store', {
            model: 'Ametys.runtime.form.widget.User.Users',
            proxy: {
                type: 'ametys',
                plugin: 'core',
    			url: "users/search.xml", 
                reader: {
                    type: 'xml',
                    record: 'user' //,
					// Disabling root node
					// Workaround for mixed users manager (ldap and jdbc etc...)
                    // rootProperty: 'users'
                }
            },
            
            pageSize: this.maxResult,
            sortOnLoad: true,
            sorters: [{property: 'fullname', direction:'ASC'}],
            
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
        operation.setParams( operation.getParams() || {} );
        
        operation.setParams( Ext.apply(operation.getParams(), {
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
    	tpl.push('<img  width="16" height="16" src="' + Ametys.getPluginResourcesPrefix('cms') + '/img/widgets/user/user_16.png"/>');
    	tpl.push('{[values.fullname]}');
    	return tpl;
    }
});
