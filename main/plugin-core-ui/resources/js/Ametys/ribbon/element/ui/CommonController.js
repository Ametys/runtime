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
 * This class is a common class for ribbon controllers (such as button or field). 
 * Use it as a mixin class in your {@link Ametys.ribbon.element.RibbonUIController} implementation.
 * 
 * - It supports enabling/disabling upon the current selection (see {@link #cfg-selection-target-id}) and associated rights (see {@link #cfg-rights}).
 * - It supports enabling/disabling upon a focused/activated/opened tool (see {@link #cfg-tool-id} and {@link #cfg-tool-enable-on-status})
 */
Ext.define(
	"Ametys.ribbon.element.ui.CommonController",
	{
		/**
		 * @cfg {String} initialize A function to call when the button is being initialized. Use it to add static initializations.
		 * The scope of the function is the parent object that holds the initialize function property.
		 * The function receives the following parameters :
		 * @cfg {Ametys.ribbon.element.ui.ButtonController} initialize.controller This button controller.
		 */

		/**
		 * @cfg {Boolean/String} disabled If 'true' the button is created disabled
		 */
		/**
		 * @property {Boolean} _disabled The current disabled state for a button
		 * @private
		 */

		/**
		 * @cfg {String} label The main text of the button (and the tooltip header)
		 */
		/**
		 * @cfg {String} description The main description text for tooltip
		 */
		/**
		 * @property {String} _description See #cfg-description
		 * @private
		 */
		/**
		 * @cfg {String} help The help id
		 */
		/**
		 * @property {String} _helpId See #cfg-help
		 * @private
		 */
		/**
		 * @cfg {String} icon-glyph The CSS class for glyph to use as the icon. This is an alternative to the set of icons.
		 */
		/**
		 * @property {String} _iconGlyph See #cfg-icon-glyph
		 * @private
		 */
		/**
		 * @cfg {String} icon-decorator The CSS class to use as decorator above the main icon.
		 */
		/**
		 * @property {String} _iconDecorator See #cfg-icon-decorator
		 * @private
		 */
		/**
		 * @cfg {String} icon-small The path to the icon of the button in size 16x16 pixels. Used for button in small size (and tooltip if no bigger image is available).
		 */
		/**
		 * @property {String} _iconSmall See #cfg-icon-small
		 * @private
		 */
		/**
		 * @cfg {String} icon-medium The path to the icon of the button in size 32x32 pixels. Used for button in large size (and tooltip if no bigger image is available).
		 */
		/**
		 * @property {String} _iconMedium See #cfg-icon-medium
		 * @private
		 */
		/**
		 * @cfg {String} icon-large The path to the icon of the button in size 48x48 pixels. Used for button's tooltip.
		 */
		/**
		 * @property {String} _iconLarge See #cfg-icon-large
		 * @private
		 */
		
		/**
		 * @cfg {String} rights The identifier of rights to check on the current selection (require #cfg-selection-target-id): a '|' separated list to consider as OR. The rights will be tested against the parameters "rights" of the target that have to be an String array of right ids.
		 */
		/**
		 * @cfg {String} rights-description-no The description when no rights are ok on the matching selection
		 */
		
        /**
	     * @cfg {Boolean} enabled-on-modifiable-only=false If true the controller will be disabled the current selection is not modifiable. The modifiable status will be tested against the parameters "isModifiable" of the target that have to be an Boolean
	     */
	    /**
	     * @cfg {String} modifiable-description-no The description text used for tooltip when the selection is not modifiable
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
		 * @cfg {String} selection-enable-multiselection=true If 'false' the button will be disabled as soon as the are many elements selected. Works only when #cfg-selection-target-id is specified. See #cfg-selection-description-multiselectionforbidden
		 */
		/**
		 * @cfg {String} selection-target-id Specify this configuration to obtain a button that enable/disable depending on the current selection type. The string is a regexp that have to match the current selection type. A leading '!' will reverse the regexp condition. See #cfg-subtarget-id. See #cfg-selection-description-nomatch
		 */
		/**
		 * @cfg {Object} selection-target-parameter Use this configuration in addition to #cfg-selection-target-id in order to be more specific. This allow to check a target parameter.
		 * @cfg {String} selection-target-parameter.name The name of the parameter to check. The string is a regexp that have to match the current selection type. A leading '!' will reverse the regexp condition. 
		 * @cfg {String} selection-target-parameter.value The value of the parameter to check. The string is a regexp that have to match the current selection type. A leading '!' will reverse the regexp condition. If the parameter is an array, it will check if the value is part of the array (using Ext.Array.contains)
		 */
		/**
		 * @cfg {String} selection-subtarget-id When specified as the same time as #cfg-selection-target-id is, the button will be enable/disabled only if the selection target is matching #cfg-selection-target-id AND if there is a subtarget that matched this regexp. A leading '!' will reverse the regexp condition. See #cfg-subtarget-id.
		 */
		/**
		 * @cfg {Object} selection-subtarget-parameter Same as #cfg-selection-target-parameter but applying to #cfg-selection-subtarget-id
		 */
		/**
		 * @cfg {String} selection-subsubtarget-id Same as #cfg-subtarget-id at a third level.
		 */
		/**
		 * @cfg {Object} selection-subsubtarget-parameter Same as #cfg-selection-target-parameter but applying to #cfg-selection-subsubtarget-id
		 */
		/**
		 * @cfg {String} selection-subsubsubtarget-id Same as #cfg-subtarget-id at a fourth level.
		 */
		/**
		 * @cfg {Object} selection-subsubsubtarget-parameter Same as #cfg-selection-target-parameter but applying to #cfg-selection-subsubsubtarget-id
		 */
			
		/**
		 * @property {Boolean} _selection See #cfg-selection-target-id. True means the button takes care of the selection
		 * @private
		 */
		/**
		 * @property {Boolean} _selectionMatch Boolean indicating if the current selection matches the prerequisite of the controller. Only used if #_selection is true
		 */
		/**
		 * @property {Ametys.message.MessageTarget[]} _selectionMatchingTargets The array of currently selected target matching the desired target type. See {@ link#cfg-selection-target-id}.
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
		 * @property {RegExp} _selectionTargetParameterName See #selection-target-parameter.name converted as a regexp. The leading '!' is transmitted to {@link #_reversedSelectionTargetParameterName}
		 * @private
		 */
		/**
		 * @property {Boolean} _reversedSelectionTargetParameterName The leading '!' from {@link #cfg-selection-target-parameter}.name converted to true.
		 * @private
		 */
		/**
		 * @property {RegExp} _selectionTargetParameterValue See #selection-target-parameter.value converted as a regexp. The leading '!' is transmitted to {@link #_reversedSelectionTargetParameterValue}
		 * @private
		 */
		/**
		 * @property {Boolean} _reversedSelectionTargetParameterValue The leading '!' from {@link #cfg-selection-target-parameter}.value converted to true.
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
		 * @property {RegExp} _selectionSubtargetParameterName See #selection-subtarget-parameter.name converted as a regexp. The leading '!' is transmitted to {@link #_reversedSelectionSubTargetParameterName}
		 * @private
		 */
		/**
		 * @property {Boolean} _reversedSelectionSubTargetParameterName The leading '!' from {@link #cfg-selection-subtarget-parameter}.name converted to true.
		 * @private
		 */
		/**
		 * @property {RegExp} _selectionSubtargetParameterValue See #selection-subtarget-parameter.value converted as a regexp. The leading '!' is transmitted to {@link #_reversedSelectionSubTargetParameterValue}
		 * @private
		 */
		/**
		 * @property {Boolean} _reversedSelectionSubTargetParameterValue The leading '!' from {@link #cfg-selection-subtarget-parameter}.value converted to true.
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
		 * @property {RegExp} _selectionSubsubTargetParameterName See #selection-subsubtarget-parameter.name converted as a regexp. The leading '!' is transmitted to {@link #_reversedSelectionSubsubTargetParameterName}
		 * @private
		 */
		/**
		 * @property {Boolean} _reversedSelectionSubsubTargetParameterName The leading '!' from {@link #cfg-selection-subsubtarget-parameter}.name converted to true.
		 * @private
		 */
		/**
		 * @property {RegExp} _selectionSubsubTargetParameterValue See #selection-subsubtarget-parameter.value converted as a regexp. The leading '!' is transmitted to {@link #_reversedSelectionSubsubTargetParameterValue}
		 * @private
		 */
		/**
		 * @property {Boolean} _reversedSelectionSubsubTargetParameterValue The leading '!' from {@link #cfg-selection-subsubtarget-parameter}.value converted to true.
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
		 * @property {RegExp} _selectionSubsubsubTargetParameterName See #selection-subsubsubtarget-parameter.name converted as a regexp. The leading '!' is transmitted to {@link #_reversedSelectionSubsubsubTargetParameterName}
		 * @private
		 */
		/**
		 * @property {Boolean} _reversedSelectionSubsubsubTargetParameterName The leading '!' from {@link #cfg-selection-subsubsubtarget-parameter}.name converted to true.
		 * @private
		 */
		/**
		 * @property {RegExp} _selectionSubsubsubTargetParameterValue See #selection-subsubsubtarget-parameter.value converted as a regexp. The leading '!' is transmitted to {@link #_reversedSelectionSubsubsubTargetParameterValue}
		 * @private
		 */
		/**
		 * @property {Boolean} _reversedSelectionSubsubsubTargetParameterValue The leading '!' from {@link #cfg-selection-subsubsubtarget-parameter}.value converted to true.
		 * @private
		 */
		/**
		 * @property {Boolean} _targetComputedForLastSelectionMessage internal boolean to stay consistent when computing the selection targets.
		 * @private
		 */
		
		/**
		 * @cfg {String} tool-id When specified, the button will only be enabled if a tool with this role is focused/activated or opened (see {@link #tool-enable-on-status}). This is a regexp. A leading '!' will reverse the regexp condition.
		 */
		/**
		 * @cfg {String} [tool-enable-on-status=focus] Will determine the tool-id mode.
		 * Can take the following value: focus, active, open. Focus by default, which means that the button will be disabled is a matching tool is not focused.
		 */
		/**
		 * @cfg {String} tool-description-inactive When a #cfg-tool-id is specified, this description is used to explain why the button is inactive.
		 */
		
		/**
		 * @cfg {Boolean} tool-enable-on-dirty-state When specified, the button will only be enabled if the dirty state of configured matching tool (see {@link #tool-id}) matches the value
		 */
		/**
		 * @cfg {String} tool-dirty-state-description-nomatch When a {@link #tool-enable-on-dirty-state} is specified, this description is used to explain why the button is inactive.
		 */
		
		/**
		 * @property {Boolean} _toolStatus See #cfg-tool-id. True means the button takes care of the status of some tools.
		 * @private
		 */
		/**
		 * @property {Boolean} _toolStatusMatch Boolean indicating if the current state of the controller matches the prerequisite towards the tool status. Only used if #_toolStatus is true
		 */
		/**
		 * @property {Boolean} _toolDirtyStateMatch Boolean indicating if the current state of the controller matches the prerequisite towards the current tool dirty state. Only used if #_enableOnDirtyState is not null.
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
		 * @private
		 */
		/**
		 * @property {Boolean} _toolActivated When using #cfg-tool-id this boolean reflects the activated state of the associated tool.
		 * @private
		 */
		/**
		 * @property {Boolean} _toolOpened When using #cfg-tool-id this boolean reflects the opened state of the associated tool.
		 * @private
		 */
		/**
		 * @property {Boolean} _enableOnToolActive Indicates that this controllers must react to activate event for the matching tools (see #tool-id and #tool-enable-on-status)
		 * @private
		 */
		/**
		 * @property {Boolean} _enableOnToolOpen Indicates that this controller must react to open event for the matching tools (see #tool-id and #tool-enable-on-status)
		 * @private
		 */
		/**
		 * @property {Boolean} _enableOnToolFocus Indicates that this controller must react to focus event for the matching tools (see #tool-id and #tool-enable-on-status)
		 * @private
		 */
		/**
		 * @property {Boolean} _enableOnDirtyState Indicates the dirty state value of the matching tool for which this controller must be enable (see #tool-id and #tool-enable-on-dirty-state)
		 * @private
		 */
		

		
		/**
		 * Initialize the controller for the initial configuration. This function has to be called in the constructor.
		 * @param {Object} config The initial configuration
		 * @protected
		 */
		_initialize: function (config)
		{
			var initFn = this.getInitialConfig("initialize");
			if (initFn)
			{
				if (this.getLogger().isDebugEnabled())
				{
					this.getLogger().debug("Initializing " + this.getId() + "");
				}
				Ametys.executeFunctionByName(initFn, null, null, this);
			}

			this._description = this.getInitialConfig("description") || this.getInitialConfig("default-description") || '';
			this._helpId = this.getInitialConfig("help") || undefined;
			this._iconGlyph = this.getInitialConfig("icon-glyph");
			this._iconDecorator = this.getInitialConfig("icon-decorator");
			this._iconSmall = this.getInitialConfig("icon-small");
			this._iconMedium = this.getInitialConfig("icon-medium");
			this._iconLarge = this.getInitialConfig("icon-large");
			this._disabled = this.getInitialConfig("disabled") == true || this.getInitialConfig("disabled") == 'true';
			
			var targetId = this.getInitialConfig("selection-target-id") || this.getInitialConfig("target-id"); 
			
			if (targetId)
			{
				this._selection = true;
				
				this._disabled = true;
				this.setAdditionalDescription(this.getInitialConfig("selection-description-empty") || this.getInitialConfig("no-selection-description"));
				
				this._selectionMatch = false;
				this._selectionMatchingTargets = [];
				this._targetComputedForLastSelectionMessage = false;
				
				Ametys.message.MessageBus.on(Ametys.message.Message.SELECTION_CHANGED, this._onSelectionChanged, this);
				
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
				
				// Has an associated target-parameter check?
				var targetParameter = this.getInitialConfig("selection-target-parameter");
				if (targetParameter)
				{
					var i = targetParameter.name.indexOf('!');
					if (i == 0)
					{
						this._selectionTargetParameterName = new RegExp(targetParameter.name.substring(1));
						this._reversedSelectionTargetParameterName = true;
					}
					else
					{
						this._selectionTargetParameterName = new RegExp(targetParameter.name);
						this._reversedSelectionTargetParameterName = false;
					}
					i = targetParameter.value.indexOf('!');
					if (i == 0)
					{
						this._selectionTargetParameterValue = new RegExp(targetParameter.value.substring(1));
						this._reversedSelectionTargetParameterValue = true;
					}
					else
					{
						this._selectionTargetParameterValue = new RegExp(targetParameter.value);
						this._reversedSelectionTargetParameterValue = false;
					}
				}
				
				// Has a sub target?
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
					
					// Has an associated subtarget-parameter check?
					var subtargetParameter = this.getInitialConfig("selection-subtarget-parameter");
					if (subtargetParameter)
					{
						var i = subtargetParameter.name.indexOf('!');
						if (i == 0)
						{
							this._selectionSubtargetParameterName = new RegExp(subtargetParameter.name.substring(1));
							this._reversedSelectionSubTargetParameterName = true;
						}
						else
						{
							this._selectionSubtargetParameterName = new RegExp(subtargetParameter.name);
							this._reversedSelectionSubTargetParameterName = false;
						}
						i = subtargetParameter.value.indexOf('!');
						if (i == 0)
						{
							this._selectionSubtargetParameterValue = new RegExp(subtargetParameter.value.substring(1));
							this._reversedSelectionSubTargetParameterValue = true;
						}
						else
						{
							this._selectionSubtargetParameterValue = new RegExp(subtargetParameter.value);
							this._reversedSelectionSubTargetParameterValue = false;
						}
					}
					 
					// Has a sub sub target?
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
						
						// Has an associated subsubtarget-parameter check?
						var subsubtargetParameter = this.getInitialConfig("selection-subsubtarget-parameter");
						if (subsubtargetParameter)
						{
							var i = subsubtargetParameter.name.indexOf('!');
							if (i == 0)
							{
								this._selectionSubsubTargetParameterName = new RegExp(subsubtargetParameter.name.substring(1));
								this._reversedSelectionSubsubTargetParameterName = true;
							}
							else
							{
								this._selectionSubsubTargetParameterName = new RegExp(subsubtargetParameter.name);
								this._reversedSelectionSubsubTargetParameterName = false;
							}
							i = subsubtargetParameter.value.indexOf('!');
							if (i == 0)
							{
								this._selectionSubsubTargetParameterValue = new RegExp(subsubtargetParameter.value.substring(1));
								this._reversedSelectionSubsubTargetParameterValue = true;
							}
							else
							{
								this._selectionSubsubTargetParameterValue = new RegExp(subsubtargetParameter.value);
								this._reversedSelectionSubsubTargetParameterValue = false;
							}
						}
						
						// Has a sub sub sub target?
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
							
							// Has an associated subsubsubtarget-parameter check?
							var subsubsubtargetParameter = this.getInitialConfig("selection-subsubsubtarget-parameter");
							if (subsubsubtargetParameter)
							{
								var i = subsubsubtargetParameter.name.indexOf('!');
								if (i == 0)
								{
									this._selectionSubsubsubTargetParameterName = new RegExp(subsubsubtargetParameter.name.substring(1));
									this._reversedSelectionSubsubsubTargetParameterName = true;
								}
								else
								{
									this._selectionSubsubsubTargetParameterName = new RegExp(subsubsubtargetParameter.name);
									this._reversedSelectionSubsubsubTargetParameterName = false;
								}
								i = subsubsubtargetParameter.value.indexOf('!');
								if (i == 0)
								{
									this._selectionSubsubsubTargetParameterValue = new RegExp(subsubsubtargetParameter.value.substring(1));
									this._reversedSelectionSubsubsubTargetParameterValue = true;
								}
								else
								{
									this._selectionSubsubsubTargetParameterValue = new RegExp(subsubsubtargetParameter.value);
									this._reversedSelectionSubsubsubTargetParameterValue = false;
								}
							}							
						}
					}
				}
			}
			
			var toolId = this.getInitialConfig("tool-id");
			if (toolId)
			{	
				this._toolStatus = true;
				
				this._disabled = true;
				this.setAdditionalDescription(this.getInitialConfig("tool-description-inactive"));
				
				this._toolStatusMatch = false;
				this._enableOnDirtyState = null;
				this._toolDirtyStateMatch = null;
				this._toolOpened = false;
				this._toolActivated = false;
				this._toolFocused = false;
				
				switch(this.getInitialConfig('tool-enable-on-status'))
				{
					case 'active':
						this._enableOnToolActive = true;
						Ametys.message.MessageBus.on(Ametys.message.Message.TOOL_ACTIVATED, this._onAnyToolStatusChange, this);
						Ametys.message.MessageBus.on(Ametys.message.Message.TOOL_DEACTIVATED, this._onAnyToolStatusChange, this);
						break;
					case 'open':
						this._enableOnToolOpen = true;
						Ametys.message.MessageBus.on(Ametys.message.Message.TOOL_OPENED, this._onAnyToolStatusChange, this);
						Ametys.message.MessageBus.on(Ametys.message.Message.TOOL_CLOSED, this._onAnyToolStatusChange, this);
						break;
					default:
						this._enableOnToolFocus = true;
						Ametys.message.MessageBus.on(Ametys.message.Message.TOOL_FOCUSED, this._onAnyToolStatusChange, this);
						Ametys.message.MessageBus.on(Ametys.message.Message.TOOL_BLURRED, this._onAnyToolStatusChange, this);
				}
				
				if (this.getInitialConfig('tool-enable-on-dirty-state'))
				{
					this._enableOnDirtyState = this.getInitialConfig('tool-enable-on-dirty-state') == "true";
					// At the opening of the tool, the tool is not dirty (dirty state is equals to false)
					this._lastDirtyStateUpdate = false;
					Ametys.message.MessageBus.on(Ametys.message.Message.TOOL_DIRTY_STATE_CHANGED, this._onDirtyStateChange, this);
				}
				
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
		 * Get the tooltip configuration
		 * @param {Boolean} [inribbon=true] The inribbon config of the tooltip.
		 * @returns {Object} The tooltip configuration. See Ametys.ui.fluent.Tooltip.
		 * @protected
		 */
		_getTooltip: function(inribbon)
		{
			var icon = this._iconLarge || this._iconMedium || this._iconSmall;
			return {
				title: this.getInitialConfig("label"),
				glyphIcon: this._iconGlyph,
				iconDecorator: this._iconDecorator,
				image: !this._iconGlyph && icon ? Ametys.CONTEXT_PATH + icon : null,
				imageWidth: this._iconGlyph || this._iconLarge ? 48 : (this._iconMedium ? 32 : 16),
				imageHeight: this._iconGlyph || this._iconLarge ? 48 : (this._iconMedium ? 32 : 16),
				text: this._description + (this._additionalDescription ? ('<br/><br/>' + this._additionalDescription) : ''),
				helpId: this.getInitialConfig("help") || undefined,
				inribbon: inribbon == null ? true : inribbon
			};
		},
		
		/**
		 * Set the description
		 * @param {String} description The new description. See #cfg-description. Can be null to keep the current value.
		 * @param {String} helpId The new help identifier. See #cfg-help. Can be null to keep the current value.
		 * @protected
		 */
		setDescription: function(description, helpId)
		{
			this._description = description != null ? description : this._description;
			this._helpId = helpId != null ? helpId : this._helpId;
			
			this._updateUI();
		},
		
		/**
		 * This function is called when title, description or icons of the controller are set. This implementation does nothing. 
		 * Override this function to update the ui controls for images and tooltip for example.
		 * @template 
		 * @protected
		 */
		_updateUI: function ()
		{
			// Nothing
		},
		
		/**
		 * Set a temporary additional description
		 * @param {String} additionalDescription The new description. See #cfg-description.
		 */
		setAdditionalDescription: function(additionalDescription)
		{
			this._additionalDescription = additionalDescription;
			
			this._updateUI();
		},
		
		/**
		 * Set the icons.
		 * @param {String} small The path to the new small icon. See #cfg-icon-small. Cannot be null.
		 * @param {String} medium The path to the new medium icon. See #cfg-icon-medium. Cannot be null.
		 * @param {String} large The path to the new large icon. See #cfg-icon-large. Can be null.
		 */
		setIcons: function(small, medium, large)
		{
			this._iconSmall = small;
			this._iconMedium = medium;
			this._iconLarge = large;
			
			this._updateUI();
		},
		
		/**
		 * Set the glyph class for main icon
		 * @param {String} glyph The CSS class of glyph
		 */
		setGlyphIcon: function(glyphIcon)
		{
			this._iconGlyph = glyphIcon;
			this._updateUI();
		},
		
		/**
		 * Set the glyph class for decorator
		 * @param {String} decorator The CSS class of decorator
		 */
		setIconDecorator: function(decorator)
		{
			this._iconDecorator = decorator;
			this._updateUI();
		},
		
		/**
		 * Enables all controlled ui
		 */
		enable: function()
		{
			if (this._disabled)
			{
				this._disabled = false;
				
				this.getUIControls().each(function (controller) {
					controller.enable();
				});
			}
		},
		
		/**
		 * Disables all controlled ui
		 */
		disable: function()
		{
			if (!this._disabled)
			{
				this._disabled = true;
				
				this.getUIControls().each(function (controller) {
					controller.disable();
				});
			}
		},
		
		/**
		 * Sets the disabled state of all controlled ui
		 * @param disabled True to disable the controlled, false to enable it
		 */
		setDisabled : function (disabled)
		{
			disabled ? this.disable() : this.enable();
		},
		
		/**
		 * Get the matching selection targets of the current selection.
		 * Always empty if {@ link#cfg-selection-target-id} is not specified.
		 */
		getMatchingTargets: function()
		{
			return this._selectionMatchingTargets || [];
		},
		
		/**
		 * Get the matching targets in the message
		 * Test if the message if matching upon the #_selectionTargetId, #_selectionSubtargetId, #_selectionSubsubtargetId and #_selectionSubsubsubtargetId.
		 * It also checks for #_selectionTargetParameter, #_selectionSubtargetParameter, #_selectionSubsubTargetParameter and #_selectionSubsubsubTargetParameter
		 * @return {Ametys.message.MessageTarget[]} the matching targets 
		 * @private
		 */		
		_getMatchingSelectionTargets: function(message)
		{
			// Avoid recomputation if the current selection has not changed.
			if (this._targetComputedForLastSelectionMessage)
			{
				return this._selectionMatchingTargets;
			}

			this._targetComputedForLastSelectionMessage = true;
			
			var me = this;
			
			var finalTargets = [];
			if (this._selection)
			{
				var targets = this._getMatchingSubtargets(message, 0);
				
				if (!me._selectionSubtargetId)
				{
					finalTargets = targets;
				}
				else
				{
					for (var i = 0; i < targets.length; i++)
					{
						var stargets = this._getMatchingSubtargets(targets[i], 1); 
							
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
								var sstargets = this._getMatchingSubtargets(stargets[j], 2); 
								
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
										var ssstargets = this._getMatchingSubtargets(sstargets[k], 3);
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
							return !me._reversedSelectionTargetId && me._toolId.test(target.getParameters()['id'])
							|| me._reversedSelectionTargetId && !me._toolId.test(target.getParameters()['id']);
						}
				);
			}
			else
			{
				return [];
			}
		},
		
		/**
		 * Listener when a tool has changed (from the point of view of the tool manager, ie focus/activation...). Registered only if #cfg-tool-id is specified. Will enable the buttons effectively.
		 * @param {Ametys.message.Message} message The tool message
		 * @private
		 */
		_onAnyToolStatusChange: function(message)
		{
			if (this._getMatchingToolsTarget(message).length > 0)
			{
				var type = message.getType();
				
				switch(message.getType())
				{
					case Ametys.message.Message.TOOL_FOCUSED:
					case Ametys.message.Message.TOOL_BLURRED:
						this._toolFocused = type == Ametys.message.Message.TOOL_FOCUSED;
						break;
					case Ametys.message.Message.TOOL_ACTIVATED:
					case Ametys.message.Message.TOOL_DEACTIVATED:
						this._toolActivated = type == Ametys.message.Message.TOOL_ACTIVATED;
						break;
					case Ametys.message.Message.TOOL_OPENED:
					case Ametys.message.Message.TOOL_CLOSED:
						this._toolOpened = type == Ametys.message.Message.TOOL_OPENED;
						break;
					default:
						this.getLogger().error("Unexpected message type : " + type + ".");
				}
				
				this.refresh(false);
			}
		},
		
		/**
		 * Listener when the dirty state of a tool has changed. Registered only if #cfg-tool-id and #cfg-tool-enable-on-dirty-state is specified. Will enable the buttons effectively.
		 * @param {Ametys.message.Message} message The tool message
		 * @private
		 */
		_onDirtyStateChange: function (message)
		{
			if (this._getMatchingToolsTarget(message).length > 0)
			{
				this._lastDirtyStateUpdate = message.getParameters().dirty;
				this.refresh(false);
			}
		},

		/**
		 * Listener when the selection has changed. Registered only if #cfg-selection-target-id is specified. 
		 * Will enable or disable the buttons effectively upon the current selection.
		 * @param {Ametys.message.Message} message The selection message.
		 * @private
		 */
		_onSelectionChanged: function(message)
		{
			this._targetComputedForLastSelectionMessage = false;
			this.refresh(false);
		},
		
		/**
		 * Refresh the controller state and update its status.
		 * Take care of the current selection, and the tool status (focused / activated / opened)
		 * Will enable or disable the buttons effectively
		 * @param {Boolean} [force=true] Do not call the refresh method with this parameter, only reserved for internal use.
		 * If explicitely set to false, the update state method will be called only if changes were detected in the refresh method.
		 * @protected
		 */
		refresh: function(force)
		{
			var toolStatusChanged = this._testToolStatusChanges();
			if (this._toolStatusMatch === false)
			{
				return;
			}
			
			var toolDirtyStateChanged = this._testToolDirtyStateChanges();
			if (this._toolDirtyStateMatch === false)
			{
				return;
			}
			
			var selectionChanged = this._testSelectionChanges();
			if (this._selectionMatch === false)
			{
				return;
			}
			
			var forcedUpdate = force !== false; // force update except if 'force' is explicitly set to false 
			if (forcedUpdate || toolStatusChanged || toolDirtyStateChanged || selectionChanged)
			{
				this.updateState(selectionChanged, toolStatusChanged, toolDirtyStateChanged);
			}
		},
		
		/**
		 * Detect if the tool status has changed for this controller and update the #_toolStatusMatch internal variable
		 * A change is detected if toolStatusMatch value is changed.
		 * If the tool status does not match anymore, the controller will be disabled with a configured additional description.
		 * @return {Boolean} true if changes have been detected.
		 * @private
		 */
		_testToolStatusChanges: function()
		{
			if (this._toolStatus !== true)
			{
				return false;
			}
			
			var currentMatch = this._toolStatusMatch;
			
			if ((this._enableOnToolFocus && this._toolFocused === false)
					|| (this._enableOnToolActive && this._toolActivated === false)
					|| (this._enableOnToolOpen && this._toolOpened === false))
			{
				this._toolStatusMatch = false;
			}
			else
			{
				this._toolStatusMatch = true;
			}
			
			var hasChanged = currentMatch !== this._toolStatusMatch;
			
			if (hasChanged && this._toolStatusMatch === false)
			{
				this.disable();
				this.setAdditionalDescription(this.getInitialConfig("tool-description-inactive"));
			}
			
			return hasChanged;
		},
		
		/**
		 * Detect if the tool dirty state has changed for this controller and update the #_toolDirtyStateMatch internal variable
		 * If the tool dirty state does not match anymore, the controller will be disabled with a configured additional description.
		 * @return {Boolean} true if changes have been detected.
		 * @private
		 */
		_testToolDirtyStateChanges: function ()
		{
			if (this._enableOnDirtyState == null)
			{
				return false;
			}
			
			var currentMatch = this._toolDirtyStateMatch;
			
			this._toolDirtyStateMatch = this._enableOnDirtyState == this._lastDirtyStateUpdate;
			
			var hasChanged = currentMatch !== this._toolDirtyStateMatch;
			
			if (hasChanged && this._toolDirtyStateMatch === false)
			{
				this.disable();
				this.setAdditionalDescription(this.getInitialConfig("tool-dirty-state-description-nomatch"));
			}
			
			return hasChanged;
		},
		
		/**
		 * Force refreshing if the current selection targets match the targets from message.
		 * The matching targets are updated before the refresh.
		 * @param {Ametys.message.Message} message the message
		 */
		refreshIfMatchingMessage: function (message)
		{
			if (this.updateTargetsInCurrentSelectionTargets (message))
			{
				this.refresh(true);
			}
		},
		
		/**
		 * @protected
		 * Update the current selection targets with the matching targets from message.
		 * If the new selection does not match, the controller will be disabled with a configured additional description.
		 * Use with caution: this method can modified the subtargets of the current selection matching targets. 
		 * @param {Ametys.message.Message} message the message
		 * @return {Boolean} true if at least one target has been updated
		 */
		updateTargetsInCurrentSelectionTargets: function (message)
		{
			var found = false;
			
			this._targetComputedForLastSelectionMessage = false;
			var targets = this._getMatchingSelectionTargets(message);
			
			var selectionTargets = this._selectionMatchingTargets;
			if (Ext.isEmpty(selectionTargets))
			{
				selectionTargets = Ametys.message.MessageBus.getCurrentSelectionMessage().getTargets();
			}
			
			for (var i=0; selectionTargets != null && i < selectionTargets.length; i++)
			{
				for (var j=0; j < targets.length; j++)
				{
					if (this.areSameTargets(selectionTargets[i], targets[j]))
					{
						selectionTargets[i] = targets[j];
						found = true;
					}
				}
			}
		
			if (found)
			{
				this._updateMatchingSelectionTargets (selectionTargets);
			}
			
			return found;
		},
		
		/**
		 * Test the current selection and update the #_selectionMatch internal variable
		 * If the selection does not match, the controller will be disabled with a configured additional description.
		 * @return {Boolean} true if the selection have changed from the point of view of the controller.
		 * @private
		 */
		_testSelectionChanges: function()
		{
			if (this._selection !== true)
			{
				return false;
			}
			
			var message = Ametys.message.MessageBus.getCurrentSelectionMessage();
			var targets = this._getMatchingSelectionTargets(message);
				
			if (!this._isCurrentSelectionChanged(targets))
			{
                if (targets.length == 0)
                {
                    // when noselection -> nomatch, we also need to update the additional description
                    this._updateMatchingSelectionTargets(targets); 
                }
				this._updateCurrentSelectionMatchingTargets(targets);
				return false;
			}
			
			this._updateMatchingSelectionTargets(targets);
			return true;
		},
		
		/**
		 * Update the matching selection target from the given targets
		 * If the selection does not match, the controller will be disabled with a configured additional description.
		 * @private
         * @param {Ametys.mesage.MessageTarget[]} targets the matching targets
		 */
		_updateMatchingSelectionTargets: function (targets)
		{
			var match = false;
			
			var noSelection = Ametys.message.MessageBus.getCurrentSelectionMessage().getTargets().length == 0;
			var multiSelectionEnabled = (this.getInitialConfig("selection-enable-multiselection") || this.getInitialConfig("enable-multiselection")) != 'false';

			if (noSelection)
			{
				// noselection
				this.setAdditionalDescription(this.getInitialConfig("selection-description-empty") || this.getInitialConfig("no-selection-description"));
			}
			else if (targets.length == 0)
			{
				// nomatch
				this.setAdditionalDescription(this.getInitialConfig("selection-description-nomatch"));
			}
			else if (targets.length != 1 && !multiSelectionEnabled)
			{
				// selectionsize
				this.setAdditionalDescription(this.getInitialConfig("selection-description-multiselectionforbidden") || this.getInitialConfig("multiselection-disabled-description"));
			}
			else if (!this.hasRightOnAny(targets))
			{
				// noright
				this.setAdditionalDescription(this.getInitialConfig("rights-description-no") || this.getInitialConfig("no-right-description"));
			}
            else if (!this.isNoModifiable(targets))
            {
                // no modifiable
                this.setAdditionalDescription(this.getInitialConfig("modifiable-description-no") || this.getInitialConfig("no-modifiable-description"));
            }
			else 
			{
				var additionnalDescription = this.additionalErrorDescriptionOnSelectionChanged(targets);
				if (additionnalDescription != null)
				{
					this.setAdditionalDescription(additionnalDescription);
				}
				else
				{
					this.setAdditionalDescription("");
					match = true;
				}
			}
			
			if (!match)
			{
				this._selectionMatchingTargets = [];
				this.disable();
			}
			else
			{
				this._updateCurrentSelectionMatchingTargets(targets);
				this.enable();
			}
			
			this._selectionMatch = this._selectionMatchingTargets.length > 0;
		},
		
		/**
		 * Update the current matching selection targets with the given targets
		 * @param {Ametys.message.MessageTarget[]} targets the targets
		 * @private
		 */
		_updateCurrentSelectionMatchingTargets: function (targets)
		{
			this._selectionMatchingTargets = targets;
		},
		
		/**
		 * Implement this method to add special error description when the selection changed.
		 * This method is call by #_testSelectionChanges if all basic tests were successful
		 * @param {Ametys.message.MessageTarget} targets The new matching selection targets.
		 * @return {String} The addition message to disable the controller (can be empty) or null to enable the controller.
		 * @template
		 * @protected
		 */
		additionalErrorDescriptionOnSelectionChanged: function(targets)
		{
			return null;
		},
		
		/**
		 * Determine if the current selection has changed from the point of view of controller
		 * @param {Ametys.message.MessageTarget[]} targets The targets to be test
		 * @returns true if targets does not match the registered selection
		 * @private
		 */
		_isCurrentSelectionChanged: function (targets)
		{
			if (this._selectionMatchingTargets == null || targets == null || this._selectionMatchingTargets.length != targets.length)
			{
				return true;
			}
			
			if (this._selectionMatchingTargets === targets)
			{
				return false;
			}
			
			if (this._areSameTargetsArray (this._selectionMatchingTargets, targets, 0))
			{
				return false;
			}
			
			return true;
		},
		
		/**
		 * Determine if the two table of targets are the same from the point of view of controller
		 * @param {Ametys.message.MessageTarget[]} targets1 The first table of targets
		 * @param {Ametys.message.MessageTarget[]} targets2 The second table of targets
		 * @param {Number} level The level of targets
		 * @return true if the targets are the same
         * @private
		 */
		_areSameTargetsArray: function (targets1, targets2, level)
		{
			if (targets1.length != targets2.length)
			{
				return false;
			}
			else
			{
				for (var i=0; i < targets1.length; i++)
				{
					var found = false;
					var sameTarget = null;
					for (var j=0; j < targets2.length; j++)
					{
						if (this._areSameTargets(targets1[i], targets2[j], level))
						{
							sameTarget = targets2[j];
							found = true;
							break;
						}
					}

					if (!found)
					{
						return false;
					}
					
					if (this._isSubtargetsComparisonNeeded(level))
					{
						// Compare the subtargets recursively
						var stargets1 = this._getMatchingSubtargets(targets1[i], level + 1);
						var stargets2 = this._getMatchingSubtargets(sameTarget, level + 1);
						
						found = this._areSameTargetsArray (stargets1, stargets2, level + 1);
					}
					
					if (!found)
					{
						return false;
					}
					
					// else continue comparison on other tab elements
				}
				
				return true;
			}
		},
		
		/**
		 * @private
		 * Determines if a comparison on subtargets is needed
		 * @param {Number} level The level of parent target
		 * @return true if a comparison on subtargets is needed
		 */
		_isSubtargetsComparisonNeeded: function (level)
		{
			switch (level) 
			{
				case 0:
					return this._selectionSubtargetId;
				case 1:
					return this._selectionSubsubtargetId;
				case 2:
					return this._selectionSubsubsubtargetId;
				default:
					return false;
			}
		},
		
		/**
		 * @private
		 * Compares two targets of any level and returns true if the two targets are equals.
		 * See #areSameTarget, #areSameSubTarget, #areSameSubsubTarget and #areSameSubsubsubTarget
		 * @param {Ametys.message.MessageTarget} target1 The first target
		 * @param {Ametys.message.MessageTarget} target2 The second target
		 * @param {Number} level The level of targets to compare. 0 for main target, 1 for subtargets, 2 for subsubtargets, ...
		 * @return true if the two targets are the same.
		 */
		_areSameTargets: function (target1, target2, level)
		{
			switch (level) 
			{
				case 0:
					return this.areSameTargets(target1, target2);
				case 1:
					return this.areSameSubtargets(target1, target2);
				case 2:
					return this.areSameSubsubtargets(target1, target2);
				case 3:
					return this.areSameSubsubsubtargets(target1, target2);
				default:
					return true;
			}
		},
		
		/**
		 * Get the direct subtargets of the given target, matching the controller selection
		 * @param {Ametys.message.Message/Ametys.message.MessageTarget} target A message (level 0) or a target (level > 0)
		 * @param {Number} level The level of the subtarget: 0 for message target, 1 for subtarget, 2 for subsubtarget, 3 for subsubsubtarget
		 * @private
		 */
		_getMatchingSubtargets: function (target, level)
		{
			switch (level) {
				case 0:
					return target.getTargets(Ext.bind(this._testTarget, this, [this._selectionTargetId, this._reversedSelectionTargetId, this._selectionTargetParameterName, this._reversedSelectionTargetParameterName, this._selectionTargetParameterValue, this._reversedSelectionTargetParameterValue], true));
					break;
					
				case 1:
					return target.getSubtargets(Ext.bind(this._testTarget, this, [this._selectionSubtargetId, this._selectionReversedSubtargetId, this._selectionSubtargetParameterName, this._reversedSelectionSubTargetParameterName, this._selectionSubtargetParameterValue, this._reversedSelectionSubTargetParameterValue], true), 1);
					break;
					
				case 2:
					return target.getSubtargets(Ext.bind(this._testTarget, this, [this._selectionSubsubtargetId, this._selectionReversedSubsubtargetId, this._selectionSubsubTargetParameterName, this._reversedSelectionSubsubTargetParameterName, this._selectionSubsubTargetParameterValue, this._reversedSelectionSubsubTargetParameterValue], true), 1);
					break;
					
				case 3:
					return target.getSubtargets(Ext.bind(this._testTarget, this, [this._selectionSubsubsubtargetId, this._selectionReversedSubsubsubtargetId, this._selectionSubsubsubTargetParameterName, this._reversedSelectionSubsubsubTargetParameterName, this._selectionSubsubsubTargetParameterValue, this._reversedSelectionSubsubsubTargetParameterValue], true), 1);
					break;
					
				default:
					return [];
					break;
			}
		},
		
		/**
		 * @private
		 * Tests if the target of level 0 matches the configured #cfg-selection-target-id
		 * @param {Ametys.message.MessageTarget} target The target to test
		 * @param {RegExp} selectionTypeRegexp The regular expression to pass on targetId
		 * @param {Boolean} selectionTypeReverseRegexp True is the preceding regular expression test shoul be reversed
		 * @param {RegExp} selectionParameterNameRegexp If non-empty, this will test all target's parameters name and a least one must match, with a correct value (see following parameters)
		 * @param {Boolean} selectionParameterNameReverseRegexp True is the preceding regular expression test shoul be reversed
		 * @param {RegExp} selectionParameterValueRegexp Used when a parameter name is matching to test its value.
		 * @param {Boolean} selectionParameterValueReverseRegexp True is the preceding regular expression test shoul be reversed
		 * @return true if the target matches
		 */
		_testTarget: function (target, selectionTypeRegexp, selectionTypeReverseRegexp, selectionParameterNameRegexp, selectionParameterNameReverseRegexp, selectionParameterValueRegexp, selectionParameterValueReverseRegexp)
		{
			function checkParameters()
			{
				if (!selectionParameterNameRegexp)
				{
					return true;
				}
				
				function checkValue(value)
				{
					if (Ext.isArray(value))
					{
						var gotOne = false;
						Ext.each(value, function(v, index, array) {
							if (checkValue(v))
							{
								gotOne = true;
								return false; // stop the iteration
							}
						});
						return gotOne;
					}
					else
					{
						return ((!selectionParameterValueReverseRegexp && selectionParameterValueRegexp.test(value)
								|| selectionParameterValueReverseRegexp && !selectionParameterValueRegexp.test(value)));
					}
				}
				
				var gotOne = false;
				Ext.Object.each(target.getParameters(), function(key, value, parameters) {
					if ((!selectionParameterNameReverseRegexp && selectionParameterNameRegexp.test(key)
							|| selectionParameterNameReverseRegexp && !selectionParameterNameRegexp.test(key))
						&& value != null && checkValue(value))
					{
						gotOne = true;
						return false; // stop the iteration
					}
				});
				return gotOne;
			}
			
			return (!selectionTypeReverseRegexp && selectionTypeRegexp.test(target.getId())
						|| selectionTypeReverseRegexp && !selectionTypeRegexp.test(target.getId()))
					&& checkParameters();
		},
		
		/**
		 * @protected
		 * @template
		 * Compares two targets and returns true if the two targets are equals. The default implementation compares the parameters "id" of the targets.
		 * @return true if the two targets are the same.
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
		 * @protected
		 * @template
		 * Compares two subtargets and returns true if the two subtargets are equals. The default implementation compares the parameters "id" of the subtargets.
		 * @return true if the two subtargets are the same.
		 */
		areSameSubtargets: function (starget1, starget2)
		{
			if (starget1.getParameters().id && starget2.getParameters().id)
			{
				return starget1.getParameters().id == starget2.getParameters().id;
			}
			return false;
		},
		
		/**
		 * @protected
		 * @template
		 * Compares two subsubtargets and returns true if the two subsubtargets are equals. The default implementation compares the parameters "id" of the subsubtargets.
		 * @return true if the two subsubtargets are the same.
		 */
		areSameSubsubtargets: function (sstarget1, sstarget2)
		{
			if (sstarget1.getParameters().id && sstarget2.getParameters().id)
			{
				return sstarget1.getParameters().id == sstarget2.getParameters().id;
			}
			return false;
		},
		
		/**
		 * @protected
		 * @template
		 * Compares two subsubsubtargets and returns true if the two subsubsubtargets are equals. The default implementation compares the parameters "id" of the subsubsubtargets.
		 * @return true if the two subsubsubtargets are the same.
		 */
		areSameSubsubsubtargets: function (ssstarget1, ssstarget2)
		{
			if (ssstarget1.getParameters().id && ssstarget2.getParameters().id)
			{
				return ssstarget1.getParameters().id == ssstarget2.getParameters().id;
			}
			return false;
		},
		
		/**
		 * Checks if the rights on at least one target is ok for at least one right required for the button.
		 * @param {Ametys.message.MessageTarget[]} targets The message targets to check
		 * @protected
		 */
		hasRightOnAny: function(targets)
		{
			var rightToCheck = this.getInitialConfig("rights");
			
			if (!rightToCheck)
			{
				// No right is needed: ok.
				return true;
			}
			
			var neededRights = rightToCheck.split('|');
			var rights = [];
			
			Ext.Array.forEach(targets, function(target) {
				var targetRights = target.getParameters().rights || [];
				rights = rights.concat(targetRights, rights);
			});
			
			for (var i=0; i < rights.length; i++)
			{
				if (Ext.Array.contains(neededRights, rights[i]))
				{
					return true;
				}
			}
			
			// No right found
			return false;
		},
		
        /**
         * Checks if at least one target is ok for the modifiable status required for the button.
         * @param {Ametys.message.MessageTarget[]} targets The message targets to check
         * @protected
         */
        isNoModifiable: function (targets)
        {
            var enabledOnModifiableOnly = String(this.getInitialConfig("enabled-on-modifiable-only") || 'false') == 'true';
            
            if (!enabledOnModifiableOnly)
            {
                // ok
                return true;
            }
            
            for (var i=0; i < targets.length; i++)
            {
                if (targets[i].getParameters().isModifiable)
                {
                    // at least one modifiable target
                    return true;
                }
            }
            
            return false;
        },
        
		/**
		 * This function is called when the graphical state and information of the controller must be updated in order to enable it.
		 * This means that at this point, all prerequisite given the current selection and the tool status are valid.
		 * This is the place to add additional treatments and/or to make a server request (with the server call mechanism).
		 * By default it only enable the controller and reset its additional description.
		 * @param {Boolean} selectionChanged true if this method has been invoked because the selection has changed
		 * @param {Boolean} toolStatusChanged true if this method has been invoked because the tool status has changed
		 * @param {Boolean} selectionChanged true if this method has been invoked because the tool dirty state has changed
		 * @template
		 * @protected
		 */
		updateState: function(selectionChanged, toolStatusChanged, toolDirtyStateChanged)
		{
			this.setAdditionalDescription('');
			this.enable();
		}
	}
);
