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
Ext.namespace('org.ametys');

/**
 * org.ametys.DesktopPanel
 * @class This class provides a container for {@link org.ametys.DesktopItem}
 * @extends Ext.Container
 * @constructor
 * @param {Object} config Configuration options
 */
org.ametys.DesktopPanel = function(config) 
{
	org.ametys.DesktopPanel.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.DesktopPanel, Ext.Container, 
{
	cls: 'desktop',
	border: false,
	autoscroll: true
});

org.ametys.DesktopPanel.prototype.onRender = function(ct, position)
{
	org.ametys.DesktopPanel.superclass.onRender.call(this, ct, position);
}


