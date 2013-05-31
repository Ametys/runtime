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
 * Class in chage of the plugins administration screen. See {@link #initialize} and {@link #createPanel}
 */
Ext.define('Ametys.plugins.core.administration.Plugins', {
	singleton: true,

	/**
	 * @private
	 * @property {String} pluginName The name of the plugin handling ajax request
	 */
	/**
	 * @private
	 * @property {Ametys.workspace.admin.rightpanel.NavigationPanel} _nav The navigation panel
	 */
	/**
	 * @private
	 * @property {Ametys.workspace.admin.rightpanel.ActionPanel} _actions The action panel
	 */
	/**
	 * @private
	 * @property {Ext.panel.Panel} _mainPanel The mail left panel (holding the different trees through a card layout)
	 */
	/**
	 * @private
	 * @property {Ext.Container} _contextualPanel The main right panel with contextual stuffs
	 */
	/**
	 * @private
	 * @property {Ext.tree.Panel} _tree The plugin tree, viewed as files
	 */
	/**
	 * @private
	 * @property {Ext.tree.Panel} _tree2 The plugin tree, viewed as extension points
	 */
	/**
	 * @private
	 * @property {Ext.tree.Panel} _tree3 The workspace tree
	 */
	/**
	 * @private
	 * @property {Ext.LoadMask} _mask A mak when saving changes and reloading
	 */
	
	/**
	 * @readonly
	 * Constant for actions : select action
	 */
	_ACTION_SELECT: 0,
	/**
	 * @readonly
	 * Constant for actions : activate action
	 */
	_ACTION_ACTIVATE: 1,
	/**
	 * @readonly
	 * Constant for actions : deactivate action
	 */
	_ACTION_DEACTIVATE: 2,
	/**
	 * @readonly
	 * Constant for actions : changes action
	 */
	_ACTION_CHANGES: 3,
	
	/**
	 * @private
	 * @property {Boolean} _changes When 'true' there is at least a change to confirm
	 */
	_changes: false,
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
	 * Initialize the elements
	 * @param {String} pluginName The name of the plugin to use for ajax request...
	 */
	initialize: function (pluginName)
	{
		this.pluginName = pluginName;
	},

	/**
	 * Create the screen
	 * @param {Object} rootNode1 The root node conf for _tree (plugins by file)
	 * @param {Object} rootNode2 The root node conf for _tree2 (plugins by extension)
	 * @param {Object} rootNode3 The root node conf for _tree3 (workspaces)
	 * @return {Ext.panel.Panel} The created panel
	 */
	createPanel: function (rootNode1, rootNode2, rootNode3)
	{
		this._contextualPanel = new Ext.Container({
			region:'east',
			
			border: false,
			cls: 'admin-right-panel',
			width: 277,
		    
			items: [this._drawNavigationPanel(),
			        this._drawActionPanel (), 
			        this._drawHelpPanel ()]
		});
		
		// Defining trees
		var model = Ext.define('Ametys.plugins.core.administration.Plugins.Items', { 
		    extend: 'Ext.data.Model', 
		    fields: [ 
		        { name: 'icon', type: 'string' }, 
		        { name: 'text', type: 'string' }, 
		        { name: 'type', type: 'string' }, // can be 'plugin', 'extension', 'feature', 'component', 'extension-point'
		        { name: 'active', type: 'boolean' }, // true if its an active feature
		        { name: 'cause', type: 'string' }, // if inactive, the cause of inactivation
		        { name: 'isMultiple', type: 'string' }, // is it a multiple extension point 
		        { name: 'pluginName', type: 'string' }, // the name of the plugin bringing the element
		        { name: 'featureName', type: 'string' }, // the name of the feature bringing the lement
		        { name: 'componentName', type: 'string' }, // the name of the component
		        { name: 'extensionPointName', type: 'string'}, // the extension point if
		        { name: 'extensionId', type: 'string'}, // the extension if
		        { name: 'leaf', type: 'boolean'} 
		    ] 
		}); 

		
		this._tree = new Ext.tree.TreePanel({
			autoScroll: true,
			listeners: {'show': Ext.bind(this._computeShowHide, this), 'hide': Ext.bind(this._computeShowHide, this)},
			store: Ext.create('Ext.data.TreeStore', {
				root: rootNode1,
				model: model
			})
		});
		this._tree.getSelectionModel().addListener("selectionchange", this._computeShowHide, this);

		
		this._tree2 = new Ext.tree.TreePanel({
			autoScroll: true,
			listeners: {'show': Ext.bind(this._computeShowHide, this), 'hide': Ext.bind(this._computeShowHide, this)},
			store: Ext.create('Ext.data.TreeStore', {
				root: rootNode2,
				model: model
			})
		});
		this._tree2.getSelectionModel().addListener("selectionchange", this._computeShowHide, this);

		
		this._tree3 = new Ext.tree.TreePanel({
			autoScroll: true,
			store: Ext.create('Ext.data.TreeStore', {
				root: rootNode3,
				model: model
			})
		});

		
		this._mainPanel = new Ext.Panel({
			region:'center',
			id:'plugin-card-panel',
			baseCls: 'transparent-panel',
			border: false,
			autoScroll : true,
			height: 'auto',
			layout: 'card',
			activeItem: 1,
			
			items: [this._tree, this._tree2, this._tree3]
		});		
		
		return new Ext.Panel({
			region: 'center',
			
			baseCls: 'transparent-panel',
			border: false,
			layout: 'border',
			autoScroll: true,
			
			items: [this._mainPanel, 
			        this._contextualPanel]
		});
	},
	
	/**
	 * @private
	 * Creates the navigation panel
	 * @returns {Ametys.workspace.admin.rightpanel.NavigationPanel} The created navigation panel
	 */
	_drawNavigationPanel: function ()
	{
		this._nav = new Ametys.workspace.admin.rightpanel.NavigationPanel ({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_MENU"/>"});
		
		this._nav.add(new Ametys.workspace.admin.rightpanel.NavigationPanel.NavigationItem ({
				text: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_PLUGIN_VIEW"/>",
				activeItem: 0,
				cardLayout: 'plugin-card-panel',
				toggleGroup : 'plugin-menu',
			})
		);
		this._nav.add(new Ametys.workspace.admin.rightpanel.NavigationPanel.NavigationItem ({
				text: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_EXTENSION_POINT_VIEW"/>",
				activeItem: 1,
				cardLayout: 'plugin-card-panel',
				toggleGroup : 'plugin-menu',
				pressed: true
			})
		);
		this._nav.add(new Ametys.workspace.admin.rightpanel.NavigationPanel.NavigationItem ({
				text: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_WORKSPACE_VIEW"/>",
				activeItem: 2,
				cardLayout: 'plugin-card-panel',
				toggleGroup : 'plugin-menu'
			})
		);
		
		return this._nav;
	},
	
	/**
	 * @private
	 * Creates the action panel
	 * @returns {Ametys.workspace.admin.rightpanel.ActionPanel} The created panel
	 */
	_drawActionPanel: function ()
	{
		this._actions = new Ametys.workspace.admin.rightpanel.ActionPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_HANDLE"/>"});
		
		// Choose
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHOOSE"/>", 
				 null,
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/plugins/select.png', 
				 Ext.bind(this.select, this));

		// Activate
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_ACTIVATE"/>", 
				 null,
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/plugins/activate.png', 
				 Ext.bind(this.activate, this));

		// Deactivate
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_DEACTIVATE"/>", 
				 null,
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/plugins/deactivate.png', 
				 Ext.bind(this.deactivate, this));

		// Changes
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES"/>", 
				 null,
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/plugins/changes.png', 
				 Ext.bind(this.changes, this));
		
		// Documentation
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_DOC"/>", 
				 null,
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/plugins/doc.png', 
				 Ext.bind(this.openDoc, this));
		
		// Quit
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CANCEL"/>", 
				 null,
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/plugins/quit.png', 
				 Ext.bind(this.goBack, this));
		
		this._actions.hideElt(this._ACTION_SELECT);
		this._actions.hideElt(this._ACTION_ACTIVATE);
		this._actions.hideElt(this._ACTION_DEACTIVATE);
		this._actions.hideElt(this._ACTION_CHANGES);
		
		return this._actions;
	},
	
	/**
	 * @private
	 * Creates the help panel
	 * @return {Ametys.workspace.admin.rightpanel.TextPanel} The panel
	 */
	_drawHelpPanel: function ()
	{
		var helpPanel = new Ametys.workspace.admin.rightpanel.TextPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_HELP"/>"});
		helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_HELP_TEXT"/>");
		
		return helpPanel;
	},
	
	/**
	 * @private
	 * Compute for all actions if they are available or not (when tree or tree2 is visible)
	 */
	_computeShowHide: function()
	{
		var node = null;
		if (this._mainPanel.layout.activeItem == this._tree)
		{
			node = this._tree.getSelectionModel().getSelection()[0];
		}
		else if (this._mainPanel.layout.activeItem == this._tree2)
		{
			node = this._tree2.getSelectionModel().getSelection()[0];
		}
		
		if (node != null && node.get('type') == "extension" && node.parentNode.get('isMultiple') == "false")
		{
			this._actions.showElt(this._ACTION_SELECT);
		}
		else
		{
			this._actions.hideElt(this._ACTION_SELECT);
		}
		
		if (node != null && node.get('type') == "feature" && node.get('active'))
		{
			this._actions.showElt(this._ACTION_DEACTIVATE);
		}
		else
		{
			this._actions.hideElt(this._ACTION_DEACTIVATE);
		}

		if (node != null && node.get('type') == "feature" && !node.get('active') && node.get('cause') == "EXCLUDED")
		{
			this._actions.showElt(this._ACTION_ACTIVATE);
		}
		else
		{
			this._actions.hideElt(this._ACTION_ACTIVATE);
		}
	},
	
	/**
	 * Open the plugin doc of the active item
	 * @private
	 */
	openDoc: function()
	{
		var zoom = "";
		var node = null;
		if (this._mainPanel.layout.activeItem == this._tree)
		{
			node = this._tree.getSelectionModel().getSelection()[0];
		}
		else if (this._mainPanel.layout.activeItem == this._tree2)
		{
			node = this._tree2.getSelectionModel().getSelection()[0];
		}
		
		if (node != null)
		{
			if (node.get('type') == "plugin")
			{
				zoom = node.get('pluginName') + "_main.html";
			}
			else if (node.get('type') == "feature")
			{
				zoom = node.get('pluginName') + "_features.html%23feature_" + node.get('featureName');
			}
			else if (node.get('type') == "component")
			{
				zoom = node.get('pluginName') + "_features.html%23feature_" + node.get('featureName') + "_component_" + node.get('componentName');
			}
			else if (node.get('type') == "extension-point")
			{
				zoom = node.get('pluginName') + "_extensions.html%23extension_point_" + node.get('extensionPointName');
			}
			else if (node.get('type') == "extension")
			{
				zoom = node.get('pluginName') + "_features.html%23feature_" + node.get('featureName') + "_extension_" + node.get('extensionId');
			}
		}
		
		window.open(Ametys.getPluginDirectPrefix(this.pluginName) + "/administrator/plugins/doc/index.html?zoom=" + zoom, "plugindoc", "");
	},
	

	/**
	 * Select this single extension
	 * @private
	 */
	select: function ()
	{
		var node = null;
		if (this._mainPanel.layout.activeItem == this._tree)
		{
			node = this._tree.getSelectionModel().getSelection()[0];
		}
		else if (this._mainPanel.layout.activeItem == this._tree2)
		{
			node = this._tree2.getSelectionModel().getSelection()[0];
		}
		
		if (node != null)
		{
			this._SEP[node.parentNode.get('extensionPointName')] = node.get('extensionId');
			this._actions.showElt(this._ACTION_CHANGES);
			
			Ext.MessageBox.alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_ALERT_TITLE"/>", "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_ALERT_TEXT"/>");
			this._changes = true;
		}
	},

	/**
	 * Activate a feature
	 * @param {Boolean} status True to active, false to unactivate. True is the default value.
	 * @private
	 */
	activate: function (status)
	{
		status = (status != false) ? true : false;
		
		var node = null;
		if (this._mainPanel.layout.activeItem == this._tree)
		{
			node = this._tree.getSelectionModel().getSelection()[0];
		}
		else if (this._mainPanel.layout.activeItem == this._tree2)
		{
			node = this._tree2.getSelectionModel().getSelection()[0];
		}
		
		if (node != null)
		{
			this._EP[node.get('pluginName') + "/" + node.get('featureName')] = status;
			this._actions.showElt(this._ACTION_CHANGES);
			
			Ext.MessageBox.alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_ALERT_TITLE"/>", "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_ALERT_TEXT"/>");
		}
	},

	/**
	 * Deactivate a feature
	 * @private
	 */
	deactivate: function ()
	{
		this.activate(false);
	},

	/**
	 * Display the changes
	 * @private
	 */
	changes: function ()
	{
		Ext.MessageBox.alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_BEWARE_TITLE"/>", 
				"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_BEWARE_TEXT"/>",
				Ext.bind(function ()
				{
					var sepchanges = "";
					for (var i in this._SEP)
					{
						sepchanges += i + " : " + this._SEP[i] + "<br/>";
					}
					var epchanges = "";
					for (var i in this._EP)
					{
						epchanges += i + " : " + (this._EP[i] ? "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_ACTIVATE"/>" : "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_DEACTIVATE"/>") + "<br/>";
					}
					
					
					var changes = "";
					if (sepchanges != "")
					{
						changes += "<b><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_SEP"/></b><br/>"
								   + sepchanges;
					}
					if (epchanges != "")
					{
						changes += "<b><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_EP"/></b><br/>"
								   + epchanges;
					}
					
					Ext.MessageBox.confirm("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_CONFIRM_TITLE"/>", 
							"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_CONFIRM_TEXT_START"/><ul>"
							+ changes
							+ "</ul><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_CONFIRM_TEXT_END"/>", 
							function(val) { if (val == "yes") { this._changesNow(); } }, this);
				}, this));
	},
	
	/**
	 * Operates the pending changes
	 * @private
	 */
	_changesNow: function()
	{
		var params = {};
		params.EP = this._EP;
		params.SEP = this._SEP;
		
		Ametys.data.ServerComm.send({
			plugin: this.pluginName, 
			url: "administrator/plugins/change", 
			parameters: params, 
			priority: Ametys.data.ServerComm.PRIORITY_MAJOR, 
			callback: {
				handler: this._changesNowCB,
				scope: this,
				arguments: null
			},
			responseType: null
		});
		
		this._mask =  new Ext.LoadMask({target: Ext.getBody()});
		this._mask.show();
	},

	/**
	 * @private
	 * Call back for the server message of the {@link #_changesNow} method
	 * @param {Object} response The xmlhttpresponse
	 */
	_changesNowCB: function(response)
	{
		this._mask.hide();

	    if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_ERROR"/>", response, "Ametys.plugins.core.administration.Plugins._changesNowCB"))
	    {
	       return;
	    }

	    alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_DONE"/>");

		this._mask =  new Ext.LoadMask({target: Ext.getBody()});
		this._mask.show();
	    
	    // Restart
	    Ext.Ajax.request({url: Ametys.getPluginDirectPrefix(this.pluginName) + "/administrator/restart", params: "", async: false});
	    
	    document.location.reload(true);
	},

	/**
	 * Back to the adminsitration home page
	 * @private
	 */
	goBack: function ()
	{
	    document.location.href = Ametys.WORKSPACE_URI;
	}   
});
