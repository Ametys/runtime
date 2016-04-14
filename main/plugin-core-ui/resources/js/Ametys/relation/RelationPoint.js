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
 * This class represents the point of a relation: move, copy or reference operation (start or end).
 * It does contains a list of resources being operated and a kind of operation.
 * 
 * The resources are described using Ametys.message.MessageTarget.
 */
Ext.define("Ametys.relation.RelationPoint", 
	{
		/**
		 * @cfg {Object/Object[]/Ametys.message.MessageTarget/Ametys.message.MessageTarget[]} targets (required) The targets configurations, or the targets themselves that will be part of the operation.
		 */
		targets: [],
		/**
		 * @property {Ametys.message.MessageTarget[]} targets The targets instances associated to the point. See #cfg-targets.
		 */
		/**
		 * @cfg {String/String[]} [relationTypes=Ametys.relation.Relation.REFERENCE] The supported relations for this point. One of the constants Ametys.relation.Relation.MOVE, Ametys.relation.Relation.COPY or Ametys.relation.Relation.REFERENCE.
		 */ 
		/**
		 * @property {String[]} relationTypes See #cfg-relationTypes.
		 */
		/**
		 * @cfg {Number} [positionInTargets=-1] When the relation point is a destination point, set this configuration to specify the index where to insert the sources in the #cfg-targets. For example, 0 means to insert as the first child and -1 means at the end. 
		 */
		/**
		 * @property {Number} positionInTargets See #cfg-positionInTargets.
		 */
		
		/**
		 * @private
		 * @property {Function[]} callbacks Callback registered through #waitForTargets during asynchronous process. 
		 */ 
		
		/**
		 * @property {Boolean} _isReady See #isReady
		 * @private
		 */
		_isReady: false,

		/**
		 * Creates a relation point.
		 * @param {Object} config The configuration object
		 */
		constructor: function(config)
		{
			// Handle relations
			this.relationTypes = config.relationTypes || [Ametys.relation.Relation.REFERENCE];
			if (!Ext.isArray(this.relationTypes))
			{
				this.relationTypes = [this.relationTypes];
			}
			
			// Handle position
			this.positionInTargets = config.positionInTargets;
			if (this.positionInTargets == null)
			{
				this.positionInTargets = -1;
			}
			
			// Handle message targets
			this.targets = config.targets || []; 
			if (!Ext.isArray(this.targets))
			{
				this.targets = [this.targets];
			}
			
			this.callbacks = [];
			
			var me = this;
			function cb()
			{
				me._isReady = true;
				for (var i = 0; i < me.callbacks.length; i++)
				{
					me.callbacks[i]();
				}
			}
			
			if (this.targets.length == 0 || this.targets[0].self)
			{
				// Targets are already fine
		        Ext.defer(cb, 1, this);
			}
			else
			{
				// Targets need to be prepared
				function prep(targets)
				{
					me.targets = targets;
			        Ext.defer(cb, 1, me);
				}
				Ametys.message.MessageTargetFactory.createTargets(this.targets, prep);
			}
		},
		
		/**
		 * Since Ametys.message.MessageTarget creation is an asynchronous process, this method allows to ensure that message targets are ready to use
		 * @param {Function} callback This function is called when the targets are ready (so is directly called if ready)
		 */
		waitForTargets: function(callback)
		{
			if (this._isReady)
			{
				Ext.defer(callback, 1, this);
			}
			else
			{
				this.callbacks.push(callback);
			}
		},
		
		/**
		 * As the creation process of a message target is an asynchronous process, this let you know if it is done.
		 * @returns {Boolean} True if the message target have been created 
		 */
		isReady: function()
		{
			return this._isReady;
		},
		
		/**
		 * Get the first target matching the filter. If no filter is provided, get the first target (if available)
		 * @param {String/RegExp/Function} [filter] The filter upon the target type. If the filter is a function, it must return a boolean true to match, and it has the following parameter:
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
				var message = "Cannot get target since it is not ready";
				this.getLogger().warn(message);
				throw new Error(message);
			}
			
			return Ametys.message.MessageTargetHelper.findTarget(this.targets, filter, depth);
		},
		
		/**
		 * Same as #getTarget, but will return all the matching targets. When a target also have subtargets that would match the filter, only the parent target is returned.
		 * Get the targets matching the filter. If no filter is provided, get all the targets available
		 * @param {String/RegExp/Function} [filter] The filter uppon the target type. If the filter is a function, it must return a boolean true to match, and it has the following parameter:
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
				var message = "Cannot get targets since it is not ready";
				this.getLogger().warn(message);
				throw new Error(message);
			}

			return Ametys.message.MessageTargetHelper.findTargets(this.targets, filter, depth);
		}
	}
);
