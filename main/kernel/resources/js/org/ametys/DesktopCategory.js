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
 * org.ametys.DesktopCategory
 *
 * @class This class represents a category of {@link org.ametys.DesktopItem}
 * @extends Ext.BoxComponent
 * @constructor
 * @param {Object} config Configuration options
 */
org.ametys.DesktopCategory = function(config) 
{
	org.ametys.DesktopCategory.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.DesktopCategory, Ext.BoxComponent, 
{
	cls: 'desktop-category-title',
	border: false
});

org.ametys.DesktopCategory.prototype.onRender = function(ct, position)
{
	org.ametys.DesktopCategory.superclass.onRender.call(this, ct, position);
	
	if (this.text)
	{
		this.el.update(this.text);
	}
}
