/*
 *  Copyright 2012 Anyware Services
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
 * The dialog box to select a super user for the rights manager
 * @private
 */
Ext.define('Ametys.plugins.core.administration.SuperUser', {
	singleton: true,
	
	/**
	 * @private
	 * @property {String} pluginName The plugin that has declared this feature
	 */
	
	/**
	 * The method to call to show up dialog boxes 
	 * @param {String} pluginName The plugin that has declared the class
	 * @param {Object} params The parameters given at declaration time
	 */
	act: function (pluginName, params)
	{
		this.pluginName = pluginName;

		Ametys.runtime.uihelper.SelectUser.act({
			callback: Ext.bind(this._affectSuperUser, this), 
			cancelCallback: null, 
			usersManagerRole: null, 
			allowMultiselection: true, 
			plugin: null
		});	
	},

	/**
	 * A callback method when a user has been selected
	 * @param {String[]} users The array of users login to affect as super users
	 */
	_affectSuperUser: function (users) 
	{
		var logins = [];
		for (var i in users)
		{
			logins.push(i);
		}
		
		var response = Ametys.data.ServerComm.send({
			plugin: this.pluginName, 
			url: "/administrator/superuser/affect", 
			parameters: { login: logins }, 
			priority: Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, 
			callback: null, 
			responseType: null
		});
		if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_SUPERUSER_AFFECT_ERROR'/>", response, "Ametys.plugins.core.administration.SuperUser._affectSuper"))
	    {
	       return;
	    }
		
		Ext.Msg.show ({
			title: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_SUPERUSER_AFFECT_TITLE'/>",
			msg: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_SUPERUSER_AFFECT_SUCCESS'/>",
			buttons: Ext.Msg.OK,
			icon: Ext.MessageBox.INFO
		});
	}
});
