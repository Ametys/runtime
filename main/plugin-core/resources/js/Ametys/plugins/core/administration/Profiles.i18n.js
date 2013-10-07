/*
 *  Copyright 2012 Anyware Services
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
 * Class in chage of the profiles administration screen. See {@link #initialize} and {@link #createPanel}
 * @private
 */
Ext.define('Ametys.plugins.core.administration.Profiles', {
	singleton: true,
	
	/**
	 * @private
	 * @property {String} _selectedElmt The currently selected profile
	 */
	/**
	 * @private
	 * @property {Boolean} _hasChanges Has any pending changes?
	 */
	/**
	 * @private
	 * @property {Ametys.admin.rightpanel.NavigationPanel} _Navigation The navigation panel between read/edit mode
	 */
	/**
	 * @private
	 * @property {Ametys.admin.rightpanel.NavigationPanel.NavigationItem} item1 The read item for navigation
	 */
	/**
	 * @private
	 * @property {Ametys.admin.rightpanel.NavigationPanel.NavigationItem} item2 The edit item for navigation
	 */
	/**
	 * @private
	 * @property {Ametys.admin.rightpanel.ActionPanel} _utilsCategory The action panel for handling selectall unselectall
	 */
	/**
	 * @private
	 * @property {Ametys.admin.rightpanel.ActionPanel} _Category The action panel for handling profiles
	 */
	/**
	 * @private
	 * @property {Ext.panel.Panel} cardPanel The main left panel holding read and edit screens
	 */
	/**
	 * @private
	 * @property {Ext.grid.Panel} listview The profile list
	 */
	/**
	 * @private
	 * @property {String} pluginName The name of the plugin handling ajax request
	 */

	/**
	 * Initialize the elements
	 * @param {String} pluginName The name of the plugin to use for ajax request...
	 */
	initialize: function (pluginName)
	{
		this.pluginName = pluginName;
		window.onbeforeunload = Ext.bind(this.checkBeforeQuit, this);
	},
	
	/**
	 * @private
	 * Quit
	 */
	goBack: function ()
	{
	    document.location.href = Ametys.WORKSPACE_URI;
	},

	/**
	 * @private
	 * Listener to create a profile
	 */
	createProfile: function()
	{
		var newEntry = Ext.create('Ametys.plugins.core.administration.Profiles.Profile', {
			'name': "<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_NEWPROFILE'/>",
			'rights': {},
			'id': 'new'
		}); 
        
        var result = Ametys.data.ServerComm.send({
        	plugin: this.pluginName, 
        	url: "/administrator/rights/profiles/create", 
        	parameters: {name: newEntry.get('name') }, 
        	priority: Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, 
        	callback: null, 
        	resonseType: null
        });
        if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_NEW_ERROR'/>", result, "Ametys.plugins.core.administration.Logs.createProfile"))
        {
            return;
        }
        else
        {
            newEntry.set('id', Ext.dom.Query.selectValue("*/id", result));
            newEntry.commit();
        }
        
		this.listview.getStore().addSorted(newEntry);
		this.listview.getSelectionModel().setCurrentPosition({row: this.listview.getStore().indexOf(newEntry), column: 0});
		
		this.rename();
	},
	
	/**
	 * @private
	 * Rename the currently selected profile
	 */
	rename: function()
	{
		var record = this.listview.getSelectionModel().getSelection()[0];
		this.listview.plugins[0].startEdit(record, 0);
	},
	
	/**
	 * @private
	 * Remove the currently selected profile (and ask for a confirmation)
	 */
	remove: function()
	{
		Ext.Msg.confirm ("<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_DELETE'/>", "<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_DELETE_CONFIRM'/>", Ext.bind(this.removeConfirm, this));
	},
	
	/**
	 * @private
	 * Remove effectively the currently selected profile
	 * @param {String} button Do remove if 'yes'
	 */
	removeConfirm: function(button)
	{
		if (button == 'yes')
		{
			var elt = this.listview.getSelectionModel().getSelection()[0];
	    	if (200 == Ext.Ajax.request({url: Ametys.getPluginDirectPrefix(this.pluginName) + "/administrator/rights/profiles/delete", params: "id=" + elt.get('id'), async: false}).status)
			{
				this.listview.getStore().remove(elt);
			}
			else
			{
				Ext.Msg.show ({
            		title: "<i18n:text i18n:key='PLUGINS_CORE_ERROR_DIALOG_TITLE'/>",
            		msg: "<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_ERROR'/>",
            		buttons: Ext.Msg.OK,
   					icon: Ext.MessageBox.ERROR
            	});
			}
		}
	},
	
	/**
	 * Save the current modifications. 
	 * @param {String} button If 'yes' will save effectively by calling {@link #saveObjects}
	 * @param {String} text undefined
	 * @param {Object} opt Options given as arfs
	 * @param {Ext.data.Model} selectedElmt The element to save. Can be null.
	 * @param {String[]} objects The saving informations. Can be null
	 * @param {Object} rights the rights identifiers. Can be null.
	 */
    saveObjectConfirm: function (button, text, opt, selectedElmt, objects, rights)
	{
		if (button == 'yes')
		{
			this.saveObjects(selectedElmt, objects, rights);
		}
		else
		{
			this._hasChanges = false;
		}
	},

	/**
	 * @private
	 * Get the rights identifier
	 * @return {Object} the rights identifiers
	 */
	_getRights: function()
	{
		var newRights = {};
		for (var i=0; i < RIGHTS_ID.length; i++)
		{
			var rightElmt = Ext.getCmp(RIGHTS_ID[i]);
			if (rightElmt.getValue())
			{
				newRights[rightElmt.getName()] = "";
			}
		}
		return newRights;
	},
	/**
	 * @private
	 * Get the objects for saving informations
	 * @return {String[]} The saving informations
	 */
	_getObjects: function()
	{
		var objects = [];
		for (var i=0; i < RIGHTS_ID.length; i++)
		{
			var rightElmt = Ext.getCmp(RIGHTS_ID[i]);
			if (rightElmt.getValue())
			{
				objects.push(rightElmt.getName());
			}
		}
		return objects;
	},
	
	/**
	 * Save the given profile to the server
	 * @param {Ext.data.Model} element The element to save. Can be null to save the currently selected profile.
	 * @param {String[]} objects The saving informations. Can be null
	 * @param {Object} newRights the rights identifiers. Can be null
	 */
    saveObjects: function(element, objects, newRights)
    {
		if (element == null)
		{
			element = this.listview.getSelectionModel().getSelection()[0];
		}

		// Update node and list rights to send
		if (objects == null)
		{
			objects = this._getObjects();
		}
		
		if (newRights == null)
		{
			newRights = this._getRights();
		}
		element.set('rights', newRights);
	
		// Send this
		var ok = false;
		while (!ok)
		{
			var result = Ametys.data.ServerComm.send({
				plugin: this.pluginName, 
				url: "/administrator/rights/profiles/modify", 
				parameters: { id: element.get('id'), objects: objects }, 
				priority: Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, 
				callback: null, 
				responseType: null
			});
		    if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_ERROR'/>", result, "Ametys.plugins.core.administration.Logs.saveObjects"))
		    {
				return;
		    }
		    else
			{
				var state = Ext.dom.Query.selectValue("*/message", result); 
				if (state != null && state == "missing")
				{
					Ext.Msg.show ({
                		title: "<i18n:text i18n:key='PLUGINS_CORE_ERROR_DIALOG_TITLE'/>",
                		msg: "<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_MISSING_ERROR'/>",
                		buttons: Ext.Msg.OK,
	   					icon: Ext.MessageBox.ERROR
                	});
					this.listview.getStore().remove(element);
				}
				else
				{
					ok = true;
					this._Category.hideElt(2);
				}
			}
			
		}
		this._hasChanges = false;
		this.selectLayout(); // update view screen if saving while changing view
    },
    
    /**
     * @private
     * Set the screen in "need saving" mode
     */
    needSave: function()
    {
    	this._Category.showElt(2);
    	this._hasChanges = true;
    },

    /**
     * Listener to check for unsaved modifications before leaving the screen
     */
    checkBeforeQuit: function()
	{
		if (this._selectedElmt != null && this._hasChanges)
		{
			if (confirm("<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_CONFIRM'/>"))
			{
				this.saveObjectConfirm ('yes', this._selectedElmt);
			}
		}
	},
	
	/**
	 * @private
	 * Select all rights
	 * @param {String} id The id of the html element surrounding the elements to check. Can be null to check all.
	 * @param {Boolean} [value=true] The value to set to all rights: true means rights are set and false unset.
	 */
	selectAll: function(id, value)
	{
		value = value == null ? true : value;
		
		if (id != null)
		{
			var fd = Ext.getCmp(id);
			fd.items.each (function (item, index) {item.setValue(value)});
		}
		else
		{
			for (var i=0; i < RIGHTS_ID.length; i++)
			{
				var rightElmt = Ext.getCmp(RIGHTS_ID[i]);
				rightElmt.setValue(value);
			}
		}
	},
	
	/**
	 * @private
	 * Unselect all rights
	 * @param {String} id The id of the html element surrounding the elements to uncheck. Can be null to uncheck all.
	 */
	unselectAll: function(id)
	{
		this.selectAll(id, false);
	},
	
	/**
	 * @private
	 * Listener when a profile is selected
	 */
	onSelectProfile: function()
	{
		if (this._selectedElmt != null && this._hasChanges)
		{
			Ext.Msg.confirm ("<i18n:text i18n:key='PLUGINS_CORE_SAVE_DIALOG_TITLE'/>", "<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_CONFIRM'/>", Ext.bind(this.saveObjectConfirm, this, [this._selectedElmt, this._getObjects(), this._getRights()], true));
		}
		
		this._Category.showElt(1);
		this._Category.hideElt(2);
		this._Category.showElt(3);
		
		var elmt = this.listview.getSelectionModel().getSelection()[0];
		this._selectedElmt = elmt; 
		var rights = elmt.get('rights') || {};
		
		for (var i=0; i < RIGHTS_CATEGORY.length; i++)
		{
			Ext.getCmp("cat_" + RIGHTS_CATEGORY[i] + "_read").hide();
		}
		
		for (var i=0; i < RIGHTS_ID.length; i++)
		{
			// Update edition screen
			var rightElmt = Ext.getCmp(RIGHTS_ID[i]);
			rightElmt.suspendEvents(false);
			var id = rightElmt.getName();
			rightElmt.setDisabled(false);
			rightElmt.setValue(rights[id] != null);
			rightElmt.resumeEvents();
			
			// Update view screen
			var cat = Ext.getCmp("cat_" + rightElmt.category + "_read");
			var display = rights[id] != null;
			if (display)
			{
				cat.show();
			}
			if (Ext.get(id + '_read') != null)
			{
				Ext.get(id + '_read').dom.style.display = display ? "" : "none";
			}
		}
	},
	
	/**
	 * Validate a rename modification
	 * @param {Object} e Event
	 * @returns true to validate the modification
	 */
	validateEdit: function(e)
	{
		var record = e.record;
		if (!/^[a-z|A-Z|0-9| |-|_]*$/.test(e.value))
        {
        	Ext.Msg.show ({
                		title: "<i18n:text i18n:key='PLUGINS_CORE_ERROR_DIALOG_TITLE'/>",
                		msg: "<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_NAMING_ERROR'/>",
                		buttons: Ext.Msg.OK,
	   					icon: Ext.MessageBox.ERROR
                	});
            return false;
        }
		return true;
	},
	
	/**
	 * @private
	 * Save a label modification of a profile 
     * @param {Ext.grid.plugin.CellEditing} editor The editor plugin
     * @param {Object} e An edit event with the following properties:
     *
     * - grid - The grid
     * - record - The record that was edited
     * - field - The field name that was edited
     * - value - The value being set
     * - originalValue - The original value for the field, before the edit.
     * - row - The grid table row
     * - column - The grid {@link Ext.grid.column.Column Column} defining the column that was edited.
     * - rowIdx - The row index that was edited
     * - colIdx - The column index that was edited
     * @param {Object} eOpts The options object passed to Ext.util.Observable.addListener.
	 */
	editLabel: function(editor, e, eOpts)
	{
		if (e.record.get('id') == "new")
		{
			// CREER
			var result = Ametys.data.ServerComm.send({
				plugin: this.pluginName, 
				url: "/admisnitrator/rights/profiles/create", 
				parameters: {name: e.record.get('name') }, 
				priority: Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, 
				callback: null, 
				responseType: null
			});
		    if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_NEW_ERROR'/>", result, "Ametys.plugins.core.administration.Logs.editLabel"))
		    {
		       return;
		    }
			else
			{
				e.record.set('id', Ext.dom.Query.selectValue("*/id", result));
				e.record.commit();
			}
		}
		else
		{
			// RENOMMER		    		
			var result = Ametys.data.ServerComm.send({
				plugin: this.pluginName, 
				url: "/administrator/rights/profiles/rename", 
				parameters: { id: e.record.get('id'), name: e.record.get('name') }, 
				priority: Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, 
				callback: null, 
				responseType: null
			});
		    if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_RENAME_ERROR'/>", result, "Ametys.plugins.core.administration.Logs.editLabel"))
		    {
		       return;
		    }
			else 
			{
				var state = Ext.dom.Query.selectValue("*/message", result); 
				if (state != null && state == "missing")
				{
					Ametys.log.ErrorDialog.display({
						title: "<i18n:text i18n:key='PLUGINS_CORE_ERROR_DIALOG_TITLE'/>", 
						text: "<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_RENAME_MISSING_ERROR'/>", 
						details: "State is missing", 
						category: "Ametys.plugins.core.administration.Logs.editLabel"
					});
					this.listview.getStore().remove(e.record);
					return;
				}
			}
			e.record.commit();
		}
		
		this.listview.getStore().sort('name', 'ASC');
	},
	
	/**
	 * @private
	 * Select the edit or read view 
	 */
	selectLayout: function()
	{
		if (this._selectedElmt != null && this._hasChanges && this.item1.pressed)
		{
			Ext.Msg.confirm ("<i18n:text i18n:key='PLUGINS_CORE_SAVE_DIALOG_TITLE'/>", "<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_CONFIRM'/>", Ext.bind(this.saveObjectConfirm, this, [this._selectedElmt, this._getObjects(), this._getRights()], true));
		}
		var rights = this._selectedElmt != null ? this._selectedElmt.get('rights') : {};
		
		// Updating view depending upon selected profile
		if (this.item1.pressed)
		{
			this.cardPanel.getLayout().setActiveItem(0);
			
			for (var i=0; i < RIGHTS_CATEGORY.length; i++)
			{
				Ext.getCmp("cat_" + RIGHTS_CATEGORY[i] + "_read").hide();
			}
			for (var i=0; i < RIGHTS_ID.length; i++)
			{
				// Update read view
				var rightElmt = Ext.getCmp(RIGHTS_ID[i]);
				var id = rightElmt.getName();
				var cat = Ext.getCmp("cat_" + rightElmt.category + "_read");
				var display = rights[id] != null;
				if (display)
				{
					cat.show();
				}
				if (Ext.get(id + '_read') != null)
				{
					Ext.get(id + '_read').dom.style.display = display ? "" : "none";
				}
			}
			this._utilsCategory.setVisible(false);
		}
		else
		{	
			this.cardPanel.getLayout().setActiveItem(1);
			
			for (var i=0; i < RIGHTS_ID.length; i++)
			{
				// Update edit view
				var rightElmt = Ext.getCmp(RIGHTS_ID[i]);
				rightElmt.suspendEvents(false);
				var id = rightElmt.getName();
				rightElmt.setDisabled(this._selectedElmt == null);
				rightElmt.setValue(rights[id] != null);
				rightElmt.resumeEvents();
			}
			this._utilsCategory.setVisible(true);
		}
	},
	
	/**
	 * Entry point to create the main panel
	 * @param {Ext.Container[]} readItems The form items for read panel
	 * @param {Ext.form.field.Field[]} editItems The form items for edit panel
	 */
	createPanel: function(readItems, editItems) 
	{
		// Tabs
		this._Navigation = new Ametys.admin.rightpanel.NavigationPanel ({title: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_CONFIG_MENU'/>"});
		this.item1 = new Ametys.admin.rightpanel.NavigationPanel.NavigationItem ({
			text: "<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILE_READ'/>",
			handlerFn: Ext.bind(this.selectLayout, this),
			activeItem: 0,
			cardLayout: 'profile-card-panel',
			toggleGroup : 'profile-menu',
			pressed: true
		})
		this._Navigation.add(this.item1);
		this.item2 = new Ametys.admin.rightpanel.NavigationPanel.NavigationItem ({
			text: "<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILE_EDIT'/>",
			handlerFn: Ext.bind(this.selectLayout, this),
			activeItem: 1,
			cardLayout: 'profile-card-panel',
			toggleGroup : 'profile-menu'
		}) 
		this._Navigation.add(this.item2);
		
		// Handling profiles
		this._Category = new Ametys.admin.rightpanel.ActionPanel({title: "<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_CATEGORY'/>"});
		this._Category.addAction("<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_CREATE'/>", null, Ametys.getPluginResourcesPrefix('core') + "/img/administrator/profiles/new.png", Ext.bind(this.createProfile, this));
		this._Category.addAction("<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_RENAME'/>", null, Ametys.getPluginResourcesPrefix('core') + "/img/administrator/profiles/rename.png", Ext.bind(this.rename, this));
		this._Category.addAction("<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_VALIDATE'/>", null, Ametys.getPluginResourcesPrefix('core') + "/img/administrator/profiles/validate.png", Ext.bind(this.saveObjects, this, []));
		this._Category.addAction("<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_DELETE'/>", null, Ametys.getPluginResourcesPrefix('core') + "/img/administrator/profiles/delete.png", Ext.bind(this.remove, this));
		this._Category.addAction("<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_QUIT'/>", null, Ametys.getPluginResourcesPrefix('core') + "/img/administrator/profiles/quit.png", Ext.bind(this.goBack, this));
		
		this._Category.hideElt(1);
		this._Category.hideElt(2);
		this._Category.hideElt(3);
		
		// Utils
		this._utilsCategory = new Ametys.admin.rightpanel.ActionPanel({title: "<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_SELECT_HANDLE_CATEGORY'/>"});
		this._utilsCategory.addAction("<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_SELECT_ALL'/>", null, Ametys.getPluginResourcesPrefix('core') + "/img/administrator/profiles/select_all.png", Ext.bind(this.selectAll, this, []));
		this._utilsCategory.addAction("<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_UNSELECT_ALL'/>", null, Ametys.getPluginResourcesPrefix('core') + "/img/administrator/profiles/unselect_all.png", Ext.bind(this.unselectAll, this, []));
		this._utilsCategory.setVisible(false);
		
		// Help
		var helpCategory = new Ametys.admin.rightpanel.TextPanel({title: "<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_HELP_CATEGORY'/>"});
		helpCategory.addText("<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_HELP_HINT'/>");
		
		Ext.define('Ametys.plugins.core.administration.Profiles.Profile', {
		    extend: 'Ext.data.Model',
		    fields: [
			           {name: 'id'},
			           {name: 'name', sortType: Ext.data.SortTypes.asUCString},
			           {name: 'rights'}
		    ]
		});
		
		this.listview = new Ext.grid.Panel({
			region: 'north',
			
			id: 'profile-list-view',
			allowDeselect: true,
			
			height: 150,
			minSize: 75,
			maxSize: 310,
			
			title : "<i18n:text i18n:key='PLUGINS_CORE_RIGHTS_PROFILES_LIST'/>",
			hideHeaders: true,
			border: false,
			autoScroll: true,
			
		    store: Ext.create('Ext.data.Store', {
				model: 'Ametys.plugins.core.administration.Profiles.Profile',
		        data: { profiles: profileData},
		        
		        sortOnLoad: true,
		        sorters: [ { property: 'name', direction: "ASC" } ],
		        
		    	proxy: {
		        	type: 'memory',
		        	reader: {
		        		type: 'json',
		        		root: 'profiles'
		        	}
		        }
			}),  
			
			selType: 'cellmodel',
			plugins: [
			          Ext.create('Ext.grid.plugin.CellEditing', {  })
			],
			
		    columns: [
				        {dataIndex: 'name', flex: 1, width: 1, editor: { xtype: 'textfield', allowBlank: false }}
		    ],
			
			listeners: {
				'select': Ext.bind(this.onSelectProfile, this), 
				'edit': Ext.bind(this.editLabel, this),
				'validateedit': Ext.bind(this.validateEdit, this)						
		    }
		});	
		
		//Panel for edit rights profils
		var editRights = new Ext.Panel({
			title: '<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_RIGHTS"/>',
			autoScroll:true,
			border: false,
			border: false,
			id: 'profile-edit-panel',
			items: editItems
		});  
		//Panel for read rights profils
		var readRights = new Ext.Panel({ 
			title: '<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_RIGHTS"/>',
			border: false,
			autoScroll:true,
			id: 'profile-read-panel',
			items: readItems
		});
		this.cardPanel = new Ext.Panel ({
			id:'profile-card-panel',
			cls: 'transparent-panel',
			region:'center',
			border: false,
			autoScroll: true,
			layout:'card',
			activeItem:0,
			minSize: 75,
			maxSize: 500,
			items:[readRights, editRights]
		});

		var rightPanel = new Ext.Container({
			region:'east',
			border: false,
			width: 277,
			baseCls: 'admin-right-panel',
		    items: [this._Navigation, this._Category, this._utilsCategory, helpCategory]
		});
		
		var centerPanel = new Ext.Panel({
			defaults: {
			    split: true
			},
			region:'center',
			cls: 'transparent-panel',
			border: false,
			layout: 'border',
			autoScroll : false,
			height: 'auto',
			items: [this.listview, this.cardPanel]
		});
		
	
		return new Ext.Panel({
			region: 'center',
			cls: 'transparent-panel',
			border: false,
			layout: 'border',
			items: [centerPanel, rightPanel]
		});
	}
});
	            			
/*
 * Element that displays a right in the bottom section
 */
Ext.define('Ametys.plugins.core.administration.Profiles.RightEntry', {
    extend: 'Ext.Container',

    cls: 'right-entry',
    
    /**
     * Set the text for the entry. The text will be automatically cut with '...' if it is too long.
     * @param {String} text The new text 
     * @param {Number} maxWidth The max size to fit in 
     */
    setText: function (text, maxWidth)
    {
    	if(this.el)
        {
        	var textWidth = Ext.util.TextMetrics.measure(this.el, this.text).width;
        	
        	var i = 0;
        	while (textWidth > maxWidth)
        	{
        		i++;
        		text = Ext.util.Format.ellipsis(this.text, this.text.length - i);
        		textWidth = Ext.util.TextMetrics.measure(this.el, text).width; 
        	}
        }
        this.text = text;
    },

	/**
	 * @private
	 * override to add the help 
	 */
	onRender: function(ct, position)
	{
		this.callParent(arguments);
		
		// Help icon
		if (this.description)
		{
			var img = this.getEl().createChild({
				id: this.id + '-img',
				cls: 'right-entry-img',
				tag:'img',
				src: Ametys.getPluginResourcesPrefix('core') + '/img/administrator/config/help.gif'});
			
			Ext.tip.QuickTipManager.register({
				cls: 'profiles-right-tooltip',
				target: this.id + '-img',
			    title: this.text,
			    text: this.description
			});
		}
		
		this.setText(this.text, this.width - 5 - 21);
		
		var divText = this.getEl().createChild({
			html: this.text,
			cls: 'right-entry-text'
		});
	}
});

/**
 * Element that displays a right in the bottom section with a checkbox
 * @private
 */
Ext.define('Ametys.plugins.core.administration.Profiles.CheckRightEntry', {
    extend: 'Ext.form.field.Checkbox',

    cls: 'check-right-entry',
    
	/**
	 * @private
	 * override to add the help 
	 */
	onRender: function(ct, position)
	{
    	this.callParent(arguments);
    	
    	// Help icon
    	if (this.description)
    	{
    		this.bodyEl.insertFirst({
    			id: this.id + '-img',
    			tag:'img',
    			src: Ametys.getPluginResourcesPrefix('core') + '/img/administrator/config/help.gif',
    			cls: 'check-right-entry-img'
    		});
    		
    		Ext.tip.QuickTipManager.register({
				cls: 'profiles-right-tooltip',
				target: this.id + '-img',
			    title: this.boxLabel,
			    text: this.description
			});
    	}
    	
    	var labelNode;
    	var nodes = this.bodyEl.dom.childNodes;
		for (var i=0; i < nodes.length; i++)
		{
			if (nodes[i].tagName.toLowerCase() == 'label')
			{
				labelNode = nodes[i];
			}
		}
    	
    	var maxWidth = this.width - 10 - 21 - 13; //padding + image + input
    	var textWidth = Ext.util.TextMetrics.measure(labelNode, this.boxLabel).width;
    	var t = this.boxLabel;
    	
    	var i = 0;
    	while (textWidth > maxWidth)
    	{
    		i++;
    		t = Ext.util.Format.ellipsis(this.boxLabel, this.boxLabel.length - i);
    		textWidth = Ext.util.TextMetrics.measure(labelNode, t).width; 
    	}
    	this.boxLabel = t;
    	labelNode.innerHTML = t;
	}
});
