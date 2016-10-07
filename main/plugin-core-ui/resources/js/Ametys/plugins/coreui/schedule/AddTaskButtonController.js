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
 * This class controls a ribbon button for adding a task.
 * @private
 */
Ext.define('Ametys.plugins.coreui.schedule.AddTaskButtonController', {
    extend: 'Ametys.ribbon.element.ui.ButtonController',
    
    /**
     * @cfg {String} [taskLabel] The label of the task which will be created. Default to the controller label.
     */
    
    /**
     * @cfg {String} [taskDescription] The description of the task which will be created. Default to the controller description.
     */
    
    /**
     * @cfg {String} schedulable The id of the schedulable to create
     */
    
    /**
     * @cfg {Object} [schedulable-parameters] The parameters of the schedulable
     */
    
    /**
     * @cfg {String} [fire-process="NOW"] The way to fire the runnable. If equals to "CRON", then {@link #cfg-cron} must be non-null
     */
    
    /**
     * @cfg {String} [cron] The cron expression, only needed if {@link #cfg-fire-process} equals to "CRON"
     */
    
    /**
     * @cfg {String} [log-category] The logging category for the ServerLog tool
     */
    
    /**
     * @cfg {String} [confirm-title] The title of the confirm dialog box. Must be non-null if {@link #cfg-confirm-msg} is non-null. If one is null, there will be no confirm dialog box.
     */
    
    /**
     * @cfg {String} [confirm-msg] The message content of the confirm dialog box. Must be non-null if {@link #cfg-confirm-title} is non-null. If one is null, there will be no confirm dialog box.
     */
    
    statics: {
        /**
         * Add the configured task
         * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
         */
        act: function(controller)
        {
            var confirmTitle = controller['confirm-title']; 
            var confirmMsg = controller['confirm-msg'];
            function callback(answer)
            {
                if (answer == 'yes')
                {
                    Ametys.plugins.core.schedule.Scheduler.add(
                        [controller.taskLabel || controller.label, controller.taskDescription || controller.description, controller.fireProcess || "NOW", controller.cron, controller.schedulable, controller['schedulable-parameters'] || {}], 
                        this._openTools, 
                        {
                            scope: this, 
                            arguments: {category: controller['log-category']}
                        }
                    );
                }
            };
            
            if (confirmTitle != null && confirmMsg != null)
            {
                Ametys.Msg.confirm(
                    confirmTitle,
                    confirmMsg,
                    callback,
                    this
                );
            }
            else
            {
                callback('yes');
            }
            
        },
        
        /**
         * @private
         * @param {Object} response The server response
         * @param {Object} arguments The callback arguments
         * @param {String} arguments.category The logging category
         */
        _openTools: function(response, arguments)
        {
            function select(store, records, tool, recordId)
            {
                var record = store.getById(recordId);
                if (record != null)
                {
                    tool.getGrid().getSelectionModel().select([record]);
                }
            }
            
            Ametys.tool.ToolsManager.openTool('uitool-server-logs', {category: arguments.category}, "cr");
            var scheduledTasksTool = Ametys.tool.ToolsManager.openTool('uitool-scheduled-tasks', {}, "cl");
            if (scheduledTasksTool)
            {
                scheduledTasksTool.getGrid().getStore().on("load", Ext.bind(select, this, [scheduledTasksTool, response.id], 2), this, {single: true});
            }
        }
    }
});
