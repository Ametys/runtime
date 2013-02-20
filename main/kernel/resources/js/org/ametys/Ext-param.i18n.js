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

Date.patterns = {
    ISO8601Long:"Y-m-d\\TH:i:s.uP",
    ISO8601Short:"Y-m-d",
    ShortDate: "n/j/Y",
    LongDate: "l, F d, Y",
    FullDateTime: "l, F d, Y g:i:s A",
    MonthDay: "F d",
    ShortTime: "g:i A",
    LongTime: "g:i:s A",
    SortableDateTime: "Y-m-d\\TH:i:s",
    UniversalSortableDateTime: "Y-m-d H:i:sO",
    YearMonth: "F, Y"
};

Ext.BLANK_IMAGE_URL = context.contextPath + "/plugins/extjs/resources/images/default/s.gif";

Utils.loadScript(context.contextPath + "/plugins/extjs/resources/js/locale/ext-lang-<i18n:text i18n:key="KERNEL_LANGUAGE_CODE" i18n:catalogue="kernel"/>.js");


// FIXING CMS-4193, RUNTIME-825 but keep in mind CMS-3096 => maybe this can be removed
if (Ext.isChrome)
{
	Ext.form.TriggerField.prototype.setSize = function()
	{
		Ext.form.TriggerField.superclass.setSize.call(this, arguments);
		this.trigger.dom.style.position = 'static';
		var me = this;
		window.setTimeout(function() { me.trigger.dom.style.position = ''; }, 1);
	}
}
