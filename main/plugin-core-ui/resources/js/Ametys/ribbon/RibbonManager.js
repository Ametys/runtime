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
 * This class manages the {@link Ametys.ribbon.RibbonElementController ribbon elements} (ui controls or tabs)
 */
Ext.define(
	"Ametys.ribbon.RibbonManager",
	{
		singleton: true,
		
		/**
		 * @property {Object} _uis The ribbon UI elements registered. The id and the {Ametys.ribbon.RibbonElementController}
		 * @private
		 */
		_uis: {},
		/**
		 * @property {Object} _tabs The ribbon tabs elements registered. The id and the {Ametys.ribbon.RibbonElementController}
		 * @private
		 */
		_tabs: {},
		
		/**
		 * Register an UI element on the manager. This only allow to get it by its identifier
		 * @param {Ametys.ribbon.RibbonElementController} element The element to register
		 */
		registerUI: function(ui) 
		{
			if (this._uis[ui.getId()] != null && this.getLogger().isWarnEnabled())
			{
				this.getLogger().warn("Replacing ribbon UI element '" + ui.getId() + "' with a new one");
			}
			else if (this.getLogger().isDebugEnabled())
			{
				this.getLogger().debug("Adding ribbon UI element '" + ui.getId() + "'");
			}
			
			this._uis[ui.getId()] = ui;
		},
		
		/**
		 * Register a tab element on the manager. This only allow to get it by its identifier
		 * @param {Ametys.ribbon.RibbonElementController} element The element to register
		 */
		registerTab: function(tab) 
		{
		    if (this._tabs[tab.getId()] != null && this.getLogger().isWarnEnabled())
		    {
		        this.getLogger().warn("Replacing ribbon tab element '" + tab.getId() + "' with a new one");
		    }
		    else if (this.getLogger().isDebugEnabled())
		    {
		        this.getLogger().debug("Adding ribbon tab element '" + tab.getId() + "'");
		    }
		    
		    this._tabs[tab.getId()] = tab;
		},
		
		/**
		 * Get the registered UI element by its identifier
		 * @returns {Ametys.ribbon.RibbonElementController} The UI or null if no UI is registered with this identifier
		 */
		getUI: function(id)
		{
			var ui = this._uis[id];
			
			if (ui == null && this.getLogger().isWarnEnabled())
			{
				this.getLogger().warn("Cannot get unexisting ui with id '" + id + "'");
			}
			else if (this.getLogger().isDebugEnabled())
			{
				this.getLogger().debug("Get ui '" + id + "'");
			}
			
			return ui;
		},
		
		/**
		 * Get the registered element by its identifier
		 * @returns {Ametys.ribbon.RibbonElementController} The element or null if no element is registered with this identifier
		 */
		getTab: function(id)
		{
		    var tab = this._tabs[id];
		    
		    if (tab == null && this.getLogger().isWarnEnabled())
		    {
		        this.getLogger().warn("Cannot get unexisting tab with id '" + id + "'");
		    }
		    else if (this.getLogger().isDebugEnabled())
		    {
		        this.getLogger().debug("Get tab '" + id + "'");
		    }
		    
		    return tab;
		},
		
		/**
		 * Determines if the UI element with given identifier is a registered UI
		 * @return {Boolean} True the UI exists
		 */
		hasUI: function (id)
		{
			return this._uis[id] != null
		},

		/**
		 * Determines if the tab element with given identifier is a registered tab
		 * @return {Boolean} True the tab exists
		 */
		hasTab: function (id)
		{
		    return this._tabs[id] != null
		}
	}
);

(function() {
	/*
	 * Add the message bus implementation of how to remember the last active tab
	 */
	Ext.define(
			"Ametys.ribbon.RibbonController",
			{
				override: "Ametys.ui.fluent.ribbon.TabPanel",
				
				/**
				 * @property {Ametys.ui.fluent.ribbon.Panel} _lastActivatedPanel The last activated tab panel of the ribbon 
				 */
				_lastActivatedTab: null,
				
				constructor: function()
				{
					this.callParent(arguments);
					
					Ametys.message.MessageBus.on('*', this.onAnyMessage, this);
				},
				
				/**
				 * @private
				 * Listener on the message bus of any message
				 * @param {Ametys.message.Message} message The message fired
				 */
				onAnyMessage: function(message)
				{
					this._lastActivatedPanel = this.getActiveTab();
				},
			
				changeWasActiveOnHideStatus: function(panel)
				{
					panel._wasActiveOnHide = (panel == this._lastActivatedPanel);
				}
			}
	);
})();
