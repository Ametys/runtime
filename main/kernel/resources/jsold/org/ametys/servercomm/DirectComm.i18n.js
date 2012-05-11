/*
 *  Copyright 2009 Anyware Services
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
 */
org.ametys.servercomm.DirectComm = function()
{
	this._suspended = 0;
	this._messages = [];
	this._nextTimer = null;
	this._sendTask = null;
	this._sendOnRestart = false;
	
	this._connection = new Ext.data.Connection({timeout: 0});
	
	this._observer = {};
}

/**
 * @property {org.ametys.servercomm.DirectComm} _instance Unique instance.
 * @private
 */
org.ametys.servercomm.DirectComm._instance;
/**
 * This method returns the unique instance of the org.ametys.servercomm.DirectComm
 * @static
 * @returns {org.ametys.servercomm.DirectComm} The unique instance of the org.ametys.servercomm.DirectComm class.
 */
org.ametys.servercomm.DirectComm.getInstance = function()
{
	if (org.ametys.servercomm.DirectComm._instance == null)
	{
		org.ametys.servercomm.DirectComm._instance = new org.ametys.servercomm.DirectComm();
	}
	return org.ametys.servercomm.DirectComm._instance;
}

/**
 * @private
 */
org.ametys.servercomm.DirectComm.prototype._getConnectionObject = function()
{
	var pub = Ext.lib.Ajax;
    var o;      	

    try {
        if (o = this._createXhrObject(pub.transactionId)) 
        {
        	pub.transactionId++;
        }
    } catch(e) {
    } finally {
        return o;
    }
}

/**
 * @private
 */
org.ametys.servercomm.DirectComm.prototype._createXhrObject = function(transactionId)
{
    var activeX = ['MSXML2.XMLHTTP.3.0',
		           'MSXML2.XMLHTTP',
		           'Microsoft.XMLHTTP'];
    var http;
	
    try 
    {
        http = new XMLHttpRequest();                
    } 
    catch(e) 
    {
        for (var i = 0; i &lt; activeX.length; ++i) 
        {	            
            try 
            {
                http = new ActiveXObject(activeX[i]);                        
                break;
            } 
            catch(e) 
            {
            	// nothing
            }
        }
    } 
    finally 
    {
        return {conn : http, tId : transactionId};
    }
}

/**
 * @private
 */
org.ametys.servercomm.DirectComm.prototype._initHeader = function(label, value)
{
	var pub = Ext.lib.Ajax;
	(pub.headers = pub.headers || {})[label] = value;
}

/**
 * @private
 */
org.ametys.servercomm.DirectComm.prototype._setHeader = function(object)
{
	var pub = Ext.lib.Ajax;
    var conn = object.conn; 
    var prop;

	function setTheHeaders(conn, headers)
	{
	 	for (prop in headers) 
	 	{
	        if (headers.hasOwnProperty(prop)) 
	        {
	            conn.setRequestHeader(prop, headers[prop]);
	        }
	    }   
	}		
	
	if (pub.defaultHeaders) 
	{
	    setTheHeaders(conn, pub.defaultHeaders);
	}
	
	if (pub.headers) 
	{
		setTheHeaders(conn, pub.headers);
	    delete pub.headers;                
	}
}

/**
 * Send a synchronous request
 * @param {String} url The url to request. Cannot be null
 * @param {String} postData The data to send. Can be null.
 * @throws An exception if the xml http request fails 
 */
org.ametys.servercomm.DirectComm.prototype.sendSynchronousRequest = function(url, postData)
{
	var pub = Ext.lib.Ajax;
	
    var connection = this._getConnectionObject();

    connection.conn.open("POST", url, false);

    if (pub.useDefaultXhrHeader) 
    {                    
    	this._initHeader('X-Requested-With', pub.defaultXhrHeader);
    }

    if(postData &amp;&amp; pub.useDefaultHeader &amp;&amp; (!pub.headers || !pub.headers['Content-Type']))
    {
        this._initHeader('Content-Type', pub.defaultPostHeader);
    }

    if (pub.defaultHeaders || pub.headers) 
    {
        this._setHeader(connection);
    }

	if (context &amp;&amp; context.parameters)
	{
		if (!postData)
		{
			postData = "";
		}
		postData += "&amp;context.parameters=" + encodeURIComponent(Ext.util.JSON.encode(context.parameters));
	}
    
	connection.conn.send(postData || null);

    return connection.conn;
}