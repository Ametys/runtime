/*
 *  Copyright 2011 Anyware Services
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
 * Singleton class defining the UI debug actions.
 * @private
 */
Ext.define('Ametys.plugins.coreui.system.debug.UIDebugActions', {
	singleton: true,
	
	/**
	 * Called during the initialization of the controller
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller for the ui debug button
	 */
	initialize: function(controller)
	{
		controller._pressed = Ametys.getAppParameter('debug.ui') == '2';
	},
	
	/**
	 * Called when the button is pressed
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller for the ui debug button
	 * @param {Boolean} state When the button is a toggle button, the new press-state of the button, null otherwise
	 */
	act: function(controller, state)
	{
		Ametys.Msg.show({
			title: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_DEBUG_UI_CONFIRM_TITLE'/>",
			msg: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_DEBUG_UI_CONFIRM_TEXT'/>",
			icon: Ext.Msg.QUESTION,
			buttons: Ext.Msg.YESNO,
			callback: this._actCb,
			controller: controller,
			state: state
		});
	},
	
	/**
	 * @private
	 * Listener when the dialog box is closed
	 * @param {String} buttonId The ID of the button pressed, one of: ok, yes, no, cancel.
	 * @param {String} text Value of the input field if either prompt or multiline is true
	 * @param {Object} opt The config object passed to show.
	 */
	_actCb: function(buttonId, text, opt)
	{
		if (buttonId == 'yes')
		{
			var debugValue = opt.state ? '2' : '0';
			Ametys.reload("debug.ui=" + debugValue);
		}
		else
		{
			// Restore previous controller state
			opt.controller.toggle(!opt.state);
		}
	}
});
