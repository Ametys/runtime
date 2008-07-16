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
                <link rel="stylesheet" href="{$workspaceContext}/resources/css/homepage.css" type="text/css"/>
            </head>
            
            <body>
                <table class="admin_index_main_table">
                    <tr>
                        <td id="actionset"/>
						<td  style="">
                            <table id="stab" style="width: 478px; height: 412px;"/>

                            <div id="logs_view" style="height: 413px; display: none;">
    	                       <div style="overflow: auto; height: 413px; width: 472px;" id="logview"/>
                            </div>

                            <div id="logs_level" style="height: 413px; display: none;">
                                <div style="overflow: auto; height: 413px; width: 472px;"  id="stree">
                                    <ul>
                                        <li>
                                            <xsl:apply-templates select="/Logger/LogLevels[@type='logkit']/logger">
                                                <xsl:sort select="@name"/>
                                            </xsl:apply-templates>
                                        </li>
                                    </ul>
                                </div>                 
                            </div>
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
                    var handle2 = sp.addCategory("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE"/>");
                        handle2.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_LOGLEVEL_TO_DEBUG"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/loglevel_debug.gif", goDebug);
                        handle2.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_LOGLEVEL_TO_INFO"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/loglevel_info.gif", goInfo);
                        handle2.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_LOGLEVEL_TO_WARN"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/loglevel_warn.gif", goWarning);
                        handle2.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_LOGLEVEL_TO_ERROR"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/loglevel_error.gif", goError);
                        handle2.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_LOGLEVEL_TO_INHERIT"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/loglevel_inherit.gif", goInherit);
                        handle2.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_LOGLEVEL_FORCE"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/loglevel_force.gif", goForce);
                        handle2.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_QUIT"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/quit.gif", goBack);
                    var help = sp.addCategory("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HELP"/>");
                        help.addElement("&lt;div style='font-size: 11px; color: #000000'&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HELP_TEXT"/>&lt;/div&gt;");
                        help.addElement("&lt;div style='font-size: 11px; color: #000000'&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HELP2_TEXT"/>&lt;/div&gt;");
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

                    <xsl:for-each select="/Logger/Logs/Log">
                            <xsl:for-each select="file">
                                <xsl:sort select="lastModified" order="descending"/>
                                
                                logview.addElement("<xsl:value-of select="location"/>",
                                                "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/log_16.gif", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/log_32.gif", "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/log_50.gif",
                                                {
                                                    date : "<xsl:value-of select="lastModified"/>",
                                                    dateDisplay : "<i18n:date src-pattern="yyyy-MM-dd'T'HH:mm" pattern="medium" value="{lastModified}"/>",
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
                    
                    
                    function stree_listener() {}
                    stree_listener.onSelect = function(link)
                    {
                        handle2.showHideElement(0, link != null &amp;&amp; (link.getAttribute("inherited") == "true" || (link.getAttribute("inherited") == "false" &amp;&amp; link.getAttribute("level") != "DEBUG")));
                        handle2.showHideElement(1, link != null &amp;&amp; (link.getAttribute("inherited") == "true" || (link.getAttribute("inherited") == "false" &amp;&amp; link.getAttribute("level") != "INFO")));
                        handle2.showHideElement(2, link != null &amp;&amp; (link.getAttribute("inherited") == "true" || (link.getAttribute("inherited") == "false" &amp;&amp; link.getAttribute("level") != "WARNING")));
                        handle2.showHideElement(3, link != null &amp;&amp; (link.getAttribute("inherited") == "true" || (link.getAttribute("inherited") == "false" &amp;&amp; link.getAttribute("level") != "ERROR")));
                        handle2.showHideElement(4, link != null &amp;&amp; link.getAttribute("category") != "" &amp;&amp; link.getAttribute("inherited") == "false");
                        
                        var ul = link != null &amp;&amp; SUtilities.getDirectChildrenByTagName(link.parentNode, "ul")[0] != null;
                        handle2.showHideElement(5, link != null &amp;&amp; ul);
                    }
                    stree_listener.canDrag = false;
                    stree_listener.canRename = false;
                    var stree = new STree("stree", null, 1, stree_listener);
                    stree_listener.onSelect(null);
                    
                    
                    
                    var tablistener = {};
                    tablistener.onTabSelected = function (index)
                    {
                        handle.showHide(index == 0);
                        handle2.showHide(index == 1);
                        help.showHideElement(0, index == 0);
                        help.showHideElement(1, index == 1);
                    }
                    
                    var myTab = new STab("stab", null, null, tablistener);
                    myTab.addTab("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_TAB_VIEW"/>", "logs_view");
                    myTab.addTab("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_TAB_LEVEL"/>", "logs_level");
                    myTab.paint();
                    
                    tablistener.onTabSelected(0);
                    
                    
                    function switchTo(mode, link, inherited, force, results)
                    {
                        var selection = link == null;
                        if (results == null)
                        {
                            results = new Array();
                        }
                    
                        link = link != null ? link : stree.tree.selection
                        inherited = inherited == null ? "false" : "true";
                    
                        if (!selection || !force)
                        {
                            var oldInherited = link.getAttribute("inherited"); 
                            if (oldInherited != inherited || oldInherited != "true")
                            {
                                results.push({type: link.getAttribute("type"), category: link.getAttribute("category"), inherited: inherited, mode: mode});
                            }
                            
                            link.setAttribute("inherited", "" + inherited);
                            link.setAttribute("level", mode.toUpperCase());
                        
                            SUtilities.getDirectChildrenByTagName(link.parentNode, "img")[0].src = "<xsl:value-of select="$resourcesPath"/>/img/administrator/logs/loglevel_" + mode.toLowerCase() + (inherited == "true" ? "_inherited" : "") + ".gif";
                        }
                        
                        if (selection)
                        {
                            stree_listener.onSelect(stree.tree.selection);
                        }
                        
                        var ul = SUtilities.getDirectChildrenByTagName(link.parentNode, "ul")[0];
                        if (ul != null)
                        {
                            var links = SUtilities.getDirectChildrenByTagName(ul, "li");
                            for (var i = 0; i &lt; links.length; i++)
                            {
                                var l = SUtilities.getDirectChildrenByTagName(links[i], "a")[0];
                                if (l.getAttribute("inherited") == "true" || force)
                                {
                                    switchTo(mode, l, "true", force, results);
                                }
                            }
                        }
                        
                        return results;
                    }
                                        
                    function goDebug() { var r = switchTo("DEBUG"); store(r); }
                    function goInfo() { var r = switchTo("INFO"); store(r);  }
                    function goWarning() { var r = switchTo("WARNING"); store(r);  }
                    function goError() { var r = switchTo("ERROR"); store(r);  }
                    function goInherit()
                    {
                        var parentNode = SUtilities.getDirectChildrenByTagName(stree.tree.selection.parentNode.parentNode.parentNode, "a")[0];
                        var r = switchTo(parentNode.getAttribute("level"), null, "true");
                        store(r);
                    }
                    function goForce()
                    {
                        var r = switchTo(stree.tree.selection.getAttribute("level"), null, "true", true);
                        store(r);
                    }
                    
                    function store(results)
                    {
                        var s = "nb=" + results.length;
                        for (var i = 0; i &lt; results.length; i++)
                        {
                            var r = results[i];
                            s += "&amp;type_" + i + "=" + r.type 
                                + "&amp;cat_" + i + "=" + encodeURIComponent(r.category) 
                                + (r.inherited == "true" ? "&amp;inherit_" + i + "=true" : ("&amp;mode_" + i + "=" + r.mode));
                        }
                        
                        var u = getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/administrator/logs/change-levels";
                        
                        var ajaxResult = Tools.postFromUrl(u, s);
                        if (ajaxResult == null || Tools.getFromXML(ajaxResult, "error") != "")
                        {
                            alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_HANDLE_ERROR"/>");
                            window.location.href = "view.html";
                        } 
                    }
                    
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
    
    <xsl:template match="logger">
        <xsl:variable name="img-path"><xsl:text>loglevel_</xsl:text><xsl:value-of select="translate(@priority, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/><xsl:if test="@inherited = 'true'">_inherited</xsl:if></xsl:variable>
        <img src="{$resourcesPath}/img/administrator/logs/{$img-path}.gif"/>

        <a type="logkit" category="{@category}" level="{@priority}" inherited="{@inherited}">
            <xsl:if test="@name = ''"><xsl:attribute name="style">font-style: italic;</xsl:attribute></xsl:if>
            <xsl:value-of select="@name"/>
            <xsl:if test="@name = ''"><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_CONFIG_LOGKITROOT"/></xsl:if>
        </a>
        <xsl:if test="logger">
            <ul>
                <xsl:for-each select="logger">
                    <xsl:sort select="@name"/>
    
                    <li><xsl:apply-templates select="."/></li>
                </xsl:for-each>
            </ul>
      </xsl:if>
        
<!--     
      <tr>
        <td><xsl:value-of select="@name"/></td>
        <td><xsl:value-of select="@priority"/><xsl:if test="@inherited='true'"> (inherited)</xsl:if></td>
        <td>
          <select name="priority_{@category}">
            <option value="-"><xsl:if test="@inherited='true' or @priority='-'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>inherit</option>
            <option value="DEBUG"><xsl:if test="@inherited='false' and @priority='DEBUG'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>DEBUG</option>
            <option value="INFO"><xsl:if test="@inherited='false' and @priority='INFO'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>INFO</option>
            <option value="WARN"><xsl:if test="@inherited='false' and @priority='WARN'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>WARN</option>
            <option value="ERROR"><xsl:if test="@inherited='false' and @priority='ERROR'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>ERROR</option>
            <option value="FATAL_ERROR"><xsl:if test="@inherited='false' and @priority='FATAL_ERROR'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>FATAL_ERROR</option>
          </select>
        </td>
      </tr>
      <xsl:apply-templates select="logger">
        <xsl:sort select="@name"/>
      </xsl:apply-templates>
-->
    </xsl:template>
    
</xsl:stylesheet>