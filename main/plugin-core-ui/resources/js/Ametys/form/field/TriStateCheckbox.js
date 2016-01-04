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
 * Checkbox field with tri-state.
 * The default possible values are 'null', '0' and '1'.
 */
Ext.define('Ametys.form.field.TriStateCheckbox', {
    extend: 'Ext.form.field.Checkbox',
    alias: ['widget.tricheckbox', 'widget.tricheckboxfield'],
    
    /**
     * @cfg {Boolean} enableNullValueOnBoxClick=true Set to 'false' to disable the 'null' value (first state) when clicking checkbox. In other words, if 'false' the first state can be setted only on initial value.
     */
    enableNullValueOnBoxClick: true,
    
    /**
     * @property {Object[]} values Array of values which are toggled through, corresponding to the three states. Equals to 'null', '0' and '1' by default.
     */
    values: ['null', '0', '1'],
    
    /**
     * @property {String[]} checkedClasses The classes used for the three different states
     */
    checkedClasses: ['a-field-tricheckbox-null', 'a-field-tricheckbox-unchecked', 'a-field-tricheckbox-checked'],
    
    /**
     * @private
     * @property {Number} The current state. Internal use only.
     */
    currentCheck: 0,

    getSubmitValue: function()
    {    
    	return this.value;
    },
    
    getRawValue: function() 
    {    
    	return this.value;
    },
    
    getValue: function()
    {    
    	return this.value;
    },
    
    initValue: function() 
    {   
    	var me = this;
        me.originalValue = me.lastValue = me.value;
        me.suspendCheckChange++;
        me.setValue(me.value);
        me.suspendCheckChange--;
    },
    
    setRawValue: function(v)
    {      
    	var me = this;
    	var checkIndex = 0;
    	
        var oldCheck = me.currentCheck;
        me.currentCheck = me.getCheckIndex(v);
        me.value = me.rawValue = me.values[me.currentCheck];
        
        // Update classes
        var inputEl = me.inputEl;
        if (inputEl)
        {    
        	inputEl.dom.setAttribute('aria-checked', me.value == '1' ? true : false);
            me['removeCls'](me.checkedClasses[oldCheck])
            me['addCls'](me.checkedClasses[me.currentCheck]);
        }
    },
    
    /**
     * Returns the index from a value to a member of me.values 
     * @param value The value
     */
    getCheckIndex: function(value) 
    {    
    	for (var i = 0; i < this.values.length; i++) 
        {    
    		if (value === this.values[i]) 
            {    
    			return i;
            }
        }
        return 0;
    },
    
    /**
     * Handles a click on a checkbox
     */
    onBoxClick: function(e) 
    {    
    	this.toggle();
    },
    
    /**
     * @event tristatechange
     * Fires when the tri-state changed.
     * @param {Ametys.form.field.TriStateCheckbox} checkbox The tri-state checkbox field
     * @param {Object} value The current value
     */
    
    /**
     * Switches to the next checkbox-state
     */
    toggle: function() 
    {    
    	var me = this;
        if (!me.disabled && !me.readOnly)
        {    
        	var check = me.currentCheck;
            check++;
            
            if (check >= me.values.length)
            {
            	// Back to first state
            	check = me.enableNullValueOnBoxClick ? 0 : 1;
            }
            
            this.setValue(me.values[check]);
            this.fireEvent ('tristatechange', me, me.values[check]);
        }
    }
});
