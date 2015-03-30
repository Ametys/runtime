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
 * The ribbon is an impl of the Microsoft Fluent Ribbon use in Microsoft Products such as Microsoft Office 2007.
 * This class is the entry point of this package.
 * 
 * 		var ribbon = Ext.create("Ametys.ui.fluent.ribbon.Ribbon", {
 *						title: 'My document',
 *						applicationTitle : 'My application',
 *						items:
 *							[
 *								// Tab Home
 *								{ 
 *									title: 'Home', 
 *									items: [
 *										// Group Test
 *										{
 *											title: 'Test',
 *											largeItems: [...],
 *											items: [
 *	                                   					{
 *                                  						xtype: 'ametys.ribbon-button',
 *		                       								scale: 'large',
 *		                       								enableToggle: true,
 *		                       								pressed: true,
 *		                       								handler: function() { alert('sitemap') },
 *		                       						
 *		                       								icon: 'resources/img/sitemap_32.png',
 *		                       								text: 'Sitemap',
 *		                       								tooltip: {title: 'Sitemap tool', image: 'resources/img/ametys.gif', text: 'Click here to open or close the sitemap tool that allow to manage pages', helpId: 'help.sitemap.id, inribbon: true}
 *                                  					},
 *                                  					{
							 	                        	columns: 2,
							 	                            align: 'middle',
						 	                            	items: [
																	{ xtype: 'ametys.ribbon-button', scale: 'small', enableToggle: true, pressed: true, handler: function() { alert('a') }, icon: 'resources/img/editpaste_16.gif', text: 'Files', tooltip: {...} },
																	{ xtype: 'ametys.ribbon-button', scale: 'small', enableToggle: true, pressed: true, handler: function() { alert('b') }, icon: 'resources/img/editpaste_16.gif', text: 'Search', tooltip: {...} },
																	{ xtype: 'ametys.ribbon-button', colspan: 2, scale: 'small', enableToggle: true, pressed: true, handler: function() { alert('c') }, icon: 'resources/img/editpaste_16.gif', text: 'Profiles', tooltip: {...} }
															]
														}
 *											],
 *											smallItems: [...]
 *										}
 *									],
 *								},
 *								// Contextual tab
 * 								{ 
 * 									title: 'Images', 
 * 									items: [...], 
 * 									contextualTab: 2, 
 * 									contextualLabel: 'Image Tools', 
 * 									contextualGroup: 'image.tools'
 * 								}
 *							],
 *							menu:
 *							{
 *								icon: 'resources/img/ametys.gif',
 * 								tooltip: {title: 'Bouton Ametys', image: 'resources/img/ametys.gif', text: 'Click here to have more general options', helpId: 'help.test.id', inribbon: false),
 * 								items:
 * 									[
 * 										{ text: 'TEST' }
 * 									]
 * 							}
 * 		});
 * 
 * For technical reason this class is just a wrapper for the ribbon Panel. So see the {Ametys.yu.fluent.ribbon.Ribbon} configuration for parameters and usefull methods
 * Use the #getPanel method to access the wrapped object
 */
Ext.define(
	"Ametys.ui.fluent.ribbon.Ribbon",
	{
		extend: "Ext.container.Container",
		alias: 'widget.ametys.ribbon',
		
		/**
		 * @cfg {String} defaultType Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		defaultType: 'ametys.ribbon-tabpanel',
		
        /**
         * @cfg {String} applicationTitle=Ametys The application title that will always be visible in the top part, in addition to the title (separated by a '-'). Can be null. Cannot be changed after configuration. Can contains HTML tags.
         */

		/**
		 * Creates new ribbon. This class is technically just a placeholder for a real Ametys.ui.fluent.ribbon.Panel
		 * @param {Object} config The config object passed to Ametys.ui.fluent.ribbon.Panel
		 * @param {Object} innerConfig The config to this object. May be empty for major use cases, but should contains values such as { region: 'north' }
		 */
		constructor: function(config, innerConfig)
		{
            innerConfig = innerConfig || {};
            innerConfig.items = [ config ];
            innerConfig.cls = "x-fluent-placeholder";
            
            this.callParent([ innerConfig ]);
		},
	
		/**
		 * Get the underlying panel
		 * @return {Ametys.ui.fluent.ribbon.Panel} The wrapper panel
		 */
		getPanel: function()
		{
			return this.floatingItems.items[0];
		},
		
		/**
		 * Set the header additional title
		 * @param {String} title The additional title. Can be null or empty to remove it.
		 */
		setTitle: function(title)
		{
			this.getPanel().setTitle(title);
		}
	}
);
