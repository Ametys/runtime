<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2009 Anyware Services

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   -->
<xsl:stylesheet version="1.0" 
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:escaper="org.ametys.runtime.util.EscapeForJavascript">
    
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
				<script type="text/javascript" src="{$contextPath}/plugins/{$pluginName}/resources/js/org/ametys/administration/JVMStatus.i18n.js"><xsl:comment>empty</xsl:comment></script>
				 
                <script type="text/javascript">
                    org.ametys.administration.JVMStatus.initialize ("<xsl:value-of select="$pluginName"/>");
           		
					org.ametys.administration.JVMStatus._navItems = [];
	                  
	                org.ametys.administration.JVMStatus._navItems.push ({label: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL"/>"}); 
	                org.ametys.administration.JVMStatus._navItems.push ({label: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_SYSTEM"/>"});                        
						
					var toolPanel = org.ametys.administration.JVMStatus.createPanel ();
					
					// Load properties data
					var data = [
							<xsl:for-each select="properties/node()">
								['<xsl:value-of select="local-name()" />',
								'<xsl:value-of select="escaper:escape(.)"/>']
								<xsl:if test="not(position()=last())">
									<xsl:text>,</xsl:text>
								</xsl:if>								
							</xsl:for-each>
					];
					
					org.ametys.administration.JVMStatus.loadProperties (data);
					
					org.ametys.administration.JVMStatus.addFieldSet (
						"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_SYS"/>",
						'system'
					);
					
					org.ametys.administration.JVMStatus.addFieldSet (
						"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_JVM"/>",
						'java'
					);
					
					org.ametys.administration.JVMStatus.addFieldSet (
						"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM"/>",
						'memory'
					);
						
					org.ametys.administration.JVMStatus.addFieldSet (
						"<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE"/>",
						'server'
					);
						
                    org.ametys.runtime.administrator.Panel.createPanel = function () 
					{
						return toolPanel;
					}
					
					Ext.onReady(org.ametys.administration.JVMStatus.onReady);
            	</script>
            </head>
        
            <body>
            	<div style="display:none">
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
    	                
    	                <button onclick="org.ametys.administration.JVMStatus.refreshData(true);" title="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_FREENOW" i18n:attr="title" onmouseover="this.style.borderColor = 'threedshadow'; this.style.borderStyle = 'outset';" onmouseout="this.style.borderColor = '#f1efe2'; this.style.borderStyle = 'solid';" style="border: 1px solid #FFF; background-color: #FFF; width: 40px; height: 32px;">
    	                	<img src="{$resourcesPath}/img/administrator/jvmstatus/recycle.png"/>
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
    	        </div>
            </body>
        </html>
    </xsl:template>    
</xsl:stylesheet>