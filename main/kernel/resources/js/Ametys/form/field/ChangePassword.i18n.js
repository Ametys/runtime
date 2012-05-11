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
    extend: 'Ametys.form.AbstractFieldWrapper',
    alias: 'widget.changepasswordfield',
    alternateClassName: ['Ext.form.ChangePasswordField', 'Ext.form.ChangePassword', 'Ext.form.field.ChangePassword'],
    
    /**
     * @cfg {Object} passwordConfig The configuration object for the first text field. Note that many configuration can be setted directly here and will we broadcasted to underlying field (allowBlank...)
     */
    /**
     * @cfg {Object} confirmConfig The configuration object for the second text field. Note that many configuration can be setted directly here and will we broadcasted to underlying field (allowBlank...). Default to {@link #passwordCondig}.
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

    layout: {type: 'table', columns: 2},
    cls: 'ametys-changepassword',

    /**
     * @protected
     * @property {Integer} INDEX_MAIN_FIELD index for the main field in the items
     */
    INDEX_MAIN_FIELD: 0,
    /**
     * @protected
     * @property {Integer} INDEX_CONFIRMATION_FIELD index for the confirmation field in the items
     */
    INDEX_CONFIRMATION_FIELD: 1,
    /**
     * @protected
     * @property {Integer} INDEX_CHANGEPASSWORD_BUTTON index for the change password button in the items
     */
    INDEX_CHANGEPASSWORD_BUTTON: 3,
    /**
     * @protected
     * @property {Integer} INDEX_RESETPASSWORD_BUTTON index for the change password button in the items
     */
    INDEX_RESETPASSWORD_BUTTON: 2,
	
	/**
	 * @property {Integer} mode The current mode of the widget. One of MODE_SETPASSWORD, MODE_SEEPASSWORD and MODE_CHANGEPASSWORD.
	 */
    /**
     * @protected
     * @property {Integer} MODE_SETPASSWORD the mode when setting a password for the first time
     */
    MODE_SETPASSWORD: 0,
    /**
     * @protected
     * @property {Integer} MODE_SEEPASSWORD the mode when displaying a password
     */
    MODE_SEEPASSWORD: 1,
    /**
     * @protected
     * @property {Integer} MODE_CHANGEPASSWORD the mode when changing a existing password 
     */
    MODE_CHANGEPASSWORD: 2,

    /**
     * @cfg {String} modificationBodyCls CSS class used when on mode 0 or 1. Default value is 'ametys-changepassword-body' that display a little connector between the two fields.
     */
    modificationBodyCls: 'ametys-changepassword-body',
    
    initComponent: function() {
    	var passwordConfig = this.passwordConfig || {};
    		passwordConfig.inputType = 'password';
    		passwordConfig.cls = 'ametys-changepassword-initial';
    		passwordConfig.colspan = 2;
       	var propertiesToCopy = ['allowBlank', 'blankText', 'emptyText', 'invalidText', 'maskRe', 'maxLength', 'maxLengthText', 'minLength', 'minLengthText', 'regex', 'regexText', 'selectOnFocus', 'size', 'stripCharsRe'];
    	this._copyPropIfDefined(passwordConfig, propertiesToCopy, this.initialConfig);
    	var field1 = Ext.create('Ext.form.field.Text', passwordConfig);
    	field1.on ('change', this.validateGlobal, this);
    	
    	var confirmConfig = this.confirmConfig || this.passwordConfig || {};
			confirmConfig.inputType = 'password';
			confirmConfig.cls = 'ametys-changepassword-confirmation';
			confirmConfig.colspan = 1;
    	this._copyPropIfDefined(confirmConfig, propertiesToCopy, this.initialConfig);
    	var field2 = Ext.create('Ext.form.field.Text', confirmConfig);
    	field2.on ('change', this.validateGlobal, this);
    	
    	var buttonConfig = this.buttonConfig || {};
    	Ext.applyIf(buttonConfig, {
    		text: "<i18n:text i18n:key='KERNEL_PASSWORD_CHANGE' i18n:catalogue='kernel'/>",
    		cls: 'ametys-changepassword-change',
    		handler: Ext.bind(this._setToMode, this, [this.MODE_CHANGEPASSWORD]),
    		colspan: 2
    	});
    	var button1 = Ext.create('Ext.button.Button', buttonConfig);

    	var button2Config = {
    		handler: Ext.bind(this._setToMode, this, [this.MODE_SEEPASSWORD]),
    		tooltip: "<i18n:text i18n:key='KERNEL_PASSWORD_CLEAR' i18n:catalogue='kernel'/>",
    		cls: 'ametys-changepassword-reset'
    	};
    	var button2 = Ext.create('Ext.button.Button', button2Config);

    	this.items = [field1, field2, button2, button1];
    	this.resetPasswordBtn = button2;
    	
    	this.mode = this.MODE_SETPASSWORD;
    	
    	this.on('afterrender', this._adaptRenderToMode, this);
    	
        this.callParent();
        
    },
    
	splitValue: function(value) {
		return [value, value];
	},
	
	concatValues: function(values) {
		if (values[0] == values[1])
		{
			return values[0];
		}
		else 
		{
			return undefined;
		}
	},
	
	splitSize: function(width, height) {
		var sizes = [{}, {}, {}, {}];
		
		switch (this.mode)
		{
			case this.MODE_SETPASSWORD:
			case this.MODE_CHANGEPASSWORD:
				if (width)
				{
					sizes[this.INDEX_MAIN_FIELD].width = width - 8; // 8 is the width of the background image for 'connecting' both fields
					sizes[this.INDEX_CONFIRMATION_FIELD].width = width - 8;
				}
				break;
			case this.MODE_SEEPASSWORD:
				if (width)
				{
					sizes[this.INDEX_MAIN_FIELD].width = width;
					sizes[this.INDEX_CHANGEPASSWORD_BUTTON].width = width;
				}
				break;
		}
		
		return sizes;
	},
	
    getErrors: function(value) {
    	var a = this.callParent(arguments);

    	if (arguments.length == 1 &amp;&amp; value == undefined
    			|| arguments.length == 0 &amp;&amp; this.items.get(this.INDEX_MAIN_FIELD).getValue() != this.items.get(this.INDEX_CONFIRMATION_FIELD).getValue())
    	{
    		a.push("<i18n:text key='KERNEL_PASSWORD_VALIDATOR' catalogue='kernel'/>");
    	}

    	return a;
    },
    
    /**
     * @private
     * Same as validate for global
     * Doc of validate is :
     * @inheritdoc #validate
     */
    validateGlobal : function() {
        var me = this,
            isValid = me.isGlobalValid();
        if (isValid !== me.wasValid) {
            me.wasValid = isValid;
            me.fireEvent('validitychange', me, isValid);
        }
        return isValid;
    },
    
    setValue: function(value)
    {
    	if (value == undefined || value == '')
    	{
    		this._setToMode(this.MODE_SETPASSWORD);
    	}
    	else
    	{
    		this._setToMode(this.MODE_SEEPASSWORD);
    	}
    	
    	this.callParent(arguments);
    },

    /**
     * @private
     * @param {Interger} mode The mode to set (see {@link #cfg-mode}). The render is modified.
     */
    _setToMode: function(mode) {
    	if (this.mode == this.MODE_SEEPASSWORD)
    	{
        	this.previousValue = this.items.get(this.INDEX_MAIN_FIELD).getValue();
    	}
    	
    	this.mode = mode;

    	if (mode == this.MODE_SEEPASSWORD)
    	{
    		this.items.get(this.INDEX_MAIN_FIELD).setValue(this.previousValue);
    	}

    	this._adaptRenderToMode();
    	this.clearInvalid();
    },
    
    /**
     * @private
     * Modify the render to adapt it to the current mode and current enable state
     */
    _adaptRenderToMode: function() {
    	if (!this.rendered)
    	{
    		return;
    	}

    	this.resetPasswordBtn.hide();
    	
    	switch (this.mode)
    	{
    		case this.MODE_CHANGEPASSWORD:
    	    	this.resetPasswordBtn.show();
    	    	
    		case this.MODE_SETPASSWORD:
    			this.items.get(this.INDEX_MAIN_FIELD).setValue('');
    			this.items.get(this.INDEX_CONFIRMATION_FIELD).setValue('');
    			
            	this.bodyEl.addCls(this.modificationBodyCls);
            	
            	this.items.get(this.INDEX_MAIN_FIELD).setDisabled(this.disabled);
            	
            	this.items.get(this.INDEX_CONFIRMATION_FIELD).setDisabled(this.disabled);
            	this.items.get(this.INDEX_CONFIRMATION_FIELD).show();
            	
            	this.items.get(this.INDEX_CHANGEPASSWORD_BUTTON).hide();

            	break;
    		case this.MODE_SEEPASSWORD:
    			this.bodyEl.removeCls(this.modificationBodyCls);
            	
    			this.items.get(this.INDEX_MAIN_FIELD).setDisabled(true);
            	
    			this.items.get(this.INDEX_CONFIRMATION_FIELD).setDisabled(true);
            	this.items.get(this.INDEX_CONFIRMATION_FIELD).hide();

            	this.items.get(this.INDEX_CHANGEPASSWORD_BUTTON).show();

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
