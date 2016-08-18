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
 * Contributor context for profile assignments.
 */
Ext.define('Ametys.plugins.coreui.profiles.ContributorRightAssignmentContext', {
    extend: 'Ametys.plugins.coreui.profiles.AbstractRightAssignmentContext',
    
    statics: {
        getCurrentObjectContext: function()
        {
            return "/contributor";
        },
        
        getComponent: function()
        {
            return Ext.create('Ext.Component', {
                html: '{{i18n plugin.core-impl:PLUGINS_CORE_RIGHT_ASSIGNMENT_CONTEXT_CONTRIBUTOR_MESSAGE}}',
                style: {
                    paddingLeft: '8px'
                }
            });
        },
        
        initialize: function()
        {
            this._changeObjectContext("/contributor", [], "{{i18n plugin.core-impl:PLUGINS_CORE_RIGHT_ASSIGNMENT_CONTEXT_CONTRIBUTOR_HINT}}");
        }
    }
});
