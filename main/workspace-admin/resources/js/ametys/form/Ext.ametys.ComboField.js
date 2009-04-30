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
 * Ext.ametys.ComboField
 *
 * @class Ext.ametys.AdminComboField
 * @extends Ext.form.ComboBox
 * @constructor
 * @param {Object} config Configuration options
 */
Ext.ametys.ComboField = function(config) 
{
	Ext.ametys.ComboField.superclass.constructor.call(this, config);
}; 

Ext.extend(Ext.ametys.ComboField, Ext.form.ComboBox, 
{
	/*listClass: 'admin-combo-list',*/
	itemCls : "ametys-select",
	width: 200,
	labelSeparator: '',
	onRender : function(ct, position)
	{
		Ext.ametys.ComboField.superclass.onRender.call(this, ct, position);
		
		this.el.parent().insertSibling({
			id: this.name + '-img',
			tag:'img',
			style: 'padding-left: 26px; padding-top : 7px;',
			src: context.contextPath + '/plugins/core/resources/img/administrator/config/help.gif'}, 'after');
		
		var tooltip = new Ext.ToolTip({
	        target: this.name + '-img',
	        html: this.desc
	    });
	}
});


