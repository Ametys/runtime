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
 * This UI helper provides a dialog box to enter a url
 * See #open method
 */
Ext.define('Ametys.helper.EnterURL', {
	singleton: true,
	
	/**
	 * @property _cbFn {Function} The call back function to call after choosing file
	 * @private
	 */
	
	/**
	 * @property _box {Ametys.window.DialogBox} The dialog box
	 * @private
	 */
	
	/**
	 * Open dialog to allow the user to enter an url
	 * @param {String} icon The full path to icon (16x16 pixels) for the dialog box
	 * @param {String} title The title of the dialog box.
	 * @param {String} helpmessage The message displayed at the top of the dialog box.
	 * @param {String} footermessage The message displayed at the bottom of the dialog box.
	 * @param {Function} callback The method that will be called when the dialog box is closed. The method signature is <ul><li>id: The id of the file</li><li>filename: The name of the file</li><li>size: The size in byte of the file</li><li>viewHref: The href to VIEW the file</li><li>downloadHref: The href to DOWNLOAD the file</li></ul> The method can return false to made the dialog box keep open (you should display an error message in this case)
	 * @param {String} [defaultValue] The value to display at startup
	 * @param {String} [regex] A RegExp string to be tested against the field value during validation. Can be null.
	 * @param {String} [regexText] The error text to display if regex is used and the test fails during validation. Can be null to use default error text.
	 */
	open: function (icon, title, helpmessage, footermessage, callback, defaultValue, regex, regexText)
	{
		this._cbFn = callback;
		this._regex  = regex;
		
		this._initialize(icon, title, helpmessage, footermessage, regex, regexText);
		this._box.show();
		
		var fd = this._box.down('form').getForm().findField('url');
		fd.setValue(defaultValue || '');
		fd.clearInvalid();
	},
	
	/**
	 * Initialize the dialog box
	 * @param {String} icon The full path to icon (16x16 pixels) for the dialog box
	 * @param {String} title The title of the dialog box.
	 * @param {String} helpmessage The message displayed at the top of the dialog box.
	 * @param {String} footermessage The message displayed at the bottom of the dialog box.
	 * @param {String} regex A RegExp string to be tested against the field value during validation. Can be null.
	 * @param {String} regexText The error text to display if regex is used and the test fails during validation. Can be null to use default error text.
	 * @private
	 */
	_initialize:  function(icon, title, helpmessage, footermessage, regex, regexText)
	{
		if (!this._initialized)
		{
			this._box = Ext.create('Ametys.window.DialogBox', {
				title: title,
				icon: icon,
				
				width: 500,
				scrollable: true,
				layout: 'form',
				
				items: [{
							xtype: 'form',
							border: false,
							defaults: {
								cls: 'ametys',
								labelAlign: 'top',
								labelSeparator: '',
								labelWidth: 130
							},
							items: [{
										xtype: 'component',
										id: 'enterurl-helpmessage',
										cls: 'a-text',
										html: helpmessage || ''
									}, 
									{
										xtype: 'textfield',
										fieldLabel : "{{i18n PLUGINS_CORE_UI_HELPER_ENTERURL_LABEL}}",
										name: 'url',
										itemId: 'url',
										width: 450,
										regex: regex ? new RegExp(regex) : null,
										regexText: regexText || '',
										allowBlank: false,
										msgTarget: 'side'
									},
									{
										xtype: 'component',
										cls: 'a-text',
										id: 'enterurl-footermessage',
										html: footermessage || ''
									}
							]
						}
				],
				
				defaultFocus: 'url',
				closeAction: 'hide',
				
				referenceHolder: true,
				defaultButton: 'validate',
				
				buttons : [{
					reference: 'validate',
					text :"{{i18n PLUGINS_CORE_UI_HELPER_ENTERURL_OK}}",
					handler : Ext.bind(this._ok, this)
				}, {
					text :"{{i18n PLUGINS_CORE_UI_HELPER_ENTERURL_CANCEL}}",
					handler: Ext.bind(this._cancel, this)
				}]
			});
			
			this._initialized = true;
		}
		else
		{
			this._box.setIcon(icon);
			this._box.setTitle(title);
			this._box.down('#enterurl-helpmessage').update(helpmessage);
			this._box.down('#enterurl-footermessage').update(footermessage);
			
			this._box.down('form').getForm().findField('url').regex = regex ? new RegExp(regex) : null;
			this._box.down('form').getForm().findField('url').regexText = regexText || '';
		}
	},
	
	/**
	 * Function invoked when validating the dialog box
	 */
	_ok: function()
	{
		var fd = this._box.down('form').getForm().findField('url');
		if (!fd.isValid())
		{
			return;
		}
		
		if (this._cbFn)
		{
			Ext.Function.defer (this._cbFn, 0, null, [fd.getValue()]);
		}
		
		this._box.hide();
	},
	
	/**
	 * Function invoked when cancelling the dialog box
	 */
	_cancel: function()
	{
		if (this._cbFn)
		{
			Ext.Function.defer (this._cbFn, 0, null, [null]);
		}
		
		this._box.hide();
	}
	
});

