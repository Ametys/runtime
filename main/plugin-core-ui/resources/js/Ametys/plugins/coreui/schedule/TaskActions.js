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
 * This class is a singleton to handle actions on scheduled tasks.
 */
Ext.define('Ametys.plugins.coreui.schedule.TaskActions', {
    singleton: true, 
    
    /**
     * Adds a new task
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    addTask: function(controller)
    {
        Ametys.plugins.coreui.schedule.EditTaskHelper.open(null, 'add');
    },
    
    /**
     * Edits a task
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    editTask: function(controller)
    {
        var taskId = controller.getMatchingTargets()[0].getParameters().id;
        Ametys.plugins.core.schedule.Scheduler.isModifiable([taskId], this._isModifiableCb, {arguments: [taskId], scope: this});
    },
    
    /**
     * @private
     * Callback function after retrieving the modifiable status
     * @param {Boolean} valid true if the task is modifiable
     * @param {Object} arguments The callback arguments
     */
    _isModifiableCb: function(modifiable, arguments)
    {
        if (!modifiable)
        {
            Ext.Msg.show({
                title: "{{i18n PLUGINS_CORE_UI_TASKS_NO_MODIFIABLE_TITLE}}",
                message: "{{i18n PLUGINS_CORE_UI_TASKS_NO_MODIFIABLE_MSG}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
        }
        else
        {
            Ametys.plugins.core.schedule.Scheduler.getParameterValues([arguments[0]], this._editTask, {scope: this});
        }
    },
    
    /**
     * @private
     * Opens the dialog box for editing a task after retrieving the values to fill in the fields
     * @param {Object} response The server response
     * @param {Array} arguments The arguments of the callback function
     * @param {Array} parameters The parameters of the server method
     */
    _editTask: function(response, arguments, parameters)
    {
        var id = parameters[0];
        Ametys.plugins.coreui.schedule.EditTaskHelper.open(response, 'edit', id);
    },
    
    /**
     * Removes a task
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    removeTask: function(controller)
    {
        var messageTargets = controller.getMatchingTargets();
        if (messageTargets.length > 0)
        {
            var id = messageTargets[0].getParameters().id;
            Ext.Msg.confirm("{{i18n PLUGINS_CORE_UI_TASKS_REMOVE_CONFIRM_TITLE}}",
                    "{{i18n PLUGINS_CORE_UI_TASKS_REMOVE_CONFIRM_MSG}}",
                    Ext.bind(this._doRemove, this, [id], 1),
                    this
            );
        }
    },
    
    /**
     * Enables a task
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    enableTask: function(controller)
    {
        var id = controller.getMatchingTargets()[0].getParameters().id;
        Ametys.plugins.core.schedule.Scheduler.enable([id, /* reverse the current state */ !controller.isPressed()]);
    },
    
    /**
     * @private
     * Calls the remove server method
     * @param {String} btn The pressed button. Can only be 'yes'/'no'
     * @param {String} id The id of the task to remove
     */
    _doRemove: function(btn, id)
    {
        if (btn == 'yes')
        {
            Ametys.plugins.core.schedule.Scheduler.remove([id]);
        }
    }
});
