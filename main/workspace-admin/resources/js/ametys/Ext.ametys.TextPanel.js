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
Ext.namespace('Ext.awt');

/**
 * Ext.ametys.TextPanel
 *
 * @class Ext.ametys.TextPanel
 * @extends Ext.Panel
 * @constructor
 * @param {Object} config Configuration options
 */
Ext.ametys.TextPanel = function(config) 
{
	Ext.ametys.CategoryPanel.superclass.constructor.call(this, config);
}; 

Ext.extend(Ext.ametys.TextPanel, Ext.Panel, 
{
	autoDestroy: false,
	collapsible: false,
	cmargins: '5 0 0 0',
	awtCls : 'text-panel',
	cls: 'text-panel',
	elements: 'body,footer',
	/**
     * Adds a text to this panel.  
     * @param {String} text The text
     */
	addText : function (text) 
	{ 
		this.add({cls: this.awtCls + '-content', html: text,  border: false });
	},
	onRender : function(ct, position)
	{
		Ext.ametys.TextPanel.superclass.onRender.call(this, ct, position);
		
		this.header.addClass(this.awtCls + '-header');
		this.body.addClass(this.awtCls + '-body');
		
		if (this.footer)
		{
			this.footer.addClass(this.awtCls + '-footer');
		}
	}
});