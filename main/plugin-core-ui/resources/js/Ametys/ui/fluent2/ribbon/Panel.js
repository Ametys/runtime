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
 * This class is a panel for the tab panel. If you just provide a config to the TabPanel, this class will be use.
 * The only point in instancing this class by yourself is to keep the variable.
 * 
 * When a tab is to be contextual, fill the configuration #contextualTab
 */
Ext.define(
	"Ametys.ui.fluent.ribbon.Panel",
	{
		extend: "Ext.panel.Panel",
		alias: 'widget.ametys.ribbon-panel',
		
		/**
		 * @cfg {Boolean} border Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		border: false,
        
        /**
         * @cfg {String} ui=ribbon-panel @inheritdoc
         */
        ui: 'ribbon-panel',
		
		/**
		 * @cfg {Boolean} shadow Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		shadow: false,
		
		/**
		 * @cfg {String} layout Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		layout: 'hbox',
        
        scrollable: true,
		
		/**
		 * @cfg {String} defaultType Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		defaultType: 'ametys.ribbon-group'

		/**
		 * @cfg {Number} contextualTab FOR CONTEXTUAL TAB ONLY. A value between 1 and 6 to choose the color of the contextual tab.
		 */
		/**
		 * @cfg {String} contextualGroup FOR CONTEXTUAL TAB ONLY. A unique identifier for grouping some contextual tabs together. Single tabs must have a group. A unique identifier will be generated if empty. 
		 */
		/**
		 * @cfg {String} contextualLabel FOR CONTEXTUAL TAB ONLY. The label of the group. Different tabs of a given group may define different group label, only the first one will be taken in account.
		 */

		/**
		 * @property {Ametys.ui.fluent.ribbon.Ribbon.ContextualTabGroup} _contextualTabGroup FOR CONTEXTUAL TAB ONLY. The element pointing to the header of the group in the ribbon top part. Will be shared between tabs of the same group.
		 * @private
		 */
		/**
		 * @property {Boolean} _activatedOnce FOR CONTEXTUAL TAB ONLY. Has the contextual tab been displayed at least once? If no, it may be selected when first displayed
		 * @private
		 */
		/**
		 * @property {Boolean} _wasActiveOnHide FOR CONTEXTUAL TAB ONLY. Was the contextual active when it was hidden? If yes, it may be selected when displayed
		 * @private
		 */

		/**
		 * @method showContextualTab
		 * FOR CONTEXTUAL TAB ONLY. This method is a shortcut to Ametys.ui.fluent.ribbon.TabPanel#showContextualTab
		 * @param {Boolean} forceSelection Was the contextual active when it was hidden? If yes, it may be selected when displayed
		 */
		/**
		 * @method hideContextualTab
		 * FOR CONTEXTUAL TAB ONLY. This method is a shortcut to Ametys.ui.fluent.ribbon.TabPanel#hideContextualTab
		 */
	}
);
