/*
 *  Copyright 2015 Anyware Services
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
 * This widget display an Ext.form.field.Time field, but the difference is that this one will use the type "string" to get/set value.
 */
Ext.define('Ametys.form.widget.StringTime', {
    
    extend: 'Ametys.form.AbstractFieldsWrapper',
    alias: ['widget.stringtimefield', 'widget.stringtime'],
    
    /**
     * @cfg {String} format the disply format. Can be "VeryShortTime" to have hours and minutes or "ShortTime" to have hours, minutes and seconds
     * Defaults to "ShortTime" 
     */
    format: "VeryShortTime",
    
    /**
	 * @cfg {Object} timeConfig A configuration to transmit to the time field. See {@link Ext.form.field.Time}
	 */
    
    /**
     * @property {Ext.form.field.Time} _timeField the time field
     */
    
    initComponent: function()
    {
        this.items = this._getItems();
        this.callParent();
    },
	
    /**
     * @private
     * Get the items composing the fields
     * @return {Ext.Component[]} The items
     */
    _getItems: function ()
    {
    	this._timeField = Ext.create('Ext.form.field.Time', Ext.applyIf(this.timeConfig || {}, this._getTimeFieldConfig()));
    	return [this._timeField];
    },
    
    /**
     * @private
     * Get the time field's configuration
     * @return {Object} the time field's configuration
     */
    _getTimeFieldConfig: function ()
    {
        return {
            format: this.format == "ShortTime" ? Ext.Date.patterns.ShortTime : Ext.Date.patterns.VeryShortTime, 
            submitFormat: this.format == "ShortTime" ? "H:i:s" : "H:i",
    		value: this.value,
            flex: 1
        };
    },
    
	getValue: function()
	{
		return Ext.Date.format(this._timeField.getValue(), this.format == "ShortTime" ? "H:i:s" : "H:i");
	},
	
	setValue: function(value)
	{
		this._timeField.setValue(value);
	},
	
    getErrors: function (value) 
    {
    	return Ext.Array.merge(this.callParent(arguments), this._timeField.getErrors(value));
    }
});