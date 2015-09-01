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
 * This class controls a ribbon button representing the lock state of a content
 * @private
 */
Ext.define('Ametys.plugins.core.profiles.controller.ViewModeController', {
	extend: 'Ametys.ribbon.element.ui.ButtonController',
	
	constructor: function(config)
	{
		this.callParent(arguments);
		
		Ametys.message.MessageBus.on(Ametys.message.Message.TOOL_PARAMS_UPDATED, this._onToolParamsUpdated, this);
	},

	/**
	 * Listener when the tool's params has been updated
	 * Will update the state of the buttons effectively upon the current selection.
	 * @param {Ametys.message.Message} message The updated message.
	 * @protected
	 */
	_onToolParamsUpdated: function (message)
	{
		var toolTarget = message.getTarget(Ametys.message.MessageTarget.TOOL);
		if (toolTarget && Ext.getClassName(toolTarget.getParameters().tool) == 'Ametys.plugins.core.profiles.ProfilesTool')
		{
			var tool = toolTarget.getParameters().tool;
			var mode = tool.getMode();
			this.toggle(mode == 'edit');
		}
	}
	
});
