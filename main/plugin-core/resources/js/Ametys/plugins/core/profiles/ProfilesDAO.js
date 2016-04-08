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
 * This class handles the access and edition of profiles
 */
Ext.define(
	"Ametys.plugins.core.profiles.ProfilesDAO", 
	{
		singleton: true,
		
		constructor: function(config)
	 	{
			/**
	    	 * @callable
	    	 * @param member Ametys.plugins.core.profiles.ProfilesDAO
	    	 * @method addProfile
	    	 * Create a new profile
	    	 * This calls the method 'addProfile' of the server DAO 'org.ametys.plugins.core.right.profile.ProfileDAO'.
	    	 * @param {Object[]} parameters The parameters to transmit to the server method
	    	 * @param {String} parameters.name The profile's name
	    	 * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
			 * @param {Object}  callback.profile The profile's properties
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
			this.addCallables({
			    role: "org.ametys.plugins.core.right.profile.ProfileDAO",
				methodName: "addProfile",
	     		callback: {
	         		handler: this._addProfileCb
	     		},
	     		errorMessage: {
					category: this.self.getName(),
					msg: "{{i18n PLUGINS_CORE_PROFILES_ADD_ERROR}}"
				}
			});
			
			/**
	    	 * @callable
	    	 * @param member Ametys.plugins.core.profiles.ProfilesDAO
	    	 * @method renameProfile
	    	 * Renames a profile
	    	 * This calls the method 'renameProfile' of the server DAO 'org.ametys.plugins.core.right.profile.ProfileDAO'.
	    	 * @param {Object[]} parameters The parameters to transmit to the server method
	    	 * @param {String} parameters.id The profile's id
	    	 * @param {String} parameters.name The profile's new name
	    	 * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
			 * @param {Object} callback.profile The profile's properties
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
			this.addCallables({
			    role: "org.ametys.plugins.core.right.profile.ProfileDAO",
				methodName: "renameProfile",
	     		callback: {
	         		handler: this._renameProfileCb
	     		},
	     		errorMessage: {
					category: this.self.getName(),
					msg: "{{i18n PLUGINS_CORE_PROFILES_RENAME_ERROR}}"
				}
			});
			
			/**
	    	 * @callable
	    	 * @param member Ametys.plugins.core.profiles.ProfilesDAO
	    	 * @method editProfileRights
	    	 * Edits profile's rights
	    	 * This calls the method 'editProfileRights' of the server DAO 'org.ametys.plugins.core.right.profile.ProfileDAO'.
	    	 * @param {Object[]} parameters The parameters to transmit to the server method
	    	 * @param {String} parameters.id The profile's id
	    	 * @param {String[]} parameters.rights The profile's rights
	    	 * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
			 * @param {Object} callback.profile The profile's properties
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
			this.addCallables({
			    role: "org.ametys.plugins.core.right.profile.ProfileDAO",
				methodName: "editProfileRights",
	     		callback: {
	         		handler: this._editProfileCb
	     		},
	     		errorMessage: {
					category: this.self.getName(),
					msg: "{{i18n PLUGINS_CORE_PROFILES_SAVE_MODIFICATIONS_ERROR}}"
				}
			});
			
			/**
	    	 * @callable
	    	 * @param member Ametys.plugins.core.profiles.ProfilesDAO
	    	 * @method deleteProfiles
	    	 * Delete profiles
	    	 * This calls the method 'deleteProfiles' of the server DAO 'org.ametys.plugins.core.right.profile.ProfileDAO'.
	    	 * @param {Object[]} parameters The parameters to transmit to the server method
	    	 * @param {String[]} parameters.logins The profiles' ids
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
			this.addCallables({
			    role: "org.ametys.plugins.core.right.profile.ProfileDAO",
				methodName: "deleteProfiles",
				localParamsIndex: 2,
	     		callback: {
	         		handler: this._deleteProfilesCb
	     		},
				errorMessage: {
				    msg: "{{i18n PLUGINS_CORE_PROFILES_DELETE_ERROR}}",
				    category: Ext.getClassName(this)
				}
			});
			
			/**
	    	 * @callable
	    	 * @param member Ametys.plugins.core.profiles.ProfilesDAO
	    	 * @method getProfile
	    	 * Get profile's properties
	    	 * This calls the method 'getProfile' of the server DAO 'org.ametys.plugins.core.right.profile.ProfileDAO'.
	    	 * @param {Object[]} parameters The parameters to transmit to the server method
	    	 * @param {String[]} parameters.id The profile's id
	    	 * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
			 * @param {Object} callback.profile The profile's properties
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
			this.addCallables({
			    role: "org.ametys.plugins.core.right.profile.ProfileDAO",
				methodName: "getProfile",
				errorMessage: {
				    msg: "{{i18n PLUGINS_CORE_PROFILES_INFOS_ERROR}}",
				    category: Ext.getClassName(this)
				}
			});
            
			/**
	    	 * @callable
	    	 * @param member Ametys.plugins.core.profiles.ProfilesDAO
	    	 * @method addUsers
	    	 * Add users to given profiles
	    	 * This calls the method 'addUsers' of the server DAO 'org.ametys.plugins.core.right.profile.ProfileDAO'.
	    	 * @param {Object[]} parameters The parameters to transmit to the server method
	    	 * @param {Object[]} parameters.users The users to add
	    	 * @param {String} parameters.users.login The login of the user
	    	 * @param {String} parameters.users.population The population id of the user
	    	 * @param {String[]} parameters.profilesIds The ids of profiles
	    	 * @param {String} parameters.profileContext The context
	    	 * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
			 * @param {Object} callback.profile The profile's properties
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
			this.addCallables({
			    role: "org.ametys.plugins.core.right.profile.ProfileDAO",
				methodName: "addUsers",
                callback: {
                    handler: this._addUsersCb
                },
				errorMessage: {
				    msg: "{{i18n PLUGINS_CORE_RIGHTS_ASSIGNMENT_ADDUSER_ERROR}}",
				    category: Ext.getClassName(this) + '.addUsers'
				}
			});
            
            /**
             * @callable
             * @param member Ametys.plugins.core.profiles.ProfilesDAO
             * @method addGroups
             * Add groups to given profiles
             * This calls the method 'addGroups' of the server DAO 'org.ametys.plugins.core.right.profile.ProfileDAO'.
             * @param {Object[]} parameters The parameters to transmit to the server method
             * @param {Object[]} parameters.groups The groups to add
             * @param {String} parameters.groups.id The id of the group
             * @param {String} parameters.groups.groupDirectory The group directory id of the group
             * @param {String[]} parameters.profilesIds The ids of profiles
             * @param {String} parameters.profileContext The context
             * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
             * @param {Object} callback.profile The profile's properties
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
            this.addCallables({
                role: "org.ametys.plugins.core.right.profile.ProfileDAO",
                methodName: "addGroups",
                callback: {
                    handler: this._addGroupsCb
                },
                errorMessage: {
                    msg: "{{i18n PLUGINS_CORE_RIGHTS_ASSIGNMENT_ADDGROUP_ERROR}}",
                    category: Ext.getClassName(this) + '.addGroups'
                }
            });
            
			/**
	    	 * @callable
	    	 * @param member Ametys.plugins.core.profiles.ProfilesDAO
	    	 * @method removeAssignment
	    	 * Remove profile assignment for given users and groups
	    	 * This calls the method 'removeAssignment' of the server DAO 'org.ametys.plugins.core.right.profile.ProfileDAO'.
	    	 * @param {Object[]} parameters The parameters to transmit to the server method
	    	 * @param {Object[]} parameters.users The users to remove
	    	 * @param {String} parameters.users.login The login of the user
	    	 * @param {String} parameters.users.population The population id of the user
	    	 * @param {Object[]} parameters.groups The groups to remove
	    	 * @param {String} parameters.groups.id The id of the group
	    	 * @param {String} parameters.groups.groupDirectory The group directory id of the group
	    	 * @param {String} parameters.profileId The id of the profile
	    	 * @param {String} parameters.profileContext The context
	    	 * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
			 * @param {Object} callback.profile The profile's properties
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
			this.addCallables({
			    role: "org.ametys.plugins.core.right.profile.ProfileDAO",
				methodName: "removeAssignment",
                callback: {
                    handler: this._removeAssignmentCb
                },
				errorMessage: {
				    msg: "{{i18n PLUGINS_CORE_RIGHTS_ASSIGNMENT_REMOVE_ERROR}}",
				    category: Ext.getClassName(this) + '.removeAssignment'
				}
			});
	 	},
	 	
	 	/**
		 * @private
		 * Callback function called after a profile was created
		 * @param {Object} profile The profile's information
		 * @param {Object} args The callback arguments
		 * @param {Object[]} params The callback parameters (server-side and client-side)
		 */
		_addProfileCb: function (profile, args, params)
		{
			if (profile && profile.id)
			{
				Ext.create('Ametys.message.Message', {
					type: Ametys.message.Message.CREATED,
					targets: {
						type: Ametys.message.MessageTarget.PROFILE,
						parameters: {id: profile.id}
					}
				});
			}
		},
		
		/**
		 * @private
		 * Callback function called after a profile was renamed
		 * @param {Object} profile The profile's information
		 * @param {Object} args The callback arguments
		 * @param {Object[]} params The callback parameters (server-side and client-side)
		 */
		_renameProfileCb: function (profile, args, params)
		{
			if (profile && profile.id)
			{
				Ext.create('Ametys.message.Message', {
					type: Ametys.message.Message.MODIFIED,
					parameters: {major: true},
					targets: {
						type: Ametys.message.MessageTarget.PROFILE,
						parameters: {id: profile.id}
					}
				});
			}
		},
		
		/**
		 * @private
		 * Callback function called after the rights associated to a profile were edited
		 * @param {Object} profile The profile's information
		 * @param {Object} args The callback arguments
		 * @param {Object[]} params The callback parameters (server-side and client-side)
		 */
		_editProfileCb: function (profile, args, params)
		{
			if (profile && profile.id)
			{
				Ext.create('Ametys.message.Message', {
					type: Ametys.message.Message.MODIFIED,
					targets: {
						type: Ametys.message.MessageTarget.PROFILE,
						parameters: {id: profile.id}
					}
				});
			}
		},
		
		/**
		 * @private
		 * Callback function called after a profile was created
		 * @param {Object} profile The profile's information
		 * @param {Object} args The callback arguments
		 * @param {Object[]} params The callback parameters (server-side and client-side)
		 * @param {String} params.id The id of deleted page
		 * @param {Ametys.message.MessageTarget} params.target The deleted target
		 */
		_deleteProfilesCb: function (profile, args, params)
		{
			var ids = params[0];
			var targets = [];
			
			Ext.Array.forEach(ids, function(id) {
				targets.push({
					type: Ametys.message.MessageTarget.PROFILE,
					parameters: {id: id}
				});
			}, this);
			
			Ext.create('Ametys.message.Message', {
				type: Ametys.message.Message.DELETED,
				targets: targets
			});
		},
        
        /**
	     * {@link #addUsers} callback.
	     * Fires a #Ametys.message.Message.MODIFIED message for the profiles.
	     * @param {Object} response The server response
	     * @param {Array} args The callback arguments
	     * @param {Array} params The parameters
	     * @private
	     */
	    _addUsersCb: function (response, args, params)
	    {
            var ids = params[1]; //profileIds
            
            var targets = [];
            Ext.Array.forEach(ids, function(id) {
		        targets.push({
		            type: Ametys.message.MessageTarget.PROFILE,
		            parameters: {id: id}
		        });
		        
            }, this);
            
	        Ext.create('Ametys.message.Message', {
	            type: Ametys.message.Message.MODIFIED,
	            targets: targets
	        });
	    },
        
        /**
	     * {@link #addGroups} callback.
	     * Fires a #Ametys.message.Message.MODIFIED message for the profiles.
	     * @param {Object} response The server response
	     * @param {Array} args The callback arguments
	     * @param {Array} params The parameters
	     * @private
	     */
	    _addGroupsCb: function (response, args, params)
	    {
            var ids = params[1]; //profileIds
            
            var targets = [];
            Ext.Array.forEach(ids, function(id) {
		        targets.push({
		            type: Ametys.message.MessageTarget.PROFILE,
		            parameters: {id: id}
		        });
		        
            }, this);
            
	        Ext.create('Ametys.message.Message', {
	            type: Ametys.message.Message.MODIFIED,
	            targets: targets
	        });
	    },
        
        /**
	     * {@link #removeAssignment} callback.
	     * Fires a #Ametys.message.Message.MODIFIED message for the profile.
	     * @param {Object} response The server response
	     * @param {Array} args The callback arguments
	     * @param {Array} params The parameters
	     * @private
	     */
	    _removeAssignmentCb: function (response, args, params)
	    {
            var id = params[2]; //profileId
            
            var target = {
                type: Ametys.message.MessageTarget.PROFILE,
                parameters: {id: id}
            };
            
	        Ext.create('Ametys.message.Message', {
	            type: Ametys.message.Message.MODIFIED,
	            targets: [target]
	        });
	    }
		
});

Ext.define("Ametys.message.ProfileMessageTarget", {
	override: "Ametys.message.MessageTarget",

     statics: 
     {
         /**
          * @member Ametys.message.MessageTarget
          * @readonly
          * @property {String} PROFILE The target type is a profile. The expected parameters are:
          * @property {String} PROFILE.id The id of profile
          * @property {String} [PROFILE.name] The name of the profile
		  * @property {String} [PROFILE.context] the context of the profile
          */
         PROFILE: "profile"
     }
});
