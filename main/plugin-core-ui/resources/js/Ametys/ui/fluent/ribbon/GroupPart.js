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
 * This class is a part of a group container. Part are invisible
 */
Ext.define(
	"Ametys.ui.fluent.ribbon.GroupPart",
	{
		extend: "Ext.panel.Panel",
		alias: 'widget.ametys.ribbon-group-part',

		/**
		 * @cfg {String} layout Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
			
		/**
		 * @cfg {String} cls Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */

		/**
		 * @cfg {Number} columns The number of colums for small elements. Default value is 1.
		 */
		
		/**
		 * @cfg {Number} height The number of colums for small elements. Default value is 1.
		 * @private
		 */
		height: 66,

		/**
		 * @cfg {String} align The vertical alignment of buttons. Can be top (3 small buttons) or middle (2 small buttons centered). Default value is top.
		 * @private
		 */
		align: 'top',
		
		defaults: {
			xtype: 'ametys.ribbon-toolbar'
		},

		
		constructor: function(config)
		{
			config.layout = {
					type: 'table',
					columns: config.columns || 1
			}

			if (config.align == 'middle')
			{
				config.cls = 'x-fluent-group-part-middle';
			}
			else
			{
				config.cls = 'x-fluent-group-part-top';
			}
			
			this.callParent(arguments);
		}
	}
);
