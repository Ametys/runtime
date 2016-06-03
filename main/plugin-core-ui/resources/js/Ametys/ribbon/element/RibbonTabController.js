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
 * This abstract class is a ribbon element standing for the logic of a tab of the ribbon.
 * A RibbonTab is a controller for one or multiple tabs: for example, show the tabs when a content is selected...
 */
Ext.define(
	"Ametys.ribbon.element.RibbonTabController",
	{
		extend: "Ametys.ribbon.RibbonElementController",

		/**
		 * @property {Ametys.ui.fluent.ribbon.Panel[]} _tabPanels The panel attached to this controller
		 * @private
		 */
		
		constructor: function(config)
		{
			this.callParent(arguments);
			
			this._tabPanels = [];
		},
		
		/**
		 * @inheritDoc
		 * @private 
		 * The server role for such components 
		 * @return {String} The component role
		 */
		getServerRole: function()
		{
			return 'org.ametys.core.ui.RibbonTabsManager';
		},
		
		/**
		 * Attach a tab to this controller
		 * @param {Ametys.ui.fluent.ribbon.Panel} tabPanel The tab to attach
		 */
		attachTab: function(tabPanel)
		{
		    this._tabPanels.push(tabPanel);
		},

		/**
		 * Show the associated tabs
		 * @param {Boolean} forceSelection True will ensure the tabs are selected, null will be agnostic (a last selected algorithm will try to guess) and false will ensure they are not selected.
		 * @protected
		 */
		show: function(forceSelection)
		{
		    Ext.Array.forEach(this._tabPanels, function(tabPanel) {
		        tabPanel.showContextualTab(forceSelection);
		    });
		},
		
		/**
		 * Hide the associated tan
		 * @protected
		 */
		hide: function()
		{
		    Ext.Array.forEach(this._tabPanels, function(tabPanel) {
		        tabPanel.hideContextualTab();
		    });
		}
		
	}
);
