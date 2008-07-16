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
  * @fileoverview Static utilities constants and methods.
  */

/**
  * SUtilities is a static class with general contants and methods.
  * @version 1.0
  * @constructor
  */
function SUtilities() 
{
}

/**
  * This is a constant you have to set.<br/>
  * This constant is a prefix for all images used by the STools.<br/>
  * Must not end with '/'.
  * @type string
  */
SUtilities.IMAGE_PREFIX_URL = "";

/**
  * The user agent
  * @private
  * @type string
  */
SUtilities._agt = navigator.userAgent.toLowerCase();

/**
  * Boolean constant telling is user uses Internet Explorer or not.<br/>
  * This constant is used by all SUtilities, but you may have to use it also.
  * @type boolean
  */
SUtilities.IS_IE = ((SUtilities._agt.indexOf("msie") != -1) && (SUtilities._agt.indexOf("opera") == -1));

/**
  * This method is proposed for debug purposes.<br/>
  * Return all properties of an object.<br>
  * Exemple: alert( SUtilities.debug(document) )
  * @param {Object} elt Object to inspect.
  * @return a string containing all properties of elt.
  * @type string
  */
SUtilities.debug = function(elt)
{
	var s = elt + " / " + typeof elt + "\n";
	for (var a in elt)
		s += a + ", ";
	return s;
}

/**
  * Loads dinamically a css.
  * @param {string} url the url to the css to load.
  * @param {Document} _document the document where to load the css. Can be null: default value is main document.
  * @param {function} onLoadListener this function will be called when the css is correctly loaded. Can be null.
  */
SUtilities.loadStyle = function (url, _document, onLoadListener)
{
	_document = (_document == null) ? document : _document;
	
	var head = _document.getElementsByTagName("head")[0];
	var link = _document.createElement("link");
	link.rel = "stylesheet";
	link.href = url;
	link.type = "text/css";
	
	if (onLoadListener != null)
	{
		link.onload = onload;
		link.onreadystatechange = function () { if (/loaded|complete/.test(this.readyState)) this.onLoadListener(); }
	}
		
	head.appendChild(link);
}

/**
  * Loads dinamically a script.
  * @param {string} url the url to the script to load.
  * @param {Document} _document the document where to load the script. Can be null: default value is main document.
  * @param {function} onLoadListener this function will be called when the script is correctly loaded. Can be null.
  */
SUtilities.loadScript = function (url, _document, onLoadListener)
{
	_document = (_document == null) ? document : _document;
	
	var head = _document.getElementsByTagName("head")[0];
	var link = _document.createElement("script");
	link.src = url;
	if (onLoadListener != null)
	{
		link.onload = onLoadListener;
		link.onreadystatechange = function () { if (/loaded|complete/.test(this.readyState)) this.onLoadListener(); }
	}
	head.appendChild(link);
}

/**
  * Get the parent of an element that match a tagname.
  * @param {HTMLElement} elt The current html element.
  * @param {string} tag The tagname of the ancestor to obtain.
  */
SUtilities.getParentWithTag = function (elt, tag)
{	
	while (tag.toLowerCase() != elt.tagName.toLowerCase())
	{
		elt = elt.parentNode;
	}
	return elt;
}

/**
  * Get the children of a given html element that have a tagname.
  * @param {HTMLElement} elt The current html element.
  * @param {string} tag The tagname of the children to obtain.
  */
SUtilities.getDirectChildrenByTagName = function(elt, tag)
{
        var a = new Array();

        for (var i=0; elt.childNodes != null && i < elt.childNodes.length; i++)
        {
                var e = elt.childNodes[i];
                if (e.tagName && e.tagName.toLowerCase() == tag.toLowerCase())
                {
                  a.push(e);
                }
        }
        return a;
}

/**
  * Get the children, grand children... and all descendants of a given html element that have a tag name.
  * @param {HTMLElement} elt The current html element.
  * @param {string} tag The tagname of the descendants to obtain.
  */
SUtilities.getChildrenByTagName = function(elt, tag)
{
        var a = new Array();

        for (var i=0; elt.childNodes != null && i < elt.childNodes.length; i++)
        {
                var e = elt.childNodes[i];
                if (e.tagName && e.tagName.toLowerCase() == tag.toLowerCase())
                {
                  a.push(e);
                }
                
                var ap = SUtilities.getChildrenByTagName(e, tag)
                for (var j=0; j<ap.length; j++)
                        a.push(ap[j]);
        }
        return a;
}

/**
  * Attach an handle of an event (such as 'click' or 'mousemove') on an element
  * @param {HTMLElement} element The html element to put the event handler on
  * @param {string} event The name of the event to check ('click', 'dblclick', ...)
  * @param {function} handler The function to launch when the event is fired on the element.
  */
SUtilities.attachEvent = function(element, event, handler)
{  
	if (!SUtilities.IS_IE)   
	{ 
		element.addEventListener(event, handler, false);		  
	}   
	else if (element.attachEvent)   
	{ 
		element.attachEvent('on' + event, handler);  
	}   
}

/**
  * Detach an handle of an event (such as 'click' or 'mousemove') from an element
  * @param {HTMLElement} element The html element to remove the event handler from
  * @param {string} event The name of the event to check ('click', 'dblclick', ...)
  * @param {function} handler The function that was launched when the event is fired on the element.
  */
SUtilities.detachEvent = function(element, event, handler)
{  
	if (!SUtilities.IS_IE)   
	{ 
		element.removeEventListener(event, handler, false);		  
	}   
	else if (element.attachEvent)   
	{ 
		element.detachEvent('on' + event, handler);  
	}   
}

/**
  * Keep the left coordinate of the mouse.<br/>
  * Can be used as soon as an moseevent has been fired once.<br/>
  * Track the mouse only in the current document (not in frames)
  * @type int
  */
SUtilities.mouseX;

/**
  * Keep the top coordinate of the mouse
  * Can be used as soon as an moseevent has been fired once.<br/>
  * Track the mouse only in the current document (not in frames)
  * @type int
  */
SUtilities.mouseY;

/**
  * Watch for mouse position
  * @private
  */
SUtilities._onMouseMove = function (e)
{
  try
  {
	if (SUtilities.IS_IE) 
	{ 
		// for IE
		SUtilities.mouseY = window.event.clientY + document.body.scrollTop + document.documentElement.scrollTop;
		SUtilities.mouseX = window.event.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
	} 
	else if (e) 
	{ 
		// for Mozilla
		SUtilities.mouseY = e.pageY;
		SUtilities.mouseX = e.pageX;
	}
  }
  catch (e)
  {
    // happens some times at startup
  }
}
// Launch the watch for mousemove
if (!SUtilities.IS_IE) 
{
	 // Defines what events to capture for Navigator
	document.captureEvents(Event.MOUSEMOVE);
}
SUtilities.attachEvent(document, "mousemove", SUtilities._onMouseMove);