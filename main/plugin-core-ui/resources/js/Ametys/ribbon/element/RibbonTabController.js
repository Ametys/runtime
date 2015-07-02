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
 * A RibbonTab is a controller for a single tab: for example, show the tab when a content is selected...
 */
Ext.define(
	"Ametys.ribbon.element.RibbonTabController",
	{
		extend: "Ametys.ribbon.RibbonElementController",

		/**
		 * @cfg {Ametys.ui.fluent.ribbon.Panel} tabPanel (required) The associated panel of the ribbon
		 * @auto
		 */
		/**
		 * @property {Ametys.ui.fluent.ribbon.Panel} _tabPanel See {@link #cfg-tabPanel}
		 * @private
		 */
		
		constructor: function(config)
		{
			this.callParent(arguments);
			
			this._tabPanel = config.tabPanel;
		},
        
        /**
         * @inheritDoc
         * @private 
         * The server role for such components 
         * @return {String} The component role
         */
        getServerRole: function()
        {
            return 'org.ametys.runtime.ribbon.RibbonTabsManager';
        },

		/**
		 * Show the associated tab
		 * @param {Boolean} forceSelection True will ensure the tab is selected, null will be agnostic (a last selected algorithme will try to guess) and false will ensure it is not selected.
		 * @protected
		 */
		show: function(forceSelection)
		{
			this._tabPanel.showContextualTab(forceSelection);
		},
		
		/**
		 * Hide the associated tan
		 * @protected
		 */
		hide: function()
		{
			this._tabPanel.hideContextualTab();
		}
		
	}
);
