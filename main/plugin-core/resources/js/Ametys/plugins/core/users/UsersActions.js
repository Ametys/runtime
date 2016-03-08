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
 * Singleton class defining the actions related to users.
 * @private
 */
Ext.define('Ametys.plugins.core.users.UsersActions', {
	singleton: true,
	
	/**
	 * Creates a user.
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	add: function (controller)
	{
		var usersManagerRole = controller.getInitialConfig('users-manager-role');
		var userTargetType = controller.getInitialConfig('message-target-type') || Ametys.message.MessageTarget.USER;
		var userToolRole = controller.getInitialConfig('users-tool-role');
		
		Ametys.plugins.core.users.EditUserHelper.add(usersManagerRole, userTargetType, Ext.bind (this._addCb, this, [userToolRole], 1));
	},
	
	/**
	 * Callback function invoked after user creation
	 * @param {Object} user The created user
	 * @param {String} userToolRole The role of users tool
	 */
	_addCb: function (user, userToolRole)
	{
		Ametys.notify({
	        type: 'info',
	        title: "{{i18n PLUGINS_CORE_USERS_NOTIFY_CREATION}}",
	        icon: '/plugins/core/resources/img/users/user_32.png',
	        description: Ext.String.format("{{i18n PLUGINS_CORE_USERS_NOTIFY_CREATION_DESC}}", user.fullname)
	    });
		
		if (userToolRole)
		{
			var tool = Ametys.tool.ToolsManager.getTool(userToolRole);
	    	if (tool == null)
	    	{
	    		Ametys.tool.ToolsManager.openTool(userToolRole, {selectedUsers: [user.login]});
	    	}
		}
	},
	
	/**
	 * Edit the user's properties
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	modify: function(controller)
	{
		var userTarget = controller.getMatchingTargets()[0];
		if (userTarget != null)
		{
			var usersManagerRole = controller.getInitialConfig('users-manager-role');
			var userTargetType = controller.getInitialConfig('message-target-type') || Ametys.message.MessageTarget.USER;
			
			Ametys.plugins.core.users.EditUserHelper.edit(userTarget.getParameters().id, usersManagerRole, userTargetType)
		}
	},
	
	/**
	 * Delete users
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	'delete': function (controller)
	{
		var userTargets = controller.getMatchingTargets();
		if (userTargets != null && userTargets.length > 0)
		{
			Ametys.Msg.confirm("{{i18n plugin.core:PLUGINS_CORE_USERS_DELETE_LABEL}}",
				"{{i18n plugin.core:PLUGINS_CORE_USERS_DELETE_CONFIRM}}",
				Ext.bind(this._doDelete, this, [userTargets, controller], 1),
				this
			);
		}
	},
	
	/**
	 * Impersonate the selected user
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function  
	 */
	impersonate: function(controller)
	{
		var userTarget = controller.getMatchingTargets()[0];
		if (userTarget != null)
		{
			Ametys.plugins.core.users.UsersDAO.impersonate([userTarget.getParameters().id], null, {});
		}
	},

	/**
	 * Callback function invoked after the 'delete' confirm box is closed
	 * @param {String} buttonId Id of the button that was clicked
	 * @param {Ametys.message.MessageTarget[]} targets The users message targets
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function 
	 * @private
	 */
	_doDelete: function(buttonId, targets, controller)
	{
		if (buttonId == 'yes')
		{
			var usersManagerRole = controller.getInitialConfig('users-manager-role');
			var userTargetType = controller.getInitialConfig('message-target-type') || Ametys.message.MessageTarget.USER;
			
			var ids = Ext.Array.map(targets, function(target) {
				return target.getParameters().id;
			});
			
			if (ids.length > 0)
			{
				Ametys.plugins.core.users.UsersDAO.deleteUsers([ids, usersManagerRole, userTargetType], null, {});
			}
		}
	}
});
