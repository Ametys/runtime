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
                <title><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_TITLE"/></title>
                <LINK rel="stylesheet" href="{$workspaceContext}/resources/css/homepage.css" type="text/css"/>
            </head>
            
            <body>
                <table class="admin_index_main_table">
                    <tr>
                        <td id="actionset"/>
						<td  style="">
	                       <div style="overflow: auto; height: 440px; width: 480px;" id="logview"/>
                        </td>
                    </tr>
                </table>
                
                <script>
                    function goBack()
                    {
                        document.location.href = context.workspaceContext;
                    }
                
                    var sp = new SContextualPanel("actionset");
                    var handle = sp.addCategory("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE"/>");
                        handle.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_VIEW"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/log_16.gif", view);
                        handle.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DOWNLOAD"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/zip_16.gif", download);
                        handle.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DELETE"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/delete.gif", del);
                        handle.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_PURGE"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/purge.gif", purge);
                        handle.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_QUIT"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/quit.gif", goBack);
                    var help = sp.addCategory("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HELP"/>");
                        help.addElement("&lt;div style='font-size: 11px; color: #000000'&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HELP_TEXT"/>&lt;/div&gt;");
                    sp.paint();
                    
                    var loglistener = {};
                    loglistener.onSelect = function(element)
                    {
                        handle.showHideElement(0, logview.selection.length == 0);
                        handle.showHideElement(1, logview.selection.length &gt;= 0);
                        handle.showHideElement(2, logview.selection.length &gt;= 0);
                        return true;
                    }
                    loglistener.onUnselect = function()
                    {
                        handle.showHideElement(0, logview.selection.length == 2);
                        handle.showHideElement(1, logview.selection.length &gt; 1);
                        handle.showHideElement(2, logview.selection.length == 2);
                    }
                    
                    var logview = new SListView("logview", null, loglistener);
                    logview.setView("detail");
                    logview.setGroup("file");
                    logview.showGroups(true);
                    logview.sort(true, "date", false);
                    logview.addColumn(null, "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_COL_NAME"/>", null, "220px");
                    logview.addColumn("date", "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_COL_DATE"/>", null, "105px", "center");
                    logview.addColumn("size", "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_COL_SIZE"/>", null, "70px", "right");

                    <xsl:for-each select="/Logs/Log">
                            <xsl:for-each select="file">
                                <xsl:sort select="lastModified" order="descending"/>
                                
                                logview.addElement("<xsl:value-of select="location"/>",
                                                "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/log_16.gif", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/log_32.gif", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/log_50.gif",
                                                {
                                                    date : "<xsl:value-of select="lastModified"/>",
                                                    dateDisplay : "<i18n:date src-pattern="yyyy-MM-dd'T'hh:mm" pattern="medium" value="{lastModified}"/>",
                                                    size : fillSize("<xsl:value-of select="size"/>"),
                                                    realSize : <xsl:value-of select="size"/>,
                                                    sizeDisplay : "<xsl:choose>
                                                                        <xsl:when test="number(size) &lt; 1024"><xsl:value-of select="size"/>&#160;o</xsl:when>
                                                                        <xsl:when test="number(size) &lt; 1024*1024"><xsl:value-of select="round(size div 1024 * 10) div 10"/>&#160;ko</xsl:when>
                                                                        <xsl:when test="number(size) &lt; 1024*1024*1024"><xsl:value-of select="round(size div 1024 div 1024 * 10) div 10"/>&#160;Mo</xsl:when>
                                                                    </xsl:choose>",
                                                    file : "<xsl:choose>
                                                                <xsl:when test="../@name != location"><xsl:value-of select="../@name"/></xsl:when>
                                                                <xsl:otherwise><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_GROUP_OTHERS"/></xsl:otherwise>
                                                            </xsl:choose>"
                                                })
                            </xsl:for-each>
                    </xsl:for-each>
                    
                    logview.paint();
                    loglistener.onUnselect();
                    
                    function view()
                    {
                        var elt = logview.getSelection()[0];
                        if (elt.properties.realSize > 1024 * 1024)
                        {
                            if (confirm("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_VIEW_CONFIRM"/>"))
                            {
                                download();
                            }
                        }
                        else
                        {
                            window.location.href = getPluginDirectUrl('<xsl:value-of select="$pluginName"/>') + "/administrator/logs/view/" + encodeURIComponent(elt.name);
                        }
                    }
                    function download()
                    {
                        var url = getPluginDirectUrl('<xsl:value-of select="$pluginName"/>') + "/administrator/logs/download.zip";
                        var args = "";
                    
                        var elts = logview.getSelection();
                        for (var i = 0; i &lt; elts.length; i++)
                        {
                            var elt = elts[i];
                            args += "file=" + encodeURIComponent(elt.name) + "&amp;";
                        }
                        
                        window.location.href = url + "?" + args;
                    }
                    function del()
                    {
                        if (confirm("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DELETE_CONFIRM"/>"))
                        {
                            var url = getPluginDirectUrl('<xsl:value-of select="$pluginName"/>') + "/administrator/logs/delete";
                            var args = "";
                        
                            var elts = logview.getSelection();
                            for (var i = 0; i &lt; elts.length; i++)
                            {
                                var elt = elts[i];
                                args += "file=" + encodeURIComponent(elt.name) + "&amp;";
                            }
                            
                            var result = Tools.postFromUrl(url, args);
                            if (result == null)
                            {
                                alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DELETE_ERROR_GRAVE"/>")
                                return;
                            }
                            
                            var failuresString = Tools.getFromXML(result, "failure");
                            
                            for (var i = 0; i &lt; elts.length; i++)
                            {
                                var elt = elts[i];
                                if (failuresString.indexOf('/' + elt.name + '/') &lt; 0)
                                {
                                    elt.remove();
                                }
                            }                            
                            
                            if (failuresString.length &gt; 0)
                            {
                                alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_DELETE_ERROR"/>");
                            }
                        }
                    }
                    function purge()
                    {
                        if (confirm("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_PURGE_CONFIRM"/>"))
                        {
                            var url = getPluginDirectUrl('<xsl:value-of select="$pluginName"/>') + "/administrator/logs/purge";

                            var result = Tools.postFromUrl(url, "");
                            if (result == null)
                            {
                                alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_PURGE_ERROR_GRAVE"/>")
                                return;
                            }
                            
                            var doneString = Tools.getFromXML(result, "done");
                            
                            var nb = 0;
                            var elts = logview.elements;
                            for (var i = elts.length - 1; i &gt;= 0; i--)
                            {
                                var elt = elts[i];
                                if (doneString.indexOf('/' + elt.name + '/') &gt;= 0)
                                {
                                    elt.remove();
                                    nb++;
                                }
                            }      
                            
                            alert(nb + " <i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_PURGE_DONE"/>")                      
                        }
                    }
                    
                    function fillSize(size)
                    {
                        while (size.length &lt; 20)
                            size = "0" + size;
                        return size;
                    }
                </script>
            </body>
        </html>
    </xsl:template>    
    
</xsl:stylesheet>