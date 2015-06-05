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
 * This tool displays the list of logs, and allows the administrator to perform several operations such as:
 * * view logs
 * * delete logs
 * * download logs
 * * clean obsolete logs
 * @private
 */
Ext.define('Ametys.plugins.core.administration.logs.LogsTool', {
	extend: 'Ametys.tool.Tool',
	
	/**
	 * @private
	 * @property {Ext.grid.Panel} _logs The logs grid
	 */
	
	constructor: function(config)
	{
		this.callParent(arguments);
		Ametys.message.MessageBus.on(Ametys.message.Message.DELETED, this._onLogFileDeleted, this);
	},
	
	getMBSelectionInteraction: function() 
	{
		return Ametys.tool.Tool.MB_TYPE_ACTIVE;
	},
	
	createPanel: function ()
	{
		this._logs = this._drawLogsPanel();
		return this._logs;
	},
	
	setParams: function (params)
	{
		this.callParent(arguments);
		this.refresh();
	},
	
	sendCurrentSelection: function()
	{
		this._selectLog();
	},
	
	refresh: function ()
	{
		this.showRefreshing();
		this._logs.getStore().load({callback: this.showRefreshed, scope: this});
	},
	
	/**
	 * @private
	 * Draw the grid panel storing the logs' files.
	 * @return {Ext.grid.Panel} The grid panel
	 */
	_drawLogsPanel: function()
	{
		var store = Ext.create('Ext.data.Store', {
			model: 'Ametys.plugins.core.administration.tool.LogsTool.Log',
	        groupField: 'name',
	        
	        sortOnLoad: true,
	        sorters: [ { property: 'date', direction: "DESC" }, { property: 'location', direction: "ASC" }],
	        
	        proxy: {
	        	type: 'ametys',
				plugin: 'core',
				url: 'administrator/logs',
	        	reader: {
	        		type: 'xml',
					record: 'log',
					rootProperty: 'logs'
	        	}
	        }
		});		
		
		return Ext.create('Ext.grid.Panel', {
			region: 'center',

			stateful: true,
			stateId: this.self.getName() + "$grid",

			store : store,
			
			selModel : {
		    	mode: 'MULTI'
		    },
		    
		    listeners: {
		    	'selectionchange': Ext.bind(this._selectLog, this),
		    	'rowdblclick': Ext.bind(this._openLog, this)
		    },
		    
		    features: [{
		        groupHeaderTpl: ['{columnName}: {name}'],
		        ftype: 'grouping'
		    }],
			
		    columns: [
		        {stateId: 'grid-location', header: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_LOGS_COL_NAME'/>", flex: 1, width : 250, renderer: this._fileNameRendered, menuDisabled : true, sortable: true, dataIndex: 'location'},
		        {stateId: 'grid-date', header: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_LOGS_COL_DATE'/>", flex: 0, width : 150, renderer: Ext.util.Format.dateRenderer('d F Y'), menuDisabled : true, sortable: true, dataIndex:'date',  align :'center'},
		        {stateId: 'grid-size', header: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_LOGS_COL_SIZE'/>", flex: 0, width : 100, renderer: this._sizeRendered, menuDisabled : true, sortable: true, dataIndex: 'size', align :'right'},
		        {stateId: 'grid-name', header: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_LOGS_COL_CATEGORY'/>", flex: 0, width : 100, hidden: true, menuDisabled : true, sortable: true, dataIndex: 'name', align :'right'}
		    ]
		});
	},

    /**
     * @private
     * Opens a log file
     * @param {Ext.grid.Panel} grid The main tool grid
     * @param {Ametys.plugins.core.administration.tool.LogsTool.Log} record The record to open
     */
	_openLog: function (grid, record)
	{
		if (record.get('size') > 1024 * 1024)
	    {
	    	Ametys.Msg.confirm ("<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_LOGS_VIEW_DIALOG_TITLE'/>", 
	    					    "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_LOGS_VIEW_DIALOG_CONFIRM'/>",
	    					    function (answer)
	    					    {
	    							if (answer == 'yes')
	    							{
	    								Ametys.plugins.core.administration.logs.LogsActions.downloadFiles ([record.get('location')]);
	    							}
	    					    });
	    }
	    else
	    {
			window.location.href = Ametys.getPluginDirectPrefix('core') + "/administrator/logs/view/" + encodeURIComponent(record.get('location'));
	    }
	},
	
	/**
	 * @private
	 * Sends a {@link Ametys.message.Message#SELECTION_CHANGED} message containing the
	 * selected log files on the message bus 
	 */
	_selectLog: function()
	{
		var targets = [];

		var selectedFiles = this._logs.getSelectionModel().getSelection();
		Ext.Array.forEach(selectedFiles, function(selectedFile) {
			
			target = Ext.create('Ametys.message.MessageTarget', {
				type: 'logfile',
				parameters: {size: selectedFile.get('size'), location: selectedFile.get('location')}
			});
			
			targets.push(target);
		});
		
		Ext.create('Ametys.message.Message', {
			type: Ametys.message.Message.SELECTION_CHANGED,
			targets: targets
		});
	},
	
	/**
	 * @private
	 * Returns a readable size for a file's size in bytes
	 * @param {Number} size The size to render
	 * @return {String} The readable size. e.g. 12 MB
	 */
	_sizeRendered: function (size)
	{
		return Ext.util.Format.fileSize(size);
	},
	
	/**
	 * @private
	 * Rendered for file name
	 * @param {String} name the file's name
	 */
	_fileNameRendered: function (name)
	{
		return '<img src="' + Ametys.getPluginResourcesPrefix('core') + '/img/administrator/logs/logs_16.png' + '" style="float: left; margin-right: 3px"/>' + name;
	},
	
	/**
	 * @private
	 * Listener called on deletion event
	 * @param {Ametys.message.Message} message the deletion message
	 */
	_onLogFileDeleted: function(message)
	{
		var targets = message.getTargets("logfile");
		if (targets.length > 0)
		{
			var store = this._logs.getStore();
			Ext.Array.forEach(targets, function(target) {
				var location = target.getParameters().location;
				var index = store.find("location", location); 
				if (index != -1)
				{
					store.removeAt(index);
				}
			});
		}
	}
});
