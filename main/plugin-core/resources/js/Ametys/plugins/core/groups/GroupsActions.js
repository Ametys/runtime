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
 * Singleton class defining the actions related to groups.
 * @private
 */
Ext.define('Ametys.plugins.core.groups.GroupsActions', {
	singleton: true,
	
	/**
	 * Creates a group.
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	add: function (controller)
	{
		var groupsManagerRole = controller.getInitialConfig('groups-manager-role');
		var groupMessageTargetType = controller.getInitialConfig('group-message-target-type') || Ametys.message.MessageTarget.GROUP;
		
		Ametys.plugins.core.groups.EditGroupHelper.add(groupsManagerRole, groupMessageTargetType);
	},
	
	/**
	 * Rename a group's properties
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	edit: function(controller)
	{
		var groupTarget = controller.getMatchingTargets()[0];
		if (groupTarget != null)
		{
			var groupsManagerRole = controller.getInitialConfig('groups-manager-role');
			var groupMessageTargetType = controller.getInitialConfig('group-message-target-type') || Ametys.message.MessageTarget.GROUP;
			
			Ametys.plugins.core.groups.EditGroupHelper.edit(groupTarget.getParameters().id, groupsManagerRole, groupMessageTargetType);
		}
	},
	
	/**
	 * Delete groups
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	'delete': function (controller)
	{
		var groupTargets = controller.getMatchingTargets();
		if (groupTargets != null && groupTargets.length > 0)
		{
			Ametys.Msg.confirm("{{i18n plugin.core:PLUGINS_CORE_GROUPS_DELETE_LABEL}}",
				"{{i18n plugin.core:PLUGINS_CORE_GROUPS_DELETE_CONFIRM}}",
				Ext.bind(this._doDelete, this, [groupTargets, controller], 1),
				this
			);
		}
	},

	/**
	 * Callback function invoked after the 'delete' confirm box is closed
	 * @param {String} buttonId Id of the button that was clicked
	 * @param {Ametys.message.MessageTarget[]} targets The groups message targets
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function 
	 * @private
	 */
	_doDelete: function(buttonId, targets, controller)
	{
		if (buttonId == 'yes')
		{
			var groupsManagerRole = controller.getInitialConfig('groups-manager-role');
			var groupMessageTargetType = controller.getInitialConfig('group-message-target-type') || Ametys.message.MessageTarget.GROUP;
			
			var ids = Ext.Array.map(targets, function(target) {
				return target.getParameters().id;
			});
			
			if (ids.length > 0)
			{
				Ametys.plugins.core.groups.GroupsDAO.deleteGroups([ids, groupsManagerRole, groupMessageTargetType], null, {});
			}
		}
	},
	
	/**
	 * Add users to a group.
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	addUsers: function(controller)
	{
		var groupTargets = controller.getMatchingTargets();
		if (groupTargets != null && groupTargets.length > 0)
		{
			Ametys.helper.SelectUser.act({
				callback: Ext.bind(this._selectUsersCb, this, [groupTargets[0], controller], 1),
				usersManagerRole: controller.getInitialConfig('users-manager-role')
			});
		}
	},
	
	/**
	 * Add the selected users to group.
	 * @param {Object} users The map of selected users to add (key: user id, value: user name).
	 * @param {Ametys.message.MessageTarget} groupTarget The group target
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The button's controller
	 * @private
	 */
	_selectUsersCb: function(users, groupTarget, controller)
	{
		var groupsManagerRole = controller.getInitialConfig('groups-manager-role');
		var groupMessageTargetType = controller.getInitialConfig('group-message-target-type') || Ametys.message.MessageTarget.GROUP;
		
		var logins = [];
		for (var i in users)
		{
			logins.push(i);
		}
		
		Ametys.plugins.core.groups.GroupsDAO.addUsersGroup([groupTarget.getParameters().id, logins, groupsManagerRole, groupMessageTargetType], null, {});
	},
	
	/**
	 * Remove users to a group.
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	removeUsers: function(controller)
	{
		var groupTargets = controller.getMatchingTargets();
		if (groupTargets != null && groupTargets.length > 0)
		{
			Ametys.Msg.confirm("{{i18n PLUGINS_CORE_GROUPS_REMOVEUSERS_LABEL}}", 
					"{{i18n PLUGINS_CORE_GROUPS_REMOVEUSERS_CONFIRM}}", 
					Ext.bind(this._confirmRemove, this, [groupTargets[0], controller], 1), 
					this);
		}
		
	},
	
	/**
	 * Remove the selected users from the group after confirmation.
	 * @param {String} buttonId Id of the button that was clicked
	 * @param {Ametys.message.MessageTarget} groupTarget The group target
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The button controller 
	 * @private
	 */
	_confirmRemove: function(buttonId, groupTarget, controller)
	{
		if (buttonId == 'yes')
		{
			var groupsManagerRole = controller.getInitialConfig('groups-manager-role');
			var groupMessageTargetType = controller.getInitialConfig('group-message-target-type') || Ametys.message.MessageTarget.GROUP;
			var userMessageTargetType = controller.getInitialConfig('user-message-target-type') || Ametys.message.MessageTarget.USER;
			
			var userTargets = groupTarget.getSubtargets(userMessageTargetType);
			var logins = Ext.Array.map(userTargets, function(target) {
				return target.getParameters().id;
			});
			
			if (logins.length > 0)
			{
				Ametys.plugins.core.groups.GroupsDAO.removeUsersGroup([groupTarget.getParameters().id, logins, groupsManagerRole, groupMessageTargetType], null, {});
			}
		}
	}
});
