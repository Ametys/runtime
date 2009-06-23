/*
 * Copyright (c) 2008 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */

// Ametys Namespace
Ext.namespace('Ext.ametys.form');
/**
 * 
 * Ext.ametys.form.PasswordWidget
 *
 * @class This class handles a widget to password field
 * @extends Ext.Panel
 * @constructor
 * 
 * @param {Object} config Configuration options
 */
Ext.ametys.form.PasswordWidget = function(config) 
{
	this._name =  config.name;
	config.baseCls = 'ametys-password-field';
	config.border = false;
	if (!config.labelWidth)
	{
		config.labelWidth = 180;
	}
	config.labelAlign = 'right';
	config.height= 75;
	config.layout = 'form';
	
	this._pwdField = new Ext.form.Hidden({
		name : this._name,
		inputType : 'hidden'
	});
	
	this._pwd = new Ext.ametys.form.PasswordField({
		name : this._name + '_pwd',
		inputType : 'password',
		fieldLabel: config.fieldLabel,
		value: config.value,
		disabled: true,
		desc : config.desc
	});
	
	this._marginLeft = 'margin-left:' + (config.labelWidth + 10) + 'px'
	this._editBtn = this._createChangePwdBtn();
	
	this.items = [this._pwdField, this._pwd, this._editBtn]

	Ext.ametys.form.PasswordWidget.superclass.constructor.call(this, config);
};


Ext.extend(Ext.ametys.form.PasswordWidget, Ext.Panel, {});

/**
 * Create the confirm password field
 * @private
 */
Ext.ametys.form.PasswordWidget.prototype._createConfirmPwdField = function ()
{
	var field = new Ext.ametys.form.PasswordField({
		name : this._name + "_confirm",
		msgTarget : 'side',
		fieldLabel: '',
		value: '',
		labelSeparator: ''
	});
	field.addListener('change', this._onChange, this);
	return field;
}

/**
 * Create the button to change the password
 * @private
 */
Ext.ametys.form.PasswordWidget.prototype._createChangePwdBtn = function ()
{
	return new Ext.Button({
		text: "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_PASSWORD_HINT" i18n:catalogue="plugin.core"/>",
		template : new Ext.Template(
				'&lt;div class="ametys-password-button" style="' + this._marginLeft + '"&gt;',
	            	'&lt;button type="{1}"&gt;{0}&lt;/button&gt;',
	            '&lt;/div&gt;'),
	    handler: this._changePassword,
	    scope: this
	});
}

/**
 * Create the button to cancel the change
 * @private
 */
Ext.ametys.form.PasswordWidget.prototype._createCancelBtn = function ()
{
	return new Ext.Button({
		icon : getPluginResourcesUrl('purge') + "/img/delete.gif",
		tooltip : "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_PASSWORD_KEEPOLD" i18n:catalogue="plugin.core"/>",
		template : new Ext.Template(
				'&lt;div class="ametys-password-button-cancel" style="' + this._marginLeft + '"&gt;',
	            	'&lt;button type="{1}"&gt;{0}&lt;/button&gt;',
	            '&lt;/div&gt;'),
	    handler: this._cancelChange,
	    scope: this
	});
}

/**
 * Edit the password fields
 * @private
 */
Ext.ametys.form.PasswordWidget.prototype._changePassword = function ()
{
	this.remove(this._editBtn, true);
	
	this._pwdConfirm = this._createConfirmPwdField ();
	this.add(this._pwdConfirm);
	
	this._cancelBtn = this._createCancelBtn();
	this.add(this._cancelBtn);
	
	this._pwd.enable();
	this._pwd.setValue('');
	
	this.doLayout();
}

/**
 * Cancel the edition
 * @private
 */
Ext.ametys.form.PasswordWidget.prototype._cancelChange = function ()
{
	this.remove(this._pwdConfirm, true);
	this.remove(this._cancelBtn, true);
	
	this._editBtn = this._createChangePwdBtn();
	this.add(this._editBtn);
	
	this._pwd.disable();
	this._pwd.setValue('PASSWORD');
	this._pwdField.setValue('');
	
	this.doLayout();
}

/**
 * This function is called when the password was edited
 * @private
 */
Ext.ametys.form.PasswordWidget.prototype._onChange = function (field)
{
	if (field.getValue() != this._pwd.getValue())
	{
		field.markInvalid("<i18n:text i18n:key="PLUGINS_CORE_WIDGET_PASSWORD_ERROR" i18n:catalogue="plugin.core"/>");
	}
	else
	{
		this._pwdField.setValue(field.getValue());
	}
}

Ext.ametys.form.PasswordWidget.prototype.onRender = function(ct, position)
{
	Ext.ametys.form.PasswordWidget.superclass.onRender.call(this, ct, position);
}