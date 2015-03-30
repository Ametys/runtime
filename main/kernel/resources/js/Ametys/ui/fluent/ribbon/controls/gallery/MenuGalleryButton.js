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
 * This class is a button in a menu gallery
 */
Ext.define(
	"Ametys.ui.fluent.ribbon.controls.gallery.MenuGalleryButton",
	{
		extend: "Ext.button.Button",
		alias: 'widget.ametys.ribbon-menugallerybutton',
		
		/**
		 * @cfg {Boolean} inribbon Is for a inribbon gallery 
		 * @private
		 */		

		/**
		 * @cfg {String} scale Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		scale: 'large',
			
		/**
		 * @cfg {String} iconAlign Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		iconAlign: 'top',
		
		/**
		 * @cfg {Number} width Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		width: 72,
		/**
		 * @cfg {Number} height Doesn't apply to ribbon element. The value HAS TO be the default value.
		 * @private
		 */
		
		/**
		 * @property {String} _rawText The text set, not modified. While the property text can have breakline or stuffs like this.
		 * @private
		 */
		
		constructor: function(config)
		{
			config.height = config.inribbon ? 56 : 70;
			
			this.callParent(arguments);
		},
		
		onRender: function()
		{
			this.callParent(arguments);
			
			this.setText(this.text);
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
	        	var textMesurer = Ext.create("Ext.util.TextMetrics", this.btnInnerEl);

	        	var maxWidth = this.width - 2;
	        	var minWidth = textMesurer.getWidth(this.text);
	        	var minText = this.text;
	        	var firstLineText = "";
	        	var secondLineText = "";
	        	
	        	if (minWidth > maxWidth)
	        	{
		        	var nextIndex = this.text.indexOf(' ', 0);
		        	while (nextIndex != -1 && nextIndex < this.text.length && !this.inribbon)
		        	{
		        		var tmpFirstLineText = this.text.substring(0, nextIndex);
		        		var tmpSecondLineText = this.text.substring(nextIndex + 1);
		        		
		        		var firstLineWidth = textMesurer.getWidth(tmpFirstLineText);
		        		var secondLineWidth = textMesurer.getWidth(tmpSecondLineText);
	
		        		// Test if first line fits
		        		if (firstLineWidth <= maxWidth)
		        		{
		        			// Test if curent tmp string are the best
		        			if (firstLineWidth < minWidth && secondLineWidth < minWidth)
		        			{
		        				// Depends if second line does not fit
		        				if (secondLineWidth > maxWidth)
		        				{
		        					minWidth = maxWidth;
		        					minText = tmpFirstLineText + "<br/>" + textMesurer.ellipseText(tmpSecondLineText, maxWidth);
		        					break;
		        				}
		        				else
		        				{
		        					minWidth = Math.max(firstLineWidth, secondLineWidth);
		        					minText = tmpFirstLineText + "<br/>" + tmpSecondLineText;
		        				}
		        			}
		        		}
		        		else
		        		{
		        			break;
		        		}
		        		
		        		nextIndex = this.text.indexOf(' ', nextIndex + 1);
		        	}
		        	
		        	if (minWidth > maxWidth)
		        	{
	        			// Truncated text to maxWidth pixels
	        			minText = textMesurer.ellipseText(this.text, maxWidth);
		        	}
	        	}
	        	
	        	this.text = minText;

		    	this.btnInnerEl.update(this.text || '&#160;');
                if (Ext.isStrict && Ext.isIE8) {
                    // weird repaint issue causes it to not resize
                	this.el.repaint();
                }
                this.updateLayout();
		    }
		}
	}		
);
