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
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
    <xsl:param name="contextPath"/>
    <xsl:param name="pluginName"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
    
    <xsl:variable name="workspaceContext"><xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/></xsl:variable>
    
    <xsl:variable name="resourcesPath"><xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources</xsl:variable>
    
    <xsl:template match="/">
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TITLE"/></title>
                <LINK rel="stylesheet" href="{$workspaceContext}/resources/css/homepage.css" type="text/css"/>
                <LINK rel="stylesheet" href="{$resourcesPath}/css/administrator/jvmstatus.css" type="text/css"/>
            </head>
            
            <body>
                <table class="admin_index_main_table">
                    <tr>
                        <td id="actionset"/>
						<td style="border: 1px inset #efebde; padding: 5px;">
                            <table id="listview" style="width: 469px; height: 412px;"/>

                            <div id="tab_general" style="height: 413px; display: none;">

                                <div style="padding: 7px; padding-bottom: 3px">
                                    <table class="form">
                                         <caption><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_SYS"/></caption>
                                         <tr>
                                            <td style="padding-left: 30px;">
                                                <table>
                                                    <colgroup>
                                                        <col width="160px"/>
                                                        <col width="269px"/>
                                                    </colgroup>
                                                    <tr>
                                                        <td style="font-weight: bold;">
                                                            
                                                             <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_SYS_OS"/>
                                                        </td>
                                                        <td>
                                                            <xsl:value-of select="/status/caracteristics/osName"/>
                                                            <xsl:text> </xsl:text>
                                                            (<xsl:value-of select="/status/caracteristics/osVersion"/>)
                                                            <xsl:text> </xsl:text>
                                                            <xsl:value-of select="/status/caracteristics/osPatch"/>
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td style="font-weight: bold;">
                                                             <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_SYS_PROC"/>
                                                        </td>
                                                        <td>
                                                            <xsl:value-of select="/status/caracteristics/availableProc"/>
                                                            <xsl:text> </xsl:text>
                                                            <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_SYS_PROC_UNIT"/>
                                                            <xsl:text> </xsl:text>
                                                            <xsl:value-of select="/status/caracteristics/architecture"/>
                                                        </td>
                                                    </tr>
                                                </table>
                                            </td>
                                        </tr>
                                    </table>
                                </div>
                                
                                <div style="padding: 7px; padding-bottom: 3px">
                                    <table class="form">
                                         <caption><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_JVM"/></caption>
                                         <tr>
                                            <td style="padding-left: 30px;">
                                                <table>
                                                    <colgroup>
                                                        <col width="160px"/>
                                                        <col width="269px"/>
                                                    </colgroup>
                                                    <tr>
                                                        <td style="font-weight: bold;">
                                                             <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_JVM_VERSION"/>
                                                        </td>
                                                        <td>
                                                            <xsl:value-of select="/status/caracteristics/javaVersion"/>
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td style="font-weight: bold;">
                                                            <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_JVM_VENDOR"/>
                                                        </td>
                                                        <td>
                                                            <xsl:value-of select="/status/caracteristics/javaVendor"/>
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td style="font-weight: bold;">
                                                            <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_JVM_TIME"/>
                                                        </td>
                                                        <td>
                                                            <i18n:date-time pattern="EEEE dd MMMM yyyy HH:mm" src-pattern="yyyy-MM-dd'T'hh:mm" value="{/status/caracteristics/startTime}"/>
                                                        </td>
                                                    </tr>
                                                </table>
                                            </td>
                                         </tr>
                                    </table>
                                </div>

                                <div style="padding: 7px; padding-bottom: 3px">
                                    <table class="form">
                                         <caption><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM"/></caption>
                                         <tr>
                                            <td style="padding-left: 30px">
                                                <table style="width: 420px; border-left: 1px solid #3c3b38; border-top: 1px solid #3c3b38; border-bottom: 1px solid #ffffff; border-right: 1px solid #ffffff; margin-left: 0px; margin-top: 5px; background-color: #f1efe2">
                                                    <colgroup>
                                                        <col width="80px"/>
                                                        <col width="25px"/>
                                                        <col/>
                                                        <col width="40px"/>
                                                    </colgroup>
                                                    <tr>
                                                        <td style="vertical-align: middle; text-align: left; font-weight: bold; padding-left: 5px;">
                                                            <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_HEAP"/>
                                                        </td>
                                                        <td style="vertical-align: middle;">
                                                            <img src="{$resourcesPath}/img/administrator/jvmstatus/help.gif" i18n:attr="title" title="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_MEMORY_HINT_HEAP"/>
                                                        </td>
                                                        <td style="text-align :center; padding-top: 5px;">
                                                            <img src="{$resourcesPath}/img/administrator/jvmstatus/bar-left.gif" width="2px" height="28px"/>
                                                            <img id="totalMemImg" src="{$resourcesPath}/img/administrator/jvmstatus/bar-used.gif" width="0px" height="28px"/>
                                                            <img id="freeMemImg" src="{$resourcesPath}/img/administrator/jvmstatus/bar-free.gif" width="0px" height="28px"/>
                                                            <img id="maxMemImg" src="{$resourcesPath}/img/administrator/jvmstatus/bar-available.gif" width="280px" height="28px"/>
                                                            <img src="{$resourcesPath}/img/administrator/jvmstatus/bar-right.gif" width="2px" height="28px"/>
                                                        </td>
                                                        <td style="text-align: center; padding-top: 2px">
                                                            <button onclick="refreshData(true);" title="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_FREENOW" i18n:attr="title" onmouseover="this.style.borderColor = 'threedshadow'; this.style.borderStyle = 'outset';" onmouseout="this.style.borderColor = '#f1efe2'; this.style.borderStyle = 'solid';" style="border: 1px solid #f1efe2; background-color: #f1efe2; width: 32px; height: 32px;">
                                                                <img src="{$resourcesPath}/img/administrator/jvmstatus/recycle.gif"/>
                                                            </button>
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td/>
                                                        <td/>
                                                        <td style="padding-bottom: 5px;">
                                                            <table style="width: 100%;" cellspacing="0" cellpadding="0" border="0">
                                                                <tr>
                                                                    <td style="width:33%; text-align: left">0&#160;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT"/></td>
                                                                    <td style="width:33%; text-align: center"><span id="middleMem"></span>&#160;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT"/></td>
                                                                    <td style="width:33%; text-align: right"><span id="maxiMem"></span>&#160;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT"/></td>
                                                                </tr>
                                                            </table>
                                                        </td>
                                                        <td/>
                                                    </tr>
                                                    <tr>
                                                        <td style="vertical-align: top; vertical-align: top; text-align: left; font-weight: bold; padding-top: 2px; padding-left: 5px;">
                                                            <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_NHEAP"/>
                                                        </td>
                                                        <td style="vertical-align: top;">
                                                            <img src="{$resourcesPath}/img/administrator/jvmstatus/help.gif" i18n:attr="title" title="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_MEMORY_HINT_NHEAP"/>
                                                        </td>
                                                        <td style="text-align :center; vertical-align: bottom; padding-bottom: 5px;">
                                                            <img src="{$resourcesPath}/img/administrator/jvmstatus/bar-left.gif" width="2px" height="14px"/>
                                                            <img id="totalMem2Img" src="{$resourcesPath}/img/administrator/jvmstatus/bar-used.gif" width="0px" height="14px"/>
                                                            <img id="freeMem2Img" src="{$resourcesPath}/img/administrator/jvmstatus/bar-free.gif" width="0px" height="14px"/>
                                                            <img id="maxMem2Img" src="{$resourcesPath}/img/administrator/jvmstatus/bar-available.gif" width="280px" height="14px"/>
                                                            <img src="{$resourcesPath}/img/administrator/jvmstatus/bar-right.gif" width="2px" height="14px"/>

                                                            <br/>
                                                            
                                                            <table style="width: 100%;" cellspacing="0" cellpadding="0" border="0">
                                                                <tr>
                                                                    <td style="width:33%; text-align: left">0&#160;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT"/></td>
                                                                    <td style="width:33%; text-align: center"><span id="middleMem2"></span>&#160;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT"/></td>
                                                                    <td style="width:33%; text-align: right"><span id="maxiMem2"></span>&#160;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_UNIT"/></td>
                                                                </tr>
                                                            </table>
                                                        </td>
                                                        <td style="padding-top: 8px;">
                                                            <!-- empty -->
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td/>
                                                        <td/>
                                                        <td style="font-weight: bold; text-align: right; padding-bottom: 5px">
                                                            <img style="width: 10px; height: 10px; vertical-align: baseline; margin-left: 10px; margin-right: 2px;" src="{$resourcesPath}/img/administrator/jvmstatus/bar-used.gif"/><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_USED_SUFFIX"/>
                                                            <img style="width: 10px; height: 10px; vertical-align: baseline; margin-left: 10px; margin-right: 2px;" src="{$resourcesPath}/img/administrator/jvmstatus/bar-free.gif"/><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_FREE_SUFFIX"/>
                                                            <img style="width: 10px; height: 10px; vertical-align: baseline; margin-left: 10px; margin-right: 2px;" src="{$resourcesPath}/img/administrator/jvmstatus/bar-available.gif"/><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_MEM_AVAILABLE_SUFFIX"/>
                                                        </td>
                                                        <td/>
                                                    </tr>
                                                </table>
                                            </td>
                                         </tr>
                                    </table>
                                </div>
                                
                                <div style="padding: 7px; padding-bottom: 3px">
                                    <table class="form">
                                         <caption><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE"/></caption>
                                         <tr>
                                            <td style="padding-left: 30px">
                                                <table>
                                                    <colgroup>
                                                        <col width="130px"/>
                                                        <col width="30px"/>
                                                        <col width="269px"/>
                                                    </colgroup>
                                                    <tr>
                                                        <td style="font-weight: bold; ">
                                                            <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_SESSION"/>
                                                        </td>
                                                        <td>
                                                            <img src="{$resourcesPath}/img/administrator/jvmstatus/help.gif" style="margin-left: 5px; vertical-align: middle" i18n:attr="title" title="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_SESSION_HELP"/>
                                                        </td>
                                                        <td id="activeSession" style="vertical-align: middle">-</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="font-weight: bold; ">
                                                            <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_REQUEST"/>
                                                        </td>
                                                        <td>
                                                            <img src="{$resourcesPath}/img/administrator/jvmstatus/help.gif" style="margin-left: 5px; vertical-align: middle" i18n:attr="title" title="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_REQUEST_HELP"/>
                                                        </td>
                                                        <td id="activeRequest" style="vertical-align: middle">-</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="font-weight: bold; ">
                                                            <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_THREAD"/>
                                                        </td>
                                                        <td>
                                                            <img src="{$resourcesPath}/img/administrator/jvmstatus/help.gif" style="margin-left: 5px; vertical-align: middle" i18n:attr="title" title="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL_HANDLE_THREAD_HELP"/>
                                                        </td>
                                                        <td style="vertical-align: middle">
                                                            <span id="activeThread">-</span>&#160;<span id="deadlockThread"></span>
                                                        </td>
                                                    </tr>
                                                </table>
                                             </td>
                                         </tr>
                                    </table>
                                </div>
                            </div>
                            
                            <div id="tab_system" style="height: 411px; display: none;">
                                <div style="overflow: auto; height: 411px; width: 464px;">
                                    <table class="form list" style="margin: 7px; border: 1px solid black">
                                        <xsl:for-each select="/status/properties/*">
                                            <xsl:sort select="name()"/>
                                            
                                            <tr>
                                                <td><xsl:value-of select="name()"/></td>
                                                <td><xsl:value-of select="."/></td>
                                            </tr>
                                        </xsl:for-each>
                                    </table>
                                </div>
                            </div>
                        </td>
                    </tr>
                </table>
                
                <script>
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
                                alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_GC_ERROR"/>");
                            else
                                alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_REFRESH_ERROR"/>");
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
                        alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_SESSIONS_ERROR_HINT"/>");
                    }
                
                    function helprequests()
                    {
                        alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_REQUESTS_ERROR_HINT"/>");
                    }
                
                    function goBack()
                    {
                        document.location.href = context.workspaceContext;
                    }
                
                    var sp = new SContextualPanel("actionset");
                    var handle = sp.addCategory("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_HANDLE"/>");
                        handle.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_HANDLE_QUIT"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/jvmstatus/quit.gif", goBack);
                    var help = sp.addCategory("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_HELP"/>");
                        help.addElement("&lt;div style='font-size: 11px; color: #000000'&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_SYSTEM_HELP_TEXT"/>&lt;/div&gt;");
                        help.addElement("&lt;div style='font-size: 11px; color: #000000'&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_PROPERTIES_HELP_TEXT"/>&lt;/div&gt;");
                    sp.paint();
                    
                    var tablistener = {};
                    tablistener.onTabSelected = function (index)
                    {
                        help.showHideElement(0, index == 0);
                        help.showHideElement(1, index == 1);
                    }
                    
                    var myTab = new STab("listview", null, null, tablistener);
                    myTab.addTab("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_GENERAL"/>", "tab_general");
                    myTab.addTab("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_STATUS_TAB_SYSTEM"/>", "tab_system");
                    myTab.paint();
                    
                    tablistener.onTabSelected(0);
                    
                    if (refreshData())
                    {
                        var at = window.setInterval("refreshData()", 10000);
                    }
                </script>
            </body>
        </html>
    </xsl:template>    
    
</xsl:stylesheet>