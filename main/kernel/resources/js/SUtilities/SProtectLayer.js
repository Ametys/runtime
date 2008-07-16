/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
 
/**
  * @fileoverview Invisible layer to prevent the user to interact with all objects under a given zIndex.
  */

/**
  * SProtectLayer is a class that can create an invisible layer you can put over background stuffs.<br/>
  * While the protection layer is displayed, only foreground stuffs will be accessible.<br/>
  * <br/>
  * Basic usage:<br/>
  * This class is usefull when you open a menu, and want to know when the user clic outsite the menu (to close it).<br/>
  * Idem for modal dialog boxes, that can blink when user clicks outside.<br/>
  * Idem for tips that will diseapears when user click outside.<br/>
  * <br/>
  * Works with:<br/>
  * <ul>
  * <li>IE 6.0</li>
  * <li>Moz 1.7</li>
  * <li>FF 1.0</li>
  * </ul>
  * <br/>
  * Limitations:<br/>
  * <ul>
  * <li>IE 6.0: 'Select' objects disapears when the layer is displayed.</li>
  * </ul>
  * @requires SUtilities This class requires that the SUtilities class to be loaded before.
  * @version 1.0
  * @constructor
  */
function SProtectLayer()
{
	this._instance = SProtectLayer._instance++;

	this._create();
}

/**
  * The number of instance.
  * @private
  * @type int
  */
SProtectLayer.prototype._instance = 0;

/**
  * The current z-index of the internal frame.
  * @private
  * @type int
  */
SProtectLayer.prototype._level = 10;

/**
  * The instance of document where layer is available.
  * @private
  * @type Document
  */
SProtectLayer.prototype._document = document;

/**
  * The association "event: handler".
  * @private
  * @type map<string, function>
  */
SProtectLayer.prototype._event = {};

/**
  * The internal transparent iframe used as layer.
  * @private
  * @type IFrame
  */
SProtectLayer.prototype._iframe = null;

/** 
  * Number of instances created.<br/>
  * Allow each instance to have a unique id.
  * @private 
  * @type int
  */
SProtectLayer._instance = 0;

/** 
  * The visible instances.<br/>
  * The key is the number of instance.<br/>
  * A special member 'length' is added.
  * @private 
  * @type Map<int, SProtectLayer>
  */
SProtectLayer._visibleInstances = {};
SProtectLayer._visibleInstances.length = 0;

/**
  * The current highest instance of layer.<br/>
  * @private
  * @type SProtectLayer
  */
SProtectLayer._highestInstance = null;

/**
  * Set the z index of the protection (default is 10).<br/>
  * Applyed only at show time.
  * @param {int} level The level to set
  */
SProtectLayer.prototype.setLevel = function(level)
{
	this._level = level;
}

/**
  * Set a listener for an event.
  * @param {string} event The name of the event to listen among:
  * <ul><li>"onclick",</li><li>"ondblclick",</li><li>"onmousedown",</li><li>"onmousemove",</li><li>"onmouseover",</li><li>"onmouseout",</li><li>"onmouseup",</li><li>"onfocus",</li><li>"onshow",</li><li>"onhide",</li><li>"onresize".</li></ul>
  * @param {function} listener The function to call if corresponding event is fired
  */
SProtectLayer.prototype.setEventListener = function(event, listener)
{
  this._event[event] = listener;

  switch (event)
  {
    case "onresize":
    case "onhide":
    case "onshow":
      this._event[event] = listener;
      break;
    case "onfocus":
      if (SUtilities.IS_IE) this._iframe.onfocus = listener;
      else this._iframe.contentWindow.onfocus = listener;
      break;
    case "onclick":
      SUtilities.attachEvent(this._iframe.contentWindow.document.getElementById('alone'), "click", listener);
      break;
    case "ondblclick":
      SUtilities.attachEvent(this._iframe.contentWindow.document.getElementById('alone'), "dblclick", listener);
      break;
    case "onmousedown":
      SUtilities.attachEvent(this._iframe.contentWindow.document.getElementById('alone'), "mousedown", listener);
      break;
    case "onmousemove":
      SUtilities.attachEvent(this._iframe.contentWindow.document.getElementById('alone'), "mousemove", listener);
      break;
    case "onmouseover":
      SUtilities.attachEvent(this._iframe.contentWindow.document.getElementById('alone'), "mouseover", listener);
      break;
    case "onmouseout":
      SUtilities.attachEvent(this._iframe.contentWindow.document.getElementById('alone'), "mouseout", listener);
      break;
    case "onmouseup":
      SUtilities.attachEvent(this._iframe.contentWindow.document.getElementById('alone'), "mouseup", listener);
      break;
  }
}

/**
  * Destroy the internal iframe.<br/>
  * Do not call, if you want to use the layer after.
  * @private
  */
SProtectLayer.prototype._dispose = function()
{
	if (this._iframe != null)
	{
        if (this._event["onclick"] != null)
          SUtilities.detachEvent(this._iframe.contentWindow.document.getElementById('alone'), "click", this._event["onclick"]);
        if (this._event["ondblclick"] != null)
          SUtilities.detachEvent(this._iframe.contentWindow.document.getElementById('alone'), "dblclick", this._event["ondblclick"]);
        if (this._event["onmousedown"] != null)
          SUtilities.detachEvent(this._iframe.contentWindow.document.getElementById('alone'), "mousedown", this._event["onmousedown"]);
        if (this._event["onmousemove"] != null)
          SUtilities.detachEvent(this._iframe.contentWindow.document.getElementById('alone'), "mousemove", this._event["onmousemove"]);
        if (this._event["onmouseover"] != null)
          SUtilities.detachEvent(this._iframe.contentWindow.document.getElementById('alone'), "mouseover", this._event["onmouseover"]);
        if (this._event["onmouseout"] != null)
          SUtilities.detachEvent(this._iframe.contentWindow.document.getElementById('alone'), "mouseout", this._event["onmouseout"]);
        if (this._event["onmouseup"] != null)
          SUtilities.detachEvent(this._iframe.contentWindow.document.getElementById('alone'), "mouseup", this._event["onmouseup"]);

		if (SUtilities.IS_IE) this._iframe.onfocus = null;
		else this._iframe.contentWindow.onfocus = null;
		
		this._event = {};
		
		this._iframe.parentNode.removeChild(this._iframe);
		delete SProtectLayer._visibleInstances[this._instance];
		SProtectLayer._visibleInstances.length--;
		this._iframe = null;
	}
}
/**
  * Create the internal iframe.
  * @private
  */
SProtectLayer.prototype._create = function()
{
	this._dispose();
	
	this._iframe = this._document.createElement('iframe');
	this._iframe.frameBorder = "0";
	this._iframe.scrolling = "no";
	this._iframe.style.cssText = "display: none; filter:alpha(opacity=0);-moz-opacity:.0;opacity:.0;-moz-user-focus: normal; position: absolute; top: 0px; left: 0px; width: 0px; height: 0px;";

	this._document.body.insertBefore(this._iframe, this._document.body.childNodes[0]);
    var _document = this._iframe.contentWindow.document;
	_document.open();
	_document.write("<html><body style='padding: 0px; margin: 0px;'><div id='alone' style='height: 100%; background-color: #ffffff'></div></body></html>");
	_document.close();
}

/**
  * Dynamicaly change the opacity.
  * @param {int} value The opacity value between 0 and 100. 0 is default
  */
SProtectLayer.prototype.setOpacity = function(value)
{
	if (SUtilities.IS_IE)
	{
		this._iframe.filters.item('alpha').opacity = value;
	}
	else
	{
		this._iframe.style.opacity = value/100;
	}
}

/**
  * Dynamicaly set a style attribute.
  * @param {string} attribute The javascript name of a css attribute. Javascript name means no space or -: z-index becomes zIndex.<br/>
  * @param {string} value The value to set.
  */
SProtectLayer.prototype.setStyle = function(attribute, value)
{
	if (/height/i.test(attribute)) 
		return;
	this._iframe.contentWindow.document.getElementById('alone').style[attribute] = value;
}

/**
  * Hide the protection.<br/>
  * Fire the onhide event.<br/>
  */
SProtectLayer.prototype.hide = function()
{
	if (this._iframe.style.display == "none")
	{
		// already hidden
		return;
	}

	// fires onhide event
	if (this._event.onhide != null)
		this._event.onhide();
		
	delete SProtectLayer._visibleInstances[this._instance];
	SProtectLayer._visibleInstances.length--;
	
	if (SProtectLayer._visibleInstances.length == 0)
		SProtectLayer._uncheckEvents();
	
	this._iframe.style.display = "none";
	
	// compute the highestInstance and the highestLevel
	SProtectLayer._computeHighestInstance();
}

/**
  * Display the protection.<br/>
  * Fires the onshow event.<br/>
  * When displayed the layer is resized.<br/>
  * The focus is given to the layer.
  */
SProtectLayer.prototype.show = function()
{
	if (this._iframe.style.display == "block")
	{
		// already shown
		return;
	}
	
	// fires onshow event
	if (this._event.onshow != null)
	{
		this._event.onshow();
	}
		
	if (SProtectLayer._visibleInstances.length == 0)
	{
		SProtectLayer._checkEvents();
	}

	// register the instance as visible	
	SProtectLayer._visibleInstances[this._instance] = this;
	SProtectLayer._visibleInstances.length++;

	this._resize();
	this._scroll();
	this._iframe.style.zIndex = this._level;
	
	this._iframe.style.display = "block";
	this._iframe.contentWindow.focus();
	
	// compute the highestInstance and the highestLevel
	SProtectLayer._computeHighestInstance();
}

/**
  * This method compute the highest visible instance of layer.
  * @private
  */
SProtectLayer._computeHighestInstance = function()
{
	var highestLevel = 0;
	SProtectLayer._highestInstance = null;
	for (var i in SProtectLayer._visibleInstances)
	{
		if (i == "length")
		{
			continue;
		}
		
		var instance = SProtectLayer._visibleInstances[i];
		
		if (instance._level > highestLevel)
		{
			highestLevel = instance._level;
			SProtectLayer._highestInstance = instance;
		}
	}	
}

/**
  * This method starts the handling of events
  * @private
  */
SProtectLayer._checkEvents = function()
{
	if (SUtilities.IS_IE)
	{
		window.attachEvent("onresize", SProtectLayer._onResize);
		window.attachEvent("onscroll", SProtectLayer._onScroll);
		document.attachEvent("onactivate", SProtectLayer._onFocusChanged);
	}
	else
	{
		window.addEventListener("resize", SProtectLayer._onResize, false);
		document.addEventListener("focus", SProtectLayer._onFocusChanged, true);
	}
}

/**
  * This method stops the handling of events
  * @private
  */
SProtectLayer._uncheckEvents = function()
{
	if (SUtilities.IS_IE)
	{
		window.detachEvent("onresize", SProtectLayer._onResize);
		window.detachEvent("onscroll", SProtectLayer._onScroll);
		document.detachEvent("onactivate", SProtectLayer._onFocusChanged);
	}
	else
	{
		window.removeEventListener("resize", SProtectLayer._onResize, false);
		document.removeEventListener("focus", SProtectLayer._onFocusChanged, true);
	}
}

/**
  * This method resize the layer to the current window size.<br/>
  * Fires the onresize event.
  * @private
  */
SProtectLayer.prototype._resize = function()
{
	if (this._event.onresize != null)
		this._event.onresize();

	if (SUtilities.IS_IE)
	{		
		// IE can not set an iframe size higher than the screen max size in both direction
		// We set it to the current window size, and will move it with _onScroll
		
		var horizontalScrollBar = (this._document.body.scrollWidth > this._document.body.offsetWidth);
		var verticalScrollBar = true;

		var width = parseInt(this._document.body.offsetWidth) + (verticalScrollBar ? -21 : -4);
		var height = parseInt(this._document.body.offsetHeight) + (horizontalScrollBar ? -21 : -4);
	}
	else
	{
		var width = parseInt(this._document.body.scrollWidth) - 4;
		var height = parseInt(this._document.body.scrollHeight) - 4;
	}

	this._iframe.style.width = width + "px";
	this._iframe.style.height = height + "px";
}

/**
  * This method set the layer's position to the scrollbars.<br/>
  * Used only for IE, because iframe size is the screen size and not the content window size.
  * @private
  */
SProtectLayer.prototype._scroll = function()
{
	if (SUtilities.IS_IE)
	{
		this._iframe.style.top = this._document.body.scrollTop;
		this._iframe.style.left = this._document.body.scrollLeft;
	}
}

/**
  * This method is an event handler.<br/>
  * Called when the window is resized if at least one instance of SProtectLayer is displayed.<br/>
  * Resized all visibles layers by calling <code>onResizeAfter</code> in another thread.
  * @private
  */
SProtectLayer._onResize = function()
{
	for (var i in SProtectLayer._visibleInstances)
	{
		if (i == "length")
			continue;
		var instance = SProtectLayer._visibleInstances[i];
		instance._iframe.style.display = 'none';						
	}
	
	// delay the real resize, so that current 'visible' protection does not affect true size
	window.setTimeout(SProtectLayer._onResizeAfter, 1);
}

/**
  * This method call <code>_resize</code> on each displayed layer.<br/>
  * It is called by the handler of the onresize event: _onResize, but in a different thread.
  * @private
  */
SProtectLayer._onResizeAfter = function()
{
	for (var i in SProtectLayer._visibleInstances)
	{
		if (i == "length")
			continue;
		var instance = SProtectLayer._visibleInstances[i];
		instance._resize();						
		instance._iframe.style.display = 'block';
	}
}

/**
  * This method is an event handler (only permtinent for IE).<br/>
  * Called when the window is scrolled if at least one instance of SProtectLayer is displayed.<br/>
  * Set the position of all visibles layers.
  * @private
  */
SProtectLayer._onScroll = function()
{
	for (var i in SProtectLayer._visibleInstances)
	{
		if (i == "length")
			continue;
		var instance = SProtectLayer._visibleInstances[i];
		instance._scroll();
	}
}

/**
  * This method is an event handler.<br/>
  * Called when the focus change.<br/>
  * Put it back to the highest layer.
  * @private
  */
SProtectLayer._onFocusChanged = function(e)
{
	if (SProtectLayer._highestInstance == null)
		return;

	var highestLevel = SProtectLayer._highestInstance._iframe.style.zIndex;

        var element = SUtilities.IS_IE ? window.event.srcElement : e.target;
        if (element == null || element.tagName == null)
        	return;

        var elementIsBody;
        var elementIsAbsolutlyPositionned;
        var elementZIndex;

         do
         {
                 elementIsBody = /^body|html$/i.test(element.tagName);
                 elementIsAbsolutlyPositionned = element.style != null && /absolute/i.test(element.style.position);
                 elementZIndex = element.style != null && element.style.zIndex;

                 if (elementIsBody || (elementIsAbsolutlyPositionned && elementZIndex != null))
                 {
                 	if (elementIsBody || elementZIndex < highestLevel)
                 	{
                 		SProtectLayer._uncheckEvents(); 
                 		SProtectLayer._highestInstance._iframe.contentWindow.focus(); 
                 		SProtectLayer._checkEvents();
                 	}
                        return;
                 }

                 element = element.parentNode;
         } while (!elementIsBody && !elementIsAbsolutlyPositionned && elementZIndex < highestLevel);	
}

// Test for SUtilities presence
try 
{
	SUtilities;
} 
catch (e)
{
	alert("SProtectLayer needs SUtilities to be imported.");
}
