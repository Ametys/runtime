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
 * org.ametys.form.LongField
 *
 * @class This class provides a text field, width a help icon. Use the <code>desc</option> to add the help icon.<br/>Only non decimal values are authorized.
 * @extends Ext.form.NumberField
 * @constructor
 * @param {Object} config Configuration options
 * @example
 * var input = new org.ametys.form.LongField ({
 *	fieldLabel: "Time period before automatic unlocking",
 *	desc: "Time period before automatic unlocking",
 *	name: "period",
 *	value: 2
 *	width: 250
 *	});
 */
org.ametys.form.LongField = function(config) 
{
	config.itemCls = "ametys-input";
	config.labelSeparator = '';
	config.allowDecimals = false,
	
	org.ametys.form.LongField.superclass.constructor.call(this, config);
}; 

Ext.extend(org.ametys.form.LongField, Ext.form.NumberField, {});

org.ametys.form.LongField.prototype.onRender = function(ct, position)
{
	org.ametys.form.LongField.superclass.onRender.call(this, ct, position);
	
	if (this.desc)
	{
		this.el.insertSibling({
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
