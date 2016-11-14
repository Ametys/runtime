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
 * Field that displays allows to type in a password. If no password is set the field is empty, else
 * the field is initially disabled.
 */
Ext.define('Ametys.form.field.Password', {
    extend: 'Ametys.form.AbstractFieldsWrapper',
    alias: ['widget.passwordfield', 'widget.password'],
    alternateClassName: ['Ext.form.PasswordField', 'Ext.form.Password', 'Ext.form.field.Password'],
    
    statics: {
    	
    	/**
         * @protected
         * @readonly
         * @property {Number} 
         * the mode when no password has been defined yet
         */
        MODE_NOPASSWORD: 0,
        /**
         * @protected
         * @readonly
         * @property {Number} 
         * the mode when setting a password 
         */
        MODE_CHANGEPASSWORD: 1,
        /**
         * @protected
         * @readonly
         * @property {Number} 
         * the mode when displaying a password
         */
        MODE_SEEPASSWORD: 2,
        /**
         * @protected
         * @readonly
         * @property {String} 
         * the tooltip of the button for changing the password
         */
        RESET_PASSWORD_TEXT: "{{i18n PLUGINS_CORE_UI_PASSWORD_RESET}}",
        /**
         * @protected
         * @readonly
         * @property {String} 
         * the tooltip of the button for resetting the password
         */
        CHANGE_PASSWORD_TEXT: "{{i18n PLUGINS_CORE_UI_PASSWORD_CHANGE}}"
    },
    
    /**
     * @cfg {Object} passwordConfig The configuration object for the first text field. Note that many configuration can be set directly here and will we broadcasted to underlying field (allowBlank...)
     */
    /**
     * @cfg {Object} buttonConfig The configuration object for the button to change password.
     */
    /**
     * @cfg {Boolean} allowBlank
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-allowBlank}.
     */
    /**
     * @cfg {Boolean} blankText
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-blankText}.
     */
    /**
     * @cfg {String} emptyText
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-emptyText}.
     */
    /**
     * @cfg {String} invalidText
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-invalidText}.
     */
    /**
     * @cfg {RegExp} maskRe
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-maskRe}.
     */
    /**
     * @cfg {Number} maxLength
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-maxLength}.
     */
    /**
     * @cfg {String} maxLengthText
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-maxLengthText}.
     */
    /**
     * @cfg {Number} minLength
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-minLength}.
     */
    /**
     * @cfg {String} minLengthText
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-minLengthText}.
     */
    /**
     * @cfg {RegExp} regex
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-regex}.
     */
    /**
     * @cfg {String} regexText
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-regexText}.
     */
    /**
     * @cfg {Boolean} selectOnFocus
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-selectOnFocus}.
     */
    /**
     * @cfg {Number} size
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-size}.
     */
    /**
     * @cfg {RegExp} stripCharsRe
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-stripCharsRe}.
     */

	/**
	 * @private
	 * @property {Number} _mode The current mode of the widget. One of MODE_CHANGEPASSWORD, MODE_SEEPASSWORD and MODE_CHANGEPASSWORD.
	 */
    /**
     * @private
     * @property  {String} _previousValue the memorized value of the field
     */
    /**
     * @private
	 * @property {Ext.form.field.Text} _field the field containing the password
	 */
    /**
     * @private
	 * @property {Ext.button.Button} _button the button allowing to change/save the password's value
	 */
    /**
     * @private
     * @property  {Boolean} _initiallyInSeeMode true if the saved password is the good one, false otherwise
     */
    
    initComponent: function() 
    {
        // password
        this._initPasswordField();
        
    	this._mode = Ametys.form.field.Password.MODE_NOPASSWORD;
    	
    	// Change password button 
    	var buttonConfig = this.buttonConfig || {};
    	
    	Ext.applyIf(buttonConfig, {
    		tooltip: '',
            cls: 'a-btn-lighter',
    		handler: Ext.bind(this._setToMode, this, [null], false),
    		margin: '1 0 0 0',
    		border: false
    	});
    	this._button = Ext.create('Ext.button.Button', buttonConfig);
    	
    	this.items = this._getItems();
    	
    	this.msgTarget = 'side';
    	
    	this.on('afterrender', this._adaptRenderToMode, this);
    	
        this.callParent(arguments);
    },
    
    /**
     * @protected
     * Initialize the password field. This function in called from {@link #initComponent} 
     */
    _initPasswordField: function()
    {
        // Password field
        var passwordConfig = this.passwordConfig || {};
            passwordConfig.inputType = 'password';
            passwordConfig.flex = 1;

        var propertiesToCopy = this._getConfigPropertiesToCopy();
        this._copyPropIfDefined(passwordConfig, propertiesToCopy, this.initialConfig);
        
        this._field = Ext.create('Ext.form.field.Text', passwordConfig);
    },
       
    /**
     * @protected
     * Retrieves the name of the configuration properties to copy to the underlying field 
     * @return {String[]} The name of the properties
     */
    _getConfigPropertiesToCopy: function()
    {
        return ['emptyText', 'invalidText', 'maskRe', 'maxLength', 'maxLengthText', 'minLength', 'minLengthText', 'regex', 'regexText', 'selectOnFocus', 'size', 'stripCharsRe'];
    },
    
    /**
     * @protected
     * Returns the items of the component. This function in called from {@link #initComponent}
     */
    _getItems: function()
    {
        return [this._field, this._button];
    },
    
    getErrors: function (value) 
    {
        if (this._mode == Ametys.form.field.Password.MODE_SEEPASSWORD)
        {
            return [];
        }
        else
        {
            return Ext.Array.merge(this.callParent(arguments), this._field.getErrors(value));
        }
    },
    
    getValue: function()
    {
        if (this._mode == Ametys.form.field.Password.MODE_SEEPASSWORD)
    	{
    		return null;
    	}
    	else 
    	{
    		return this._field.getValue();
    	}
    },
    
    getSubmitData: function ()
    {
    	return this._mode == Ametys.form.field.Password.MODE_SEEPASSWORD ? null : this.callParent(arguments);
    },
    
    setValue: function(value)
    {
		this._setToMode((value == undefined || value == '') ? Ametys.form.field.Password.MODE_NOPASSWORD : Ametys.form.field.Password.MODE_SEEPASSWORD);
    		
		this._field.setValue(value);
		this._onFieldSetValue(value);
    	
    	this.callParent(arguments);
    },
    
    enable: function()
    {
    	this._adaptRenderToMode();

    	this.callParent(arguments);
    },
    
    disable: function()
    {
    	this._adaptRenderToMode();

    	this.callParent(arguments);
    },
    
    /**
     * @private
     * Change the mode
     * @param {Number} mode The mode to set (see {@link #property-_mode}). The render is modified.
     */
    _setToMode: function(mode) 
    {
    	if (mode == null && this._initiallyInSeeMode && this._mode == Ametys.form.field.Password.MODE_CHANGEPASSWORD)
		{
    		// we should not be able to go in MODE_SEEPASSWORD if the field was not initially in that mode
    		mode = Ametys.form.field.Password.MODE_SEEPASSWORD;
		}
    	
    	if (this._mode == Ametys.form.field.Password.MODE_SEEPASSWORD)
    	{
    		if (mode == null)
			{
    			mode = Ametys.form.field.Password.MODE_CHANGEPASSWORD;
			}
    		
        	this._previousValue = this._field.getValue();
        	this._initiallyInSeeMode = true;
    	}
    	
    	this._mode = mode;

    	if (mode == Ametys.form.field.Password.MODE_SEEPASSWORD)
    	{
    		this._field.setValue(this._previousValue);
    		this._onFieldSetValue(this._previousValue);
    	}
        else if (mode == Ametys.form.field.Password.MODE_CHANGEPASSWORD)
        {
            this._field.setValue('');
            this._onFieldSetValue('');
        }

    	this._adaptRenderToMode();
    	this.clearInvalid();
    },
    
    /**
     * @private
     * Modify the render to adapt it to the current mode and current enable state
     */
    _adaptRenderToMode: function() 
    {
    	if (!this.rendered)
    	{
    		return;
    	}

    	if (this._mode == Ametys.form.field.Password.MODE_NOPASSWORD)
		{
    		this._field.setDisabled(this.disabled);
    		this._onFieldSetDisabled(this.disabled);
    		
    		this._button.hide();
		}
    	else
		{
    		if (this._mode == Ametys.form.field.Password.MODE_SEEPASSWORD)
			{
				this._field.disable();
				this._onFieldSetDisabled(true);
				
				this._button.setTooltip(Ametys.form.field.Password.CHANGE_PASSWORD_TEXT);
				this._button.setIconCls("a-field-password-change");
			}
    		else
			{
    			// The user is modifying the value
    			this._field.enable();
    			this._onFieldSetDisabled(false);
    			
                this._field.focus();
                
				this._button.setTooltip(Ametys.form.field.Password.RESET_PASSWORD_TEXT);
				this._button.setIconCls("a-field-password-reset");
			}

    		this._button.show();
		}
    },
    
    /**
     * @protected
     * Internal hook on field set value to add specific process in inherited classes
     * @param {String} value The value set
     */
    _onFieldSetValue: function(value)
    {
        // nothing
    },
    
    /**
     * @protected
     * Internal hook on field set disabled to add specific process in inherited classes
     * @param {Boolean} disabled True is disabled
     */
    _onFieldSetDisabled: function(disabled)
    {
        // nothing
    }
});
