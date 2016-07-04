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
 * Abstract class to creates a field that do not extend {@link Ext.form.field.Base} (field not representing by an input field).<br/>
 * You have to implement {@link #setValue}
 * To handle global errors override {@link #getErrors}.<br/>
 */
Ext.define('Ametys.form.AbstractField', {
    extend:'Ext.form.FieldContainer',
    
    mixins: {field: 'Ext.form.field.Field'},
    
    statics: {
        /**
         * @cfg {String} READABLE_TEXT_CLS
         * The CSS class to use for a component/container/panel used to display a readable value.
         */
        READABLE_TEXT_CLS: 'a-form-readable-text',
        /**
         * @private
         * @cfg {String} BASE_FIELD_CLS
         * The CSS class to use when the field contains no visible input field but a readable text.
         */
        BASE_FIELD_CLS: 'a-form-abstract-field'
    },
    
    /**
     * @cfg {String} invalidCls
     * The CSS class to use when marking the component invalid.
     */
    invalidCls : Ext.baseCSSPrefix + 'form-invalid',
    
    /**
     * @cfg {String} warningCls
     * The CSS class to use when marking the component warning.
     */
    warningCls : Ext.baseCSSPrefix + 'form-warning',
    
    /**
     * @cfg {String} dirtyCls
     * The CSS class to use when the field value {@link #isDirty is dirty}.
     */
    dirtyCls : Ext.baseCSSPrefix + 'form-dirty',
    
    /**
     * @cfg {String} emptyCls
     * The CSS class to use when the value if the field is empty.
     */
    emptyCls: Ext.baseCSSPrefix + 'form-empty',
    
    /**
     * @property {String} fieldFocusCls The CSS to apply to root element when field is focused 
     * @private
     */
    fieldFocusCls: "x-field-focus",
    
    /**
     * @cfg {String} blankText
     * The error text to display if the **{@link #allowBlank}** validation fails
     */
    blankText : "{{i18n PLUGINS_CORE_UI_DEFAULT_VALIDATOR_MANDATORY}}",
    
    /**
     *  @cfg {Boolean} allowBlank=true. Specify false to validate that the value is not empty
     */
    allowBlank: true,
    
    /**
     *  @cfg {Boolean} preventMark=false true to disable displaying any error message set on this object.
     */
    preventMark: false,
    
    /**
     *  @cfg {Boolean} focusable=true true to allow this field to be focused
     */
    focusable: true,
    
    /**
     * @cfg {Number} tabIndex=0 DOM tabIndex attribute for the focused element
     */
    tabIndex: 0,
    
    validateOnBlur: true,
    
    constructor: function(config)
    {
        config = config || {};
        config.cls = Ext.Array.from(config.cls);
        config.cls.push("x-field");
        config.cls.push(this.getDefaultCls());
        
        config.listeners = config.listeners || {};
        config.listeners.blur = {
            fn: this._onBlur,
            scope: this
        }
        
        this.callParent(arguments);
    },
    
    /**
     * @protected
     * Get the name of a CSS class that will be added during creation to the field
     * @return {String} A CSS classname
     */
    getDefaultCls: function()
    {
        return Ametys.form.AbstractField.BASE_FIELD_CLS;
    },
    
    initComponent: function ()
    {
        this.callParent();
        this.initField();
    },
    
    /**
     * @protected
     * On blur listener.
     * Currently used to validate the field
     */
    _onBlur: function()
    {
        if (this.validateOnBlur)
        {
            this.validate();
        }
    },
    
    /**
     * Called when the field's dirty state changes. Adds/removes the dirtyCls on the main element.
     * @param {Boolean} isDirty The new dirty state
     */
    onDirtyChange: function(isDirty) 
    {
        this[isDirty ? 'addCls' : 'removeCls'](this.dirtyCls);
    },
    
    /**
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
    
    setValue: function (value) 
    {   
        this.mixins.field.setValue.apply(this, arguments);
        
        if (Ext.isEmpty(value) || (Ext.isObject(value) && Ext.Object.isEmpty(value)))
        {
            this.addCls(this.emptyCls);
        }
        else
        {
            this.removeCls(this.emptyCls)
        }
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
        value = value || this.getValue();
        
        var errors = [];
        
        if (!this.allowBlank && (!value || (Ext.isArray(value) && value.length == 0)))
        {
            errors.push(this.blankText);
        }
        
        return errors;
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
        
        if (me.hasActiveWarning())
        {
            // Hide active warning message(s) if exist
            me.hideActiveWarning();
        }
        
        me.setActiveErrors(Ext.Array.from(errors));
        if (oldMsg !== me.getActiveError()) 
        {
            me.updateLayout();
        }
    },
    
    clearInvalid: function() 
    {
        // Clear the message and fire the 'valid' event
        var me = this,
            hadError = me.hasActiveError();
        
        me.unsetActiveError();
        if (hadError) 
        {
            me.updateLayout();
        }
        
        if (me.hasActiveWarning())
        {
            // Display active warning message(s) if exist
            me.renderActiveWarning();
        }
    },
    
    markWarning: function (warns)
    {
        this.setActiveWarnings(Ext.Array.from(warns));
        
        if (this.hasActiveError())
        {
            // Hide active warning message(s)
            this.hideActiveWarning();
        }
        
        this.updateLayout();
    },
    
    clearWarning: function() 
    {
        this.unsetActiveWarnings();
        this.updateLayout();
    },
    
    getFocusEl: function()
    {
    	var focusEl = this.element || this.el;
    	Ext.Array.each(this.items.items, function(item) {
    		if (item != null && item.focusable && Ext.isFunction(item.focus))
			{
    			focusEl = item;
    			return false;
			}
    	});
    	
    	return focusEl;
    },
    
    onFocus: function(e) 
    {
        var me = this;

        me.callParent(arguments);

        me.addCls(me.fieldFocusCls);
    },

    onBlur: function(e) 
    {
        var me = this;

        me.callParent(arguments);

        me.removeCls(me.fieldFocusCls);    
    }
});
