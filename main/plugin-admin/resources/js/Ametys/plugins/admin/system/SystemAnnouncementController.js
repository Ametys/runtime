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
 * This class controls a ribbon button reflecting the state of the system announcement message
 * @private
 */
Ext.define('Ametys.plugins.admin.system.SystemAnnouncementController', {
	extend: 'Ametys.ribbon.element.ui.button.OpenToolButtonController',
	
	/**
	 * @cfg {Boolean} [available=false] True if the system announcement is available
	 */
	/**
	 * @cfg {String} [announcement-on-icon-decorator] The CSS class for decorator when the system announcement is on
	 */
	/**
	 * @cfg {String} [announcement-off-icon-decorator] The CSS class for decorator when the system announcement is off
	 */
	
	/**
	 * @cfg {String} [announcement-on-label] The label when when the system announcement is on
	 */
	/**
	 * @cfg {String} [announcement-off-label] The label when when the system announcement is off
	 */
	/**
	 * @cfg {String} [announcement-on-description] The description when when the system announcement is on
	 */
	/**
	 * @cfg {String} [announcement-off-description] The description when when the system announcement is off
	 */
	
	/**
	 * @property {String} _onIconDecorator See #cfg-announcement-on-icon-decorator
	 * @private
	 */
	/**
	 * @property {String} _offIconDecorator See #cfg-announcement-off-icon-decorator
	 * @private
	 */
	
	constructor: function(config)
	{
		this.callParent(arguments);
		
		this._offIconDecorator = this.getInitialConfig("announcement-off-icon-decorator");
		this._onIconDecorator = this.getInitialConfig("announcement-on-icon-decorator");
		
		if (config.available)
		{
			this._iconDecorator = this._onIconDecorator;
			this._additionalDescription = this.getInitialConfig("announcement-on-description");
		}
		else
		{
			this._iconDecorator = this._offIconDecorator;
			this._additionalDescription = this.getInitialConfig("announcement-off-description");
		}

		Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, this._onModified, this);
	},

	/**
	 * Listener when the toggle state of the button has been initialized/modified
	 * Will update the state of the button
	 * @param {Ametys.message.Message} message The modified message.
	 * @protected
	 */
	_onModified: function (message)
	{
		var target = message.getTarget('system-announcement');
		
		if (target != null)
		{
			var state = target.getParameters().state;
			if (state)
			{
				this.setIconDecorator (this._onIconDecorator);
				this.setAdditionalDescription (this.getInitialConfig("announcement-on-description") || null);
			}
			else
			{
				this.setIconDecorator (this._offIconDecorator);
				this.setAdditionalDescription (this.getInitialConfig("announcement-off-description") || null);
			}
		}
	}
});
