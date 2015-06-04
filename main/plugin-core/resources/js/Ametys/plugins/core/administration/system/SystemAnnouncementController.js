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
Ext.define('Ametys.plugins.core.administration.system.SystemAnnouncementController', {
	extend: 'Ametys.ribbon.element.ui.button.OpenToolButtonController',
	
	/**
	 * @cfg {Boolean} [available=false] True if the system announcement is available
	 */
	/**
	 * @cfg {String} [announcement-on-icon-small] The path to the icon of the button in size 16x16 pixels when the system announcement is on
	 */
	/**
	 * @cfg {String} [announcement-on-icon-medium] The path to the icon of the button in size 32x32 pixels when the system announcement is on
	 */
	/**
	 * @cfg {String} [announcement-on-icon-large] The path to the icon of the button in size 48x48 pixels when the system announcement is on
	 */
	/**
	 * @cfg {String} [announcement-off-icon-small] The path to the icon of the button in size 16x16 pixels when the system announcement is off
	 */
	/**
	 * @cfg {String} [announcement-off-icon-medium] The path to the icon of the button in size 32x32 pixels when the system announcement is off
	 */
	/**
	 * @cfg {String} [announcement-off-icon-large] The path to the icon of the button in size 48x48 pixels when the system announcement is off
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
	 * @property {String} _onIconSmall See #cfg-announcement-on-icon-small
	 * @private
	 */
	/**
	 * @property {String} _onIconMedium See #cfg-announcement-on-icon-medium
	 * @private
	 */
	/**
	 * @property {String} _onIconLarge See #cfg-announcement-on-icon-large
	 * @private
	 */
	/**
	 * @property {String} _offIconSmall See #cfg-announcement-off-icon-small
	 * @private
	 */
	/**
	 * @property {String} _offIconMedium See #cfg-announcement-off-icon-medium
	 * @private
	 */
	/**
	 * @property {String} _offIconLarge See #cfg-announcement-off-icon-large
	 * @private
	 */
	
	constructor: function(config)
	{
		this.callParent(arguments);
		
		this._offIconSmall = this.getInitialConfig("announcement-off-icon-small") || this.getInitialConfig("icon-small");
		this._offIconMedium = this.getInitialConfig("announcement-off-icon-medium") || this.getInitialConfig("icon-medium");
		this._offIconLarge = this.getInitialConfig("announcement-off-icon-large") || this.getInitialConfig("icon-large");
		
		this._onIconSmall = this.getInitialConfig("announcement-on-icon-small") || this.getInitialConfig("icon-small");
		this._onIconMedium = this.getInitialConfig("announcement-on-icon-medium") || this.getInitialConfig("icon-medium");
		this._onIconLarge = this.getInitialConfig("announcement-on-icon-large") || this.getInitialConfig("icon-large");
		
		if (config.available)
		{
			this._iconSmall = this._onIconSmall;
			this._iconMedium = this._onIconMedium;
			this._iconLarge = this._onIconLarge;
			
			this._additionalDescription = this.getInitialConfig("announcement-on-description");
		}
		else
		{
			this._iconSmall = this._offIconSmall;
			this._iconMedium = this._offIconMedium;
			this._iconLarge = this._offIconLarge;
			
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
				this.setIcons (this._onIconSmall, this._onIconMedium, this._onIconLarge);
				this.setAdditionalDescription (this.getInitialConfig("announcement-on-description") || null);
			}
			else
			{
				this.setIcons (this._offIconSmall, this._offIconMedium, this._offIconLarge);
				this.setAdditionalDescription (this.getInitialConfig("announcement-off-description") || null);
			}
		}
	}
});
