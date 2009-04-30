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
 * Ext.ametys.ActionPanel
 *
 * @class Ext.ametys.ActionPanel
 * @extends Ext.BoxComponent
 * @constructor
 * @param {Object} config Configuration options
 */
Ext.ametys.ActionPanel = function(config) 
{
	Ext.ametys.ActionPanel.superclass.constructor.call(this, config);
	
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
		    "click"
	);
}; 

Ext.extend(Ext.ametys.ActionPanel, Ext.BoxComponent, 
{
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
		Ext.ametys.ActionPanel.superclass.onRender.call(this, ct, position);
		
		if(!this.el) 
		{
			this.el = ct.createChild({
	            id: this.id,
	            cls: this.baseCls
	        }, position);
		}
		
		this.el.on('click', this.onClick, this);
		this.el.dom.innerHTML = this.html;
	}
});