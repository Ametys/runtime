/*
 *  Copyright 2009 Anyware Services
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

Ext.namespace('org.ametys.form');

/**
 * 
 * org.ametys.form.PasswordWidget
 *
 * @class This class handles a widget to password field
 * @extends Ext.Panel
 * @constructor
 * 
 * @param {Object} config Configuration options
 */
org.ametys.form.PasswordWidget = function(config) 
{
	this._rndVal = Math.floor(Math.random() * 100);
	
	this._name =  config.name;
	config.baseCls = 'ametys-password-field';
	config.border = false;
	if (!config.fdLabelWidth)
	{
		config.fdLabelWidth = 180;
	}
	config.labelAlign = 'right';
	config.height= 75;
	config.layout = 'form';
	
	this._pwdField = new Ext.form.Hidden({
		name : this._name,
		inputType : 'hidden'
	});
	
	this._pwd = new org.ametys.form.PasswordField({
		name : this._name + '_pwd',
		inputType : 'password',
		fieldLabel: config.fdLabel,
		value: config.value,
		tabIndex: this._rndVal,
		disabled: true,
		desc : config.desc
	});
	
	this._marginLeft = 'margin-left:' + (config.fdLabelWidth + 10) + 'px';
	
	if (!config.value || config.value == '')
	{
		this._pwdConfirm = this._createConfirmPwdField();
		this.items = [this._pwdField, this._pwd, this._pwdConfirm];
		this._pwd.enable();
	}
	else
	{
		this._editBtn = this._createChangePwdBtn();
		this.items = [this._pwdField, this._pwd, this._editBtn];
	}
	
	org.ametys.form.PasswordWidget.superclass.constructor.call(this, config);
};


Ext.extend(org.ametys.form.PasswordWidget, Ext.Panel, {});

/**
 * Set the value of password field. Use it only to initialize the widget.
 */
org.ametys.form.PasswordWidget.prototype.setValue = function (value)
{
	this._pwd.setValue(value);
	if (value != '')
	{
		this.remove(this._pwdConfirm, true);
		
		if (!this._editBtn)
		{
			this._editBtn = this._createChangePwdBtn();
			this.add(this._editBtn);
		}
		
		this._pwd.disable();
		
		this.doLayout();
	}
	else
	{
		if (!this._pwdConfirm)
		{
			this._pwdConfirm = this._createConfirmPwdField();
			this.add(this._pwdConfirm);
		}
		if (this._editBtn)
		{
			this.remove(this._editBtn, true);
		}
		this._pwdConfirm.setValue(value);
		this._pwdConfirm.clearInvalid();
		this._pwd.enable();
		
		this.doLayout();
	}
}


/**
 * Mark this widget as invalid.
 * @param msg The message to display. Can be null. If null the default message is used. 
 */
org.ametys.form.PasswordWidget.prototype.markInvalid = function (msg)
{
	if (msg)
		this._pwdConfirm.markInvalid(msg);
	else
		this._pwdConfirm.markInvalid("<i18n:text i18n:key="PLUGINS_CORE_WIDGET_PASSWORD_ERROR" i18n:catalogue="plugin.core"/>");
}

/**
 * Create the confirm password field
 * @private
 */
org.ametys.form.PasswordWidget.prototype._createConfirmPwdField = function ()
{
	var field = new org.ametys.form.PasswordField({
		name : this._name + "_confirm",
		widget:this,
		enableKeyEvents: true,
		msgTarget : 'side',
		fieldLabel: '',
		value: '',
		tabIndex: this._rndVal + 1,
		labelSeparator: '',
		validateValue : function(value){
			return this.getValue() == this.widget._pwd.getValue();
    	}
	});
	field.addListener('change', this._onChange, this);
	
	return field;
}

/**
 * Create the button to change the password
 * @private
 */
org.ametys.form.PasswordWidget.prototype._createChangePwdBtn = function ()
{
	return new Ext.Button({
		text: "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_PASSWORD_HINT" i18n:catalogue="plugin.core"/>",
		template : new Ext.Template(
				'&lt;div class="ametys-password-button x-btn {3}" style="' + this._marginLeft + '"&gt;',
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
org.ametys.form.PasswordWidget.prototype._createCancelBtn = function ()
{
	return new Ext.Button({
		icon : getPluginResourcesUrl('purge') + "/img/delete.gif",
		tooltip : "<i18n:text i18n:key="PLUGINS_CORE_WIDGET_PASSWORD_KEEPOLD" i18n:catalogue="plugin.core"/>",
		template : new Ext.Template(
				'&lt;div class="ametys-password-button-cancel" style="margin-left: 388px; margin-top: 4px; position: absolute"&gt;',
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
org.ametys.form.PasswordWidget.prototype._changePassword = function ()
{
	this.remove(this._editBtn, true);
	
	this._cancelBtn = this._createCancelBtn();
	this.add(this._cancelBtn);

	this._pwdConfirm = this._createConfirmPwdField ();
	this.add(this._pwdConfirm);
	
	this._pwd.enable();
	this._pwd.setValue('');
	
	this.doLayout();
	
	this._pwd.focus(10);
}

/**
 * Cancel the edition
 * @private
 */
org.ametys.form.PasswordWidget.prototype._cancelChange = function ()
{
	this._pwdConfirm.clearInvalid();
	
	var pNode = this._pwdConfirm.el.dom.parentNode.parentNode;
	this.remove(this._pwdConfirm, true);
	pNode.parentNode.removeChild(pNode);
	
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
org.ametys.form.PasswordWidget.prototype._onChange = function (field)
{
	if (!field.isValid())
	{
		field.markInvalid("<i18n:text i18n:key="PLUGINS_CORE_WIDGET_PASSWORD_ERROR" i18n:catalogue="plugin.core"/>");
	}
	else
	{
		this._pwdField.setValue(field.getValue());
	}
}
