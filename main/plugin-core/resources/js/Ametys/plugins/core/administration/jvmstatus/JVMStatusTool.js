/*
 *  Copyright 2015 Anyware Services
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
 * This tool displays the general status of the JVM
 * @private
 */
Ext.define('Ametys.plugins.core.administration.jvmstatus.JVMStatusTool', {
	extend: 'Ametys.tool.Tool',
	
	statics: {
		
		/**
		 * Runs a garbage collect
		 */
		garbageCollect: function ()
		{
			var tool = Ametys.tool.ToolsManager.getTool('uitool-admin-jvmstatus');
			
			Ametys.data.ServerComm.callMethod({
				role: "org.ametys.runtime.plugins.core.administrator.jvmstatus.JVMStatusHelper",
				methodName: "garbageCollect",
				parameters: [],
				callback: {
					handler: tool != null ? tool.refreshData : Ext.emptyFn,
					scope: tool || this
				},
				errorMessage: {
					category: 'Ametys.plugins.core.administration.jvmstatus.JVMStatusTool.garbageCollect',
					msg: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_TOOL_JVMSTATUS_SERVER_ERROR'/>"
				}
			});
		}
	},
		
	/**
	 * @private
	 * @property {Ext.Container} _jvmStatusPanel The JVM status main panel
	 */
	
	constructor: function(config)
	{
		this.callParent(arguments);
	},
	
	getMBSelectionInteraction: function() 
	{
		return Ametys.tool.Tool.MB_TYPE_NOSELECTION;
	},
	
	createPanel: function ()
	{
		this._jvmStatusPanel = Ext.create('Ext.Container', {
									border: false,
									autoScroll: true,
									cls: 'jvmstatus-tool',
									
									defaults: {
										cls: 'jvmstatus-fd',
										collapsible: true,
										titleCollapse: true,
										hideCollapseTool: true,
										
										border: false,
										shadow: false
									},
									
									items: [{
    										xtype: 'panel',
    										itemId: 'system',
    										title : "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_SYS'/>",
    										html: ''
    									},
    									{
    										xtype: 'panel',
    										itemId: 'jvm',
    										title : "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_JVM'/>",
    										html: ''
    									},{
    										xtype: 'panel',
    										itemId: 'memory',
    										title : "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM'/>",
    										html: ''
    									},{
    										xtype: 'panel',
    										itemId: 'server',
    										title : "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE'/>",
    										html: ''
    									}
									]
								});
		
		return this._jvmStatusPanel;
	},
	
	setParams: function (params)
	{
		this.callParent(arguments);
		this.refresh();
	},
	
	/**
	 * Refreshes the tool
	 */
	refresh: function ()
	{
		this.showRefreshing();

		Ametys.data.ServerComm.send({
			plugin: 'core', 
			url: 'administrator/jvmstatus',
			parameters: {},
			priority: Ametys.data.ServerComm.PRIORITY_MAJOR,
			errorMessage: {
				category: this.self.getName(),
				msg: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_TOOL_JVMSTATUS_SERVER_ERROR'/>"
			},
			callback: {
				handler: this._refreshCb,
				scope: this
			}
		});
	},
	
	/**
	 * @private
	 * Callback for the refreshing process
	 * @param {Object} response the server's xml response
	 * @param {Object[]} args the callback arguments
	 * @param {Function} args.callback the callback 
	 */
	_refreshCb: function (response, args)
	{
		// Html for the 4 panels of the tool
		var html = Ext.dom.Query.jsSelect("div[id='system']", response)[0].innerHTML;
		this._jvmStatusPanel.down('#system').update(html);
		
		html = Ext.dom.Query.jsSelect("div[id='java']", response)[0].innerHTML;
		this._jvmStatusPanel.down('#jvm').update(html);
		
		html = Ext.dom.Query.jsSelect("div[id='memory']", response)[0].innerHTML;
		this._jvmStatusPanel.down('#memory').update(html);
		
		html = Ext.dom.Query.jsSelect("div[id='server']", response)[0].innerHTML;
		this._jvmStatusPanel.down('#server').update(html);
		
		// Tooltips
	    Ext.create ('Ext.ToolTip', {
	        target: 'mem-heap-help-img',
	        html: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_MEMORY_HINT_HEAP'/>"
	    });
	    Ext.create ('Ext.ToolTip', {
	        target: 'mem-nheap-help-img',
	        html: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_MEMORY_HINT_NHEAP'/>"
	    });
	    Ext.create ('Ext.ToolTip', {
	        target: 'handle-session-help-img',
	        html: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_SESSION_HELP'/>"
	    });
	    Ext.create ('Ext.ToolTip', {
	        target: 'handle-request-help-img',
	        html: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_REQUEST_HELP'/>"
	    });
	    Ext.create ('Ext.ToolTip', {
	        target: 'handle-thread-help-img',
	        html: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_THREAD_HELP'/>"
	    });
		
		// Refresh used memory
		 this.refreshData();
		 
		 this.showRefreshed();
	},
	
	/**
	 * Refresh the data displayed for status
	 * @private
	 */
	refreshData: function ()
	{
		Ametys.data.ServerComm.callMethod({
			role: "org.ametys.runtime.plugins.core.administrator.jvmstatus.JVMStatusHelper",
			methodName: "getGeneralStatus",
			parameters: [],
			callback: {
				scope: this,
				handler: this._refreshDataCb
			},
			errorMessage: {
				category: this.self.getName(),
				msg: "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_TOOL_JVMSTATUS_SERVER_ERROR'/>"
			}
		});
	},

    /**
     * @private
     * The callback of #refreshData
     * @param {Object} response The http response
     */
	_refreshDataCb: function(response)
	{
		console.info('refresh data');
	    if (response == null || !this.isNotDestroyed())
	    {
	       return;
	    }
	    
		// Refresh every 10 seconds
	    window.setTimeout(Ext.bind(this.refreshData, this), 10000);
	    
	    // HEAP MEMORY
	    var commitedMem = response['heap-memory-commited'];
	    var usedMem = response['heap-memory-used'];
	    var maxMem = response['heap-memory-max'];
	    
	    var tip  = "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_USED'/> : " + Math.round(usedMem / (1024*1024) * 10) / 10 + " <i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT'/>"
	            + "\n<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_FREE'/> : " + Math.round((commitedMem - usedMem) / (1024*1024) * 10) / 10 + " <i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT'/>"
	            + "\n<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_AVAILABLE'/> : " + Math.round((maxMem-commitedMem) / (1024*1024) * 10) / 10  + " <i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT'/>";
	    
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
	    commitedMem = response['non-heap-memory-commited'];
	    usedMem = response['non-heap-memory-used'];
	    maxMem = response['non-heap-memory-max'];
	    
	    var tip  = "<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_USED'/> : " + Math.round(usedMem / (1024*1024) * 10) / 10 + " <i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT'/>"
	            + "\n<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_FREE'/> : " + Math.round((commitedMem - usedMem) / (1024*1024) * 10) / 10 + " <i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT'/>"
	            + "\n<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_AVAILABLE'/> : " + Math.round((maxMem-commitedMem) / (1024*1024) * 10) / 10  + " <i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT'/>";
	    
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
	    var sessions = response.activeSessions;
	    if (sessions == null) 
	        document.getElementById("activeSession").innerHTML = "<a href='#' onclick='Ametys.plugins.core.administration.JVMStatus.helpSessions(); return false;'><i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_SESSIONS_ERROR'/></a>";
	    else
	        document.getElementById("activeSession").innerHTML = sessions;

	    // ACTIVE REQUEST
	    var requests = response.activeRequests;
	    if (requests == null) 
	        document.getElementById("activeRequest").innerHTML = "<a href='#' onclick='Ametys.plugins.core.administration.JVMStatus.helpRequests(); return false;'><i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_REQUESTS_ERROR'/></a>";
	    else
	        document.getElementById("activeRequest").innerHTML = requests;

	    // ACTIVE THREAD
	    document.getElementById("activeThread").innerHTML = response.activeThreads;
	    var locked = response.deadlockThreads;
	    if (locked != "0")
	    {
	        document.getElementById("deadlockThread").innerHTML = "(<a href='#' title='<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_THREADS_ERROR_LOCK_HINT'/>' style='color: red; font-weight: bold' onclick='Ametys.plugins.core.administration.JVMStatus.deadLock()'>" + locked + " <i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_STATUS_THREADS_LOCK'/></a>)";
		}
	    
	    // TIME
	    document.getElementById("startTime").innerHTML = Ext.Date.format(Ext.Date.parse(response.startTime, Ext.Date.patterns.ISO8601DateTime), Ext.Date.patterns.FullDateTime);
	    document.getElementById("osTime").innerHTML = Ext.Date.format(Ext.Date.parse(response.osTime, Ext.Date.patterns.ISO8601DateTime), Ext.Date.patterns.FullDateTime); 
	    return true;
	}
});
