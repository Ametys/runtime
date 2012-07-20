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
	 * @property {Ametys.workspace.admin.rightpanel.NavigationPanel} _Navigation The navigation panel between read/edit mode
	 */
	/**
	 * @private
	 * @property {Ametys.workspace.admin.rightpanel.NavigationPanel.NavigationItem} item1 The read item for navigation
	 */
	/**
	 * @private
	 * @property {Ametys.workspace.admin.rightpanel.NavigationPanel.NavigationItem} item2 The edit item for navigation
	 */
	/**
	 * @private
	 * @property {Ametys.workspace.admin.rightpanel.ActionPanel} _utilsCategory The action panel for handling selectall unselectall
	 */
	/**
	 * @private
	 * @property {Ametys.workspace.admin.rightpanel.ActionPanel} _Category The action panel for handling profiles
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
		var newEntry = new record({
								'name': "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_NEWPROFILE"/>",
								'id': "new",
                                'rights': {}
								});
        
        var result = Ametys.data.ServerComm.send(this.pluginName, "/administrator/rights/profiles/create", {name: newEntry.get('name') }, Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
        if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_NEW_ERROR"/>", result, "Ametys.plugins.core.administration.Logs.createProfile"))
        {
            return;
        }
        else
        {
            newEntry.set('id', Ext.dom.Query.selectValue("*/id", result));
            newEntry.commit();
        }
        
		this.listview.getStore().addSorted([newEntry]);
        
		if(this.listview.getStore().getCount() &gt; 0)
		{
			this.listview.getSelectionModel().select(this.listview.getStore().getCount() -1, 0);
		}
		else
		{
			this.listview.getSelectionModel().select(0, 0);
		}
        selectedElmt = newEntry;
		
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
		Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_DELETE"/>", "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_DELETE_CONFIRM"/>", Ext.bind(this.removeConfirm, this));
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
				this.listview.removeElement(elt);
			}
			else
			{
				Ext.Msg.show ({
            		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
            		msg: "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_ERROR"/>",
            		buttons: Ext.Msg.OK,
   					icon: Ext.MessageBox.ERROR
            	});
			}
		}
	},
	
	/**
	 * Save the current modifications. 
	 * @param {String} button If 'yes' do save the modification, else discard it.
	 * @param {Ext.data.Model} selectedElmt The element to save. Can be null.
	 */
    saveObjectConfirm: function (button, selectedElmt)
	{
		if (button == 'yes')
		{
			this.saveObjects(selectedElmt);
		}
		else
		{
			this._hasChanges = false;
		}
	},

	/**
	 * Save the given profile to the server
	 * @params {Ext.data.Model} element The element to save. Can be null to save the currently selected profile.
	 */
    saveObjects: function(element)
    {
		if (element == null)
		{
			element = this.listview.getSelectionModel().getSelection()[0];
		}
						
		// Update node and list rights to send
		var objects = [];
		
		var newRights = {};
		for (var i=0; i &lt; RIGHTS_ID.length; i++)
		{
			var rightElmt = Ext.getCmp(RIGHTS_ID[i]);
			if (rightElmt.getValue())
			{
				newRights[rightElmt.getName()] = "";
				objects.push(rightElmt.getName());
			}
		}
		element.set('rights', newRights);
	
		// Send this
		var ok = false;
		while (!ok)
		{
			var result = Ametys.data.ServerComm.send(this.pluginName, "/administrator/rights/profiles/modify", { id: element.get('id'), objects: objects }, Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
		    if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_ERROR"/>", result, "Ametys.plugins.core.administration.Logs.saveObjects"))
		    {
				return;
		    }
		    else
			{
				var state = Ext.dom.Query("*/message", result); 
				if (state != null &amp;&amp; state == "missing")
				{
					Ext.Msg.show ({
                		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
                		msg: "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_MISSING_ERROR"/>",
                		buttons: Ext.Msg.OK,
	   					icon: Ext.MessageBox.ERROR
                	});
					this.listview.removeElement(element);
				}
				else
				{
					ok = true;
					this._Category.hideElt(2);
				}
			}
			
		}
		this._hasChanges = false;
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
		if (this._selectedElmt != null &amp;&amp; this._hasChanges)
		{
			if (confirm("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_CONFIRM"/>"))
			{
				this.saveObjectConfirm ('yes', this._selectedElmt);
			}
		}
	},
	
	/**
	 * @private
	 * Select all rights
	 * @param {String} id The id of the html element surrounding the elements to check. Can be null to check all.
	 */
	selectAll: function(id, value)
	{
		value = value || true;
		
		if (id != null)
		{
			var fd = Ext.getCmp(id);
			fd.items.each (function (item) {item.setValue(value)});
		}
		else
		{
			for (var i=0; i &lt; RIGHTS_ID.length; i++)
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
		if (this.selectedElmt != null &amp;&amp; this.hasChanges)
		{
			Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_SAVE_DIALOG_TITLE"/>", "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_CONFIRM"/>", Ext.bind(this.saveObjectConfirm, this, this.selectedElmt, true));
		}
		
		this._Category.showElt(1);
		this._Category.hideElt(2);
		this._Category.showElt(3);
		
		var elmt = this.listview.getSelection()[0];
		this.selectedElmt = elmt; 
		var rights = elmt.get('rights') || {};
		
		for (var i=0; i &lt; RIGHTS_CATEGORY.length; i++)
		{
			Ext.getCmp("cat_" + RIGHTS_CATEGORY[i] + "_read").hide();
		}
		
		for (var i=0; i &lt; RIGHTS_ID.length; i++)
		{
			// Update edition screen
			var rightElmt = Ext.getCmp(RIGHTS_ID[i]);
			rightElmt.removeListener('check', this.needSave);
			var id = rightElmt.getName();
			rightElmt.setDisabled(false);
			rightElmt.setValue(rights[id] != null);
			rightElmt.addListener('check', Ext.bind(this.needSave, this));
			
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
                		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
                		msg: "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_NAMING_ERROR"/>",
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
	 * @param {Ext.data.Store} store
     * @param {Ext.data.Model} record The Model instance that was updated
     * @param {String} operation The update operation being performed. Value may be one of:
     *
     *     Ext.data.Model.EDIT
     *     Ext.data.Model.REJECT
     *     Ext.data.Model.COMMIT
     * @param {String[]} modifiedFieldNames Array of field names changed during edit.
     * @param {Object} eOpts The options object passed to Ext.util.Observable.addListener.
	 */
	editLabel: function(store, record, operation, modifiedFieldNames, eOpts)
	{
		if (operation == Ext.data.Record.EDIT)
		{
			if (record.get('id') == "new")
			{
			
				// CREER
				var result = Ametys.data.ServerComm.send(this.pluginName, "/admisnitrator/rights/profiles/create", {name: record.get('name') }, Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
			    if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_NEW_ERROR"/>", result, "Ametys.plugins.core.administration.Logs.editLabel"))
			    {
			       return;
			    }
				else
				{
					record.set('id', Ext.dom.Query.selectValue("*/id", result));
					record.commit();
				}
			}
			else
			{
				// RENOMMER
				var result = Ametys.data.ServerComm.send(this.pluginName, "/administrator/rights/profiles/rename", { id: record.get('id'), name: record.get('name') }, Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
			    if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_RENAME_ERROR"/>", result, "Ametys.plugins.core.administration.Logs.editLabel"))
			    {
			       return;
			    }
				else 
				{
					var state = Ext.dom.Query("*/message", result); 
					if (state != null &amp;&amp; state == "missing")
					{
						Ametys.log.ErrorDialog.display("<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>", "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_RENAME_MISSING_ERROR"/>", "State is missing", "Ametys.plugins.core.administration.Logs.editLabel");
						this.listview.removeElement(record);
						return;
					}
				}
				record.commit();
			}
		}
	},
	
	/**
	 * @private
	 * Select the edit or read view 
	 */
	selectLayout: function()
	{
		if (this.selectedElmt != null &amp;&amp; this.hasChanges &amp;&amp; this.item1.pressed)
		{
			Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_SAVE_DIALOG_TITLE"/>", "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_MODIFY_CONFIRM"/>", Ext.bind(this.saveObjectConfirm, this, this.selectedElmt, true));
		}
		var rights = this.selectedElmt != null ? this.selectedElmt.get('rights') : {};
		
		// Updating view depending upon selected profile
		if (this.item1.pressed)
		{
			this.cardPanel.getLayout().setActiveItem(0);
			
			for (var i=0; i &lt; RIGHTS_CATEGORY.length; i++)
			{
				Ext.getCmp("cat_" + RIGHTS_CATEGORY[i] + "_read").hide();
			}
			for (var i=0; i &lt; RIGHTS_ID.length; i++)
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
			
			for (var i=0; i &lt; RIGHTS_ID.length; i++)
			{
				// Update edit view
				var rightElmt = Ext.getCmp(RIGHTS_ID[i]);
				rightElmt.removeListener('check', needSave);
				var id = rightElmt.getName();
				rightElmt.setDisabled(selectedElmt == null);
				rightElmt.setValue(rights[id] != null);
				rightElmt.addListener('check', needSave);
			}
			this._utilsCategory.setVisible(true);
		}
	},
	
	/**
	 * Entry point to create the main panel
	 */
	createPanel: function() 
	{
		// Tabs
		this._Navigation = new Ametys.workspace.admin.rightpanel.NavigationPanel ({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_MENU"/>"});
		this.item1 = new Ametys.workspace.admin.rightpanel.NavigationPanel.NavigationItem ({
			text: "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILE_READ"/>",
			handlerFn: Ext.bind(this.selectLayout, this),
			activeItem: 0,
			cardLayout: 'profile-card-panel',
			toggleGroup : 'profile-menu',
			pressed: true
		})
		this._Navigation.add(this.item1);
		this.item2 = new Ametys.workspace.admin.rightpanel.NavigationPanel.NavigationItem ({
			text: "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILE_EDIT"/>",
			handlerFn: Ext.bind(this.selectLayout, this),
			activeItem: 1,
			cardLayout: 'profile-card-panel',
			toggleGroup : 'profile-menu'
		}) 
		this._Navigation.add(this.item2);
		
		// Handling profiles
		this._Category = new Ametys.workspace.admin.rightpanel.ActionPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_CATEGORY"/>'});
		this._Category.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_CREATE"/>", Ametys.getPluginResourcesPrefix('core') + "/img/administrator/profiles/new.png", Ext.bind(this.createProfile, this));
		this._Category.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_RENAME"/>", Ametys.getPluginResourcesPrefix('core') + "/img/administrator/profiles/rename.png", Ext.bind(this.rename, this));
		this._Category.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_VALIDATE"/>", Ametys.getPluginResourcesPrefix('core') + "/img/administrator/profiles/validate.png", Ext.bind(this.saveObjects, this));
		this._Category.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_DELETE"/>", Ametys.getPluginResourcesPrefix('core') + "/img/administrator/profiles/delete.png", Ext.bind(this.menuRemove, this));
		this._Category.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_QUIT"/>", Ametys.getPluginResourcesPrefix('core') + "/img/administrator/profiles/quit.png", Ext.bind(this.goBack, this));
		
		this._Category.hideElt(1);
		this._Category.hideElt(2);
		this._Category.hideElt(3);
		
		// Utils
		this._utilsCategory = new Ametys.workspace.admin.rightpanel.ActionPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_SELECT_HANDLE_CATEGORY"/>'});
		this._utilsCategory.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_SELECT_ALL"/>", Ametys.getPluginResourcesPrefix('core') + "/img/administrator/profiles/select_all.png", Ext.bind(this.selectAll, this));
		this._utilsCategory.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_UNSELECT_ALL"/>", Ametys.getPluginResourcesPrefix('core') + "/img/administrator/profiles/unselect_all.png", Ext.bind(this.unselectAll, this));
		this._utilsCategory.setVisible(false);
		
		// Help
		var helpCategory = new Ametys.workspace.admin.rightpanel.TextPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HELP_CATEGORY"/>'});
		helpCategory.addText("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_HANDLE_HELP_HINT"/>");
		
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
			
			title : "<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_LIST"/>",
			hideHeaders: true,
			border: false,
			
		    store: Ext.create('Ext.data.Store', {
				model: 'Ametys.plugins.core.administration.Profiles.Profile',
		        data: { profiles: profileData},
		        
		        sortOnLoad: true,
		        sorters: [ { property: 'name', direction: "ASC" } ],
		        
		    	listeners: {'update': Ext.bind(this.editLabel, this)},

		    	proxy: {
		        	type: 'memory',
		        	reader: {
		        		type: 'json',
		        		root: 'groups'
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
				'select': Ext.bind(this.onSelectProfil, true), 
				'validateedit': Ext.bind(this.validateEdit, true)						
		    }
		});	
		
		//Panel for edit rights profils
		var editRights = new Ext.Panel({
			title: '<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_RIGHTS"/>',
			autoScroll:true,
			border: false,
			border: false,
			id: 'profile-edit-panel'
		});  
		//Panel for read rights profils
		var readRights = new Ext.Panel({ 
			title: '<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_PROFILES_RIGHTS"/>',
			border: false,
			autoScroll:true,
			id: 'profile-read-panel'
		});
		this.cardPanel = new Ext.Panel ({
			id:'profile-card-panel',
			baseCls: 'transparent-panel',
			region:'center',
			border: false,
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
			baseCls: 'transparent-panel',
			border: false,
			layout: 'border',
			autoScroll : true,
			height: 'auto',
			items: [this.listview, this.cardPanel]
		});
		
	
		return new Ext.Panel({
			region: 'center',
			baseCls: 'transparent-panel',
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
     */
    setText: function (text, width)
    {
    	if(this.el)
        {
    		var textMesurer = Ext.util.TextMetrics.createInstance(this.el);
    		var textWidth = textMesurer.getWidth(text); // Taille en pixel
    		var nbCars = text.length; // Nombre de caractères
    		var carWidth = Math.floor(textWidth / nbCars); //Taille moyenne d'un caractères
    		if (textWidth &gt; (width || this.width))
    		{
    			text = text.substring(0, Math.floor(maxWidth /carWidth) - 3) + "...";
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
				src: getPluginResourcesUrl('core') + '/img/administrator/config/help.gif'});
			var tooltip = new Ext.ToolTip({
		        target: this.id + '-img',
		        html: this.description
		    });
		}
		
		this.setText(this.text, this.width - 5 - 20);
		
		var divText = this.getEl().createChild({
			html: this.text,
			cls: 'right-entry-text'
		});
	}
});

/**
 * Element that displays a right in the bottom section with a checkbox
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
    		this.el.parent().insertFirst({
    			id: this.id + '-img',
    			tag:'img',
    			src: Ametys.getPluginResourcesPrefix('core') + '/img/administrator/config/help.gif',
    			cls: 'check-right-entry-img'
    		});
    		var tooltip = new Ext.ToolTip({
    	        target: this.id + '-img',
    	        html: this.description
    	    });
    	}
    	
    	var maxWidth = this.width - 0; //padding + image
    	var textMesurer = Ext.util.TextMetrics.createInstance(this.el);
    	var textWidth = textMesurer.getWidth(this.boxLabel); // Taille en pixel
    	var nbCars = this.boxLabel.length; // Nombre de caractères
    	var carWidth = Math.floor(textWidth / nbCars);//Taille moyenne d'un caractères
    	if (textWidth > maxWidth)
    	{
    		this.boxLabel = this.boxLabel.substring(0, (Math.floor(maxWidth /carWidth) - 3*carWidth)) + "...";
    		if (this.wrap)
    		{
    			var nodes = this.wrap.dom.childNodes;
    			for (var i=0; i &lt; nodes.length; i++)
    			{
    				if (nodes[i].tagName.toLowerCase() == 'label')
    				{
    					nodes[i].innerHTML = this.boxLabel;
    				}
    			}
    		}
    	}
	}
});
