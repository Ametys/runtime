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
 * This class is the "group" container, that you can see in each tab to host buttons and other components
 */
Ext.define(
	"Ametys.ui.fluent.ribbon.Group",
	{
		extend: "Ext.panel.Panel",
		alias: 'widget.ametys.ribbon-group',

		/**
		 * @cfg {String} layout Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		layout: 'fit',
		/**
		 * @cfg {String} cls Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		cls: 'x-fluent-group',
		/**
		 * @cfg {Boolean} frame Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		frame: false,
		/**
		 * @cfg {String} defaultType Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		/**
		 * @cfg {Object} defaults Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		defaults:
		{
			xtype: 'container',
			layout: 'table',	
			defaults: {
				xtype: 'ametys.ribbon-group-part'
			}
		},

		/**
		 * @cfg {String} headerPosition Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		headerPosition: 'bottom',

		/**
		 * @cfg {Number} width Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		/**
		 * @cfg {Number} minWidth Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		minWidth: 30,
		
		/**
		 * @cfg {Number} height Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		height: 87,
		
		/**
		 * @cfg {Object} header Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		header: {
			height: 17
		},
		
		/**
		 * @cfg {String} margin Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		margin: '0 1px 4px 0',

		/**
		 * @cfg {Object[]} smallItems Items in the group when it is in small size 
		 */
		/**
		 * @cfg {Object[]} items Items in the group when it is in medium size. Beware that those items will not be transmitted as direct children of this panel.
		 */
		/**
		 * @cfg {Object[]} largeItems Items in the group when it is in large size
		 */
		/**
		 * @cfg {Boolean} dialogBoxLauncher When true, a tool button to launch a dialog box will be set in the header. It will launch the event {@link #dialogboxlaunch}
		 */
		

		/**
		 * @property {Object[]} items The children of the group. The items here are not thoses given in the configuration. You will find here 1, 2 or 3 items, that are the medium container and optionnaly the large and/or the small ones.
		 */
		
		/**
		 * @private
		 * @property {String} _scale The current scale of the group "small", "medium" or "large".
		 */
		_scale: null,
		/**
		 * @private
		 * @property {Object} _scaleContainer Is an association between ("small", "medium" and "large") with the index of the associated container in the {@link #property-items} property. Will be null if the container does not exists
		 */
		
		/**
		 * Creates a group
		 * @param {Object} config The configuration object
		 */
		constructor: function(config)
		{
			var smallContainer, mediumContainer, largeContainer;
			
			this._scale = "medium";
			
			// Creates a smallContainer if smallItems are set
			if (config.smallItems && config.smallItems.length > 0)
			{
				smallContainer = {
					items: config.smallItems, 
					hidden: true
				};
			}
			
			// Creates a largeContainer if largeItems are set
			if (config.largeItems && config.largeItems.length > 0)
			{
				this._scale = "large";
				largeContainer = {
					items: config.largeItems,
					hidden: false
				};
			}

			// Creates a mediumContainer (items are mandatory)
			mediumContainer = {
				items: config.items, 
				hidden: largeContainer != null
			};
			
			// Initialize the pointers
			this._scaleContainer = {};
			this._scaleContainer["small"] = smallContainer ? 0 : null;
			this._scaleContainer["medium"] = smallContainer ? 1 : 0;
			this._scaleContainer["large"] = largeContainer ? (smallContainer ? 2 : 1) : null;

			// Original items where transmitter to the mediumContainer
			// New items will be the containers
			config.items = [];
			if (smallContainer)
			{
				config.items.push(smallContainer);
			}
			config.items.push(mediumContainer);
			if (largeContainer)
			{
				config.items.push(largeContainer);
			}
			
			// Add the dialogbox launcher
			if (config.dialogBoxLauncher)
			{
				if (!config.tools)
				{
					config.tools = [];
				}

				config.tools.push({
					cls: 'x-fluent-dialogboxlauncher',
					type: 'gear',
					handler: Ext.bind(this._onDialogBoxLauncherClickSendEvent, this)
				});
			}
			
			this.callParent(arguments);
		},
		
		/**
		 * @private
		 * Listener on dialog box launcher tool to fire the event
		 */
		_onDialogBoxLauncherClickSendEvent: function ()
		{
            /**
             * @event dialogboxlaunch
             * Fires when the dialog box button was clicked
             * @param {Ext.Panel} p the current Panel.
             */
			this.fireEvent('dialogboxlaunch', this);
		},
		
		/**
		 * Get the currently set scale
		 * @return {String} The scale. Can be "small", "medium" or "large".
		 */
		getScale: function()
		{
			return this._scale;
		},

		/**
		 * Is the given scale supported?
		 * @param {String} newScale The scale to test
		 * @returns {Boolean} true if the scale does exist, false if not
		 */
		supportScale: function(newScale)
		{
			return this._scaleContainer[newScale] != null;
		},
		
		/**
		 * Change the scale. If the new scale is not supproted, the method will silently fail.
		 * @param {String} newScale The scale to set.
		 */
		setScale: function(newScale)
		{
			if (newScale == this.scale || !this.supportScale(newScale))
			{
				return;
			}

			this.items.get(this._scaleContainer[this._scale]).hide();
			this._scale = newScale;
			this.items.get(this._scaleContainer[this._scale]).show();
		},
		
		/**
		 * Determine if the group is currently normally visible
		 * @returns {Boolean} True if the group is normally visible inside the ribbon. False if the group do overlap the right of the ribbon, or is on a second line
		 */
		canBeVisible: function()
		{
			return this.el ? this.el.getTop() < 60 && this.el.getRight() < this.el.parent().getRight() : true;
		},
		

		onRender: function(parentNode, containerIdx)
		{
			this.callParent(arguments);
			
			this.el.hover(this._onOverDoFadeIn, this._onOutDoFadeOut, this);
		},
		
		/**
		 * Listener when mouse goes over the group, we fade-in the background
		 */
		_onOverDoFadeIn: function()
		{
			var nbFrames = 8;
			var frameSize = 264;

			this.el.stopAnimation().animate({ to: { 'background-position': [0, - frameSize * (nbFrames-1)], 'background-position-step': [1, frameSize] }, duration: 500, easing: 'linear' });
			this.el.child(".x-panel-header").stopAnimation().animate({ to: { 'background-position': ['100%', -158 - frameSize * (nbFrames-1)], 'background-position-step': [1, frameSize] }, duration: 500, easing: 'linear' });
			this.el.child(".x-panel-header").child("div").stopAnimation().animate({ to: { 'background-position': [0, -246 - frameSize * (nbFrames-1)], 'background-position-step': [1, frameSize] }, duration: 500, easing: 'linear' });
			this.el.down(".x-panel-body").stopAnimation().animate({ to: { 'background-position': ['100%', -88 - frameSize * (nbFrames-1)], 'background-position-step': [1, frameSize] }, duration: 500, easing: 'linear' });
            // loop on small, medium and large container
            for (var i = 1; i <= 3; i++)
            {
			     var elt = this.el.down(".x-panel-body").child(".x-container:nth-child(" + i + ")");
                 if (elt != null)
                 {
                    elt.stopAnimation().animate({ to: { 'background-position': [0, -176 - frameSize * (nbFrames-1)], 'background-position-step': [1, frameSize] }, duration: 500, easing: 'linear' });
                 }
            }
		},			

		/**
		 * Listener when mouse leaves the group, we fade-out the background
		 */
		_onOutDoFadeOut: function()
		{
			var nbFrames = 8;
			var frameSize = 264;

			this.el.stopAnimation()
			this.el.animate({ to: { 'background-position': [0, 0], 'background-position-step': [1, frameSize] }, duration: 500, easing: 'linear' });
			this.el.child(".x-panel-header").stopAnimation().animate({ to: { 'background-position': ['100%', -158], 'background-position-step': [1, frameSize] }, duration: 500, easing: 'linear' });
			this.el.child(".x-panel-header").child("div").stopAnimation().animate({ to: { 'background-position': [0, -246], 'background-position-step': [1, frameSize] }, duration: 500, easing: 'linear' });
			this.el.child(".x-panel-body").stopAnimation().animate({ to: { 'background-position': ['100%', -88], 'background-position-step': [1, frameSize] }, duration: 500, easing: 'linear' });
            // loop on small, medium and large container
            for (var i = 1; i <= 3; i++)
            {
                 var elt = this.el.down(".x-panel-body").child(".x-container:nth-child(" + i + ")");
                 if (elt != null)
                 {
                    elt.stopAnimation().animate({ to: { 'background-position': [0, -176], 'background-position-step': [1, frameSize] }, duration: 500, easing: 'linear' });
                 }
            }
		}
	}
);
