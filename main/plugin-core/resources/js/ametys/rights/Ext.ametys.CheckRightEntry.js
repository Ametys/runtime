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
 * Ext.ametys.CheckRightEntry
 *
 * @class Ext.ametys.CheckRightEntry
 * @extends Ext.form.Checkbox
 * @constructor
 * @param {Object} config Configuration options
 */
Ext.ametys.CheckRightEntry = function(config) 
{
	Ext.ametys.CheckRightEntry.superclass.constructor.call(this, config);
}; 

Ext.extend(Ext.ametys.CheckRightEntry,  Ext.form.Checkbox, 
{
	itemCls : "profile-checkbox",
	onRender : function(ct, position)
	{
		Ext.ametys.CheckRightEntry.superclass.onRender.call(this, ct, position);
		
		this.el.parent().insertFirst({
			tag:'img',
			src: context.contextPath + '/plugins/core/resources/img/administrator/config/help.gif',
			style : 'margin-top: 2px; margin-right: 5px; margin-left: 5px;',
			title : this.desc});
		
		var maxWidth = this.width - 10 - 20; //padding + image
		var textMesurer = Ext.util.TextMetrics.createInstance(this.el);
    	var textWidth = textMesurer.getWidth(this.boxLabel); // Taille en pixel
    	var nbCars = this.boxLabel.length; // Nombre de caractères
    	var carWidth = Math.floor(textWidth / nbCars);//Taille moyenne d'un caractères
    	if (textWidth > maxWidth)
    	{
    		this.boxLabel = this.boxLabel.substring(0, (Math.floor(maxWidth /carWidth) - 3*carWidth)) + "...";
    		this.labelEl.dom.innerHTML = this.boxLabel;
    	}
	}
});


