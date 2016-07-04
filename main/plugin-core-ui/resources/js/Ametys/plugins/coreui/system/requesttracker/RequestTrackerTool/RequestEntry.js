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
 * This class is the model for entries in the grid of the request tracker tool
 * @private
 */
Ext.define("Ametys.plugins.coreui.system.requesttracker.RequestTrackerTool.RequestEntry", {
	extend: 'Ext.data.Model',
	
    fields: [
		{name: 'id'},
		{name: 'type'},
		{name: 'date', type: 'date', dateFormat: 'Y-m-d'},
		{name: 'duration'},
		{name: 'return'},
		{name: 'size', type: 'int'},
        {name: 'errors', type: 'int'},
		{name: 'messages'},
		{name: 'response'}
    ]
});