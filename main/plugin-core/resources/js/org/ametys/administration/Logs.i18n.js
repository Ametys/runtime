/*
 *  Copyright 2009 Anyware Services
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

Ext.namespace('org.ametys.administration.Logs');

org.ametys.administration.Logs = function ()
{
}

org.ametys.administration.Logs.LOGS_VIEW_VIEW = 0;
org.ametys.administration.Logs.LOGS_VIEW_DOWNLOAD = 1;
org.ametys.administration.Logs.LOGS_VIEW_DELETE = 2;
org.ametys.administration.Logs.LOGS_VIEW_PURGE = 3;
org.ametys.administration.Logs.LOGS_CONF_DEBUG = 4;
org.ametys.administration.Logs.LOGS_CONF_INFO = 5;
org.ametys.administration.Logs.LOGS_CONF_WARN = 6;
org.ametys.administration.Logs.LOGS_CONF_ERROR = 7;
org.ametys.administration.Logs.LOGS_CONF_INHERIT = 8;
org.ametys.administration.Logs.LOGS_CONF_FORCE = 9;
org.ametys.administration.Logs.LOGS_OTHER_QUIT = 10;

org.ametys.administration.Logs.initialize = function (pluginName)
{
	org.ametys.administration.Logs.pluginName = pluginName;
}

org.ametys.administration.Logs.createPanel = function ()
{
	org.ametys.administration.Logs._cardPanel = new Ext.Panel({
		region:'center',
		layout:'card',
		activeItem: 0,
		
		id:'system-card-panel',
		baseCls: 'transparent-panel',
		border: false,
		autoScroll : true,
		height: 'auto',
		
		items: [org.ametys.administration.Logs._drawLogsPanel(),
		        org.ametys.administration.Logs._drawConfPanel()]
	});		
	
	org.ametys.administration.Logs._contextualPanel = new org.ametys.HtmlContainer({
		region:'east',
	
		cls : 'admin-right-panel',
		border: false,
		width: 277,
	    
		items: [org.ametys.administration.Logs._drawNavigationPanel (),
		        org.ametys.administration.Logs._drawActionsPanel (),
		        org.ametys.administration.Logs._drawHelpPanel ()]
	});
	
	org.ametys.administration.Logs._onLogsPanelShow();
	org.ametys.administration.Logs._onConfHide();
	
	return new Ext.Panel({
		region: 'center',
		
		autoScroll: true,
		
		baseCls: 'transparent-panel',
		border: false,
		layout: 'border',
		
		items: [org.ametys.administration.Logs._cardPanel, 
		        org.ametys.administration.Logs._contextualPanel],
		
		layoutConfig: {
        	autoWidth: true
    	}

	});
	
}

org.ametys.administration.Logs._onLogsPanelShow = function()
{
	org.ametys.administration.Logs._helpPanel.items.get(0).show();
	org.ametys.administration.Logs._onSelectLog();
}
org.ametys.administration.Logs._onLogsPanelHide = function()
{
	org.ametys.administration.Logs._actions.hideElt(org.ametys.administration.Logs.LOGS_VIEW_VIEW);
	org.ametys.administration.Logs._actions.hideElt(org.ametys.administration.Logs.LOGS_VIEW_DOWNLOAD);
	org.ametys.administration.Logs._actions.hideElt(org.ametys.administration.Logs.LOGS_VIEW_DELETE);
	org.ametys.administration.Logs._actions.hideElt(org.ametys.administration.Logs.LOGS_VIEW_PURGE);
	org.ametys.administration.Logs._helpPanel.items.get(0).hide();
}
org.ametys.administration.Logs._drawLogsPanel = function()
{
	var reader = new Ext.data.ArrayReader({}, [
	                					       {name: 'location'},
	                					       {name: 'date', type: 'date', dateFormat: Date.patterns.ISO8601Long},
	                					       {name: 'size', type: 'int'},
	                					       {name: 'file'}
	                					    ]);
	
	var store =  new Ext.data.GroupingStore({
	        reader: reader,
	        sortInfo:{field: 'date', direction: "DESC"},
	        groupField:'file'
	});
	 
	org.ametys.administration.Logs._logs = new org.ametys.ListView({
		region: 'center',
		
		width: 610,
		autoWidth: true,
		autoScroll: true,
		id: 'detail-view-logs',
		baseCls: 'detail-view-logs',
		
		animCollapse: true,
		
		listeners: {'rowclick': org.ametys.administration.Logs._onSelectLog,
					'hide': org.ametys.administration.Logs._onLogsPanelHide,
					'show': org.ametys.administration.Logs._onLogsPanelShow
		},
		
	    store : store,
		
	    view: new Ext.grid.GroupingView({
	            forceFit:true,
	            groupTextTpl: '{text}',
	            hideGroupedColumn : true
	    }),	
	    
	    columns: [
	        {header: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_LABEL"/>", menuDisabled : true, sortable: true, dataIndex: 'file'},
	        {header: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_COL_NAME"/>", width : 250, menuDisabled : true, sortable: true, dataIndex: 'location'},
	        {header: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_COL_DATE"/>", width : 150, renderer: Ext.util.Format.dateRenderer('d F Y'), menuDisabled : true, sortable: true, dataIndex:'date',  align :'center'},
	        {header: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_COL_SIZE"/>", width : 100, renderer: org.ametys.administration.Logs._sizeRendered, menuDisabled : true, sortable: true, dataIndex: 'size', align :'right'}
	    ]
	});
	
	org.ametys.administration.Logs._logs.setMultipleSelection(true);		
	
	return org.ametys.administration.Logs._logs;
}

org.ametys.administration.Logs._onConfShow = function()
{
	org.ametys.administration.Logs._helpPanel.items.get(1).show();
	org.ametys.administration.Logs._onSelectCategory();
}
org.ametys.administration.Logs._onConfHide = function()
{
	org.ametys.administration.Logs._helpPanel.items.get(1).hide();
	org.ametys.administration.Logs._actions.hideElt(org.ametys.administration.Logs.LOGS_CONF_DEBUG);
	org.ametys.administration.Logs._actions.hideElt(org.ametys.administration.Logs.LOGS_CONF_INFO);
	org.ametys.administration.Logs._actions.hideElt(org.ametys.administration.Logs.LOGS_CONF_WARN);
	org.ametys.administration.Logs._actions.hideElt(org.ametys.administration.Logs.LOGS_CONF_ERROR);
	org.ametys.administration.Logs._actions.hideElt(org.ametys.administration.Logs.LOGS_CONF_INHERIT);
	org.ametys.administration.Logs._actions.hideElt(org.ametys.administration.Logs.LOGS_CONF_FORCE);
}

org.ametys.administration.Logs._onSelectCategory = function()
{
	org.ametys.administration.Logs._actions.actions[org.ametys.administration.Logs.LOGS_CONF_DEBUG].enable();
	org.ametys.administration.Logs._actions.actions[org.ametys.administration.Logs.LOGS_CONF_INFO].enable();
	org.ametys.administration.Logs._actions.actions[org.ametys.administration.Logs.LOGS_CONF_WARN].enable();
	org.ametys.administration.Logs._actions.actions[org.ametys.administration.Logs.LOGS_CONF_ERROR].enable();
	org.ametys.administration.Logs._actions.actions[org.ametys.administration.Logs.LOGS_CONF_INHERIT].enable();
	org.ametys.administration.Logs._actions.actions[org.ametys.administration.Logs.LOGS_CONF_FORCE].enable();

	var selectedNode = org.ametys.administration.Logs._categoryTree.getSelectionModel().getSelectedNode();
	if (selectedNode == null)
	{
		org.ametys.administration.Logs._actions.hideElt(org.ametys.administration.Logs.LOGS_CONF_DEBUG);
		org.ametys.administration.Logs._actions.hideElt(org.ametys.administration.Logs.LOGS_CONF_INFO);
		org.ametys.administration.Logs._actions.hideElt(org.ametys.administration.Logs.LOGS_CONF_WARN);
		org.ametys.administration.Logs._actions.hideElt(org.ametys.administration.Logs.LOGS_CONF_ERROR);
		org.ametys.administration.Logs._actions.hideElt(org.ametys.administration.Logs.LOGS_CONF_INHERIT);
		org.ametys.administration.Logs._actions.hideElt(org.ametys.administration.Logs.LOGS_CONF_FORCE);
	}
	else
	{
		org.ametys.administration.Logs._actions.showElt(org.ametys.administration.Logs.LOGS_CONF_DEBUG);
		org.ametys.administration.Logs._actions.showElt(org.ametys.administration.Logs.LOGS_CONF_INFO);
		org.ametys.administration.Logs._actions.showElt(org.ametys.administration.Logs.LOGS_CONF_WARN);
		org.ametys.administration.Logs._actions.showElt(org.ametys.administration.Logs.LOGS_CONF_ERROR);
		org.ametys.administration.Logs._actions.showElt(org.ametys.administration.Logs.LOGS_CONF_INHERIT);
		org.ametys.administration.Logs._actions.showElt(org.ametys.administration.Logs.LOGS_CONF_FORCE);
		
		switch (selectedNode.attributes.level)
		{
			case "DEBUG": org.ametys.administration.Logs._actions.actions[org.ametys.administration.Logs.LOGS_CONF_DEBUG].disable(); break;
			case "INFO": org.ametys.administration.Logs._actions.actions[org.ametys.administration.Logs.LOGS_CONF_INFO].disable(); break;
			case "WARN": org.ametys.administration.Logs._actions.actions[org.ametys.administration.Logs.LOGS_CONF_WARN].disable(); break;
			case "ERROR": org.ametys.administration.Logs._actions.actions[org.ametys.administration.Logs.LOGS_CONF_ERROR].disable(); break;
			case "inherit": org.ametys.administration.Logs._actions.actions[org.ametys.administration.Logs.LOGS_CONF_INHERIT].disable(); break;
		}
		
		if (selectedNode.getDepth() == 0)
		{
			org.ametys.administration.Logs._actions.actions[org.ametys.administration.Logs.LOGS_CONF_INHERIT].disable();
		}
	}
}

org.ametys.administration.Logs._changeLogLevel = function(level)
{
	var selectedNode = org.ametys.administration.Logs._categoryTree.getSelectionModel().getSelectedNode();
	if (selectedNode == null || selectedNode.attributes.level == level)
	{
		// should not happend... no node was selecting or the node selected already has the right level
		org.ametys.administration.Logs._onSelectCategory();
	}
	else
	{
		org.ametys.administration.Logs._mask = new org.ametys.msg.Mask();

		var args = {level: level, category: selectedNode.attributes.fullname };
		var serverMessage = new org.ametys.servercomm.ServerMessage(org.ametys.administration.Logs.pluginName, "administrator/logs/change-levels", args, org.ametys.servercomm.ServerComm.PRIORITY_MAJOR, org.ametys.administration.Logs._changeLogLevelCB, this, [args, selectedNode]);
		org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);
	}
}
org.ametys.administration.Logs._changeLogLevelCB = function (response, argsArray)
{
	var args = argsArray[0];
	var selectedNode = argsArray[1];
	
	org.ametys.administration.Logs._mask.hide();

    if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_ERROR"/>", response, "org.ametys.administration.Groups._selectGroup"))
    {
       return;
    }
    
    function getLevel(node)
    {
    	if (node.attributes.level != 'inherit')
    	{
    		return node.attributes.level;
    	}
    	else
    	{
    		return getLevel(node.parentNode);
    	}
    }

    
	function changeNode(node, level, inherited, force)
	{
        var selection = node == null;
        inherited = inherited == true;
        node = node != null ? node : selectedNode;

        if (!selection || !force)
        {
    		Ext.get(node.ui.elNode).child("img:last").dom.src = getPluginResourcesUrl("core") + "/img/administrator/logs/loglevel_" + level.toLowerCase() + (inherited ? "-inherit" : "") + ".png";
        }

		for (var i = 0; i &lt; node.childNodes.length; i++)
		{
			var childNode = node.childNodes[i];
			if (childNode.attributes.level == "inherit" || force)
			{
				changeNode(childNode, level, true, force);
			}
		}
	}
	
	function changeSNode(node, level, inherited, force)
	{
        var selection = node == null;
        inherited = inherited == true;
        node = node != null ? node : selectedNode.attributes;

        if (!selection || !force)
        {
    		node.level = inherited ? "inherit" : level;
    		node.icon = getPluginResourcesUrl("core") + "/img/administrator/logs/loglevel_" + level.toLowerCase() + (inherited ? "-inherit" : "") + ".png";
        }		
		
		for (var i = 0; i &lt; node.children.length; i++)
		{
			var childNode = node.children[i];
			if (childNode.level == "inherit" || force)
			{
				changeSNode(childNode, level, true, force);
			}
		}
	}

	if (args.level == 'FORCE')
	{
		var level = getLevel(selectedNode);
		changeNode(null, level, true, true);
		changeSNode(null, level, true, true);
	}
	else if (args.level == 'INHERIT')
	{
		var level = getLevel(selectedNode.parentNode);
		changeNode(null, level, true, false);
		changeSNode(null, level, true, false);
	}
	else
	{
		changeNode(null, args.level, false, false);
		changeSNode(null, args.level, false, false);
	}
	
	org.ametys.administration.Logs._onSelectCategory();
}

org.ametys.administration.Logs._drawConfPanel = function()
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
	   		icon: getPluginResourcesUrl("core") + "/img/administrator/logs/loglevel_" + (category.level == "inherit" ? parentLevel.toLowerCase() + "-inherit": category.level.toLowerCase()) + ".png",
	   		text: name,
	   		fullname: fullname != "" ? fullname : "root",
	   		level: category.level,
	   		leaf: childNodes.length == 0
		};
		
		node.children = childNodes;
		
		return node;
	}
	
	var rootNodeConf = createNode("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_CONFIG_LOGKITROOT"/>", "", logcategories);
	rootNodeConf.expanded = true;
	
	var rootNode = new Ext.tree.AsyncTreeNode(rootNodeConf);
	
	org.ametys.administration.Logs._categoryTree = new Ext.tree.TreePanel({
		id: 'monitoring-panel',
		root: rootNode,
		
		baseCls: 'transparent-panel',
		border: false,
		autoScroll: true,
		
		listeners: {'show': org.ametys.administration.Logs._onConfShow,
		            'hide': org.ametys.administration.Logs._onConfHide 
		}
	});
	
	org.ametys.administration.Logs._categoryTree.getSelectionModel().addListener("selectionchange", org.ametys.administration.Logs._onSelectCategory);
	
	return org.ametys.administration.Logs._categoryTree;
}

org.ametys.administration.Logs.load = function (data)
{
	org.ametys.administration.Logs._logs.getStore().loadData(data);
}

org.ametys.administration.Logs._navItems = [
	{label: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_TAB_VIEW"/>"},
	{label: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_TAB_LEVEL"/>"}];
/**
 * Draw the navigation panel. This function needs the org.ametys.administration.JVMStatus._navItems was filled first.
 * @return {org.ametys.NavigationPanel} The navigation panel
 * @private
 */
org.ametys.administration.Logs._drawNavigationPanel = function ()
{
	org.ametys.administration.Logs._nav = new org.ametys.NavigationPanel ({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_MENU"/>"});
	
	for (var i=0; i &lt; org.ametys.administration.Logs._navItems.length; i++)
	{
		var item = new org.ametys.NavigationItem ({
			text: org.ametys.administration.Logs._navItems[i].label,
			
			activeItem: i,
			
			cardLayout: 'system-card-panel',
			toggleGroup : 'system-menu',
			
			allowDepress: false,
			pressed: i == 0
		});
		
		org.ametys.administration.Logs._nav.add(item);
	}
	
	return org.ametys.administration.Logs._nav;
}
/**
 * Draw the actions panel.
 * @return {org.ametys.ActionsPanel} The action panel
 * @private
 */
org.ametys.administration.Logs._drawActionsPanel = function ()
{
	org.ametys.administration.Logs._actions = new org.ametys.ActionsPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE"/>"});
	
	// View file
	org.ametys.administration.Logs._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_VIEW"/>", 
			 getPluginResourcesUrl(org.ametys.administration.Logs.pluginName) + '/img/administrator/logs/file.png', 
			 org.ametys.administration.Logs.viewFile);
	
	// Download files
	org.ametys.administration.Logs._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DOWNLOAD"/>", 
			 getPluginResourcesUrl(org.ametys.administration.Logs.pluginName) + '/img/administrator/logs/download.png', 
			 org.ametys.administration.Logs.downloadFiles);
	
	// Delete files
	org.ametys.administration.Logs._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DELETE"/>", 
			 getPluginResourcesUrl(org.ametys.administration.Logs.pluginName) + '/img/administrator/logs/delete.png', 
			 org.ametys.administration.Logs.deleteFiles);
	
	// Purge files
	org.ametys.administration.Logs._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_PURGE"/>", 
			 getPluginResourcesUrl(org.ametys.administration.Logs.pluginName) + '/img/administrator/logs/purge.png', 
			 org.ametys.administration.Logs.purgeFiles);
	
	// Conf debug
	org.ametys.administration.Logs._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_LOGLEVEL_TO_DEBUG"/>", 
			 getPluginResourcesUrl(org.ametys.administration.Logs.pluginName) + '/img/administrator/logs/loglevel_btn_debug.png', 
			 org.ametys.administration.Logs._changeLogLevel.createDelegate(this, ['DEBUG']));
	
	// Conf info
	org.ametys.administration.Logs._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_LOGLEVEL_TO_INFO"/>", 
			 getPluginResourcesUrl(org.ametys.administration.Logs.pluginName) + '/img/administrator/logs/loglevel_btn_info.png', 
			 org.ametys.administration.Logs._changeLogLevel.createDelegate(this, ['INFO']));
	
	// Conf warn
	org.ametys.administration.Logs._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_LOGLEVEL_TO_WARN"/>", 
			 getPluginResourcesUrl(org.ametys.administration.Logs.pluginName) + '/img/administrator/logs/loglevel_btn_warn.png', 
			 org.ametys.administration.Logs._changeLogLevel.createDelegate(this, ['WARN']));
	
	// Conf error
	org.ametys.administration.Logs._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_LOGLEVEL_TO_ERROR"/>", 
			 getPluginResourcesUrl(org.ametys.administration.Logs.pluginName) + '/img/administrator/logs/loglevel_btn_error.png', 
			 org.ametys.administration.Logs._changeLogLevel.createDelegate(this, ['ERROR']));
	
	// Inherit
	org.ametys.administration.Logs._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_LOGLEVEL_INHERIT"/>", 
			 getPluginResourcesUrl(org.ametys.administration.Logs.pluginName) + '/img/administrator/logs/loglevel_btn_inherit.png', 
			 org.ametys.administration.Logs._changeLogLevel.createDelegate(this, ['INHERIT']));
	
	// Force
	org.ametys.administration.Logs._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_LOGLEVEL_FORCE"/>", 
			 getPluginResourcesUrl(org.ametys.administration.Logs.pluginName) + '/img/administrator/logs/loglevel_btn_force.png', 
			 org.ametys.administration.Logs._changeLogLevel.createDelegate(this, ['FORCE']));
	
	// Quit
	org.ametys.administration.Logs._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_QUIT"/>", 
			 getPluginResourcesUrl(org.ametys.administration.Logs.pluginName) + '/img/administrator/logs/quit.png', 
			 org.ametys.administration.Logs.goBack);

	return org.ametys.administration.Logs._actions;
}

/**
 * Draw the help panel.
 * @return {org.ametys.TextPanel} The help panel
 * @private
 */
org.ametys.administration.Logs._drawHelpPanel = function ()
{
	org.ametys.administration.Logs._helpPanel = new org.ametys.TextPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HELP"/>"});
	org.ametys.administration.Logs._helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HELP_TEXT"/>");
	org.ametys.administration.Logs._helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HELP2_TEXT"/>");
	
	for (var i = 0; i &lt; org.ametys.administration.Logs._helpPanel.items.length; i++)
	{
		org.ametys.administration.Logs._helpPanel.items.get(i).hide();
	}
	
	return org.ametys.administration.Logs._helpPanel;
}

/**
 * Quit
 */
org.ametys.administration.Logs.goBack = function ()
{
    document.location.href = context.workspaceContext;
}

/**
 * View the log file
 */
org.ametys.administration.Logs.viewFile = function ()
{
    var elt = org.ametys.administration.Logs._logs.getSelection()[0];
    
    if (elt.get('size') > 1024 * 1024)
    {
    	Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_VIEW"/>", 
    					 "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_VIEW_CONFIRM"/>", 
    					 function () {org.ametys.administration.Logs.downloadFile (elt)});
    }
    else
    {
		window.location.href = getPluginDirectUrl(org.ametys.administration.Logs.pluginName) + "/administrator/logs/view/" + encodeURIComponent(elt.get('location'));
    }
}

org.ametys.administration.Logs.downloadFile = function (elt)
{
	var url = getPluginDirectUrl(org.ametys.administration.Logs.pluginName) + "/administrator/logs/download.zip";
    var args = "file=" + encodeURIComponent(elt.get('location'));
    
    window.location.href = url + "?" + args;
}
/**
 * Download the selected file logs
 */
org.ametys.administration.Logs.downloadFiles = function ()
{
    var url = getPluginDirectUrl(org.ametys.administration.Logs.pluginName) + "/administrator/logs/download.zip";
    var args = "";

    var elts = org.ametys.administration.Logs._logs.getSelection();
    for (var i = 0; i &lt; elts.length; i++)
    {
        var elt = elts[i];
        args += "file=" + encodeURIComponent(elt.get('location')) + "&amp;";
    }
    
    window.location.href = url + "?" + args;
}

/**
 * Delete the selected log files
 */
org.ametys.administration.Logs.deleteFiles = function ()
{
	Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DELETE"/>", 
					 "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DELETE_CONFIRM"/>", 
					 org.ametys.administration.Logs.doDelete);
}
org.ametys.administration.Logs.doDelete = function (answer)
{
	if (answer == 'yes')
    {
        var files = [];
    
        var elts = org.ametys.administration.Logs._logs.getSelection();
        for (var i = 0; i &lt; elts.length; i++)
        {
            var elt = elts[i];
            files.push(elt.get('location'));
        }
        
    	var serverMessage = new org.ametys.servercomm.ServerMessage(org.ametys.administration.Logs.pluginName, "administrator/logs/delete", { file: files }, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
    	var result = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);

        if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DELETE_ERROR_GRAVE"/>", result, "org.ametys.administration.Logs.doDelete"))
        {
           return;
        }

        var failuresString = org.ametys.servercomm.ServerComm.handleResponse(result, "failure");
        
        for (var i = 0; i &lt; elts.length; i++)
        {
            var elt = elts[i];
            if (failuresString.indexOf('/' + elt.get('location') + '/') &lt; 0)
            {
            	org.ametys.administration.Logs._logs.removeElement(elt);
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
}

/**
 * Purge the selected files
 */
org.ametys.administration.Logs.purgeFiles = function ()
{
	Ext.Msg.confirm ("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_PURGE"/>", 
			         "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_PURGE_CONFIRM"/>",
			         org.ametys.administration.Logs.doPurge);
}
org.ametys.administration.Logs.doPurge = function (anwser)
{
	if (anwser == 'yes')
    {
    	var serverMessage = new org.ametys.servercomm.ServerMessage(org.ametys.administration.Logs.pluginName, "administrator/logs/purge", {}, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
    	var result = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);

        if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_PURGE_ERROR_GRAVE"/>", result, "org.ametys.administration.Logs.doPurge"))
        {
           return;
        }
        
        var doneString = org.ametys.servercomm.ServerComm.handleResponse(result, "done");
        
        var nb = 0;
        var elts = org.ametys.administration.Logs._logs.getElements();
        for (var i = elts.length - 1; i &gt;= 0; i--)
        {
            var elt = elts[i];
            if (doneString.indexOf('/' + elt.get('location') + '/') &gt;= 0)
            {
            	org.ametys.administration.Logs._logs.removeElement(elt);
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
}

org.ametys.administration.Logs._onSelectLog = function (grid, rowindex, e)
{
	var hasSelection  = org.ametys.administration.Logs._logs.getSelection().length &gt; 0;
	if (hasSelection)
	{
		org.ametys.administration.Logs._actions.showElt(0);
		org.ametys.administration.Logs._actions.showElt(1);
		org.ametys.administration.Logs._actions.showElt(2);
		org.ametys.administration.Logs._actions.showElt(3);
	}
	else
	{
		org.ametys.administration.Logs._actions.hideElt(0);
		org.ametys.administration.Logs._actions.hideElt(1);
		org.ametys.administration.Logs._actions.hideElt(2);
		org.ametys.administration.Logs._actions.showElt(3);
	}
}

/**
 * Returns the fill size
 */
org.ametys.administration.Logs._fillSize = function (size)
{
    while (size.length &lt; 20)
    {
        size = "0" + size;
    }
    return size;
}

org.ametys.administration.Logs._sizeRendered = function (size, metadata, record, rowIndex, colIndex, store)
{
	if (size &lt; 1024)
	{
		return size + " o";
	}
	else if (size &lt; 1024*1024)
	{
		return Math.round(size / 1024 * 10)/10 + " ko";
	}
	else if (size &lt; 1024*1024*1024)
	{
		return Math.round(size/1024/1024*10)/10 + " Mo";
	}
}
