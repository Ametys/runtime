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
    /**
     * @cfg {String} label The readable label
     */
    
    /**
     * @private
     * @property {Ext.panel.Panel} _contextPanel The context panel
     */
    /**
     * @private
     * @property {String} _serverId The id of this component on the server side
     */
    
    /**
     * Creates the instance
     * @param {Object} config The configuration
     */
    constructor: function(config)
    {
        this._config = config;
    },
    
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
     * Get the label
     * @return {String} the label
     */
    getLabel: function()
    {
        return this._config.label;
    },
    
    /**
     * @protected
     * This methods should return the server-side role of the component to call.
     * @return {String} The component role
     */
    getServerId: function()
    {
        return this._serverId;
    },
    
    /**
     * @protected
     * This methods set the server-side role of the component to call.
     * @param {String} The component role
     */
    setServerId: function(serverId)
    {
        this._serverId = serverId;
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
     * @param {String} hintTextContext A quick description on the current object context to display in the hint text.
     */
    _changeObjectContext: function(object, hintTextContext)
    {
        this._contextPanel.fireEvent('objectcontextchange', object, hintTextContext);
    }
});
