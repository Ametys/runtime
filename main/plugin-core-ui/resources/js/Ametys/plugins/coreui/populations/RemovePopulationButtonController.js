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
 * This class controls a ribbon button enabled if the current selection is a users population which can be removed.
 * @private
 */
Ext.define('Ametys.plugins.coreui.populations.RemovePopulationButtonController', {
    extend: 'Ametys.ribbon.element.ui.ButtonController',
    
    updateState: function()
    {
        this.disable();
        
        var targets = this.getMatchingTargets();
        
        if (targets.length > 0)
        {
            var id = targets[0].getParameters().id;
	        Ametys.plugins.core.populations.UserPopulationDAO.isInUse([id], this._isInUseCb, {scope: this, refreshing: true});
        }

    },
    
    /**
     * @private
     * Enables or disables the controller depending on the server response (is the population in use ?)
     * @param {Boolean} response The server response
     */
    _isInUseCb: function(response)
    {
        if (response === false)
        {
            this.enable();
            this.setAdditionalDescription("");
        }
        else
        {
            this.disable();
            this.setAdditionalDescription( this.getInitialConfig("is-in-use-description") );
        }
    }
});