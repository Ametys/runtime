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
 * This class controls a button to log out the current logged user.
 * @private
 */
Ext.define('Ametys.plugins.coreui.user.LogoutButtonController', {
    extend: 'Ametys.ribbon.element.ui.ButtonController',
    
    statics: {
        
        /**
         * Logs out the current logged user.
         * @param {Ametys.ribbon.element.ui.ButtonController} controller The button controller
         */
        logout: function(controller)
        {
            controller.serverCall("logout", [], this._logoutCb);
        },
        
        _logoutCb: function(response)
        {
            if (response == true)
            {
                // Reload the application
                Ametys.reload();
            }
            else
            {
                Ametys.log.ErrorDialog.display({
					title: "{{i18n PLUGINS_CORE_UI_LOGOUT_ERROR_TITLE}}",
					text: "{{i18n PLUGINS_CORE_UI_LOGOUT_ERROR_TEXT}}",
                    category: this.self.getName()
                });
            }
        }
    }
    
});