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
 * This tool lists the issued notifications. It allows the unit deletion and the deletion of all notifications. 
 * @private
 */
Ext.define('Ametys.plugins.coreui.notification.NotificationTool', {
	extend: 'Ametys.tool.Tool',
	
	/**
	 * @private
	 * @property {Ext.data.Store} _store the notifications' store
	 */
	
	/**
	 * @private
	 * @property {Object} _storeListeners the listeners of the tool attached to the notificator's store
	 */
	
	/**
	 * @private
	 * @property {Ext.container.Container} _container the main container of the tool
	 */
	
	/**
	 * @private
	 * @property {Ext.container.Container} _notificationsContainer the container for the notifications
	 */
	
	/**
	 * @private
	 * @property {Ext.Button} _deleteAllButton the button allowing the deletion of all notifications
	 */
	
	getMBSelectionInteraction: function() 
	{
		return Ametys.tool.Tool.MB_TYPE_NOSELECTION;
	},
	
	createPanel: function()
	{
		// Get the store
		this._store = Ext.getCmp('ribbon').getNotificator().getStore();
		
		var notificationPanels = this._getNotificationPanels();
		
		this._notificationsContainer = Ext.create('Ext.container.Container', {
			flex: 1, // set height, height '100%' => layout run failed
			width: '100%',
			scrollable: true,
        	items: notificationPanels
		});
		
		this._deleteAllButton = Ext.create('Ext.Button', {
		   text: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_NOTIFICATIONS_DELETE_ALL_LABEL'/>",
    	   tooltip: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_NOTIFICATIONS_DELETE_ALL_TOOLTIP'/>",
    	   handler: Ext.bind(this._deleteAllNotifications, this),
    	   disabled: true
		});
		
		this._container = Ext.create('Ext.container.Container', {
			
			layout: 'vbox',
			
			items: [
		        {
	        	    xtype: 'toolbar',
	        	    width: '100%',
				    dock: 'top',
				    items: [
						'->',
						this._deleteAllButton
					]
		        },
		        this._notificationsContainer
	        ]
		});
		
		if (notificationPanels.length == 0)
		{
			this._notificationsContainer.update("<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_NOTIFICATIONS_NO_NOTIFICATIONS'/>");
		}
		else
		{
			this._deleteAllButton.enable();
		}
		
		return this._container;
	},
	
	setParams: function(params)
	{
		this._storeListeners = this._store.on({
			add: Ext.bind(this._notificationAdded, this),
			remove: Ext.bind(this._notificationRemoved, this),
			update: Ext.bind(this._notificationUpdated, this),
			destroyable: true,
			scope: this
		});
		
		this.callParent(arguments);
	},
	
	close: function(manual)
	{
		Ext.destroy(this._storeListeners);
		this.callParent(arguments);
	},
	
	destroy: function()
	{
		Ext.destroy(this._storeListeners);
		this.callParent(arguments);
	},
	
	/**
	 * @private
	 * Compute the list of notification panels sorted by descending date from the store
	 */
	_getNotificationPanels: function()
	{
		var panels = [];
	
		this._store.getData().each(function(notification, index) {
			var panel = Ext.create('Ext.panel.Panel', this._getNotificationPanelConfig(notification));
			panels.push(panel);
		}, this);
		
		return panels;
	},
	
	/**
	 * Get the configuration object for a notification
	 * @param {Ext.data.Record} notification the notification record
	 */
	_getNotificationPanelConfig: function(notification)
	{
		var me = this;
		
		return {
			
			title: notification.get('title'),
			html: notification.get('description'),
			
			width: '100%',
			
			itemId: notification.get('id'),
			
			// FIXME 
			cls: notification.get('read') ? 'notification-read' : 'notification-unread',
			
            listeners: {
				click: Ext.bind(this._setReadIfNot, this, [notification], false),
				scope: this,
				element: 'el', // bind the click listener handler on the dom of the panel after the component is rendered
				single: true
            }, 
                
			tools: [{
				type: 'close',
				tooltip: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_NOTIFICATIONS_DELETE_TOOLTIP'/>",
				handler: Ext.bind(me._deleteNotification, me, [notification], false)
			}]
		};
	},
	
	/**
	 * @private
	 * Delete the notification
	 * @param {Ext.data.Record} the notification to delete
	 */
	_deleteNotification: function(record)
	{
		this._store.remove(record);
		this._store.commitChanges();
	},
	
	/**
	 * @private
	 * Delete all the notifications
	 */
	_deleteAllNotifications: function()
	{
		this._store.getData().each(function(record) {
			this._store.remove(record);
		}, this);
		
		this._store.commitChanges();
		
		this._deleteAllButton.disable();
	},
	
	/**
	 * @private
	 * Set the record as read if it wasn't the case already
	 * @param {Ext.data.Record} record the notification's record 
	 */
	_setReadIfNot: function(record)
	{
		if (!record.get('read'))
		{
			record.set('read', true);
			record.commit();
		}
	},
	
	/**
	 * @private
	 * Listener function invoked whenever records have been added to the store.
	 * Adds the panel to the main panel at the appropriate position
     * @param {Ext.data.Store} store The store.
     * @param {Ext.data.Model[]} records The records that were added.
     * @param {Number} index The index at which the records were inserted.
	 */
	_notificationAdded: function(store, records, index)
	{
		var newPanel = Ext.create('Ext.panel.Panel', this._getNotificationPanelConfig(records[0]));
		
		// Always insert new notifications on top
		this._notificationsContainer.insert(0, newPanel);
		
		// Remove 'No notifications' text and enable the 'delete all' button
		this._deleteAllButton.enable();
		this._notificationsContainer.update('');
	},
	
    /**
     * @private
     * Listener function invoked a notification record has been updated.
     * @param {Ext.data.Store} store the store
     * @param {Ext.data.Model} record The Model instance that was updated
     * @param {String} operation The update operation being performed. Value may be one of:
     *
     *     Ext.data.Model.EDIT
     *     Ext.data.Model.REJECT
     *     Ext.data.Model.COMMIT
     * @param {String[]} modifiedFieldNames Array of field names changed during edit.
     * @param {Object} details An object describing the change. See the
     * {@link Ext.util.Collection#event-itemchange itemchange event} of the store's backing collection
     */
	_notificationUpdated: function(store, record, operation, modifedFieldNames, details)
	{
		var modifiedPanel = this._notificationsContainer.getComponent(record.get('id'));
		
		if (!record.get('read'))
		{
			this._notificationsContainer.removeCls('notification-unread');
			this._notificationsContainer.addCls('notification-read');
		}
	},
	
    /**
     * @private
     * Listener function invoked whenever one or more records have been removed from the store.
     * @param {Ext.data.Store} store The Store object
     * @param {Ext.data.Model[]} records The records that were removed
     * @param {Number} index The index at which the records were removed.
     */
	_notificationRemoved: function(store, records, index)
	{
		Ext.Array.each(records, function(record) {
			this._notificationsContainer.remove(this._notificationsContainer.getComponent(record.get('id')));
		}, this);
		
		if (this._notificationsContainer.items.length == 0)
		{
			this._deleteAllButton.disable();
			this._notificationsContainer.update("<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_NOTIFICATIONS_NO_NOTIFICATIONS'/>");
		}
	}
});

/**
 * Create an alias in order to ease the issuing of notifications
 */
Ext.override(Ametys, {

   /**
    * @member Ametys
    * @method notify 
    * @since Ametys Runtime 4.0
    * @ametys
    * Simple alias for #Ametys.ui.fluent.ribbon.Ribbon.Notificator.notify.
    */
	notify: function(config)
	{
		Ext.getCmp('ribbon').getNotificator().notify(config);
	}
});