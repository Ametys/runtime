/*
 *  Copyright 2012 Anyware Services
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
 * This class allow to create a message for the ametys server : it will use the dispatch generator to group requests. See createMessage for more information.
 */
Ext.define(
	"Ametys.data.ServerComm", 
	{
		singleton: true,
		
		/**
		 * @private
		 * @readonly
		 * @type String
		 * The full url to the server dispatch url
		 */
		SERVERCOMM_URL: Ametys.getPluginDirectPrefix("core") + "/servercomm/messages.xml",
		
		/**
		 * The enumeration for message priority : The message will leave now regardless of the queue and of the suspend.
		 * The request will have a 10x longer timeout.
		 * The request will be alone.
		 * Sample: creating a long search message.
		 * @type {Number}
		 * @readonly
		 */
		PRIORITY_LONG_REQUEST: -2,
		/**
		 * The enumeration for message priority : The message will leave now regardless of the queue and of the suspend.
		 * The send method will become blocking and will return the response.
		 * Callback and callback parameters are simply ignored.
		 * Sample: creating a target message.
		 * @type {Number}
		 * @readonly
		 */
		PRIORITY_SYNCHRONOUS: -1,
		/**
		 * The enumeration for message priority : The message needs to leave as soon as possible.
		 * Sample: saving any user modifications.
		 * @type {Number}
		 * @readonly  
		 */
		PRIORITY_MAJOR: 0,
		/**
		 * The enumeration for message priority : The message can be delayed.
		 * Sample : updating a minor view.
		 * @type {Number}
		 * @readonly  
		 */
		PRIORITY_NORMAL: 10,
		/**
		 * The enumeration for message priority : The message is for background use.
		 * Sample : ping, preferences save.
		 * @type {Number}
		 * @readonly  
		 */
		PRIORITY_MINOR: 40,
		
		/**
		 * @private
		 * @property {Object} _messages The waiting messages. See {@link #send} method to see the message object
		 */
		_messages: [],

		/**
		 * @private
		 * @property {Object} _observer The ServerComm observer that will receive events on each action. There is no setter, you have to set it directly.
		 */
		_observer: {},

		/**
		 * @private
		 * @property {Number} _suspended The number of times the communication was suspended. 0 means communication are not suspended. Cannot be negative.
		 */
		_suspended: 0,

		/**
		 * @private
		 * @property {Number} _sendTask The time out value (return by setTimeout)
		 */
		_sendTask: null,

		/**
		 * @private
		 * @proptery {long} _nextTimer The date as long when the next timer should stop. Null if no timer.
		 */
		_nextTimer: null,

		/**
		 * @private
		 * @type {Object} Association of id and send options ; to remember while timeout
		 */
		_runningRequests: {},
		
		/**
		 * @private
		 * @type {Number} The index for the next running request.
		 */
		_runningRequestsIndex: 0,
		
		/**
		 * Suspend the communication with the server.
		 * Use it when you know that several component will add messages with a major priority to do a single request
		 * Do not forget to call the restart method to effectively send messages.
		 * Be sure to finally call the {@link #restart} method.
		 */
		suspend: function()
		{
			this._suspended++;
		},

		/**
		 * Restart suspended communications with server.
		 * Do not call this method if you do not have call the {@link #suspend} one before.
		 */
		restart: function()
		{
			if (this._suspended == 0)
			{
				throw "Servercomm#restart method has been called but communications where not suspended";
			}

			this._suspended--;
			
			if (this._suspended == 0 &amp;&amp; this._nextTimer != null &amp;&amp; this._nextTimer &lt; new Date().getTime())
			{
				this._sendMessages();
			}
		},
		
		/**
		 * Add a message to the 'to send' list of message. Depending on its priority, it will be send immediatly or later.
		 * @param {String} pluginOrWorkspace The name of the server plugin or workpace targeted. Can be null to send to current workspace. Prefix by '_' to target a specific workspace.
		 * @param {String} url The url on the server relative to the plugin
		 * @param {Object} parameters The parameters to send to the server (Map&lt;String, String&gt;)
		 * @param {Number} priority The priority of the message. Use ServerComm.PRIORITY_* constants.
		 * @param {Function} callback The function to call when the message will come back. 
		 * @param {Object} callback.response Will be the xml parent node of the response. This node can be null or empty on fatal error. An attribute 'code' is available on this node with the http code. This reponse has an extra method 'getText' that get the text from a node in parameter of the response.
		 * @param {Object[]} callback.callbackarguments Is the 'callbackarguments' array
		 * @param {Object} callbackscope The scope of the function call. Optionnal.
		 * @param {String[]} callbackarguments An array of string that will be given as arguments of the callback. Optionnal
		 * @param {String} responseType Can be "xml" (default) to have a xml response, "text" to have a single text node response or "xml2text" to have a single text node response where xml prologue as text is removed
		 * @return {Object} The XHR object containing the response data for a synchronous priority message or null for other priorities.
		 */
		send: function(pluginOrWorkspace, url, parameters, priority, callback, callbackscope, callbackarguments, responseType)
		{
			var message = {
				pluginOrWorkspace: pluginOrWorkspace,
				url: url,
				parameters: parameters,
				priority: priority,
				callback: callback,
				callbackscope: callbackscope,
				callbackarguments: callbackarguments,
				responseType: responseType == null ? "xml" : responseType,
						
				toRequest: function() {
					var m = {};
					
					m.pluginOrWorkspace = this.pluginOrWorkspace || '_' + Ametys.WORKSPACE_NAME;
					m.responseType = this.responseType;
					m.url = this.url;
					m.parameters = this.parameters;
					
					return m;
				}
			};
			
			if (message.priority == Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS)
			{
				return this._sendSynchronousMessage(message.toRequest());
			}
			else if (message.priority == Ametys.data.ServerComm.PRIORITY_LONG_REQUEST)
			{
				this._sendMessages(message);
			}
			else
			{
				// add the message to the list
				this._messages.push(message);
				
				// compute delay wanted and ring time associater (add a 50 milliseconds delay to try to cumulate several messages)
				var delay = 1000 * message.getPriority() + 50;
				var ringTime = new Date().getTime() + delay;
			
				// if the current timer rings after the wanted time (at 20 milliseconds near)
				if (this._nextTimer == null || ringTime &lt; this._nextTimer - 20)
				{
					this._nextTimer = ringTime;
					this._sendTask = setTimeout(function () { Ametys.data.ServerComm._sendMessages(); }, delay);
				}
				return null;
			}
		},
		
		/**
		 * @private
		 * Send a synchronous message
		 * @param {Object} messageRequest An object returned by ServerMessage.toRequest
		 * @return {Object} The XHR object containing the response data.
		 */
		_sendSynchronousMessage: function(messageRequest)
		{
			if (typeof this._observer.onSyncRequestDeparture == "function")
			{
				try
				{ 
					this._observer.onSyncRequestDeparture(messageRequest);
				}
				catch (e)
				{
					alert("Exception in Ametys.data.ServerComm._observer.onSyncRequestDeparture: " + e);
				}
			}

			var conn = null;
			
			try
			{
				conn = Ext.Ajax.request({url: Ametys.data.ServerComm.SERVERCOMM_URL, params: "content=" + encodeURIComponent(Ext.JSON.encode({0: messageRequest})), async: false});
			}
			catch(e)
			{
				if (typeof this._observer.onSyncRequestArrival == "function")
				{
					try
					{ 
						this._observer.onRequestArrival(messageRequest, 2);
					}
					catch (e)
					{
						alert("Exception in Ametys.data.ServerComm._observer.onSyncRequestArrival: " + e);
					}
				}
				
				if (confirm("<i18n:text i18n:key="KERNEL_SERVERCOMM_LISTENERREQUEST_FAILED_UNAVAILABLE"/>") &amp;&amp; !this._off)
				{
					return this._sendSynchronousMessage(messageRequest);
				}
				else
				{
					Ametys.shutdown();
					return null;
				}
			}
		    
			if (typeof this._observer.onSyncRequestArrival == "function")
			{
				try
				{ 
					this._observer.onRequestArrival(messageRequest, 0);
				}
				catch (e)
				{
					alert("Exception in Ametys.data.ServerComm._observer.onSyncRequestArrival: " + e);
				}
			}
			
			if (conn.responseXML == null)
			{

				if (confirm("<i18n:text i18n:key="KERNEL_SERVERCOMM_NOTXML_DESC"/>") &amp;&amp; !this._off)
				{
					return this._sendSynchronousMessage(messageRequest);
				}
				else
				{
					this._shutdown();
					return null;
				}
			}
			else
			{
				return Ext.dom.Query.selectNode("/responses/response[@id='0']");
			}
		},
		

		/**
		 * @private
		 * Shut down all running requests. And prevent any new request.
		 */
		_shutdown: function(m)
		{
			this._off = true;
			this.suspend();
			
			// cancel running requests
			for (var requestOptions in Ametys.data.ServerComm._runningRequests)
			{
				if (requestOptions._transactionId)
				{
					this._abort(requestOptions, true);
				}
			}
		},
		
		/**
		 * Send the waiting messages to the server
		 * @param m A optional message. If null the method will empty the queue, else it will send only this message.
		 * @private
		 */
		_sendMessages: function(m)
		{
			var timeout = Ametys.data.ServerComm.TimeoutDialog.TIMEOUT;
			
			if (m == null)
			{
				clearTimeout(this._sendTask);
				this._sendTask = null;
				this._nextTimer = null;
				
				if (this._suspended &gt; 0)
				{
					// communication is suspended - messages will be sent as soon as possible
					return;
				}
				
				if (this._messages.length == 0)
				{
					return;
				}
			
				// Effectively send messages
				var parameters = {};
				var count = 0;
				for (var i = 0; i &lt; this._messages.length; i++)
				{
					var message = this._messages[i];
					parameters[count++] = message.toRequest();
				}
			}
			else
			{
				timeout *= 600;
				var parameters = {0: m.toRequest()};
			}
			
			var sendOptions = {};
			var index = Ametys.data.ServerComm._runningRequestsIndex ++;
			Ametys.data.ServerComm._runningRequests[index] = sendOptions;
			
			sendOptions.url = Ametys.data.ServerComm.SERVERCOMM_URL;
			sendOptions.success = this._onRequestComplete;
			sendOptions.failure = this._onRequestFailure;
			sendOptions.scope = this;
			sendOptions.params = "content=" + encodeURIComponent(Ext.util.JSON.encode(parameters)) 
			sendOptions.messages = m != null ? [m] : this._messages;
			sendOptions._timeoutIndex = index;
			sendOptions._timeout = window.setTimeout("Ametys.data.ServerComm._onRequestTimeout ('" + index + "', " + timeout + ");", timeout);

			if (m == null)
			{
				this._messages = [];
			}

			sendOptions._transactionId = Ext.Ajax.request(sendOptions);

			if (typeof this._observer.onRequestDeparture == "function")
			{
				try
				{
					this._observer.onRequestDeparture(sendOptions);
				}
				catch (e)
				{
					alert("Exception in Ametys.data.ServerComm._observer.onRequestDeparture: " + e);
				}
			}
		},
		
		/**
		 * @private
		 * Abort a request (note that the server will still execute it but the result will be discard)
		 * @param {Object} options The send options
		 * @param {boolean} silently Do not call any listeners. Default to false.
		 */
		_abort: function(options, silently)
		{
			Ext.Ajax.abort(options._transactionId);

			if (silently !== false &amp;&amp; typeof this._observer.onRequestArrival == "function")
			{
				try
				{ 
					this._observer.onRequestArrival(options, 1);
				}
				catch (e)
				{
					alert("Exception in Ametys.data.ServerComm._observer.onRequestArrival (1): " + e);
				}
			}

			this._cancelTimeout(options);

			if (silently !== false)
			{
				// for each message call the handler
				for (var i = 0; i &lt; options.messages.length; i++)
				{
						var message = options.messages[i];
			
						try
						{
							message.getCallback().apply(message.getCallbackScope(), [null, message.getCallbackArguments()]);
						}
						catch (e)
						{
							function throwException(e) 
							{ 
								throw e; 
							}
							throwException.defer(1, this, [e]);
			
							Ametys.log.ErrorDialog.display("<i18n:text i18n:key='KERNEL_SERVERCOMM_ERROR_TITLE'/>",
									"<i18n:text i18n:key='KERNEL_SERVERCOMM_ERROR_DESC'/>",
			                        e + '',
			                        "Ametys.data.ServerComm");
						}
				}
			}
		},
		
		/**
		 * @private
		 * When a request times out
		 * @param {Number} index The index of the request in the _runningRequest map.
		 * @param {Number} timeout The timeout value
		 */
		_onRequestTimeout: function(index, timeout)
		{
			var sendOptions = Ametys.data.ServerComm._runningRequests[index];
			sendOptions._timeout = null;
			sendOptions._timeoutDialog = new Ametys.data.ServerComm.TimeoutDialog(sendOptions.params, index, timeout);
		},

		/**
		 * @private
		 * Cancel the timeout and kill the timeout dialogue
		 */
		_cancelTimeout: function(options)
		{
			if (options._timeout != null)
			{
				window.clearTimeout(options._timeout);
			}
			if (options._timeoutDialog != null)
			{
				options._timeoutDialog.kill();
				options._timeoutDialog = null;
			}
			delete Ametys.data.ServerComm._runningRequests[options._timeoutIndex];
		},

		/**
		 * @private
		 * Listener on requests that succeed
		 * @param {Object} response The XHR object containing the response data.
		 * @param {Object} options The arguments of the request method call
		 */
		_onRequestComplete: function(response, options)
		{
			if (typeof this._observer.onRequestArrival == "function")
			{
				try
				{ 
					this._observer.onRequestArrival(options, 0);
				}
				catch (e)
				{
					alert("Exception in Ametys.data.ServerComm._observer.onRequestArrival (0): " + e);
				}
			}

			this._cancelTimeout(options);
			
			if (response.responseXML == null)
			{

				if (confirm("<i18n:text i18n:key="KERNEL_SERVERCOMM_NOTXML_DESC"/>"))
				{
					this._sendMessages(options.messages);
				}
				else
				{
					Ametys.shutdown();
				}
			}
			else
			{
				// for each message call the handler
				for (var i = 0; i &lt; options.messages.length; i++)
				{
						var message = options.messages[i];
			
						var node = Ext.dom.Query.selectNode("/responses/response[@id='" + i + "']", response.responseXML);
						try
						{
							message.getCallback().apply(message.getCallbackScope(), [node, message.getCallbackArguments()]);
						}
						catch (e)
						{
							function throwException(e) 
							{ 
								throw e; 
							}
							throwException.defer(1, this, [e]);
			
							Ametys.log.ErrorDialog.display("<i18n:text i18n:key='KERNEL_SERVERCOMM_ERROR_TITLE'/>",
									"<i18n:text i18n:key='KERNEL_SERVERCOMM_ERROR_DESC'/>",
			                        e + '',
			                        "Ametys.data.ServerComm");
						}
				}
			}
		},

		/**
		 * @private
		 * Listener on requests that failed
		 * @param {Object} response The XHR object containing the response data
		 * @param {Object} options The arguments of the request method call
		 */
		_onRequestFailure: function(response, options)
		{
			this._cancelTimeout(options);
			
			if (typeof this._observer.onRequestArrival == "function")
			{
				try
				{ 
					this._observer.onRequestArrival(options, 2);
				}
				catch (e)
				{
					alert("Exception in Ametys.data.ServerComm._observer.onRequestArrival (2): " + e);
				}
			}

			if (confirm("<i18n:text i18n:key='KERNEL_SERVERCOMM_LISTENERREQUEST_FAILED_UNAVAILABLE'/>") &amp;&amp; !this._off)
			{
				Ext.Ajax.request(options);
			}
			else
			{
				Ametys.shutdown();
			}
		},
		
		/**
		 * Call this method to handle a bad response for you. Test for a null response or a 404 or a 500.
		 * @param {String} message The error message to display if the response is bad
		 * @param {Object} response The response received
		 * @param {String} category The log category. Can be null to avoid logging.
		 * @return {boolean} True if a bad request was found (and you should abord your treatment)
		 */
		handleBadResponse: function(message, response, category)
		{
			if (response == null || response.getAttribute("code") == "500" || response.getAttribute("code") == "404")
			{
				if (response == null)
				{
					Ametys.log.ErrorDialog.display(
							"<i18n:text i18n:key='KERNEL_SERVERCOMM_BADRESPONSE_TITLE'/>", 
							message,
							"<i18n:text i18n:key='KERNEL_SERVERCOMM_BADRESPONSE_DESC'/>",
							category
					);
				}
				else
				{
					var intMsg = Ext.dom.Query.selectValue("message", response);
					var hasMsg = intMsg != null &amp;&amp; intMsg != "";
					var intStk = Ext.dom.Query.selectValue("stacktrace", respoonse);
					var hasStk = intStk != null &amp;&amp; intStk != "";
						
					Ametys.log.ErrorDialog.display(
							"<i18n:text i18n:key='KERNEL_SERVERCOMM_SERVER_FAILED_DESC'/>" + response.getAttribute("code") + ")", 
							message,
							(hasMsg ? intMsg : "")
							+ (hasMsg &amp;&amp; hasStk ? "&lt;br/&gt;&lt;br/&gt;" : "")
							+ (hasStk ? intStk : ""),
							category
					);
				}
				return true;
			}
			else
			{
				return false;
			}
		}
	}
);

