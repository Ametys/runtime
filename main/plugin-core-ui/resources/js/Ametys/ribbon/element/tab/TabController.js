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
 * This class stands for the logic of a tab of the ribbon that handle show/hide:
 *   * depending of the current selection
 *   * depending of the currently focused tool
 */
Ext.define(
	"Ametys.ribbon.element.tab.TabController",
	{
		extend: "Ametys.ribbon.element.RibbonTabController",

		/**
		 * @cfg {String} selection-target-type Specify this configuration to obtain a tab that show/hide depending on the current selection type. The string is a regexp that have to match the current selection type. A leading '!' will reverse the regexp condition. See #cfg-subtarget-type. 
		 */
		/**
		 * @cfg {String} selection-subtarget-type When specified as the same time as #cfg-selection-target-type is, the tab will be show/hide only if the selection target is matching #cfg-selection-target-type AND if there is a subtarget that matched this regexp. A leading '!' will reverse the regexp condition. See #cfg-subtarget-type.
		 */
		/**
		 * @cfg {String} selection-subsubtarget-type Same as #cfg-subtarget-type at a third level.
		 */
		/**
		 * @cfg {String} selection-subsubsubtarget-type Same as #cfg-subtarget-type at a fourth level.
		 */
		/**
		 * @cfg {Boolean/String} only-first-level-target Specify to true to restrict the depth for filtering the selection target to the first level only. Otherwise it will search in all subtargets.
		 */
		
		/**
		 * @property {Boolean} _selection See #cfg-selection-target-type. True means the tab takes care of the selection
		 * @private
		 */
		/**
		 * @property {Ametys.message.MessageTarget[]} _matchingTargets The array of currently selected target matching the desired target type. See {@ link#cfg-selection-target-type}.
		 * @private
		 */
		/**
		 * @property {RegExp} _selectionTargetType See #cfg-selection-target-type converted as a regexp. The leading '!' is transmitted to {@link #_reversedSelectionTargetType}
		 * @private
		 */
		/**
		 * @property {Boolean} _reversedSelectionTargetType The leading '!' from {@link #cfg-selection-target-type} converted to true.
		 * @private
		 */
		/**
		 * @property {RegExp} _selectionSubtargetType See #cfg-selection-subtarget-type converted as a regexp. The leading '!' is transmitted to #_selectionReversedSubtargetType
		 * @private
		 */
		/**
		 * @property {Boolean} _selectionReversedSubtargetType The leading '!' from #cfg-subtarget-type converted to true.
		 * @private
		 */	
		/**
		 * @property {RegExp} _selectionSubsubtargetType See #cfg-selection-subsubtarget-type converted as a regexp. The leading '!' is transmitted to #_selectionReversedSubsubtargetType
		 * @private
		 */
		/**
		 * @property {Boolean} _selectionReversedSubsubtargetType The leading '!' from #cfg-subsubtarget-type converted to true.
		 * @private
		 */	
		/**
		 * @property {RegExp} _selectionSubsubsubtargetType See #cfg-selection-subsubsubtarget-type converted as a regexp. The leading '!' is transmitted to #_selectionReversedSubsubsubtargetType
		 * @private
		 */
		/**
		 * @property {Boolean} _selectionReversedSubsubsubtargetType The leading '!' from #cfg-subsubsubtarget-type converted to true.
		 * @private
		 */	
		/**
		 * @property {Boolean} _onlyFirstLevelTarget The internal property corresponding to #cfg-only-first-level-target
		 * @private
		 */
		
		/**
		 * @cfg {String} tool-role When specified, the tab will only be visible if a tool with this role is focused. This is a regexp. A leading '!' will reverse the regexp condition.
		 */
		/**
		 * @property {RegExp} _toolRole The #cfg-tool-role converted as a regexp. The '!' condition is available in #_toolReveredRole.
		 * @private
		 */
		/**
		 * @property {Boolean} _toolReveredRole The #cfg-tool-role converted as a regexp and this boolean stands for the '!' condition.
		 * @private
		 */
		/**
		 * @property {Boolean} _toolFocused When using #cfg-tool-role this boolean reflects the focus state of the associated tool.
		 */
		
		constructor: function(config)
		{
			this.callParent(arguments);
			
			var targetType = this.getInitialConfig("selection-target-type") || this.getInitialConfig("target-type"); 
			this._matchingTargets = [];
			
			if (targetType)
			{
				this._selection = true;
				Ametys.message.MessageBus.on(Ametys.message.Message.SELECTION_CHANGING, this._onSelectionChanging, this);
				Ametys.message.MessageBus.on(Ametys.message.Message.SELECTION_CHANGED, this._onSelectionChanged, this);
				
				this._onlyFirstLevelTarget = String(this.getInitialConfig("only-first-level-target")) == "true";
				
				var i = targetType.indexOf('!');
				if (i == 0)
				{
					this._selectionTargetType = new RegExp(targetType.substring(1));
					this._reversedSelectionTargetType = true;
				}
				else
				{
					this._selectionTargetType = new RegExp(targetType);
					this._reversedSelectionTargetType = false;
				}
				
				var subtargetType = this.getInitialConfig("selection-subtarget-type") || this.getInitialConfig("subtarget-type"); 
				if (subtargetType)
				{
					var i = subtargetType.indexOf('!');
					if (i == 0)
					{
						this._selectionSubtargetType = new RegExp(subtargetType.substring(1));
						this._selectionReversedSubtargetType = true;
					}
					else
					{
						this._selectionSubtargetType = new RegExp(subtargetType);
						this._selectionReversedSubtargetType = false;
					}
					 
					var subsubtargetType = this.getInitialConfig("selection-subsubtarget-type") || this.getInitialConfig("subsubtarget-type"); 
					if (subsubtargetType)
					{
						var i = subsubtargetType.indexOf('!');
						if (i == 0)
						{
							this._selectionSubsubtargetType = new RegExp(subsubtargetType.substring(1));
							this._selectionReversedSubsubtargetType = true;
						}
						else
						{
							this._selectionSubsubtargetType = new RegExp(subsubtargetType);
							this._selectionReversedSubsubtargetType = false;
						}
						
						var subsubsubtargetType = this.getInitialConfig("selection-subsubsubtarget-type") || this.getInitialConfig("subsubsubtarget-type"); 
						if (subsubsubtargetType)
						{
							var i = subsubsubtargetType.indexOf('!');
							if (i == 0)
							{
								this._selectionSubsubsubtargetType = new RegExp(subsubsubtargetType.substring(1));
								this._selectionReversedSubsubsubtargetType = true;
							}
							else
							{
								this._selectionSubsubsubtargetType = new RegExp(subsubsubtargetType);
								this._selectionReversedSubsubsubtargetType = false;
							}
						}
					}
				}
			}
			
			var toolRole = this.getInitialConfig("tool-role");
			if (toolRole)
			{
				this._toolFocused = false;
				
				Ametys.message.MessageBus.on(Ametys.message.Message.TOOL_FOCUSED, this._onAnyToolFocused, this);
				Ametys.message.MessageBus.on(Ametys.message.Message.TOOL_BLURRED, this._onAnyToolBlurred, this);

				var i = toolRole.indexOf('!');
				if (i == 0)
				{
					this._toolRole = new RegExp(toolRole.substring(1));
					this._toolReveredRole = true;
				}
				else
				{
					this._toolRole = new RegExp(toolRole);
					this._toolReveredRole = false;
				}
			}
		},
		
		/**
		 * @private
		 * Listener when the selection is about to changed. For ribbon policy it is important to hide a tabpanel as soon as possible 
		 * @param {Ametys.message.Message} message The selection message. Cannot be null.
		 */
		_onSelectionChanging: function(message)
		{
			var index = this._tabPanel.ownerCt.items.indexOf(this._tabPanel);
			var tabEl = this._tabPanel.ownerCt.getTabBar().items.get(index);
			if (tabEl.isVisible())
			{
				var matchingTargets = false;
				var me = this;
				var targets = message.getParameters()['targets'];
				
				Ext.Array.each(targets, function(target) {
					if (!me._reversedSelectionTargetType && me._selectionTargetType.test(target)
							|| me._reversedSelectionTargetType && !me._selectionTargetType.test(target))
					{
						matchingTargets = true;
					}
				});
				
				if (!matchingTargets)
				{
					this.hide();
				}
			}
		},

		/**
		 * Listener when the selection has changed. Registered only if #cfg-selection-target-type is specified, but can always be called manually. 
		 * Will show or hide the tab effectively upon the current selection.
		 * @param {Ametys.message.Message} [message] The selection message. Can be null to get the last selection message
		 * @protected
		 */
		_onSelectionChanged: function(message)
		{
			message = message || Ametys.message.MessageBus.getCurrentSelectionMessage();
			
			if (this._toolFocused === false)
			{
				// this tab works only with a tool #cfg-tool-role; when the tool is not focused selection message need to be ignored
				return;
			}
			
			var noSelection = message.getTargets().length == 0;
			this._matchingTargets = this._getMatchingSelectionTargets(message);
			
			if (this._selection)
			{
				if (noSelection || this._matchingTargets.length == 0)
				{
					this.hide();
				}
				else
				{
					this.show();
				}
			}
		},
		
		/**
		 * Listener when a tool has been focused. Registered only if #cfg-tool-role is specified. Will enable the buttons effectively.
		 * @param {Ametys.message.Message} message The focus message
		 * @protected
		 */
		_onAnyToolFocused: function(message)
		{
			if (this._getMatchingToolsTarget(message).length > 0)
			{
				this._toolFocused = true;
				this.show();
			}
		},

		/**
		 * Listener when a tool has been blurred. Registered only if #cfg-tool-role is specified. Will disable the buttons effectively.
		 * @param {Ametys.message.Message} message The focus message
		 * @protected
		 */
		_onAnyToolBlurred: function(message)
		{
			if (this._getMatchingToolsTarget(message).length > 0)
			{
				this._toolFocused = false;
				this.hide();
			}
		},
		
		/**
		 * Get the matching targets in the message
		 * Test if the message if matching upon the #_selectionTargetType, #_selectionSubtargetType and #_selectionSubsubtargetType
		 * @param {Ametys.message.Message} message The message to test
		 * @return {Ametys.message.MessageTarget[]} The non-null array of matching targets
		 * @private
		 */		
		_getMatchingSelectionTargets: function(message)
		{
			var me = this;
			
			var finalTargets = [];
			if (this._selection)
			{
				var targets = message.getTargets(Ext.bind(this._testTargetLevel0, this), this._onlyFirstLevelTarget ? 1 : 0);
				
				if (!me._selectionSubtargetType)
				{
					finalTargets = targets;
				}
				else
				{
					for (var i = 0; i < targets.length; i++)
					{
						var stargets = targets[i].getSubtargets(Ext.bind(this._testTargetLevel1, this), 1);
						
						if (!me._selectionSubsubtargetType)
						{
							if (stargets.length > 0 || (me._selectionReversedSubtargetType && targets[i].getSubtargets().length == 0))
							{
								finalTargets.push(targets[i]);
							}
						}
						else
						{
							for (var j = 0; j < stargets.length; j++)
							{
								var sstargets = stargets[j].getSubtargets(Ext.bind(this._testTargetLevel2, this), 1);
								
								if (!me._selectionSubsubsubtargetType)
								{
									if (sstargets.length > 0 || (me._selectionReversedSubsubtargetType && stargets[j].getSubtargets().length == 0))
									{
										finalTargets.push(targets[i]);
									}
								}
								else
								{
									for (var k = 0; k < sstargets.length; k++)
									{
										var ssstargets = sstargets[k].getSubtargets(Ext.bind(this._testTargetLevel3, this), 1);
										if (ssstargets.length > 0)
										{
											finalTargets.push(targets[i]);
										}
									}
								}
							}
						}					
					}
				}
			}
			
			return finalTargets;
		},
		
		/**
		 * @private
		 * Tests if the target of level 0 matches the configured #cfg-selection-target-type
		 * @return true if the target matches
		 */
		_testTargetLevel0: function (target)
		{
			return !this._reversedSelectionTargetType && this._selectionTargetType.test(target.getType())
			|| this._reversedSelectionTargetType && !this._selectionTargetType.test(target.getType());
		},
		
		/**
		 * @private
		 * Tests if the target of level 1 matches the configured #cfg-selection-subtarget-type
		 * @return true if the target matches
		 */
		_testTargetLevel1: function (target)
		{
			return !this._selectionReversedSubtargetType && this._selectionSubtargetType.test(target.getType())
			|| this._selectionReversedSubtargetType && !this._selectionSubtargetType.test(target.getType());
		},
		
		/**
		 * @private
		 * Tests if the target of level 2 matches the configured #cfg-selection-subsubtarget-type
		 * @return true if the target matches
		 */
		_testTargetLevel2: function (target)
		{
			return !this._selectionReversedSubsubtargetType && this._selectionSubsubtargetType.test(target.getType())
			|| this._selectionReversedSubsubtargetType && !this._selectionSubsubtargetType.test(target.getType());
		},
		
		/**
		 * @private
		 * Tests if the target of level 3 matches the configured #cfg-selection-subsubsubtarget-type
		 * @return true if the target matches
		 */
		_testTargetLevel3: function (target)
		{
			return !this._selectionReversedSubsubsubtargetType && this._selectionSubsubsubtargetType.test(target.getType())
			|| this._selectionReversedSubsubsubtargetType && !this._selectionSubsubsubtargetType.test(target.getType());
		},
		
		/**
		 * Get the matching targets in the message
		 * Test if the message if matching upon the #_toolRole
		 * @param {Ametys.message.Message} message The message to test
		 * @returns {Ametys.message.MessageTarget[]} The non-null array of matching targets
		 * @private
		 */		
		_getMatchingToolsTarget: function(message)
		{
			var me = this;
			
			if (this._toolRole)
			{
				return message.getTargets(
						function (target)
						{
							return !me._toolReveredRole && me._toolRole.test(target.getParameters()['id'])
							|| me._toolReveredRole && !me._toolRole.test(target.getParameters()['id']);
						}
				);
			}
			else
			{
				return [];
			}
		}
	}
);
