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
 * The title's header for the ribbon panel.
 * This implementation always add the application title to the given title
 * @inheritdoc
 * @private
 */
Ext.define(
    "Ametys.ui.fluent.ribbon.Ribbon.SearchMenu",
    {
        extend: "Ext.form.field.Text",
        alias: 'widget.ametys.ribbon-searchmenu',
        
        /** 
         * @cfg {String} [emptyText="Tell me what you want to do..."] @inheritdoc. 
         */

        /** 
         * @cfg {Object/Object[]} items The menu items to search into
         * @cfg {String/String[]} items.<element>.keywords Terms that will be part of the search (additionnaly to 'text' configuration)
         */
        
        /**
         * @cfg {Boolean} allowSearch When true, a menu item is added at the end to be able to search in the HelpTool.
         */
        /**
         * @property {Boolean} allowSearch True if search is allowed. See #cfg-allowSearch
         * @private
         */
        
        /**
         * @private
         * @cfg {String} ui Not overidable
         */
        ui: 'ribbon-header-text',
         
        /**
         * @property {String} searchMenuCls The CSS classname to set on the search menu field
         * @readonly
         * @private
         */
        searchMenuCls: 'a-fluent-searchmenu',
        
        /**
         * @property {String} searchMenuDefaultText If #cfg-searchMenu is not a string, this text will be used as default value)
         * @readonly
         * @private
         */
        searchMenuDefaultText: "{{i18n plugin.core-ui:PLUGINS_CORE_UI_WORKSPACE_AMETYS_RIBBON_SEARCHMENU_PLACEHOLDER}}",
        /**
         * @property {String} searchMenuHelpItem A template string for the final item menu to open help with that search. An object with a string property "text" is provided to the template.
         * @readonly
         * @private
         */
        searchMenuHelpItem: "{{i18n plugin.core-ui:PLUGINS_CORE_UI_WORKSPACE_AMETYS_RIBBON_SEARCHMENU_SEARCHITEM_LABEL}}",
        
        /**
         * @readonly
         * @private
         * @property {Number} searchMenuMaxResults The max number of results displayed
         */
        searchMenuMaxResults: 15,
        
        /**
         * @readonly
         * @private
         * @property {Number} searchMenuMaxDisabledResults The max number of results which are disabled controllers displayed
         */
        searchMenuMaxDisabledResults: 5,
        
        /**
         * @private
         * @property {Ext.menu.Menu} _menu The menu instance for search results.
         */
        
        /**
         * @readonly
         * @private
         * @property {Number} searchAfterTime Time in millisecond to wait after last change to launch a search
         */
        searchAfterTime: 500,
        
        /**
         * @property {Number} codeStep The current step for "the code"
         */
        codeStep: 0, 
        
        /**
         * @private
         * @property {Array} _lastResults The results of the last search by lunr.
         */
        
        constructor: function(config) 
        {
            var me = this;
            
            config = config || {};
            
            config.cls = Ext.Array.from(config.cls);
            config.cls.push(this.searchMenuCls);
            
            config.emptyText = config.emptyText || this.searchMenuDefaultText;
            
            config.items = Ext.Array.from(config.items);
            config.items.push({
            	id: "enabledControllersGroup",
            	text: "<b>{{i18n PLUGINS_CORE_UI_WORKSPACE_AMETYS_RIBBON_SEARCHMENU_ITEMS_AVAILABLE}}</b>",
            	hideOnClick: false,
            	focusable: false
            }, {
            	xtype: 'menuseparator',
            	id: 'groupsSeparator'
            }, {
            	id: "disabledControllersGroup",
            	text: "<b>{{i18n PLUGINS_CORE_UI_WORKSPACE_AMETYS_RIBBON_SEARCHMENU_ITEMS_UNAVAILABLE}}</b>",
            	hideOnClick: false,
            	focusable: false
            });
            
            this.searchMenuHelpItem = Ext.create("Ext.Template", this.searchMenuHelpItem);

            if (config.allowSearch)
            {
                this.allowSearch = true;
                config.items.push("-");
                config.items.push({
                	id: 'openHelpButton',
                    handler: this._openHelp,
                    scope: this
                });
                delete config.allowSearch;
            }
            
            this._menu = Ext.create("Ext.menu.Menu", {
                defaultAlign: "tr-br",
                ui: 'ribbon-menu',
                listeners: {
                    'close': this._onMenuClose,
                    scope: this
                },
                items: config.items
            })
            delete config.items;
            
            this.callParent([config]);
            
            this.on('click', this._reopenLastSearchIfAvailable, this, { element: 'inputEl' });
            this.on('change', Ext.Function.createBuffered(this._searchNow, this.searchAfterTime, this));
            this.on('specialkey', this._onSpecialKey);
            this.on('render', this._setMinWidthMenu, this);
            this.on('blur', this._onBlur, this);
        },
        
        /**
         * @private
         * Set the minimum width of the menu to the value of the search menu width
         */
        _setMinWidthMenu: function()
        {
        	this._menu.setMinWidth(this.getWidth());
        },
        
        /**
         * @private
         * On blur we should remove value if menu is closed
         */
        _onBlur: function() 
        {
            if (!this._menu.isVisible())
            {
                this.reset();
            }
        },
        
        /**
         * @private
         * When the menu is closed
         */
        _onMenuClose: function()
        {
            if (!this.hasFocus)
            {
                this.reset();
            }
        },
        
        /**
         * @private
         * Open the help
         */
        _openHelp: function()
        {
            Ametys.tool.ToolsManager.openTool("uitool-help", {searchQuery: this.getValue()});
        },
        
        /**
         * @private
         * Event when a special key is pressed
         * @param {Ext.form.field.Text} input The search input
         * @param {Ext.event.Event} event The key event
         */
        _onSpecialKey: function(input, event)
        {
            if (event.getKey() == event.UP)
            {
                if (this.codeStep == 0 || this.codeStep == 1)
                {
                    this.codeStep++;
                }
                else
                {
                    this.codeStep = 0;
                }
            }
            else if (event.getKey() == event.LEFT)
            {
                if (this.codeStep == 4 || this.codeStep == 6)
                {
                    this.codeStep++;
                }
                else
                {
                    this.codeStep = 0;
                }
            }
            else if (event.getKey() == event.RIGHT)
            {
                if (this.codeStep == 5 || this.codeStep == 7)
                {
                    this.codeStep++;
                }
                else
                {
                    this.codeStep = 0;
                }
            }
            else if (event.getKey() == event.DOWN) 
            {
                if (this.codeStep == 2 || this.codeStep == 3)
                {
                    this.codeStep++;
                }
                else
                {
                    this.codeStep = 0;
                    this._reopenLastSearchIfAvailable();
                    this._menu.focus();
                }
            }
            else
            {
                this.codeStep = 0;
            }
        },
        
        /**
         * @private
         * Launch a search now
         */
        _searchNow: function()
        {
            var me = this;
            
            if (!this.getValue())
            {
                this._menu.hide();
                return;
            }
            
            if (this.getValue().toLowerCase() == 'ba' && this.codeStep == 8) 
            {
                this.codeStep = 0;
                this._loadCompanions();
                this.reset();
                return;
            }
            
            if (this.allowSearch)
            {
                // Update the search menu items
                var value = this.getValue();
                if (value.length > 22)
                {
                    value = value.substring(0, 20) + "…";
                }
                this._menu.items.getByKey("openHelpButton").setText(this.searchMenuHelpItem.apply({ text: value }));
            }
            
            // Search with lunr.js the relevant controllers ids
            var foundControllers = lunr.controllersIndex.search(this.getValue());
            this._lastResults = foundControllers;
            
            this._displayResults(foundControllers);
        },
        
        /**
         * @private
         * Display the results of the query
         * @param {Array} controllers The controllers found by lunr
         */
        _displayResults: function(controllers)
        {
        	var me = this;
        	
        	// Browse all items for hiding them
            this._menu.items.each(function(item) {
            	if (item.getId() != "openHelpButton" && Ext.ClassManager.getName(item) != "Ext.menu.Separator")
            	{
            		item.hide();
            	}
            });
        	
        	// Retrieve the controllers and put the top 'searchMenumaxResults' results in arrays
            var enableItems = [],
            	disableItems = [];
            Ext.each(controllers, function(controller, index) {
            	if (enableItems.length + disableItems.length >= this.searchMenuMaxResults) {return false;}
            	
            	var menuItem = this._menu.items.getByKey( controller.ref );
            	if (!menuItem.isDisabled())
            	{
            		enableItems.push(menuItem);
            	}
            	else if (menuItem.isDisabled() && disableItems.length < this.searchMenuMaxDisabledResults)
            	{
	            	disableItems.push(menuItem);
            	}
            }, this);
            
            // Finally reverse the lists, put the items at the top and show them
            Ext.Array.forEach(disableItems.reverse(), function (item) {
            	me._menu.insert(0, item);
                item.show();
            });
            var disableGroup = me._menu.items.getByKey('disabledControllersGroup');
            me._menu.insert(0, disableGroup);
            disableGroup.setVisible(disableItems.length > 0);
            
            var separator = me._menu.items.getByKey('groupsSeparator');
            me._menu.insert(0, separator);
            separator.setVisible(disableItems.length > 0 && enableItems.length > 0);
            
            Ext.Array.forEach(enableItems.reverse(), function (item) {
            	me._menu.insert(0, item);
                item.show();
            });
            var enableGroup = me._menu.items.getByKey('enabledControllersGroup');
            me._menu.insert(0, enableGroup);
            enableGroup.setVisible(enableItems.length > 0);
            
            // Show the open help button separator (the item just before the last one)
            if (this.allowSearch)
            {
                this._menu.items.get(this._menu.items.getCount() - 2).setVisible(controllers.length > 0);
            }
            
            this._menu.showBy(this);
        },
        
        /**
         * @private
         * Load companions
         */
        _loadCompanions: function(msg)
        {
            var me = this;
            if (!me._agents)
            {
                me._agents = {};
                me._currentAgent = null;
                me._companions = ['Bonzi', 'Clippy', 'F1', 'Genie', 'Genius', 'Links', 'Merlin', 'Peedy', 'Rocky', 'Rover'];
                
                Ext.override(Ametys.ui.fluent.ribbon.Ribbon.Notificator, {
                    notify: function(config)
                    {
                        this.callParent(arguments);
                        
                        if (me._currentAgent)
                        {
                            var text = config.isModel ? 
                                (config.get('title')) : 
                                (config.title);
                            me._currentAgent.speak(text);
                        }
                    }
                });
                
                Ext.override(Ametys.ui.fluent.ribbon.Ribbon.Notificator.Toast, {
                    show: function()
                    {
                        if (!me._currentAgent)
                        {
                            this.callParent(arguments);
                        }                        
                        else
                        {
                            Ext.destroy(this);
                        }
                    }
                });
            }
            
            function loadAgentRnd()
            {
                if (me._currentAgent != null)
                {
                    me._currentAgent.hide();
                    me._currentAgent = null;
                    return;
                }
                
                var index = Math.floor(Math.random() * me._companions.length);
                
                
                if (me._agents[me._companions[index]])
                {
                    me._agents[me._companions[index]].show();
                    me._currentAgent =  me._agents[me._companions[index]];
                }
                else
                {
                    clippy.load(me._companions[index], function (agent) {
                        me._agents[me._companions[index]] = agent;
                        
                        agent.show() 
                        me._currentAgent = agent;
                    });
                }
            }
            
            if (!this._companionsInitialization)
            {
                /**
                 * @private
                 * @property {Boolean} _backLoadInitialized Was the back load already initialize
                 */
                this._companionsInitialization = 'loading';
                
                Ametys.loadStyle("//clippy.js.s3.amazonaws.com/clippy.css");
                if (!window.jQuery)
                {
                    Ametys.loadScript("//code.jquery.com/jquery-1.11.3.min.js");
                }
                Ametys.loadScript("//clippy.js.s3.amazonaws.com/clippy.js", function() {
                    me._companionsInitialization = 'loaded';
                    clippy.BASE_PATH = '//clippy.js.s3.amazonaws.com/Agents/';
                    
                    loadAgentRnd();
                });
            }
            else if (this._companionsInitialization == 'loaded')
            {
                loadAgentRnd();
            }
             
        },
        
        /**
         * @private
         * Reshow last menu
         */
        _reopenLastSearchIfAvailable: function()
        {
            if (this.getValue() && this._lastResults)
            {
            	this._displayResults(this._lastResults);
            }
        }
    }
);
    