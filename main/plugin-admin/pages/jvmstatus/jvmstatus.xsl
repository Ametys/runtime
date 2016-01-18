<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2015 Anyware Services

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
                xmlns:ametys="org.ametys.core.util.AmetysXSLTHelper">
    
    <xsl:param name="pluginName"/>
    
    <xsl:variable name="context-path" select="ametys:uriPrefix(false())"/>
    <xsl:variable name="workspace-name" select="ametys:workspaceName()"/>
    <xsl:variable name="workspace-prefix" select="ametys:workspacePrefix()"/>
    <xsl:variable name="workspace-uri" select="concat($context-path, $workspace-prefix)"/>
    <xsl:variable name="resources-uri" select="concat($context-path, '/plugins/', $pluginName, '/resources')"/>
                    
    <xsl:template match="/characteristics">
	     <div>
	         <div id="system">
	              <div>
		              <span class="label">
		                   <i18n:text i18n:key="PLUGINS_ADMIN_STATUS_TAB_GENERAL_SYS_OS"/>
	                  </span>
		              <span>
			              <xsl:value-of select="osName"/>
			              <xsl:text> </xsl:text>
			              (<xsl:value-of select="osVersion"/>)
			              <xsl:text> </xsl:text>
			              <xsl:value-of select="osPatch"/>
		              </span>
	              </div>
	              <div>
		              <span class="label"><i18n:text i18n:key="PLUGINS_ADMIN_STATUS_TAB_GENERAL_SYS_PROC"/></span>
		              <span>
			              <xsl:value-of select="availableProc"/>
			              <xsl:text> </xsl:text>
			              <i18n:text i18n:key="PLUGINS_ADMIN_STATUS_TAB_GENERAL_SYS_PROC_UNIT"/>
			              <xsl:text> </xsl:text>
			              <xsl:value-of select="architecture"/>
		              </span>
	              </div>
	              <div>
		              <span class="label"><i18n:text i18n:key="PLUGINS_ADMIN_STATUS_TAB_GENERAL_SYS_TIME"/></span>
		              <span id="osTime"><i18n:date pattern="long" src-pattern="yyyy-MM-dd'T'HH:mm" value="{osTime}"/>&#160;<i18n:time pattern="short" src-pattern="yyyy-MM-dd'T'HH:mm" value="{osTime}"/></span>
	              </div>
	         </div>
	         
	         <div id="java">
	             <div class="label"><i18n:text i18n:key="PLUGINS_ADMIN_STATUS_TAB_GENERAL_JVM_VERSION"/></div>
	             <xsl:value-of select="javaVersion"/>
	             <br/>
	             <div class="label"><i18n:text i18n:key="PLUGINS_ADMIN_STATUS_TAB_GENERAL_JVM_VENDOR"/></div>
	             <xsl:value-of select="javaVendor"/>
	             <br/>
	             <div class="label"><i18n:text i18n:key="PLUGINS_ADMIN_STATUS_TAB_GENERAL_JVM_NAME"/></div>
	             <xsl:value-of select="jvmName"/>
	             <br/>
	             <div>
	               <span class="label"><i18n:text i18n:key="PLUGINS_ADMIN_STATUS_TAB_GENERAL_JVM_TIME"/></span>
	               <span id="startTime"><i18n:date pattern="long" src-pattern="yyyy-MM-dd'T'HH:mm" value="{startTime}"/>&#160;<i18n:time pattern="short" src-pattern="yyyy-MM-dd'T'HH:mm" value="{startTime}"/></span>
	             </div>
	         </div>
             
             <div id="ametys">
                <div class="label help" id="ametys-home-help-img"><i18n:text i18n:key="PLUGINS_ADMIN_STATUS_TAB_GENERAL_AMETYS_AMETYS_HOME"/></div>
                <xsl:value-of select="ametysHome"/>
             </div>
	           
	         <div id="memory">
	             <div class="label help" id="mem-heap-help-img"><i18n:text i18n:key="PLUGINS_ADMIN_STATUS_TAB_GENERAL_MEM_HEAP"/></div>
                 <div id="totalMemImg"><xsl:comment/></div>
                 <div id="freeMemImg"><xsl:comment/></div>
                 <div id="maxMemImg"><xsl:comment/></div>
	             
	             <button id="btn-gc" onclick="Ametys.plugins.admin.jvmstatus.JVMStatusTool.garbageCollect();" title="PLUGINS_ADMIN_STATUS_TAB_GENERAL_MEM_FREENOW" i18n:attr="title" onmouseover="Ext.get('btn-gc').addCls('over');" onmouseout="Ext.get('btn-gc').removeCls('over');">
	                 <img src="{$resources-uri}/img/jvmstatus/recycle.png"/>
	             </button>
	             
	             <br/>
	             
	             <div style="float: left; text-align: right; width: 170px">0&#160;<i18n:text i18n:key="PLUGINS_ADMIN_STATUS_TAB_GENERAL_MEM_UNIT"/></div>
	             <div style="float: left; text-align: right; width: 136px"><span id="middleMem"></span>&#160;<i18n:text i18n:key="PLUGINS_ADMIN_STATUS_TAB_GENERAL_MEM_UNIT"/></div>
	             <div style="float: left; text-align: right; width: 124px"><span id="maxiMem"></span>&#160;&#160;<i18n:text i18n:key="PLUGINS_ADMIN_STATUS_TAB_GENERAL_MEM_UNIT"/></div>
	             
	             <br/><br/>
	             <div class="legend">
	                 <div id="totalMemImg-legend"><xsl:comment/></div><i18n:text i18n:key="PLUGINS_ADMIN_STATUS_TAB_GENERAL_MEM_USED_SUFFIX"/>
	                 <div id="freeMemImg-legend"><xsl:comment/></div><i18n:text i18n:key="PLUGINS_ADMIN_STATUS_TAB_GENERAL_MEM_FREE_SUFFIX"/>
	                 <div id="maxMemImg-legend"><xsl:comment/></div><i18n:text i18n:key="PLUGINS_ADMIN_STATUS_TAB_GENERAL_MEM_AVAILABLE_SUFFIX"/>
	             </div>
	         </div>
	         
	         <div id="server">
	             <div class="label help" id="handle-session-help-img"><i18n:text i18n:key="PLUGINS_ADMIN_STATUS_TAB_GENERAL_HANDLE_SESSION"/></div>
	             <span id="activeSession">-</span>  
	             <br/>
	             
	             <div class="label help" id="handle-request-help-img"><i18n:text i18n:key="PLUGINS_ADMIN_STATUS_TAB_GENERAL_HANDLE_REQUEST"/></div>
	             <span id="activeRequest">-</span>
	             <br/>
	             
	             <div class="label help" id="handle-thread-help-img"><i18n:text i18n:key="PLUGINS_ADMIN_STATUS_TAB_GENERAL_HANDLE_THREAD"/></div>
	             <span id="activeThread">-</span>&#160;<span id="deadlockThread"></span>
	         </div>
	     </div>
    </xsl:template>

</xsl:stylesheet>