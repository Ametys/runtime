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
 * This tool displays the list of system announcement's messages for various languages. It allows the administrator to perform several operations 
 * such as :
 * &lt;ul&gt;
 * 	&lt;li&gt; Add a welcome message in a language of his choice
 *  &lt;li&gt; Modify existing messages
 *  &lt;li&gt; Delete messages
 * &lt;/ul&gt;
 * Note that only one message by language can be recorded at a time. 
 */
Ext.define('Ametys.plugins.core.administration.tool.SystemTool', {
	extend: 'Ametys.tool.Tool',
	
	/**
	 * @private
	 * @property {Ext.grid.Panel} _grid The messages grid
	 */
	
	constructor: function(config)
	{
		this.callParent(arguments);
		
		Ametys.message.MessageBus.on(Ametys.message.Message.DELETED, this._onAnnouncementDeleted, this);
		Ametys.message.MessageBus.on(Ametys.message.Message.CREATED, this._onAnnoucementCreatedOrModified, this);
		Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, this._onAnnoucementCreatedOrModified, this);
	},
	
	getMBSelectionInteraction: function() 
	{
		return Ametys.tool.Tool.MB_TYPE_ACTIVE;
	},
	
	createPanel: function ()
	{
		this._grid = this._drawMessagesPanel();
		return this._grid;
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
		this._grid.getStore().load({callback: this.showRefreshed, scope: this});
	},
	
	sendCurrentSelection: function()
	{
		this._onSelectMessage();
	},
	
	/**
	 * @private
	 * Draw the panel displaying the logs
	 */
	_drawMessagesPanel: function()
	{
		var store = Ext.create('Ext.data.Store', {
			model: 'Ametys.plugins.core.administration.tool.SystemTool.Message',
	        
			sortOnLoad: true,
	        sorters: [ { property: 'language', direction: "ASC" } ],
			
	        proxy: {
	        	type: 'ametys',
				plugin: this._pluginName,
				url: 'administrator/system-announcements',
	        	reader: {
	        		type: 'json',
					rootProperty: 'announcements'
	        	}
	        }
		});		
		
		return Ext.create('Ext.grid.Panel',{
			stateful: true,
			stateId: this.self.getName() + "$grid",

			store : store,
			
		    columns: [
		        {stateId: 'grid-lang', header: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_SYSTEM_COL_LANG'/>", menuDisabled : true, sortable: true, width: 80, dataIndex: 'language' },
		        {stateId: 'grid-message', header: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_SYSTEM_COL_MESSAGE'/>", menuDisabled : true, sortable: true, flex: 1, dataIndex: 'message' }
		    ],
		    
			listeners: {
				'selectionchange': Ext.bind(this._onSelectMessage, this)
			}
		})
	},
	
	/**
	 * @private
	 * Listener when a record is selected
	 */
	_onSelectMessage: function()
	{
		var targets = [];
		
		var selectedRecords = this._grid.getSelectionModel().getSelection();
		Ext.Array.forEach(selectedRecords, function(selectedRecord) {
			
			target = Ext.create('Ametys.message.MessageTarget', {
				type: 'system-announcement-message',
				parameters: {
					language: selectedRecord.get('language'), 
					message: selectedRecord.get('message')
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
	 * @private
	 * Listener when a message is deleted
	 * @param {Ametys.message.Message} message the deletion message
	 */
	_onAnnouncementDeleted: function(message)
	{
		var targets = message.getTargets('system-announcement-message');
		if (targets.length > 0)
		{
			var store = this._grid.getStore();
			Ext.Array.forEach(targets, function(target) {
				var language = target.getParameters().language;
				var index = store.find("language", language); 
				if (index != -1)
				{
					store.removeAt(index);
				}
			});
		}
	},
	
	/**
	 * @private
	 * Listener when a message is created or modified
	 * @param {Ametys.message.Message} message The created/modified message.
	 */
	_onAnnoucementCreatedOrModified: function(message)
	{
		if (message.getTargets('system-announcement-message').length > 0)
		{
			this.showOutOfDate();
		}
	}
});

Ext.define('Ametys.plugins.core.administration.tool.SystemTool.Message', {
    extend: 'Ext.data.Model',
    
    fields: [
       {name: 'language'},
       {name: 'message'}
    ]
});
