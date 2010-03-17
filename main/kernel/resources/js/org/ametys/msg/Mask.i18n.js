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

Ext.namespace('org.ametys.msg');
/**
 * Creates and displays directly a load mask for waiting message during loading
 * @constructor
 * @class This class creates a {@link Ext.LoadMask} and shows it
 * @param el The element or DOM node, or its id
 * @param msg The text to display in a centered loading message box 
 */
org.ametys.msg.Mask = function (el, msg)
{
	if (msg == null)
	{
		// Default message
		msg = "<i18n:text i18n:key="KERNEL_LOADMASK_DEFAULT_MESSAGE"/>";
	}
	var loadMask = new Ext.LoadMask(el, {msg: msg, removeMask: true});
	loadMask.show ();
	return loadMask;
}

org.ametys.msg.Mask.prototype.hide = function ()
{
	this.hide();
	delete this;
}