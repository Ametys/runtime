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
 * Defines the target of a message.
 * A target is defined by a type and its associated parameters.
 * The type is defined by a constant (in this class) which defined the parameters needed.
 * 
 * To add your own message target, add a constant in this class documentation.
 * 
 * Advanced target, may require helper to create it (potentially asynchronous). See the associated constant documentation.
 * 
 * A target may carry subtargets, that are not necessary the children of the object.
 * When a content is selected in a page, a selection message is sent with the target PageTarget and subtarget ContentTarget. The page may have other contents or services that are not sent.
 * Some message could event sent the opposite structure : a content target with its page as a subtarget...
 * 
 * Do not creates such objects by your self. Transmit the configuration to the Ametys.message.Message constructor that will call the right Ametys.message.MessageTargetFactory.
 */
Ext.define("Ametys.message.MessageTarget", 
	{
		config: 
		{
			/**
			 * @cfg {String} type (required) The message target type. A non null and non empty string. You must use a constant defined in this class.
			 */
			type: null,
			/**
			 * @cfg {Object} parameters The parameter associated to the target, the value to set here depends on the specification of the constant you are using for the type. Cannot be null, but may be empty.
			 */
			parameters: {},
			/**
			 * @cfg {Ametys.message.MessageTarget[]} subtargets The targets associated to the message, the value to set here depends on the specification of the constant you are using for the type. Cannot be null, but may be empty.
			 */
			subtargets: []
		},
		
        /**
         * @property {Boolean} isMessageTarget Always true for MessageTarget objects.
         */
        isMessageTarget: true,
        
		/**
		 * Creates a target.
		 * @param {Object} config See configuration doc.
		 */
		constructor: function(config)
		{
			this.initConfig(config);
		},
		
		/**
		 * Get the first target matching the filter. If no filter is provided, get the first target (if available)
		 * @param {String/RegExp/Function} [filter] The filter on the target type. If the filter is a function, it must return a boolean true to match, and it has the following parameter:
		 * @param {Ametys.message.MessageTarget} filter.target The target to test. 
		 * @param {Number} [depth=0] The depth for filtering. 0 means it will dig all subtargets what ever the level is. 1 means it will only seek in the first level targets. And so on.
		 * @returns {Ametys.message.MessageTarget} The matching target, or the array of type hierarchy. Can be null.
		 * 
		 * The following examples will return a content target or null
		 * 		msg.getTarget("content");
		 * 		msg.getTarget(/^content$/);
		 * 		msg.getTarget(function (target) { return target.getType() == 'content' });
		 */
		getSubtarget: function(filter, depth)
		{
			return Ametys.message.MessageTargetHelper.findTarget(this._subtargets, filter, depth);
		},
		
		/**
		 * Same as #getTarget, but will return all the matching targets. When a target also have subtargets that would match the filter, only the parent target is returned.
		 * Get the targets matching the filter. If no filter is provided, get all the targets availables
		 * @param {String/RegExp/Function} [filter] The filter on the target type. If the filter is a function, it must return a boolean true to match, and it has the following parameter:
		 * @param {Ametys.message.MessageTarget} filter.target The target to test. 
		 * @param {Number} [depth=0] The depth for filtering. 0 means it will dig all subtargets what ever the level is. 1 means it will only seek in the first level targets. And so on.
		 * @returns {Ametys.message.MessageTarget[]} The non-null array of matching targets.
		 * 
		 * The following examples will return an array of PageTarget or an empty array
		 * 		msg.getTargets("page");
		 * 		msg.getTargets(/^page$/);
		 * 		msg.getTargets(function (target) { return target.getType() == 'page' });
		 */
		getSubtargets: function(filter, depth)
		{
			return Ametys.message.MessageTargetHelper.findTargets(this._subtargets, filter, depth);
		}	
	}
);
