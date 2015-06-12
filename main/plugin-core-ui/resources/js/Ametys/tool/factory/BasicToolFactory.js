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
 * This implementation of Ametys.tool.ToolFactory is a basic factory since it use the id parameter to create the tool parametrized.
 * 
 * The tool do receive as id : the factory class name + $ + the id parameter.
 * 
 * 		var myFactory = Ext.create("Ametys.tool.factory.BasicToolFactory", {
 * 			pluginName: 'myplugin',
 * 			toolClass: "MyContentTool",
 *			...
 * 			role: 'content'
 * 		});
 * 
 * 		Ametys.tool.ToolsManager.openTool('content', {id: contentId})
 */
Ext.define("Ametys.tool.factory.BasicToolFactory", 
	{
		extend: "Ametys.tool.ToolFactory",
			
		/**
		 * @cfg {String} toolClass (required) The class name to use to create the tool
		 */
		/**
		 * @property {String} _toolClass See {@link #cfg-toolClass}
		 * @private
		 */
	
		/**
		 * @cfg {Object} tool (required) The tool configuration. See the configuration required by the {@link Ametys.tool.Tool} implementation your are using.
		 * Do not transmit the configuration parameters 'id', 'factory' nor 'pluginName' that will be automatically added.
		 */
	
		constructor: function(config)
		{
			this.callParent(arguments);
			
			this._toolClass = config.toolClass;
		},
	
		/**
		 * @inheritdoc
		 * @param {Object} toolParams Use the following required parameter
		 * @param {String} toolParams.id A unique identifier for the tool
		 */
		openTool: function(toolParams)
		{
			// The id of the associated unique tool
			var toolId = this.getRole() + "$" + toolParams.id
			
			var tool = Ametys.tool.ToolsManager.getTool(toolId); 
			if (tool == null)
			{
				// Tool does not exist => Tool creation.
				var toolConfig = Ext.applyIf({
						id: toolId,
						factory: this,
						pluginName: this.getPluginName()
					}, this.getInitialConfig()
				);
				
				tool = Ext.create(this._toolClass, toolConfig);
			}	
			
			return tool;
		}
	}
);
