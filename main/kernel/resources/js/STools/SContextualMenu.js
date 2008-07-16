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
 
function SContextualMenu(id, _document, listener)
{
	this.document = _document ? _document : document;

	this.listener = listener

	this.ui = {};
	this.ui.element = (typeof id == "string") ? this.document.getElementById(id) : id; 
	this.ui.config = new SToolBar.Config();
	this.ui.table = this.document.createElement("table");

	var body = this.ui.element.ownerDocument.body
	body.insertBefore(this.ui.table, body.childNodes[0])

	this.ui.table.insertRow(this.ui.table.rows.length);
	this.ui.table.insertRow(this.ui.table.rows.length);

	this.ui.table.style.width = "0px"
	this.ui.table.style.height = "0px"
	this.ui.table.style.display = "none"
	this.ui.table.style.position = "absolute"
	this.ui.table.style.zIndex = "+2"
	this.ui.table.style.top = "0px"
	this.ui.table.style.left = "0px"

	this.ui.table.rows[0].style.display = "none"
	
	this.smenubar = new SMenuBar(this)
	this.smenubar.titleRow = this.ui.table.rows[0]
	this.smenubar.menusRow = this.ui.table.rows[1]
	
	this.smenu = this.smenubar.addMenu("ContextualMenu")
	
	this.ui.element.contextualMenu = this
	this.ui.element.oncontextmenu = function(e) 
					{ 
						if (this.contextualMenu.listener && this.contextualMenu.listener.onopen)
							this.contextualMenu.listener.onopen(e, this);
					
						this.contextualMenu.lastTarget = STools.is_ie ? window.event.srcElement : e.originalTarget;
						
						var x, y
						if (STools.is_ie) { // for IE
							y = window.event.clientY+document.body.scrollTop-5
							x = window.event.clientX+document.body.scrollLeft-5
						} else { // for Navigator
							y = e.pageY-10
							x= e.pageX-10
						}
						this.contextualMenu.open(x, y); return false; 
					}
}

SContextualMenu.prototype.remove = function()
{
        this.ui.element.oncontextmenu = null;
        this.ui.table.parentNode.removeChild(this.ui.table)
}

SContextualMenu.prototype.paint = function()
{
	this.smenu.paint(this.ui.table.rows[0], this.ui.table.rows[1])
}

SContextualMenu.prototype.getMenu = function()
{
	return this.smenu;
}

SContextualMenu.prototype.open = function(x, y)
{
	this.ui.table.style.top = y + "px";
	this.ui.table.style.left = x + "px";
	this.ui.table.style.display = "";
	SMenu.openClose(this.smenubar.titleRow.cells[0])
}
