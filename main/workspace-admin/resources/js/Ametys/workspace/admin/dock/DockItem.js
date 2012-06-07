/*
 *  Copyright 2012 Anyware Services
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
 * This class handles an item for the left dock bar
 */
Ext.define('Ametys.workspace.admin.dock.DockItem', {
	extend: 'Ext.button.Button',
	
	/**
	 * @cfg {String} plugin The plugin name declaring the item
	 */
	/**
	 * @cfg {Function} actionFunction A function to launch on the item click
	 */
	/**
	 * @cfg {Object} actionParams A object that will be used as argument for the button click
	 */
	
	cls : "dock-item",
	border: false,
	width: '100%',
	enableToggle : false,
	
	handler: function ()
	{
		this.actionFunction(this.plugin, this.actionParams);
	},
	
	setTooltip: function(tip) {
		return this.callParent([{
			text: "<div class='dock-button-tooltip'>" 
				+ "<div class='dock-button-tooltip-img'>"
				+ "<img src='" + tip.image + "'/>"
				+ "</div>"
				+ (tip.title ? "<div class='dock-button-tooltip-title'>" + tip.title + "</div>" : "")
				+ "<div class='dock-button-tooltip-text'>"
				+    tip.text 
				+ "</div>"
				+ "<div class='x-clear'/>"
				+ "</div>",
			dismiss: 0
			}
		]);
	},
	
	statics: {
		/**
		 * @static
		 * Function to redirect to argument named 'Link'
		 * @param {String} plugin The name of the pluggin declaring the item that needs to redirect
		 * @param {Object} A map String,String. A key 'Link' should exists with an url, relative to declaring plugin 
		 */
		redirect: function(plugin, params) {
            function _checkLink (link)
            {
                if (link.substring(0, 1) != '/')
                {
                    link = '/' + link;
                }
                return link;
            }
            
            window.location.href = Ametys.getPluginWrapperPrefix(plugin) + _checkLink(params.Link);
		}
	}
});
