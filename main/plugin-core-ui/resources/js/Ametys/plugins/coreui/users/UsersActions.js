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
Ext.define('Ametys.plugins.coreui.users.UsersActions', {
	singleton: true,
	
	/**
	 * Creates a user.
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	add: function (controller)
	{
		var userTargetType = controller.getInitialConfig('message-target-type') || Ametys.message.MessageTarget.USER;
		var userToolRole = controller.getInitialConfig('users-tool-role');
        var context = Ametys.getAppParameter('context');
		
		Ametys.plugins.coreui.users.EditUserHelper.add(context, userTargetType, userToolRole, Ext.bind (this._addCb, this, [userToolRole], 1));
	},
	
	/**
     * @private
	 * Callback function invoked after user creation
	 * @param {Object} user The created user
	 * @param {String} userToolRole The role of users tool
	 */
	_addCb: function (user, userToolRole)
	{
		Ametys.notify({
	        type: 'info',
	        title: "{{i18n PLUGINS_CORE_UI_USERS_NOTIFY_CREATION}}",
	        icon: '/plugins/core/resources/img/users/user_32.png',
	        description: Ext.String.format("{{i18n PLUGINS_CORE_UI_USERS_NOTIFY_CREATION_DESC}}", user.fullname)
	    });
		
		if (userToolRole)
		{
			var tool = Ametys.tool.ToolsManager.getTool(userToolRole);
	    	if (tool == null)
	    	{
	    		Ametys.tool.ToolsManager.openTool(userToolRole, {selectedUsers: [user.login]}); //FIXME context for the UserTool ?
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
			var userTargetType = controller.getInitialConfig('message-target-type') || Ametys.message.MessageTarget.USER;
			
			Ametys.plugins.coreui.users.EditUserHelper.edit(userTarget.getParameters().id, userTarget.getParameters().population, userTargetType)
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
			Ametys.Msg.confirm("{{i18n PLUGINS_CORE_UI_USERS_DELETE_LABEL}}",
				"{{i18n PLUGINS_CORE_UI_USERS_DELETE_CONFIRM}}",
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
			Ametys.plugins.core.users.UsersDAO.impersonate([userTarget.getParameters().id, userTarget.getParameters().population], null, {});
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
			var userTargetType = controller.getInitialConfig('message-target-type') || Ametys.message.MessageTarget.USER;
			
            var users = [];
            Ext.Array.forEach(targets, function(target) {
                users.push({
                    login: target.getParameters().id,
                    population: target.getParameters().population
                });
            }, this);
			
			if (targets.length > 0)
			{
				Ametys.plugins.core.users.UsersDAO.deleteUsers([users, userTargetType], null, {});
			}
		}
	}
});
