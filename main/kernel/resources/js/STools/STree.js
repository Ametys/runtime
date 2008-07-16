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
 
STree.css = "resources/css/";
STree.img = "resources/img/";

function STree(id, _document, level, listener)
{
        if (!level)
                level = 0;
	if (!_document)
		_document = document;

	STools.loadStyle (_document, STree.css + "stree.css")

	this.listener = listener;
	this._document = _document;
	this.tree = _document.getElementById(id);
	this.tree.onclick = STree.openClose;
        this.tree.ondblclick = STree.openClose;
	this.tree.oncontextmenu = function() {return false;};
	this.tree.selection = null;
	this.tree.style.display = "none";
	this.tree.className = "STree";
	this.tree.stree = this;
	this.tree.ondragstart = function() {return false;};

	var links = STools.getChildrenByTagName (this.tree, 'a');
	this.tree.linksSize = links.length;
        for (var i=0; i<this.tree.linksSize; i++)
	{
		STree.aToLink(links[i], _document, this.tree, i);
	}

	STree.affectClasses(this.tree, level);

	this.tree.style.display = "";
}

STree.aToLink = function(link, _document, tree, i)
{
	link.id = "id-" + tree.id + "-" + i
	link.tree = tree
	link.href = "#"
	link.oldonclick = link.onclick ? link.onclick : function () {} ;
	link._document = _document;
	link.onclick = STree.normalClick;
	if (tree.stree.listener == null || tree.stree.listener.canDrag == null || tree.stree.listener.canDrag)
		link.onmousedown = function (e) { STree.startDrag(this.tree, this, this._document, e); return false; };
	link.onmouseover = function (e) { if (STree.dragElement) { if (STree.canDrag(STools.is_ie ? this.document.parentWindow.event : e)) STree.drageable(); STree.select(this.tree, this, this._document); return false;} }
	link.onmouseout = function (e) { STree.notDrageable(); }
	
	if (link.getAttribute("select") != null)
		STree.select(tree, link, _document, link.select);
}

STree.normalClick = function()
{
	STree.select(this.tree, this, this._document);
	this.oldonclick();
	if (this.tree.stree.listener == null || this.tree.stree.listener.canRename == null || this.tree.stree.listener.canRename)
        	this.onclick = STree.renameClick;
	return false;
}

STree.renameClick = function()
{
	STree.edit(this.tree, this, this._document);
}

STree.affectClasses = function (currentElement, level)
{
	var uls = STools.getDirectChildrenByTagName(currentElement, "ul")
	for (var j=0; j < uls.length; j++)
	{
		var ul = uls[j];
        	var lis = STools.getDirectChildrenByTagName(ul, 'li')
        	for (var i=0; i < lis.length; i++)
        	{
        	       var li = lis[i]

                        var closed = (level == 0) && (li.className != "opened")

                       li.className = ((STools.getDirectChildrenByTagName(li, "ul").length > 0) ? (closed ? "closed_" : "opened_") : "")
                                        + ((i == (lis.length-1) && (j == (uls.length-1))) ? "terminal" : "intermediate")

                        switch (level)
                        {
                                case -1: STree.affectClasses(li, -1); break;
                                case 0: STree.affectClasses(li, 0); break;
                                default: STree.affectClasses(li, level-1); break;
                        }

                }
	}

}

STree.openClose = function (event)
{
        if (STree.cancelNextOpenClose)
        {
                STree.cancelNextOpenClose = false;
                return;
        }

	// find the element
	var elt = STools.is_ie ? this.document.parentWindow.event.srcElement : event.originalTarget;

	if (/a/i.test(elt.tagName))
		return;

	// open or close the node
	if (elt.className == "opened_intermediate")
		elt.className = "closed_intermediate";
	else if (elt.className == "closed_intermediate")
		elt.className = "opened_intermediate";
	else if (elt.className == "opened_terminal")
		elt.className = "closed_terminal";
	else if (elt.className == "closed_terminal")
		elt.className = "opened_terminal";

	// select the node, if selection was a child
	var thelinks = STools.getDirectChildrenByTagName(elt, 'a')
	var thelink = (thelinks.length != 0) ? thelinks[0] : null
	var links = STools.getChildrenByTagName(elt, 'a')
	for (var i=0; i<links.length; i++)
	{
		if (links[i] != thelink && links[i] == this.selection)
		{
		      if (thelink != null)
			     thelink.onclick();
		      else
                             STree.select(this, null, this.stree._document)
		}
	}

	// do not fire the event to the hierarchy
	if (this.document && this.document.parentWindow.event)
	{
		this.document.parentWindow.event.cancelBubble = true
		this.document.parentWindow.event.returnValue = false;
	}
	else
	{
		event.preventDefault();
		event.stopPropagation();
	}
}

STree.edit = function (tree, elt, _document)
{
	STree.select(tree, elt, _document);
	
	if (tree.stree.listener && tree.stree.listener.onBeforeEditLabel  && !tree.stree.listener.onBeforeEditLabel(tree.stree, elt))
		return;

	if (STree.editing)
		STree.unedit();
	
	elt.style.display = "none";
	
	var input = _document.createElement("input")
	input.value = elt[STools.innerTextTag]
	input.onblur = STree.unedit 
	input.className = "edit"
	input.onkeydown = function (e)
		{
			var key = STools.is_ie ? _document.parentWindow.event.keyCode : e.keyCode;
			if (key == 13)
			{
				STree.unedit();
			}
			if (key == 27)
			{
				STree.unedit(false);
			}
		}
	elt.parentNode.insertBefore(input, elt)
	try { input.select (); } catch (e) {}
	try { input.focus (); } catch (e) {}

	STree.editing = {}
	STree.editing.elt = elt
	STree.editing.tree = tree
	STree.editing._document = _document
	STree.editing.input = input
}
STree.unedit = function (save)
{
	if (!STree.editing)
		return;
		
	if (STree.editing.input.value == '' || STree.editing.input.value.replace(' ', '') == '')
		save = false;

	var tree = STree.editing.tree
	var elt = STree.editing.elt

	if (save == null || save != false)
	{
		var newName = STree.editing.input.value;
		
		if (!tree.stree.listener || !tree.stree.listener.onEditLabel || tree.stree.listener.onEditLabel(tree.stree, elt, newName))
		{
			STree.editing.elt.innerHTML = STree.editing.input.value
		}
	}
	
	STree.editing.elt.style.display = ""
	STree.editing.input.parentNode.removeChild(STree.editing.input)
	
	STree.editing = null

	if (save == false && tree.stree.listener && tree.stree.listener.onCancelEditLabel)
			  tree.stree.listener.onCancelEditLabel(tree.stree, elt)
}

STree.select = function (tree, elt, _document)
{
	if (tree.selection == elt)
		return;

	if (tree.stree.listener && tree.stree.listener.onSelect && !STree.dragElement)
		tree.stree.listener.onSelect(elt);

	// select node
	if (tree.selection)
	{
          	STree.unsetCanInsertStyle(tree.selection);
		tree.selection.className = "";
		tree.selection.onclick = STree.normalClick
	}
	tree.selection = elt;
	if (tree.selection != null)
	       tree.selection.className = "selected"
}
STree.prototype.getState = function ()
{
	var state = "";

	var lis = STools.getChildrenByTagName (this.tree, "li");
	for (var i=0; i<lis.length; i++)
	{
		var li = lis[i]

		if (li.className.indexOf("opened_") == 0)
		{
			state += "1";
		}
		else if (li.className.indexOf("closed_") == 0)
		{
			state += "0";
		}
	}

	/// compress
	var compressedState = "";
	while (state != "")
	{
		while (state.length < 4)
			state += "0";

		var l = parseInt(state.substring(0,1))*8 + parseInt(state.substring(1,2))*4 + parseInt(state.substring(2,3))*2 + parseInt(state.substring(3,4))
			switch (l)
		{
			case 10: l = 'a'; break;
			case 11: l = 'b'; break;
			case 12: l = 'c'; break;
			case 13: l = 'd'; break;
			case 14: l = 'e'; break;
			case 15: l = 'f'; break;
		}
		compressedState += l;
		state = state.substring(4);
	}

	return compressedState;
}
STree.prototype.setState = function (compressedState)
{
	// uncompress
	var state = "";
	while (compressedState != "")
	{
		var l = compressedState.substring(0,1);
		switch (l)
		{
			case 'a': l = 10; break;
			case 'b': l = 11; break;
			case 'c': l = 12; break;
			case 'd': l = 13; break;
			case 'e': l = 14; break;
			case 'f': l = 15; break;
		}
		state += (l >= 8 ? "1" : "0") + (l % 8 >= 4 ? "1" : "0") + (l % 4 >= 2 ? "1" : "0") + (l % 2 >= 1 ? "1" : "0")

		compressedState = compressedState.substring(1);
	}

	var lis = STools.getChildrenByTagName (this.tree, "li");

	for (var i=0; state != "" && i<lis.length; i++)
	{
		var li = lis[i]
		var thisState = state.substring(0, 1);

		if (li.className.indexOf("opened_") == 0 || li.className.indexOf("closed_") == 0)
		{
			li.className = (thisState == 1 ? "opened_" : "closed_") + li.className.substring(7);

			state = state.substring(1);
		}
	}
}

STree.prototype.insertNode = function(elt, img, label, action)
{
	var parentLi = elt;
	while (!/li/i.test(parentLi.tagName) && !/div/i.test(parentLi.tagName))
	{
		parentLi = parentLi.parentNode
	}

	var uls = STools.getDirectChildrenByTagName (parentLi, "ul")
	var ul
	if (uls.length == 0)
	{
		ul = this.tree.stree._document.createElement("ul");
		parentLi.appendChild(ul)
	}
	else
	{
		ul = uls[0]
	}

	var lis = STools.getDirectChildrenByTagName(ul, 'li')
	if (lis.length >= 1)
	{
		var lastLi = lis[lis.length-1]
		lastLi.className = lastLi.className.substring(0, lastLi.className.indexOf("terminal")) + "intermediate"

                if (/li/i.test(parentLi.tagName))
            	{
    	          	parentLi.className = "opened_" + parentLi.className.substring(7)
    	        }
	}
	else if (/li/i.test(parentLi.tagName))
	{
		parentLi.className = "opened_" + parentLi.className
	}

	var li = this.tree.stree._document.createElement("li")
	li.className = "terminal"

	if (img)
	{
		var imgElt = this.tree.stree._document.createElement("img");
		imgElt.src = img
		li.appendChild(imgElt)
	}
	var aElt = this.tree.stree._document.createElement("a");
	aElt.onclick = action
	aElt.appendChild(this.tree.stree._document.createTextNode(label));
	li.appendChild(aElt)
	STree.aToLink(aElt, this.tree.stree._document, this.tree, ++this.tree.linksSize);

	ul.appendChild(li)

	return li;
}

STree.prototype.moveNode = function (elt, newParent, beforeBrotherElt)
{
	while (!/li/i.test(elt.tagName) && !/div/i.test(elt.tagName))
		elt = elt.parentNode
	while (!/li/i.test(newParent.tagName) && !/div/i.test(newParent.tagName))
		newParent = newParent.parentNode
	while (beforeBrotherElt && !/li/i.test(beforeBrotherElt.tagName) && !/div/i.test(beforeBrotherElt.tagName))
		beforeBrotherElt = beforeBrotherElt.parentNode

	// Garde l'ancien ul
	var oldUl = elt;
	while (!/ul/i.test(oldUl.tagName))
		oldUl = oldUl.parentNode;

	// Prend le UL du nouveau parent
	var uls = STools.getDirectChildrenByTagName (newParent, "ul")
	var ul
	if (uls.length == 0)
	{
		ul = this.tree.stree._document.createElement("ul");
		newParent.appendChild(ul)
		newParent.className = "opened_" + newParent.className
	}
	else
	{
		ul = uls[0]
	}

	// D?place le noeud
	if (beforeBrotherElt)
	{
		ul.insertBefore (elt, beforeBrotherElt)
		// v?rifie que c'est une icone interm?diaire
		if (elt.className.indexOf("terminal") >= 0)
			elt.className = elt.className.substring(0, elt.className.indexOf("terminal")) + "intermediate"
	}
	else
	{
		ul.appendChild (elt)

		// met un icone finale sur l'element
		if (elt.className.indexOf("intermediate") >= 0)
			elt.className = elt.className.substring(0, elt.className.indexOf("intermediate")) + "terminal"
		// met une icone interm?diaire sur l'ancien final
		var lis = STools.getDirectChildrenByTagName(ul, 'li');
		if (lis.length > 1 && lis[lis.length-2].className.indexOf("terminal") >= 0)
			lis[lis.length-2].className = lis[lis.length-2].className.substring(0, lis[lis.length-2].className.indexOf("terminal")) + "intermediate"
	}
		
	// Efface l'ancien ul si n?cessaire, ou met la bonne icone sur le dernier li
	var lis =  STools.getDirectChildrenByTagName (oldUl, "li")
	if (lis.length == 0)
	{
		if (oldUl.parentNode.className.indexOf("opened_") == 0 || oldUl.parentNode.className.indexOf("closed_") == 0)
			oldUl.parentNode.className = oldUl.parentNode.className.substring(7)

		oldUl.parentNode.removeChild(oldUl);
        }
	else if (lis[lis.length-1].className.indexOf("intermediate") >= 0)
		lis[lis.length-1].className = lis[lis.length-1].className.substring(0, lis[lis.length-1].className.indexOf("intermediate")) + "terminal"
}

STree.prototype.removeNode = function (elt)
{
	var liToRemove = STools.getParentWithTag(elt, 'li')

	var ul = liToRemove.parentNode
	ul.removeChild(liToRemove)

	var lis = STools.getDirectChildrenByTagName(ul, 'li');
	if (lis.length == 0)
	{
		if (ul.parentNode.className.indexOf("opened_") == 0 || ul.parentNode.className.indexOf("closed_") == 0)
			ul.parentNode.className = ul.parentNode.className.substring(7)
		ul.parentNode.removeChild(ul)
	}
	else
	{
		var lastLi = lis[lis.length-1]
		lastLi.className = lastLi.className.substring(0, lastLi.className.indexOf("intermediate")) + "terminal"
	}
}

STree.startDrag = function (tree, elt, _document, e)
{
	if (STree.dragElement)
		STree.stopDrag(e);

	if (tree.stree.listener != null && tree.stree.listener.onStartDrag != null && !tree.stree.listener.onStartDrag(tree.stree, elt))
	{
		return false;
	}

	var link = elt

	// Creer l'element graphique a deplacer
	try
        {
	       elt = STools.getParentWithTag(elt, 'li')
        }
        catch (e)
        {
                return false;
        }

	STree.dragElement = _document.createElement("table")
	STree.dragElement.className = "drag"
	STree.dragElement.source = elt;
	STree.dragElement.sourceLink = link;
	STree.dragElement.tree = tree;
	STree.dragElement.insertRow(0);
	STree.dragElement.style.position = "absolute"
	STree.dragElement.style.display = "none"
	STree.dragElement._document = _document
	STree.dragElement.ondragstart = function(){return false}
	STree.dragElement.style.opacity = "0.8";

	_document.onmouseup = STree.stopDrag

	var html = elt.innerHTML
	var ulPos = html.toLowerCase().indexOf('<ul>')
	if (ulPos != -1)
		html = html.substring(0, ulPos);

	var td = _document.createElement("td");
	td.innerHTML = html;

	STree.dragElement.notImage = _document.createElement("img");
	STree.dragElement.notImage.style.display = 'none'
	STree.dragElement.notImage.style.position = 'absolute'
	STree.dragElement.notImage.style.zIndex = '2'
	STree.dragElement.notImage.src = STree.img + 'not.gif'
	td.insertBefore(STree.dragElement.notImage, td.childNodes[0]);

	STree.dragElement.rows[0].appendChild(td);

	_document.body.appendChild(STree.dragElement);

	tree.stree.registerOnMouseMoveEvent();
}

STree.canDrag = function (event)
{
	var elt = STools.is_ie ? event.srcElement : event.originalTarget;
	if (elt.tagName.toLowerCase() != "a")
		return false;
	if (elt == STree.dragElement.source)
		return false;

        var appendDragMode = STree.isDragAppendMode(STree.dragElement.tree);
        if (!appendDragMode)
        {
               var eltP = elt;
	       while (!/li/i.test(eltP.tagName) && !/div/i.test(eltP.tagName))
		      eltP = eltP.parentNode
               if (/div/i.test(eltP.tagName))
                        return false;
        }

	var eltI = elt
	while (eltI.tagName.toLowerCase() != "div")
	{
		if (eltI == STree.dragElement.source)
			return false;
		eltI = eltI.parentNode;
	}
		
	if (STree.dragElement.tree.stree.listener != null && STree.dragElement.tree.stree.listener.onCanDrop != null)
	{
		return STree.dragElement.tree.stree.listener.onCanDrop(STree.dragElement.tree.stree, STree.dragElement.source, elt);
	}
	
	return true;
}

STree.drageable = function()
{
	if (!STree.dragElement) return;
	
	STree.dragElement.notImage.style.display = 'none';
}

STree.notDrageable = function()
{
	if (!STree.dragElement) return;

	STree.dragElement.notImage.style.display = '';
}

STree.stopDrag = function (event)
{
        if (STree.dragElement == null)
                return;

	if (STree.dragElement.tree.stree.listener && STree.dragElement.tree.stree.listener.onSelect)
		STree.dragElement.tree.stree.listener.onSelect(STree.dragElement.tree.selection);

        var appendDragMode = STree.isDragAppendMode(STree.dragElement.tree);
        STree.dragAppendMode (STree.dragElement.tree);

	var elt = STools.is_ie ? this.parentWindow.event.srcElement : event.originalTarget;
	// Une cible ???
	if (STree.canDrag(STools.is_ie ? this.parentWindow.event : event))
	{
	       if (appendDragMode)
	       {
            	       if (STree.dragElement.tree.stree.listener == null || STree.dragElement.tree.stree.listener.onDrop == null || STree.dragElement.tree.stree.listener.onDrop(STree.dragElement.tree.stree, STree.dragElement.sourceLink, elt, true))
    	   	       {
    			         STree.dragElement.tree.stree.moveNode (STree.dragElement.source, elt)
    		       }
               }
               else if (!/div/i.test(elt.parentNode.tagName))
               {
                    var parentElt = STools.getParentWithTag(elt, 'li').parentNode;
    	               while (!/li/i.test(parentElt.tagName) && !/div/i.test(parentElt.tagName))
    		                  parentElt = parentElt.parentNode;
            	       if (STree.dragElement.tree.stree.listener == null || STree.dragElement.tree.stree.listener.onDrop == null || STree.dragElement.tree.stree.listener.onDrop(STree.dragElement.tree.stree, STree.dragElement.sourceLink, elt, false))
    	   	       {
    			         STree.dragElement.tree.stree.moveNode (STree.dragElement.source, parentElt, elt)
    		       }
               }
	}
        else
        {
                STree.cancelNextOpenClose = true;
        }

	STree.dragElement.parentNode.removeChild(STree.dragElement)
	STree.dragElement._document.onmousemove = STree.dragElement.oldOnMouseMove;
	STree.dragElement._document.onmouseup = null
	STree.dragElement = null
}

STree.isDragAppendMode = function (tree)
{
        return !tree.selection || tree.selection.className != "selectedBetween";
}
STree.dragInsertMode = function (tree)
{
    	if (tree.selection)
    	{
	          tree.selection.className = "";
	          STree.setCanInsertStyle(tree.selection);
	          tree.selection.className = "selectedBetween";
        }
}
STree.setCanInsertStyle = function(here)
{
	here.style.textDecoration = "none";
	here.style.width = "103px";
	here.style.color = "#000000";
	here.style.whiteSpace = "nowrap";
	here.style.backgroundColor = "#ffffff";
	here.style.backgroundImage = "url('" + STree.img + "insert.gif')";
	here.style.backgroundPosition = "top left";
	here.style.backgroundRepeat = "no-repeat";
}
STree.unsetCanInsertStyle = function(here)
{
	here.style.textDecoration = "";
	here.style.color = "";
	here.style.whiteSpace = "";
	here.style.width = "";
	here.style.backgroundColor = "";
	here.style.backgroundImage = "";
	here.style.backgroundPosition = "";
	here.style.backgroundRepeat = "";
}
STree.dragAppendMode = function (tree)
{
    	if (tree.selection)
    	{
	          tree.selection.className = "";
	          STree.unsetCanInsertStyle(tree.selection);
	          tree.selection.className = "selected";
        }
}

STree.prototype.registerOnMouseMoveEvent = function()
{
	if (!STools.is_ie)
		STree.dragElement._document.captureEvents(Event.MOUSEMOVE) // Defines what events to capture for Navigator

	STree.iMouseX = 0;
	STree.iMouseY = 0;
	STree.dragElement.oldOnMouseMove = STree.dragElement._document.onmousemove
	STree.dragElement._document.onmousemove = function (e) { // catches and processes the mousemove event
		if (STools.is_ie) { // for IE
			STree.iMouseY = STree.dragElement.document.parentWindow.event.clientY+this.body.scrollTop+6
			STree.iMouseX = STree.dragElement.document.parentWindow.event.clientX+this.body.scrollLeft+6
		} else { // for Navigator
			STree.iMouseY = e.pageY+6
			STree.iMouseX = e.pageX+6
		}

		if (STree.dragElement)
		{
			if (STools.is_ie)
			{
	                        if (STree.dragElement._document.parentWindow.event.shiftKey)
	                                STree.dragInsertMode (STree.dragElement.tree)
	                        else
	                                STree.dragAppendMode (STree.dragElement.tree)
	                }
	                else
	                {
	                        if (e.shiftKey)
	                                STree.dragInsertMode (STree.dragElement.tree)
	                        else
	                                STree.dragAppendMode (STree.dragElement.tree)
	                }

			STree.dragElement.style.left = STree.iMouseX;
			STree.dragElement.style.top = STree.iMouseY;
			STree.dragElement.style.display = "";
			if (!STools.is_ie)
			{
			     STree.dragElement.notImage.style.left = STree.iMouseX;
			     STree.dragElement.notImage.style.top = STree.iMouseY;
                        }
		}
		if (STree.dragElement.oldOnMouseMove)
			STree.dragElement.oldOnMouseMove(e);
	};
}

