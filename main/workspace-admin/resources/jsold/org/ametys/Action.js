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
 * org.ametys.Action
 *
 * @class This class provides an action item. Do not use this constructor, use the <code>addAction</code> function on a {@link org.ametys.ActionsPanel} instead
 * @extends Ext.BoxComponent
 * @constructor
 * @param {Object} config Configuration options
 */
org.ametys.Action = function(config) 
{
	org.ametys.Action.superclass.constructor.call(this, config);
	
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

Ext.extend(org.ametys.Action, Ext.BoxComponent, 
{
	cls: 'action-item'
});


/**
 * This function is called when the mouse hovers the action item
 * @param e The event object
 * @private
 */
org.ametys.Action.prototype._overFn = function(e)
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
 * This function is called when the mouse exits the action item
 * @param e The event object
 * @private
 */
org.ametys.Action.prototype._outFn = function(e)
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
 * This function is called when the action item is clicked
 * @param e The event object
 * @private
 */
org.ametys.Action.prototype._onClick = function(e)
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
}

org.ametys.Action.prototype.onRender = function(ct, position)
{
	org.ametys.Action.superclass.onRender.call(this, ct, position);
	
	if(!this.el) 
	{
		this.el = ct.createChild({
            id: this.id,
            cls: this.baseCls
        }, position);
	}
	
	this.el.on('click', this._onClick, this);
	this.el.dom.innerHTML = this.html;
	this.el.hover(this._overFn, this._outFn, this);
}
