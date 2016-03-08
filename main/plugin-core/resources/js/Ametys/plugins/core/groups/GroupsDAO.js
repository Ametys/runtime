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
 * This class handles the access and edition for groups
 */
Ext.define(
	"Ametys.plugins.core.groups.GroupsDAO", 
	{
		singleton: true,
		
		constructor: function(config)
	 	{
			/**
	    	 * @callable
	    	 * @param member Ametys.plugins.core.groups.groupsDAO
	    	 * @method addGroup
	    	 * Create a new group
	    	 * This calls the method 'addGroup' of the server DAO 'org.ametys.plugins.core.group.GroupDAO'.
	    	 * @param {Object[]} parameters The parameters to transmit to the server method
	    	 * @param {String} parameters.name The group's name
	    	 * @param {String} [parameters.groupManagerRole] The groups manager's role. Can be null or empty to use the default one.
	    	 * @param {String} [parameters.groupMessageTargetType=group] The type of group target
	    	 * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
			 * @param {Object} callback.group The group's properties
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
			    role: "org.ametys.plugins.core.group.GroupDAO",
				methodName: "addGroup",
				localParamsIndex: 2,
	     		callback: {
	         		handler: this._addGroupCb
	     		},
	     		errorMessage: {
					category: this.self.getName(),
					msg: "{{i18n PLUGINS_CORE_GROUPS_ADD_ERROR}}"
				}
			});
			
			/**
	    	 * @callable
	    	 * @param member Ametys.plugins.core.groups.groupsDAO
	    	 * @method renameGroup
	    	 * Rename a group
	    	 * This calls the method 'renameGroup' of the server DAO 'org.ametys.plugins.core.group.GroupDAO'.
	    	 * @param {Object[]} parameters The parameters to transmit to the server method
	    	 * @param {String} parameters.name The groups's new name
	    	 * @param {String} [parameters.groupManagerRole] The groups manager's role. Can be null or empty to use the default one.
	    	 * @param {String} [parameters.groupMessageTargetType=group] The type of group target
	    	 * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
			 * @param {Object} callback.group The group's properties
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
			    role: "org.ametys.plugins.core.group.GroupDAO",
				methodName: "renameGroup",
				localParamsIndex: 3,
	     		callback: {
	         		handler: this._renameGroupCb
	     		},
	     		errorMessage: {
					category: this.self.getName(),
					msg: "{{i18n PLUGINS_CORE_GROUPS_RENAME_ERROR}}"
				}
			});
			
			/**
	    	 * @callable
	    	 * @param member Ametys.plugins.core.groups.groupsDAO
	    	 * @method deleteGroups
	    	 * Delete groups
	    	 * This calls the method 'deleteGroups' of the server DAO 'org.ametys.plugins.core.group.GroupDAO'.
	    	 * @param {Object[]} parameters The parameters to transmit to the server method
	    	 * @param {String[]} parameters.ids The groups' ids
	    	 * @param {String} [parameters.groupManagerRole] The groups manager's role. Can be null or empty to use the default one.
	    	 * @param {String} [parameters.groupMessageTargetType=group] The type of group target
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
			    role: "org.ametys.plugins.core.group.GroupDAO",
				methodName: "deleteGroups",
				localParamsIndex: 2,
	     		callback: {
	         		handler: this._deleteGroupsCb,
	     		},
				errorMessage: {
				    msg: "{{i18n PLUGINS_CORE_GROUPS_DELETE_ERROR}}",
				    category: Ext.getClassName(this)
				}
			});
			
			/**
	    	 * @callable
	    	 * @param member Ametys.plugins.core.groups.groupsDAO
	    	 * @method getGroup
	    	 * Get group's properties
	    	 * This calls the method 'getGroup' of the server DAO 'org.ametys.plugins.core.group.GroupDAO'.
	    	 * @param {Object[]} parameters The parameters to transmit to the server method
	    	 * @param {String[]} parameters.id The group's id
	    	 * @param {String} [parameters.groupManagerRole] The groups manager's role. Can be null or empty to use the default one.
	    	 * @param {Function} callback The function to call when the java process is over. Use options.scope for the scope. 
			 * @param {Object} callback.group The group's properties
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
			    role: "org.ametys.plugins.core.group.GroupDAO",
				methodName: "getGroup",
				errorMessage: {
				    msg: "{{i18n PLUGINS_CORE_GROUPS_INFOS_ERROR}}",
				    category: Ext.getClassName(this)
				}
			});
			
			/**
	    	 * @callable
	    	 * @param member Ametys.plugins.core.groups.groupsDAO
	    	 * @method addUsersGroup
	    	 * Add users to group
	    	 * This calls the method 'addUsersGroup' of the server DAO 'org.ametys.plugins.core.group.GroupDAO'.
	    	 * @param {Object[]} parameters The parameters to transmit to the server method
	    	 * @param {String[]} parameters.id The group' ids
	    	 * @param {String[]} parameters.users The users's login to add
	    	 * @param {String} [parameters.groupManagerRole] The groups manager's role. Can be null or empty to use the default one.
	    	 * @param {String} [parameters.groupMessageTargetType=group] The type of group target
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
			    role: "org.ametys.plugins.core.group.GroupDAO",
				methodName: "addUsersGroup",
				localParamsIndex: 3,
	     		callback: {
	         		handler: this._editGroupCb,
	     		},
				errorMessage: {
				    msg: "{{i18n PLUGINS_CORE_GROUPS_UPDATEUSERS_ERROR}}",
				    category: Ext.getClassName(this)
				}
			});
			
			/**
	    	 * @callable
	    	 * @param member Ametys.plugins.core.groups.groupsDAO
	    	 * @method removeUsersGroup
	    	 * Remove users from group
	    	 * This calls the method 'removeUsersGroup' of the server DAO 'org.ametys.plugins.core.group.GroupDAO'.
	    	 * @param {Object[]} parameters The parameters to transmit to the server method
	    	 * @param {String[]} parameters.id The group' ids
	    	 * @param {String[]} parameters.users The users's login to remove
	    	 * @param {String} [parameters.groupManagerRole] The groups manager's role. Can be null or empty to use the default one.
	    	 * @param {String} [parameters.groupMessageTargetType=group] The type of group target
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
			    role: "org.ametys.plugins.core.group.GroupDAO",
				methodName: "removeUsersGroup",
				localParamsIndex: 3,
	     		callback: {
	         		handler: this._editGroupCb,
	     		},
				errorMessage: {
				    msg: "{{i18n PLUGINS_CORE_GROUPS_UPDATEUSERS_ERROR}}",
				    category: Ext.getClassName(this)
				}
			});
	 	},
	 	
	 	/**
		 * @private
		 * Callback function called after a group was created
		 * @param {Object} group The group's information
		 * @param {Object} args The callback arguments
		 * @param {Object[]} params The callback parameters (server-side and client-side)
		 */
		_addGroupCb: function (group, args, params)
		{
			if (group && group.id)
			{
				Ext.create('Ametys.message.Message', {
					type: Ametys.message.Message.CREATED,
					targets: {
						type: params[2] || Ametys.message.MessageTarget.GROUP,
						parameters: {id: group.id}
					}
				});
				
				Ametys.notify({
			        type: 'info',
			        title: "{{i18n PLUGINS_CORE_GROUPS_ADD_LABEL}}",
			        icon: '/plugins/core/resources/img/groups/group_32.png',
			        description: Ext.String.format("{{i18n PLUGINS_CORE_GROUPS_ADD_NOTIFY}}", group.label)
			    });
			}
		},
		
		/**
		 * @private
		 * Callback function called after a group was created
		 * @param {Object} group The group's information
		 * @param {Object} args The callback arguments
		 * @param {Object[]} params The callback parameters (server-side and client-side)
		 */
		_renameGroupCb: function (group, args, params)
		{
			if (group && group.id)
			{
				Ext.create('Ametys.message.Message', {
					type: Ametys.message.Message.MODIFIED,
					parameters: {major: true},
					targets: {
						type: params[3] || Ametys.message.MessageTarget.GROUP,
						parameters: {id: group.id}
					}
				});
			}
		},
		
		/**
		 * @private
		 * Callback function called after a group was created
		 * @param {Object} group The group's information
		 * @param {Object} args The callback arguments
		 * @param {Object[]} params The callback parameters (server-side and client-side)
		 */
		_editGroupCb: function (group, args, params)
		{
			if (group && group.id)
			{
				Ext.create('Ametys.message.Message', {
					type: Ametys.message.Message.MODIFIED,
					targets: {
						type: params[3] || Ametys.message.MessageTarget.GROUP,
						parameters: {id: group.id}
					}
				});
			}
		},
		
		/**
		 * @private
		 * Callback function called after a group was created
		 * @param {Object} group The group's information
		 * @param {Object} args The callback arguments
		 * @param {Object[]} params The callback parameters (server-side and client-side)
		 * @param {String} params.id The id of deleted page
		 * @param {Ametys.message.MessageTarget} params.target The deleted target
		 */
		_deleteGroupsCb: function (group, args, params)
		{
			var ids = params[0];
			var targets = [];
			
			Ext.Array.forEach(ids, function(id) {
				targets.push({
					type: params[2] || Ametys.message.MessageTarget.GROUP,
					parameters: {id: id}
				});
			}, this);
			
			Ext.create('Ametys.message.Message', {
				type: Ametys.message.Message.DELETED,
				targets: targets
			});
		}
		
});

Ext.define("Ametys.message.GroupMessageTarget", {
	override: "Ametys.message.MessageTarget",

     statics: 
     {
         /**
          * @member Ametys.message.MessageTarget
          * @readonly
          * @property {String} GROUP The target type is a group. The expected parameters are:
          * @property {String} GROUP.id The id of group
          * 
          */
         GROUP: "group"
     }
});
