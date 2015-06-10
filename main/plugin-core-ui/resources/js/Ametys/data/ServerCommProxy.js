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
 * Defines a dedicated proxy that use the {@link Ametys.data.ServerComm} to communicate with the server.
 * 
 * Example of configuration in a store:
 * 
 * 		Ext.create('Ext.data.Store', {
 * 			model: 'my.own.Model',
 * 			proxy: {
 * 				type: 'ametys',
 * 				plugin: 'projects',
 * 				url: 'administrator/projects/list.xml',
 * 				reader: {
 * 					type: 'xml',
 * 					record: 'element',
 * 					rootProperty: 'root'
 * 				}
 * 			}
 * 		});
 */
Ext.define('Ametys.data.ServerCommProxy', {
	alias: 'proxy.ametys',
	extend: 'Ext.data.proxy.Server',
	
	noCache: false,
	
	/**
	 * @cfg {String} plugin The name of the plugin receiving the request. See Ametys.data.ServerComm#send.
	 */
	plugin: null,
	/**
	 * @cfg {String} workspace The name of the workspace receiving the request. See Ametys.data.ServerComm#send.
	 */
	workspace: null,
	
	/**
	 * @cfg {String} errorMessage The error message to display if an error occures. There is a default value.
	 */
	errorMessage: "<i18n:text i18n:key='KERNEL_SERVERCOMMPROXY_ERROR_MSG'/>",
	
	/**
	 * @cfg {Object} reader2ResponseTypes Values are the responseType of Ametys.data.ServerComm#send and keys possible reader type.
	 * The default value do associate xml&lt;-&gt;xml and json&lt;-&gt;text
	 */
	reader2ResponseTypes: {
		xml: 'xml',
		json: 'text'
	},
	
	statics: {
		/**
		 * @static
		 * Add a new mapping between a reader type and a response type. This is
		 * necessary to define a new reader to be used by this proxy.
		 * @param {String} readerType The type of the reader
		 * @param {String} responseType The type of the response
		 */
		addReader2ResponseType: function(readerType, responseType)
		{
			this.prototype.reader2ResponseTypes[readerType] = responseType;
		}
	},
	
	doRequest: function(operation, callback, scope)
	{
		var writer  = this.getWriter();
		var request = this.buildRequest(operation);
		
		if (operation.allowWrite()) {
			request = writer.write(request);
		}
		
		Ametys.data.ServerComm.send({
			priority: Ametys.data.ServerComm.PRIORITY_MAJOR,
			plugin: this.plugin,
			workspace: this.workspace,
			url: request.getUrl(),
			parameters: request.getParams(),
			responseType: this.getResponseType(),
			callback: {
				handler: this.createRequestCallback,
				scope: this,
				arguments: {
					request: request,
					operation: operation,
					callback: callback,
					scope: scope
				}
			}
		});
	},
	
	/**
	 * @private
	 * Get the response type for Ametys.data.ServerComm#send depending on the current reader type.
	 * @return {String} A response type accepted by Ametys.data.ServerComm#send. Default value is 'xml'.
	 */
	getResponseType: function()
	{
		return this.reader2ResponseTypes[this.getReader().type || 'xml'];
	},
	
	/**
	 * @private
	 * Creates an intermediary callback for the Ametys.data.ServerComm#send to call the #doRequest callback with the rights arguments
	 * @param {Object} response The response
	 * @param {Object[]} arguments The arguments transmited to the Ametys.data.ServerComm#send call in the #doRequest method.
	 * @return {Function} A callback function at the Ametys.data.ServerComm#send format to call the #doRequest callback
	 */
	createRequestCallback: function(response, arguments)
	{
		var failure = Ametys.data.ServerComm.handleBadResponse(this.errorMessage, 
																response, 
																Ext.getClassName(this) + '.createRequestCallback');
		
		this.processResponse(!failure, arguments.operation, arguments.request.options, response, arguments.callback, arguments.scope);
	},
	
	extractResponseData: function(response)
	{
		var responseType = this.getResponseType();
		
		// Return first child which is the expected root node
		if (responseType == 'xml')
		{
			return Ext.dom.Query.selectNode('*', response)
		}
		else
		{
			return Ext.JSON.decode(Ext.dom.Query.selectValue('', response));
		}
	}
});
