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
 * Building the jvm status screen for administration workspace.
 * See {@link #initialize} and {@link #createPanel}.
 */
Ext.define('Ametys.plugins.core.administration.JVMStatus', {
	singleton: true,
	
	/**
	 * @cfg {String[]} periods (required) An array of existing periods to analyse monitoring. Can be ['hours', 'day', 'week', 'month', 'year']
	 */
	/**
	 * @cfg {Object[]} samples (required) An array of existing samples to analyse monitoring. Object have : 
	 * - String id : an id for the monitoring
	 * - String label : a readable name for the monitoring
	 * - String description a associated readable long description for the monitoring
	 */
	/**
	 * @cfg {String[]} _navItems (required) The labels to associate to the panel that will allow to navigate
	 */
	
	/**
	 * @private
	 * @property {String} pluginName The plugin declaring this file
	 */
	
	/**
	 * @private
	 * @property {Ext.Panel} _cardPanel The left panel displaying 'general', 'properties' or 'monitoring'
	 */
	/**
	 * @private
	 * @property {Ext.Panel} _statusPanel The main status panel (in the card panel)
	 */
	/**
	 * @private
	 * @property {Ext.grid.Panel} _propertyPanel The panel displaying properties (in the card panel)
	 */
	/**
	 * @private
	 * @property {Ext.Panel} _monitoringPanel The panel that displays monitoring graphs (in the card panel)
	 */
	
	/**
	 * @private
	 * @property {Ext.Component} _contextualPanel The right panel displaying text and actions
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
	 * @property {Number} _at The identifier for the interval call to {@link #refreshData}
	 */
	
	/**
	 * Initialize the class
	 * @param {String} pluginName The plugin declaring this file
	 */
	initialize: function (pluginName)
	{
		this.pluginName = pluginName;
	},

	/**
	 * Create the panel for displaying this screen
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
			
			items: [this._drawStatusPanel (),
			        this._drawPropertiesPanel (),
			        this._drawMonitoringPanel ()
			]
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
		
		return new Ext.Panel({
			region: 'center',
			
			autoScroll: true,
			baseCls: 'transparent-panel',
			
			border: false,
			layout: 'border',
			
			items: [this._cardPanel , 
			        this._contextualPanel]
		});
	},

	/**
	 * @private
	 * Draw the general panel showing status
	 */
	_drawStatusPanel: function ()
	{
		this._statusPanel =  new Ext.Panel ({
			id : 'general-panel',
			
			border: false,
			autoScroll: true,
			
			html: ''
		});
		
		return this._statusPanel;
	},
	
	/**
	 * @private
	 * Draw the panel displaying the system properties 
	 */
	_drawPropertiesPanel: function ()
	{
		var model = Ext.define('Ametys.plugins.core.administrator.JVMStatus.Properties', {
		    extend: 'Ext.data.Model',
		    fields: [
		        {name: 'name',  type: 'string'},
		        {name: 'value',  type: 'string'}
		    ]
		});

		var store = Ext.create('Ext.data.Store', {
			model: 'Ametys.plugins.core.administrator.JVMStatus.Properties',
	        data: { properties: []},
	        proxy: {
	        	type: 'memory',
	        	reader: {
	        		type: 'json',
	        		root: 'properties'
	        	}
	        }
		});

		this._propertyPanel = new Ext.grid.Panel({
			region: 'center',
			
			id : 'properties-panel',
			
		    store : store,
		    
		    hideHeaders : true,
		    columnmove : false,
		    columns: [
		        {width : 250, menuDisabled : true, sortable: true, dataIndex: 'name', flex: 0},
		        {width : 360, menuDisabled : true, sortable: true, dataIndex: 'value', flex: 1}
		    ]
		});	
		
		return this._propertyPanel;
	},

	/**
	 * @private
	 * This method is called to go to the next image in the monitoring panel 
	 */
	_nextImg: function(id, dir)
	{
		var img = Ext.get('img-' + id);
		var src = img.dom.src;
		
		var currentPeriod = src.substring(src.lastIndexOf("/") + 1, src.length - 4);
		
		src = src.substring(0, src.lastIndexOf("/") + 1);
		for (var i = 0; i &lt; this.periods.length; i++)
		{
			if (this.periods[i] == currentPeriod)
			{
				src += this.periods[i + dir]
	            if (i + dir == 0)
	            {
	            	Ext.get("btn-" + id + "-left").hide();
	            	Ext.get("btn-" + id + "-right").show();
	            }
	            else if (i + dir == this.periods.length - 1)
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
	},

	/**
	 * @private
	 * Draw the monitoring panel
	 */
	_drawMonitoringPanel: function ()
	{
		var items = [];
		for (var i = 0; i &lt; this.samples.length; i++)
		{
			var id = this.samples[i].id;
			var label = this.samples[i].label;
			var description = this.samples[i].description;
			
		    items.push(new Ext.panel.Panel({
		    	title : label,
		    		
		    	html: '&lt;div class="monitoring"&gt;'
		    		+ '    &lt;button style="border-left-style: none;" id="btn-' + id + '-left" onclick="org.ametys.administration.JVMStatus._nextImg(\'' + id + '\', -1); return false;"&gt;&amp;lt;&amp;lt;&lt;/button&gt;'
		    		+ '    &lt;img id="img-' + id + '" src="' + Ametys.getPluginDirectPrefix("core") + '/administrator/jvmstatus/monitoring/' + id + '/' + this.periods[1] + '.png" title="' + description + '"/&gt;'
		    		+ '    &lt;button style="border-right-style: none;" id="btn-' + id + '-right"  onclick="org.ametys.administration.JVMStatus._nextImg(\'' + id + '\', +1); return false;"&gt;&amp;gt;&amp;gt;&lt;/button&gt;'
		    		+ '&lt;br/&gt;&lt;a target="_blank" href="' + Ametys.getPluginDirectPrefix("core") + '/administrator/jvmstatus/monitoring/' + id + '.xml"&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_MONITORING_EXPORT"/>&lt;a&gt;'
		    	    + '&lt;/div&gt;'
		    }));
		}
		
		this._monitoringPanel = new Ext.Panel({
			id: 'monitoring-panel',
			
			border: false,
			autoScroll: true,
			
			items: items
		});

		return this._monitoringPanel;
	},
	
	/**
	 * @private
	 * Draw the navigation panel. This function needs the _navItems was filled first.
	 * @return {org.ametys.NavigationPanel} The navigation panel
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
				
				pressed: i == 0
			});
			
			this._nav.add(item);
		}
		
		return this._nav;
	},

	/**
	 * @private
	 * Draw the actions panel.
	 * @return {Ametys.workspace.admin.rightpanel.ActionPanel} The action panel
	 */
	_drawActionsPanel: function ()
	{
		this._actions = new Ametys.workspace.admin.rightpanel.ActionPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_HANDLE"/>"});
		
		// Quit action
		this._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_HANDLE_QUIT"/>", 
					     Ametys.getPluginResourcesPrefix(this.pluginName) + '/img/administrator/jvmstatus/quit.png',
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
		var helpPanel = new Ametys.workspace.admin.rightpanel.TextPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_HELP"/>"});
		helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_GENERAL_HELP_TEXT"/>");
		helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_SYSTEM_HELP_TEXT"/>");
		helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_PROPERTIES_HELP_TEXT"/>");
		helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_MONITORING_HELP_TEXT"/>");
		
		return helpPanel;
	},

	/**
	 * Quit the screen
	 */
	goBack: function ()
	{
		document.location.href = Ametys.WORKSPACE_URI;
	},

	/**
	 * Load the properties
	 * @param {Object[]} data The data
	 */
	loadProperties: function (data)
	{
		this._propertyPanel.getStore().loadData(data);
	},

	/**
	 * Add a panel in the general panel
	 * @param {String} title The panel title
	 * @param {String} contentEl The identifier of the content element in the existing html
	 */
	addFieldSet: function (title, contentEl)
	{
		var fd = new Ext.panel.Panel({
			title : title,

			collapsible: true,
			titleCollapse: true,
			hideCollapseTool: true,
			
			border: false,
			shadow: false,
			
			items : [new Ext.Component ({
						contentEl : contentEl
					})
			]
		});
		
		this._statusPanel.add(fd);
	},
	
	/**
	 * Refresh the data displayed for status
	 * @param {Boolean} gc True to also garbage collect java heap memory
	 * @private
	 */
	refreshData: function (gc)
	{
		var result = Ametys.data.ServerComm.send(this.pluginName, "administrator/jvmstatus/refresh", gc ? { gc: "gc" } : {}, Ametys.data.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);

	    if (Ametys.data.ServerComm.handleBadResponse(gc ? "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_GC_ERROR"/>" : "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_REFRESH_ERROR"/>", result, "Ametys.plugins.core.administration.JVMStatus.refreshData"))
	    {
	       window.clearInterval(this._at);
	       return false;
	    }

	    // HEAP MEMORY
	    var commitedMem = Ext.dom.Query.selectNumber("status/general/memory/heap/commited", result);
	    var usedMem = Ext.dom.Query.selectNumber("status/general/memory/heap/used", result);
	    var maxMem = Ext.dom.Query.selectNumber("status/general/memory/heap/max", result);
	    
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
	    var commitedMem = Ext.dom.Query.selectNumber("status/general/memory/nonHeap/commited", result);
	    var usedMem = Ext.dom.Query.selectNumber("status/general/memory/nonHeap/used", result);
	    var maxMem = Ext.dom.Query.selectNumber("status/general/memory/nonHeap/max", result);
	    
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
	    var sessions = Ext.dom.Query.selectNumber("status/general/activeSessions", result);
	    if (sessions == null) 
	        document.getElementById("activeSession").innerHTML = "&lt;a href='#' onclick='Ametys.plugins.core.administration.JVMStatus.helpSessions(); return false;'&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_SESSIONS_ERROR"/>&lt;/a&gt;";
	    else
	        document.getElementById("activeSession").innerHTML = sessions;

	    // ACTIVE REQUEST
	    var sessions = Ext.dom.Query.selectNumber("status/general/activeRequests", result);
	    if (sessions == null) 
	        document.getElementById("activeRequest").innerHTML = "&lt;a href='#' onclick='Ametys.plugins.core.administration.JVMStatus.helpRequests(); return false;'&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_REQUESTS_ERROR"/>&lt;/a&gt;";
	    else
	        document.getElementById("activeRequest").innerHTML = sessions;

	    // ACTIVE THREAD
	    document.getElementById("activeThread").innerHTML = Ext.dom.Query.selectNumber("status/general/activeThreads", result);
	    var locked = Ext.dom.Query.selectValue("status/general/deadlockThreads", result);
	    if (locked != "0")
	    {
	        document.getElementById("deadlockThread").innerHTML = "(&lt;a href='#' title='<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_THREADS_ERROR_LOCK_HINT"/>' style='color: red; font-weight: bold' onclick='Ametys.plugins.core.administration.JVMStatus.deadLock()'&gt;" + locked + " <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_THREADS_LOCK"/>&lt;/a&gt;)";
		}
	    
	    // TIME
	    document.getElementById("osTime").innerHTML = Ext.dom.Query.selectValue("status/general/osTime", result);
	    
	    return true;
	},

	/**
	 * Startup listener
	 */
	onReady: function ()
	{
		if (this.refreshData())
	    {
			this._at = window.setInterval(Ext.bind(this.refreshData, this), 10000);
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
	},

	/**
	 * @private
	 * Download a deadlock log
	 */
	deadLock: function ()
	{
	    var d = new Date();
	    var year = d.getFullYear();
	    var month = d.getMonth() + 1;
	    var day = d.getDate();
	    
	    window.location.href = Ametys.getPluginResourcesPrefix(this.pluginName) + "/administrator/jvmstatus/threads_" + year + "-" + (day.length == 1 ? '0' : '') + month + "-" + (month.length == 1 ? '0' : '') + day + "-T-" + d.getHours() + "-" + d.getMinutes() + ".log";
	},

	/**
	 * @private
	 * Display an error message on session
	 */
	helpSessions: function ()
	{
		Ext.Msg.show ({
			title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
			msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_SESSIONS_ERROR_HINT"/>",
			buttons: Ext.Msg.OK,
			icon: Ext.MessageBox.WARNING
	    });
	},

	/**
	 * @private
	 * Display an error message on request
	 */
	helpRequests: function ()
	{
		Ext.Msg.show ({
			title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
			msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_REQUESTS_ERROR_HINT"/>",
			buttons: Ext.Msg.OK,
			icon: Ext.MessageBox.ERROR
	    });
	}
});
