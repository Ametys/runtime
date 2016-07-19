/*
 *  Copyright 2016 Anyware Services
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
 * Singleton class checking the validity of a {@link Ametys.form.ConfigurableFormPanel} in order to save it
 */
Ext.define('Ametys.form.SaveHelper', {
    singleton: true,
    
    /**
     * Get the form from message targets
     * @param {Ametys.message.MessageTarget[]} targets the message targets
     * @return the form is found, null otherwise
     */
    getForm: function(targets)
    {
        var target = targets[0];
        if (target != null)
        {
            var subtargets = target.getSubtargets(function(target) { return target.getId() ==  Ametys.message.MessageTarget.FORM }, 0);
            if (subtargets.length == 0)
            {
            	Ext.MessageBox.alert("{{i18n PLUGINS_CORE_UI_SAVE_NOFORM_TITLE}}", "{{i18n PLUGINS_CORE_UI_CONFIG_SAVE_NOFORM}}");
                return null;
            }

            return subtargets[0].getParameters().object.owner;
        }
        
        return null;
    },
    
    /**
     * Dialog asking to save current modifications.
     * @param {String} title the title of the dialog box. Set to null in order to use a generic title
     * @param {String} message the explanatory text of the dialog box. Set to null in order to use a generic description
     * @param {String} icon the path of the icon to display within the dialog box. Set to null in order to use a generic icon
     * @param {Function} callback the function invoked after the user selected one of the three above choices.
     * @param {Boolean} callback.save true means the user want to save. false means the user does not want to save. null means the user does not want to save nor quit.
     */
    promptBeforeQuit: function(title, message, icon, callback)
    {	
    	Ametys.form.SaveHelper.SaveBeforeQuitDialog.showDialog(title, message, icon, callback);
    },
    
    /**
     * Determine if a form is ready to be saved. This function is asynchronous since some user interaction may be required.
     * @param {Ametys.form.ConfigurableFormPanel} form the configurable form panel
     * @param {Function} callback function invoked after the saving process was authorized or not
     * @param {Function} callback.canSave true if the form can be saved.
     */
    canSave: function(form, callback)
    {
        var me = this,
	        testsOk = true,
	        fieldCheckersManager = form._fieldCheckersManager,
	        fieldCheckers = fieldCheckersManager._fieldCheckers;
        
		// Validate form
		var invalidFields = Ext.Array.merge(form.getInvalidFields(), form.getInvalidRepeaters());
		if (invalidFields.length > 0)
		{
            // At least one field is invalid
			var errorMessage = "{{i18n PLUGINS_CORE_UI_SAVE_ACTION_INVALIDFIELDS}}";
			errorMessage += "<ul>";
			Ext.Array.each(invalidFields, function(invalidField) {
				errorMessage += "<li>";
				errorMessage += invalidField
				errorMessage += "</li>";
			})
			errorMessage += "</ul>";
			
			Ametys.Msg.show({
				   title: "{{i18n PLUGINS_CORE_UI_SAVE_ACTION_INVALID_TITLE}}", 
				   msg: errorMessage,
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR,
				   handler: function() {callback(false);}  
			});
		}
        else if (!this._areTestsOk(fieldCheckers))
        {
            // At least one test wasn't successful
            var msgBox = Ext.create('Ext.window.MessageBox', {closeAction: 'destroy'});
            
            msgBox.buttonText.yes = "{{i18n PLUGINS_CORE_UI_SAVE_TESTS_NOK_MBOX_SAVE}}";
            msgBox.buttonText.no = "{{i18n PLUGINS_CORE_UI_SAVE_TESTS_NOK_MBOX_RETRY}}";
            msgBox.buttonText.cancel = "{{i18n PLUGINS_CORE_UI_SAVE_TESTS_NOK_MBOX_CANCEL}}";
            msgBox.show({
                title: "{{i18n PLUGINS_CORE_UI_SAVE_TESTS_NOK_MBOX_TITLE}}", 
                msg: "{{i18n PLUGINS_CORE_UI_SAVE_TESTS_NOK_MBOX_MSG}}",
                buttons: Ext.Msg.YESNOCANCEL,
                icon: Ext.Msg.WARNING,
                fn: function(answer)
                {
                    if (answer == 'yes')
                    {
                    	this._handleWarnedFields(form, callback);
                    }
                    else if (answer == 'no')
                    {
                        Ext.getBody().mask("{{i18n PLUGINS_CORE_UI_SAVE_WAIT_MSG}}");
                        fieldCheckersManager.check(null, 
	            					               true, 
	            					               Ext.bind(function(success) 
	    					            		   { 
	        					            	  	 Ext.getBody().unmask(); 
	        					            	  	 if (success) 
	        					            	  	 { 
	        					            		 	this._handleWarnedFields(form, callback);
	    					            	  		 }  
	    					            		   }, me), false);
                    }
                },
                scope: this
            });
        }
        // All tests passed, check the warning on fields
        else
    	{
        	this._handleWarnedFields(form, callback);
    	}
		
        
        // Force the rendering of errors / warnings
        form._updateTabsStatus(true);
    },
   
    /**
     * Display a dialog box listing the errors found by the server
     * @param {Ametys.form.ConfigurableFormPanel} form the configurable form panel
     * @param {String} errorTitle the title of the save error dialog
     * @param {Ametys.form.ConfigurableFormPanel} errorMsg the introduction message of the error dialog
     * @param {Object} fieldErrors the mapping of field names with the corresponding error message
     * @param {Array} globalErrors the global error messages
     */
    handleServerErrors: function(form, errorTitle, errorMsg, fieldErrors, globalErrors)
    {
    	var fieldNamePrefix = form.getFieldNamePrefix();
    	var detailedMsg = '';
    	
    	// First treat the global errors
    	if (globalErrors && globalErrors.length > 0)
    	{
    		for (var i=0; i < globalErrors.length; i++)
    		{
    			detailedMsg += '<li>' + globalErrors[i] + '</li>'
    		}
    	}
    	
    	// Then treat the errors on fields
    	if (!Ext.Object.isEmpty(fieldErrors))
    	{
    		for (var name in fieldErrors)
    		{
    			var fieldName = fieldNamePrefix + name;
    			var fd = form.getForm().findField(fieldName);
    			
    			detailedMsg += '<li><b>' + (fd != null ? fd.getFieldLabel() : fieldName) + '</b>: ' + fieldErrors[name] + '</li>';
    		}
    		
    		form.markFieldsInvalid (fieldErrors);
    	}
    
    	Ametys.form.SaveHelper.SaveErrorDialog.showErrorDialog (errorTitle, errorMsg, '<ul>' + detailedMsg + '</ul>');
    },
    
    /**
     * @private
     * Determine if there are fields with warnings and so display a dialog box listing the fields in warning
     * @param {Ametys.form.ConfigurableFormPanel} form the configurable form panel
     * @param {Function} callback the callback function
     * @param {Function} callback.canSave true if there is no warning or if the user is fine with existing ones.
     */
    _handleWarnedFields: function(form, callback)
    {
    	var warnedFields = form.getWarnedFields();
		if (Ext.Object.getSize(warnedFields) > 0)
		{
			var msgBox = Ext.create('Ext.window.MessageBox', {closeAction: 'destroy'});
            msgBox.buttonText.yes = "{{i18n PLUGINS_CORE_UI_SAVE_ACTION_WARNED_FIELDS_YES}}";
            msgBox.buttonText.no = "{{i18n PLUGINS_CORE_UI_SAVE_ACTION_WARNED_FIELDS_NO}}";
			
			var message = "<ul>";
			Ext.Object.each(warnedFields, function(warnedFieldLabel) {
				var warningMessages = warnedFields[warnedFieldLabel];
				
				var warningList = "<ul>";
				Ext.Array.each(warningMessages, function(warningMessage) {
					warningList += "<li>";
					warningList += warningMessage;
					warningList += "</li>";
				});
				warningList += "</ul>";
				
				message += "<li>";
				message += "<strong>" + warnedFieldLabel + "</strong>: " + warningList
				message += "</li>";
			});
			message += "</ul>";
			
			msgBox.show({
		        title: "{{i18n PLUGINS_CORE_UI_SAVE_ACTION_WARNED_FIELDS_TITLE}}",
		        msg: "{{i18n PLUGINS_CORE_UI_SAVE_ACTION_WARNED_FIELDS_START}}" + message + "{{i18n PLUGINS_CORE_UI_SAVE_ACTION_WARNED_FIELDS_END}}",
		        buttons: Ext.Msg.YESNO,
		        icon: Ext.Msg.WARNING,
		        fn: function(answer) {callback(answer == 'yes');},
		        scope: this
		    });
		}
		else
		{
			// All checks passed successfully
	  		callback(true); 
		}
    },
    
    /**
     * @private
     * Are all the tests currently successful?
     * @param {Ametys.form.ConfigurableFormPanel.FieldChecker[]} fieldCheckers the field checkers
     * @return true if all the tests are currently successful, false otherwise
     */
    _areTestsOk: function(fieldCheckers)
    {
    	var testsOk = true;
		Ext.Array.each(fieldCheckers, function(fieldChecker) {
	        var status = fieldChecker.getStatus();
	        
	        if (Ext.getCmp(fieldChecker.buttonId).isVisible() 
	            && (status == Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_FAILURE 
	                || status == Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_NOT_TESTED
	                || status == Ametys.form.ConfigurableFormPanel.FieldChecker.STATUS_WARNING))
	        {
	        	testsOk = false;
                return false; // stop iteration
	        }
	    });
		
		return testsOk;
    }
});