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
 * This implementation of Ametys.ui.tool.ToolsLayout do not handle location and simply display one tool.
 * Please note that there is no way to close a tool brought by this layout.
 */
Ext.define("Ametys.ui.tool.layout.SimpleToolsLayout", 
	{
		extend: "Ametys.ui.tool.ToolsLayout",
	
		createLayout: function()
		{
			// TODO
		},
		
		focusTool: function(tool)
		{
			// TODO
		},
		
		getFocusedTool: function()
		{
			// TODO
		},
		
		getSupportedLocations: function()
		{
			return [''];
		},
		
		getToolsAtLocation: function(location)
		{
			// TODO
		},

		onToolInfoChanged: function(tool)
		{
			// TODO
		},

		addTool: function(tool, forceToolLocation)
		{
			// TODO
		},
		
		moveTool: function(tool, location)
		{
			// TODO
		},
		
		removeTool: function(tool)
		{
			// TODO
		}		
	}
);
