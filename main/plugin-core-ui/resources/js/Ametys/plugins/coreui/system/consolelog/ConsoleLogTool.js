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
 * This tool does display the logs of Ametys.log.Logger.
 * @private
 */
Ext.define("Ametys.plugins.coreui.system.consolelog.ConsoleLogTool",
	{
		extend: "Ametys.tool.Tool",
		
		statics: {
			/**
			 * This action find the unique instance of the log tool, and removes all the entries
			 */
			removeAll: function()
			{
				Ametys.log.LoggerFactory.getStore().removeAll();
			},
			
			/**
			 * Get the root log level to toggle the controller
			 * @param {Ametys.ribbon.element.RibbonUIController} controller The controller of this log level
			 */
			getRootLogLevel: function(controller)
			{
				var level = parseInt(controller.getInitialConfig("log-level"));
				controller.toggle(Ametys.log.LoggerFactory.getLoggerFor('').getLogLevel() == level);
			},
			
			/**
			 * Change the main log level defined in configuration "log-level"
			 * @param {Ametys.ribbon.element.RibbonUIController} controller The controller of this log level
			 * @param {Boolean} state The controller state
			 */
			setRootLogLevel: function(controller, state)
			{
				var idsToUntoggle = controller.getInitialConfig("untoggle").split(",");
				Ext.Array.each(idsToUntoggle, function(id) {
					Ametys.ribbon.RibbonManager.getElement(id).toggle(false);
				});
				
				controller.toggle(true);
				
				var level = parseInt(controller.getInitialConfig("log-level"));
				Ametys.log.LoggerFactory.getLoggerFor('').setLogLevel(level);
			},
			
			/**
			 * Renderer for the other columns
			 * @param {Number} value The level
			 * @param {Object} metadata A collection of metadata about the current cell; can be used or modified by the renderer. Recognized properties are: tdCls, tdAttr, and style
			 * @param {Ext.data.Model} record The record for the current row
			 * @private
			 */
			allRenderer: function(value, metadata, record)
			{
				metadata.tdCls = "msg-level-" + record.get('level');
				return value;
			},	
			
			/**
			 * @private
			 * @property {Function} _dateRenderer The internal real date renderer
			 */
			_dateRenderer: Ext.util.Format.dateRenderer(Ext.Date.patterns.ShortDateTime),
			/**
			 * @private
			 * Renderer for the date with color
			 * @param {Number} value The level
			 * @param {Object} metadata A collection of metadata about the current cell; can be used or modified by the renderer. Recognized properties are: tdCls, tdAttr, and style
			 * @param {Ext.data.Model} record The record for the current row
			 */
			dateRenderer: function(value, metadata, record)
			{
				metadata.tdCls = "msg-level-" + record.get('level');
				return Ametys.plugins.coreui.system.consolelog.ConsoleLogTool._dateRenderer.apply(Ametys.plugins.coreui.system.consolelog.ConsoleLogTool, arguments);
			},
            
            /**
             * @private
             * Renderer for the stacktrace with color
             * @param {Number} value The level
             * @param {Object} metadata A collection of metadata about the current cell; can be used or modified by the renderer. Recognized properties are: tdCls, tdAttr, and style
             * @param {Ext.data.Model} record The record for the current row
             */
            stacktraceRendered: function(value, metadata, record)
            {
                metadata.tdCls = "msg-level-" + record.get('level');
                return Ext.String.stacktraceToHTML(value, 1);                
            }
		},
		
        /**
         * Renderer for the level column
         * @param {Number} value The level
         * @param {Object} metadata A collection of metadata about the current cell; can be used or modified by the renderer. Recognized properties are: tdCls, tdAttr, and style
         * @private
         */
        levelRenderer: function(value, metadata)
        {
            var imgSrc = Ametys.CONTEXT_PATH + this.getInitialConfig("icon-level-" + value);
            
            metadata.tdCls = "msg-level-" + value;
            switch (value)
            {
                case Ametys.log.Logger.Entry.LEVEL_DEBUG: return "<img src=\"" + imgSrc + "\"/><i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_CONSOLELOGTOOL_COL_LEVEL_0'/>";
                case Ametys.log.Logger.Entry.LEVEL_INFO: return "<img src=\"" + imgSrc + "\"/><i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_CONSOLELOGTOOL_COL_LEVEL_1'/>";
                case Ametys.log.Logger.Entry.LEVEL_WARN: return "<img src=\"" + imgSrc + "\"/><i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_CONSOLELOGTOOL_COL_LEVEL_2'/>";
                case Ametys.log.Logger.Entry.LEVEL_ERROR: return "<img src=\"" + imgSrc + "\"/><i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_CONSOLELOGTOOL_COL_LEVEL_3'/>";
                default:
                case Ametys.log.Logger.Entry.LEVEL_FATAL: return "<img src=\"" + imgSrc + "\"/><i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_CONSOLELOGTOOL_COL_LEVEL_4'/>";
            }
        },
            
		/**
		 * @property {Ext.grid.Panel} grid The grid panel displaying the logs
		 * @private
		 */
		
		createPanel: function()
		{
			this.grid = Ext.create("Ext.grid.Panel", { 
				stateful: true,
				stateId: this.self.getName() + "$grid",
				store: Ametys.log.LoggerFactory.getStore(),
				autoScroll: true,
				cls: 'uitool-consolelog',
			    columns: [
			        {stateId: 'grid-date', header: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_CONSOLELOGTOOL_COL_DATE'/>", width: 120, sortable: true, renderer: Ametys.plugins.coreui.system.consolelog.ConsoleLogTool.dateRenderer, dataIndex: 'date'},
			        {stateId: 'grid-level', header: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_CONSOLELOGTOOL_COL_LEVEL'/>", width: 80, sortable: true, renderer: Ext.bind(this.levelRenderer, this), dataIndex: 'level', hideable: false},
			        {stateId: 'grid-category', header: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_CONSOLELOGTOOL_COL_CATEGORY'/>", width: 200, sortable: true, renderer: Ametys.plugins.coreui.system.consolelog.ConsoleLogTool.allRenderer, dataIndex: 'category'},
			        {stateId: 'grid-message', header: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_CONSOLELOGTOOL_COL_MESSAGE'/>", flex: 0.5, sortable: true, renderer: Ametys.plugins.coreui.system.consolelog.ConsoleLogTool.allRenderer, dataIndex: 'message', hideable: false},
			        {stateId: 'grid-details', header: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_CONSOLELOGTOOL_COL_DETAILS'/>", flex: 1, sortable: true, renderer: Ametys.plugins.coreui.system.consolelog.ConsoleLogTool.allRenderer, dataIndex: 'details'},
                    {stateId: 'grid-stacktrace', header: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_CONSOLELOGTOOL_COL_STACKTRACE'/>", flex: 1, hidden: true, renderer: Ametys.plugins.coreui.system.consolelog.ConsoleLogTool.stacktraceRendered, dataIndex: 'stacktrace'}
			    ]
			});

			return this.grid;
		},
		
		getMBSelectionInteraction: function() 
		{
		    return Ametys.tool.Tool.MB_TYPE_NOSELECTION;
		},
        
        getType: function()
        {
            return Ametys.tool.Tool.TYPE_DEVELOPER;
        }
	}
);