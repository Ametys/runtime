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
 * This impl handles the RightAssignmentContext instances
 * @private
 */
Ext.define("Ametys.plugins.coreui.profiles.ProfileAssignmentsToolFactory", 
    {
        extend: "Ametys.tool.factory.UniqueToolFactory",
        
        /** 
         * @protected
         * @property {Ametys.plugins.coreui.profiles.AbstractRightAssignmentContext} _rightAssignmentContexts instances of availables RightAssignmentContext
         */
        
        constructor: function(config)
        {
            this.callParent(arguments);
            
            // Create the instances of RightAssignmentContext
            this._rightAssignmentContexts = {};
            Ext.Object.each(config.classes, function(id, classConfig) {
                this._rightAssignmentContexts[id] = Ametys.createObjectByName(classConfig.className, null, classConfig.parameters);
                this._rightAssignmentContexts[id].setServerId(classConfig.serverId);
            }, this);
        }
    }
);
            