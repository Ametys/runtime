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
 * Abstract class to creates a field that wraps several fields.<br/>
 * You have to implement {@link #splitValue}, {@link #concatValues} and {@link #splitSize}.<br/>
 * Default layout is column.<br/>
 * To handle global errors override {@link getErrors}.<br/>
 * To make your field compatible with form, if your are not returning strings, see {@link getSubmitData}
 * <code>
 *      // A component to enter a number (&gt; 5) with spinner and a two chars text
 * 		Ext.define('Ametys.toto', {
 * 			extend : 'Ametys.form.AbstractFieldWrapper',
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
 * 			splitValue: function(value) {
 * 				var values = [undefined, undefined];
 * 
 * 				var i = value ? value.indexOf('-') : -1; 
 * 				if (i != -1)
 * 				{
 * 					values[0] = value.substring(0, i);
 * 					values[1] = value.substring(i + 1);
 * 				}
 * 
 * 				return values;
 * 			},
 * 
 * 			concatValues: function(values) {
 * 				if (values[0] == undefined || values[1] == undefined)
 * 				{
 * 					return undefined;
 * 				}
 * 				else
 * 				{
 * 					return values[0] + '-' + values[1];
 * 				}
 * 			},
 * 
 * 			splitSize: function(width, height) {
 * 				var sizes = [{}, {}];
 * 
 * 				if (width)
 * 				{
 * 					sizes[0].width = width * 0.3;
 * 					sizes[1].width = width * 0.7;
 * 				}
 * 
 * 				return sizes;
 * 			}
 *  	})
 * </code>
 */
Ext.define('Ametys.form.AbstractFieldWrapper', {
    extend:'Ext.form.FieldContainer',
    
    mixins: {field: 'Ext.form.field.Field'},
    
    layout: 'column',
    
    /**
     * Implements this method to spread the value out to the fields.
     * You do not have to set the value in the field, but simply returns an array of value that would be set (in the same order that fields are)
     * @param {Object} value The value to split
     * @return {Object[]} A non null array that always have the size corresponding to the number of fields.
     * @protected
     */
    splitValue: function(value)
    {
    	Ext.Error.raise('splitValue method must be implemented!');
    },
    
    /**
     * Implements this method to concat the internal fields values to the unique value.
     * You do not have to get the vluaes in the fields, but simply returns the unique value built from the values given (in the same order that fields are)
     * @param {Object[]} values A non null array that always have the size corresponding to the number of fiels.
     * @return {Object} The typed value for this field
     * @protected
     */
    concatValues: function(values)
    {
    	Ext.Error.raise('concatValues method must be implemented!');
    },
    
    
    /**
     * Implements this method to spread the size out to the wrapped elements (not only fields).
     * You do not have to set the size in the field, but simply returns an array of value that would be set (in the same order that elements are)
     * @param {Number} width The width of the wrapping 
     * @param {Number} height The height of the wrapping field
     * @return {Object[]} A non null array that always have the size corresponding to the number of elements. Each element is an object {width: , height: };
     * @protected
     */
    splitSize: function(width, height)
    {
    	Ext.Error.raise('splitSize method must be implemented!');
    },
    
    /**
     * @private
     * Listener on size
     */
    _sizeHasChanged: function()
    {
    	var size = this.bodyEl.getSize();
    	
    	var sizes = this.splitSize(size.width, size.height);

    	var index = 0;
    	this.items.each(function (item) {
    		if (item.setSize)
    		{
    			item.setSize(sizes[index].width, sizes[index].height);
    			index++;
    		}
    	});
    },
    
    /**
     * @private
     * Listener on new fields, to set the property {@link Ext.form.field.Field#isFormField} to false
     */
    _itemAdd: function (me, newComponent)
    {
    	if (newComponent.isFormField)
    	{
    		newComponent.isFormField = false;
    	}
    },
    
    onRender: function()
    {
    	this.callParent(arguments);
    	this.setSize(this.width, this.height);
        this.onLabelableRender();
        this.renderActiveError();
	},
    
    initComponent: function() {
    	this.on('boxready', this._sizeHasChanged, this)
    	this.on('resize', this._sizeHasChanged, this)
    	this.on('add', this._itemAdd, this)
    	
        this.callParent();
        
        this.initField();
    },
    
    getValue: function() {
    	var values = [];
    	
    	this.items.each(function (item) {
    		if (item.getValue)
    		{
    			values.push(item.getValue());
    		}
    	});
    	
    	return this.concatValues(values);
    },

    setValue: function(value) {
    	var values = this.splitValue(value);
    	
    	var index = 0;
    	this.items.each(function (item) {
    		if (item.setValue)
    		{
    			item.setValue(values[index]);
    			index++;
    		}
    	});
    	
    	this.checkChange();
    	return this;
    },

    isFileUpload: function() {
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
    
    extractFileInput : function() {
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
    
    onDirtyChange: function(isDirty) {
    	this[isDirty ? 'addCls' : 'removeCls'](this.dirtyCls);
    	
    	this.items.each(function (item) {
    		if (item.onDirtyChange)
    		{
    			item.onDirtyChange(isDirty);
    		}
    	});
    },
    
    /**
     * @private Overrides the method from the Ext.form.Labelable mixin to also add the invalidCls to the inputEl,
     * as that is required for proper styling in IE with nested fields (due to lack of child selector)
     */
    renderActiveError: function() {
        var me = this,
            hasError = me.hasActiveError();
        if (me.el) {
            // Add/remove invalid class
            me.el[hasError ? 'addCls' : 'removeCls'](me.invalidCls + '-field');
        }
        me.mixins.labelable.renderActiveError.call(me);
    },
    
    /**
     * <strong>Override this method to add a global validation.</strong><br/>
     * <strong>Individual validation are already made using classic individual field validators.</strong><br/>
     * Inherited documentation:<br/>
     * @inheritdoc
     * @method getErrors
     */
    
    /**
     * <strong>Mark global only errors.</strong><br/>
     * Inherited documentation:<br/>
     * @inheritdoc
     */
    markInvalid: function(errors) {
        // Save the message and fire the 'invalid' event
        var me = this,
            oldMsg = me.getActiveError();
        me.setActiveErrors(Ext.Array.from(errors));
        if (oldMsg !== me.getActiveError()) {
            me.updateLayout();
        }
    },
    
    /**
     * <strong>Clear both invalids (global and local)</strong><br/>
     * Inherited documentation:<br/>
     * @inheritdoc
     */
    clearInvalid: function() {
    	// global clear
    	this.clearGlobalInvalid();

    	var args = arguments;
    	
    	// local clear
    	this.items.each(function (item) {
    		if (item.clearInvalid)
    		{
    			item.clearInvalid(args);
    		}
    	});
    },
    
    /**
     * <strong>Test both validations (global and local)</strong><br/>
     * Inherited documentation:<br/>
     * @inheritdoc
     */
    isValid: function() { 
    	var me = this,
        	disabled = me.disabled,
        	validate = me.forceValidation || !disabled;
        
	    if (validate)
	    {
	    	var state = me.isGlobalValid();
	    	
	    	var args = arguments;
	    	var preventMark = this.preventMark;
	    	me.items.each(function (item) {
	    		if (item.isValid)
	    		{
	    			var oldPreventMark = item.preventMark; 
	    			item.preventMark = preventMark;
	    			state = item.isValid(args) ? state : false;
	    			item.preventMark = oldPreventMark;
	    		}
	    	});
	    	return state;
	    }
	    return disabled;
    },
    
    /**
     * Do the clearInvalid for the global errors only.
     * See {@link #clearInvalid}.
     * @protected
     */
    clearGlobalInvalid: function() {
        // Clear the message and fire the 'valid' event
        var me = this,
            hadError = me.hasActiveError();
        me.unsetActiveError();
        if (hadError) {
            me.updateLayout();
        }
    },

    /**
     * Do the isInvalid for the global errors only.
     * See {@link #isValid}.
     * @protected
     */
    isGlobalValid: function() {
        var me = this,
        disabled = me.disabled,
        validate = me.forceValidation || !disabled;
        
	    if (validate)
	    {
	    	var values = [];
	    	
	    	this.items.each(function (it) {
	    		if (it.processRawValue && it.getRawValue)
	    		{
	    			values.push(it.processRawValue(it.getRawValue()));
	    		}
	    	});
	    	
	    	var value = this.concatValues(values);
	    	return me.validateGlobalValue(value);
	    }
	    else
	    {
	    	return disabled;
	    }
    },
    
    validateGlobalValue: function(value) {
        var me = this,
            errors = me.getErrors(value),
            isValid = Ext.isEmpty(errors);
        if (!me.preventMark) {
            if (isValid) {
                me.clearGlobalInvalid();
            } else {
                me.markInvalid(errors);
            }
        }

        return isValid;
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
    
    /**
     * @protected
     * Copy the properties listed from source to target, only if they do not already exist in target and if they do exist in source.
     * @param {Object} target The target object
     * @properties {String[]} properties The properties to copy
     * @param {Object} source The source object
     */
    _copyPropIfDefined : function(target, properties, source) {
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
