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
 * Creates a message for the server
 * @constructor
 * @class This class is a message for the server
 * @param {String} plugin The name of the server plugin targeted. Can be null to send to server root.
 * @param {String} url The url on the server relative to the plugin
 * @param {Map<String, String>} parameters The parameters to send to the server
 * @param {integer} priority The priority of the message. Use org.ametys.servercomm.ServerComm.PRIORITY_MAJOR, org.ametys.servercomm.ServerComm.PRIORITY_NORMAL, or org.ametys.servercomm.ServerComm.PRIORITY_MINOR.
 * @param {function} callback The function to call when the message will come back. 
 * First argument will be the xml parent node of the response. This node can be null or empty on fatal error. An attribute 'code' is available on this node with the http code. This reponse has an extra method 'getText' that get the text from a node in parameter of the response.
 * Second argument is the 'callbackarguments' array
 * @param {Object} callbackscope The scope of the function call. Optionnal.
 * @param {String[]} callbackarguments An array of string that will be given as arguments of the callback. Optionnal
 * @param {String} responseType Can be "xml" (default) to have a xml response, "text" to have a single text node response or "xml2text" to have a single text node response where xml prologue as text is removed
 */
org.ametys.servercomm.ServerMessage = function(plugin, url, parameters, priority, callback, callbackscope, callbackarguments, responseType)
{
	this._plugin = plugin;
	this._url = url;
	this._parameters = parameters;
	this._priority = priority;
	this._callback = callback;
	this._callbackscope = callbackscope;
	this._callbackarguments = callbackarguments;
	this._responseType = responseType == null ? "xml" : responseType; 
}

/**
 * @private
 * @property {String} _plugin The name of the server plugin targeted. Can be null to send to server root.
 */
org.ametys.servercomm.ServerMessage.prototype._plugin;
/**
 * Get the name of the server plugin targeted.
 * @return {String} The name of the server plugin targeted. Can be null to send to server root.
 */
org.ametys.servercomm.ServerMessage.prototype.getPlugin = function()
{
	return this._plugin;
}

/**
 * @private
 * @property {String} _url The url on the server relative to the plugin
 */
org.ametys.servercomm.ServerMessage.prototype._url;
/**
 * Get the url on the server relative to the plugin
 * @return {String} The url on the server relative to the plugin
 */
org.ametys.servercomm.ServerMessage.prototype.getUrl = function()
{
	return this._url;
}

/**
 * @private
 * @property {Map<String, String>} _parameters The parameters to send to the server
 */
org.ametys.servercomm.ServerMessage.prototype._parameters;
/**
 * Get the parameters to send to the server
 * @return {Map<String, String>} The parameters to send to the server
 */
org.ametys.servercomm.ServerMessage.prototype.getParameters = function()
{
	return this._parameters;
}

/**
 * @private
 * @property {integer} _priority The priority of the message. Use org.ametys.servercomm.ServerComm.PRIORITY_MAJOR, org.ametys.servercomm.ServerComm.PRIORITY_NORMAL, or org.ametys.servercomm.ServerComm.PRIORITY_MINOR.
 */
org.ametys.servercomm.ServerMessage.prototype._priority;
/**
 * Get the priority of the message. org.ametys.servercomm.ServerComm.PRIORITY_MAJOR, org.ametys.servercomm.ServerComm.PRIORITY_NORMAL, or org.ametys.servercomm.ServerComm.PRIORITY_MINOR.
 * @return {integer} The priority of the message
 */
org.ametys.servercomm.ServerMessage.prototype.getPriority = function()
{
	return this._priority;
}

/**
 * @private
 * @property {function} _callback The function to call when the message will come back
 */
org.ametys.servercomm.ServerMessage.prototype._callback;
/**
 * Get the function to call when the message will come back
 * @return {function} The function to call when the message will come back
 */
org.ametys.servercomm.ServerMessage.prototype.getCallback = function()
{
	return this._callback;
}

/**
 * @private
 * @property {Object} _callbackscope The scope of the function call. Optionnal.
 */
org.ametys.servercomm.ServerMessage.prototype._callbackscope;
/**
 * Get the scope of the function call.
 * @return {Object} The scope of the function call. Can be null.
 */
org.ametys.servercomm.ServerMessage.prototype.getCallbackScope = function()
{
	return this._callbackscope;
}

/**
 * @private
 * @property {String[]} _callbackarguments An array of string that will be given as arguments of the callback. Optionnal
 */
org.ametys.servercomm.ServerMessage.prototype._callbackarguments;
/**
 * Get an array of string that will be given as arguments of the callback.
 * @return {String[]} An array of string that will be given as arguments of the callback. Optionnal. Can be null or empty.
 */
org.ametys.servercomm.ServerMessage.prototype.getCallbackArguments = function()
{
	return this._callbackarguments;
}

/**
 * @private
 * @property {String} _responseType Can be "xml" (default) to have a xml response, "text" to have a single text node response or "xml2text" to have a single text node response where xml prologue as text is removed
 */
org.ametys.servercomm.ServerMessage.prototype._responseType;
/**
 * Get the response required : xml, xml2text or text
 * @return {boolean} _responseType value.
 */
org.ametys.servercomm.ServerMessage.prototype.getResponseType = function()
{
	return this._responseType;
}

/**
 * Convert the message to the request format
 * @return {Map<String, String>} The map of parameters
 */
org.ametys.servercomm.ServerMessage.prototype.toRequest = function()
{
	var m = {};
	
	if (this._plugin != null)
	{
		m.plugin = this._plugin;
	}
	
	m.responseType = this._responseType;
	m.url = this._url;
	m.parameters = this._parameters;
	
	return m;
}
