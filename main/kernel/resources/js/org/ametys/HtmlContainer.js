/*
 *  Copyright 2009 Anyware Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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

org.ametys.HtmlContainer.prototype.update = function (html)
{
	if (this.rendered)
	{
		org.ametys.HtmlContainer.superclass.update.call(this, html);
	}
	else
	{
		this.html = html;
	}
}

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
