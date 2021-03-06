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
 * This class is the class for all tools who take care of the current selection.
 */
Ext.define('Ametys.tool.SelectionTool', {
	extend: "Ametys.tool.Tool",
	
	/**
	 * @cfg {String} selection-target-id Specify this configuration to obtain a tool that take care of the current selection type. The string is a regexp that have to match the current selection type. See #cfg-subtarget-id.
	 */
	/**
	 * @cfg {String} selection-subtarget-id When specified as the same time as #cfg-selection-target-id is, the tool will be enable/disabled only if the selection target is matching #cfg-selection-target-id AND if there is a subtarget that matched this regexp. See #cfg-subsubtarget-id.
	 */
	/**
	 * @cfg {String} selection-subsubtarget-id Same as #cfg-subtarget-id at a third level.
	 */
	/**
	 * @cfg {String} selection-enable-multiselection=false If 'false' the tool will be in a "no match selection state" as soon as the are many elements selected. Works only when #cfg-selection-target-id is specified.
	 */
	/**
	 * @cfg {String} selection-description-empty The description when the selection is empty
	 */
	/**
	 * @cfg {String} selection-description-nomatch The description when the selection does not match the awaited #cfg-selection-target-id
	 */
	/**
	 * @cfg {String} selection-description-multiselectionforbidden The description when the selection is multiple but #cfg-selection-enable-multiselection is false.
	 */
	
	/**
	 * @property {Ametys.message.MessageTarget[]} _currentSelectionTargets The current selection targets matching to the tool configuration. See #cfg-selection-enable-multiselection. See #cfg-selection-target-id. See #cfg-selection-subtarget-id. See #cfg-selection-subsubtarget-id.
	 * @private
	 */
	/**
	 * @property {Boolean} _selection See #cfg-selection-target-id. True means the tool takes care of the selection
	 * @private
	 */
	/**
	 * @property {RegExp} _selectionTargetType See #cfg-selection-target-id converted as a regexp.
	 * @private
	 */
	/**
	 * @property {RegExp} _selectionSubtargetId See #cfg-selection-subtarget-id converted as a regexp.
	 * @private
	 */
	/**
	 * @property {RegExp} _selectionSubsubtargetId See #cfg-selection-subsubtarget-id converted as a regexp.
	 * @private
	 */
	
	constructor: function(config)
	{
		this.callParent(arguments);
		
		var targetId = this.getInitialConfig("selection-target-id"); 
		if (targetId)
		{
			this._selection = true;
			
			Ametys.message.MessageBus.on(Ametys.message.Message.SELECTION_CHANGED, this._onSelectionChanged, this);
			this._selectionTargetType = new RegExp(targetId);
			
			var subtargetId = this.getInitialConfig("selection-subtarget-id"); 
			if (subtargetId)
			{
				this._selectionSubtargetId = new RegExp(subtargetId);
				
				var subsubtargetId = this.getInitialConfig("selection-subsubtarget-id"); 
				if (subsubtargetId)
				{
					this._selectionSubsubtargetId = new RegExp(subsubtargetId);
				}
			}
		}
	},
	
	getMBSelectionInteraction: function()
	{
		return Ametys.tool.Tool.MB_TYPE_LISTENING;
	},
	
	/**
	 * Listener when the selection has changed. Registered only if #cfg-selection-target-id is specified, but can always be called manually. 
	 * @param {Ametys.message.Message} [message] The selection message. Can be null to get the last selection message
	 * @protected
	 */
	_onSelectionChanged: function(message)
	{
		message = message || Ametys.message.MessageBus.getCurrentSelectionMessage();
		
		if (this._isRefreshNeeded (message))
		{
			this.showOutOfDate(true);
		}
	},
	
	setParams: function (params)
	{
		this.callParent(arguments);
		
		var message = Ametys.message.MessageBus.getCurrentSelectionMessage();
		if (this._isRefreshNeeded (message))
		{
			this.refresh();
		}
	},
	
	/**
	 * @protected
	 * This function looks at the given selection message to determine if the tool has to be refreshed.
	 * If the current targets do not match the expected target(s) the function #setNoSelectionMatchState is called and the function will return false.
	 * @param message the selection message. 
	 * @return true if the tool has to be refreshed
	 */
	_isRefreshNeeded: function (message)
	{
		var noSelection = message.getTargets().length == 0;
		var singleSelection = message.getTargets().length == 1;
		var multiSelectionEnabled = Ext.isBoolean (this.getInitialConfig("selection-enable-multiselection")) ? this.getInitialConfig("selection-enable-multiselection") : this.getInitialConfig("selection-enable-multiselection") == 'true';
		
		if (noSelection)
		{
			// noselection
			this.setNoSelectionMatchState(this.getInitialConfig("selection-description-empty"));
			return false;
		}
		else if (!singleSelection && !multiSelectionEnabled)
		{
			// selectionsize
			this.setNoSelectionMatchState(this.getInitialConfig("selection-description-multiselectionforbidden"));
			return false;
		}
		else
		{
			var matchingTargets = this._getMatchingSelectionTargets(message);
			
			if (matchingTargets.length == 0)
			{
				// nomatch
				this.setNoSelectionMatchState(this.getInitialConfig("selection-description-nomatch") || this.getInitialConfig("selection-description-empty"));
				return false;
			}
			else if (this._isCurrentSelectionChanged (matchingTargets))
			{
				this._currentSelectionTargets = matchingTargets;
				return true;
			}
			
			return false;
		}
	},
	
	/**
	 * Compares two targets and returns true if the two targets are equals. The default implementation compares the parameters "id" of the targets.
	 * @return true if the tow targets are the same.
	 * @template
	 */
	areSameTargets: function (target1, target2)
	{
		if (target1.getParameters().id && target2.getParameters().id)
		{
			return target1.getParameters().id == target2.getParameters().id;
		}
		return false;
	},
	
	/**
	 * Get the current selection targets matching the tool selection configuration
	 * @return {Ametys.message.MessageTarget[]} the current selection targets concerning the tool. Can be null.
	 */
	getCurrentSelectionTargets: function ()
	{
		return this._currentSelectionTargets || [];
	},
	
	/**
	 * @protected
	 * @template
	 * This function is called when the selection is empty or do not no match the excepted target.
	 * Override this function to handle the display of the message. In your implementation you have to call parent function!
	 * @param message The message to display
	 */
	setNoSelectionMatchState: function (message)
	{
		this._currentSelectionTargets = null;
		this.showUpToDate();
	},
	
	/**
	 * @protected
	 * Returns the targets from message matching the current selection targets of the tool
	 * @param {Ametys.message.Message} message the message
	 * @return {Ametys.message.MessageTarget[]} The common targets between the current targets of the tool and the targets in message
	 */
	getTargetsInCurrentSelectionTargets: function (message)
	{
		var targets = this._getMatchingSelectionTargets(message)
		
		if (this.getCurrentSelectionTargets() == null || targets == null)
		{
			return [];
		}	
		
		var targetsInSelection = [];
		
		for (var i=0; i < this.getCurrentSelectionTargets().length; i++)
		{
			for (var j=0; j < targets.length; j++)
			{
				if (this.areSameTargets(this.getCurrentSelectionTargets()[i], targets[j]))
				{
					targetsInSelection.push(targets[j]);
				}
			}
		}
		
		return targetsInSelection;
	},
	
	/**
	 * Determine if the current selection has changed from the point of view of tool
	 * @param {Ametys.message.MessageTarget[]} targets The targets to be test
	 * @returns true if targets does not match the registered selection
	 */
	_isCurrentSelectionChanged: function (targets)
	{
		if (this.getCurrentSelectionTargets() == null || targets == null || this.getCurrentSelectionTargets().length != targets.length)
		{
			return true;
		}	
		
		for (var i=0; i < this.getCurrentSelectionTargets().length; i++)
		{
			var found = false;
			for (var j=0; j < targets.length; j++)
			{
				if (this.areSameTargets(this.getCurrentSelectionTargets()[i], targets[j]))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				return true;
			}
		}
		
		return false;
	},
	
	/**
	 * Get the matching targets in the message
	 * Test if the message if matching upon the #_selectionTargetType, #_selectionSubtargetId and #_selectionSubsubtargetId
	 * @param {Ametys.message.Message} message The message to test
	 * @returns {Ametys.message.MessageTarget[]} The non-null array of matching targets
	 * @private
	 */		
	_getMatchingSelectionTargets: function(message)
	{
		var me = this;
		
		var finalTargets = [];
		if (this._selection)
		{
			var targets = message.getTargets(
					function (target)
					{
						return me._selectionTargetType.test(target.getId());
					}
			);
			
			if (!me._selectionSubtargetId)
			{
				finalTargets = targets;
			}
			else
			{
				for (var i = 0; i < targets.length; i++)
				{
					var stargets = targets[i].getSubtargets(
							function (target)
							{
								return me._selectionSubtargetId.test(target.getId());
							}
					);
					
					if (!me._selectionSubsubtargetId)
					{
						if (stargets.length > 0)
						{
							finalTargets.push(targets[i]);
						}
					}
					else
					{
						for (var j = 0; j < targets.length; j++)
						{
							var sstargets = stargets[j].getSubtargets(
									function (target)
									{
										return me._selectionSubsubtargetId.test(target.getId());
									}
							);
						}
						
						if (sstargets.length > 0)
						{
							finalTargets.push(targets[i]);
						}
					}					
				}
			}
		}
		
		return finalTargets;
	}
});

	