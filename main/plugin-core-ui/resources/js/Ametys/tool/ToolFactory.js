/*
 *  Copyright 2013 Anyware Services
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
 * A tool factory is mainly in charge for creating a tool throught the #openTool method.
 * This class is an abstract class and cannot be used. Use one of its subclasses or create your own one.
 * 
 * A factory is defined by a role and registrer at the Ametys.tool.ToolsManager, which is in charge of handling tools.
 * 
 * Here is an example to declare a factory in a plugin.xml
 * 
 *        <feature name="userinterface.messages-tracker">
 *           <extensions>
 *               <extension id="org.ametys.cms.userinterface.MessageTrackerFactory"
 *                         point="org.ametys.runtime.ui.UIToolsFactoriesManager"
 *                         class="org.ametys.runtime.ui.StaticClientSideElement">
 *                  <class name="Ametys.tool.factory.UniqueToolFactory">
 *                      <role>uitool-messagestracker</role>
 *                      <toolClass>Ametys.plugins.cms.system.messagetracker.MessageTrackerTool</toolClass>
 *                  
 *                      <title i18n="true">PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_LABEL</title>
 *                      <description i18n="true">PLUGINS_CORE_UI_TOOLS_MESSAGES_TRACKER_DESCRIPTION</description>
 *                      <icon-small file="true">img/messages/messages_16.png</icon-small>
 *                      <icon-medium file="true">img/messages/messages_32.png</icon-medium>
 *                      <icon-large file="true">img/messages/messages_48.png</icon-large>
 *                  
 *                      <default-location>b</default-location>
 *                  </class>                       
 *                  <scripts>
 *                      <file>js/Ametys/plugins/cms/system/messagetracker/MessageTrackerTool.js</file>
 *                  </scripts>
 *              </extension>
 *           </extensions>
 *       </feature>
 */
Ext.define("Ametys.tool.ToolFactory", 
    {
        /**
         * @auto
         * @cfg {String} id (required) The unique identifier for the tool factory. Cannot be null.
         */
        /**
         * @property {String} _id See {@link #cfg-id}
         * @private
         */
        
        /**
         * @auto
         * @cfg {String} pluginName (required) The plugin name where the factory was declared.
         */
        /**
         * @cfg {String} role (required) The role of the factory. Has to be unique.
         */
        /**
         * @cfg {String} autoRefresh=true When "true", the tools made by this factory will automatically refresh after an Ametys.tool.Tool#outOfDate. "false" will ask the user to refresh the tool.
         */
        
        /**
         * @property {Object} _initConfig See {@link #getInitialConfig}
         * @private
         */
        /**
         * @property {String} _pluginName See {@link #cfg-pluginName}
         * @private
         */
        /**
         * @property {String} _role See {@link #cfg-role}
         * @private
         */
        /**
         * @property {Boolean} _autoRefresh See {@link #cfg-autoRefresh}
         * @private
         */
        
        /**
         * Creates a new tool factory
         * @param {Object} config See configuration parameters.
         */
        constructor: function(config)
        {
            this._id = config.id;
            this._pluginName = config.pluginName;
            this._role = config.role;
            this._autoRefresh = config.autoRefresh != "false";
            
            this._initConfig = config;
        },
        
        /**
         * Get the name of the plugin where the factory instance was declared
         * @returns {String} The name of the plugin configured
         */
        getPluginName: function()
        {
            return this._pluginName;
        },
        
        /**
         * Get the parameters set when the factory was declared in the plugin
         * Do not change this object since it is a reference to this internal object.
         * @returns {Object} The configuration parameters
         */
        getInitialConfig: function()
        {
            return this._initConfig;
        },
        
        /**
         * Get the identifier of the factory. Do not confuse with #getRole
         * @returns {String} The unique plugin identifier for this factory instance
         */
        getId: function()
        {
            return this._id;
        },
        
        /**
         * Get the role of the factory. Do not confuse with #getId
         * @returns {String} The unique role for this factory instance
         */
        getRole: function()
        {
            return this._role;
        },
        
        /**
         * Should tools made with this factory automatically refresh?
         * @return {Boolean} true to enable auto refresh after a Ametys.tool.Tool#outOfDate
         */
        isAutoRefreshEnabled: function()
        {
            return this._autoRefresh;
        },

        /**
         * This method is in charge for returning a tool based upon potentially arguments.
         * See your factory implementation to see arguments needed.
         * 
         * This method can creates or returns an existing tool.
         * The factory will not call Ametys.tool.Tool#setParams or Ametys.tool.Tool#activate or any other method on the tool, this is done by the Ametys.tool.ToolsManager
         * @param {Object} toolParams A configuration object to parametrize the tool
         * @returns {Ametys.tool.Tool} A tool. Cannot be null.
         * @template 
         */
        openTool: function(toolParams)
        {
            throw new Error("This method is not implemented in " + this.self.getName());
        }
    }
);
