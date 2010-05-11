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
		listeners: {'show': org.ametys.administration.Plugins._computeShowHide, 'hide': org.ametys.administration.Plugins._computeShowHide},
		root: rootNode
	});
	org.ametys.administration.Plugins._tree.getSelectionModel().on('selectionchange', org.ametys.administration.Plugins._computeShowHide);

	org.ametys.administration.Plugins._tree2 = new Ext.tree.TreePanel({
		autoScroll: true,
		listeners: {'show': org.ametys.administration.Plugins._computeShowHide, 'hide': org.ametys.administration.Plugins._computeShowHide},
		root: rootNode2
	});
	org.ametys.administration.Plugins._tree2.getSelectionModel().on('selectionchange', org.ametys.administration.Plugins._computeShowHide);

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

org.ametys.administration.Plugins._ACTION_SELECT = 0;
org.ametys.administration.Plugins._ACTION_ACTIVATE = 1;
org.ametys.administration.Plugins._ACTION_DEACTIVATE = 2;
org.ametys.administration.Plugins._ACTION_CHANGES = 3;
org.ametys.administration.Plugins._drawActionPanel = function ()
{
	org.ametys.administration.Plugins._actions = new org.ametys.ActionsPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_HANDLE"/>"});
	
	// Choose
	org.ametys.administration.Plugins._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHOOSE"/>", 
														 getPluginResourcesUrl(org.ametys.administration.Plugins.pluginName) + '/img/administrator/plugins/select.png', 
														 org.ametys.administration.Plugins.select);

	// Activate
	org.ametys.administration.Plugins._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_ACTIVATE"/>", 
														 getPluginResourcesUrl(org.ametys.administration.Plugins.pluginName) + '/img/administrator/plugins/activate.png', 
														 org.ametys.administration.Plugins.activate);

	// Deactivate
	org.ametys.administration.Plugins._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_DEACTIVATE"/>", 
														 getPluginResourcesUrl(org.ametys.administration.Plugins.pluginName) + '/img/administrator/plugins/deactivate.png', 
														 org.ametys.administration.Plugins.deactivate);

	// Changes
	org.ametys.administration.Plugins._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES"/>", 
														 getPluginResourcesUrl(org.ametys.administration.Plugins.pluginName) + '/img/administrator/plugins/changes.png', 
														 org.ametys.administration.Plugins.changes);
	
	// Documentation
	org.ametys.administration.Plugins._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_DOC"/>", 
														 getPluginResourcesUrl(org.ametys.administration.Plugins.pluginName) + '/img/administrator/plugins/doc.png', 
														 org.ametys.administration.Plugins.openDoc);
	
	// Quit
	org.ametys.administration.Plugins._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CANCEL"/>", 
														 getPluginResourcesUrl(org.ametys.administration.Plugins.pluginName) + '/img/administrator/plugins/quit.png', 
														 org.ametys.administration.Plugins.goBack);
	
	org.ametys.administration.Plugins._actions.hideElt(org.ametys.administration.Plugins._ACTION_SELECT);
	org.ametys.administration.Plugins._actions.hideElt(org.ametys.administration.Plugins._ACTION_ACTIVATE);
	org.ametys.administration.Plugins._actions.hideElt(org.ametys.administration.Plugins._ACTION_DEACTIVATE);
	org.ametys.administration.Plugins._actions.hideElt(org.ametys.administration.Plugins._ACTION_CHANGES);
	
	return org.ametys.administration.Plugins._actions;
}

org.ametys.administration.Plugins._drawHelpPanel = function ()
{
	var helpPanel = new org.ametys.TextPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_HELP"/>"});
	helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_HELP_TEXT"/>");
	
	return helpPanel;
}

org.ametys.administration.Plugins._computeShowHide = function()
{
	var node = null;
	if (org.ametys.administration.Plugins._mainPanel.layout.activeItem == org.ametys.administration.Plugins._tree)
	{
		node = org.ametys.administration.Plugins._tree.getSelectionModel().getSelectedNode();
	}
	else if (org.ametys.administration.Plugins._mainPanel.layout.activeItem == org.ametys.administration.Plugins._tree2)
	{
		node = org.ametys.administration.Plugins._tree2.getSelectionModel().getSelectedNode();
	}
	
	if (node != null &amp;&amp; node.attributes.type == "extension" &amp;&amp; node.parentNode.attributes.isMultiple == "false")
	{
		org.ametys.administration.Plugins._actions.showElt(org.ametys.administration.Plugins._ACTION_SELECT);
	}
	else
	{
		org.ametys.administration.Plugins._actions.hideElt(org.ametys.administration.Plugins._ACTION_SELECT);
	}
	
	if (node != null &amp;&amp; node.attributes.type == "feature" &amp;&amp; node.attributes.active)
	{
		org.ametys.administration.Plugins._actions.showElt(org.ametys.administration.Plugins._ACTION_DEACTIVATE);
	}
	else
	{
		org.ametys.administration.Plugins._actions.hideElt(org.ametys.administration.Plugins._ACTION_DEACTIVATE);
	}

	if (node != null &amp;&amp; node.attributes.type == "feature" &amp;&amp; !node.attributes.active &amp;&amp; node.attributes.cause == "EXCLUDED")
	{
		org.ametys.administration.Plugins._actions.showElt(org.ametys.administration.Plugins._ACTION_ACTIVATE);
	}
	else
	{
		org.ametys.administration.Plugins._actions.hideElt(org.ametys.administration.Plugins._ACTION_ACTIVATE);
	}
}

/**
 * Open the plugin doc
 */
org.ametys.administration.Plugins.openDoc = function()
{
	var zoom = "";
	var node = null;
	if (org.ametys.administration.Plugins._mainPanel.layout.activeItem == org.ametys.administration.Plugins._tree)
	{
		node = org.ametys.administration.Plugins._tree.getSelectionModel().getSelectedNode();
	}
	else if (org.ametys.administration.Plugins._mainPanel.layout.activeItem == org.ametys.administration.Plugins._tree2)
	{
		node = org.ametys.administration.Plugins._tree2.getSelectionModel().getSelectedNode();
	}
	
	if (node != null)
	{
		if (node.attributes.type == "plugin")
		{
			zoom = node.attributes.pluginName + "_main.html";
		}
		else if (node.attributes.type == "feature")
		{
			zoom = node.attributes.pluginName + "_features.html%23feature_" + node.attributes.featureName;
		}
		else if (node.attributes.type == "component")
		{
			zoom = node.attributes.pluginName + "_features.html%23feature_" + node.attributes.featureName + "_component_" + node.attributes.componentName;
		}
		else if (node.attributes.type == "extension-point")
		{
			zoom = node.attributes.pluginName + "_extensions.html%23extension_point_" + node.attributes.extensionPointName;
		}
		else if (node.attributes.type == "extension")
		{
			zoom = node.attributes.pluginName + "_features.html%23feature_" + node.attributes.featureName + "_extension_" + node.attributes.extensionId;
		}
	}
	
	window.open(getPluginDirectUrl(org.ametys.administration.Plugins.pluginName) + "/administrator/plugins/doc/index.html?zoom=" + zoom, "plugindoc", "");
}

/**
 * @private
 * @property {boolean} _changes When 'true' there is at least a change to confirm
 */
org.ametys.administration.Plugins._changes = false;
/**
 * @private
 * @property {Map&lt;String, String&gt;} _SEP
 * This association of single extension point name and extension to choose.
 * This association records the changes wanted until there are saved
 */
org.ametys.administration.Plugins._SEP = {};
/**
 * @private
 * @property {Map&lt;String, boolean&gt;} _EP
 * This association of multiple extension point name and true to active / false to deactive.
 * This association records the changes wanted until there are saved
 */
org.ametys.administration.Plugins._EP = {};

/**
 * Select this single extension
 */
org.ametys.administration.Plugins.select = function ()
{
	var node = null;
	if (org.ametys.administration.Plugins._mainPanel.layout.activeItem == org.ametys.administration.Plugins._tree)
	{
		node = org.ametys.administration.Plugins._tree.getSelectionModel().getSelectedNode();
	}
	else if (org.ametys.administration.Plugins._mainPanel.layout.activeItem == org.ametys.administration.Plugins._tree2)
	{
		node = org.ametys.administration.Plugins._tree2.getSelectionModel().getSelectedNode();
	}
	
	if (node != null)
	{
		org.ametys.administration.Plugins._SEP[node.parentNode.attributes.extensionPointName] = node.attributes.extensionId;
		org.ametys.administration.Plugins._actions.showElt(org.ametys.administration.Plugins._ACTION_CHANGES);
		
		Ext.MessageBox.alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_ALERT_TITLE"/>", "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_ALERT_TEXT"/>");
		org.ametys.administration.Plugins._changes = true;
	}
}

/**
 * Activate a feature
 */
org.ametys.administration.Plugins.activate = function (status)
{
	status = (status != false) ? true : false;
	
	var node = null;
	if (org.ametys.administration.Plugins._mainPanel.layout.activeItem == org.ametys.administration.Plugins._tree)
	{
		node = org.ametys.administration.Plugins._tree.getSelectionModel().getSelectedNode();
	}
	else if (org.ametys.administration.Plugins._mainPanel.layout.activeItem == org.ametys.administration.Plugins._tree2)
	{
		node = org.ametys.administration.Plugins._tree2.getSelectionModel().getSelectedNode();
	}
	
	if (node != null)
	{
		org.ametys.administration.Plugins._EP[node.attributes.pluginName + "/" + node.attributes.featureName] = status;
		org.ametys.administration.Plugins._actions.showElt(org.ametys.administration.Plugins._ACTION_CHANGES);
		
		Ext.MessageBox.alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_ALERT_TITLE"/>", "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_ALERT_TEXT"/>");
	}
}

/**
 * Deactivate a feature
 */
org.ametys.administration.Plugins.deactivate = function ()
{
	org.ametys.administration.Plugins.activate(false);
}

/**
 * Display the changes
 */
org.ametys.administration.Plugins.changes = function ()
{
	Ext.MessageBox.alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_BEWARE_TITLE"/>", 
			"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_BEWARE_TEXT"/>",
			function ()
			{
				var sepchanges = "";
				for (var i in org.ametys.administration.Plugins._SEP)
				{
					sepchanges += i + " : " + org.ametys.administration.Plugins._SEP[i] + "&lt;br/&gt;";
				}
				var epchanges = "";
				for (var i in org.ametys.administration.Plugins._EP)
				{
					epchanges += i + " : " + (org.ametys.administration.Plugins._EP[i] ? "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_ACTIVATE"/>" : "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_DEACTIVATE"/>") + "&lt;br/&gt;";
				}
				
				
				var changes = "";
				if (sepchanges != "")
				{
					changes += "&lt;b&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_SEP"/>&lt;/b&gt;&lt;br/&gt;"
							   + sepchanges;
				}
				if (epchanges != "")
				{
					changes += "&lt;b&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_EP"/>&lt;/b&gt;&lt;br/&gt;"
							   + epchanges;
				}
				
				Ext.MessageBox.confirm("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_CONFIRM_TITLE"/>", 
						"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_CONFIRM_TEXT_START"/>&lt;ul&gt;"
						+ changes
						+ "&lt;/ul&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_CONFIRM_TEXT_END"/>", 
						function(val) { if (val == "yes") { org.ametys.administration.Plugins._changesNow(); } }, this);
			});
}
org.ametys.administration.Plugins._changesNow = function()
{
	var params = {};
	params.EP = org.ametys.administration.Plugins._EP;
	params.SEP = org.ametys.administration.Plugins._SEP;
	
	var serverMessage = new org.ametys.servercomm.ServerMessage(org.ametys.administration.Plugins.pluginName, "administrator/plugins/change", params, org.ametys.servercomm.ServerComm.PRIORITY_MAJOR, org.ametys.administration.Plugins._changesNowCB, this);
	org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);

    org.ametys.administration.Plugins._mask = new org.ametys.msg.Mask();
}

org.ametys.administration.Plugins._changesNowCB = function(response)
{
	org.ametys.administration.Plugins._mask.hide();

    if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_ERROR"/>", response, "org.ametys.administration.Groups._selectGroup"))
    {
       return;
    }

    alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CHANGES_DONE"/>");

    org.ametys.administration.Plugins._mask = new org.ametys.msg.Mask();
    document.location.reload(true);
}

/**
 * Back to the adminsitration home page
 */
org.ametys.administration.Plugins.goBack = function ()
{
    document.location.href = context.workspaceContext;
}   