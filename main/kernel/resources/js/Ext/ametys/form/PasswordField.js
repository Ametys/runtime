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
 * org.ametys.form.PasswordField
 *
 * @class This class provides a password field width a help icon. Use the <code>desc</option> to add the help icon.
 * @extends Ext.form.TextField
 * @constructor
 * @param {Object} config Configuration options
 */
org.ametys.form.PasswordField = function(config) 
{
	config.itemCls = "ametys-input";
	if (!config.labelSeparator)
	{
		config.labelSeparator = '';
	}
	config.inputType = "password";
	org.ametys.form.PasswordField.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.form.PasswordField, Ext.form.TextField, {});

org.ametys.form.PasswordField.prototype.onRender = function(ct, position)
{
	org.ametys.form.PasswordField.superclass.onRender.call(this, ct, position);
	
	if (this.desc)
	{
		this.el.insertSibling({
			id: this.name + '-img',
			tag:'img',
			style: 'padding-left: 9px; padding-top : 7px;',
			src: getPluginResourcesUrl('core') + '/img/administrator/config/help.gif'}, 'after');
	
		var tooltip = new Ext.ToolTip({
	        target: this.name + '-img',
	        html: this.desc
	    });
	}
}

