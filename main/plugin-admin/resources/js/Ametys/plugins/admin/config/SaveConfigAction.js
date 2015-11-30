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
     * Save the configuration
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    save: function(controller)
    {
    	var form = Ametys.form.SaveHelper.getForm(controller.getMatchingTargets()); 
    	if (form != null)
		{
            Ametys.form.SaveHelper.canSave(form, Ext.bind(this._doSave, this, [form], 1));
        }
    },
    
    /**
     * Trigger the save process if it was authorized
     * @param {Boolean} canSave true if the saving is allowed, false otherwise
     * @param {Ametys.form.ConfigurableFormPanel} form the form panel
     */
    _doSave: function(canSave, form)
    {
    	if (!canSave)
		{
			return;
		}
    	
    	Ext.getBody().mask("<i18n:text i18n:key='PLUGINS_ADMIN_CONFIG_SAVE_WAIT_MSG'/>");
        Ext.defer(Ext.bind(this._save, this, [form.getValues()], false), 1, this);
    },

    /**
     * The actual saving process, calling the server side
     * @param {Object} params the parameters used by the server
     */
    _save: function(params)
    {
        var result = null,
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
        
        // Server error
        var error = Ext.dom.Query.selectValue("* > error", result);
        if (!Ext.isEmpty(error))
        {
        	Ametys.notify({
    	        type: 'error',
    	        title: "<i18n:text i18n:key='PLUGINS_ADMIN_SAVE_DIALOG_TITLE'/>",
    	        icon: Ametys.getPluginResourcesPrefix('admin') + '/img/config/config_32.png',
    	        description: "<i18n:text i18n:key='PLUGINS_ADMIN_CONFIG_SAVE_ERROR'/>"
    	    });
        	
        	Ametys.log.ErrorDialog.display({
                title: "<i18n:text i18n:key='PLUGINS_ADMIN_SAVE_DIALOG_TITLE'/>", 
                text: "<i18n:text i18n:key='PLUGINS_ADMIN_CONFIG_SAVE_ERROR'/>",
                details: error,
                category: "Ametys.plugins.core.administration.Config.save"
            });
            return;
        }
        
        Ametys.notify({
	        type: 'info',
	        title: "<i18n:text i18n:key='PLUGINS_ADMIN_SAVE_DIALOG_TITLE'/>",
	        icon: Ametys.getPluginResourcesPrefix('admin') + '/img/config/config_32.png',
	        description: "<i18n:text i18n:key='PLUGINS_ADMIN_CONFIG_SAVE_OK'/>"
	    });
        
    }
});