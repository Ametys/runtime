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

org.ametys.administration.Logs.initialize = function (pluginName)
{
	org.ametys.administration.Logs.pluginName = pluginName;
}

org.ametys.administration.Logs.createPanel = function ()
{
	var reader = new Ext.data.ArrayReader({}, [
	                					       {name: 'location'},
	                					       {name: 'date', type: 'date', dateFormat: 'Y-m-dTH:i'},
	                					       {name: 'size', type: 'int'},
	                					       {name: 'file'}
	                					    ]);
	
	var store =  new Ext.data.GroupingStore({
	        reader: reader,
	        sortInfo:{field: 'location', direction: "ASC"},
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
		
		listeners: {'rowclick': org.ametys.administration.Logs._onSelectLog},
		
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
	
	org.ametys.administration.Logs._contextualPanel = new org.ametys.HtmlContainer({
		region:'east',
	
		cls : 'admin-right-panel',
		border: false,
		width: 277,
	    
		items: [org.ametys.administration.Logs._drawActionsPanel (),
		        org.ametys.administration.Logs._drawHelpPanel ()]
	});
	
	return new Ext.Panel({
		region: 'center',
		
		autoScroll: true,
		
		baseCls: 'transparent-panel',
		border: false,
		layout: 'border',
		
		items: [org.ametys.administration.Logs._logs , 
		        org.ametys.administration.Logs._contextualPanel],
		
		layoutConfig: {
        	autoWidth: true
    	}

	});
	
}

org.ametys.administration.Logs.load = function (data)
{
	org.ametys.administration.Logs._logs.getStore().loadData(data);
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
	
	// Quit
	org.ametys.administration.Logs._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_QUIT"/>", 
			 getPluginResourcesUrl(org.ametys.administration.Logs.pluginName) + '/img/administrator/logs/quit.png', 
			 org.ametys.administration.Logs.goBack);

	org.ametys.administration.Logs._actions.hideElt(0);
	org.ametys.administration.Logs._actions.hideElt(1);
	org.ametys.administration.Logs._actions.hideElt(2);
	
	return org.ametys.administration.Logs._actions;
}

/**
 * Draw the help panel.
 * @return {org.ametys.TextPanel} The help panel
 * @private
 */
org.ametys.administration.Logs._drawHelpPanel = function ()
{
	var helpPanel = new org.ametys.TextPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HELP"/>"});
	helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HELP_TEXT"/>");
	
	return helpPanel;
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
    					 org.ametys.administration.download);
    }
    else
    {
		window.location.href = getPluginDirectUrl(org.ametys.administration.Logs.pluginName) + "/administrator/logs/view/" + encodeURIComponent(elt.get('location'));
    }
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
		var url = getPluginDirectUrl(org.ametys.administration.Logs.pluginName) + "/administrator/logs/delete";
        var args = "";
    
        var elts = org.ametys.administration.Logs._logs.getSelection();
        for (var i = 0; i &lt; elts.length; i++)
        {
            var elt = elts[i];
            args += "file=" + encodeURIComponent(elt.get('location')) + "&amp;";
        }
        
        var result = Tools.postFromUrl(url, args);
        if (result == null)
        {
        	Ext.Msg.show({
				title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DELETE"/>",
				msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DELETE_ERROR_GRAVE"/>",
				buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.ERROR
			});
            return;
        }
        
        var failuresString = Tools.getFromXML(result, "failure");
        
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
        var url = getPluginDirectUrl(org.ametys.administration.Logs.pluginName) + "/administrator/logs/purge";

        var result = Tools.postFromUrl(url, "");
        if (result == null)
        {
        	Ext.Msg.show({
				title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_PURGE"/>",
				msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_PURGE_ERROR_GRAVE"/>",
				buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.ERROR
			});
            return;
        }
        
        var doneString = Tools.getFromXML(result, "done");
        
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
	org.ametys.administration.Logs._actions.showElt(0);
	org.ametys.administration.Logs._actions.showElt(1);
	org.ametys.administration.Logs._actions.showElt(2);
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
