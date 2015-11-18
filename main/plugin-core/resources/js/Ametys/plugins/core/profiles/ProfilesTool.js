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
 * This tool displays the list of users
 */
Ext.define('Ametys.plugins.core.profiles.ProfilesTool', {
	extend: 'Ametys.tool.Tool',
	
	/**
	 * @private
	 * @property {Ext.grid.Panel} _profilesGrid the profiles' grid panel  
	 */
	
	/**
	 * @private
	 * @property {Ext.panel.Panel} _rightsPanel the rights' panel
	 */
	
	/**
	 * @private
	 * @property {Object} _rightsByCategory the right records sorted by category name
	 */
	
	/**
	 * @private
	 * @property {String} _currentProfileId the id of current selected profile
	 */
	
	/**
	 * @private
	 * @property {String[]} _rights the list of rights before the tool gets dirty
	 */
	
	statics: {
		
		/**
		 * Save the rights for the selected profile
		 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function 
		 */
		save: function(controller)
		{
			var profileTool = Ametys.tool.ToolsManager.getFocusedTool();
			profileTool.saveCurrentChanges();
		},
		
		/**
		 * Discard the pending changes on the selected profile
		 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function 
		 */
		discardChanges: function(controller)
		{
			var profileTool = Ametys.tool.ToolsManager.getFocusedTool();
			
			profileTool._updateCardPanels(profileTool._rights);
			profileTool.setDirty(false);
			profileTool.sendCurrentSelection();
		},

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
		 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function 
		 */
		selectAll: function (controller)
		{
			var profileTool = Ametys.tool.ToolsManager.getFocusedTool();
			profileTool.selectAll();
		},
		
		/**
		 * Unselect all rights
		 * @param {Ametys.ribbon.element.ui.ButtonController} controller The controller calling this function 
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
	
    setParams: function (params)
    {
		this.callParent(arguments);
		this._initialSelectedProfiles = params.selectedProfiles || [];
		
		this.refresh();
		
		var role = this.getFactory().getRole();
	    var toolParams = this.getParams();

        Ametys.navhistory.HistoryDAO.addEntry({
			id: this.getId(),
			label: this.getTitle(),
			description: this.getDescription(),
			iconSmall: this.getSmallIcon(),
			iconMedium: this.getMediumIcon(),
			iconLarge: this.getLargeIcon(),
			type: Ametys.navhistory.HistoryDAO.TOOL_TYPE,
			action: Ext.bind(Ametys.tool.ToolsManager.openTool, Ametys.tool.ToolsManager, [role, toolParams], false)
        });
    },
	
	createPanel: function()
	{
		var profileStore = this._createProfileStore();
		var rightsStore = this._createRightsStore();
		
		this._profilesGrid = Ext.create('Ext.grid.Panel', {
			border: true,
			scrollable: true,
			
			stateful: true,
			stateId: this.self.getName() + "$grid",
			
			minWidth: 100,
			maxWidth: 400,
			flex: 0.3,
			split: true,
			
			store: profileStore,
			columns: [{flex: 1, header: "<i18n:text i18n:key='PLUGINS_CORE_UITOOL_PROFILES_LABEL' i18n:catalogue='plugin.core'/>", dataIndex: 'label', renderer: this._renderProfilName, hideable: false}],
			
			listeners: {
				beforeselect: {fn: this._onBeforeSelectProfile, scope: this}
			}
		});
		
		this._rightsPanel = Ext.create('Ext.Panel', {
			flex: 1,
			border: true,
			minWidth: 350,
			split: true,
			layout: 'card',
			cls: 'profile-rights-tool',
			activeItem: 0,
			
			store: rightsStore,
			items: [{
				itemId: 'card-view',
				scrollable: 'vertical',
				border: false,
				items: []
			}, {
				itemId: 'card-edit',
				scrollable: true,
				border: false,
				items: []
			}]
		});
		
		return Ext.create('Ext.container.Container', {
			layout: {
				type: 'hbox',
				pack: 'start',
				align: 'stretch'
			},
			scrollable: 'horizontal',
			items: [this._profilesGrid, this._rightsPanel]
		});
	},
	
	sendCurrentSelection: function()
	{
		var targets = [];
		var currentProfileId = this._currentProfileId; 
		
		if (currentProfileId != null)
		{
			var subtargets = [];
			
			if (this.getMode() == 'edit')
			{
				subtargets.push({
		            type: Ametys.message.MessageTarget.FORM,
		            parameters: {
		                isDirty: this.isDirty()
		            }
		        });
			}
			
			targets.push ({
				type: Ametys.message.MessageTarget.PROFILE,
				parameters: {id: currentProfileId},
				subtargets: subtargets
			});
		}
		
		Ext.create('Ametys.message.Message', {
			type: Ametys.message.Message.SELECTION_CHANGED,
			targets: targets
		});
	},
	
	close: function(manual)
	{
        if (this.isDirty())
		{
        	var me = this;
        	var callback = function (doSave)
        	{
        		if (doSave == null)
    			{
        			return;
    			}
        		else
    			{
        			if (doSave)
    				{
        				me.saveCurrentChanges(function() {Ametys.plugins.core.profiles.ProfilesTool.superclass.close.call(me, [manual])});
    				}
        			else
    				{
        				Ametys.plugins.core.profiles.ProfilesTool.superclass.close.call(me, [manual]);
    				}
    			}
        	}
        	
        	Ametys.form.SaveHelper.promptBeforeQuit(null, "<i18n:text i18n:key='PLUGINS_CORE_PROFILES_RIGHTS_CONFIRM_DESCRIPTION'/>", null, callback);
        	return;
		}

        this.callParent(arguments);
	},
	
	/**
	 * @private
	 * Create the profiles' store
	 * @return {Ext.data.Store} The profiles' store
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
	 * @private
	 * Listener invoked when the profiles store is loaded
	 * @param {Ext.data.Store} store the profiles store
	 * @param {Ext.data.Record[]} records the records of the store
	 */
	_onProfileStoreLoaded: function(store, records)
	{
    	if (this._initialSelectedProfiles.length > 0)
    	{
    		var records = [];
    		var sm = this._profilesGrid.getSelectionModel();
    		var store = this._profilesGrid.getStore();
    		
    		Ext.Array.each (this._initialSelectedProfiles, function (id) {
    			var index = store.find("id", id); 
                if (index != -1)
                {
                	records.push(store.getAt(index));
                }
    		});
    		
    		sm.select(records);
    		
    		this._initialSelectedProfiles = []; // reset
    	}
    },
	
	/**
	 * @private
	 * Create the rights' store
	 * @return {Ext.data.Store} the rights' store
	 */
	_createRightsStore: function()
	{
		var storeConfig = Ext.merge({
			autoLoad: true,
			
			model: 'Ametys.plugins.core.profiles.ProfilesTool.Right',
			proxy: {
				type: 'ametys',
				reader: {
					type: 'json',
					rootProperty: 'rights'
				}
			},
			
			remoteSort: false,
			sortOnLoad: true,
			sorters: [{property: 'category', direction:'ASC'}, {property: 'label', direction: 'ASC'}]
			
		}, this.getRightsStoreConfig());
		
		return Ext.create('Ext.data.Store', storeConfig);
	},
	
	/**
	 * Returns the elements of configuration of profiles store to be overridden.
	 * Override this function if you want to override the profiles store configuration.
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
			},
			
			listeners: {
				load: {fn: this._onProfileStoreLoaded, scope: this}
			} 
		};
	},
	
	/**
	 * Returns the elements of configuration of rights store to be overridden.
	 * Override this function if you want to override the rights store configuration.
	 * @return {Object} The elements of store configuration to be overridden
	 */
	getRightsStoreConfig: function()
	{
		return {
			proxy: {
				plugin: 'core',
				url: 'rights/rights.json'
			},
			
			listeners: {
				load: {fn: this._onRightsStoreLoaded, scope: this}
			} 
		};
	},
	
	/**
	 * @private
	 * Function invoked before a profile is selected. Update the rights' panel
	 * @param {Ext.selection.Model} model The selection model
	 * @param {Ext.data.Model} selection The selected record
	 */
	_onBeforeSelectProfile: function(model, selection)
	{
		if (this._currentProfileId != null && this.isDirty())
		{
			var me = this; 
			var rights = this._getRights();
			var callback = function (doSave)
			{
				if (doSave != null)
				{
					if (doSave)
					{
						me.saveCurrentChanges(function () {
							me._profilesGrid.setSelection(selection);
						})
					}
					else
					{
						me.setDirty(false);
						me._profilesGrid.setSelection(selection);
					}
				}
			}
			
			Ametys.form.SaveHelper.promptBeforeQuit(null, "<i18n:text i18n:key='PLUGINS_CORE_PROFILES_RIGHTS_CONFIRM_DESCRIPTION'/>", null, callback);

			// Cancel the selection, it will be done after the user answered the dialog
			return false;
    	}
		
		this._currentProfileId = selection.getId();

		this.sendCurrentSelection();
		this._updateCardPanels(selection.get('rights') || []);
	},
	
	/**
	 * @private
	 * Listener invoked when the rights store is loaded
	 * @param {Ext.data.Store} store the rights store
	 * @param {Ext.data.Record[]} records the records of the store
	 */
	_onRightsStoreLoaded: function(store, records)
	{
		this._rightsByCategory = {};
		
		// Categorize the rights
		// We generate the id 'categoryId' that will be used later in order to avoid adding it in the server method right2Json
		// Indeed, we can't use the category name directly because some of their characters may not be accepted within an itemId
		
		var categoriesIds = {};
		var categoryId = null; 
		for (var i = 0; i < store.getTotalCount(); i++)
		{
			// work on store data instead of the records because the records are not sorted
			var record = store.getAt(i);
			
			var categoryName = record.get('category');
			categoryId = categoriesIds[categoryName];
			if (categoryId == null)
			{
				// Create a new identifier every time we encounter an unregistered category name
				categoryId = Ext.id();
				categoriesIds[categoryName] = categoryId;
				
				this._rightsByCategory[categoryId] = [];
			}
			
			this._rightsByCategory[categoryId].push(record);
		}
		this._drawCardPanels();
	},
	
	/**
	 * @private
	 * Draw items inside both of the panels of the card panel of the tool and hide them
	 */ 
	_drawCardPanels: function()
	{
		var me = this;
		
		var cardViewPanel = this._rightsPanel.getComponent('card-view');
		var cardEditPanel = this._rightsPanel.getComponent('card-edit');
		
		Ext.suspendLayouts();
		
		Ext.Object.each(this._rightsByCategory, function(categoryId) {

			var categoryRights = this._rightsByCategory[categoryId];
			
			// Common configuration
			var categoryPanelCfg = {
				itemId: categoryId,
					
				title : categoryRights[0].get('category'),
	            layout: 'column',
	            cls: 'ametys-fieldset',
 				
				collapsible: true,
	            titleCollapse: true,
	            hideCollapseTool: true,
	            
	            border: false
			};
			
			var categoryViewPanel = Ext.create('Ext.panel.Panel', categoryPanelCfg);
			var categoryEditPanel = Ext.create('Ext.panel.Panel', Ext.applyIf(
			{
				tools: [
			        {
			        	xtype: 'button',
			        	itemId: 'category-select-all',
			        	icon: Ametys.getPluginResourcesPrefix('core') + '/img/profiles/select_16.png',
			        	tooltip: '<i18n:text i18n:key="PLUGINS_CORE_UITOOL_PROFILES_CATEGORY_SELECT_ALL"/>',
			        	border: false,
                        handler: function() {me.selectAll(categoryId)},
                        listeners: 
						{
						    click: 
						    {
						        fn: function(header, event) 
						        {
						    	  // Prevent panel collapsing mechanism
						    	  event.stopPropagation();
						        }
						    }
						}
                        
			        },
			        {
			        	xtype: 'button',
			        	itemId: 'category-unselect-all',
			        	icon: Ametys.getPluginResourcesPrefix('core') + '/img/profiles/unselect_16.png',
			        	tooltip: '<i18n:text i18n:key="PLUGINS_CORE_UITOOL_PROFILES_CATEGORY_UNSELECT_ALL"/>',
			        	border: false,
			        	handler: function() {me.unselectAll(categoryId)},
			        	listeners: 
			        	{
						    click: 
						    {
						        fn: function(header, event) 
						        {
						    	  // Prevent panel collapsing mechanism
						    	  event.stopPropagation();
						        }
						    } 
						}
			        }
		        ]
			}, categoryPanelCfg));
			
			Ext.Array.each(categoryRights, function(right) {
				
  				var rightId = right.get('id'),
				    rightLabel = right.get('label'),
				    rightDescription = right.get('description');
				
  				// common configuration
				var rightContainerConfig = 
				{
					columnWidth: 0.33,
					padding: 5,
					
					cls: 'right-element',
					
					layout: {
						type: 'hbox',
						align: 'middle'
					}
				};
				
				// Visualization container for a right
				var viewRightContainer = Ext.create('Ext.Container', 
					Ext.applyIf(
					{
						itemId: rightId + '-view',
						
						items: [
				        {
					        xtype: 'component',
					        cls: 'ametys-description',
					        width: 20,
					        
					        margin: '-1 0 0 0',

							listeners: {
								render: function() {
									this.getEl().set({'data-qtip': rightDescription, 'data-qtitle': rightLabel});
								}
							}
				        }]
					
					}, rightContainerConfig)
				);
				
				// add the text
				viewRightContainer.add({
					xtype: 'component',
					html: rightLabel
				});

				// Edition container for a right
				var editRightContainer = Ext.create('Ext.form.field.Checkbox', 
					Ext.applyIf(
					{
						itemId: rightId + '-edit',
						
						boxLabel: rightLabel, // text for the component that will be created
						disabled: true,
						
						listeners: {
							change: Ext.bind(me._onEditRightFieldChange, me),
							render: function() 
							{
								this.getEl().insertFirst({
									cls: 'ametys-description',
							        width: 20,
							        
							        margin: '-1 0 0 0',
									'data-qtitle': rightLabel,
									'data-qtip': rightDescription
								});
							}
						}
					}, rightContainerConfig)
				);
				
				categoryViewPanel.add(viewRightContainer);
				categoryEditPanel.add(editRightContainer);
			});
			
			cardViewPanel.add(categoryViewPanel);
			cardEditPanel.add(categoryEditPanel);
			
			// The category panels are initially hidden for visualization mode
			categoryViewPanel.hide();

		}, this);
		
		Ext.resumeLayouts(true);
	},
	
	/**
	 * @private
	 * Handler function invoked when an right edition field has changed
	 * @param {Ext.form.field.Checkbox} box the checkbox
	 * @param {Object} newValue the value of the field after it has changed
	 * @param {Object} oldValue the value of the field before it has changed
	 */
	_onEditRightFieldChange: function(box, newValue, oldValue) 
	{
        if (!this.isDirty())
    	{
        	this.setDirty(true);
        	this.sendCurrentSelection();
    	}
	},
	
	/**
	 * @private
	 * Update the card panels according to the selected profile's right ids
	 * @param {String[]} profileRightsIds the ids of the rights for the selected profile
	 */
	_updateCardPanels: function(profileRightsIds)
	{
		Ext.suspendLayouts();
		
		// Keep a copy of the rights before the tool is dirty
		var categoriesToShow = [];
		var cardViewPanel = this._rightsPanel.getComponent('card-view');
		var cardEditPanel = this._rightsPanel.getComponent('card-edit');
		
		Ext.Object.each(this._rightsByCategory, function(categoryId) {
			
			var rights = this._rightsByCategory[categoryId];
			
			var categoryViewPanel = cardViewPanel.getComponent(categoryId);
			var categoryEditPanel = cardEditPanel.getComponent(categoryId);
			
			Ext.Array.each(rights, function (right)
			{
				var rightId = right.get('id');

				var hasRight = Ext.Array.contains(profileRightsIds, rightId);
				
				// Update edition screen without triggering a change event
				var editRightContainer = categoryEditPanel.getComponent(rightId + '-edit');
				editRightContainer.suspendEvents(false);
				editRightContainer.setValue(hasRight);
				editRightContainer.enable();
				editRightContainer.resumeEvents();
				
				// Update view screen
				var viewRightContainer = categoryViewPanel.getComponent(rightId + '-view');
				viewRightContainer.setVisible(hasRight);
				
				// Update categories
				if (Ext.Array.contains(profileRightsIds, rightId) && !Ext.Array.contains(categoriesToShow, categoryId))
				{
					categoriesToShow.push(categoryId);
				}
			}, this);
			
			categoryEditPanel.show();
		}, this);

		// Update category panels for visualization mode
		Ext.Object.each(this._rightsByCategory, function(categoryId) {
			cardViewPanel.getComponent(categoryId).setVisible(Ext.Array.contains(categoriesToShow, categoryId));
		}, this);
		
		this._rights = this._getRights();
		
		Ext.resumeLayouts(true);
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
			this.sendCurrentSelection();
		}
		else
		{	
			if (this.isDirty())
			{
				var me = this;
				var rights = this._getRights();
			
				var callback = function(doSave) 
				{
					if (doSave == null)
					{
						return;
					}
					else
					{
						if (doSave)
						{
							me.saveCurrentChanges(function() {
								me._rightsPanel.getLayout().setActiveItem(0);
								me.sendCurrentSelection();
							});
						}
						else
						{
							me.setDirty(false);
							me._updateCardPanels(me._rights);
							me._rightsPanel.getLayout().setActiveItem(0);
							me.sendCurrentSelection();
						}
					}
				}
				
				Ametys.form.SaveHelper.promptBeforeQuit(null, "<i18n:text i18n:key='PLUGINS_CORE_PROFILES_RIGHTS_CONFIRM_DESCRIPTION'/>", null, callback);
			}
			else
			{
				// Go to view mode
				this._rightsPanel.getLayout().setActiveItem(0);
				this.sendCurrentSelection();
			}
		}
	},
	
	/**
	 * @private
	 * Get the selected rights
	 */
	_getRights: function()
	{
		var rights = [];
		
		// Gather the selected rights
		var cardEditPanel = this._rightsPanel.getComponent('card-edit');
		Ext.Object.each(this._rightsByCategory, function(categoryId) 
		{
			var categoryEditPanel = cardEditPanel.getComponent(categoryId);
			
			var categoryRights = this._rightsByCategory[categoryId];
			Ext.Array.each(categoryRights, function (right)
			{
				var rightId = right.get('id');
				var editRightContainer = categoryEditPanel.getComponent(rightId + '-edit');
				
				if (editRightContainer.getValue())
				{
					rights.push(rightId);
				}
				
			}, this);
		}, this);
		
		return rights;
	},
	
	/**
	 * Save the current changes done on selected profile
	 * @param {Function} callback the callback function
	 */
	saveCurrentChanges: function(callback)
	{
		var rights = this._getRights();
		Ametys.plugins.core.profiles.ProfilesDAO.editProfileRights([this._currentProfileId, rights], this._saveCb, {scope: this, arguments: {callback: callback}});
	},	
	
	_saveCb: function (profile, args, params)
	{
		// Reset the dirty state of the tool
		this.setDirty(false);
		this._updateCardPanels(params[1]);
		
		if (Ext.isFunction(args.callback))
		{
			args.callback ();
		}
	},

	/**
	 * Select all rights
	 * @param {String} categoryId The item id of the html element surrounding the elements to check. Can be null to check all.
	 * @param {Boolean} [value=true] The value to set to all rights: true means rights are set and false unset.
	 */
	selectAll: function(categoryId, value)
	{
		var cardEditPanel = this._rightsPanel.getComponent('card-edit');
		
		value = value == null ? true : value;
		if (categoryId != null)
		{
			var categoryEditPanel = cardEditPanel.getComponent(categoryId);
			
			categoryEditPanel.items.each (function (item, index) {item.setValue(value)});
		}
		else
		{
			cardEditPanel.items.each(function(item, index) {
				item.items.each(function(item, index) {
					item.setValue(value);
				});
			});
		}
	},
	
	/**
	 * Unselect all rights
	 * @param {String} categoryId The item id of the html element surrounding the elements to uncheck. Can be null to uncheck all.
	 */
	unselectAll: function(categoryId)
	{
		this.selectAll(categoryId, false);
	},
	
	/**
	 * Get the current mode
	 * @return the mode
	 */
	getMode: function ()
	{
		return this._rightsPanel.getLayout().getActiveItem().getItemId() == 'card-view' ? 'view' : 'edit';
	},
	
	/**
	 * @private
	 * Listener when a Ametys.message.Message#CREATED message was received
	 * @param {Ametys.message.Message} message The received message
	 */
	_onMessageCreated: function(message)
	{
		var target = message.getTarget(Ametys.message.MessageTarget.PROFILE);
		if (target)
		{
			var id = target.getParameters().id;
			var profile = this._profilesGrid.getStore().getById(id);
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
	 * @private
	 * Listener when a Ametys.message.Message#EDITED message was received
	 * @param {Ametys.message.Message} message The received message
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
				this._profilesGrid.getStore().reload();
			}
		}
	},
	
	/**
	 * @private
	 * Listener when a Ametys.message.Message#DELETED message was received
	 * @param {Ametys.message.Message} message The received message
	 */
	_onMessageDeleted: function(message)
	{
		var targets = message.getTargets(Ametys.message.MessageTarget.PROFILE);
		
		var store = this._profilesGrid.getStore();
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
			this._profilesGrid.getStore().addSorted(record);
			this._profilesGrid.getSelectionModel().select([record]);
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
			var store = this._profilesGrid.getStore();
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
