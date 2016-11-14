/*
 *  Copyright 2016 Anyware Services
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
 * This tool displays the list of data sources 
 * @private
 */
Ext.define('Ametys.plugins.admin.datasource.DataSourceTool', {
	extend: 'Ametys.tool.Tool',
	
	/**
	 * @private
	 * @property {Ext.grid.Panel} _panel the data sources' grid panel
	 */
	
	getMBSelectionInteraction: function() 
	{
		return Ametys.tool.Tool.MB_TYPE_ACTIVE;
	},
	
	constructor: function(config)
	{
		this.callParent(arguments);
		
		// Bus messages listeners
		Ametys.message.MessageBus.on(Ametys.message.Message.CREATED, this._onCreated, this);
		Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, this._onModified, this);
		Ametys.message.MessageBus.on(Ametys.message.Message.DELETED, this._onDeleted, this);
	},
	
	createPanel: function()
	{
		var store = Ext.create('Ext.data.Store', {
			model: 'Ametys.plugins.admin.datasource.DataSourceTool.DataSource',
	        groupField: 'type',
			
	        proxy: {
	        	type: 'ametys',
				plugin: 'admin',
				url: 'datasources/get',
	        	reader: {
	        		type: 'json',
					rootProperty: 'datasources'
	        	},
                
                extraParams: {
                    includePrivate: 'true',
                    includeInternal: 'true'
                }
	        },
            
            sorters: [{property: 'name', direction: 'ASC'}]
		});
		
		this._panel = Ext.create('Ext.grid.Panel', {
			stateful: true,
			stateId: this.self.getName() + "$grid",

			store: store,
			
			selModel: {
		    	mode: 'MULTI'
		    },
		    
		    listeners: {
		    	'selectionchange': {fn: this.sendCurrentSelection, scope: this}
		    },
		    
		    features: [{
		    	ftype: 'grouping',
		        groupHeaderTpl: Ext.create('Ext.XTemplate',
		            '<div>{name:this.formatName}</div>',
	        	    {
			        	formatName: function(name)
				        {
				        	switch (name)
				        	{
					        	case 'SQL':
					        		return "{{i18n PLUGINS_ADMIN_UITOOL_DATASOURCE_GROUP_SQL}}";
					        		
					        	case 'LDAP':
					        		return "{{i18n PLUGINS_ADMIN_UITOOL_DATASOURCE_GROUP_LDAP}}";
					        		
					        	default:
					        		throw 'Unrecognized type ' + name;
				        	}
				        }
	        	    }
	        	)
		    }],
			
		    columns: [
		        {stateId: 'grid-name', header: "{{i18n PLUGINS_ADMIN_UITOOL_DATASOURCE_COL_NAME}}", flex: 0.8, sortable: true, dataIndex:'name', renderer: this._renderName},
		        {stateId: 'grid-private', header: "{{i18n PLUGINS_ADMIN_UITOOL_DATASOURCE_COL_PRIVATE}}", width: 120, sortable: true, dataIndex: 'private', align: 'center', renderer: this._renderBoolean},
		        {stateId: 'grid-inUse', header: "{{i18n PLUGINS_ADMIN_UITOOL_DATASOURCE_COL_USED}}", width: 120, sortable: true, dataIndex: 'isInUse', align :'center', renderer: this._renderBoolean},
		        {stateId: 'grid-isDefault', header: "{{i18n PLUGINS_ADMIN_UITOOL_DATASOURCE_COL_DEFAULT}}", width: 120, sortable: true, dataIndex: 'isDefault', align :'center', renderer: this._renderBoolean},
		        {stateId: 'grid-type', header: "{{i18n PLUGINS_ADMIN_UITOOL_DATASOURCE_COL_TYPE}}", width: 80, hidden: true, dataIndex: 'type'},
		        {stateId: 'grid-id', header: "{{i18n PLUGINS_ADMIN_UITOOL_DATASOURCE_COL_ID}}", flex: 0.2, hidden: true, dataIndex: 'id'}
		    ]
		});
		
		return this._panel;
	},
    
    setParams : function ()
    {
        this.callParent(arguments);
        this.refresh();
    },
    
    refresh: function ()
    {
        this.showRefreshing();
        this._panel.getStore().load({callback: this.showRefreshed, scope: this});
        
    },
	
	sendCurrentSelection: function()
	{
		var targets = [];

		var selectedDataSources = this._panel.getSelectionModel().getSelection();
		Ext.Array.forEach(selectedDataSources, function(selectedDataSource) {
			
			target = Ext.create('Ametys.message.MessageTarget', {
				id: Ametys.message.MessageTarget.DATASOURCE,
				parameters: {
					id: selectedDataSource.get('id'),
					type: selectedDataSource.get('type'),
					isDefault: selectedDataSource.get('isDefault'),
					isInUse: selectedDataSource.get('isInUse')
				}
			});
			
			targets.push(target);
		});
		
		Ext.create('Ametys.message.Message', {
			type: Ametys.message.Message.SELECTION_CHANGED,
			targets: targets
		});
	},
	
	/**
	 * Get the default data source object
	 * @param {String} type the type of the desired default data source
	 * @return {Object} the default data source object
	 */
	getDefaultDataSource: function(type)
	{
		var dataSources = this._panel.getStore().getRange();
		var defaultDataSource = null;
		Ext.Array.each(dataSources, function(dataSource){
			if (dataSource.get('isDefault') && dataSource.get('type') == type)
			{
				defaultDataSource = dataSource;
				return false;
			}
		});
		
		return defaultDataSource;
	},
	
	/**
	 * @private
	 * Renderer function for the name column
	 * @param {String} value the value of the name
	 * @param {Object} md the metadata of the selected cell
	 * @param {Ext.data.Model} record The record for the current row
	 * @return {String} the html representation of the name with an icon
	 */
	_renderName: function(value, md, record)
	{
        var decorator = record.get('isValid') ? '' : ' decorator-ametysicon-caution9 datasource-warning';
        var glyph = record.get('type') == 'LDAP' ? 'ametysicon-agenda3' : 'ametysicon-data110';
        return '<span class="a-grid-glyph ' + glyph + decorator + '"></span>' + value;
	},
	
    /**
     * @private
     * Renderer function for a boolean
     * @param {String|Boolean} value the restricted value
     * @return {String} the html representation of the boolean as an icon.
     */
    _renderBoolean: function(value)
    {
        var isTrue = Ext.isBoolean(value) ? value : value == 'true';
        if (isTrue)
        {
            return '<span class="a-grid-glyph ametysicon-check34" title="' + "{{i18n PLUGINS_ADMIN_UITOOL_DATASOURCE_CHECKED_TITLE}}" + '"></span>';
        }
        else
        {
            return "";
        }
    },
	
	/**
	 * @private
	 * Handler function invoked whenever a {@link Ametys.message.Message#CREATED} message is sent out on the 
	 * message bus. Add the corresponding record to the grid panel's store.
	 * @param {Ametys.message.Message} message the message
	 */
	_onCreated: function(message)
	{
        var targets = message.getTargets(Ametys.message.MessageTarget.DATASOURCE);
        if (targets.length > 0)
        {
            this.showOutOfDate();
        }
	},
	
	/**
	 * @private
	 * Handler function invoked whenever a {@link Ametys.message.Message#MODIFIED} message is sent out on the 
	 * message bus. Update the corresponding record of the grid panel's store.
	 * @param {Ametys.message.Message} message the message
	 */
	_onModified: function (message)
	{
		var targets = message.getTargets(Ametys.message.MessageTarget.DATASOURCE);
        if (targets.length > 0)
        {
            this.showOutOfDate();
        }
	},
	
	/**
	 * @private
	 * Handler function invoked whenever a {@link Ametys.message.Message#DELETED} message 
	 * is sent out on the message bus. Delete the corresponding record from the grid panel's store.
	 * @param {Ametys.message.Message} message the message
	 */
	_onDeleted: function (message)
	{
        var targets = message.getTargets(Ametys.message.MessageTarget.DATASOURCE);
        for (var i = 0; i < targets.length; i++)
        {
            var record = this._panel.getStore().getById(targets[i].getParameters().id);
            this._panel.getStore().remove(record);
        }
        
        if (targets.length > 0)
        {
            this.showOutOfDate();
        }
	}
});
