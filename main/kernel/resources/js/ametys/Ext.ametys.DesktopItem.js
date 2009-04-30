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
 * Ext.ametys.HomeItem
 *
 * @class Ext.ametys.DesktopItem
 * @extends Ext.BoxComponent
 * @constructor
 * @param {Object} config Configuration options
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
	cls: 'desktop-item',
	//overCls : 'item-over',
	onMouseOver : function(e)
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
		
	},
	onMouseOut : function(e)
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
		
	},
	onClick : function(e)
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
	},
	onRender : function(ct, position)
	{
		Ext.ametys.DesktopItem.superclass.onRender.call(this, ct, position);
		
		if(!this.el) 
		{
			this.el = ct.createChild({
	            id: this.id,
	            cls: this.cls
	        }, position);
		}
		
		this.el.on('click', this.onClick, this);
		this.el.on('mouseover', this.onMouseOver, this);
		this.el.on('mouseout', this.onMouseOut, this);
		this.el.createChild({cls: this.cls + '-img', html: '<img src="' + this.icon + '"/>'});
		this.el.createChild({cls: this.cls + '-title', html: this.text});
		
		var tooltip = new Ext.ToolTip({
	        target: this.id,
	        html: Ext.ametys.AdminTools.DesktopItemTooltipFormater(this.text, this.desc),
	        shadow : false
	    });
		//this.el.addClassOnOver(this.overCls);
	}
});