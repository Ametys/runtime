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
 * @class Use to create a simple div with optional id (id) and css class (baseCls)
 * @extends Ext.BoxComponent
 * @constructor
 * @param {Object} config Configuration options
 * @example
 * new org.ametys.HtmlContainer ({
 *	id : 'my-id',
 * 	cls : 'my-css-class',
 * 	html : '&lt;p&gt;The HTML content for the div&lt;/p&gt;'
 * });<br/>
 * <b>done :</b><br/>
 * &lt;div id="my-id" class="my-css-class"&gt;
 * 	&lt;p&gt;The HTML content for the div&lt;/p&gt;
 * &lt;/div&gt;
 */
org.ametys.HtmlContainer = function(config) 
{
	org.ametys.HtmlContainer.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.HtmlContainer, Ext.Container, 
{
	border: false,
	autoscroll: true
});


org.ametys.HtmlContainer.prototype.onRender = function(ct, position)
{
	org.ametys.HtmlContainer.superclass.onRender.call(this, ct, position);
	
	if (this.html)
	{
		this.el.update(this.html);
	}
	if (this.contentEl)
	{
		var ce = Ext.getDom(this.contentEl);
		this.el.dom.appendChild(ce);
	}
	
}
