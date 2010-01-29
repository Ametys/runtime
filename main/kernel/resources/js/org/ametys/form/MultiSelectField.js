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
 * org.ametys.form.MultiSelectField
 *
 * @class This class provides a multi-select combo field width a help icon. Use the <code>desc</option> to add the help icon.
 * @extends Ext.ux.form.MultiSelect
 * @constructor
 * @param {Object} config The configuration options
 */
org.ametys.form.MultiSelectField = function(config) 
{
	config.itemCls = "ametys-multi-select";
	config.labelSeparator = '';
	
	org.ametys.form.MultiSelectField.superclass.constructor.call(this, config);
};

Ext.extend(org.ametys.form.MultiSelectField, Ext.ux.form.MultiSelect, {});

org.ametys.form.MultiSelectField.prototype.onRender = function(ct, position)
{
	org.ametys.form.MultiSelectField.superclass.onRender.call(this, ct, position);
	
	if (this.desc)
	{
		this.itemCt.child('div.x-form-element div.x-form-field').insertSibling({
			id: this.name + '-img',
			tag:'img',
			style: 'padding-left: 20px; padding-top : 7px;',
			src: getPluginResourcesUrl('core') + '/img/administrator/config/help.gif'}, 'after');
	
		var tooltip = new Ext.ToolTip({
	        target: this.name + '-img',
	        html: this.desc
	    });
	}
}