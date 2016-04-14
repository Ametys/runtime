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
 * This class stands for the logic of a tab of the ribbon that handle show/hide:
 *   * depending of the current selection
 *   * depending of the content type of the selected content
 */
Ext.define(
	"Ametys.ribbon.element.tab.ContentTabController",
	{
		extend: "Ametys.ribbon.element.tab.TabController",
		
		statics: 
		{
			/**
			 * @property {RegExp[]} _handledContentTypes The content types handled by a specific ContentTabController
			 * @private
			 * 
			 */
			_handledContentTypes : [],
			
			/**
			 * Add a content type to the list of content types handled by a specific ContentTabController
			 * @property {RegExp} contentType The content type as regexp
			 */
			registerContentType: function (contentType)
			{
				Ametys.ribbon.element.tab.ContentTabController._handledContentTypes.push(contentType);
			},
			
			/**
			 * Determines if a content type is handled by a specific ContentTabController
			 * @param {String[]} types The content types to test
			 * @return {Boolean} true if the content type is handled by a specific ContentTabController
			 */
			isContentTypeHandled: function (types)
			{
				for (var i=0; i < types.length; i++)
				{
					for (var j=0; j < Ametys.ribbon.element.tab.ContentTabController._handledContentTypes.length; j++)
					{
						if (Ametys.ribbon.element.tab.ContentTabController._handledContentTypes[j].test(types[i]))
						{
							return true;
						}
					}
				}
				
				return false;
			}
		},
		
		/**
		 * @cfg {String} selection-content-type Specify this configuration to obtain a tab that show/hide depending on the content type of the selected content. The string is a regexp that have to match the content type. 
		 */
		
		/**
		 * @property {RegExp} _selectionContentType See #cfg-selection-content-type converted as a regexp.
		 * @private
		 */
		
		constructor: function(config)
		{
			this.callParent(arguments);
			
			var contentType = this.getInitialConfig("selection-content-type") || this.getInitialConfig("content-type"); 
			if (contentType)
			{
				this._selectionContentType = new RegExp(contentType);
				
				// Add content type(s) to the global list of content types handled by specific tab controller
				Ametys.ribbon.element.tab.ContentTabController.registerContentType(this._selectionContentType);
			}
		},
		
		/**
		 * @inheritdoc
		 */
		_testTargetLevel0: function (target)
		{
			var matchSelection = this.callParent(arguments);
			if (!matchSelection)
			{
				return false;
			}

			return this._isSelectionMatchedContentType (target);
		},
		
		/**
		 * @inheritdoc
		 */
		_testTargetLevel1: function (target)
		{
			var matchSelection = this.callParent(arguments);
			if (!matchSelection)
			{
				return false;
			}

			return this._isSelectionMatchedContentType (target);
		},
		
		/**
		 * @inheritdoc
		 */
		_testTargetLevel2: function (target)
		{
			var matchSelection = this.callParent(arguments);
			if (!matchSelection)
			{
				return false;
			}

			return this._isSelectionMatchedContentType (target);
		},
		
		/**
		 * @inheritdoc
		 */
		_testTargetLevel3: function (target)
		{
			var matchSelection = this.callParent(arguments);
			if (!matchSelection)
			{
				return false;
			}

			return this._isSelectionMatchedContentType (target);
		},
		
		/**
		 * @private
		 * Determines if the target matching the selection also matches the content type if configured
		 * @param {Ametys.message.MessageTarget} target The target matching the selection
		 * @return true if the target matches.
		 */
		_isSelectionMatchedContentType: function (target)
		{
			if (this._selectionContentType != null && target.getId() == Ametys.message.MessageTarget.CONTENT)
			{
				var cTypes = target.getParameters().types;
				for (var i=0; i < cTypes.length; i++)
				{
					if (this._selectionContentType.test(cTypes[i]))
					{
						// The content is handled by this ContentTabController
						return true;
					}
				}
				return false;
			}
			
			if (target.getId() == Ametys.message.MessageTarget.CONTENT && Ametys.ribbon.element.tab.ContentTabController.isContentTypeHandled(target.getParameters().types))
			{
				// The content is handled by another ContentTabController
				return false;
			}
			
			// The content has no specific ContentTabController
			return true;
		}
	}
);