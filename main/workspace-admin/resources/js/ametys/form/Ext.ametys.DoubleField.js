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
 * Ext.ametys.DoubleField
 *
 * @class Ext.ametys.DoubleField
 * @extends Ext.form.NumberField
 * @constructor
 * @param {Object} config Configuration options
 */
Ext.ametys.DoubleField = function(config) 
{
	Ext.ametys.DoubleField.superclass.constructor.call(this, config);
}; 

Ext.extend(Ext.ametys.DoubleField, Ext.form.NumberField, 
{
	itemCls : "ametys-input",
	allowDecimals : true,
	labelSeparator: '',
	onRender : function(ct, position)
	{
		Ext.ametys.DoubleField.superclass.onRender.call(this, ct, position);
		
		this.el.parent().insertSibling({
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


