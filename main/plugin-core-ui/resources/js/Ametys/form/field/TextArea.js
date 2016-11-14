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
 * Field that displays a textarea field with a character counter bar 
 */
 Ext.define('Ametys.form.field.TextArea', {
    extend: 'Ametys.form.AbstractFieldsWrapper',
    
 	alias: ['widget.ametystextarea'],
    
    /**
     * @property {Ext.form.field.TextArea} _textareaField the textarea field
     */
    
    /** 
     * @cfg {String/Object} layout The textarea layout
     * @private 
     */
    layout: { 
        type: 'vbox',
        align: 'stretch'
    },
    
    /**
     * @private
     * @property {String} focusedCls The class for textarea when focused
     */
    focusedCls: 'x-field-focus',
    
    /**
     * @property {String} textareaCls The base class for textarea
     * @private
     */
    textareaCls: "x-field-textarea",
    
    /**
     * @private
     * @property {String} charCounterCls The css classname for the counter
     */
    charCounterCls: 'char-counter',
    /**
     * @private
     * @property {String} charCounterValueCls The css classname for the counter value
     */
    charCounterValueCls: 'char-counter-value',
    /**
     * @private
     * @property {String} charCounterMaxExceededCls The css classname when the max number of characters was exceeded
     */
    charCounterMaxExceededCls: 'char-count-maxexceed',
    
 	constructor: function(config)
 	{
        var items = [];
        
        this._textareaField = Ext.create('Ext.form.field.TextArea', Ext.applyIf(config.textareaConfig || {}, this._getTextAreaFieldConfig(config)));
        items.push(this._textareaField);
        
        config.id = config.id || Ext.id();
        config.cls = Ext.Array.from(config.cls);
        config.cls.push(this.textareaCls);
        
 		if (config.charCounter !== false)
		{
            var toolbarItems = [];
            this._maxLength = Ext.isNumber(config.maxLength) ? config.maxLength : Number.MAX_VALUE;
            
            toolbarItems.push({ 
                xtype: 'component', 
                cls: this.charCounterCls,
                html: "{{i18n PLUGINS_CORE_UI_FIELD_CARACTERS_COUNTER_1}} "
                     +    '<span id="' + config.id + '-counter-val' + '" class="' + this.charCounterValueCls + '">0</span>'
                     +    (this._maxLength == Number.MAX_VALUE ? '' : (" {{i18n PLUGINS_CORE_UI_FIELD_CARACTERS_COUNTER_2}} " + this._maxLength)) 
            });
            
            items.push({
                xtype: 'container',
                cls: this.textareaCls + "-toolbar",
                border: true,
                layout: {
                    type: 'hbox',
                    align: 'middle'
                },
                items: toolbarItems
            });
		}
 		
        config.items = items;
        
 		this.callParent(arguments);
        
        this._textareaField.on('change', this._updateCharCounter, this);
        this._textareaField.on('keyup', this._updateCharCounter, this);
        this._textareaField.on('render', this._updateCharCounter, this);
        
        this.on('focus', this._onFocus, this);
        this.on('blur', this._onBlur, this);
 	},
    
    /**
     * @private
     * Get the time field's configuration
     * @return {Object} the time field's configuration
     */
    _getTextAreaFieldConfig: function (initialConfig)
    {
        return {
            flex: 1,
            allowBlank: initialConfig.allowBlank,
            disabled: initialConfig.disabled || false,
            hidden: initialConfig.hidden || false,
            regex: initialConfig.regex || null,
            regexText: initialConfig.regexText || null,
            readOnly: initialConfig.readOnly || null,
            msgTarget: 'none',
            hideLabel: true,
            value: initialConfig.value,
            style: {
                marginBottom: 0
            }
        };
    },
    
    getValue: function()
    {
        return this._textareaField.getValue();
    },
    
    setValue: function(value)
    {
        this._textareaField.setValue(value);
    },
    
    getErrors: function (value) 
    {
        var errors = this._textareaField.getErrors(value);
        
        var count = this._textareaField.getValue().length;
        if (this._maxLength != Number.MAX_VALUE && count > this._maxLength)
        {
        	errors.push("{{i18n PLUGINS_CORE_UI_VALIDATOR_TEXT_MAXLENGTH}}");
        }
        return errors;
    },
    
    /**
     * @private
     * Updates the char counter under the textarea field.
     */
    _updateCharCounter: function()
    {
        var count = this._textareaField.getValue().length;
        
        var counter = Ext.get(this.getId() + '-counter-val'); 
        if (counter != null)
        {
            counter.setHtml("" + count);
            
            // is there a maxlength ?
            if (this._maxLength != Number.MAX_VALUE)
            {
                if (count > this._maxLength)
                {
                    counter.parent().addCls(this.charCounterMaxExceededCls);
                }
                else
                {
                    counter.parent().removeCls(this.charCounterMaxExceededCls);
                }
            }
        }
    },
    
    /**
     * @private
     * Add the focus CSS class when textarea field receives focus
     */
    _onFocus: function ()
    {
        this.addCls(this.focusedCls);
    },
    
    /**
     * @private
     * Remove the focus CSS class when textarea field loses focus
     */
    _onBlur: function ()
    {
        this.removeCls(this.focusedCls);
    }
 });
 