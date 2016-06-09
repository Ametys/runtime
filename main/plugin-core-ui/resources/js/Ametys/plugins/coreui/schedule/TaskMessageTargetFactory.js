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
 * This factory creates Ametys.message.MessageTarget for a task.
 * 
 * See #createTargets to know more.
 * @private
 */
Ext.define("Ametys.plugins.coreui.schedule.TaskMessageTargetFactory", {
    extend: "Ametys.message.factory.DefaultMessageTargetFactory",
    
    /**
     * Creates the targets for a message
     * @param {Object} parameters The parameters needed by the factory to create the message. Can not be null. Handled elements are
     * @param {String[]} parameters.ids The tasks identifiers. Must be present if tasks/id is empty
     * @param {String} parameters.id The task identifier (if alone). Must be present if tasks/ids is empty
     * @param {Ametys.plugins.coreui.schedule.Task[]} parameters.tasks The tasks themselves. Must be present if ids/id is empty
     * @param {Function} callback The callback function called when the targets are created. Parameters are
     * @param {Ametys.message.MessageTarget[]} callback.targets The targets created. Cannot be null.
     * @param {String} targetId The id of target specified. Useful for gerneric MessageTargetFactories.
     */
    createTargets: function(parameters, callback, targetId)
    {
        if (parameters.tasks)
        {
            this._createTargets(parameters.tasks, {callback: callback})
        }
        else
        {
            var ids = parameters.ids || (parameters.id ? [parameters.id] : []);
            Ametys.plugins.core.schedule.Scheduler.getTasksInformation([ids], this._createTargets, {scope: this, arguments: {callback: callback}});
        }
    },
    
    /**
     * @private
     * Create the targets for a task
     * @param {Ametys.plugins.coreui.schedule.Task[]} tasks The tasks
     * @param {Object} arguments The callback arguments
     * @param {Function} callback The callback function called when the targets are created. Parameters are
     * @param {Ametys.message.MessageTarget[]} callback.targets The targets created. Cannot be null.
     */
    _createTargets: function(tasks, arguments)
    {
        var callback = arguments.callback;
        var targets = [];
        Ext.Array.forEach(tasks, function(task) {
            targets.push(Ext.create('Ametys.message.MessageTarget', {
                id: this.getId(),
                parameters: Ext.merge(task.getProperties({}), {task: task})
            }));
        }, this);
        
        callback(targets);
    }
    
});

Ext.define("Ametys.message.TaskMessageTarget",{
    override: "Ametys.message.MessageTarget",
    statics: 
    {
        /**
         * @member Ametys.message.MessageTarget
         * @readonly
         * @property {String} TASK The target of the message is a task
         * @property {String} TASK.id The id of the task
         * @property {Boolean} [TASK.modifiable] true if the task is modifiable
         * @property {Boolean} [TASK.removable] true if the task is removable
         * @property {Boolean} [TASK.deactivatable] true if the task is deactivatable
         */
        TASK: "task"
    }
});