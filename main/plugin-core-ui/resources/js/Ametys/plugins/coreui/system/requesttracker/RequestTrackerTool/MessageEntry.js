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
 * This class is the model for entries in the sub grid of the request tracker tool
 * @private
 */
Ext.define("Ametys.plugins.coreui.system.requesttracker.RequestTrackerTool.MessageEntry", {
	extend: 'Ext.data.Model',
	
    fields: [
        {name: 'id'},
        {
        	name: 'readableCallType', //True if the value to display is 'PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_READABLE_VAL1', 
        								//false if 'PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_READABLE_VAL2'
        	
        	calculate: function(data) 
        	{
        		return (data.url == "client-call" && data.plugin == "core-ui");
        	}
        },
        {
        	name: 'readableCallValue',
        	
        	calculate: function(data) 
        	{
	        	if (data.url == "client-call" && data.plugin == "core-ui")
				{
                    function shortValue(s)
                    {
                        return s.substring(s.lastIndexOf(".") + 1);
                    }
					return shortValue(data.message.parameters.id ? data.message.parameters.id : data.message.parameters.role) + "#" + data.message.parameters.methodName;
				}
				else
				{
					return (data.workspace ? ("/_" + data.workspace) : data.plugin ? ('/plugins/' + data.plugin) : '') + "/" + data.url;
				}
        	}
        },
        {name: 'plugin'},
        {name: 'workspace'},
        {name: 'url'},
        {name: 'clientCallRole'},
        {name: 'clientCallId'},
        {name: 'clientCallMethod'},
        {name: 'url'},
        {name: 'priority'},
        {name: 'type'},
        {name: 'message'},
        {name: 'status'},
        {name: 'response'},
        {name: 'callstack'}
    ]
});