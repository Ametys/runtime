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
 * This class is manages the {@link Ametys.ribbon.RibbonElementController ribbon elements} (controls or tabs)
 */
Ext.define(
	"Ametys.ribbon.RibbonManager",
	{
		singleton: true,
		
		/**
		 * @property {Object} _elements The ribbon elements registered. The id and the {Ametys.ribbon.RibbonElementController}
		 * @private
		 */
		_elements: {},
		
		/**
		 * Register an element on the manager. This only allow to get it by its identifier
		 * @param {Ametys.ribbon.RibbonElementController} element The element to register
		 */
		registerElement: function(element) 
		{
			if (this._elements[element.getId()] != null && this.getLogger().isWarnEnabled())
			{
				this.getLogger().warn("Replacing ribbon element '" + this._elements[element.getId()] + "' with a new one");
			}
			else if (this.getLogger().isDebugEnabled())
			{
				this.getLogger().debug("Adding ribbon element '" + this._elements[element.getId()] + "'");
			}
			
			this._elements[element.getId()] = element;
		},
		
		/**
		 * Get the registered element by its identifier
		 * @returns {Ametys.ribbon.RibbonElementController} The element or null if no element is registered with this identifier
		 */
		getElement: function(id)
		{
			var element = this._elements[id];
			
			if (element == null && this.getLogger().isWarnEnabled())
			{
				this.getLogger().warn("Cannot get unexisting element with id '" + id + "'");
			}
			else if (this.getLogger().isDebugEnabled())
			{
				this.getLogger().debug("Get element '" + id + "'");
			}
			
			return element;
		},
		
		/**
		 * Determines if the element with given identifier is a registered elements
		 * @return {Boolean} True the element exists
		 */
		hasElement: function (id)
		{
			return this._elements[id] != null
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
