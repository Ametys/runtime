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
 * This class has to be used as a mixin for classes corresponding to a serverside ClientSideElement to automatically call the right java component with callable methods.
 * You have to implements at least #getServerRole and #getServerId. 
 * 
 * This class is also used by Ext.Base#addCallables.
 */
Ext.define("Ametys.data.ServerCaller", {

        isServerCaller: true,
        
        /**
         * @template
         * @protected
         * This methods should return the server-side role of the component to call.
         * @return {String} The component role
         */
        getServerRole: function()
        {
            throw new Error("This method is not implemented in " + this.self.getName());
        },
        
        /**
         * @template
         * @protected
         * If the server-side role (#getServerRole) is a multiple extension point, this method should return the instance identifier.
         * Note that this method is always call event if the server-side component is not multiple.  
         * @return {String} The sub-component id. null for non multiple components.
         */
        getServerId: function()
        {
            throw new Error("This method is not implemented in " + this.self.getName());
        },
        
        /**
         * @template
         * @protected
         * Called before every #serverCall, this implementation do nothing.
         * Sub-implementation should document the default values modifications and additional options.
         * @param {Object) options See #serverCall options argument.
         */
        beforeServerCall: function(options)
        {
            // Default implementation does nothing
        },

        /**
         * @template
         * @protected
         * This method is always added as a "ignoreOnError" callback to the #serverCall and the default implementation does nothing.
         * @param {Object} serverResponse The server response. Can be null if an exception occurred.
         * @param {Object} options The options used for #serverCall and modified by #beforeServerCall.
         */
        afterServerCall: function(serverResponse, options)
        {
            // Default implementation does nothing
        },
    
        /**
         * This method sends a request to the execute remote method on the server using the corresponding server-side component.
         * When the response has arrived, the callback function is invoked.
         * 
         * See #beforeServerCall, because this method can add additional options and set different default values.
         * 
         * Do not overrides this method, since Ext.Base#addCallables will call #beforeServerCall but not #serverCall.
         * 
         * @param {String} methodName The name of the java method to call. The java method must be annotate as "callable". The component use to call this method, is the java class used when declaring this controller in the plugin.xml.
         * @param {Object[]} parameters The parameters to transmit to the java method. Types are important.
         * 
         * @param {Function} callback The function to call when the java process is over. 
         * @param {Object} callback.returnedValue The returned value of the java call. Can be null if an error occurred (but the callback may not be called on error depending on the errorMessage value).
         * @param {Object} callback.arguments Other arguments specified in option.arguments
         * 
         * @param {Object} [options] Advanced options for the call.
         * @param {Boolean/String/Object} [options.errorMessage] Display an error message. See Ametys.data.ServerCall#callMethod errorMessage.
         * @param {Boolean/String/Object} [options.waitMessage] Display a waiting message. See Ametys.data.ServerCall#callMethod waitMessage.
         * @param {Number} [options.priority] The message priority. See Ametys.data.ServerCall#callMethod for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
         * @param {String} [options.cancelCode] Cancel similar unachieved read operations. See Ametys.data.ServerCall#callMethod cancelCode.
         * @param {Object} [options.arguments] Additional arguments set in the callback.arguments parameter.
         * @param {Boolean} [options.ignoreCallbackOnError] If the server throws an exception, should the callback beeing called with a null parameter. See Ametys.data.ServerCall#callMethod ignoreOnError.
         */
        serverCall: function (methodName, parameters, callback, options)
        {
            options = options || {};
            
            this.beforeServerCall(options);
            
            Ametys.data.ServerComm.callMethod({
                role: this.getServerRole(), 
                id: this.getServerId(), 
                methodName: methodName, 
                parameters: parameters || [],
                callback: [
                    {
                        handler: callback,
                        scope: this,
                        arguments: options.arguments,
                        ignoreOnError: options.ignoreCallbackOnError
                    },
                    {
                        handler: this.afterServerCall,
                        scope: this,
                        arguments: options,
                        ignoreOnError: false
                    }
                ],
                waitMessage: options.waitMessage,
                errorMessage: options.errorMessage,
                cancelCode: options.cancelCode,
                priority: options.priority
            });
        }
});