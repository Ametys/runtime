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
 * This class is the "group" container, that you can see in each tab to host buttons and other components.
 * For real, it is just a host for the three underlying icon, small, medium and large sized groups.
 */
Ext.define(
	"Ametys.ui.fluent.ribbon.Group",
	{
		extend: "Ext.container.Container",
		alias: 'widget.ametys.ribbon-group',

        statics: {
            /**
             * @readonly
             * @private
             * @property {String} SIZE_ICON The name of the iconized group in items, to find the index
             */
            SIZE_ICON: "icon",
            /**
             * @readonly
             * @private
             * @property {String} SIZE_SMALL The name of the small group in items, to find the index
             */
            SIZE_SMALL: "small",
            /**
             * @readonly
             * @private
             * @property {String} SIZE_MEDIUM The name of the medium group in items, to find the index
             */
            SIZE_MEDIUM: "medium",
            /**
             * @readonly
             * @private
             * @property {String} SIZE_LARGE The name of the large group in items, to find the index
             */
            SIZE_LARGE: "large"
        },
        
        /**
         * @property {String} groupCls The CSS classname to set on the group
         * @readonly
         * @private
         */
        groupCls: 'a-fluent-group',
        
		/**
		 * @cfg {String} layout Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		layout: 'card',

        
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

		/**
		 * @cfg {Number} width Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */

        /**
         * @cfg {Object[]} iconItems Items in the group when it is in icon size 
         */
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
		 * @property {Object} _scaleContainer Is an association between ("icon", "small", "medium" and "large") with the index of the associated container in the {@link #property-items} property. Will be null if the container does not exist
		 */
		
		/**
		 * Creates a group
		 * @param {Object} config The configuration object
		 */
		constructor: function(config)
		{
            config = config || {};
            
            config.cls = Ext.Array.from(config.cls);
            config.cls.push(this.groupCls);
            
            var mediumItems = config.items;
            config.items = [];
            
            var defaultConfig = {
                xtype: 'ametys.ribbon-groupscale',
                title: config.title,
                tools: []
            };
            
            // Initialize the pointers
            this._scaleContainer = {};
            
            // Add the dialogbox launcher
            if (config.dialogBoxLauncher)
            {
                defaultConfig.tools.push({
                    type: 'openlinkto',
                    handler: Ext.bind(this._onDialogBoxLauncherClickSendEvent, this)
                });
            }                
            
            // Creates the icon sized container
            config.items.push(Ext.applyIf({
                cls: this.groupCls + "-icon",
                title: "&#160;",
                tools: [],
                
                items: [ {
                    xtype: 'ametys.ribbon-button',
                    text: config.title,
                    icon: config.icon,
                    iconCls: "a-ribbon-button-default-icon", // this ensure that button will be structured with an icon, even if icon is undefined
                    iconAlign: 'top',
                    scale: 'large',
                    arrowAlign: 'bottom',
                    menu: {
                        plain: true,
                        minWidth: 1, 
                        items: [ {xtype: 'container'} ]
                    },
                    listeners: {
                        'menushow': this._onIconizedMenuShow,
                        'menuhide': this._onIconizedMenuHide,
                        scope: this
                    }
                }]
            }, defaultConfig));
            this._scaleContainer[Ametys.ui.fluent.ribbon.Group.SIZE_ICON] = config.items.length - 1;
            
			// Creates a smallContainer if smallItems are set
			if (config.smallItems && config.smallItems.length > 0)
			{
				config.items.push(Ext.apply({
                    cls: this.groupCls + "-small",
					items: config.smallItems
				}, defaultConfig));
                this._scaleContainer[Ametys.ui.fluent.ribbon.Group.SIZE_SMALL] = config.items.length - 1; 
			}
			
			// Creates a mediumContainer (items are mandatory)
            config.items.push(Ext.apply({
                cls: this.groupCls + "-medium",
                items: mediumItems
            }, defaultConfig));            
            config.activeItem = this._scaleContainer[Ametys.ui.fluent.ribbon.Group.SIZE_MEDIUM] = config.items.length - 1;
            
			// Creates a largeContainer if largeItems are set
			if (config.largeItems && config.largeItems.length > 0)
			{
                config.items.push(Ext.apply({
                    cls: this.groupCls + "-large",
                    items: config.largeItems
                }, defaultConfig));
                config.activeItem = this._scaleContainer[Ametys.ui.fluent.ribbon.Group.SIZE_LARGE] = config.items.length - 1;
			}

			this.callParent([config]);
		},
        
        /**
         * @private
         * The icon button menu is shown: let's insert the buttons in the menu
         * @param {Ext.button.Button} button The icon button
         * @param {Ext.menu.Menu} menu The menu displayed
         */
        _onIconizedMenuShow: function(button, menu)
        {
            var groupContainer = menu.items.get(0);
            var smallerGroupScale = this.items.get(1); 
            groupContainer.add(smallerGroupScale);
            smallerGroupScale.show();
        },
		        
        /**
         * @private
         * The icon button menu is hiden: let's put the buttons back in the ribbon
         * @param {Ext.button.Button} button The icon button
         * @param {Ext.menu.Menu} menu The menu displayed
         */
        _onIconizedMenuHide: function(button, menu)
        {
            var groupContainer = menu.items.get(0);
            var smallerGroupScale = groupContainer.items.get(0); 
            smallerGroupScale.hide();
            this.items.insert(1, smallerGroupScale);
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
		 * @return {String} The scale as a constant (such as Ametys.ui.fluent.ribbon.Group#SIZE_MEDIUM)
		 */
		getScale: function()
		{
            var scale = null;
            
            var currentIndex = this.items.indexOf(this.getLayout().getActiveItem());
            Ext.Object.each(this._scaleContainer, function (key, value) {
                if (value == currentIndex)
                {
                    scale = key;
                    return false;
                }
            });
            
			return scale;
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

            this.getLayout().setActiveItem(this._scaleContainer[newScale]);
		}
	}
);
