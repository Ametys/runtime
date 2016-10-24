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
 * This class allow to create a message for the ametys server : it will use the dispatch generator to group requests. 
 * 
 * See #send for more information on sending request to an url:
 * 
 * 		Ametys.data.ServerComm.send({
 * 			plugin: 'cms',
 *			url: 'contents/get-info',
 *			parameters: {ids: ids}, 
 *			priority: Ametys.data.ServerComm.PRIORITY_MAJOR, 
 *			callback: {
 *				handler: this._getContentsCB,
 *				scope: this,
 *				arguments: [callback]
 *			},
 *			responseType: 'text'
 *      });
 *      
 * See #callMethod for more information on directly calling a java method:
 * 
 *   	Ametys.data.ServerComm.callMethod({
 *    		role: "org.ametys.core.ui.RibbonControlsManager",
 *    		id: "org.ametys.cms.content.Delete",
 *    		methodName: "getStatus",
 *    		parameters: [
 *        		['content://12345678-1234-1234']
 *    		],
 *    		callback: {
 *        		handler: function(a) { alert(a) },
 *              ignoreOnError: false
 *    		},
 *    		waitMessage: true,
 *    		errorMessage: { msg: 'An error occured' }
 * 		});   
 * 
 * The mapping between Javascript types and Java types is the following:
 * 
 * <table>
 * 	<tr><th><strong>JAVASCRIPT TYPE</strong></th>		<th><strong>JAVA CLASS</strong></th></tr>
 * 	<tr><td>object</td>						<td>java.util.LinkedHashMap&lt;String, Object&gt;</td></tr>
 * 	<tr><td>array</td>						<td>java.util.ArrayList&lt;Object&gt;</td></tr>
 * 	<tr><td>string</td>						<td>java.lang.String</td></tr>
 * 	<tr><td>Number (without decimals)</td>	<td>java.lang.Integer, java.lang.Long or java.math.BigInteger<br/>(the smallest possible)</td></tr>
 * 	<tr><td>Number (with decimal)</td>		<td>java.lang.Double</td></tr>
 *	<tr><td>boolean</td>					<td>java.lang.Boolean</td></tr>
 * 	<tr><td>null</td>						<td>null</td></tr>
 * </table>
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
		SERVERCOMM_URL: Ametys.getPluginDirectPrefix("core-ui") + "/servercomm/messages.xml",
		
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
		 * Callback is simply ignored.
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
		 * @property {Object[]} _messages The waiting messages. See {@link #send} method to see the message object
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
		 * @property {Number} _nextTimer The date as long when the next timer should stop. Null if no timer.
		 */
		_nextTimer: null,

		/**
		 * @private
		 * @property {Object} _runningRequests Association of id and send options ; to remember while timeout
		 */
		_runningRequests: {},
		
		/**
		 * @private
		 * @property {Number} _runningRequestsIndex The index for the next running request.
		 */
		_runningRequestsIndex: 0,
		
		/**
		 * @private
		 * @property {Object} _lastUniqueIdForCancelCode Contains an association {String} cancelCode / {String} the identifier of the last request called for this code.
		 */
		_lastUniqueIdForCancelCode: {},
		
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
			
			if (this._suspended == 0 && this._nextTimer != null && this._nextTimer < new Date().getTime())
			{
				this._sendMessages();
			}
		},
		
		/**
		 * Add a message to the 'to send' list of message. Depending on its priority, it will be send immediatly or later.
		 * @param {Object} message The config object for the message to send.
		 * @param {String} message.plugin The name of the server plugin targeted. Can be null. Do not use with workspace.
		 * @param {String} message.workspace The name of the server workpace targeted. Can be null if plugin is specified or to send to current workspace.
		 * @param {String} message.url The url on the server relative to the plugin or workspace
		 * @param {Object} message.parameters The parameters to send to the server (Map<String, String>)
		 * 
		 * @param {Number} [message.priority=Ametys.data.ServerComm.PRIORITY_MAJOR] The priority of the message. Use ServerComm.PRIORITY_* constants.
		 * 
		 * @param {Object/Object[]} message.callback When using non synchronous messages, a callback configuration is required. Not available for #PRIORITY_SYNCHRONOUS requests. 
		 * @param {Function} message.callback.handler The function to call when the message will come back. 
		 * @param {Object} message.callback.handler.response Will be the xml parent node of the response. This node can be null or empty on fatal error (see errorMessage). An attribute 'code' is available on this node with the http code. This response has an extra method 'getText' that get the text from a node in parameter of the response.
		 * @param {Object[]} message.callback.handler.callbackarguments Is the 'callback.arguments' array
		 * @param {Object} [message.callback.scope] The scope of the function call. Optional.
		 * @param {String[]} [message.callback.arguments] An array of string that will be given as arguments of the callback. Optional.
         * @param {Boolean} [message.callback.ignoreOnError=true] Is the callback called with a null or empty response?
		 * 
		 * @param {String} [message.responseType=xml] Can be "xml" (default) to have a xml response, "text" to have a single text node response or "xml2text" to have a single text node response where xml prologue as text is removed
		 *
         * @param {Boolean/String/Object} [message.waitMessage=false] Display a Ext.LoadMask while the request is running. Set to true to display a default loading message. Set to a string to display your message. Set to a Ext.LoadMask configuration object to do more accurate stuffs (such as covering only a component - since by default all the ui is grayed), but if no target is specified, this will use the Ametys.mask.GlobalMask and so will ignore most properties. Not available for #PRIORITY_SYNCHRONOUS requests.
		 * 
		 * @param {Boolean/String/Object} [message.errorMessage=false] When the request is a failure display a message to the user (using #handleBadResponse). 
		 * Set to false, the callback is called with a null or empty response (you should protected your code with #handleBadResponse). 
		 * Set to true to display a default error message and your callback will not be called.
		 * Set to a string to display a custom error message and your callback will not be called.
		 * Set to an object with the following options: 
		 * @param {String} [message.errorMessage.msg] The error message. There is a default message. 
		 * @param {String} [message.errorMessage.category] The error message category for log purposes. 
		 *  
		 * @param {String} [message.cancelCode] This parameter allow to cancel a request or ignore its response if it is out-dated. Not available for #PRIORITY_SYNCHRONOUS requests.
		 * A classic case it that the button wants more information on the last selected content: while asking server for information on content A, if the content B is selected by the user: this parameter allow to discard the information on A. 
		 * Note that you will not be informed if the cancelled request was not sent or send but ignored by the client : so this is to use on read request only and should be an identifier for your kind of operation (such as 'MyClass$getContentInfo').
		 * 
		 * @param {Object} [message.cancellationCallback] Use this parameter to be informed and do some action when the message was cancelled or ignored by the client.
		 * @param {Function} message.cancellationCallback.handler The function to call when the message was cancelled.
		 * @param {Object[]} message.cancellationCallback.handler.callbackarguments Is the 'callback.arguments' array
		 * @param {Object} [message.cancellationCallback.scope] The scope of the function call. Optional.
		 * @param {String[]} [message.cancellationCallback.arguments] An array of string that will be given as arguments of the callback. Optional.
      
		 * @return {Object} The XHR object containing the response data for #PRIORITY_SYNCHRONOUS requests. Null in other cases.
		 */
		send: function(message)
		{
			// Generating a unique id for this message for cancelling purposes
			if (message.cancelCode)
			{
				message.uniqueId = Ext.id(null, 'serverinfo-');
				this._lastUniqueIdForCancelCode[message.cancelCode] = message.uniqueId;

				// removing any unsent message with the same cancelCode
				var messagesIndexToCancel = [];
				for (var i = 0; i < this._messages.length; i++)
				{
					var oldMessage = this._messages[i];
					if (oldMessage.cancelCode == message.cancelCode)
					{
						if (this.getLogger().isDebugEnabled())
						{
							this.getLogger().debug("Discarding message with cancel code '" + message.cancelCode + "'");
						}
						oldMessage.cancelled = true;
						
						messagesIndexToCancel.push(i);
						
                        this._hideWaitMessage(oldMessage.waitMessage);
                        
                        if (message.cancellationCallback)
                        {
                        	message.cancellationCallback.handler.apply(message.cancellationCallback.scope, [message.cancellationCallback.arguments]);
                        }
                    }
				}
				for (var i = messagesIndexToCancel.length - 1; i >= 0; i--)
				{
					var index = messagesIndexToCancel[i];
					Ext.Array.remove(this._messages, this._messages[index]);
				}
			}

			Ext.applyIf(message, {
				pluginOrWorkspace: message.plugin ? message.plugin : (message.workspace ? '_' + message.workspace : null),
				toRequest: function() {
					var m = {};
					
					m.pluginOrWorkspace = this.pluginOrWorkspace || '_' + Ametys.WORKSPACE_NAME;
					m.responseType = this.responseType;
					m.url = this.url;
					m.parameters = this.parameters;
					
					return m;
				}
			});
			message.responseType = message.responseType || "xml";
			message.priority = message.priority || Ametys.data.ServerComm.PRIORITY_MAJOR;
            
            try
            {
                throw new Error("get trace");
            }
            catch (e)
            {
                message.callstack = e.stack;
            }
			
			if (message.priority == Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS)
			{
				return this._sendSynchronousMessage(message.toRequest());
			}
            
            message.callback = Ext.Array.from(message.callback);
			
			// Load mask
			message.waitMessage = this._showWaitMessage(message.waitMessage);
            
			if (message.priority == Ametys.data.ServerComm.PRIORITY_LONG_REQUEST)
			{
				this._sendMessages(message);
				return null;
			}
			else
			{
				// add the message to the list
				this._messages.push(message);
				
				// compute delay wanted and ring time associated (add a 50 milliseconds delay to try to cumulate several messages)
				var delay = 1000 * message.priority + 50;
				var ringTime = new Date().getTime() + delay;
			
				// if the current timer rings after the wanted time (at 20 milliseconds near)
				if (this._nextTimer == null || ringTime < this._nextTimer - 20)
				{
					this._nextTimer = ringTime;
					if (this._sendTask)
					{
						window.clearTimeout(this._sendTask);
					}
					this._sendTask = window.setTimeout(function () { Ametys.data.ServerComm._sendMessages(); }, delay);
				}
				return null;
			}
		},
		
		/**
		 * Directly calls Java code on server-side.
		 * @param {Object} config The configuration object
		 * @param {String} config.role The Java component id
		 * @param {String} [config.id] If the role refers to an extension point, the id refers to an extension. If not, the id should be null.
		 * @param {String} config.methodName The "callable" method to call in the Java component.
		 * @param {Object[]} config.parameters The methods parameters. They will be converted to Java Objects keeping types as much as possible.
		 * 
		 * @param {Object/Object[]} config.callback The callback configuration.
		 * @param {Function} config.callback.handler Called after method execution.
		 * @param {Object} config.callback.handler.response The server response. Can be null for a void response or undefined if an error occurred when ignoreOnError is false.
		 * @param {Object[]} config.callback.handler.arguments Is the 'callback.arguments' array
		 * @param {Object} [config.callback.scope] The scope of the function call. Optional.
		 * @param {String[]} [config.callback.arguments] An array of Objects that will be passed to the callback as second argument. Optional.
         * @param {Boolean} [config.callback.ignoreOnError=true] Is the callback called with a null or empty response?
         * 
         * @param {Object} [message.cancellationCallback] Use this parameter to be informed and do some action when the message was cancelled or ignored by the client. See #send for more information about this parameter.
		 * 
		 * @param {String} [config.cancelCode] This allow to cancel a previous unfinished request. See #send for more information on the cancelCode.
		 * @param {Boolean/String/Object} [config.waitMessage] Display a waiting message while the request is running. See #send for more information on the waitingMessage.
		 * @param {Boolean/String/Object} [config.errorMessage] An error message. See #send for more information on the errorMessage.
		 * @param {Number} [config.priority] The message priority. See #send for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
		 */
		callMethod: function(config)
		{
			this.send({
				plugin: 'core-ui',
				url: 'client-call',
				parameters: {
					role: config.role,
					id: config.id,
					methodName: config.methodName,
					parameters: config.parameters
				},
				callback: {
					handler: this._callProcessed,
					scope: this,
                    ignoreOnError: false,
					arguments: {cb: config.callback}
				},
				responseType: 'text',
				cancellationCallback: {
					handler: this._cancellationProcessed,
					scope: this,
					arguments: {cb: config.cancellationCallback}
				},
				cancelCode: config.cancelCode,
				waitMessage: config.waitMessage,
				errorMessage: config.errorMessage,
				priority: (config.priority == Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS) ? null : config.priority
			});
		},
		
		/**
		 * @private
		 * Internal callback after cancellation
		 * @param {Object} response The server response
		 * @param {Object} arguments The arguments 
		 * @param {Function} arguments.cb The callback function 
		 */
		_cancellationProcessed: function (arguments)
		{
			var callback = arguments.cb;
			
			callback.handler.apply(callback.scope || this, [callback.arguments]);
		},
		
		/**
		 * @private
		 * Internal callback for the #callMethod function.
		 * @param {Object} response The server response
		 * @param {Object} arguments The arguments 
		 * @param {Function} arguments.cb The callback function 
		 */
		_callProcessed: function(response, arguments)
		{
			var callback = arguments.cb;
			
			var responseAsObject = undefined;
			if (!this.isBadResponse(response))
			{
				responseAsObject = Ext.JSON.decode(response.textContent || response.text);
			}
			
            callback = Ext.Array.from(callback);
            Ext.Array.forEach(callback, function (cb) {
                if (responseAsObject !== undefined || cb.ignoreOnError === false)
                {
    			     cb.handler.apply(cb.scope || this, [responseAsObject, cb.arguments]);
                }
            }, this);
		},
		
		/**
		 * @private
		 * Send a synchronous message
		 * @param {Object} messageRequest An object returned by ServerMessage.toRequest
		 * @return {Object} The XHR object containing the response data, or null if the ServerComm is shut down, or if a error occured
		 */
		_sendSynchronousMessage: function(messageRequest)
		{
			if (this._off == true)
			{
				return null;
			}
			
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
				conn = Ext.Ajax.request({url: Ametys.data.ServerComm.SERVERCOMM_URL, params: "content=" + encodeURIComponent(Ext.JSON.encode({0: messageRequest})) + "&context.parameters=" + encodeURIComponent(Ext.JSON.encode(Ametys.getAppParameters())), async: false});
			}
			catch(e)
			{
				if (typeof this._observer.onSyncRequestArrival == "function")
				{
					try
					{ 
						this._observer.onSyncRequestArrival(messageRequest, 2, null);
					}
					catch (e)
					{
						alert("Exception in Ametys.data.ServerComm._observer.onSyncRequestArrival: " + e);
					}
				}
				
				if (!this._off && confirm("{{i18n PLUGINS_CORE_UI_SERVERCOMM_LISTENERREQUEST_FAILED_UNAVAILABLE}}"))
				{
					return null; // this._sendSynchronousMessage(messageRequest);
				}
				else
				{
					Ametys.shutdown("{{i18n PLUGINS_CORE_UI_SERVERCOMM_LISTENERREQUEST_LOST_CONNECTION_1}}", "{{i18n PLUGINS_CORE_UI_SERVERCOMM_LISTENERREQUEST_LOST_CONNECTION_2}}");
					return null;
				}
			}
		    
			if (typeof this._observer.onSyncRequestArrival == "function")
			{
				try
				{ 
					this._observer.onSyncRequestArrival(messageRequest, 0, conn);
				}
				catch (e)
				{
					alert("Exception in Ametys.data.ServerComm._observer.onSyncRequestArrival: " + e);
				}
			}
			
			if (conn.responseXML == null)
			{

				if (confirm("{{i18n PLUGINS_CORE_UI_SERVERCOMM_NOTXML_DESC}}") && !this._off)
				{
					return null; // this._sendSynchronousMessage(messageRequest);
				}
				else
				{
					this._shutdown();
					return null;
				}
			}
			else
			{
				return Ext.dom.Query.selectNode("/responses/response[@id='0']", conn.responseXML);
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
		 * @param {Object} m An optional message. If null the method will empty the queue, else it will send only this message.
		 * @private
		 */
		_sendMessages: function(m)
		{
			var timeout = Ametys.data.ServerComm.TimeoutDialog.TIMEOUT;
			
			if (m == null)
			{
				window.clearTimeout(this._sendTask);
				this._sendTask = null;
				this._nextTimer = null;
				
				if (this._suspended > 0)
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
				for (var i = 0; i < this._messages.length; i++)
				{
					var message = this._messages[i];
					parameters[i] = message.toRequest();
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
			sendOptions.params = "content=" + encodeURIComponent(Ext.JSON.encode(parameters)) + "&context.parameters=" + encodeURIComponent(Ext.JSON.encode(Ametys.getAppParameters()));
			sendOptions._timeoutIndex = index;
			sendOptions._timeout = window.setTimeout("Ametys.data.ServerComm._onRequestTimeout ('" + index + "', " + timeout + ");", timeout);
			
			sendOptions._transactionId = Ext.Ajax.request(sendOptions);
			sendOptions.messages = m != null ? [m] : this._messages;

			if (m == null)
			{
				this._messages = [];
			}


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

			this._cancelTimeout(options);
			
			if (silently !== false && typeof this._observer.onRequestArrival == "function")
			{
				try
				{ 
					this._observer.onRequestArrival(options, 1, null);
				}
				catch (e)
				{
					alert("Exception in Ametys.data.ServerComm._observer.onRequestArrival (1): " + e);
				}
			}

			if (silently !== false)
			{
				this._dispatch({}, options)
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
			this.getLogger().debug("Request timeout [n°" + index + "]");

			var sendOptions = Ametys.data.ServerComm._runningRequests[index];
			sendOptions._timeout = null;
			sendOptions._timeoutDialog = new Ametys.data.ServerComm.TimeoutDialog(sendOptions.params, index, timeout);
		},

		/**
		 * @private
		 * Cancel the timeout and kill the timeout dialogue
		 * @param {Object} options The arguments passed
		 */
		_cancelTimeout: function(options)
		{
			if (options._timeout != null)
			{
				this.getLogger().debug("Clear timeout [n°" + options._timeoutIndex + "]");
				
				window.clearTimeout(options._timeout);
			}
			else
			{
				this.getLogger().debug("No timeout [n°" + options._timeoutIndex + "]");
			}
			
			if (options._timeoutDialog != null)
			{
				this.getLogger().debug("Closing timeout dialog [n°" + options._timeoutIndex + "]");

				options._timeoutDialog.kill();
				options._timeoutDialog = null;
			}
			delete Ametys.data.ServerComm._runningRequests[options._timeoutIndex];
			
			for (var i = 0; i < options.messages.length; i++)
			{
				var message = options.messages[i];
                
                this._hideWaitMessage(message.waitMessage);
			}
		},
        
        /**
         * @private
         * Display a wait message during request.
         * @param {Boolean/String/Object} waitMessage See waitMessage configuration on #send method.
         * @return {String/Ext.LoadMask} The wait message instance, or Ametys.mask.GlobalLoadMask identifier
         */
        _showWaitMessage: function(waitMessage)
        {
            if (waitMessage != null && waitMessage !== false)
            {
                if (waitMessage === true)
                {
                    waitMessage = { };
                }
                if (Ext.isString(waitMessage))
                {
                    waitMessage = { msg: waitMessage };
                }
                
                if (!waitMessage.target)
                {
                    waitMessage = Ametys.mask.GlobalLoadMask.mask(waitMessage.msg);
                }
                else
                {
                    waitMessage = Ext.create("Ext.LoadMask", waitMessage);
                    waitMessage.show();
                }
            }
            
            return waitMessage;
        },
        
        /**
         * @private
         * Wait the wait message
         * @param {String/Ext.LoadMask} waitMessage The wait message instance, or Ametys.mask.GlobalLoadMask identifier
         */
        _hideWaitMessage: function(waitMessage)
        {
            if (Ext.isString(waitMessage))
				{
                Ametys.mask.GlobalLoadMask.unmask(waitMessage);
				}
            else if (Ext.isObject(waitMessage))
            {
                waitMessage.hide();
                Ext.destroy(waitMessage);
			}
		},

		/**
		 * @private
		 * Listener on requests that succeed
		 * @param {Object} response The XHR object containing the response data.
		 * @param {Object} options The arguments of the request method call
		 */
		_onRequestComplete: function(response, options)
		{
			this._cancelTimeout(options);
			
			if (typeof this._observer.onRequestArrival == "function")
			{
				try
				{ 
					this._observer.onRequestArrival(options, 0, response);
				}
				catch (e)
				{
					alert("Exception in Ametys.data.ServerComm._observer.onRequestArrival (0): " + e);
				}
			}
			
			if (!this._off && (response.responseXML != null || confirm("{{i18n PLUGINS_CORE_UI_SERVERCOMM_NOTXML_DESC}}")))
			{
				this._dispatch(response, options);
			}
			else
			{
				Ametys.shutdown("{{i18n PLUGINS_CORE_UI_SERVERCOMM_LISTENERREQUEST_LOST_CONNECTION_1}}", "{{i18n PLUGINS_CORE_UI_SERVERCOMM_LISTENERREQUEST_LOST_CONNECTION_2}}");
			}
		},

		/**
		 * @private
		 * Call the callbacks for the response (that can be null)
		 * @param {Object} response The XHR object containing the response data
		 * @param {Object} options The arguments of the request method call
		 */
		_dispatch: function(response, options)
		{
			Ext.suspendLayouts();
			
			// for each message call the handler
			for (var i = 0; i < options.messages.length; i++)
			{
					var message = options.messages[i];
					
					var node = Ext.dom.Query.selectNode("/responses/response[@id='" + i + "']", response.responseXML);
					if ((node == null && message.cancelled == true)
						|| (message.cancelCode && this._lastUniqueIdForCancelCode[message.cancelCode] != message.uniqueId))
					{
						// only discard a canceled request if there is no answer
						// a cancel message with an answer means it has been canceled too late
						if (!message.cancelled && this.getLogger().isDebugEnabled())
						{
							this.getLogger().debug("Discarding response for a message with cancel code '" + message.cancelCode + "'");
						}
						
						if (message.cancellationCallback)
                        {
							message.cancellationCallback.handler.apply(message.cancellationCallback.scope, [message.cancellationCallback.arguments]);
                        }
						
						continue;
					}
					
					var badResponse = false;
					if (message.errorMessage != null && message.errorMessage !== false)
					{
						var msg = "{{i18n PLUGINS_CORE_UI_SERVERCOMM_ERROR_DESC}}";
						var category = this.self.getName(); 
						
						if (Ext.isString(message.errorMessage))
						{
							msg = message.errorMessage;
						}
						else if (Ext.isObject(message.errorMessage))
						{
							if (message.errorMessage.msg != null)
							{
								msg = message.errorMessage.msg;
							}
							if (message.errorMessage.category != null)
							{
								category = message.errorMessage.category;
							}
						}
						
						badResponse = this.handleBadResponse(msg, node, category);
					}
					
                    // Call message callbacks
                    Ext.Array.forEach(message.callback, function(callback) {
                        if (!badResponse || callback.ignoreOnError === false)  
                        {
    						try
    						{
    							callback.handler.apply(callback.scope, [node, callback.arguments]);
    						}
    						catch (e)
    						{
    							function throwException(e) 
    							{ 
    								throw e; 
    							}
    							Ext.defer(throwException, 1, this, [e]);
    							
    							Ametys.log.ErrorDialog.display({
    								title: "{{i18n PLUGINS_CORE_UI_SERVERCOMM_ERROR_TITLE}}",
    								text: "{{i18n PLUGINS_CORE_UI_SERVERCOMM_ERROR_DESC}}",
    								details: e,
    								category: "Ametys.data.ServerComm"
    							});
    						}
                        }
                    }, this);
			}
			
			Ext.resumeLayouts(true);
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
					this._observer.onRequestArrival(options, 2, null);
				}
				catch (e)
				{
					alert("Exception in Ametys.data.ServerComm._observer.onRequestArrival (2): " + e);
				}
			}

			if (!this._off && confirm("{{i18n PLUGINS_CORE_UI_SERVERCOMM_LISTENERREQUEST_FAILED_UNAVAILABLE}}"))
			{
				this._dispatch(response, options);
			}
			else
			{
				Ametys.shutdown("{{i18n PLUGINS_CORE_UI_SERVERCOMM_LISTENERREQUEST_LOST_CONNECTION_1}}", "{{i18n PLUGINS_CORE_UI_SERVERCOMM_LISTENERREQUEST_LOST_CONNECTION_2}}");
			}
		},
		
		/**
		 * Test for a null response or a 404 or a 500.
		 * @param {Object} response The response received
		 * @return {boolean} True if a bad request was found (and you should alert the user and abort your process)
		 */
		isBadResponse: function(response)
		{
			return response == null || response.getAttribute("code") == "500" || response.getAttribute("code") == "404";
		},
		
		/**
		 * Call this method to handle a bad response for you. Test response with #isBadResponse.
		 * @param {String} message The error message to display if the response is bad
		 * @param {Object} response The response received
		 * @param {String} category The log category. Can be null to avoid logging.
		 * @return {boolean} True if a bad request was found (and you should abort your process)
		 */
		handleBadResponse: function(message, response, category)
		{
			if (this.isBadResponse(response))
			{
				if (response == null)
				{
					Ametys.log.ErrorDialog.display({
							title: "{{i18n PLUGINS_CORE_UI_SERVERCOMM_BADRESPONSE_TITLE}}", 
							text: message,
							details: "{{i18n PLUGINS_CORE_UI_SERVERCOMM_BADRESPONSE_DESC}}",
							category: category
					});
				}
				else
				{
					var intMsg = Ext.dom.Query.selectValue("message", response);
					var hasMsg = intMsg != null && intMsg != "";
					var intStk = Ext.dom.Query.selectValue("stacktrace", response);
					var hasStk = intStk != null && intStk != "";
						
					Ametys.log.ErrorDialog.display({
							title: "{{i18n PLUGINS_CORE_UI_SERVERCOMM_SERVER_FAILED_DESC}}" + response.getAttribute("code") + ")", 
							text: message,
							details: (hasMsg ? intMsg : "")
							+ (hasMsg && hasStk ? "\n\n" : "")
							+ (hasStk ? intStk : ""),
							category: category
					});
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
