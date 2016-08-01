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
 * This class controls a ribbon button.
 * 
 * - It can call a configured function when pressed.
 * - It supports enabling/disabling upon the current selection (see {@link #cfg-selection-target-id}) and associated rights (see {@link #cfg-rights}).
 * - It supports enabling/disabling upon a focused tool (see {@link #cfg-tool-id})
 * - It can be a toggle button (see {@link #cfg-toggle-enabled}).
 * 
 * Note that a property "controlId" is available on the created button. This string references this controller id, that can be retrieve with {@link Ametys.ribbon.RibbonManager#getUI}
 */
Ext.define(
	"Ametys.ribbon.element.ui.ButtonController",
	{
		extend: "Ametys.ribbon.element.RibbonUIController",
		
		mixins: { common: 'Ametys.ribbon.element.ui.CommonController' },
		
		/**
		 * @cfg {String} action A function to call when the button is pressed.
		 * Called with the following parameters:
		 * @cfg {Ametys.ribbon.element.ui.ButtonController} action.controller This button controller.
		 * @cfg {Boolean} action.state When the button is a toggle button, the new press-state of the button, null otherwise.
		 */

		/**
		 * @cfg {Boolean/String} toggle-enabled=false When 'true', the button will be a toggle button. {@link #cfg-action} is still called, and the state can be retrieved using #isPressed 
		 */
		/**
		 * @property {Boolean} _toggleEnabled See ({@link #cfg-toggle-enabled})
		 * @private
		 */
		/**
		 * @cfg {Boolean/String} toggle-state=false When 'true', the button is created as pressed. Only available for toggle buttons (#cfg-toggle-enabled)=true.
		 */
		/**
		 * @property {Boolean} _pressed The current pressed state for a toggle button ({@link #cfg-toggle-enabled})
		 * @private
		 */
		
        /**
         * @cfg {Boolean/String} sort-menu-items=false When 'true', the items of a menu will be sorted by alphabetical order.
         */
        /**
         * @property {Boolean} _sortMenuItems See ({@link #sort-menu-items})
         * @private
         */
        
        /**
         * @cfg {Boolean/String} sort-gallery-items=false When 'true', the items of a gallery into a group will be sorted by alphabetical order.
         */
        /**
         * @property {Boolean} _sortGalleryItems See ({@link #sort-gallery-items})
         * @private
         */
        
		/**
		 * @property {Boolean/Object} _refreshing=false An object determining the refresh state or false if not refreshing
		 * @property {Boolean} _refreshing.disabled The _disabled member before the refreshing starts
		 */
		
		/**
		 * @property {String[]} _referencedControllerIds The ids of the other controllers referenced by this controller (such as the menu or gallery items)
		 * @private
		 */
		
		/**
		 * @cfg {Object} ui-config Additionnal configuration object to be passed to UI controls
		 */
		
		/**
		 * @private
		 * @property {Number} _refreshingCounter the counter on refreshing state.
		 */
		_refreshingCounter: 0,

		constructor: function(config)
		{
			this.callParent(arguments);
			this._toggleEnabled = this.getInitialConfig("toggle-enabled") == true || this.getInitialConfig("toggle-enabled") == 'true';
			this._pressed = this.getInitialConfig("toggle-state") == true || this.getInitialConfig("toggle-state") == 'true';
			this._refreshing = false;
            
            this._sortMenuItems = this.getInitialConfig("sort-menu-items") == true || this.getInitialConfig("sort-menu-items") == 'true';
            this._sortGalleryItems = this.getInitialConfig("sort-gallery-items") == true || this.getInitialConfig("sort-gallery-items") == 'true';
			
			this._referencedControllerIds = [];

			this._initialize(config);
		},
		
		createUI: function(size, colspan)
		{
			if (this.getLogger().isDebugEnabled())
			{
				this.getLogger().debug("Creating new UI button for controller " + this.getId() + " in size " + size + " (with colspan " + colspan + ")");
			}

			var hasGalleryItems = this.getInitialConfig("gallery-item") && this.getInitialConfig("gallery-item")["gallery-groups"].length > 0;
			var menuItemsCount = this._getMenuItemsCount();
			
            // FIXME RUNTIME-1539
//		    if (!hasGalleryItems && menuItemsCount == 1)
//		    {
//		    	// There is only one menu item => transform menu to one button
//		    	var menuItemCfg = this.getInitialConfig("menu-items");
//				for (var i=0; i < menuItemCfg.length; i++)
//				{
//					var elmt = Ametys.ribbon.RibbonManager.getUI(menuItemCfg[i]);
//					if (elmt != null)
//					{
//				    	return elmt.createUI (size, colspan);
//					}
//				}
//		    }
//		    else
//		    {
		    	var menu = this._getMenu();
				
				var hasActionFn = this.getInitialConfig("action") != null;
				
				// Is this a split button, where the action is the one from a 'primary-menu-item-id' ?
				var primaryMenuItemId = this.getInitialConfig("primary-menu-item-id");
				var menuItemHandler = primaryMenuItemId && Ametys.ribbon.RibbonManager.hasUI(primaryMenuItemId) ? Ametys.ribbon.RibbonManager.getUI(primaryMenuItemId) : this;
				var isSplitButton = hasActionFn && menu;

				var element = Ext.create(isSplitButton ? "Ametys.ui.fluent.ribbon.controls.SplitButton" : "Ametys.ui.fluent.ribbon.controls.Button", Ext.apply({
					text: this.getInitialConfig("label"),
					scale: size,
					colspan: colspan,
					iconCls: this._iconGlyph + (this._iconDecorator ? ' ' + this._iconDecorator : ''),
					icon: this._iconGlyph ? null : Ametys.CONTEXT_PATH + (size == 'large' ? this._iconMedium : this._iconSmall),
					tooltip: this._getTooltip(),
					
					handler: hasActionFn && !this._toggleEnabled ? Ext.bind(menuItemHandler.onPress, menuItemHandler) : null,
					toggleHandler: hasActionFn && this._toggleEnabled ? Ext.bind(menuItemHandler.onPress, menuItemHandler) : null,
					
					disabled: this._disabled,
					controlId: this.getId(),
					
					enableToggle: this._toggleEnabled,
					pressed: this._pressed,
					
					menu: menu
				}, this.getInitialConfig('ui-config') || {}));
//		    }
			
			return element;
		},
		
		createMenuItemUI: function ()
		{
			if (this.getLogger().isDebugEnabled())
			{
				this.getLogger().debug("Creating new UI menu item for controller " + this.getId());
			}

			var menu = this._getMenu();
			
			var element = Ext.create(this._toggleEnabled ? "Ext.menu.CheckItem" : "Ext.menu.Item", Ext.apply({
				text: this.getInitialConfig("label"),
				iconCls: this._iconGlyph + (this._iconDecorator ? ' ' + this._iconDecorator : ''),
				icon: !this._iconGlyph && this._iconSmall ? Ametys.CONTEXT_PATH + this._iconSmall : null,
				tooltip: this._getTooltip(false),
				showCheckbox: false,
				
				handler: this._toggleEnabled ? null : Ext.bind(this.onPress, this),
				checkHandler : this._toggleEnabled ? Ext.bind(this.onPress, this) : null,
				
				disabled: this._disabled,
				controlId: this.getId(),
				
				checked: this._toggleEnabled ? this._pressed : null,
				
				hideOnClick: this.getInitialConfig("hide-on-click") || !this._toggleEnabled,
						
				menu: menu
			}, this.getInitialConfig('ui-config') || {}));
			
			return element;
		},
		
		createGalleryItemUI: function ()
		{
			if (this.getLogger().isDebugEnabled())
			{
				this.getLogger().debug("Creating new UI gallery item for controller " + this.getId());
			}

			var element = Ext.create("Ametys.ui.fluent.ribbon.controls.Button", Ext.apply({
		        text: this.getInitialConfig("label"),
		        tooltip: this._getTooltip(false),
		        
		        iconCls: this._iconGlyph + (this._iconDecorator ? ' ' + this._iconDecorator : ''),
				icon: this._iconGlyph && this._iconMedium ? null : Ametys.CONTEXT_PATH + this._iconMedium,
						
		        scale: 'large',
		        
		        handler: this._toggleEnabled ? null : Ext.bind(this.onPress, this),
				toggleHandler: this._toggleEnabled ? Ext.bind(this.onPress, this) : null,
				
				disabled: this._disabled,
				controlId: this.getId(),
				
				enableToggle: this._toggleEnabled,
				pressed: this._pressed
			}, this.getInitialConfig('ui-config') || {}));
			
			return element;
		},
		
		/**
		 * Get the menu constructed from button configuration. Call #_getMenuPanels to build items from galleries configuration. Call #_getMenuItems to build from items configuration.
		 * @return {Object/Ext.menu.Menu} A menu configuration or the menu itself. Can be null if there is no menu.
		 * @protected
		 */
		_getMenu: function ()
		{
			var items = [];
			
			items = this._getMenuPanels ();
			
			var menuItems = this._getMenuItems();
			for (var i=0; i < menuItems.length; i++)
			{
				items.push(menuItems[i]);
			}
			
			if (items.length > 0)
			{
				return new  Ext.create("Ext.menu.Menu", {
					cls: 'x-fluent-menu',
					items: items 
				});
			}
			return null;
		},
		
		/**
		 * Get the menu panels from galleries configuration if exists
		 * @returns {Ametys.ui.fluent.ribbon.controls.gallery.MenuPanel[]} The menu panels
		 * @protected
		 */
		_getMenuPanels: function ()
		{
			var menuPanels = [];
			
			if (this.getInitialConfig("gallery-item"))
			{
				var galleryGroupsCfg = this.getInitialConfig("gallery-item")["gallery-groups"];
				
				for (var i=0; i < galleryGroupsCfg.length; i++)
				{
					var gpItems = [];
					
					var items = galleryGroupsCfg[i].items;
					for (var j=0; j < items.length; j++)
					{
						if (typeof (items[j]) == 'string')
						{
							var elmt = Ametys.ribbon.RibbonManager.getUI(items[j]);
							if (elmt != null)
							{
								gpItems.push(elmt.addGalleryItemUI());
								if (!Ext.Array.contains(this._referencedControllerIds, elmt.getId()))
								{
									this._referencedControllerIds.push(elmt.getId());
								}
								
							}
						}
						else if (items[j].className)
						{
							var elmt = Ext.create(items[j].className, Ext.applyIf(items[j].config, {id: this.getId() + '.group-.' + i + '-item' + j, pluginName: this.getPluginName()}));
							Ametys.ribbon.RibbonManager.registerUI(elmt);		
							gpItems.push(elmt.addGalleryItemUI());  
							
							if (!Ext.Array.contains(this._referencedControllerIds, elmt.getId()))
							{
								this._referencedControllerIds.push(elmt.getId());
							}
						}
					}
					
					if (gpItems.length > 0)
					{
                        if (this._sortGalleryItems)
                        {
                            gpItems.sort(this._compareGalleryItemText);
                        }
                        
						var menuPanelCfg = Ext.applyIf({
							title: galleryGroupsCfg[i].label,
							items: gpItems
						}, this._getMenuPanelConfig());
						
						var menuPanel = Ext.create("Ametys.ui.fluent.ribbon.controls.gallery.MenuPanel", menuPanelCfg);
						menuPanels.push(menuPanel);
					}
				}
			}
			return menuPanels;
		},
        
        /**
         * Comparison function for two gallery items based on item' text.
         * @param {Ext.menu.Item} gpItem1 The first item to compare
         * @param {Ext.menu.Item} gpItem2 The second item to compare with
         * @return {Number}
         */
        _compareGalleryItemText: function (gpItem1, gpItem2)
        {
            var text1 = Ext.data.SortTypes.asNonAccentedUCString (gpItem1.text || '');   
            var text2 = Ext.data.SortTypes.asNonAccentedUCString (gpItem2.text || '');  
            
            if (text1 == text2)
            {
                return 0;
            }
            else
            {
                return text1 < text2 ? -1 : 1;
            }
        },
        
		/**
		 * Get the configuration of each menu panel from initial configuration if it exists. Called by #_getMenuPanels.
		 * @return {Object} The menu panel configuration object
		 * @protected
		 */
		_getMenuPanelConfig: function()
		{
			var config = {
				defaults: {
					width: 80
				}	
			};
			var width = this.getInitialConfig("gallery-width");
			
			if (width)
			{
				config.width = parseInt(width);
			}
			else
			{
				config.width = 402;
			}
			
			return config;
		},
		
        // TO remove
		_getMenuItemsCount: function ()
		{
			var count = 0;
			
			if (this.getInitialConfig("menu-items"))
			{
				var menuItemCfg = this.getInitialConfig("menu-items");
				
				for (var i=0; i < menuItemCfg.length; i++)
				{
					var elmt = Ametys.ribbon.RibbonManager.getUI(menuItemCfg[i]);
					if (elmt != null)
					{
						count++;
					}
				}
			}
			
			return count;
		},
		
		/**
		 * Get the menu items from menu items configuration if it exists
		 * @returns {Ext.menu.Item[]} The menu items
		 * @protected
		 */
		_getMenuItems: function ()
		{
			var menuItems = [];
			
			if (this.getInitialConfig("menu-items"))
			{
				var menuItemCfg = this.getInitialConfig("menu-items");
				
				for (var i=0; i < menuItemCfg.length; i++)
				{
					var elmt = Ametys.ribbon.RibbonManager.getUI(menuItemCfg[i]);
					if (elmt != null)
					{
						menuItems.push(elmt.addMenuItemUI());
						if (!Ext.Array.contains(this._referencedControllerIds, elmt.getId()))
						{
							this._referencedControllerIds.push(elmt.getId());
						}
					}
				}
			}
            
            if (this._sortMenuItems)
            {
                // Sort items by alphabetical order
                menuItems.sort(this._compareMenuItemText);
            }
            
			return menuItems;
		},
        
        /**
         * Comparison function for two menu items based on item' text.
         * @param {Ext.menu.Item} item1 The first item to compare
         * @param {Ext.menu.Item} item2 The second item to compare with
         * @return {Number}
         */
        _compareMenuItemText: function (item1, item2)
        {
            var text1 = Ext.data.SortTypes.asNonAccentedUCString (item1.text || '');   
            var text2 = Ext.data.SortTypes.asNonAccentedUCString (item2.text || '');  
            
            if (text1 == text2)
            {
                return 0;
            }
            else
            {
                return text1 < text2 ? -1 : 1;
            }
        },
		
		/**
		 * Update the ui controls for images and tooltip
		 * @protected
		 */
		_updateUI: function()
		{
			var me = this;
			this.getUIControls().each(function (element) {
				if (!me._iconGlyph && element instanceof Ext.button.Button)
				{
					if (element.scale == 'large')
					{
						element.setIcon(Ametys.CONTEXT_PATH + me._iconMedium);
					}
					else
					{
						element.setIcon(Ametys.CONTEXT_PATH + me._iconSmall);
					}
				}
				else if (me._iconGlyph)
				{
					element.setIconCls(me._iconGlyph + (me._iconDecorator ? ' ' + me._iconDecorator : ''))
				}
				
				var isNotInRibbon = element.ownerCt instanceof Ametys.ui.fluent.ribbon.controls.gallery.MenuPanel 
								 || element instanceof Ext.menu.Item; 
				element.setTooltip(me._getTooltip(!isNotInRibbon));
			});			
		},
		
		/**
		 * Get the ids of others controllers referenced by this controller (such as the menu or gallery items)
		 * @return {String[]} the ids of others controllers
		 */
		getReferencedControllerIds: function ()
		{
			return this._referencedControllerIds;
		},
		
		/**
		 * Handler for the button. The default behavior is to call the function defined in #cfg-action
		 * @param {Ametys.ui.fluent.ribbon.controls.Button} button The pressed button
		 * @param {Boolean} state When the button is a toggle button, the new press-state of the button
		 * @protected
		 */
		onPress: function(button, state)
		{
			if (this.getLogger().isInfoEnabled())
			{
				this.getLogger().info("Pressing button " + this.getId() + "");
			}
			
			// ensure, all UIs are coherent
			if (this._toggleEnabled)
			{
				this.toggle(state);
                Ext.menu.Manager.hideAll();
			}

			var actionFn = this.getInitialConfig("action");
			if (actionFn)
			{
				if (this.getLogger().isDebugEnabled())
				{
					this.getLogger().debug("Calling action for button " + this.getId() + ": " + actionFn);
				}
				
				Ametys.executeFunctionByName(actionFn, null, null, this, this._toggleEnabled ? state : null);
			}
		},
		
		/**
		 * Get the current controller state (pressed or not) for a toggle button controller (#cfg-toggle-enabled is 'true').
		 * @returns {Boolean} The state
		 */
		isPressed: function()
		{
			return this._pressed;
		},
		
		/**
		 * When the button is a toggle button (#cfg-toggle-enabled is 'true') this allow to change all controlled UIs state.
		 * No event is thrown on the UIs
		 * @param {Boolean} [state] Change the state to the current value. When not specified the new state is the opposite of the current state.
		 */
		toggle: function(state)
		{
			var me = this;

			if (Ext.isBoolean(state))
			{
				this._pressed = state;
			}
			else
			{
				this._pressed = !this._pressed;
			}
			
			this.getUIControls().each(function (elmt) {
				if (elmt instanceof Ext.Button)
				{
					elmt.toggle(me._pressed, true);
				}
				else if (elmt instanceof Ext.menu.CheckItem)
				{
					elmt.setChecked(me._pressed, true);
				}
			});
		},
		
		/**
		 * Set all controlled UIs in a refreshing state. See #stopRefreshing
		 */
		refreshing: function ()
		{
			this._refreshingCounter++;
			
			var currentDisableState = this._disabled;
			this.disable();
			this._refreshing = {
					disabled: currentDisableState
			};
			
			this.getUIControls().each(function (elmt) {
				if (elmt instanceof Ext.Button)
				{
					elmt.refreshing();
				}
			});
		},

		/**
		 * Stop the refreshing state for all controlled UIs. See #refreshing
		 */
		stopRefreshing: function ()
		{
			this._refreshingCounter--;
			
			// Do NOT stop refreshing until a refresh is still in progress
			if (this._refreshingCounter == 0)
			{
				this.getUIControls().each(function (elmt) {
					if (elmt instanceof Ext.Button)
					{
						elmt.stopRefreshing();
					}
				});
				
				var oldDisableState = this._refreshing.disabled; 
				this._refreshing = false;
				this.setDisabled(oldDisableState);
			}
			
		},
		
		disable: function()
		{
			if (!this._refreshing)
			{
				this.mixins.common.disable.apply(this);
			}
			else
			{
				this._refreshing.disabled = true;
			}
		},
		
		enable: function()
		{
			if (!this._refreshing)
			{
				this.mixins.common.enable.apply(this);
			}
			else
			{
				this._refreshing.disabled = false;
			}
		},
		
        /**
         * Called to prepare options for a #serverCall 
         * @param {Object} options See #serverCall for default options. This implementation additionnal have the following properties
         * @param {Boolean} [options.refreshing=false] When 'true', the button will automatically call #refreshing and #stopRefreshing before and after the server request.
         */
        beforeServerCall: function(options)
        {
            if (options.refreshing == true)
            {
                this.refreshing();
            }
        },
        
        /**
         * @inheritDoc
         * @private 
         */
        afterServerCall: function(serverResponse, options)
        {
            if (options.refreshing == true)
            {
                this.stopRefreshing();
            }
        },
        
        /**
         * @inheritDoc
         * @private 
         */
        afterCancelledServerCall: function (options)
        {
        	if (options.refreshing == true)
        	{
        		this.stopRefreshing();
        	}
        }
	}
);
