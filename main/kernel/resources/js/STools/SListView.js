SListView.css = "resources/css/";
SListView.listViewTrunc = 22;
SListView.iconViewTrunc = 20;
SListView.thumbViewTrunc = 22;
SListView.mosaicViewTrunc = 22;
SListView.detailViewTrunc = 6;
SListView.viewerNoPreviewMessage = "No preview for this element";

function SListView(id, _document, listener)
{
	this.ui = {};
	this.ui.document = _document ? _document : document;
	this.ui.mainElement = (typeof id == "string") ? this.ui.document.getElementById(id) : id;

	STools.loadStyle (this.ui.document, SListView.css + "slistview.css");

	this.ui.mainElement.slistview = this;
	this.ui.mainElement.className = "SListView";
	this.ui.mainElement.oncontextmenu = function () {return false; };
	this.ui.mainElement.onclick = function (e, force) { 
		var elt = STools.is_ie ? this.slistview.ui.document.parentWindow.event.srcElement : e.originalTarget;
		if (elt != this && force != true)
			return;
	
		var controlKey = ((STools.is_ie && this.slistview.ui.document.parentWindow.event.ctrlKey ) || (!STools.is_ie && e.ctrlKey))
		if (!this.slistview.multipleSelection || !controlKey)
		{
			this.slistview.unselect();
		}
	};

	this.listener = listener;
	this.elements = new Array();
	this.columns = new Array();
	this.showHeaders(true);
	this.selection = new Array();
	this.group = null;
	this.showGroups(false);

	this.sort();
	this.setView();
	this.setMultipleSelection();

	this.elementsAreSorted = true;
}

SListView.prototype.sort = function (active, by, ascending)
{
	if (this.sortCriteria == null)
		this.sortCriteria = {};
	this.sortCriteria.active = active != null ? (active == true) : true;
	this.sortCriteria.by = by; // null means by name, another string means by property of this value
	this.sortCriteria.ascending = ascending != null ? (ascending == true) : true;

	this.elementsAreSorted = false;
}

SListView.prototype.setMultipleSelection = function (value)
{
	this.multipleSelection = value != false;
}

SListView.prototype.showHeaders = function (value)
{
	this.showColumns = value != false;
}

SListView.prototype.setGroup = function (group)
{
	this.group = group;
}

SListView.prototype.showGroups = function (showGroup)
{
	this.showGroup = showGroup == true;
}

SListView.prototype.setView = function (view)
{
	switch (view)
	{
		case "icon":
		case "list":
		case "detail":
		case "thumb":
		case "viewer":
			this.currentView = view;
			break;
		default:
			this.currentView = "mozaic";
	}
}

SListView.prototype.addColumn = function (id, name, showInMozaicView, size, align)
{
	var val = {};
	val.id = id;
	val.name = name;
	val.showInMozaicView = showInMozaicView;
	val.size = parseInt(size != null ? size : "100px") + (STools.is_ie ? 0 : -15) + "px";
	val.align = align != null ? align : "left";

	this.columns.push(val);
}

// Paint needed
SListView.prototype.addElement = function (name, icon16, icon32, icon50, properties, tooltip)
{
	var element = new Object();
	element.name = name;
	element.icon16 = icon16;
	element.icon32 = icon32;
	element.icon50 = icon50;
	element.properties = properties;
    element.tooltip = tooltip;
	element.selected = false;
	element.listView = this;
	element.select = function (controlKey) { this.listView.select(this.representation, controlKey); };
	element.unselect = function (controlKey) { this.listView.unselect(this.representation); };
	element.isSelected = function () { return this.listView.isSelected(this.representation); };
	element.remove = function () { return this.listView.removeElement(this.representation); };
	element.rename = function () { return this.listView.renameElement(this.representation); };
	element.paint = function () { return this.listView.paintElement(this.representation); };

	this.elements.push(element);

	this.elementsAreSorted = false;

	return element;
}

SListView.prototype.renameElement = function(td)
{
	if (this.listener != null && this.listener.onBeforeEditLabel != null && this.listener.onBeforeEditLabel(td.element) == false)
		return;

	var input = this.ui.document.createElement('input');
	input.type = 'text';
	input.value = td.element.name;
	input.td = td;
	input.onblur = function () {
			this.td.element.listView.renamedElement(this, true);
		}
	input.onkeydown = function (e) {
			var key = STools.is_ie ? this.td.element.listView.ui.document.parentWindow.event.keyCode : e.keyCode;
			if (key == 13)
			{
				this.td.element.listView.renamedElement(this, true);
			}
			if (key == 27)
			{
				this.td.element.listView.renamedElement(this, false);
			}
		}

	if (this.currentView == "mozaic")
	{
		var itable = STools.getParentWithTag(td, 'table');
	        input.itable = itable;

		var cell = itable.parentNode;
		itable.style.display = 'none';

		cell.appendChild(input);
	}
	else if (this.currentView == "icon")
	{
		var itable = STools.getParentWithTag(td, 'table');
	        input.itable = itable;

		var cell = itable.parentNode;
		itable.style.display = 'none';

		cell.appendChild(input);
        }
	else if (this.currentView == "thumb")
	{
		var itable = STools.getParentWithTag(td, 'table');
	        input.itable = itable;

		var cell = itable.parentNode;
		itable.style.display = 'none';

		cell.appendChild(input);
        }
	else if (this.currentView == "viewer")
	{
		var itable = STools.getParentWithTag(td, 'table');
	        input.itable = itable;

		var cell = itable.parentNode;
		itable.style.display = 'none';

		cell.appendChild(input);
        }
	else if (this.currentView == "list")
	{
		var itable = STools.getParentWithTag(td, 'table');
	        input.itable = itable;

		var cell = itable.parentNode;
		itable.style.display = 'none';

		cell.appendChild(input);
        }
	else if (this.currentView == "detail")
	{
		var itr = STools.getParentWithTag(td, 'tr');
	        input.itr = itr;

		itr.cells[1].style.display = 'none';
		var ntd = this.ui.document.createElement("td")
		itr.appendChild(ntd);
		ntd.appendChild(input);
		
		input.style.width = (parseInt(td.column.size) - 20) + "px";
        }	
        else
	{
		alert("Error on 'SListView.renameElement'. Unsupported view " + this.currentView);
	}
        input.onfocus = function () { try {this.select ();} catch (e) {} }
	try { input.focus (); } catch (e) {}
}

SListView.prototype.renamedElement = function(input, ok)
{
	if (this.currentView == "mozaic")
	{
		input.itable.style.display = "";
	}
	else if (this.currentView == "icon")
	{
		input.itable.style.display = "";
	}
	else if (this.currentView == "thumb")
	{
		input.itable.style.display = "";
	}
	else if (this.currentView == "viewer")
	{
		input.itable.style.display = "";
	}
	else if (this.currentView == "list")
	{
		input.itable.style.display = "";
	}
	else if (this.currentView == "detail")
	{
		input.itr.cells[1].style.display = "";
        input.itr.removeChild(input.itr.cells[2]);
	}
	else
	{
		alert("Error on 'SListView.renamedElement'. Unsupported view " + this.currentView);
	}

	input.parentNode.removeChild(input);
	
	if (ok != false)
	{
		input.td.element.name = input.value
		input.td.element.paint();
	}
		
	if (ok == false && this.listener != null && this.listener.onCancelEditLabel != null)
		this.listener.onCancelEditLabel(input.td.element);
		
	if (ok != false && this.listener != null && this.listener.onEditLabel != null)
		this.listener.onEditLabel(input.td.element);

}

// Auto paint
SListView.prototype.removeElement = function(td)
{
	td.element.unselect();
	for (var i=0; i<this.elements.length; i++)
	{
		if (this.elements[i] == td.element)
		{
			// Remove from the group, if any
			if (td.element.groupUI != null)
			{
				td.element.groupUI.attached--;
				// Clear also the group if the lastone
				if (td.element.groupUI.attached == 0)
					td.element.groupUI.parentNode.removeChild(td.element.groupUI);
			}

			if (this.currentView == "mozaic")
			{
				var itable = STools.getParentWithTag(this.elements[i].representation, 'table');
				var table = STools.getParentWithTag(itable.parentNode, 'table');

				table.parentNode.removeChild(table);
			}
			else if (this.currentView == "icon")
			{
				var itable = STools.getParentWithTag(this.elements[i].representation, 'table');
				var table = STools.getParentWithTag(itable.parentNode, 'table');

				table.parentNode.removeChild(table);
                        }
			else if (this.currentView == "thumb")
			{
				var itable = STools.getParentWithTag(this.elements[i].representation, 'table');
				var table = STools.getParentWithTag(itable.parentNode, 'table');

				table.parentNode.removeChild(table);
                        }
			else if (this.currentView == "viewer")
			{
				var itable = STools.getParentWithTag(this.elements[i].representation, 'table');
				var table = STools.getParentWithTag(itable.parentNode, 'table');
				var td = STools.getParentWithTag(table.parentNode, 'td');
                this.ui.previewElement.innerHTML = SListView.viewerNoPreviewMessage;
				td.parentNode.removeChild(td);
                        }
			else if (this.currentView == "list")
			{
				var itable = STools.getParentWithTag(this.elements[i].representation, 'table');
				var table = STools.getParentWithTag(itable.parentNode, 'table');

				table.parentNode.removeChild(table);
                        }
			else if (this.currentView == "detail")
			{
				var itable = STools.getParentWithTag(this.elements[i].representation, 'table');
				var tr = STools.getParentWithTag(itable.parentNode, 'tr');

				tr.parentNode.removeChild(tr);
                        }
			else
			{
				alert("Error on 'SListView.removeElement'. Unsupported view " + this.currentView);
			}

			for (var j=i+1; j<this.elements.length; j++)
			{
				this.elements[j-1] = this.elements[j];
			}
			this.elements.length--;
			
			return;
		}
	}
}

// function that compare 2 elements of the list for sort purposes
SListView.compare = function (elt1, elt2)
{
	if (elt1.listView.sortCriteria.by == null)
	{
		// compare by name
		if (elt1.name.toLowerCase() == elt2.name.toLowerCase())
			return 0;
		if (elt1.name.toLowerCase() < elt2.name.toLowerCase())
			return elt1.listView.sortCriteria.ascending ? -1 : 1;
		if (elt1.name.toLowerCase() > elt2.name.toLowerCase())
			return elt1.listView.sortCriteria.ascending ? 1 : -1;
	}
	else
	{
		if (elt1.properties[elt1.listView.sortCriteria.by].toLowerCase() == elt2.properties[elt1.listView.sortCriteria.by].toLowerCase())
			return 0;
		if (elt1.properties[elt1.listView.sortCriteria.by].toLowerCase() < elt2.properties[elt1.listView.sortCriteria.by].toLowerCase())
			return elt1.listView.sortCriteria.ascending ? -1 : 1;
		if (elt1.properties[elt1.listView.sortCriteria.by].toLowerCase() > elt2.properties[elt1.listView.sortCriteria.by].toLowerCase())
			return elt1.listView.sortCriteria.ascending ? 1 : -1;
	}
}

SListView.prototype.getElements = function()
{
	return this.elements;
}

SListView.prototype.getSelection = function()
{
	var sel = new Array();
	
	for (var i=0; i<this.selection.length; i++)
	{
		sel.push(this.selection[i].element);
	}	
	
	return sel;
}

SListView.prototype.isSelected = function (elt)
{
	return elt.element.selected;
}

SListView.prototype.unselect = function (elt)
{
	// d?selectionne tout
	if (elt == null)
	{
		while (this.selection.length != 0)
		{
			if (this.listener != null && this.listener.onUnselect != null)
				this.listener.onUnselect(this.selection[0].element);
	               
			this.selection[0].element.selected = false;
			this.selection[0].element.paint();
	        	this.selection[0] = this.selection[this.selection.length-1];
			this.selection.length--;
		}
	}
	// d?selectionne un seul
	else
	{
		for (var i=0; i<this.selection.length; i++)
		{
			if (this.selection[i] == elt)
			{
				if (this.listener != null && this.listener.onUnselect != null)
					this.listener.onUnselect(this.selection[i].element);

				this.selection[i].element.selected = false;
				this.selection[i].element.paint();
				this.selection[i] = this.selection[this.selection.length-1];
				this.selection.length--;
				break;
			}
		}
	}
}

SListView.prototype.selectAll = function (elt)
{
	this.selection = new Array();
	for (var i=0; i<this.elements.length; i++)
	{
		if (this.listener != null && this.listener.onSelect != null && !this.listener.onSelect(this.elements[i]))
			continue;

		this.elements[i].selected = true;
		this.elements[i].paint();
               	this.selection.push(this.elements[i].representation);		
        }
}

SListView.prototype.select = function (elt, controlKey, shiftKey)
{
    var oldLastSelection = this.selection.length > 0 ? this.selection[this.selection.length - 1].element : this.elements[0];
    // CTRL + SHIFT
	if (this.multipleSelection == true && controlKey == true && shiftKey == true)
	{
        // nothing, we only add selections from last to click
	}
    // CTRL
    else if (this.multipleSelection == true && controlKey == true)
    {
        // revert selection
        if (this.isSelected(elt))
        {
            this.unselect(elt);
            return;
        }
    }
    // SHIFT OR NOTHING
    else
	{
		this.unselect();
	}
	
    var toSelect = new Array();
    
    if (this.multipleSelection == true && shiftKey == true)
    {
    	// oldLastSelection et elt.element
		if (this.showGroup)
		{
			var sawOld = false;
			var sawCurrent = false;
			
            var groups = this.computeGroups()
			
			for (var j=0; j<groups.length; j++)
			{
				var group = groups[j];
				
				for (var i=0; i<this.elements.length; i++)
				{
					var element = this.elements[i];
					if (element.properties[this.group] == group)
					{
						var seeOld = (oldLastSelection == element);
						var seeCurrent = (elt.element == element);
						
						if (seeOld)
						{
							sawOld = true;
						}
						if (seeCurrent)
						{
							sawCurrent = true;
						}

						if (seeOld || seeCurrent)
						{
							toSelect.push(element.representation);
						}
						else if (sawOld != sawCurrent)
						{
							toSelect.push(element.representation);
						}
					}
				}
			}
		}
		else
		{
	    	var startIndex = -1;
	        var endIndex = -1;
	        for (var i = 0; i < this.elements.length; i++)
	        {
	            if (oldLastSelection == this.elements[i])
	            {
	                startIndex = i;
	            }
	            if (elt.element == this.elements[i])
	            {
	                endIndex = i;
	            }
	        }
	        
	        if (startIndex != -1 && endIndex != -1)
	        {
	            if (startIndex > endIndex)
	            {
	                var t = startIndex;
	                startIndex = endIndex;
	                endIndex = t;
	            }
	            for (var i = startIndex; i <= endIndex; i++)
	            {
	                toSelect.push(this.elements[i].representation);
	            }
	        }
		}
    }
    else
    {
        toSelect.push(elt);
    }
    
    for (var i = 0; i < toSelect.length; i++)
    {
        var currentElt = toSelect[i];
        
        if (this.isSelected(currentElt))
            continue;
    
        if (this.listener != null && this.listener.onSelect != null && !this.listener.onSelect(currentElt.element))
            continue;

        this.selection.push(currentElt);
        currentElt.element.selected = true;

        // selectionne un element
        currentElt.element.paint();
    }
  
	
	
	if (this.currentView == "viewer")
	{
		var view = null;
		if (this.listener != null && this.listener.onViewer != null)
			view = this.listener.onViewer(elt.element, this.ui.document);
		if (view == null)			
			view = SListView.viewerNoPreviewMessage;
		if (typeof view == "string")
			view = document.createTextNode(view);

		this.ui.previewElement.innerHTML = "";
		this.ui.previewElement.appendChild(view);
	}
}

SListView.prototype.paintElement = function (td)
{
	if (this.currentView == "mozaic")
	{
		td.innerHTML = "";
		this.mosaic_paintElement(td);
	}
	else if (this.currentView == "icon")
        {
		this.icon_paintElement(td);
        }
	else if (this.currentView == "thumb")
        {
		this.thumb_paintElement(td);
        }
	else if (this.currentView == "viewer")
        {
		this.viewer_paintElement(td);
        }
	else if (this.currentView == "list")
        {
		this.list_paintElement(td);
        }
	else if (this.currentView == "detail")
        {
		this.detail_paintElement(td);
        }
	else
	{
		alert("Error on 'SListView.paintElement'. Unsupported view " + this.currentView);
	}
}

// Create one icon in mozaic view for an element
SListView.prototype.create_mozaic = function (element)
{
	var table = this.ui.document.createElement("table");
	table.className = "mosaic";
	var tr = table.insertRow(0);
	var td_img = this.ui.document.createElement("td");
	var td_txt = this.ui.document.createElement("td");
	tr.appendChild(td_img);
	tr.appendChild(td_txt);
	
	// Image
	td_img.className = "img";
	var pre_img = this.ui.document.createElement("img");
		pre_img.style.width = "60px";
		pre_img.style.height = "1px";
		pre_img.style.display = "block";
		td_img.appendChild(pre_img);
	var img = this.ui.document.createElement("img");
		img.src = element.icon50;
		td_img.appendChild(img);
	
	// Texte
	var pre_img2 = this.ui.document.createElement("img");
		pre_img2.style.width = "160px";
		pre_img2.style.height = "1px";
		pre_img2.style.display = "block";
		td_txt.appendChild(pre_img2);
	td_txt.className = "txt";
	var itable = this.ui.document.createElement("table");
	td_txt.appendChild(itable);
	var irow = itable.insertRow(0);
	var itd = this.ui.document.createElement("td");
	irow.appendChild(itd);
	element.representation = itd;
	itd.slistview = this;
	itd.element = element;
  
	var func = function (e) {
		if ((STools.is_ie && this.slistview.ui.document.parentWindow.event.button != 1)
			|| (!STools.is_ie && e.button != 0))
		{
			return true;
		}
	 
		// clic
		var controlKey = ((STools.is_ie && this.slistview.ui.document.parentWindow.event.ctrlKey ) || (!STools.is_ie && e.ctrlKey))
        var shiftKey = ((STools.is_ie && this.slistview.ui.document.parentWindow.event.shiftKey ) || (!STools.is_ie && e.shiftKey))
		try {this.slistview.ui.document.execCommand("Unselect",false,null);} catch (e) {};
		this.slistview.select(this, controlKey, shiftKey); 

		if ((STools.is_ie && this.slistview.ui.document.parentWindow.event.button >= 2) || (!STools.is_ie && e.button >= 2))
		{
			// menu contextuel
			if (this.slistview.listener != null && this.slistview.listener.onContextMenu != null)
				this.slistview.listener.onContextMenu(this.element);				
		}

		if (STools.is_ie) {
			this.slistview.ui.document.parentWindow.event.cancelBubble = true;
			this.slistview.ui.document.parentWindow.event.returnValue = false;
		} else {
			e.preventDefault();
			e.stopPropagation();
		}

		return true; 
	};
    var func2 = function (e) {
        // clic
        try {this.slistview.ui.document.execCommand("Unselect",false,null);} catch (e) {};
        this.slistview.select(this); 

        if (this.slistview.listener != null && this.slistview.listener.onExecute != null)
          this.slistview.listener.onExecute(this.element);        

        if (STools.is_ie) {
          this.slistview.ui.document.parentWindow.event.cancelBubble = true;
          this.slistview.ui.document.parentWindow.event.returnValue = false;
        } else {
          e.preventDefault();
          e.stopPropagation();
        }
    
        return false; 
    }
  
	table.itd = itd;

	if (STools.is_ie)
	{
		itd.onmouseup = func;
		table.onmouseup = function (e) {
		     this.itd.onmouseup(e);
	        }
          
        itd.ondblclick = func2;
        table.ondblclick = function (e) {
            this.itd.ondblclick(e);
          }
	}
	else
	{
		itd.onclick = func;
		table.onclick = function (e) {
		     this.itd.onclick(e);
	        }

        itd.ondblclick = func2;
        table.ondblclick = function (e) {
             this.itd.ondblclick(e);
              }
	}
	SListView.create_mozaic_listener (table, element);
	
	this.mosaic_paintElement(itd);

	this.ui.mainElement.appendChild(table);
}

SListView.create_mozaic_listener = function (itable, element) {};

// Create one icon in icon view for an element
SListView.prototype.create_icon = function (element)
{
	var table = this.ui.document.createElement("table");
	table.className = "icon";
	var tr = table.insertRow(0);
	var td_img = this.ui.document.createElement("td");
	tr.appendChild(td_img);
	var tr2 = table.insertRow(1);
	var td_txt = this.ui.document.createElement("td");
	tr2.appendChild(td_txt);

	// Image
	td_img.className = "img";
	var pre_img = this.ui.document.createElement("img");
		pre_img.style.width = "32px";
		pre_img.style.height = "1px";
		pre_img.style.display = "block";
		td_img.appendChild(pre_img);
	var img = this.ui.document.createElement("img");
		img.src = element.icon32;
		td_img.appendChild(img);
	
	// Texte
	var pre_img2 = this.ui.document.createElement("img");
		pre_img2.style.width = "80px";
		pre_img2.style.height = "1px";
		pre_img2.style.display = "block";
		td_txt.appendChild(pre_img2);
	td_txt.className = "txt";
	var itable = this.ui.document.createElement("table");
	td_txt.appendChild(itable);
	var irow = itable.insertRow(0);
	var itd = this.ui.document.createElement("td");
	irow.appendChild(itd);
	element.representation = itd;
	itd.slistview = this;
	itd.element = element;
	var func = function (e) { 
		if ((STools.is_ie && this.slistview.ui.document.parentWindow.event.button != 1)
			|| (!STools.is_ie && e.button != 0))
		{
			return false;
		}
		
		// clic
		var controlKey = ((STools.is_ie && this.slistview.ui.document.parentWindow.event.ctrlKey ) || (!STools.is_ie && e.ctrlKey))
        var shiftKey = ((STools.is_ie && this.slistview.ui.document.parentWindow.event.shiftKey ) || (!STools.is_ie && e.shiftKey))
		try {this.slistview.ui.document.execCommand("Unselect",false);} catch (e) {};
		this.slistview.select(this, controlKey, shiftKey); 

		if ((STools.is_ie && this.slistview.ui.document.parentWindow.event.button >= 2) || (!STools.is_ie && e.button >= 2))
		{
			// menu contextuel
			if (this.slistview.listener != null && this.slistview.listener.onContextMenu != null)
				this.slistview.listener.onContextMenu(this.element);				
		}
		
		if (STools.is_ie) {
			this.slistview.ui.document.parentWindow.event.cancelBubble = true;
			this.slistview.ui.document.parentWindow.event.returnValue = false;
		} else {
			e.preventDefault();
			e.stopPropagation();
		}

		return false; 
	};
    var func2 = function (e) {
        // clic
        try {this.slistview.ui.document.execCommand("Unselect",false,null);} catch (e) {};
        this.slistview.select(this); 

        if (this.slistview.listener != null && this.slistview.listener.onExecute != null)
          this.slistview.listener.onExecute(this.element);        

        if (STools.is_ie) {
          this.slistview.ui.document.parentWindow.event.cancelBubble = true;
          this.slistview.ui.document.parentWindow.event.returnValue = false;
        } else {
          e.preventDefault();
          e.stopPropagation();
        }
  
        return false; 
    }	
	table.itd = itd;

	if (STools.is_ie)
	{
		itd.onmouseup = func;
		table.onmouseup = function (e) {
		     this.itd.onmouseup(e);
	        }
          
        itd.ondblclick = func2;
        table.ondblclick = function (e) {
            this.itd.ondblclick(e);
          }          
	}
	else
	{
		itd.onclick = func;
		table.onclick = function (e) {
		     this.itd.onclick(e);
	        }
       
        itd.ondblclick = func2;
        table.ondblclick = function (e) {
             this.itd.ondblclick(e);
         }
	}
	SListView.create_icon_listener (table, element);
	
	this.icon_paintElement (itd);
	
	this.ui.mainElement.appendChild(table);
}

SListView.create_icon_listener = function (itable, element) {};

// Create one icon in thumb view for an element
SListView.prototype.create_thumb = function (element)
{
	var table = this.ui.document.createElement("table");
	table.className = "thumb";
	var tr = table.insertRow(0);
	var td_img = this.ui.document.createElement("td");
	tr.appendChild(td_img);
	var tr2 = table.insertRow(1);
	var td_txt = this.ui.document.createElement("td");
	tr2.appendChild(td_txt);

	// Image
	td_img.className = "img";
	
	// Do thumb
	var thumb = null;
	if (this.listener != null && this.listener.onPaintThumb != null)
		thumb = this.listener.onPaintThumb(element, document);
	if (thumb == null)
	{
		thumb = "<img src='" + element.icon50 + "'/>";
	}
	td_img.innerHTML = thumb;
	
	var pre_img = this.ui.document.createElement("img");
		pre_img.style.width = "100px";
		pre_img.style.height = "1px";
		pre_img.style.display = "block";
		td_img.appendChild(pre_img);
	
	// Texte
	var pre_img2 = this.ui.document.createElement("img");
		pre_img2.style.width = "100px";
		pre_img2.style.height = "1px";
		pre_img2.style.display = "block";
		td_txt.appendChild(pre_img2);
	td_txt.className = "txt";
	var itable = this.ui.document.createElement("table");
	td_txt.appendChild(itable);
	var irow = itable.insertRow(0);
	var itd = this.ui.document.createElement("td");
	irow.appendChild(itd);
	element.representation = itd;
	itd.slistview = this;
	itd.element = element;
	var func = function (e) { 
		if ((STools.is_ie && this.slistview.ui.document.parentWindow.event.button != 1)
			|| (!STools.is_ie && e.button != 0))
		{
			return false;
		}
		
		// clic
		var controlKey = ((STools.is_ie && this.slistview.ui.document.parentWindow.event.ctrlKey ) || (!STools.is_ie && e.ctrlKey))
        var shiftKey = ((STools.is_ie && this.slistview.ui.document.parentWindow.event.shiftKey ) || (!STools.is_ie && e.shiftKey))
		try {this.slistview.ui.document.execCommand("Unselect",false);} catch (e) {};
		this.slistview.select(this, controlKey, shiftKey); 

		if ((STools.is_ie && this.slistview.ui.document.parentWindow.event.button >= 2) || (!STools.is_ie && e.button >= 2))
		{
			// menu contextuel
			if (this.slistview.listener != null && this.slistview.listener.onContextMenu != null)
				this.slistview.listener.onContextMenu(this.element);				
		}
		
		if (STools.is_ie) {
			this.slistview.ui.document.parentWindow.event.cancelBubble = true;
			this.slistview.ui.document.parentWindow.event.returnValue = false;
		} else {
			e.preventDefault();
			e.stopPropagation();
		}

		return false; 
	};
    var func2 = function (e) {
        // clic
        try {this.slistview.ui.document.execCommand("Unselect",false,null);} catch (e) {};
        this.slistview.select(this); 

        if (this.slistview.listener != null && this.slistview.listener.onExecute != null)
          this.slistview.listener.onExecute(this.element);        

        if (STools.is_ie) {
          this.slistview.ui.document.parentWindow.event.cancelBubble = true;
          this.slistview.ui.document.parentWindow.event.returnValue = false;
        } else {
          e.preventDefault();
          e.stopPropagation();
        }
  
        return false; 
    } 	
	table.itd = itd;

	if (STools.is_ie)
	{
		itd.onmouseup = func;
		table.onmouseup = function (e) {
		     this.itd.onmouseup(e);
	        }

        itd.ondblclick = func2;
        table.ondblclick = function (e) {
            this.itd.ondblclick(e);
          }
	}
	else
	{
		itd.onclick = func;
		table.onclick = function (e) {
		     this.itd.onclick(e);
	        }

        itd.ondblclick = func2;
        table.ondblclick = function (e) {
             this.itd.ondblclick(e);
         }
	}
	SListView.create_thumb_listener (table, element);
	
	this.thumb_paintElement (itd);
	
	this.ui.mainElement.appendChild(table);
}

SListView.create_thumb_listener = function (itable, element) {};

// Create one icon in viewer view for an element
SListView.prototype.create_viewer = function (element, where)
{
	var table = this.ui.document.createElement("table");
	table.className = "thumb";
	var tr = table.insertRow(0);
	var td_img = this.ui.document.createElement("td");
	tr.appendChild(td_img);
	var tr2 = table.insertRow(1);
	var td_txt = this.ui.document.createElement("td");
	tr2.appendChild(td_txt);

	// Image
	td_img.className = "img";
	
	// Do thumb
	var thumb = null;
	if (this.listener != null && this.listener.onPaintThumb != null)
		thumb = this.listener.onPaintThumb(element, document);
	if (thumb == null)
	{
		thumb = "<img src='" + element.icon50 + "'/>";
	}
	td_img.innerHTML = thumb;
	
	var pre_img = this.ui.document.createElement("img");
		pre_img.style.width = "100px";
		pre_img.style.height = "1px";
		pre_img.style.display = "block";
		td_img.appendChild(pre_img);
	
	// Texte
	var pre_img2 = this.ui.document.createElement("img");
		pre_img2.style.width = "100px";
		pre_img2.style.height = "1px";
		pre_img2.style.display = "block";
		td_txt.appendChild(pre_img2);
	td_txt.className = "txt";
	var itable = this.ui.document.createElement("table");
	td_txt.appendChild(itable);
	var irow = itable.insertRow(0);
	var itd = this.ui.document.createElement("td");
	irow.appendChild(itd);
	element.representation = itd;
	itd.slistview = this;
	itd.element = element;
	var func = function (e) { 
		if ((STools.is_ie && this.slistview.ui.document.parentWindow.event.button != 1)
			|| (!STools.is_ie && e.button != 0))
		{
			return false;
		}
		
		// clic
		var controlKey = ((STools.is_ie && this.slistview.ui.document.parentWindow.event.ctrlKey ) || (!STools.is_ie && e.ctrlKey))
        var shiftKey = ((STools.is_ie && this.slistview.ui.document.parentWindow.event.shiftKey ) || (!STools.is_ie && e.shiftKey))
		try {this.slistview.ui.document.execCommand("Unselect",false);} catch (e) {};
		this.slistview.select(this, controlKey, shiftKey); 

		if ((STools.is_ie && this.slistview.ui.document.parentWindow.event.button >= 2) || (!STools.is_ie && e.button >= 2))
		{
			// menu contextuel
			if (this.slistview.listener != null && this.slistview.listener.onContextMenu != null)
				this.slistview.listener.onContextMenu(this.element);				
		}
		
		if (STools.is_ie) {
			this.slistview.ui.document.parentWindow.event.cancelBubble = true;
			this.slistview.ui.document.parentWindow.event.returnValue = false;
		} else {
			e.preventDefault();
			e.stopPropagation();
		}

		return false; 
	};
    var func2 = function (e) {
        // clic
        try {this.slistview.ui.document.execCommand("Unselect",false,null);} catch (e) {};
        this.slistview.select(this); 

        if (this.slistview.listener != null && this.slistview.listener.onExecute != null)
          this.slistview.listener.onExecute(this.element);        

        if (STools.is_ie) {
          this.slistview.ui.document.parentWindow.event.cancelBubble = true;
          this.slistview.ui.document.parentWindow.event.returnValue = false;
        } else {
          e.preventDefault();
          e.stopPropagation();
        }
  
        return false; 
    }   
	table.itd = itd;

	if (STools.is_ie)
	{
		itd.onmouseup = func;
		table.onmouseup = function (e) {
		     this.itd.onmouseup(e);
	        }

        itd.ondblclick = func2;
        table.ondblclick = function (e) {
            this.itd.ondblclick(e);
          }
	}
	else
	{
		itd.onclick = func;
		table.onclick = function (e) {
		     this.itd.onclick(e);
	        }

        itd.ondblclick = func2;
        table.ondblclick = function (e) {
             this.itd.ondblclick(e);
         }
	}
  SListView.create_viewer_listener (table, element);
	
	this.viewer_paintElement (itd);
	
	where.appendChild(table);
}

SListView.create_viewer_listener = function (itable, element) {};

// Create one icon in list view for an element
SListView.prototype.create_list = function (element)
{
	var table = this.ui.document.createElement("table");
	table.className = "list";
	var tr = table.insertRow(0);
	var td_img = this.ui.document.createElement("td");
	tr.appendChild(td_img);
	var td_txt = this.ui.document.createElement("td");
	tr.appendChild(td_txt);

	// Image
	td_img.className = "img";
	var pre_img = this.ui.document.createElement("img");
		pre_img.style.width = "16px";
		pre_img.style.height = "1px";
		pre_img.style.display = "block";
		td_img.appendChild(pre_img);
	var img = this.ui.document.createElement("img");
		img.src = element.icon16;
		td_img.appendChild(img);
	
	// Texte
	var pre_img2 = this.ui.document.createElement("img");
		pre_img2.style.width = "140px";
		pre_img2.style.height = "1px";
		pre_img2.style.display = "block";
		td_txt.appendChild(pre_img2);
	td_txt.className = "txt";
	var itable = this.ui.document.createElement("table");
	td_txt.appendChild(itable);
	var irow = itable.insertRow(0);
	var itd = this.ui.document.createElement("td");
	irow.appendChild(itd);
	element.representation = itd;
	itd.slistview = this;
	itd.element = element;
	var func = function (e) { 
		if ((STools.is_ie && this.slistview.ui.document.parentWindow.event.button != 1)
			|| (!STools.is_ie && e.button != 0))
		{
			return false;
		}
		
		// clic
		var controlKey = ((STools.is_ie && this.slistview.ui.document.parentWindow.event.ctrlKey ) || (!STools.is_ie && e.ctrlKey))
        var shiftKey = ((STools.is_ie && this.slistview.ui.document.parentWindow.event.shiftKey ) || (!STools.is_ie && e.shiftKey))
		try {this.slistview.ui.document.execCommand("Unselect",false);} catch (e) {};
		this.slistview.select(this, controlKey, shiftKey); 

		if ((STools.is_ie && this.slistview.ui.document.parentWindow.event.button >= 2) || (!STools.is_ie && e.button >= 2))
		{
			// menu contextuel
			if (this.slistview.listener != null && this.slistview.listener.onContextMenu != null)
				this.slistview.listener.onContextMenu(this.element);				
		}

		if (STools.is_ie) {
			this.slistview.ui.document.parentWindow.event.cancelBubble = true;
			this.slistview.ui.document.parentWindow.event.returnValue = false;
		} else {
			e.preventDefault();
			e.stopPropagation();
		}
		
		return false; 
	};
    var func2 = function (e) {
        // clic
        try {this.slistview.ui.document.execCommand("Unselect",false,null);} catch (e) {};
        this.slistview.select(this); 

        if (this.slistview.listener != null && this.slistview.listener.onExecute != null)
          this.slistview.listener.onExecute(this.element);        

        if (STools.is_ie) {
          this.slistview.ui.document.parentWindow.event.cancelBubble = true;
          this.slistview.ui.document.parentWindow.event.returnValue = false;
        } else {
          e.preventDefault();
          e.stopPropagation();
        }
  
        return false; 
    }   
	table.itd = itd;

	if (STools.is_ie)
	{
		itd.onmouseup = func;
		table.onmouseup = function (e) {
		     this.itd.onmouseup(e);
	        }

        itd.ondblclick = func2;
        table.ondblclick = function (e) {
            this.itd.ondblclick(e);
          }
	}
	else
	{
		itd.onclick = func;
		table.onclick = function (e) {
		     this.itd.onclick(e);
	        }

        itd.ondblclick = func2;
        table.ondblclick = function (e) {
             this.itd.ondblclick(e);
         }
	}
  SListView.create_list_listener (table, element);
	
	this.list_paintElement (itd);

	this.ui.mainElement.appendChild(table);
}

SListView.create_list_listener = function (itable, element) {};

// Create one icon in detail view for an element
SListView.prototype.create_detail = function (element, table)
{
	var row = table.insertRow(table.rows.length);
	var itd_2 = null;
	for (var r = 0; r < this.columns.length; r++)
	{
		var td = this.ui.document.createElement("td");
		td.colSpan = "2"
		row.appendChild(td);
		td.style.textAlign = this.columns[r].align;
        td.style.width = this.columns[r].size;
		if (this.sortCriteria.active && this.showColumns && this.columns[r].id == this.sortCriteria.by)
			td.className = "sort";
		
		if (this.columns[r].id == null)
		{
			var itable = this.ui.document.createElement("table");
			td.appendChild(itable);
			
			var irow = itable.insertRow(0);
			var itd_1 = this.ui.document.createElement("td");
			irow.appendChild(itd_1);
			var itd_2 = this.ui.document.createElement("td");
			itd_2.element = element;
			itd_2.slistview = this;
			itd_2.column = this.columns[r];
			element.representation = itd_2;
			irow.appendChild(itd_2);
			
      			var func = function (e) {
      				if ((STools.is_ie && this.slistview.ui.document.parentWindow.event.button != 1)
      					|| (!STools.is_ie && e.button != 0))
      				{
      					return false;
      				}
      				
      				// clic
      				var controlKey = ((STools.is_ie && this.slistview.ui.document.parentWindow.event.ctrlKey ) || (!STools.is_ie && e.ctrlKey))
                    var shiftKey = ((STools.is_ie && this.slistview.ui.document.parentWindow.event.shiftKey ) || (!STools.is_ie && e.shiftKey))
      				try {this.slistview.ui.document.execCommand("Unselect",false);} catch (e) {};
      				this.slistview.select(this, controlKey, shiftKey); 
      		
      				if ((STools.is_ie && this.slistview.ui.document.parentWindow.event.button >= 2) || (!STools.is_ie && e.button >= 2))
      				{
      					// menu contextuel
      					if (this.slistview.listener != null && this.slistview.listener.onContextMenu != null)
      						this.slistview.listener.onContextMenu(this.element);				
      				}
    
                      if (STools.is_ie) {
                        this.slistview.ui.document.parentWindow.event.cancelBubble = true;
                        this.slistview.ui.document.parentWindow.event.returnValue = false;
                      } else {
                        e.preventDefault();
                        e.stopPropagation();
                      }
                
      				return false;
      			};
            var func2 = function (e) {
                // clic
                try {this.slistview.ui.document.execCommand("Unselect",false,null);} catch (e) {};
                this.slistview.select(this); 
        
                if (this.slistview.listener != null && this.slistview.listener.onExecute != null)
                  this.slistview.listener.onExecute(this.element);        
        
                if (STools.is_ie) {
                  this.slistview.ui.document.parentWindow.event.cancelBubble = true;
                  this.slistview.ui.document.parentWindow.event.returnValue = false;
                } else {
                  e.preventDefault();
                  e.stopPropagation();
                }
          
                return false;
            } 
    			
			var iimg = this.ui.document.createElement("img");
            if (element.icon16 == null || element.icon16 == "")
              iimg.style.width = "0px";
             else
              iimg.src = element.icon16;
			itd_1.style.textAlign = "center";
			itd_1.appendChild(iimg);
			iimg.itd = itd_2;

			if (STools.is_ie)
			{
				itd_2.onmouseup = func;
				iimg.onmouseup = function (e) {
			     	this.itd.onmouseup(e);
                }					

                itd_2.ondblclick = func2;
                iimg.ondblclick = function (e) {
                    this.itd.ondblclick(e);
                        }   
			}
			else
			{
				itd_2.onclick = func;
				iimg.onclick = function (e) {
			     	this.itd.onclick(e);
                }					

                itd_2.ondblclick = func2;
                iimg.ondblclick = function (e) {
                     this.itd.ondblclick(e);
                 }
			}
			
			SListView.create_detail_listener (itable, element);
		}
		else
		{
			td.slistview = this;
			td.onclick = function (e) {
				this.slistview.ui.mainElement.onclick(e, true);
			}
		}
	}
	if (itd_2 != null)
		this.detail_paintElement(itd_2);			
}

SListView.create_detail_listener = function (itable, element) {};
	
SListView.prototype.paint = function (forceSort)
{
	// reset view
	this.ui.mainElement.innerHTML = "";
	this.ui.mainElement.slistview = this;
	this.ui.mainElement.onselectstart = function() { return false};
	this.ui.mainElement.onstartdrag = function() { return false};
	this.ui.mainElement.onmousedown = function() { return false; };
	this.ui.mainElement.onmousemove = function() { return false; };
	this.ui.mainElement.onmouseup = function() { return false; };

	// tri
	if (this.sortCriteria.active && (!this.elementsAreSorted || forceSort == true))
		this.elements.sort(SListView.compare);
	
	/* ******************** MOZAIC ********************** */
	if (this.currentView == "mozaic")
	{
		var groups;
		if (this.showGroup)
		{
			groups = this.computeGroups()
			
			for (var j=0; j<groups.length; j++)
			{
				var group = groups[j];
				var groupUI = this.paintGroup(group);
				groupUI.attached = 0;
				
				for (var i=0; i<this.elements.length; i++)
				{
					var element = this.elements[i];
					if (element.properties[this.group] == group)
					{
						groupUI.attached++;
						element.groupUI = groupUI;
						this.create_mozaic(element);
					}
				}
				
				this.paintGroupAfter(group);
			}
		}
		else
		{
			for (var i=0; i<this.elements.length; i++)
			{
				var element = this.elements[i];
				
				this.create_mozaic(element);
				
			}
		}
	}
	/* ******************** ICON ********************** */
	else if (this.currentView == "icon")
	{
		var groups;
		if (this.showGroup)
		{
			groups = this.computeGroups()
			
			for (var j=0; j<groups.length; j++)
			{
				var group = groups[j];
				var groupUI = this.paintGroup(group);
				groupUI.attached = 0;
				
				for (var i=0; i<this.elements.length; i++)
				{
					var element = this.elements[i];
					if (element.properties[this.group] == group)
					{
						groupUI.attached++;
						element.groupUI = groupUI;
						this.create_icon(element);
					}
				}
				
				this.paintGroupAfter(group);
			}
		}
		else
		{
			for (var i=0; i<this.elements.length; i++)
			{
				var element = this.elements[i];
				
				this.create_icon(element);
				
			}
		}
        }
	/* ******************** LIST ********************** */
	else if (this.currentView == "list")
	{
		var groups;
		if (this.showGroup)
		{
			groups = this.computeGroups()
			
			for (var j=0; j<groups.length; j++)
			{
				var group = groups[j];
				var groupUI = this.paintGroup(group);
				groupUI.attached = 0;
				
				for (var i=0; i<this.elements.length; i++)
				{
					var element = this.elements[i];
					if (element.properties[this.group] == group)
					{
						groupUI.attached++;
						element.groupUI = groupUI;
						this.create_list(element);
					}
				}
				
				this.paintGroupAfter(group);
			}
		}
		else
		{
			for (var i=0; i<this.elements.length; i++)
			{
				var element = this.elements[i];
				
				this.create_list(element);
				
			}
		}
	}
	/* ******************** THUMB ********************** */
	else if (this.currentView == "thumb")
	{
		var groups;
		if (this.showGroup)
		{
			groups = this.computeGroups()
			
			for (var j=0; j<groups.length; j++)
			{
				var group = groups[j];
				var groupUI = this.paintGroup(group);
				groupUI.attached = 0;
				
				for (var i=0; i<this.elements.length; i++)
				{
					var element = this.elements[i];
					if (element.properties[this.group] == group)
					{
						groupUI.attached++;
						element.groupUI = groupUI;
						this.create_thumb(element);
					}
				}
				
				this.paintGroupAfter(group);
			}
		}
		else
		{
			for (var i=0; i<this.elements.length; i++)
			{
				var element = this.elements[i];
				
				this.create_thumb(element);
			}
		}
	}
	/* ******************** VIEWER ********************** */
	else if (this.currentView == "viewer")
	{
		var table = this.ui.document.createElement("table");
		table.className = "viewer-global";
		this.ui.mainElement.appendChild(table);
		
		// PREVIEW ROW
		var row_preview = table.insertRow(0);
		var td_preview = this.ui.document.createElement("td");
		td_preview.className = "viewer-preview"
		row_preview.appendChild(td_preview);
		
		var preview_div = this.ui.document.createElement("div");
		preview_div.className = "viewer";
		this.ui.previewElement = preview_div;
		this.ui.previewElement.innerHTML = SListView.viewerNoPreviewMessage;
		td_preview.appendChild(preview_div);
		
		// COMMAND ROW
		var row_command = table.insertRow(1);
		var td_command = this.ui.document.createElement("td");
		td_command.className = "viewer-command"
		row_command.appendChild(td_command);
		var img_command = this.ui.document.createElement("img");
		img_command.width = "1px";
		img_command.height = "30px";
		td_command.appendChild(img_command);
		
		// MOZAIC ROW
		var row_mozaic = table.insertRow(2);
		var td_mozaic = this.ui.document.createElement("td");
		td_mozaic.className = "viewer-mozaic"
		row_mozaic.appendChild(td_mozaic);
		var div_mozaic = this.ui.document.createElement("div");
		div_mozaic.className = "viewer";
		div_mozaic.style.width = parseInt(this.ui.mainElement.style.width) -2;
		td_mozaic.appendChild(div_mozaic);
		
		var innerTable = this.ui.document.createElement("table");
		innerTable.className = "viewer";
		div_mozaic.appendChild(innerTable);
		
		var rowForThumb = innerTable.insertRow(0);
		
		var innerTd = this.ui.document.createElement("td");
		innerTd.width = "1px";
		var img_mozaic = this.ui.document.createElement("img");
		img_mozaic.width = "1px";
		img_mozaic.height = "170px";
		innerTd.appendChild(img_mozaic);
		rowForThumb.appendChild(innerTd);

		for (var i=0; i<this.elements.length; i++)
		{
			var element = this.elements[i];
			
			var td = this.ui.document.createElement("td");
			rowForThumb.appendChild(td);
			
			this.create_viewer(element, td);
		}
	}
	/* ******************** DETAIL ********************** */
	else if (this.currentView == "detail")
	{
		var table = this.ui.document.createElement("table");
		table.className = "detail";
		this.ui.mainElement.appendChild(table);

		if (this.showColumns)
		{
			var onmouseup = function (e) {
					if (SListView.startColResize == null)
						return;
					
					// Calcule la largeur
					var endX = STools.is_ie ? (this.listview.ui.document.parentWindow.event.clientX + this.listview.ui.document.body.scrollLeft) : e.pageX;

					var addX = endX - SListView.startColXPosition;
					var newX = parseInt(SListView.startColResize.size) + addX;
					newX = newX < 30 ? 30 : newX;
					newX = newX > 500 ? 500 : newX;
					
					SListView.startColResize.size = newX + "px";
					this.listview.paint();

					SListView.startColResize = null;
				};
			

			var row = table.insertRow(0);
			for (var r = 0; r < this.columns.length; r++)
			{
				var th = this.ui.document.createElement("th");
				row.appendChild(th);
				
				th.style.width = this.columns[r].size;
				
				var preImg = this.ui.document.createElement("img");
				preImg.style.height = "1px";
				preImg.style.width = this.columns[r].size;
				preImg.style.display = "block";
				th.appendChild(preImg)
				
				th.column = this.columns[r];
				th.listview = this;
				th.onclick = function() {
					var asc = true;
					if (this.listview.sortCriteria.active && this.listview.sortCriteria.by == this.column.id)
						asc = !this.listview.sortCriteria.ascending;
					this.listview.sort (true, this.column.id, asc);
					this.listview.paint();
				};
				var span = this.ui.document.createElement("span");
				th.appendChild (span);
				th.style.textAlign = this.columns[r].align;
				span.appendChild(this.ui.document.createTextNode(this.columns[r].name))
				
				if (this.sortCriteria.active && this.sortCriteria.by == this.columns[r].id)
				{
					if (this.sortCriteria.ascending)
						span.style.backgroundImage = "url('" + STools.Ressource + "slistview_sort_asc.gif" + "')";
					else
						span.style.backgroundImage = "url('" + STools.Ressource + "slistview_sort_desc.gif" + "')";
					span.style.backgroundRepeat = "no-repeat";
					span.style.backgroundPosition = "right";
					span.style.paddingRight = "20px";
				}

				var th2 = this.ui.document.createElement("th");
				th2.className = "resize";
				th2.innerHTML = "<img width='2px' height='1px'/>"
				th2.th = th; 
				th2.listview = this;
				th2.onmousedown = function (e) {
					var th = this.th;
					
					SListView.startColResize = th.column;
					SListView.startColXPosition = STools.is_ie ? (this.listview.ui.document.parentWindow.event.clientX + this.listview.ui.document.body.scrollLeft) : e.pageX;

					this.parentNode.className = "resize";
				}
				
				if (STools.is_ie)
					th2.onmouseup = onmouseup;
				else
					th2.onclick = onmouseup;
				th.th2 = th2;
				
				th.onmouseover = function() {this.className = "over"; this.th2.className = "resize over"; };
				th.onmouseout = function() {this.className = "";  this.th2.className = "resize"; };
				th.onmouseup = onmouseup;
				row.appendChild(th2);
			}
			// last unexisting col
			var th = this.ui.document.createElement("th");
			th.style.width="100%"
			if (STools.is_ie)
				th.onmouseup = onmouseup;
			else
				th.onclick = onmouseup;
			th.listview = this;
			row.appendChild(th);
		}
		
		var groups;
		if (this.showGroup)
		{
			groups = this.computeGroups()
			
			for (var j=0; j<groups.length; j++)
			{
				var row = table.insertRow(table.rows.length);
				var td = this.ui.document.createElement("td");
				row.appendChild(td);
				td.colSpan = this.columns.length * 2 + 1;

				var group = groups[j];
				var groupUI = this.paintGroup(group, td);
				groupUI.attached = 0;
				
				for (var i=0; i<this.elements.length; i++)
				{
					var element = this.elements[i];
					if (element.properties[this.group] == group)
					{
						groupUI.attached++;
						element.groupUI = groupUI;
						this.create_detail(element, table);
					}
				}
				
				//this.paintGroupAfter(group, td);
			}
		}
		else
		{
			for (var i=0; i<this.elements.length; i++)
			{
				var element = this.elements[i];
				
				this.create_detail(element, table);
				
			}
		}
		
		// last unexisting line
		var row = table.insertRow(table.rows.length);
		row.slistview = this;
		row.onclick = function (e) {
			this.slistview.ui.mainElement.onclick(e, true);
		}
		for (var r = 0; r < this.columns.length; r++)
		{
			var td = this.ui.document.createElement("td");
			if (this.sortCriteria.active && this.showColumns && this.columns[r].id == this.sortCriteria.by)
				td.className = "sort";
			td.colSpan = "2"
			td.style.height = "100%"
			row.appendChild(td);
		}
		var td = this.ui.document.createElement("td");
		row.appendChild(td);
        }
        else
	{
		alert("Error on 'SListView.paint'. Unsupported view " + this.currentView);
		return;
	}
}

SListView.trunc = function (str, len)
{
        var lines = str.split("<br/>");
        
        var result = "";
        for (var i=0; i<lines.length; i++)
        {
                if (i != 0)
                        result += "<br/>";
                result += SListView.truncLine(lines[i], len);
        }
        return result;
}

SListView.truncLine = function (str, len)
{
   if (str.length <= len)
        return str;
   else
        return str.substring(0, len) + '...';
}

SListView.prototype.mosaic_paintElement = function (itd)
{
	var element = itd.element;

	var h1 = this.ui.document.createElement("h1");
	h1.innerHTML = SListView.trunc(element.name, SListView.mosaicViewTrunc);
    itd.title = element.tooltip ? element.tooltip : element.name;
	itd.appendChild(h1);

	itd.className = element.selected ? "selected" : "";

	for (var j=0; j<this.columns.length; j++)
	{
	       if (this.columns[j].showInMozaicView == true)
	       {
            	    var id = this.columns[j].id;
    	   	    if (id != null && element.properties[id] != null)
    	            {
    			if (element.properties[id + "Display"] != null)
    				itd.appendChild(this.ui.document.createTextNode(SListView.trunc(element.properties[id + "Display"], SListView.mosaicViewTrunc)));
    			else
    				itd.appendChild(this.ui.document.createTextNode(SListView.trunc(element.properties[id], SListView.mosaicViewTrunc)));
    			itd.appendChild(this.ui.document.createElement("br"));
    		    }
              }
	}
}

SListView.prototype.icon_paintElement = function (itd)
{
	var element = itd.element;

    var truncked = SListView.trunc(element.name, SListView.iconViewTrunc);
	itd.innerHTML = element.selected ? ("<div>" + element.name + "</div>") : ("<div>" + truncked + "</div>");
    itd.title = !element.selected ? (element.tooltip ? element.tooltip : element.name) : "";
	itd.className = element.selected ? "selected" : "";
}

SListView.prototype.thumb_paintElement = function (itd)
{
	var element = itd.element;

    var truncked = SListView.trunc(element.name, SListView.thumbViewTrunc);
	itd.innerHTML = element.selected ? ("<div class='thumb'>" + element.name + "</div>") : ("<div class='thumb'>" + truncked + "</div>");
    itd.title = !element.selected ? (element.tooltip ? element.tooltip : element.name) : "";
	itd.className = element.selected ? "selected" : "";
}

SListView.prototype.viewer_paintElement = function (itd)
{
	var element = itd.element;

	itd.innerHTML = "<div>" + element.name + "</div>";
	itd.className = element.selected ? "selected" : "";
}


SListView.prototype.list_paintElement = function (itd)
{
	var element = itd.element;

	itd.innerHTML = SListView.trunc(element.name, SListView.listViewTrunc);
    itd.title = element.tooltip ? element.tooltip : element.name;
	itd.className = element.selected ? "selected" : "";
}

SListView.prototype.detail_paintElement = function (itd)
{
	var element = itd.element;
	
	itd.innerHTML = SListView.trunc(element.name, (parseInt(itd.column.size) - 20)/SListView.detailViewTrunc);
    itd.title = element.tooltip ? element.tooltip : element.name;
	itd.className = element.selected ? "selected" : "";
	
	var itable = STools.getParentWithTag (itd, "table");
	var row = STools.getParentWithTag (itable.parentNode, "tr");
	
	for (var r = 0; r < this.columns.length; r++)
	{
        var td = STools.getDirectChildrenByTagName(row, 'td')[r];
		
		if (element.properties[this.columns[r].id] == null)
			continue;
		else if (element.properties[this.columns[r].id + "Display"] != null)
			td.innerHTML = SListView.trunc(element.properties[this.columns[r].id + "Display"], (parseInt(this.columns[r].size) - 20)/SListView.detailViewTrunc);
		else
			td.innerHTML = SListView.trunc(element.properties[this.columns[r].id], (parseInt(this.columns[r].size) - 20)/SListView.detailViewTrunc);
	}
}

SListView.prototype.computeGroups = function ()
{
	// Liste les groupes
	var groups = new Array();
	
	function isIn (elt, hArray) {
		for (var i=0; i < hArray.length; i++)
		{	
			if (elt == hArray[i])
				return true;
		}
		return false;
	}

	for (var i=0; i<this.elements.length; i++)
	{
		var element = this.elements[i];
		var groupValue = element.properties[this.group];
		if (!isIn(groupValue, groups))
			groups.push(groupValue);
	}	
	
	return groups;	
}
		
SListView.prototype.paintGroup = function (group, here)
{
	if (here == null)
		here = this.ui.mainElement;
	
	var megadiv = this.ui.document.createElement("div");
	megadiv.className = "group_surround";
	
	var imgS1 = this.ui.document.createElement("img");
	imgS1.className = "group";
	imgS1.style.width = "1px";
	imgS1.style.height = "10px";
	megadiv.appendChild(imgS1);

	var div = this.ui.document.createElement("div");
	div.className = "group";
	div.innerHTML = group;
	megadiv.appendChild(div);
	
	var img = this.ui.document.createElement("img");
	img.src = STools.Ressource + "group_separator.gif";
	img.style.height = "1px";
	img.style.width = "301px";
	img.className = "group";
	megadiv.appendChild(img);
	
	var imgS2 = this.ui.document.createElement("img");
	imgS2.className = "group";
	imgS2.style.width = "1px";
	imgS2.style.height = "10px";
	megadiv.appendChild(imgS2);

	here.appendChild(megadiv);
	
	return megadiv;
}

SListView.prototype.paintGroupAfter = function (group, here)
{
	if (here == null)
		here = this.ui.mainElement;

	var imgS1 = this.ui.document.createElement("img");
	imgS1.className = "group";
	imgS1.style.width = "1px";
	imgS1.style.height = "10px";
	here.appendChild(imgS1);
}
