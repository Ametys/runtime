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
 * This implementation of Ametys.ui.tool.ToolsLayout is a complex one.
 * It put the tools into several Ext.tab.Panel that are devided into locations.
 * It supports drag'n'drop between locations and save the new default location for tools into the user preferences.
 * Regions supported are : center (left and right) left, right, top and bottom.
 */
Ext.define("Ametys.ui.tool.layout.ZonedTabsToolsLayout", 
	{
		extend: "Ametys.ui.tool.ToolsLayout",
        
        /** @cfg {String} layoutCls A css classname added to the general container AND to the central container */
        layoutCls: "ametys-tools-panel-wrapper",
        /** @cfg {String} focusCls A css classname added to the zone, when one of its toos has the focus */
        focusCls: "ametys-toolpanel-focused",
        /** @cfg {String} toolPrefixCls A css classname prefix. Added with the type value, it leads to a classname added to the zone corresponding to the type of the active tool. */
        toolPrefixCls: "ametys-toolpanel-",
        
		/**
		 * @property {Ext.container.Container} _panel The big main panel
		 * @private
		 */
		/**
		 * @property {Ametys.ui.tool.ToolPanel} _focusedTool The currently focused tool
		 * @private
		 */

		/**
		 * @property {Object} _panelHierarchy The Ametys.ui.tool.layout.ZonedTabsToolsLayout pointed by location ('cl', 'cr, 'l', 't', 'r' or 'b').
		 * @private
		 */
	
		/**
		 * @property {Object[]} _lastUse The last date of use of each location. A use means have a focused tab.
		 * @property {String} _lastUse.location The location
		 * @property {String} _lastUse.date The date of last use
		 * @private
		 */	
		_lastUse: [],

        /**
         * @property {String} _tabPolicy The current tab policy for new tools
         * @private
         */
		
		constructor: function(config)
		{
            this._tabPolicy = this.getSupportedToolPolicies()[0];
            
            this.callParent(arguments);
		},
		
		getSupportedLocations: function()
		{
			return ['cl', 'cr', 'l', 't', 'r', 'b'];
		},
        
        getNearestSupportedLocation: function(desiredLocation)
        {
            // No location specified
            if (!desiredLocation)
            {
                return this.getSupportedLocations()[0];
            }
            
            // Matching location
            if (this.getSupportedLocations().indexOf(desiredLocation) != -1)
            {
                return desiredLocation;
            }
            
            // Let's shorten the desired location to see if its matching now
            desiredLocation = desiredLocation.substring(0, desiredLocation.length - 1);
            return this.getNearestSupportedLocation(desiredLocation);
        },
		
		getToolsAtLocation: function(location)
		{
			var tools = [];
            
			var zoneTabToolsPanel = this._panelHierarchy[location];
			if (zoneTabToolsPanel != null)
			{
				zoneTabToolsPanel.items.each(function (tool) {
					tools.push(tool);
				});
			}
			
			return tools;
		},
	
		createLayout: function()
		{
			var panels = this._createRegionPanels();

			this._panel = new Ext.Container ({
				region: 'center', 
				border: false,
				layout: 'border',
				cls: this.layoutCls,
				items: panels
			});
			
			return this._panel;
		},
		
		/**
		 * Creates the panels for the hierarchy of panels in the hmi
		 * @param {String} offset The location offset.
		 * @return {Ext.Panel[]} The created panels
		 * @private
		 */
		_createRegionPanels: function()
		{
			this._panelHierarchy = {};
			
			var panelClass = "Ametys.ui.tool.layout.ZonedTabsToolsLayout.ZoneTabsToolsPanel";
			
			var panels = [
				this._panelHierarchy['l'] = Ext.create(panelClass, { region: 'west', location: 'l', toolsLayout: this }),
				this._panelHierarchy['r'] = Ext.create(panelClass, { region: 'east', location: 'r', toolsLayout: this }),
				this._panelHierarchy['t'] = Ext.create(panelClass, { region: 'north', location: 't', toolsLayout: this }),
				this._panelHierarchy['b'] = Ext.create(panelClass, { region: 'south', location: 'b', toolsLayout: this }),
				{
					xtype: 'panel',
					cls: this.layoutCls,
					region: 'center',
					border: false,
					layout: {
						type: 'hbox',
						pack: 'start',
						align: 'stretch'
					},
					items: [
					        this._panelHierarchy['cl'] = Ext.create(panelClass, { location: 'cl', toolsLayout: this }),
					        this._panelHierarchy['cr'] = Ext.create(panelClass, { location: 'cr', toolsLayout: this })
					]
				}
			];
			
			return panels;
		},
		
		focusTool: function(tool)
		{
			var zoneTabsToolsPanel = tool.ownerCt;
			
			if (zoneTabsToolsPanel.getActiveTab() != tool)
			{
				zoneTabsToolsPanel.setActiveTab(tool);
			}
			
			this._onToolFocused(tool);
		},
		
		/**
		 * @private
		 * Will slide in a zone and slide out the others
		 * @param {Ametys.ui.tool.layout.ZonedTabsToolsLayout.ZoneTabsToolsPanel} zone The zone to show. Can be null to slide out all zones
		 */
		_slideInZone: function(zone)
		{
			for (var loc in this._panelHierarchy)
			{
				var otherZone = this._panelHierarchy[loc];
				if (otherZone == zone)
				{
					if (zone.getCollapsed())
					{
						zone.floatCollapsedPanel();
					}
				}
				else if (!otherZone._isMainArea() && otherZone.getPlaceholder().isVisible() && !otherZone.getCollapsed())
				{
					otherZone.slideOutFloatedPanel();
				}
			}
		},
		
		getFocusedTool: function()
		{
			return this._focusedTool;
		},
		
		/**
		 * @private
		 * Listener when a tool is activated (the first of its zone). Different from #_onToolFocused.
		 * @param {Ametys.ui.tool.ToolPanel} tool The tool that is activated
		 */
		_onToolActivated: function(tool)
		{
			// Graphically set the color on the zone
			var zoneTabsToolsPanel = tool.ownerCt;
			zoneTabsToolsPanel.addCls(this.toolPrefixCls + tool.getType());
			
			// Launch the listener
			tool.fireEvent("toolactivate", tool);
		},
		
		/**
		 * Listener when a tool is deactivated (not the first of its zone). Different from #_onToolBlurred.
		 * Internal call by listeners
		 * @param {Ametys.ui.tool.ToolPanel} tool The tool that is deactivated
		 */
		_onToolDeactivated: function(tool)
		{
			// Graphically remove the color on the zone
			var zoneTabsToolsPanel = tool.ownerCt;
			zoneTabsToolsPanel.removeCls(this.toolPrefixCls + tool.getType());
			
			// Launch the listener
            tool.fireEvent("tooldeactivate", tool);

		},

		/**
		 * Listener when a tool receive the focus.
		 * Internal call by listeners
		 * @param {Ametys.ui.tool.ToolPanel} tool The tool that has focus
		 * @param {Boolean} [noevent=false] Prevent to fire toolfocus event on the tool
		 */
		_onToolFocused: function(tool, noevent)
		{
			// Set the active tool
			if (tool == this.getFocusedTool())
			{
				return;
			}
			
			if (this.getFocusedTool())
			{
				this._onToolBlurred(this.getFocusedTool());
			}
			this._focusedTool = tool;
			
			// Focus the element
			if (document.activeElement != null && !this._hasForParent(document.activeElement, tool.getId()))    
			{
				tool.getEl().dom.tabIndex = "1";
				tool.el.focus();
			}
			
			// Graphically set the focus
			var zoneTabsToolsPanel = tool.ownerCt;
			zoneTabsToolsPanel.addCls(this.focusCls);

			if (this.initialized)
			{
    			// If not timeouted, a moved tool in a retracted zone will lead to graphical issues CMS-5912
				window.setTimeout(Ext.bind(this._slideInZone, this, [zoneTabsToolsPanel]), 1);
			}


			// Store the last use of this zone
			Ext.Array.remove(this._lastUse, Ext.Array.findBy(this._lastUse, function(item) { return item.location == zoneTabsToolsPanel._location; }));
			Ext.Array.insert(this._lastUse, 0, [{ location: zoneTabsToolsPanel._location, date: new Date() }]);
			
			// Launch the listener
			if (!noevent)
			{
				tool.fireEvent("toolfocus", tool);
			}
		},
		
		/**
		 * @private
		 * Determine if element has a parent with id
		 * @param {HTMLElement} element The element to test
		 * @param {String} parentId The id of a parent
		 */
		_hasForParent: function(element, parentId)
		{
			if (element == null)
			{
				return false;
			}
			else if (element.id == parentId)
			{
				return true;
			}
			
			return this._hasForParent(element.parentNode, parentId)
		},
		
		/**
		 * Listener when a tool lost the focus.
		 * @param {Ametys.ui.tool.ToolPanel} tool The tool that lost focus
		 * @param {Boolean} [noevent=false] Prevent to fire the toolblur event on the panel
		 */
		_onToolBlurred: function(tool, noevent)
		{
			// Graphically set the focus
			var zoneTabsToolsPanel = tool.ownerCt;
			if (zoneTabsToolsPanel)
			{
				zoneTabsToolsPanel.removeCls(this.focusCls);
			}
			
			if (this._focusedTool == tool)
			{
				this._focusedTool = null;
			}

			// Launch the listener
			if (!noevent)
			{
                tool.fireEvent("toolblur", tool);
			}
		},
		
		onToolInfoChanged: function(tool)
		{
			var zoneTabsToolsPanel = tool.ownerCt;
			
			var index = zoneTabsToolsPanel.items.indexOf(tool);
			var tabEl = zoneTabsToolsPanel.getTabBar().items.get(index);
			
			tabEl.setText((tool.getDirtyState() ? "* " : "") + tool.getTitle());
			tabEl.setIcon(Ametys.CONTEXT_PATH + tool.getSmallIcon());
			
			tabEl.setTooltip({
				title: tool.getTitle(),
				image: tool.getLargeIcon() || tool.getMediumIcon() || tool.getSmallIcon() ? (Ametys.CONTEXT_PATH + (tool.getLargeIcon() || tool.getMediumIcon() || tool.getSmallIcon())) : null,
				imageWidth: tool.getLargeIcon() ? 48 : (tool.getMediumIcon() ? 32 : 16),
				imageHeight: tool.getLargeIcon() ? 48 : (tool.getMediumIcon() ? 32 : 16),
				text: tool.getDescription(),
				helpId: tool.getHelpId(),
				inribbon: false
			});
			
			zoneTabsToolsPanel._updateCollapsePlaceHolder();
		},
		
		addTool: function(tool, location)
		{
            location = this.getNearestSupportedLocation(location);
            tool._toolsLayout = this;
            
			Ext.suspendLayouts();

            try
            {
			     this._addToolToUI(tool, location);
                 
    			 tool.fireEvent("toolopen", tool);
            }
            finally
            {
    			Ext.resumeLayouts(true);
            }
		},
        
        /**
         * @private
         * Add a tool to the correct location
         * @param {Ametys.ui.tool.ToolPanel} tool The rendered tool to add
         * @param {String} location The location where to add the tool
         */
        _addToolToUI: function(tool, location)
        {
            var zonedTabsToolPanel = this._panelHierarchy[location];
            
            var newToolType = tool.getType();

            var insertBeforeTabIndex = null;
            zonedTabsToolPanel.items.each( function(item) {
                if ((this._tabPolicy == 'color' && item.getType() == newToolType) // Tab policy color mean, a single tool per color 
                        || (this._tabPolicy == 'tool' && item.getType() == newToolType && item.getType() != Ametys.ui.tool.ToolPanel.TOOLTYPE_0)) // Tab policy tool mean, a single tool per color if color is different from default
                {
                    this.removeTool(item);
                }
                else if (tool.getType() > newToolType)
                {
                    insertBeforeTabIndex = zonedTabsToolPanel.items.indexOf(item);
                    return false;
                }
            });
            
            if (insertBeforeTabIndex != null)
            {
                zonedTabsToolPanel.insert(insertBeforeTabIndex, tool);
            }
            else
            {
                zonedTabsToolPanel.add(tool);
            }
            
            // effectively add
            this.onToolInfoChanged(tool);
        },        
		
		moveTool: function(tool, location)
		{
            location = this.getNearestSupportedLocation(location);
                        
			var oldZoneTabsToolsPanel = tool.ownerCt;
			if (this._panelHierarchy[location] == oldZoneTabsToolsPanel)
			{
                // Tool moved to its current location... let's discard this call
				return;
			}
			
			Ext.suspendLayouts();
			
			if (this.getLogger().isDebugEnabled())
			{
				this.getLogger().debug("Moving tool '" + tool.getId() + "' to location '" + location + "' (from location '" + oldZoneTabsToolsPanel.location + "')");
			}
			
			var isToolActive = this.getFocusedTool() == tool;
			
			// blur the tool (and its old tools panel)
			if (isToolActive)
			{
				this._onToolBlurred(tool, true);
			}
			
			// moves the panel
			var zonedTabsToolPanel = this._panelHierarchy[location];
			
			this._addToolToUI(tool, location);
			
			// activates the tool in its new location
			zonedTabsToolPanel.setActiveTab(tool);

			// fix a bug from extjs
			// when the old location replaces this tab by a new activated tab, the hidden status is incorrect and the tool invisible in the new tab (when this new tab was empty)
			tool.hidden = true
			tool.show();
            tool.tab.show();
			
			// focus the tool
			if (isToolActive)
			{
				this._onToolFocused(tool, true);
			}
			else
			{
				this.focusTool(tool);
			}
			
			Ext.resumeLayouts(true);
            
            tool.fireEvent("toolmoved", tool, location);
		},
		
		removeTool: function(tool)
		{
			Ext.suspendLayouts();

			var zoneTabsToolsPanel = tool.ownerCt;
			
			var wasFocused = this.getFocusedTool() == tool; 
			if (wasFocused)
			{
				this._onToolBlurred(tool);
			}
			
			tool.fireEvent('toolclose', tool, wasFocused);
            
			zoneTabsToolsPanel.remove(tool);
			
			if (this.getFocusedTool() == null)
			{
				var me = this;
		
				// Search for another zone to focus
				Ext.Array.each(this._lastUse, function(item) {
					var location = item.location;
					var zone = me._panelHierarchy[location];
					if (zone.isVisible())
					{
                        me.focusTool(zone.getActiveTab());
						return false;
					}
				});
			}
			
			Ext.resumeLayouts(true);
		},
        
        getSupportedToolPolicies: function()
        {
            return ['all', 'color', 'tool'];
        },
        
        setToolPolicy: function(policy)
        {
            this._tabPolicy = policy;
        }

	}
);
