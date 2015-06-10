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
 * This class is an internal helper to find targets
 * @private
 */
Ext.define("Ametys.message.MessageTargetHelper",
	{
		singleton: true,
	
		/**
		 * Filter an array of target and returns the first matching one
		 * @param {Object/Object[]/Ametys.message.MessageTarget/Ametys.message.MessageTarget[]} targets The targets to test (or the target config) 
		 * @param {String/RegExp/Function} [filter] The filter uppon the target type. If the filter is a function, it must returns a boolean true to match, and it has the target as single parameter.
		 * @param {Number} [depth=0] The depth for filtering. 0 means it will dig all subtargets what ever the level is. 1 means it will only seek in the first level targets. And so on.
		 * @returns {Ametys.message.MessageTarget} The matching target, or the array of type hierarchy (or the target config if config was provided). Can be null.
		 */
		findTarget: function(targets, filter, depth)
		{
			depth = depth || 0;
			
			if (filter == null)
			{
				if (targets == null || !targets.length || targets.length == 0)
				{
					return null;
				}
				else
				{
					return targets[0];
				}
			}
			else
			{
				targets = Ext.isArray(targets) ? targets : [targets];
				
				// Search root level
				for (var i=0; i < targets.length; i++)
				{
					var target = targets[i];
					// Getting type and subtargets from either an object or a MessageTarget
					var type;
					if (Ext.getClassName(target) == "Ametys.message.MessageTarget")
					{
						type = target.getType();
					}
					else
					{
						type = target.type;
					}
					
					if ((typeof filter.test == "function" && filter.test(type))
							|| (typeof filter == "function" && filter(target))
							|| (filter.toString() == type))
					{
						return target;
					}
				}
				
				// Seach down levels
				if (depth != 1)
				{
					for (var i=0; i < targets.length; i++)
					{
						var target = targets[i];
						// Getting type and subtargets from either an object or a MessageTarget
						var subtargets;
						if (Ext.getClassName(target) == "Ametys.message.MessageTarget")
						{
							subtargets = target.getSubtargets();
						}
						else
						{
							subtargets = target.subtargets;
						}

						var matchingTarget;
						
						
						if (matchingTarget = Ametys.message.MessageTargetHelper.findTarget(subtargets || [], filter, depth - 1))
						{
							return matchingTarget;
						}
					}
				}
				
				return null;
			}
		},
		
		/**
		 * Filter an array of target and returns the matching ones. When a target also have subtargets that would match the filter, only the parent target is returned.
		 * @param {Object/Object[]/Ametys.message.MessageTarget/Ametys.message.MessageTarget[]} targets The target to test. 
		 * @param {String/RegExp/Function} [filter] The filter upon the target type. If the filter is a function, it must returns a boolean true to match, and it has the following parameter:
		 * @param {Number} [depth=0] The depth for filtering. 0 means it will dig all subtargets what ever the level is. 1 means it will only seek in the first level targets. And so on.
		 * @returns {Object/Ametys.message.MessageTarget[]} The matching targets in a non-null array (or the targets config if config was provided).
		 */
		findTargets: function(targets, filter, depth)
		{
			depth = depth || 0;
			
			if (filter == null)
			{
				if (targets == null || !targets.length)
				{
					return [];
				}
				else
				{
					return targets;
				}
			}
			else
			{
				targets = Ext.isArray(targets) ? targets : [targets];

				var matchingTargets = [];
				
				// Search root level
				for (var i=0; i < targets.length; i++)
				{
					var target = targets[i];
					// Getting type and subtargets from either an object or a MessageTarget
					var type, subtargets;
					if (Ext.getClassName(target) == "Ametys.message.MessageTarget")
					{
						type = target.getType();
						subtargets = target.getSubtargets();
					}
					else
					{
						type = target.type;
						subtargets = target.subtargets;
					}
					
					if ((typeof filter.test == "function" && filter.test(type))
							|| (typeof filter == "function" && filter(target))
							|| (filter.toString() == type))
					{
						matchingTargets.push(target);
					}
					// Search down levels
					else if (depth != 1)
					{
						if (matchingSubTarget = Ametys.message.MessageTargetHelper.findTargets(subtargets || [], filter, depth - 1))
						{
							for (var j=0; j < matchingSubTarget.length; j++)
							{
								matchingTargets.push(matchingSubTarget[j]);
							}
						}
						
						/*for (var i=0; i < targets.length; i++)
						{
							var target = targets[i];

							var matchingSubTarget;
							if (matchingSubTarget = Ametys.message.MessageTargetHelper.findTargets(target.subtargets || [], filter, depth - 1))
							{
								for (var j=0; j < matchingSubTarget.length; j++)
								{
									matchingTargets.push(matchingSubTarget[j]);
								}
							}
						}*/
					}
				}
				
				return matchingTargets;
			}
		}
	}
);
