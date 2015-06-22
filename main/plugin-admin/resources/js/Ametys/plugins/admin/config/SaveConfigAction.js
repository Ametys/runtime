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
 * Singleton class defining the actions related to the saving of the configuration.
 * @private
 */
Ext.define('Ametys.plugins.admin.config.SaveConfigAction', {
    singleton: true,
    
    /**
	 * @property {Number} LAUNCH_TESTS_SAVE_MBOX_HEIGHT The height and of the "launch tests and save" message box
	 * @private
	 * @readonly 
	 */
	LAUNCH_TESTS_SAVE_MBOX_HEIGHT: 130,
    
    /**
     * Save the configuration
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    save: function(controller)
    {
        var target = controller.getMatchingTargets()[0];
        if (target != null)
        {
        	var subtarget = target.getSubtargets()[0]; 
			if (subtarget.getType() != Ametys.message.MessageTarget.FORM)
			{
				return;
			}
        	
			var me = this,
				testsOk = true,
				form = subtarget.getParameters().form,
				paramCheckersDAO = form._paramCheckersDAO,
				paramCheckers = paramCheckersDAO._paramCheckers;
				
            // We save only when the form is valid
			if (!form.getForm().isValid())
			{
				Ext.MessageBox.alert("<i18n:text i18n:key='PLUGINS_ADMIN_CONFIG_SAVE_INVALID_TITLE'/>", "<i18n:text i18n:key='PLUGINS_ADMIN_CONFIG_SAVE_INVALID'/>");
				form.getForm().markInvalid();
				return;
		    }
            
            function doSave()
            {
                Ext.getBody().mask("<i18n:text i18n:key='PLUGINS_ADMIN_CONFIG_SAVE_WAIT_MSG'/>");
                Ext.defer(Ext.bind(me._save, me, [form.getValues()], false), 1, me);
            }


            // If all tests are not OK, we show a dialogbox to inform the user 
            Ext.Array.each(paramCheckers, function(paramChecker) {
                var status = paramChecker.getStatus();
                
                if (Ext.getCmp(paramChecker.buttonId).isVisible() 
                    && (status == Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_FAILURE 
                        || status == Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_NOT_TESTED
                        || status == Ametys.form.ConfigurableFormPanel.ParameterChecker.STATUS_WARNING))
                {
                    testsOk = false;
                    return false;
                }
            });
            
            if (!testsOk)
			{
				var msgBox = new Ext.window.MessageBox({closeAction: 'destroy'});
				
				msgBox.buttonText.yes = "<i18n:text i18n:key='PLUGINS_ADMIN_CONFIG_TESTS_NOK_MBOX_SAVE'/>";
				msgBox.buttonText.no = "<i18n:text i18n:key='PLUGINS_ADMIN_CONFIG_TESTS_NOK_MBOX_RETRY'/>";
				msgBox.buttonText.cancel = "<i18n:text i18n:key='PLUGINS_ADMIN_CONFIG_TESTS_NOK_MBOX_CANCEL'/>";
				msgBox.show({
							title: "<i18n:text i18n:key='PLUGINS_ADMIN_CONFIG_TESTS_NOK_MBOX_TITLE'/>", 
							msg: "<i18n:text i18n:key='PLUGINS_ADMIN_CONFIG_TESTS_NOK_MBOX_MSG'/>",
							buttons: Ext.Msg.YESNOCANCEL,
							icon: Ext.Msg.ERROR,
							height: me.LAUNCH_TESTS_SAVE_MBOX_HEIGHT,
							fn: function(btn)
							{
								if (btn == 'yes')
								{
									doSave();
								}
								else if (btn == 'no')
								{
									Ext.getBody().mask("<i18n:text i18n:key='PLUGINS_ADMIN_CONFIG_SAVE_WAIT_MSG'/>");
									Ametys.form.ConfigurableFormPanel.ParameterCheckersActions._check(paramCheckers, form, true, Ext.bind(function(success) { Ext.getBody().unmask(); if (success) { this.save(); } } , me), false);
								}
							}
						});
				return;
			}

            doSave();
        }
    },
    
    /**
     * The actual saving process, calling the server side
     * @param {Object} params the parameters used by the server
     */
    _save: function(params)
    {
	    var	result = null,
	    	ex = "";
	
	    try
	    {
	    	result =  Ext.Ajax.request({url: Ametys.getPluginDirectPrefix("admin") + "/config/set", params: params, async: false});	
	    }
	    catch (e)
	    {
	    	ex = e;
	    }
	    
	    Ext.getBody().unmask();
	    
		if (result == null)
	    {
			Ametys.log.ErrorDialog.display({
				title: "<i18n:text i18n:key='PLUGINS_ADMIN_SAVE_DIALOG_TITLE'/>", 
				text: "<i18n:text i18n:key='PLUGINS_ADMIN_CONFIG_SAVE_FATALERROR'/>",
	    		details: ex,
	    		category: "Ametys.plugins.core.administration.Config.save"
			});
	        return;
	    }
	    result = result.responseXML;
	    
	    var error = Ext.dom.Query.selectValue("*/error", result);
	    if (error != null && error != "")
	    {
	    	Ametys.Msg.show ({
	    		title: "<i18n:text i18n:key='PLUGINS_ADMIN_SAVE_DIALOG_TITLE'/>",
	    		msg: "<i18n:text i18n:key='PLUGINS_ADMIN_CONFIG_SAVE_ERROR'/>",
	    		buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.ERROR
	    	});
	        return;
	    }
	    
	    Ametys.Msg.show ({
	    		title: "<i18n:text i18n:key='PLUGINS_ADMIN_SAVE_DIALOG_TITLE'/>",
	    		msg: "<i18n:text i18n:key='PLUGINS_ADMIN_CONFIG_SAVE_OK'/>",
	    		buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.INFO,
				fn: Ametys.reload
	    });
    }
});