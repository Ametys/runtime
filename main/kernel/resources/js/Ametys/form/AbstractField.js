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
 * Abstract class to creates a field that do not extend {@link Ext.form.Base} (field not representing by an input field).<br/>
 * You have to implement {@link #setValue}
 * To handle global errors override {@link #getErrors}.<br/>
 */
Ext.define('Ametys.form.AbstractField', {
    extend:'Ext.form.FieldContainer',
    
    mixins: {field: 'Ext.form.field.Field'},
    
    /**
     * @cfg {String} invalidCls
     * The CSS class to use when marking the component invalid.
     */
    invalidCls : Ext.baseCSSPrefix + 'form-invalid',
    
    /**
     * @cfg {String} dirtyCls
     * The CSS class to use when the field value {@link #isDirty is dirty}.
     */
    dirtyCls : Ext.baseCSSPrefix + 'form-dirty',
    
    /**
     * @cfg {String} blankText
     * The error text to display if the **{@link #allowBlank}** validation fails
     */
    blankText : Ext.form.field.Text.prototype.blankText,
    
    /**
     *  @cfg {Boolean} allowBlank=true. Specify false to validate that the value is not empty
     */
    allowBlank: true,
    
    /**
     *  @cfg {Boolean} preventMark=false true to disable displaying any error message set on this object.
     */
    preventMark: false,
    
    /**
     * Called when the field's dirty state changes. Adds/removes the dirtyCls on the main element.
     * @param {Boolean} isDirty The new dirty state
     * @private
     */
    onDirtyChange: function(isDirty) 
    {
    	this[isDirty ? 'addCls' : 'removeCls'](this.dirtyCls);
    },
    
    /**
     * @private 
     * Overrides the method from the Ext.form.Labelable mixin to also add the invalidCls to the inputEl,
     * as that is required for proper styling in IE with nested fields (due to lack of child selector)
     */
    renderActiveError: function() 
    {
        var me = this,
            hasError = me.hasActiveError();
        if (me.el) {
            // Add/remove invalid class
            me.el[hasError ? 'addCls' : 'removeCls'](me.invalidCls + '-field');
        }
        me.mixins.labelable.renderActiveError.call(me);
    },
    
    isValid: function() 
    {
    	var me = this,
        disabled = me.disabled,
        validate = me.forceValidation || !disabled;
        
    	return validate ? me.validateValue(me.getValue()) : disabled;
    },
    
    /**
     * <strong>Override this method to add specific validation.</strong><br/>
     * <strong>Validation on empty field are already made.</strong><br/>
     * Inherited documentation:<br/>
     * @inheritdoc
     * @method getErrors
     */
    getErrors: function (value)
    {
    	var errors = [];
    	
    	if (!this.allowBlank && (!value))
    	{
    		errors.push(this.blankText);
    	}
    	
    	return errors;
    },
    
    setError: function ()
    {
    	var me = this,
        msgTarget = me.msgTarget,
        prop;
        
    	if (me.rendered) {
	        if (msgTarget == 'title' || msgTarget == 'qtip') {
	            if (me.rendered) {
	                prop = msgTarget == 'qtip' ? 'data-errorqtip' : 'title';
	            }
	            me.getActionEl().dom.setAttribute(prop, active || '');
	        } else {
	            me.updateLayout();
	        }
    	}
    },
    
    /**
     * Uses {@link #getErrors} to build an array of validation errors. If any errors are found, they are passed to
     * {@link #markInvalid} and false is returned, otherwise true is returned.
     *
     * Previously, subclasses were invited to provide an implementation of this to process validations - from 3.2
     * onwards {@link #getErrors} should be overridden instead.
     *
     * @param {Object} value The value to validate
     * @return {Boolean} True if all validations passed, false if one or more failed
     * @private
     */
    validateValue: function(value) 
    {
        var me = this,
            errors = me.getErrors(value),
            isValid = Ext.isEmpty(errors);
        
        if (!me.preventMark) 
        {
            if (isValid) 
            {
                me.clearInvalid();
            } else 
            {
                me.markInvalid(errors);
            }
        }

        return isValid;
    },
    
    markInvalid: function(errors) 
    {
        // Save the message and fire the 'invalid' event
        var me = this,
            oldMsg = me.getActiveError();
        me.setActiveErrors(Ext.Array.from(errors));
        if (oldMsg !== me.getActiveError()) {
            me.updateLayout();
        }
    },
    
    clearInvalid: function() 
    {
    	// Clear the message and fire the 'valid' event
        var me = this,
            hadError = me.hasActiveError();
        me.unsetActiveError();
        if (hadError) {
            me.updateLayout();
        }
    }
    
});
