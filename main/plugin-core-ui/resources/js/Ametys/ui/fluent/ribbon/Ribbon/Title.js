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
 * The title's header for the ribbon panel.
 * Always add the application title to the given title
 * @private
 */
Ext.define(
	"Ametys.ui.fluent.ribbon.TabPanel.Title",
	{
		extend: "Ext.panel.Title",
		alias: 'widget.ametys.ribbon-header-title',
		
		statics: {
			/**
			 * @private
			 * Enhance the title to add a trailing ' - ' if necessary (non empty and not included) 
			 * @param {String} title The title. Can be null or empty
			 * @return {String} The title modified
			 */
			enhanceTitle: function(title)
			{
				if (title && title != "&#160;" && !Ext.String.endsWith(" - "))
				{
					title += " - ";
				}
				return title;
			}
		},
		
		/**
		 * @cfg {String} applicationTitle The application title. Cannot be changed after configuration. Can contains HTML tags.
		 */
		/**
		 * @property {Ext.dom.Element} applicationTitleEl The application title element.
		 * @private
		 * @readonly
		 */
		
        initRenderData: function()
        {
            var renderData = this.callParent(arguments);
            renderData.text = '<span class="x-fluent-tab-panel-header-title">' + this.self.enhanceTitle(renderData.text) + '</span>' 
                                + '<span class="x-fluent-tab-panel-header-title-extension">' + this.applicationTitle + '</span>';
            return renderData;
        },
        
        /**
         * @private
         * Called when the text of the title is being updated
         * @param {String} text The new text to set
         */
        updateText: function(text)
        {
            if (this.rendered)
            {
                this.textEl.child(".x-fluent-tab-panel-header-title").setHtml(this.self.enhanceTitle(text));
            }
        }
	}
);
