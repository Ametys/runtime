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

Ext.namespace('org.ametys.runtime');

org.ametys.runtime.HomePage = function ()
{
}

org.ametys.runtime.HomePage.TOP_HEIGHT = 90;
org.ametys.runtime.HomePage.FOOTER_HEIGHT = 35;
org.ametys.runtime.HomePage.PADDLE_WIDTH = 44;
org.ametys.runtime.HomePage.MAIN_WIDTH = 1018;
org.ametys.runtime.HomePage.CONTENT_WIDTH = 930;

/**
 * Draw the center panel
 * @private
 */
org.ametys.runtime.HomePage._drawCenterPanel = function ()
{
	this._contentCenter = new org.ametys.HtmlContainer( {
		region :'center',
		layout :'border',
		
		id :'content_center',
		width: org.ametys.runtime.HomePage.CONTENT_WIDTH,
		
		items : [org.ametys.runtime.HomePage._drawTopPanel (),
		         org.ametys.runtime.HomePage._drawMainPanel (), 
		         org.ametys.runtime.HomePage._drawBottomPanel ()]
	});
	
	this._centerPanel = new org.ametys.HtmlContainer ({
		layout :'border',
		region :'center',
		
		id :'wrapper',
		width: org.ametys.runtime.HomePage.MAIN_WIDTH,
		
		items : [new org.ametys.HtmlContainer({
					region :'west',
					width: org.ametys.runtime.HomePage.PADDLE_WIDTH,
					id :'content_left'
				 }),
				 this._contentCenter,
				 new org.ametys.HtmlContainer({
						region :'east',
						width: org.ametys.runtime.HomePage.PADDLE_WIDTH,
						id :'content_right'
				 })]
	})
	
	return this._centerPanel;
}


/**
 * Draw the right panel
 * @private
 */
org.ametys.runtime.HomePage._drawRightPanel = function ()
{
	this._rightPanel = new org.ametys.HtmlContainer ({
		region: 'east',
		id :'column-left'
	});
	
	return this._rightPanel;
}

/**
 * Draw the left panel
 * @private
 */
org.ametys.runtime.HomePage._drawLeftPanel = function ()
{
	this._leftPanel = new org.ametys.HtmlContainer ({
		region: 'west',
		id :'column-right'
	});
	
	return this._leftPanel;
}

/**
 * Draw the top panel containing the logo Ametys
 * @return The top panel
 * @private
 */
org.ametys.runtime.HomePage._drawTopPanel = function ()
{
	// TODO method to change the top background image
	this._topPanel = new org.ametys.HtmlContainer({
		region :'north',
		id :'top',
		height: org.ametys.runtime.HomePage.TOP_HEIGHT,
		items : [ new org.ametys.HtmlContainer({
		    	   id :'logo'
		       })
		]
	});
	
	return this._topPanel;
}

/**
 * Draw the main panel
 * @private
 */
org.ametys.runtime.HomePage._drawMainPanel = function ()
{
	this._mainPanel = new org.ametys.HtmlContainer( {
		region :'center',
		
		layout: 'fit',
		autoScroll: false,
		
		id :'main',
		
		items : [org.ametys.runtime.HomePage.createPanel ()
		]
	});
	
	return this._mainPanel;
}

/**
 * Draw the bottom panel
 * @private
 */
org.ametys.runtime.HomePage._drawBottomPanel = function ()
{
	this._bottomPanel = new org.ametys.HtmlContainer({
		region :'south',
		height: org.ametys.runtime.HomePage.FOOTER_HEIGHT,
		id :'footer',
		items : [new org.ametys.HtmlContainer({
		        	 id: 'box',
		        	 items: [org.ametys.runtime.HomePage.drawFooterPanel ()]
		         })
		]
	});
	
	return this._bottomPanel;
}

/**
 * Override this function to draw your own center panel
 * @return {Ext.Component} the created panel
 */
org.ametys.runtime.HomePage.createPanel = function ()
{
	this._panel = new org.ametys.HtmlContainer ({
		html: '<p><i>Override the <b>org.ametys.runtime.HomePage.createPanel</b> function to create your own panel here ...</i></p>'
	});
	
	return this._panel;
}


/**
 * Override this function to draw your own footer panel
 * @return {Ext.Component} the footer panel
 */
org.ametys.runtime.HomePage.drawFooterPanel = function ()
{
	return new org.ametys.HtmlContainer ({
		html: ''
	});
}

function onreadyfunction() 
{
	org.ametys.runtime.HomePage._view = new Ext.Viewport({
		
		id: 'viewport-home',
		
		layout :'border',
		
		items : [ org.ametys.runtime.HomePage._drawCenterPanel(),
		          org.ametys.runtime.HomePage._drawRightPanel(),
		          org.ametys.runtime.HomePage._drawLeftPanel()
		],
		
		listeners : {
			'resize' : function (vp, adjWidth, adjHeight, rawWidth, rawHeight) {
		  		var width = adjWidth;
		  		if (width == null || width == 'auto') {
		  			width = this.getSize()['width'];
		  		}
		  		var columnWidth = (width - org.ametys.runtime.HomePage.MAIN_WIDTH) / 2;
		  		org.ametys.runtime.HomePage._leftPanel.setWidth(columnWidth);
		  		org.ametys.runtime.HomePage._rightPanel.setWidth(columnWidth);
			}
		}
		
	});

	Ext.QuickTips.init();
	
	/** IE 6*/
	pngFixFn();
}

Ext.onReady(onreadyfunction);

function pngFixFn() 
{
	if (window.pngFix)
	{
		pngFix.fixAllByTagName(["img", "div"]);
	}
}
