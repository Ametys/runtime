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
 * Abstract class to create a field that wraps several fields.<br/>
 * You have to implement {@link #getValue} and {@link #setValue}.<br/>
 * Default layout is hbox.<br/>
 * To handle global errors override {@link #getErrors}.<br/>
 * To make your field compatible with form, if your are not returning strings, see {@link #getSubmitData}
 * <pre><code>
 *      // A component to enter a number (&gt; 5) with spinner and a two char text
 * 		Ext.define('Ametys.foo', {
 * 			extend : 'Ametys.form.AbstractFieldsWrapper',
 * 		
 * 			items: [
 * 				Ext.create('Ext.form.field.Number', {minValue: 5}),
 * 				Ext.create('Ext.form.field.Text', {
 * 					validator: function(value) {
 * 						if (value == undefined || value.length != 2)
 * 						{
 * 							return "String should have 2 chars";
 * 						}
 * 						return true;
 * 					}
 * 				})
 * 			],
 * 
 * 			getErrors: function(value) {
 *              var myLocalErrors = [];
 *              // compute errors
 * 				return Ext.Array.merge(this.callParent(arguments), myLocalErrors);
 * 			},
 * 
 * 			setValue: function(value) {
 * 				// split the value between items
 * 			},
 * 
 * 			getValue: function() {
 * 				// return the value composed by items
 * 			}
 *  	})
 * </code></pre>
 */
Ext.define('Ametys.form.AbstractFieldsWrapper', {
    extend:'Ametys.form.AbstractField',
    
    layout: 'hbox',

    /**
     * @inheritdoc
     * Listener on new fields, to set the property {@link Ext.form.field.Field#isFormField} and relay some events
     */
    onAdd: function (newComponent)
    {
    	this.callParent(arguments);
    	
    	if (newComponent.isFormField)
    	{
    		newComponent.isFormField = false;
    		newComponent.on('change', this.checkChange, this);
    		newComponent.on('specialkey', this._checkSpecialKey, this);
    		
    		newComponent.on('focus', function (fd, e) { this.fireEvent ('focus', this, e)}, this);
    		newComponent.on('blur', function (fd, e) { this.fireEvent ('blur', this, e)}, this);
    	}
    	else if (newComponent.isButton)
		{
    		newComponent.on('focus', function (fd, e) { this.fireEvent ('focus', this, e)}, this);
    		newComponent.on('blur', function (fd, e) { this.fireEvent ('blur', this, e)}, this);
		}
    },
    
    onRender: function()
    {
    	this.callParent(arguments);
        // this.onLabelableRender();
        this.renderActiveError();
	},
	
	/**
	 * @private
	 * Handle specialkey event on all items and will intercept TAB and SHIFT-TAB or transmit it
	 * @param {Ext.Component} item The item throwing the event
	 * @param {Ext.event.Event} e The event
	 */
	_checkSpecialKey: function(item, e)
	{
		var key = e.getKey();
		
		// Let us intercept TAB to navigate between internal items first
		if (key == e.TAB)
		{
			var direction = e.shiftKey ? -1 : +1;
			
			var index = this.items.indexOf(item);
			var itemToFocus;
			do 
			{
				index += direction;
				itemToFocus = this.items.getAt(index);
				if (this._isFocusable(itemToFocus) && itemToFocus.isFormField === false)
				{
					e.stopEvent()
					itemToFocus.focus();
					return;
				}
				
			}
			while (itemToFocus != null);
		}
		
        /**
         * @event specialkey
		 * Fires when any key related to navigation (arrows, tab, enter, esc, etc.) is pressed. To handle other keys see Ext.util.KeyMap. You can check Ext.event.Event.getKey to determine which key was pressed
         * @param {Ametys.form.AbstractFieldsWrapper} this
         * @param {Ext.event.Event} e The event object         
         */
    	this.fireEvent('specialkey', this, e);
	},
	
    /**
     * @inheritdoc
     * @template
     * @method getValue
     */

    /**
     * @inheritdoc
     * @template
     * @method setValue
     */

	/**
     * @inheritdoc
     * @method getErrors
     * @template
     */
	
    isFileUpload: function() 
    {
    	this.items.each(function (item) {
    		if (item.isFileUpload)
    		{
    			var val = item.isFileUpload();
    			if (val)
    			{
    				return val;
    			}
    		}
    	});
    	
    	return false;
    },
    
    extractFileInput : function() 
    {
    	this.items.each(function (item) {
    		if (item.extractFileInput)
    		{
    			var val = item.extractFileInput();
    			if (val)
    			{
    				return val;
    			}
    		}
    	});
    	
    	return null;
    },
    
    /**
     * Called when the field's dirty state changes. Adds/removes the dirtyCls on the main element.
     * @param {Boolean} isDirty The new dirty state
     * @private
     */
    onDirtyChange: function(isDirty) 
    {
    	this.callParent(arguments);
    	
    	this.items.each(function (item) {
    		if (item.onDirtyChange)
    		{
    			item.onDirtyChange(isDirty);
    		}
    	});
    },
    
    /**
     * <strong>Clear both invalids (global and local)</strong><br/>
     * Inherited documentation:<br/>
     * @inheritdoc
     */
    clearInvalid: function() 
    {
    	this.callParent(arguments);

    	var args = arguments;
    	
    	// local clear
    	this.items.each(function (item) {
    		if (item.clearInvalid)
    		{
    			item.clearInvalid(args);
    		}
    	});
    },
    
    enable: function()
    {
    	this.callParent(arguments);
    	
    	var args = arguments;

    	this.items.each(function (item) {
    		if (item.enable)
    		{
    			item.enable(args);
    		}
    	});
    },
    
    disable: function()
    {
    	this.callParent(arguments);
    	
    	var args = arguments;

    	this.items.each(function (item) {
    		if (item.disable)
    		{
    			item.disable(args);
    		}
    	});
    },
    
    cancelFocus: function()
    {
		for (var i = 0; i < this.items.getCount(); i++)
		{
			var item = this.items.getAt(i);
			if (this._isFocusable(item), true)
			{
				return item.cancelFocus.apply(item, arguments);
			}
		}
    	
    	return this.callParent(arguments);
    },
    
    focus: function()
    {
		for (var i = 0; i < this.items.getCount(); i++)
		{
			var item = this.items.getAt(i);
			if (this._isFocusable(item))
			{
				return item.focus.apply(item, arguments);
			}
		}
    	
    	return this.callParent(arguments);
    },
    
    /**
     * @private
     * Test if this item is focusable
     * @param {Ext.Component} item The item being focused
     * @param {Boolean} cancelable=false If true will test for cancelFocus instead
     */
    _isFocusable: function(item, cancelable)
    {
    	cancelable = cancelable || false;
    	
    	return item != null
    			&& (!cancelable && Ext.isFunction(item.focus) || (cancelable && Ext.isFunction(item.cancelFocus))) 
    			&& (!Ext.isFunction(item.isVisible) || item.isVisible()) 
    			&& (!Ext.isFunction(item.isDisabled) || !item.isDisabled());
    },
    
    /**
     * @protected
     * Copy the properties listed from source to target, only if they do not already exist in target and if they do exist in source.
     * @param {Object} target The target object
     * @param {String[]} properties The properties to copy
     * @param {Object} source The source object
     */
    _copyPropIfDefined : function(target, properties, source) 
    {
    	for (var i = 0; i < properties.length; i++)
    	{
    		var prop = properties[i];
    		
    		if (target[prop] == undefined && source != undefined && source[prop] != undefined)
    		{
    			target[prop] = source[prop];
    		}
    	}
    }
});
