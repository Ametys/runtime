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
 * Actions for adding/deleting records in the grid of the {@link Ametys.plugins.coreui.profiles.ProfileAssignmentsTool}
 */
Ext.define('Ametys.plugins.coreui.profiles.ProfileAssignmentsActions', {
    singleton: true,
    
    /**
     * Add users to the assignment grid of the assignment tool
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    addUsers: function(controller)
    {
        var tool = this._getTool();
        Ametys.helper.SelectUser.act({
            callback: Ext.bind(tool.addUsers, tool)
        });
    },
    
    /**
     * Add groups to the assignment grid of the assignment tool
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    addGroups: function(controller)
    {
        var tool = this._getTool();
        Ametys.helper.SelectGroup.act({
            callback: Ext.bind(tool.addGroups, tool)
        });
    },
    
    /**
     * Removes the current selected records of the assignment grid of the assignment tool
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    removeAssignments: function(controller)
    {
        var matchingTargets = controller.getMatchingTargets();
        if (matchingTargets.length > 0)
        {
            var tool = this._getTool();
            var assignments = Ext.Array.map(matchingTargets, function(target) {
                return {
                    id: target.getParameters().id,
                    context: target.getParameters().context
                };
            }, this);
            tool.removeAssignments(assignments);
        }
    },
    
    /**
     * Saves the changes made in the assignment tool
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    save: function(controller)
    {
        var tool = this._getTool();
        tool.saveChanges();
    },
    
    /**
     * Cancel the changes made in the assignment tool
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    cancel: function(controller)
    {
        var tool = this._getTool();
        tool.cancelChanges();
    },
    
    /**
     * @private
     * Gets the assignment tool
     * @return {Ametys.tool.Tool} the assignment tool
     */
    _getTool: function()
    {
        return Ametys.tool.ToolsManager.getFocusedTool();
    }
});
