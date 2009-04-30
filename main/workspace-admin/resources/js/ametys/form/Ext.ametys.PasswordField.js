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
 * Ext.ametys.PasswordField
 *
 * @class Ext.ametys.PasswordField
 * @extends Ext.form.TextField
 * @constructor
 * @param {Object} config Configuration options
 */
Ext.ametys.PasswordField = function(config) 
{
	Ext.ametys.PasswordField.superclass.constructor.call(this, config);
}; 

Ext.extend(Ext.ametys.PasswordField, Ext.form.TextField, 
{
	itemCls : "ametys-input",
	inputType: 'password',
	labelSeparator: '',
	onRender : function(ct, position)
	{
		Ext.ametys.PasswordField.superclass.onRender.call(this, ct, position);
		
		this.el.insertSibling({
			id: this.name + '-img',
			tag:'img',
			style: 'padding-left: 9px; padding-top : 7px;',
			src: context.contextPath + '/plugins/core/resources/img/administrator/config/help.gif'}, 'after');

		var tooltip = new Ext.ToolTip({
	        target: this.name + '-img',
	        html: this.desc
	    });
	}
});


