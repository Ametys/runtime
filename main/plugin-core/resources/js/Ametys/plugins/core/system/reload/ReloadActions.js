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
 * Define general actions to reload the applciation
 * @private
 */
Ext.define('Ametys.plugins.core.system.reload.ReloadActions', {
	singleton: true,
	
	/**
	 * This method will reset the workspace user preference AND reload the UI after asking the user
	 */
	reload: function()
	{
		Ametys.Msg.show({
		    title: "<i18n:text i18n:key='PLUGINS_CORE_RELOAD_WORKSPACE_PROMPT_LABEL'/>",
		    msg: "<i18n:text i18n:key='PLUGINS_CORE_RELOAD_WORKSPACE_PROMPT_DESC'/>",
		    buttons: Ext.Msg.OKCANCEL,
		    fn: Ext.bind(this._reloadNow, this),
		    icon: Ext.window.MessageBox.QUESTION
		});
	},
	
	/**
	 * @private
	 * Callback for the dialog of #resetWorkspace. Do the work effectively.
	 * @param {String} buttonId The ID of the button pressed, one of: ok, yes, no, cancel
	 * @param {String} text Value of the input field if either prompt or multiline is true
	 * @param {Object} opt The config object passed to show.
	 */
	_reloadNow: function(buttonId, text, opt)
	{
		if (buttonId == 'ok')
		{
			Ametys.reload();
		}
	}
});