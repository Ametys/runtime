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

Ext.namespace('org.ametys.administration.JVMStatus');

org.ametys.administration.JVMStatus = function ()
{
}

org.ametys.administration.JVMStatus.initialize = function (pluginName)
{
	org.ametys.administration.JVMStatus.pluginName = pluginName;
}

org.ametys.administration.JVMStatus.createPanel = function ()
{
	org.ametys.administration.JVMStatus._cardPanel = new Ext.Panel({
		region:'center',
		layout:'card',
		activeItem: 0,
		
		id:'system-card-panel',
		baseCls: 'transparent-panel',
		border: false,
		autoScroll: false,
		
		items: [org.ametys.administration.JVMStatus._drawStatusPanel (),
		        org.ametys.administration.JVMStatus._drawPropertiesPanel (),
		        org.ametys.administration.JVMStatus._drawMonitoringPanel ()
		]
	});		
	
	org.ametys.administration.JVMStatus._contextualPanel = new org.ametys.HtmlContainer({
		region:'east',
	
		cls : 'admin-right-panel',
		border: false,
		width: 277,
	    
		items: [org.ametys.administration.JVMStatus._drawNavigationPanel (),
		        org.ametys.administration.JVMStatus._drawActionsPanel (),
		        org.ametys.administration.JVMStatus._drawHelpPanel ()]
	});
	
	return new Ext.Panel({
		region: 'center',
		
		autoScroll: false,
		baseCls: 'transparent-panel',
		border: false,
		layout: 'border',
		
		items: [org.ametys.administration.JVMStatus._cardPanel , 
		        org.ametys.administration.JVMStatus._contextualPanel]
	});
}

org.ametys.administration.JVMStatus._drawStatusPanel = function ()
{
	org.ametys.administration.JVMStatus._statusPanel =  new Ext.Panel ({
		id : 'general-panel',
		
		baseCls: 'transparent-panel',
		border: false,
		autoScroll: true,
		
		html: ''
	});
	
	return org.ametys.administration.JVMStatus._statusPanel;
}

org.ametys.administration.JVMStatus._drawPropertiesPanel = function ()
{
	org.ametys.administration.JVMStatus._propertyPanel = new org.ametys.ListView({
		autoScroll: true,
		
		baseCls: 'properties-view',
		id : 'properties-panel',
		
		store: new Ext.data.SimpleStore({
			id:0,
	        fields: [
	           {name: 'name'},
	           {name: 'value'}
	        ]
		}),
		
		hideHeaders : true,
		columns: [
			        {width : 235, menuDisabled : true, sortable: true, dataIndex: 'name'},
			        {width : 355, menuDisabled : true, sortable: true, dataIndex: 'value'}
	    ]
	});
	
	return org.ametys.administration.JVMStatus._propertyPanel;
}

org.ametys.administration.JVMStatus._nextImg = function(id, dir)
{
	var img = Ext.get('img-' + id);
	var src = img.dom.src;
	
	var currentPeriod = src.substring(src.lastIndexOf("/") + 1, src.length - 4);
	
	src = src.substring(0, src.lastIndexOf("/") + 1);
	for (var i = 0; i &lt; org.ametys.administration.JVMStatus.periods.length; i++)
	{
		if (org.ametys.administration.JVMStatus.periods[i] == currentPeriod)
		{
			src += org.ametys.administration.JVMStatus.periods[i + dir]
            if (i + dir == 0)
            {
            	Ext.get("btn-" + id + "-left").hide();
            	Ext.get("btn-" + id + "-right").show();
            }
            else if (i + dir == org.ametys.administration.JVMStatus.periods.length - 1)
            {
            	Ext.get("btn-" + id + "-left").show();
            	Ext.get("btn-" + id + "-right").hide();
            }
            else
            {
            	Ext.get("btn-" + id + "-left").show();
            	Ext.get("btn-" + id + "-right").show();
            }
			break;
		}
	}
	src += '.png';
		
	img.dom.src = src;
}

org.ametys.administration.JVMStatus._drawMonitoringPanel = function ()
{
	var items = [];
	for (var i = 0; i &lt; org.ametys.administration.JVMStatus.samples.length; i++)
	{
		var id = org.ametys.administration.JVMStatus.samples[i].id;
		var label = org.ametys.administration.JVMStatus.samples[i].label;
		var description = org.ametys.administration.JVMStatus.samples[i].description;
		
	    items.push(new org.ametys.Fieldset({
	    	title : label,
	    		
	    	html: '&lt;div class="monitoring"&gt;'
	    		+ '    &lt;button style="border-left-style: none;" id="btn-' + id + '-left" onclick="org.ametys.administration.JVMStatus._nextImg(\'' + id + '\', -1); return false;"&gt;&amp;lt;&amp;lt;&lt;/button&gt;'
	    		+ '    &lt;img id="img-' + id + '" src="' + getPluginDirectUrl("core") + '/administrator/jvmstatus/monitoring/' + id + '/' + org.ametys.administration.JVMStatus.periods[1] + '.png" title="' + description + '"/&gt;'
	    		+ '    &lt;button style="border-right-style: none;" id="btn-' + id + '-right"  onclick="org.ametys.administration.JVMStatus._nextImg(\'' + id + '\', +1); return false;"&gt;&amp;gt;&amp;gt;&lt;/button&gt;'
	    		+ '&lt;br/&gt;&lt;a target="_blank" href="' + getPluginDirectUrl("core") + '/administrator/jvmstatus/monitoring/' + id + '.xml"&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_MONITORING_EXPORT"/>&lt;a&gt;'
	    	    + '&lt;/div&gt;'
	    }));
	}
	
	org.ametys.administration.JVMStatus._monitoringPanel = new Ext.Panel({
		id: 'monitoring-panel',
		
		baseCls: 'transparent-panel',
		border: false,
		autoScroll: true,
		
		items: items
	});

	return org.ametys.administration.JVMStatus._monitoringPanel;
}

org.ametys.administration.JVMStatus._navItems;
/**
 * Draw the navigation panel. This function needs the org.ametys.administration.JVMStatus._navItems was filled first.
 * @return {org.ametys.NavigationPanel} The navigation panel
 * @private
 */
org.ametys.administration.JVMStatus._drawNavigationPanel = function ()
{
	org.ametys.administration.JVMStatus._nav = new org.ametys.NavigationPanel ({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_MENU"/>"});
	
	for (var i=0; i &lt; org.ametys.administration.JVMStatus._navItems.length; i++)
	{
		var item = new org.ametys.NavigationItem ({
			text: org.ametys.administration.JVMStatus._navItems[i].label,
			
			activeItem: i,
			
			cardLayout: 'system-card-panel',
			toggleGroup : 'system-menu',
			
			pressed: i == 0
		});
		
		org.ametys.administration.JVMStatus._nav.add(item);
	}
	
	return org.ametys.administration.JVMStatus._nav;
}

/**
 * Draw the actions panel.
 * @return {org.ametys.ActionsPanel} The action panel
 * @private
 */
org.ametys.administration.JVMStatus._drawActionsPanel = function ()
{
	org.ametys.administration.JVMStatus._actions = new org.ametys.ActionsPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_HANDLE"/>"});
	
	// Quit action
	org.ametys.administration.JVMStatus._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_HANDLE_QUIT"/>", 
				     getPluginResourcesUrl(org.ametys.administration.JVMStatus.pluginName) + '/img/administrator/jvmstatus/quit.png',
				     org.ametys.administration.JVMStatus.goBack);
	
	return org.ametys.administration.JVMStatus._actions;
}

/**
 * Draw the help panel.
 * @return {org.ametys.TextPanel} The help panel
 * @private
 */
org.ametys.administration.JVMStatus._drawHelpPanel = function ()
{
	var helpPanel = new org.ametys.TextPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_HELP"/>"});
	helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_GENERAL_HELP_TEXT"/>");
	helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_SYSTEM_HELP_TEXT"/>");
	helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_PROPERTIES_HELP_TEXT"/>");
	helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_MONITORING_HELP_TEXT"/>");
	
	return helpPanel;
}

/**
 * Quit
 */
org.ametys.administration.JVMStatus.goBack = function ()
{
    document.location.href = context.workspaceContext;
}

/**
 * Load the properties
 * @param {Object[]} data The data
 */
org.ametys.administration.JVMStatus.loadProperties = function (data)
{
	org.ametys.administration.JVMStatus._propertyPanel.getStore().loadData(data);
}

org.ametys.administration.JVMStatus.addFieldSet = function (title, contentEl)
{
	var fd = new org.ametys.Fieldset({
		title : title,
		
		items : [new org.ametys.HtmlContainer ({
					contentEl : contentEl
				})
		]
	});
	
	org.ametys.administration.JVMStatus._statusPanel.add(fd);
	
}
/**
 * Refresh
 */
org.ametys.administration.JVMStatus.refreshData = function (gc)
{
	var serverMessage = new org.ametys.servercomm.ServerMessage(org.ametys.administration.JVMStatus.pluginName, "administrator/jvmstatus/refresh", gc ? { gc: "gc" } : {}, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
	var result = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);

    if (org.ametys.servercomm.ServerComm.handleBadResponse(gc ? "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_GC_ERROR"/>" : "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_REFRESH_ERROR"/>", result, "org.ametys.administration.JVMStatus.refreshData"))
    {
       window.clearInterval(org.ametys.administration.JVMStatus._at);
       return false;
    }

    // HEAP MEMORY
    var commitedMem = result.selectSingleNode("status/general/memory/heap/commited")[org.ametys.servercomm.ServerComm.xmlTextContent];
    var usedMem = result.selectSingleNode("status/general/memory/heap/used")[org.ametys.servercomm.ServerComm.xmlTextContent];
    var maxMem = result.selectSingleNode("status/general/memory/heap/max")[org.ametys.servercomm.ServerComm.xmlTextContent];
    
    var tip  = "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_USED"/> : " + Math.round(usedMem / (1024*1024) * 10) / 10 + " <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT"/>"
            + "\n<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_FREE"/> : " + Math.round((commitedMem - usedMem) / (1024*1024) * 10) / 10 + " <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT"/>"
            + "\n<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_AVAILABLE"/> : " + Math.round((maxMem-commitedMem) / (1024*1024) * 10) / 10  + " <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT"/>";
    
    document.getElementById("totalMemImg").title = tip;
    document.getElementById("freeMemImg").title = tip;
    document.getElementById("maxMemImg").title = tip;
    
    document.getElementById("middleMem").innerHTML = Math.round((maxMem/2) / (1024*1024));
    document.getElementById("maxiMem").innerHTML = Math.round((maxMem) / (1024*1024));

    var v1 = Math.round(usedMem/maxMem * 280);
    var v2 = Math.round((commitedMem - usedMem)/maxMem * 280);
    document.getElementById("totalMemImg").width = v1;
    document.getElementById("freeMemImg").width = v2;
    document.getElementById("maxMemImg").width = 280 - v1 - v2;
    
    // non HEAP MEMORY
    var commitedMem = result.selectSingleNode("status/general/memory/nonHeap/commited")[org.ametys.servercomm.ServerComm.xmlTextContent];
    var usedMem = result.selectSingleNode("status/general/memory/nonHeap/used")[org.ametys.servercomm.ServerComm.xmlTextContent];
    var maxMem = result.selectSingleNode("status/general/memory/nonHeap/max")[org.ametys.servercomm.ServerComm.xmlTextContent];
    
    var tip  = "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_USED"/> : " + Math.round(usedMem / (1024*1024) * 10) / 10 + " <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT"/>"
            + "\n<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_FREE"/> : " + Math.round((commitedMem - usedMem) / (1024*1024) * 10) / 10 + " <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT"/>"
            + "\n<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_AVAILABLE"/> : " + Math.round((maxMem-commitedMem) / (1024*1024) * 10) / 10  + " <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT"/>";
    document.getElementById("totalMem2Img").title = tip;
    document.getElementById("freeMem2Img").title = tip;
    document.getElementById("maxMem2Img").title = tip;

    document.getElementById("middleMem2").innerHTML = Math.round((maxMem/2) / (1024*1024));
    document.getElementById("maxiMem2").innerHTML = Math.round((maxMem) / (1024*1024));

    var v1 = Math.round(usedMem/maxMem * 280);
    var v2 = Math.round((commitedMem - usedMem)/maxMem * 280);
    document.getElementById("totalMem2Img").width = v1;
    document.getElementById("freeMem2Img").width = v2;
    document.getElementById("maxMem2Img").width = 280 - v1 - v2;
    
    // ACTIVE SESSION
    var sessions = result.selectSingleNode("status/general/activeSessions");
    if (sessions == null) 
        document.getElementById("activeSession").innerHTML = "&lt;a href='#' onclick='org.ametys.administration.JVMStatus.helpSessions(); return false;'&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_SESSIONS_ERROR"/>&lt;/a&gt;";
    else
        document.getElementById("activeSession").innerHTML = sessions[org.ametys.servercomm.ServerComm.xmlTextContent];

    // ACTIVE REQUEST
    var sessions = result.selectSingleNode("status/general/activeRequests");
    if (sessions == null) 
        document.getElementById("activeRequest").innerHTML = "&lt;a href='#' onclick='org.ametys.administration.JVMStatus.helpRequests(); return false;'&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_REQUESTS_ERROR"/>&lt;/a&gt;";
    else
        document.getElementById("activeRequest").innerHTML = sessions[org.ametys.servercomm.ServerComm.xmlTextContent];

    // ACTIVE THREAD
    document.getElementById("activeThread").innerHTML = result.selectSingleNode("status/general/activeThreads")[org.ametys.servercomm.ServerComm.xmlTextContent];
    var locked = result.selectSingleNode("status/general/deadlockThreads")[org.ametys.servercomm.ServerComm.xmlTextContent];
    if (locked != "0")
        document.getElementById("deadlockThread").innerHTML = "(&lt;a href='#' title='<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_THREADS_ERROR_LOCK_HINT"/>' style='color: red; font-weight: bold' onclick='org.ametys.administration.JVMStatus.deadLock()'&gt;" + locked + " <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_THREADS_LOCK"/>&lt;/a&gt;)";
    
    // TIME
    document.getElementById("osTime").innerHTML = result.selectSingleNode("status/general/osTime")[org.ametys.servercomm.ServerComm.xmlTextContent];
    
    return true;
}

org.ametys.administration.JVMStatus._at;
org.ametys.administration.JVMStatus.onReady = function ()
{
	if (org.ametys.administration.JVMStatus.refreshData())
    {
		org.ametys.administration.JVMStatus._at = window.setInterval("org.ametys.administration.JVMStatus.refreshData()", 10000);
    }
    
    new Ext.ToolTip({
        target: 'mem-heap-help-img',
        html: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_MEMORY_HINT_HEAP"/>"
    });
    new Ext.ToolTip({
        target: 'mem-nheap-help-img',
        html: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_MEMORY_HINT_NHEAP"/>"
    });
    new Ext.ToolTip({
        target: 'handle-session-help-img',
        html: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_SESSION_HELP"/>"
    });
    new Ext.ToolTip({
        target: 'handle-request-help-img',
        html: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_REQUEST_HELP"/>"
    });
    new Ext.ToolTip({
        target: 'handle-thread-help-img',
        html: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_THREAD_HELP"/>"
    });
}

org.ametys.administration.JVMStatus.deadLock = function ()
{
    var d = new Date();
    var year = d.getFullYear();
    var month = d.getMonth() + 1;
    var day = d.getDate();
    
    window.location.href = getPluginDirectUrl(org.ametys.administration.JVMStatus.pluginName) + "/administrator/jvmstatus/threads_" + year + "-" + (day.length == 1 ? '0' : '') + month + "-" + (month.length == 1 ? '0' : '') + day + "-T-" + d.getHours() + "-" + d.getMinutes() + ".log";
}

org.ametys.administration.JVMStatus.helpSessions = function ()
{
	Ext.Msg.show ({
		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
		msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_SESSIONS_ERROR_HINT"/>",
		buttons: Ext.Msg.OK,
		icon: Ext.MessageBox.WARNING
    });
}

org.ametys.administration.JVMStatus.helpRequests = function ()
{
	Ext.Msg.show ({
		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
		msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_REQUESTS_ERROR_HINT"/>",
		buttons: Ext.Msg.OK,
		icon: Ext.MessageBox.ERROR
    });
}