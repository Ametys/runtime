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
 * This tool lists the issued notifications in a timeline. It allows to filter notifications by type.
 * @private
 */
Ext.define('Ametys.plugins.coreui.notification.NotificationTool', {
	extend: 'Ametys.tool.Tool',
	
	/**
	 * @private
	 * @property {Ametys.plugins.coreui.timeline.Timeline} _timeline the timeline of activities feed
	 */
	
	/**
	 * @private
	 * @property {Object} _notificationStoreListeners the listeners of the tool attached to the notificator's store
	 */
	
	getMBSelectionInteraction: function() 
	{
		return Ametys.tool.Tool.MB_TYPE_NOSELECTION;
	},
	
	createPanel: function()
	{
		this._timeline = Ext.create('Ametys.timeline.Timeline', {
			scrollable: true,
			
			dockedItems: this._getToolBarConfig()
		});
		
		return this._timeline;
	},
	
	/**
	 * @private
	 * Get the store of notifications
	 * @return {Ext.data.Store} the store of notifications
	 */
	_getNotificationStore: function ()
	{
		return Ext.getCmp('ribbon').getNotificator().getStore();
	},
	
	/**
	 * @private
	 * Get the store of navigation history
	 * @return {Ext.data.Store} the store of navigation history
	 */
	_getNavigationHistoryStore: function ()
	{
		return Ametys.navhistory.HistoryDAO.getStore();
	},
	
	/**
	 * @private
	 * Get configuration for toolbar
	 * @return {Object} the config object
	 */
	_getToolBarConfig: function ()
	{
		return [{
			dock: 'top',
			xtype: 'toolbar',
            layout: { 
                type: 'hbox',
                align: 'stretch'
            },
			border: false,
			defaultType: 'button',
			defaults: {
				cls: 'a-btn-light',
				enableToggle: true
			},
			items: [
					{
						text: "{{i18n PLUGINS_CORE_UI_TOOLS_NOTIFICATIONS_FILTER_INFO_LABEL}}",
						tooltip: "{{i18n PLUGINS_CORE_UI_TOOLS_NOTIFICATIONS_FILTER_INFO_DESC}}",
						filterName: 'info',
						pressed: true,
						toggleHandler: Ext.bind (this._filter, ['info'], true)
					},
					{
						text: "{{i18n PLUGINS_CORE_UI_TOOLS_NOTIFICATIONS_FILTER_WARN_LABEL}}",
						tooltip: "{{i18n PLUGINS_CORE_UI_TOOLS_NOTIFICATIONS_FILTER_WARN_DESC}}",
						pressed: true,
						filterName: 'warn',
						toggleHandler: Ext.bind (this._filter, this, ['warn'], true)
					},
					{
						text: "{{i18n PLUGINS_CORE_UI_TOOLS_NOTIFICATIONS_FILTER_ERROR_LABEL}}",
						tooltip: "{{i18n PLUGINS_CORE_UI_TOOLS_NOTIFICATIONS_FILTER_ERROR_DESC}}",
						pressed: true,
						filterName: 'error',
						toggleHandler: Ext.bind (this._filter, this, ['error'], true)
					},
					{
						text: "{{i18n PLUGINS_CORE_UI_TOOLS_NOTIFICATIONS_FILTER_NAVHISTORY_LABEL}}",
						tooltip: "{{i18n PLUGINS_CORE_UI_TOOLS_NOTIFICATIONS_FILTER_NAVHISTORY_DESC}}",
						pressed: true,
						filterName: 'navhistory',
						toggleHandler: Ext.bind (this._filter, this, ['navhistory'], true)
					},
                    {
                        xtype: 'tbspacer',
                        flex: 0.0001
                    },
					{
                    	text: "{{i18n PLUGINS_CORE_UI_TOOLS_NOTIFICATIONS_DELETE_LABEL}}",
						tooltip: "{{i18n PLUGINS_CORE_UI_TOOLS_NOTIFICATIONS_DELETE_DESC}}",
						handler: this._deleteAll,
						scope: this,
						hidden: true,
						enableToggle: false,
						toggleGroup: null
					}
			]
		},{
			dock: 'top',
			ui: 'tool-hintmessage',
			itemId: 'no-notification-hint',
			xtype: 'component',
			hidden: true,
			html: "{{i18n PLUGINS_CORE_UI_TOOLS_NOTIFICATIONS_NO_NOTIFICATIONS}}"
		}];
	},
	
	setParams: function(params)
	{
		this.callParent(arguments);
		
		var data = [];
		
		this._loadNotifications(data);
		this._loadNavigationHistory(data);
		
		this._timeline.getStore().loadData(data);
		
		if (data.length == 0)
		{
			this._timeline.getComponent('no-notification-hint').show();
		}
		else
		{
			this._timeline.getComponent('no-notification-hint').hide();
		}
		
		
		// Register listeners on notification store
		this._notificationStoreListeners = this._getNotificationStore().on({
			add: Ext.bind(this._onNotificationAdded, this),
			destroyable: true,
			scope: this
		});
		
		// Register listeners on navigation history store
		this._navHistoryStoreListeners = this._getNavigationHistoryStore().on({
			add: Ext.bind(this._onEventHistoryAdded, this),
			destroyable: true,
			scope: this
		});
	},
	
	/**
	 * @private
	 * Filter the timeline 
	 * @param {Ext.Button} The filter button
	 * @param {Boolean} state the button's state
	 * @param {String} type The type of item to filter
	 */
	_filter: function (btn, state, type)
	{
		var me = this;
		this._activeTypes = [];
		
		this._timeline.child("*[dock='top']").items.each (function (item) {
			if (item.filterName && item.pressed)
			{
				me._activeTypes.push(item.filterName);
			}
		});
		
		// this._timeline.getStore().clearFilter();
		this._timeline.getStore().filterBy(this._filterByType, this);
	},
	
	/**
	 * @private
	 * The filter function to filter records by active types
	 * @param {Ext.data.Model} record The timeline record
	 */
	_filterByType: function (record)
	{
		return Ext.Array.contains (this._activeTypes, record.get('type'));
	},
	
	/**
	 * @private
	 * Clear the timeline
	 */
	_deleteAll: function ()
	{
		this._timeline.getStore().removeAll();
		this._timeline.getComponent('no-notification-hint').show();
	},
	
	/**
	 * @private
	 * Load the notifications in timeline
	 * @param {Object} data the timeline records
	 */
	_loadNotifications: function (data)
	{
		var me = this;
		var readRecords = [];
		
		this._getNotificationStore().getData().each (function(record, index, length) {
			data.push(me._convertNotification2Timeline(record));
			record.set('read', true);
			record.commit();
		});
	},
	
	/**
	 * @private
	 * Load the navigation history events in timeline
	 * @param {Object} data the timeline records
	 */
	_loadNavigationHistory: function (data)
	{
		var me = this;
		this._getNavigationHistoryStore().getData().each (function(record, index, length) {
			data.push(me._convertNavHistory2Timeline(record));
		});
	},
	
	/**
	 * @private
	 * Convert a notification record to a timeline record
	 * @param {Ext.data.Model} record the notification
	 * @return {Object} the configuration of a timeline record
	 */
	_convertNotification2Timeline: function (record)
	{
		return {
			id: this.getId() + '-' + record.getId(),
			type: record.get('type') || 'info',
			date: record.get('creationDate'),
			username: Ametys.getAppParameter('user').fullname,
			profileImg: Ametys.getPluginDirectPrefix('core-ui') + '/current-user/image_46',
			icon: record.get('icon') ? Ametys.CONTEXT_PATH + record.get('icon') : null,
		    iconGlyph: record.get('iconGlyph'),
			text: '<strong>' + record.get('title') + '</strong><br/>' + record.get('description'),
			topText: '',
			comment: ''
		}
	},
	
	/**
	 * @private
	 * Convert a notification record to a timeline record
	 * @param {Ext.data.Model} record the notification
	 * @return {Object} the configuration of a timeline record
	 */
	_convertNavHistory2Timeline: function (record)
	{
		return {
			id: this.getId() + '-' + record.getId(),
			type: 'navhistory',
			date: record.get('date'),
			username: Ametys.getAppParameter('user').fullname,
			profileImg: Ametys.CONTEXT_PATH + '/plugins/core-ui/current-user/image_46',
			icon: record.get('iconGlyph') != null ? null : Ametys.CONTEXT_PATH + record.get('iconMedium') ,
			iconGlyph: record.get('iconGlyph'),
			action: record.get('action'),
			text: '<strong>' + Ametys.getAppParameter('user').fullname + '</strong><br/>' + record.get('description'),
			topText: '',
			comment: ''
		}
	},
	
	/**
	 * @private
	 * Listener function invoked whenever a new notification has been created
	 * Add the notification in timeline
     * @param {Ext.data.Store} store The notification store.
     * @param {Ametys.ui.fluent.ribbon.Ribbon.Notificator.Notification[]} records The added notifications.
     * @param {Number} index The index at which the records were inserted.
	 */
	_onNotificationAdded: function (store, records, index)
	{
		var me = this;
		var items = [];
		
		Ext.Array.each (records, function (record) {
			items.push(me._convertNotification2Timeline(record));
			record.set('read', true);
			record.commit();
		});
		
		this._timeline.getStore().add(items);
		this._timeline.getComponent('no-notification-hint').hide();
	},
	
	/**
	 * @private
	 * Listener function invoked whenever a new event has been created on navigation history
	 * Add the event in timeline
     * @param {Ext.data.Store} store The history store.
     * @param {Ametys.navhistory.HistoryDAO.HistoryEntry[]} records The added events.
     * @param {Number} index The index at which the records were inserted.
	 */
	_onEventHistoryAdded: function (store, records, index)
	{
		var me = this;
		var items = [];
		
		Ext.Array.each (records, function (record) {
			items.push(me._convertNavHistory2Timeline(record));
			record.set('read', true);
			record.commit();
		});
		
		this._timeline.getStore().add(items);
		this._timeline.getComponent('no-notification-hint').hide();
	},
	
	onClose: function(hadFocus)
	{
		Ext.destroy(this._notificationStoreListeners);
		Ext.destroy(this._navHistoryStoreListeners);
		this.callParent(arguments);
	}
});

