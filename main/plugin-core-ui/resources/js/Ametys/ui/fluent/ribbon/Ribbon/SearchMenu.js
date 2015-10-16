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
         * @cfg {String} searchURL When provided, a menu item is added at the end to be able to search an url with the query. The value is a template string. An object with a string property "query" is provided to the template.
         */
        /**
         * @property {Ext.Template} searchURL The template creating using #cfg-searchURL
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
        searchMenuDefaultText: "<i18n:text i18n:key='PLUGINS_CORE_UI_WORKSPACE_AMETYS_RIBBON_SEARCHMENU_PLACEHOLDER' i18n:catalogue='plugin.core-ui'/>",
        /**
         * @property {String} searchMenuHelpItem A template string for the final item menu to open help with that search. An object with a string property "text" is provided to the template.
         * @readonly
         * @private
         */
        searchMenuHelpItem: "<i18n:text i18n:key='PLUGINS_CORE_UI_WORKSPACE_AMETYS_RIBBON_SEARCHMENU_SEARCHITEM_LABEL' i18n:catalogue='plugin.core-ui'>See help for \"{text}\"</i18n:text>",
        
        /**
         * @readonly
         * @private
         * @property {Number} searchMenuMaxResults The max number of results displayed
         */
        searchMenuMaxResults: 10,
        
        /**
         * @property {Ext.Template} searchMenuHelpItem The template creating using #cfg-searchMenuHelpItem
         * @private
         */
        /**
         * @private
         * @property {Ext.menu.Menu} _menu The menu instance for search results.
         */
        
        /**
         * @readonly
         * @private
         * @property {Number} searchAfterTime Time in millisecond to wait after last change to launch a search
         */
        searchAfterTime: 300,
        
        constructor: function(config) 
        {
            var me = this;
            
            config = config || {};
            
            config.cls = Ext.Array.from(config.cls);
            config.cls.push(this.searchMenuCls);
            
            config.emptyText = config.emptyText || this.searchMenuDefaultText;
            
            config.items = Ext.Array.from(config.items);
            
            this.searchMenuHelpItem = Ext.create("Ext.Template", this.searchMenuHelpItem);

            if (config.searchURL)
            {
                this.searchURL = Ext.create("Ext.Template", config.searchURL);
                config.items.push("-");
                config.items.push({
                    handler: this._openHelp,
                    scope: this
                });
                delete config.searchURL;
            }
            
            this._menu = Ext.create("Ext.menu.Menu", {
                defaultAlign: "tl-bl",
                items: config.items
            })
            delete config.items;
            
            this.callParent([config]);
            
            this.on('click', this._reopenLastSearchIfAvailable, this, { element: 'inputEl' });
            this.on('change', Ext.Function.createBuffered(this._searchNow, this.searchAfterTime, this));
            this.on('specialkey', this._onSpecialKey);
        },
        
        /**
         * @private
         * Open the help
         */
        _openHelp: function()
        {
            var url = this.searchURL.apply({ query: Ext.Object.toQueryString({q : this.getValue()}).substring(2) });
            window.open(url, "_blank");
        },
        
        /**
         * @private
         * Event when a special key is pressed
         * @param {Ext.form.field.Text} input
         * @param {Ext.event.Event} event
         */
        _onSpecialKey: function(input, event)
        {
            if (event.getKey() == event.DOWN) 
            {
                this._reopenLastSearchIfAvailable();
                this._menu.focus();
            }
        },
        
        /**
         * @private
         * Launch a search now
         */
        _searchNow: function()
        {
            var me = this;
            var menuItemsToAvoid = 0;
            
            if (!this.getValue())
            {
                this._menu.hide();
                return;
            }
            
            if (this.getValue().toLowerCase().indexOf('show companions') == 0) 
            {
                this._loadCompanions();
            }
            
            if (this.searchURL)
            {
                // Update the search metnu item
                var value = this.getValue();
                if (value.length > 22)
                {
                    value = value.substring(0, 20) + "…";
                }
                this._menu.items.last().setText(this.searchMenuHelpItem.apply({ text: value }));
                
                menuItemsToAvoid = 2;
            }
            
            // Browse all items to compute score and hide them all
            var visibleItems = [];
            for (var i = 0; i < this._menu.items.getCount() - menuItemsToAvoid; i++)
            {
                var menuItem = this._menu.items.get(i); 
                
                var score = this._computeScore(this.getValue(), menuItem.text, menuItem.keywords);
                menuItem.hide();
                if (score > 0)
                {
                    visibleItems.push({ score: score, item: menuItem });
                }
            }
            
            // Now order
            Ext.Array.sort(visibleItems, function(a, b) { 
                if (a.score < b.score) 
                {
                    return -1;
                }
                else if (a.score > b.score) 
                {
                   return 1;
                }
                else if (a.item.text < b.item.text)
                {
                    return 1;
                }
                else if (a.item.text > b.item.text)
                {
                    return -1;
                }
                else
                { 
                   return 0; 
                }
            });
            
            // Finally order and set visible the better N items only
            Ext.Array.each(Ext.Array.erase(visibleItems, 0, Math.max(0, visibleItems.length - this.searchMenuMaxResults)), function (item) {
                me._menu.insert(0, item.item);
                item.item.show();
            });
            
            if (this.searchURL)
            {
                this._menu.items.get(this._menu.items.getCount() - menuItemsToAvoid).setVisible(visibleItems.length > 0);
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
                    
                    function load(item)
                    {
                        item.disable();
                        clippy.load(item.text, function (agent) { 
                            var animationItems = [];
                            var animations = agent.animations();
                            for (var i = 0; i < animations.length; i++)
                            {
                                animationItems.push({
                                    text: animations[i],
                                    handler: function() {
                                        this.ownerCt.ownerCmp.ownerCt.ownerCmp.agent.play(this.text);
                                    }
                                });
                            }

                            var menu = item.ownerCt; 
                            var newItem = menu.insert(menu.items.indexOf(item), {
                                text: item.text,
                                menu: {
                                    items: [
                                        { text: 'show', handler: function() { this.ownerCt.ownerCmp.agent.show(); } },
                                        { text: 'animate', handler: function() { this.ownerCt.ownerCmp.agent.animate(); }, menu: { items: animationItems } },
                                        { text: 'say something', handler: function() { this.ownerCt.ownerCmp.agent.speak("Something... yes, it is very interesting."); } },
                                        { text: 'hide', handler: function() { this.ownerCt.ownerCmp.agent.hide(); } }
                                    ]
                                } 
                            });
                            menu.remove(item);
                            
                            newItem.agent = agent;
                            
                            agent.show() 
                        });
                    }
                    
                    me._menu.insert(0, {
                        text: "Companions",
                        menu: { 
                            items: [
                                { text: 'Bonzi', handler: load },
                                { text: 'Clippy', handler: load },
                                { text: 'F1', handler: load },
                                { text: 'Genie', handler: load },
                                { text: 'Genius', handler: load },
                                { text: 'Links', handler: load },
                                { text: 'Merlin', handler: load },
                                { text: 'Peedy', handler: load },
                                { text: 'Rocky', handler: load },
                                { text: 'Rover', handler: load }
                            ]
                        }
                    });
                });
            }
        },
        
        /**
         * @private
         * Reshow last menu
         */
        _reopenLastSearchIfAvailable: function()
        {
            if (this.getValue())
            {
                this._menu.showBy(this);
            }
        },
        
        /**
         * @private
         * Compute a score based on searchTerm is near from title and keywords
         * @param {String} searchTerm The term to search. Will also search with split words.
         * @param {String} title The title (high scoring words)
         * @param {String/String[]} keywords (low scoring words)
         * @return {Number} The score. 0 means not found, and a positive number means found.
         */
        _computeScore: function(searchTerm, title, keywords)
        {
            var me = this;
            
            if (!searchTerm)
            {
                return -1;
            }
            
            searchTerm = Ext.String.deemphasize((searchTerm || "").trim().replace(/\s+/g, ' ')).toLowerCase().split(" ");
            title = Ext.String.deemphasize((title || "").trim().replace(/\s+/g, ' ')).toLowerCase().split(" ");
            keywords = Ext.String.deemphasize(Ext.Array.from(keywords).join(" ").trim().replace(/\s+/g, ' ')).toLowerCase().split(" ");
            
            var score = 0;
            
            Ext.Array.each(searchTerm, function(st) {
                Ext.Array.each(title, function(t) {
                    score += me._coincidence(st, t) * 10;
                });
                Ext.Array.each(keywords, function(k) {
                    score += me._coincidence(st, k);
                });
            });
            
            return score;
        },
        
        /**
         * @private
         * Compute if the searchTerm is part of a word
         * @param {String} searchTerm The term to seach. If the term is an array of term, will return the medium score.
         * @param {String} word The word to search in
         * @return {Number} A score between 0 and 1.
         */
        _coincidence: function(searchTerm, word)
        {
            var score;
            var index = word.indexOf(searchTerm);
            if (index == -1)
            {
                score = 0;
            }
            else
            {
                score = (searchTerm.length / word.length) * ((word.length - index) / word.length);
            }
            return score;
        }
    }
);
    