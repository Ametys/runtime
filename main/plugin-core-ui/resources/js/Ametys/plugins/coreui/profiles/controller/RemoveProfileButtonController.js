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
 * This class controls a ribbon button enabled if the current selection is a profile which can be removed (if it is not the reader profile).
 * @private
 */
Ext.define('Ametys.plugins.coreui.profile.controller.RemoveProfileButtonController', {
    extend: 'Ametys.ribbon.element.ui.ButtonController',
    
    /**
     * @readonly
     * @property {String} READER_PROFILE_ID The id of the special profile (READER profile)
     */
    READER_PROFILE_ID: 'READER',
    
    updateState: function()
    {
        this.disable();
        
        var targets = this.getMatchingTargets();
        
        if (targets.length > 0)
        {
            var id = targets[0].getParameters().id;
            if (id == this.READER_PROFILE_ID)
            {
                this.disable();
                this.setAdditionalDescription( this.getInitialConfig("no-removable-description") );
            }
            else
            {
                this.enable();
                this.setAdditionalDescription("");
            }
        }
    }
});