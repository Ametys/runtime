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
 * This class is the model for nodes of a profile tree. See {@link Ametys.runtime.profiles.ProfilesTreePanel}
 */
Ext.define('Ametys.runtime.profiles.ProfilesTreePanel.NodeEntry', {
	extend: 'Ext.data.TreeModel',
	
	fields: 
	[
	 	{name: 'type', mapping: '@type'},
	 	{
	 		name: 'id', 
	 		
	 		convert: function (v) {
	 			return Ext.id();
	 		}
	 	},
		{
			name: 'text', 
			sortType: Ext.data.SortTypes.asNonAccentedUCString,
			calculate: function (data) 
            {
                if (data && data.type == 'profile')
				{
					return ' <b>' + data.name + '<b>';
				}
				else if (data && data.type == 'user')
				{
					return Ametys.plugins.core.users.UsersDAO.renderUser(data.login, data.populationLabel, data.sortablename);
				}
				else
				{
					return data.name;
				}
				
			}
		},
		{name: 'name', mapping: '@name'},
		{name: 'sortablename', mapping: '@sortablename'}, // only for users
		{
            name: 'displayName', // only for users
            sortType: Ext.data.SortTypes.asNonAccentedUCString,
            calculate: function (data) 
            {
                if (data && data.type == 'user')
                {
                    return Ametys.plugins.core.users.UsersDAO.renderUser(data.login, data.populationLabel, data.sortablename);
                }
                else
                {
                    return data.name;
                }
            }
        },
		{name: 'login', mapping:'@login'},
		{name: 'population', mapping:'@population'},
		{name: 'populationLabel', mapping:'@populationLabel'},
		{name: 'groupId', mapping:'@groupId'},
		{name: 'groupDirectory', mapping:'@groupDirectory'},
		{
			name: 'profileId', 
			mapping:'@profileId',
			convert: function (v, record) {
				return v != '' ? 'profile-' + v : null;
			}
		},
		{name: 'context', mapping: '@context'},
		{name: 'inherit', mapping: '@inherit', type: 'boolean'},
		{
			name: 'cls', 
			calculate: function(data) {
				var cls = [];
				if (data.inherit)
				{
					cls.push('disabled');
				}
				
				if (data.type == 'user')
				{
					cls.push('a-tree-icon-user')
				}
				
				return cls.join(' ');
			}
		},
		{
			name: 'icon', 
			calculate: function(data) {
				if (data && data.type == 'user')
		        {
		            return Ametys.getPluginDirectPrefix('core-ui') + '/user/' + data.population + '/' + data.login + '/image_16';
		        }
		        else
		        {
		            return Ametys.getPluginResourcesPrefix('core-ui') + '/img/profiles/' + data.type + '_16.png';
		        }
			}
		},
		{
			name: 'leaf', 
			calculate: function(data) {
				return data ? (data.type == 'user' || data.type == 'group') : '';
			}
		}
	]
			
});
