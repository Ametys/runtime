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
 * This implementation of Ametys.ui.tool.ToolsLayout is a complex one.
 * It put the tools into several Ext.tab.Panel that are devided into locations.
 * It supports drag'n'drop between locations and save the new default location for tools into the user preferences.
 * Regions supported are : center (left and right) left, right, top and bottom.
 */
Ext.define("Ametys.ui.tool.layout.ZonedTabsToolsLayout", 
	{
		extend: "Ametys.ui.tool.ToolsLayout",
        
        statics:
        {
            /**
             * @property {Object} __REGION_MINSIZE The minimum size of regions of this layout 
             * @property {Object} __REGION_MINSIZE.width The minimum width in pixels for a region 
             * @property {Object} __REGION_MINSIZE.height The minimum height in pixels for a region 
             * @private
             * @readonly
             */         
            __REGION_MINSIZE: {width: 100, height: 100},
            
            /**
             * @property {Object} __ADDITIONNAL_ZONE_CONFIG_LEFT Properties applyed to the configuration of the left zone
             * @private
             * @readonly
             */
            __ADDITIONNAL_ZONE_CONFIG_LEFT: {},
            
            /**
             * @property {Object} __ADDITIONNAL_ZONE_CONFIG_RIGHT Properties applyed to the configuration of the left zone
             * @private
             * @readonly
             */
            __ADDITIONNAL_ZONE_CONFIG_RIGHT: {}
        },
        
        /** @cfg {String} focusCls A css classname added to the zone, when one of its tools has the focus */
        focusCls: "a-tool-layout-zoned-focused",
        /** @cfg {String} nofocusCls A css classname added to the zone, when none of its tools has the focus */
        nofocusCls: "a-tool-layout-zoned-notfocused",
        
        /** @cfg {String} toolPrefixCls A css classname prefix. Added with the type value, it leads to a classname added to the zone corresponding to the type of the active tool. */
        toolPrefixCls: "a-tool-layout-zoned-panel-",
        
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
            
            Ext.getDoc().on("click", this._onAnyMouseDown, this);
            Ext.getDoc().on("contextmenu", this._onAnyMouseDown, this);
		},
        
        /**
         * Listener for preventing right click on the ui
         * @param {Ext.event.Event} e The {@link Ext.event.Event} encapsulating the DOM event.
         * @param {HTMLElement} t The target of the event.
         * @param {Object} eOpts The options object passed to Ext.util.Observable.addListener
         */
        _onAnyMouseDown: function(e, t, eOpts) 
        {
            // We want to cancel right-click
            if (e.button != 0)
            {
                if (this._panel && (Ext.fly(t).findParent("#" + this._panel.getId())))
                {
                    e.preventDefault();
                    return;
                }       
                
                // As tabpanels may not be nested, we need to test on each directly
                var locs = this.getSupportedLocations();
                for (var i = 0; i < locs.length; i++)
                {
                    var el = this._panelHierarchy[locs[i]].getEl();
                    if (el && Ext.fly(t).findParent("#" + el.getId()))
                    {
                        e.preventDefault();
                        return;
                    }
                }
            }                
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
            if (Ext.Array.indexOf(this.getSupportedLocations(), desiredLocation) != -1)
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
                ui: 'tool-layout',
                
				border: false,
				layout: {
                    type: 'vbox',
                    align: 'stretch'
                },

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
            var placeholderClass = "Ametys.ui.tool.layout.ZonedTabsToolsLayout.ZoneTabsToolsPanelPlaceHolder";
            var containerClass = "Ametys.ui.tool.layout.ZonedTabsToolsLayout.Container";
			
			var panels = [
                Ext.create(placeholderClass, {
                    stateful: true,
                    stateId: placeholderClass + "$t",
                    split: this._createSplitConfig('top'),
                    minHeight: this.self.__REGION_MINSIZE.height,
                    items: [ this._panelHierarchy['t'] = Ext.create(panelClass, Ext.apply({ location: 't', toolsLayout: this, collapsible: true, collapseMode: "header", collapseDirection: "top", floating: true, autoShow: true, shadow: false, floatingSplit: this._createFloatingSplit('top') }, this.self.__ADDITIONNAL_ZONE_CONFIG_OTHER)) ]
                }),
                Ext.create(containerClass, {
                    stateful: true,
                    stateId: containerClass + "$",
                    split: this._createSplitConfig('bottom'),
                    items: [
                        Ext.create(placeholderClass, {
                            stateful: true,
                            stateId: placeholderClass + "$l",
                            split: this._createSplitConfig('left'),
                            minWidth: this.self.__REGION_MINSIZE.width,
                            items: [ this._panelHierarchy['l'] = Ext.create(panelClass, Ext.apply({ location: 'l', toolsLayout: this, collapsible: true, collapseMode: "header", collapseDirection: "left", floating: true, autoShow: true, shadow: false, floatingSplit: this._createFloatingSplit('left') }, this.self.__ADDITIONNAL_ZONE_CONFIG_LEFT)) ]
                        }),
                        Ext.create(containerClass, {
                            stateful: true,
                            stateId: containerClass + "$c",
                            split: this._createSplitConfig('right'),
                            items: [
        				        this._panelHierarchy['cl'] = Ext.create(panelClass, Ext.apply({ location: 'cl', toolsLayout: this, stateful: true, stateId: panelClass + "$cl", minHeight: this.self.__REGION_MINSIZE.height, minWidth: this.self.__REGION_MINSIZE.width }, this.self.__ADDITIONNAL_ZONE_CONFIG_OTHER)),
        				        this._panelHierarchy['cr'] = Ext.create(panelClass, Ext.apply({ location: 'cr', toolsLayout: this, stateful: true, stateId: panelClass + "$cr", minHeight: this.self.__REGION_MINSIZE.height, minWidth: this.self.__REGION_MINSIZE.width }, this.self.__ADDITIONNAL_ZONE_CONFIG_OTHER))
                            ]
                        }),
                        Ext.create(placeholderClass, {
                            stateful: true,
                            stateId: placeholderClass + "$r",
                            minWidth: this.self.__REGION_MINSIZE.width,
                            items: [ this._panelHierarchy['r'] = Ext.create(panelClass, Ext.apply({ location: 'r', toolsLayout: this, collapsible: true, collapseMode: "header", collapseDirection: "right", floating: true, autoShow: true, shadow: false, floatingSplit: this._createFloatingSplit('right') }, this.self.__ADDITIONNAL_ZONE_CONFIG_RIGHT)) ]
                        })
                    ]
                }),
                Ext.create(placeholderClass, {
                    stateful: true,
                    stateId: placeholderClass + "$b",
                    minHeight: this.self.__REGION_MINSIZE.height,
                    items: [ this._panelHierarchy['b'] = Ext.create(panelClass, Ext.apply({ location: 'b', toolsLayout: this,collapsible: true, collapseMode: "header", collapseDirection: "bottom", floating: true, autoShow: true, shadow: false, floatingSplit: this._createFloatingSplit('bottom') }, this.self.__ADDITIONNAL_ZONE_CONFIG_OTHER)) ]
                })
			];
			
			return panels;
		},
        
        /**
         * @private
         * Create the split configuration
         * @param {String} direction The collapse direction between 'left', 'right', 'top' and 'bottom'.
         * @return {Object} A split configuration  
         */
        _createSplitConfig: function(direction)
        {
            return { 
                ui: 'tool-layout', 
                performCollapse: false,
                renderData: { 
                    collapsible: true, 
                    collapseDir: direction 
                },
                listeners: {
                    'click': {
                        element: 'collapseEl',
                        fn: this._splitClickListener,
                        args: [ direction ],
                        scope: this
                    }
                }
            };
        },
        
        /**
         * @private
         * Create the split configuration for the floating panel
         * @param {String} direction The collapse direction between 'left', 'right', 'top' and 'bottom'.
         * @return {Ext.resize.Splitter} The splitter
         */
        _createFloatingSplit: function(direction)
        {
            return Ext.create({
                xtype: 'splitter',
                ui: 'tool-layout',
                hidden: true,
                performCollapse: false,
                renderData: { 
                    collapsible: true, 
                    collapseDir: direction == 'top' ? 'bottom' : direction == 'bottom' ? 'top' : direction == 'left' ? 'right' : 'left' 
                },
                tracker: {
                    xclass: 'Ametys.ui.tool.layout.ZonedTabsToolsLayout.FloatingSplitterTracker'
                },
                listeners: {
                    'click': {
                        element: 'collapseEl',
                        fn: this._splitClickListener,
                        args: [ direction ],
                        scope: this
                    }
                }
            });
        },
        
        /**
         * @private
         * Click on the splitter
         * @param {String} The direction used during #_createSplitConfig
         * @param {Ext.event.Event} event The click event
         * @param {HTMLElement} target The click target
         * @param {Object} options The listener options
         */
        _splitClickListener: function(direction, event, target, options)
        {
            this._panelHierarchy[direction.substring(0, 1)]._expandOrCollapse()
        },
		
		focusTool: function(tool)
		{
            this.callParent(arguments);
            
            if (tool != null)
            {
    			var zoneTabsToolsPanel = tool.ownerCt;
    			
    			if (zoneTabsToolsPanel.getActiveTab() != tool)
    			{
    				zoneTabsToolsPanel.setActiveTab(tool);
    			}
    			
    			this._onToolFocused(tool);
            }
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
    				zone._slideIn();
				}
				else if (!otherZone._isMainArea())
				{
					otherZone._slideOut();
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
            zoneTabsToolsPanel.removeCls(this.nofocusCls);

			if (this.initialized)
			{
    			// If not timeouted, a moved tool in a retracted zone will lead to graphical issues CMS-5912
			    window.setTimeout(Ext.bind(this._slideInZone, this, [zoneTabsToolsPanel]), 1);
                
                // Moreover a tool added in a collapsed zone may have layout issues (especially grids)
                if (zoneTabsToolsPanel.collapsed && tool.getWidth() < 10)
                {
                    zoneTabsToolsPanel.on('expand', tool.updateLayout, tool, { single: true});
                }
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
		 * @param {Ametys.ui.tool.ToolPanel} tool The tool that lost focus. Can be null.
		 * @param {Boolean} [noevent=false] Prevent to fire the toolblur event on the panel
		 */
		_onToolBlurred: function(tool, noevent)
		{
            if (tool == null)
            {
                return;
            }
            
			// Graphically set the focus
			var zoneTabsToolsPanel = tool.ownerCt;
			if (zoneTabsToolsPanel)
			{
				zoneTabsToolsPanel.removeCls(this.focusCls);
                zoneTabsToolsPanel.addCls(this.nofocusCls);
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
            
            if (Ext.isFunction(this.titleChangedCallback))
            {
                this.titleChangedCallback(null);
            }
		},
		
		onToolInfoChanged: function(tool)
		{
			var zoneTabsToolsPanel = tool.ownerCt;
			
			var index = zoneTabsToolsPanel.items.indexOf(tool);
			var tabEl = zoneTabsToolsPanel.getTabBar().items.get(index);
			
			tabEl.setText((tool.getDirtyState() ? "* " : "") + tool.getTitle());
			
			if (tool.getGlyphIcon() != null)
			{
				tabEl.setIconCls(tool.getGlyphIcon() + (tool.getIconDecorator() != null ? ' ' + tool.getIconDecorator() : ''));
                tabEl.setIcon(null);
			}
			else
			{
				tabEl.setIcon(Ametys.CONTEXT_PATH + tool.getMediumIcon());
                tabEl.setIconCls(null);
			}
			
			tabEl.setTooltip({
				title: tool.getTitle(),
				glyphIcon: tool.getGlyphIcon(),
				iconDecorator: tool.getIconDecorator(),
				image: !tool.getGlyphIcon() && (tool.getLargeIcon() || tool.getMediumIcon() || tool.getSmallIcon()) ? (Ametys.CONTEXT_PATH + (tool.getLargeIcon() || tool.getMediumIcon() || tool.getSmallIcon())) : null,
				imageWidth: tool.getGlyphIcon() || tool.getLargeIcon() ? 48 : (tool.getMediumIcon() ? 32 : 16),
				imageHeight: tool.getGlyphIcon() || tool.getLargeIcon() ? 48 : (tool.getMediumIcon() ? 32 : 16),
				text: tool.getDescription(),
				helpId: tool.getHelpId(),
				inribbon: false
			});
			
            if (tool.ownerCt.hasCls(this.focusCls) && tool.ownerCt.getActiveTab() == tool && Ext.isFunction(this.titleChangedCallback))
            {
                this.titleChangedCallback(tool.getTitle());
            }
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
            
            var newToolPriority = tool.getPriority();

            var insertBeforeTabIndex = null;
            zonedTabsToolPanel.items.each( function(item) {
                if ((this._tabPolicy == 'color' && item.getPriority() == newToolPriority) // Tab policy color mean, a single tool per color 
                        || (this._tabPolicy == 'tool' && item.getPriority() == newToolPriority && item.getPriority() != 0)) // Tab policy tool mean, a single tool per color if color is different from default
                {
                    item.close();
                }
                else if (item.getPriority() > newToolPriority)
                {
                    insertBeforeTabIndex = zonedTabsToolPanel.items.indexOf(item);
                    return false;
                }
            }, this);
            
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
			
			
            this._onToolBlurred(this.getFocusedTool());
            this._onToolDeactivated(tool);
			
			// moves the panel
			var zoneTabsToolPanel = this._panelHierarchy[location];
			
			this._addToolToUI(tool, location);
			
			// activates the tool in its new location
			zoneTabsToolPanel.setActiveTab(tool);

			// fix a bug from extjs
			// when the old location replaces this tab by a new activated tab, the hidden status is incorrect and the tool invisible in the new tab (when this new tab was empty)
			tool.hidden = true
			tool.show();
            tool.tab.show();

            // activate the tool ?
            
			// focus the tool
			this.focusTool(tool);
			
			Ext.resumeLayouts(true);
            
            tool.fireEvent("toolmoved", tool, location);
		},
		
		removeTool: function(tool)
		{
			Ext.suspendLayouts();

			var zoneTabsToolsPanel = tool.ownerCt;
			
            if (zoneTabsToolsPanel.getActiveTab() == tool)
            {
                this._onToolDeactivated(tool);
            }
            
			var wasFocused = this.getFocusedTool() == tool; 
			if (wasFocused)
			{
				this._onToolBlurred(tool);
			}
			
			tool.fireEvent('toolclose', tool, wasFocused);
			
			tool._toolsLayout = null; // remove tool panel reference to the layout
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
                
                if (this.getFocusedTool() == null)
                {
                    this.focusTool(null);
                }
			}
			
			Ext.resumeLayouts(true);
		},
        
        getSupportedToolPolicies: function()
        {
            return ['all', 'color', 'tool'];
        },
        
        setToolPolicy: function(policy)
        {
            if (Ext.Array.contains(this.getSupportedToolPolicies(), policy))
            {
                this._tabPolicy = policy;
            }
            else
            {
                this._tabPolicy =  this.getSupportedToolPolicies()[0];
            }
        }

	}
);
