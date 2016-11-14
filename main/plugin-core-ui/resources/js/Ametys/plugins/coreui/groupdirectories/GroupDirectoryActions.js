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
 * This class is a singleton to handle actions on group directories.
 * @private
 */
Ext.define('Ametys.plugins.coreui.groupdirectories.GroupDirectoryActions', {
    singleton: true, 
    
    /**
     * Adds a new group directory
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    addGroupDirectory: function(controller)
    {
        Ametys.plugins.coreui.groupdirectories.EditGroupDirectoryHelper.open(null, 'add');
    },
    
    /**
     * Edits a group directory
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    editGroupDirectory: function(controller)
    {
        var id = controller.getMatchingTargets()[0].getParameters().id;
        Ametys.plugins.core.groupdirectories.GroupDirectoryDAO.getGroupDirectoryParameterValues([id], this._editGroupDirectory, {scope: this});
    },
    
    /**
     * @private
     * Opens the dialog box for editing a group directory after retrieving the values to fill in the fields
     * @param {Object} response The server response
     * @param {Array} arguments The arguments of the callback function
     * @param {Array} parameters The parameters of the server method
     */
    _editGroupDirectory: function(response, arguments, parameters)
    {
        var id = parameters[0];
        Ametys.plugins.coreui.groupdirectories.EditGroupDirectoryHelper.open(response, 'edit', id);
    },
    
    /**
     * Removes a group directory
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    removeGroupDirectory: function(controller)
    {
        var messageTargets = controller.getMatchingTargets();
        if (messageTargets.length > 0)
        {
            var id = messageTargets[0].getParameters().id;
            Ametys.Msg.confirm("{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_REMOVE_CONFIRM_TITLE}}",
                    "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_REMOVE_CONFIRM_MSG}}",
                    Ext.bind(this._doRemove, this, [id], 1),
                    this
            );
        }
    },
    
    /**
     * @private
     * Calls the remove server method
     * @param {String} btn The pressed button. Can only be 'yes'/'no'
     * @param {String} id The id of the group directory to remove
     */
    _doRemove: function(btn, id)
    {
        if (btn == 'yes')
        {
            Ametys.plugins.core.groupdirectories.GroupDirectoryDAO.remove([id]);
        }
    },
    
    /**
     * Opens a dialog box to select the group directories to link to a given context
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    link: function(controller)
    {
        var context = controller.getInitialConfig('context') || "";
        var hintText = controller.getInitialConfig('hintText') || "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_CHOOSE_GROUP_DIRECTORIES_HINT}}";
        
        this.linkToContext(context, hintText);
    },
    
    /**
     * Opens a dialog box to select the group directories to link to a context
     * @param {String} context The context
     * @param {String} hintText The hint text for the dialog box
     */
    linkToContext: function(context, hintText)
    {
        Ametys.plugins.core.groupdirectories.GroupDirectoryDAO.getGroupDirectoriesOnContext([context], this._openChooseDialog, {scope: this, arguments: [hintText]});
    },
    
    /**
     * @private
     * Function to open the choose dialog box
     * @param {String[]} ids The already linked group directories
     * @param {Array} arguments The arguments of the callback function
     * @param {Array} parameters The parameters of the server method
     */
    _openChooseDialog: function(ids, arguments, parameters)
    {
        var context = parameters[0];
        var hintText = arguments[0];
        
        Ametys.plugins.coreui.groupdirectories.ChooseGroupDirectoriesHelper.open({
            title: "{{i18n PLUGINS_CORE_UI_GROUP_DIRECTORIES_CHOOSE_GROUP_DIRECTORIES_TITLE}}",
            hintText: hintText,
            selectedIds: ids,
            allowCreation: true,
            okAction: Ext.bind(this._doLink, this, [context], 1)
        });
    },
    
    /**
     * @private
     * Links some group directories to a context
     * @param {String[]} ids The ids of the selected group directories
     * @param {String} context The context
     */
    _doLink: function(ids, context)
    {
        Ametys.plugins.core.groupdirectories.GroupDirectoryDAO.link([context, ids]);
    }
    
});