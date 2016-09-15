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
 * The model of a notification.
 */
Ext.define("Ametys.ui.fluent.ribbon.Ribbon.Notificator.Notification", {
    extend: 'Ext.data.Model',

    /** @cfg {String} title (required) The notification title */
    /** @cfg {String} description (required) The notification description */
    /** @cfg {Function} action The function to execute when clicking on '&lt;a&gt;' elements of description */
    /** @cfg {String} type=info The notification type between 'info', 'warn', 'error' */
    /** @cfg {Boolean} displayed=false Is the notification currently visible on a toast */
    /** @cfg {Boolean} read=false Was the notification already read? */
    
    fields: [
     	{name: 'id', type: 'string'},
        {name: 'title',  type: 'string'},
        {name: 'action'},
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
        {name: 'type',  type: 'string', defaultValue: 'info'},
        {name: 'iconGlyph', type: 'string'},
        {name: 'icon', type: 'string'}, // if no icon specified, the default icon for the selected type will be used
        {name: 'creationDate', type: 'date', dateFormat: Ext.Date.patterns.ISO8601DateTime},
        {name: 'displayed', type: 'boolean'},
        {name: 'read', type: 'boolean', defaultValue: false}
    ]
});
