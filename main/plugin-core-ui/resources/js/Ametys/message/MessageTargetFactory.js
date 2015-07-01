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
 * This class is an interface for classes that creates Ametys.message.MessageTarget
 * Sub classes of this interface are used by the Ametys.message.Message constructor to build the messages.
 * 
 * The default implementation is to be used for simple messages that does not need any calculation nor server requests.
 * You have to implement this class if you need a complex message bus, that will autofill: e.g. a content message that get the content automatically using the given id.
 * Beware when implementing this class taht you are in charge for creating subtargets provided if necessary throught #createTargets
 * 
 * You do not have to call theses methods.
 */
Ext.define("Ametys.message.MessageTargetFactory",
	{
		statics:
		{
			/**
			 * @property {Object} _targetFactories The association type (String) and factory (Ametys.message.MessageTargetFactory)
			 * @private
			 */
			_targetFactories: {},
			
			/**
			 * Register a new target factory (used for message creation)
			 * @param {Ametys.message.MessageTargetFactory} factory The factory to register
			 */
			registerTargetFactory: function(factory)
			{
				if (this._targetFactories[factory.getType()] != null && this.getLogger().isWarnEnabled())
				{
					this.getLogger().warn("Replacing factory '" + factory.getType() + "' with a new one");
				}
				else if (this.getLogger().isDebugEnabled())
				{
					this.getLogger().debug("Adding factory '" + factory.getType() + "'");
				}
				
				this._targetFactories[factory.getType()] = factory;
			},
			
			/**
			 * Create the targets for a message
			 * @param {Object/Object[]} targets One or more targets configuration. One configuration can lead to several targets depending on the Ametys.message.MessageTargetFactory implementation used upon its type. An array of config here will return a single level array of targets, but a configuration may contain "subtargets" to recursively add targets.
			 * @param {Object/Object[]} targets.subtargets One or more targets configuration for subtargets of the target. Note that depending on the implementation of the Ametys.message.MessageTargetFactory used (depending on its type) you may obtain subtargets without having to transmit it your self.
			 * @param {Function} callback The function called when all targets are ready.
			 * @param {Ametys.message.MessageTarget[]} callback.targets The targets created. Cannot be null.
			 */
			createTargets: function(targets, callback)
			{
				targets = targets || [];
				if (!Ext.isArray(targets))
				{
					targets = [targets];
				}

				if (targets.length == 0)
				{
					callback([]);
					return;
				}

				// First we want to convert subtargets config to instance of Ametys.message.MessageTarget
				for (var i = 0; i < targets.length; i++)
				{
					var targetConfig = targets[i];
					targetConfig.subtargets = targetConfig.subtargets || [];
					if (!Ext.isArray(targetConfig.subtargets))
					{
						targetConfig.subtargets = [targetConfig.subtargets];
					}

					var subtargetconfig = targetConfig.subtargets[0];
					if (subtargetconfig != null && !(subtargetconfig instanceof Ametys.message.MessageTarget))
					{
						var me = this;
						function subtargetsCreatedCallback(subtargets)
						{
							targetConfig.subtargets = subtargets;
							me.createTargets(targets, callback);
						}
						
						this.createTargets(targetConfig.subtargets, subtargetsCreatedCallback);
						return;
					}
				}				
					
				var called = 0;
				var cumulatedTargetsCreated = [];

				for (var i = 0; i < targets.length; i++)
				{
					var targetConfig = targets[i];
					
					var factory = this._targetFactories[targetConfig.type];
					if (!factory)
					{
						factory = this._targetFactories['*'];
						targetConfig.parameters.type = targetConfig.type
					}
					
					function createTargetsCallback(targetsCreated)
					{
						for (var j=0; j < targetsCreated.length; j++)
						{
							targetsCreated[j].setSubtargets(Ext.Array.merge(targetsCreated[j].getSubtargets(), targetConfig.subtargets));
						}
						
						called++;
						
						cumulatedTargetsCreated = Ext.Array.merge(cumulatedTargetsCreated, targetsCreated);
						
						if (called == targets.length)
						{
							callback(cumulatedTargetsCreated);
						}
					}
					
					factory.createTargets(targetConfig.parameters, createTargetsCallback)
				}
			}
		},
	
		/**
		 * @cfg {String} type (required) The factory role. Cannot be null nor empty.
		 * @auto
		 */
		/**
		 * @property {String} _type See {@link #cfg-type}
		 * @private
		 */
		
		/**
		 * @auto
		 * @cfg {String} id (required) The identifier of the extension in the plugin. Cannot be null
		 */
		/**
		 * @property {String} _id See {@link #cfg-id}
		 * @private
		 */
		
		/**
		 * @auto
		 * @cfg {String} pluginName (required) The name of the plugin that declared the factory. Cannot be null.
		 */
		/**
		 * @property {String} _pluginName See {@link #cfg-pluginName}
		 * @private
		 */
		
		/**
		 * Creates the message target factory. Do not call this.
		 * @param {Object} config See configuration options.
		 */
		constructor: function(config)
		{
			this._type = config.type;
			this._pluginName = config.pluginName;
			this._id = config.id;
		},
		
		/**
		 * Get the type. See {@link #cfg-type}
		 * @returns {String} The type
		 */
		getType: function()
		{
			return this._type;
		},
		
		/**
		 * Get the identifier. See {@link #cfg-id}
		 * @returns {String} The id
		 */
		getId: function()
		{
			return this._id;
		},
		
		/**
		 * Get the plugin name. See {@link #cfg-pluginName}
		 * @returns {String} The plugin name
		 */
		getPluginName: function()
		{
			return this._pluginName;
		},
	
		/**
		 * Implement this method to create the {@link Ametys.message.MessageTarget}.
		 * @param {Object} parameters The parameters needed by the factory to create the messages. Can not be null. See implementation for details.
		 * @param {Function} callback The callback function called when the targets are created. Parameters are
		 * @param {Ametys.message.MessageTarget[]} callback.targets The targets created. Cannot be null.
		 * @template
		 */
		createTargets: function(parameters, callback)
		{
			throw new Error("This method is not implemented in " + this.self.getName());
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
         * @param {Boolean} [options.ignoreCallbackOnError=true] If the server throws an exception, should the callback beeing called with a null parameter.
		 */
		serverCall: function (methodName, parameters, callback, options)
		{
			var role = "org.ametys.runtime.ui.MessageTargetFactoriesManager";

			options = options || {};
			
			Ametys.data.ServerComm.callMethod({
				role: role, 
				id: this.getId(), 
				methodName: methodName, 
				parameters: parameters || [],
				callback: {
					handler: callback,
					scope: this,
					arguments: options.arguments,
                    ignoreOnError: options.ignoreCallbackOnError
				},
				waitMessage: options.waitMessage,
				errorMessage: options.errorMessage,
				cancelCode: options.cancelCode,
				priority: options.priority
			});
		}
	}
);
