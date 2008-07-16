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
  * @fileoverview This listview allow to view element in several views
  */
  
 /**
  * Basic Usage :<br/>
  * This class is usefull to keep a size respected : the panel take all.<br/>
  * <br/>
  * Works with:<br/>
  * <ul>
  * <li>IE 7.0</li>
  * <li>FF 1.5</li>
  * </ul>
  * <br/>
  * @requires SUtilities This class requires that the SUtilities class to be loaded before.
  * @version 1.0
  * @constructor
  */
function SFullTable(htmlElement, minSize, maxSize)
{
  this._instance = SFullTable._instance++;
  SFullTable._instances[this._instance] = this;
  this._document = document;
  
  this._htmlElement = (typeof htmlElement == "string") ? this._document.getElementById(htmlElement) : htmlElement;
  this._minSize = minSize == null ? { width : 0, height : 0 } : minSize;
  this._maxSize = maxSize == null ? { width : 32768, height : 32768 } : maxSize;
  
  this._table = this._surround(this._htmlElement);
  this.resize();
}

SFullTable._instance = 0;

SFullTable._instances = {};

SFullTable.prototype._instance = 0;

SFullTable.prototype._event = {};

SFullTable.prototype._document;

SFullTable.prototype._parent;

SFullTable.prototype._surround = function(element)
{
  var table = this._createTable();
  element.parentNode.insertBefore(table, element);
  table.rows[0].cells[0].appendChild(element);
  return table;
}

SFullTable.prototype._createTable = function()
{
  var table = this._document.createElement("table");
  table.cellSpacing = "0";
  table.cellPadding = "0";
  table.style.margin = "0px";
  table.style.padding = "0px";
  table.style.borderStyle = "none";
  table.style.position = "";
  table.style.tableLayout = "fixed";
  table.style.overflow = "hidden";
  
  var tr = table.insertRow(0);
  tr.style.margin = "0";
  tr.style.padding = "0";
  
  var td = this._document.createElement("td");
  tr.appendChild(td);
  td.style.margin = "0";
  td.style.padding = "0";
  td.style.overflow = "hidden";
  
  return table;
}

SFullTable.prototype.setEventListener = function(event, listener)
{
  this._event[event] = listener;
}

SFullTable.prototype.resize = function()
{
  this._table.rows[0].cells[0].removeChild(this._htmlElement);
  
  this._table.style.width = "100%";
  this._table.style.height = "100%";
  this._table.rows[0].cells[0].style.width = "";
  this._table.rows[0].cells[0].style.height = "";

  this._width = this._table.offsetWidth;
  this._width = this._width < this._minSize.width ? this._minSize.width : this._width;
  this._width = this._width > this._maxSize.width ? this._maxSize.width : this._width;

  this._height = this._table.offsetHeight;
  this._height = this._height < this._minSize.height ? this._minSize.height : this._height;
  this._height = this._height > this._maxSize.height ? this._maxSize.height : this._height;

  this._table.style.width = this._width;
  this._table.style.height = this._height;
  this._table.rows[0].cells[0].style.width = this._width;
  this._table.rows[0].cells[0].style.height = this._height;

  this._table.rows[0].cells[0].appendChild(this._htmlElement);
  
  // fires onresize event
  if (this._event.onresize != null)
  {
    this._event.onresize(this);
  }
  
}

SFullTable._onResize = function()
{
  for (var i in SFullTable._instances)
  {
    SFullTable._instances[i].resize();
  }
}

// Test for SUtilities presence
try 
{
  SUtilities;
} 
catch (e)
{
  var msg = "SFullTable needs SUtilities to be imported."; 
  alert(msg);
  throw msg;
}

SUtilities.attachEvent(window, "resize", SFullTable._onResize);
