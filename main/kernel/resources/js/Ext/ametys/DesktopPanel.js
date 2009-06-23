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
 * Ext.ametys.DesktopPanel
 * @class This class provides a container for {@link Ext.ametys.DesktopItem}
 * @extends Ext.Container
 * @constructor
 * @param {Object} config Configuration options
 */
Ext.ametys.DesktopPanel = function(config) 
{
	Ext.ametys.DesktopPanel.superclass.constructor.call(this, config);
}; 

Ext.extend(Ext.ametys.DesktopPanel, Ext.Container, 
{
	border: false,
	autoscroll: true
}

Ext.ametys.DesktopPanel.prototype.onRender = function(ct, position)
{
	Ext.ametys.DesktopPanel.superclass.onRender.call(this, ct, position);
	
	if(!this.el) 
	{
		this.el = ct.createChild({
            id: this.id,
            cls: this.baseCls
        }, position);
	}
}


