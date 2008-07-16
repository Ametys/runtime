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
  * @fileoverview Display a message that will disapear on a click or after a given time.<br/>
  * Can be attached to an object to show up automaticaly.
  */

/**
  * STooltip is a class that can create a tooltip message (generaly for help purposes).<br/>
  * <br/>
  * Basic usage:<br/>
  * This class is usefull when you want to provide information on an element when the user put his mouse over it.<br/>
  * Idem when you do not have enough space to write something, you can show it after a short delay.
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
  * <li>Under Windows OS, tooltip stay behind combobox</li>
  * </ul>
  * <br/>
  * @requires SUtilities This class requires that the SUtilities class to be loaded before.
  * @version 1.0
  * @constructor
  */
function STooltip()
{
	this._instance = STooltip._instance++;
	STooltip._instances[this._instance] = this;
	
	this._create();
}

/**
  * The instance of document where tooltip is available.
  * @private
  * @type Document
  */
STooltip.prototype._document = document;

/** 
  * Number of instances created.<br/>
  * Allow each instance to have a unique id.
  * @private 
  * @type int
  */
STooltip._instance = 0;

/** 
  * All tooltip instances.<br/>
  * The key is the number of instance.<br/>
  * @private 
  * @type Map<int, STooltip>
  */
STooltip._instances = {};

/**
  * The number of instance.
  * @private
  * @type int
  */
STooltip.prototype._instance = 0;

/**
  * The z-index the tooltip will use on next show.<br/>
  * @private
  * @type int
  */
STooltip.prototype._level = 20;

/**
  * The association "event: handler".
  * @private
  * @type map<string, function>
  */
STooltip.prototype._event = {};

/**
  * The time (in ms) the tooltip will stay visible.<br/>
  * The constant STooltip.POPUP_TIME is used if _popuptime is null.
  * @private
  * @type int
  */
STooltip.prototype._popuptime = null;

/**
  * The time (in ms) the tooltip will wait before appearing.<br/>
  * The constant STooltip.WAIT_TIME is used if _popuptime is null.
  * @private
  * @type int
  */
STooltip.prototype._waittime = null;

/**
  * The current timer (if shown with autohide).
  * @private
  * @type int
  */
STooltip.prototype._popupTimeout = null;

/**
  * When the tooltip is popuping up, keeps a pointer on the element.
  * @private
  * @type HTMLElement
  */
STooltip.prototype._popupElement = null;

/**
  * The html element root of the tooltip.
  * @private
  * @type HTMLElement
  */
STooltip.prototype._tooltip = null;

/**
  * The html element over of the tooltip.
  * @private
  * @type HTMLElement
  */
STooltip.prototype._overTooltip = null;

/**
  * The time of the last popdown. Used to know if wait_time should be wait
  * @private
  * @type int
  */
STooltip._lastPopdown = null;

/**
  * This constant determines the time (in ms) tooltips will stay visible on an automatic popup.<br/>
  * Default value is 5000 ms.
  * @type int
  */
STooltip.POPUP_TIME = 5000;

/**
  * This constant determines the time (in ms) tooltips will wait before appearing.<br/>
  * Default value is 500 ms.
  * @type int
  */
STooltip.WAIT_TIME = 500;

/**
  * The name of the css classname (for tooltip) used by default when creating tooltips.
  * @type string
  */
STooltip.CSS_CLASSNAME = "STooltip";

/**
  * The name of the css classname (for the element over the tooltip) used by default when creating tooltips.
  * @type string
  */
STooltip.CSS_CLASSNAME_OVER = "STooltip_Over";

/**
  * When the tooltip is popuped up automatically, it will hide after this time.<br/>
  * The default value is the <code>STooltip.POPUP_TIME</code>.
  * @param {int} time The time in ms after which the tooltip will hide, or null to use <code>STooltip.POPUP_TIME</code>.
  */
STooltip.prototype.setPopupTime = function(time)
{
	this._popuptime = time;
}

/**
  * Before the tooltip is popuped up automatically, it will wait the wait time.<br/>
  * The default value is the <code>STooltip.WAIT_TIME</code>.
  * @param {int} time The time in ms after which the tooltip will show, or null to use <code>STooltip.WAIT_TIME</code>.
  */
STooltip.prototype.setWaitTime = function(time)
{
	this._waittime = time;
}

/**
  * Get the current width setted for the tooltip (not its real).
  * @return The current width.
  * @type int
  */
STooltip.prototype.getWidth = function()
{
	return parseInt(this._overTooltip.style.width);
}

/**
  * Get the current height setted for the tooltip (not its real).
  * @return The current height.
  * @type int
  */
STooltip.prototype.getHeight = function()
{
	return parseInt(this._overTooltip.style.height);
}

/**
  * Change the (minimal) tooltip size.<br/>
  * The effect is immediate on a displayed tooltip.<br/>
  * If the size is bigger than the maximal size, the maximal size will be increased to the size.
  * @param {int} width The width to set.
  * @param {int} height The height to set.
  */
STooltip.prototype.setSize = function(width, height)
{
	this._overTooltip.style.width = width;
	this._overTooltip.style.height = height;
}

/**
  * Get the current left coordinate of the tooltip.
  * @return The current left coordinate.
  * @type int
  */
STooltip.prototype.getLeftPosition = function()
{
	return parseInt(this._overTooltip.style.left);
}

/**
  * Get the current top coordinate of the tooltip.
  * @return The current top coordinate.
  * @type int
  */
STooltip.prototype.getTopPosition = function()
{
	return parseInt(this._overTooltip.style.top);
}

/**
  * Change the tooltip position.<br/>
  * The effect is immediate on a displayed popup.
  * @param {int} left The new left coordinate.
  * @param {int} top The new top eft coordinate.
  */
STooltip.prototype.setPosition = function(left, top)
{
	this._overTooltip.style.left = left;
	this._overTooltip.style.top = top;
}

/**
  * Change the z-index of the tooltip. Default value is 20.<br/>
  * Apply only at show time.<br/>
  * @param {int} level The level to set.
  */
STooltip.prototype.setLevel = function(level)
{
	this._level = level;
}

/**
  * Get the html content of the tooltip.
  * @return The current html content.
  * @type string
  */
STooltip.prototype.getContent = function()
{
	return this._tooltip.innerHTML;
}

/**
  * The the html content of the tooltip.
  * @param {string} html The html to show in the tooltip.
  */
STooltip.prototype.setContent = function(html)
{
	this._tooltip.innerHTML = html;
}

/**
  * Apply a css class to the tooltip.
  * @param {string} className The name of a css class to apply to the tooltip.
  */
STooltip.prototype.setCSSClass = function(className)
{
	this._tooltip.className = className;
}

/**
  * Apply a css class to the element over the tooltip.<br/>
  * Might be set for shadow purposes.
  * @param {string} className The name of a css class to apply to the over element.
  */
STooltip.prototype.setCSSClassForOver = function(className)
{
	this._overTooltip.className = className;
}

/**
  * Set a listener for an event.
  * @param {string} event The name of the event to listen among:
  * <ul><li>"onshow" <code>void function (stooltip)</code>,</li>
  * <li>"onhide" <code>void function (stooltip)</code>,</li>
  * <li>"onpopup" (called before show, when show is coming by an attached element. can cancel the show by return false;) <code>boolean function (stooltip, element)</code>,</li>
  * @param {function} listener The function to call if corresponding event is fired.
  */
STooltip.prototype.setEventListener = function(event, listener)
{
	this._event[event] = listener;
}

/**
  * Attach listeners to the given html element, after the wait time, the tooltip will show.
  * Then after the popup time (or an event) the tooltip will hide itself.
  */
STooltip.prototype.attachToElement = function(htmlElement)
{
	htmlElement.tooltipInstance = this._instance;
	
	SUtilities.attachEvent (htmlElement, "mouseover", STooltip._startTooltip);
	
	SUtilities.attachEvent (htmlElement, "mouseout", STooltip._cancelTooltip);	
	SUtilities.attachEvent (htmlElement, "mousedown", STooltip._cancelTooltip);
	SUtilities.attachEvent (htmlElement, "keydown", STooltip._cancelTooltip);
}

/**
  * Detach all listeners from the given html element.
  */
STooltip.prototype.dettachFromElement = function(htmlElement)
{
	htmlElement.tooltipInstance = null;
	
	SUtilities.detachEvent (htmlElement, "mouseover", STooltip._startTooltip);
	
	SUtilities.detachEvent (htmlElement, "mouseout", STooltip._cancelTooltip);	
	SUtilities.detachEvent (htmlElement, "mousedown", STooltip._cancelTooltip);
	SUtilities.detachEvent (htmlElement, "keydown", STooltip._cancelTooltip);
}

/**
  * Create the internal objects
  * @private
  */
STooltip.prototype._create = function()
{
	// creating the surrounding tooltip
	this._overTooltip = this._document.createElement("div");
		this._overTooltip.style.display = 'none';
		this._overTooltip.style.position = 'absolute';
		this._overTooltip.className = STooltip.CSS_CLASSNAME_OVER;
		this._document.body.appendChild(this._overTooltip);

	// creating the tooltip itself
	this._tooltip = this._document.createElement("div");
		this._tooltip.style.display = '';
		this._tooltip.style.position = 'static';
		this._tooltip.className = STooltip.CSS_CLASSNAME;
		this._overTooltip.appendChild(this._tooltip);		
}

/**
  * Display the tooltip at the last position setted.<br/>
  * Fires the onshow event.
  * @param {boolean} autohide When autohide is on, the tooltip will close automatically after the pop time has exceeded. Default value is false.
  */
STooltip.prototype.show = function(autohide)
{
	if (this._overTooltip.style.display == 'block')
	{
		// already shown
		return;
	}

	// Show the instance	
	this._overTooltip.style.zIndex = this._level;
	this._overTooltip.style.display = 'block';

	// Set the autohide
	if (autohide == true)
	{
		this._popupTimeout = window.setTimeout("STooltip._hide(" + this._instance + ");", 
				this._popuptime != null ? this._popuptime : STooltip.POPUP_TIME);
	}
	else
	{
		this._popupTimeout = null;
	}
	
	// fires onshow event
	if (this._event.onshow != null)
	{
		this._event.onshow(this);
	}
}

/**
  * Hide the tooltip immediately.<br/>
  * Fires the onhide event.
  */
STooltip.prototype.hide = function()
{
	if (this._overTooltip.style.display == 'none')
	{
		// already hidden
		return;
	}
	
	// If this method is called, but a timeout is running
	if (this._popupTimeout != null)
	{
		// Cancel the timeout
		window.clearTimeout(this._popupTimeout);
	}

	// hide
	this._overTooltip.style.display = 'none';
	
	// fires onhide event
	if (this._event.onhide != null)
	{
		this._event.onhide(this);
	}
}

/**
  * Launch the hide method on the instance identified by its number
  * @param {int} instance The number of instance.
  * @private
  */
STooltip._hide = function(instance)
{
	if (STooltip._instances[instance]._popupTimeout != null)
	{
		window.clearTimeout(STooltip._instances[instance]._popupTimeout);
		STooltip._instances[instance]._popupTimeout = null;
	}
	
	STooltip._instances[instance].hide();
	STooltip._lastPopdown = new Date().getTime();
}

/**
  * Launch the show method on the instance identified by its number
  * @param {int} instance The number of instance.
  * @private
  */
STooltip._show = function(instance)
{
	var element = STooltip._instances[instance]._popupElement;
	element.tooltipTimer = null;
	STooltip._instances[instance]._popupElement = null;
	
	if (STooltip._instances[instance]._event.onpopup != null
		&& STooltip._instances[instance]._event.onpopup(STooltip._instances[instance], element) == false)
	{
		// explicitly canceled
		return;
	}
	
	STooltip._instances[instance].setPosition(SUtilities.mouseX + 12, SUtilities.mouseY + 20);
	
	STooltip._instances[instance].show(true);
}

/**
  * For all events for html element attached to tooltips that start the tooltip
  * @private
  */
STooltip._startTooltip = function()
{
	var element = SUtilities.IS_IE ? window.event.srcElement : this;
    var stooltip;

    do
    {
      stooltip = STooltip._instances[element.tooltipInstance];

      if (stooltip == null)
      {
        element = element.parentNode;
        if (element == null)
          return;
      }
    } while (stooltip == null);
  
	var timeToWait = stooltip._waittime != null ? stooltip._waittime : STooltip.WAIT_TIME;
	
	var currentTime = new Date().getTime()
	if (STooltip._lastPopdown != null && currentTime - STooltip._lastPopdown < 50)
	{
		timeToWait = 10;
	}
	
	element.tooltipTimer = window.setTimeout("STooltip._show(" + element.tooltipInstance + ");", timeToWait);
					
	stooltip._popupElement = element;
}

/**
  * For all events for html element attached to tooltips, that cancel the tooltip
  * @private
  */
STooltip._cancelTooltip = function()
{
	var element = SUtilities.IS_IE ? window.event.srcElement : this;
    var stooltip;

    do
    {
      stooltip = STooltip._instances[element.tooltipInstance];

      if (stooltip == null)
      {
        element = element.parentNode;
        if (element == null)
          return;
      }
    } while (stooltip == null);
    
	if (element.tooltipTimer != null)
	{
		window.clearTimeout(element.tooltipTimer);
		
		element.tooltipTimer = null;
		STooltip._instances[element.tooltipInstance]._popupElement = null;
	}
	else if (STooltip._instances[element.tooltipInstance]._popupTimeout != null)
	{		
		STooltip._hide(element.tooltipInstance);
	}
}

// Test for SUtilities presence
try 
{
	SUtilities;
} 
catch (e)
{
	alert("STooltip needs SUtilities to be imported.");
}
