/*
 * Copyright (c) 2008 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */

// Ametys Namespace
Ext.namespace('org.ametys');

org.ametys.AdminTools = {}

org.ametys.AdminTools.DockTooltipFormater = function (title, image, text)
{
	return "<div class='dock-button-tooltip'>" 
		+ "<div class='dock-button-tooltip-img'>"
		+ "<img src='" + image + "'/>"
		+ "</div>"
		+ (title ? "<div class='dock-button-tooltip-title'>" + title + "</div>" : "")
		+ "<div class='dock-button-tooltip-text'>"
		+    text 
		+ "</div>"
		+ "<div class='x-clear'/>"
		+ "</div>";
}

org.ametys.AdminTools.DesktopItemTooltipFormater = function (title, text)
{
	return "<div class='desktop-item-tooltip'>" 
		+ (title ? "<div class='desktop-item-tooltip-title'>" + title + "</div>" : "")
		+ "<div class='desktop-item-tooltip-text'>"
		+    text 
		+ "</div>"
		+ "</div>";
}