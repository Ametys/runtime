/*
 *  Copyright 2010 Anyware Services
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
 * org.ametys.form.PasswordCreationField
 *
 * @class This class handles a widget to password field
 * @extends Ext.Panel
 * @constructor
 * 
 * @param {Object} config Configuration options
 */
org.ametys.form.PasswordCreationField = function(config) 
{
	if (config.width)
	{
		config.width = config.width - org.ametys.form.PasswordCreationField.SIZE;
	}
	config.labelStyle = 'height: 40px;';
	
	org.ametys.form.PasswordCreationField.superclass.constructor.call(this, config);
	
	this.addListener('afterrender', this._drawFields, this);
	this.addListener('resize', this._resizeFields, this);
};

org.ametys.form.PasswordCreationField.SIZE = 10;
Ext.extend(org.ametys.form.PasswordCreationField, org.ametys.form.PasswordField, {});

org.ametys.form.PasswordCreationField.prototype._resizeFields = function()
{
	if (this.elParent)
	{
		this.elParent.setWidth(this.el.getWidth())
		this.el2.setWidth(this.el.getWidth());
	}
}

org.ametys.form.PasswordCreationField.prototype._getAutoEl = function()
{
	var autoEl = {};
	for (var i in this.defaultAutoCreate)
	{
		autoEl[i] = this.defaultAutoCreate[i];
	}
	autoEl.type = 'password';
	
	return autoEl;
}

org.ametys.form.PasswordCreationField.prototype.validateValue = function(value)
{
	if (this.el2 &amp;&amp; !this.el.dom.disabled)
	{
		return this.el2.dom.value == this.el.dom.value;
	}
	return true;
}

org.ametys.form.PasswordCreationField.prototype._drawFields = function(component)
{
	// add a parent div to wrap both password fields
	this.elParent = this.el.insertSibling({cls: 'ametys-input-password'}, 'before')
	this.elParent.dom.appendChild(this.el.dom);
	this.elParent.setWidth(this.el.getWidth());
	
	// creates the confirmation password field
	this.el2 = this.elParent.createChild(this._getAutoEl());
    if(this.tabIndex !== undefined)
    {
    	this.el2.dom.setAttribute('tabIndex', this.tabIndex);
    }
	this.el2.addClass([this.fieldClass, this.cls, 'x-form-text', 'ametys-input-password-confirmation']);
	this.el2.setWidth(this.el.getWidth());
	this.el2.setVisibilityMode(Ext.Element.DISPLAY);

	// change password
	this.changeBtn = this.elParent.createChild({tag: 'button', html: "<i18n:text i18n:key="KERNEL_PASSWORD_CHANGE" i18n:catalogue="kernel"/>"});
	this.changeBtn.addListener('click', this._setPassword, this);
	this.changeBtn.setVisibilityMode(Ext.Element.DISPLAY);
	this.changeBtn.hide();
	this.changeBtn.setWidth(this.el.getWidth() + org.ametys.form.PasswordCreationField.SIZE);

	// create a div for the "connection" symbol
	this.elConnection = this.elParent.insertSibling({cls: 'ametys-input-password-connector'}, 'after');
	this.elConnection.setHeight(this.elParent.getHeight() - 20);
	this.elConnection.setWidth((org.ametys.form.PasswordCreationField.SIZE - 3) + 'px');
	this.elConnection.setVisibilityMode(Ext.Element.DISPLAY);
	
	// reset password
	this.resetBtn = this.elConnection.next().insertSibling({
		id: Ext.id(),
		tag:'img',
		style: 'padding-left: 20px; padding-top : 7px; display: block; cursor: pointer;',
		src: getPluginResourcesUrl('core') + '/img/administrator/config/password_reset.png'}, 'after');
	this.resetBtn.addListener('click', this._resetPassword, this);
	this.resetBtn.hide();
	this.helpEl.setStyle('float', '');

	var tooltip = new Ext.ToolTip({
        target: this.resetBtn.id,
        html: "<i18n:text i18n:key="KERNEL_PASSWORD_CLEAR" i18n:catalogue="kernel"/>",
        
        dismissDelay: 0 // disable automatic hiding
    });
	
	// add event
	this.el.addListener('blur', this._blurMain, this);
	this.el2.addListener('blur', this._blurConfirmation, this);
	
	if (this.value != null &amp;&amp; this.value != '')
	{
		this._resetPassword();
	}
}

org.ametys.form.PasswordCreationField.prototype._blurMain = function()
{
	if (!this.isValid())
	{
		this.el2.dom.value = '';
		this.el2.focus();
	}
}

org.ametys.form.PasswordCreationField.prototype._blurConfirmation = function()
{
	if (!this.isValid())
	{
		Ext.MessageBox.alert("<i18n:text i18n:key="KERNEL_PASSWORD_VALIDATOR_TITLE" i18n:catalogue="kernel"/>", "<i18n:text i18n:key="KERNEL_PASSWORD_VALIDATOR" i18n:catalogue="kernel"/>", this.el.focus.createCallback(this.el));
		this.el.dom.value = '';
		this.el2.dom.value = '';
		this.markInvalid();
	}
}

org.ametys.form.PasswordCreationField.prototype.getValue = function()
{
	if (this.el &amp;&amp; this.el.dom.disabled)
	{
		return null;
	}
	else
	{
		return org.ametys.form.PasswordCreationField.superclass.getValue.call(this);
	}
}

org.ametys.form.PasswordCreationField.prototype.setValue = function(v)
{
	org.ametys.form.PasswordCreationField.superclass.setValue.call(this, v);

	if (!this.el2)
	{
		return;
	}
	
	this.originalValue = v;

	if (v == "")
	{
		this._setPassword();
		this.resetBtn.hide();
	}
	else
	{
		this._resetPassword();
	}
}

org.ametys.form.PasswordCreationField.prototype._setPassword = function()
{
	this.clearInvalid();
	this.setDisabled(false);
	this.el.setWidth(this.width);
	this.elParent.setWidth(this.el.getWidth());
	this.changeBtn.hide();
	this.el2.show();
	this.elConnection.show();
	this.resetBtn.show();
	this.el.dom.value = '';
	this.el2.dom.value = '';
	this.el.focus(10);
}

org.ametys.form.PasswordCreationField.prototype._resetPassword = function()
{
	this.clearInvalid();
	this.setDisabled(true);
	this.el.setWidth(this.width + org.ametys.form.PasswordCreationField.SIZE);
	this.elConnection.hide();
	this.elParent.setWidth(this.el.getWidth());
	this.el.dom.value = this.originalValue;
	this.el2.hide();
	this.resetBtn.hide();
	this.changeBtn.show();
}
