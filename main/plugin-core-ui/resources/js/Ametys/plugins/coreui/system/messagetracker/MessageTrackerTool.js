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
 * This tool does display the message running on the message bus.
 * @private
 */
Ext.define("Ametys.plugins.coreui.system.messagetracker.MessageTrackerTool",
	{
		extend: "Ametys.tool.Tool",
		
		statics: {
			/**
			 * This action find the unique instance of the message tracker tool, and removes all the entries
			 */
			removeAll: function()
			{
				var tool = Ametys.tool.ToolsManager.getTool("uitool-messagestracker");
				if (tool != null)
				{
					tool.store.removeAll();
				}
				else
				{
					this.getLogger().error("Cannot remove entries from unexisting tool 'uitool-messsagestracker'")
				}
			}
		},
		
		/**
		 * @property {Ext.data.ArrayStore} store The store with the messages received
		 * @private
		 */
		
		/**
		 * @property {Ext.grid.Panel} grid The grid panel displaying the messages
		 * @private
		 */
		
		constructor: function(config)
		{
			this.callParent(arguments);
			
			Ametys.message.MessageBus.on('*', this.onMessage, this);
		},
		
		createPanel: function()
		{
			this.store = Ext.create("Ext.data.ArrayStore",{
				autoDestroy: true,
				autoSync: true,
				proxy: { type: 'memory' },				
				sorters: [{property: 'fireDate', direction:'DESC'}],
				model: "Ametys.plugins.coreui.system.messagetracker.MessageTrackerTool.MessageEntry"
		    });
			
			this.grid = Ext.create("Ext.grid.Panel", { 
				stateful: true,
				stateId: this.self.getName() + "$grid",
				store: this.store,
				scrollable: true,
			    columns: [
			    	{stateId: 'grid-id', header: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_TOOL_COL_ID'/>", width: 40, sortable: true, dataIndex: 'id', hideable: false},
			        {stateId: 'grid-creationdate', header: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_TOOL_COL_CREATIONDATE'/>", width: 120, sortable: true, renderer: Ext.util.Format.dateRenderer(Ext.Date.patterns.ShortDateTime), dataIndex: 'creationDate'},
			        {stateId: 'grid-firedate', header: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_TOOL_COL_FIREDATE'/>", width: 120, sortable: true, renderer: Ext.util.Format.dateRenderer(Ext.Date.patterns.ShortDateTime), dataIndex: 'fireDate'},
			        {stateId: 'grid-type', header: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_TOOL_COL_TYPE'/>", width: 200, sortable: true, dataIndex: 'type'},
			        {stateId: 'grid-target', header: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_TOOL_COL_TARGET'/>", flex: 1, sortable: true, dataIndex: 'target'},
			        {stateId: 'grid-callstack', header: "<i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_TOOL_COL_CALLSTACK'/>", flex: 0.5, sortable: true, hidden: true, dataIndex: 'callstack'}
			    ]
			});

			return this.grid;
		},

		getMBSelectionInteraction: function() 
		{
		    return Ametys.tool.Tool.MB_TYPE_NOSELECTION;
		},
		
		/**
		 * The listener on all bus messages
		 * @param {Ametys.message.Message} message The message received
		 * @private
		 */
		onMessage: function(message)
		{
			var store = this.store;
			try
			{
				var parametersAsString = Ext.JSON.prettyEncode(message.getParameters());
				
				var record = Ext.create("Ametys.plugins.coreui.system.messagetracker.MessageTrackerTool.MessageEntry", {
					id: message.getNumber(),
					creationDate: message.getCreationDate(),
					fireDate: new Date(),
					type: "<span style='font-weight: bold'>" + message.getType() + "</span>" + (parametersAsString ? ("<br/>" + parametersAsString) : ''),
					target: this._targetsToString(message.getTargets()),
					callstack: "<div class='callstack'>" + this._stackToString(message.getCallStack()) + "</div>"
				});
				store.addSorted(record);

				// Scroll to the top of the panel
				if (this.grid.getView().getEl())
				{
					this.grid.getView().getEl().scrollTo('Top', 0, false);
				}
			}
			catch (e)
			{
				this.getLogger().error({
					message: "Cannot create the message entry",
					details: e
				});
			};
		},
		
		/**
		 * Converts a stack as string as a readable html string
		 * @param {String} stack The stack to convert. Cannot be null.
		 * @private
		 */
		_stackToString: function(stack)
		{
			var stack2 = stack.replace(/\r?\n/g, "<br/>");
			
			var linesToRemove = 5; // remove useless stack due to extjs creation process
			if (stack2.substring(0,5) == "Error")
			{
				linesToRemove++;
			}
			
			for (var i = 0; i < linesToRemove; i ++)
			{
				stack2 = stack2.substring(stack2.indexOf("<br/>") + 5);
			}
			
			var stack3 = "";
		    Ext.each(stack2.split('<br/>'), function(node, index) 
			    	{
		    			// Firefox
		    			node = node.replace(/^([^@]*)@(.*):([0-9])*$/, "<span class='method'>$1</span>@<a class='filename' href='$2' target='_blank'>$2</a>:<span class='line'>$3</span>");
		    	
		    			// IE - Chrome
		    			node = node.replace(/^.*at (.*) \((.*):([0-9]*):([0-9]*)\).*$/, "at <span class='method'>$1</span> (<a class='filename' href='$2' target='_blank'>$2</a>:<span class='line'>$3</span>:<span class='line'>$4</span>)");
		    			
		    			stack3 += node + "<br/>"
			    	}
		    );
		    return stack3.substring(0, stack3.length - 5); // remove last <br/>
		},
		
		/**
		 * Converts '@link Ametys.message.MessageTarget} to a readable string
		 * @param {Ametys.message.MessageTarget[]} targets The message parameters to convert
		 * @param {Number} [offset = 0] The offset to indent the text
		 * @private
		 */
		_targetsToString: function (targets, offset)
		{
			offset = offset || 0;
			
			var s = "";
			for (var i = 0; i < targets.length; i++)
			{
				var target = targets[i];
				
				if (offset != 0 && i != 0)
				{
					s += "<br/>";
				}
				
				for (var j = 0; j < offset; j++)
				{
					s += "&#160;&#160;&#160;&#160;";
				}
				
				s += "<span style='font-weight: bold'>" + target.getType() + "</span><br/>"
				s += Ext.JSON.prettyEncode(target.getParameters(), offset);
				
				if (target.getSubtargets().length > 0)
				{
					s += "<br/>";
				}
				s += this._targetsToString(target.getSubtargets(), offset + 1);
			}
			
			if (targets.length == 0 && offset == 0)
			{
				s = "<span style='color: #7f7f7f; font-style: italic;'><i18n:text i18n:key='PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_TOOL_NOTARGET'/></span>";
			}
			
			return s;
		}
	}
);
