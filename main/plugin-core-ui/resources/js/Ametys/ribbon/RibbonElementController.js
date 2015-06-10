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
 * This abstract class is the super class for an element of the ribbon (a tab or a control).
 * An element of the ribbon is a controller for a ui element (such as Ametys.ui.fluent.ribbon.controls.Button) but is not itself a button.
 */
Ext.define(
	"Ametys.ribbon.RibbonElementController",
	{
		/**
		 * @auto
		 * @cfg {String} id (required) The unique identifier for the element. Cannot be null.
		 */
		/**
		 * @property {String} _id See {@link #cfg-id}
		 * @private
		 */
		
		/**
		 * @auto
		 * @cfg {String} pluginName (required) The name of the plugin that declared the tool. Cannot be null.
		 */
		/**
		 * @property {String} _pluginName See {@link #cfg-pluginName}
		 * @private
		 */

		/**
		 * The constructor should only be called by sub classes
		 * @param {Object} config See the configuration options.
		 */
		constructor: function(config)
		{
			this.initConfig(config);
			
			this._id = config.id;
			this._pluginName = config.pluginName;
		},
	
		/**
		 * Get the identifier of the element provided.
		 * @returns {String} The identifier of the element. Cannot be null.
		 */
		getId: function()
		{
			return this._id;
		},
		
		/**
		 * Get the name of the plugin that defined this element.
		 * @returns {String} The name of the plugin. Cannot be null.
		 */
		getPluginName: function()
		{
			return this._pluginName;
		},
		
		/**
		 * Get the server call role (Java component id).
		 * @protected
		 */
		_getRole: function()
		{
			return this instanceof Ametys.ribbon.element.RibbonTabController ? 'org.ametys.runtime.ribbon.RibbonTabsManager' : 'org.ametys.core.ui.RibbonControlsManager';
		},
		
		/**
		 * This method sends a request to the execute remote method on server.
		 * When the response has arrived, the callback function is invoked.
		 * 
		 * @param {String} methodName The name of the java method to call. The java method must be annotate as "callable". The component use to call this method, is the java class used when declaring this controller in the plugin.xml.
		 * @param {Object[]} parameters The parameters to transmit to the java method. Types are important.
		 * 
		 * @param {Function} callback The function to call when the java process is over. 
		 * @param {Object} callback.returnedValue The returned value of the java call. Can be null if an error occurred (but the callback may not be called on error depending on the errorMessage value).
		 * @param {Object} callback.arguments Other arguments specified in option.arguments
		 * 
		 * @param {Object} [options] Advanced options for the call.
		 * @param {Boolean/String/Object} [options.errorMessage] Display an error message. See Ametys.data.ServerCall.callMethod errorMessage.
		 * @param {Boolean/String/Object} [options.waitMessage] Display a waiting message. See Ametys.data.ServerCall.callMethod waitMessage.
		 * @param {Number} [options.priority] The message priority. See Ametys.data.ServerCall.callMethod for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
		 * @param {String} [options.cancelCode] Cancel similar unachieved read operations. See Ametys.data.ServerCall.callMethod cancelCode.
		 * @param {Object} [options.arguments] Additional arguments set in the callback.arguments parameter.
		 */
		serverCall: function (methodName, parameters, callback, options)
		{
			var role = this._getRole();

			options = options || {};
			
			Ametys.data.ServerComm.callMethod({
				role: role, 
				id: this.getId(), 
				methodName: methodName, 
				parameters: parameters || [],
				callback: {
					handler: callback,
					scope: this,
					arguments: options.arguments
				},
				waitMessage: options.waitMessage,
				errorMessage: options.errorMessage,
				cancelCode: options.cancelCode,
				priority: options.priority
			});
		}
	}
);
