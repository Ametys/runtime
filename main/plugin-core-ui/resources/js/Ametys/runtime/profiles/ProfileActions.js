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
 * This class is a singleton to handle actions on profiles
 */
Ext.define('Ametys.runtime.profiles.ProfileActions', {
	singleton: true,
	
	/**
	 * Affects a profile to users 
	 * @param {String} profileId The profile id
	 * @param {Object[]} users The users.
     * @param {String} users.login The login of the user
     * @param {String} users.population The population id of the user
	 * @param {String} context The context right
	 * @param {Function} callback The callback function
	 * @param {Object} additionalParams The optional additional parameters. Can be null.
	 */
	addUsers: function(profileId, users, context, callback, additionalParams)
	{
		var params = [
			users,
			[profileId],
			context
		];
		
        callback = callback || Ext.emptyFn;
        Ametys.plugins.core.profiles.ProfilesDAO.addUsers(params, callback, {scope: this});
	},
	
	/**
	 * Affects a profile to groups 
	 * @param {String} profileId The profile id
	 * @param {Object[]} groups The groups.
     * @param {String} groups.id The id of the group
     * @param {String} groups.groupDirectory The group directory id of the group
	 * @param {String} context The context right
	 * @param {Function} callback The callback function
	 * @param {Object} additionalParams The optional additional parameters. Can be null.
	 */
	addGroups: function(profileId, groups, context, callback, additionalParams)
	{
		var params = [
			groups,
			[profileId],
			context
		];
		
        callback = callback || Ext.emptyFn;
        Ametys.plugins.core.profiles.ProfilesDAO.addGroups(params, callback, {scope: this});
	},
	
	/**
	 * Removes assignment
	 * @param {String} profileId The profile id
	 * @param {String} context The context right
	 * @param {Object[]} users The users to remove.
     * @param {String} users.login The login of the user
     * @param {String} users.population The population id of the user
     * @param {Object[]} groups The groups to remove
     * @param {String} groups.id The id of the group
     * @param {String} groups.groupDirectory The group directory id of the group
	 * @param {Function} callback The callback function
	 * @param {Object} additionalParams The optional additional parameters. Can be null.
	 */
	remove: function(profileId, context, users, groups, callback, additionalParams) 
	{
		Ametys.Msg.confirm("{{i18n plugin.core-ui:PLUGINS_CORE_UI_RIGHTS_ASSIGNMENT_REMOVE_LABEL}}", 
			"{{i18n plugin.core-ui:PLUGINS_CORE_UI_RIGHTS_ASSIGNMENT_REMOVE_CONFIRM}}", 
			Ext.bind(this._doRemove, this, [profileId, context, users, groups, callback, additionalParams], 1),
			this);
	},

	/**
	 * @private
	 * Internal #remove callback. Executed after the user removal confirmation through the message box.
	 * @param {String} btn the ID of the button pressed
	 * @param {String} profileId The profile id
	 * @param {String} context The context right
	 * @param {Object[]} users The users to remove.
     * @param {String} users.login The login of the user
     * @param {String} users.population The population id of the user
	 * @param {Object[]} groups The groups to remove
     * @param {String} groups.id The id of the group
     * @param {String} groups.groupDirectory The group directory id of the group
	 * @param {Function} callback The callback function
	 * @param {Object} additionalParams The optional additional parameters. Can be null.
	 */
	_doRemove: function(btn, profileId, context, users, groups, callback, additionalParams) 
	{
		if (btn == 'yes')
		{
			var params = [
				users,
				groups,
				profileId,
				context
			];
            
            callback = callback || Ext.emptyFn;
            Ametys.plugins.core.profiles.ProfilesDAO.removeAssignment(params, callback, {scope: this});
		}
	}
});
