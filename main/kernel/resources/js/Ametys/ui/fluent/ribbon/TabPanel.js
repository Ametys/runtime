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
 * This class is the main logic class. But creates it through the wrapper Ametys.ui.fluent.ribbon.Ribbon.
 * 
 * Items are {@link Ametys.ui.fluent.ribbon.Panel} and can be classic tabs or contextual tabs.
 * 
 */
Ext.define(
	"Ametys.ui.fluent.ribbon.TabPanel",
	{
		extend: "Ext.tab.Panel",
		alias: 'widget.ametys.ribbon-tabpanel',
		
		statics: {
			/**
			 * The height of the top part of the ribbon (the part holding the menu button and the tabs headers).
			 * @type Number
			 * @private
			 * @readonly
			 */
			HEADER_HEIGHT: 50,
			/**
			 * The height of the main part of the ribbon (the part holding the tabs contents).
			 * @type String
			 * @private
			 * @readonly
			 */
			BODY_HEIGHT: 93 
		},
		
        /**
         * @cfg {String} applicationTitle The application title that will always be visible in the top part, in addition to the title (separated by a '-'). Can be null. Cannot be changed after configuration. Can contains HTML tags.
         */
        applicationTitle: "Ametys",
        
		/**
		 * @cfg {Boolean} border Doesn't apply to ribbon element. Always false.
		 * @private
		 */
		border: false,
		
		/**
		 * @cfg {Boolean} floating Doesn't apply to ribbon element. Always true.
		 * @private
		 */
		floating: true,
		/**
		 * @cfg {Boolean} autoShow Doesn't apply to ribbon element. Always true.
		 * @private
		 */
		autoShow: true,
		/**
		 * @cfg {Boolean} shadow Doesn't apply to ribbon element. Always true.
		 * @private
		 */
		shadow: false,

		/**
		 * @cfg {Ametys.ui.fluent.ribbon.Panel/Ametys.ui.fluent.ribbon.Panel[]} items The tabs to add at creation time. Use {@link Ametys.ui.fluent.ribbon.Panel} here only. See class doc for more information.
		 */

		/**
		 * @cfg {String/Number/Ext.Component} activeTab Doesn't apply to ribbon element. The default tab HAS TO be the first one according to Microsoft UI Fluent Licence. 
		 * @private
		 */
		activeTab: 0,
		/**
		 * @cfg {String} cls Doesn't apply to ribbon element. The value HAS TO be the default value 'x-fluent-tab-panel'.
		 * @private
		 */
		cls: 'x-fluent-tab-panel',

		/**
		 * @cfg {String} defaultType Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		defaultType: 'ametys.ribbon-panel',
		
		/**
		 * @private
		 * @property {Object} defaults Doesn't apply to ribbon element. The value HAS TO be the default value.
		 */
		tabBar: {
			defaults: {
				frame: false
			}
		},

		/**
		 * @property {Boolean} _ribbonCollapsed The ribbon state: collapsed or expanded? Used with #_ribbonFloating.
		 * @private
		 */
		_ribbonCollapsed: false,
		/**
		 * @property {Boolean} _ribbonFloating The ribbon state: floating over the ui or fixed? Used with #_ribbonCollapsed.
		 * @private
		 */
		_ribbonFloating: false,
		/**
		 * @private
		 * @property {Ametys.ui.fluent.ribbon.Panel} _lastContextualTabHidden Last contextual panel selected. Null when hidden for too long.
		 */
		_lastContextualTabHidden: null,
		/**
		 * @private
		 * @property {Boolean} _actionPerfomed Has an action been performed since the last contextual tab was hidden.
		 */
		_actionPerfomed: false,
				
		/**
		 * @cfg {Object} menu The application button configuration. 
		 * The object properties are: 
		 * {String} icon Path to the icon for the application button, 
		 * {Object} tooltip Optionnaly the associated tooltip. Must be a config for {@link Ametys.ui.fluent.tip.Tooltip#create}, 
		 * {Ext.menu.Item/Object[]} items One or array of {@link Ext.menu.Item}. Will be provided as it is to {@link Ext.menu.Menu#cfg-items}
		 */

		/**
		 * Creates new ribbon panel.
		 * Do not call this method by yourself: creates a Ametys.ui.fluent.ribbon.Ribbon instead.
		 * @param {Object} config The config object
		 */
		constructor: function(config)
		{
			/**
			 * @private
			 * @property {Object} _contextualGroups Map&lt;String, Ametys.ui.fluent.ribbon.Panel&gt; The map of contextual group id and the associated group label (in the top part of the ribbon)
			 * The panels will have a _tabs property : {Ametys.ui.fluent.ribbon.Panel[]} _tabs that is a list of the panels associated with this group. 
			 */
			this._contextualGroups = {};

			/**
			 * @private
			 * @property {Object} _scaleGrid The map of the groups organized by size.
			 *	The map is a Map&lt;Number index, Object o&gt; 
			 * 		index is the index of the tab. 
			 * 			o is an object with 2 properties: 
			 * 				current a Number representing the index of the currently selected matrix
			 * 				matrix an Array[Object o2] where o2 is an object with the following properties:
			 * 					resize an Array[String s]. Its size is the same as the tabs one, its values can be "small", "medium" or "large". The array reprensents the possible combinaison of "small" "medium" and "large" groups for a tab
			 * 					doesNotFitIn is a Number representing a size in pixel known for NOT beeing able to display the tab in this configuration of groups.
			 * 					fitIn is a Numer representing a size in pixel known for beeing able to display the tab in this configuration of groups. 
			 */
			this._scaleGrid = {};
			
			/**
			 * @property {Ext.button.Button} _appButton The application button in the top left corner. This button supports the application menu 
			 * @private 
			 */
			this._appButton = new Ext.button.Button({
				arrowCls: 'noarrow',
				cls: 'x-fluent-appbutton' + (config.menu && config.menu.items ? '' : ' x-fluent-appbutton-empty-menu'),
				frame: false,
				
				icon: (config.menu && config.menu.icon) || null,
				tooltip: config.menu && config.menu.tooltip ? config.menu.tooltip : null,
				scale: 'large',
						
				menu: config.menu && config.menu.items ? new Ext.menu.Menu({ items: config.menu.items }) : null,
				handler: config.menu && config.menu.handler
			});

			/**
			 * @cfg {Boolean/Object} header Doesn't apply to ribbon element. The value HAS TO be the default value of type {@link Ametys.ui.fluent.ribbon.TabPanel.Header}.
			 * @private
			 */
			config.title = {
                text: config.title,
				applicationTitle: config.applicationTitle || this.applicationTitle,
				xtype: 'ametys.ribbon-header-title'
			};
            config.header = {
                xtype: 'ametys.ribbon-header',
                titleAlign: 'center',
                titlePosition: 1,
                items: [this._appButton]
            };
            config.tabBar = {
                height: 25
            }

			/**
			 * @cfg {Number} height Doesn't apply to ribbon element. The value HAS TO be the default value HEADER_HEIGHT+BODY_HEIGHT.
			 * @private
			 */
			config.height = Ametys.ui.fluent.ribbon.TabPanel.HEADER_HEIGHT + Ametys.ui.fluent.ribbon.TabPanel.BODY_HEIGHT;
			/**
			 * @cfg {Number} width Doesn't apply to ribbon element. The value HAS TO be the default value HEADER_HEIGHT+BODY_HEIGHT.
			 * @private
			 */

			/**
			 * @cfg {Object} help Specify this to display a help icon in the top right corner. The object is a configuration for a tool: see {@link Ext.panel.Tool} where somme properties cannot be overiden.
			 */
			/**
			 * @cfg {Object} user Specify this to display the name of the connected user. The object is a configuration for a tool: see {@link Ext.button.Button} where somme properties cannot be overiden.
			 */
            
            if (!config.tools)
            {
                config.tools = [];
            }
            config.tools.push({ xtype: 'tbspacer', flex: 0.000000000000001 }); // This spacer will be here to take space when a contextual panel appears
            
            if (config.help || config.user)
			{
				if (config.user)
				{
					config.tools.push(new Ext.Button(Ext.apply({
							cls: 'x-fluent-user',
							iconCls: 'x-fluent-usericon',
							margin: config.help ? '0 5 0 0' : 0
						},
						config.user
					)));
				}
				
				if (config.help)
				{
					config.tools.push(Ext.apply({
							cls: 'x-fluent-help',
							type: 'help'
						}, 
						config.help
					));
				}
			}
            

			config.stateful = true;
			config.stateId = this.self.getName();
			
			this.callParent(arguments);
		},
		
		onRender: function()
		{
			this.setWidth(this.floatParent.getWidth());

			this.callParent(arguments);
		},

        /**
         * @private
         * Do initialization after rendering
         * @param {} container
         * @param {} position
         */
		afterRender: function(container, position) 
		{
			this.callParent(arguments);

			/**
			 * @private
			 * @property {Ext.Element} _rightEl The right part of the ribbon
			 */
			this._rightEl = this.getEl().createChild({ cls: 'x-fluent-tab-right', style: 'height: ' + (Ametys.ui.fluent.ribbon.TabPanel.HEADER_HEIGHT + Ametys.ui.fluent.ribbon.TabPanel.BODY_HEIGHT) + 'px;'}, this.getEl().first());
			
			Ext.getDoc().on("click", this._onAnyMouseDown, this);
			Ext.getDoc().on("contextmenu", this._onAnyMouseDown, this);

			this.tabBar.getEl().on('dblclick', this.toggleRibbonCollapse, this);
			
			this.floatParent.setHeight(Ametys.ui.fluent.ribbon.TabPanel.HEADER_HEIGHT + Ametys.ui.fluent.ribbon.TabPanel.BODY_HEIGHT);
			this.floatParent.on('resize', this._onPlaceHolderResize, this);
			this.getEl().on('mousewheel', this._onTabWheel, this);
			
			this.items.each(this._panelAddedAfterRender, this);
			
			if (this._ribbonCollapsed)
			{
				this.collapseRibbon();
			}
		},
		
		getState: function()
		{
			return {
				_ribbonCollapsed: this._ribbonCollapsed
			};
		},
		
		setActiveTab: function(card)
		{
			var me = this,
				previous;
			
			card = me.getComponent(card)
			if (card)
			{
				previous = me.getActiveTab();

	            if (previous !== card) 
	            {
			        me.lastActiveTab = card;
		
					// Effect deactivated on IE7- for performance purposes
					if (!Ext.isIE7m && previous && previous.contextualTab != null)
					{
						me.getEl().removeCls("x-fluent-tab-panel-contextual-" + previous.contextualTab);
					}
					
					// Effect deactivated on IE7- for performance purposes
					if (!Ext.isIE7m && card.contextualTab != null)
					{
						me.getEl().addCls("x-fluent-tab-panel-contextual-" + card.contextualTab);
					}
	            }
	            
				var result = me.callParent(arguments);
	
				if (previous !== card)
				{
					if (me._ribbonCollapsed) 
					{
						me._showCollapsed();
					}
					
					me._checkSize();
				}
				
				return result;
			}
		},
		
		onResize: function() 
		{
			this.callParent(arguments);
			this._checkSize();
		},
		
		/**
		 * Listener on adding tab to check for contextual tabs (thoses with the contextualTab property).
		 * See class doc to see the properties
		 * @param {Ametys.ui.fluent.ribbon.Panel} panel The panel added
		 * @param {Number} index The new tab index
		 */
		_panelAddedAfterRender: function(panel, index)
		{
			var tabElBtn = this.getTabBar().items.get(index);

			// Add some wrapping div
			tabElBtn.getEl().first().wrap({}).wrap({});

			if (panel.contextualTab != null)
			{
				// Generates a group if no group is specified (the tab will be alone in this group)
				if (panel.contextualGroup == null)
				{
					panel.contextualGroup = Ext.id();
				}

				// Hide the tab button
				tabElBtn.hide();
				// Color the tab button
				tabElBtn.addCls(["x-fluent-tab-contextual", "x-fluent-tab-contextual-" + panel.contextualTab]);
				
				// Creates the group if the tab is the first one of the group
				if (this._contextualGroups[panel.contextualGroup] == null)
				{
					panel._ribbonLabel = this.header.addGroupLabel(panel.contextualLabel, panel.contextualTab);
					panel._ribbonLabel.on('click', this._onRibbonLabelClick, this, panel._ribbonLabel);
					panel._ribbonLabel._tabs = [];
					
					this._contextualGroups[panel.contextualGroup] = panel._ribbonLabel;
				}
				// Add the tab to the existing group
				else
				{
					panel._ribbonLabel = this._contextualGroups[panel.contextualGroup];
				}
				
				// Add the reference to the tab in the group label object
				panel._ribbonLabel._tabs.push(panel);
				
				// Add shortcuts method for showing/hiding the contextual tab
				panel.showContextualTab = function(forceSelection)
				{
					this.ownerCt.showContextualTab(this, forceSelection);
				}
				panel.hideContextualTab = function()
				{
					this.ownerCt.hideContextualTab(this);
				}
			}
			else
			{
				// checking that non contextual tabs must not be after contextual
				for (var i = index - 1; i >= 0; i--)
				{
					var tabEl = this.items.get(i);
					if (!tabEl.hasCls("x-fluent-tab-contextual"))
					{
						if (i + 1 != index)
						{
							tabElBtn.dom.parentNode.insertBefore(tabElBtn.dom, this.items.get(i + 1));
						}
						break;
					}
				}
			}
		},
		
		onAdd: function(panel, index)
		{
			this.callParent(arguments);

			if (this.rendered)
			{
				this._panelAddedAfterRender(panel, index);
			}
		},

		/**
		 * @private
		 * Get the visible contextual tabs in the contextual group of the ribbon panel
		 * @param {Ext.dom.Element} ribbonPanel The ribbon label
		 * @return {Ametys.ui.fluent.ribbon.Panel[]} The tab selection (not the strip panel) that are visible in the contextual group
		 */
		_getVisibleContextualTabs: function(ribbonPanel)
		{
			var visibles = [];
			for (var i = 0; i < ribbonPanel._tabs.length; i++)
			{
				var tab = ribbonPanel._tabs[i];
				var index = this.items.indexOf(tab);
				var tabEl = this.getTabBar().items.get(index);
				if (tabEl.isVisible())
				{
					visibles.push(tab);
				}
			}
			return visibles;
		},
		

		/**
		 * @private
		 * Listener when clicking on a contextual tab group header
		 * @param {Event} e The event
		 * @param {Object} target The html object 
		 * @param {Ext.dom.Element} ribbonPanel The ribbon label panel
		 */
		_onRibbonLabelClick: function(e, target, ribbonPanel)
		{
			this.setActiveTab(this._getVisibleContextualTabs(ribbonPanel)[0]);
		},
		
		/**
		 * @private
		 * Set the width and the position of the ribbonPanel to the sum of the width of the button of the tabs of the given ribbon panel (contextual group label)
		 * Others ribbonPanels positions are modified
		 * @param {Ext.dom.Element} ribbonPanel The ribbon element (contextual tab label)
		 */
		_setTotalWidthOrHide: function(ribbonPanel)
		{
			var tabs = this._getVisibleContextualTabs(ribbonPanel)
			
			// Any tab visible ? show the group label and size it
			if (tabs.length > 0)
			{
				var sum = 0;
				for (var i = 0; i < tabs.length; i++)
				{
					var panel = tabs[i];
					var index = this.items.indexOf(panel);
					var tabEl = this.getTabBar().items.get(index);

					sum += tabEl.getWidth();
					
					var firstVisibleOfTheGroup = (i == 0);
					tabEl[firstVisibleOfTheGroup ? 'addCls' : 'removeCls'](["x-fluent-tab-contextual-left", "x-fluent-tab-contextual-" + panel.contextualTab + "-left"]);
					
					var lastVisibleOfTheGroup = (i == tabs.length - 1);
					tabEl.getEl().first()[lastVisibleOfTheGroup ? 'addCls' : 'removeCls']("x-fluent-tab-contextual-right");
				}
			
				ribbonPanel.setWidth(sum + 2*(tabs.length - 1));
				ribbonPanel.show();
			}
			// No contextual tab is visible: hide it
			else
			{
				ribbonPanel.hide();
			}
			
			for (var ribbonPanelIndex in this._contextualGroups)
			{
				var otherRibbonPanel = this._contextualGroups[ribbonPanelIndex];
				
				var tabs = this._getVisibleContextualTabs(otherRibbonPanel);
				if (tabs.length > 0)
				{
					var panel = tabs[0];
					var index = this.items.indexOf(panel);
					var tabEl = this.getTabBar().items.get(index);
					
					otherRibbonPanel.dom.style.left = (tabEl.getPosition()[0] - 4) + "px";
				}				
			}
		},
		
		/**
		 * @private
		 * Compute the header: size of the text, size for contextual tabs labels...
		 */
		_computeContextualTabsLabels: function()
		{
			// look for first contextual tab
			var contextualTabStripAtLeft = null;
			for (var i = 0; i < this.items.length; i++)
			{
				var panel = this.items.get(i); 
				if (panel.contextualTab != null)
				{
					var tabEl = this.getTabBar().items.get(i);
					if (tabEl.isVisible())
					{
						contextualTabStripAtLeft = tabEl;
						break;
					}
				}
			}
			
			// No contextual tab means the title takes it all
			if (contextualTabStripAtLeft == null)
			{
				this.header.setTitleWidth("auto");
			}
			// Any contextual tab means the title only takes what's available
			else
			{
				this.header.setTitleWidth((contextualTabStripAtLeft.getPosition()[0] - 42 - 4));
			}
		},

		/**
		 * Make the contextual tab visible
		 * @param {Ametys.ui.fluent.ribbon.Panel} panel The tab panel to show
		 * @param {Boolean} forceSelection True will ensure the tab is selected, null will be agnostic (a last selected algo will try to guess) and false will ensure it is not selected. Inherit #changeWasActiveOnHideStatus to change the logic
		 */
		showContextualTab: function(panel, forceSelection)
		{
			if (panel.contextualTab != null)
			{
				var index = this.items.indexOf(panel);
				var tabEl = this.getTabBar().items.get(index);
				if (!tabEl.isVisible())
				{
					tabEl.show();
					
					this._setTotalWidthOrHide(panel._ribbonLabel)
					
					this._computeContextualTabsLabels();
				}
					
			}
			
			if (forceSelection !== false && (panel._activatedOnce == null || panel._wasActiveOnHide == true || forceSelection === true || this._lastContextualTabHidden == panel))
			{
				panel._activatedOnce = true;
				panel._wasActiveOnHide = false;
				
				this.setActiveTab(panel);
				if (this._ribbonCollapsed)
				{
					this._hideCollapsed();
				}
			}
		},		


		/**
		 * Make the contextual tab invisible
		 * @param {Ametys.ui.fluent.ribbon.Panel} panel The tab panel to hide
		 */
		hideContextualTab: function(panel)
		{
			this._lastContextualTabHidden = null;
			this._actionPerfomed = false;

			if (panel.contextualTab != null)
			{
				var index = this.items.indexOf(panel);
				var tabEl = this.getTabBar().items.get(index);
				if (tabEl.isVisible())
				{
					tabEl.hide();
	
					this._setTotalWidthOrHide(panel._ribbonLabel);
	
					panel._wasActiveOnHide = this.activeTab == panel; 
					if (panel._wasActiveOnHide)
					{
						var index = panel._ribbonLabel._tabs.indexOf(panel)
	
						var panelToTests = [];
						for (var i = index - 1; i >= 0; i--)
						{
							var tab = panel._ribbonLabel._tabs[i];
							panelToTests.push(tab);
						}
						for (var i = index + 1; i < panel._ribbonLabel._tabs.length; i++)
						{
							var tab = panel._ribbonLabel._tabs[i];
							panelToTests.push(tab);
						}
						for (var i = this.items.length - 1; i >= 0; i--)
						{
							var tab = this.items.get(i);
							if (tab._ribbonLabel != null)
							{
								for (var j = 0; j < tab._ribbonLabel._tabs.length; j++)
								{
									var tab = tab._ribbonLabel._tabs[j];
									panelToTests.push(tab);
								}
							}
						}
						
						var done = false;
						for (var i = 0; i < panelToTests.length; i++)
						{
							var tab = panelToTests[i];
							var index = this.items.indexOf(tab);
							var tabEl = this.getTabBar().items.get(index);
							if (tabEl.isVisible())
							{
								this.setActiveTab(tab);
								done = true;
								break;
							}
						}
								
						if (!done)
						{
							this.setActiveTab(0);
						}
						
						this._lastContextualTabHidden = panel;
					}
	
					this.changeWasActiveOnHideStatus(panel);
					this._computeContextualTabsLabels();
				}
			}	
		},
		
		/**
		 * @template
		 * @protected
		 * Implement this method to determine if panel was active when hiding.
		 * Default implementation rely on time ellapsed.
		 */
		changeWasActiveOnHideStatus: function(panel)
		{
			// Finally wa may want to remove this flag if activation was too recent to be true
			if (panel._wasActiveOnHide && this._lastHide != null)
			{
				var d = new Date().getTime();
				if (d - this._lastHide < 500)
				{
					panel._wasActiveOnHide = false;
				}
			}
			this._lastHide = new Date().getTime();	
		},

		
		/**
		 * @private Listener when mouse wheel is used over tabs
		 * @param {Event} e The event
		 */
		_onTabWheel: function(e) 
		{
			if (this._ribbonCollapsed) 
			{
				return;
			}

			var scrollSens = (e.getWheelDelta() > 0)
			var initialValue = scrollSens ? 1 : 0;
			var increment = scrollSens ? -1 : 1;

			var i = this.items.indexOf(this.getActiveTab());
			for (var j = i + increment; j >= 0 && j < this.items.length; j += increment)
			{
				if (this.getTabBar().items.get(j).isVisible())
				{
					this.setActiveTab(this.items.get(j));
					e.stopEvent();
					Ext.menu.MenuMgr.hideAll();
					return;
				}
			}
		},		
		
		/**
		 * Listener when the placeholder get resized
         * @param {Ext.Component} placeHolder The place holder
         * @param {Number} width The new width that was set.
         * @param {Number} height The new height that was set.
         * @param {Number} oldWidth The previous width.
         * @param {Number} oldHeight The previous height. 
         * @param {Object} eOpts The event options
         * @private
         */
		_onPlaceHolderResize: function(placeHolder, width, height, oldWidth, oldHeight, eOpts)
		{
			if (width != oldWidth)
			{
				this.setWidth(width);
			}
		},
		
		/**
		 * Enlarge or reduce groups of the active tab to best fit in the ribbon (depending on the screen width)
		 * @private
		 */
		_checkSize: function()
		{
			if (this.getActiveTab())
			{
				var currentActiveTabWidth = this.getActiveTab().getWidth();
				
				var index = this.items.indexOf(this.getActiveTab());
				if (this._scaleGrid[index] == null)
				{
					this._buildScalesGrid(index);
				}
				var grid = this._scaleGrid[index];
				
				var doesNotFit = false;
				this.getActiveTab().items.each(function (e)
				{
					if (e.canBeVisible && !e.canBeVisible())
					{
						doesNotFit = true;
					}
				});
				
				if (doesNotFit)
				{
					if (!grid.matrix[grid.current].doesNotFitIn || grid.matrix[grid.current].doesNotFitIn < currentActiveTabWidth)
					{
						grid.matrix[grid.current].doesNotFitIn = currentActiveTabWidth;
					}

					if (grid.current + 1 < grid.matrix.length)
					{
						grid.current = this._findNextIndexToTestInGrowingGrid(grid, currentActiveTabWidth);
						
						Ext.suspendLayouts();
						for (var i = 0; i < grid.matrix[grid.current].resize.length; i++)
						{
							this.getActiveTab().items.get(i).setScale(grid.matrix[grid.current].resize[i]);
						}
						Ext.resumeLayouts(true);
						
						// may need to go under !
						this._checkSize();
					}
					else
					{
						// does not fit in the window, but already at the min size
						return;
					}
				}
				else
				{
					if (!grid.matrix[grid.current].fitIn || grid.matrix[grid.current].fitIn > currentActiveTabWidth)
					{
						grid.matrix[grid.current].fitIn = currentActiveTabWidth;
					}
					
					// Can we grow ?
					if (grid.current > 0)
					{
						if (!grid.matrix[grid.current - 1].doesNotFitIn || currentActiveTabWidth > grid.matrix[grid.current - 1].doesNotFitIn)
						{
							// have a try
							grid.current = this._findNextIndexToTestInDecreasingGrid(grid, currentActiveTabWidth);
							
							Ext.suspendLayouts();
							for (var i = 0; i < grid.matrix[grid.current].resize.length; i++)
							{
								this.getActiveTab().items.get(i).setScale(grid.matrix[grid.current].resize[i]);
							}
							Ext.resumeLayouts(true);

							this._checkSize();
						}
					}
				}
			}
		},
		
		/**
		 * @private
		 * Test the given grid for the largest scale possible while screen is reduced
		 * @param {Object} grid One grid of #_scaleGrid
		 * @param {Number} currentActiveTabWidth The width available
		 */
		_findNextIndexToTestInGrowingGrid: function(grid, currentActiveTabWidth)
		{
			var minimalAnswer = grid.current + 1;
			
			for (var i = minimalAnswer; i < grid.matrix.length - 1; i++)
			{
				if (grid.matrix[i].fitIn && currentActiveTabWidth >= grid.matrix[i].fitIn)
				{
					return i;
				}
				else if (grid.matrix[i].doesNotFitIn &&  currentActiveTabWidth <= grid.matrix[i].doesNotFitIn)
				{
					minimalAnswer = i + 1;
				}
			}
			
			return minimalAnswer;
		},
		
		/**
		 * @private
		 * Test the given grid for the largest scale possible while screen is enlarged
		 * @param {Object} grid One grid of #_scaleGrid
		 * @param {Number} currentActiveTabWidth The width available
		 */
		_findNextIndexToTestInDecreasingGrid: function(grid, currentActiveTabWidth)
		{
			var minimalAnswer = grid.current - 1;
			
			for (var i = minimalAnswer; i >= 0; i--)
			{
				if (grid.matrix[i].fitIn && currentActiveTabWidth >= grid.matrix[i].fitIn)
				{
					minimalAnswer = i;
				}
				else if (grid.matrix[i].doesNotFitIn &&  currentActiveTabWidth <= grid.matrix[i].doesNotFitIn)
				{
					return i + 1;
				}
			}
			
			return minimalAnswer;
		},
		
		/**
		 * Build the scale grid #_scaleGrid.
		 * @param {Number} index The tab index to compute
		 * @private
		 */
		_buildScalesGrid: function(index)
		{
			var scaleGrid = this._scaleGrid; 
			scaleGrid[index] = [];

			var tab = this.items.get(index);
			
			// compute the priority between groups
			var priority = [];
			tab.items.each(function (e, i)
			{
				priority.push({index: i, priority: e.priority ? e.priority : 0});
			});
			function compare(e1, e2)
			{
				return (e1.priority != e2.priority) ? e1.priority < e2.priority : e2.index < e1.index;
			}
			priority.sort(compare);
			
			// fill the matrix
			function fill(first, second, fullspeed)
			{
				for (var cursor = fullspeed ? 1 : 0; cursor <= priority.length; cursor++)
				{
					var line = [];
					for (var i = 0; i < priority.length; i++) { line.push("X"); }

					for (var i = 0; i < priority.length - cursor; i++)
					{
						line[priority[i].index] = first;
					}
					for (var i = 0; i < cursor; i++)
					{
						line[priority[priority.length - cursor + i].index] =second;
					}
					
					scaleGrid[index].matrix.push({resize: line});
				}
			}
			
			scaleGrid[index].matrix = [];
			scaleGrid[index].current = 0;

			fill("large", "medium", false);
			fill("medium", "small", true);
			
			// change unexisting combo & remove duplicates
			for (var i = scaleGrid[index].matrix.length - 1; i >= 0 ; i--)
			{
				var grid = scaleGrid[index].matrix[i];
				for (var j = 0; j < grid.resize.length; j++)
				{
					if (!tab.items.get(j).supportScale(grid.resize[j]))
					{
						grid.resize[j] = "medium";
					}
				}
				
				if (i + 1 < scaleGrid[index].matrix.length)
				{
					var areTheSame = true;
					
					var grid = scaleGrid[index].matrix[i];
					var grid2 = scaleGrid[index].matrix[i + 1];
					for (var j = 0; j < grid.resize.length; j++)
					{
						if (grid.resize[j] != grid2.resize[j])
						{
							areTheSame = false;
							break;
						}
					}	
					
					if (areTheSame)
					{
						Ext.Array.remove(scaleGrid[index].matrix, grid2);
					}
				}
			}
		},
		
		/**
		 * Listener for preventing right click on the ribbon
         * @param {Ext.event.Event} e The {@link Ext.event.Event} encapsulating the DOM event.
         * @param {HTMLElement} t The target of the event.
         * @param {Object} eOpts The options object passed to Ext.util.Observable.addListener
         */
		_onAnyMouseDown: function(e, t, eOpts) 
		{
			if (Ext.fly(t).findParent(".x-fluent-tab-panel"))
			{
				if (e.button != 0)
				{
					e.preventDefault();
				}
			}
			else
			{
				// We want to recollapse the ribbon when in collapse mode (but not if the click was in the tab panel
				if (this._ribbonCollapsed && this._ribbonFloating) 
				{
					this._hideCollapsed();
				}
				
				if (this._actionPerfomed)
				{
					this._lastContextualTabHidden = null;
				}
				
				this._actionPerfomed = true;
			}
		},
		
		/**
		 * Toggle the ribbon state
		 * @chainable
		 */
		toggleRibbonCollapse: function() 
		{
			this[this._ribbonCollapsed ? 'expandRibbon' : 'collapseRibbon']();
			return this;
		},

		/**
		 * Enter in collapsed mode
		 * @chainable
		 */
		collapseRibbon: function() 
		{
			Ext.suspendLayouts();
			
			this.floatParent.setHeight(Ametys.ui.fluent.ribbon.TabPanel.HEADER_HEIGHT);
			
			this._ribbonCollapsed = true;
			this.saveState();

			this._ribbonFloating = true;

			this._hideCollapsed();

			Ext.resumeLayouts(true);

			return this;
		},
		
		/**
		 * @private 
		 * When in collapsed mode but visible: hide it
		 */
		_hideCollapsed: function() 
		{
			if (this._ribbonCollapsed && this._ribbonFloating) 
			{
				Ext.suspendLayouts();

				this._ribbonFloating = false;

				this.body.setDisplayed(false);
				this.setHeight(Ametys.ui.fluent.ribbon.TabPanel.HEADER_HEIGHT);
				
				// Deactivate the active tab
				this.getTabBar().activeTab.el.removeCls('x-tab-active');
				if (this.activeTab) 
				{
					this.activeTab.fireEvent('deactivate', this.activeTab);
					this.activeTab = null;
				}
				
				Ext.resumeLayouts(true);
			}
		},

		/**
		 * Expand the ribbon
		 * @chainable
		 */
		expandRibbon: function() 
		{
			Ext.suspendLayouts();

			this.floatParent.setHeight(Ametys.ui.fluent.ribbon.TabPanel.HEADER_HEIGHT + Ametys.ui.fluent.ribbon.TabPanel.BODY_HEIGHT);

			this._showCollapsed();
			this._ribbonCollapsed = false;
			this.saveState();

			Ext.resumeLayouts(true);

			return this;
		},

		/**
		 * @private When in collapsed mode, show it
		 */
		_showCollapsed: function() 
		{
			if (this._ribbonCollapsed && !this._ribbonFloating) 
			{
				Ext.suspendLayouts();

				this._ribbonFloating = true;
				
				this.body.setDisplayed(true);
				this.setHeight(Ametys.ui.fluent.ribbon.TabPanel.HEADER_HEIGHT + Ametys.ui.fluent.ribbon.TabPanel.BODY_HEIGHT);

				if (this.getTabBar().activeTab.el.isVisible())
				{
					this.setActiveTab(this.lastActiveTab);
					this.getTabBar().activeTab.el.addCls('x-tab-active');
				}
				else
				{
					this.setActiveTab(0);
				}
				
				Ext.resumeLayouts(true);
			}
		}		
	}
);
