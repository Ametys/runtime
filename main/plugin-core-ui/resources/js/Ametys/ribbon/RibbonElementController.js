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
 * This abstract class is the super class for an element of the ribbon (a tab or a control).
 * An element of the ribbon is a controller for a ui element (such as Ametys.ui.fluent.ribbon.controls.Button) but is not itself a button.
 */
Ext.define(
	"Ametys.ribbon.RibbonElementController",
	{
        mixins: { servercaller: 'Ametys.data.ServerCaller' },
        
		/**
		 * @auto
		 * @cfg {String} id (required) The unique identifier for the element. Cannot be null.
		 */
		/**
		 * @property {String} _id See {@link #cfg-id}
		 * @private
		 */

        /**
         * @auto
         * @cfg {String} serverId The unique identifier for the server element associated with this element.
         */
        /**
         * @property {String} _serverId See {@link #cfg-serverId}
         * @private
         */
		
		/**
		 * @auto
		 * @cfg {String} pluginName (required) The name of the plugin that declared the tool. Cannot be null.
		 */
		/**
		 * @property {String} _pluginName See {@link #cfg-pluginName}
		 * @private
		 */

		/**
		 * The constructor should only be called by sub classes
		 * @param {Object} config See the configuration options.
		 */
		constructor: function(config)
		{
			this.initConfig(config);
			
			this._id = config.id;
			this._serverId = config.serverId;
			this._pluginName = config.pluginName;
		},
	
		/**
		 * Get the identifier of the element provided.
		 * @returns {String} The identifier of the element. Cannot be null.
		 */
		getId: function()
		{
			return this._id;
		},
		
		/**
		 * Get the name of the plugin that defined this element.
		 * @returns {String} The name of the plugin. Cannot be null.
		 */
		getPluginName: function()
		{
			return this._pluginName;
		},
        
        /**
         * @inheritDoc
         * @private 
         * The server id for this component 
         * @return {String} #getId
         */
        getServerId: function()
        {
            return this._serverId || this.getId();
        }
        
        /**
         * @method beforeServerCall
         * @private
         * Do nothing
         */

        /**
         * @method afterServerCall
         * @private
         * Do nothing
         */
	}
);
