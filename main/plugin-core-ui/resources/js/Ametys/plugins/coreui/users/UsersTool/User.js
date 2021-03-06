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
 * This class is the model for entries users
 * @private
 */
Ext.define("Ametys.plugins.coreui.users.UsersTool.User", {
	extend: 'Ext.data.Model',
	
	fields: [
		{name: 'login'},
        {name: 'population'},
        {name: 'populationLabel'},
        {name: 'directory'},
		{name: 'lastname', sortType: Ext.data.SortTypes.asNonAccentedUCString},
		{name: 'firstname', sortType: Ext.data.SortTypes.asNonAccentedUCString},
		{name: 'email'},
		{name: 'fullname', sortType: Ext.data.SortTypes.asNonAccentedUCString},
		{name: 'sortablename', sortType: Ext.data.SortTypes.asNonAccentedUCString},
		{
			name: 'displayName',
			sortType: Ext.data.SortTypes.asNonAccentedUCString,
			calculate: function (data)
			{
                return data.sortablename;
			}
		}
	]
});