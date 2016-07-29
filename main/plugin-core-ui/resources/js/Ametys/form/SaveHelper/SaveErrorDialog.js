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
 * This UI helper provides a dialog box to display errors while saving
 * See #showErrorDialog method
 * @private
 */
Ext.define('Ametys.form.SaveHelper.SaveErrorDialog', {
	singleton: true,
	
	/**
	 * @private
	 * @property {Boolean} _initialized Is the dialog already initialized?
	 */
	_initialized: false,
	
	/**
	 * Show the error dialog box
	 * @param {String} title The dialog box's title. Can be null to have a default 'save' title
	 * @param {String} msg The dialog box's main message
	 * @param {String} detailedMsg The detailed message (scrolling if too large)
	 */
	showErrorDialog: function (title, msg, detailedMsg)
	{
		this._delayedInitialized();
		this._errorDialog.setTitle (title || "{{i18n PLUGINS_CORE_UI_SAVE_ACTION_FAILED_TITLE}}");
		
		this._errorDialog.items.get(0).update(msg);
		this._errorDialog.items.get(1).update(detailedMsg);
		
		this._errorDialog.show();
	},
	
	/**
	 * @private
	 * Initialize the error dialog box
	 */
	_delayedInitialized: function ()
	{
		if (!this._initialized)
		{
			this._errorDialog = Ext.create('Ametys.window.DialogBox', {
		    	
		    	title: "{{i18n PLUGINS_CORE_UI_SAVE_ACTION_FAILED_TITLE}}",
		    	iconCls: 'ametysicon-letter-x5',
		    	
				width: 450,
				maxHeight: 280,
				layout: 'vbox',
					
				items: [{
						xtype: 'container',
						cls: 'save-error-dialog-text',
						width: '100%',
						html: ''
					},
					{
						xtype: 'container',
						cls: 'save-error-dialog-details',
						flex: 1,
						width: '100%',
						scrollable: true,
						html: ''
					}
				],
				
				defaultFocus: 0,
				
				closeAction: 'hide',
				
				buttons: [{
					text : "{{i18n PLUGINS_CORE_UI_SAVE_ACTION_FAILED_OK_BTN}}",
					handler : function () { this._errorDialog.close()},
					scope: this
				}]
			});

			this._initialized = true;
		}
	}
});