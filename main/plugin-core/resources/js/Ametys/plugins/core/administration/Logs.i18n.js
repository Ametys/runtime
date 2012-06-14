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
 * Class in chage of the log administration screen. See {@link #initialize} and {@link @createPanel}
 */
Ext.define('Ametys.plugins.core.administration.Logs', {
	singleton: true,
	
	/**
	 * @private
	 * @property {Number} LOGS_VIEW_VIEW Index of the action VIEW in the VIEW panel
	 */
	LOGS_VIEW_VIEW: 0,
	/**
	 * @private
	 * @property {Number} LOGS_VIEW_DOWNLOAD Index of the action DOWNLOAD in the VIEW panel
	 */
	LOGS_VIEW_DOWNLOAD: 1,
	/**
	 * @private
	 * @property {Number} LOGS_VIEW_DELETE Index of the action DELETE in the VIEW panel
	 */
	LOGS_VIEW_DELETE: 2,
	/**
	 * @private
	 * @property {Number} LOGS_VIEW_PURGE Index of the action PURGE in the VIEW panel
	 */
	LOGS_VIEW_PURGE: 3,
	
	/**
	 * @private
	 * @property {Number} LOGS_CONF_DEBUG Index of the action DEBUG in the CONF panel
	 */
	LOGS_CONF_DEBUG: 4,
	/**
	 * @private
	 * @property {Number} LOGS_CONF_INFO Index of the action INFO in the CONF panel
	 */
	LOGS_CONF_INFO: 5,
	/**
	 * @private
	 * @property {Number} LOGS_CONF_WARN Index of the action WARN in the CONF panel
	 */
	LOGS_CONF_WARN: 6,
	/**
	 * @private
	 * @property {Number} LOGS_CONF_ERROR Index of the action ERROR in the CONF panel
	 */
	LOGS_CONF_ERROR: 7,
	/**
	 * @private
	 * @property {Number} LOGS_CONF_INHERIT Index of the action INHERIT in the CONF panel
	 */
	LOGS_CONF_INHERIT: 8,
	/**
	 * @private
	 * @property {Number} LOGS_CONF_FORCE Index of the action FORCE in the CONF panel
	 */
	LOGS_CONF_FORCE: 9,

	/**
	 * @private
	 * @property {Number} LOGS_OTHER_QUIT Index of the action QUIT in the all panels
	 */
	LOGS_OTHER_QUIT: 10,
	
	/**
	 * @private
	 * @property {String} pluginName The name of the plugin that declares this class
	 */
	/**
	 * @private
	 * @property {Ext.Panel} _cardPanel The main card panel 
	 */
	/**
	 * @private
	 * @property {Ext.Container} _contextualPanel The right panel
	 */
	/**
	 * @property {Object[]} _navItems The navigation items
	 * @private
	 */
	_navItems: [
	    {label: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_TAB_VIEW"/>"},
		{label: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_TAB_LEVEL"/>"}
	],
	
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
	 * @property {Ametys.workspace.admin.rightpanel.TextPanel} _helpPanel The action panel
	 */
	
	/**
	 * Initialize information needed
	 * @param {String} pluginName The name of the plugin that declares this class
	 */
	initialize: function (pluginName)
	{
		this.pluginName = pluginName;
	},

	/**
	 * Creates the main panel for this screen
	 */
	createPanel: function ()
	{
		this._cardPanel = new Ext.Panel({
			region:'center',
			layout:'card',
			activeItem: 0,
			
			id:'system-card-panel',

			border: false,
			autoScroll : true,
			
			items: [this._drawLogsPanel(),
			        this._drawConfPanel()]
		});		
		
		this._contextualPanel = new Ext.Container({
			region:'east',
		
			cls : 'admin-right-panel',
			border: false,
			width: 277,
		    
			items: [this._drawNavigationPanel (),
			        this._drawActionsPanel (),
			        this._drawHelpPanel ()]
		});
		
		this._onLogsPanelShow();
		this._onConfHide();
		
		return new Ext.Panel({
			region: 'center',
			
			autoScroll: true,
			
			baseCls: 'transparent-panel',
			border: false,
			layout: 'border',
			
			items: [this._cardPanel, 
			        this._contextualPanel],
			
			layoutConfig: {
	        	autoWidth: true
	    	}

		});
		
	},

	/**
	 * @private
	 * Listener when the log panel is shown
	 */
	_onLogsPanelShow: function()
	{
		this._helpPanel.items.get(0).show();
		this._onSelectLog();
	},
	/**
	 * @private
	 * Listener when the log panel is hidden
	 */
	_onLogsPanelHide: function()
	{
		this._actions.hideElt(this.LOGS_VIEW_VIEW);
		this._actions.hideElt(this.LOGS_VIEW_DOWNLOAD);
		this._actions.hideElt(this.LOGS_VIEW_DELETE);
		this._actions.hideElt(this.LOGS_VIEW_PURGE);
		this._helpPanel.items.get(0).hide();
	},
	/**
	 * @private
	 * Draw the panel displaying the logs
	 */
	_drawLogsPanel: function()
	{
		var model = Ext.define('Ametys.plugins.core.administrator.Logs.Log', {
		    extend: 'Ext.data.Model',
		    fields: [
		       {name: 'location'},
		       {name: 'date', type: 'date', dateFormat: Ext.Date.patterns.ISO8601Long},
		       {name: 'size', type: 'int'},
		       {name: 'file'}
		    ]
		});

		var store = Ext.create('Ext.data.Store', {
			model: 'Ametys.plugins.core.administrator.Logs.Log',
	        data: { logs: []},
	        groupField: 'file',
	        
	        sortOnLoad: true,
	        sorters: [ { property: 'date', direction: "DESC" } ],
	        
	        proxy: {
	        	type: 'memory',
	        	reader: {
	        		type: 'json',
	        		root: 'properties'
	        	}
	        }
		});		
		
		var groupingFeature = Ext.create('Ext.grid.feature.Grouping', {
		    groupHeaderTpl: '{name}'
		});
		
		this._logs = new Ext.grid.Panel({
			region: 'center',
			
			width: 610,
			
			id: 'detail-view-logs',
			
			features: [ groupingFeature ],
			
			animCollapse: true,
			
			listeners: {'select': Ext.bind(this._onSelectLog, this),
						'hide': Ext.bind(this._onLogsPanelHide, this),
						'show': Ext.bind(this._onLogsPanelShow, this)
			},
			
		    store : store,
			
		    multiSelect: true,
		    simpleSelect: false,

		    hideHeaders : false,
		    columnmove : false,
		    columns: [
		        {header: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_COL_NAME"/>", flex: 1, width : 250, menuDisabled : true, sortable: true, dataIndex: 'location'},
		        {header: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_COL_DATE"/>", flex: 0, width : 150, renderer: Ext.util.Format.dateRenderer('d F Y'), menuDisabled : true, sortable: true, dataIndex:'date',  align :'center'},
		        {header: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_COL_SIZE"/>", flex: 0, width : 100, renderer: this._sizeRendered, menuDisabled : true, sortable: true, dataIndex: 'size', align :'right'}
		    ]
		});
		
		return this._logs;
	},

	/**
	 * Listener when configuration panel is shown
	 * @private
	 */
	_onConfShow: function()
	{
		this._helpPanel.items.get(1).show();
		this._onSelectCategory();
	},
	/**
	 * Listener when configuration panel is hidden
	 * @private
	 */
	_onConfHide: function()
	{
		this._helpPanel.items.get(1).hide();
		this._actions.hideElt(this.LOGS_CONF_DEBUG);
		this._actions.hideElt(this.LOGS_CONF_INFO);
		this._actions.hideElt(this.LOGS_CONF_WARN);
		this._actions.hideElt(this.LOGS_CONF_ERROR);
		this._actions.hideElt(this.LOGS_CONF_INHERIT);
		this._actions.hideElt(this.LOGS_CONF_FORCE);
	},
	/**
	 * Listener when a category is selected on the configuration panel
	 * @private
	 */
	_onSelectCategory: function()
	{
		this._actions.items.get(this.LOGS_CONF_DEBUG).enable();
		this._actions.items.get(this.LOGS_CONF_INFO).enable();
		this._actions.items.get(this.LOGS_CONF_WARN).enable();
		this._actions.items.get(this.LOGS_CONF_ERROR).enable();
		this._actions.items.get(this.LOGS_CONF_INHERIT).enable();
		this._actions.items.get(this.LOGS_CONF_FORCE).enable();

		var selectedNode = this._categoryTree.getSelectionModel().getSelection()[0];
		if (selectedNode == null)
		{
			this._actions.hideElt(this.LOGS_CONF_DEBUG);
			this._actions.hideElt(this.LOGS_CONF_INFO);
			this._actions.hideElt(this.LOGS_CONF_WARN);
			this._actions.hideElt(this.LOGS_CONF_ERROR);
			this._actions.hideElt(this.LOGS_CONF_INHERIT);
			this._actions.hideElt(this.LOGS_CONF_FORCE);
		}
		else
		{
			this._actions.showElt(this.LOGS_CONF_DEBUG);
			this._actions.showElt(this.LOGS_CONF_INFO);
			this._actions.showElt(this.LOGS_CONF_WARN);
			this._actions.showElt(this.LOGS_CONF_ERROR);
			this._actions.showElt(this.LOGS_CONF_INHERIT);
			this._actions.showElt(this.LOGS_CONF_FORCE);

			switch (selectedNode.get('level'))
			{
				case "DEBUG": this._actions.items.get(this.LOGS_CONF_DEBUG).disable(); break;
				case "INFO": this._actions.items.get(this.LOGS_CONF_INFO).disable(); break;
				case "WARN": this._actions.items.get(this.LOGS_CONF_WARN).disable(); break;
				case "ERROR": this._actions.items.get(this.LOGS_CONF_ERROR).disable(); break;
				case "inherit": this._actions.items.get(this.LOGS_CONF_INHERIT).disable(); break;
			}
			
			if (selectedNode.getDepth() == 0)
			{
				this._actions.items.get(this.LOGS_CONF_INHERIT).disable();
			}
		}
	},
	/**
	 * @private
	 * Change the log level of the selected category to the given level
	 * @param {String} level The new level
	 */
	_changeLogLevel: function(level)
	{
		var selectedNode = this._categoryTree.getSelectionModel().getSelection()[0];
		if (selectedNode == null || selectedNode.get('level') == level)
		{
			// should not happend... no node was selecting or the node selected already has the right level
			this._onSelectCategory();
		}
		else
		{
			this._mask = new Ext.LoadMask(Ext.getBody());
			this._mask.show();

			var args = { level: level, category: selectedNode.get('fullname') };
			Ametys.data.ServerComm.send(this.pluginName, "administrator/logs/change-levels", args, Ametys.data.ServerComm.PRIORITY_MAJOR, this._changeLogLevelCB, this, [args, selectedNode]);
		}
	},
	/**
	 * @private
	 * {@link #_changeLogLevel} callback to effectively display the change
	 * @param {Object} response The response to the request
	 * @param {Object[]} argsArray First element is an object with id 'level' that is a string of the new level and with id 'category' that is a string with the modified catagory. The second element is the node in the tree to modify 
	 */
	_changeLogLevelCB: function (response, argsArray)
	{
		var args = argsArray[0];
		var selectedNode = argsArray[1];

		this._mask.hide();

	    if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_ERROR"/>", response, "org.ametys.administration.Groups._selectGroup"))
	    {
	       return;
	    }
	    
	    function getLevel(node)
	    {
	    	if (node.get('level') != 'inherit')
	    	{
	    		return node.get('level');
	    	}
	    	else
	    	{
	    		return getLevel(node.parentNode);
	    	}
	    }

		function changeSNode(node, level, inherited, force)
		{
	        var selection = node == null;
	        inherited = inherited == true;
	        node = node != null ? node : selectedNode;

	        if (!selection || !force)
	        {
	    		node.set('level', inherited ? "inherit" : level);
	    		node.set('icon', Ametys.getPluginResourcesPrefix("core") + "/img/administrator/logs/loglevel_" + level.toLowerCase() + (inherited ? "-inherit" : "") + ".png");
	        }		
			
			for (var i = 0; i &lt; node.childNodes.length; i++)
			{
				var childNode = node.childNodes[i];
				if (childNode.get('level') == "inherit" || force)
				{
					changeSNode(childNode, level, true, force);
				}
			}
		}

		if (args.level == 'FORCE')
		{
			var level = getLevel(selectedNode);
			changeSNode(null, level, true, true);
		}
		else if (args.level == 'INHERIT')
		{
			var level = getLevel(selectedNode.parentNode);
			changeSNode(null, level, true, false);
		}
		else
		{
			changeSNode(null, args.level, false, false);
		}
		
		this._onSelectCategory();
	},

	/**
	 * @private
	 * Draw the panel for the configuration (the one with the tree)
	 */
	_drawConfPanel: function()
	{
		function createNode(name, fullname, category, parentLevel)
		{
			parentLevel = category.level == "inherit" ? parentLevel : category.level;
			
			var childNodes = [];
			for (var c in category.child)
			{
				childNodes.push(createNode(c, (fullname != "" ? fullname + "." : "") + c, category.child[c], parentLevel));
			}

			var node = {
		   		icon: Ametys.getPluginResourcesPrefix("core") + "/img/administrator/logs/loglevel_" + (category.level == "inherit" ? parentLevel.toLowerCase() + "-inherit": category.level.toLowerCase()) + ".png",
		   		text: name,
		   		fullname: fullname != "" ? fullname : "root",
		   		level: category.level,
		   		leaf: childNodes.length == 0
			};
			
			node.children = childNodes;
			
			return node;
		}
		
		var model = Ext.define('Ametys.plugins.core.administration.Logs.Category', { 
		    extend: 'Ext.data.Model', 
		    fields: [ 
		        { name: 'id', type: 'int' }, 
		        { name: 'icon', type: 'string' }, 
		        { name: 'fullname', type: 'string' }, 
		        { name: 'text', type: 'string' }, 
		        { name: 'level', type: 'string' }, 
		        { name: 'leaf', type: 'boolean'} 
		    ] 
		}); 

		var rootNodeConf = createNode("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_CONFIG_LOGKITROOT"/>", "", logcategories);
		rootNodeConf.expanded = true;
		
		var store = Ext.create('Ext.data.TreeStore', {
			root : rootNodeConf,
			model: model
		})
		
		this._categoryTree = new Ext.tree.Panel({
			id: 'monitoring-panel',
			store: store,
			
			border: false,
			autoScroll: true,
			
			listeners: {'show': Ext.bind(this._onConfShow, this),
			            'hide': Ext.bind(this._onConfHide, this) 
			}
		});
		
		this._categoryTree.getSelectionModel().addListener("selectionchange", this._onSelectCategory, this);
		
		return this._categoryTree;
	},

	/**
	 * Load data
	 * @param {Object[]} data The data to load to the store
	 */
	load: function (data)
	{
		this._logs.getStore().loadData(data);
	},

	/**
	 * Draw the navigation panel. This function needs the org.ametys.administration.JVMStatus._navItems was filled first.
	 * @return {Ametys.workspace.admin.rightpanel.NavigationPanel} The navigation panel
	 * @private
	 */
	_drawNavigationPanel: function ()
	{
		this._nav = new Ametys.workspace.admin.rightpanel.NavigationPanel ({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_MENU"/>"});
		
		for (var i=0; i &lt; this._navItems.length; i++)
		{
			var item = new Ametys.workspace.admin.rightpanel.NavigationPanel.NavigationItem ({
				text: this._navItems[i].label,
				
				activeItem: i,
				
				cardLayout: 'system-card-panel',
				toggleGroup : 'system-menu',
				
				allowDepress: false,
				pressed: i == 0
			});
			
			this._nav.add(item);
		}
		
		return this._nav;
	},
	/**
	 * Draw the actions panel.
	 * @return {Ametys.workspace.admin.rightpanel.ActionPanel} The action panel
	 * @private
	 */
	_drawActionsPanel: function ()
	{
		this._actions = new Ametys.workspace.admin.rightpanel.ActionPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE"/>"});
		
		// View file
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_VIEW"/>", 
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/logs/file.png', 
				 Ext.bind(this.viewFile, this));
		
		// Download files
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DOWNLOAD"/>", 
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/logs/download.png', 
				 Ext.bind(this.downloadFiles, this));
		
		// Delete files
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DELETE"/>", 
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/logs/delete.png', 
				 Ext.bind(this.deleteFiles, this));
		
		// Purge files
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_PURGE"/>", 
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/logs/purge.png', 
				 Ext.bind(this.purgeFiles, this));
		
		// Conf debug
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_LOGLEVEL_TO_DEBUG"/>", 
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/logs/loglevel_btn_debug.png', 
				 Ext.bind(this._changeLogLevel, this, ['DEBUG']));
		
		// Conf info
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_LOGLEVEL_TO_INFO"/>", 
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/logs/loglevel_btn_info.png', 
				 Ext.bind(this._changeLogLevel, this, ['INFO']));
		
		// Conf warn
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_LOGLEVEL_TO_WARN"/>", 
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/logs/loglevel_btn_warn.png', 
				 Ext.bind(this._changeLogLevel, this, ['WARN']));
		
		// Conf error
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_LOGLEVEL_TO_ERROR"/>", 
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/logs/loglevel_btn_error.png', 
				 Ext.bind(this._changeLogLevel, this, ['ERROR']));
		
		// Inherit
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_LOGLEVEL_INHERIT"/>", 
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/logs/loglevel_btn_inherit.png', 
				 Ext.bind(this._changeLogLevel, this, ['INHERIT']));
		
		// Force
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_LOGLEVEL_FORCE"/>", 
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/logs/loglevel_btn_force.png', 
				 Ext.bind(this._changeLogLevel, this, ['FORCE']));
		
		// Quit
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_QUIT"/>", 
				 Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/logs/quit.png', 
				 Ext.bind(this.goBack, this));

		return this._actions;
	},

	/**
	 * Draw the help panel.
	 * @return {Ametys.workspace.admin.rightpanel.TextPanel} The help panel
	 * @private
	 */
	_drawHelpPanel: function ()
	{
		this._helpPanel = new Ametys.workspace.admin.rightpanel.TextPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HELP"/>"});
		this._helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HELP_TEXT"/>");
		this._helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HELP2_TEXT"/>");
		
		for (var i = 0; i &lt; this._helpPanel.items.length; i++)
		{
			this._helpPanel.items.get(i).hide();
		}
		
		return this._helpPanel;
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
	 * View the selected log file
	 */
	viewFile: function ()
	{
	    var elt = this._logs.getSelectionModel().getSelection()[0];
	    
	    if (elt.get('size') > 1024 * 1024)
	    {
	    	Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_VIEW"/>", 
	    					 "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_VIEW_CONFIRM"/>", 
	    					 Ext.bind(this.downloadFile, this, [elt]));
	    }
	    else
	    {
			window.location.href = Ametys.getPluginDirectPrefix(this.pluginName) + "/administrator/logs/view/" + encodeURIComponent(elt.get('location'));
	    }
	},

	/**
	 * @private
	 * Download one file
	 * @param {Ext.data.Model} elt The element of the file to download
	 */
	downloadFile: function (elt)
	{
		var url = Ametys.getPluginDirectPrefix(this.pluginName) + "/administrator/logs/download.zip";
	    var args = "file=" + encodeURIComponent(elt.get('location'));
	    
	    window.location.href = url + "?" + args;
	},
	/**
	 * @private
	 * Download the selected log files
	 */
	downloadFiles: function ()
	{
	    var url = Ametys.getPluginDirectPrefix(this.pluginName) + "/administrator/logs/download.zip";
	    var args = "";

	    var elts = this._logs.getSelectionModel().getSelection();
	    for (var i = 0; i &lt; elts.length; i++)
	    {
	        var elt = elts[i];
	        args += "file=" + encodeURIComponent(elt.get('location')) + "&amp;";
	    }
	    
	    window.location.href = url + "?" + args;
	},

	/**
	 * @private
	 * Delete the selected log files with a confirmation
	 */
	deleteFiles: function ()
	{
		Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DELETE"/>", 
						 "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DELETE_CONFIRM"/>", 
						 Ext.bind(this.doDelete, this));
	},
	
	/**
	 * @private
	 * Callback to actually delete the log files
	 * @param {String} answser Will do the delteion if 'yes'
	 */
	doDelete: function (answer)
	{
		if (answer == 'yes')
	    {
	        var files = [];
	    
	        var elts = this._logs.getSelectionModel().getSelection();
	        for (var i = 0; i &lt; elts.length; i++)
	        {
	            var elt = elts[i];
	            files.push(elt.get('location'));
	        }
	        
	    	var result = Ametys.data.ServerComm.send(this.pluginName, "administrator/logs/delete", { file: files }, Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
	        if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DELETE_ERROR_GRAVE"/>", result, "Ametys.plugins.core.administration.Logs.doDelete"))
	        {
	           return;
	        }

	        var failuresString = Ext.dom.Query.selectValue("*/failure", result, "");
	        
	        for (var i = 0; i &lt; elts.length; i++)
	        {
	            var elt = elts[i];
	            if (failuresString.indexOf('/' + elt.get('location') + '/') &lt; 0)
	            {
	            	this._logs.getStore().remove(elt);
	            }
	        }                            
	        
	        if (failuresString.length &gt; 0)
	        {
	        	Ext.Msg.show({
					title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DELETE"/>",
					msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DELETE_ERROR"/>",
					buttons: Ext.Msg.OK,
					icon: Ext.MessageBox.ERROR
				});
	        }
	    }
	},

	/**
	 * Purge the selected files with a confirmation dialog
	 * @private
	 */
	purgeFiles: function ()
	{
		Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_PURGE"/>", 
				         "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_PURGE_CONFIRM"/>",
				         Ext.bind(this.doPurge, this));
	},
	
	/**
	 * @private
	 * Callback function to finally do the purge action
	 * @param {String} answser Will do the purge if 'yes'
	 */
	doPurge: function (anwser)
	{
		if (anwser == 'yes')
	    {
	    	var result = Ametys.data.ServerComm.send(this.pluginName, "administrator/logs/purge", {}, Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
	        if (Ametys.data.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_PURGE_ERROR_GRAVE"/>", result, "Ametys.plugins.core.administration.Logs.doPurge"))
	        {
	           return;
	        }
	        
	        var doneString = Ext.dom.Query.selectValue("*/done", result);
	        
	        var nb = 0;
	        var elts = this._logs.getElements();
	        for (var i = elts.length - 1; i &gt;= 0; i--)
	        {
	            var elt = elts[i];
	            if (doneString.indexOf('/' + elt.get('location') + '/') &gt;= 0)
	            {
	            	this._logs.getStore().remove(elt);
	                nb++;
	            }
	        }      
	        
	        Ext.Msg.show({
					title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_PURGE"/>",
					msg: nb + " " + "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_PURGE_DONE"/>",
					buttons: Ext.Msg.OK,
					icon: Ext.MessageBox.INFO
				});
	    }
	},

	/**
	 * @private
	 * Listener when a log file is selected
	 */
	_onSelectLog: function ()
	{
		var hasSelection  = this._logs.getSelectionModel().getSelection().length &gt; 0;
		if (hasSelection)
		{
			this._actions.showElt(0);
			this._actions.showElt(1);
			this._actions.showElt(2);
			this._actions.showElt(3);
		}
		else
		{
			this._actions.hideElt(0);
			this._actions.hideElt(1);
			this._actions.hideElt(2);
			this._actions.showElt(3);
		}
	},

	/**
	 * @private
	 * Returns the size filled with leading zeros to obtain a 20 caracters string
	 * @param {Number} size The size to edit
	 * @return {String} The number as a string with leading zeros. e.g. 00000012345678901234 
	 */
	_fillSize: function (size)
	{
	    while (size.length &lt; 20)
	    {
	        size = "0" + size;
	    }
	    return size;
	},

	/**
	 * @private
	 * Returns a readable size for a size
	 * @param {Number} size The size to render
	 * @return {String} The readable size. e.g. 12 MB
	 */
	_sizeRendered: function (size)
	{
		return Ext.util.Format.fileSize(size);
	}
});
