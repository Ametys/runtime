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
 * org.ametys.DesktopItem
 *
 * @class This class handles an item of the desktop
 * @extends Ext.BoxComponent
 * @constructor
 * @param {Object} config Configuration options
 * @example
 * var item = new org.ametys.DesktopItem ({
 *	text: "Mot de passe",
 *	desc: "Modifier le mot de passe du compte 'admin'",
 *	icon: '/plugins/core/resources/img/administrator/password/icon_large.png',
 *	iconOver: '/plugins/core/resources/img/administrator/password/icon_large_over.png', 
 *	plugin : 'core',
 *	actionFunction : RUNTIME_Plugin_Runtime_Administrator_Password.act,
 *	actionParams : {}
 * });
 */
org.ametys.DesktopItem = function(config) 
{
	org.ametys.DesktopItem.superclass.constructor.call(this, config);
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

Ext.extend(org.ametys.DesktopItem, Ext.BoxComponent, 
{
	cls: 'desktop-item'
});

/**
 * This function is called when the mouse hovers the item
 * @param e The event object
 * @private
 */
org.ametys.DesktopItem.prototype._onMouseOver = function(e)
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
org.ametys.DesktopItem.prototype._onMouseOut = function(e)
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
org.ametys.DesktopItem.prototype._onClick = function(e)
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

org.ametys.DesktopItem.prototype.onRender = function(ct, position)
{
	org.ametys.DesktopItem.superclass.onRender.call(this, ct, position);
	
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
        html: org.ametys.DesktopHelper.createDesktopItemTooltip (this.text, this.desc),
        shadow : false
    });
	//this.el.addClassOnOver(this.overCls);
}

// -----------------------------------------------------
//-----------------------------------------------------
org.ametys.DesktopHelper = function ()
{
}

org.ametys.DesktopHelper.createDesktopItemTooltip = function (title, text)
{
	return "<div class='desktop-item-tooltip'>" 
		+ (title ? "<div class='desktop-item-tooltip-title'>" + title + "</div>" : "")
		+ "<div class='desktop-item-tooltip-text'>"
		+    text 
		+ "</div>"
		+ "</div>";
}