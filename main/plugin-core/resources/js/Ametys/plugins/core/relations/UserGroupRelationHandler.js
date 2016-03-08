/*
 *  Copyright 2015 Anyware Services
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
 * @private
 * This is a relation handler between:
 * * source : user
 * * destination : group
 */
Ext.define('Ametys.plugins.core.relations.UserGroupRelationHandler', {
	extend: 'Ametys.relation.RelationHandler',
	
	/**
	 * @private
	 * Do the #supportedRelations work but based only on targets array
	 * This method return the list of supported operations between the source and the target points of a relation.
	 * The implementation should only handle targets and should not have to check the source and target relations: a later filter is done by the  {@link Ametys.relation.RelationManager}.
	 * @param {Ametys.message.MessageTarget[]} sourceTargets The source point of the relation operation. Targets are assumed to be ready.
	 * @param {Ametys.message.MessageTarget[]} targetTargets The end point of the relation operation. Targets are assumed to be ready.
	 * @return {Ametys.relation.Relation/Ametys.relation.Relation[]} Return the supported operations between those two points. Order matters: after filtering that array, the first relation is considered as the default one.
	 */
	_supportedRelations: function(sourceTargets, targetTargets)
	{
		var sourceMatch = Ametys.message.MessageTargetHelper.findTargets(sourceTargets, Ametys.message.MessageTarget.USER, 1);
		if (sourceMatch.length == 0 || sourceMatch.length != sourceTargets.length)
		{
			return [];
		}
		
		var targetMatch = Ametys.message.MessageTargetHelper.findTargets(targetTargets, Ametys.message.MessageTarget.GROUP, 1);
		if (targetMatch.length != 1)
		{
			return [];
		}

		var relations = [ 
		    Ext.create('Ametys.relation.Relation', {
    			type: Ametys.relation.Relation.REFERENCE,
    			label: "{{i18n PLUGINS_CORE_RELATIONS_ADD_USER_LABEL}}",
    			description: "{{i18n PLUGINS_CORE_RELATIONS_ADD_USER_DESCRIPTION}}",
    			smallIcon: null,
    			mediumIcon: null,
    			largeIcon: null
    		})
		];
			
		return relations;
	},
	
	supportedRelations: function(source, target)
	{
		return this._supportedRelations(source.targets, target.targets);
	},
	
	/**
	 * @private
	 * Do the #link work but based only on targets array
	 * The method is called to link source to target using the given relation. 
	 * This operation can be asynchronous and will invoke a callback at the end.
	 * In most cases this implementation will send a {@link Ametys.message.Message} to let the UI know that the operation is finished.
	 * @param {Ametys.message.MessageTarget[]} sourceTargets The source point of the link operation. Targets are assumed to be ready.
	 * @param {Ametys.message.MessageTarget} target The end point of the link operation. Targets are assumed to be ready.
	 * @param {Function} callback The callback to call when the operation has ended. 
	 * @param {Boolean} callback.success True if the operation was successful
	 */
	_link: function(sourceTargets, target, callback)
	{
		var sourceIds = [];
		Ext.Array.forEach(Ametys.message.MessageTargetHelper.findTargets(sourceTargets, Ametys.message.MessageTarget.USER, 1), function(target) {
			sourceIds.push(target.getParameters().id);
		});
		
		Ametys.plugins.core.groups.GroupsDAO.addUsersGroup([target.getParameters().id, sourceIds, null, Ametys.message.MessageTarget.GROUP], null, {});
	},
	
	link: function(source, target, callback, relationType)
	{
		this._link(source.getTargets(), target.getTarget(), callback);
	}
});
