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
 * This factory creates Ametys.message.MessageTarget when the target is a tool.
 * 
 * The target can be the tool, or the tool id.
 * 
 * To create a tool message you have to transmit parameters documented in the #createTargets method.
 * Once created a tool message has two properties: id or tool. See #cfg-parameters for details.
 * 
 * 			Ext.create("Ametys.message.Message", {
 *				type: Ametys.message.Message.TOOL_CLOSED,
 *				
 *				targets: {
 *					type: Ametys.message.MessageTarget.TOOL,
 *					parameters: { tools: [this] }
 *				}
 *			});
 */
Ext.define("Ametys.tool.ToolMessageTargetFactory",
	{
		extend: "Ametys.message.MessageTargetFactory",

		/**
		 * Create the targets for a message
		 * @param {Object} parameters The parameters needed by the factory to create the message. Can not be null. Handled elements are
		 * @param {String[]} parameters.ids The tool identifier. Must be present if tools is empty
		 * @param {Ametys.tool.Tool[]} parameters.tools The tools themselves. Must be present if ids is empty
		 * @param {Function} callback The callback function called when the targets are created. Parameters are
		 * @param {Ametys.message.MessageTarget[]} callback.targets The targets created. Cannot be null.
		 */
		createTargets: function(parameters, callback)
		{
			var targets = [];
			
			if (parameters.ids)
			{
				for (var i = 0; i < parameters.ids.length; i++)
				{
					var id = parameters.ids[i];
					var tool = Ametys.tool.ToolsManager.getTool(id);
					
					var target = Ext.create("Ametys.message.MessageTarget", {
						type: this.getType(),
						parameters: {
							id: id,
							tool: tool
						}
					});		
					targets.push(target);
				}
			}
			else
			{
				for (var i = 0; i < parameters.tools.length; i++)
				{
					var tool = parameters.tools[i];
					var id = tool.getId();
					
					var target = Ext.create("Ametys.message.MessageTarget", {
						type: this.getType(),
						parameters: {
							id: id,
							tool: tool
						}
					});						
					targets.push(target);
				}
			}

			callback(targets);
		}
	}
);

Ext.define("Ametys.message.ToolMessageTarget",
	{
		override: "Ametys.message.MessageTarget",
		
		statics: 
		{
			/**
			 * @member Ametys.message.MessageTarget
			 * @readonly
			 * @property {String} TOOL The target type is a tool. See Ametys.tool.ToolMessageTargetFactory#createTargets parameters to know more of the associated parameters. 
			 */
			TOOL: "tool"
		}
	}
);
