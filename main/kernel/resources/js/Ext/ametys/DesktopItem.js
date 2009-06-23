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
 * Ext.ametys.DesktopItem
 *
 * @class This class handles an item of the desktop
 * @extends Ext.BoxComponent
 * @constructor
 * @param {Object} config Configuration options
 * @example
 * var item = new Ext.ametys.DesktopItem ({
 *	text: "Mot de passe",
 *	desc: "Modifier le mot de passe du compte 'admin'",
 *	icon: '/plugins/core/resources/img/administrator/password/icon_large.png',
 *	iconOver: '/plugins/core/resources/img/administrator/password/icon_large_over.png', 
 *	plugin : 'core',
 *	actionFunction : RUNTIME_Plugin_Runtime_Administrator_Password.act,
 *	actionParams : {}
 * });
 */
Ext.ametys.DesktopItem = function(config) 
{
	Ext.ametys.DesktopItem.superclass.constructor.call(this, config);
	this.addEvents(
			/**
		     * @event beforeclick
		     * Fires before click processing. Return false to cancel the default action.
		     * @param {Ext.EventObject} e The event object
		    */
		    "beforeclick",
		    /**
		    * @event click
		    * Fires when this node is clicked
		    * @param {Ext.EventObject} e The event object
		    */
		    "click",
		    /**
             * @event mouseover
             * Fires mouse over processing.
             * @param {Ext.EventObject} e
             */
            "mouseover",
            /**
             * @event mouseout
             * Fires mouse out processing.
             * @param {Ext.EventObject} e
             */
            "mouseout"
	);
}; 

Ext.extend(Ext.ametys.DesktopItem, Ext.BoxComponent, 
{
	cls: 'desktop-item'
});

/**
 * This function is called when the mouse hovers the item
 * @param e The event object
 * @private
 */
Ext.ametys.DesktopItem.prototype._onMouseOver = function(e)
{
	if(this.fireEvent("mouseover", this.node, e) !== false)
	{
		if (this.iconOver != null)
		{
			var img = this.el.dom.getElementsByTagName("img")[0];
			img.src = this.iconOver;
		}
		e.preventDefault();
	}
	else
    {
        e.stopEvent();
    }
	
}

/**
* This function is called when the mouse exits the item
* @param e The event object
* @private
*/
Ext.ametys.DesktopItem.prototype._onMouseOut : function(e)
{
	if(this.fireEvent("mouseout", this.node, e) !== false)
	{
		if (this.icon != null)
		{
			var img = this.el.dom.getElementsByTagName("img")[0];
			img.src = this.icon;
		}
		e.preventDefault();
	}
	else
    {
        e.stopEvent();
    }
	
}

/**
 * This function is called when the item is clicked
 * @param e The event object
 * @private
 */
Ext.ametys.DesktopItem.prototype._onClick = function(e)
{
    if(this.fireEvent("beforeclick", this.node, e) !== false)
    {
    	this.actionFunction(this.plugin, this.actionParams);
        e.preventDefault();
    }
    else
    {
        e.stopEvent();
    }
}

Ext.ametys.DesktopItem.prototype.onRender = function(ct, position)
{
	Ext.ametys.DesktopItem.superclass.onRender.call(this, ct, position);
	
	if(!this.el) 
	{
		this.el = ct.createChild({
            id: this.id,
            cls: this.cls
        }, position);
	}
	
	this.el.on('click', this._onClick, this);
	this.el.on('mouseover', this._onMouseOver, this);
	this.el.on('mouseout', this._onMouseOut, this);
	this.el.createChild({cls: this.cls + '-img', html: '<img src="' + this.icon + '"/>'});
	this.el.createChild({cls: this.cls + '-title', html: this.text});
	
	var tooltip = new Ext.ToolTip({
        target: this.id,
        html: Ext.ametys.AdminTools.DesktopItemTooltipFormater(this.text, this.desc),
        shadow : false
    });
	//this.el.addClassOnOver(this.overCls);
}
