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
 * Field that displays allows to change a password by double entering it
 */

Ext.define('Ametys.form.field.ChangePassword', {
    extend: 'Ametys.form.AbstractFieldsWrapper',
    alias: ['widget.changepasswordfield', 'widget.changepassword'],
    alternateClassName: ['Ext.form.ChangePasswordField', 'Ext.form.ChangePassword', 'Ext.form.field.ChangePassword'],
    
    statics: {
        /**
         * @protected
         * @readonly
         * @property {Number} 
         * index for the main field in the items
         */
        INDEX_MAIN_FIELD: 0,
        /**
         * @protected
         * @readonly
         * @property {Number} 
         * index for the confirmation field in the items
         */
        INDEX_CONFIRMATION_FIELD: 1,
        /**
         * @protected
         * @readonly
         * @property {Number} 
         * index for the change password button in the items
         */
        INDEX_CHANGEPASSWORD_BUTTON: 2,
        /**
         * @protected
         * @readonly
         * @property {Number} 
         * index for the change password button in the items
         */
        INDEX_RESETPASSWORD_BUTTON: 3,

        /**
         * @protected
         * @readonly
         * @property {Number} 
         * the mode when setting a password for the first time
         */
        MODE_SETPASSWORD: 0,
        /**
         * @protected
         * @readonly
         * @property {Number} 
         * the mode when displaying a password
         */
        MODE_SEEPASSWORD: 1,
        /**
         * @protected
         * @readonly
         * @property {Number} 
         * the mode when changing a existing password 
         */
        MODE_CHANGEPASSWORD: 2
    },
    
    /**
     * @cfg {Object} passwordConfig The configuration object for the first text field. Note that many configuration can be setted directly here and will we broadcasted to underlying field (allowBlank...)
     */
    /**
     * @cfg {Object} confirmConfig The configuration object for the second text field. Note that many configuration can be setted directly here and will we broadcasted to underlying field (allowBlank...). Default to {@link #passwordConfig}.
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
     * @cfg {Boolean} emptyText
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-emptyText}.
     */
    /**
     * @cfg {Boolean} invalidText
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-invalidText}.
     */
    /**
     * @cfg {Boolean} maskRe
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-maskRe}.
     */
    /**
     * @cfg {Boolean} maxLength
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-maxLength}.
     */
    /**
     * @cfg {Boolean} maxLengthText
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-maxLengthText}.
     */
    /**
     * @cfg {Boolean} minLength
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-minLength}.
     */
    /**
     * @cfg {Boolean} minLengthText
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-minLengthText}.
     */
    /**
     * @cfg {Boolean} regex
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-regex}.
     */
    /**
     * @cfg {Boolean} regexText
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-regexText}.
     */
    /**
     * @cfg {Boolean} selectOnFocus
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-selectOnFocus}.
     */
    /**
     * @cfg {Boolean} size
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-size}.
     */
    /**
     * @cfg {Boolean} stripCharsRe
     * This property is copied to underlying text fields. See {@link Ext.form.field.Text#cfg-stripCharsRe}.
     */

    layout: null,
    baseCls: 'ametys-changepassword ' + Ext.baseCSSPrefix + 'container',

	/**
	 * @property {Number} mode The current mode of the widget. One of MODE_SETPASSWORD, MODE_SEEPASSWORD and MODE_CHANGEPASSWORD.
	 */

    /**
     * @cfg {String} modificationBodyCls CSS class used when on mode 0 or 1. Default value is 'ametys-changepassword-body' that display a little connector between the two fields.
     */
    modificationBodyCls: 'ametys-changepassword-body',
    
    /**
     * @inheritdoc
     */
    initComponent: function() 
    {
    	// Password
    	var passwordConfig = this.passwordConfig || {};
    		passwordConfig.inputType = 'password';
    		passwordConfig.width = '100%';
    		passwordConfig.cls = 'ametys-changepassword-initial';
       	var propertiesToCopy = ['allowBlank', 'blankText', 'emptyText', 'invalidText', 'maskRe', 'maxLength', 'maxLengthText', 'minLength', 'minLengthText', 'regex', 'regexText', 'selectOnFocus', 'size', 'stripCharsRe'];
    	this._copyPropIfDefined(passwordConfig, propertiesToCopy, this.initialConfig);
    	var field1 = Ext.create('Ext.form.field.Text', passwordConfig);
    	
    	// Password confirm
    	var confirmConfig = this.confirmConfig || this.passwordConfig || {};
			confirmConfig.inputType = 'password';
			confirmConfig.cls = 'ametys-changepassword-confirmation';
			confirmConfig.width = '100%';
    	this._copyPropIfDefined(confirmConfig, propertiesToCopy, this.initialConfig);
    	var field2 = Ext.create('Ext.form.field.Text', confirmConfig);
    	
    	// Change password button
    	var buttonConfig = this.buttonConfig || {};
    	Ext.applyIf(buttonConfig, {
    		text: "<i18n:text i18n:key='KERNEL_PASSWORD_CHANGE' i18n:catalogue='kernel'/>",
    		cls: 'ametys-changepassword-change',
    		width: '100%',
    		handler: Ext.bind(this._setToMode, this, [Ametys.form.field.ChangePassword.MODE_CHANGEPASSWORD])
    	});
    	var chgBtn = Ext.create('Ext.button.Button', buttonConfig);

    	// Reset change button
    	var rstButtonCfg = {
    		handler: Ext.bind(this._setToMode, this, [Ametys.form.field.ChangePassword.MODE_SEEPASSWORD]),
    		tooltip: "<i18n:text i18n:key='KERNEL_PASSWORD_CLEAR' i18n:catalogue='kernel'/>",
    		cls: 'ametys-changepassword-reset',
    		height: 0
    	};
    	var rstButton = Ext.create('Ext.button.Button', rstButtonCfg);
    	rstButton.on ('show', this._placeResetButton, this);
    	
    	this.items = [field1, field2, chgBtn, rstButton];
    	
    	this.mode = Ametys.form.field.ChangePassword.MODE_SETPASSWORD;
    	this.msgTarget = 'side';
    	
    	this.on('afterrender', this._adaptRenderToMode, this);
    	
        this.callParent(arguments);
        
    },
    
    onResize: function (width, height)
    {
    	this.callParent(arguments);
    	this._placeResetButton();
    },
    
    /**
     * Place the reset button to the right position
     * @private
     */
    _placeResetButton: function ()
    {
    	var rstButton = this.items.get(Ametys.form.field.ChangePassword.INDEX_RESETPASSWORD_BUTTON);
    	if (rstButton.isVisible())
    	{
    		rstButton.el.setStyle ('left', (this.items.get(0).getWidth() + this.errorEl.getWidth() + 5) + 'px');
        	rstButton.el.setStyle ('top', '-28px');
    	}
    },
    
    getErrors: function (value) {
    	
    	var errors = [];
    	if (!this.allowBlank && !this.items.get(Ametys.form.field.ChangePassword.INDEX_MAIN_FIELD).getValue())
    	{
    		errors.push(this.blankText);
    	}

    	if (this.mode == Ametys.form.field.ChangePassword.MODE_SEEPASSWORD)
    	{
    		return errors;
    	}
    	
    	if ((arguments.length == 1 && value == undefined) || (arguments.length == 0 && this.items.get(Ametys.form.field.ChangePassword.INDEX_MAIN_FIELD).getValue() != this.items.get(Ametys.form.field.ChangePassword.INDEX_CONFIRMATION_FIELD).getValue()))
    	{
    		errors.push("<i18n:text key='KERNEL_PASSWORD_VALIDATOR' catalogue='kernel'/>");
    	}

    	return errors;
    },
    
    getValue: function()
    {
    	if (this.mode == Ametys.form.field.ChangePassword.MODE_SEEPASSWORD)
    	{
    		return null;
    	}
    	else if (this.items.get(Ametys.form.field.ChangePassword.INDEX_MAIN_FIELD).getValue() == this.items.get(Ametys.form.field.ChangePassword.INDEX_CONFIRMATION_FIELD).getValue())
		{
			return this.items.get(Ametys.form.field.ChangePassword.INDEX_MAIN_FIELD).getValue();
		}
		else 
		{
			return undefined;
		}
    },
    
    getSubmitData: function ()
    {
    	if (this.mode == Ametys.form.field.ChangePassword.MODE_SEEPASSWORD)
    	{
    		return null;
    	}
    	else
    	{
    		return this.callParent();
    	}
    },
    
    setValue: function(value)
    {
    	if (value == undefined || value == '')
    	{
    		this._setToMode(Ametys.form.field.ChangePassword.MODE_SETPASSWORD);
    	}
    	else
    	{
    		this._setToMode(Ametys.form.field.ChangePassword.MODE_SEEPASSWORD);
    	}
    	
    	this.items.get(Ametys.form.field.ChangePassword.INDEX_MAIN_FIELD).setValue(value);
    	this.callParent(arguments);
    },

    /**
     * Change the mode
     * @param {Number} mode The mode to set (see {@link #property-mode}). The render is modified.
     * @private
     */
    _setToMode: function(mode) {
    	if (this.mode == Ametys.form.field.ChangePassword.MODE_SEEPASSWORD)
    	{
        	this.previousValue = this.items.get(Ametys.form.field.ChangePassword.INDEX_MAIN_FIELD).getValue();
    	}
    	
    	this.mode = mode;

    	if (mode == Ametys.form.field.ChangePassword.MODE_SEEPASSWORD)
    	{
    		this.items.get(Ametys.form.field.ChangePassword.INDEX_MAIN_FIELD).setValue(this.previousValue);
    	}

    	this._adaptRenderToMode();
    	this.clearInvalid();
    },
    
    /**
     * Modify the render to adapt it to the current mode and current enable state
     * @private
     */
    _adaptRenderToMode: function() {
    	if (!this.rendered)
    	{
    		return;
    	}

    	var rstButton = this.items.get(Ametys.form.field.ChangePassword.INDEX_RESETPASSWORD_BUTTON);
    	rstButton.hide();
    	
    	var chgButton = this.items.get(Ametys.form.field.ChangePassword.INDEX_CHANGEPASSWORD_BUTTON);
    	
    	switch (this.mode)
    	{
    		case Ametys.form.field.ChangePassword.MODE_CHANGEPASSWORD:
    			rstButton.show();
    	    	
    		case Ametys.form.field.ChangePassword.MODE_SETPASSWORD:
    			this.items.get(Ametys.form.field.ChangePassword.INDEX_MAIN_FIELD).setValue('');
    			this.items.get(Ametys.form.field.ChangePassword.INDEX_CONFIRMATION_FIELD).setValue('');
    			
            	this.items.get(Ametys.form.field.ChangePassword.INDEX_MAIN_FIELD).setDisabled(this.disabled);
            	
            	this.items.get(Ametys.form.field.ChangePassword.INDEX_CONFIRMATION_FIELD).setDisabled(this.disabled);
            	this.items.get(Ametys.form.field.ChangePassword.INDEX_CONFIRMATION_FIELD).show();
            	
            	chgButton.hide();

            	break;
    		case Ametys.form.field.ChangePassword.MODE_SEEPASSWORD:
            	
    			this.items.get(Ametys.form.field.ChangePassword.INDEX_MAIN_FIELD).setDisabled(true);
            	
    			this.items.get(Ametys.form.field.ChangePassword.INDEX_CONFIRMATION_FIELD).setDisabled(true);
            	this.items.get(Ametys.form.field.ChangePassword.INDEX_CONFIRMATION_FIELD).hide();

            	chgButton.show();

            	break;
    	}
    },
    
    enable: function()
    {
    	this.callParent(arguments);
    	
    	this._adaptRenderToMode();
    },
    
    disable: function()
    {
    	this.callParent(arguments);
    	
    	this._adaptRenderToMode();
    }
});
