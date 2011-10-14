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

Ext.namespace('org.ametys.rights');

/**
 * org.ametys.rights.CheckRightEntry
 * @class This class provides a checkbox with a help icon for a right. If the right text is too long, it will be automatically cut with '...'.
 * @extends Ext.form.Checkbox
 * @constructor
 * @param {Object} config Configuration options
 * @example
 * var right = new org.ametys.right.CheckRightEntry({
 * 	text: "&lt;i18n:text i18n:key="PLUGINS_CORE_RIGHTS_HANDLE_LABEL"/&gt;",
 * 	description: "&lt;i18n:text i18n:key="PLUGINS_CORE_RIGHTS_HANDLE_DESCRIPTION"/&gt;",
 *  category: "<xsl:value-of select="$category"/>",
 * 	name: 'Runtime_Rights_Rights_Handle',
 * 	id: 'RIGHT_Runtime_Rights_Rights_Handle',
 * 	width: 190
 * });
 */
org.ametys.rights.CheckRightEntry = function(config) 
{
	config.boxLabel = config.text;
	config.itemCls = 'check-right-entry';
	org.ametys.rights.CheckRightEntry.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.rights.CheckRightEntry,  Ext.form.Checkbox, {});

org.ametys.rights.CheckRightEntry.prototype.onRender = function (ct, position)
{
	org.ametys.rights.CheckRightEntry.superclass.onRender.call(this, ct, position);
	
	// Help icon
	if (this.description)
	{
		this.el.parent().insertFirst({
			id: this.id + '-img',
			tag:'img',
			src: getPluginResourcesUrl('core') + '/img/administrator/config/help.gif',
			cls: 'check-right-entry-img'
		});
		var tooltip = new Ext.ToolTip({
	        target: this.id + '-img',
	        html: this.description
	    });
	}
	
	var maxWidth = this.width - 0; //padding + image
	var textMesurer = Ext.util.TextMetrics.createInstance(this.el);
	var textWidth = textMesurer.getWidth(this.boxLabel); // Taille en pixel
	var nbCars = this.boxLabel.length; // Nombre de caractères
	var carWidth = Math.floor(textWidth / nbCars);//Taille moyenne d'un caractères
	if (textWidth > maxWidth)
	{
		this.boxLabel = this.boxLabel.substring(0, (Math.floor(maxWidth /carWidth) - 3*carWidth)) + "...";
		if (this.wrap)
		{
			var nodes = this.wrap.dom.childNodes;
			for (var i=0; i < nodes.length; i++)
			{
				if (nodes[i].tagName.toLowerCase() == 'label')
				{
					nodes[i].innerHTML = this.boxLabel;
				}
			}
		}
	}
}


