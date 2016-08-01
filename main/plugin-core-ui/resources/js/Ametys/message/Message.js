/*
 *  Copyright 2014 Anyware Services
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
 * This class is a message of the bus. It is determined by its type, and the associated targets.
 * The following example explains that the selection has changed for the search tool.
 * 
 * 		Ext.create("Ametys.message.Message", {
 * 			type: Ametys.message.Message.SELECTION_CHANGED,
 * 
 *			targets: {
 *				id: Ametys.message.MessageTarget.TOOL,
 *				parameters: { ids: ['search-tool'] }
 *			}
 * 		});
 * 
 * Beware that the message creation is asynchronous and the message is automatically fired as soon as ready.
 * You can be aware of the fire event using a #cfg-callback
 * You can prepare targets to share them in several messages using Ametys.message.MessageTargetFactory#createTargets
 * 
 * To specify the type you should use constants defined in this class.
 * Each constant documentation defines potential associated parameters and targets specification.
 * 
 * All external contribution should define their constants in this class.
 * 
 * Beware: #SELECTION_CHANGED is a special event. Read its documentation carefully.
 */
Ext.define("Ametys.message.Message", 
	{
		statics:
		{
			/**
			 * @readonly
			 * @property {String} SELECTION_CHANGED Event when the selection has changed. 
			 * The target and subtarget is the new element selected and can be of any type.
			 * This event is special on two ways:
			 *    * first, it is listened by the Ametys.message.MessageBus itself to feed its method Ametys.message.MessageBus#getCurrentSelectionMessage
			 *    * second, selection changed messages are concurrent and volatile: so, when you create such an event with an asynchronous target, it may be never thrown if some other code do create and throw another selection message in between.
			 * Please note, that when you send such a message, a #SELECTION_CHANGING is thrown before.
			 * Parameters are:
			 * @property {String} SELECTION_CHANGED.creation If the selection was just consecutive to a creation of the object, you can specify here the type of target because the ribbon tab policy is different for object created and selected in a row: it will force tab selection. So be careful, when creating and selecting an object in a row, you must first fire the #CREATED event.
			 * @property {String} SELECTION_CHANGED.navigation-tool-referrer The id of the tool implementing the Ametys.cms.navigation.AbstractNavigationElement mixin in order to navigate within this tool.
			 */
			SELECTION_CHANGED: "selectionChanged",
			
			/**
			 * @readonly
			 * @property {String} SELECTION_CHANGING Event when the selection is about to change. 
			 * *Do not fire this event manually since it will be sent automatically IF NECESSARY*, when a #SELECTION_CHANGED is created, such an event is throw before to allow components to prepare for the new kind of target (because sometimes new selection require server informations).
			 * You may also note, that since #SELECTION_CHANGED events can be canceled, SELECTION_CHANGING may not be followed by the associated SELECTION_CHANGED
			 * This event has no target but have the following parameter: 
			 * @property {String[]} SELECTION_CHANGING.targets An array of targets types. Can be empty but cannot be null. A type may be present several times. Subtargets are also present in that array (with no distinction).  
			 */
			SELECTION_CHANGING: "selectionChanging",
			
			/**
			 * @readonly
			 * @property {String} CREATED Event when an object has been created. 
			 * The target and subtarget is the new element created and can be of any type.
			 * Generally the new object is selected, so you must also fire a #SELECTION_CHANGED event with the creation parameters
			 * This event has no parameters.
			 */
			CREATED: "created",
			
			/**
			 * @readonly
			 * @property {String} DELETING Event when an object is to be deleted. 
			 * The target and subtarget is the element to be deleted and can be of any type.
			 * This event has to be followed by a #DELETED event after the effective deletion.
			 * When this event is fired other components can get information they need about the object (that has not been deleted yet), and use this information when the #DELETED event will be fired.
			 * If an error occured during the deletion process, #DELETED may never be fired.
			 * This event has no parameters.
			 */
			DELETING: "deleting",
			
			/**
			 * @readonly
			 * @property {String} DELETED Event when an object has been deleted. 
			 * The target and subtarget is the element deleted and can be of any type.
			 * At this time, object is deleted and no information on it can be get anymore. Consider the #DELETING event to get information before the deletion.
			 * This event has no parameters.
			 */
			DELETED: "deleted",			

			/**
			 * @readonly
			 * @property {String} MOVED Event when an object has been moved. 
			 * The target and subtarget is the element moved and can be of any type.
			 * This event has no parameters.
			 */
			MOVED: "moved",			

			/**
			 * @readonly
			 * @property {String} MODIFIED Event when an object has been modified. 
			 * The target and subtarget is the element modified and can be of any type.
			 * This event has one mandatory parameter:
			 * @property {Boolean} MODIFIED.major (required) True if the modification was important enough to require a refresh of tools handling the target. 
			 */
			MODIFIED: "modified",	
			
			/**
			 * @readonly
			 * @property {String} MODIFYING Event when an object is beeing modified and has on-going changes. 
			 * The target and subtarget is the element being modified and can be of any type.
			 */
			MODIFYING: "modifying",	
            
            /**
             * @readonly
             * @property {String} REVERTED Event when the on-going changes have been cancelled on an object. 
             * The target and subtarget is the element being modified and can be of any type.
             */
            REVERTED: "reverted", 
            
            /**
             * @readonly
             * @property {String} SORTED Event when an object has been sorted. 
             * The target and subtarget is the element being sorted and can be of any type.
             * @property {String} SORTED.order (required) The sort order. 
             */
            SORTED: "sorted", 
			
            /**
             * @readonly
             * @property {String} ARCHIVED Event fired when an object has been archived. 
             * The target is the archived element and can be of any type.
             * At this time, object is archived and no information on it can be gotten anymore.
             * This event has no parameters.
             */
            ARCHIVED: "archived",
            
            /**
             * @readonly
             * @property {String} UNARCHIVED Event fired when an object has been unarchived. 
             * The target is the unarchived element and can be of any type.
             * This event has no parameters.
             */
            UNARCHIVED: "unarchived",
            
            /**
             * @readonly
             * @property {String} DIRTYCHANGE Event fired when an object change it's dirty state. 
             * The target is the object whose dirty state changed.
             * This event has one mandatory parameter:
             * @property {Boolean} DIRTYCHANGE.dirty (required) True if the object is now dirty, False otherwise.
            */
            DIRTYCHANGE: "dirtychange",
            
            /**
             * @readonly
             * @property {String} LOADED Event fired when an object is loaded. 
             * This event has no parameters.
            */
            LOADED: "loaded",
            
            /**
             * @private
             * @property {Number} _num The number of the message. Auto incremented during creation process.
             */
            _num: 1
		},
	
		config: 
		{
			/**
			 * @cfg {String} type (required) The message type. A non null and non empty string. You must use a constant defined in this class.
			 */
			type: null,
			/**
			 * @cfg {Object} parameters The parameter associated to the message, the value to set here depends on the specification of the constant you are using for the type. Cannot be null, but may be empty.
			 * Be careful, theses parameters are the one to specify the message, not the targets. To specify parameters on the target, consider the target class.
			 */
			parameters: {},
			/**
			 * @cfg {Object/Object[]/Ametys.message.MessageTarget/Ametys.message.MessageTarget[]} targets (required) The targets configuration, or the targets themselves, associated to the message, the value to set here depends on the specification of the constant you are using for the type (see Ametys.message.MessageFactory). Cannot be null, but may be empty.
			 */
			targets: [],
			/**
			 * @cfg {Function} callback The function called when the message has been fired. Has the following parameters:
			 * @cfg {Function} callback.msg The message created
			 */
			callback: null
		},

		/**
		 * @property {Boolean} _isReady See #isReady
		 * @private
		 */
		_isReady: false,
        
        /**
         * @private
         * @property {Number} _num The number of the message.
         */

		/**
		 * @property {Date} _creationDate The date the message was created
		 * @private
		 */

		/**
		 * @property {String} _stack The call stack used to build the message
		 * @private
		 */
		
		/**
		 * Creates a message. Be careful, the message is not ready to use, as aynchronous called are done. See #cfg-callback to get the message when its ready.
		 * In 99% of the time, you do not have to keep this is as a variable. Creating a message will fire it automatically.
		 * @param {Object} config See configuration parameters.
		 */
		constructor: function(config)
		{
			// IF THE EVENT IS A SELECTION CHANGED, FIRE A SELECTION CHANGING BEFORE
			if (config.type == Ametys.message.Message.SELECTION_CHANGED)
			{
				var targetsTypes = [];
				function addTargetsType(targets)
				{
					for (var i = 0; i < targets.length; i++)
					{
						if (Ext.isObject(targets[i]))
						{
							targetsTypes.push(targets[i].id);
							addTargetsType(Ext.Array.from(targets[i].subtargets));
						}
					}
				}
				addTargetsType(Ext.Array.from(config.targets));
				
				Ext.create(this.self.getName(), {
					type: Ametys.message.Message.SELECTION_CHANGING,
					parameters: { targets: targetsTypes },
					targets: []
				});
			}
			
			this._creationDate = new Date();
			this._num = this.self._num++;
			try
			{
				throw new Error("get trace");
			}
			catch (e)
			{
				this._stack = e.stack;
			}
			
			if (this.getLogger().isDebugEnabled())
			{
				this.getLogger().debug("Creating message " + this._num + " of type '" + config.type + "'");
			}
			
			config.targets = config.targets || []; 
			if (!Ext.isArray(config.targets))
			{
				config.targets = [config.targets];
			}
			
			// CREATE EVENT
			this.initConfig(config);
			
			// FIRE EVENT
			var me = this;
			function cb()
			{
				if (this.getLogger().isDebugEnabled())
				{
					this.getLogger().debug("Firing message " + this._num + " of type '" + config.type + "'");
				}

				me._isReady = true;
				Ametys.message.MessageBus.fire(me);
				if (config.callback)
				{
					config.callback(me);
				}
			}
			
			if (config.targets.length == 0 || config.targets[0].self)
			{
				// Targets are already fine
		        Ext.defer(cb, 1, this);
			}
			else
			{
				// Targets need to be prepared
				function prep(targets)
				{
					me.setTargets(targets);
			        Ext.defer(cb, 1, me);
				}
				Ametys.message.MessageTargetFactory.createTargets(config.targets, prep);
			}
		},
		
		/**
		 * As the creation process of a message is an asynchronous process, this let you know if it is done.
		 * @returns {Boolean} True if the message has been created 
		 */
		isReady: function()
		{
			return this._isReady;
		},
		
		/**
		 * Get the date the message was created
		 * @return {Date} The date the constructor was called. Cannot be null.
		 */
		getCreationDate: function()
		{
			return this._creationDate;
		},
		
		/**
		 * Get the unique number of the message
		 * @return {Number} The number of the message. Cannot be null.
		 */
		getNumber: function()
		{
			return this._num;
		},
		
		/**
		 * Get the callstack used to create the message. For debug purposes
		 * @return {String} The callstack.
		 * @private
		 */
		getCallStack: function()
		{
			return this._stack;
		},
		
		/**
		 * Get the first target matching the filter. If no filter is provided, get the first target (if available)
		 * @param {String/RegExp/Function} [filter] The filter upon the target type. If the filter is a function, it must return a boolean true to match, and it has the target as parameter.
		 * @param {Ametys.message.MessageTarget} filter.target The target to test. 
		 * @param {Number} [depth=0] The depth for filtering. 0 means it will dig all subtargets what ever the level is. 1 means it will only seek in the first level targets. And so on.
		 * @returns {Ametys.message.MessageTarget} The matching target, or the array of type hierarchy. Can be null.
		 * 
		 * The following examples will return a content target or null
		 * 		msg.getTarget("content");
		 * 		msg.getTarget(/^content$/);
		 * 		msg.getTarget(function (target) { return target.getId() == 'content' });
		 */
		getTarget: function(filter, depth)
		{
			if (!this.isReady())
			{
				var message = "Cannot get target on message " + this.getType() + " since it is not ready";
				this.getLogger().warn(message);
				throw new Error(message);
			}
			
			return Ametys.message.MessageTargetHelper.findTarget(this._targets, filter, depth);
		},
		
		/**
		 * Same as #getTarget, but will return all the matching targets. When a target also have subtargets that would match the filter, only the parent target is returned.
		 * Get the targets matching the filter. If no filter is provided, get all the targets available
		 * @param {String/RegExp/Function} [filter] The filter upon the target type. If the filter is a function, it must return a boolean true to match, and it has the target as parameter.
		 * @param {Ametys.message.MessageTarget} filter.target The target to test. 
		 * @param {Number} [depth=0] The depth for filtering. 0 means it will dig all subtargets what ever the level is. 1 means it will only seek in the first level targets. And so on.
		 * @returns {Ametys.message.MessageTarget[]} The non-null array of matching targets.
		 * 
		 * The following examples will return an array of PageTarget or an empty array
		 * 		msg.getTargets("page");
		 * 		msg.getTargets(/^page$/);
		 * 		msg.getTargets(function (target) { return target.getId() == 'page' });
		 */
		getTargets: function(filter, depth)
		{
			if (!this.isReady())
			{
				var message = "Cannot get targets on message " + this.getType() + " since it is not ready";
				this.getLogger().warn(message);
				throw new Error(message);
			}

			return Ametys.message.MessageTargetHelper.findTargets(this._targets, filter, depth);
		}
	}
);
