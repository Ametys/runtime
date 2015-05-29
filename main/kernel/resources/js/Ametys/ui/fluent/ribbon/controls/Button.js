/*
 *  Copyright 2013 Anyware Services
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
 * This class is a button in the ribbon tab. 3 sized are possible : 'large', 'small' and 'very-small'.
 */
Ext.define(
	"Ametys.ui.fluent.ribbon.controls.Button",
	{
		extend: "Ext.button.Button",
		alias: 'widget.ametys.ribbon-button',
		
		/**
		 * @cfg {String} scale The size of the button. Can be 'large', 'small' or 'very-small'.
		 * 'large' buttons have to be used directly in a Group. Icon size is 32x32.
		 * 'small' buttons have to be used in a GroupPart. Icon size is 16x16.
		 * 'very-small' buttons have to be used in a GroupPart or in a Toolbar. Icon size is 16x16.
		 */
		
		/**
		 * @cfg {String} iconAlign Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		/**
		 * @cfg {String} arrowAlign Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		/**
		 * @cfg {Number} height Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		/**
		 * @cfg {Number} width Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		/**
		 * @cfg {Boolean} frame Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		frame: false,
		
	    /**
	     * @cfg {Function} handler A function called when the button is clicked (can be used instead of click event).
	     */		
		
		
		/**
		 * @property {String} _rawText The text set, not modified. While the property text can have breakline or stuffs like this.
		 * @private
		 */
		/**
		 * @property {Boolean} _split True for a split button (menu and action at once)
		 * @private
		 */
		
		/**
		 * Creates a button for the ribbon
		 */
		constructor: function(config) 
		{
			if (config.scale == 'very-small')
			{
				config.text = null;
				config.scale = 'small';
				config.iconAlign = 'left';
				config.arrowAlign = 'right';
				config.height = 22;
			}
			else if (config.scale == 'small')
			{
				config.iconAlign = 'left';
				config.arrowAlign = 'right';
				config.height = 22;
			}
			else
			{
				config.scale = 'large';
				config.iconAlign = 'top';
				config.arrowAlign = 'bottom';
				config.height = 66;
			}

			this._split = (config.menu && (config.handler || config.enableToggle));
			
			this.callParent(arguments);
			
			if (this._split)
			{
				this.addCls('x-fluent-control-splitbutton-' + this.scale)
			}
            
            this.on('afterrender', this._onAfterRender, this);
		},
		
        /**
         * @private
         * Called after render
         */
		_onAfterRender: function()
		{
	        if (this.handleMouseEvents && this._split)
	        {
	            this.el.on("mousemove", this._onMouseMove, this);
	        }	
			
			this.setText(this.text);
		},
		
		/**
		 * Override default behaviour to determine if the mouse has clicked on the button or on the arrow
		 */
		onClick: function(e)
		{
		    e.preventDefault();
		    
		    if(!this.disabled)
		    {
		        if(this._split && this._isClickOnArrow(e) || !this._split && this.menu && !this.menu.isVisible() && !this.ignoreNextClick)
		        {
		            this.showMenu();

                    /**
                     * @event arrowclick
                     * Fires when the menu arrow was clicked
                     * @param {Ext.button.Button} button the current Button.
                     */
		            this.fireEvent("arrowclick", this, e);
		            
		            if(this.arrowHandler)
		            {
		                this.arrowHandler.call(this.scope || this, this, e);
		            }
		        }
		        else
		        {
		            if (this.enableToggle && (this.allowDepress !== false || !this.pressed))
		            {
		                this.toggle();
		            }

		            this.fireEvent("click", this, e);
		            if(this.handler)
		            {
		                this.handler.call(this.scope || this, this, e);
		            }
		        }
		    }
		},
		
		/**
		 * Listener on mouse move over the button, to determine if the mouse if over the button or the arrow 
		 * @param {Ext.event.Event} e The {@link Ext.event.Event} encapsulating the DOM event.
		 */
		_onMouseMove: function(e)
		{
			if (this._isClickOnArrow(e))
			{
				this.el.removeCls('x-fluent-control-splitbutton-' + this.scale);
				this.el.addCls('x-fluent-control-splitbutton-menu-' + this.scale);
			}
			else
			{
				this.el.addCls('x-fluent-control-splitbutton-' + this.scale);
				this.el.removeCls('x-fluent-control-splitbutton-menu-' + this.scale);
			}
		},
		
		/**
		 * This method determines if the passed event representing a click on a button, has hit the arrow or not
		 * @param {Ext.event.Event} e The {@link Ext.event.Event} encapsulating the DOM event.
		 * @returns {Boolean} True if the arrow was hit
		 * @private
		 */
		_isClickOnArrow: function(e)
		{
		    return this.arrowAlign != 'bottom' ?
		           e.getX() >= Math.max(this.btnInnerEl.getRight(), this.btnIconEl.getRight() + 2) :
		           e.getY() >= this.el.getTop() + 37;
		},
		
		getText: function()
		{
		    return this._rawText;
		},
		
		setText: function(text)
		{
			this._rawText = text;
			this.text = text;

			if(this.el)
		    {
		    	if (this.scale == 'large')
		    	{
		        	var textMesurer = Ext.create("Ext.util.TextMetrics", this.btnInnerEl);

		        	var minWidth = textMesurer.getWidth(this.text);
		        	var minText = this.text;
		        	var secondLineText = "";
		        	
		        	var menuOffset = this.menu ? "&#160;&#160;&#160;" : "";

		        	var nextIndex = this.text.indexOf(' ', 0);
		        	while (nextIndex != -1 && nextIndex < this.text.length)
		        	{
		        		var tmpSecondLineText = this.text.substring(nextIndex + 1) + menuOffset;
		        		var testText = this.text.substring(0, nextIndex) + '<br/>' + tmpSecondLineText;
		        		var testWidth = textMesurer.getWidth(testText);
		        		
		        		if (testWidth < minWidth)
		        		{
		        			minWidth = testWidth;
		        			minText = testText;
		        			secondLineText = tmpSecondLineText;
		        		}
		        		
		        		nextIndex = this.text.indexOf(' ', nextIndex + 1);
		        	}
		        	
		        	while (minWidth < 32)
		        	{
		        		var brpos = minText.indexOf('<br/>'); 
		        		if (brpos == -1)
		        		{
		        			minText = "&#160;" + minText + "&#160;";
		        		}
		        		else
		        		{
		        			minText = "&#160;" + minText.substring(0, brpos) + "&#160;" + "<br/>" + "&#160;" + minText.substring(brpos + 5) + "&#160;";
		        		}
		        		minWidth = textMesurer.getWidth(minText);
		        	}
		        	
		        	this.text = minText;

		            if (this.menu)
		            {
		            	var secondLineWidth = textMesurer.getWidth(secondLineText);
		            	
		            	if (secondLineWidth != 0)
		            	{
		            		// -9 for the 3 spacings
		                	var posX = minWidth / 2 + secondLineWidth / 2 - 8;

		                	if (this.btnInnerEl.getStyle("background-position-x") != undefined)
		                	{
		                		this.btnInnerEl.setStyle("background-position-x", posX + "px");
		                	}	
		                	else
		                	{	
		                		var currentStyle = this.btnInnerEl.getStyle("background-position")
		                		var topPosition = currentStyle.substring(currentStyle.indexOf(' '));
		                		this.btnInnerEl.setStyle("background-position", posX + "px" + topPosition);
		                	}
		            	}
		            }
		    	}
		    	else if (this.text == null)
		    	{
		    		this.text = '&#160;';

		    		this.el.addCls("x-btn-very-small");
		    	}
		        
		    	this.btnInnerEl.update(this.text || '&#160;');
                if (Ext.isStrict && Ext.isIE8) {
                    // weird repaint issue causes it to not resize
                	this.el.repaint();
                }
                this.updateLayout();
		    }
		},
		
		/**
		 * Set the button in a refreshing state. See #stopRefreshing
		 */
		refreshing: function ()
		{
			this.addCls('x-fluent-loading');
		},

		/**
		 * Stop the refreshing state. See #stopRefreshing
		 */
		stopRefreshing: function ()
		{
			this.removeCls('x-fluent-loading');
		}
	}
);
