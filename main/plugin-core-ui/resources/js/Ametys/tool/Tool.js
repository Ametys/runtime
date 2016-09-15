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
 * This class is the abstract class for all tools.
 * A tool is the main part of the ui and takes place in the Ametys.ui.tool.ToolsLayout. 
 * 
 * A tool is always created by its associated factory, called automatically by the Ametys.tool.ToolsManager#openTool.
 * 
 * To create a tool, you first have to choose and configure a {@link Ametys.tool.ToolFactory factory} or create your own for advanced purposes.
 * Then creates your own Tool class by inheriting this one and define at least the following method: #createPanel.
 * 
 * 		Ext.define("My.Tool", {
 * 			extend: "Ametys.tool.Tool",
 * 
 * 			createPanel: function() {
 * 				return Ext.create("Ext.panel.Panel", { html: "My first tool" });
 * 			},
 * 
 * 			getMBSelectionInteraction: function() {
 * 				return Ametys.tool.Tool.MB_TYPE_NOSELECTION;
 * 			}
 * 		});
 * 
 * When the tool is created it only receive some factories information, and after beeing rendered it will receive the parameters through #setParams and will be activated.
 * 
 * Implement the #getMBSelectionInteraction to choose the type of tool. 
 * 
 * For easy server request, user #serverCall method.
 * 
 * Note that many Tools methods are protected to avoid problems. For example, sendCurrentSelection will automatically be ignored if the tool do not have focus, and most other methods will return null if the Toos #isNotDestroyed
 */
Ext.define("Ametys.tool.Tool", 
	{
        mixins: { servercaller: 'Ametys.data.ServerCaller' },
        
		statics:
		{
			/**
			 * @readonly
			 * @property {String} TYPE_DEFAULT Stands for a tool type {@link #getType}. This is the default tool type.
			 */
			TYPE_DEFAULT: "default",
			// Declares other constants from other files. The associated values should be one of the handles by Ametys.ui.tool.ToolPanel
            /**
             * @readonly
             * @property {String} TYPE_DEVELOPER Stands for a tool type {@link #getType}. This is the type for 'developer' tools.
             */
            TYPE_DEVELOPER: "developer",
            
            /**
             * @readonly
             * @property {Object} TYPES Association of type key and informations
             * @property {String} TYPES.key The key should be declared as a TYPE_* constant
             * @property {Object} TYPES.value The information for the key type
             * @property {Number} TYPES.value.ui The UI to use for this type. Should be a constant of Ametys.ui.tool.ToolPanel TOOLTYPE_*
             * @property {Number} TYPES.value.priority A value to order tools in the same zone. Low value on the left. Use a value between 1 and 99.
             */
            TYPES: {
                "default": { ui: Ametys.ui.tool.ToolPanel.TOOLTYPE_40, priority: 0 },
                "developer": { ui: Ametys.ui.tool.ToolPanel.TOOLTYPE_20, priority: 99 }
            },
			
			/**
			 * @protected
			 * @readonly
			 * @property {Number} MB_TYPE_ACTIVE Active tool: See {@link #getMBSelectionInteraction}.
			 */
			MB_TYPE_ACTIVE: 0,
			/**
			 * @protected
			 * @readonly
			 * @property {Number} MB_TYPE_LISTENING Listening tool: See {@link #getMBSelectionInteraction}.
			 */
			MB_TYPE_LISTENING: 1,
			/**
			 * @protected
			 * @readonly
			 * @property {Number} MB_TYPE_NOSELECTION Non-selecting tool: See {@link #getMBSelectionInteraction}.
			 */
			MB_TYPE_NOSELECTION: 2,
			
			/**
			 * @readonly
			 * @property {Number} OOD_UPTODATE Constant for #isOutOfDate. The tool is not out of date.
			 */
			OOD_UPTODATE: 0,
			/**
			 * @readonly
			 * @property {Number} OOD_MINOROUTOFDATE Constant for #isOutOfDate. The tool is out of date because of a minor modification.
			 */
			OOD_MINOROUTOFDATE: 1,
			/**
			 * @readonly
			 * @property {Number} OOD_MAJOROUTOFDATE Constant for #isOutOfDate. The tool is out of date because of a major modification.
			 */
			OOD_MAJOROUTOFDATE: 2
		},
		
        /**
         * @property {Ext.Component} _contentPanel The content panel of the tool
         * @private
         */
        
		/**
		 * @auto
		 * @cfg {Ametys.tool.ToolFactory} factory (required) The factory that created the tool. Cannot be null.
		 */
		/**
		 * @property {Ametys.tool.ToolFactory} _factory See {@link #cfg-factory}
		 * @private
		 */

		/**
		 * @auto
		 * @cfg {String} id (required) The unique identifier for the tool. Cannot be null.
		 */
		/**
		 * @property {String} _id See {@link #cfg-id}
		 * @private
		 */
		
		/**
		 * @auto
		 * @cfg {String} pluginName (required) The name of the plugin that declared the tool. Cannot be null.
		 */
		/**
		 * @property {String} _pluginName See {@link #cfg-pluginName}
		 * @private
		 */
		
		/**
		 * @cfg {String} title (required) The title of the tool. Should be updated when necessary
		 */
		/**
		 * @property {String} _title See #cfg-title
		 * @private
		 */
		/**
		 * @cfg {String} description The description of the tool. Should be updated when necessary.
		 */
		/**
		 * @property {String} _description See #cfg-description
		 * @private
		 */
		/**
		 * @cfg {String} help The help identifier.
		 */
		/**
		 * @property {String} _toolHelpId See #cfg-help
		 * @private
		 */
		
		/**
		 * @cfg {String} icon-glyph The CSS class for glyph to use as the icon. This is an alternative to the set of icons: #cfg-icon-small, #cfg-icon-medium, #cfg-icon-large
		 */
		/**
		 * @property {String} _iconGlyph See #cfg-icon-glyph
		 * @private
		 */
		/**
		 * @cfg {String} icon-decorator The CSS class to use as decorator above the main icon.
		 */
		/**
		 * @property {String} _iconDecorator See #cfg-icon-decorator
		 * @private
		 */
		/**
		 * @cfg {String} icon-small (require) The path to the icon of the tool in size 16x16 pixels. Used for panel (and tooltip if no bigger image is available).
		 */
		/**
		 * @property {String} _iconSmall See #cfg-icon-small
		 * @private
		 */
		/**
		 * @cfg {String} icon-medium The path to the icon of the tool in size 32x32 pixels. Used for tooltip if no bigger image is available.
		 */
		/**
		 * @property {String} _iconMedium See #cfg-icon-medium
		 * @private
		 */
		/**
		 * @cfg {String} icon-large The path to the icon of the tool in size 48x48 pixels. Used for button's tooltip.
		 */
		/**
		 * @property {String} _iconLarge See #cfg-icon-large
		 * @private
		 */

		/**
		 * @cfg {String} default-location='' The default location of the tool. See Ametys.ui.tool.ToolsLayout to know more about locations.
		 */
		/**
		 * @property {String} _defaultLocation See {@link #cfg-default-location}
		 * @private
		 */
		
		/**
		 * @property {String} _dirty=false True if the tool is now dirty, false otherwise.
		 * @private
		 */
		_dirty: false,
		
		/**
		 * @property {Boolean} _destroyed=false The tool state. A destroyed tool should not be used anymore.
         * @private
		 */
		_destroyed: false,
		
		/**
		 * @property {Object} _params The parameters provided the last time the tool was set #setParams
		 * @private
		 */
		_params: {},
		
		/**
		 * @property {Ext.container.Container} _wrapper The main panel created by #createPanel
		 * @private
		 */
		/**
		 * @property {Ext.panel.Panel} _oodPanel The "out of date" yellow panel that do appear when the tool needs to be refreshed
		 * @private
		 */
		/**
		 * @property {Number} _outOfDate Is the tool currently out of date? Can be #OOD_UPTODATE, #OOD_MINOROUTOFDATE or #OOD_MAJOROUTOFDATE.
		 * @private
		 */
		
		/**
		 * @property {Ext.LoadMask} _refreshMask The mask when the tool is refreshing
		 * @private
		 */

		/**
		 * @property {Boolean} _focusedOnce True if the tool has been focused once at least.
		 * @protected
		 */
		
		/**
		 * Creates the tool. Do not call this constructor. It has to be call by a factory, itself called by the Ametys.tool.ToolsManager#openTool.
		 * @param {Object} config See configuration doc for more information.
		 */
		constructor: function(config)
		{
			this.initConfig(config);
			
			this._id = config.id;
			this._factory = config.factory;
			this._pluginName = config.pluginName;
			this._defaultLocation = config.defaultLocation || config['default-location'] || '';
			
			this._focusedOnce = false;
			
			this._title = config.title;
			this._description = config['description'] || config['default-description'];
			this._toolHelpId = config['help'] || undefined;
			
			this._iconGlyph = config["icon-glyph"];
			this._iconDecorator = config["icon-decorator"];
			
			this._iconSmall = config["icon-small"];
			this._iconMedium = config["icon-medium"];
			this._iconLarge = config["icon-large"];
			
			this._outOfDate = Ametys.tool.Tool.OOD_UPTODATE;
			
			this._protectMethods();
		},

		/**
		 * This method will protect many tools methods from beeing called at bad time
		 * Do not call "sendCurrentSelection" if tool has not the focus, do not call "onFocus" if the tool is destroyed... 
		 * @private
		 */
		_protectMethods: function()
		{
			this.close = Ext.Function.createInterceptor(this.close, this.isNotDestroyed, this);
			this.focus = Ext.Function.createInterceptor(this.focus, this.isNotDestroyed, this);
			this.getContentPanel = Ext.Function.createInterceptor(this.getContentPanel, this.isNotDestroyed, this);
			this.getWrapper = Ext.Function.createInterceptor(this.getWrapper, this.isNotDestroyed, this);
			this.hasFocus = Ext.Function.createInterceptor(this.hasFocus, this.isNotDestroyed, this);
			this.isActivated = Ext.Function.createInterceptor(this.isActivated, this.isNotDestroyed, this);
			this.move = Ext.Function.createInterceptor(this.move, this.isNotDestroyed, this);
			this.onActivate = Ext.Function.createInterceptor(this.onActivate, this.isNotDestroyed, this);
			this.onBlur = Ext.Function.createInterceptor(this.onBlur, this.isNotDestroyed, this);
			this.onClose = Ext.Function.createInterceptor(this.onClose, this.isNotDestroyed, this);
			this.onDeactivate = Ext.Function.createInterceptor(this.onDeactivate, this.isNotDestroyed, this);
			this.onFocus = Ext.Function.createInterceptor(this.onFocus, this.isNotDestroyed, this);
			this.onOpen = Ext.Function.createInterceptor(this.onOpen, this.isNotDestroyed, this);
			this.refresh = Ext.Function.createInterceptor(this.refresh, this.isNotDestroyed, this);
			this.sendCurrentSelection = Ext.Function.createInterceptor(this.sendCurrentSelection, this.hasFocus, this);
			this.serverCall = Ext.Function.createInterceptor(this.serverCall, this.isNotDestroyed, this);
			this.setParams = Ext.Function.createInterceptor(this.setParams, this.isNotDestroyed, this);
			this.showOutOfDate = Ext.Function.createInterceptor(this.showOutOfDate, this.isNotDestroyed, this);
			this.showRefreshing = Ext.Function.createInterceptor(this.showRefreshing, this.isNotDestroyed, this);
			this.showRefreshed = Ext.Function.createInterceptor(this.showRefreshed, this.isNotDestroyed, this);
			this.showUpToDate = Ext.Function.createInterceptor(this.showUpToDate, this.isNotDestroyed, this);
		},

		/**
		 * Get the identifier of the tool provided by its factory during creation process.
		 * @returns {String} The identifier of the tool
		 */
		getId: function()
		{
			return this._id;
		},
		
		/**
		 * Get the factory instance that created the tool
		 * @returns {Ametys.tool.ToolFactory} The factory. Cannot be null.
		 */
		getFactory: function()
		{
			return this._factory;
		},
		
		/**
		 * Get the default location for the tool. See Ametys.ui.tool.ToolsLayout to know more about locations.
		 * @returns {String} The default location. Cannot be null.
		 */
		getDefaultLocation: function()
		{
			return this._defaultLocation;
		},
	
		/**
		 * Get the name of the plugin that defined the tool.
		 * @returns {String} The name of the plugin. Cannot be null.
		 */
		getPluginName: function()
		{
			return this._pluginName;
		},
		
		/**
		 * Get the #cfg-title
		 * @returns {String} The title
		 */
		getTitle: function()
		{
			return this._title || "";
		},
        
        /**
         * Set the #cfg-title
         * @param {String} title The new title
         */
        setTitle: function(title)
        {
            this._title = title;
            if (this.getWrapper()) 
            {
            	this.getWrapper().setTitle(this.getTitle());
            }
            
            var ribbon = Ametys.tool.ToolsManager.getRibbon();
            if (ribbon && this.hasFocus())
            {
                ribbon.setTitle(this.getTitle());
            }
        },
		
		/**
		 * Get the #cfg-description
		 * @returns {String} The description
		 */
		getDescription: function()
		{
			return this._description;
		},
        
        /**
         * Set the #cfg-description
         * @param {String} description The new description
         */
        setDescription: function(description)
        {
            this._description = description;
            if (this.getWrapper()) 
            {
            	this.getWrapper().setDescription(this.getDescription());
            }
        },
        
		/**
         * Get the {@link #cfg-help}
         * @returns {String} The help identifier
         */
        getToolHelpId: function()
        {
            return this._toolHelpId;
        },
        /**
         * Set the {@link #cfg-help}
         * @param {String} toolHelpId The new help identifier
         */
        setToolHelpId: function(toolHelpId)
        {
            this._toolHelpId = toolHelpId;
            if (this.getWrapper()) 
            {
            	this.getWrapper().setHelpId(this.getToolHelpId());
            }
        },        

        /**
		 * Get the icon #cfg-icon-glyph.
		 * @returns {String} The CSS class for glyph icon
		 */
        getGlyphIcon: function()
		{
			return this._iconGlyph;
		},
        /**
         * Set the #cfg-iconGlyph 
         * @param {String} iconGlyph The new glyph icon
         */
		setGlyphIcon: function(iconGlyph)
        {
            this._iconGlyph = iconGlyph;
            if (this.getWrapper()) 
            {
            	this.getWrapper().setGlyphIcon(this.getGlyphIcon());
            }
        },
        
        /**
		 * Get the icon #cfg-icon-decorator.
		 * @returns {String} The CSS class for decorator icon
		 */
        getIconDecorator: function()
		{
			return this._iconDecorator;
		},
        /**
         * Set the #cfg-iconGlyph 
         * @param {String} decorator The new decorator
         */
		setIconDecorator: function(decorator)
        {
            this._iconDecorator = decorator;
            if (this.getWrapper()) 
            {
            	this.getWrapper().setIconDecorator(this.getIconDecorator());
            }
        },
        
		/**
		 * Get the icon #cfg-icon-small.
		 * @returns {String} The icon path (relative to the workspace)
		 */
		getSmallIcon: function()
		{
			return this._iconSmall;
		},
        /**
         * Set the #cfg-iconSmall 
         * @param {String} iconSmall The new small icon
         */
        setSmallIcon: function(iconSmall)
        {
            this._iconSmall = iconSmall;
            if (this.getWrapper()) 
            {
            	this.getWrapper().setSmallIcon(this.getSmallIcon());
            }
        },
		/**
		 * Get the icon #cfg-icon-medium.
		 * @returns {String} The icon path (relative to the workspace)
		 */
		getMediumIcon: function()
		{
			return this._iconMedium;
		},
        /**
         * Set the #cfg-iconMedium 
         * @param {String} iconMedium The new medium icon
         */
        setMediumIcon: function(iconMedium)
        {
            this._iconMedium = iconMedium;
            if (this.getWrapper()) 
            {
            	this.getWrapper().setMediumIcon(this.getMediumIcon());
            }
        },        
		/**
		 * Get the icon #cfg-icon-large.
		 * @returns {String} The icon path (relative to the workspace)
		 */
		getLargeIcon: function()
		{
			return this._iconLarge;
		},
		/**
         * Set the #cfg-iconLarge 
         * @param {String} iconLarge The new large icon
         */
        setLargeIcon: function(iconLarge)
        {
            this._iconLarge = iconLarge;
            if (this.getWrapper()) 
            {
            	this.getWrapper().setLargeIcon(this.getLargeIcon());
            }
        },  
        
        /**
         * Returns true if the tool is dirty
         * @return true if the tool is dirty
         */
        isDirty: function ()
        {
            return this._dirty;
        },
        /**
         * Set the #cfg-dirty
         * @param {Boolean} dirty The new dirty state
         */
        setDirty: function (dirty)
        {
        	if (this._dirty != dirty)
        	{
        		this._dirty = dirty;
                if (this.getWrapper()) 
                {
                	this.getWrapper().setDirtyState(this.isDirty());
                }
                
                Ext.create("Ametys.message.Message", {
    				type: Ametys.message.Message.TOOL_DIRTY_STATE_CHANGED,
    				parameters: {
    					dirty: dirty
    				},
    				targets: {
    					id: Ametys.message.MessageTarget.TOOL,
    					parameters: { tools: [this] }
    				}
    			});
        	}
        },
        
		/**
		 * Get the type of the tool. The type may be use to classify or colorize the tools. Depends on the Ametys.ui.tool.ToolsLayout implementation. Use one of the available constants. Default value is #TYPE_DEFAULT.
		 * @returns {String} The tool's type. Cannot be null.
		 * @template
		 */
		getType: function()
		{
			return Ametys.tool.Tool.TYPE_DEFAULT;
		},
		
		/**
		 * This method is in charge for creating the inside panel of the tool
		 * @returns {Ext.Component} The inside of the tool
		 * @protected
		 * @template
		 */
		createPanel: function()
		{
			throw new Error("This method is not implemented in " + this.self.getName());
		},
		
		/**
		 * Implement this method to choose your kind of tool.
		 * Upon the message bus selection there are three kind of tools:
		 * 
		 * - **Active tools** (write selection on the bus): when activated the tools change the selection. For example a content tool will send a message saying "the current selection is the content XXX". When an active tool is closed, it sends a null selection message. Implement #sendCurrentSelection.
		 * - **Listening tools** (read selection on the bus): the tools never launch any selection message but only listen to the current selection. For example an history tool, will listen and react when the selection change to a content. Consider extending Ametys.tool.SelectionTool for this case.
		 * - **Non-message tools** (have no interaction with the bus): the tools does not wants to launch messages nor read it, BUT this tool will still have to send a null selection on activation to inactivate previous selection. For example, a help tool that display a web page.
		 * 
		 * Use the constants #MB_TYPE_ACTIVE, #MB_TYPE_LISTENING, #MB_TYPE_NOSELECTION 
		 * @returns {Number} The kind of tool upon the message bus. 
		 * @protected
		 * @template
		 */
		getMBSelectionInteraction: function()
		{
			throw new Error("This method is not implemented in " + this.self.getName());
		},

		/**
		 * Get the parameters that was set.
		 * @returns {Object} The parameters set when opening the tools. Depends on the tool implementation, but the final data may be saved for a later opening of the tool and so should be serializable to string AND compatible with setParams.
		 */
		getParams: function()
		{
			return this._params;
		},
		
		/**
		 * Set the parameters.
		 * This method is called after the tool rendering.
		 * It can be call several times if the tool is reused.
		 * @param {Object} params The new parameter. Depends on the tool implementation.
		 * @template
		 */
		setParams: function(params)
		{
			this._params = params;
			
			Ext.create("Ametys.message.Message", {
				type: Ametys.message.Message.TOOL_PARAMS_UPDATED,
				
				targets: {
					id: Ametys.message.MessageTarget.TOOL,
					parameters: { tools: [this] }
				}
			});
		},
		
		/**
		 * This method creates the root panel for the tool. This method calls #createPanel for the main part, and add the outofdate panel
		 * @returns {Ext.panel.Panel} The panel.
		 */
		createWrapper: function()
		{
			var oodPanel = this._createOutOfPanel();
			this._contentPanel = this.createPanel();
			this._contentPanel.region = "center";
            
			this._wrapper = Ext.create("Ametys.ui.tool.ToolPanel", {
				layout: 'fit',
				border: false,
				
				scrollable: false,
                
				uiTool: this.getId(),
                
                title: this.getTitle(),
                description: this.getDescription(),
                glyphIcon: this.getGlyphIcon(),
                iconDecorator: this.getIconDecorator(),
                smallIcon: this.getSmallIcon(),
                mediumIcon: this.getMediumIcon(),
                largeIcon: this.getLargeIcon(),
                helpId: this.getToolHelpId(),
                dirtyState: this.isDirty(),
                
                priority: (Ametys.tool.Tool.TYPES[this.getType() || Ametys.tool.Tool.TYPE_DEFAULT] || { priority: 0 }).priority,
				type: (Ametys.tool.Tool.TYPES[this.getType() || Ametys.tool.Tool.TYPE_DEFAULT] || { ui: Ametys.ui.tool.ToolPanel.TOOLTYPE_0 }).ui,
                
				dockedItems: [oodPanel],
				items : [this._contentPanel],
				
				closable: true,
				listeners: 
				{
					'toolactivate': Ext.bind(this.onActivate, this),
                    'tooldeactivate': Ext.bind(this.onDeactivate, this),
                    'toolfocus': Ext.bind(this.onFocus, this),
                    'toolblur': Ext.bind(this.onBlur, this),
                    'toolopen': Ext.bind(this.onOpen, this),
                    'toolclose': Ext.bind(this.onClose, this),
                    
                    'beforeclose': Ext.bind(this._onBeforeManualClose ,this)
				}
			});

			return this._wrapper;
		},
        
        /**
         * @private
         * Called when the panel is closed by user
         * @param {Ametys.ui.tool.ToolPanel} panel the panel on which user click on manual close
         */
        _onBeforeManualClose: function(panel)
        {
            this.close(true);
            return false;
        },
		
		/**
		 * @protected
		 * Get the content panel created by #createPanel function
         * @return {Ext.Component} The main component of the tool 
		 */
		getContentPanel: function()
		{
			return this._contentPanel;
		},
		
		/**
		 * Get the tool wrapping panel
		 * @return {Ext.container.Container}
		 */
		getWrapper: function()
		{
			return this._wrapper;
		},
		
		/**
		 * Provides the out of date component to display when the tool is out of date.
		 * @return {Ext.panel.Panel} The out of date panel
		 * @private
		 */
		_createOutOfPanel: function()
		{
            this._oodPanel = Ext.create("Ext.Button", {
                dock: 'top',
        
                ui: 'tool-hintmessage',
                textAlign: 'left',
                hidden: true,
                text:"{{i18n PLUGINS_CORE_UI_MSG_TOOLS_OUTOFDATEPANEL_LABEL}}",
                tooltip: {
                    title: "{{i18n PLUGINS_CORE_UI_MSG_TOOLS_OUTOFDATEPANEL_TOOLTIP_TITLE}}",
                    image: Ametys.getPluginResourcesPrefix('core-ui') + "/img/tools/reload_32.png",
                    imageWidth: 32,
                    imageHeight: 32,
                    text: "{{i18n PLUGINS_CORE_UI_MSG_TOOLS_OUTOFDATEPANEL_TOOLTIP_DESCRIPTION}}",
                    inribbon: false
                },
                handler: Ext.bind(this.refresh, this, [true], false)
            });
            
            return this._oodPanel;
		},		
		
		/**
		 * Move the tool to a new location
		 * @param {String} newLocation The new location of the tool. See Ametys.ui.tool.ToolsManager to know more about locations.
		 */
		move: function(newLocation)
		{
			Ametys.tool.ToolsManager.moveTool(this, newLocation);
		},

		/**
		 * Close the tool. Just call Ametys.tool.ToolsManager#removeTool
		 * @param {boolean} [manual=false] True is the close method was call by the contributor. Should be considered as false when called with no arguments
		 */
		close: function(manual)
		{
			Ametys.tool.ToolsManager.removeTool(this);
		},
		
		/**
		 * Graphically set the tool in "out of date" mode
		 * See #showUpToDate and #isOutOfDate
		 * @param {Boolean} major True if it is a major out of date. A major out of date, means the tool display something that has nothing to deal with the current selection for example. 
		 * @protected
 		 */
		showOutOfDate: function(major)
		{
			this.showRefreshed();
			
			if (!(this.isOutOfDate() == Ametys.tool.Tool.OOD_UPTODATE || (this.isOutOfDate() == Ametys.tool.Tool.OOD_MINOROUTOFDATE && !major)))
			{
				return;
			}

			this._outOfDate = major ? Ametys.tool.Tool.OOD_MAJOROUTOFDATE : Ametys.tool.Tool.OOD_MINOROUTOFDATE;
			
			this._oodPanel.show();
			if (major)
			{
                var msg = Ext.String.format("{{i18n PLUGINS_CORE_UI_MSG_TOOLS_OUTOFDATEPANEL_MAJOR}}", "<a href=\"#\" onclick=\"Ametys.tool.ToolsManager.getTool('" + this.getId() + "').refresh(); return false;\"", "</a>"); 
				if (this._contentPanel.rendered)
				{
					this._contentPanel.mask(msg, "ametys-mask-unloading");
				}
				else
				{
					this._contentPanel.on('afterrender', function() {
						if (this._outOfDate == Ametys.tool.Tool.OOD_MAJOROUTOFDATE)
						{
							this._contentPanel.mask(msg, "ametys-mask-unloading");
						}
					}, this, { single: true });
				}

			}
		},
		
		/**
		 * Determines if the tool is out of date
		 * @return {Number} The "out of date" state of the tool. Can be #OOD_UPTODATE, #OOD_MINOROUTOFDATE or #OOD_MAJOROUTOFDATE.
		 */
		isOutOfDate: function()
		{
			return this._outOfDate;
		},
		
		/**
		 * Graphically set the tool NOT in "out of date" mode.
		 * See #isOutOfDate and #showOutOfDate.
		 * @protected
		 */
		showUpToDate: function()
		{
			this.showRefreshed();
			
			if (this.isOutOfDate() == Ametys.tool.Tool.OOD_UPTODATE)
			{
				return;
			}
			
			this._outOfDate = Ametys.tool.Tool.OOD_UPTODATE;
			this._oodPanel.hide();
			
			if (this._contentPanel.rendered)
			{
				this._contentPanel.unmask();
			}
		},
		
		/**
		 * Display that the tool is currently refreshing its state
		 * Stop this by calling #showRefreshed
		 * @param {String} [msg] The message to display. A default message is provided.
		 * @protected
		 */
		showRefreshing: function(msg)
		{
			this.showUpToDate();

			msg = msg || "{{i18n PLUGINS_CORE_UI_MSG_TOOLS_REFRESHING}}";
			
			if (!this._refreshMask)
			{
				this._refreshMask = Ext.create("Ext.LoadMask", {msg: msg, target: this.getWrapper()});
			}
			if (this.getWrapper().isVisible(true))
			{
				this._refreshMask.show();
			}
			else
			{
				this.getWrapper().on('show', function() {
					if (this._refreshMask)
					{
						this._refreshMask.show();
					}
				}, this, { single: true });
			}
		},
		
		/**
		 * This method is called when the tool can refresh. Do not call this by your self.
		 * Your implementation can now refresh the tool (e.g. by doing server requests)
		 * The default implementation just #showRefreshed, but if your are doing an asynchronous request, wait for having the server answer before calling #showRefreshed.
		 * @param {Boolean} [manual=false] Was the refresh call manually by the user (clicking on the out of date panel)? Or automatically called, because the tool is in automatic mode.
		 * @template
		 */
		refresh: function(manual)
		{
			this.showUpToDate();
		},
		
		/**
		 * Display that the tool has refreshed its state
		 * Was launched this by calling #showRefreshing
		 * @protected
		 */
		showRefreshed: function()
		{
			if (this._refreshMask)
			{
				this._refreshMask.hide();
				Ext.destroy(this._refreshMask);
				delete this._refreshMask;
			}
		},
		
		/**
		 * Determines if the tool is refreshing or not
		 * @returns {Boolean} true if the tool is refreshing
		 */
		isRefreshing: function()
		{
			return this._refreshMask != null;
		},
		
		/**
		 * For tools of type #MB_TYPE_ACTIVE, implements this method to send the current message bus selection. Called #onFocus for example to make the ribbon adapted to your tools.
		 * @protected 
		 */
		sendCurrentSelection: function()
		{
			throw new Error("This method is not implemented in " + this.self.getName());
		},
		
        /**
         * @inheritDoc
         * @private 
         * The server role for such components 
         * @return {String} The component role
         */
		getServerRole: function()
        {
            return "org.ametys.core.ui.UIToolsFactoriesManager";
        },
        
        /**
         * @inheritDoc
         * @private 
         * The server id for this component 
         * @return {String} #getFactory . Ametys.tool.ToolFactory#getId
         */
        getServerId: function()
        {
            return this.getFactory().getId();
        },
        
        /**
         * Some default values of #serverCall are modified
         * @param {Object} options The options
         * @param {Boolean/String/Object} [options.waitMessage={ target: this.getWrapper() }] Default value is to display a wait message, and default wait message target if the tool panel.
         * @param {Boolean/String/Object} [options.errorMessage=true] Default value is set to true. 
         */
        beforeServerCall: function(options)
        {
            // Default wait message on the tool location
            if (options.waitMessage == null || options.waitMessage === true)
            {
                options.waitMessage = { };
            }
            else if (Ext.isString(options.waitMessage))
            {
                options.waitMessage = { msg: options.waitMessage };
            }
            if (Ext.isObject(options.waitMessage))
            {
                options.waitMessage = Ext.applyIf(options.waitMessage, { target: this.getWrapper() });
            }
            
            // Default error message
            if (options.errorMessage == null)
            {
                options.errorMessage = true;
            }
        },
        
        /**
         * @method afterServerCall
         * @private
         * Do nothing
         */
		
		/**
		 * Graphically activates the tool by bringing it back to view and setting the focus to it
		 */
		focus: function()
		{
			Ametys.tool.ToolsManager.getToolsLayout().focusTool(this.getWrapper());
		},
		
		/**
		 * Indicates if the tool is visible.
		 * @return {Boolean} true if the toolis visible (active in its location)
		 */
		isActivated: function()
		{
			return this.getWrapper().isVisible(true);
		},
		
		/**
		 * Indicates if the tool is currently focused.
		 * @return {Boolean} true if the tool has the focus.
		 */
		hasFocus: function()
		{
			return Ametys.tool.ToolsManager.getFocusedTool() == this;
		},
		
		/**
		 * Is the tool destroyed?
		 * @return {Boolean} False if the tool is destroyed and should not be used or referenced anymore
		 */
		isNotDestroyed: function()
		{
			return this._destroyed == false;
		},
		
		/**
		 * Listener when the tool is focused
		 * @template
		 */
		onFocus: function()
		{
			this._focusedOnce = true;
			
			var ribbon = Ametys.tool.ToolsManager.getRibbon();
			if (ribbon)
			{
				ribbon.setTitle(this.getTitle());
			}
			
			if (this.getMBSelectionInteraction() == Ametys.tool.Tool.MB_TYPE_NOSELECTION)
			{
				Ext.create("Ametys.message.Message", {
					type: Ametys.message.Message.SELECTION_CHANGED,
					
					targets: null
				});
			}			
			else if (this.getMBSelectionInteraction() == Ametys.tool.Tool.MB_TYPE_ACTIVE)
			{
				this.sendCurrentSelection();
			}

			Ext.create("Ametys.message.Message", {
				type: Ametys.message.Message.TOOL_FOCUSED,
				parameters: {
					creation: this._focusedOnce ? null : this.getFactory().getId()
				},
				
				targets: {
					id: Ametys.message.MessageTarget.TOOL,
					parameters: { tools: [this] }
				}
			});
		},

		/**
		 * Listener when the tool is blurred
		 * @template
		 */
		onBlur: function()
		{
			var ribbon = Ametys.tool.ToolsManager.getRibbon();
			if (ribbon)
			{
				ribbon.setTitle(null);
			}

			Ext.create("Ametys.message.Message", {
				type: Ametys.message.Message.TOOL_BLURRED,
				
				targets: {
					id: Ametys.message.MessageTarget.TOOL,
					parameters: { tools: [this] }
				}
			});
		},
		
		/**
		 * Listener when the tool is activated
		 * @template
		 */
		onActivate: function()
		{
			Ext.create("Ametys.message.Message", {
				type: Ametys.message.Message.TOOL_ACTIVATED,
				
				targets: {
					id: Ametys.message.MessageTarget.TOOL,
					parameters: { tools: [this] }
				}
			});
		},
		
		/**
		 * Listener when the tool is deactivated
		 * @template
		 */
		onDeactivate: function()
		{
			Ext.create("Ametys.message.Message", {
				type: Ametys.message.Message.TOOL_DEACTIVATED,
				
				targets: {
					id: Ametys.message.MessageTarget.TOOL,
					parameters: { tools: [this] }
				}
			});

		},

		/**
		 * Listener when the tool is opened
		 * @template
		 */
		onOpen: function()
		{
			Ext.create("Ametys.message.Message", {
				type: Ametys.message.Message.TOOL_OPENED,
				
				targets: {
					id: Ametys.message.MessageTarget.TOOL,
					parameters: { tools: [this] }
				}
			});
		},
		
		/**
		 * Listener when the tool is closed
		 * @param [hadFocus=true] The tool did have to focus at the instant of #close. If false, the tool should not send any Ametys.message.Message#SELECTION_CHANGED event.
		 * @template
		 */
		onClose: function(hadFocus)
		{
			this._destroyed = true;
			this._focusedOnce = false;
			this._wrapper = null;
			
			// Unregister the tool for all messages 
			Ametys.message.MessageBus.unAll(this);
			
			Ext.create("Ametys.message.Message", {
				type: Ametys.message.Message.TOOL_CLOSED,
				
				targets: {
					id: Ametys.message.MessageTarget.TOOL,
					parameters: { tools: [this] }
				}
			});
			
			if (hadFocus !== false)
			{
				if (this.getMBSelectionInteraction() == Ametys.tool.Tool.MB_TYPE_ACTIVE)
				{
					Ext.create("Ametys.message.Message", {
						type: Ametys.message.Message.SELECTION_CHANGED,
						
						targets: []
					});
				}
			}
		}
	}
);

Ext.define("Ametys.message.ToolMessage",
	{
		override: "Ametys.message.Message",
		
		statics: 
		{
			/**
			 * @readonly
			 * @static
			 * @member Ametys.message.Message
			 * @property {String} TOOL_FOCUSED Event when a tool receives focus.
			 * The target is a Ametys.tool.Tool. 
			 * This event has one parameter:
			 * @property {String} TOOL_FOCUSED.creation If the tool was created and focused in a row, this parameter contains the factory id. null in other cases.
			 */
			TOOL_FOCUSED: "toolFocused",

			/**
			 * @readonly
			 * @static
			 * @member Ametys.message.Message
			 * @property {String} TOOL_BLURRED Event when a tool lost the focus. 
			 * This event has no parameters
			 */
			TOOL_BLURRED: "toolBlurred",

			/**
			 * @readonly
			 * @static
			 * @member Ametys.message.Message
			 * @property {String} TOOL_ACTIVATED Event when a tool has been activated: activated means it becomes the active tool in its location. 
			 * Even if it goes often together, it is necessarily focused. 
			 * The target is a Ametys.tool.Tool. 
			 * This event has no parameters
			 */
			TOOL_ACTIVATED: "toolActivated",

			/**
			 * @readonly
			 * @static
			 * @member Ametys.message.Message
			 * @property {String} TOOL_DEACTIVATED Event when a tool has been deactivated: deactivated means it is not the visible tool in its location (for example not the active tab). 
			 * Even if it goes often together, it is necessarily blurred. 
			 * This does not mean that the tool is closed. 
			 * The target is a Ametys.tool.Tool. 
			 * This event has no parameters
			 */
			TOOL_DEACTIVATED: "toolDeactivated",

			/**
			 * @readonly
			 * @static
			 * @member Ametys.message.Message
			 * @property {String} TOOL_PARAMS_UPDATED Event when an opened tool received new parameters.
			 * The target is a Ametys.tool.Tool. 
			 * This event has no parameters
			 */
			TOOL_PARAMS_UPDATED: "toolParamsUpdated",
			
			/**
			 * @readonly
			 * @static
			 * @member Ametys.message.Message
			 * @property {String} TOOL_DIRTY_STATE_CHANGED Event when the dirty state of the tool has changed.
			 * The target is a Ametys.tool.Tool. 
			 * This event has one parameter:
			 * @property {Boolean} TOOL_DIRTY_STATE_CHANGED.dirty The new dirty state
			 * 
			 */
			TOOL_DIRTY_STATE_CHANGED: "toolDirtyStateChanged",
			
			/**
			 * @readonly
			 * @static
			 * @member Ametys.message.Message
			 * @property {String} TOOL_OPENED Event when a tool is opened. Even if it goes often together, it is necessarily focused nor activated.
			 * The target is a Ametys.tool.Tool. 
			 * This event has no parameters
			 */
			TOOL_OPENED: "toolOpened",

			/**
			 * @readonly
			 * @static
			 * @member Ametys.message.Message
			 * @property {String} TOOL_CLOSED Event when a tool is closed. Even if it goes often together, it is necessarily blurred nor deactivated. 
			 * The target is a Ametys.tool.Tool. 
			 * This event has no parameters
			 */
			TOOL_CLOSED: "toolClosed"
		}
	}
);
