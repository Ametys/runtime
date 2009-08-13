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
 * org.ametys.ActionsPanel
 *
 * @class This action provides a contextual panel for link actions.<br/>Use the <code>addAction</code> to add a new link action to this panel.
 * @extends Ext.Panel
 * @constructor
 * @param {Object} config Configuration options
 * @example
 * var handle = new org.ametys.ActionsPanel({title: "&lt;i18n:text i18n:key="CONFIG_HANDLE"/&gt;"});
 *	handle.addAction("&lt;i18n:text i18n:key="CONFIG_HANDLE_SAVE"/&gt;", "/img/save.png", save);
 *	handle.addAction("&lt;i18n:text i18n:key="CONFIG_HANDLE_QUIT"/&gt;", "/img/quit.png", goBack);
 */
org.ametys.ActionsPanel = function(config) 
{
	org.ametys.ActionsPanel.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.ActionsPanel, Ext.Panel, 
{
	autoDestroy: false,
	collapsible: false,
	cmargins: '5 0 0 0',
	awtCls : 'actions-panel',
	cls: 'actions-panel',
	actions : [],
	elements: 'body,footer'
});


/**
 * Adds an action (see <code>org.ametys.Action</code> to this panel.  
 * @param {String} text The text of the action
 * @param {String} icon The absolute url of the icon. Can be null
 * @param {Function} act The function to be called on click event 
 */
org.ametys.ActionsPanel.prototype.addAction = function (text, icon, act) 
{ 
	var span = document.createElement("span");
	if (icon)
    {
		var image = document.createElement("img");
		image.src = icon;
		image.className = "icon";
        span.appendChild (image);
    }

	var link = document.createElement("a");
    link.innerHTML = text;
    link.href = "#";
    link.className = "link"
    span.appendChild (link);

    var action = new org.ametys.Action ({ 
		border: false,
		html : span.innerHTML,
		listeners: {"click" : act},
		icon : icon,
		iconOver : icon.substring(0, icon.indexOf('.')) +  '-over.png'
	});
    
	this.add(action);
	this.actions.push(action);
}
/**
 * Hide the action to the position argument
 * @param position The position of the action to hide in the panel
 */
org.ametys.ActionsPanel.prototype.hideElt = function (position) 
{ 
	var act = this.actions[position];
	if(act != null)
	{
		act.setVisible(false);
	}
}

/**
 * Show the action to the position argument
 *  @param position The position of the action to show in the panel
 */
org.ametys.ActionsPanel.prototype.showElt = function (position) 
{ 
	var act = this.actions[position];
	if(act != null)
	{
		act.setVisible(true);
	}
}
org.ametys.ActionsPanel.prototype.onRender = function(ct, position)
{
	org.ametys.ActionsPanel.superclass.onRender.call(this, ct, position);
	
	this.header.addClass(this.awtCls + '-header');
	this.body.addClass(this.awtCls + '-body');
	
	if (this.footer)
	{
		this.footer.addClass(this.awtCls + '-footer');
	}
}
