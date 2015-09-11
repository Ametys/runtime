/*
 *  Copyright 2014 Anyware Services
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
 * This class is a superclass to implements a relation: move, copy or reference operation on any object that do affect server side.
 * 
 * To implement a new behavior (such as "drag explorer resources on a content") extends this class by implementing all templates method and to register it on the Ametys.relation.RelationManager.
 * 
 * Every Ametys.relation.RelationHandler is called by the Ametys.relation.RelationManager to determine which handle best fits the relation operation using #supportedRelations.
 * Only the winner Ametys.relation.RelationHandler will effectively be used through #link.
 * 
 * When implementing, you can call #serverCall to make simple server calls.
 */
Ext.define("Ametys.relation.RelationHandler", 
	{
        mixins: { servercaller: 'Ametys.data.ServerCaller' },
        
		/**
		 * @auto
		 * @cfg {String} id (required) The unique identifier for the tool. Cannot be null.
		 */
		/**
		 * @property {String} _id See {@link #cfg-id}
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
		 * @cfg {String} label The value for #getLabel
		 */
		/**
		 * @private
		 * @property {String} _label See #cfg-label
		 */
		/**
		 * @cfg {String} description The value for #getDescription
		 */
		/**
		 * @private
		 * @property {String} _description See #cfg-description
		 */
		/**
		 * @cfg {String} icon-small The value for #getSmallIcon
		 */
		/**
		 * @private
		 * @property {String} _iconSmall See #cfg-icon-medium
		 */
		/**
		 * @cfg {String} icon-medium The value for #getMediumIcon
		 */
		/**
		 * @private
		 * @property {String} _iconMedium See #cfg-icon-medium
		 */
		/**
		 * @cfg {String} icon-large The value for #getLargeIcon
		 */
		/**
		 * @private
		 * @property {String} _iconLarge See #cfg-icon-large
		 */
		
		/**
		 * Creates the relation handler instance
		 * @param {Object} config The configuration
		 */
		constructor: function(config)
		{
			this._id = config.id;
			this._pluginName = config.pluginName;
			this._label = config.label;
			this._description = config.description;
			this._iconSmall = Ametys.CONTEXT_PATH + config["icon-small"];
			this._iconMedium = Ametys.CONTEXT_PATH + config["icon-medium"];
			this._iconLarge = Ametys.CONTEXT_PATH + config["icon-large"];
		},
		
		/**
		 * Get the identifier of the relation.
		 * @returns {String} The identifier
		 */
		getId: function()
		{
			return this._id;
		},
		
		/**
		 * Get the name of the plugin that defined the relation.
		 * @returns {String} The name of the plugin. Cannot be null.
		 */
		getPluginName: function()
		{
			return this._pluginName;
		},
		
		/** 
		 * Get the handler label. Used when the popup menu is display to the user to choose which operation should be done
		 * @return {String}  A readable label.
		 */
		getLabel: function()
		{
			return this._label || '';
		},
		
		/** 
		 * Get the handler description. Used when the popup menu is display to the user to choose which operation should be done
		 * @return {String}  A readable description.
		 */
		getDescription: function()
		{
			return this._description || '';
		},	
		
		/** 
		 * Get the handler small icon (16x16 pixels). Used when the popup menu is display to the user to choose which operation should be done
		 * @return {String}  The icon url (relative to the application e.g. /plugins/PLUGIN/resources/img/image_16.png). Can be null.
		 */
		getSmallIcon: function()
		{
			return this._iconSmall;
		},
		
		/** 
		 * Get the handler medium icon (32x32 pixels). Used when the popup menu is display to the user to choose which operation should be done
		 * @return {String}  The icon url (relative to the application e.g. /plugins/PLUGIN/resources/img/image_16.png). Can be null.
		 */
		getMediumIcon: function()
		{
			return this._iconMedium || this.getSmallIcon();
		},	
		
		/** 
		 * Get the handler large icon (48x48 pixels). Used when the popup menu is display to the user to choose which operation should be done
		 * @return {String}  The icon url (relative to the application e.g. /plugins/PLUGIN/resources/img/image_16.png). Can be null.
		 */
		getLargeIcon: function()
		{
			return this._iconMarge || this.getMediumIcon();
		},	
		
		/**
		 * This method return the list of supported operations between the source and the target points of a relation.
		 * The implementation should only cares about targets and does not have to check upon source and target relations: a later filter is done by the Ametys.relation.RelationManager.
		 * 
		 * When used by Ametys.relation.RelationManager#testLink, you have source and target as Object. You have to return "potentially" supported relations.
		 * When used by Ametys.relation.RelationManager#link, you have source and target as Ametys.relation.RelationPoint. You have to return supported relations precisely.
		 * 
		 * Note that arguments can be relation points or config for relation points. In that last case, it means you only have targets configuration and you can use Ametys.message.MessageTargetHelper to handle targets. 
		 * @param {Object/Ametys.relation.RelationPoint} source The source point of the relation operation (or its config). Targets are assumed to be ready.
		 * @param {Object/Ametys.relation.RelationPoint} target The end point of the relation operation (or its config). Targets are assumed to be ready.
		 * @return {Ametys.relation.Relation/Ametys.relation.Relation[]} Returns the supported operations between those two points. The order is important: after filtering that array, the first relation is considered as the default one.
		 * @template
		 */
		supportedRelations: function(source, target)
		{
			throw new Error("This method is not implemented in " + this.self.getName());
		},

		/**
		 * The method is called to link source to target using the given relation. 
		 * This operation can be asynchronous and will call callback at the end.
		 * In most cases this implementation will send a Ametys.message.Message to inform the UI that the operation was done.
		 * @param {Ametys.relation.RelationPoint} source The source point of the link operation. Targets are assumed to be ready.
		 * @param {Ametys.relation.RelationPoint} target The end point of the link operation. Targets are assumed to be ready.
		 * @param {Function} callback The callback to call when operation has ended. 
		 * @param {boolean} callback.sucess True if the operation was successful
		 * @param {String} relationType The relation to create. One of the constants Ametys.relation.Relation.MOVE, Ametys.relation.Relation.COPY or Ametys.relation.Relation.REFERENCE.
		 * @template
		 */
		link: function(source, target, callback, relationType)
		{
			throw new Error("This method is not implemented in " + this.self.getName());
		},
        
        /**
         * @inheritDoc
         * @private 
         * The server role for such components 
         * @return {String} The component role
         */
        getServerRole: function()
        {
            return "org.ametys.core.ui.RelationsManager";
        },
        
        /**
         * @inheritDoc
         * @private 
         * The server id for this component
         * @return {String} #getId
         */
        getServerId: function()
        {
            return this.getId();
        },
		
        /**
         * Some default values of #serverCall are modified
         * @param {Object} options The options
         * @param {Boolean/String/Object} [options.errorMessage=true] Default value is set to true. 
         */
        beforeServerCall: function(options)
		{
			// Default error message
			if (options.errorMessage == null)
			{
				options.errorMessage = true;
			}
		}
        
        /**
         * @method afterServerCall
         * @private
         * Do nothing
         */
	}
);
