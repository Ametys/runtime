/*
 *  Copyright 2014 Anyware Services
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
 * This class is the main point to open, close or get tools (through tools factories).
 * This class is also the entry point to get and set the tools layout implementation #getToolsLayout.
 * 
 * 		Ametys.tool.ToolsManager.addFactory(MyFactory);
 * 		Ametys.tool.ToolsManager.openTool(MyFactory.getRole(), { ... });
 */
Ext.define("Ametys.tool.ToolsManager", 
	{
		singleton: true,
		mixins: {
			state: 'Ext.state.Stateful'
		},
		
		/**
		 * @property {Object} _factories The registered factories stored by role name
		 * @private
		 */
		_factories: {},

		/**
		 * @property {Object} _autoOpenedTools The tools automatically opened during startup. See the parameter config.autoOpenedTools of #init for the stucture.
		 * This property is used internally by #init, but is not an internal variable because it can be modified by #applyState when restoring a previous state.
		 * @private
		 */
		_autoOpenedTools: {},
        
        /**
         * @property {Object} _overridenDefaultLocation The keys are factories identifier, and the associated values are the default location that will override the {@link Ametys.tool.Tool#getDefaultLocation} call.
         * Theses values are set when the user manually moves tools.
         * @private
         */
        _overridenDefaultLocation: {},

		/**
		 * @property {Object} _tools The opened tools stored by unique identifier
		 * @private
		 */
		_tools: {},
		
		/**
		 * @property {Boolean} _initialized Is the tools manager initialized? (call to method #init done) so we can save state now... (avoid saving while reopening)
		 * @private
		 */
		_initialized: false,
		
		/**
		 * @property {Ametys.ui.tool.ToolsLayout} _layout The current tools layout implementation.
		 * @private
		 */
		
		/**
		 * @property {Ametys.ui.fluent.ribbon.Ribbon} _ribbon The current ribbon instance. See #setRibbon.
		 * @private
		 */
		_ribbon: null,
		
		/**
		 * Initialize the tools opened at startup.
		 * The values transmitted here can be ignored and replaced by the ones stored by the user.
		 * This method is called automatically during startup and should not be called after. 
		 * @param {Object} config The config object may have the following subobjects
		 * @param {Object[]} config.autoOpenedTools The tools to open
		 * @param {String} config.autoOpenedTools.role The factory role of the tool. See #openTool.
		 * @param {Object} config.autoOpenedTools.toolParams The parameters to open the tool. See #openTool.
		 * @param {String} [config.autoOpenedTools.forceToolLocation] The parameters where to open the tool. See #openTool.
		 */
		init: function(config)
		{
			this._autoOpenedTools = config ? config.autoOpenedTools || [] : [];
			
			// if the user do not want to remember tools, we do not reopen them by doing this: 
			// we remember the default opened tools in the variable replaceAutoOpenedTools, and after restoring the state, we crash the value 
			var replaceAutoOpenedTools;
			if (Ametys.userprefs.UserPrefsDAO.getValue("remember-opened-tools") == "false")
			{
				replaceAutoOpenedTools = this._autoOpenedTools;
			}
			
			// restore state to overwrite values
            this.stateful = true;
            this.stateId = this.self.getName();
            this.hasListeners = {}; // used by saveState
			this.mixins.state.constructor.call(this);

			// open tools in #_autoOpenedTools
			if (replaceAutoOpenedTools)
			{
				this._autoOpenedTools = replaceAutoOpenedTools;
			}
            
			for (var i = 0; i < this._autoOpenedTools.length; i++)
			{
				var tool = this.openTool(this._autoOpenedTools[i].role, this._autoOpenedTools[i].toolParams, this._autoOpenedTools[i].forceToolLocation);
                if (tool == null)
                {
                    throw new Error("Cannot load tool '" + this._autoOpenedTools[i].role + "' at location '" + this._autoOpenedTools[i].forceToolLocation + "'")
                }
			}
			
			this._initialized = true;
		},
		
		/**
		 * Is the tools manager initialized ?
		 * @return {boolean} True if the tools manager is initialized
		 */
		isInitialized: function()
		{
			return this._initialized === true;
		},
		
        /**
         * @private
         * Used by #saveState to delegate save to plugins.
         * @return {Ext.plugin.Abstract[]} The plugins. Will be null here.
         */
        getPlugins: function() 
        {
            return null;
        },
        
		getState: function()
		{
			var state = {
				_autoOpenedTools: [],
                _overridenDefaultLocation: this._overridenDefaultLocation
			};
			
			// Do we want to remember opened tools?
			if (Ametys.userprefs.UserPrefsDAO.getValue("remember-opened-tools") == "true")
			{
				var locations = this.getToolsLayout().getSupportedLocations();
				for (var i = locations.length - 1; i >= 0; i--)
				{
					var location = locations[i];
					var toolsPanels = this.getToolsLayout().getToolsAtLocation(location);
					for (var j = 0; j < toolsPanels.length; j++)
					{
						var toolPanel = toolsPanels[j];
                        var tool = this.getTool(toolPanel.uiTool);
						
						state._autoOpenedTools.push({
							role: tool.getFactory().getRole(),
							toolParams: tool.getParams(),
							forceToolLocation: location
						});
					}
				}
			}
			
			return state;
		},
		
		/**
		 * Handle a new Ametys.tool.ToolFactory
		 * @param {Ametys.tool.ToolFactory} factory The factory
		 */
		addFactory: function(factory)
		{
			if (this._factories[factory.getRole()] != null && this.getLogger().isWarnEnabled())
			{
				this.getLogger().warn("Replacing factory '" + factory.getRole() + "' with a new one");
			}
			else if (this.getLogger().isDebugEnabled())
			{
				this.getLogger().debug("Adding factory '" + factory.getRole() + "'");
			}
			
			this._factories[factory.getRole()] = factory;
		},
		
		/**
		 * Get a tool by its id
		 * @param {String} id The id of the tool to get
		 * @returns {Ametys.tool.Tool} The tool or null if the tool is not opened anymore
		 */
		getTool: function (id)
		{
			var tool = this._tools[id]
			if (tool == null && this.getLogger().isWarnEnabled())
			{
				this.getLogger().warn("Can not get tool of id '" + id + "' because it is not opened");
			}
			else if (this.getLogger().isDebugEnabled())
			{
				this.getLogger().debug("Getting tool of id '" + id + "'");
			}
				
			return tool;
		},
		
		/**
		 * Get the currently focused tool
		 * @returns {Ametys.tool.Tool} tool The tool focused. Can be null if no tools are opened
		 */
		getFocusedTool: function()
		{
            var toolPanel = this.getToolsLayout().getFocusedTool();
            if (!toolPanel)
            {
                return null;
            }
            else
            {
                return this.getTool(toolPanel.uiTool);
            }
		},
		
		/**
		 * Get the tools. Do not modify the return object.
		 * @returns {Object} The tools in an object where property name is the object unique identifier and the value the associated Ametys.tool.Tool object.
		 */
		getTools: function ()
		{
			return this._tools;
		},	
		
		/**
		 * Open a tool given a role name and parameters. Ask the factory to open the tool : the tool may already be existing (even already opened) or created now.
		 * This is the main method to call when you want to open a tool.
		 * 
		 * This method creates or gets the tool through Ametys.tool.ToolFactory#openTool
		 * If the tool is new, it will add it to the graphic layout #getToolsLayout Ametys.ui.tool.ToolsLayout#addTool
		 * In all cases, it will then call Ametys.tool.Tool#setParams and Ametys.tool.Tool#activate
		 * 
		 * @param {String} role The role of the factory which will create the tool to open
		 * @param {Object} toolParams The parameters needed to open the tool. See the factory documentation for further details.
		 * @param {String} [forceToolLocation] A tool location (see Ametys.tool.ToolsLayout) to replace the Ametys.tool.Tool#getDefaultLocation.
		 * @returns {Ametys.tool.Tool} The new tool or undefined if an error occured while opening the tool
		 */
		openTool: function (role, toolParams, forceToolLocation)
		{
			try
			{
				var factory = this._factories[role];
				if (factory == null)
				{
					throw new Error("The factory '" + role + "' is not registered");
				}
				
				var tool = factory.openTool(toolParams);
                
				var effectiveLocation = null;
				
				// Is the tool already registered
				if (this._tools[tool.getId()] == null)
				{
					// Add a new tool
					if (this.getLogger().isInfoEnabled())
					{
						this.getLogger().info({message: "Opening a tool '" + tool.getId() + "' from factory '" + role + "'", details: "Parameters are " + toolParams});
					}
					
					this._tools[tool.getId()] =  tool;
					
                    // Determining location
                    if (forceToolLocation != null)
                    {
                        effectiveLocation = forceToolLocation;
                    }
                    else if (this._overridenDefaultLocation[tool.getFactory().getRole()] != null)
                    {
                        effectiveLocation = this._overridenDefaultLocation[tool.getFactory().getRole()];
                    }
                    else
                    {
                        effectiveLocation = tool.getDefaultLocation();
                    }
                    
                    // change the default location for tools of that kind
                    this._overridenDefaultLocation[tool.getFactory().getRole()] = effectiveLocation;
                    if (this.isInitialized())
                    {
                        this.saveState();
                    }
                    
					this.getToolsLayout().addTool(tool.createWrapper(), effectiveLocation);
                    
                    tool.getWrapper().on("toolmoved", this._onToolPanelMoved, this, { toolId: tool.getId() });
				}
				else
				{
					if (forceToolLocation != null)
					{
                        // change the default location for tools of that kind
						this.moveTool(tool, forceToolLocation);
					}
					
					if (this.getLogger().isInfoEnabled())
					{
						this.getLogger().info({message: "Reopening a tool '" + tool.getId() + "' from factory '" + role + "'", details: "Parameters are " + toolParams});
					}
				}
				
				tool.setParams(toolParams);
				
				if (this.isInitialized() || effectiveLocation != null && this.getToolsLayout().getToolsAtLocation(effectiveLocation).length == 1)
				{
					tool.focus();
					
					if (this.isInitialized())
					{
						this.saveState();
					}
				}
				
				
				return tool;
			}
			catch (e)
			{
				Ametys.log.ErrorDialog.display({
					title: "<i18n:text i18n:key='PLUGINS_CORE_UI_MSG_TOOLS_OPEN_ERROR_TITLE'/>",
					text: "<i18n:text i18n:key='PLUGINS_CORE_UI_MSG_TOOLS_OPEN_ERROR_TEXT'/>",
					details: e,
					category: this.self.getName()
				});
			}
		},
        
        /**
         * @private
         * Listener when a toolPanel have been moved
         * @param {Ametys.ui.tool.ToolPanel} toolPanel The moved panel
         * @param {String} newLocation The new location
         * @param {Object} eOpts The options
         * @param {String} eOpts.toolId The tool identifier
         */
        _onToolPanelMoved: function(toolPanel, newLocation, eOpts)
        {
            if (this.isInitialized())
            {
                var tool = this.getTool(eOpts.toolId); 
                this._overridenDefaultLocation[tool.getFactory().getRole()] = newLocation;
                this.saveState();
            }
        },
		
		/**
		 * Moves an opened tool to a new location of the Ametys.ui.tool.ToolsLayout
		 * @param {Ametys.tool.Tool} tool The already added tool to graphically move
		 * @param {String} newLocation The location where to move the tool to
		 **/
		moveTool: function(tool, newLocation)
		{
			this.getToolsLayout().moveTool(tool.getWrapper(), newLocation);
		},
		
		/**
		 * Get the factory by its role.
		 * Use this #openTool instead to create a tool.
		 * @param {String} role The role of the factory which will creates the tool to open
		 * @returns {Ametys.tool.ToolFactory} The factory registered for this role. Can be null.
		 */
		getFactory: function(role)
		{
			return this._factories[role];
		},
		
		/**
		 * Removes the tool from the list of opened tools
		 * @param {Ametys.tool.Tool} tool The id of the tool to get
		 */
		removeTool: function(tool)
		{
			if (this.getLogger().isDebugEnabled())
			{
				this.getLogger().debug("Removing tool '" + tool.getId() + "'");
			}
			
			this.getToolsLayout().removeTool(tool.getWrapper());
			
			delete this._tools[tool.getId()];
			
			if (this.isInitialized())
			{
				this.saveState();
			}
		},
		
		/**
		 * Refresh the out dated tools if they are in auto refresh state and if they are visible
		 * Internal call when necessary
		 */
		refreshTools: function ()
		{	
			var tools = this.getTools();
			for (var i in tools)
			{
				var tool = tools[i];
				
				if (tool.isOutOfDate()
                    && tool.getFactory().isAutoRefreshEnabled()
                    && tool.getWrapper().isVisible())
				{
					// Test if tool is visible (if not, the tool will send an (activated) message that will makes the messagebus to recall the #refreshTools)
					tool.refresh();
				}
			}
		},	
		
		/**
		 * Get the active impl of the Ametys.ui.tool.ToolsLayout
		 * @returns {Ametys.ui.tool.ToolsLayout} The active tools layout implementation. Cannot be null.
		 */
		getToolsLayout: function()
		{
			return this._layout;
		},
		
		/**
		 * Set the active implementation of the Ametys.ui.tool.ToolsLayout. This method has to be call once and once only.
		 * @param {String} layoutName The active tools layout implementation. Cannot be null.
         * @param {Object} layoutConfig The configuration to transmit to the new layout object.
		 * @throws If called a second time, an exception will be thrown (and logged)
		 */
		setToolsLayout: function(layoutName, layoutConfig)
		{
			if (this._layout == null)
			{
				this._layout = Ext.create(layoutName, layoutConfig);
			}
			else
			{
				var details = "Layout was '" + this._layout.self.getName() + "' and will not be changed to '" + layoutName + "'";
				this.getLogger().error({message: "setToolsLayout cannot be called twice", details: details});
				throw new Error(details); 
			}
		},

		/**
		 * Get the ribbon associated with the current layout.
		 * @return {Ametys.ui.fluent.ribbon.Ribbon} The current instance of the ribbon. Can be null.
		 */
		getRibbon: function()
		{
			return this._ribbon;
		},
		
		/**
		 * Set the ribbon instance associated. Call this during initialization. Used by all tools to set the current tool as title
		 * @param {Ametys.ui.fluent.ribbon.Ribbon} ribbon The ribbon instance
		 */
		setRibbon: function(ribbon)
		{
			this._ribbon = ribbon;
		}
	}
);
