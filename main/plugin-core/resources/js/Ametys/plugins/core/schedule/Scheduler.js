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
 * The client-side for Scheduler component.
 */
Ext.define('Ametys.plugins.core.schedule.Scheduler', {
    singleton: true,
    
    constructor: function(config)
    {
        /**
         * @callable
         * @member Ametys.plugins.core.schedule.Scheduler
         * @method getTasksInformation 
         * Gets tasks information
         * This calls the method 'getTasksInformation' of the server DAO 'Scheduler'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String[]} parameters.taskids The ids of the tasks
         * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
         * @param {Ametys.plugins.coreui.schedule.Task[]} callback.returnedValue The value return from the server. Null on error (please note that when an error occured, the callback may not be called depending on the value of errorMessage).
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
                role: "org.ametys.plugins.core.schedule.Scheduler",
                methodName: "getTasksInformation",
                convertor: this._convertTasks,
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_MESSAGE}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.schedule.Scheduler
         * @method getEditionConfiguration 
         * Gets the configuration for creating/editing a runnable.
         * This calls the method 'getEditionConfiguration' of the server DAO 'Scheduler'.
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
                role: "org.ametys.plugins.core.schedule.Scheduler",
                methodName: "getEditionConfiguration",
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_MESSAGE}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.schedule.Scheduler
         * @method getParameterValues 
         * Gets the values of the parameters of the given task.
         * This calls the method 'getParameterValues' of the server DAO 'Scheduler'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.id The id of the task
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
                role: "org.ametys.plugins.core.schedule.Scheduler",
                methodName: "getParameterValues",
                callback: {
                    handler: this._getParameterValuesCb
                },
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_MESSAGE}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.schedule.Scheduler
         * @method isModifiable 
         * Returns true if the given task is modifiable.
         * This calls the method 'isModifiable' of the server DAO 'Scheduler'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.id The id of the task
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
                role: "org.ametys.plugins.core.schedule.Scheduler",
                methodName: "isModifiable",
                callback: {
                    handler: this._isModifiableCb
                },
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_MESSAGE}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.schedule.Scheduler
         * @method add 
         * Adds a new task.
         * This calls the method 'add' of the server DAO 'Scheduler'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.label The label
         * @param {String} parameters.description The description
         * @param {String} parameters.runAtStartup true to run the task once, when the server will restart
         * @param {String} parameters.cron The cron expression
         * @param {String} parameters.schedulableId The id of the schedulable model
         * @param {Object} parameters.params The values of the parameters
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
                role: "org.ametys.plugins.core.schedule.Scheduler",
                methodName: "add",
                callback: {
                    handler: this._addCb
                },
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_MESSAGE}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.schedule.Scheduler
         * @method edit 
         * Edits the given task.
         * This calls the method 'edit' of the server DAO 'Scheduler'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.id The id of the task
         * @param {String} parameters.label The label
         * @param {String} parameters.description The description
         * @param {String} parameters.runAtStartup true to run the task once, when the server will restart
         * @param {String} parameters.cron The cron expression
         * @param {Object} parameters.params The values of the parameters
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
                role: "org.ametys.plugins.core.schedule.Scheduler",
                methodName: "edit",
                callback: {
                    handler: this._editCb
                },
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_MESSAGE}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.schedule.Scheduler
         * @method remove 
         * Removes the given task.
         * This calls the method 'remove' of the server DAO 'Scheduler'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.id The id of the task
         * @param {Ametys.message.MessageTarget} parameters.messageTarget The message target of the task for creating the deletion message
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
                role: "org.ametys.plugins.core.schedule.Scheduler",
                methodName: "remove",
                localParamsIndex: 1,
                callback: {
                    handler: this._removeCb
                },
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_MESSAGE}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.schedule.Scheduler
         * @method enable 
         * Enables/disables the given task.
         * This calls the method 'enable' of the server DAO 'Scheduler'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.id The id of the task
         * @param {Boolean} parameters.enable true to enable the task, false to disable it.
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
                role: "org.ametys.plugins.core.schedule.Scheduler",
                methodName: "enable",
                callback: {
                    handler: this._enableCb
                },
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_MESSAGE}}",
                    category:Ext.getClassName(this)
                }
            }
        );
        
        /**
         * @callable
         * @member Ametys.plugins.core.schedule.Scheduler
         * @method isEnabled 
         * Returns the enabled state of the task.
         * This calls the method 'isEnabled' of the server DAO 'Scheduler'.
         * @param {Object[]} parameters The parameters to transmit to the server method
         * @param {String} parameters.id The id of the task
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
                role: "org.ametys.plugins.core.schedule.Scheduler",
                methodName: "isEnabled",
                callback: {
                    handler: this._isEnabledCb
                },
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_MESSAGE}}",
                    category:Ext.getClassName(this)
                }
            }
        );
    },
    
    /**
     * @private
     * Extract and convert task configurations received from server
     * @param {Object[]} taskConfigs
     * @param {Object} arguments The callback arguments
     * @param {Object} parameters The call parameters
     * @return {Ametys.plugins.coreui.schedule.Task[]} The tasks
     */
    _convertTasks: function(taskConfigs, arguments, parameters)
    {
        var tasks = [];
        Ext.Array.forEach(taskConfigs || [], function(config) {
            tasks.push(Ext.create('Ametys.plugins.coreui.schedule.Task', config));
        }, this);
        
        return tasks;
    },
    
    /**
     * @private
     * Callback function called after {@link #getParameterValues} has been processed.
     * @param {Object} response The server response
     */
    _getParameterValuesCb: function(response)
    {
        var error = response['error'] || '';
        if (error == "scheduler-error")
        {
            Ext.Msg.show({
                title: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_SCHEDULER_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_SCHEDULER}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
    },
    
    /**
     * @private
     * Callback function called after {@link #isModifiable} has been processed.
     * @param {Object} response The server response
     */
    _isModifiableCb: function(response)
    {
        var error = response['error'] || '';
        if (error == "not-found")
        {
            Ext.Msg.show({
                title: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_NOT_FOUND_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_NOT_FOUND}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
    },
    
    /**
     * @private
     * Callback function called after {@link #add} has been processed.
     * @param {Object} response The server response
     */
    _addCb: function(response)
    {
        var error = response['error'] || '';
        var id = response['id'];
        if (error == "invalid-schedulable")
        {
            Ext.Msg.show({
                title: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_INVALID_SCHEDULABLE_ID_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_INVALID_SCHEDULABLE_ID}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (error == "private")
        {
            Ext.Msg.show({
                title: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_PRIVATE_SCHEDULABLE_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_PRIVATE_SCHEDULABLE}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (error == "scheduler-error")
        {
            Ext.Msg.show({
                title: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_SCHEDULER_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_SCHEDULER}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (id != null)
        {
            Ext.create('Ametys.message.Message', {
                type: Ametys.message.Message.CREATED,
                targets: [{
                    id: Ametys.message.MessageTarget.TASK,
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
        if (error == "no-modifiable")
        {
            Ext.Msg.show({
                title: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_UNMODIFIABLE_TASK_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_UNMODIFIABLE_TASK}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (error == "not-found")
        {
            Ext.Msg.show({
                title: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_NOT_FOUND_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_NOT_FOUND}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (error == "scheduler-error")
        {
            Ext.Msg.show({
                title: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_SCHEDULER_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_SCHEDULER}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (id != null)
        {
            Ext.create('Ametys.message.Message', {
                type: Ametys.message.Message.MODIFIED,
                targets: [{
                    id: Ametys.message.MessageTarget.TASK,
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
    _removeCb: function(response, arguments, parameters)
    {
        var error = response['error'] || '';
        var id = response['id'];
        var messageTarget = parameters[1];
        if (error == "not-found")
        {
            Ext.Msg.show({
                title: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_NOT_FOUND_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_NOT_FOUND}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (error == "no-removable")
        {
            Ext.Msg.show({
                title: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_UNREMOVABLE_TASK_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_UNREMOVABLE_TASK}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (error == "no-delete")
        {
            Ext.Msg.show({
                title: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_NODELETE_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_NODELETE}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (error == "scheduler-error")
        {
            Ext.Msg.show({
                title: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_SCHEDULER_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_SCHEDULER}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (id != null)
        {
            Ext.create('Ametys.message.Message', {
                type: Ametys.message.Message.DELETED,
                targets: [messageTarget]
            });
        }
    },
    
    /**
     * @private
     * Callback function called after {@link #enable} has been processed.
     * @param {Object} response The server response
     */
    _enableCb: function(response)
    {
        var error = response['error'] || '';
        var id = response['id'];
        if (error == "not-found")
        {
            Ext.Msg.show({
                title: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_NOT_FOUND_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_NOT_FOUND}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (error == "no-deactivatable")
        {
            Ext.Msg.show({
                title: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_UNDEACTIVATABLE_TASK_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_UNDEACTIVATABLE_TASK}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (error == "scheduler-error")
        {
            Ext.Msg.show({
                title: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_SCHEDULER_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_SCHEDULER}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (id != null)
        {
            Ext.create('Ametys.message.Message', {
                type: Ametys.message.Message.MODIFIED,
                targets: [{
                    id: Ametys.message.MessageTarget.TASK,
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
        if (error == "not-found")
        {
            Ext.Msg.show({
                title: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_NOT_FOUND_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_NOT_FOUND}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else if (error == "scheduler-error")
        {
            Ext.Msg.show({
                title: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_SCHEDULER_TITLE}}",
                msg: "{{i18n PLUGINS_CORE_SCHEDULER_ERROR_SCHEDULER}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
    }
});
