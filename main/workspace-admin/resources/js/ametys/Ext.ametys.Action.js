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
 * Ext.ametys.Action
 *
 * @class Ext.ametys.Action
 * @extends Ext.BoxComponent
 * @constructor
 * @param {Object} config Configuration options
 */
Ext.ametys.Action = function(config) 
{
	Ext.ametys.Action.superclass.constructor.call(this, config);
	
	this.addEvents(
			/**
		     * @event beforeclick
		     * Fires before click processing. Return false to cancel the default action.
		     * @param {Node} this This node
		     * @param {Ext.EventObject} e The event object
		    */
		    "beforeclick",
		    /**
		    * @event click
		    * Fires when this node is clicked
		    * @param {Node} this This node
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

Ext.extend(Ext.ametys.Action, Ext.BoxComponent, 
{
	cls: 'action-item',
	overFn : function(e)
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
	outFn : function(e)
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
	        var a = e.getTarget('a');
	        if(!this.disabled && a)
	        {
	            this.fireEvent("click", this.node, e);
		        e.preventDefault();
	            return;
	        }
	        e.preventDefault();
	    }
	    else
	    {
	        e.stopEvent();
	    }
	    
	    
	},
	onRender : function(ct, position)
	{
		Ext.ametys.Action.superclass.onRender.call(this, ct, position);
		
		if(!this.el) 
		{
			this.el = ct.createChild({
	            id: this.id,
	            cls: this.baseCls
	        }, position);
		}
		
		this.el.on('click', this.onClick, this);
		this.el.dom.innerHTML = this.html;
		this.el.hover(this.overFn, this.outFn, this);
	}
});