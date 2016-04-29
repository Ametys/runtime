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
 * This class controls a ribbon button allowing to change the draw mode of the {@link Ametys.plugins.admin.jvmstatus.MonitoringTool}.
 * @private
 */
Ext.define('Ametys.plugins.admin.jvmstatus.SwitchDrawModeController', {
    extend: 'Ametys.ribbon.element.ui.ButtonController',
    
    constructor: function(config)
    {
        this.callParent(arguments);
        
        Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, this._onModified, this);
        Ametys.message.MessageBus.on(Ametys.message.Message.TOOL_CLOSED, this._onToolClosed, this);
    },
    
    /**
     * Listener when the monitoring tool has been modified
     * @param {Ametys.message.Message} message The modified message.
     * @protected
     */
    _onModified: function(message)
    {
        var targets = message.getTargets(Ametys.message.MessageTarget.TOOL);
        if (targets.length > 0 && targets[0].getParameters().id == "uitool-admin-monitoring")
        {
            var monitoringTool = targets[0].getParameters().tool;
            var drawMode = monitoringTool.getDrawMode();
            if (drawMode)
            {
                this.setDescription(this.getInitialConfig()['enabled-description']);
            }
            else
            {
                this.setDescription(this.getInitialConfig()['disabled-description']);
            }
        }
    },
    
    /**
     * Listener when the monitoring tool has been closed
     * @param {Ametys.message.Message} message The modified message.
     * @protected
     */
    _onToolClosed: function(message)
    {
        var targets = message.getTargets(Ametys.message.MessageTarget.TOOL);
        if (targets.length > 0 && targets[0].getParameters().id == "uitool-admin-monitoring")
        {
            this.toggle(false);
            this.setDescription(this.getInitialConfig()['disabled-description']);
        }
    }
});
