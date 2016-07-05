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
 * Define general actions on user prefs
 * @private
 */
Ext.define('Ametys.plugins.coreui.userprefs.UserPrefsActions', {
	singleton: true,
	
	/**
	 * This method will reset the workspace user preference AND reload the UI after asking the user
	 */
	resetWorkspace: function()
	{
		Ametys.Msg.show({
		    title: "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_RESET_WORKSPACE_PROMPT_TITLE}}",
		    msg: "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_RESET_WORKSPACE_PROMPT_DESC}}",
		    buttons: Ext.Msg.OKCANCEL,
		    fn: Ext.bind(this._resetWorkspaceNow, this),
		    icon: Ext.window.MessageBox.QUESTION
		});
	},
	
	/**
	 * @private
	 * Callback for the dialog of #resetWorkspace. Do the work effectively.
	 * @param {String} buttonId The ID of the button pressed, one of: ok, yes, no, cancel
	 * @param {String} text Value of the input field if either prompt or multiline is true
	 * @param {Object} opt The config object passed to show.
	 */
	_resetWorkspaceNow: function(buttonId, text, opt)
	{
		if (buttonId == 'ok')
		{
			Ametys.userprefs.UserPrefsDAO.saveValues( { "workspace": {} }, Ext.bind(this._workspaceResetedCB, this) );
		}
	},
	
	/**
	 * @private
	 * Callback of the Ametys.userprefs.UserPrefsDAO#saveValues done in #_resetWorkspaceNow
	 * @param {Boolean} success True is save worked fine.
     * @param {Object} errors Association (preference name, error message)
	 */
	_workspaceResetedCB: function(success, errors)
	{
    	if (success == false && errors != null)
    	{
    		var details = "";
    		for (var error in errors)
    		{
    			details += error + ": " + errors[error] + "\n";
    		}
    		
    	    Ametys.log.ErrorDialog.display({
    	        title: "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_RESET_WORKSPACE_FAILURE_TITLE}}",
    	        text: "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_RESET_WORKSPACE_FAILURE_DESC}}",
    	        details: details,
    	        category: this.self.getName() 
    	    });
    	}
    	else
    	{
    		Ametys.reload();
    	}
	},
	
	/**
	 * This method will restore the default user preferences AND reload the UI after asking the user
	 */
	restoreDefaults: function ()
	{
		this._form.getForm().reset();
	},
	
	/**
	 * This method will open a dialog box to edit the user preferences
	 */
	edit: function ()
	{
		if (!this._delayedInitializeUserPrefsDialog())
		{
			return;
		}
		
		this._initUserPrefsDialog();
		this._box.show();
	},
	
	/**
	 * @private
	 * Init the form dialog box with the user preferences values
	 */
	_initUserPrefsDialog: function ()
	{
		var values = {};
		var keys = Ametys.userprefs.UserPrefsDAO.keys();
		
		Ext.Array.each (keys, function (key) {
			values[key] = Ametys.userprefs.UserPrefsDAO.getValue(key);
		});
		
		this._form.setValues({values: values, repeaters: [], invalid: {}});
	},
	
	/**
	 * @private
	 * Draw the user preferences dialog box
	 */
	_delayedInitializeUserPrefsDialog: function ()
	{
		if (this._userPrefsDialogInitialized)
		{
			return true;
		}
		
		this._form = this._createFormEditionPanel();
		
		var response = Ametys.data.ServerComm.send({
			plugin: 'core',
			url: 'userprefs/def.json',
			parameters: {prefContext: Ametys.userprefs.UserPrefsDAO.getDefaultPrefContext(), excludePrivate: true}, 
			priority: Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, 
			responseType: 'text'
		});
		
		if (Ametys.data.ServerComm.handleBadResponse("{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_ERROR}}", response, Ext.getClassName(this)))
        {
            return false;
        }
		
		var result = Ext.JSON.decode(Ext.dom.Query.selectValue("", response));
		this._form.configure(result.preferences);
		
		this._box = Ext.create('Ametys.window.DialogBox', {
			title :"{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_LABEL}}",
			icon : Ametys.getPluginResourcesPrefix('core-ui') + "/img/userprefs/userprefs_16.png",
			
			width : 600,
			maxHeight: 500,
			scrollable: true,
			
			layout: {
				type: 'vbox',
				align: 'stretch'
			},
			
			items : [ this._form ],
			
			closeAction: 'hide',
			defaultFocus: this._form,
			
			buttons : [ {
				text :"{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_BTN_RESTORE_DEFAULTS}}",
				handler : this.restoreDefaults,
				scope: this
			}, ' ', ' ',{
				text :"{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_BTN_OK}}",
				handler : this._saveUserPrefs,
				scope: this
			}, {
				text :"{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_BTN_CANCEL}}",
				handler : function () { this._box.close()},
				scope: this
			} ]
		});
		
		this._userPrefsDialogInitialized = true;
		return true;
	},
	
	/**
	 * @private 
	 * Get the form panel to edit the user preferences
	 * @return {Ext.Panel} The form panel
	 */
	_createFormEditionPanel: function ()
	{
		return Ext.create('Ametys.form.ConfigurableFormPanel', {
				cls: 'userpref-form-inner',
				'tab-policy-mode': 'default',
                // fieldNamePrefix: 'content.input.',
				showAmetysComments: false
		});
	},
	
	/**
	 * @private
	 * Save the user preferences
	 */
	_saveUserPrefs: function ()
	{
        var me = this;
        function canSaveCb(canSave)
        {
            if (canSave)
            {
                Ametys.userprefs.UserPrefsDAO.saveValues(me._form.getValues(), Ext.bind(me._saveCb, me));
            }
        }
        Ametys.form.SaveHelper.canSave(this._form, canSaveCb);
	},
	
	/**
	 * @private
	 * Callback of the Ametys.userprefs.UserPrefsDAO#saveValues done in #_saveUserPrefs
	 * @param {Boolean} success True is save worked fine.
     * @param {Object} errors Association (preference name, error message)
	 */
	_saveCb: function (success, errors)
	{
		if (success == false && errors != null)
    	{
			Ametys.form.SaveHelper.handleServerErrors(this._form, 
					"{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_RESET_WORKSPACE_FAILURE_TITLE}}", 
					"{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_RESET_WORKSPACE_FAILURE_DESC}}", 
					errors);
    	}
    	else
    	{
    	    this._box.close();
    		
            Ext.create('Ametys.message.Message', {
                type: Ametys.message.Message.MODIFIED,
                
                targets: {
                    id: Ametys.message.MessageTarget.USER_PREFS,
                    parameters: {
                        context: Ametys.userprefs.UserPrefsDAO.getDefaultPrefContext()
                    }
                }
            });
    	}
	}
});
