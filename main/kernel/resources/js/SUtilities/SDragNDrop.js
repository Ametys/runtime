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
  * @fileoverview Class to hanlde drag'n'drop issues
  */

/**
  * SDragNDrop is a class to handle drag'n'drop issue.<br/>
  * <br/>
  * Basic usage:<br/>
  * This class is to use on html elements to drag and on html element that are drop zone.
  * <br/>
  * Works with:<br/>
  * <ul>
  * <li>FF 1.5</li>
  * <li>IE 7.0</li>
  * </ul>
  * <br/>
  * @requires SUtilities This class requires that the SUtilities class to be loaded before.
  * @version 1.0
  * @constructor
  */
function SDragNDrop(element)
{
	this._instance = "" + SDragNDrop._instanceCursor++;
	SDragNDrop._instances[this._instance] = this;
	SDragNDrop._instances.length++;

  this._element = (typeof element == "string") ? this._document.getElementById(element) : element;
  if (this._element == null)
  {
    throw "SDragNDrop needs an existing html element";
  }
  
  this._event = {};
  this._element.setAttribute("SDragNDrop", this._instance);
  this._isDrageable = false;
  this._isDropZone = false;
}

SDragNDrop._instanceCursor = 0;

SDragNDrop._draggingInstanceNumber = null;

SDragNDrop._draggingElement = null;

SDragNDrop._draggingNot = null;

SDragNDrop._instances = {};

SDragNDrop._instances.length = 0;

SDragNDrop.prototype._document = document;

SDragNDrop.prototype._instance = "0";

SDragNDrop.prototype._element = null;

SDragNDrop.prototype._isDrageable = false;

SDragNDrop.prototype._isDropZone = false;

SDragNDrop.prototype._event;

SDragNDrop.prototype._properties = null;

SDragNDrop.prototype._dragInitiated = false;

/**
 * Destroy all drag'n'drop of the current document by calling the dispose method of each objects.<br/>
 * New SDragNDrop can be created after a call to this method. 
 */
SDragNDrop.disposeAll = function ()
{
  for (var i in SDragNDrop._instances)
  {
    var elt = SDragNDrop._instances[i];
    if (typeof(elt) == "object" && elt.dispose)
    {
      elt.dispose();
    }
  }
}

/**
 * Destroy the SDragNDrop.<br/>
 * The html element attached is no longer drageable or a drop zone.<br/>
 * This instance cannot be used anymore.  
 */ 
SDragNDrop.prototype.dispose = function ()
{
  this._element.setAttribute("SDragNDrop", null);
  if (this._isDrageable)
  {
    SUtilities.detachEvent(this._element, "mousedown", SDragNDrop._onMouseDownForDrag);
    SUtilities.detachEvent(this._element, "mouseout", SDragNDrop._onMouseOutForDrag);
    SUtilities.detachEvent(this._element, "mouseup", SDragNDrop._onMouseUpForDrag);
    SUtilities.detachEvent(this._element, "startdrag", SDragNDrop._onStartDragForDrag);
    SUtilities.detachEvent(this._element, "selectstart", SDragNDrop._onSelectStartForDrag);
  }
  
  if (this._isDropZone)
  {
    SUtilities.detachEvent(this._element, "mouseover", SDragNDrop._onMouseOverForDrop);
    SUtilities.detachEvent(this._element, "mouseup", SDragNDrop._onMouseOverForDrop);
    SUtilities.detachEvent(this._element, "mouseout", SDragNDrop._onMouseOutForDrop);
  }
   
  this._element = null;
  this._document = null;
  
  delete SDragNDrop._instances[this._instance];
  SDragNDrop._instances.length--;
}

SDragNDrop.getInstance = function(instanceNumber)
{
  return SDragNDrop._instances[instanceNumber];
}

SDragNDrop.prototype.makeDrageable = function()
{ 
  this._isDrageable = true;
  SUtilities.attachEvent(this._element, "mousedown", SDragNDrop._onMouseDownForDrag);
  SUtilities.attachEvent(this._element, "mouseout", SDragNDrop._onMouseOutForDrag);
  SUtilities.attachEvent(this._element, "mouseup", SDragNDrop._onMouseUpForDrag);
  SUtilities.attachEvent(this._element, "startdrag", SDragNDrop._onStartDragForDrag);
  SUtilities.attachEvent(this._element, "selectstart", SDragNDrop._onSelectStartForDrag);
}

SDragNDrop.prototype.makeDropZone = function()
{
  this._isDropZone = true;
  SUtilities.attachEvent(this._element, "mouseover", SDragNDrop._onMouseOverForDrop);
  SUtilities.attachEvent(this._element, "mouseup", SDragNDrop._onMouseUpForDrop);
  SUtilities.attachEvent(this._element, "mouseout", SDragNDrop._onMouseOutForDrop);
}

SDragNDrop.prototype.isDrageable = function()
{
  return this._isDrageable;
}

SDragNDrop.prototype.isDropZone = function()
{
  return this._isDropZone;
}

SDragNDrop.prototype.getProperties = function()
{
  return this._properties;
}

SDragNDrop.prototype.setProperties = function(properties)
{
  this._properties = properties;
}

/**
 * ondrag (raised if drageable to accept) boolean function (SDragNDrop)
 * drawdrag (raised when need to drag) htmlElement function (SDragNDrop)
 * candrop  (raised if dropzone to accept) boolean function (SDragNDrop, SDragNDrop, targetElement)
 * nodrop  (raised if dropzone is leaved) void function (SDragNDrop, SDragNDrop, targetElement)
 * ondrop  (raised if dropzone to do the job) void function (SDragNDrop, SDragNDrop, targetElement)
 */ 
SDragNDrop.prototype.setEventListener = function(event, listener)
{
  this._event[event] = listener;
}

SDragNDrop.prototype._startUIDrag = function()
{
  if (this._event["drawdrag"] != null && typeof(this._event["drawdrag"]) == "function")
  {
    SDragNDrop._draggingElement = this._event["drawdrag"](this);
  }
  else
  {
    SDragNDrop._draggingElement = this._element.cloneNode(true);
    if (SDragNDrop._draggingElement.style.backgroundColor == "")
    {
      SDragNDrop._draggingElement.style.backgroundColor = "#ffffff";
    }
  }
  this._document.body.appendChild(SDragNDrop._draggingElement);
  SDragNDrop._draggingElement.style.position = "absolute";
  SDragNDrop._draggingElement.style.zIndex = 1000;

  if (SDragNDrop._draggingNot == null)
  {
    SDragNDrop._draggingNot = document.createElement("div");
    SDragNDrop._draggingNot.innerHTML = "&#160";
    SDragNDrop._draggingNot.className = "SDragNDrop_NotAZone";
    SDragNDrop._draggingNot.style.position = "absolute";
    SDragNDrop._draggingNot.style.zIndex = 1001;
    this._document.body.appendChild(SDragNDrop._draggingNot);
  }
  SDragNDrop._draggingNot.style.display = "";
}

SDragNDrop.prototype._moveUIDrag = function(event, overDropZone)
{
  SDragNDrop._draggingElement.style.top = SUtilities.mouseY + 20;
  SDragNDrop._draggingElement.style.left = SUtilities.mouseX + 15;
  
  SDragNDrop._draggingNot.style.top = SUtilities.mouseY + 20;
  SDragNDrop._draggingNot.style.left = SUtilities.mouseX + 15;
  
  if (overDropZone != null)
  {
    SDragNDrop._draggingNot.style.display = overDropZone ? "none" : "";
  }
  
  var size = 60;
  if (SUtilities.IS_IE)
  {
    if (window.event.clientX < size)
    {
      window.scrollBy(-10, 0);
    }    

    if (window.event.clientX + size > document.body.offsetWidth)
    {
      window.scrollBy(10, 0);
    }    

    if (window.event.clientY < size)
    {
      window.scrollBy(0, -10);
    }    
      
    if (window.event.clientY + size > document.body.offsetHeight)
    {
      window.scrollBy(0, 10);
    }    
  }
  else
  {
    if (event.screenX < size)
    {
      window.scrollBy(-10, 0);
    }    
      
    if (event.screenX + size > window.innerWidth)
    {
      window.scrollBy(10, 0);
    }    
      
    if (event.screenY < size)
    {
      window.scrollBy(0, -10);
    }    
      
    if (event.screenY + size > window.innerHeight)
    {
      window.scrollBy(0, 10);
    }    
  }
}

SDragNDrop.prototype._endUIDrag = function()
{
  SDragNDrop._draggingInstanceNumber = null;

  SDragNDrop._draggingElement.parentNode.removeChild(SDragNDrop._draggingElement);
  SDragNDrop._draggingElement = null;

  SDragNDrop._draggingNot.style.display = "none";
}

/**
 * Seek a given HTML element to find a drag'n'drop objet
 * @param {HTMLObject} element The HTML element to inspect
 * @param {boolean} seekForDrageable Specify to search for drageable or not SDragNDrop. Can be null to avoid filtering.
 * @param {boolean} seekForDropZone Specify to search for drop zone or not SDragNDrop. Can be null to avoid filtering.
 * @param {boolean} seekAbove Specify to search on parent elements
 * @return The drag'n'drop object found or null otherwise 
 * @type SDragNDrop
 * @private 
 */ 
SDragNDrop._seekSDragNDrop = function(element, seekForDrageable, seekForDropZone, seekAbove)
{
  var sDragNDropNumber = element.getAttribute("SDragNDrop");
  if (sDragNDropNumber != null)
  {
    var sDragNDrop = SDragNDrop.getInstance(sDragNDropNumber);
    if (sDragNDrop != null  && (seekForDropZone == null || sDragNDrop.isDropZone() == seekForDropZone) 
                            && (seekForDrageable == null || sDragNDrop.isDrageable() ==  seekForDrageable))
    {
      return sDragNDrop;
    }
  }
  
  if (seekAbove != false && element.parentNode != null && element.parentNode.getAttribute != null)
  {
    return SDragNDrop._seekSDragNDrop(element.parentNode, seekForDrageable, seekForDropZone);
  }
  else
  {
    return null;
  }
}

SDragNDrop._onSelectStartForDrag = function(event)
{
  return false;
}

SDragNDrop._onStartDragForDrag = function(event)
{
  return false;
}

SDragNDrop._onMouseDownForDrag = function(event)
{
  var element = SUtilities.IS_IE ? window.event.srcElement : event.target;
  var sDragNDrop = SDragNDrop._seekSDragNDrop(element, true, false);
  if (sDragNDrop != null)
  {
    if (sDragNDrop._event["ondrag"] == null || typeof(sDragNDrop._event["ondrag"]) != 'function' || sDragNDrop._event["ondrag"](sDragNDrop) != false)
    {
      sDragNDrop._dragInitiated = true;
    }
  }  
}

SDragNDrop._onMouseUpForDrag = function(event)
{
  var element = SUtilities.IS_IE ? window.event.srcElement : event.target;
  var sDragNDrop = SDragNDrop._seekSDragNDrop(element, true, false);
  if (sDragNDrop != null)
  {
    sDragNDrop._dragInitiated = false;
  }
}

SDragNDrop._onMouseOutForDrag = function(event)
{
  if (SDragNDrop._draggingInstanceNumber == null)
  {
    var element = SUtilities.IS_IE ? window.event.srcElement : event.target;
    var sDragNDrop = SDragNDrop._seekSDragNDrop(element, true, false, false);
    if (sDragNDrop != null && sDragNDrop._dragInitiated)
    {
      sDragNDrop._dragInitiated = false;
      
      SDragNDrop._draggingInstanceNumber = sDragNDrop._instance;
      
    sDragNDrop._document.body.focus();
    
      sDragNDrop._startUIDrag();
    }
  }
}

SDragNDrop._onMouseUp = function(event)
{
  if (SDragNDrop._draggingInstanceNumber != null)
  {
    var sDragNDrop = SDragNDrop.getInstance(SDragNDrop._draggingInstanceNumber)
    sDragNDrop._endUIDrag();
  }
  
}

SDragNDrop._onMouseMove = function(event)
{
  if (SDragNDrop._draggingInstanceNumber != null)
  {
    var sDragNDrop = SDragNDrop.getInstance(SDragNDrop._draggingInstanceNumber)
    
		try {this.slistview.ui.document.execCommand("Unselect",false,null);} catch (e) {};
    
    if ((SUtilities.IS_IE ? window.event.button : event.which) == 0)    
    {
      sDragNDrop._endUIDrag();
    }
    else
    {
      sDragNDrop._moveUIDrag(event);
    }
  }
}

SDragNDrop._onMouseOverForDrop = function(event)
{
  if (SDragNDrop._draggingInstanceNumber != null)
  {
    var element = SUtilities.IS_IE ? window.event.srcElement : event.target;
    var sDragNDrop = SDragNDrop._seekSDragNDrop(element, false, true, true);
    if (sDragNDrop != null)
    {
      if (sDragNDrop._event["candrop"] == null || typeof(sDragNDrop._event["candrop"]) != 'function' || sDragNDrop._event["candrop"](sDragNDrop, SDragNDrop.getInstance(SDragNDrop._draggingInstanceNumber), element) != false)
      {
        sDragNDrop._moveUIDrag(event, true);
      }
      else
      {
        sDragNDrop._moveUIDrag(event, false);
      }
    }
  }
}

SDragNDrop._onMouseOutForDrop = function(event)
{
  if (SDragNDrop._draggingInstanceNumber != null)
  {
    var element = SUtilities.IS_IE ? window.event.srcElement : event.target;
    var sDragNDrop = SDragNDrop._seekSDragNDrop(element, false, true, true);
    if (sDragNDrop != null)
    {
      if (sDragNDrop._event["nodrop"] != null && typeof(sDragNDrop._event["nodrop"]) == 'function')
      {
        sDragNDrop._event["nodrop"](sDragNDrop, SDragNDrop.getInstance(SDragNDrop._draggingInstanceNumber), element);
      } 

      sDragNDrop._moveUIDrag(event, false);
    }
  }
}

SDragNDrop._onMouseUpForDrop = function(event)
{
  if (SDragNDrop._draggingInstanceNumber != null)
  {
    var element = SUtilities.IS_IE ? window.event.srcElement : event.target;
    var sDragNDrop = SDragNDrop._seekSDragNDrop(element, false, true);
    if (sDragNDrop != null)
    {
      if (sDragNDrop._event["candrop"] == null || typeof(sDragNDrop._event["candrop"]) != 'function' || sDragNDrop._event["candrop"](sDragNDrop, SDragNDrop.getInstance(SDragNDrop._draggingInstanceNumber), element) != false)
      {
        if (sDragNDrop._event["ondrop"] != null && typeof(sDragNDrop._event["ondrop"]) == 'function')
        {
          sDragNDrop._event["ondrop"](sDragNDrop, SDragNDrop.getInstance(SDragNDrop._draggingInstanceNumber), element);
        }
      }
    }
    
    sDragNDrop._endUIDrag();
  }
}

// Test for SUtilities presence
try 
{
  SUtilities;
} 
catch (e)
{
  var msg = "SDragNDrop needs SUtilities to be imported."; 
  alert(msg);
  throw msg;
}

SUtilities.attachEvent(document, "mousemove", SDragNDrop._onMouseMove);
SUtilities.attachEvent(document, "mouseup", SDragNDrop._onMouseUp);
