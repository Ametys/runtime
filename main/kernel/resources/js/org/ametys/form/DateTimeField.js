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
 * org.ametys.form.DateTimeField
 *
 * @class This class provides a calendar and time field width a help icon. Use the <code>desc</option> to add the help icon.
 * @extends Ext.form.DateField
 * @constructor
 * @param {Object} config Configuration options
 */
org.ametys.form.DateTimeField = function(config) 
{
	config.itemCls = "ametys-input";
	config.labelSeparator = '';
	
	org.ametys.form.DateTimeField.superclass.constructor.call(this, config);
}; 


Ext.extend(org.ametys.form.DateTimeField, Ext.form.DateField, {
	xtype: 'datetimefield'
});

org.ametys.form.DateTimeField.prototype._valueInitialized;

org.ametys.form.DateTimeField.prototype.setValue = function (value)
{
	var date = this.parseDate (value);
	
	var time = date.dateFormat(this._timeField.format);
	// Test to not re-init time value at each change of date
	if (!org.ametys.form.DateTimeField.prototype._valueInitialized || time != '00:00')
	{
		this._timeField.setValue(date.dateFormat(this._timeField.format));
	}
	
	org.ametys.form.DateTimeField.superclass.setValue.call(this, date.dateFormat(this.format));
	
	org.ametys.form.DateTimeField.prototype._valueInitialized = true;
}

org.ametys.form.DateTimeField.prototype.getValue = function ()
{
	var date = org.ametys.form.DateTimeField.superclass.getValue.call(this);
	
	var time = this._timeField.getValue();
	var timeDate = Date.parseDate(time, this._timeField.format);
    if (timeDate != null)
    {
        date.setHours(timeDate.getHours());
        date.setMinutes(timeDate.getMinutes());
        date.setSeconds(timeDate.getSeconds());
    }
    
	return date;
}

org.ametys.form.DateTimeField.prototype.onRender = function(ct, position)
{
	org.ametys.form.DateTimeField.superclass.onRender.call(this, ct, position);
	
	this.wrap2 = this.wrap.wrap({cls:'x-form-date-time-wrap'});
	
	this._timeField = new Ext.form.TimeField({
		format: 'H:i',
		width: 80,
		renderTo: this.wrap2
	});
	
	if (this.desc)
	{
		this.itemCt.child('div.x-form-element div.x-form-date-time-wrap').insertSibling({
			id: this.name + '-img',
			tag:'img',
			style: 'padding-left: 20px; padding-top : 6px; float: left;',
			src: getPluginResourcesUrl('core') + '/img/administrator/config/help.gif'}, 'after');
	
		var tooltip = new Ext.ToolTip({
	        target: this.name + '-img',
	        html: this.desc,
	        
	        dismissDelay: 0 // disable automatic hiding
	    });
	}
}
