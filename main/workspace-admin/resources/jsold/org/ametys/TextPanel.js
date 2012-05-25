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

Ext.namespace('org.ametys');

/**
 * org.ametys.TextPanel
 *
 * @class This action provide a contextual text panel.<br/>Use the <code>addText</code> to add html text to this panel.
 * @extends Ext.Panel
 * @constructor
 * @param {Object} config Configuration options
 * @example
 * var help = new org.ametys.TextPanel({
 * 	title: "&lt;i18n:text i18n:key="ADMINISTRATOR_CONFIG_HELP"/&gt;"
 * });
 * help.addText("Cet écran vous permet de modifier la configuration système de l'application.
 * 	&lt;br/&gt;&lt;br/&gt;Naviguez en utilisant &lt;b&gt;les onglets&lt;/b&gt; et modifiez 
 * 	les valeurs des paramètres.");
 */
org.ametys.TextPanel = function(config) 
{
	org.ametys.TextPanel.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.TextPanel, Ext.Panel, 
{
	autoDestroy: false,
	collapsible: false,
	cmargins: '5 0 0 0',
	awtCls : 'text-panel',
	cls: 'text-panel',
	elements: 'body,footer'
});

/**
 * Adds a text to this panel.  
 * @param {String} text The html text
 */
org.ametys.TextPanel.prototype.addText = function (text)
{
	this.add({cls: this.awtCls + '-content', html: text,  border: false });
}
	
org.ametys.TextPanel.prototype.onRender = function(ct, position)
{
	org.ametys.TextPanel.superclass.onRender.call(this, ct, position);
	
	this.header.addClass(this.awtCls + '-header');
	this.body.addClass(this.awtCls + '-body');
	
	if (this.footer)
	{
		this.footer.addClass(this.awtCls + '-footer');
	}
}