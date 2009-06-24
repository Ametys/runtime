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
 * Ext.ametys.NavigationPanel
 *
 * @class Ext.ametys.NavigationPanel
 * @extends Ext.Panel
 * @constructor
 * @param {Object} config Configuration options
 * @example
 * var navigation = new Ext.ametys.NavigationPanel ({title: "Sommaire"});
 * var item = new Ext.ametys.NavigationItem ({
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
Ext.ametys.NavigationPanel = function(config) 
{
	Ext.ametys.NavigationPanel.superclass.constructor.call(this, config);
}; 

Ext.extend(Ext.ametys.NavigationPanel, Ext.Panel, 
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
 * Adds an item of navigation to this panel (see {@link Ext.ametys.NavigationItem}).  
 * @param {String} text The text of the item
 * @param act The function to call on click event
 */
Ext.ametys.NavigationPanel.prototype.addItems = function (text, act) 
{ 
	var span = document.createElement("span");
	
	var link = document.createElement("a");
    link.innerHTML = text;
    link.href = "#";
    link.className = "link"
    span.appendChild (link);

    var navitem = new Ext.ametys.NavigationItem ({ 
		border: false,
		html : span.innerHTML,
		listeners: {"click" : act}
	});
    
	this.add(navitem);
	this.navitems.push(navitem);
}


Ext.ametys.NavigationPanel.prototype.onRender = function(ct, position)
{
	Ext.ametys.NavigationPanel.superclass.onRender.call(this, ct, position);
	
	this.header.addClass(this.awtCls + '-header');
	this.body.addClass(this.awtCls + '-body');
	
	if (this.footer)
	{
		this.footer.addClass(this.awtCls + '-footer');
	}
}
