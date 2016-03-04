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
 * This tool displays the list of data sources 
 * @private
 */
Ext.define('Ametys.plugins.core.datasource.DataSourceDAO', {
	
	singleton: true,

	constructor: function(config)
	{
		/**
    	 * @callable
    	 * @member Ametys.plugins.core.datasource.DataSourceDAO
    	 * @method getDataSource
    	 * Retrieve a data source from its id
    	 * This calls the method 'getSQLDataSource' of the server DAO 'org.ametys.core.datasource.DataSourceClientInteraction'.
    	 * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.type The type of data source to retrieve
    	 * @param {String} parameters.id The id of the data source to retrieve
    	 * @param {Function} callback The function to call when the java process is over. Can be null. Use options.scope for the scope. 
     	 * @param {Object} callback.returnedValue The data source information. Null on error (please note that when an error occurred, the callback may not be called depending on the value of errorMessage).
		 * @param {Object} callback.args Other arguments specified in option.arguments
		 * @param {Object[]} callback.parameters the parameters the server was called with
		 * @param {Object} [options] Advanced options for the call.
		 * @param {Boolean/String/Object} [options.errorMessage] Display an error message. See Ametys.data.ServerCall#callMethod errorMessage.
		 * @param {Boolean/String/Object} [options.waitMessage] Display a waiting message. See Ametys.data.ServerCall#callMethod waitMessage.
		 * @param {Number} [options.scope] This parameter is the scope used to call the callback. Moreover is the given class is a mixin of Ametys.data.ServerCaller, its methods #beforeServerCall and #afterServerCall will be used so see their documentation to look for additional options (such a refreshing on Ametys.ribbon.element.ui.ButtonController#beforeServerCall).
		 * @param {Number} [options.priority] The message priority. See Ametys.data.ServerCall#callMethod for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
		 * @param {String} [options.cancelCode] Cancel similar unachieved read operations. See Ametys.data.ServerCall#callMethod cancelCode.
		 * @param {Object} [options.arguments] Additional arguments set in the callback.arguments parameter.                  
		 * @param {Boolean} [options.ignoreCallbackOnError] If the server throws an exception, should the callback be called with a null parameter. See Ametys.data.ServerCall#callMethod ignoreOnError.
    	 */
		this.addCallables({
		    role: "org.ametys.core.datasource.DataSourceClientInteraction",
			methodName: "getDataSource",
     		errorMessage: {
				category: this.self.getName(),
				msg: "{{i18n PLUGINS_CORE_DATASOURCEDAO_GET_DATASOURCE_ERROR}}"
			}
		});
		
		/**
    	 * @callable
    	 * @member Ametys.plugins.core.datasource.DataSourceDAO
    	 * @method addDataSource
    	 * Add a data source
    	 * This calls the method 'addDataSource' of the server DAO 'org.ametys.core.datasource.DataSourceClientInteraction'.
    	 * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.type (required) The type of data source to create
         * @param {Object} parameters.dsParams The parameters of the data source. It should contains at least the following parameters:
    	 * @param {String} parameters.dsParams.name the name of the data source
		 * @param {String} parameters.dsParams.description the optional description of the data source
		 * @param {Boolean} parameters.dsParams.private is the data source public or private ?
    	 * @param {Function} callback The function to call when the java process is over. Can be null. Use options.scope for the scope. 
    	 * @param {Object} callback.datasource The data source information. Null on error (please note that when an error occurred, the callback may not be called depending on the value of errorMessage).
    	 * @param {Object} callback.args Other arguments specified in option.arguments
		 * @param {Object[]} callback.parameters the parameters the server was called with 
		 * @param {Object} [options] Advanced options for the call.
		 * @param {Boolean/String/Object} [options.errorMessage] Display an error message. See Ametys.data.ServerCall#callMethod errorMessage.
		 * @param {Boolean/String/Object} [options.waitMessage] Display a waiting message. See Ametys.data.ServerCall#callMethod waitMessage.
		 * @param {Number} [options.scope] This parameter is the scope used to call the callback. Moreover is the given class is a mixin of Ametys.data.ServerCaller, its methods #beforeServerCall and #afterServerCall will be used so see their documentation to look for additional options (such a refreshing on Ametys.ribbon.element.ui.ButtonController#beforeServerCall).
		 * @param {Number} [options.priority] The message priority. See Ametys.data.ServerCall#callMethod for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
		 * @param {String} [options.cancelCode] Cancel similar unachieved read operations. See Ametys.data.ServerCall#callMethod cancelCode.
		 * @param {Object} [options.arguments] Additional arguments set in the callback.arguments parameter.                  
		 * @param {Boolean} [options.ignoreCallbackOnError] If the server throws an exception, should the callback be called with a null parameter. See Ametys.data.ServerCall#callMethod ignoreOnError.
    	 */
		this.addCallables({
		    role: "org.ametys.core.datasource.DataSourceClientInteraction",
			methodName: "addDataSource",
			callback: {
         		handler: this._addDataSourceCb
     		},
     		errorMessage: {
				category: this.self.getName(),
				msg: "{{i18n PLUGINS_CORE_DATASOURCEDAO_ADD_DATASOURCE_ERROR}}"
			}
		});
		
		/**
    	 * @callable
    	 * @member Ametys.plugins.core.datasource.DataSourceDAO
    	 * @method editDataSource
    	 * Edit a data source
    	 * This calls the method 'editDataSource' of the server DAO 'org.ametys.core.datasource.DataSourceClientInteraction'.
    	 * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.type (required) The type of data source to edit
         * @param {Object} parameters.dsParams The parameters of the data source. It should contains at least the following parameters:
         * @param {String} parameters.dsParams.id the id of the data source
         * @param {String} parameters.dsParams.name the name of the data source
         * @param {String} parameters.dsParams.description the optional description of the data source
         * @param {Boolean} parameters.dsParams.private is the data source public or private ?
    	 * @param {Function} callback The function to call when the java process is over. Can be null. Use options.scope for the scope. 
     	 * @param {Object} callback.datasource The data source information. Null on error (please note that when an error occurred, the callback may not be called depending on the value of errorMessage).
		 * @param {Object} callback.args Other arguments specified in option.arguments with
		 * @param {Object[]} callback.parameters the parameters the server was called with
		 * @param {Object} [options] Advanced options for the call.
		 * @param {Boolean/String/Object} [options.errorMessage] Display an error message. See Ametys.data.ServerCall#callMethod errorMessage.
		 * @param {Boolean/String/Object} [options.waitMessage] Display a waiting message. See Ametys.data.ServerCall#callMethod waitMessage.
		 * @param {Number} [options.scope] This parameter is the scope used to call the callback. Moreover is the given class is a mixin of Ametys.data.ServerCaller, its methods #beforeServerCall and #afterServerCall will be used so see their documentation to look for additional options (such a refreshing on Ametys.ribbon.element.ui.ButtonController#beforeServerCall).
		 * @param {Number} [options.priority] The message priority. See Ametys.data.ServerCall#callMethod for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
		 * @param {String} [options.cancelCode] Cancel similar unachieved read operations. See Ametys.data.ServerCall#callMethod cancelCode.
		 * @param {Object} [options.arguments] Additional arguments set in the callback.arguments parameter.                  
		 * @param {Boolean} [options.ignoreCallbackOnError] If the server throws an exception, should the callback be called with a null parameter. See Ametys.data.ServerCall#callMethod ignoreOnError.
    	 */
		this.addCallables({
			role: "org.ametys.core.datasource.DataSourceClientInteraction",
			methodName: "editDataSource",
			callback: {
         		handler: this._editDataSourceCb
     		},
     		errorMessage: {
				category: this.self.getName(),
				msg: "{{i18n PLUGINS_CORE_DATASOURCEDAO_EDIT_DATASOURCE_ERROR}}"
			}
		});
		
		/**
    	 * @callable
    	 * @member Ametys.plugins.core.datasource.DataSourceDAO
    	 * @method removeDataSource
    	 * Remove the selected sql data source
    	 * This calls the method 'removeDataSource' of the server DAO 'org.ametys.core.datasource.DataSourceClientInteraction'.
    	 * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.type (required) The type of data source to create
         * @param {String[]} parameters.ids The ids of data source to delete
    	 * @param {Function} callback The function to call when the java process is over. Can be null. Use options.scope for the scope. 
		 * @param {Object} callback.args Other arguments specified in option.arguments with
		 * @param {Object[]} callback.parameters the parameters the server was called with
		 * @param {Object} [options] Advanced options for the call.
		 * @param {Boolean/String/Object} [options.errorMessage] Display an error message. See Ametys.data.ServerCall#callMethod errorMessage.
		 * @param {Boolean/String/Object} [options.waitMessage] Display a waiting message. See Ametys.data.ServerCall#callMethod waitMessage.
		 * @param {Number} [options.scope] This parameter is the scope used to call the callback. Moreover is the given class is a mixin of Ametys.data.ServerCaller, its methods #beforeServerCall and #afterServerCall will be used so see their documentation to look for additional options (such a refreshing on Ametys.ribbon.element.ui.ButtonController#beforeServerCall).
		 * @param {Number} [options.priority] The message priority. See Ametys.data.ServerCall#callMethod for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
		 * @param {String} [options.cancelCode] Cancel similar unachieved read operations. See Ametys.data.ServerCall#callMethod cancelCode.
		 * @param {Object} [options.arguments] Additional arguments set in the callback.arguments parameter.                  
		 * @param {Boolean} [options.ignoreCallbackOnError] If the server throws an exception, should the callback be called with a null parameter. See Ametys.data.ServerCall#callMethod ignoreOnError.
    	 */
		this.addCallables({
			role: "org.ametys.core.datasource.DataSourceClientInteraction",
			methodName: "removeDataSource",
			callback: {
         		handler: this._removeDataSourceCb
     		},
     		errorMessage: {
				category: this.self.getName(),
				msg: "{{i18n PLUGINS_CORE_DATASOURCEDAO_DELETE_DATASOURCE_ERROR}}"
			}
		});
	},
	
 	/**
	 * @private
	 * Callback function invoked after a data source is created
	 * @param {Object} datasource the created data source
	 * @param {Object} args The callback arguments
	 * @param {Object[]} params The call parameters 
	 */
	_addDataSourceCb: function(datasource, args, params)
	{
		Ext.create('Ametys.message.Message', {
			type: Ametys.message.Message.CREATED,
			targets: {
				type: Ametys.message.MessageTarget.DATASOURCE,
				parameters: {
					id: datasource.id,
					type: params[0]
				}
			}
		});
	},
	
 	/**
	 * @private
	 * Callback function invoked after the SQL data source is edited
	 * @param {Object} datasource the created data source
	 * @param {Object} args The callback arguments
	 * @param {Object[]} params The call parameters 
	 */
	_editDataSourceCb: function(datasource, args, params)
	{
		Ext.create('Ametys.message.Message', {
			type: Ametys.message.Message.MODIFIED,
			targets: {
				type: Ametys.message.MessageTarget.DATASOURCE,
				parameters: {
					id: datasource.id,
					type: params[0]
				}
			}
		});
	},
	
 	/**
	 * @private
	 * Callback function invoked after data sources were deleted
	 * @param {Object} response the server's response
	 * @param {Object} args The callback arguments
	 * @param {Object[]} params The call parameters
	 */
	_removeDataSourceCb: function(response, args, params)
	{
		var ids = params[1];
		
        var targets = [];
        
		Ext.Array.each(ids, function(id){
			targets.push({
                type: Ametys.message.MessageTarget.DATASOURCE,
                parameters: {
                    id: id,
                    type: params[0]
                }
            });
		});
        
        Ext.create('Ametys.message.Message', {
            type: Ametys.message.Message.DELETED,
            targets: targets
        });
	}
 	
});

Ext.define('Ametys.message.DataSourceMessageTarget', {
	
	override: 'Ametys.message.MessageTarget',
	statics: 
	{
		/**
		 * @member Ametys.message.MessageTarget
		 * @readonly
		 * @property {String} DATASOURCE The target of the message is a data source. The expected parameters are: 
	 	 * @property {String} DATASOURCE.id The id of the data source
	 	 * @property {String} DATASOURCE.type The type of the data source
		 */
		DATASOURCE: "datasource"
	}
});

