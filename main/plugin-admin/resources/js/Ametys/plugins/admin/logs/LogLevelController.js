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
 * This class controls a ribbon button allowing to modify the log level of a given category 
 * @private
 */
Ext.define('Ametys.plugins.admin.logs.LogLevelController', {
	extend: 'Ametys.ribbon.element.ui.ButtonController',
	
	/**
	 * @property {String} _level the logging level of the button
	 * @private
	 */
	constructor: function(config)
	{
		this.callParent(arguments);
		this._level = config.level;
		
		Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, this._onModified, this);
	},

	/**
	 * Listener when the content has been modified
	 * Will update the state of the buttons effectively upon the current selection.
	 * @param {Ametys.message.Message} message The modified message.
	 * @protected
	 */
	_onModified: function (message)
	{
		if (this.updateTargetsInCurrentSelectionTargets (message))
		{
			this.refresh();
		}
	},
	
	updateState: function()
	{
		this._getStatus(this.getMatchingTargets());
	},
	
	/**
	 * Get the logging level of the selected target
	 * @param targets The content targets
	 * @private
	 */
	_getStatus: function (targets)
	{
		var enable = false,
			isRoot = false;
		
		for (var i=0; i < targets.length; i++)
		{
			if (targets[i].getParameters().level != this._level)
			{
				enable = true;
			}
			
			if (targets[i].getParameters().name == "root")
			{
				isRoot = true;
			}
		}
		
		if (this._level != "INHERIT" && this._level != "FORCE")
		{
			// INFO, DEBUG, WARN or ERROR
			
			this.setDisabled(!enable);
		}
		else
		{
			// INHERIT || FORCE
			this.setDisabled(isRoot);
		}
	}
});
