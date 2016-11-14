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
 * The title's header for the ribbon panel.
 * This implementation always add the application title to the given title
 * @inheritdoc
 * @private
 */
Ext.define(
	"Ametys.ui.fluent.ribbon.Ribbon.Title",
	{
		extend: "Ext.panel.Title",
		alias: 'widget.ametys.ribbon-title',
		
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
         * @property {String} titleTextCls The CSS classname of the text part of the title
         * @readonly
         * @private
         */
        titleTextCls: 'a-fluent-header-title',        
        /**
         * @property {String} titleExtensionTextCls The CSS classname of the application part of the title
         * @readonly
         * @private
         */
        titleExtensionTextCls: 'a-fluent-header-title-extension',        
		
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
            renderData.text = '<span class="' + this.titleTextCls + '">' + this.self.enhanceTitle(renderData.text) + '</span>' 
                                + '<span class="' + this.titleExtensionTextCls + '">' + this.applicationTitle + '</span>';
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
                this.textEl.child("." + this.titleTextCls).setHtml(this.self.enhanceTitle(text));
            }
        }
	}
);
