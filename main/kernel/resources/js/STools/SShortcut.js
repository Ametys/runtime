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
*/

function SShortcut (key, action, _document)
{
        if (_document == null)
                _document = document;
        
        if (_document.SShortcut == null)
                _document.SShortcut = {}

	if (!_document.SShortcut.list)
	{
		_document.SShortcut.list = new Array();
		SShortcut.registerOnKeyDownEvent (_document);
	}

	this.key = key;

	if (typeof action == "function" || action == undefined) {
		this.func = action;
		this.funcArg = null;
		this.isEnabled = function () {return true;};
		this.isEnabledArg = null;
	} else {
		this.func = action.act;
		this.funcArg = action.actArg;
		this.isEnabled = action.isEnabled;
		this.isEnabledArg = action.isEnabledArg;
	}

	_document.SShortcut.list.push(this);
}

SShortcut.onKeyDown = function(_document, e)
{
		var pressed = "";

		if ((STools.is_ie && _document.parentWindow.event.ctrlKey )
			|| (!STools.is_ie && e.ctrlKey))
			pressed += "Ctrl+";
		if ((STools.is_ie && _document.parentWindow.event.altKey )
			|| (!STools.is_ie && e.altKey))
			pressed += "Alt+";
		if ((STools.is_ie && _document.parentWindow.event.shiftKey )
			|| (!STools.is_ie && e.shiftKey))
			pressed += "Shift+";

		var key = STools.is_ie ? _document.parentWindow.event.keyCode : e.keyCode;
		if (key == 0 && !STools.is_ie)
			key = e.which - 32;
		if (key == 13 || key == 27 || (key >= 65 && key <= 90) || (key >= 112 && key <= 123))
		{
			if (key >= 65 && key <= 90)
				pressed += String.fromCharCode(key).toUpperCase();
			else if (key == 13)
				pressed += "Return";
			else if (key == 27)
				pressed += "Esc";
			else
				pressed += "F" + (key - 111);

			for (s in _document.SShortcut.list)
			{
				// v?rifie dans les shortcuts enregistr?s
				if (_document.SShortcut.list[s].key == pressed && _document.SShortcut.list[s].isEnabled (_document.SShortcut.list[s].isEnabledArg))
				{
					var result = _document.SShortcut.list[s].func(STools.is_ie ? _document.parentWindow.event : e, _document.SShortcut.list[s].funcArg);
					if (result != false)
					{
						if (STools.is_ie) {
							_document.parentWindow.event.cancelBubble = true;
							_document.parentWindow.event.returnValue = false;
						} else {
							e.preventDefault();
							e.stopPropagation();
						}
					}
					
					break;
				}
			}
		}
}

SShortcut.registerOnKeyDownEvent = function(_document)
{
	if (!STools.is_ie)
		_document.captureEvents(Event.KEYDOWN) // Defines what events to capture for Navigator

	_document.SShortcut.olderonkeydown = _document.onkeydown;
	_document.onkeydown = function (e) {
		SShortcut.onKeyDown(this, e);

		if (this.SShortcut.olderonkeydown)
			document.SShortcut.olderonkeydown(e);
	}
}