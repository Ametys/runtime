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
 * Singleton class implementing restart actions 
 * @private
 */
Ext.define('Ametys.plugins.admin.restart.RestartActions', {
    singleton: true,
    
    /**
     * Restart the application
     */
    forceNormalMode: function()
    {
        Ametys.Msg.show({
            title: "{{i18n PLUGINS_ADMIN_PLUGINS_SAVE_CHANGES_LABEL}}",
            message: "{{i18n PLUGINS_ADMIN_RESTART_RESTART_MSG}}",
            icon: Ext.Msg.QUESTION,
            buttons: Ext.Msg.OKCANCEL,
            scope: this,
            fn : function(btn) {
                if (btn == 'ok') {
                    this.restart(true);
                }
            }
        }, this);
    },
    
    /**
     * Restart the application and force the safe mode
     */
    forceSafeMode: function()
    {
        Ametys.Msg.show({
            title: "{{i18n PLUGINS_ADMIN_PLUGINS_SAVE_CHANGES_LABEL}}",
            message: "{{i18n PLUGINS_ADMIN_RESTART_SAFE_MODE_RESTART_MSG}}",
            icon: Ext.Msg.QUESTION,
            buttons: Ext.Msg.OKCANCEL,
            scope: this,
            fn : function(btn) {
                if (btn == 'ok') {
                    this.restart(false, true);
                }
            }
        }, this);
    },
    
    /**
     * The main restart function
     * @param {Boolean} normalMode True to force to restart in normal (useful if the application was previously in safe mode)
     * @param {Boolean} safeMode True to force to restart in safe mode
     */
    restart: function(normalMode, safeMode)
    {
        Ext.getBody().mask("{{i18n plugin.core-ui:PLUGINS_CORE_UI_LOADMASK_DEFAULT_MESSAGE}}", '');
            
        var result =  Ext.Ajax.request({
            url: Ametys.getPluginDirectPrefix('admin') + '/restart',
            params: {
                normalMode: normalMode === true,
                safeMode: safeMode === true
            },
            callback: function(opts, success, response) {
                Ext.getBody().unmask();
                
                if (success)
                {
                    Ametys.reload();
                }
                else
                {
                    if (this.getLogger().isErrorEnabled())
                    {
                        this.getLogger().error('server-side failure with status code ' + response.status + '.');
                    }
                    
                    Ametys.log.ErrorDialog.display({
                        title: "{{i18n PLUGINS_ADMIN_RESTART_RESTART_FAILED}}", 
                        text: "{{i18n PLUGINS_ADMIN_RESTART_RESTART_FAILED_MSG}}",
                        details: '',
                        category: 'Ametys.plugins.admin.restart'
                    });
                }
            }
        });
    }
});

