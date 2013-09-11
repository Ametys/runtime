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
    extend: 'Ametys.form.AbstractFieldWrapper',
    alias: ['widget.datetimefield', 'widget.datetime'],
    alternateClassName: ['Ext.form.DateTimeField', 'Ext.form.DateTime', 'Ext.form.field.DateTime'],
    
	/**
	 * @cfg {Object} dateConfig A configuration to transmit to the date field. See {@link Ext.form.field.Date}
	 */
    
    /**
	 * @cfg {Object} timeConfig A configuration to transmit to the time field. See {@link Ext.form.field.Time
	 */
    
    /**
     * @cfg {String} submitFormat
     * The date format string which will be submitted to the server. The format must be valid according to
     * {@link Ext.Date#parse}.
     * Default to Ext.Date.patterns.ISO8601Long
     */
    submitFormat: Ext.Date.patterns.ISO8601Long,
    
    
    initComponent: function() {
    	this.items = [
    	      Ext.create('Ext.form.field.Date', this.dateConfig), 
    	      Ext.create('Ext.form.field.Time', this.timeConfig)
    	];
    	
        this.callParent();
    },
	
	splitValue: function(value) {
		if (!Ext.isDate(value))
		{
			value = Ext.Date.parse(value, this.submitFormat);
		}
		return value ? [Ext.Date.clone(value), Ext.Date.clone(value)] : [null, null];
	},
	
	concatValues: function(values) {
		if (values[0] == undefined)
		{
			return undefined;
		}
		else if (Ext.isDate(values[0]))
		{
			var d = Ext.Date.clearTime(values[0], true);
			if (values[1] != undefined)
			{
				d.setHours(values[1].getHours(), values[1].getMinutes(), values[1].getSeconds(), values[1].getMilliseconds());
			}
			return d;
		}
		else
		{
			return "";
		}
	},
	
	splitSize: function(width, height) {
		var sizes = [{}, {}];
		
		if (width)
		{
			sizes[0].width = width * 0.7;
			sizes[1].width = width * 0.3;
		}
		
		return sizes;
	},
	
    getSubmitData: function() {
        var me = this,
            data = null,
            val;
        if (!me.disabled && me.submitValue && !me.isFileUpload()) {
            val = me.getSubmitValue();
            if (val !== null) {
                data = {};
                data[me.getName()] = val;
            }
        }
        return data;
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
    }
});
