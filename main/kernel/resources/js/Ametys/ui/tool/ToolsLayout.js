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
 * This class is an interface of the main part of the ui: the zone with tools.
 * 
 * The implementations are in charge for creating the layout for tools and to display them.
 * 
 * All the follwing methods may have a location as argument, but keep in mind that the implementation of the location if dependant on the implentation your are using. 
 * All implementations can interpret the location as they want and could simply ignore it if a single location is used.
 * The string location is a succession of letters between c (center), t (top), r (right), b (bottom), l (left).
 * So "l" the left location, "lr" the right part of the left location, "c" the center part of the default location. Remember that even if you write "lrlrlr" the implemetation may interpret it as "l" as it does not know any other location.
 * 
 * A tool policy is the way new tools are opened : a new tool should replace an existing tool or be opened aside?. See #getSupportedToolPolicies and #setToolPolicy.
 */
Ext.define("Ametys.ui.tool.ToolsLayout", 
	{
        /**
         * @cfg {Boolean} initialized=true Use this configuration to start the layout as non-initialized, and call #setAsInitialized once you are ready.
         * Set to false, when you want to add initial tool and then call #setAsInitialized 
         */
        initialized: true,
        
		/**
		 * Create the layout instance
         * @param {Object} config The configuration
		 * @template
		 */
		constructor: function(config)
		{
            this.initConfig(config);
		},

		/**
		 * This method is in charge for creating the extjs container that will graphically holds the tools panels.
		 * Should be called once only during layout creation.
		 * @template
		 * @return {Ext.container.Container} The layout
		 */
		createLayout: function()
		{
			throw new Error("This method is not implemented in " + this.self.getName());
		},
		
		/**
		 * The layout will activate the tool by bringing back to visible if necessary, and will focus it.
		 * @param {Ametys.ui.tool.ToolPanel} tool The tool to focus
		 * @template
		 */
		focusTool: function(tool)
		{
			// Implement this in sub class
		},
		
		/**
		 * Get the currently focused tool
		 * @returns {Ametys.ui.tool.ToolPanel} tool The tool focused. Can be null if no tools are opened
		 */
		getFocusedTool: function()
		{
			throw new Error("This method is not implemented in " + this.self.getName());
		},
		
		/**
		 * Get the list of supported locations.
		 * @return {String[]} An non-null array with at least one element. Elements are non-null and uniques.
		 * @template
		 */
		getSupportedLocations: function()
		{
			throw new Error("This method is not implemented in " + this.self.getName());
		},
        
        /**
         * Get the supported location nearest of the desired location
         * @param {String} desiredLocation The desired location. Can be null or empty to get the default location.
         * @return {String} A valid location.
         */
        getNearestSupportedLocation: function(desiredLocation)
        {
            throw new Error("This method is not implemented in " + this.self.getName());
        },
		
		/**
		 * Get the ordered list of opened tools at the given location.
		 * @param {String} location The location where tools are. Cannot be null.
		 * @return {Ametys.ui.tool.ToolPanel[]} A non-null ordered array of opened tools at the location. If location is unsupported array will be empty. 
		 * @template
		 */
		getToolsAtLocation: function(location)
		{
			throw new Error("This method is not implemented in " + this.self.getName());
		},

		/**
		 * Update the informations of the tool (label, icon, ...)
		 * @param {Ametys.ui.tool.ToolPanel} tool The tool that has to been updated
		 * @template
		 */
		onToolInfoChanged: function(tool)
		{
			// Implement this in sub class
		},

		/**
		 * Add a tool that has never been rendered
		 * @param {Ametys.ui.tool.ToolPanel} tool The tool to add
		 * @param {String} [location] The location required to place the tool. The default location will depend on the implementation (will be #getNearestSupportedLocation(null))
		 * @template
		 */
		addTool: function(tool, location)
		{
			throw new Error("This method is not implemented in " + this.self.getName());
		},
		
		/**
		 * Move a tool to another location.
		 * Do not call this: internal call of Ametys.tool.ToolsManager#move
		 * @param {Ametys.ui.tool.ToolPanel} tool The already added tool to graphically move
		 * @param {String} location The location where to move the tool to. The default location will depend on the implementation (will be #getNearestSupportedLocation(null))
		 * @template
		 */
		moveTool: function(tool, location)
		{
			// Implement this in sub class
		},
		
		/**
		 * Removes a tool
		 * @param {Ametys.ui.tool.ToolPanel} tool The tool to remove
		 * @template
		 */
		removeTool: function(tool)
		{
			throw new Error("This method is not implemented in " + this.self.getName());
		},
        
        /**
         * Get the list of available tool policies.
         * @return {String[]} The non-null and non-empty list of supported policies
         */
        getSupportedToolPolicies: function()
        {
            throw new Error("This method is not implemented in " + this.self.getName());
        },
        
        /**
         * Set the policy to use when opening tool. Depending on implementation, policy may only be applied on new tools.
         * @param {String} policy The policy to apply. Can be null or empty to use the default policy.
         * @template
         */
        setToolPolicy: function(policy)
        {
            // Implement this in sub class
        },
        
        /**
         * Call this method once initially added
         */
        setAsInitialized: function()
        {
            this.initialized = true;            
        }
	}
);
