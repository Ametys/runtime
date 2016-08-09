/*
 *  Copyright 2013 Anyware Services
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
 * This class provides a hidden field with a generated 6-digit number as value if value is not set.
 * This widget is registered for fields of type Ametys.form.WidgetManager#TYPE_STRING or type Ametys.form.WidgetManager#TYPE_LONG.<br>
 */
Ext.define('Ametys.form.widget.RandomNumber', {
    extend: 'Ext.form.field.Hidden',
  
    /**
     * @cfg {Number|String} digits=6 The number of digits
     */
    
    constructor: function (config)
    {
    	config.digits = parseInt (config.digits || 6);
    	this.callParent(arguments);
    },
    
    setValue: function (value)
    {
    	if (Ext.isEmpty(value))
    	{
    		// Generate random key with 6-digit number if empty
    		value = Math.floor((Math.random() * 0.9 + 0.1) * Math.pow(10, this.digits));
    	}
    	
    	this.callParent([value]);
    }
});
