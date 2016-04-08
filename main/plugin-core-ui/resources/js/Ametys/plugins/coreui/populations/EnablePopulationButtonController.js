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
 * This class controls a ribbon button for enabling/disabling a user population.
 * @private
 */
Ext.define('Ametys.plugins.coreui.populations.EnablePopulationButtonController', {
    extend: 'Ametys.ribbon.element.ui.ButtonController',
    
    /**
     * @property {Boolean} populationEnabled True if the population is enabled, false otherwise.
     */
    
    updateState: function()
    {
        var populationId = this.getMatchingTargets()[0].getParameters().id;
        Ametys.plugins.core.populations.UserPopulationDAO.isEnabled([populationId], this._isEnabledCb, {scope: this, refreshing: true});
    },
    
    _isEnabledCb: function(response)
    {
        var enabled = response.enabled;
        if (enabled != null)
        {
            this.setDescription(enabled ? "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_DISABLE_DESCRIPTION}}" : "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_ENABLE_DESCRIPTION}}");
            this.setIconDecorator(enabled ? this.getInitialConfig("enabled-icon-decorator") : this.getInitialConfig("disabled-icon-decorator"));
            this.toggle(enabled);
            this.populationEnabled = enabled;
        }
        else
        {
            this.setDescription("{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_ENABLE_DESCRIPTION}}");
            this.setIconDecorator(null);
            this.toggle(false);
        }
    }
    
});