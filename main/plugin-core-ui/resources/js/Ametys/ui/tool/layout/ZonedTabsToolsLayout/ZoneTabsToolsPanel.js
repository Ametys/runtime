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
        /**
         * @private
         * @property {String} _floatingCls The css class added when floating
         */        
        _floatingCls: "x-panel-tool-layoutzone-floating",
        
        /**
         * @private
         * @property {String} _collapsibleDirectionCls The css class prefix added to collapsible panel upon its direction
         */
        _collapsibleDirectionCls: "x-panel-tool-layoutzone-collapsible",
        
        /**
         * @property {Boolean} _floating=false Is the zone expanded over the others zones? 
         */
        _floating: false,
        
        /**
         * @property {Object} _bodySizeBeforeCollapse The last known value of the size of the body when it was expanded. null if not collapsed.
         * @property {Number} _bodySizeBeforeCollapse.width The width
         * @property {Number} _bodySizeBeforeCollapse.height The height
         */
		
		hidden: true,
        
        hideCollapseTool: true,
		
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
		            text: "{{i18n PLUGINS_CORE_UI_MSG_TOOLS_CLOSE_TAB}}", 
		            handler: this._closeCurrentTab,
		            iconCls: "a-tools-menu-close",
		            scope: this
		        },
		        '-',
		        {
		            text: "{{i18n PLUGINS_CORE_UI_MSG_TOOLS_CLOSE_OTHERS}}", 
		            handler: this._closeOthersTabs,
                    iconCls: "a-tools-menu-close-others",
		            scope: this
		        },
		        {
		            text: "{{i18n PLUGINS_CORE_UI_MSG_TOOLS_CLOSE_ALL}}", 
		            handler: this._closeAllTabs,
                    iconCls: "a-tools-menu-close-all",
		            scope: this
		        }]
		    });

			
			this._location = config.location;
			this._toolsLayout = config.toolsLayout;
            
			config.stateful = true;
			config.stateId = this.self.getName() + "$" + this._location;
            
            config.cls = Ext.Array.from(config.cls);
            config.cls.push(this._collapsibleDirectionCls + "-" + (config.collapseDirection || 'top'));
            
            config.tabBarHeaderPosition = config.collapsible ? 1 : 0;
            config.tabBar = config.tabBar || {};
            config.tabBar.flex = 1;
            config.header = { 
                titlePosition: config.collapsible ? 2 : 1
            };
            config.title = {
                text: '',
                hidden: true,
                flex: 0
            };
            
            if (config.collapsible)
            {
                config.tools = Ext.Array.from(config.tools);
                config.tools.push({
                    xtype: 'button',
                    cls: 'a-btn-lighter',
                    glyph: '',
                    handler: Ext.bind(this._expandOrCollapse, this)
                });
            }
            
            config.stateEvents
            
			this.callParent(arguments);

            this.on('beforeshow', this._onShow, this);
            
            this.on('collapse', this._onUnfloat, this);
			this.on('unfloat', this._onUnfloat, this);
		},
        
        /**
         * @private
         * Function to expand or collapse the panel depending on its current #collapse state AND the #_floating state
         */
        _expandOrCollapse: function()
        {
            if (this._floating)
            {
                // Let's do a collapse/expand in a row with no animation
                this.collapse(null, false);
                this.expand(false);
                // We also have to mannualy do what's slideOut do                
                this.removeCls([this._floatingCls, this._floatingCls + "-" + this._location]);
            }
            else if (this.collapsed)
            {
                this.expand();
            }
            else
            {
                this.collapse();
            }
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
                if (brother = this.nextSibling("zonetabpanel") || this.previousSibling("zonetabpanel"))
                {
                    this.flex = brother.flex || 1;
                }
            }
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

        afterRender: function() 
        {
            this.callParent(arguments);

	    	// Context menu
	    	this.getTabBar().mon(this.getTabBar().getEl(), {
	            scope: this,
	            contextmenu: this._onContextMenu,
	            delegate: '.x-tab'
	        });

            // Click the bar will focus the current active tool
            this.getTabBar().mon(this.getTabBar().getEl(), 'click', this._onClickOnTabBar, this);
	    },	    

		/**
		 * @private
		 * Listener when the tabpanel is unfloated to focus another zone
		 */
		_onUnfloat: function()
		{
			this.getLogger().debug("Unfloating '" + this._location + "' zone")

			var panel = this.getActiveTab()
	    	if (panel != null)
	    	{
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
                        this._toolsLayout.focusTool(null);
	    			}
	    		}
	    	}
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
         * For lateral zone, would display it over the others zones
         */
        _slideIn: function()
        {
            if (!this._isMainArea() && this.collapsed)
            {
                this.addCls([this._floatingCls, this._floatingCls + "-" + this._location]);
                this._floating = true;
                this.expand();
            }
        },
        
        /**
         * @private
         * For lateral zone, would display it over the others zones
         */
        _slideOut: function()
        {
            if (!this._isMainArea() && this._floating)
            {
                this.collapse();
                // floating will be set to false in placeholder collapse listener because collapse is asynchronous
                this.removeCls([this._floatingCls, this._floatingCls + "-" + this._location]);
            }
        }
	}
);
