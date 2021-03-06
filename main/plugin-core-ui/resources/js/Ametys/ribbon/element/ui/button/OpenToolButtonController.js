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
 * This class controls a ribbon button that opens/closes a tool.
 * Unique instance tool state will be reflected the open/close status by beeing toggled or not.
 */
Ext.define(
	"Ametys.ribbon.element.ui.button.OpenToolButtonController",
	{
		extend: "Ametys.ribbon.element.ui.ButtonController",

		/**
		 * @cfg {String} action Will be autogenerated.
		 * @inheritdoc
		 * @private
		 */
		/**
		 * @cfg {Boolean} toggle-enabled Will be autogenerated
		 * @inheritdoc
		 * @private
		 */

		/**
		 * @cfg {String} opentool-id (required) This configuration is required to set the factory id to use. See Ametys.tool.ToolFactory. Parameters can be provided througth #cfg-tool-config
		 */
		/**
		 * @cfg {Object} opentool-params This configuration is a json object that will be transmited to the method Ametys.tool.ToolsManager#openTool (and consequently to the factory Ametys.tool.ToolFactory#openTool and to the tool Ametys.tool.Tool#setParams.
		 */
		
		constructor: function(config)
		{
			config.action =  config.action || "Ametys.ribbon.element.ui.button.OpenToolButtonController._act";
			
			var toolId = config["opentool-id"];
			if (Ametys.tool.ToolsManager.getFactory(toolId) instanceof Ametys.tool.factory.UniqueToolFactory || Ametys.tool.ToolsManager.getFactory(toolId) instanceof Ametys.tool.factory.BasicToolFactory)
			{
				config['toggle-enabled'] = true;
	
				Ametys.message.MessageBus.on(Ametys.message.Message.TOOL_OPENED, this._onToolOpenedOrClosed, this);
				Ametys.message.MessageBus.on(Ametys.message.Message.TOOL_CLOSED, this._onToolOpenedOrClosed, this);
				
				if (config["opentool-params"])
				{
					Ametys.message.MessageBus.on(Ametys.message.Message.TOOL_PARAMS_UPDATED, this._onToolParamsUpdated, this);
				}
			}
			
			this.callParent(arguments);
		},
	
		statics: {
			/**
			 * @protected
			 * This action do open the tool using the given Ametys.tool.ToolFactory id. The following additional configuration are required: #cfg-opentool-id, #cfg-opentool-params.
			 * @param {Ametys.ribbon.element.ui.button.OpenToolButtonController} button This controller.
			 */
			_act: function (button)
			{
				var toolId = button.getInitialConfig("opentool-id");
				var parameters = button.getInitialConfig("opentool-params") || {};
				
				var tool = null;
				if (Ametys.tool.ToolsManager.getFactory(toolId) instanceof Ametys.tool.factory.UniqueToolFactory)
				{
					 tool = Ametys.tool.ToolsManager.getTool(toolId);
				}
				else if (Ametys.tool.ToolsManager.getFactory(toolId) instanceof Ametys.tool.factory.BasicToolFactory)
				{
					tool = Ametys.tool.ToolsManager.getTool(toolId + "$" + parameters.id)
				}
				
				if (tool != null && this._toolParametersMatch(tool, parameters))
				{
					if (tool.isActivated())
					{
						tool.close();
					}
					else
					{
						tool.focus();
						
						// Manually toggle to true because the user click untoggle the button by default.
						button.toggle(true);
					}
				}
				else
				{
					Ametys.tool.ToolsManager.openTool(toolId, parameters);
				}
			},
			
			/**
			 * @private
			 * Helper function testing the parameters of a tool against a set a parameters passed as arguments to the function.
			 * @param {Ametys.tool.Tool} tool the tool to test
			 * @param {Object} parameters An association parameters name and value (as strings) 
			 * @return {Boolean} `true` if for each parameter passed as arguments the tool has a parameter with the same name (key) and the same value. 
			 */
			_toolParametersMatch: function(tool, parameters)
			{
				parameters = parameters || {};
				var match = true;
				
				Ext.Object.each(parameters, function(key, value) {
					if (tool.getParams()[key] != value)
					{
						match = false;
					}
					
					// stop iteration when match is false.
					return match;
				});
				
				return match;
			}
		},
		
		/**
		 * Listener on tool opened/closed
		 * @param {Ametys.message.Message} message Tool message
		 * @private
		 */
		_onToolOpenedOrClosed: function(message)
		{
			var toolId = this.getInitialConfig("opentool-id");
			var parameters = this.getInitialConfig("opentool-params") || {};
			
			var thisToolTarget = message.getTarget(function (target) { return target.getParameters()['tool'].getFactory().getId() == toolId; });
			
			if (thisToolTarget)
			{
				var tool = thisToolTarget.getParameters().tool;
				var match = tool && Ametys.ribbon.element.ui.button.OpenToolButtonController._toolParametersMatch(tool, parameters);
				if (match)
				{
					var isOpen = message.getType() == Ametys.message.Message.TOOL_OPENED;
					this.toggle(isOpen);
				}
			}
		},
		
		/**
		 * Listener on tool params updated.
		 * @param {Ametys.message.Message} message Tool message
		 * @private
		 */
		_onToolParamsUpdated: function(message)
		{	
			var toolId = this.getInitialConfig("opentool-id");
			var parameters = this.getInitialConfig("opentool-params") || {};
			
			var thisToolTarget = message.getTarget(function (target) { return target.getParameters()['tool'].getFactory().getId() == id; });
			if (thisToolTarget)
			{
				// FIXME CMS-5743 Iterate on all tools
				var tool = thisToolTarget.getParameters().tool;
				var match = tool && Ametys.ribbon.element.ui.button.OpenToolButtonController._toolParametersMatch(tool, parameters);
				this.toggle(match);
			}
		}
	}
);

