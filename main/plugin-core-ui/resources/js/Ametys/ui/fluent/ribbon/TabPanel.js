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
 * This class is the inside of the ribbon. You have to create it through the wrapper Ametys.ui.fluent.ribbon.Ribbon.
 * 
 * Items are {@link Ametys.ui.fluent.ribbon.Panel} and can be classic tabs or contextual tabs.
 * 
 */
Ext.define(
    "Ametys.ui.fluent.ribbon.TabPanel",
    {
        extend: "Ext.tab.Panel",
        alias: 'widget.ametys.ribbon-tabpanel',

        /**
         * @cfg {String} ui=ribbon-tabpanel @inheritdoc
         */
        ui: 'ribbon-tabpanel',
        
        /**
         * @property {String} contextualTabCls The CSS classname to set on the button of contextual tabs
         * @readonly
         * @private
         */
        contextualTabCls: 'a-fluent-tab-contextual',
        
        /**
         * @cfg {String} defaultType Doesn't apply to ribbon element.
         * @private
         */
        defaultType: 'ametys.ribbon-panel',
        
        /**
         * @private
         * @property {Object} userCfg A configuration for user menu button
         */
        userCfg: {},
        
        /**
         * @cfg {Object} user Specify this to display the name of the connected user. This will create a button with user photo and name, and clicking on it will open a menu with a profile card. This object is also a configuration for a {@link Ext.button.Button} (you can for example add a menu configuration to add items under the profile card):
         * @cfg {String} user.fullName The full name of the connected user
         * @cfg {String} user.login The identifier of the user
         * @cfg {String} [user.email] The user email address
         * @cfg {String} [user.smallPhoto] The absolute path to the user photo in 16x16.
         * @cfg {String} [user.mediumPhoto] The absolute path to the user photo in 32x32.
         * @cfg {String} [user.largePhoto] The absolute path to the user photo in 48x48.
         * @cfg {String} [user.extraLargePhoto] The absolute path to the user photo in 64x64.
         */
        
        /**
         * @cfg {Object} mainButton The configuration or object that will be added to #cfg-tools of the header at the left of the tabs.
         */
            
        /**
         * @cfg {Object/Ametys.ui.fluent.ribbon.Ribbon.Notificator} notification Display the notification indicator by providing a notification configuration
         */
        
        /**
         * @private
         * @property {Ametys.ui.fluent.ribbon.Panel} _lastContextualTabHidden Last contextual panel selected. Null when hidden for too long.
         */
        _lastContextualTabHidden: null,

        constructor: function (config)
        {
            /**
             * @private
             * @property {Object} _contextualGroups Map<String, Ametys.ui.fluent.ribbon.Ribbon.ContextualTabGroup> 
             * The map of contextual group id and the associated group label (in the top part of the ribbon)
             */
            this._contextualGroups = {};

            /**
             * @private
             * @property {Object} _scaleGrid The map of the groups organized by size.
             *  The map is a Map&lt;Number index, Object o&gt; 
             *      index is the index of the tab. 
             *          o is an object with 2 properties: 
             *              current a Number representing the index of the currently selected matrix
             *              matrix an Array[Object o2] where o2 is an object with the following properties:
             *                  resize an Array[String s]. Its size is the same as the tabs one, its values can be "small", "medium" or "large". The array represents the possible combination of "small" "medium" and "large" groups for a tab
             *                  width is a Number representing the size in pixel of the tab.
             *              groupsWidth an Array[Object o3] where o3 is for each group an object containing the different Ametys.ui.fluent.ribbon.Group.SIZE_*  associated with the corresponding widths
             */
            this._scaleGrid = {};

            
            config = config || {};
            
            /**
             * @cfg {Object[]/Ext.panel.Tool[]} tools tools Doesn't apply to ribbon element.
             * @private
             */
            config.tools = [];
            
            // We want that tabs goes into header line : with an invisible title
            var titlePosition = 1;
            config.title = {
                text: '',
                flex: 0
            };
            
            config.tabBarHeaderPosition = 1;
            config.tabBar = config.tabBar || {};
            config.tabBar.flex = 1;
            config.tabBar.layout = {
                type: 'hbox',
                overflowHandler: 'menu'
            };
            
            // Handle main button
            if (config.mainButton)
            {
                titlePosition = 2;
                config.tabBarHeaderPosition = 2;
                config.mainButton.ui = 'ribbon-tabpanel-mainbutton';
                config.tools.push(config.mainButton);
            }
            
            config.tools.push({
                xtype: 'tbspacer',
                width: 3
            })
            
            // Handle user
            if (config.user)
            {
                config.user = Ext.applyIf(config.user, this.userCfg);
                
                config.user.text = config.user.fullName;
                config.user.ui = 'ribbon-tabpanel-linkbutton';
                config.user.menuAlign = 'tr-br?';
                config.user.menu = config.user.menu || {};
                config.user.menu.ui = 'ribbon-menu';
                //config.user.menu.ui = config.user.menu.ui || 'ribbon-menu';
                config.user.menu.items = config.user.menu.items || [];
                config.user.menu.items = Ext.Array.insert(config.user.menu.items, 0, [{
                   xtype: 'component',
                   isMenuItem: true, // to be responsible to draw the whole line => this allow to be on the left of menu even with vertical separator activated.
                   html: new Ext.XTemplate(
                        "<div class='a-fluent-user-card'>",
                           "<tpl if='extraLargePhoto'><div class='photo'><img src='{extraLargePhoto}'/></div></tpl>",
                           "<div class='main'>", 
                               "<div class='name-wrapper'>", 
                                   "<div class='name'>{fullName}</div>",
                                   "<div class='login'>{login}</div>",
                               "</div>",
                               "<tpl if='email'><div class='email' title=\"{email}\">{email}</div></tpl>", 
                           "</div>",
                       "</div>").apply(config.user)
                }]);
                
                config.tools.push(new Ext.Button(Ext.apply({
                        cls: 'a-fluent-user',
                        icon: config.user.smallPhoto,
                        iconCls: 'foo' // ensure icon is set (even if there is no smallPhoto as theme will put a background image
                    },
                    config.user
                )));
            }
            
            // Handle notifications
            if (config.notification)
            {
                config.tools.push(Ext.applyIf({
                    xtype: 'ametys.ribbon-notificator'
                }, config.notification));
            }

            // let's move title to somewhere it won't be annoying
            config.header = {
                titlePosition: titlePosition
            };
            
            this.callParent([config]);
            
            this.on('mousewheel', this._onTabWheel, this, { element: 'el' });
            
            // Fix because default position is quite strange
            this.setPosition(0, 0);
        },
        
        /**
         * Get the notificator if created
         * @return {Ametys.ui.fluent.ribbon.Ribbon.Notificator} The notification button or null
         */
        getNotificator: function()
        {
            return this.getHeader().items.last();
        },
        
        /**
         * @private
         * Do initialization after rendering
         */
        afterRender: function() 
        {
            this.callParent(arguments);

            this.items.each(this._panelAddedAfterRender, this);
        },
        
        setActiveTab: function(card)
        {
            var previous;
        
            // Check for a config object
            if (!Ext.isObject(card) || card.isComponent) 
            {
                card = this.getComponent(card);
            }
            previous = this.getActiveTab();
            if (card) 
            {
                Ext.suspendLayouts();
                card = this.callParent(arguments);
                
                // We want to add contextual CSS classes to roots element
                if (previous && previous.contextualTab)
                {
                    this.removeCls(this.contextualTabCls + "-" + previous.contextualTab);
                    this.floatParent.removeCls(this.contextualTabCls + "-" + previous.contextualTab);
                }
                if (card.contextualTab)
                {
                    this.addCls(this.contextualTabCls + "-" + card.contextualTab);
                    this.floatParent.addCls(this.contextualTabCls + "-" + card.contextualTab);
                }
                
                Ext.resumeLayouts(true);
                
                // We want to show the ribbon if it is collapsed
                if (previous !== card)
                {
                    if (this.floatParent._ribbonCollapsed) 
                    {
                        this.floatParent._showCollapsed();
                    }
                    
                    this.updateLayout();
                }
            }
                
            return card;
        },
        
        afterComponentLayout: function(width, height, oldWidth, oldHeight) 
        {
            this.callParent(arguments);
            this._checkSize();
        },
        
        onResize: function(width, height, oldWidth, oldHeight) 
        {
            this.callParent(arguments);
            
            if (height != oldHeight)
            {
                this.floatParent._setRibbonHeight();
            }
            
            if (width != oldWidth)
            {
                this._checkSize();
            }
        },
        
        /**
         * Listener on adding tab to check for contextual tabs (thoses with the contextualTab property).
         * See class doc to see the properties
         * @param {Ametys.ui.fluent.ribbon.Panel} panel The panel added
         * @param {Number} index The new tab index
         */
        _panelAddedAfterRender: function(panel, index)
        {
            if (panel.contextualTab != null)
            {
                var tabElBtn = this.getTabBar().items.get(index);
                
                // Generates a group if no group is specified (the tab will be alone in this group)
                if (panel.contextualGroup == null)
                {
                    panel.contextualGroup = Ext.id();
                }

                // Hide the tab button
                tabElBtn.hide();
                // Color the tab button
                tabElBtn.addCls([this.contextualTabCls, this.contextualTabCls + "-" + panel.contextualTab]);
                
                // Creates the group if the tab is the first one of the group
                if (this._contextualGroups[panel.contextualGroup] == null)
                {
                    panel._contextualTabGroup = this.floatParent.getContextualTabGroupsContainer().addGroup(panel.contextualLabel, panel.contextualTab);
                    
                    this._contextualGroups[panel.contextualGroup] = panel._contextualTabGroup;
                }
                // Add the tab to the existing group
                else
                {
                    panel._contextualTabGroup = this._contextualGroups[panel.contextualGroup];
                }
                
                // Add the reference to the tab in the group label object
                panel._contextualTabGroup.associateContextualPanel(panel);
                
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
                    if (!tabEl.hasCls(this.contextualTabCls))
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
                }
            }
            
            if (forceSelection !== false && (panel._activatedOnce == null || panel._wasActiveOnHide == true || forceSelection === true || this._lastContextualTabHidden == panel))
            {
                panel._activatedOnce = true;
                panel._wasActiveOnHide = false;
                
                this.setActiveTab(panel);
                if (this.floatParent._ribbonCollapsed)
                {
                    this.floatParent._hideCollapsed();
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
    
                    panel._wasActiveOnHide = this.activeTab == panel; 
                    if (panel._wasActiveOnHide)
                    {
                        var index = Ext.Array.indexOf(panel._contextualTabGroup._tabs, panel);
    
                        var panelToTests = [];
                        for (var i = index - 1; i >= 0; i--)
                        {
                            var tab = panel._contextualTabGroup._tabs[i];
                            panelToTests.push(tab);
                        }
                        for (var i = index + 1; i < panel._contextualTabGroup._tabs.length; i++)
                        {
                            var tab = panel._contextualTabGroup._tabs[i];
                            panelToTests.push(tab);
                        }
                        for (var i = this.items.length - 1; i >= 0; i--)
                        {
                            var tab = this.items.get(i);
                            if (tab._contextualTabGroup != null)
                            {
                                for (var j = 0; j < tab._contextualTabGroup._tabs.length; j++)
                                {
                                    var tab = tab._contextualTabGroup._tabs[j];
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
            if (this.floatParent._ribbonCollapsed) 
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
         * Enlarge or reduce groups of the active tab to best fit in the ribbon (depending on the screen width)
         * @private
         */
        _checkSize: function()
        {
            // we need to first close menu to ensure that any iconized group will go back in the ribbon prior to any scale computing
            Ext.menu.MenuMgr.hideAll();
            
            if (this.getActiveTab())
            {
                var currentActiveTabWidth = this.getActiveTab().getWidth();
                
                var index = this.items.indexOf(this.getActiveTab());
                if (this._scaleGrid[index] == null)
                {
                    this._buildScalesGrid(index);
                }
                var grid = this._scaleGrid[index];

                if (!grid.matrix[grid.current].width)
                {
                    grid.matrix[grid.current].width = this.getActiveTab().items.last().getRegion().right - this.getActiveTab().getRegion().left;
                }

                this._fillGridGroupsSize(grid);

                if (this.getActiveTab().items.getCount() > 0 && grid.matrix[grid.current].width > currentActiveTabWidth)
                {
                    // does not fit, try to reduce
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
                    // Can we grow ?
                    if (grid.current > 0)
                    {
                        if (!grid.matrix[grid.current - 1].width || currentActiveTabWidth > grid.matrix[grid.current - 1].width)
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
            var currentAnswer = grid.current + 1;
            
            for (var i = currentAnswer; i < grid.matrix.length - 1; i++)
            {
                if (grid.matrix[i].width)
                {
                    if (currentActiveTabWidth >= grid.matrix[i].width)
                    {
                        return i;
                    }
                    currentAnswer = i + 1;   
                }
            }
            
            return currentAnswer;
        },
        
        /**
         * @private
         * Test the given grid for the largest scale possible while screen is enlarged
         * @param {Object} grid One grid of #_scaleGrid
         * @param {Number} currentActiveTabWidth The width available
         */
        _findNextIndexToTestInDecreasingGrid: function(grid, currentActiveTabWidth)
        {
            var currentAnswer = grid.current - 1;
            
            for (var i = currentAnswer; i >= 0; i--)
            {
                if (grid.matrix[i].width)
                {
                    if (currentActiveTabWidth <= grid.matrix[i].width)
                    {
                        return i + 1;
                    }
                    currentAnswer = i;
                }
            }
            
            return currentAnswer;
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
            function fill(first, second, avoidFullFirst)
            {
                for (var cursor = avoidFullFirst ? 1 : 0; cursor <= priority.length; cursor++)
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
            scaleGrid[index].groupsWidth = {};
            for (var i = 0; i < priority.length; i++)
            {
                scaleGrid[index].groupsWidth[i] = {};
            }

            fill(Ametys.ui.fluent.ribbon.Group.SIZE_LARGE, Ametys.ui.fluent.ribbon.Group.SIZE_MEDIUM, false);
            fill(Ametys.ui.fluent.ribbon.Group.SIZE_MEDIUM, Ametys.ui.fluent.ribbon.Group.SIZE_SMALL, true);
            fill(Ametys.ui.fluent.ribbon.Group.SIZE_SMALL, Ametys.ui.fluent.ribbon.Group.SIZE_ICON, true);
            
            // change unexisting combo & remove duplicates
            for (var i = scaleGrid[index].matrix.length - 1; i >= 0 ; i--)
            {
                var grid = scaleGrid[index].matrix[i];
                for (var j = 0; j < grid.resize.length; j++)
                {
                    if (!tab.items.get(j).supportScale(grid.resize[j]))
                    {
                        grid.resize[j] = Ametys.ui.fluent.ribbon.Group.SIZE_MEDIUM;
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
         * @private
         * Fill the group size for the current tab grid, and remove any anomaly in known size group layouts
         * @param {Object} tabGrid The grid for the current tab
         */
        _fillGridGroupsSize: function(tabGrid)
        {
            // "sizeOrder" is used to know the relative order of each size compare to another
            var sizeOrder = [
                Ametys.ui.fluent.ribbon.Group.SIZE_LARGE,
                Ametys.ui.fluent.ribbon.Group.SIZE_MEDIUM,
                Ametys.ui.fluent.ribbon.Group.SIZE_SMALL,
                Ametys.ui.fluent.ribbon.Group.SIZE_ICON
            ];

            for (var groupIndex = 0; groupIndex < this.getActiveTab().items.length; groupIndex++)
            {
                // for each group
                
                var currentGroup = this.getActiveTab().items.get(groupIndex);
                var currentGroupSize = tabGrid.matrix[tabGrid.current].resize[groupIndex];

                if (!tabGrid.groupsWidth[groupIndex][currentGroupSize])
                {
                    // New size
                    
                    tabGrid.groupsWidth[groupIndex][currentGroupSize] = currentGroup.getWidth();

                    var currentSizeOrder = sizeOrder.indexOf(currentGroupSize);

                    for (var compareToSize = 0; compareToSize < sizeOrder.length; compareToSize++)
                    {
                        if (currentSizeOrder > compareToSize && tabGrid.groupsWidth[groupIndex][sizeOrder[compareToSize]] < tabGrid.groupsWidth[groupIndex][sizeOrder[currentSizeOrder]])
                        {
                            // remove current group size, because the larger one is smaller
                            this._changeGroupSize(tabGrid, groupIndex, sizeOrder[currentSizeOrder], sizeOrder[compareToSize]);
                        }
                        if (currentSizeOrder < compareToSize && tabGrid.groupsWidth[groupIndex][sizeOrder[compareToSize]] > tabGrid.groupsWidth[groupIndex][sizeOrder[currentSizeOrder]])
                        {
                            // remove compareTo group size, because it is larger than the current one
                            this._changeGroupSize(tabGrid, groupIndex, sizeOrder[compareToSize], sizeOrder[currentSizeOrder]);
                        }
                    }
                }
            }
        },

        /**
         * @private
         * Replace all occurrences of the size of a group by another size, and remove duplicate entries from the tab matrix
         * @param {Object} tabGrid The tab grid
         * @param {Number} groupIndex The index of the group to edit
         * @param {String} oldSize The old size name
         * @param {String} newSize The new size name
         */
        _changeGroupSize: function(tabGrid, groupIndex, oldSize, newSize)
        {
            for (var index = tabGrid.matrix.length - 1; index >= 0 ; index--)
            {
                if (tabGrid.matrix[index].resize[groupIndex] == oldSize)
                {
                    tabGrid.matrix[index].resize[groupIndex] = newSize;
                    delete tabGrid.matrix[index].width;

                    var foundMatch = false;
                    for (var compareIndex = index - 1; compareIndex >= 0 && !foundMatch; compareIndex--)
                    {
                        var areTheSame = tabGrid.matrix[index].resize.length == tabGrid.matrix[compareIndex].resize.length;
                        for (var resizeIndex = 0; areTheSame && resizeIndex < tabGrid.matrix[index].resize.length; resizeIndex++)
                        {
                            areTheSame = tabGrid.matrix[compareIndex].resize[resizeIndex] == tabGrid.matrix[index].resize[resizeIndex];
                        }

                        if (areTheSame)
                        {
                            if (tabGrid.current == index)
                            {
                                tabGrid.current = tabGrid.current == 0 ? 0 : tabGrid.current - 1;
                            }

                            Ext.Array.removeAt(tabGrid.matrix, index);
                        }
                    }
                }
            }
        }
    }
);
