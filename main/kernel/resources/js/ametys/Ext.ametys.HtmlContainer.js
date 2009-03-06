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
 * Ext.ametys.HtmlContainer
 * Use to create a simple div with optional id (id) and css class (baseCls) 
 * new Ext.ametys.HtmlContainer ({
 * 		id : 'my-id',
 * 		baseCls : 'my-css-class',
 * 		html : '<p>The HTML content for the div</p>'
 * }); 
 * done :
 * &lt;div id="my-id" class="my-css-class"&gt;
 * 		&lt;p&gt;The HTML content for the div&lt;/p&gt;
 * &lt;/div&gt;
 * @class Ext.ametys.HtmlContainer
 * @extends Ext.BoxComponent
 * @constructor
 * @param {Object} config Configuration options
 */
Ext.ametys.HtmlContainer = function(config) 
{
	Ext.ametys.HtmlContainer.superclass.constructor.call(this, config);
}; 

Ext.extend(Ext.ametys.HtmlContainer, Ext.Container, 
{
	border: false,
	autoscroll: true, 
	onRender : function(ct, position)
	{
		Ext.ametys.HtmlContainer.superclass.onRender.call(this, ct, position);
		
		if(!this.el) 
		{
			this.el = ct.createChild({
	            id: this.id,
	            cls: this.baseCls,
	            tag: this.tag
	        }, position);
		}
		if (this.html)
		{
			this.el.dom.innerHTML = this.html;
		}
		if (this.contentEl)
		{
			var ce = Ext.getDom(this.contentEl);
			this.el.dom.appendChild(ce);
		}
		
	}
});


