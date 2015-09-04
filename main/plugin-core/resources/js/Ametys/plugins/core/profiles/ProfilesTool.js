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
 * This tool displays the list of users
 * @private
 */
Ext.define('Ametys.plugins.core.profiles.ProfilesTool', {
	extend: 'Ametys.tool.Tool',
	
	statics: {
		/**
		 * Switch mode
		 * @param {String} mode The mode: 'view' or 'edit'
		 */
		switchMode: function (controller)
		{
			var profileTool = Ametys.tool.ToolsManager.getFocusedTool();
			profileTool.switchMode (controller.isPressed() ? 'view': 'edit');
		},
		
		/**
		 * Select all rights
		 */
		selectAll: function (controller)
		{
			var profileTool = Ametys.tool.ToolsManager.getFocusedTool();
			profileTool.selectAll();
		},
		
		/**
		 * Unselect all rights
		 */
		unselectAll: function (controller)
		{
			var profileTool = Ametys.tool.ToolsManager.getFocusedTool();
			profileTool.unselectAll();
		}
	},
	
	constructor: function(config)
	{
		this.callParent(arguments);
		
		// Listening to some bus messages.
		Ametys.message.MessageBus.on(Ametys.message.Message.CREATED, this._onMessageCreated, this);
		Ametys.message.MessageBus.on(Ametys.message.Message.MODIFIED, this._onMessageEdited, this);
		Ametys.message.MessageBus.on(Ametys.message.Message.DELETED, this._onMessageDeleted, this);
	},
	
	getMBSelectionInteraction: function() 
	{
		return Ametys.tool.Tool.MB_TYPE_ACTIVE;
	},
	
	setParams: function(params)
	{
		this.callParent(arguments);
		this.refresh();
	},
	
	createPanel: function()
	{
		var profileStore = this._createProfileStore();
		
		this._profileGrid = Ext.create('Ext.grid.Panel', {
			region: 'west',
			border: false,
			scrollable: true,
			width: 500,
			
			store: profileStore,
			columns: [{flex: 1, header: "<i18n:text i18n:key='PLUGINS_CORE_UITOOL_PROFILES_LABEL' i18n:catalogue='plugin.core'/>", width: 330, dataIndex: 'label', renderer: this._renderProfilName, hideable: false}],
			
			listeners: {
				selectionchange: {fn: this._onProfileSelectionChange, scope: this}
			}
		});
		
		this._rightsPanel = Ext.create('Ext.panel.Panel', {
			region: 'center',
			layout: 'card',
			activeItem:0,
			
			items: [{
				itemId: 'card-view',
				scrollable:true,
				border: false,
				html: 'TODO Mode visualisation'
			}, {
				itemId: 'card-edit',
				scrollable:true,
				border: false,
				html: 'TODO Mode edition'
			}]
		});
		
		return Ext.create('Ext.panel.Panel', {
			defaults: {
				split: true
			},
			layout: 'border',
			items: [this._profileGrid, this._rightsPanel]
		});
	},
	
	/**
	 * @private
	 * Create the profiles' store
	 * @return {Ext.data.Store} The store
	 */
	_createProfileStore: function ()
	{
		var storeConfig = Ext.merge({
			remoteSort: false,
			autoLoad: true,
			
			model: 'Ametys.plugins.core.profiles.ProfilesTool.Profile',
			proxy: {
				type: 'ametys',
				reader: {
					type: 'json',
					rootProperty: 'profiles'
				}
			},
			
			sortOnLoad: true,
			sorters: [{property: 'label', direction:'ASC'}]
			
		}, this.getProfileStoreConfig());
		
		return Ext.create('Ext.data.Store', storeConfig);
	},
	
	/**
	 * Returns the elements of configuration of group store to be overridden.
	 * Override this function if you want to override the group store configuration.
	 * @return {Object} The elements of store configuration to be overridden
	 */
	getProfileStoreConfig: function()
	{
		return {
			proxy: {
				plugin: 'core',
				url: 'rights/profiles.json',
				
				extraParams: {
					limit: null // No pagination
				}
			}
		};
	},
	
	/**
	 * Listener when a profile is selected. Update the rights' panel
	 * @param {Ext.selection.Model} model The selection model
	 * @param {Ext.data.Model[]} selected The selected records
	 * @private
	 */
	_onProfileSelectionChange: function(model, selected)
	{
		// TODO Update rights panel
		
		this.sendCurrentSelection();
	},
	
	/**
	 * Switch mode
	 * @param {String} mode The mode: 'view' or 'edit'
	 */
	switchMode: function (mode)
	{
		if (mode == 'edit')
		{
			// Go to edition mode
			this._rightsPanel.getLayout().setActiveItem(1);
		}
		else
		{
			// Go to view mode
			this._rightsPanel.getLayout().setActiveItem(0);
			// TODO Update rights
		}
		
		Ext.create('Ametys.message.Message', {
			type: Ametys.message.Message.TOOL_PARAMS_UPDATED, // TODO TOOL_STATE_CHANGED ??
			targets: {
				type: Ametys.message.MessageTarget.TOOL,
				parameters: {'tools': [this]}
			}
		});
	},
	
	
	/**
	 * Select all rights
	 */
	selectAll: function ()
	{
		alert("Method #selectAll is not yet implemented");
	},
	
	/**
	 * Unselect all rights
	 */
	unselectAll: function ()
	{
		alert("Method #unselectAll is not yet implemented");
	},
	
	/**
	 * Get the current mode
	 * @return the mode
	 */
	getMode: function ()
	{
		return this._rightsPanel.getLayout().getActiveItem().getItemId() == 'card-view' ? 'view' : 'edit';
	},
	
	sendCurrentSelection: function()
	{
		var targets = [];
		var selection = this._profileGrid.getSelectionModel().getSelection();
		
		Ext.Array.forEach (selection, function (profile) {
			targets.push ({
				type: Ametys.message.MessageTarget.PROFILE,
				parameters: {id: profile.getId()}
			})
		});
		
		Ext.create('Ametys.message.Message', {
			type: Ametys.message.Message.SELECTION_CHANGED,
			targets: targets
		});
	},
	
	/**
	 * Listener when a Ametys.message.Message#CREATED message was received
	 * @param {Ametys.message.Message} message The received message
	 * @private
	 */
	_onMessageCreated: function(message)
	{
		var target = message.getTarget(Ametys.message.MessageTarget.PROFILE);
		if (target)
		{
			var id = target.getParameters().id;
			var profile = this._profileGrid.getStore().getById(id);
			if (profile)
			{
				Ametys.plugins.core.profiles.ProfilesDAO.getProfile([id], this._updateProfile, {scope: this});
			}
			else
			{
				Ametys.plugins.core.profiles.ProfilesDAO.getProfile([id], this._addProfile, {scope: this});
			}
		}
	},
	
	/**
	 * Listener when a Ametys.message.Message#MODIFIED message was received
	 * @param {Ametys.message.Message} message The received message
	 * @private
	 */
	_onMessageEdited: function(message)
	{
		var target = message.getTarget(Ametys.message.MessageTarget.PROFILE);
		if (target != null)
		{
			var id = target.getParameters().id;
			if (message.getParameters().major)
			{
				Ametys.plugins.core.profiles.ProfilesDAO.getProfile([id], this._updateProfile, {scope: this});
			}
			else
			{
				// TODO reload rights
				
			}
		}
	},
	
	/**
	 * Listener when a Ametys.message.Message#DELETED message was received
	 * @param {Ametys.message.Message} message The received message
	 * @private
	 */
	_onMessageDeleted: function(message)
	{
		var targets = message.getTargets(Ametys.message.MessageTarget.PROFILE);
		
		var store = this._profileGrid.getStore();
		Ext.Array.forEach(targets, function(target) {
			var profile = store.getById(target.getParameters().id);
			if (profile)
			{
				store.remove(profile);
			}
		}, this);
	},
	
	/**
	 * @private
	 * Add a profile record 
	 * @param {Object} profile The profile's properties
	 */
	_addProfile: function (profile)
	{
		if (profile && profile.id)
		{
			var record = Ext.create('Ametys.plugins.core.profiles.ProfilesTool.Profile', profile);
			this._profileGrid.getStore().addSorted(record);
			this._profileGrid.getSelectionModel().select([record]);
		}
	},
	
	/**
	 * @private
	 * Update a profile record 
	 * @param {Object} profile The profile's properties
	 */
	_updateProfile: function (profile)
	{
		if (profile && profile.id)
		{
			var store = this._profileGrid.getStore();
			var record = store.getById(profile.id);
			
			record.beginEdit();
			record.set('label', profile.label);
			record.endEdit();
			
			// commit changes (record is not marked as dirty anymore)
			record.commit();
			
			// re-sort
			store.sort();
		}
	}
	

});
