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
 
SMenuBar.currentOpened = "";
SMenuBar.menus = new Array();
SMenuBar.bars = new Array();
SMenuBar.initialization = false;

/* -----------------------------
	MENUBAR
   ----------------------------- */
function SMenuBar(parent) {
	this.parent = parent;
	
	this.content = new Array();
	SMenuBar.bars.push(this);
	if (!SMenuBar.initialization)
	{
		SMenuBar.initialization = true;
		SMenu.createProtection();
		new SShortcut("Esc", function() {if (SMenuBar.currentOpened) SMenu.openClose (SMenuBar.currentOpened)});
	}
}

SMenuBar.prototype.addMenu = function(name) {
	var menu = new SMenu(this.parent, name);
	
	this.content.push(menu);
	SMenuBar.menus.push(menu);
	
	return menu;
}

SMenuBar.prototype.paint = function(parentElement) {
	parentElement.innerHTML = "";

	var table = SMenu.createTable();
    parentElement.appendChild(table);
	
	this.titleRow = table.insertRow(table.rows.length);
	this.menusRow = table.insertRow(table.rows.length);
		this.menusRow.style.height = "0px";

	for (var i=0; i<this.content.length; i++) {
		this.content[i].paint(this.titleRow, this.menusRow);
	}
}

/* -----------------------------
	MENU
   ----------------------------- */
function SMenu (parent, text) {
	this.parent = parent;
	this.text = text;
	
	this.content = new Array();
	
	this.action = new Object();
	this.action.act = function() {};
	this.action.isEnabled = function () {return true;};
	this.action.isEnabledArg = null;
}

SMenu.prototype.addEntry = function(text, img, action, shortcut, defaultEntry) {
	var entry = new Object();
	entry.parent = this.parent;
	entry.content = new Array();
	entry.defaultEntry = defaultEntry;
	
	entry.text = text;
	
	if (typeof img == "string" || img == undefined) {
		entry.img = img;
		entry.disabledImg = img;
	} else {
		entry.img = img.normal;
		entry.disabledImg = img.disabled;
	}

	entry.action = new Object();
	if (typeof action == "function" || action == undefined) {
		entry.action.act = action;
		entry.action.isEnabled = function () {return true;};
		entry.action.isEnabledArg = null;
	} else {
		entry.action.act = action.act;
		entry.action.isEnabled = action.isEnabled;
		entry.action.isEnabledArg = action.isEnabledArg;
	}
		
	if (shortcut)
		entry.shortcut = new SShortcut (shortcut, entry.action);
	 
	this.content.push(entry);
	return entry;
}

SMenu.prototype.addSeparator = function() {
	var entry = new Object();
	entry.parent = this.parent;
	entry.separator = true;
	entry.content = new Array();

	this.content.push(entry);
	return entry;
}

SMenu.prototype.addSubMenu = function(text) {
	var entry = new SMenu(this.parent, text);
	this.content.push(entry);
	return entry;
}

SMenu.prototype.paint = function(titleRow, menusRow) {
	this.titleRow = titleRow;
	this.menusRow = menusRow;
	this.createMenu();
}

SMenu.createTable = function() 
{
	var table = document.createElement("TABLE");
		table.style.position = "relative";
		table.style.zIndex = "+15";
		table.cellSpacing = "0";
		table.cellPadding = "0";
		table.border = "0";
	
	return table;
}

SMenu.createProtection = function()
{
    SMenu._sprotect = new SProtectLayer();
    SMenu._sprotect.setLevel (10);
    SMenu._sprotect.setOpacity (0);
    SMenu._sprotect.setEventListener ("onclick", SMenu_openCloseCurrent);
}
function SMenu_openCloseCurrent()
{
  SMenu.openClose(SMenuBar.currentOpened);
}

SMenu.prototype.createMenu = function()
{
	var col1 = document.createElement("TD");
		col1.innerHTML = this.text;
		STools.applyStyle(col1, this.parent.ui.config.menuStyle, this.parent.ui.config.menuClass);
		col1.smenu = this;
		col1.style.whiteSpace = "nowrap";
		col1.onclick = function () {
			SMenu.openClose(this);
		};
		col1.onmouseover = function () {
			STools.applyStyle(this, this.smenu.parent.ui.config.overMenuStyle, this.smenu.parent.ui.config.overMenuClass);
			if (SMenuBar.currentOpened != "" && SMenuBar.currentOpened != this)
				SMenu.openClose(this);
		};
		col1.onmouseout = function () {
			if (this != SMenuBar.currentOpened) 
				STools.applyStyle(this, this.smenu.parent.ui.config.menuStyle, this.smenu.parent.ui.config.menuClass);
		};
	this.titleRow.appendChild(col1);

	var col2 = document.createElement("TD");
		col2.style.whiteSpace = "nowrap";
		col2.style.height = "0px";
		col2.appendChild(this.createSubMenu(this, col1));
	this.menusRow.appendChild(col2);
}

SMenu.prototype.createSubMenu = function(menu, parent)
{
	var table = SMenu.createTable();
	if (!STools.is_ie)
	{
      table.width="auto"
	}

	STools.applyStyle(table, this.parent.ui.config.subMenuStyle, this.parent.ui.config.subMenuClass);
	table.style.paddingRight = "0px"
	table.style.paddingLeft = "0px"
	table.style.paddingTop = "0px"
	table.style.paddingBottom = "0px"
	
	for (var i=0; i<menu.content.length; i++)
	{
		if (menu.content[i].content.length == 0)
		{
			var row = table.insertRow(table.rows.length);
				row.option = menu.content[i];
			menu.content[i].ui = row;

			if (menu.content[i].separator)
			{
				var col = document.createElement("TD");
					col.colSpan = "4";
					col.style.height = "5px";
				row.appendChild(col);
				
				var tableS = SMenu.createTable();
					tableS.style.width = "100%";
					tableS.style.height = "100%";

				STools.applyStyle(tableS, this.parent.ui.config.subElementSeparatorStyle, this.parent.ui.config.subElementSeparatorClass);

				var rowS = tableS.insertRow(tableS.rows.length)
				var colS = document.createElement("TD");
				rowS.appendChild(colS);

				col.appendChild(tableS);
			}				
			else
			{
				for (var j=0; j<5; j++)
				{
					var col = document.createElement("TD");
					row.appendChild(col);

                    if (menu.content[i].action.isEnabled(menu.content[i].action.isEnabledArg))
                    {
					    STools.applyStyle(col, this.parent.ui.config.subElementMenuStyle, this.parent.ui.config.subElementMenuClass);
              		    col.onclick = function() {
      	 					   SMenu.openClose(this.parent);
      	 					   this.onclickaction();
      	 				      };
      	 		      col.onmouseover = function () {
      	 				        STools.applyStyle(this.parentNode.childNodes[0], this.smenu.parent.ui.config.overSubElementMenuStyle, this.smenu.parent.ui.config.overSubElementMenuClass);
      	 					    STools.applyStyle(this.parentNode.childNodes[1], this.smenu.parent.ui.config.overSubElementMenuStyle, this.smenu.parent.ui.config.overSubElementMenuClass);
      						    STools.applyStyle(this.parentNode.childNodes[2], this.smenu.parent.ui.config.overSubElementMenuStyle, this.smenu.parent.ui.config.overSubElementMenuClass);
      						    STools.applyStyle(this.parentNode.childNodes[3], this.smenu.parent.ui.config.overSubElementMenuStyle, this.smenu.parent.ui.config.overSubElementMenuClass);
      						    STools.applyStyle(this.parentNode.childNodes[4], this.smenu.parent.ui.config.overSubElementMenuStyle, this.smenu.parent.ui.config.overSubElementMenuClass);
      						    this.parentNode.childNodes[0].style.paddingRight = "0px";
                                this.parentNode.childNodes[0].style.paddingLeft = "2px";
      						    this.parentNode.childNodes[3].style.paddingRight = "0px";
      						    this.parentNode.childNodes[4].style.paddingLeft = "0px";
      						    this.parentNode.childNodes[4].style.paddingRight = "0px";
      
      						    SMenu.allSubMenuClose(this, this.parentNode.parentNode.parentNode);
      
      					     };
      				  col.onmouseout = function () {
      					     	STools.applyStyle(this.parentNode.childNodes[0], this.smenu.parent.ui.config.subElementMenuStyle, this.smenu.parent.ui.config.subElementMenuClass);
      						    STools.applyStyle(this.parentNode.childNodes[1], this.smenu.parent.ui.config.subElementMenuStyle, this.smenu.parent.ui.config.subElementMenuClass);
      						    STools.applyStyle(this.parentNode.childNodes[2], this.smenu.parent.ui.config.subElementMenuStyle, this.smenu.parent.ui.config.subElementMenuClass);
      						    STools.applyStyle(this.parentNode.childNodes[3], this.smenu.parent.ui.config.subElementMenuStyle, this.smenu.parent.ui.config.subElementMenuClass);
      						    STools.applyStyle(this.parentNode.childNodes[4], this.smenu.parent.ui.config.subElementMenuStyle, this.smenu.parent.ui.config.subElementMenuClass);
                                this.parentNode.childNodes[0].style.paddingLeft = "2px";
      						    this.parentNode.childNodes[0].style.paddingRight = "0px";
      						    this.parentNode.childNodes[3].style.paddingRight = "0px";
      						    this.parentNode.childNodes[4].style.paddingLeft = "0px";
      						    this.parentNode.childNodes[4].style.paddingRight = "0px";
      					     };
                    }
					else
					{
					   STools.applyStyle(col, this.parent.ui.config.disabledSubElementMenuStyle, this.parent.ui.config.disabledSubElementMenuClass);
                    }

					col.smenu = this;
					col.parent = parent;
					col.style.whiteSpace = "nowrap";
					col.onclickaction = menu.content[i].action.act;

					switch (j) {
						case 0:
							if (menu.content[i].img)
							{
								var img = document.createElement("img");
								img.width = "16";
								img.height = "16";
								img.src = menu.content[i].img;
								col.appendChild(img);
                                col.style.paddingLeft = "2px";
								col.style.paddingRight = "0px";
							}
							else
							{
                                var img = document.createElement("img");
                                img.width = "16";
                                img.height = "0px";
                                col.appendChild(img);
                                col.style.paddingLeft = "2px";
								col.style.paddingRight = "0px";
							}
							break;
						case 1:
						        if (menu.content[i].defaultEntry == true) 
                                                                col.style.fontWeight = "bold"
                               col.innerHTML = menu.content[i].text;
							break;
						case 2:
							if (menu.content[i].shortcut) {
								col.appendChild(document.createTextNode(menu.content[i].shortcut.key));
							}
							break;
						case 3:
								col.style.paddingRight = "0px";
							break;
						case 4:
								col.style.paddingLeft = "0px";
								col.style.paddingRight = "0px";
							break;
					}
				}
			}
		} 
		else 
		{
			var row = table.insertRow(table.rows.length);
				row.option = menu.content[i];
				row.submenu = this.createSubMenu (menu.content[i], parent);

				for (var j=0; j<5; j++)
				{
					var col = document.createElement("TD");
					row.appendChild(col);
						
					STools.applyStyle(col, this.parent.ui.config.subElementMenuStyle, this.parent.ui.config.subElementMenuClass);
					col.smenu = this;
					col.parent = parent;
					col.style.whiteSpace = "nowrap";
					col.onmouseover = function () {
						STools.applyStyle(this.parentNode.childNodes[0], this.smenu.parent.ui.config.overSubElementMenuStyle, this.smenu.parent.ui.config.overSubElementMenuClass);
						STools.applyStyle(this.parentNode.childNodes[1], this.smenu.parent.ui.config.overSubElementMenuStyle, this.smenu.parent.ui.config.overSubElementMenuClass);
						STools.applyStyle(this.parentNode.childNodes[2], this.smenu.parent.ui.config.overSubElementMenuStyle, this.smenu.parent.ui.config.overSubElementMenuClass);
						STools.applyStyle(this.parentNode.childNodes[3], this.smenu.parent.ui.config.overSubElementMenuStyle, this.smenu.parent.ui.config.overSubElementMenuClass);
						STools.applyStyle(this.parentNode.childNodes[4], this.smenu.parent.ui.config.overSubElementMenuStyle, this.smenu.parent.ui.config.overSubElementMenuClass);

						if (this.parentNode.submenu.style.display == 'none')
							SMenu.allSubMenuClose(this, this.parentNode.parentNode.parentNode);

						this.parentNode.childNodes[0].style.paddingRight = "0px";
                        this.parentNode.childNodes[0].style.paddingLeft = "2px";
						this.parentNode.childNodes[3].style.paddingRight = "0px";
						this.parentNode.childNodes[4].style.paddingLeft = "0px";
						this.parentNode.childNodes[4].style.paddingRight = "0px";

						this.parentNode.childNodes[3].childNodes[0].src = STools.Ressource + "submenu_high.gif";
						this.parentNode.childNodes[4].childNodes[0].style.display = 'block';
					};
					col.onmouseout = function () {
						if (this.parentNode.submenu.style.display != 'none')
							return;
						
						STools.applyStyle(this.parentNode.childNodes[0], this.smenu.parent.ui.config.subElementMenuStyle, this.smenu.parent.ui.config.subElementMenuClass);
						STools.applyStyle(this.parentNode.childNodes[1], this.smenu.parent.ui.config.subElementMenuStyle, this.smenu.parent.ui.config.subElementMenuClass);
						STools.applyStyle(this.parentNode.childNodes[2], this.smenu.parent.ui.config.subElementMenuStyle, this.smenu.parent.ui.config.subElementMenuClass);
						STools.applyStyle(this.parentNode.childNodes[3], this.smenu.parent.ui.config.subElementMenuStyle, this.smenu.parent.ui.config.subElementMenuClass);
						STools.applyStyle(this.parentNode.childNodes[4], this.smenu.parent.ui.config.subElementMenuStyle, this.smenu.parent.ui.config.subElementMenuClass);
						this.parentNode.childNodes[0].style.paddingRight = "0px";
                        this.parentNode.childNodes[0].style.paddingLeft = "2px";
						this.parentNode.childNodes[3].style.paddingRight = "0px";
						this.parentNode.childNodes[4].style.paddingLeft = "0px";
						this.parentNode.childNodes[4].style.paddingRight = "0px";
						this.parentNode.childNodes[3].childNodes[0].src = STools.Ressource + "submenu.gif";
					};
					
					switch (j) {
						case 0:
							if (menu.content[i].img)
							{
								var img = document.createElement("img");
								img.width = "16";
								img.height = "16";
								img.src = menu.content[i].img;
								col.appendChild(img);
                                col.style.paddingLeft = "2px";
								col.style.paddingRight = "0px";
							}
							else
							{
                                var img = document.createElement("img");
                                img.width = "16";
                                img.height = "0px";
                                col.appendChild(img);
                                col.style.paddingLeft = "2px";
								col.style.paddingRight = "0px";
							}
							break;
						case 1:
							col.innerHTML = menu.content[i].text;
							break;
						case 2:
							break;
						case 3:
							var img = document.createElement("img");
								img.width = "13";
								img.height = "13";
								img.src = STools.Ressource + "submenu.gif";
							col.appendChild(img);
							col.style.paddingRight = "0px";
							break;
						case 4:
							col.appendChild(row.submenu);
							col.style.paddingLeft = "0px";
							col.style.paddingRight = "0px";
							col.style.verticalAlign = "top";
							break;
					}
				}
				
		}
	}
  
	return table;
}

SMenu.allSubMenuClose = function (th, menu)
{
	var rows = menu.childNodes[0];

	for (var i=0; i<rows.childNodes.length; i++)
	{
		if (rows.childNodes[i].submenu && rows.childNodes[i].submenu.style.display != 'none')
		{
			SMenu.allSubMenuClose (th, rows.childNodes[i].submenu);
			
			rows.childNodes[i].childNodes[3].childNodes[0].src = STools.Ressource + "submenu.gif";;
			rows.childNodes[i].submenu.style.display = 'none';
			
			STools.applyStyle(rows.childNodes[i].childNodes[0], th.smenu.parent.ui.config.subElementMenuStyle, th.smenu.parent.ui.config.subElementMenuClass);
			STools.applyStyle(rows.childNodes[i].childNodes[1], th.smenu.parent.ui.config.subElementMenuStyle, th.smenu.parent.ui.config.subElementMenuClass);
			STools.applyStyle(rows.childNodes[i].childNodes[2], th.smenu.parent.ui.config.subElementMenuStyle, th.smenu.parent.ui.config.subElementMenuClass);
			STools.applyStyle(rows.childNodes[i].childNodes[3], th.smenu.parent.ui.config.subElementMenuStyle, th.smenu.parent.ui.config.subElementMenuClass);
			STools.applyStyle(rows.childNodes[i].childNodes[4], th.smenu.parent.ui.config.subElementMenuStyle, th.smenu.parent.ui.config.subElementMenuClass);
			rows.childNodes[i].childNodes[0].style.paddingRight = "0px";
            rows.childNodes[i].childNodes[0].style.paddingLeft = "2px";
			rows.childNodes[i].childNodes[3].style.paddingRight = "0px";
			rows.childNodes[i].childNodes[4].style.paddingLeft = "0px";
			rows.childNodes[i].childNodes[4].style.paddingRight = "0px";
		}
	}
}
SMenu.subMenuClose = function (menu)
{
	var rows = menu.childNodes[0];
	
	for (var i=0; i<rows.childNodes.length; i++)
	{
		if (rows.childNodes[i].submenu != undefined && rows.childNodes[i].submenu.style.display != 'none')
		{
			SMenu.subMenuClose (rows.childNodes[i].submenu);
			rows.childNodes[i].childNodes[3].childNodes[0].src = STools.Ressource + "submenu.gif";;
			rows.childNodes[i].submenu.style.display = 'none';
			rows.childNodes[i].childNodes[3].onmouseout();
		}
	}
}

SMenu.openClose = function(menu) 
{
	for (var j=0; j<SMenuBar.bars.length; j++) 
	{
		var menuList = SMenuBar.bars[j].titleRow;
		var subMenuList = SMenuBar.bars[j].menusRow;
		
		if (menuList == null)
		      continue;

		for (var i=0; i<menuList.childNodes.length; i++) 
		{
			var currentMenu = menuList.childNodes[i];
			var currentSubMenu = subMenuList.childNodes[i].childNodes[0];

			SMenu.subMenuClose(currentSubMenu)

			if (currentMenu == menu) 
			{
				if (currentSubMenu.style.display != 'block')
				{
					SMenuBar.currentOpened = menu;
					STools.applyStyle(currentMenu, SMenuBar.menus[j].parent.ui.config.overMenuStyle, SMenuBar.menus[j].parent.ui.config.overMenuClass);
					currentSubMenu.style.display = 'block';
					
                    SMenu._sprotect.show();
				}
				else
				{
					SMenuBar.currentOpened = "";
					STools.applyStyle(currentMenu, SMenuBar.menus[j].parent.ui.config.menuStyle, SMenuBar.menus[j].parent.ui.config.menuClass);
					currentSubMenu.style.display = 'none';

                    SMenu._sprotect.hide();
				}
			} 
			else 
			{
				STools.applyStyle(currentMenu, SMenuBar.menus[j].parent.ui.config.menuStyle, SMenuBar.menus[j].parent.ui.config.menuClass);
				currentSubMenu.style.display = 'none';
			}
		}
	}
}
