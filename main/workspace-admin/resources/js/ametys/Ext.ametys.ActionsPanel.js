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
 * Ext.ametys.ActionsPanel
 *
 * @class Ext.ametys.ActionsPanel
 * @extends Ext.Panel
 * @constructor
 * @param {Object} config Configuration options
 */
Ext.ametys.ActionsPanel = function(config) 
{
	Ext.ametys.ActionsPanel.superclass.constructor.call(this, config);
}; 

Ext.extend(Ext.ametys.ActionsPanel, Ext.Panel, 
{
	autoDestroy: false,
	collapsible: false,
	cmargins: '5 0 0 0',
	awtCls : 'actions-panel',
	cls: 'actions-panel',
	actions : [],
	elements: 'body,footer',
	
	/**
     * Adds an action (see <code>Ext.ametys.ActionPanel</code> to this panel.  Note that this method must be called prior to rendering.  The preferred
     * approach is to add buttons via the {@link #buttons} config.
     * @param {String} text The text of the action
     * @param {String} icon The absolute url of the icon. Can be null
     * @param {Function} act The function to be called on click event 
     */
	addAction : function (text, icon, act) 
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
    
	    var action = new Ext.ametys.Action ({ 
			border: false,
			html : span.innerHTML,
			listeners: {"click" : act},
			icon : icon,
			iconOver : icon.substring(0, icon.indexOf('.')) +  '-over.png'
		});
	    
		this.add(action);
		this.actions.push(action);
	},
	/**
	 * Hide the action to the position argument
	 */
	hideElt : function (position) 
	{ 
		var act = this.actions[position];
		if(act != null)
		{
			act.setVisible(false);
		}
	},
	/**
	 * Show the action to the position argument
	 */
	showElt : function (position) 
	{ 
		var act = this.actions[position];
		if(act != null)
		{
			act.setVisible(true);
		}
	},
	onRender : function(ct, position)
	{
		Ext.ametys.ActionsPanel.superclass.onRender.call(this, ct, position);
		
		this.header.addClass(this.awtCls + '-header');
		this.body.addClass(this.awtCls + '-body');
		
		if (this.footer)
		{
			this.footer.addClass(this.awtCls + '-footer');
		}
	}
});