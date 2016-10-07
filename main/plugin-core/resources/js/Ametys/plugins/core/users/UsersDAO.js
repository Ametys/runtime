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
 * This class handles the access and edition for users
 */
Ext.define(
	"Ametys.plugins.core.users.UsersDAO", 
	{
		singleton: true,
        
		constructor: function(config)
	 	{
			/**
	    	 * @callable
	    	 * @param member Ametys.plugins.core.users.UsersDAO
	    	 * @method addUser
	    	 * Create a new user
	    	 * This calls the method 'addUser' of the server DAO 'org.ametys.plugins.core.user.UserDAO'.
	    	 * @param {Object[]} parameters The parameters to transmit to the server method
	    	 * @param {String} parameters.populationId The id of the population where to add the user
	    	 * @param {Number} userDirectoryIndex The index of the user directory
	    	 * @param {String} parameters.values The users's parameters
	    	 * @param {String} [parameters.userMessageTargetType=user] The type of user target
	    	 * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
			 * @param {Object} callback.user The user's properties
			 * @param {Object} callback.arguments Other arguments specified in option.arguments                 
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
			    role: "org.ametys.plugins.core.user.UserDAO",
				methodName: "addUser",
				localParamsIndex: 3,
	     		callback: {
	         		handler: this._addUserCb
	     		},
	     		errorMessage: {
					category: this.self.getName(),
					msg: "{{i18n PLUGINS_CORE_USERS_ADD_ERROR}}"
				}
			});
			
			/**
	    	 * @callable
	    	 * @param member Ametys.plugins.core.users.UsersDAO
	    	 * @method addUser
	    	 * Edit a user
	    	 * This calls the method 'editUser' of the server DAO 'org.ametys.plugins.core.user.UserDAO'.
	    	 * @param {Object[]} parameters The parameters to transmit to the server method
	    	 * @param {String} parameters.populationId The id of the population of the user to edit
	    	 * @param {Object} parameters.values The users's parameters
	    	 * @param {String} [parameters.userMessageTargetType=user] The type of user target
	    	 * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
			 * @param {Object} callback.user The user's properties
			 * @param {Object} callback.arguments Other arguments specified in option.arguments                 
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
			    role: "org.ametys.plugins.core.user.UserDAO",
				methodName: "editUser",
				localParamsIndex: 2,
	     		callback: {
	         		handler: this._editUserCb
	     		},
	     		errorMessage: {
					category: this.self.getName(),
					msg: "{{i18n PLUGINS_CORE_USERS_EDIT_ERROR}}"
				}
			});
			
			/**
	    	 * @callable
	    	 * @param member Ametys.plugins.core.users.UsersDAO
	    	 * @method deleteUsers
	    	 * Delete users
	    	 * This calls the method 'deleteUsers' of the server DAO 'org.ametys.plugins.core.user.UserDAO'.
	    	 * @param {Object[]} parameters The parameters to transmit to the server method
	    	 * @param {Object} parameters.users The users to delete
	    	 * @param {String} parameters.users.login The login of the user
	    	 * @param {String} parameters.users.population The population id of the user
	    	 * @param {String} [parameters.userMessageTargetType=user] The type of user target
	    	 * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
			 * @param {Object} callback.returnedValue The value return from the server. Null on error (please note that when an error occurred, the callback may not be called depending on the value of errorMessage).
			 * @param {Object} callback.arguments Other arguments specified in option.arguments                 
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
			    role: "org.ametys.plugins.core.user.UserDAO",
				methodName: "deleteUsers",
				localParamsIndex: 1,
	     		callback: {
	         		handler: this._deleteUsersCb
	     		},
				errorMessage: {
				    msg: "{{i18n PLUGINS_CORE_USERS_DELETE_ERROR}}",
				    category: Ext.getClassName(this)
				}
			});
			
			/**
	    	 * @callable
	    	 * @param member Ametys.plugins.core.users.UsersDAO
	    	 * @method getUser
	    	 * Get user's properties
	    	 * This calls the method 'getUser' of the server DAO 'org.ametys.plugins.core.user.UserDAO'.
	    	 * @param {Object[]} parameters The parameters to transmit to the server method
	    	 * @param {String} parameters.login The user's login
	    	 * @param {String} parameters.populationId The user's population
	    	 * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
			 * @param {Object} callback.user The user's properties
			 * @param {Object} callback.arguments Other arguments specified in option.arguments                 
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
			    role: "org.ametys.plugins.core.user.UserDAO",
				methodName: "getUser",
				errorMessage: {
				    msg: "{{i18n PLUGINS_CORE_USERS_INFOS_ERROR}}",
				    category: Ext.getClassName(this)
				}
			});
			
			/**
	    	 * @callable
	    	 * @param member Ametys.plugins.core.users.UsersDAO
	    	 * @method isModifiable
	    	 * Checks if the user is modifiable
	    	 * This calls the method 'isModifiable' of the server DAO 'org.ametys.plugins.core.user.UserDAO'.
	    	 * @param {Object[]} parameters The parameters to transmit to the server method
	    	 * @param {String} parameters.login The user's login
	    	 * @param {String} parameters.populationId The user's population
	    	 * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
			 * @param {Object} callback.user The user's properties
			 * @param {Object} callback.arguments Other arguments specified in option.arguments                 
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
			    role: "org.ametys.plugins.core.user.UserDAO",
				methodName: "isModifiable",
				errorMessage: {
				    msg: "{{i18n PLUGINS_CORE_USERS_IS_MODIFIABLE_ERROR}}",
				    category: Ext.getClassName(this)
				}
			});
			
			/**
	    	 * @callable
	    	 * @param member Ametys.plugins.core.users.UsersDAO
	    	 * @method isRemovable
	    	 * Checks if the user is removable
	    	 * This calls the method 'isRemovable' of the server DAO 'org.ametys.plugins.core.user.UserDAO'.
	    	 * @param {Object[]} parameters The parameters to transmit to the server method
	    	 * @param {String} parameters.login The user's login
	    	 * @param {String} parameters.populationId The user's population
	    	 * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
			 * @param {Object} callback.user The user's properties
			 * @param {Object} callback.arguments Other arguments specified in option.arguments                 
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
			    role: "org.ametys.plugins.core.user.UserDAO",
				methodName: "isRemovable",
				errorMessage: {
				    msg: "{{i18n PLUGINS_CORE_USERS_IS_MODIFIABLE_ERROR}}",
				    category: Ext.getClassName(this)
				}
			});
			
			/**
	    	 * @callable
	    	 * @param member Ametys.plugins.core.users.UsersDAO
	    	 * @method impersonate
	    	 * Impersonate the selected user
	    	 * This calls the method 'impersonate' of the server DAO 'org.ametys.plugins.core.user.UserDAO'.
	    	 * @param {Object[]} parameters The parameters to transmit to the server method
	    	 * @param {String} parameters.login The user's login
	    	 * @param {String} parameters.populationId The user's population
	    	 * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
			 * @param {String} callback.user The user's properties
			 * @param {String} callback.user.login the user's login
			 * @param {String} callback.user.name the user's full name
			 * @param {Object} callback.arguments Other arguments specified in option.arguments                 
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
			    role: "org.ametys.plugins.core.user.UserDAO",
				methodName: "impersonate",
	     		callback: {
	         		handler: this._impersonateUserCb
	     		},
				errorMessage: {
				    msg: "{{i18n PLUGINS_CORE_USERS_IMPERSONATE_ERROR}}",
				    category: Ext.getClassName(this)
				}
			});
	 	},
        
        /**
         * Helper function to render the name of a user.
         * @param {String} login The login of the user
         * @param {String} populationLabel The label of the population of the user
         * @param {String} [sortablename] The full name of the user.
         * @return {String} A string representation of the user.
         */
        renderUser: function(login, populationLabel, sortablename)
        {
            if (sortablename != null)
            {
                return sortablename + ' (' + login + ', ' + populationLabel + ')';
            }
            else
            {
                return login + ' (' + populationLabel + ')';
            }
        },
	 	
	 	/**
		 * @private
		 * Callback function called after a user was created
		 * @param {Object} user The user's information
		 * @param {Object} args The callback arguments
		 * @param {Object[]} params The callback parameters (server-side and client-side)
		 */
		_addUserCb: function (user, args, params)
		{
			if (user && user.login)
			{
				Ext.create('Ametys.message.Message', {
					type: Ametys.message.Message.CREATED,
					targets: {
						id: params[3] || Ametys.message.MessageTarget.USER,
						parameters: {
                            id: user.login,
                            population: user.population
                        }
					}
				});
			}
		},
		
		/**
		 * @private
		 * Callback function called after a user was created
		 * @param {Object} user The user's information
		 * @param {Object} args The callback arguments
		 * @param {Object[]} params The callback parameters (server-side and client-side)
		 */
		_editUserCb: function (user, args, params)
		{
			if (user && user.login)
			{
				Ext.create('Ametys.message.Message', {
					type: Ametys.message.Message.MODIFIED,
					parameters: {major: true},
					targets: {
						id: params[2] || Ametys.message.MessageTarget.USER,
						parameters: {
                            id: user.login,
                            population: user.population
                        }
					}
				});
			}
		},
		
		/**
		 * @private
		 * Callback function called after a user was created
		 * @param {Object} user The user's information
		 * @param {Object} args The callback arguments
		 * @param {Object[]} params The callback parameters (server-side and client-side)
		 * @param {String} params.id The id of deleted page
		 * @param {Ametys.message.MessageTarget} params.target The deleted target
		 */
		_deleteUsersCb: function (user, args, params)
		{
			var users = params[0];
			var targets = [];
			
			Ext.Array.forEach(users, function(user) {
				targets.push({
					id: params[1] || Ametys.message.MessageTarget.USER,
					parameters: {
                        id: user.login,
                        population: user.population
                    }
				});
			}, this);
			
			Ext.create('Ametys.message.Message', {
				type: Ametys.message.Message.DELETED,
				targets: targets
			});
		},
		
		/**
		 * @private
		 * Callback function called after a user was created
		 * @param {Object} user The server's response
		 * @param {String} user.error if the user was not found
		 * @param {String} user.name the name of the user
		 * @param {String} user.login the login of the user		 		 
		 * @param {Object} args The callback arguments
		 * @param {Object[]} params The callback parameters (server-side and client-side)
		 */
		_impersonateUserCb: function (user, args, params)
		{
			if (user['error'])
			{
				Ametys.Msg.show ({
					title: "{{i18n PLUGINS_CORE_USERS_IMPERSONATE_FAILURE_TITLE}}",
					msg: "{{i18n PLUGINS_CORE_USERS_IMPERSONATE_FAILURE}}",
					buttons: Ext.Msg.OK,
					icon: Ext.MessageBox.ERROR
				});
			}
			else
			{
				Ametys.Msg.show ({
					title: "{{i18n PLUGINS_CORE_USERS_IMPERSONATE_SUCCESS_TITLE}}",
					msg: "{{i18n PLUGINS_CORE_USERS_IMPERSONATE_SUCCESS}} " + user.name + " (" + user.login + ", " + user.population + ").\n{{i18n PLUGINS_CORE_USERS_IMPERSONATE_SUCCESS_2}}",
					buttons: Ext.Msg.OK,
					icon: Ext.MessageBox.INFO,
					fn: function() 
					{ 
						Ametys.openWindow(Ametys.CONTEXT_PATH);
					}
				});
			}
		}
});

Ext.define("Ametys.message.UserMessageTarget", {
	override: "Ametys.message.MessageTarget",

     statics: 
     {
         /**
          * @member Ametys.message.MessageTarget
          * @readonly
          * @property {String} USER The target type is a user. The expected parameters are:
          * @property {String} USER.id The login of user
          * @property {String} USER.population The population id of user
          * @property {String} [USER.inherited] true if the user inherits rights
          * 
          */
         USER: "user"
     }
});
