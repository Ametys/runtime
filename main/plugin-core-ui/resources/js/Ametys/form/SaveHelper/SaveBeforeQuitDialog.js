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
 * This UI helper provides a dialog box if modifications are pending while closing a tool
 * @private
 */
Ext.define('Ametys.form.SaveHelper.SaveBeforeQuitDialog', {
	singleton: true,
	
	/**
	 * @private
	 * @property {Boolean} _initialized Is the dialog already initialized?
	 */
	_initialized: false,
	
	/**
	 * @private
	 * @property {Function} _callback the callback function invoked upon the user's choice. See #showDialog
	 */
	
	/**
	 * @private
	 * @property {Ametys.window.DialogBox} _dialog the dialog box
	 */
	
	/**
	 * Show the dialog box
	 * @param {String} title The dialog box's title. Can be null to use a generic title
	 * @param {String} msg The dialog box's explanatory message
	 * @param {String} icon the path of the icon to display in the dialog box
	 * @param {Function} callback the callback function
     * @param {Boolean} callback.save true means the user want to save. false the user does not want to save. null the user does not want to save nor quit.
	 */
	showDialog: function (title, msg, icon, callback)
	{
		this._callback = callback;
		
		this._delayedInitialized();
	
		this._dialog.setTitle (title || "{{i18n PLUGINS_CORE_UI_HELPER_SAVE_BEFORE_QUIT_TITLE}}");
		this._dialog.items.get(0).update(msg || "{{i18n PLUGINS_CORE_UI_HELPER_SAVE_BEFORE_QUIT_MESSAGE}}");
		this._dialog.setIcon(icon || Ametys.getPluginResourcesPrefix('core-ui') + "/img/save_16.png");
		
		this._dialog.show();
	},
	
	/**
	 * @private
	 * Initialize the dialog box
	 */
	_delayedInitialized: function ()
	{
		if (!this._initialized)
		{
			this._dialog = Ext.create('Ametys.window.DialogBox', {
				width: 450,
				maxHeight: 280,
					
				closeAction: 'hide',

				items: [{
					xtype: 'component',
					width: '100%',
					html: ''
				}],				
				
				defaultFocus: 0,
				
				buttons: [{
					text: "{{i18n PLUGINS_CORE_UI_HELPER_SAVE_BEFORE_QUIT_SAVE_BTN_LABEL}}",
					handler: function () 
					{ 
						this._callback(true);
						this._callback = null;
						this._dialog.close();
				    },
					scope: this
				}, {
					text: "{{i18n PLUGINS_CORE_UI_HELPER_SAVE_BEFORE_QUIT_DONT_SAVE_BTN_LABEL}}",
					handler: function () 
					{ 
						this._callback(false);
						this._callback = null;
						this._dialog.close();
				    },
					scope: this
				}, {
					text: "{{i18n PLUGINS_CORE_UI_HELPER_SAVE_BEFORE_QUIT_CANCEL_BTN_LABEL}}",
					handler: function () 
					{ 
						this._dialog.close();
				    },
					scope: this
				}],
				
				listeners: {
					close: function() {
						if (Ext.isFunction(this._callback))
						{
							this._callback(null);
							this._callback = null;
						}
					},
					scope: this
				}
			});
			
			this._initialized = true;
		}
	}
});