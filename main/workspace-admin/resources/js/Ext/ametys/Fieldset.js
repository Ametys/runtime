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
 * Ext.ametys.Fieldset
 *
 * @class This class provides a collapsible panel 
 * @extends Ext.Panel
 * @constructor
 * @param {Object} config Configuration options
 */
Ext.ametys.Fieldset = function(config) 
{
	Ext.ametys.Fieldset.superclass.constructor.call(this, config);
}; 

Ext.extend(Ext.ametys.Fieldset, Ext.Panel, 
{
	baseCls : "ametys-fieldset",
	collapsible: true,
	titleCollapse: true,
	animCollapse : false,
	border: false
});

Ext.ametys.Fieldset.prototype.onRender = function(ct, position)
{
	Ext.ametys.Fieldset.superclass.onRender.call(this, ct, position);
	
	//this.header.addClass(this.awtCls + '-header');
	//this.body.addClass(this.awtCls + '-body');
	
	this.el.insertFirst({tag: "a", name :this.id});
}


