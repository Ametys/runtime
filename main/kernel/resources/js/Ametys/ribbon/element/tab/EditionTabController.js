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
 * This class stands for the logic of a tab of the ribbon for richtext edition that handle show/hide:
 *   * depending of the current selection
 *   * depending of the currently focused tool
 *   * depending on the editor current selection #selection-node-type and #selection-node-attribute
 * Default selection detection is:
 *   * any main target
 *   * with a 'form' subtarget
 *   * with a 'field' subsubtarget
 *   * with a 'node' subsubsubtarget
 */
Ext.define(
	"Ametys.ribbon.element.tab.EditionTabController",
	{
		extend: "Ametys.ribbon.element.tab.TabController",

		/**
		 * @cfg {String} selection-target-type="^.*$" The default value is to accept any target type
		 * @inheritdoc
		 */
		/**
		 * @cfg {String} selection-subtarget-type="^form$" The default value is to accept any target type
		 * @inheritdoc
		 */
		/**
		 * @cfg {String} selection-subsubtarget-type="^field$" The default value is to accept any target type
		 * @inheritdoc
		 */
		/**
		 * @cfg {String} selection-subsubsubtarget-type="^node$" The default value is to accept any target type
		 * @inheritdoc
		 */
		
		/**
		 * @cfg {String} selection-node-type Specify this configuration to obtain a tab that show/hide depending on the type of current HTML node in edition mode. The string is a CSS selector which go from the current selection up to the required element: so it can be the tag name of the HTML element that have to match the current selection. 
		 */
		/**
		 * @cfg {String} selection-node-attribute Specify this configuration to obtain a tab that show/hide depending on the current HTML node attributes. The string is list of regexp separated by ';' that have to match the attributes of current HTML element. A leading '!' will reverse the regexp condition. 
		 */
		
		/**
		 * @property {String} _selectionNodeType See #cfg-selection-node-type.
		 * @private
		 */
		
		/**
		 * @property {String[]} _selectionNodeAttributes See #cfg-selection-node-attribute. The leading '!' is transmitted to {@link #_reversedSelectionNodeAttributes}
		 * @private
		 */
		
		/**
		 * @property {String[]} _reversedSelectionNodeAttributes The leading '!' from {@link #cfg-selection-node-attribute} converted to true.
		 * @private
		 */
		
		constructor: function(config)
		{
			config = Ext.applyIf({
				"selection-target-type": "^.*$",
				"selection-subtarget-type": "^form$",
				"selection-subsubtarget-type": "^field$",
				"selection-subsubsubtarget-type": "^node$"
			}, config);
			
			this.callParent(arguments);
			
			var nodeType = this.getInitialConfig("selection-node-type") || this.getInitialConfig("node-type"); 
			if (nodeType)
			{
				this._selectionNodeType = nodeType;
				this._selectionNodeAttributes = [];
				this._reversedSelectionNodeAttributes = [];
				
				var attributes = this.getInitialConfig('selection-node-attribute') || this.getInitialConfig('attribute');
				
				var attributesAsArray = attributes != null ? attributes.split(";") : [];
				for (var j=0; j < attributesAsArray.length; j++)
				{
					var attribute = attributesAsArray[j];
					
					var i = attribute.indexOf('!');
					if (i == 0)
					{
						this._reversedSelectionNodeAttributes.push(attribute.substring(1));
					}
					else
					{
						this._selectionNodeAttributes.push(attribute);
					}
				}
			}
		},
		
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
				if (noSelection || this._matchingTargets.length == 0 || tinyMCE.activeEditor == null || tinyMCE.activeEditor.dom == null)
				{
					this.hide();
				}
				else
				{
					for (var i=0; i < this._matchingTargets.length; i++)
					{
						var show = true;
						var nodeTarget = this._matchingTargets[i].getSubtarget('node');
						var node = tinyMCE.activeEditor.dom.getParent(nodeTarget.getParameters()['object'], this._selectionNodeType);
						
						while (node != null)
						{
							for (var j=0; j < this._selectionNodeAttributes.length; j++)
							{
								if (node.getAttribute(this._selectionNodeAttributes[j]) == null)
								{
									show = false;
								}
							}
							
							for (var j=0; j < this._reversedSelectionNodeAttributes.length; j++)
							{
								if (node.getAttribute(this._reversedSelectionNodeAttributes[j]) != null)
								{
									show = false;
								}
							}
							
							if (show)
							{
								var shouldForce = node.getAttribute("_mce_ribbon_select") == "1"; 
								this.show(shouldForce ? shouldForce : null);
								
								if (shouldForce)
								{
									// Disarm the force flag, defer it so other tabs will also receive the flag.
								    var removeFlag = Ext.defer(tinyMCE.activeEditor.dom.setAttrib, 1, tinyMCE.activeEditor.dom, [node, "_mce_ribbon_select", '']);
								}
								return;
							}
							
							node = tinyMCE.activeEditor.dom.getParent(node.parentNode, this._selectionNodeType);
						}
					}
					
					this.hide();
				}
			}
		}
	}
);
