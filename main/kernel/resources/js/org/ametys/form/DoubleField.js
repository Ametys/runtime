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
Ext.namespace('org.ametys.form');

/**
 * org.ametys.form.LongField
 *
 * @class This class provides a text field, width a help icon. Use the <code>desc</option> to add the help icon.<br/>Only decimal or no decimal values are accepted.
 * @extends Ext.form.NumberField
 * @constructor
 * @param {Object} config Configuration options
 */
org.ametys.form.DoubleField = function(config) 
{
	config.itemCls = "ametys-input";
	config.labelSeparator = '';
	config.allowDecimals = true,
	
	org.ametys.form.DoubleField.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.form.DoubleField, Ext.form.NumberField, {});

org.ametys.form.DoubleField.prototype.onRender = function(ct, position)
{
	org.ametys.form.DoubleField.superclass.onRender.call(this, ct, position);
	
	if (this.desc)
	{
		this.el.insertSibling({
			id: this.name + '-img',
			tag:'img',
			style: 'padding-left: 20px; padding-top : 7px;',
			src: getPluginResourcesUrl('core') + '/img/administrator/config/help.gif'}, 'after');
	
		var tooltip = new Ext.ToolTip({
	        target: this.name + '-img',
	        html: this.desc
	    });
	}
}
