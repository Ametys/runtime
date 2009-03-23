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
 * Ext.ametys.TextField
 *
 * @class Ext.ametys.TextField
 * @extends Ext.form.TextField
 * @constructor
 * @param {Object} config Configuration options
 */
Ext.ametys.TextField = function(config) 
{
	Ext.ametys.TextField.superclass.constructor.call(this, config);
	
	this.addEvents(
			/**
		     * @event beforeclick
		     * Fires before click processing. Return false to cancel the default action.
		     * @param {Node} this This node
		     * @param {Ext.EventObject} e The event object
		    */
		    "keyup"
	);
}; 

Ext.extend(Ext.ametys.TextField, Ext.form.TextField, 
{
	itemCls : "ametys-input",
	labelSeparator: '',
	onRender : function(ct, position)
	{
		Ext.ametys.TextField.superclass.onRender.call(this, ct, position);
		
		if (this.desc)
		{
			this.el.insertSibling({
				tag:'img',
				id: this.name + '-img',
				style: 'padding-left: 9px; padding-top : 7px;',
				src: context.contextPath + '/plugins/core/resources/img/administrator/config/help.gif'
				}, 'after');
			
			var tooltip = new Ext.ToolTip({
		        target: this.name + '-img',
		        html: this.desc
		    });
		}
	}
});


