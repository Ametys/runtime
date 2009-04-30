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
Ext.namespace('Ext.ametys');

/**
 * Ext.ametys.RightEntry
 *
 * @class Ext.ametys.RightEntry
 * @extends Ext.Container
 * @constructor
 * @param {Object} config Configuration options
 */
Ext.ametys.RightEntry = function(config) 
{
	Ext.ametys.RightEntry.superclass.constructor.call(this, config);
}; 

Ext.extend(Ext.ametys.RightEntry,  Ext.Container, 
{
	itemCls : "profile-text",
	/** 
     * Cutting the text with a '...' if too long 
     * */
	setText : function(text)
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
    },
	onBeforeRender : function(a, z)
	{
		debugger;
	},
	onRender : function(ct, position)
	{
		Ext.ametys.RightEntry.superclass.onRender.call(this, ct, position);
		
		if(!this.el) 
		{
			this.el = ct.createChild({
	            id: this.id,
	            cls: this.itemCls
	        }, position);
		}
		var img = '<img src="' + context.contextPath + '/plugins/core/resources/img/administrator/config/help.gif" style="margin-right: 5px; margin-left: 5px;" title="' + this.desc + '"/>';
		
		var maxWidth = this.width - 10 - 20; //padding + image
		var textMesurer = Ext.util.TextMetrics.createInstance(this.el);
    	var textWidth = textMesurer.getWidth(this.text); // Taille en pixel
    	var nbCars = this.text.length; // Nombre de caractères
    	var carWidth = Math.floor(textWidth / nbCars);//Taille moyenne d'un caractères
    	if (textWidth > maxWidth)
    	{
    		this.text = this.text.substring(0, (Math.floor(maxWidth /carWidth) - 3*carWidth)) + "...";
    	}
    	this.el.dom.innerHTML = img + this.text;
	}
});


