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
 * This class is the model for entries of the navigationhistory store.
 * @private
 */
Ext.define("Ametys.navhistory.HistoryDAO.HistoryEntry", {
	extend: 'Ext.data.Model',
	
	/**
	 * @property {Object} fields Data structure of the history entries
	 * @property {String} fields.id Unique identifier of entry
	 * @property {String} fields.label Label of the entry
	 * @property {String} fields.date Date of the entry
	 * @property {String} fields.description Description of the entry
	 * @property {String} fields.iconSmall The full path to the small icon (in 16x16 pixels)
	 * @property {String} fields.iconMedium The full path to the medium icon (in 32x32 pixels)
	 * @property {String} fields.iconLarge The full path to the large icon (in 32x32 pixels)
	 * @property {String} fields.type The type
	 * @property {Function} fields.action Action to execute on entry
	 * @private
	 */
    fields: [
		{name: 'id'},    
		{name: 'objectId'},    
		{name: 'label', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString},
		{name: 'date'},
		{name: 'description', type: 'string', sortType: Ext.data.SortTypes.asNonAccentedUCString},
		{name: 'actionFunction',  type: 'string'},
        {
        	name: 'description',  
        	type: 'string',
        	convert: function (initialDesc, record)
        	{
        		var d = '';
        		
        		var cursor = 0;
        		var count = 0;
        		var index = initialDesc.indexOf("<a>", index);
        		while (index != -1)
        		{
        			d += initialDesc.substring (cursor, index);
        			d += "<a href=\"#\" onclick=\""+ record.data.actionFunction+ "('" + record.data.id + "', " + count + ")\">";
        			
        			cursor = index + 3;
        			index = initialDesc.indexOf("<a>", index + 3);
        			count++;
        		}
        		
        		d += initialDesc.substring (cursor);
        		
        		return d;
        	}
        },
        {name: 'iconGlyph'},
		{name: 'iconSmall'},
		{name: 'iconMedium'},
		{name: 'iconLarge'},
		{name: 'type'},
		{name: 'action'},
		{name: 'actionFunction'}
    ]
});


/**
 * This class handles the access to navigation history. 
 */
Ext.define('Ametys.navhistory.HistoryDAO', {
	singleton: true,
	
	/**
	 * @readonly
	 * @property {String} SEARCH_TYPE Type for entries dealing with a research
	 */
	SEARCH_TYPE: "{{i18n PLUGINS_CORE_UI_NAVHISTORY_DAO_SEARCH_TYPE}}",
	
	/**
	 * @readonly
	 * @property {String} TOOL_TYPE Type for entries dealing with a tool
	 */
	TOOL_TYPE: "{{i18n PLUGINS_CORE_UI_NAVHISTORY_DAO_TOOL_TYPE}}",
	
	/**
	 * @readonly
	 * @property {String} CONTENT_TYPE Type for entries dealing with a content
	 */
	CONTENT_TYPE: "{{i18n PLUGINS_CORE_UI_NAVHISTORY_DAO_CONTENT_TYPE}}",
	
	/**
	 * @property {Ext.data.Store} _store The store containing the navhistory events
	 * @private
	 */
	
	/**
	 * Returns the store listing the entries of history
	 * @return {Ext.data.Store} The history store
	 */
	getStore: function()
	{
		if (!this._store)
		{
			this._store = Ext.create('Ext.data.Store', {
				model: 'Ametys.navhistory.HistoryDAO.HistoryEntry',
				sortOnLoad: true,
				// autoSync: true, Bug of ExtJS 6.0.0 https://www.sencha.com/forum/showthread.php?301316-Bug-in-store.removeAt&p=1101096#post1101096
				proxy: { type: 'memory' },
				sorters: [{property: 'date', direction:'DESC'}]	
			});
		}
		return this._store;
	},
	
	/**
     * Execute the action attached to this record
     * @param {String} recordId The id of record
     * @param {Number} index The action of index
     */
	executeEntryAction: function (recordId, index)
    {
    	var record = this.getStore().getById(recordId);
    	if (record != null)
    	{
    		var action = record.get('action');
    		if (Ext.isFunction(action))
    		{
    			action();
    		}
    	}
    },
	
	/**
	 * Add an entry into the history store
	 * @param {String} config The configuration object of the entry. Has the following parameters:
	 * @param {String} config.id (required) The unique identifier of the event in the history. If the entry of this id already exist, it will be replaced.
	 * @param {String} config.objectId The id of concerned object. This id will be use for remove entry from history.
	 * @param {String} config.label (required) The entry's label
	 * @param {String} config.description The description of the entry
	 * @param {String} config.iconSmall The full path to the small icon (in 16x16 pixels)
	 * @param {String} config.iconMedium The full path to the medium icon (in 32x32 pixels)
	 * @param {String} config.iconLarge The full path to the large icon (in 48x48 pixels)
	 * @param {Function} config.action The function to run when double-clicking on the history's entry
	 */
	addEntry: function (config)
	{		
		var existingItemIndex = this.getStore().find('id', config.id);
		if (existingItemIndex != -1)
		{
			this.getStore().removeAt(existingItemIndex);
		}
		
		this.getStore().add({
			id: config.id,
			objectId: config.objectId || Ext.id(),
			label: config.label,
			date: new Date(),  
			description: config.description || '',
			iconGlyph: config.iconGlyph,
			iconSmall: config.iconSmall,
			iconMedium: config.iconMedium,
			iconLarge: config.iconLarge,
			type: config.type || '',
			action: config.action || Ext.emptyFn,
			actionFunction: "Ametys.navhistory.HistoryDAO.executeEntryAction"
		});
	},
	
	/**
	 * Remove all entries from history store with the given object id
	 * @param {String} objectId The object id to remove
	 */
	removeEntry: function (objectId)
	{
		var existingItemIndex = this.getStore().findExact('objectId', objectId);
		while (existingItemIndex != -1)
		{
			this.getStore().removeAt(existingItemIndex);
			
			existingItemIndex = this.getStore().findExact('objectId', objectId);
		}
	},
	
	/**
	 * Remove an entry from history store if exists
	 * @param {String[]} objectIds The entry id
	 */
	removeEntries: function (objectIds)
	{
		for (var i=0; i < objectIds.length; i++)
		{
			this.removeEntry(objectIds[i]);
		}
	},
	
	/**
	 * Clear all entries if the history
	 */
	clearHistoryEvents: function()
	{
		this.getStore().removeAll();
	}
	
});

