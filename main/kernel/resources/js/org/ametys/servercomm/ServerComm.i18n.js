/*
 *  Copyright 2011 Anyware Services
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

Ext.namespace('org.ametys.servercomm');

/**
 * Do not call this constructor. Use getInstance() instead.
 * @constructor
 * @class This class has a single instance. Use getInstance() to get it.
 * This class allow to create a message. See createMessage for more information.
 */
org.ametys.servercomm.ServerComm = function()
{
	this._suspended = 0;
	this._messages = [];
	this._nextTimer = null;
	this._sendTask = null;
	this._sendOnRestart = false;
	
	this._connection = new Ext.data.Connection({timeout: 0});
	
	this._observer = {};
}

org.ametys.servercomm.ServerComm.SERVERCOMM_URL = context.workspaceContext + "/plugins/core/servercomm/messages.xml";

/**
 * @property {org.ametys.servercomm.ServerComm} _instance Unique instance.
 * @private
 */
org.ametys.servercomm.ServerComm._instance;
/**
 * This method returns the unique instance of the org.ametys.servercomm.ServerComm
 * @static
 * @returns {org.ametys.servercomm.ServerComm} The unique instance of the org.ametys.servercomm.ServerComm class.
 */
org.ametys.servercomm.ServerComm.getInstance = function()
{
	if (org.ametys.servercomm.ServerComm._instance == null)
	{
		org.ametys.servercomm.ServerComm._instance = new org.ametys.servercomm.ServerComm();
	}
	return org.ametys.servercomm.ServerComm._instance;
}

/**
 * The enumeration for message priority : The message will leave now regardless of the queue and of the suspend.
 * The request will have a 10x longer timeout.
 * The request will be alone.
 * Sample: creating a long search message.
 * @type {integer}
 * @constant  
 */
org.ametys.servercomm.ServerComm.PRIORITY_LONG_REQUEST = -2;
/**
 * The enumeration for message priority : The message will leave now regardless of the queue and of the suspend.
 * The send method will become blocking and will return the response.
 * Callback and callback parameters are simply ignored.
 * Sample: creating a target message.
 * @type {integer}
 * @constant  
 */
org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS = -1;
/**
 * The enumeration for message priority : The message needs to leave as soon as possible.
 * Sample: saving any user modifications.
 * @type {integer}
 * @constant  
 */
org.ametys.servercomm.ServerComm.PRIORITY_MAJOR = 0;
/**
 * The enumeration for message priority : The message can be delayed.
 * Sample : updating a minor view.
 * @type {integer}
 * @constant  
 */
org.ametys.servercomm.ServerComm.PRIORITY_NORMAL = 10;
/**
 * The enumeration for message priority : The message is for background use.
 * Sample : ping, preferences save.
 * @type {integer}
 * @constant  
 */
org.ametys.servercomm.ServerComm.PRIORITY_MINOR = 40;

/**
 * @private
 * @property {org.ametys.servercomm.ServerMessage[]} _messages The waiting messages
 */
org.ametys.servercomm.ServerComm.prototype._messages;

/**
 * @private
 * @property {Object} _observer The ServerComm observer that will receive events on each action. There is no setter, you have to set it directly.
 */
org.ametys.servercomm.ServerComm.prototype._observer;

/**
 * @private
 * @property {Ext.data.Connection} _connection The ajax connection
 */
org.ametys.servercomm.ServerComm.prototype._connection;

/**
 * @private
 * @property {integer} _suspended The number of times the communication was suspended. 0 means communication are not suspended. Cannot be negative.
 */
org.ametys.servercomm.ServerComm.prototype._suspended;

/**
 * @private
 * @property {integer} _sendTask The time out value (return by setTimeout)
 */
org.ametys.servercomm.ServerComm.prototype._sendTask;

/**
 * @private
 * @proptery {long} _nextTimer The date as long when the next timer should stop. Null if no timer.
 */
org.ametys.servercomm.ServerComm.prototype._nextTimer;

/**
 * Suspend the communication with the server.
 * Use it when you know that several component will add messages with a major priority to do a single request
 * Do not forget to call the restart method to effectively send messages.
 * Be sure to finally call the restart method.
 */
org.ametys.servercomm.ServerComm.prototype.suspend = function()
{
	this._suspended++;
}

/**
 * Restart suspended communications with server.
 * Do not call this method if you do not have call the suspend one before.
 */
org.ametys.servercomm.ServerComm.prototype.restart = function()
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
}

/**
 * Add a message to the 'to send' list of message. Depending on its priority, it will be send immediatly or later.
 * @param {org.ametys.servercomm.ServerMessage} message The message to send to the server. Can not be null.
 * @return {Object} The XHR object containing the response data for a synchronous priority message or null for other priorities.
 */
org.ametys.servercomm.ServerComm.prototype.send = function(message)
{
	if (message.getPriority() == org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS)
	{
		return this._sendSynchronousMessage(message.toRequest());
	}
	else if (message.getPriority() == org.ametys.servercomm.ServerComm.PRIORITY_LONG_REQUEST)
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
			this._sendTask = setTimeout(function () { org.ametys.servercomm.ServerComm.getInstance()._sendMessages(); }, delay);
		}
		return null;
	}
}

/**
 * @private
 * Send a synchronous message
 * @param {Object} messageRequest An object returned by ServerMessage.toRequest
 * @return {Object} The XHR object containing the response data.
 */
org.ametys.servercomm.ServerComm.prototype._sendSynchronousMessage = function(messageRequest)
{
	if (typeof this._observer.onSyncRequestDeparture == "function")
	{
		try
		{ 
			this._observer.onSyncRequestDeparture(messageRequest);
		}
		catch (e)
		{
			alert("Exception in org.ametys.servercomm.ServerComm._observer.onSyncRequestDeparture: " + e);
		}
	}

	var postData = "content=" + encodeURIComponent(Ext.util.JSON.encode({0: messageRequest}));
	var conn = null;
	
	try
	{
		conn = org.ametys.servercomm.DirectComm.getInstance().sendSynchronousRequest(org.ametys.servercomm.ServerComm.SERVERCOMM_URL, postData);
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
				alert("Exception in org.ametys.servercomm.ServerComm._observer.onSyncRequestArrival: " + e);
			}
		}
		
		if (!this._off &amp;&amp; confirm("<i18n:text i18n:key="KERNEL_SERVERCOMM_LISTENERREQUEST_FAILED_UNAVAILABLE"/>"))
		{
			return null;// this._sendSynchronousMessage(messageRequest);
		}
		else
		{
			this._shutdown();
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
			alert("Exception in org.ametys.servercomm.ServerComm._observer.onSyncRequestArrival: " + e);
		}
	}
	
	if (conn.responseXML == null)
	{

		if (confirm("<i18n:text i18n:key="KERNEL_SERVERCOMM_NOTXML_DESC"/>") &amp;&amp; !this._off)
		{
			return null;// this._sendSynchronousMessage(messageRequest);
		}
		else
		{
			this._shutdown();
			return null;
		}
	}
	else
	{
		return conn.responseXML.selectSingleNode("/responses/response[@id='0']");
	}
}

/**
 * @private
 * Shut the application because the server is down
 */
org.ametys.servercomm.ServerComm.prototype._shutdown = function(m)
{
	this._off = true;
	this.suspend();
	
	org.ametys.msg.ErrorDialog._okMessages();
	org.ametys.msg.ErrorDialog = function() {};
	
	document.body.style.backgroundColor = "#FFFFFF";
	document.body.style.color = "#000000";
	document.body.innerHTML = "&lt;h1&gt;" + "<i18n:text i18n:key='KERNEL_SERVERCOMM_LISTENERREQUEST_LOST_CONNECTION_1'/>" + "&lt;/h1&gt; &lt;p&gt;" + "<i18n:text i18n:key='KERNEL_SERVERCOMM_LISTENERREQUEST_LOST_CONNECTION_2'/>" +"&lt;/p&gt;&lt;p&gt;&lt;a href='javascript:location.reload(true);'&gt;" + "<i18n:text i18n:key='KERNEL_SERVERCOMM_LISTENERREQUEST_LOST_CONNECTION_3'/>" + "&lt;/a&gt;&lt;/p&gt;"
}

/**
 * @property {String} xmlTextContent This is the name of the attributes to use on nodes to get text as value. E.g. response.selectSingleNode("Person/name")[org.ametys.servercomm.ServerComm.xmlTextContent]
 */
org.ametys.servercomm.ServerComm.xmlTextContent = Ext.isIE ? 'text' : 'textContent';

/**
 * Send the waiting messages to the server
 * @param m A optional message. If null the method will empty the queue, else it will send only this message.
 * @private
 */
org.ametys.servercomm.ServerComm.prototype._sendMessages = function(m)
{
    var timeout = org.ametys.servercomm.TimeoutDialog.TIMEOUT;
    
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
    var index = org.ametys.servercomm.ServerComm._runningRequestsIndex ++;
    org.ametys.servercomm.ServerComm._runningRequests[index] = sendOptions;
    
    sendOptions.url = org.ametys.servercomm.ServerComm.SERVERCOMM_URL;
    sendOptions.success = this._onRequestComplete;
    sendOptions.failure = this._onRequestFailure;
    sendOptions.scope = this;
    sendOptions.params = "content=" + encodeURIComponent(Ext.util.JSON.encode(parameters)) 
    if (context &amp;&amp; context.parameters)
    {
        sendOptions.params += "&amp;context.parameters=" + encodeURIComponent(Ext.util.JSON.encode(context.parameters));
    }
    
    sendOptions._timeoutIndex = index;
    sendOptions._timeout = window.setTimeout("org.ametys.servercomm.ServerComm._onRequestTimeout ('" + index + "', " + timeout + ");", timeout);


    sendOptions._transactionId = this._connection.request(sendOptions);
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
            alert("Exception in org.ametys.servercomm.ServerComm._observer.onRequestDeparture: " + e);
        }
    }
}     

/**
 * @private
 * Abort a request (note that the server will still execute it but the result will be discard)
 * @param {Map} options The send options
 */
org.ametys.servercomm.ServerComm.prototype._abort = function(options)
{
	this._connection.abort(options._transactionId);

	if (typeof this._observer.onRequestArrival == "function")
	{
		try
		{ 
			this._observer.onRequestArrival(options, 1);
		}
		catch (e)
		{
			alert("Exception in org.ametys.servercomm.ServerComm._observer.onRequestArrival (1): " + e);
		}
	}

	this._cancelTimeout(options);

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

				new org.ametys.msg.ErrorDialog("<i18n:text i18n:key='KERNEL_SERVERCOMM_ERROR_TITLE'/>",
						"<i18n:text i18n:key='KERNEL_SERVERCOMM_ERROR_DESC'/>",
                        e,
                        "org.ametys.servercomm.ServerComm");
			}
	}
}

/**
 * @private
 * @static
 * @type {Map&lt;String, Object} Association of id and send options ; to remember while timeout
 */
org.ametys.servercomm.ServerComm._runningRequests = {};
/**
 * @private
 * @static
 * @type {interger} The index for the next running request.
 */
org.ametys.servercomm.ServerComm._runningRequestsIndex = 0;

/**
 * @private
 * @static
 * When a request times out
 * @param {integer} index The index of the request in the _runningRequest map.
 * @param {integer} timeout The timeout value
 */
org.ametys.servercomm.ServerComm._onRequestTimeout = function(index, timeout)
{
	var sendOptions = org.ametys.servercomm.ServerComm._runningRequests[index];
	sendOptions._timeout = null;
	sendOptions._timeoutDialog = new org.ametys.servercomm.TimeoutDialog(sendOptions.params, index, timeout);
}

/**
 * @private
 * Cancel the timeout and kill the timeout dialogue
 */
org.ametys.servercomm.ServerComm.prototype._cancelTimeout = function(options)
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
	delete org.ametys.servercomm.ServerComm._runningRequests[options._timeoutIndex];

}

/**
 * @private
 * Listener on requests that succeed
 * @param {Object} response The XHR object containing the response data.
 * @param {Object} options The arguments of the request method call
 */
org.ametys.servercomm.ServerComm.prototype._onRequestComplete = function(response, options)
{
	if (typeof this._observer.onRequestArrival == "function")
	{
		try
		{ 
			this._observer.onRequestArrival(options, 0);
		}
		catch (e)
		{
			alert("Exception in org.ametys.servercomm.ServerComm._observer.onRequestArrival (0): " + e);
		}
	}

	this._cancelTimeout(options);
	
	if (!this._off &amp;&amp; (response.responseXML != null || confirm("<i18n:text i18n:key="KERNEL_SERVERCOMM_NOTXML_DESC"/>")))
	{
		this._dispatch(response, options);
	}
	else
	{
		this._shutdown();
	}
}

/**
 * @private
 * Call the callbacks for the response (that can be null)
 * @param {Object} response The XHR object containing the response data
 * @param {Object} options The arguments of the request method call
 */
org.ametys.servercomm.ServerComm.prototype._dispatch = function(response, options)
{
	// for each message call the handler
	for (var i = 0; i &lt; options.messages.length; i++)
	{
			var message = options.messages[i];

			var node = response.responseXML != null ? response.responseXML.selectSingleNode("/responses/response[@id='" + i + "']") : null;
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

				new org.ametys.msg.ErrorDialog("<i18n:text i18n:key='KERNEL_SERVERCOMM_ERROR_TITLE'/>",
						"<i18n:text i18n:key='KERNEL_SERVERCOMM_ERROR_DESC'/>",
                        e + '',
                        "org.ametys.servercomm.ServerComm");
			}
	}
}

/**
 * @private
 * Listener on requests that failed
 * @param {Object} response The XHR object containing the response data
 * @param {Object} options The arguments of the request method call
 */
org.ametys.servercomm.ServerComm.prototype._onRequestFailure = function(response, options)
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
			alert("Exception in org.ametys.servercomm.ServerComm._observer.onRequestArrival (2): " + e);
		}
	}

	if (!this._off &amp;&amp; confirm("<i18n:text i18n:key="KERNEL_SERVERCOMM_LISTENERREQUEST_FAILED_UNAVAILABLE"/>"))
	{
		this._dispatch(response, options);
	}
	else
	{
		this._shutdown();
	}
}

/**
 * @static
 * Call this method to handle a response by searching a tag.
 * This method is made to match a special xml pattern returned by the action-result-generator.
 * It should looks like : &lt;rootTag&gt; &lt;tag1&gt;value&lt;tag1&gt; &lt;tag2&gt;value&lt;tag2&gt; &lt;/rootTag&gt;
 * @param {Object} response The xml response received
 * @param {String} tagname The tag to get
 * @return {String} The value of the tag or null if something get wrong.
 */
org.ametys.servercomm.ServerComm.handleResponse = function(response, tagname)
{
	if (response == null)
	{
		return null;
	}
	else
	{
		var node = response.selectSingleNode("*/" + tagname);
		if (node == null)
		{
			return null;
		}
		else
		{
			return node[org.ametys.servercomm.ServerComm.xmlTextContent];
		}
	}
}

/**
 * @static
 * Call this method to handle a bad response for you. Test for a null response or a 404 or a 500.
 * @param {String} message The error message to display if the response is bad
 * @param {Object} response The response received
 * @param {String} category The log category. Can be null to avoid logging.
 * @return {boolean} True if a bad request was found (and you should abord your treatment)
 */
org.ametys.servercomm.ServerComm.handleBadResponse = function(message, response, category)
{
	if (response == null || response.getAttribute("code") == "500" || response.getAttribute("code") == "404")
	{
		if (response == null)
		{
			new org.ametys.msg.ErrorDialog(
					"<i18n:text i18n:key='KERNEL_SERVERCOMM_BADRESPONSE_TITLE'/>", 
					message,
					"<i18n:text i18n:key='KERNEL_SERVERCOMM_BADRESPONSE_DESC'/>",
					category
			);
		}
		else
		{
			var intMsg = response.selectSingleNode("message")[org.ametys.servercomm.ServerComm.xmlTextContent];
			var hasMsg = intMsg != null &amp;&amp; intMsg != "";
			var intStk = response.selectSingleNode("stacktrace")[org.ametys.servercomm.ServerComm.xmlTextContent];
			var hasStk = intStk != null &amp;&amp; intStk != "";
				
			new org.ametys.msg.ErrorDialog(
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
