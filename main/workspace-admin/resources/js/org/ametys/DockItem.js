/*
 *  Copyright 2009 Anyware Services
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

Ext.namespace('org.ametys');

/**
 * org.ametys.DockItem
 *
 * @class This class handles an item for the dock
 * @extends Ext.Button
 * @constructor
 * @param {Object} config Configuration options
 * @example
 * var tooltip = org.ametys.AdminTools.DockTooltipFormater('Configuration', 
 * 	'/config/icon_large.png', 
 * 	'Réglage des paramètres de configuration');
 * 
 * var item = new org.ametys.DockItem ({
 *	tooltip: tooltip,
 *	icon : getPluginResourcesUrl('core') + '/img/config/icon_dock.png',
 *	pressed: false,
 *	actionFunction: Runtime_InteractionActionLibrary_Link.act,
 *	actionParams : {'Link' : 'administrator/config/edit.html', 'Mode' : 'plugin-wrapped'}
 * });
 */
org.ametys.DockItem = function(config) 
{
	org.ametys.DockItem.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.DockItem, Ext.Button, 
{
	cls : "dock-item",
	border: false,
	width: '100%',
	enableToggle : false,
	template : new Ext.Template(
            '<div class="{3}">',
            '<button class="dock-item-button" type="{1}">{0}</button>',
            '</div>')
});
	
org.ametys.DockItem.prototype.handler = function ()
{
	this.actionFunction(this.plugin, this.actionParams);
}


org.ametys.DockItem.prototype.onRender = function(ct, position)
{
	org.ametys.DockItem.superclass.onRender.call(this, ct, position);
	if (this.desc)
	{
		var tooltip = new Ext.ToolTip({
	        target: this.id,
	        html: '<div style="float:left"><img src="' + this.tooltipIcon + '"/></div><b>' + this.title + '</b><br/><br/>' + this.desc
	    });
	}
}
