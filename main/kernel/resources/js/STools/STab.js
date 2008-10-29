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
STab.css = "resources/css/";
STab.img = "resources/img/"; 

function STab(id, config, _document, listener) {
	if (_document == null) _document = document;
	
	STools.loadStyle (_document, STree.css + "stab.css")

	this.listener = listener;
	this.document = _document;
	this.ui = new Object();
	this.ui.element = (typeof id == "string") ? this.document.getElementById(id) : id;
	this.ui.tabs = new Array();
	this.ui.config = config != null ? config : new STab.Config();
}

STab.prototype.addTab = function(caption, id, img) {
	var tab = new Object();
	tab.caption = caption;
	tab.img = img;
	tab.element = (typeof id == "string") ? this.document.getElementById(id) : id;
	tab.element.style.display = "none";

	this.ui.tabs.push(tab);
}

STab.prototype.paint = function() {

	STools.applyStyle (this.ui.element, this.ui.config.globalTableStyle, this.ui.config.globalTableClass);
	
	// Créer les en-têtes
	this.ui.headers = new Array();
	var headersRow = this.ui.element.insertRow(0);
	var headersCell = headersRow.insertCell(0);
	STools.applyStyle (headersCell, this.ui.config.globalHeadTableStyle, this.ui.config.globalHeadTableClass);
	
	
	for (var i=0; i<this.ui.tabs.length; i++)
	{
		var head = this.document.createElement("table");
		STools.applyStyle (head, this.ui.config.headTableStyle, this.ui.config.headTableClass);
		headersCell.appendChild(head);
		this.ui.headers.push(head);
		
		for (var row=0; row<4; row++)
		{
			var headRow = head.insertRow(row);
			
			for (var cell=0; cell<5; cell++)
			{
				var _cell = headRow.insertCell(cell);
				_cell.tabIndex = i;
				_cell.tab = this;
				_cell.onclick = function() {
					this.tab.selectTab(this.tabIndex);
				};

				_cell.style.cursor = "default";
				if (cell != 2)
				{
					_cell.style.width = "1px";
					_cell.style.padding = "0px";
					_cell.style.margin = "0px";
				}
				if (row != 2)
				{
					_cell.style.height = "1px";
					_cell.style.padding = "0px";
					_cell.style.margin = "0px";
				}
			}
		}
		if (this.ui.tabs[i].img)
		{
			var img = this.document.createElement("img")
			img.src = this.ui.tabs[i].img
			STools.applyStyle (img, this.ui.config.imgStyle, this.ui.config.imgClass);
			head.rows[2].cells[2].appendChild(img)
		}
		
    head.rows[2].cells[2].style.whiteSpace = "nowrap";
		head.rows[2].cells[2].appendChild( this.document.createTextNode(this.ui.tabs[i].caption) )
	}


	// Contenu
	var contentRow = this.ui.element.insertRow(1);
	var contentCell = contentRow.insertCell(0);
	STools.applyStyle (contentCell, this.ui.config.contentStyle, this.ui.config.contentClass);
	
	for (var i=0; i<this.ui.tabs.length; i++)
	{
		this.ui.tabs[i].element.parentNode.removeChild(this.ui.tabs[i].element)
		contentCell.appendChild(this.ui.tabs[i].element)
	}

	this.selectTab(0);
}

STab.prototype.selectTab = function(index)
{
	for (var i=0; i<this.ui.tabs.length; i++)
	{
		if (i == index)
		{
			this.ui.tabs[i].element.style.display = "";
		}
		else
		{
			this.ui.tabs[i].element.style.display = "none";
		}
	}

	for (var i=0; i<this.ui.headers.length; i++)
	{
		var head = this.ui.headers[i];
		
		if (i == index)
		{		
			head.style.marginLeft = "0px"
			head.style.marginTop = "0px"
    
			STools.applyStyle (head.rows[0].cells[2], this.ui.config.headCell1SelectedStyle, this.ui.config.headCell1SelectedClass);
			STools.applyStyle (head.rows[1].cells[1], this.ui.config.headCell1SelectedStyle, this.ui.config.headCell1SelectedClass);
			STools.applyStyle (head.rows[2].cells[0], this.ui.config.headCell1SelectedStyle, this.ui.config.headCell1SelectedClass);
			STools.applyStyle (head.rows[2].cells[3], this.ui.config.headCell2SelectedStyle, this.ui.config.headCell2SelectedClass);
			STools.applyStyle (head.rows[1].cells[3], this.ui.config.headCell3SelectedStyle, this.ui.config.headCell3SelectedClass);
			STools.applyStyle (head.rows[2].cells[4], this.ui.config.headCell3SelectedStyle, this.ui.config.headCell3SelectedClass);
			STools.applyStyle (head.rows[1].cells[2], this.ui.config.headCell4SelectedStyle, this.ui.config.headCell4SelectedClass);
			STools.applyStyle (head.rows[2].cells[1], this.ui.config.headCell4SelectedStyle, this.ui.config.headCell4SelectedClass);
			STools.applyStyle (head.rows[2].cells[2], this.ui.config.headCell5SelectedStyle, this.ui.config.headCell5SelectedClass);
			STools.applyStyle (head.rows[3].cells[0], this.ui.config.headCell1SelectedStyle, this.ui.config.headCell1SelectedClass);
			STools.applyStyle (head.rows[3].cells[1], this.ui.config.headCell4SelectedStyle, this.ui.config.headCell4SelectedClass);
			STools.applyStyle (head.rows[3].cells[2], this.ui.config.headCell4SelectedStyle, this.ui.config.headCell4SelectedClass);
			STools.applyStyle (head.rows[3].cells[3], this.ui.config.headCell2SelectedStyle, this.ui.config.headCell2SelectedClass);
			STools.applyStyle (head.rows[3].cells[4], this.ui.config.headCell3SelectedStyle, this.ui.config.headCell3SelectedClass);
		}
		else
		{
			head.style.marginLeft = "1px"			
			head.style.marginTop = "2px"			

			STools.applyStyle (head.rows[2].cells[0], this.ui.config.headCell1Style, this.ui.config.headCell1Class);
			STools.applyStyle (head.rows[2].cells[3], this.ui.config.headCell2Style, this.ui.config.headCell2Class);
			STools.applyStyle (head.rows[1].cells[3], this.ui.config.headCell3Style, this.ui.config.headCell3Class);
			STools.applyStyle (head.rows[2].cells[4], this.ui.config.headCell3Style, this.ui.config.headCell3Class);
			STools.applyStyle (head.rows[1].cells[2], this.ui.config.headCell4Style, this.ui.config.headCell4Class);
			STools.applyStyle (head.rows[2].cells[1], this.ui.config.headCell4Style, this.ui.config.headCell4Class);
			STools.applyStyle (head.rows[2].cells[2], this.ui.config.headCell5Style, this.ui.config.headCell5Class);
			STools.applyStyle (head.rows[3].cells[0], this.ui.config.headCell1Style, this.ui.config.headCell1Class);
			STools.applyStyle (head.rows[3].cells[1], this.ui.config.headCell1Style, this.ui.config.headCell1Class);
			STools.applyStyle (head.rows[3].cells[2], this.ui.config.headCell1Style, this.ui.config.headCell1Class);
			STools.applyStyle (head.rows[3].cells[3], this.ui.config.headCell1Style, this.ui.config.headCell1Class);
			STools.applyStyle (head.rows[3].cells[4], this.ui.config.headCell1Style, this.ui.config.headCell1Class);
		}
	}
	
	if (this.listener != null && this.listener.onTabSelected != null)
		this.listener.onTabSelected(index);
}

/* -----------------------------
	CONFIG
   ----------------------------- */
STab.Config = function () 
{	
	this.globalTableStyle = {
		padding: "0px",
		margin: "0px",
		borderCollapse: "collapse"
	}
	this.globalTableClass = "";
	this.globalHeadTableStyle = {
		padding: "0px",
		paddingLeft: "0px",
		margin: "0px",
		verticalAlign: "bottom"
	}
	this.globalHeadTableClass = "";

	this.imgStyle = {
		marginRight: "5px"
	}
	this.imgClass = "";
	
	this.headTableStyle = {
		float: "left",
		padding: "0px",
		margin: "0px",
		borderCollapse: "collapse"/*,
		verticalAlign: "bottom"*/
	}
	this.headTableClass = "stab_tabs";

	this.headCell1Style = {
		backgroundColor: "#ffffff"
	}
	this.headCell1Class = "";
	this.headCell2Style = {
		backgroundColor: "#aca899"
	}
	this.headCell2Class = "";
	this.headCell3Style = {
		backgroundColor: "#716f64"
	}
	this.headCell3Class = "";
	this.headCell4Style = {
		backgroundColor: "#f1efe2"
	}
	this.headCell4Class = "";
	this.headCell5Style = {
		paddingLeft: "5px",
		paddingRight: "5px",
		paddingBottom: "2px",
		backgroundColor: "#f1efe2",
		fontSize: "11px",
		fontFamily: "Verdana"
	}
	this.headCell5Class = "";

	this.headCell1SelectedStyle = {
		backgroundColor: "#ffffff"
	}
	this.headCell1SelectedClass = "";
	this.headCell2SelectedStyle = {
		backgroundColor: "#aca899"
	}
	this.headCell2SelectedClass = "";
	this.headCell3SelectedStyle = {
		backgroundColor: "#716f64"
	}
	this.headCell3SelectedClass = "";
	this.headCell4SelectedStyle = {
		backgroundColor: "#ece9d8"
	}
	this.headCell4SelectedClass = "";
	this.headCell5SelectedStyle = {
		paddingLeft: "5px",
		paddingRight: "5px",
		paddingBottom: "4px",
		backgroundColor: "#ece9d8",
		fontSize: "11px",
		fontFamily: "Verdana"
	}
	this.headCell5SelectedClass = "";

	this.contentStyle = {
		borderStyle: "solid",
		borderWidth: "1px",
		borderLeftColor: "#ffffff",
		borderBottomColor: "#716f64",
		borderRightColor: "#716f64",
		borderTopColor: "#ece9d8",
		backgroundColor: "#ece9d8",
		height: "100%",
		padding: "2px",
		verticalAlign: "top"
	}
	this.contentClass = "";

}
