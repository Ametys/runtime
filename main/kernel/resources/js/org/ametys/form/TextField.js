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
 * org.ametys.form.TextField
 *
 * @class This class provides a text field width a help icon. Use the <code>desc</option> to add the help icon.
 * @extends Ext.form.TextField
 * @constructor
 * @param {Object} config Configuration options
 */
org.ametys.form.TextField = function(config) 
{
	config.itemCls = "ametys-input";
	config.labelSeparator = '';
	
	org.ametys.form.TextField.superclass.constructor.call(this, config);
	
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

Ext.extend(org.ametys.form.TextField, Ext.form.TextField, {});

org.ametys.form.TextField.prototype.onRender = function(ct, position)
{
	org.ametys.form.TextField.superclass.onRender.call(this, ct, position);
	
	if (this.desc)
	{
		this.el.insertSibling({
			id: this.name + '-img',
			tag:'img',
			style: 'padding-left: 20px; padding-top : 7px; float: left;',
			src: getPluginResourcesUrl('core') + '/img/administrator/config/help.gif'}, 'after');
	
		var tooltip = new Ext.ToolTip({
	        target: this.name + '-img',
	        html: this.desc
	    });
	}
}
