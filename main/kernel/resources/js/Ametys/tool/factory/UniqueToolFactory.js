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
 * This implementation of Ametys.tool.ToolFactory ensure that a single tool is used.
 * On first call a tool is created, but on a second call it is just parametrized.
 * If the tool was closed in between it is recreated.
 * 
 * The tool do receive as id : the factory class name + $ + the factory role.
 * 
 * 		var myFactory = Ext.create("Ametys.tool.factory.UniqueToolFactory", {
 * 			pluginName: 'myplugin',
 * 			toolClass: "MyHistoryTool",
 * 			parameters: {
 * 				...
 * 			},
 * 			role: 'history'
 * 		});
 * 
 * 		Ametys.tool.ToolsManager.openTool('history', {})
 */
Ext.define("Ametys.tool.factory.UniqueToolFactory", 
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
		 * The parameter toolParam is not used by this implementation
		 */
		openTool: function(toolParams)
		{
			// The id of the associated unique tool
			var toolId = this.getRole();
			
			var tool = Ametys.tool.ToolsManager.getTool(toolId); 
			if (tool == null)
			{
				// Tool does not exists => Tool creation.
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
