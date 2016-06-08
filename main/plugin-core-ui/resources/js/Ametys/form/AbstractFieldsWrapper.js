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
    
    statics: {
        /**
         * @private
         * @cfg {String} GLOBAL_ERRORS_FIELD_CLS
         * The CSS class to use when the field is invalid with at least a global error.
         */
        GLOBAL_ERRORS_FIELD_CLS: 'a-form-invalid-global',
        /**
         * @private
         * @cfg {String} BASE_FIELD_CLS
         * The CSS class to use when the field contains no visible input field but a readable text.
         */
        BASE_FIELD_CLS: 'a-form-abstract-field-wrapper'
    },
    
    /**
     * 
     * @property {Boolean} _hasGlobalErrors Current fields errors is the sum of errors of local fields and global errors. When true this property sepcify that there are global errors. E.g. If this field is mandatory but none of its sub fields are. 
     */
    _hasGlobalErrors: false,

    getDefaultCls: function()
    {
        return Ametys.form.AbstractFieldsWrapper.BASE_FIELD_CLS;
    },
    
    getErrors: function (value)
    {
        var errors = this.callParent(arguments);
        
        this._hasGlobalErrors = Ext.isArray(errors) && errors.length > 0; 
        
        return errors;
    },
    
    toggleInvalidCls: function(hasError) 
    {
        // Do not call parent, to not show invalid cls on inner field.
        // this.callParent(arguments);
        
        // On the global error cls must be added on the wrapper
        this.el[hasError && this._hasGlobalErrors ? 'addCls' : 'removeCls'](Ametys.form.AbstractFieldsWrapper.GLOBAL_ERRORS_FIELD_CLS);
    },
    
    /**
     * @inheritdoc
     * Listener on new fields, to set the property {@link Ext.form.field.Field#isFormField} and relay some events
     */
    onAdd: function (newComponent)
    {
        this.callParent(arguments);
        
        var formFields = [];
        
        if (newComponent.isFormField)
        {
            formFields.push(newComponent);
        }
        else if (newComponent.isContainer)
        {
            // deeper form field
            formFields = formFields.concat(newComponent.query('component[isFormField]'));
        }
        else if (newComponent.isButton)
        {
            newComponent.on({
                'focus': function (fd, e) { 
                    this.fireEvent ('focus', this, e);
                },
                'blur': function (fd, e) { 
                    if (this.getEl().query(Ext.makeIdSelector(document.activeElement.id)).length == 0) 
                    {
                        // We do not transmit blur if we still are in the same "parent" field
                        this.fireEvent ('blur', this, e); 
                    }
                },
                scope: this
            });
        }
        
        Ext.Array.forEach(formFields, function(field)
        {
            field.isFormField = false;
            field.isWrappedFormField = true; // internally mark the field to be able to still retrieves it later.
            
            field.on('change', this.checkChange, this);
            field.on('specialkey', this._checkSpecialKey, this);
            
            field.on({
                'focus': function (fd, e) { 
                    this.fireEvent ('focus', this, e);
                },
                'blur': function (fd, e) { 
                    if (this.getEl().query(Ext.makeIdSelector(document.activeElement.id)).length == 0) 
                    {
                        // We do not transmit blur if we still are in the same "parent" field
                        this.fireEvent ('blur', this, e); 
                    }
                },
                scope: this
            });
        }, this /* scope */);
    },
    
    onRender: function()
    {
        this.callParent(arguments);
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
			
			var formFields = this.query('component[isWrappedFormField],button');
			var index = formFields.indexOf(item);
			
			var itemToFocus;
			do 
			{
				index += direction;
				itemToFocus = formFields[index];
				if (itemToFocus != null && this._isFocusable(itemToFocus) && itemToFocus.isFormField === false)
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
        var isFileUpload = false;
        
        Ext.Array.each(this.query('component[isWrappedFormField]'), function (item) {
			if (Ext.isFunction(item.isFileUpload) && item.isFileUpload())
			{
			    isFileUpload = true;
			    return false; // stop iteration
			}
    	});
    	
    	return isFileUpload;
    },
    
    extractFileInput : function() 
    {
        var val = null;
        
        Ext.Array.each(this.query('component[isWrappedFormField]'), function (item) {
            if (Ext.isFunction(item.extractFileInput))
            {
                val = item.extractFileInput();
                if (val)
                {
                    return false; // stop iteration
                }
            }
        });
        
        return val;
    },
    
    /**
     * Called when the field's dirty state changes. Adds/removes the dirtyCls on the main element.
     * @param {Boolean} isDirty The new dirty state
     * @private
     */
    onDirtyChange: function(isDirty) 
    {
        this.callParent(arguments);
        
        Ext.Array.each(this.query('component[isWrappedFormField]'), function (item) {
            if (Ext.isFunction(item.onDirtyChange))
            {
                item.onDirtyChange();
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
        Ext.Array.each(this.query('component[isWrappedFormField]'), function (item) {
            if (Ext.isFunction(item.clearInvalid))
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
		var args = arguments,
		    canceled = false,
		    retValue;
		
        Ext.Array.each(this.query('component[isWrappedFormField],button'), function (item) {
            if (this._isFocusable(item), true)
            {
                retValue = item.cancelFocus.apply(item, args);
                canceled = true;
                return false; // stop iteration
            }
        }, this /* scope */);
        
        if (!canceled)
        {
            retValue = this.callParent(arguments);
        }
        
        return retValue;
    },
    
    focus: function()
    {
        var args = arguments,
            focused = false,
            retValue;
    
        Ext.Array.each(this.query('component[isWrappedFormField],button'), function (item) {
            if (this._isFocusable(item))
            {
                retValue = item.focus.apply(item, args);
                focused = true;
                return false; // stop iteration
            }
        }, this /* scope */);
        
        if (!focused)
        {
            retValue = this.callParent(arguments);
        }
        
        return retValue;
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
