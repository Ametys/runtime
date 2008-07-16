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
 
/* -----------------------------
	TOOLBAR
   ----------------------------- */
function SToolBar(table, config) {
	this.content = new Array();

	this.ui = new Object();
	this.ui.table = (typeof table == "string") ? document.getElementById(table) : table;
	this.ui.config = config ? config : new SToolBar.Config();	
}

SToolBar.prototype.addHTML = function(html) {
	this.content.push(html);
	return this.content.length;
}

SToolBar.prototype.addMenuBar = function() {
	var menu = new SMenuBar(this);
	this.content.push(menu);
	return menu;
}

SToolBar.prototype.addButton = function(text, img, action, shortcut, tooltip, textStyle, textClass) {
	var button = new SToolBarButton(this, text, img, action, shortcut, tooltip, textStyle, textClass);
	this.content.push(button);
	return button;
}

SToolBar.prototype.addSeparator = function() {
	var separator = new SToolBarSeparator(this);
	this.content.push(separator);
	return separator;
}

SToolBar.prototype.paint = function() {
	var toolBar = this.ui.table;
	
	while (toolBar.rows.length != 0)
		toolBar.rows.removeChild(toolBar.rows[0])
		
	toolBar.cellPadding = "0";
	toolBar.cellSpacing = "0";
	
	var row = toolBar.insertRow(toolBar.rows.length);
	var td = document.createElement("td");
	STools.applyStyle (td, this.ui.config.toolBarStyle, this.ui.config.toolBarClass);
	row.appendChild(td)
	
	var subTable = document.createElement("table");
	subTable.cellPadding = "0";
	subTable.cellSpacing = "0";
	var subRow = subTable.insertRow(subTable.rows.length);
	
	for (var i=0; i<this.content.length; i++)
	{
		var aTd = document.createElement("td");
		STools.applyStyle (aTd, this.ui.config.toolBarBlockStyle, this.ui.config.toolBarBlockClass);
		subRow.appendChild(aTd)

		if (typeof this.content[i] == "string")
		{
			aTd.innerHTML = this.content[i];
		}
		else if (this.content[i].paint)
		{
			this.content[i].paint(aTd);
		}
	}
	
	td.appendChild(subTable)
}

/* -----------------------------
	SEPARATOR
   ----------------------------- */
function SToolBarSeparator(parent) {
	this.parent = parent;
}

SToolBarSeparator.prototype.paint = function(parentElement) {
	parentElement.innerHTML = "";
	
	var table = document.createElement("table");
	table.cellSpacing = "0";
	table.cellPadding = "0";
	
	STools.applyStyle (table, this.parent.ui.config.separatorStyle, this.parent.ui.config.separatorClass);
	
	var row = table.insertRow(table.rows.length);
	var td = document.createElement("td");
	row.appendChild(td);	
	
	parentElement.appendChild(table);
}

/* -----------------------------
	BUTTON
   ----------------------------- */
function SToolBarButton(parent, text, img, action, shortcut, tooltip, textStyle, textClass) {
	this.parent = parent;
	
	this.text = text;
	this.textStyle = textStyle ? textStyle : {};
	this.textClass = textClass ? textClass : "";
	
	if (typeof img == "string" || img == undefined) {
		this.img = img;
		this.disabledImg = img;
	} else {
		this.img = img.normal;
		this.disabledImg = img.disabled;
	}

	this.action = new Object();
	if (typeof action == "function" || action == undefined) {
		this.action.act = action;
		this.action.isEnabled = function () {return true;};
		this.action.isEnabledArg = null;
	} else {
		this.action.act = action.act;
		this.action.isEnabled = action.isEnabled;
		this.action.isEnabledArg = action.isEnabledArg;
	}
		
		
	if (shortcut)
		this.shortcut = new SShortcut (shortcut, this.action);

	if (tooltip)
	       this.tooltip = tooltip;
}

SToolBarButton.prototype.paint = function(parentElement) {
	parentElement.innerHTML = "";
	
	var table = document.createElement("table");
	table.cellSpacing = "0";
	table.cellPadding = "0";
	
	var row = table.insertRow(table.rows.length);
	var td = document.createElement("td");
	row.appendChild(td);
	
	var isEnabled = this.action.isEnabled(this.action.isEnabledArg);
	STools.applyStyle (td, this.parent.ui.config.buttonStyle, this.parent.ui.config.buttonClass);

	var innerTable = document.createElement("table");
	innerTable.cellSpacing = "0";
	innerTable.cellPadding = "0";
	
	var innerRow = innerTable.insertRow(innerTable.rows.length);
	if (this.img)
	{
		var innerTd_1 = document.createElement("td");
		innerRow.appendChild(innerTd_1);

		var img = document.createElement("img");
		img.src = isEnabled ? this.img : this.disabledImg;
		STools.applyStyle (img, this.parent.ui.config.imageStyle, this.parent.ui.config.imageClass);
		innerTd_1.appendChild(img);
	}
	if (this.text)
	{
		var innerTd_2 = document.createElement("td");
		innerRow.appendChild(innerTd_2);
		innerTd_2.innerHTML = this.text;

		if (isEnabled  || !this.action.act)
			STools.applyStyle (innerTd_2, this.parent.ui.config.enabledStyle, this.parent.ui.config.enabledClass);
		else
			STools.applyStyle (innerTd_2, this.parent.ui.config.disabledStyle, this.parent.ui.config.disabledClass);

		STools.applyStyle (innerTd_2, this.textStyle, this.textClass);
	}
	if (this.tooltip)
		td.title = this.tooltip;

	td.appendChild(innerTable);

	if (isEnabled && this.action.act)
	{
		td.model = this;
		td.onmouseover = function() {
			STools.applyStyle (this, this.model.parent.ui.config.buttonOverStyle, this.model.parent.ui.config.buttonOverClass);
		}
		td.onmouseup = function() {
			STools.applyStyle (this, this.model.parent.ui.config.buttonOverStyle, this.model.parent.ui.config.buttonOverClass);
		}
		td.onmousedown = function() {
			STools.applyStyle (this, this.model.parent.ui.config.buttonDownStyle, this.model.parent.ui.config.buttonDownClass);
		}
		td.onclick = function() {
			this.model.action.act();
		}
		td.onmouseout = function() {
			STools.applyStyle (this, this.model.parent.ui.config.buttonStyle, this.model.parent.ui.config.buttonClass);
		}
	}

	
	parentElement.appendChild(table);
}

/* -----------------------------
	CONFIG
   ----------------------------- */
SToolBar.Config = function () 
{	
	// TOOLBAR
	this.toolBarStyle = {
	 	backgroundColor: "ThreeDFace",
	 	borderStyle: "solid",
	 	borderWidth: "1px",
	 	height: "1px",
	 	paddingTop: "2px",
	 	paddingLeft: "4px",
	 	paddingRight: "4px",
	 	paddingBottom: "2px",
	 	borderTopColor: "ActiveBorder",
	 	borderLeftColor: "ActiveBorder",
	 	borderBottomColor: "ThreeDDarkShadow",
	 	borderRightColor: "ThreeDDarkShadow"
	};
	this.toolBarClass = "";

	this.toolBarBlockStyle = {
	 	height: "1px",
	 	paddingTop: "0px",
	 	paddingLeft: "4px",
	 	paddingRight: "4px",
	 	paddingBottom: "0px"
	};
	this.toolBarBlockClass = "";
	
	// SEPARATOR
	this.separatorStyle = {
	 	backgroundColor: "ThreeDFace",
	 	borderStyle: "solid",
	 	borderWidth: "1px",
	 	padding: "0px",
	 	margin: "0px",
	 	width: "2px",
	 	height: "100%",
	 	display: "",
	 	borderTopColor: "ThreeDDarkShadow",
	 	borderLeftColor: "ThreeDDarkShadow",
	 	borderBottomColor: "ActiveBorder",
	 	borderRightColor: "ActiveBorder"
	};
	this.separatorClass = "";
	
	// BUTTON
	this.buttonStyle = {
	 	backgroundColor: "ThreeDFace",
	 	borderStyle: "solid",
	 	borderWidth: "1px",
	 	padding: "2px",
	 	margin: "0px",
	 	height: "100%",
	 	display: "",
	 	cursor: "default",
	 	borderColor: "ThreeDFace"
	};
	this.buttonClass = "";
	
	this.enabledStyle = {
		fontFamily: "Verdana, Arial",
		fontSize: "11px"
	};
	this.enabledClass = "";
	this.disabledStyle = {
		color: "#7f7f7f",
		fontFamily: "Verdana, Arial",
		fontSize: "11px"
	};
	this.disabledClass = "";
	
	this.buttonOverStyle = {
	 	borderTopColor: "ActiveBorder",
	 	borderLeftColor: "ActiveBorder",
	 	borderBottomColor: "ThreeDDarkShadow",
	 	borderRightColor: "ThreeDDarkShadow"
	};
	this.buttonOverClass = "";
	
	this.buttonDownStyle = {
	 	borderTopColor: "ThreeDDarkShadow",
	 	borderLeftColor: "ThreeDDarkShadow",
	 	borderBottomColor: "ActiveBorder",
	 	borderRightColor: "ActiveBorder"
	};
	this.buttonDownClass = "";
	
	this.imageStyle = {
	 	margin: "2px"
	};
	this.imageClass = "";
	
		this.menuStyle = {
		backgroundColor: "ThreeDFace", 
		fontFamily: "Verdana, Arial",
		fontSize: "11px",
		color: "MenuText",
		textDecoration: "none",
		padding: "4px",
		paddingLeft: "8px",
		paddingRight: "8px",
		cursor: "default"
	};
	this.menuClass = "";
	
	this.overMenuStyle = {
		backgroundColor: "Highlight", 
		fontFamily: "Verdana, Arial",
		fontSize: "11px",
		color: "HighlightText",
		textDecoration: "none",
		padding: "4px",
		paddingLeft: "8px",
		paddingRight: "8px",
		cursor: "default"
	};
	this.overMenuClass = "";
	
	this.subMenuStyle = {
	 	backgroundColor: "Menu", 
	 	padding: "4px",
		paddingLeft: "8px",
		paddingRight: "8px",
	 	position: "absolute", 
	 	display: "none",
	 	borderStyle: "solid",
	 	borderWidth: "1px",
	 	borderTopColor: "ActiveBorder",
	 	borderLeftColor: "ActiveBorder",
	 	borderBottomColor: "ThreeDDarkShadow",
	 	borderRightColor: "ThreeDDarkShadow"
 	}
	this.subMenuClass = "";

	this.subElementMenuStyle = {
	 	backgroundColor: "Menu", 
		fontFamily: "Verdana, Arial",
		fontSize: "11px",
		color: "MenuText",
		cursor: "default",
		paddingLeft: "4px",
		paddingRight: "4px",
		paddingTop: "2px",
		paddingBottom: "2px"
	}
	this.subElementMenuClass = "";
	
	this.disabledSubElementMenuStyle = {
	 	backgroundColor: "Menu", 
		fontFamily: "Verdana, Arial",
		fontSize: "11px",
		color: "GrayText",
		cursor: "default",
		paddingLeft: "4px",
		paddingRight: "4px",
		paddingTop: "2px",
		paddingBottom: "2px"
	}
	this.disabledSubElementMenuClass = "";
	
	this.overSubElementMenuStyle = {
	 	backgroundColor: "Highlight", 
		fontFamily: "Verdana, Arial",
		fontSize: "11px",
		color: "HighlightText",
		cursor: "default"
	}
	this.overSubElementMenuClass = "";
	
	this.subElementSeparatorStyle = {
		height: "1px",
		borderBottomStyle: "solid",
		borderBottomWidth: "1px",
		borderBottomColor: "ActiveBorder"
	};
	this.subElementSeparatorClass = "";
}