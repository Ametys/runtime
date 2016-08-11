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
 * &lt;p&gt;This tool displays the list of logs, and allows the administrator to perform several operations such as :&lt;/p&gt;
 * &lt;ul&gt;
 * 	&lt;li&gt; view logs &lt;/li&gt;
 * 	&lt;li&gt; delete logs &lt;/li&gt;
 * 	&lt;li&gt; download logs &lt;/li&gt;
 * 	&lt;li&gt; clean obsolete logs &lt;/li&gt;
 * &lt;/ul&gt;
 * @private
 */
Ext.define('Ametys.plugins.admin.logs.LogsTool', {
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
			model: 'Ametys.plugins.admin.logs.LogsTool.Log',
	        groupField: 'name',
	        
	        sortOnLoad: true,
	        sorters: [ { property: 'date', direction: "DESC" }, { property: 'location', direction: "ASC" }],
	        
	        proxy: {
	        	type: 'ametys',
				plugin: 'admin',
				url: 'logs',
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
		    	'selectionchange': Ext.bind(this.sendCurrentSelection, this),
		    	'rowdblclick': Ext.bind(this._openLog, this)
		    },
		    
		    features: [{
		        groupHeaderTpl: ['{columnName}: {name}'],
		        ftype: 'grouping'
		    }],
			
		    columns: [
		        {stateId: 'grid-location', header: "{{i18n PLUGINS_ADMIN_LOGS_COL_NAME}}", flex: 1, width : 250, renderer: this._fileNameRendered, menuDisabled : true, sortable: true, dataIndex: 'location'},
		        {stateId: 'grid-date', header: "{{i18n PLUGINS_ADMIN_LOGS_COL_DATE}}", flex: 0, width : 150, renderer: Ext.util.Format.dateRenderer('d F Y'), menuDisabled : true, sortable: true, dataIndex:'date',  align :'center'},
		        {stateId: 'grid-size', header: "{{i18n PLUGINS_ADMIN_LOGS_COL_SIZE}}", flex: 0, width : 100, renderer: this._sizeRendered, menuDisabled : true, sortable: true, dataIndex: 'size', align :'right'},
		        {stateId: 'grid-name', header: "{{i18n PLUGINS_ADMIN_LOGS_COL_CATEGORY}}", flex: 0, width : 100, hidden: true, menuDisabled : true, sortable: true, dataIndex: 'name', align :'right'}
		    ]
		});
	},

    /**
     * @private
     * Opens a log file
     * @param {Ext.grid.Panel} grid The main tool grid
     * @param {Ametys.plugins.admin.logs.LogsTool.Log} record The record to open
     */
	_openLog: function (grid, record)
	{
		if (record.get('size') > 1024 * 1024)
	    {
	    	Ametys.Msg.confirm ("{{i18n PLUGINS_ADMIN_LOGS_VIEW_DIALOG_TITLE}}", 
	    					    "{{i18n PLUGINS_ADMIN_LOGS_VIEW_DIALOG_CONFIRM}}",
	    					    function (answer)
	    					    {
	    							if (answer == 'yes')
	    							{
	    								Ametys.plugins.admin.logs.LogsActions.downloadFiles ([record.get('location')]);
	    							}
	    					    });
	    }
	    else
	    {
			window.location.href = Ametys.getPluginDirectPrefix('admin') + "/logs/view/" + encodeURIComponent(record.get('location'));
	    }
	},
	
	/**
	 * @private
	 * Sends a {@link Ametys.message.Message#SELECTION_CHANGED} message containing the
	 * selected log files on the message bus 
	 */
	sendCurrentSelection: function()
	{
		var targets = [];

		var selectedFiles = this._logs.getSelectionModel().getSelection();
		Ext.Array.forEach(selectedFiles, function(selectedFile) {
			
			target = Ext.create('Ametys.message.MessageTarget', {
				id: Ametys.message.MessageTarget.LOG_FILE,
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
		return '<img src="' + Ametys.getPluginResourcesPrefix('admin') + '/img/logs/logs_16.png' + '" style="float: left; margin-right: 3px"/>' + name;
	},
	
	/**
	 * @private
	 * Listener called on deletion event
	 * @param {Ametys.message.Message} message the deletion message
	 */
	_onLogFileDeleted: function(message)
	{
		var targets = message.getTargets(Ametys.message.MessageTarget.LOG_FILE);
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

Ext.define("Ametys.message.LogMessageTarget", {
	override: "Ametys.message.MessageTarget",

     statics: 
     {
         /**
          * @member Ametys.message.MessageTarget
          * @readonly
          * @property {String} LOG_FILE The target type is a log file. The expected parameters are:
          * @property {String} LOG_FILE.location The file location
          * @property {String} [LOG_FILE.size] The file size
          */
         LOG_FILE: "logfile"
     }
});
