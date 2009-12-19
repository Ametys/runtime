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

Ext.namespace('org.ametys.administration.Plugins');

org.ametys.administration.Plugins = function ()
{
}

org.ametys.administration.Plugins.initialize = function (pluginName)
{
	org.ametys.administration.Plugins.pluginName = pluginName;
}

org.ametys.administration.Plugins.createPanel = function (rootNode, rootNode2, rootNode3)
{
	org.ametys.administration.Plugins._contextualPanel = new org.ametys.HtmlContainer({
		region:'east',
		
		border: false,
		cls: 'admin-right-panel',
		width: 277,
	    
		items: [org.ametys.administration.Plugins._drawNavigationPanel(),
		        org.ametys.administration.Plugins._drawActionPanel (), 
		        org.ametys.administration.Plugins._drawHelpPanel ()]
	});
	
	org.ametys.administration.Plugins._tree = new Ext.tree.TreePanel({
		autoScroll: true,
		root: rootNode
	});

	org.ametys.administration.Plugins._tree2 = new Ext.tree.TreePanel({
		autoScroll: true,
		root: rootNode2
	});

	org.ametys.administration.Plugins._tree3 = new Ext.tree.TreePanel({
		autoScroll: true,
		root: rootNode3
	});

	org.ametys.administration.Plugins._mainPanel = new Ext.Panel({
		region:'center',
		id:'plugin-card-panel',
		baseCls: 'transparent-panel',
		border: false,
		autoScroll : true,
		height: 'auto',
		layout: 'card',
		activeItem: 0,
		
		items: [org.ametys.administration.Plugins._tree, org.ametys.administration.Plugins._tree2, org.ametys.administration.Plugins._tree3]
	});		
	
	return new Ext.Panel({
		region: 'center',
		
		baseCls: 'transparent-panel',
		border: false,
		layout: 'border',
		autoScroll: true,
		
		items: [org.ametys.administration.Plugins._mainPanel, 
		        org.ametys.administration.Plugins._contextualPanel]
	});
	
}

org.ametys.administration.Plugins._drawNavigationPanel = function ()
{
	org.ametys.administration.Plugins._nav = new org.ametys.NavigationPanel ({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_MENU"/>"});
	
	org.ametys.administration.Plugins._nav.add(
		new org.ametys.NavigationItem ({
			text: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_PLUGIN_VIEW"/>",
			activeItem: 0,
			cardLayout: 'plugin-card-panel',
			toggleGroup : 'plugin-menu'
		})
	);
	org.ametys.administration.Plugins._nav.add(
		new org.ametys.NavigationItem ({
			text: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_EXTENSION_POINT_VIEW"/>",
			activeItem: 1,
			cardLayout: 'plugin-card-panel',
			toggleGroup : 'plugin-menu'
		})
	);
	org.ametys.administration.Plugins._nav.add(
		new org.ametys.NavigationItem ({
			text: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_WORKSPACE_VIEW"/>",
			activeItem: 2,
			cardLayout: 'plugin-card-panel',
			toggleGroup : 'plugin-menu'
		})
	);
	
	return org.ametys.administration.Plugins._nav;
}

org.ametys.administration.Plugins._drawActionPanel = function ()
{
	org.ametys.administration.Plugins._actions = new org.ametys.ActionsPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_HANDLE"/>"});
	
	// Quit
	org.ametys.administration.Plugins._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CANCEL"/>", 
														 getPluginResourcesUrl(org.ametys.administration.Plugins.pluginName) + '/img/administrator/plugins/quit.png', 
														 org.ametys.administration.Plugins.goBack);
	
	return org.ametys.administration.Plugins._actions;
}

org.ametys.administration.Plugins._drawHelpPanel = function ()
{
	var helpPanel = new org.ametys.TextPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_HELP"/>"});
	helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_HELP_TEXT"/>");
	
	return helpPanel;
}
/**
 * Back to the adminsitration home page
 */
org.ametys.administration.Plugins.goBack = function ()
{
    document.location.href = context.workspaceContext;
}   