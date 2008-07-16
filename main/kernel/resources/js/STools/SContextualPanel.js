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
 
SContextualPanel.css = "";

function SContextualPanel(id, config, _document) {
	this.ui = new Object();
    this._document = _document == null ? document : _document;
	this.ui.element = (typeof id == "string") ? this._document.getElementById(id) : id;
	this.ui.config = config != null ? config : new SContextualPanel.Config();
    this.ui.categories = new Array();

	STools.loadStyle (this._document, SContextualPanel.css + "scontextualpanel.css")
}

SContextualPanel.prototype.addCategory = function(caption, visibleAtStartup, openedAtStartup) {
	var category = new SContextualPanelCategory(this, caption, visibleAtStartup, openedAtStartup);

	this.ui.categories.push(category);

	return category;
}

SContextualPanel.openClose = function ()
{
        var cell = this;
        var tableToHide = cell.tableToHide;

        if (tableToHide.style.display != "")
        {
                tableToHide.style.display = "";
        }
        else
        {
                tableToHide.style.display = "none";
        }

}

SContextualPanel.prototype.paint = function ()
{
        STools.applyStyle (this.ui.element, this.ui.config.englobingCellStyle, "ContextualAction");

        for (var c=0; c < this.ui.categories.length; c++)
        {
           var cat = this.ui.categories[c];

           cat.div = this._document.createElement ("div");
           this.ui.element.appendChild(cat.div);

           /* ************************************ */
           /* ************************************ */
           var tableTitle = this._document.createElement ("table");
           STools.applyStyle (tableTitle, this.ui.config.categoryTitleTableStyle, this.ui.config.categoryTitleTableClass);
           if (!cat.visibleAtStartup)
                tableTitle.style.display = "none";

           /* ************************************ */
           var preTitleRow = tableTitle.insertRow(0);

           var preTitleCell = this._document.createElement ("td");
           preTitleCell.style.height = "6px";
           preTitleCell.style.backgroundImage = "url('" + STools.Ressource + "cat_top.gif')";
           preTitleCell.style.backgroundPosition = "0 0";
           preTitleCell.style.backgroundRepeat = "no-repeat";
           preTitleRow.appendChild(preTitleCell);

           /* ************************************ */
           var titleRow = tableTitle.insertRow(1);

           var titleCell = this._document.createElement ("td");
           titleRow.appendChild(titleCell);
           titleCell.style.backgroundImage = "url('" + STools.Ressource + "cat_bcg.gif')";
           titleCell.appendChild(this._document.createTextNode(cat.caption));
           STools.applyStyle (titleCell, this.ui.config.titleCellStyle, this.ui.config.titleCellClass);
           titleCell.onclick = SContextualPanel.openClose

           cat.div.appendChild(tableTitle);

           /* ************************************ */
           /* ************************************ */
           var table = this._document.createElement ("table");
           titleCell.tableToHide = table;
           cat.table = table;
           STools.applyStyle (table, this.ui.config.categoryTableStyle, this.ui.config.categoryTableClass);
           if (!cat.openedAtStartup || !cat.visibleAtStartup)
                table.style.display = "none";

           var row = table.insertRow(table.rows.length);
           var cell = this._document.createElement ("td")
           cell.style.height = "11px";
           cell.appendChild (this._document.createTextNode(" "));
           row.appendChild(cell);

           for (var e=0; e < cat.elements.length; e++)
           {
               var elt = cat.elements[e];

               var row = table.insertRow(table.rows.length);
               var cell = this._document.createElement ("td")
               if (typeof elt == "string")
                        cell.innerHTML = elt;
               else
                        cell.appendChild(elt);
               STools.applyStyle (cell, this.ui.config.categoryCellStyle, this.ui.config.categoryCellClass);
               row.appendChild(cell);
           }

           row = table.insertRow(table.rows.length);
           cell = this._document.createElement ("td")
           cell.style.height = "11px";
           cell.appendChild (this._document.createTextNode(" "));
           row.appendChild(cell);

           cat.div.appendChild(table);
        }
}

function SContextualPanelCategory (panel, caption, visibleAtStartup, openedAtStartup)
{
        this.panel = panel;
	this.caption = caption;
	this.visibleAtStartup = visibleAtStartup != false;
	this.openedAtStartup = openedAtStartup != false;
	this.elements = new Array();
}

SContextualPanelCategory.prototype.showHide = function (show)
{
        if ((this.div.style.display == "" && show == null) || (show != null && !show))
        {
                this.div.style.display = "none";
        }
        else
        {
                this.div.style.display = "";
        }
}

SContextualPanelCategory.prototype.showHideElement = function (index, show)
{
        if ((show == null && this.table.rows[index+1].style.display == "") || (show != null && show == false))
        {
                this.table.rows[index+1].style.display = "none"
        }
        else
        {
                this.table.rows[index+1].style.display = ""
        }
}

SContextualPanelCategory.prototype.addElement = function (html)
{
        this.elements.push (html);
}

SContextualPanelCategory.prototype.addLink = function (text, img, act)
{
        var span = this.panel._document.createElement("span");

        if (img)
        {
                var image = this.panel._document.createElement("img");
                image.src = img;
                span.appendChild (image);
        }

        var link = this.panel._document.createElement("a");
        link.innerHTML = text;
        link.href = "#";
        link.act = act;
        link.onclick = function () { this.act(); return false; };
        span.appendChild (link);

        this.elements.push (span);
}


/* -----------------------------
	CONFIG
   ----------------------------- */
SContextualPanel.Config = function ()
{
  	this.englobingCellStyle = {
		padding: "12px",
		paddingTop: "0px",
		margin: "0px",
		borderCollapse: "collapse",
		backgroundColor: "#e1e1e1",
		width: "206px",
		overflow: "hidden",
		verticalAlign: "top"
	}

    	this.categoryTitleTableStyle = {
    	        height: "23px",
    	        width: "206px",
    	        backgroundColor: "#ffffff",
    	        borderCollapse: "collapse",
    	        padding: "0px",
    	        margin: "0px",
    	        marginTop: "12px"
	}
  	this.categoryTitleTableClass = "";

    	this.categoryTableStyle = {
    	        width: "206px",
    	        backgroundColor: "#f0f1f5",
    	        borderCollapse: "collapse",
    	        borderStyle: "solid",
    	        borderTopStyle: "none",
    	        borderWidth: "1px",
    	        borderColor: "#cdcdcd",
    	        padding: "0px",
    	        margin: "0px"
	}
  	this.categoryTableClass = "";

    	this.categoryCellStyle = {
    	        padding: "1px",
    	        paddingLeft: "12px",
    	        paddingRight: "12px",
    	        margin: "0px",
    	        verticalAlign: "top",
    	        textAlign: "left",
    	        color: "#215dc6",
    	        fontFamily: "Verdana",
    	        fontSize: "11px",
    	        fontWeight: "normal"
        }
  	this.categoryCellClass = "";

    	this.titleCellStyle = {
    	        padding: "0px",
    	        margin: "0px",
    	        backgroundRepeat: "no-repeat",
    	        backgroundPosition: "0 0",
    	        verticalAlign: "top",
    	        paddingLeft: "12px",
    	        fontFamily: "Verdana",
    	        fontSize: "11px",
    	        fontWeight: "bold",
    	        color: "#215dc6"
	}
  	this.titleCellClass = "";
}
