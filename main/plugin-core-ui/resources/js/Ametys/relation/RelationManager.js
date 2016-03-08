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
 * This class is the central point for moving, copying or referencing objects that will affect the server side.
 * The list of supported operations depends on the list of registered extensions.
 */
Ext.define("Ametys.relation.RelationManager", 
	{
		singleton: true,
		
		/**
		 * @private
		 * @property {Ametys.relation.RelationHandler[]} _handlers The know relation handlers.
		 */
		_handlers: [],
		
		/**
		 * Register a new relation handler, that will handle a new kind of relation .
		 * @param {Ametys.relation.RelationHandler} handler The instance of the new relation to handle
		 */
		register: function(handler)
		{
			this._handlers.push(handler);
		},
		
		/**
		 * This method is a quick test to know if there is a chance that #link would work.
		 * It can be called often, is fast and synchronous (no server request) in order to display the right drop-ok or drop-ko icon.
		 * It is based on all Ametys.relation.RelationHandler resgistered.
		 * @param {Object} sourceCfg The Ametys.relation.RelationPoint object from the drag source.
		 * @param {Object} targetCfg The Ametys.relation.RelationPoint object from the drop zone.
		 * @return {Boolean} False if link will fail. True if link can success.
		 */
		testLink: function(sourceCfg, targetCfg)
		{
			var sourceRelationTypes = (Ext.isArray(sourceCfg.relationTypes) ? sourceCfg.relationTypes : [sourceCfg.relationTypes || Ametys.relation.Relation.REFERENCE]);
			var targetRelationTypes = (Ext.isArray(targetCfg.relationTypes) ? targetCfg.relationTypes : [targetCfg.relationTypes || Ametys.relation.Relation.REFERENCE]);
			
			var possibleRelationTypes = Ext.Array.intersect(sourceRelationTypes, targetRelationTypes);
			if (possibleRelationTypes.length == 0)
			{
				return false;
			}

			for (var i = 0; i < this._handlers.length; i++)
			{
				var handler = this._handlers[i];
				var possibleRelation = handler.supportedRelations(sourceCfg, targetCfg) || [];
				possibleRelation = Ext.isArray(possibleRelation) ? possibleRelation : [possibleRelation];
				
				// Filter available relations with given relation
				possibleRelation = Ext.Array.filter(possibleRelation, function(r, index) {
					return Ext.Array.contains(possibleRelationTypes, r.getType());
				});
				
				if (possibleRelation.length > 0)
				{
					return true;
				}
			}
			
			return false;
		},
		
		/**
		 * Effectively do a relation operation by delegating to a Ametys.relation.RelationHandler.
		 * This is an asynchronous process. Note that a popup to select handler and/or relation may appear.
		 * @param {Object/Ametys.relation.RelationPoint} source The start point of the relation operation. Can be a Ametys.relation.RelationPoint config or the object itself.
		 * @param {Object/Ametys.relation.RelationPoint} target The end point of the relation operation. Can be a Ametys.relation.RelationPoint config or the object itself.
		 * @param {Function} callback The function called when the relation operation is over.
		 * @param {Boolean/String} callback.success The success. False is a problem occurred, a Ametys.relation.Relation constant else determining which operation was done
		 * @param {String} [relationType=null] The relation type to establish.
		 * 										The default value is null that means to use the default relation of the chosen handler.
		 * 										You may transmit a relation (one of the constants Ametys.relation.Relation.MOVE, Ametys.relation.Relation.COPY or Ametys.relation.Relation.REFERENCE) to try to force the relation kind (if available only else it will leads to a popupmenu): this should happens if the user do not select the default UI option (e.g. a drag and drop with shift key maintained would force relation to Ametys.relation.RelationManager.MOVE).
		 * @param {Boolean} [handlerchoice=false] When true, if necessary a popup will appear to make the user choice the handler and/or relation to use. The user can "remember" its choice. So, when false (most cases), the manager will choose a handler.True should happens when the user do a special operation (e.g. drag and drop using the right button of the mouse). Even when this value is false, the popup may appear if the user did never select a default value.
		 */
		link: function(source, target, callback, relationType, handlerchoice)
		{
			this.getLogger().debug("Trying to start link");

			if (!source.self)
			{
				source = Ext.create("Ametys.relation.RelationPoint", source);
			}
			if (!target.self)
			{
				target = Ext.create("Ametys.relation.RelationPoint", target);
			}
			
			if (!source.isReady())
			{
				source.waitForTargets(Ext.bind(this.link, this, arguments, false));
				return;
			}
			if (!target.isReady())
			{
				target.waitForTargets(Ext.bind(this.link, this, arguments, false));
				return;
			}

			this.getLogger().debug("Starting link...");

			// List compatible relations between source and target
			var possibleRelationTypes = Ext.Array.intersect(source.relationTypes, target.relationTypes);
			if (possibleRelationTypes.length == 0)
			{
				this.getLogger().warn("Cannot find compatible relations types");
				callback(false);
				return;
			}
			
			// Loop on handlers to get those handling this source and this target, and filter it by possibleRelationTypes to keep only the possible relations
			var possibleRelations = {};
			for (var i = 0; i < this._handlers.length; i++)
			{
				var handler = this._handlers[i];
				var possibleRelation = handler.supportedRelations(source, target) || [];
				possibleRelation = Ext.isArray(possibleRelation) ? possibleRelation : [possibleRelation];
				
				// Filter available relations with given relation
				possibleRelation = Ext.Array.filter(possibleRelation, function(r, index) {
					return Ext.Array.contains(possibleRelationTypes, r.getType());
				});
				
				if (possibleRelation.length > 0)
				{
					possibleRelations[i] = possibleRelation;
				}
			}
			var indexes = Ext.Object.getKeys(possibleRelations);
			if (indexes.length == 0)
			{
				Ametys.Msg.alert("{{i18n PLUGINS_CORE_UI_RELATIONS_UNSUPPORTED_MOVE_LABEL}}", "{{i18n PLUGINS_CORE_UI_RELATIONS_UNSUPPORTED_MOVE_DESCRIPTION}}");
				this.getLogger().warn("No RelationHandler supports one of the compatible relations");
				callback(false);
				return;
			}
				
			// Now we have to determine which handler and relation will do the job
			var choosenHandler = null;
			var choosenRelation = null;
				
			if (indexes.length == 1)
			{
				// A single handler that'it
				choosenHandler = indexes[0];
			}			
			else if (!handlerchoice)
			{
				// Many possible handlers, is there any that was previously chosen?
				// TODO
				// choosenHandler = ...
			}
			
			if (choosenHandler)
			{
				// The handler is known
				if (possibleRelations[choosenHandler].length == 1)
				{
					// A single possible relation in it. that's it
					choosenRelation = possibleRelations[choosenHandler][0];
				}
				else
				{
					// Many relation in the chosen handler
					if (relationType)
					{
						// A relation type was imposed
						
						// Let's see if its compatible with the possible relations
						var compatible = null;
						Ext.Array.every(possibleRelations[choosenHandler], function(item, index, array) {
							if (item.getType() == relationType)
							{
								compatible = index;
								return false;
							}
							return true;
						});
						
						if (compatible)
						{
							// The imposed relation type is possible => choice is done
							choosenRelation = possibleRelations[choosenHandler][compatible];
						}
					}
					else if (!handlerchoice)
					{
						// No relation type was imposed and the user did not wanted to choose, let's take the first one (e.g. the default choice)
						choosenRelation = possibleRelations[choosenHandler][0];
					}
				}
			}
			
			if (choosenHandler == null || choosenRelation == null)
			{
				// Either the handler or the relation in it, is not determined: let's display a menu to make the user choose
				
				// TODO
				callback(false);
				throw new Error("Not Yet implemented: The user have to choose which handler will do the relation");
			}

			
			this.getLogger().debug("Doing link...");

			// Finally we have a handler and a relationType: let's link
			this._handlers[choosenHandler].link(source, target, Ext.bind(this._linkCb, true, [choosenRelation.getType(), callback], true), choosenRelation.getType());

			this.getLogger().debug("Link is running...");
		},
	
		/**
		 * The link callback
		 * @param {Boolean} success Determine if the link was successfully done
		 * @param {String} tryedRelation The relation tried (Ametys.relation.Relation constant)
		 * @param {Function} callback The initial function called when the relation operation is over.
		 * @param {Boolean/String} callback.success The success. False is a problem occurred, a Ametys.relation.Relation constant else determining which operation was done
		 */
		_linkCb: function(success, tryedRelation, callback)
		{
			return callback(success ? tryedRelation : false);
		}
	}
);
