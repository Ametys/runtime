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
		
		Ametys.plugins.core.users.EditUserHelper.add(usersManagerRole, userTargetType)
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
			Ametys.Msg.confirm("<i18n:text i18n:key='PLUGINS_CORE_USERS_DELETE_LABEL' i18n:catalogue='plugin.core'/>",
				"<i18n:text i18n:key='PLUGINS_CORE_USERS_DELETE_CONFIRM' i18n:catalogue='plugin.core'/>",
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
