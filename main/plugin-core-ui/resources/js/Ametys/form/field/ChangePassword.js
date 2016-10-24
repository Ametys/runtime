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
    extend:'Ametys.form.field.Password',
    alias: ['widget.changepasswordfield', 'widget.changepassword'],
    alternateClassName: ['Ext.form.ChangePasswordField', 'Ext.form.ChangePassword', 'Ext.form.field.ChangePassword'],
    
    /**
     * @cfg {Object} confirmConfig The configuration object for the second text field. Note that many configuration can be set directly here and will we broadcasted to underlying field (allowBlank...). Default to {@link #passwordConfig}.
     */
    
    baseCls: 'ametys-changepassword ' + Ext.baseCSSPrefix + 'container',
    
    /**
     * @cfg {String} modificationBodyCls CSS class used when on mode 0 or 1. Default value is 'ametys-changepassword-body' that display a little connector between the two fields.
     */
//    modificationBodyCls: 'ametys-changepassword-body',
    // FIXME css rule is defined but modificationBodyCls is not used.
    
    layout: {
        type: 'hbox'
    },
    
    /**
     * @protected
     * Initialize the password field. This function in called from {@link #initComponent} 
     */
    _initPasswordField: function()
    {
        // Password
        this.passwordConfig = this.passwordConfig || {};
        this.passwordConfig.cls = 'ametys-changepassword-initial';
        
        this.callParent();
        
        // Password confirm
        var confirmConfig = this.confirmConfig || this.passwordConfig || {};
            confirmConfig.inputType = 'password';
            confirmConfig.cls = 'ametys-changepassword-confirmation';
            confirmConfig.flex = 1;
            
        var propertiesToCopy = this._getConfigPropertiesToCopy();
        this._copyPropIfDefined(confirmConfig, propertiesToCopy, this.initialConfig);
        this._confirmField = Ext.create('Ext.form.field.Text', confirmConfig);
    },
    
    /**
     * @protected
     * Returns the items of the component. This function in called from {@link #initComponent}
     */
    _getItems: function()
    {
        return [
                {
                    xtype: 'container',
                    flex: 1,
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    items: [
                        this._field,
                        this._confirmField
                    ]
                },
                this._button
            ];
    },
    
    getErrors: function (value) 
    {
        var errors = [];
        
        if (this._mode != Ametys.form.field.Password.MODE_SEEPASSWORD)
        {
            if  (value === undefined || this._field.getValue() != this._confirmField.getValue())
            {
                errors.push("{{i18n PLUGINS_CORE_UI_CHANGEPASSWORD_VALIDATOR}}");
            }
            
            errors = Ext.Array.merge(errors, this._confirmField.getErrors(value));
        }
        
        return Ext.Array.merge(this.callParent(arguments), errors);
    },
    
    getValue: function()
    {
        var value = this._field.getValue();
        
    	if (this._mode == Ametys.form.field.ChangePassword.MODE_SEEPASSWORD)
    	{
    		return null;
    	}
    	else if (value == this._confirmField.getValue())
		{
			return value;
		}
		else 
		{
			return undefined;
		}
    },
    
    _onFieldSetValue: function(value)
    {
        this._confirmField.setValue(value);
    },
    
    /**
     * @protected
     * Internal hook on field set disabled to add specific process in inherited classes
     * @param {Boolean} disabled True is disabled
     */
    _onFieldSetDisabled: function(disabled)
    {
        this._confirmField.setDisabled(disabled);
    }
});
