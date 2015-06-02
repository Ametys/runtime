/*
 *  Copyright 2015 Anyware Services
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
 * This class controls a ribbon button allowing to apply the changes made on the selection of plugins and extension points
 * @private
 */
Ext.define('Ametys.plugins.core.administration.plugins.SaveChangesController', {
	extend: 'Ametys.ribbon.element.ui.ButtonController',
	
	constructor: function(config)
	{
		this.callParent(arguments);
		
		Ametys.message.MessageBus.on(Ametys.message.Message.MODIFYING, this._updateState, this);
		Ametys.message.MessageBus.on(Ametys.message.Message.TOOL_FOCUSED, this._updateState, this);
		Ametys.message.MessageBus.on(Ametys.message.Message.TOOL_ACTIVATED, this._updateState, this);
	},

	/**
	 * This listener updates the button's state according the current state of {@link Ametys.plugins.core.administration.plugins.PluginsDAO}
	 * @param {Ametys.message.Message} message The bus message.
	 * @protected
	 */
	_updateState: function (message)
	{
		this.setDisabled(!Ametys.plugins.core.administration.plugins.PluginsDAO.hasPendingChanges());
	},
	
	/**
	 * Listener when the tree is being modified
	 * Will update the state of the button
	 * @param {Ametys.message.Message} message The modified message.
	 * @protected
	 */
	_onModifying: function (message)
	{
		var targets = message.getTargets();
		for (var i=0; i < targets.length; i++)
		{
			if (targets[i].getType() == Ametys.message.MessageTarget.AMETYS_PLUGIN)
			{
				this.setDisabled(false);
				return;
			}
		}
	}
});
