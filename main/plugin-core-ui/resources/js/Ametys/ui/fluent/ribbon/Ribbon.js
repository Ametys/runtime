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
 * The ribbon is an impl of the Microsoft Fluent Ribbon use in Microsoft Products such as Microsoft Office 2007.
 * This class is the entry point of this package.
 * 
 * 		var ribbon = Ext.create("Ametys.ui.fluent.ribbon.Ribbon", {
 *                      quickToolbar: [ ...small buttons configuration... ],
 *                  
 *                      title: 'My document',
 *                      applicationTitle : 'My application',
 *                  
 *                      searchMenu: {  // to activate a Tell me what you want to do feature
 *                          // emptyText: "Specify to replace the default value",
 *                          searchURL: "http://www.google.com?q={query}", // to search in doc
 *                          items: [
 *                              {
 *                                  text: 'A button',
 *                                  searchTerms: ['a', 'button']
 *                              }
 *                          ]
 *                      },
 *                   
 *                      help: {
 *                          handler: function() { alert('help'); },
 *                          tooltip: "A little bit of help?"
 *                      },
 *
 *
 *                      // The main menu button
 *                      mainButton:
 *                      {
 *                          xtype: 'button',
 *                          icon: 'resources/img/ametys.gif',
 *                          text: 'Ametys',
 *                          tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'A long descriptive text', footertext: 'See help for details', inribbon: true},
 *                          items:
 *                          [ ...todo... ]
 *                      },
 *                   
 *                      // The tabs
 *                      items:
 *                          [
 *                              // Tab Home
 *                              { 
 *                                  title: 'Home', 
 *                                  items: [
 *                                      // Group Test
 *                                      {
 *                                          title: 'Test',
 *                                          largeItems: [...],
 *                                          items: [
 *                                                      {
 *                                                          xtype: 'ametys.ribbon-button',
 *                                                          scale: 'large',
 *                                                          enableToggle: true,
 *                                                          pressed: true,
 *                                                          handler: function() { alert('sitemap') },
 *                                                  
 *                                                          icon: 'resources/img/sitemap_32.png',
 *                                                          text: 'Sitemap',
 *                                                          tooltip: {title: 'Sitemap tool', image: 'resources/img/ametys.gif', text: 'Click here to open or close the sitemap tool that allow to manage pages', helpId: 'help.sitemap.id, inribbon: true}
 *                                                      },
 *                                                      {
 *                                                           columns: 2,
 *                                                           align: 'middle',
 *                                                           items: [
 *                                                                   { xtype: 'ametys.ribbon-button', scale: 'small', enableToggle: true, pressed: true, handler: function() { alert('a') }, icon: 'resources/img/editpaste_16.gif', text: 'Files', tooltip: {...} },
 *                                                                   { xtype: 'ametys.ribbon-button', scale: 'small', enableToggle: true, pressed: true, handler: function() { alert('b') }, icon: 'resources/img/editpaste_16.gif', text: 'Search', tooltip: {...} },
 *                                                                   { xtype: 'ametys.ribbon-button', colspan: 2, scale: 'small', enableToggle: true, pressed: true, handler: function() { alert('c') }, icon: 'resources/img/editpaste_16.gif', text: 'Profiles', tooltip: {...} }
 *                                                           ]
 *                                                       }
 *                                          ],
 *                                          smallItems: [...]
 *                                      }
 *                                  ],
 *                              },
 *                              // Contextual tab
 *                              { 
 *                                  title: 'Images', 
 *                                  items: [...], 
 *                                  contextualTab: 2, 
 *                                  contextualLabel: 'Image Tools', 
 *                                  contextualGroup: 'image.tools'
 *                              }
 *                          ],
 *
 *                      // Display the user logged in with a profile card in a menu
 *                      user: {
 *                          fullName: 'Raphaël Franchet',
 *                          login: 'raphael',
 *                          email: 'raphael.franchet@anyware-services.com',
 *                             smallPhoto: "http://www.gravatar.com/avatar/2f7124c10b1d2775303cd40bc6244419?s=16&d=blank",
 *                          mediumPhoto: "http://www.gravatar.com/avatar/2f7124c10b1d2775303cd40bc6244419?s=32&d=mm",
 *                          largePhoto: "http://www.gravatar.com/avatar/2f7124c10b1d2775303cd40bc6244419?s=48&d=mm",
 *                          extraLargePhoto: "http://www.gravatar.com/avatar/2f7124c10b1d2775303cd40bc6244419?s=64&d=mm",                        
 *                          menu: { items: [ "-", {text: 'Disconnect' } ]}
 *                      },
 *                      
 *                      // Activate the notification system
 *                      notification: {
 *                          tooltip: 'A descriptive text',
 *                          handler: function() { ... }
 *                      }
 * 		});
 * 
 * For technical reason this class is just a wrapper for the ribbon Panel. So see the {Ametys.yu.fluent.ribbon.Ribbon} configuration for parameters and usefull methods
 * Use the #getPanel method to access the wrapped object
 */
Ext.define(
	"Ametys.ui.fluent.ribbon.Ribbon",
	{
		extend: "Ext.panel.Panel",
		alias: 'widget.ametys.ribbon',
		
        /**
         * @property {String} defaultApplicationTitle The #cfg-applicationTitle default value.
         * @readonly
         * @private
         */
        defaultApplicationTitle: "Ametys",
        
        /**
         * @property {String} contextualTabGroupContainerCls The CSS classname to set on the container of contextual tabs groups
         * @readonly
         * @private
         */
        contextualTabGroupContainerCls: 'a-fluent-header-tabgroups',

        /**
         * @cfg {String} ui=ribbon @inheritdoc
         */
        ui: 'ribbon',
        
        /**
         * @cfg {Object/Object[]} message Display one or more information messages under the ribbon
         * @cfg {String} message.title The message title
         * @cfg {String} message.type=info The type of message 'info', 'question', 'warning' or 'error'.
         * @cfg {String} message.text The message text
         * @cfg {boolean} message.closeable=true Can the message be dimissed
         */
        
        /**
         * @private
         * @property {String} messageCloseText The message on the close button of the information message #cfg-message
         */
        messageCloseText: "{{i18n plugin.core-ui:PLUGINS_CORE_UI_WORKSPACE_AMETYS_RIBBON_MESSAGE_CLOSE_DESCRIPTION}}",
		
        /**
         * @cfg {Object/Ext.toolbar.Toolbar} quickToolbar The quicktoolbar (configuration or object) on the top left of the ribbon. 
         */
        /**
         * @cfg {Object/Ametys.ui.fluent.ribbon.Ribbon.SearchMenu} searchMenu A search field configuration or object that will open results as a menu.
         */
        /**
         * @cfg {Object/Ext.panel.Tool} help A help button on the top right.
         */
        
        /**
         * @property {Ametys.ui.fluent.ribbon.Ribbon.MessageContainer} _messageContainer The container for information messages (under the ribbon)
         * @private
         */
        
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
         * @property {Boolean} _actionPerfomed Has an action been performed since the last contextual tab was hidden.
         */
        _actionPerfomed: false,
        
		/**
		 * Creates new ribbon. This class is technically just a placeholder for a real Ametys.ui.fluent.ribbon.Panel
		 * @param {Object} config The config object passed to Ametys.ui.fluent.ribbon.Panel
		 */
		constructor: function(config)
		{
            config = config || {};
            
            /**
             * @cfg {Object[]/Ext.panel.Tool[]} tools tools Doesn't apply to ribbon element.
             * @private
             */
            config.tools = [];
            
            this._messageContainer = Ext.create("Ametys.ui.fluent.ribbon.Ribbon.MessageContainer", {});
            config.dockedItems = Ext.Array.from(config.dockedItems);
            config.dockedItems.push(this._messageContainer);
            
            if (config.message)
            {
                config.message = Ext.Array.from(config.message);
                
                for (var i = 0; i < config.message.length; i++)
                {
                    var message = config.message[i];

                    this.addMessage(message);
                }
            }
            
            
            // The position of "title" between tools in the top header line
            var titlePosition = 0;
            
            // Handle the quickbar
            if  (config.quickToolbar)
            {
                if (!config.quickToolbar.isComponent)
                {
                    config.quickToolbar.xtype = config.quickToolbar.xtype || 'toolbar';
                    config.quickToolbar.ui = 'ribbon-header-toolbar';
                    config.quickToolbar.defaults = { ui: 'ribbon-header-button' };
                }
                
                config.tools.push(config.quickToolbar);
                titlePosition++;
            }
            
            config.tools.push({ 
                xtype: 'component', 
                itemId: 'separator',
                hidden: true
            }); 
            titlePosition++;

            // Handle the title
            config.title = {
                xtype: 'ametys.ribbon-title',
                text: config.title,
                applicationTitle: config.applicationTitle || this.defaultApplicationTitle
            };
            
            // This container will receive contextual panel groups
            // It will automatically show/hide depending on its items
            config.tools.push({ 
                xtype: 'ametys.ribbon-contextualtabgroups', 
                itemId: 'contextualTabGroups',
                flex: 1,                hidden: true
            }); 
            
            /**
             * @cfg {Boolean/Object} header Doesn't apply to ribbon element. 
             * @private
             */
            config.header = {
                titleAlign: 'center',
                titlePosition: titlePosition
            };
            
            // Handle the search menu
            if (config.searchMenu)
            {
                if (!config.searchMenu.isSearchMenu)
                {
                    config.searchMenu.xtype = 'ametys.ribbon-searchmenu';
                }
                config.tools.push(config.searchMenu);
            }
            
            // Handle the help button
            if (config.help)
            {
                if (!config.help.isComponent)
                {
                    config.help = Ext.applyIf(config.help, {
                        type: 'help'
                    });
                }
                
                config.tools.push(config.help);
            }            
        
            // The tabPanel
            
            // move many properties from ribbon config to tabpanel config
            var tabPanelConfig = Ext.moveTo({
                xtype: 'ametys.ribbon-tabpanel',
                
                // The default tab HAS TO be the first one according to Microsoft UI Fluent guide lines. 
                activeTab: 0,
                
                border: false,
                shadow: false,
                
                floating: true,
                autoShow: true,
                
                tabBar: {
                    listeners: {
                        'dblclick': {
                            fn: this.toggleRibbonCollapse,
                            scope: this,
                            element: 'el'
                        }
                    }
                }
            }, config, [ 'defaults', 'items', 'mainButton', 'user', 'notification' ]);
            /**
             * @cfg {Object/Function} defaults This configuration is transmitting to the underlying tab panel. See Ametys.ui.fluent.ribbon.TabPanel#cfg-mainButton 
             */
            /**
             * @cfg {String} defaultType This configuration is transmitting to the underlying tab panel. See Ametys.ui.fluent.ribbon.TabPanel#cfg-mainButton 
             */
            /**
             * @cfg {Object/Object[]} items This configuration is transmitting to the underlying tab panel. See Ametys.ui.fluent.ribbon.TabPanel#cfg-mainButton 
             */
            /**
             * @cfg {Object} mainButton This configuration is transmitting to the underlying tab panel. See Ametys.ui.fluent.ribbon.TabPanel#cfg-mainButton 
             */
            /**
             * @cfg {Object} user This configuration is transmitting to the underlying tab panel. See Ametys.ui.fluent.ribbon.TabPanel#cfg-user 
             */
            /**
             * @cfg {Boolean} notification This configuration is transmitting to the underlying tab panel. See Ametys.ui.fluent.ribbon.TabPanel#cfg-notification 
             */
            
            config.items = [tabPanelConfig];
            
            this.callParent([config]);
            
            this.on('resize', this._onRibbonResize, this);
            this.on('afterlayout', this._setRibbonHeight, this);
		},
        
        /**
         * Add an information message
         * @param {Object} message See #cfg-message.
         * @return {String} The id of the newly created component containing the added message
         */
        addMessage: function(message)
        {
            var msgId = Ext.id();
            
            var items = [
                {
                    xtype: 'component',
                    ui: 'ribbon-message-title',
                    html: message.title
                },
                {
                    xtype: 'component',
                    flex: 1,
                    ui: 'ribbon-message-text',
                    html: message.text
                }
            ];
            
            if (message.closeable !== false)
            {
                
                items.push({
                    xtype: 'tool',
                    type: 'close',
                    tooltip: this.messageCloseText,
                    callback: Ext.bind(this.closeMessage, this, [msgId], false)
                });
            }
            
            var messageCmp = this._messageContainer.add({
                id: msgId,
                cls: 'a-message-type-' + (message.type || 'info'),
                items: items
            });
            
            return msgId;
        },
        
        /**
         * Remove a previously added message. 
         * @param {String} msgId The component id of the message component returned by {@link #addMessage}
         */
        closeMessage: function(msgId)
        {
            this._messageContainer.remove(msgId);
        },
        
        /**
         * Get the container for contextual tabs groups
         * @return {Ametys.ui.fluent.ribbon.Ribbon.ContextualTabGroupContainer} The container
         */
        getContextualTabGroupsContainer: function()
        {
            return this.getHeader().getComponent("contextualTabGroups");
        },
        
        getState: function()
        {
            return {
                _ribbonCollapsed: this._ribbonCollapsed
            };
        },   
	
		/**
		 * Get the underlying panel
		 * @return {Ametys.ui.fluent.ribbon.Panel} The wrapper panel
		 */
		getPanel: function()
		{
			return this.floatingItems.items[0];
		},
        
        /**
         * @private
         * We want to protect all right click, and potentially re-collapse the floating panel
         */
        afterRender: function() 
        {
            this.callParent(arguments);
            
            Ext.getDoc().on("click", this._onAnyMouseDown, this);
            Ext.getDoc().on("contextmenu", this._onAnyMouseDown, this);
            
            if (this._ribbonCollapsed)
            {
                this.collapseRibbon();
            }
        },
        
        /**
         * Listener for preventing right click on the ribbon, and potentially re-collapse the floating panel
         * @param {Ext.event.Event} e The {@link Ext.event.Event} encapsulating the DOM event.
         * @param {HTMLElement} t The target of the event.
         * @param {Object} eOpts The options object passed to Ext.util.Observable.addListener
         */
        _onAnyMouseDown: function(e, t, eOpts) 
        {
            // We want to cancel right-click on the ribbon (but the ribbon has 2 dom root because the tabpanel is floating)
            if (Ext.fly(t).findParent("#" + this.getId()) || Ext.fly(t).findParent("#" + this.getPanel().getId()))
            {
                if (e.button != 0 && !/^(input|textarea)$/i.test(t.tagName))
                {
                    e.preventDefault();
                }
            }
            
            // We want to recollapse the ribbon when in collapse mode (but not if the click was in the tab panel
            else
            {
                if (this._ribbonCollapsed && this._ribbonFloating) 
                {
                    this._hideCollapsed();
                }
                
                if (this._actionPerfomed)
                {
                    this.getPanel()._lastContextualTabHidden = null;
                }
                
                this._actionPerfomed = true;
            }
        },
        
        /**
         * Listener when the ribbon get resized to apply new width to the floating subpanel
         * @param {Ext.Component} placeHolder The place holder
         * @param {Number} width The new width that was set.
         * @param {Number} height The new height that was set.
         * @param {Number} oldWidth The previous width.
         * @param {Number} oldHeight The previous height. 
         * @param {Object} eOpts The event options
         * @private
         */
        _onRibbonResize: function(placeHolder, width, height, oldWidth, oldHeight, eOpts)
        {
            if (width != oldWidth)
            {
                this.getPanel().setWidth(width);
                this.getContextualTabGroupsContainer() && this.getContextualTabGroupsContainer()._changeTitlePositionOrWidth();
            }
        },
        
        /**
         * @protected
         * Change the ribbon height to reflect its current state
         */
        _setRibbonHeight: function()
        {
            if (this.rendered)
            {
                var newHeight = this.getHeader().getHeight() 
                            + (this._ribbonCollapsed ? this.getPanel().getHeader().getHeight() : this.getPanel().getHeight()) 
                            + this._messageContainer.getHeight();

                this.setHeight(newHeight);
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
            this._ribbonCollapsed = true;
            this._ribbonFloating = true;
            this.saveState();
            this._hideCollapsed();

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

                this.getPanel().body.setDisplayed(false);
                this.getPanel().setHeight(this.getHeader().getHeight());
                
                // Deactivate the active tab
                this.getPanel().getTabBar().activeTab.el.removeCls('x-tab-active');
                if (this.getPanel().activeTab) 
                {
                    this.getPanel().activeTab.fireEvent('deactivate', this.getPanel().activeTab);
                    this.getPanel().activeTab = null;
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
            this._ribbonCollapsed = false;
            this._showCollapsed(true);
            this._ribbonFloating = false;
            this.saveState();

            return this;
        },

        /**
         * @private 
         * When in collapsed mode, show it
         * @param {Boolean} force To force the collapse
         */
        _showCollapsed: function(force) 
        {
            if ((this._ribbonCollapsed && !this._ribbonFloating) || force) 
            {
                Ext.suspendLayouts();

                this._ribbonFloating = true;
                
                this.getPanel().body.setDisplayed(true);
                this.getPanel().setHeight(null);
                this._setRibbonHeight(); // the panel onResize may not have do the job if the panel was floating

                if (this.getPanel().getTabBar().activeTab.el.isVisible())
                {
                    this.getPanel().setActiveTab(this.getPanel().lastActiveTab);
                    this.getPanel().getTabBar().activeTab.el.addCls('x-tab-active');
                }
                else
                {
                    this.getPanel().setActiveTab(0);
                }
                
                Ext.resumeLayouts(true);
            }
        },
        
        /**
         * Get the notificator if created
         * @return {Ametys.ui.fluent.ribbon.Ribbon.Notificator} The notification button or null
         */
        getNotificator: function()
        {
            return this.getPanel().getNotificator();
        }
	}
);
