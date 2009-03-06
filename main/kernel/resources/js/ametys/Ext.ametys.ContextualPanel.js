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
 * Ext.ametys.ContextualPanel
 *
 * @class Ext.ametys.ContextualPanel
 * @extends Ext.Panel
 * @constructor
 * @param {Object} config Configuration options
 */
Ext.ametys.ContextualPanel = function(config) 
{
	Ext.ametys.ContextualPanel.superclass.constructor.call(this, config);
}; 

Ext.extend(Ext.ametys.ContextualPanel, Ext.Panel, 
{
	margins: '5 0 0 0',
    cmargins: '5 5 0 0',
    layout: 'anchor',
    cls: 'context-panel',
    border: false,
    onRender : function(ct, position)
	{
		Ext.ametys.ContextualPanel.superclass.onRender.call(this, ct, position);
		
		this.body.addClass('context-panel-body');
	}
});