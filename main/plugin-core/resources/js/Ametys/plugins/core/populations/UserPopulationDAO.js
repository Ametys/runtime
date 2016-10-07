/*
 *  Copyright 2016 Anyware Services
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
 * DAO for user populations.
 */
Ext.define('Ametys.plugins.core.populations.UserPopulationDAO', {
    singleton: true,
    
    constructor: function(config)
    {
        /**
         * @callable
         * @member Ametys.plugins.core.populations.UserPopulationDAO
         * @method getUserPopulationsIds 
         * Gets the list of the ids of all the population of the application.
         * This calls the method 'getUserPopulationsIds' of the server DAO 'UserPopulationDAO'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
         * @param {String[]} callback.returnedValue The value return from the server. Null on error (please note that when an error occured, the callback may not be called depending on the value of errorMessage).
         * @param {Object} callback.arguments Other arguments specified in option.arguments                 
         * @param {Object} [options] Advanced options for the call.
         * @param {Boolean/String/Object} [options.errorMessage] Display an error message. See Ametys.data.ServerCall#callMethod errorMessage.
         * @param {Boolean/String/Object} [options.waitMessage] Display a waiting message. See Ametys.data.ServerCall#callMethod waitMessage.
         * @param {Number} [options.scope] This parameter is the scope used to call the callback. Moreover is the given class is a mixin of Ametys.data.ServerCaller, its methods #beforeServerCall and #afterServerCall will be used so see their documentation to look for additional options (such a refreshing on Ametys.ribbon.element.ui.ButtonController#beforeServerCall).
         * @param {Number} [options.priority] The message priority. See Ametys.data.ServerCall#callMethod for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
         * @param {String} [options.cancelCode] Cancel similar unachieved read operations. See Ametys.data.ServerCall#callMethod cancelCode.
         * @param {Object} [options.arguments] Additional arguments set in the callback.arguments parameter.                  
         * @param {Boolean} [options.ignoreCallbackOnError] If the server throws an exception, should the callback beeing called with a null parameter. See Ametys.data.ServerCall#callMethod ignoreOnError.
         */
        this.addCallables(
            {
                role: "org.ametys.core.user.population.UserPopulationDAO",
                methodName: "getUserPopulationsIds",
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_GET_POPULATIONS_IDS_ERROR}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.populations.UserPopulationDAO
         * @method getEditionConfiguration 
         * Gets the configuration for creating/editing a user population.
         * This calls the method 'getEditionConfiguration' of the server DAO 'UserPopulationDAO'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
         * @param {Object} callback.returnedValue The value return from the server. Null on error (please note that when an error occured, the callback may not be called depending on the value of errorMessage).
         * @param {Object} callback.arguments Other arguments specified in option.arguments                 
         * @param {Object} [options] Advanced options for the call.
         * @param {Boolean/String/Object} [options.errorMessage] Display an error message. See Ametys.data.ServerCall#callMethod errorMessage.
         * @param {Boolean/String/Object} [options.waitMessage] Display a waiting message. See Ametys.data.ServerCall#callMethod waitMessage.
         * @param {Number} [options.scope] This parameter is the scope used to call the callback. Moreover is the given class is a mixin of Ametys.data.ServerCaller, its methods #beforeServerCall and #afterServerCall will be used so see their documentation to look for additional options (such a refreshing on Ametys.ribbon.element.ui.ButtonController#beforeServerCall).
         * @param {Number} [options.priority] The message priority. See Ametys.data.ServerCall#callMethod for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
         * @param {String} [options.cancelCode] Cancel similar unachieved read operations. See Ametys.data.ServerCall#callMethod cancelCode.
         * @param {Object} [options.arguments] Additional arguments set in the callback.arguments parameter.                  
         * @param {Boolean} [options.ignoreCallbackOnError] If the server throws an exception, should the callback beeing called with a null parameter. See Ametys.data.ServerCall#callMethod ignoreOnError.
         */
        this.addCallables(
            {
                role: "org.ametys.core.user.population.UserPopulationDAO",
                methodName: "getEditionConfiguration",
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_GET_EDITION_CONFIGURATION_ERROR}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.populations.UserPopulationDAO
         * @method getPopulationParameterValues 
         * Gets the values of the parameters of the given population
         * This calls the method 'getPopulationParameterValues' of the server DAO 'UserPopulationDAO'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.id The id of the population 
         * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
         * @param {Object} callback.returnedValue The value return from the server. Null on error (please note that when an error occured, the callback may not be called depending on the value of errorMessage).
         * @param {Object} callback.arguments Other arguments specified in option.arguments                 
         * @param {Object} [options] Advanced options for the call.
         * @param {Boolean/String/Object} [options.errorMessage] Display an error message. See Ametys.data.ServerCall#callMethod errorMessage.
         * @param {Boolean/String/Object} [options.waitMessage] Display a waiting message. See Ametys.data.ServerCall#callMethod waitMessage.
         * @param {Number} [options.scope] This parameter is the scope used to call the callback. Moreover is the given class is a mixin of Ametys.data.ServerCaller, its methods #beforeServerCall and #afterServerCall will be used so see their documentation to look for additional options (such a refreshing on Ametys.ribbon.element.ui.ButtonController#beforeServerCall).
         * @param {Number} [options.priority] The message priority. See Ametys.data.ServerCall#callMethod for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
         * @param {String} [options.cancelCode] Cancel similar unachieved read operations. See Ametys.data.ServerCall#callMethod cancelCode.
         * @param {Object} [options.arguments] Additional arguments set in the callback.arguments parameter.                  
         * @param {Boolean} [options.ignoreCallbackOnError] If the server throws an exception, should the callback beeing called with a null parameter. See Ametys.data.ServerCall#callMethod ignoreOnError.
         */
        this.addCallables(
            {
                role: "org.ametys.core.user.population.UserPopulationDAO",
                methodName: "getPopulationParameterValues",
                callback: {
                    handler: this._getPopulationParameterValuesCb
                },
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_GET_POPULATION_PARAMETERS_VALUES_ERROR}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.populations.UserPopulationDAO
         * @method add 
         * Adds a new population
         * This calls the method 'add' of the server DAO 'UserPopulationDAO'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.id The unique id of the population
         * @param {String} parameters.label The label of the population
         * @param {Object[]} parameters.userDirectories A list of user directory parameters
         * @param {Objec[]t} parameters.credentialProviders A list of credential provider parameters
         * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
         * @param {Object} callback.returnedValue The value return from the server. Null on error (please note that when an error occured, the callback may not be called depending on the value of errorMessage).
         * @param {Object} callback.arguments Other arguments specified in option.arguments                 
         * @param {Object} [options] Advanced options for the call.
         * @param {Boolean/String/Object} [options.errorMessage] Display an error message. See Ametys.data.ServerCall#callMethod errorMessage.
         * @param {Boolean/String/Object} [options.waitMessage] Display a waiting message. See Ametys.data.ServerCall#callMethod waitMessage.
         * @param {Number} [options.scope] This parameter is the scope used to call the callback. Moreover is the given class is a mixin of Ametys.data.ServerCaller, its methods #beforeServerCall and #afterServerCall will be used so see their documentation to look for additional options (such a refreshing on Ametys.ribbon.element.ui.ButtonController#beforeServerCall).
         * @param {Number} [options.priority] The message priority. See Ametys.data.ServerCall#callMethod for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
         * @param {String} [options.cancelCode] Cancel similar unachieved read operations. See Ametys.data.ServerCall#callMethod cancelCode.
         * @param {Object} [options.arguments] Additional arguments set in the callback.arguments parameter.                  
         * @param {Boolean} [options.ignoreCallbackOnError] If the server throws an exception, should the callback beeing called with a null parameter. See Ametys.data.ServerCall#callMethod ignoreOnError.
         */
        this.addCallables(
            {
                role: "org.ametys.core.user.population.UserPopulationDAO",
                methodName: "add",
                callback: {
                    handler: this._addCb
                },
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_ADD_ERROR}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.populations.UserPopulationDAO
         * @method edit 
         * Edits the given population
         * This calls the method 'edit' of the server DAO 'UserPopulationDAO'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.id The id of the population to edit
         * @param {String} parameters.label The label of the population
         * @param {Object[]} parameters.userDirectories A list of user directory parameters
         * @param {Objec[]t} parameters.credentialProviders A list of credential provider parameters
         * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
         * @param {Object} callback.returnedValue The value return from the server. Null on error (please note that when an error occured, the callback may not be called depending on the value of errorMessage).
         * @param {Object} callback.arguments Other arguments specified in option.arguments                 
         * @param {Object} [options] Advanced options for the call.
         * @param {Boolean/String/Object} [options.errorMessage] Display an error message. See Ametys.data.ServerCall#callMethod errorMessage.
         * @param {Boolean/String/Object} [options.waitMessage] Display a waiting message. See Ametys.data.ServerCall#callMethod waitMessage.
         * @param {Number} [options.scope] This parameter is the scope used to call the callback. Moreover is the given class is a mixin of Ametys.data.ServerCaller, its methods #beforeServerCall and #afterServerCall will be used so see their documentation to look for additional options (such a refreshing on Ametys.ribbon.element.ui.ButtonController#beforeServerCall).
         * @param {Number} [options.priority] The message priority. See Ametys.data.ServerCall#callMethod for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
         * @param {String} [options.cancelCode] Cancel similar unachieved read operations. See Ametys.data.ServerCall#callMethod cancelCode.
         * @param {Object} [options.arguments] Additional arguments set in the callback.arguments parameter.                  
         * @param {Boolean} [options.ignoreCallbackOnError] If the server throws an exception, should the callback beeing called with a null parameter. See Ametys.data.ServerCall#callMethod ignoreOnError.
         */
        this.addCallables(
            {
                role: "org.ametys.core.user.population.UserPopulationDAO",
                methodName: "edit",
                callback: {
                    handler: this._editCb
                },
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_EDIT_ERROR}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.populations.UserPopulationDAO
         * @method remove 
         * Removes the given population
         * This calls the method 'remove' of the server DAO 'UserPopulationDAO'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.id The id of the population to remove
         * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
         * @param {Object} callback.returnedValue The value return from the server. Null on error (please note that when an error occured, the callback may not be called depending on the value of errorMessage).
         * @param {Object} callback.arguments Other arguments specified in option.arguments                 
         * @param {Object} [options] Advanced options for the call.
         * @param {Boolean/String/Object} [options.errorMessage] Display an error message. See Ametys.data.ServerCall#callMethod errorMessage.
         * @param {Boolean/String/Object} [options.waitMessage] Display a waiting message. See Ametys.data.ServerCall#callMethod waitMessage.
         * @param {Number} [options.scope] This parameter is the scope used to call the callback. Moreover is the given class is a mixin of Ametys.data.ServerCaller, its methods #beforeServerCall and #afterServerCall will be used so see their documentation to look for additional options (such a refreshing on Ametys.ribbon.element.ui.ButtonController#beforeServerCall).
         * @param {Number} [options.priority] The message priority. See Ametys.data.ServerCall#callMethod for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
         * @param {String} [options.cancelCode] Cancel similar unachieved read operations. See Ametys.data.ServerCall#callMethod cancelCode.
         * @param {Object} [options.arguments] Additional arguments set in the callback.arguments parameter.                  
         * @param {Boolean} [options.ignoreCallbackOnError] If the server throws an exception, should the callback beeing called with a null parameter. See Ametys.data.ServerCall#callMethod ignoreOnError.
         */
        this.addCallables(
            {
                role: "org.ametys.core.user.population.UserPopulationDAO",
                methodName: "remove",
                callback: {
                    handler: this._removeCb
                },
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_REMOVE_ERROR}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.populations.UserPopulationDAO
         * @method enable 
         * Enables/Disables the given population
         * This calls the method 'enable' of the server DAO 'UserPopulationDAO'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.id The id of the population to enable/disable
         * @param {String} parameters.enabled True to enable the population, false to disable it.
         * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
         * @param {Object} callback.returnedValue The value return from the server. Null on error (please note that when an error occured, the callback may not be called depending on the value of errorMessage).
         * @param {Object} callback.arguments Other arguments specified in option.arguments                 
         * @param {Object} [options] Advanced options for the call.
         * @param {Boolean/String/Object} [options.errorMessage] Display an error message. See Ametys.data.ServerCall#callMethod errorMessage.
         * @param {Boolean/String/Object} [options.waitMessage] Display a waiting message. See Ametys.data.ServerCall#callMethod waitMessage.
         * @param {Number} [options.scope] This parameter is the scope used to call the callback. Moreover is the given class is a mixin of Ametys.data.ServerCaller, its methods #beforeServerCall and #afterServerCall will be used so see their documentation to look for additional options (such a refreshing on Ametys.ribbon.element.ui.ButtonController#beforeServerCall).
         * @param {Number} [options.priority] The message priority. See Ametys.data.ServerCall#callMethod for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
         * @param {String} [options.cancelCode] Cancel similar unachieved read operations. See Ametys.data.ServerCall#callMethod cancelCode.
         * @param {Object} [options.arguments] Additional arguments set in the callback.arguments parameter.                  
         * @param {Boolean} [options.ignoreCallbackOnError] If the server throws an exception, should the callback beeing called with a null parameter. See Ametys.data.ServerCall#callMethod ignoreOnError.
         */
        this.addCallables(
            {
                role: "org.ametys.core.user.population.UserPopulationDAO",
                methodName: "enable",
                callback: {
                    handler: this._editCb
                },
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_ENABLE_ERROR}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.populations.UserPopulationDAO
         * @method isEnabled 
         * Returns the enabled state of the given population
         * This calls the method 'isEnabled' of the server DAO 'UserPopulationDAO'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.id The id of the population to retrieve state
         * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
         * @param {Object} callback.returnedValue The value return from the server. Null on error (please note that when an error occured, the callback may not be called depending on the value of errorMessage).
         * @param {Object} callback.arguments Other arguments specified in option.arguments                 
         * @param {Object} [options] Advanced options for the call.
         * @param {Boolean/String/Object} [options.errorMessage] Display an error message. See Ametys.data.ServerCall#callMethod errorMessage.
         * @param {Boolean/String/Object} [options.waitMessage] Display a waiting message. See Ametys.data.ServerCall#callMethod waitMessage.
         * @param {Number} [options.scope] This parameter is the scope used to call the callback. Moreover is the given class is a mixin of Ametys.data.ServerCaller, its methods #beforeServerCall and #afterServerCall will be used so see their documentation to look for additional options (such a refreshing on Ametys.ribbon.element.ui.ButtonController#beforeServerCall).
         * @param {Number} [options.priority] The message priority. See Ametys.data.ServerCall#callMethod for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
         * @param {String} [options.cancelCode] Cancel similar unachieved read operations. See Ametys.data.ServerCall#callMethod cancelCode.
         * @param {Object} [options.arguments] Additional arguments set in the callback.arguments parameter.                  
         * @param {Boolean} [options.ignoreCallbackOnError] If the server throws an exception, should the callback beeing called with a null parameter. See Ametys.data.ServerCall#callMethod ignoreOnError.
         */
        this.addCallables(
            {
                role: "org.ametys.core.user.population.UserPopulationDAO",
                methodName: "isEnabled",
                callback: {
                    handler: this._isEnabledCb
                },
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_IS_ENABLED_ERROR}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.populations.UserPopulationDAO
         * @method isValid 
         * Returns the enabled state of the given population
         * This calls the method 'isValid' of the server DAO 'UserPopulationDAO'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.id The id of the population to retrieve state
         * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
         * @param {Boolean} callback.returnedValue true if the population has a valid configuration                
         * @param {Object} [options] Advanced options for the call.
         * @param {Boolean/String/Object} [options.errorMessage] Display an error message. See Ametys.data.ServerCall#callMethod errorMessage.
         * @param {Boolean/String/Object} [options.waitMessage] Display a waiting message. See Ametys.data.ServerCall#callMethod waitMessage.
         * @param {Number} [options.scope] This parameter is the scope used to call the callback. Moreover is the given class is a mixin of Ametys.data.ServerCaller, its methods #beforeServerCall and #afterServerCall will be used so see their documentation to look for additional options (such a refreshing on Ametys.ribbon.element.ui.ButtonController#beforeServerCall).
         * @param {Number} [options.priority] The message priority. See Ametys.data.ServerCall#callMethod for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
         * @param {String} [options.cancelCode] Cancel similar unachieved read operations. See Ametys.data.ServerCall#callMethod cancelCode.
         * @param {Object} [options.arguments] Additional arguments set in the callback.arguments parameter.                  
         * @param {Boolean} [options.ignoreCallbackOnError] If the server throws an exception, should the callback beeing called with a null parameter. See Ametys.data.ServerCall#callMethod ignoreOnError.
         */
        this.addCallables(
            {
                role: "org.ametys.core.user.population.UserPopulationDAO",
                methodName: "isValid",
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_IS_VALID_ERROR}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.populations.UserPopulationDAO
         * @method isInUse 
         * Returns true if the user population is currently used.
         * This calls the method 'isInUse' of the server DAO 'PopulationConsumerExtensionPoint'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.id The id of the users population to check
         * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
         * @param {Object} callback.returnedValue The value return from the server. Null on error (please note that when an error occured, the callback may not be called depending on the value of errorMessage).
         * @param {Object} callback.arguments Other arguments specified in option.arguments                 
         * @param {Object} [options] Advanced options for the call.
         * @param {Boolean/String/Object} [options.errorMessage] Display an error message. See Ametys.data.ServerCall#callMethod errorMessage.
         * @param {Boolean/String/Object} [options.waitMessage] Display a waiting message. See Ametys.data.ServerCall#callMethod waitMessage.
         * @param {Number} [options.scope] This parameter is the scope used to call the callback. Moreover is the given class is a mixin of Ametys.data.ServerCaller, its methods #beforeServerCall and #afterServerCall will be used so see their documentation to look for additional options (such a refreshing on Ametys.ribbon.element.ui.ButtonController#beforeServerCall).
         * @param {Number} [options.priority] The message priority. See Ametys.data.ServerCall#callMethod for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
         * @param {String} [options.cancelCode] Cancel similar unachieved read operations. See Ametys.data.ServerCall#callMethod cancelCode.
         * @param {Object} [options.arguments] Additional arguments set in the callback.arguments parameter.                  
         * @param {Boolean} [options.ignoreCallbackOnError] If the server throws an exception, should the callback beeing called with a null parameter. See Ametys.data.ServerCall#callMethod ignoreOnError.
         */
        this.addCallables(
            {
                role: "org.ametys.core.user.population.PopulationConsumerExtensionPoint",
                methodName: "isInUse",
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_IS_IN_USE_ERROR}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.populations.UserPopulationDAO
         * @method getUserPopulationsOnContext 
         * Gets the populations linked to the given context
         * This calls the method 'getUserPopulationsOnContext' of the server DAO 'PopulationContextHelper'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.context The context
         * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
         * @param {Object} callback.returnedValue The value return from the server. Null on error (please note that when an error occured, the callback may not be called depending on the value of errorMessage).
         * @param {Object} callback.arguments Other arguments specified in option.arguments                 
         * @param {Object} [options] Advanced options for the call.
         * @param {Boolean/String/Object} [options.errorMessage] Display an error message. See Ametys.data.ServerCall#callMethod errorMessage.
         * @param {Boolean/String/Object} [options.waitMessage] Display a waiting message. See Ametys.data.ServerCall#callMethod waitMessage.
         * @param {Number} [options.scope] This parameter is the scope used to call the callback. Moreover is the given class is a mixin of Ametys.data.ServerCaller, its methods #beforeServerCall and #afterServerCall will be used so see their documentation to look for additional options (such a refreshing on Ametys.ribbon.element.ui.ButtonController#beforeServerCall).
         * @param {Number} [options.priority] The message priority. See Ametys.data.ServerCall#callMethod for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
         * @param {String} [options.cancelCode] Cancel similar unachieved read operations. See Ametys.data.ServerCall#callMethod cancelCode.
         * @param {Object} [options.arguments] Additional arguments set in the callback.arguments parameter.                  
         * @param {Boolean} [options.ignoreCallbackOnError] If the server throws an exception, should the callback beeing called with a null parameter. See Ametys.data.ServerCall#callMethod ignoreOnError.
         */
        this.addCallables(
            {
                role: "org.ametys.core.user.population.PopulationContextHelper",
                methodName: "getUserPopulationsOnContext",
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_GET_USER_POPULATIONS_ON_CONTEXT_ERROR}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.populations.UserPopulationDAO
         * @method link 
         * Links given populations to a context.
         * This calls the method 'link' of the server DAO 'PopulationContextHelper'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.context The context
         * @param {String[]} parameters.ids The ids of the populations to link
         * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
         * @param {Object} callback.returnedValue The value return from the server. Null on error (please note that when an error occured, the callback may not be called depending on the value of errorMessage).
         * @param {Object} callback.arguments Other arguments specified in option.arguments                 
         * @param {Object} [options] Advanced options for the call.
         * @param {Boolean/String/Object} [options.errorMessage] Display an error message. See Ametys.data.ServerCall#callMethod errorMessage.
         * @param {Boolean/String/Object} [options.waitMessage] Display a waiting message. See Ametys.data.ServerCall#callMethod waitMessage.
         * @param {Number} [options.scope] This parameter is the scope used to call the callback. Moreover is the given class is a mixin of Ametys.data.ServerCaller, its methods #beforeServerCall and #afterServerCall will be used so see their documentation to look for additional options (such a refreshing on Ametys.ribbon.element.ui.ButtonController#beforeServerCall).
         * @param {Number} [options.priority] The message priority. See Ametys.data.ServerCall#callMethod for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
         * @param {String} [options.cancelCode] Cancel similar unachieved read operations. See Ametys.data.ServerCall#callMethod cancelCode.
         * @param {Object} [options.arguments] Additional arguments set in the callback.arguments parameter.                  
         * @param {Boolean} [options.ignoreCallbackOnError] If the server throws an exception, should the callback beeing called with a null parameter. See Ametys.data.ServerCall#callMethod ignoreOnError.
         */
        this.addCallables(
            {
                role: "org.ametys.core.user.population.PopulationContextHelper",
                methodName: "link",
                callback: {
                    handler: this._linkCb
                },
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_LINK_ERROR}}",
                    category:Ext.getClassName(this)
                }
            }
        );
    },
    
    /**
     * @private
     * Callback function called after {@link #getPopulationParameterValues} has been processed.
     * @param {Object} response The server response
     */
    _getPopulationParameterValuesCb: function(response)
    {
        var error = response['error'] || '';
        if (error == "unknown")
        {
            Ametys.Msg.show({
                title: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_ERROR_UNKNOWN_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_ERROR_UNKNOWN}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
    },
    
    /**
     * @private
     * Callback function called after {@link #add} has been processed.
     * @param {String} id The id of the created population
     */
    _addCb: function(id)
    {
        if (id != null)
        {
            Ext.create('Ametys.message.Message', {
		        type: Ametys.message.Message.CREATED,
		        targets: [{
                    id: Ametys.message.MessageTarget.USER_POPULATION,
                    parameters: {
                        id: id
                    }
                }]
		    });
        }
        else
        {
            Ametys.Msg.show({
                title: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_ERROR_SERVER_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_ERROR_SERVER}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
    },
    
    /**
     * @private
     * Callback function called after {@link #edit} has been processed.
     * @param {Object} response The server response
     */
    _editCb: function(response)
    {
        var error = response['error'] || '';
        var id = response['id'];
        if (error == "unknown")
        {
            Ametys.Msg.show({
                title: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_ERROR_UNKNOWN_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_ERROR_UNKNOWN}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (error == "server")
        {
            Ametys.Msg.show({
                title: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_ERROR_SERVER_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_ERROR_SERVER}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (id != null)
        {
            Ext.create('Ametys.message.Message', {
		        type: Ametys.message.Message.MODIFIED,
		        targets: [{
                    id: Ametys.message.MessageTarget.USER_POPULATION,
                    parameters: {
                        id: id
                    }
                }]
		    });
        }
    },
    
    /**
     * @private
     * Callback function called after {@link #link} has been processed.
     * @param {Object} response The server response
     */
    _linkCb: function(response)
    {
        if (response != null && response.length > 0)
        {
            var targets = Ext.Array.map(response, function(id) {
                return {
                    id: Ametys.message.MessageTarget.USER_POPULATION,
                    parameters: {
                        id: id
                    }
                };
            }, this);
            
            Ext.create('Ametys.message.Message', {
		        type: Ametys.message.Message.MODIFIED,
		        targets: targets
		    });
        }
    },
    
    /**
     * @private
     * Callback function called after {@link #remove} has been processed.
     * @param {Object} response The server response
     */
    _removeCb: function(response)
    {
        var error = response['error'] || '';
        var id = response['id'];
        if (error == 'used')
        {
            Ametys.Msg.show({
                title: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_ERROR_IS_USED_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_ERROR_IS_USED}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (error == "unknown")
        {
            Ametys.Msg.show({
                title: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_ERROR_UNKNOWN_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_ERROR_UNKNOWN}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (error == "server")
        {
            Ametys.Msg.show({
                title: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_ERROR_SERVER_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_ERROR_SERVER}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (id != null)
        {
            Ext.create('Ametys.message.Message', {
		        type: Ametys.message.Message.DELETED,
		        targets: [{
                    id: Ametys.message.MessageTarget.USER_POPULATION,
                    parameters: {
                        id: id
                    }
                }]
		    });
        }
    },
    
    /**
     * @private
     * Callback function called after {@link #isEnabled} has been processed.
     * @param {Object} response The server response
     */
    _isEnabledCb: function(response)
    {
        var error = response['error'] || '';
        if (error == "unknown")
        {
            Ametys.Msg.show({
                title: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_ERROR_UNKNOWN_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_USER_POPULATION_DAO_ERROR_UNKNOWN}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
            return;
        }
    }
    
});