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
 * The model of a notification.
 * The main fields to set:
 * 
 * * {String} title The notification title
 * * {String} description The notification description
 * * {String} type=info The notification type between 'info', 'warn', 'error'.
 * 
 * Other fields:
 * 
 * * {Boolean} displayed Is the notification currently visible on a toast
 * * {Boolean} read Was the notification already read?
 */
Ext.define("Ametys.ui.fluent.ribbon.Ribbon.Notificator.Notification", {
    extend: 'Ext.data.Model',
    
    fields: [
     	{name: 'id', type: 'string'},
        {name: 'title',  type: 'string'},
        {name: 'description',  type: 'string'},
        {name: 'type',  type: 'string', defaultValue: 'info'},
        {name: 'icon', type: 'string'}, // if no icon specified, the default icon for the selected type will be used
        {name: 'creationDate', type: 'date', dateFormat: Ext.Date.patterns.ISO8601DateTime},
        {name: 'displayed', type: 'boolean'},
        {name: 'read', type: 'boolean', defaultValue: false}
    ]
});
