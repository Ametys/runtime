/*
 * Copyright (c) 2008 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */

// Ametys Namespace
Ext.namespace('Ext.ametys.rights');

/**
 * Ext.ametys.rights.RightEntry
 * @class This class provides a simple container text with a help icon for a right. If the right text is too long, it will be automatically cut with '...'.
 * @extends Ext.Container
 * @constructor
 * @param {Object} config Configuration options
 * @example
 * var right = new Ext.ametys.right.RightEntry({
 * 	text: "&lt;i18n:text i18n:key="PLUGINS_CORE_RIGHTS_HANDLE_LABEL"/&gt;",
 * 	description: "&lt;i18n:text i18n:key="PLUGINS_CORE_RIGHTS_HANDLE_DESCRIPTION"/&gt;",
 * 	id: 'RIGHT_Runtime_Rights_Rights_Handle',
 * 	width: 190
 * });
 */
Ext.ametys.rights.RightEntry = function(config) 
{
	config.cls = "right-entry";
	Ext.ametys.rights.RightEntry.superclass.constructor.call(this, config);
}; 

Ext.extend(Ext.ametys.rights.RightEntry, Ext.Container, {});

/**
 * Set the text of this right entry. The text will be automatically cut with '...' if it is too long
 */
Ext.ametys.rights.RightEntry.prototype.setText = function (text)
{
	if(this.el)
    {
		var textMesurer = Ext.util.TextMetrics.createInstance(this.el);
		var textWidth = textMesurer.getWidth(text); // Taille en pixel
		var nbCars = text.length; // Nombre de caractères
		var carWidth = Math.floor(textWidth / nbCars);//Taille moyenne d'un caractères
		if (textWidth > this.width)
		{
			text = text.substring(0, Math.floor(maxWidth /carWidth)) + "...";
		}
    }
    this.text = text;
}

Ext.ametys.rights.RightEntry.prototype.onRender = function(ct, position)
{
	Ext.ametys.rights.RightEntry.superclass.onRender.call(this, ct, position);
	
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
	
	var maxWidth = this.width - 10 - 20; //padding + image
	var textMesurer = Ext.util.TextMetrics.createInstance(this.el);
	var textWidth = textMesurer.getWidth(this.text); // Taille en pixel
	var nbCars = this.text.length; // Nombre de caractères
	var carWidth = Math.floor(textWidth / nbCars);//Taille moyenne d'un caractères
	if (textWidth > maxWidth)
	{
		this.text = this.text.substring(0, (Math.floor(maxWidth /carWidth) - 3*carWidth)) + "...";
	}
	
	var divText = this.getEl().createChild({
		html: this.text,
		cls: 'right-entry-text'
	});
}
