Ext.namespace('org.ametys.administration.Plugins');

org.ametys.administration.Plugins = function ()
{
}

org.ametys.administration.Plugins.initialize = function (pluginName)
{
	org.ametys.administration.Plugins.pluginName = pluginName;
}

org.ametys.administration.Plugins.createPanel = function ()
{
	org.ametys.administration.Plugins._contextualPanel = new org.ametys.HtmlContainer({
		region:'east',
		
		border: false,
		cls: 'admin-right-panel',
		width: 277,
	    
		items: [org.ametys.administration.Plugins._drawActionPanel (), 
		        org.ametys.administration.Plugins._drawHelpPanel ()]
	});
	
	org.ametys.administration.Plugins._mainPanel = new Ext.Panel({
		region:'center',
		
		baseCls: 'transparent-panel',
		border: false,
		autoScroll : true,
		
		html: '&lt;i&gt;En construction ...&lt;/i&gt;'
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