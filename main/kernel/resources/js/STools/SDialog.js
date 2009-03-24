SDialog.initialization = false;
SDialog.currentOpen = new Array();
SDialog.dragging = false;

function SDialog(table, caption, image, width, height, config, listener) {
	this.ui = new Object();
	this.ui.table = (typeof table == "string") ? document.getElementById(table) : table;
	this.ui.config = config ? config : new SDialog.Config();	
	this.ui.width = width;
	this.ui.height = height;
	this.ui.caption = caption;
	this.ui.image = image;
	this.ui.listener = listener;
  
    if (this.ui.table == null)
    {
      throw "The dialog box cannot be created with a null template";
    }
}

SDialog.prototype.getDocument = function() {
  return this.ui.iframe.contentWindow.document;
}

SDialog.prototype.getWindow = function() {
  return this.ui.iframe.contentWindow;
}

SDialog.prototype.paint = function() {
	// Creation of an englobing iframe
	this.ui.iframe = document.createElement("iframe");
	this.ui.iframe.dialog = this;
	this.ui.iframe.style.width = this.ui.width + "px"
	this.ui.iframe.style.height= this.ui.height + "px"
	this.ui.iframe.scrolling = "no";
	this.ui.iframe.style.position = "absolute";
	this.ui.iframe.style.display = "none";
	this.ui.iframe.style.backgroundColor = "buttonface";
	this.ui.iframe.frameBorder = "no";

	document.body.insertBefore(this.ui.iframe, document.body.childNodes[0]);
	this.ui.table.parentNode.removeChild(this.ui.table);

	var _document = this.ui.iframe.contentWindow.document
	_document.charset = document.charset;
	_document.open();
	_document.write("<html><body style='padding: 0px; margin: 0px; cursor: default;'></body></html>")
	_document.close();
	
	this.ui.englobingTable = _document.createElement("table");
	STools.applyStyle (this.ui.englobingTable, this.ui.config.globalTableStyle, this.ui.config.globalTableClass);
	this.ui.englobingTable.style.width = this.ui.width + "px"
	this.ui.englobingTable.style.height = this.ui.height + "px"
	this.ui.englobingTable.style.borderCollapse="collapse"		
	this.ui.englobingTable.cellSpacing="0"
	this.ui.englobingTable.cellPadding="0"
	_document.body.appendChild(this.ui.englobingTable)

	var tr = this.ui.englobingTable.insertRow(0);
	var td = _document.createElement("TD");
	STools.applyStyle (td, this.ui.config.globalExternCaptionStyle, this.ui.config.globalExternCaptionClass);
	tr.appendChild(td);
    
	this.ui.captionDiv = _document.createElement("div");
	td.appendChild(this.ui.captionDiv)
	STools.applyStyle (this.ui.captionDiv, this.ui.config.globalCaptionStyle, this.ui.config.globalCaptionClass);
	
	this.ui.captionDiv.style.paddingLeft = "4px";
	
	if (this.ui.image && this.ui.image != null)
	{
		this.ui.captionDiv.style.paddingLeft = "24px";
		this.ui.captionDiv.style.backgroundImage = "url('" + this.ui.image + "')"; 
		this.ui.captionDiv.style.backgroundRepeat = "no-repeat"; 
		this.ui.captionDiv.style.backgroundPosition = "2px";
	}
	this.ui.captionDiv.appendChild(_document.createTextNode (this.ui.caption))
	
	this.ui.captionDiv.iframe = this.ui.iframe;
  
    if (STools.is_ie) 
    {
        this.ui.captionDiv.onselectstart = function () { this.iframe.dialog.startDragging(); return false};
    }
    else
    {
        this.ui.captionDiv.onmousedown = function () {
            this.iframe.dialog.startDragging();
            return false;
        }
    }

	
	this.ui.englobingTable.cellspacing="0"
	this.ui.englobingTable.cellpadding="0"
	
	var tr2 = this.ui.englobingTable.insertRow(1);
	var td2 = _document.createElement("TD");
	STools.applyStyle (td2, this.ui.config.globalCellStyle, this.ui.config.globalCellClass);
	tr2.appendChild(td2);
	
	if (STools.is_ie) 
	{
		this.ui.table.cellspacing="0"
		this.ui.table.cellpadding="0"
		STools.applyStyle (this.ui.table, this.ui.config.innerTableStyle, this.ui.config.innerTableClass);
		td2.innerHTML = this.ui.table.outerHTML;
	}
	else
	{
		var t = _document.createElement("table")
		t.style.width = this.ui.width + "px"
		t.style.borderCollapse="collapse"		
		t.cellSpacing="0"
		t.cellPadding="0"
		STools.applyStyle (t, this.ui.config.innerTableStyle, this.ui.config.innerTableClass);
		t.innerHTML = this.ui.table.innerHTML
		td2.appendChild(t);
	}

	if (!SDialog.initialization)
	{
		SDialog.initialization = true;
		SDialog.createProtection();
		SDialog.registerOnScroll();
		SDialog.registerOnResize();
	}
	this.createInternalProtection(_document);
	this.registerInternalOnMouseMoveEvent(_document);
	this.registerInternalOnScrollEvent(_document);

	if (this.ui.listener && this.ui.listener.ok)
	       new SShortcut("Return", {act: SDialog.checkReturnShortcut, actArg:this.ui.listener.ok, isEnabled: function() {return true;}} , this.ui.iframe.contentWindow.document);
	if (this.ui.listener && this.ui.listener.cancel)
	       new SShortcut("Esc", this.ui.listener.cancel, this.ui.iframe.contentWindow.document);
}

SDialog.checkReturnShortcut = function (e, functionToCall)
{
	try
	{
		if (/textarea/i.test(e[STools.is_ie ? 'srcElement' : 'originalTarget'].tagName))
			return false;
	}
	catch (e)
	{
	}
	
	functionToCall();
	
	return true;
}

SDialog.prototype.showModal = function ()
{
	SDialog.currentOpen.push(this);
    if (SDialog.currentOpen.length > 1)
    {
      SDialog._sprotect.hide();
    }
    SDialog._sprotect.setLevel (19 + SDialog.currentOpen.length * 5);
    SDialog._sprotect.show();

    this.ui.iframe.style.zIndex = 20 + SDialog.currentOpen.length * 5;
  
	this.ui.top = (STools.is_ie ? document.body.scrollTop : window.pageYOffset);
	this.ui.left = (STools.is_ie ? document.body.scrollLeft : window.pageXOffset);

	this.ui.iframe.style.left = ((STools.is_ie ? document.body.offsetWidth : window.innerWidth) - this.ui.width) / 2  + this.ui.left
	this.ui.iframe.style.top = ((STools.is_ie ? document.body.offsetHeight : window.innerHeight) - this.ui.height) / 2  + this.ui.top
	this.ui.iframe.style.display = "";
	
	if (this.ui.listener && this.ui.listener.show)
			this.ui.listener.show (parseInt(this.ui.iframe.style.left), parseInt(this.ui.iframe.style.top));
			
	this.ui.iframe.contentWindow.focus();
	
	window.scrollTo(this.ui.left, this.ui.top)
}


SDialog.prototype.close = function () 
{	
	SDialog.currentOpen.pop();
    
    SDialog._sprotect.hide();
    SDialog._sprotect.setLevel (15 + SDialog.currentOpen.length * 5);
    if (SDialog.currentOpen.length > 0)
    {
        SDialog._sprotect.show();
    }
  	
	this.ui.iframe.style.display = "none";
	
	if (this.ui.listener && this.ui.listener.hide)
			this.ui.listener.hide ();
			
        window.focus();
}

SDialog._registerOnResize = function (e) {
	for (var i=0; i<SDialog.currentOpen.length; i++) {
		var current = SDialog.currentOpen[i].ui.captionDiv.iframe
		SDialog.move (parseInt(current.style.top), parseInt(current.style.left), current);
	}
}
SDialog.registerOnResize = function () 
{
	SUtilities.attachEvent(document.body, "resize", SDialog._registerOnResize);
	if (!STools.is_ie )
		setInterval("SDialog._registerOnResize()", 100);
}

SDialog._registerOnScroll = function (e) {
	var newTop = STools.is_ie ? document.body.scrollTop : window.pageYOffset;
	var newLeft = STools.is_ie ? document.body.scrollLeft : window.pageXOffset;

	for (var i=0; i<SDialog.currentOpen.length; i++) {
		var current = SDialog.currentOpen[i]
		if (current.ui.iframe.style.display == 'block')
		{
			current.ui.iframe.style.top = parseInt(current.ui.iframe.style.top) + newTop - current.ui.top;
			current.ui.iframe.style.left = parseInt(current.ui.iframe.style.left) + newLeft - current.ui.left;

			current.ui.top = newTop;
			current.ui.left = newLeft;
		}
	}
}
SDialog.registerOnScroll = function () {

		setInterval("SDialog._registerOnScroll()", 100);
}

SDialog.prototype.registerInternalOnScrollEvent = function(_document)
{
        _document.body.onscroll = function () {
			for (var i=0; i<SDialog.currentOpen.length; i++) {
				var current = SDialog.currentOpen[i].ui.captionDiv.iframe.contentWindow.scrollTo(0,0);
			}
        }
	if (!STools.is_ie )
		setInterval(_document.body.onscroll, 100);
}

SDialog.prototype.registerInternalOnMouseMoveEvent = function(_document)
{
	if (!STools.is_ie)
		_document.captureEvents(Event.MOUSEMOVE) // Defines what events to capture for Navigator
	
	SDialog.iMouseX = 0;
	SDialog.iMouseY = 0;
	_document.onmousemove = function (e) { // catches and processes the mousemove event
		if (STools.is_ie) { // for IE
			var i=0;
			while (i < document.frames.length && document.frames[i].window.document != this) {
				i++;
			}

			SDialog.iMouseY = document.frames[i].window.event.clientY+this.body.scrollTop-10
			SDialog.iMouseX = document.frames[i].window.event.clientX+this.body.scrollLeft-10
		} else { // for Navigator
			SDialog.iMouseY = e.pageY-12
			SDialog.iMouseX = e.pageX-12
		}

		if (SDialog.dragging)
		{
			SDialog.draggingElement.dialog._internalSProtect.style.left = SDialog.iMouseX;
			SDialog.draggingElement.dialog._internalSProtect.style.top = SDialog.iMouseY;

			var left = parseInt(SDialog.draggingElement.style.left) + (SDialog.iMouseX - SDialog.draggingFromX);
			var top = parseInt(SDialog.draggingElement.style.top) + (SDialog.iMouseY - SDialog.draggingFromY);
			SDialog.move(top, left);
		}
	};
}

SDialog.move = function(top, left, draggingElement)
{
	if (draggingElement == null)	
		draggingElement = SDialog.draggingElement;
	
	var maxLeft;
	var maxTop;
	
	if (STools.is_ie)
	{
		maxLeft = document.body.scrollLeft + document.body.offsetWidth - draggingElement.offsetWidth - 21;
		maxTop = document.body.scrollTop + document.body.offsetHeight - draggingElement.offsetHeight - 21;
	}
	else
	{
		maxLeft = window.pageXOffset + window.innerWidth - draggingElement.offsetWidth - 20;
		maxTop = window.pageYOffset + window.innerHeight - draggingElement.offsetHeight - 20;
	}

	draggingElement.style.left = (left > 0) ? ((left < maxLeft) ? left : maxLeft): 0;
	draggingElement.style.top = (top > 0) ? ((top < maxTop) ? top : maxTop): 0;
	
	if (draggingElement.dialog.ui.listener && draggingElement.dialog.ui.listener.move)
	{
		draggingElement.dialog.ui.listener.move (parseInt(draggingElement.style.left), parseInt(draggingElement.style.top));
	}
}

SDialog.clickOutside = function()
{
  SDialog.blink();
}

SDialog.moveOutside = function(e, _document)
{
	if (STools.is_ie) { // for IE
                var windowEvent = _document.parentWindow.event;

		SDialog.mouseY = windowEvent.clientY+document.body.scrollTop-10
		SDialog.mouseX = windowEvent.clientX+document.body.scrollLeft-10
	} else { // for Navigator
		SDialog.mouseY = e.pageY-12
		SDialog.mouseX = e.pageX-12
	}

	if (document != _document)
	{
       	var i=0;
        var _frames = document.getElementsByTagName('iframe')
       	while (i < _frames.length && _frames[i].contentWindow.document != _document) {
	              	i++;
                }
	    var obj = _frames[i]


		var curleft = 0;
		var curtop = 0;
		while (obj.offsetParent)
		{
			curleft += obj.offsetLeft
			curtop += obj.offsetTop
			obj = obj.offsetParent;
		}
        SDialog.mouseY += curtop
        SDialog.mouseX += curleft
    }

	if (SDialog.dragging)
	{
		var left = SDialog.mouseX - SDialog.draggingFromX
		var top = SDialog.mouseY - SDialog.draggingFromY
		SDialog.move(top, left);
	}
}

SDialog.setBack = function(level, color)
{
	SDialog._sprotect.setOpacity (level);
    SDialog._sprotect.setStyle('backgroundColor', color); 
}

SDialog.createProtection = function()
{
    SDialog._sprotect = new SProtectLayer();
    SDialog._sprotect.setOpacity (0);
    SDialog._sprotect.setEventListener ("onclick", SDialog.clickOutside);
    SDialog._sprotect.setEventListener ("onmousemove", function(e) { SDialog.moveOutside(e, STools.is_ie ? SDialog._sprotect._iframe.contentWindow.document : document); });
    SDialog._sprotect.setEventListener ("onmouseup", SDialog.stopDragging);
}

SDialog.prototype.startDragging = function()
{
	SDialog.dragging = true;
	SDialog.draggingElement = this.ui.iframe;
	SDialog.draggingFromX = SDialog.iMouseX;
	SDialog.draggingFromY = SDialog.iMouseY;
	this._internalSProtect.style.display = 'block';
}

SDialog.stopDragging = function()
{
	SDialog.dragging = false;
	if (SDialog.draggingElement != null)
	{
		SDialog.draggingElement.dialog._internalSProtect.style.display = 'none';
		SDialog.draggingElement.dialog._internalSProtect.style.left = '-100px';
		SDialog.draggingElement.dialog._internalSProtect.style.top = '-100px';
	}
}

SDialog.prototype.createInternalProtection = function(_document)
{
	var table = _document.createElement("TABLE");
		table.style.zIndex = "+4";
		table.cellSpacing = "0";
		table.cellPadding = "0";
		table.border = "0";
		table.style.position = "absolute";
		table.style.display = "";
		table.style.top = "-100";
		table.style.left = "-100";
		table.style.width = "20px";
		table.style.height = "20px";
		table.style.cursor = "default";
		table.dialog = this;
		table.onmouseup = function () {
			SDialog.stopDragging();
		}

	var row = table.insertRow(table.rows.length);
	var col = _document.createElement("TD");
		col.dialog = this;
		col.onmouseup = table.onmouseup;
		col.appendChild(_document.createTextNode(" "));
		col.style.cursor = "move";
		
	row.appendChild(col);
	
	this._internalSProtect = table;
	_document.body.appendChild(this._internalSProtect);
}

SDialog.blink = function(times)
{
	if (times == undefined)
		times = 3;
	if (times == 0)
		return;
	
	if (SDialog.currentOpen.length > 0)
	{
		var dialog = SDialog.currentOpen[SDialog.currentOpen.length-1];
		STools.applyStyle (dialog.ui.captionDiv, dialog.ui.config.globalBlinkingCaptionStyle, dialog.ui.config.globalBlinkingCaptionClass);
		window.setTimeout("SDialog.unblink (" + times + ")", 50);
	}
}

SDialog.unblink = function(times)
{
	if (SDialog.currentOpen.length > 0)
	{
		var dialog = SDialog.currentOpen[SDialog.currentOpen.length-1];
		STools.applyStyle (dialog.ui.captionDiv, dialog.ui.config.globalCaptionStyle, dialog.ui.config.globalCaptionClass);
		window.setTimeout( "SDialog.blink (" + (times-1) + ")", 50);
	}
}

/* -----------------------------
	CONFIG
   ----------------------------- */
SDialog.Config = function () 
{	
	this.globalTableStyle = {
		padding: "0px",
		margin: "0px",
		borderWidth: "1px",
		borderStyle: "solid",
		borderTopColor: "#f1efe2",
		borderLeftColor: "#f1efe2",
		borderBottomColor: "#716f64",
		borderRightColor: "#716f64",
		position: "absolute"
	}
	this.globalTableClass = "";

	this.globalExternCaptionStyle = {
		padding: "0px",
		margin: "0px",
		borderWidth: "1px",
		borderStyle: "solid",
		borderTopColor: "#ffffff",
		borderLeftColor: "#ffffff",
		borderBottomWidth: "0px",
		borderRightColor: "#aca899",
		backgroundColor: "#ece9d8",
		height: "18px"
	}
	this.globalExternCaptionClass = "";
	
	this.globalCaptionStyle = {
		paddingRight: "0px",
		paddingBottom: "0px",
		paddingTop: "3px",
		margin: "1px",
		borderWidth: "0px",
		borderStyle: "none",
		color: "#ffffff",
		fontFamily: "Verdana",
		fontSize: "10px",
		backgroundColor: "#0054e3",
		fontWeight: "bold",
		height: "18px",
		cursor: "default"
	}
	this.globalCaptionClass = "";

	this.globalBlinkingCaptionStyle = {
		backgroundColor: "#7a96df"
	}
	this.globalBlinkingCaptionClass = "";
	
	this.globalCellStyle = {
		padding: "0px",
		margin: "0px",
		borderWidth: "1px",
		borderStyle: "solid",
		borderTopWidth: "0px",
		borderLeftColor: "#ffffff",
		borderBottomColor: "#aca899",
		borderRightColor: "#aca899"
	}
	this.globalCellClass = "";

	this.innerTableStyle = {
		padding: "0px",
		margin: "0px",
		borderWidth: "0px",
		borderStyle: "none",
		width: "100%",
		height: "100%",
		display: "block",
		backgroundColor: "#ece9d8"
}
	this.innerTableClass = "";

}
