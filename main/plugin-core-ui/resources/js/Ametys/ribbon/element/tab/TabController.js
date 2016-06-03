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
		 * @cfg {String} selection-target-id Specify this configuration to obtain a tab that show/hide depending on the current selection type. The string is a regexp that have to match the current selection type. A leading '!' will reverse the regexp condition. See #cfg-subtarget-id. 
		 */
		/**
		 * @cfg {String} selection-subtarget-id When specified as the same time as #cfg-selection-target-id is, the tab will be show/hide only if the selection target is matching #cfg-selection-target-id AND if there is a subtarget that matched this regexp. A leading '!' will reverse the regexp condition. See #cfg-subtarget-id.
		 */
		/**
		 * @cfg {String} selection-subsubtarget-id Same as #cfg-subtarget-id at a third level.
		 */
		/**
		 * @cfg {String} selection-subsubsubtarget-id Same as #cfg-subtarget-id at a fourth level.
		 */
		/**
		 * @cfg {Boolean/String} only-first-level-target Specify to true to restrict the depth for filtering the selection target to the first level only. Otherwise it will search in all subtargets.
		 */
		
		/**
		 * @property {Boolean} _selection See #cfg-selection-target-id. True means the tab takes care of the selection
		 * @private
		 */
		/**
		 * @property {Ametys.message.MessageTarget[]} _matchingTargets The array of currently selected target matching the desired target type. See {@ link#cfg-selection-target-id}.
		 * @private
		 */
		/**
		 * @property {RegExp} _selectionTargetId See #cfg-selection-target-id converted as a regexp. The leading '!' is transmitted to {@link #_reversedSelectionTargetId}
		 * @private
		 */
		/**
		 * @property {Boolean} _reversedSelectionTargetId The leading '!' from {@link #cfg-selection-target-id} converted to true.
		 * @private
		 */
		/**
		 * @property {RegExp} _selectionSubtargetId See #cfg-selection-subtarget-id converted as a regexp. The leading '!' is transmitted to #_selectionReversedSubtargetId
		 * @private
		 */
		/**
		 * @property {Boolean} _selectionReversedSubtargetId The leading '!' from #cfg-subtarget-id converted to true.
		 * @private
		 */	
		/**
		 * @property {RegExp} _selectionSubsubtargetId See #cfg-selection-subsubtarget-id converted as a regexp. The leading '!' is transmitted to #_selectionReversedSubsubtargetId
		 * @private
		 */
		/**
		 * @property {Boolean} _selectionReversedSubsubtargetId The leading '!' from #cfg-subsubtarget-id converted to true.
		 * @private
		 */	
		/**
		 * @property {RegExp} _selectionSubsubsubtargetId See #cfg-selection-subsubsubtarget-id converted as a regexp. The leading '!' is transmitted to #_selectionReversedSubsubsubtargetId
		 * @private
		 */
		/**
		 * @property {Boolean} _selectionReversedSubsubsubtargetId The leading '!' from #cfg-subsubsubtarget-id converted to true.
		 * @private
		 */	
		/**
		 * @property {Boolean} _onlyFirstLevelTarget The internal property corresponding to #cfg-only-first-level-target
		 * @private
		 */
		
		/**
		 * @cfg {String} tool-id When specified, the tab will only be visible if a tool with this id is focused. This is a regexp. A leading '!' will reverse the regexp condition.
		 */
		/**
		 * @property {RegExp} _toolId The #cfg-tool-id converted as a regexp. The '!' condition is available in #_toolReveredRole.
		 * @private
		 */
		/**
		 * @property {Boolean} _toolReveredRole The #cfg-tool-id converted as a regexp and this boolean stands for the '!' condition.
		 * @private
		 */
		/**
		 * @property {Boolean} _toolFocused When using #cfg-tool-id this boolean reflects the focus state of the associated tool.
		 */
		
		constructor: function(config)
		{
			this.callParent(arguments);
			
			var targetId = this.getInitialConfig("selection-target-id") || this.getInitialConfig("target-id"); 
			this._matchingTargets = [];
			
			if (targetId)
			{
				this._selection = true;
				Ametys.message.MessageBus.on(Ametys.message.Message.SELECTION_CHANGING, this._onSelectionChanging, this);
				Ametys.message.MessageBus.on(Ametys.message.Message.SELECTION_CHANGED, this._onSelectionChanged, this);
				
				this._onlyFirstLevelTarget = String(this.getInitialConfig("only-first-level-target")) == "true";
				
				var i = targetId.indexOf('!');
				if (i == 0)
				{
					this._selectionTargetId = new RegExp(targetId.substring(1));
					this._reversedSelectionTargetId = true;
				}
				else
				{
					this._selectionTargetId = new RegExp(targetId);
					this._reversedSelectionTargetId = false;
				}
				
				var subtargetId = this.getInitialConfig("selection-subtarget-id") || this.getInitialConfig("subtarget-id"); 
				if (subtargetId)
				{
					var i = subtargetId.indexOf('!');
					if (i == 0)
					{
						this._selectionSubtargetId = new RegExp(subtargetId.substring(1));
						this._selectionReversedSubtargetId = true;
					}
					else
					{
						this._selectionSubtargetId = new RegExp(subtargetId);
						this._selectionReversedSubtargetId = false;
					}
					 
					var subsubtargetId = this.getInitialConfig("selection-subsubtarget-id") || this.getInitialConfig("subsubtarget-id"); 
					if (subsubtargetId)
					{
						var i = subsubtargetId.indexOf('!');
						if (i == 0)
						{
							this._selectionSubsubtargetId = new RegExp(subsubtargetId.substring(1));
							this._selectionReversedSubsubtargetId = true;
						}
						else
						{
							this._selectionSubsubtargetId = new RegExp(subsubtargetId);
							this._selectionReversedSubsubtargetId = false;
						}
						
						var subsubsubtargetId = this.getInitialConfig("selection-subsubsubtarget-id") || this.getInitialConfig("subsubsubtarget-id"); 
						if (subsubsubtargetId)
						{
							var i = subsubsubtargetId.indexOf('!');
							if (i == 0)
							{
								this._selectionSubsubsubtargetId = new RegExp(subsubsubtargetId.substring(1));
								this._selectionReversedSubsubsubtargetId = true;
							}
							else
							{
								this._selectionSubsubsubtargetId = new RegExp(subsubsubtargetId);
								this._selectionReversedSubsubsubtargetId = false;
							}
						}
					}
				}
			}
			
			var toolId = this.getInitialConfig("tool-id");
			if (toolId)
			{
				this._toolFocused = false;
				
				Ametys.message.MessageBus.on(Ametys.message.Message.TOOL_FOCUSED, this._onAnyToolFocused, this);
				Ametys.message.MessageBus.on(Ametys.message.Message.TOOL_BLURRED, this._onAnyToolBlurred, this);

				var i = toolId.indexOf('!');
				if (i == 0)
				{
					this._toolId = new RegExp(toolId.substring(1));
					this._toolReveredRole = true;
				}
				else
				{
					this._toolId = new RegExp(toolId);
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
		    Ext.Array.forEach(this._tabPanels, function(tabPanel) {
		        var index = tabPanel.ownerCt.items.indexOf(tabPanel);
		        var tabEl = tabPanel.ownerCt.getTabBar().items.get(index);
		        if (tabEl.isVisible())
		        {
		            var matchingTargets = false;
		            var me = this;
		            var targets = message.getParameters()['targets'];
		            
		            Ext.Array.each(targets, function(target) {
		                if (!me._reversedSelectionTargetId && me._selectionTargetId.test(target)
		                        || me._reversedSelectionTargetId && !me._selectionTargetId.test(target))
		                {
		                    matchingTargets = true;
		                }
		            });
		            
		            if (!matchingTargets)
		            {
		                this.hide();
		            }
		        }
		    }, this);
		},

		/**
		 * Listener when the selection has changed. Registered only if #cfg-selection-target-id is specified, but can always be called manually. 
		 * Will show or hide the tab effectively upon the current selection.
		 * @param {Ametys.message.Message} [message] The selection message. Can be null to get the last selection message
		 * @protected
		 */
		_onSelectionChanged: function(message)
		{
			message = message || Ametys.message.MessageBus.getCurrentSelectionMessage();
			
			if (this._toolFocused === false)
			{
				// this tab works only with a tool #cfg-tool-id; when the tool is not focused selection message need to be ignored
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
		 * Listener when a tool has been focused. Registered only if #cfg-tool-id is specified. Will enable the buttons effectively.
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
		 * Listener when a tool has been blurred. Registered only if #cfg-tool-id is specified. Will disable the buttons effectively.
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
		 * Test if the message if matching upon the #_selectionTargetId, #_selectionSubtargetId and #_selectionSubsubtargetId
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
				
				if (!me._selectionSubtargetId)
				{
					finalTargets = targets;
				}
				else
				{
					for (var i = 0; i < targets.length; i++)
					{
						var stargets = targets[i].getSubtargets(Ext.bind(this._testTargetLevel1, this), 1);
						
						if (!me._selectionSubsubtargetId)
						{
							if (stargets.length > 0 || (me._selectionReversedSubtargetId && targets[i].getSubtargets().length == 0))
							{
								finalTargets.push(targets[i]);
							}
						}
						else
						{
							for (var j = 0; j < stargets.length; j++)
							{
								var sstargets = stargets[j].getSubtargets(Ext.bind(this._testTargetLevel2, this), 1);
								
								if (!me._selectionSubsubsubtargetId)
								{
									if (sstargets.length > 0 || (me._selectionReversedSubsubtargetId && stargets[j].getSubtargets().length == 0))
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
		 * Tests if the target of level 0 matches the configured #cfg-selection-target-id
		 * @return true if the target matches
		 */
		_testTargetLevel0: function (target)
		{
			return !this._reversedSelectionTargetId && this._selectionTargetId.test(target.getId())
			|| this._reversedSelectionTargetId && !this._selectionTargetId.test(target.getId());
		},
		
		/**
		 * @private
		 * Tests if the target of level 1 matches the configured #cfg-selection-subtarget-id
		 * @return true if the target matches
		 */
		_testTargetLevel1: function (target)
		{
			return !this._selectionReversedSubtargetId && this._selectionSubtargetId.test(target.getId())
			|| this._selectionReversedSubtargetId && !this._selectionSubtargetId.test(target.getId());
		},
		
		/**
		 * @private
		 * Tests if the target of level 2 matches the configured #cfg-selection-subsubtarget-id
		 * @return true if the target matches
		 */
		_testTargetLevel2: function (target)
		{
			return !this._selectionReversedSubsubtargetId && this._selectionSubsubtargetId.test(target.getId())
			|| this._selectionReversedSubsubtargetId && !this._selectionSubsubtargetId.test(target.getId());
		},
		
		/**
		 * @private
		 * Tests if the target of level 3 matches the configured #cfg-selection-subsubsubtarget-id
		 * @return true if the target matches
		 */
		_testTargetLevel3: function (target)
		{
			return !this._selectionReversedSubsubsubtargetId && this._selectionSubsubsubtargetId.test(target.getId())
			|| this._selectionReversedSubsubsubtargetId && !this._selectionSubsubsubtargetId.test(target.getId());
		},
		
		/**
		 * Get the matching targets in the message
		 * Test if the message if matching upon the #_toolId
		 * @param {Ametys.message.Message} message The message to test
		 * @returns {Ametys.message.MessageTarget[]} The non-null array of matching targets
		 * @private
		 */		
		_getMatchingToolsTarget: function(message)
		{
			var me = this;
			
			if (this._toolId)
			{
				return message.getTargets(
						function (target)
						{
							return !me._toolReveredRole && me._toolId.test(target.getParameters()['id'])
							|| me._toolReveredRole && !me._toolId.test(target.getParameters()['id']);
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
