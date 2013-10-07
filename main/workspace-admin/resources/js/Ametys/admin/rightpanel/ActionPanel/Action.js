/*
 *  Copyright 2012 Anyware Services
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

/**
 * This class provides an action item. Do not use this constructor, use the <code>addAction</code> function on a {@link Ametys.admin.rightpanel.ActionPanel} instead
 * @private
 */
Ext.define('Ametys.admin.rightpanel.ActionPanel.Action', {
	extend: 'Ext.Component',
	cls: 'action-item',
	
	/**
	 * @private
	 */
    constructor: function (config) {
        this.callParent(arguments);

    	this.addEvents(
    			/**
    		     * @event beforeclick
    		     * Fires before click processing. Return false to cancel the default action.
    		     * @param {Ext.Component} this This node
    		     * @param {Event} e The event object
    		    */
    		    "beforeclick",
    		    /**
    		    * @event click
    		    * Fires when this node is clicked
    		    * @param {Ext.Component} this This node
    		    * @param {Event} e The event object
    		    */
    		    "click",
    		    /**
                 * @event mouseover
                 * Fires mouse over processing.
                 * @param {Event} e The event object
                 */
                "mouseover",
                /**
                 * @event mouseout
                 * Fires mouse out processing.
                 * @param {Event} e The event object
                 */
                "mouseout"
    	);
    },

	/**
	 * This function is called when the mouse hovers the action item
	 * @param e The event object
	 * @private
	 */
	_overFn: function(e) {
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

	/**
	 * This function is called when the mouse exits the action item
	 * @param e The event object
	 * @private
	 */
	_outFn: function(e) {
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

	/**
	 * This function is called when the action item is clicked
	 * @param e The event object
	 * @private
	 */
	_onClick: function(e) {
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

	onRender: function(ct, position) {
		this.callParent(arguments);
		
		if(!this.el) 
		{
			this.el = ct.createChild({
	            id: this.id,
	            cls: this.baseCls
	        }, position);
		}

		this.el.on('click', this._onClick, this);
		this.el.dom.innerHTML = this.initialConfig.html;
		this.el.hover(this._overFn, this._outFn, this);
		
		if (this.tooltip)
		{
	        new Ext.ToolTip({
	        		target: this.el,
	        		html: this.tooltip,
	        		width: 200,
	       	});
		}
	}
});
