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
Ext.define('Ametys.plugins.core.profiles.ProfilesActions', {
	singleton: true,
	
	/**
	 * Creates a profile.
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	add: function (controller)
	{
		var context = controller.getInitialConfig('context');
		Ametys.plugins.core.profiles.EditProfileHelper.add(context);
	},
	
	/**
	 * Rename a profile's properties
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	edit: function(controller)
	{
		var profileTarget = controller.getMatchingTargets()[0];
		if (profileTarget != null)
		{
			Ametys.plugins.core.profiles.EditProfileHelper.edit(profileTarget.getParameters().id);
		}
	},
	
	/**
	 * Delete groups
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	'delete': function (controller)
	{
		var profileTargets = controller.getMatchingTargets();
		if (profileTargets != null && profileTargets.length > 0)
		{
			Ametys.Msg.confirm("<i18n:text i18n:key='PLUGINS_CORE_PROFILES_DELETE_LABEL' i18n:catalogue='plugin.core'/>",
				"<i18n:text i18n:key='PLUGINS_CORE_PROFILES_DELETE_CONFIRM' i18n:catalogue='plugin.core'/>",
				Ext.bind(this._doDelete, this, [profileTargets, controller], 1),
				this
			);
		}
	},

	/**
	 * Callback function invoked after the 'delete' confirm box is closed
	 * @param {String} buttonId Id of the button that was clicked
	 * @param {Ametys.message.MessageTarget[]} targets The profiles message targets
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function 
	 * @private
	 */
	_doDelete: function(buttonId, targets, controller)
	{
		if (buttonId == 'yes')
		{
			var ids = Ext.Array.map(targets, function(target) {
				return target.getParameters().id;
			});
			
			if (ids.length > 0)
			{
				Ametys.plugins.core.profiles.ProfilesDAO.deleteProfiles([ids], null, {});
			}
		}
	}
});
