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
 * This singleton class enable to notify some server-side configured notifications
 * to the administrator at startup.
 * See {@link #notify}.
 */
Ext.define('Ametys.plugins.admin.notificator.AdministratorNotificator', {
    singleton: true,
    
    /**
     * Notify the administrator notifications.
     */
    notify: function()
    {
        Ametys.data.ServerComm.callMethod({
            role: "org.ametys.runtime.plugins.admin.notificator.AdministratorNotificatorExtensionPoint",
            methodName: "getNotifications",
            parameters: [],
            callback: {
                handler: this._getNotificationsCb,
                scope: this
            },
            waitMessage: false
        });
    },
    
    /**
     * @private
     * Callback method called after calling the server to retrieve the notifications to send
     * @param {Object[]} notifications The server response: the notifications to send.
     * @param {String} notifications.type The type of notification
     * @param {String} notifications.title The title of the notificattion
     * @param {String} notifications.message The description of the notification
     * @param {String} notifications.iconGlyph The icon glyph of the notification
     * @param {String} notifications.action The action of the notification
     */
    _getNotificationsCb: function(notifications)
    {
        Ext.Array.forEach(notifications, function(notification) {
            Ametys.notify({
                type: notification.type,
                iconGlyph: notification.iconGlyph,
                title: notification.title,
                description: notification.message,
                action: eval(notification.action)
            });
        }, this);
    }
});