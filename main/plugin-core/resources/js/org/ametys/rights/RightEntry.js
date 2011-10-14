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
 * org.ametys.rights.RightEntry
 * @class This class provides a simple container text with a help icon for a right. If the right text is too long, it will be automatically cut with '...'.
 * @extends Ext.Container
 * @constructor
 * @param {Object} config Configuration options
 * @example
 * var right = new org.ametys.right.RightEntry({
 * 	text: "&lt;i18n:text i18n:key="PLUGINS_CORE_RIGHTS_HANDLE_LABEL"/&gt;",
 * 	description: "&lt;i18n:text i18n:key="PLUGINS_CORE_RIGHTS_HANDLE_DESCRIPTION"/&gt;",
 * 	id: 'RIGHT_Runtime_Rights_Rights_Handle',
 * 	width: 190
 * });
 */
org.ametys.rights.RightEntry = function(config) 
{
	config.cls = "right-entry";
	org.ametys.rights.RightEntry.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.rights.RightEntry, Ext.Container, {});

/**
 * Set the text of this right entry. The text will be automatically cut with '...' if it is too long
 */
org.ametys.rights.RightEntry.prototype.setText = function (text)
{
	if(this.el)
    {
		var textMesurer = Ext.util.TextMetrics.createInstance(this.el);
		var textWidth = textMesurer.getWidth(text); // Taille en pixel
		var nbCars = text.length; // Nombre de caractères
		var carWidth = Math.floor(textWidth / nbCars); //Taille moyenne d'un caractères
		if (textWidth > this.width)
		{
			text = text.substring(0, Math.floor(maxWidth /carWidth)) + "...";
		}
    }
    this.text = text;
}

org.ametys.rights.RightEntry.prototype.onRender = function(ct, position)
{
	org.ametys.rights.RightEntry.superclass.onRender.call(this, ct, position);
	
	// Help icon
	if (this.description)
	{
		var img = this.getEl().createChild({
			id: this.id + '-img',
			cls: 'right-entry-img',
			tag:'img',
			src: getPluginResourcesUrl('core') + '/img/administrator/config/help.gif'});
		var tooltip = new Ext.ToolTip({
	        target: this.id + '-img',
	        html: this.description
	    });
	}
	
	var maxWidth = this.width - 5 - 20; //padding + image
	var textMesurer = Ext.util.TextMetrics.createInstance(this.el);
	var textWidth = textMesurer.getWidth(this.text); // Taille en pixel
	var nbCars = this.text.length; // Nombre de caractères
	var carWidth = Math.floor(textWidth / nbCars);//Taille moyenne d'un caractères
	if (textWidth > maxWidth)
	{
		this.text = this.text.substring(0, (Math.floor(maxWidth /carWidth) - 3)) + "...";
	}
	
	var divText = this.getEl().createChild({
		html: this.text,
		cls: 'right-entry-text'
	});
}
