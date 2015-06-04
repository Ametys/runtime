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
 * Singleton class defining the actions related to the plugins and workpaces tools
 * @private
 */
Ext.define('Ametys.plugins.core.administration.plugins.PluginsDAO', {
	singleton: true,
	
	/**
	 * @private
	 * @property {Object} _SEP Map<String, String>
	 * This association of single extension point name and extension to choose.
	 * This association records the changes wanted until there are saved
	 */
	_SEP: {},
	/**
	 * @private
	 * @property {Object} _EP Map<String, boolean>
	 * This association of multiple extension point name and true to active / false to deactive.
	 * This association records the changes wanted until there are saved
	 */
	_EP: {},
	
	/**
	 * Reset the list of changes
	 */
	reset: function()
	{
		this._SEP = {}; 
		this._EP = {};			
	},
	
	/**
	 * Select a single extension point
	 * @param {String} role the single extension point's role
	 * @param {String} id the id of the single extension point
	 */
	selectSingleExtensionPoint: function (role, id)
	{
		this._SEP[role] = id;
	},
	
	/**
	 * Activate an extension point 
	 * @param {String} the id of the extension point to activate
	 */
	activateExtensionPoint: function (id)
	{
		this._EP[id] = true;
	},
	
	/**
	 * Deactivate an extension point 
	 * @param {String} the id of the extension point to deactivate
	 */
	deactivateExtensionPoint: function (id)
	{
		this._EP[id] = false;
	},
	
	/**
	 * Get the list of modified single extension points
	 * @return {Object} The modified single extension points
	 */
	getModifiedSingleExtensionPoints: function ()
	{
		return this._SEP;
	},
	
	/**
	 * Get the list of modified extension points
	 * @return {Object} The modified extension points
	 */
	getModifiedExtensionPoints: function ()
	{
		return this._EP;
	},
	
    /**
     * Has the plugins extensions points pending changes?
     * @return {Boolean} True if they are changes that are not saved yet
     */
	hasPendingChanges: function ()
	{
		return !Ext.Object.isEmpty(this._EP) || !Ext.Object.isEmpty(this._SEP);
	}
});