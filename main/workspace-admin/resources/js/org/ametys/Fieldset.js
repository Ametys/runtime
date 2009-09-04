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
Ext.namespace('org.ametys');

/**
 * org.ametys.Fieldset
 *
 * @class This class provides a collapsible panel 
 * @extends Ext.Panel
 * @constructor
 * @param {Object} config Configuration options
 */
org.ametys.Fieldset = function(config) 
{
	org.ametys.Fieldset.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.Fieldset, Ext.Panel, 
{
	baseCls : "ametys-fieldset",
	collapsible: true,
	titleCollapse: true,
	animCollapse : true,
	border: false
});

org.ametys.Fieldset.prototype._onCollapse = function (panel)
{
	if (panel.ownerCt.ownerCt)
		panel.ownerCt.ownerCt.doLayout.defer(100, panel.ownerCt.ownerCt);
	else
		panel.ownerCt.doLayout.defer(100, panel.ownerCt);
}
org.ametys.Fieldset.prototype._onExpand = function (panel)
{
	if (panel.ownerCt.ownerCt)
		panel.ownerCt.ownerCt.doLayout();
	else
		panel.ownerCt.doLayout();
}

org.ametys.Fieldset.prototype.onRender = function(ct, position)
{
	org.ametys.Fieldset.superclass.onRender.call(this, ct, position);
	
	//this.header.addClass(this.awtCls + '-header');
	//this.body.addClass(this.awtCls + '-body');
	
	this.el.insertFirst({tag: "a", name :this.id});
	
	this.addListener('collapse', this._onCollapse, this);
	this.addListener('expand', this._onExpand, this);
}


