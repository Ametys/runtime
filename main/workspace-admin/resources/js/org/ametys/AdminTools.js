/*
 *  Copyright 2009 Anyware Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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