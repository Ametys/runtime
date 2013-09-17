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
Ext.define('Ametys.runtime.profile.ProfilesTreePanel.NodeEntry', {
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
			mapping: '@name',
			convert: function (v, record) {
				if (record.get('type') == 'profile')
				{
					return ' <b>' + v + '<b>';
				}
				else if (record.get('type') == 'user')
				{
					return v + ' (' + record.get('login') + ')';
				}
				else
				{
					return v;
				}
			}
		},
		{name: 'name', mapping: '@name'},
		{name: 'login', mapping:'@login'},
		{name: 'groupId', mapping:'@groupId'},
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
			mapping: '@inherit', 
			convert: function (v, record) {
				return record.get('inherit') ? 'inherit' : '';
			}
		},
		{
			name: 'iconCls', 
			defaultValue: '',
			convert: function (v, record) {
				return 'tree-icon-' + record.get('type');
			}
		},
		{
			name: 'leaf', 
			defaultValue: false,
			convert: function (v, record) {
				return record.get('type') != 'profile';
			}
		}
	]
			
});
