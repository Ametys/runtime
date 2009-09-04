<?xml version="1.0" encoding="UTF-8"?>
<!--+
    | Copyright (c) 2007 Anyware Technologies and others.
    | All rights reserved. This program and the accompanying materials
    | are made available under the terms of the Eclipse Public License v1.0
    | which accompanies this distribution, and is available at
    | http://www.opensource.org/licenses/eclipse-1.0.php
    | 
    | Contributors:
    |     Anyware Technologies - initial API and implementation
    +-->
<xsl:stylesheet version="1.0" 
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:escaper="org.ametys.runtime.plugins.core.EscapeForJavascript">
    
    <xsl:param name="contextPath"/>
    <xsl:param name="pluginName"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
    
    <xsl:variable name="workspaceContext"><xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/></xsl:variable>
    
    <xsl:variable name="resourcesPath"><xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources</xsl:variable>
    
    <xsl:template match="/status">
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TITLE"/></title>
                <link rel="stylesheet" href="{$resourcesPath}/css/administrator/jvmstatus.css" type="text/css"/>    
            </head>
				<script>
	            	<script type="text/javascript">
	            			function refreshData(gc)
		                    {
		                        var url = getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/administrator/jvmstatus/refresh";
		                        var args = gc ? "gc=gc" : "";
		                        
		                        var result;
		                        try
		                        {
		                            result = Tools.postFromUrl(url, args);
		                        }
		                        catch (e)
		                        {
		                            result = null;
		                        }
		                        
		                        if (result == null)
		                        {
		                            window.clearInterval(at);
		                            if (gc)
		                            {
		                            	Ext.Msg.show ({
				                        		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
				                        		msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_GC_ERROR"/>",
				                        		buttons: Ext.Msg.OK,
							   					icon: Ext.MessageBox.ERROR
				                        });
		                            }
		                            else {
		                            	Ext.Msg.show ({
				                        		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
				                        		msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_REFRESH_ERROR"/>",
				                        		buttons: Ext.Msg.OK,
							   					icon: Ext.MessageBox.ERROR
				                        });
		                            }
		                            return false;
		                        }
		                        
		                        // HEAP MEMORY
		                        var commitedMem = result.selectSingleNode("/status/general/memory/heap/commited")[Tools.xmlTextContent];
		                        var usedMem = result.selectSingleNode("/status/general/memory/heap/used")[Tools.xmlTextContent];
		                        var maxMem = result.selectSingleNode("/status/general/memory/heap/max")[Tools.xmlTextContent];
		                        
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
		                        var commitedMem = result.selectSingleNode("/status/general/memory/nonHeap/commited")[Tools.xmlTextContent];
		                        var usedMem = result.selectSingleNode("/status/general/memory/nonHeap/used")[Tools.xmlTextContent];
		                        var maxMem = result.selectSingleNode("/status/general/memory/nonHeap/max")[Tools.xmlTextContent];
		                        
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
		                        var sessions = result.selectSingleNode("/status/general/activeSessions");
		                        if (sessions == null) 
		                            document.getElementById("activeSession").innerHTML = "&lt;a href='#' onclick='helpsessions(); return false;'&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_SESSIONS_ERROR"/>&lt;/a&gt;";
		                        else
		                            document.getElementById("activeSession").innerHTML = sessions[Tools.xmlTextContent];
		
		                        // ACTIVE REQUEST
		                        var sessions = result.selectSingleNode("/status/general/activeRequests");
		                        if (sessions == null) 
		                            document.getElementById("activeRequest").innerHTML = "&lt;a href='#' onclick='helprequests(); return false;'&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_REQUESTS_ERROR"/>&lt;/a&gt;";
		                        else
		                            document.getElementById("activeRequest").innerHTML = sessions[Tools.xmlTextContent];
		
		                        // ACTIVE THREAD
		                        document.getElementById("activeThread").innerHTML = result.selectSingleNode("/status/general/activeThreads")[Tools.xmlTextContent];
		                        var locked = result.selectSingleNode("/status/general/deadlockThreads")[Tools.xmlTextContent];
		                        if (locked != "0")
		                            document.getElementById("deadlockThread").innerHTML = "(&lt;a href='#' title='<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_THREADS_ERROR_LOCK_HINT"/>' style='color: red; font-weight: bold' onclick='deadlock()'&gt;" + locked + " <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_THREADS_LOCK"/>&lt;/a&gt;)";
		                        
		                        return true;
		                    }
		                    
		                    function deadlock()
		                    {
		                        var d = new Date();
		                        var year = d.getFullYear();
		                        var month = d.getMonth() + 1;
		                        var day = d.getDate();
		                        window.location.href = getPluginDirectUrl('<xsl:value-of select="$pluginName"/>') + "/administrator/jvmstatus/threads_" + year + "-" + (day.length == 1 ? '0' : '') + month + "-" + (month.length == 1 ? '0' : '') + day + "-T-" + d.getHours() + "-" + d.getMinutes() + ".log";
		                    }
		                    
		                    function helpsessions()
		                    {
		                    	Ext.Msg.show ({
		                        		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
		                        		msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_SESSIONS_ERROR_HINT"/>",
		                        		buttons: Ext.Msg.OK,
					   					icon: Ext.MessageBox.WARNING
		                        });
		                    }
		                
		                    function helprequests()
		                    {
		                    	Ext.Msg.show ({
		                        		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
		                        		msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_REQUESTS_ERROR_HINT"/>",
		                        		buttons: Ext.Msg.OK,
					   					icon: Ext.MessageBox.ERROR
		                        });
		                    }
                    
		               		function goBack()
		                    {
		                        document.location.href = context.workspaceContext;
		                    }   
		                    
		                    //Navigation 
		                    var navigation = new org.ametys.NavigationPanel ({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_MENU"/>"});
							var item1 = new org.ametys.NavigationItem ({
								text: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL"/>",
								activeItem: 0,
								cardLayout: 'system-card-panel',
								toggleGroup : 'system-menu',
								pressed: true
							});
							navigation.add(item1);
							var item2 = new org.ametys.NavigationItem ({
								text: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_SYSTEM"/>",
								activeItem: 1,
								cardLayout: 'system-card-panel',
								toggleGroup : 'system-menu'
							});
							navigation.add(item2);
							
							//Actions
							var handle = new org.ametys.ActionsPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_HANDLE"/>'});
							handle.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_HANDLE_QUIT"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/jvmstatus/quit.png", goBack);
							
							//Help
							var help = new org.ametys.TextPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_HELP"/>'});
							help.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_PROPERTIES_HELP_TEXT"/>");
							
							var rightPanel = new org.ametys.HtmlContainer({
									region:'east',
									border: false,
									cls: 'admin-right-panel',
									width: 277,
								    items: [navigation, handle, help]
							});
						
							var dummyData = [
								<xsl:for-each select="properties/node()">
									['<xsl:value-of select="local-name()" />',
									'<xsl:value-of select="escaper:escape(.)"/>']
									<xsl:if test="not(position()=last())">
										<xsl:text>,</xsl:text>
									</xsl:if>								
								</xsl:for-each>
							              ];
							              
							var store = new Ext.data.SimpleStore({
										id:0,
								        fields: [
								           {name: 'name'},
								           {name: 'value'}
								        ]
								});
								
							store.loadData(dummyData);
							//Create the list View Group
							var properties = new org.ametys.ListView({
									id : 'properties-panel',
								    store : store,
									hideHeaders : true,
								    columns: [
								        {width : 250, menuDisabled : true, sortable: true, dataIndex: 'name'},
								        {width : 360, menuDisabled : true, sortable: true, dataIndex: 'value'}
								    ],
									region: 'center',
									baseCls: 'properties-view'
							});		
							properties.hide();
							
							var generalPanel =  new Ext.Panel ({
								baseCls: 'transparent-panel',
								autoScroll: true,
								id : 'general-panel',
								border: false
							});
							var systemFd = new org.ametys.Fieldset({
								title : "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_SYS"/>"
							});
							var system = new org.ametys.HtmlContainer ({
								contentEl : 'system'
							});
							systemFd.add(system);
							generalPanel.add(systemFd);  
						
							var javaFd = new org.ametys.Fieldset({
								title : "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_JVM"/>"
							});
							var java = new org.ametys.HtmlContainer ({
								contentEl : 'java'
							});
							javaFd.add(java);
							generalPanel.add(javaFd);  
							   
							var memoryFd = new org.ametys.Fieldset({
								title : "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM"/>"
							});
							var memory = new org.ametys.HtmlContainer ({
								contentEl : 'memory'
							});
							memoryFd.add(memory);
							generalPanel.add(memoryFd);
							
							var serverFd = new org.ametys.Fieldset({
								title : "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE"/>"
							});
							var server = new org.ametys.HtmlContainer ({
								contentEl : 'server'
							});
							serverFd.add(server);
							generalPanel.add(serverFd);
							  
							var centerPanel = new Ext.Panel({
								id:'system-card-panel',
								layout:'card',
								activeItem: 0,
								region:'center',
								baseCls: 'transparent-panel',
								border: false,
								autoScroll : true,
								height: 'auto',
								items: [generalPanel, properties]
							});		
		                    
		                    function _getAdminPanel ()
							{
								return new Ext.Panel({
									region: 'center',
									baseCls: 'transparent-panel',
									border: false,
									layout: 'border',
									autoScroll: true,
									items: [centerPanel, rightPanel]
								});
							}
							
		    
							var at;
							function onready() 
		                    {
			               		if (refreshData())
			                    {
			                        at = window.setInterval("refreshData()", 10000);
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
							Ext.onReady(onready);
	            	</script>
            </script>	
        
        
        <body>
        	<div id="system">
        		 <div class="label"><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_SYS_OS"/></div>
        		 <xsl:value-of select="/status/caracteristics/osName"/>
                 <xsl:text> </xsl:text>
                 (<xsl:value-of select="/status/caracteristics/osVersion"/>)
                 <xsl:text> </xsl:text>
                 <xsl:value-of select="/status/caracteristics/osPatch"/>
                 
                 <br/>
                 
                 <div class="label"><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_SYS_PROC"/></div>
                 <xsl:value-of select="/status/caracteristics/availableProc"/>
                 <xsl:text> </xsl:text>
                 <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_SYS_PROC_UNIT"/>
                 <xsl:text> </xsl:text>
                 <xsl:value-of select="/status/caracteristics/architecture"/>
        	</div>
        	
        	<div id="java">
        		<div class="label"><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_JVM_VERSION"/></div>
        		<xsl:value-of select="/status/caracteristics/javaVersion"/>
        		<br/>
        		<div class="label"><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_JVM_VENDOR"/></div>
        		<xsl:value-of select="/status/caracteristics/javaVendor"/>
        		<br/>
        		<div class="label"><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_JVM_TIME"/></div>
        		<i18n:date-time pattern="EEEE dd MMMM yyyy HH:mm" src-pattern="yyyy-MM-dd'T'hh:mm" value="{/status/caracteristics/startTime}"/>
        	</div>
        	
        	<div id="memory">
        		<div class="label"><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_HEAP"/></div>
        		<img class="help" id="mem-heap-help-img" src="{$resourcesPath}/img/administrator/jvmstatus/help.gif"/>
        		<img src="{$resourcesPath}/img/administrator/jvmstatus/bar-mem-left.png" width="14px" height="34px"/>
                <img id="totalMemImg" style="background: url({$resourcesPath}/img/administrator/jvmstatus/bar-mem-used.png) repeat-x 0px 0px" src="{$resourcesPath}/img/administrator/jvmstatus/s.gif" width="0px" height="34px"/>
                <img id="freeMemImg" style="background: url({$resourcesPath}/img/administrator/jvmstatus/bar-mem-free.png) top left repeat-x;" src="{$resourcesPath}/img/administrator/jvmstatus/s.gif" width="0px" height="34px"/>
                <img id="maxMemImg" style="background: url({$resourcesPath}/img/administrator/jvmstatus/bar-mem-available.png) top left repeat-x;" src="{$resourcesPath}/img/administrator/jvmstatus/s.gif" width="280px" height="34px"/>
                <img src="{$resourcesPath}/img/administrator/jvmstatus/bar-mem-right.png" width="14px" height="34px"/>
                
                <button onclick="refreshData(true);" title="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_FREENOW" i18n:attr="title" onmouseover="this.style.borderColor = 'threedshadow'; this.style.borderStyle = 'outset';" onmouseout="this.style.borderColor = '#f1efe2'; this.style.borderStyle = 'solid';" style="border: 1px solid #FFF; background-color: #FFF; width: 32px; height: 32px;">
                	<img src="{$resourcesPath}/img/administrator/jvmstatus/recycle.gif"/>
                </button>
                
        		<br/>
        		
        		<div style="float: left; text-align: right; width: 184px">0&#160;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT"/></div>
        		<div style="float: left; text-align: right; width: 136px"><span id="middleMem"></span>&#160;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT"/></div>
        		<div style="float: left; text-align: right; width: 124px"><span id="maxiMem"></span>&#160;&#160;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT"/></div>
        		
        		<br/><br/>
        		
        		<div class="label"><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_NHEAP"/></div>
        		<img class="help" id="mem-nheap-help-img" src="{$resourcesPath}/img/administrator/jvmstatus/help.gif"/>
        		<img src="{$resourcesPath}/img/administrator/jvmstatus/bar-left.png" width="14px" height="28px"/>
                <img id="totalMem2Img" style="background: url({$resourcesPath}/img/administrator/jvmstatus/bar-used.png) repeat-x 0px 0px" src="{$resourcesPath}/img/administrator/jvmstatus/s.gif" width="0px" height="28px"/>
                <img id="freeMem2Img" style="background: url({$resourcesPath}/img/administrator/jvmstatus/bar-free.png) repeat-x 0px 0px" src="{$resourcesPath}/img/administrator/jvmstatus/s.gif" width="0px" height="28px"/>
                <img id="maxMem2Img" style="background: url({$resourcesPath}/img/administrator/jvmstatus/bar-available.png) repeat-x 0px 0px" src="{$resourcesPath}/img/administrator/jvmstatus/s.gif" width="280px" height="28px"/>
                <img src="{$resourcesPath}/img/administrator/jvmstatus/bar-right.png" width="14px" height="28px"/>
                <br/>    
        		<div style="float: left; text-align: right; width: 184px">0&#160;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT"/></div>
        		<div style="float: left; text-align: right; width: 136px"><span id="middleMem2"></span>&#160;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT"/></div>
        		<div style="float: left; text-align: right; width: 124px"><span id="maxiMem2"></span>&#160;&#160;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT"/></div>
        		
        		<br/><br/>
        		<div class="legend">
        			<img src="{$resourcesPath}/img/administrator/jvmstatus/legend-used.gif"/><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_USED_SUFFIX"/>
                    <img src="{$resourcesPath}/img/administrator/jvmstatus/legend-free.gif"/><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_FREE_SUFFIX"/>
                    <img src="{$resourcesPath}/img/administrator/jvmstatus/legend-available.gif"/><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_AVAILABLE_SUFFIX"/>
               	</div>
        	</div>
        	
        	<div id="server">
        	 	<div class="label"><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_SESSION"/></div>
                <img id="handle-session-help-img" src="{$resourcesPath}/img/administrator/jvmstatus/help.gif" class="help" />                                       
                <span id="activeSession">-</span>  
                <br/>
                
                <div class="label"><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_REQUEST"/></div>
               	<img id="handle-request-help-img" src="{$resourcesPath}/img/administrator/jvmstatus/help.gif" class="help" />
                <span id="activeRequest">-</span>
				<br/>
				
				<div class="label"><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_THREAD"/></div>
				<img id="handle-thread-help-img" src="{$resourcesPath}/img/administrator/jvmstatus/help.gif" class="help" />
				<span id="activeThread">-</span>&#160;<span id="deadlockThread"></span>
        	</div>
        </body>
        </html>
    </xsl:template>    
    
</xsl:stylesheet>