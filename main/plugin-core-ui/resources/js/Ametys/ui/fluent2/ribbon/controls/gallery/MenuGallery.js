/*
 *  Copyright 2015 Anyware Services
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
		 * @cfg {Boolean} inribbon Is the menu for a inribbon gallery 
		 */		
        
        /**
         * @readonly
         * @private
         * @property {String} galleryCls The CSS classname on the main menu item
         */
        galleryCls: 'a-fluent-menugallery',

        /**
         * @readonly
         * @private
         * @property {String} inRibbonGalleryCls Additionnal CSS classname on the main menu item if it is inribbon
         */
        inRibbonGalleryCls: 'x-item-inribbon',
        
		/**
		 * @cfg {Object} defaulttype Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */		
		defaultType: 'ametys.ribbon-menugallerypart',

		/**
		 * @property {Boolean} isMenuItem Property of all Ext.menu.Item
		 */
		isMenuItem: true,
		
		// FIXME
		onClick: function() {this.up('menu').hide();}, 
		
		constructor: function(config)
		{
            config = config || {};
            config.cls =  Ext.Array.from(config.cls);
			config.cls.push(this.galleryCls);
            if (config.inribbon)
            {
                config.cls.push(this.inRibbonGalleryCls);
            }
			
			if (config.inribbon)
			{
				config.defaults = { defaults: { inribbon: true } };
			}
			
			this.callParent(arguments);
		}
	}		
);
