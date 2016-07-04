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
 * This tool does display the messages sent to the server.
 * @private
 */
Ext.define("Ametys.plugins.coreui.system.requesttracker.RequestTrackerTool",
	{
		extend: "Ametys.tool.Tool",
		
		statics: {
			/**
			 * This action find the unique instance of the request tracker tool, and removes all the entries
			 */
			removeAll: function()
			{
				var tool = Ametys.tool.ToolsManager.getTool("uitool-requeststracker");
				if (tool != null)
				{
					tool.store.removeAll();
				}
				else
				{
					this.getLogger().error("Cannot remove entries from unexisting tool 'uitool-requeststracker'");
				}
			}
		},
		
		/**
		 * @property {Number} _currentId The current identifier. Each request displayed will show an identifier
		 * @private
		 */
		
		/**
		 * @property {Ext.data.ArrayStore} store The store with the requests sent
		 * @private
		 */
		
		/**
		 * @property {Ext.grid.Panel} grid The grid panel displaying the requests
		 * @private
		 */
		
		constructor: function()
		{
			this.callParent(arguments);
			this._currentId = 1;
			
			Ametys.data.ServerComm._observer = this;
		},
		
		createPanel: function()
		{
			this.store = Ext.create("Ext.data.ArrayStore",{
				sorters: [{property: 'id', direction:'DESC'}],
				model: "Ametys.plugins.coreui.system.requesttracker.RequestTrackerTool.RequestEntry",
				autoDestroy: true,
				autoSync: true,
				proxy: { type: 'memory' }
		    });
			
			this.grid = Ext.create("Ext.grid.Panel", { 
                minHeight: 60,
                flex: 0.3,
				stateful: true,
				stateId: this.self.getName() + "$grid",
				store: this.store,
				scrollable: true,
                border: true,
			    columns: [
			        {stateId: 'grid-id', header: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_COL_ID}}", width: 55, sortable: true, dataIndex: 'id', hideable: false},
			        {stateId: 'grid-type', header: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_COL_TYPE}}", width: 85, sortable: true, dataIndex: 'type'},
			        {stateId: 'grid-date', header: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_COL_DATE}}", width: 130, sortable: true, renderer: Ext.util.Format.dateRenderer(Ext.Date.patterns.ShortDateTime), dataIndex: 'date'},
			        {stateId: 'grid-duration', header: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_COL_DURATION}}", width: 65, sortable: true, dataIndex: 'duration'},
                    {stateId: 'grid-errors', header: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_COL_ERRORS}}", width: 75, sortable: true, dataIndex: 'errors'},
			        {stateId: 'grid-size', header: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_COL_SIZE}}", width: 60, sortable: true, dataIndex: 'size'},
			        {stateId: 'grid-return', header: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_COL_RETURN}}", width: 70, sortable: true, dataIndex: 'return'}
			    ],
                
                viewConfig: { 
                    getRowClass: function(record) { 
                        return record.get('errors') > 0 ? (record.get('size') == record.get('errors') ? 'request-errors' : 'request-warnings') : ''; 
                    } 
                }, 
			    
			    listeners: {'selectionchange': Ext.bind(this._onSelectRequest, this)}
			});

			
			this.msgStore = Ext.create("Ext.data.ArrayStore",{
				sorters: [{property: 'id', direction:'DESC'}],
				model: "Ametys.plugins.coreui.system.requesttracker.RequestTrackerTool.MessageEntry",
				autoDestroy: true,
				autoSync: true,
				proxy: { type: 'memory' }
		    });
			
			this.msgGrid = Ext.create("Ext.grid.Panel", { 
				minHeight: 50,
                flex: 0.7,
				stateful: true,
				stateId: this.self.getName() + "$msggrid",
                border: true,
				split: true,
				store: this.msgStore,
				scrollable: true,
			    columns: [
			        {stateId: 'msgrid-id', header: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_ID}}", width: 55, sortable: true, dataIndex: 'id', hideable: false},
			        {stateId: 'msgrid-readabletype', header: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_READABLE_TYPE}}", width: 70, sortable: true, dataIndex: 'readableCallType', renderer: Ext.bind(this._renderCallType, this)},
			        {stateId: 'msgrid-readablevalue', header: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_READABLE_VALUE}}", width: 250, sortable: true, dataIndex: 'readableCallValue'},
			        {stateId: 'msgrid-plugin', header: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_PLUGIN}}", width: 70, sortable: true, hidden: true, dataIndex: 'plugin'},
			        {stateId: 'msgrid-workspace', header: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_WORKSPACE}}", width: 95, sortable: true, hidden: true, dataIndex: 'workspace'},
			        {stateId: 'msgrid-url', header: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_URL}}", width: 180, sortable: true, hidden: true, dataIndex: 'url'},
			        {stateId: 'msgrid-ccrole', header: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_CCROLE}}", width: 180, sortable: true, hidden: true, dataIndex: 'clientCallRole'},
			        {stateId: 'msgrid-ccid', header: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_CCID}}", width: 180, sortable: true, hidden: true, dataIndex: 'clientCallId'},
			        {stateId: 'msgrid-ccmethod', header: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_CCMETHOD}}", width: 180, sortable: true, hidden: true, dataIndex: 'clientCallMethod'},
			        {stateId: 'msgrid-priority', header: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_PRIORITY}}", width: 70, sortable: true, dataIndex: 'priority'},
                    {stateId: 'msgrid-status', header: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_STATUS}}", width: 75, sortable: true, dataIndex: 'status'},
			        {stateId: 'msgrid-type', header: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_TYPE}}", width: 75, sortable: true, dataIndex: 'type'}
			    ],
                
                viewConfig: { 
                    getRowClass: function(record) { 
                        return record.get('status') != '200' ? 'request-errors' : ''; 
                    } 
                }, 
			    
			    listeners: {'selectionchange': Ext.bind(this._onSelectMessage, this) }
			});
			
			this.leftPanel = Ext.create("Ext.Container", {
				stateful: true,
				stateId: this.self.getName() + "$leftPanel",
				split: true,
                layout: { 
                    type: 'vbox',
                    align: 'stretch'
                },
				minWidth: 100,
                flex: 0.5,
				items: [ this.grid, this.msgGrid ]
			});
			
			this.rightPanel = Ext.create("Ext.Component", {
                stateId: this.self.getName() + "$rightPanel",
				scrollable: true,
                minWidth: 100,
                split: true,
                border: true,
                flex: 0.5,
                ui: 'panel',
                cls: 'a-panel-text',
				defaultHtml: "{{i18n PLUGINS_CORE_UI_TOOLS_REQUESTS_TRACKER_MESSAGE}}",
				html: "{{i18n PLUGINS_CORE_UI_TOOLS_REQUESTS_TRACKER_MESSAGE}}"
			});
			
			this._messageTpl = new Ext.Template(
					"<b>{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_DISPLAYMESSAGE_URL}}</b> : ",
					"{url}<br/><br/>",
					"<b>{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_DISPLAYMESSAGE_PARAMETERS}}</b> : ",
					"<code class='request-tracker'>{parameters}</code><br/>",
					"<b>{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_DISPLAYMESSAGE_RESPONSE}}</b> : ",
					"<code class='request-tracker'>{response}</code><br/>",
                    "<b>{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_DISPLAYMESSAGE_CALLSTACK}}</b> : <br/>",
                    "{callstack}"
			);
            
			return Ext.create("Ext.container.Container", {
				layout: { 
                    type: 'hbox',
                    align: 'stretch'
                },
                cls: 'uitool-requesttracker',
				items: [ this.leftPanel, this.rightPanel ]
			});
		},
		
		/**
		 * @private
		 * The renderer for the call column
	     * @param {Object} value The data value for the current cell
	     * @param {Object} metaData A collection of metadata about the current cell; can be used or modified by the renderer. Recognized properties are: tdCls, tdAttr, and style.
	     * @param {Ext.data.Model} record The record for the current row
	     * @param {Number} rowIndex The index of the current row
	     * @param {Number} colIndex The index of the current column
	     * @param {Ext.data.Store} store The data store
	     * @param {Ext.view.View} view The current view
	     * @return {String} The HTML string to be rendered.
	     */
		_renderCallType: function(value, metaData, record, rowIndex, colIndex, store, view)
		{
			if (record.get('readableCallType'))
			{
				return "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_READABLE_VAL1}}";
			}
			else
			{
				return "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_READABLE_VAL2}}";
			}
		},
		
		getMBSelectionInteraction: function() 
		{
		    return Ametys.tool.Tool.MB_TYPE_NOSELECTION;
		},
		        
        getType: function()
        {
            return Ametys.tool.Tool.TYPE_DEVELOPER;
        },
        
		/**
		 * Listener on the main grid panel, when selecting a record
		 * @param {Ext.selection.RowModel} selModel The selection mode
		 * @param {Ext.data.Model[]} records The record selected
		 * @param {Object} eOpts The options object passed to Ext.util.Observable.addListener.
		 * @private
		 */
		_onSelectRequest: function (selModel, records, eOpts)
		{
			this.msgStore.removeAll();
			
			function getClientCallInfo(message)
			{
				if (message && message.url == "client-call" && message.plugin == "core-ui")
				{
					return {
						role: message.parameters ? message.parameters.role : null,
						id: message.parameters ? message.parameters.id : null,
						method: message.parameters ? message.parameters.methodName : null
					}
				}
				else
				{
					return {
						role: null,
						id: null
					}
				}
			}
			
			var messages = records.length > 0 ? records[0].get("messages") : [];
			var response = records.length > 0 ? records[0].get("response") : null;
			for (var i = 0; i < messages.length; i++)
			{
				var message = messages[i];
				var clientCallInfo = getClientCallInfo(message);
				if (message.priority != null)
				{
					var priority = "";
					switch (message.priority)
					{
						case Ametys.data.ServerComm.PRIORITY_MAJOR: priority = "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_PRIORITY_MAJOR}}"; break;
						case Ametys.data.ServerComm.PRIORITY_NORMAL: priority = "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_PRIORITY_NORMAL}}"; break;
						case Ametys.data.ServerComm.PRIORITY_MINOR: priority = "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_PRIORITY_MINOR}}"; break;
						case Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS: priority = "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_PRIORITY_SYNCHRONOUS}}"; break;
						case Ametys.data.ServerComm.PRIORITY_LONG_REQUEST: priority = "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_PRIORITY_LONG_REQUEST}}"; break;
						default: priority = message.priority;
					}
					
					var r = Ext.create("Ametys.plugins.coreui.system.requesttracker.RequestTrackerTool.MessageEntry", {
						id: i,
						plugin: message.plugin,
						workspace: message.workspace,
						url: this._removeGetParameters(message.url),
						priority: priority,
						type: message.responseType,
						clientCallRole: clientCallInfo.role,
						clientCallId: clientCallInfo.id,
						clientCallMethod: clientCallInfo.method,
						message: message,
						response: response,
                        status: response && response.responseXML ? Ext.dom.Query.selectNode("/responses/response[@id='" + i + "']", response.responseXML).getAttribute("code") : "500",
                        callstack: message.callstack
					});
					this.msgStore.addSorted(r);
				}
				else
				{
					var r = Ext.create("Ametys.plugins.coreui.system.requesttracker.RequestTrackerTool.MessageEntry", {
						id: 0,
						plugin: message.plugin,
						workspace: message.workspace,
						url: this._removeGetParameters(message.url),
						priority: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_MESSAGE_COL_PRIORITY_SYNCHRONOUS}}",
						type: message.responseType,
						clientCallRole: clientCallInfo.role,
						clientCallId: clientCallInfo.id,
						clientCallMethod: clientCallInfo.method,
						message: message,
						response: response,
                        status: response && response.responseXML ? Ext.dom.Query.selectNode("/responses/response[@id='0']", response.responseXML).getAttribute("code") : "500",
                        callstack: message.callstack
					});
					this.msgStore.addSorted(r);
				}
			}
		},
		
		/**
		 * Listener on the south grid panel (details in a request), when selecting a record
		 * @param {Ext.selection.RowModel} selModel The selection mode
		 * @param {Ext.data.Model} records The record selected
		 * @param {Object} eOpts The options object passed to Ext.util.Observable.addListener.
		 * @private
		 */
		_onSelectMessage: function (selModel, records, eOpts)
		{
			if (records.length > 0)
			{
				var record = records[0];
				var id = record.getId();
				var message = record.get("message");
				var response = record.get("response");
				
				var parametersAsString = Ext.JSON.prettyEncode(message.parameters);
				var responseAsString = this._responseToString(response, id); 
                var stacktraceAsString = Ext.String.stacktraceToHTML(message.callstack, 1);
				this.rightPanel.update(this._messageTpl.applyTemplate({url: message.url, parameters: parametersAsString, response: responseAsString, callstack: stacktraceAsString}));
			}
			else
			{
				this.rightPanel.update(this.rightPanel.defaultHtml);
			}
		},
		
		/**
		 * Display a part of the response as a readable string
		 * @param {Object} response The XMLHTTPResponse
		 * @param {String} id The id of the part of the response to extract and display
		 * @private
		 */
		_responseToString: function(response, id)
		{
			if (response == null)
			{
				return "null";
			}
			else
			{
				function xmlstringToHTML(xmlstring)
				{
					function escape(htmlstring)
					{
						return htmlstring.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/\r\n/g, '<br/>').replace(/&amp;#160;/g, '&#160;');
					}
					function escapeOpening(tagstring)
					{
						return "<span class='tag'>" + tagstring.replace(/( [^=]+)(=)(\"[^"]*\")/g, "<span class='attr-name'>$1</span><span class='attr-eq'>$2</span><span class='attr-value'>$3</span>") + "</span>";
					}
					function escapeText(textstring, pad)
					{
						var json = Ext.JSON.decode(textstring, true)
						if (json != null)
						{
							var ppad = parseInt(pad/ 2) + 1;
							textstring = Ext.JSON.prettyEncode(json, ppad);
							if (textstring.indexOf("<br/>") != -1)
							{
								var padding = "";
						        for (var i = 0; i < ppad; i++) 
						        {
						            padding += '&#160;&#160;&#160;&#160;';
						        }
								textstring = "<br/>" + padding + textstring + "<br/>";
							}
						}
						
						return "<span class='text'>" + textstring + "</span>";
					}
					function escapeClosing(tagstring)
					{
						return "<span class='tag'>" + tagstring + "</span>";
					}
					
				    var formatted = '';
				    xml = xmlstring.replace(/(>)(<)(\/*)/g, '$1\r\n$2$3');
				    var pad = 0;
				    Ext.each(xml.split('\r\n'), function(node, index) 
				    	{
					        var indent = 0;
					        if (node.match( /.+<\/\w[^>]*>$/ )) 
					        {
					        	// full tag <test>foo</test>
					        	node = escape(node);
					        	
					        	var i = node.indexOf('>');
					        	var j = node.indexOf('&lt;', i+1);
					        	node = escapeOpening(node.substring(0, i+1)) 
					        			+ escapeText(node.substring(i+1, j), pad) 
					        			+ escapeClosing(node.substring(j));
					            indent = 0;
					        } 
					        else if (node.match( /^<\/\w/ )) 
					        {
					        	// just a closing tag </test>
					        	node = escape(node);
					        	node = escapeClosing(node);
					            if (pad != 0) 
					            {
					                pad -= 1;
					            }
					        } 
					        else if (node.match( /^<\w[^>]*[^\/]>.*$/ )) 
					        {
					        	// just an opening tag <test attr="1">
					        	node = escape(node);
					        	node = escapeOpening(node);
					            indent = 1;
					        } 
					        else 
					        {
					        	// autoclosing tags <test/>
					        	node = escape(node);
					        	node = escapeOpening(node);
					            indent = 0;
					        }
					 
					        var padding = '';
					        for (var i = 0; i < pad; i++) 
					        {
					            padding += '&#160;&#160;';
					        }
					 
					        formatted += padding + node + '<br/>';
					        pad += indent;
				    	}
				    );
				 
				    return formatted;
				}
				
				var node = Ext.dom.Query.selectNode("/responses/response[@id='" + id + "']", response.responseXML);
				if (node.outerHTML)
				{
					return xmlstringToHTML(node.outerHTML);
				}
				else
				{
					try 
					{
						// Gecko- and Webkit-based browsers (Firefox, Chrome), Opera.
						return xmlstringToHTML((new XMLSerializer()).serializeToString(node));
					}
					catch (e) 
					{
						try 
						{
							// Internet Explorer.
							return xmlstringToHTML(xmlNode.xml);
						}
						catch (e) {  
							//Other browsers without XML Serializer
							return "..."
						}
					}
				}
			}
		},
		
		
		/**
		 * Remove the GET request parameters from the url
		 * @param url The url 
		 * @returns The request url without the GET paramters
		 * @private
		 */
		_removeGetParameters: function (url)
		{
			var i = url.indexOf("?");
			if (i == -1) return url;
			else return url.substring(0, i);
		},
		
		/**
		 * Observer of the Ametys.data.ServerComm for sent request
		 * @param {Object} sendOptions The options used in Ametys.data.ServerComm#_sendMessages 
		 */
		onRequestDeparture: function (sendOptions)
		{
			var store = this.store;
			try
			{
				var id = this._currentId++;
				
				var record = Ext.create("Ametys.plugins.coreui.system.requesttracker.RequestTrackerTool.RequestEntry", {
					id: id,
					type: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_COL_TYPE_ASYNC}}",
					date: new Date(),
					duration: "...",
					"return": "...",
					size: sendOptions.messages.length,
                    errors: null,
					messages: sendOptions.messages,
					response: null
				});
				store.addSorted(record);
				// Scroll to the top of the panel
				this.grid.getView().getEl().scrollTo('Top', 0, false);
				
				sendOptions.observerId = id;
			}
			catch (e)
			{
				this.getLogger().error({
					message: "Cannot create the request entry",
					details: e
				})
			};
		},
		
		/**
		 * Observer of the Ametys.data.ServerComm for request back
		 * @param {Object} sendOptions The options used in Ametys.data.ServerComm#_sendMessages 
		 * @param {Number} responseType 0 for success, 1 for canceled and 2 for failure
		 * @param {Object} [response] The XMLHTTPResponse when available
		 */
		onRequestArrival: function (sendOptions, responseType, response)
		{
			var store = this.store;
			try
			{
				var record = this.store.query("id", sendOptions.observerId).getAt(0);
                if (record == null)
                {
                    // Let's ignore this request arrival, because we were not listening when it started 
                    return;
                }
				record.set("duration", (new Date().getTime() - record.get("date").getTime()) / 1000.0);
                record.set("errors", response && response.responseXML ? Ext.dom.Query.select("/responses/response[@code!='200']", response.responseXML).length : record.get("size"));
				record.set("response", response);
				switch (responseType)
				{
					case 0:
						record.set("return", "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_COL_RETURN_OK}}"); 
						break;
					case 1:
						record.set("return", "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_COL_RETURN_CANCELED}}");
						break;
					case 2:
						record.set("return", "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_COL_RETURN_FAILURE}}");
						break;
				}
				record.commit();
			}
			catch (e)
			{
				this.getLogger().error({
					message: "Cannot update the request entry",
					details: e
				})
			};
		},

		/**
		 * Observer of the Ametys.data.ServerComm for sent synchronous request
		 * @param {Object} message The message sent. Argument of Ametys.data.ServerComm#_sendSynchronousMessage
		 */
		onSyncRequestDeparture: function (message)
		{
			var store = this.store;
			try
			{
				var id = this._currentId++;
				
				window.setTimeout(function() {
					var record = Ext.create("Ametys.plugins.coreui.system.requesttracker.RequestTrackerTool.RequestEntry", {
						id: id,
						type: "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_COL_TYPE_SYNC}}",
						date: new Date(),
						duration: "...",
						"return": "...",
						size: 1,
                        errors: null,
						messages: [message]
					});
					store.addSorted(record);
					
				}, 1);
				message.observerId = id;
			}
			catch (e)
			{
				this.getLogger().error({
					message: "Cannot create the request entry",
					details: e
				})
			};
		},
		
		/**
		 * Observer of the Ametys.data.ServerComm back from a synchronous request
		 * @param {Object} message The message sent. Argument of Ametys.data.ServerComm#_sendSynchronousMessage
		 * @param {Number} responseType 0 for success and 2 for failure
		 * @param {Object} [response] The XMLHTTPResponse when available
		 */
		onSyncRequestArrival: function (message, responseType, response)
		{
			var store = this.store;
			try
			{
				window.setTimeout(function() {
					var record = store.query("id", message.observerId).getAt(0);
					record.set("duration", (new Date().getTime() - record.get("date").getTime()) / 1000.0);
                    record.set("errors", responseType == 0 ? 0 : 1);
					record.set("response", response);
					switch (responseType)
					{
						case 0:
							record.set("return", "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_COL_RETURN_OK}}"); 
							break;
						case 2:
							record.set("return", "{{i18n PLUGINS_CORE_UI_REQUESTSTRACKER_TOOL_COL_RETURN_FAILURE}}");
							break;
					}
					record.commit();
				}, 1);
			}
			catch (e)
			{
				this.getLogger().error({
					message: "Cannot update the request entry",
					details: e
				})
			};
		},
		
		onClose: function ()
		{
			this.callParent(arguments);
			Ametys.data.ServerComm._observer = {};
		}
	}
);
