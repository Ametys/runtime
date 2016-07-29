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
 * Field displaying a combobox allowing to select one of the available SQL data sources. Optionally, 
 * this field can allow the addition of other SQL data sources. 
 */
Ext.define('Ametys.form.widget.SQLDataSource', {
    extend: 'Ametys.form.widget.AbstractDataSource',
    alias: ['widget.datasource-sql'],
	
    /**
     * @cfg {String[]} [allowedDbTypes] The allowed types of database such as ['mysql', 'derby', 'oracle']. If empty or null, all database types will be allowed. 
     */
    
    dataSourceType: 'SQL',
    createButtonIconCls: 'ametysicon-data110 decorator-ametysicon-add64',
	createButtonTooltip: "{{i18n PLUGINS_CORE_UI_WIDGET_SQL_DATASOURCE_BUTTON_TOOLTIP}}",

	constructor: function(config)
	{
		// FIXME RUNTIME-2015 The configuration of a validator as JSON object should be pass to the field configuration
		// => validationConfig is null when configuration was built from a XML
		config.allowedDbTypes = config.allowedDbTypes || (config.validationConfig ? config.validationConfig.allowedDbTypes : []);
		if (!Ext.isArray(config.allowedDbTypes))
		{
			config.allowedDbTypes = config.allowedDbTypes.split(",");
		}
			
		this.callParent(arguments);
	},
	
	createDataSource: function (callback)
	{
		Ametys.plugins.admin.datasource.EditSQLDataSourceHelper.add(this.allowedDbTypes, callback);
	},
	
	getStoreExtraParameters: function ()
	{
		// Filter by allowed database types
		if (this.allowedDbTypes && this.allowedDbTypes.length > 0)
		{
			return {'allowedTypes': this.allowedDbTypes};
		}
		return {};
	},
	
	getDataModel: function ()
	{
		return 'Ametys.form.widget.SQLDataSource.Model';
	},
	
	getErrors: function(value) 
	{
		var errors = this.callParent(arguments);
		
		if (value && this.allowedDbTypes && this.allowedDbTypes.length > 0)
		{
			var index = this.getStore().find('id', value);
			if (index != -1)
			{
				var datasource = this.getStore().getAt(index);
				var dbtype = datasource.get('dbtype');
				
				if (!Ext.Array.contains(this.allowedDbTypes, dbtype))
				{
					errors.push("{{i18n plugin.core:PLUGINS_CORE_SQL_DATASOURCETYPE_VALIDATOR_FAILED}}");
				}
					
			}
		}
		return errors;
		
	}
});

Ext.define('Ametys.form.widget.SQLDataSource.Model', { 
	extend: 'Ametys.form.widget.AbstractDataSource.Model',
	
	fields: [
		{name: 'dbtype'},
		{name: 'url'}
	]
});