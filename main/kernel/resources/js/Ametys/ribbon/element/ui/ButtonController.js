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
 * - It supports enabling/disabling upon the current selection (see {@link #cfg-selection-target-type}) and associated rights (see {@link #cfg-rights}).
 * - It supports enabling/disabling upon a focused tool (see {@link #cfg-tool-role})
 * - It can be a toggle button (see {@link #cfg-toggle-enabled}).
 * 
 * Note that a property "controlId" is available on the created button. This string references this controller id, that can be retrieve with {@link Ametys.ribbon.RibbonManager#getElement}
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
		 * @property {Boolean/Object} _refreshing=false An object determining the refresh state or false if not refreshing
		 * @property {Boolean} _refreshing.disabled The _disabled member before the refreshing startss 
		 */
		
		/**
		 * @cfg {Object} ui-config Additionnal configuration object to be passed to UI controls
		 */

		constructor: function(config)
		{
			this.callParent(arguments);
			this._toggleEnabled = this.getInitialConfig("toggle-enabled") == true || this.getInitialConfig("toggle-enabled") == 'true';
			this._pressed = this.getInitialConfig("toggle-state") == true || this.getInitialConfig("toggle-state") == 'true';
			this._refreshing = false;

			this._initialize(config);
		},
		
		createUI: function(size, colspan)
		{
			if (this.getLogger().isDebugEnabled())
			{
				this.getLogger().debug("Creating new UI button for controller " + this.getId() + " in size " + size + " (with colspan " + colspan + ")");
			}

			var menu = this._getMenu();
			
			var hasActionFn = this.getInitialConfig("action") != null;
			
			// Is this a split button, where the action is the one from a 'primary-menu-item-id' ?
			var primaryMenuItemId = this.getInitialConfig("primary-menu-item-id");
			var menuItemHandler = primaryMenuItemId && Ametys.ribbon.RibbonManager.hasElement(primaryMenuItemId) ? Ametys.ribbon.RibbonManager.getElement(primaryMenuItemId) : this;
			
			var element = Ext.create("Ametys.ui.fluent.ribbon.controls.Button", Ext.apply({
				text: this.getInitialConfig("label"),
				scale: size,
				colspan: colspan,
				icon: Ametys.CONTEXT_PATH + (size == 'large' ? this._iconMedium : this._iconSmall),
				tooltip: this._getTooltip(),
				
				handler: hasActionFn && !this._toggleEnabled ? Ext.bind(menuItemHandler.onPress, menuItemHandler) : null,
				toggleHandler: hasActionFn && this._toggleEnabled ? Ext.bind(menuItemHandler.onPress, menuItemHandler) : null,
				
				disabled: this._disabled,
				controlId: this.getId(),
				
				enableToggle: this._toggleEnabled,
				pressed: this._pressed,
				
				menu: menu
			}, this.getInitialConfig('ui-config') || {}));
			
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
				icon: this._iconSmall ? Ametys.CONTEXT_PATH + this._iconSmall : null,
				tooltip: this._getTooltip(false),
				showCheckbox: false,
				
				handler: this._toggleEnabled ? null : Ext.bind(this.onPress, this),
				checkHandler : this._toggleEnabled ? Ext.bind(this.onPress, this) : null,
				
				disabled: this._disabled,
				controlId: this.getId(),
				
				checked: this._toggleEnabled ? this._pressed : null,
						
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

			var element = Ext.create("Ametys.ui.fluent.ribbon.controls.gallery.MenuGalleryButton", Ext.apply({
		        text: this.getInitialConfig("label"),
		        tooltip: this._getTooltip(false),
		        icon: Ametys.CONTEXT_PATH + this._iconMedium,
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
		 * Get the menu constructed from button configuration
		 * @return {Object/Ext.menu.Menu} A menu configuration or the menu itself. Can be null if there is no menu.
		 * @private
		 */
		_getMenu: function ()
		{
			var items = [];
			
			var menuGallery = this._getMenuGallery ();
			if (menuGallery != null)
			{
				items.push(menuGallery);
			}
			
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
		 * Get the menu gallery from configuration if exists
		 * @returns {Ametys.ui.fluent.ribbon.controls.gallery.MenuGallery} The menu gallery
		 * @private
		 */
		_getMenuGallery: function ()
		{
			if (this.getInitialConfig("gallery-item"))
			{
				var menuGallery = Ext.create("Ametys.ui.fluent.ribbon.controls.gallery.MenuGallery", this._getMenuGalleryConfig());
				
				var galleryGroupsCfg = this.getInitialConfig("gallery-item")["gallery-groups"];
				
				for (var i=0; i < galleryGroupsCfg.length; i++)
				{
					var gpItems = [];
					
					var items = galleryGroupsCfg[i].items;
					for (var j=0; j < items.length; j++)
					{
						if (typeof (items[j]) == 'string')
						{
							var elmt = Ametys.ribbon.RibbonManager.getElement(items[j]);
							if (elmt != null)
							{
								gpItems.push(elmt.addGalleryItemUI());
							}
						}
						else if (items[j].className)
						{
							var elmt = Ext.create(items[j].className, Ext.applyIf(items[j].config, {id: this.getId() + '.group-.' + i + '-item' + j, pluginName: this.getPluginName()}));
							Ametys.ribbon.RibbonManager.registerElement(elmt);		
							gpItems.push(elmt.addGalleryItemUI());   
						}
					}
					
					if (gpItems.length > 0)
					{
						menuGallery.add({title: galleryGroupsCfg[i].label, items: gpItems});
					}
				}
				
				return menuGallery;
			}
			return null;
		},
		
		/**
		 * Get the configuration of menu gallery from initial configuration if exists
		 * @return {Object} The menu configuration object
		 * @protected
		 */
		_getMenuGalleryConfig: function()
		{
			var config = {};
			var width = this.getInitialConfig("gallery-width");
			
			if (width)
			{
				config.width = parseInt(width, 10);
			}
			
			return config;
		},
		
		/**
		 * Get the menu items from configuration if exist
		 * @returns {Ext.menu.Item[]} The menu items
		 * @private
		 */
		_getMenuItems: function ()
		{
			var menuItems = [];
			
			if (this.getInitialConfig("menu-items"))
			{
				var menuItemCfg = this.getInitialConfig("menu-items");
				
				for (var i=0; i < menuItemCfg.length; i++)
				{
					var elmt = Ametys.ribbon.RibbonManager.getElement(menuItemCfg[i]);
					if (elmt != null)
					{
						menuItems.push(elmt.addMenuItemUI());
					}
				}
			}
			
			return menuItems;
		},
		
		/**
		 * Update the ui controls for images and tooltip
		 * @protected
		 */
		_updateUI: function()
		{
			var me = this;
			this.getUIControls().each(function (element) {
				if (element instanceof Ext.button.Button)
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
				
				var isNotInRibbon = element instanceof Ametys.ui.fluent.ribbon.controls.gallery.MenuGalleryButton 
								 || element instanceof Ext.menu.Item; 
				element.setTooltip(me._getTooltip(!isNotInRibbon));
			});			
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
			this.getUIControls().each(function (elmt) {
				if (elmt instanceof Ext.Button)
				{
					elmt.stopRefreshing();
				}
			});
			
			var oldDisableState = this._refreshing.disabled; 
			this._refreshing = false;
			this.setDisabled(oldDisableState);
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
		 * @inheritdoc
		 * @param {String} methodName The name of the java method to call. The java method must be annotate as "callable". The component use to call this method, is the java class used when declaring this controller in the plugin.xml.
		 * @param {Object[]} parameters The parameters to transmit to the java method. Types are important.
		 * 
		 * @param {Function} callback The function to call when the java process is over. 
		 * @param {Object} callback.returnedValue The returned value of the java call. Can be null if an error occured (but the callback may not be called on error depending on the errorMessage value).
		 * @param {Object} callback.arguments Other arguments specified in option.arguments
		 * 
		 * @param {Object} [options] Advanced options for the call.
		 * @param {Boolean/String/Object} [options.errorMessage] Display an error message. See Ametys.data.ServerComm#callMethod errorMessage.
		 * @param {Boolean/String/Object} [options.waitMessage] Display a waiting message. See Ametys.data.ServerComm#callMethod waitMessage.
		 * @param {Number} [options.priority] The message priority. See Ametys.data.ServerCall.callMethod for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
		 * @param {String} [options.cancelCode] Cancel similar unachieved read operations. See Ametys.data.ServerComm#callMethod cancelCode.
		 * @param {Object} [options.arguments] Additional arguments set in the callback.arguments parameter.
		 * @param {Boolean} [refreshing=false] When 'true', the button will automatically call #refreshing and #stopRefreshing
		 */
		serverCall: function (methodName, parameters, callback, options, refreshing)
		{
			options = options || {};
			
			var errorMessage = options.errorMessage;
			var ignoreOriginalCallback = false;
			if (errorMessage != null)
			{
				if (Ext.isBoolean(errorMessage) && errorMessage == true)
				{
					errorMessage = { };
					ignoreOriginalCallback = true;
				}
				else if (Ext.isString(errorMessage))
				{
					errorMessage = { msg: errorMessage };
					ignoreOriginalCallback = true;
				}
				else
				{
					ignoreOriginalCallback = errorMessage.ignoreCallback || true;
				}
				errorMessage.ignoreCallback = false;
			}
			
			if (refreshing == true)
			{
				this.refreshing();
			}
			
			var opts = {
				arguments: { 
					callback: callback,
					refreshing: refreshing,
					arguments: options.arguments,
					ignoreCallback: ignoreOriginalCallback
				},
				errorMessage: errorMessage,
				waitMessage: options.waitMessage,
				cancelCode: options.cancelCode,
				priority: options.priority
			}
			
			this.callParent([methodName, parameters, this._serverCallCB, opts]);
		},
		
		/**
		 * @private
		 * The intermediary callback of the #serverCall.
		 * @param {Object} result The java result
		 * @param {Object} arguments Additionnal args
		 */
		_serverCallCB: function(result, arguments)
		{
			var initialCallback = arguments.callback;
			var initialArguments = arguments.arguments;
			var ignoreCallbackOnError = arguments.ignoreCallback;
			
			if (arguments.refreshing == true)
			{
				this.stopRefreshing();
			}
			
			if (result != null || !ignoreCallbackOnError)
			{
				initialCallback.apply(this, [result, initialArguments]);
			}
		}
	}
);