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
 * This class is a container for very small buttons that goes together visually
 */
Ext.define("Ametys.ui.fluent.ribbon.Ribbon.Notificator", {
    extend: "Ext.panel.Tool",
    alias: 'widget.ametys.ribbon-notificator',
    
    /**
     * @cfg {String} type='notification' @inheritdoc
     */
    type: 'notification',
    
    /**
     * @cfg {Object/Ext.data.Store} store The Ametys.ui.fluent.ribbon.Ribbon.Notificator.Notification store. An empty one will be automatically created.
     */
    /**
     * @property {Ext.data.Store} _store See #cfg-store.
     */
    
    constructor: function(config)
    {
        config = config || {};
        
        this.callParent(arguments);
        
        if (config.store && config.store.isStore)
        {
            this._store = config.store;
        }
        else
        {
            this._store = Ext.create("Ext.data.Store", Ext.apply({
                model: 'Ametys.ui.fluent.ribbon.Ribbon.Notificator.Notification',
                autoSync: true,
                proxy: {
                    type: 'memory'
                },
                sorters: [{ property: 'creationDate', direction: 'DESC' }]
            }, config.store));
        }
        
        this._notificationChanged = Ext.Function.createBuffered(this._notificationChanged, 10);
        this._store.on('datachanged', this._notificationChanged, this);
        this._store.on('update', this._notificationChanged, this);
    },
    
    /**
     * @private
     * Listener when notification has changed to update badge
     * @param {Ext.data.Store} store The store
     * @param {Ametys.ui.fluent.ribbon.Ribbon.Notificator.Notification} notification The modified notification
     * @param {String} operation The update operation being performed. Value may be one of:
     *
     *     Ext.data.Model.EDIT
     *     Ext.data.Model.REJECT
     *     Ext.data.Model.COMMIT
     * @param {String[]} modifiedFieldNames Array of field names changed during edit.
     * @param {Object} details An object describing the change. See the
     * {@link Ext.util.Collection#event-itemchange itemchange event} of the store's backing collection
     */
    _notificationChanged: function(store, notification, operation, modifiedFieldNames, details)
    {
        var unreadAndClosedNotification = 0;
        var biggerType = 'info';
        
        store.getData().each(function(notif, index) {
            if (!notif.get('displayed') && !notif.get('read'))
            {
                unreadAndClosedNotification++;
                
                switch (notif.get('type'))
                {
                    case 'error': 
                        biggerType = 'error';
                        break;
                    case 'warn': 
                        biggerType = biggerType == 'info' ? 'warn' : biggerType
                        break;
                    case 'info':
                    default: 
                        break;
                }
            }
        });
        
        // Setting unread counter on the badge
        if (unreadAndClosedNotification == 0)
        {
            this.setBadgeText(null);
        }
        else if (unreadAndClosedNotification < 100)
        {
            this.setBadgeText(unreadAndClosedNotification);
        }
        else
        {
            this.setBadgeText("+99");
        }
        
        // Update badge aspect regarding the type
        this.removeCls(['warn-badge', 'error-badge']);
        switch (biggerType)
        {
            case 'error':
                this.addCls('error-badge');
                break;
            case 'warn':
                this.addCls('warn-badge');
                break;
            case 'info':
            default:
                break;
        }
    },
    
    /**
     * Execute the action attached to this record
     * @param {String} recordId The id of record
     * @param {Number} index The action of index
     */
    executeAction: function (recordId, index)
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
     * Notify a new message.
     * @param {Object/Ametys.ui.fluent.ribbon.Ribbon.Notificator.Notification} config The new notification
     */
    notify: function(config)
    {
    	// add the creation date
    	Ext.apply(config, {
    			id: Ext.id(), 
    			creationDate: Ext.Date.format(new Date(), Ext.Date.patterns.ISO8601DateTime),
    			actionFunction: "Ext.getCmp('" + this.getId() + "').executeAction"
    	});
    	
        var newNotification = this.getStore().add(config)[0];
        
        Ext.create("Ametys.ui.fluent.ribbon.Ribbon.Notificator.Toast", {
             endTarget: this.getId(),
             notification: newNotification,
             
             align: 'tr',
             closable: true,
             anchor: this.ownerCt.ownerCt.floatParent.nextSibling()
        }).show();
    },
    
    /**
     * Get the internal notification store
     * @return {Ext.data.Store} The store of Ametys.ui.fluent.ribbon.Ribbon.Notificator.Notification 
     */
    getStore: function()
    {
        return this._store;
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
    * Simple alias for Ametys.ui.fluent.ribbon.Ribbon.Notificator#notify.
    */
	notify: function(config)
	{
		Ext.getCmp('ribbon').getNotificator().notify(config);
	}
});