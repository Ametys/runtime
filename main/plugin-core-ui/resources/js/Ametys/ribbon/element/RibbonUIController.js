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
 * This abstract class is a ribbon element standing for a control of the ribbon (button, menu gallery...).
 * A RibbonControl is a controller for many ui element (such as Ametys.ui.fluent.ribbon.controls.Button): for example, if the button is in several size or in several tabs...
 */
Ext.define(
	"Ametys.ribbon.element.RibbonUIController",
	{
		extend: "Ametys.ribbon.RibbonElementController",
		
		/**
		 * @cfg {String} dialog-box-launcher A function to call when the bottom tool button of a {@link Ametys.ui.fluent.ribbon.Group} is pressed.
		 * Called with the following parameters:
		 * @cfg {Ametys.ribbon.element.RibbonUIController} dialog-box-launcher.controller This UI controller.
		 */
		
		/**
		 * Creates the ribbon control instance.
		 */
		constructor: function()
		{
			this.callParent(arguments);
			this._uiControls = Ext.create("Ext.util.MixedCollection");
		},
		
		/**
		 * This methods creates a ui for the ribbon and can be called several times.
		 * It is returned and registered. See #getUIControls.
		 * The parameter creation is delegated to the protected methos #createUI
		 * @param {String} size The size required for the control. Can be 'small', 'very-small' or 'large'.
		 * @param {Number} colspan The colspan to set in the element configuration when the element is placed in a Ametys.ui.fluent.ribbon.GroupPart.
		 * @returns {Ametys.ui.fluent.ribbon.controls.Button/Ext.form.field.Field/Ext.Component} A ui that can takes place in the ribbon groups
		 */
		addUI: function(size, colspan)
		{
			var newUIControl = this.createUI(size,colspan);
			
			this.getUIControls().add(newUIControl);
			
			return newUIControl;
		},
	
		/**
		 * This methods creates a ui for the ribbon and can be called several times.
		 * @param {String} size The size required for the control. Can be 'small', 'very-small' or 'large'.
		 * @param {Number} colspan The colspan to set in the element configuration when the element is placed in a Ametys.ui.fluent.ribbon.GroupPart.
		 * @returns {Ametys.ui.fluent.ribbon.controls.Button/Ext.form.field.Field/Ext.Component} A ui that can takes place in the ribbon groups
		 * @template
		 * @protected
		 */
		createUI: function(size, colspan)
		{
			throw new Error("This method is not implemented in " + this.self.getName());
		},
		
		/**
		 * This methods creates a ui for a menu and can be called several times.
		 * It is returned and registered. See #getUIControls.
		 * The parameter creation is delegated to the protected method #createMenuItemUI
		 * @returns {Ext.menu.Item} A ui that can takes place in a menu
		 */
		addMenuItemUI: function()
		{
			var newUIControl = this.createMenuItemUI();
			
			this.getUIControls().add(newUIControl);
			
			return newUIControl;
		},
	
		/**
		 * This methods creates a ui for a menu and can be called several times.
		 * @returns {Ext.menu.Item} A ui that can takes place in a menu
		 * @template
		 * @protected
		 */
		createMenuItemUI: function()
		{
			throw new Error("This method is not implemented in " + this.self.getName());
		},
		
		/**
		 * This methods creates a ui for a menu gallery and can be called several times.
		 * It is returned and registered. See #getUIControls.
		 * The parameter creation is delegated to the protected method #createGalleryItemUI
		 * @returns {Ametys.ui.fluent.ribbon.controls.gallery.MenuGalleryButton} A ui that can takes place in a menu gallery. See {Ametys.ui.fluent.ribbon.controls.gallery.MenuGallery}.
		 */
		addGalleryItemUI: function()
		{
			var newUIControl = this.createGalleryItemUI();
			
			this.getUIControls().add(newUIControl);
			
			return newUIControl;
		},
	
		/**
		 * This methods creates a ui for a menu gallery and can be called several times.
		 * @returns {Ametys.ui.fluent.ribbon.controls.gallery.MenuGalleryButton} A ui that can takes place in a menu gallery
		 * @template
		 * @protected
		 */
		createGalleryItemUI: function()
		{
			throw new Error("This method is not implemented in " + this.self.getName());
		},
		
		/**
		 * Get all the ui controls created by this controller
		 * @returns {Ext.util.MixedCollection} The controls. Cannot be null.
		 */
		getUIControls: function()
		{
			return this._uiControls;
		},
        
        /**
         * @inheritDoc
         * @private
         * The server role for such components 
         * @return {String} The component role
         */
        getServerRole: function()
        {
            return 'org.ametys.core.ui.RibbonControlsManager';
        }
	}
);
