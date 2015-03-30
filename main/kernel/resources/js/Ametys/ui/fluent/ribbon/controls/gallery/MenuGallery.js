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
 * This class is a menu item to hold a gallery
 */
Ext.define(
	"Ametys.ui.fluent.ribbon.controls.gallery.MenuGallery",
	{
		extend: "Ext.container.Container",
		alias: 'widget.ametys.ribbon-menugallery',
		
		/**
		 * @cfg {Boolean} frame Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */		
		frame: false,
		
		/**
		 * @cfg {Boolean} inribbon Is the menu for a inribbon gallery 
		 * @private
		 */		
		/**
		 * @cfg {String} cls Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */		

		/**
		 * @cfg {Object} defaults Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */		
		defaults: {
			xtype: 'panel',
			border: false,
			frame: false,
			cls: 'x-fluent-menugallerypart',
			defaultType: 'ametys.ribbon-menugallerybutton'
		},

		/**
		 * @property {Boolean} isMenuItem Property of all Ext.menu.Item
		 */
		isMenuItem: true,
		
		/**
		 * @cfg {String} width Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */		
		width: 360,
		
		// FIXME
		onClick: function() {this.up('menu').hide();}, 
		
		constructor: function(config)
		{
			config.cls = config.inribbon ? 'x-fluent-menugallery x-item-inribbon' : 'x-fluent-menugallery';
			
			if (config.inribbon)
			{
				this.defaults.defaults = {inribbon: true};
			}
			
			this.callParent(arguments);
		}
	}		
);
