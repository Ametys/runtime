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
 * This class controls a ribbon button for enabling/disabling a task.
 * @private
 */
Ext.define('Ametys.plugins.coreui.schedule.EnableTaskButtonController', {
    extend: 'Ametys.ribbon.element.ui.ButtonController',
    
    constructor: function(config)
    {
        this.callParent(arguments);
        Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, this._onModified, this);
    },
    
    /**
     * Listener when the content has been modified
     * Will update the state of the buttons effectively upon the current selection.
     * @param {Ametys.message.Message} message The modified message.
     * @protected
     */
    _onModified: function (message)
    {
        this.refresh();
    },
    
    updateState: function()
    {
        this.disable();
        
        var taskId = this.getMatchingTargets()[0].getParameters().id;
        Ametys.plugins.core.schedule.Scheduler.isEnabled([taskId], this._isEnabledCb, {scope: this, refreshing: true});
    },
    
    /**
     * @private
     * Callback function invoked after getting the task state
     * @param {Object} response The server response
     * @param {String} response.error if an error occurred
     * @param {Boolean} response.enabled if the current task target is enabled
     */
    _isEnabledCb: function(response)
    {
        if (response.error)
        {
            this.setDescription("{{i18n PLUGINS_CORE_UI_TASKS_ENABLE_DESCRIPTION_ERROR}}");
            this.setIconDecorator(null);
            this.disable();
            this.toggle(false);
        }
        else
        {
            this.setDescription(response.enabled ? "{{i18n PLUGINS_CORE_UI_TASKS_DISABLE_DESCRIPTION}}" : "{{i18n PLUGINS_CORE_UI_TASKS_ENABLE_DESCRIPTION}}");
            this.setIconDecorator(response.enabled ? this.getInitialConfig("enabled-icon-decorator") : this.getInitialConfig("disabled-icon-decorator"));
            this.toggle(response.enabled);
            this.enable();
        }
    }
});
