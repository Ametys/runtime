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
 * This class controls a button to delete a user.
 * @private
 */
Ext.define('Ametys.plugins.coreui.users.controller.DeleteUserButtonController', {
    extend: 'Ametys.ribbon.element.ui.ButtonController',
    
    updateState: function()
    {
        this.disable();
        
        var targets = this.getMatchingTargets();
        
        if (targets.length > 0)
        {
            this._stopComputing = false;
            Ext.Array.each(targets, function(target, index) {
                if (this._stopComputing)
                {
                    return false;
                }
                var login = target.getParameters().id;
                var populationId = target.getParameters().population;
                var last = (index == targets.length - 1);
                Ametys.plugins.core.users.UsersDAO.isRemovable([login, populationId], this._isRemovableCb, {scope: this, refreshing: true, arguments: [last]});
            }, this);
        }

    },
    
    /**
     * @private
     * Enables or disables the controller depending on the server response (is the user removable ?)
     * @param {Object} response The server response
     * @param {Array} arguments The callback arguments
     */
    _isRemovableCb: function(response, arguments)
    {
        var isRemovable = response['isRemovable'];
        var last = arguments[0];
        if (!this._stopComputing && isRemovable === true && last)
        {
            this.enable();
            this.setAdditionalDescription("");
        }
        else if (isRemovable === false)
        {
            this._stopComputing = true;
            this.disable();
            var additionalDescription = response['additionalDescription'];
            this.setAdditionalDescription(additionalDescription);
        }
    }
    
});