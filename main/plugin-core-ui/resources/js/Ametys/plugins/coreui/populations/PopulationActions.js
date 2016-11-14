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
 * This class is a singleton to handle actions on user populations.
 */
Ext.define('Ametys.plugins.coreui.populations.PopulationActions', {
    singleton: true, 
    
    /**
     * Adds a new user population
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    addPopulation: function(controller)
    {
        Ametys.plugins.coreui.populations.EditPopulationHelper.open(null, 'add');
    },
    
    /**
     * Edits a user population
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    editPopulation: function(controller)
    {
        var id = controller.getMatchingTargets()[0].getParameters().id;
        Ametys.plugins.core.populations.UserPopulationDAO.getPopulationParameterValues([id], this._editPopulation, {scope: this});
    },
    
    /**
     * @private
     * Opens the dialog box for editing a population after retrieving the values to fill in the fields
     * @param {Object} response The server response
     * @param {Array} arguments The arguments of the callback function
     * @param {Array} parameters The parameters of the server method
     */
    _editPopulation: function(response, arguments, parameters)
    {
        var id = parameters[0];
        Ametys.plugins.coreui.populations.EditPopulationHelper.open(response, 'edit', id);
    },
    
    /**
     * Removes a user population
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    removePopulation: function(controller)
    {
        var messageTargets = controller.getMatchingTargets();
        if (messageTargets.length > 0)
        {
            var id = messageTargets[0].getParameters().id;
            Ametys.Msg.confirm("{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_REMOVE_CONFIRM_TITLE}}",
	                "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_REMOVE_CONFIRM_MSG}}",
	                Ext.bind(this._doRemove, this, [id], 1),
	                this
	        );
        }
    },
    
    /**
     * Enables a user population
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    enablePopulation: function(controller)
    {
        var populationId = controller.getMatchingTargets()[0].getParameters().id;
        if (!controller.isPressed())
        {
            Ametys.plugins.core.populations.UserPopulationDAO.isValid([populationId], this._isValidCb, {arguments: {populationId: populationId, controller: controller}});
        }
        else
        {
            Ametys.plugins.core.populations.UserPopulationDAO.enable([populationId, /* reverse the current state */ !controller.isPressed()]);
        }
    },
    
    /**
     * @private
     * Callback function after retrieving the valid status
     * @param {Boolean} valid true if the population is valid
     * @param {Object} args The callback arguments
     */
    _isValidCb: function (valid, args)
    {
        if (!valid)
        {
            Ametys.Msg.show({
			    title: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_MISCONFIGURED_TITLE}}",
			    message: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_CANNOT_ENABLE_MSG}}",
			    buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
			});
        }
        else
        {
            Ametys.plugins.core.populations.UserPopulationDAO.enable([args.populationId, /* reverse the current state */ !args.controller.isPressed()]);
        }
    },
    
    /**
     * @private
     * Calls the remove server method
     * @param {String} btn The pressed button. Can only be 'yes'/'no'
     * @param {String} id The id of the population to remove
     */
    _doRemove: function(btn, id)
    {
        if (btn == 'yes')
        {
	        Ametys.plugins.core.populations.UserPopulationDAO.remove([id]);
        }
    },
    
    /**
     * Opens a dialog box to select the user populations to link to a given context
     * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function
     */
    link: function(controller)
    {
        var context = controller.getInitialConfig('context') || "";
        var hintText = controller.getInitialConfig('hintText') || "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_CHOOSE_POPULATIONS_HINT}}";
        
        this.linkToContext(context, hintText);
    },
    
    /**
     * Opens a dialog box to select the user populations to link to a context
     * @param {String} context The context
     * @param {String} hintText The hint text for the dialog box
     * @param {Function} [callback] The callback function to call after the populations are effectively linked to the context
     */
    linkToContext: function(context, hintText, callback)
    {
        Ametys.plugins.core.populations.UserPopulationDAO.getUserPopulationsOnContext([context], this._openChooseDialog, {scope: this, arguments: [hintText, callback]});
    },
    
    /**
     * @private
     * Function to open the choose dialog box
     * @param {String[]} ids The already linked user populations
     * @param {Array} arguments The arguments of the callback function
     * @param {Array} parameters The parameters of the server method
     */
    _openChooseDialog: function(ids, arguments, parameters)
    {
        var context = parameters[0];
        var hintText = arguments[0];
        var callback = arguments[1];
        
        Ametys.plugins.coreui.populations.ChooseUserPopulationsHelper.open({
            title: "{{i18n PLUGINS_CORE_UI_USER_POPULATIONS_CHOOSE_POPULATIONS_TITLE}}",
            hintText: hintText,
            selectedIds: ids,
            allowCreation: true,
            okAction: Ext.bind(this._doLinkPopulations, this, [context, callback], 1)
        });
    },
    
    /**
     * @private
     * Links some user populations to a context
     * @param {String[]} ids The ids of the selected user populations
     * @param {String} context The context
     * @param {Function} [callback] The callback function
     */
    _doLinkPopulations: function(ids, context, callback)
    {
        Ametys.plugins.core.populations.UserPopulationDAO.link([context, ids], callback || Ext.emptyFn, {scope: this});
    }
    
});