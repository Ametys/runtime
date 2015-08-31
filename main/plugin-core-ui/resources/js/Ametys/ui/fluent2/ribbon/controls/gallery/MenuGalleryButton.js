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
 * This class is a button in a menu gallery
 */
Ext.define(
	"Ametys.ui.fluent.ribbon.controls.gallery.MenuGalleryButton",
	{
		extend: "Ext.button.Button",
		alias: 'widget.ametys.ribbon-menugallerybutton',
        mixins: { button: 'Ametys.ui.fluent.ribbon.controls.RibbonButtonMixin' },
		
        /**
         * @readonly
         * @private
         * @property {String} buttonCls The CSS classname to set on buttons
         */
        galleryButtonCls: 'a-fluent-control-gallerybutton',
        
		/**
		 * @cfg {Boolean} inribbon Is for a inribbon gallery 
		 */		

		/**
		 * @cfg {String} scale Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		scale: 'large',
			
		/**
		 * @cfg {String} iconAlign Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		iconAlign: 'top',

        /**
         * @cfg {String} arrowAlign Doesn't apply to ribbon element. The value HAS TO be the default value.
         * @private
         */
        arrowAlign: 'bottom',
		
        constructor: function(config)
        {
            config = config || {};
            
            this.mixins.button.constructor.call(this, config);
            
            config.cls = Ext.Array.from(config.cls);
            config.cls.push(this.galleryButtonCls);

            
            this.callParent(arguments);
        },
        
        afterRender: function()
        {
            this.callParent(arguments);
            
            this.setText(this.text);
        }        
	}		
);
