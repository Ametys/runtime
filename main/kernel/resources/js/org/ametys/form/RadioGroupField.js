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

Ext.namespace('org.ametys.form');

/**
 * org.ametys.form.RadioGroupField
 *
 * @class This class provides a radio group field width a help icon. Use the <code>desc</option> to add the help icon.
 * @extends Ext.form.RadioGroup
 * @constructor
 * @param {Object} config Configuration options
 */
org.ametys.form.RadioGroupField = function(config) 
{
	config.cls = "ametys-radio-group";
	config.itemCls = "ametys-input";
	config.labelSeparator = '';
	
	org.ametys.form.RadioGroupField.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.form.RadioGroupField, Ext.form.RadioGroup, {});

org.ametys.form.RadioGroupField.prototype.onRender = function(ct, position)
{
	org.ametys.form.RadioGroupField.superclass.onRender.call(this, ct, position);
	
	if (this.desc)
	{
		this.helpEl = this.el.insertSibling({
			id: this.id + '-img',
			tag:'img',
			style: 'padding-left: 20px; padding-top : 6px; float: left;',
			src: getPluginResourcesUrl('core') + '/img/administrator/config/help.gif'}, 'after');
	
		Ext.QuickTips.register({
		    target: this.id + '-img',
		    text: this.desc,
		    dismissDelay: 0 // disable automatic hiding
		});
	}
}
