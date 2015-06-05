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
 * This tool displays a list of system properties
 * @private
 */
Ext.define('Ametys.plugins.core.administration.jvmstatus.SystemPropertiesTool', {
	extend: 'Ametys.tool.Tool',
	
	/**
	 * @private
	 * @property {Ext.grid.Panel} _propertiesGrid The properties grid
	 */
	
	constructor: function(config)
	{
		this.callParent(arguments);
	},
	
	getMBSelectionInteraction: function() 
	{
		return Ametys.tool.Tool.MB_TYPE_NOSELECTION;
	},
	
	createPanel: function ()
	{
		this._propertiesGrid = this._drawPropertiesPanel();
		return this._propertiesGrid;
	},
	
	setParams: function (params)
	{
		this.callParent(arguments);
		this.refresh();
	},
	
	/**
	 * Refreshes the tool
	 */
	refresh: function ()
	{
		this.showRefreshing();
		this._propertiesGrid.getStore().load({callback: this.showRefreshed, scope: this});
	},
	
	/**
	 * @private
	 * Draw the panel displaying the logs
	 */
	_drawPropertiesPanel: function()
	{
		var store = Ext.create('Ext.data.Store', {
			model: 'Ametys.plugins.core.administration.tool.SystemPropertiesTool.Property',
	        
	        proxy: {
	        	type: 'ametys',
				plugin: 'core',
				url: 'administrator/system-properties',
	        	reader: {
	        		type: 'xml',
					record: 'property',
					rootProperty: 'properties'
	        	}
	        }
		});		
		
		return Ext.create('Ext.grid.Panel',{
			region: 'center',

			stateful: true,
			stateId: this.self.getName() + "$grid",

			store : store,
			
		    columns: [
		        {stateId: 'grid-name', header: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_TOOL_SYSTEMPROPERTIES_COL_NAME'/>", menuDisabled : true, sortable: true, flex: 1, dataIndex: 'name' },
		        {stateId: 'grid-value', header: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_TOOL_SYSTEMPROPERTIES_COL_VALUE'/>", menuDisabled : true, sortable: true, flex: 1, dataIndex: 'value' }
		    ]
		})
	},
});

Ext.define('Ametys.plugins.core.administration.tool.SystemPropertiesTool.Property', {
    extend: 'Ext.data.Model',
    
    fields: [
       {name: 'name', mapping: '@name'},
       {name: 'value', mapping: '@value'}
    ]
});
