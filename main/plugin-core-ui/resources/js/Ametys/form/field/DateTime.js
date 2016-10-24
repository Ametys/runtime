/*
 *  Copyright 2012 Anyware Services
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

/**
 * Field that displays a date and a time selector
 */
Ext.define('Ametys.form.field.DateTime', {
    extend: 'Ametys.form.AbstractFieldsWrapper',
    alias: ['widget.datetimefield', 'widget.datetime'],
    alternateClassName: ['Ext.form.DateTimeField', 'Ext.form.DateTime', 'Ext.form.field.DateTime'],
    
	/**
	 * @cfg {Object} dateConfig A configuration to transmit to the date field. See {@link Ext.form.field.Date}
	 */
    
    /**
	 * @cfg {Object} timeConfig A configuration to transmit to the time field. See {@link Ext.form.field.Time}
	 */
    
    /**
     * @cfg {String} submitFormat
     * The date format string which will be submitted to the server. The format must be valid according to
     * {@link Ext.Date#parse}.
     * Default to Ext.Date.patterns.ISO8601DateTime
     */
    submitFormat: Ext.Date.patterns.ISO8601DateTime,
    
    initComponent: function() 
    {
    	var me = this;
    	this.items = [
    	      Ext.create('Ext.form.field.Date', Ext.applyIf(this.dateConfig || {}, {flex: 0.7})), 
    	      Ext.create('Ext.form.field.Time', Ext.applyIf(this.timeConfig|| {}, {
    	    	  flex: 0.3
    	      }))
    	];
    	
        this.callParent();
    },
    
    /**
     * Get the date input field
     * @return {Ext.form.field.Date} the date input field
     */
    getDateField: function()
    {
    	return this.items.get(0);
    },
    
    /**
     * Get the time input field
     * @return {Ext.form.field.Time} the time input field
     */
    getTimeField: function()
    {
    	return this.items.get(1);
    },
    
    /**
     * Get the value of date input field (without time)
     * @return {Object} the date field value
     */
    getDateValue: function()
    {
    	return this.getDateField().getValue();
    },
    
    /**
     * Get the value of time input field (without date)
     * @return {Object} the time field value
     */
    getTimeValue: function()
    {
    	return this.getTimeField().getValue();
    },
    
    getValue: function ()
    {
    	var v1 = this.items.get(0).getValue();
    	var v2 = this.items.get(1).getValue();
    	
    	if (Ext.isDate(v1))
    	{
    		var d = Ext.Date.clearTime(v1, true);
			if (v2 != undefined)
			{
				d.setHours(v2.getHours(), v2.getMinutes(), v2.getSeconds(), v2.getMilliseconds());
			}
			return d;
    	}
    	else
    	{
    		return v1;
    	}
    },
    
    getJsonValue: function()
    {
    	return this.getSubmitValue();
    },
    
    setValue: function (value)
    {
    	if (!Ext.isDate(value))
		{
			value = Ext.Date.parse(value, this.submitFormat);
		}
    
    	this.items.each (function (item) {
    		item.setValue(value ? Ext.Date.clone(value) : null);
    	});
    },
	
    /**
     * @private
     * Compute the value to submit in #getSubmitData
     * @return {String} A date formated using #submitFormat. Or empty string if there is no value.
     */
    getSubmitValue: function() {
        var format = this.submitFormat,
            value = this.getValue();

        return value ? Ext.Date.format(value, format) : '';
    },
    
    getErrors: function (value) 
    {
    	var errors = this.callParent(arguments);
    	
    	errors = errors.concat(this.items.get(0).getErrors());
    	errors = errors.concat(this.items.get(1).getErrors());

    	return errors;
    }
});
