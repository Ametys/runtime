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
 * Abstract singleton representing a context for profile assignments.
 * For creating a new context of right assignments, extends this class and implements its template methods.
 */
Ext.define('Ametys.plugins.coreui.profiles.AbstractRightAssignmentContext', {

    inheritableStatics: {
        /**
         * @private
         * @property {Ext.panel.Panel} _contextPanel The context panel
         */
        
        /**
         * @template
         * Gets the panel of this context
         * @return {Ext.Component} the component of this context
         */
        getComponent: function()
        {
            throw new Error("This method is not implemented in " + this.getName());
        },
        
        /**
         * @template
         * Method called when the panel is set as active item, to initialize it (such as a server call, etc.).
         */
        initialize: function(contextPanel)
        {
            throw new Error("This method is not implemented in " + this.getName());
        },
        
        /**
         * Sets the context panel
         * @param {Ext.panel.panel} contextPanel The context panel to set
         */
        setContextPanel: function(contextPanel)
        {
            this._contextPanel = contextPanel;
        },
        
        /**
         * @protected
         * Fires an event to notify the Profile Assignment Tool that the current object context has been changed.
         * This method has to be called at least once.
         * @param {Object} object The object context
         * @param {Object[]} parentObjects The parent object contexts
         * @param {String} hintTextContext A quick description on the current object context to display in the hint text.
         */
        _changeObjectContext: function(object, parentObjects, hintTextContext)
        {
            this._contextPanel.fireEvent('objectcontextchange', object, parentObjects, hintTextContext);
        }
    }
});
