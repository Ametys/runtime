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
 
STools.Ressource = "";

STools.agt = navigator.userAgent.toLowerCase();
STools.is_ie = ((STools.agt.indexOf("msie") != -1) && (STools.agt.indexOf("opera") == -1));
STools.innerTextTag = STools.is_ie ? "innerText" : "textContent";

function STools() {
}

STools.applyStyle = function(element, style, className) 
{
	element.className = className;
	for (var p in style) 
		element.style[p] = style[p];
}

STools.debug = function(elt)
{
	var s = elt + " / " + typeof elt + "\n";
	for (var a in elt)
		s += a + ", "
	return s;
}

STools.loadStyle = function (_document, url)
{
	var head = _document.getElementsByTagName("head")[0];
	var link = _document.createElement("link");
	link.rel = "stylesheet";
	link.href = url;
	link.type = "text/css";
	head.appendChild(link);
}

STools.getParentWithTag = function (elt, tag)
{	
	while (tag.toLowerCase() != elt.tagName.toLowerCase())
	{
		elt = elt.parentNode
	}
	return elt
}

STools.getDirectChildrenByTagName = function(elt, name)
{
        var a = new Array();

        for (var i=0; elt.childNodes != null && i < elt.childNodes.length; i++)
        {
                var e = elt.childNodes[i]
                if (e.tagName && e.tagName.toLowerCase() == name.toLowerCase())
                {
                  a.push(e);
                }
        }
        return a
}


STools.getChildrenByTagName = function(elt, name)
{
        var a = new Array();

        for (var i=0; elt.childNodes != null && i < elt.childNodes.length; i++)
        {
                var e = elt.childNodes[i]
                if (e.tagName && e.tagName.toLowerCase() == name.toLowerCase())
                {
                  a.push(e);
                }
                
                var ap = STools.getChildrenByTagName(e, name)
                for (var j=0; j<ap.length; j++)
                        a.push(ap[j])
        }
        return a
}
