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
 * Ext.ametys.DialogBox
 *
 * @class Ext.ametys.DialogBox
 * @extends Ext.Window
 * @constructor
 * @param {Object} config Configuration options
 */
Ext.ametys.DialogBox = function(config) 
{
	Ext.ametys.DialogBox.superclass.constructor.call(this, config);
}; 

Ext.extend(Ext.ametys.DialogBox, Ext.Window, 
{
	resizable : true,
	shadow : true,
	ametysCls : 'ametys-box',
	onRender : function(ct, position)
	{
		Ext.ametys.DialogBox.superclass.onRender.call(this, ct, position);
		this.body.addClass(this.ametysCls + '-body');
		this.header.addClass(this.ametysCls + '-header');
	}
});