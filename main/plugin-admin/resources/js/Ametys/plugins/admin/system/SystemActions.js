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
 * Singleton class defining the actions related to the welcoming message of the application 
 * @private
 */
Ext.define('Ametys.plugins.admin.system.SystemActions', {
	singleton: true,
	
	/**
	 * @private
	 * @property {String} _mode The current mode when opening the edition dialog box {@link #_box}. Can be 'new' or 'edit'.
	 */
	/**
	 * @private
	 * @property {Boolean} _initialized Determine if the dialog box {@link #_box} for editing announce is initialized
	 */
	/**
	 * @private
	 * @property {Ext.form.Panel} _form The form panel in the dialog box {@link #_box} for editing
	 */
	/**
	 * @private
	 * @property {Ametys.window.DialogBox} _box The dialog box for editing
	 */
	
	/**
	 * Activate/deactivate the system announcement
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	setAnnouncementAvailable: function(controller)
	{
		var enable = controller.isPressed();
		
		Ametys.data.ServerComm.callMethod({
			role: "org.ametys.runtime.plugins.admin.system.SystemHelper",
			methodName: "setAnnouncementAvailable",
			parameters: [enable],
			callback: {
				scope: this,
				handler: this._enableAnnoucementCB,
				arguments: {
					state: enable
				}
			},
			errorMessage: {
				category: this.self.getName(),
				msg: "{{i18n PLUGINS_ADMIN_SYSTEM_ERROR_SAVE}}"
			}
		});
	},
	
	/**
	 * @private
	 * Callback called after enabling/disabling system announcement
	 * @param {Object} response the server's response
	 * @param {Object} args the callback arguments
	 */
	_enableAnnoucementCB: function(response, args)
	{
		Ext.create('Ametys.message.Message', {
			type: Ametys.message.Message.MODIFIED,
			targets: [{
				id: 'system-announcement',
				parameters: {
					state: args.state
				}
			}]
		});
	},
	
	/**
	 * Add a message
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	addMessage: function(controller)
	{
		this._mode = 'new';
		if (!this._delayedInitialize())
		{
			return;
		}
		
		this._box.show();
		this._initForm();
	},
	
	/**
	 * Edit the current selected message
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	editMessage: function (controller)
	{
		var target = controller.getMatchingTargets()[0];
		if (target != null)
		{
			var targetParameters = target.getParameters();
			this._mode = 'edit';
			
			if (!this._delayedInitialize())
			{
				return;
			}
			
			this._box.show();
			this._initForm(targetParameters.language, targetParameters.message);
		}
	},
	
	/**
	 * Delete the current selected message
	 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
	 */
	deleteMessage: function (controller)
	{
		var target = controller.getMatchingTargets()[0];
		if (target != null)
		{
			Ametys.Msg.confirm ("{{i18n PLUGINS_ADMIN_SYSTEM_ANNOUNCEMENT_DELETE_LABEL}}", 
				    "{{i18n PLUGINS_ADMIN_SYSTEM_ANNOUNCEMENT_DELETE_CONFIRM}}", 
				    Ext.bind(this._confirmDelete, this, [target.getParameters().language], 1));
		}
	},

    /**
     * @private
     * The callback after the delete confirmation messagebox
     * @param {String} answer If "yes" the process will continue.
     * @param {String} lang The message to delete identified by its language code.
     */
	_confirmDelete: function (answer, lang)
	{
		if (answer == 'yes')
		{
			Ametys.data.ServerComm.callMethod({
				role: "org.ametys.runtime.plugins.admin.system.SystemHelper",
				methodName: "deleteAnnouncement",
				parameters: [lang],
				callback: {
					scope: this,
					handler: this._deleteCb,
					arguments: {language: lang}
				},
				errorMessage: {
					category: this.self.getName(),
					msg: "{{i18n PLUGINS_ADMIN_SYSTEM_ERROR_SAVE}}"
				}
			});
		}
	},
	
	/**
	 * @private
	 * Callback for the deletion process
	 * @param {Object} response the server's response
	 * @param {Object} args the callback arguments
	 * @param {String} args.language the deleted language
	 */
	_deleteCb: function(response, args)
	{
		Ext.create('Ametys.message.Message', {
			type: Ametys.message.Message.DELETED,
			targets: [{
				id: 'system-announcement-message',
				parameters: {
					language: args.language
				}
			}]
		});
	},
	
	/**
	 * @private
	 * Initialize the dialog box
	 */
	_delayedInitialize: function ()
	{
		if (this._initialized)
		{
			return true;
		}
		
		this._form = Ext.create('Ext.form.Panel', {
			border: false,
			defaults : {
				cls: 'ametys',
				labelWidth: 70,
				width: 200,
				msgTarget: 'side',
				anchor:'90%',
				xtype: 'textfield'
			},
			
			items : [ {
							fieldLabel: "{{i18n PLUGINS_ADMIN_SYSTEM_COL_LANG}}",
							ametysDescription: "{{i18n PLUGINS_ADMIN_SYSTEM_ANNOUNCEMENT_DIALOG_LANG_HELP}}",
							name: 'language',
							allowBlank: false,
							regex: /^[a-z]{2}$/,
							regexText: "{{i18n PLUGINS_ADMIN_SYSTEM_ANNOUNCEMENT_DIALOG_ERROR_LANG}}"
						},
						{
							xtype: 'textarea',
							fieldLabel :"{{i18n PLUGINS_ADMIN_SYSTEM_COL_MESSAGE}}",
							ametysDescription: "{{i18n PLUGINS_ADMIN_SYSTEM_ANNOUNCEMENT_DIALOG_MESSAGE_HELP}}",
							allowBlank: false,
							name: 'message',
							height: 80
						}
			]
		});
		
		this._box = Ext.create('Ametys.window.DialogBox', {
			title :"{{i18n PLUGINS_ADMIN_SYSTEM_ANNOUNCEMENT_DIALOG_CAPTION}}",
			iconCls: 'ametysicon-megaphone19',
			
			layout :'fit',
			width: 450,
			
			items : [ this._form ],
			
			closeAction: 'hide',
			
			referenceHolder: true,
			defaultButton: 'okButton',
			
			buttons : [{
				reference: 'okButton',
				text :"{{i18n PLUGINS_ADMIN_SYSTEM_ANNOUNCEMENT_DIALOG_OK}}",
				handler : Ext.bind(this._ok, this)
			}, {
				text :"{{i18n PLUGINS_ADMIN_SYSTEM_ANNOUNCEMENT_DIALOG_CANCEL}}",
				handler : function () {this._box.hide();},
				scope: this
			}]
		});
		
		this._initialized = true;
		
		return true;
	},
	
	/**
	 * @private
	 * Initialize the form values
	 * @param {String} [language] the language's values. Can be null.
	 * @param {String} [message] the HTML message's value. Can be null.
	 */
	_initForm: function (language, message)
	{
		var form = this._form.getForm();
		var langFd = form.findField("language");
		var msgFd = form.findField("message");
		
		if (this._mode == 'new')
		{
			msgFd.reset();
			langFd.reset();
			
			langFd.focus(true);
		}
		else
		{
			msgFd.setValue(Ametys.convertHtmlToTextarea(message));
			
			langFd.setDisabled(true);
			langFd.setValue(language);
			
			msgFd.focus(true, 10);
		}
	},
	
	/**
	 * @private 
	 * Listener when clicking on the 'Ok' button
	 */
	_ok: function ()
	{
		if (!this._form.isValid())
		{
			return;
		}
		
		var lang = this._form.getForm().findField("language").getValue();
	    var message = Ametys.convertTextareaToHtml(this._form.getForm().findField("message").getValue());
	    
	    // First click on "Ok" => We check if the language exists on both addition and edition modes
	    this._editAnnouncement(lang, message, this._mode == 'edit');
	},

	/**
	 * @private
	 * Server call to add/edit the existing announcements
	 * @param {String} language the language
	 * @param {String} message the HTML message
	 * @param {Boolean} [override=false] true to override message if already exist
	 */
	_editAnnouncement: function(language, message, override)
	{
		Ametys.data.ServerComm.callMethod({
			role: "org.ametys.runtime.plugins.admin.system.SystemHelper",
			methodName: "editAnnouncement",
			parameters: [language, message, override || false],
			callback: {
				scope: this,
				handler: this._editAnnouncementCb,
				arguments: {
					language: language, 
					message: message
				} // if the user tries to replace an existing message by clicking on "Add"
			},
			errorMessage: {
				category: this.self.getName(),
				msg: "{{i18n PLUGINS_ADMIN_SYSTEM_ERROR_SAVE}}"
			}
		});
	},
	
	/**
	 * @private
	 * Function invoked after the user's chose to replace/not replace the existing message for the selected language key
	 * @param {String} answer 'yes' if the user clicked "Ok"
	 * @param {String} language the language key of the message
	 * @param {String} message the message itself
	 */
	_confirmCb: function(answer, language, message)
	{
		if (answer == 'yes')
		{
			this._editAnnouncement(language, message, true);
		}
	},
	
	/**
	 * @private
	 * Callback for the addition/edition process
	 * @param {Object} response the server's response
	 * @param {String} response.already-exists true if the language already exists, undefined otherwise
	 * @param {Object} args the callback arguments
	 * @param {String} args.language the language typed in by the user
	 * @param {String} args.message the message typed in by the user 
	 */
	_editAnnouncementCb: function(response, args)
	{
		if (response['already-exists'])
		{
			// Ask the user if he wants to replace the existing welcoming language
			Ametys.Msg.confirm ("{{i18n PLUGINS_ADMIN_SYSTEM_CONFIRM_DIALOG_TITLE}}", 
							    "{{i18n PLUGINS_ADMIN_SYSTEM_CONFIRM_DIALOG_TEXT}}", 
							    Ext.bind(this._confirmCb, this, [args.language, args.message], 1));
		}
		else
		{
			this._box.hide();
			
			Ext.create('Ametys.message.Message', {
				type: this._mode == "new" ? Ametys.message.Message.CREATED : Ametys.message.Message.MODIFIED,
				targets: [{
					id: 'system-announcement-message',
					parameters: {
						language: args.language,
						message: args.message
					}
				}]
			});
		}
	}
	
});

