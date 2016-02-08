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
 * One tab panel for tool in regions of the Ametys.ui.tool.layout.ZonedTabsToolsLayout.
 * @private
 */
Ext.define("Ametys.ui.tool.layout.ZonedTabsToolsLayout.ZoneTabsToolsPanel", 
	{
		extend: "Ext.tab.Panel",
        
        xtype: 'zonetabpanel',
		
        /**
         * @cfg {String} ui=tool-layoutzone @inheritdoc
         */
        ui: 'tool-layoutzone',
        
		/**
		 * @cfg {String} location The location of the zone. See locations in Ametys.ui.tool.layout.ZonedTabsToolsLayout.
		 */
		/**
		 * @property {String} _location See #cfg-location
		 * @private
		 */
		/**
		 * @cfg {Ametys.ui.tool.layout.ZonedTabsToolsLayout} toolsLayout The tools layout instance
		 */
		/**
		 * @property {Ametys.ui.tool.layout.ZonedTabsToolsLayout} _toolsLayout See #cfg-toolsLayout
		 * @private
		 */	
		
        /** @cfg {String} typePrefixCls A css classname prefix. Concatenated with the type will give a CSS classname for the tool */
        typePrefixCls: "ametys-tab-color-",
		
		hidden: true,
		
		deferredRender: false,
		
		split: { ui: 'tool-layout' },
        
		/**
		 * @property {Ext.menu.Menu} _contextMenu The menu to show on right click on the tabs
		 * @private
		 */
		/**
		 * @property {String} _contextualMenuTargetToolId The target tool id of the right click when context menu is opened
		 * @private
		 */
		
		/**
		 * Creates a zone tabs
		 * @param {Object} config See configuration doc.
		 */
		constructor: function(config)
		{
            config = config || {};
            
			this._contextMenu = new Ext.menu.Menu({
		        items: [{
		            text: "<i18n:text i18n:key='PLUGINS_CORE_UI_MSG_TOOLS_CLOSE_TAB'>Close</i18n:text>", // Default translation for js test purposes
		            handler: this._closeCurrentTab,
		            iconCls: "a-tools-menu-close",
		            scope: this
		        },
		        '-',
		        {
		            text: "<i18n:text i18n:key='PLUGINS_CORE_UI_MSG_TOOLS_CLOSE_OTHERS'>Close others</i18n:text>", // Default translation for js test purposes
		            handler: this._closeOthersTabs,
                    iconCls: "a-tools-menu-close-others",
		            scope: this
		        },
		        {
		            text: "<i18n:text i18n:key='PLUGINS_CORE_UI_MSG_TOOLS_CLOSE_ALL'>Close all</i18n:text>", // Default translation for js test purposes
		            handler: this._closeAllTabs,
                    iconCls: "a-tools-menu-close-all",
		            scope: this
		        }]
		    });

			
			this._location = config.location;
			this._toolsLayout = config.toolsLayout;
            
			config.stateful = true;
			config.stateId = this.self.getName() + "$" + this._location;
            
            
            config.tabBarHeaderPosition = 0;
            config.tabBar = config.tabBar || {};
            config.tabBar.flex = 1;
            config.header = { 
                titlePosition: 1
            };
            config.title = {
                text: '',
                hidden: true,
                flex: 0
            };
            
			this.callParent(arguments);

            this.on('beforeshow', this._onShow, this);
			this.on('unfloat', this._onUnfloat, this);
            this.on('afterrender', this._onAfterRender, this);
		},
        
        /**
         * @private
         * Listener on show
         * @param {Ametys.ui.tool.layout.ZonedTabsToolsLayout.ZoneTabsToolsPanel} me This component
         */
        _onShow: function(me)
        {
            if (!this.flex)
            {
                var brother;
                if (brother = this.nextSibling("zoned-container") || this.previousSibling("zoned-container"))
                {
                    this.flex = brother.flex / 3;
                }
                else if (brother = this.nextSibling("zonetabpanel") || this.previousSibling("zonetabpanel"))
                {
                    this.flex = brother.flex || 1;
                }
            }
        },
		
		getState: function()
		{
			var state = this.callParent(arguments);
			
			if (!this._isMainArea())
			{
				state = this.addPropertyToState(state, 'collapsed', (this.getCollapsed() != false) || Boolean(this.floatedFromCollapse));
			}
			
			return state;
		},

		/**
		 * @private
		 * Update the tabs buttons visible on the collapsed tab
		 */
		_updateCollapsePlaceHolder: function()
		{
            if (this._isMainArea())
            {
                return;
            }
            
			Ext.suspendLayouts();
			
			var me = this;
			Ext.Array.each(this.getPlaceholder().getTools(), function(item) {
				me.getPlaceholder().remove(item);
			});
			
			this.getPlaceholder().tools = [];

            if (this.getPlaceholder().isVisible())
            {
                this.getPlaceholder().getTitle().hide();
            }
			
			var size = this.items ? this.items.getCount() : 0;
			if (size > 0)
			{
				var vertical = this._location == 'l' || this._location == 'r';
				
				me.getPlaceholder().addTool({
					type: 'none',
					cls: 'x-tool-first',
					width: vertical ? 20 : 1,
					height: vertical ? 1 : 20
				});

				for (var index = 0; index < size; index++)
				{
					var item = this.items.getAt(index);
					
					me.getPlaceholder().addTool({
						type: 'none',
						width: vertical ? 20 : 16,
								height: vertical ? 16 : 20,
										renderData: { blank: Ametys.CONTEXT_PATH + item.getSmallIcon() },
										tooltip: this.getTabBar().items.get(index).tooltip,
										handler: Ext.bind(this._onCollapsePlaceHolderClick, this, [item.id], false) 
					});
				}

				me.getPlaceholder().addTool({
					type: 'none',
					cls: 'x-tool-last',
					width: vertical ? 20 : 1,
					height: vertical ? 1 : 20
				});
			}

			if (!this.getPlaceholder().hasCls("ametys-tool-collapsed"))
			{
				this.getPlaceholder().addCls("ametys-tool-collapsed");
			}
			
			Ext.resumeLayouts(true);
		},
		
		/**
		 * @private
		 * Listener when a click is done on a button of the collapse panel
		 * @param {String} toolId The tool to focus
		 */
		_onCollapsePlaceHolderClick: function(toolId)
		{
            this._toolsLayout.focusTool(Ext.getCmp(toolId));
		},
		
	    /**
	     * @private
	     * Listener when user clicks on the tab bar
	     */
	    _onClickOnTabBar: function()
	    {
			var panel = this.getActiveTab()
	    	if (panel != null)
	    	{
                this._toolsLayout.focusTool(panel);
	    	}
	    },

        /**
         * @private
         * Listener after render
         */
	    _onAfterRender: function()
	    {
	    	// Context menu
	    	this.getTabBar().mon(this.getTabBar().getEl(), {
	            scope: this,
	            contextmenu: this._onContextMenu,
	            delegate: '.x-tab'
	        });
	    	this.getTabBar().mon(this.getTabBar().getEl(), 'click', this._onClickOnTabBar, this);
	    	
			/* TODO reimplement pin/unpin without border layout 
            if (!this._isMainArea())
			{
				// Insert a spacer that will take only available space
				this.getTabBar().add({
					xtype: 'tbspacer',
					flex: 0.000000001,
					minWidth: 2
				})
				
				this.getTabBar().add({
                    xtype: 'tool',
					itemId: 'unpin',
					type: "unpin",
				    qtip: "<i18n:text i18n:key='PLUGINS_CORE_UI_MSG_TOOLS_UNPIN'/>",
				    handler: Ext.bind(this._onTabBarUnpinToolClick, this),
				    width: 16,
				    flex: 0
				});
				this.getTabBar().add({
                    xtype: 'tool',
					itemId: 'pin',
					type: "pin",
					hidden: true,
				    qtip: "<i18n:text i18n:key='PLUGINS_CORE_UI_MSG_TOOLS_PIN'/>",
				    handler: Ext.bind(this._onTabBarPinToolClick, this),
				    width: 16,
				    flex: 0
				});
				
				this.on('collapse', this._onCollapse, this);
				this.on('expand', this._onExpand, this);
			}*/
	    },
	    
	    /**
	     * @private
	     * Listener when clicking on the unpin button
	     * @param {Ext.event.Event} event The click event.
		 * @param {Ext.Element} toolEl The tool Element.
		 * @param {Ext.panel.Header} bar The host panel header. 
		 * @param {Ext.panel.Tool} tool The tool object
	     */
	    _onTabBarUnpinToolClick: function(event, toolEl, bar, tool)
	    {
	    	bar.ownerCt.collapse();
	    },

	    /**
	     * @private
	     * Listener when clicking on the pin button
	     * @param {Ext.event.Event} event The click event.
		 * @param {Ext.Element} toolEl The tool Element.
		 * @param {Ext.panel.Header} bar The host panel header. 
		 * @param {Ext.panel.Tool} tool The tool object
	     */
	    _onTabBarPinToolClick: function(event, toolEl, bar, tool)
	    {
	    	bar.ownerCt.expand(false);
	    },

		/**
		 * @private
		 * Listener when the tabpanel is collapsed
		 */
		_onCollapse: function(animated)
		{
			this.getLogger().debug("Collapsing '" + this._location + "' zone")

			this.getTabBar().getComponent("unpin").hide();
			this.getTabBar().getComponent("pin").show()
		
			Ext.defer(this._updateCollapsePlaceHolder, 1, this);
	    	
	    	this._onUnfloat();
	    	
	    	this.saveState();
		},
		
		/**
		 * @private
		 * Listener when the tabpanel is unfloated
		 */
		_onUnfloat: function()
		{
			this.getLogger().debug("Unfloating '" + this._location + "' zone")

			var panel = this.getActiveTab()
	    	if (panel != null)
	    	{
	    		this._toolsLayout._onToolDeactivated(panel);
	    		if (this._toolsLayout.getFocusedTool() == panel)
	    		{
	    			this._toolsLayout._onToolBlurred(panel);
	    			
	    			var centerTab = this._toolsLayout._panelHierarchy['cl'].getActiveTab() || this._toolsLayout._panelHierarchy['cr'].getActiveTab();
	    			if (centerTab)
	    			{
	    				this._toolsLayout.focusTool(centerTab);
	    			}
	    			else
	    			{
                        // FIXME no link with such class
	    				// Ext.create("Ametys.message.Message", { type: Ametys.message.Message.SELECTION_CHANGED, targets: [] });
	    			}
	    		}
	    	}
		},
		
		/**
		 * @private
		 * Listener when the tabpanel is expanded
		 */
		_onExpand: function()
		{
			this.getLogger().debug("Expanding '" + this._location + "' zone")

			this.getTabBar().getComponent("unpin").show();
			this.getTabBar().getComponent("pin").hide()

	    	this.saveState();
		},

		onAdd: function(tool, index)
		{
			var me = this;
			
			this.callParent(arguments);

			function toolFocused()
			{
				me._toolsLayout._onToolFocused(tool);
			}
			function toolBlurred()
			{
				me._toolsLayout._onToolBlurred(tool);
			}
			function toolActivated()
			{
				me._toolsLayout._onToolActivated(tool);
			}
			function toolDeactivated()
			{
				me._toolsLayout._onToolDeactivated(tool);
			}
			
			// Enhance the tab button
			var tabElBtn = this.getTabBar().items.get(index);
			
			tabElBtn.addListener('click', toolFocused);

			tabElBtn.addListener('activate', toolActivated);
			tabElBtn.addListener('deactivate', toolDeactivated);

			tabElBtn.on('render', Ext.bind(this._onTabEltRender, this, [tabElBtn, tool.getId()], false));
			tabElBtn.addCls(this.typePrefixCls + tool.getType());
			
			// Add tool's listeners on the ui panel
			tool.addListener('render', function() {
				tool.mon(tool.el, 'click', toolFocused);
				tool.mon(tool.el, 'keydown', toolFocused);	
			});
			
			// Ensure the owner tabpanel is visible
			this.show();
			
			Ext.defer(this._updateCollapsePlaceHolder, 1, this);
		},
		
		/**
		 * @private
		 * Listener when the tab element button is rendered
		 * @param {Ext.Button} tabElBtn The button
		 * @param {String} toolId The id of the tool associated to the button
		 */
		_onTabEltRender: function(tabElBtn, toolId)
		{
			// Add a wrapping div element for css purposes
			tabElBtn.getEl().first().wrap({});
			
			// Activate drag'n'drop
			var dragDropProxy = Ext.create("Ametys.ui.tool.layout.ZonedTabsToolsLayout.ZonedTabsDD", tabElBtn, 'toolstabsDDGroup', { isTarget: false, toolId: toolId, toolsLayout: this._toolsLayout });
			tabElBtn.on('destroy', Ext.bind(dragDropProxy.destroy, dragDropProxy, [], false));
		},
		
		onRemove: function(panel, autoDestroy)
		{
			this.callParent(arguments);

			Ext.defer(this._updateCollapsePlaceHolder, 1, this);
			
			if (this.items.length == 0)
			{
				if (this.floatedFromCollapse)
				{
					this.expand();
				}
				this.hide();
			}
		},
		
		/**
		 * @private
		 * Determines if the panel is a main area of the Ametys.ui.tool.layout.ZonedTabsToolsLayout
		 * A main area does not pin and slide for example.
		 * @returns {Boolean} True if this is a main area
		 */
		_isMainArea: function()
		{
			return this._location == 'cl' || this._location == 'cr'; 
		},
		
		/**
		 * @private
		 * Listener when the element detects a context menu is required
         * @param {Ext.event.Event} e The {@link Ext.event.Event} encapsulating the DOM event.
         * @param {HTMLElement} t The target of the event.
         * @param {Object} eOpts The object registered with the listener.
         **/
		_onContextMenu: function(e, t, eOpts)
		{
			e.stopEvent();

			var cmp = Ext.getCmp(t.id);
			var index = this.getTabBar().items.indexOf(cmp);
			var panel = this.items.get(index);
			if (panel == null)
			{
				return;
			}

			var toolId = panel.getId();
			
			this._contextualMenuTargetToolId = toolId;
		    this._contextMenu.showAt(e.getXY());
		},
		
		/**
		 * @private
		 * Will close the currently activated tab
		 */
		_closeCurrentTab: function ()
		{
            var toolPanel = Ext.getCmp(this._contextualMenuTargetToolId);
            toolPanel.close();
            
			this._contextualMenuTargetToolId = null;
		},
		
		/**
		 * @private
		 * Will close all tabs BUT the currently activated tab
		 */
		_closeOthersTabs: function ()
		{
			var _contextualMenuTargetToolId = this._contextualMenuTargetToolId;
			
			this.items.each( function(item) {
				if (_contextualMenuTargetToolId != item.getId())
				{
                    item.close();
				}
			});
			this._contextualMenuTargetToolId = null;
		},
		
		/**
		 * @private
		 * Will close all the tabs
		 */
		_closeAllTabs: function ()
		{
			this.items.each( function(item) {
                item.close();
			});
			this._contextualMenuTargetToolId = null;
		},
		
		/**
		 * @private
		 * Listener when mouse leave the float zone
		 */
		onMouseLeaveFloated: function()
		{
			// nothing, to avoid the default parent behavior that is to slideOut floating zone if mouse leave
		},

		/**
		 * @private
		 * Makes a collapse panel to slide in
		 */
		floatCollapsedPanel: function()
		{
			// Avoid recusive calls, since _slidInZone will call floatCollapsedPanel
			if (this.isSliding)
			{
				return;
			}
			
			this.callParent(arguments);
			
			this._toolsLayout._slideInZone(this);
			
	    	var panel = this.getActiveTab()
	    	if (panel != null)
	    	{
	    		this._toolsLayout._onToolActivated(panel);
                this._toolsLayout.focusTool(panel);
	    	}
		}
	}
);
