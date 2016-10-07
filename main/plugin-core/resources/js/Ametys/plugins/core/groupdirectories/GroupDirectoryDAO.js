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
 * DAO for group directories.
 */
Ext.define('Ametys.plugins.core.groupdirectories.GroupDirectoryDAO', {
    singleton: true,
    
    constructor: function(config)
    {
        /**
         * @callable
         * @member Ametys.plugins.core.groupdirectories.GroupDirectoryDAO
         * @method getGroupDirectoriesIds 
         * Gets the list of the ids of all the population of the application.
         * This calls the method 'getGroupDirectoriesIds' of the server DAO 'GroupDirectoryDAO'.
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
                role: "org.ametys.core.group.GroupDirectoryDAO",
                methodName: "getGroupDirectoriesIds",
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_GROUP_DIRECTORY_DAO_GET_DIRECTORIES_IDS_ERROR}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.groupdirectories.GroupDirectoryDAO
         * @method getEditionConfiguration 
         * Gets the configuration for creating/editing a group directory.
         * This calls the method 'getEditionConfiguration' of the server DAO 'GroupDirectoryDAO'.
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
                role: "org.ametys.core.group.GroupDirectoryDAO",
                methodName: "getEditionConfiguration",
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_GROUP_DIRECTORY_DAO_GET_EDITION_CONFIGURATION_ERROR}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.groupdirectories.GroupDirectoryDAO
         * @method getPopulationParameterValues 
         * Gets the values of the parameters of the given group directory
         * This calls the method 'getPopulationParameterValues' of the server DAO 'GroupDirectoryDAO'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.id The id of the group directory 
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
                role: "org.ametys.core.group.GroupDirectoryDAO",
                methodName: "getGroupDirectoryParameterValues",
                callback: {
                    handler: this._getGroupDirectoryParameterValuesCb
                },
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_GROUP_DIRECTORY_DAO_GET_PARAMETERS_VALUES_ERROR}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.groupdirectories.GroupDirectoryDAO
         * @method add 
         * Adds a new group directory
         * This calls the method 'add' of the server DAO 'GroupDirectoryDAO'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.id The unique id of the group directory
         * @param {String} parameters.label The label of the group directory
         * @param {String} parameters.modelId The id of the group directory model
         * @param {Object} parameters.params The parameters of the group directory
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
                role: "org.ametys.core.group.GroupDirectoryDAO",
                methodName: "add",
                callback: {
                    handler: this._addCb
                },
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_GROUP_DIRECTORY_DAO_ADD_ERROR}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.groupdirectories.GroupDirectoryDAO
         * @method edit 
         * Edits the given group directory
         * This calls the method 'edit' of the server DAO 'GroupDirectoryDAO'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.id The id of the group directory to edit
         * @param {String} parameters.label The label of the group directory
         * @param {String} parameters.modelId The id of the group directory model
         * @param {Object} parameters.params The parameters of the group directory
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
                role: "org.ametys.core.group.GroupDirectoryDAO",
                methodName: "edit",
                callback: {
                    handler: this._editCb
                },
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_GROUP_DIRECTORY_DAO_EDIT_ERROR}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.groupdirectories.GroupDirectoryDAO
         * @method remove 
         * Removes the given group directory
         * This calls the method 'remove' of the server DAO 'GroupDirectoryDAO'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.id The id of the group directory to remove
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
                role: "org.ametys.core.group.GroupDirectoryDAO",
                methodName: "remove",
                callback: {
                    handler: this._removeCb
                },
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_GROUP_DIRECTORY_DAO_REMOVE_ERROR}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.groupdirectories.GroupDirectoryDAO
         * @method getUserPopulationsOnContext 
         * Gets the group directories linked to the given context
         * This calls the method 'getGroupDirectoriesOnContext' of the server DAO 'GroupDirectoryContextHelper'.
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
                role: "org.ametys.core.group.GroupDirectoryContextHelper",
                methodName: "getGroupDirectoriesOnContext",
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_GROUP_DIRECTORY_DAO_GET_GROUP_DIRECTORIES_ON_CONTEXT_ERROR}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.groupdirectories.GroupDirectoryDAO
         * @method link 
         * Links given group directories to a context.
         * This calls the method 'link' of the server DAO 'GroupDirectoryContextHelper'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.context The context
         * @param {String[]} parameters.ids The ids of the group directories to link
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
                role: "org.ametys.core.group.GroupDirectoryContextHelper",
                methodName: "link",
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_GROUP_DIRECTORY_DAO_LINK_ERROR}}",
                    category:Ext.getClassName(this)
                }
            }
        );
    },
    
    /**
     * @private
     * Callback function called after {@link #getGroupDirectoryParameterValues} has been processed.
     * @param {Object} response The server response
     */
    _getGroupDirectoryParameterValuesCb: function(response)
    {
        var error = response['error'] || '';
        if (error == "unknown")
        {
            Ametys.Msg.show({
                title: "{{i18n PLUGINS_CORE_GROUP_DIRECTORY_DAO_ERROR_UNKNOWN_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_GROUP_DIRECTORY_DAO_ERROR_UNKNOWN}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
    },
    
    /**
     * @private
     * Callback function called after {@link #add} has been processed.
     * @param {String} id The id of the created group directory
     */
    _addCb: function(id)
    {
        if (id != null)
        {
            Ext.create('Ametys.message.Message', {
		        type: Ametys.message.Message.CREATED,
		        targets: [{
                    id: Ametys.message.MessageTarget.GROUP_DIRECTORY,
                    parameters: {
                        id: id
                    }
                }]
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
                title: "{{i18n PLUGINS_CORE_GROUP_DIRECTORY_DAO_ERROR_UNKNOWN_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_GROUP_DIRECTORY_DAO_ERROR_UNKNOWN}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (id != null)
        {
            Ext.create('Ametys.message.Message', {
		        type: Ametys.message.Message.MODIFIED,
		        targets: [{
                    id: Ametys.message.MessageTarget.GROUP_DIRECTORY,
                    parameters: {
                        id: id
                    }
                }]
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
        if (error == "unknown")
        {
            Ametys.Msg.show({
                title: "{{i18n PLUGINS_CORE_GROUP_DIRECTORY_DAO_ERROR_UNKNOWN_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_GROUP_DIRECTORY_DAO_ERROR_UNKNOWN}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (id != null)
        {
            Ext.create('Ametys.message.Message', {
		        type: Ametys.message.Message.DELETED,
		        targets: [{
                    id: Ametys.message.MessageTarget.GROUP_DIRECTORY,
                    parameters: {
                        id: id
                    }
                }]
		    });
        }
    }
    
});