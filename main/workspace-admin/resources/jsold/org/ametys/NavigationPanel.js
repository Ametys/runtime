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
 * org.ametys.NavigationPanel
 *
 * @class org.ametys.NavigationPanel
 * @extends Ext.Panel
 * @constructor
 * @param {Object} config Configuration options
 * @example
 * var navigation = new org.ametys.NavigationPanel ({title: "Sommaire"});
 * var item = new org.ametys.NavigationItem ({
 * 	text: 'Base de données',
 *	divToScroll: 'ext-125554',
 * 	ctToScroll:  'config-inner',
 * 	bindScroll: bindScrollFn,
 * 	unbindScroll: unbindScrollFn,
 * 	toggleGroup : 'config-menu',
 * 	id : 'a-ext-125554'
 * });
 * navigation.add(item);
 */
org.ametys.NavigationPanel = function(config) 
{
	org.ametys.NavigationPanel.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.NavigationPanel, Ext.Panel, 
{
	autoDestroy: false,
	collapsible: false,
	cmargins: '5 0 0 0',
	awtCls : 'navigation-panel',
	cls: 'navigation-panel',
	elements: 'body,footer',
	navitems : []
});

/**
 * Adds an item of navigation to this panel (see {@link org.ametys.NavigationItem}).  
 * @param {String} text The text of the item
 * @param act The function to call on click event
 */
org.ametys.NavigationPanel.prototype.addItems = function (text, act) 
{ 
	var span = document.createElement("span");
	
	var link = document.createElement("a");
    link.innerHTML = text;
    link.href = "#";
    link.className = "link"
    span.appendChild (link);

    var navitem = new org.ametys.NavigationItem ({ 
		border: false,
		html : span.innerHTML,
		listeners: {"click" : act}
	});
    
	this.add(navitem);
	this.navitems.push(navitem);
}


org.ametys.NavigationPanel.prototype.onRender = function(ct, position)
{
	org.ametys.NavigationPanel.superclass.onRender.call(this, ct, position);
	
	this.header.addClass(this.awtCls + '-header');
	this.body.addClass(this.awtCls + '-body');
	
	if (this.footer)
	{
		this.footer.addClass(this.awtCls + '-footer');
	}
}
