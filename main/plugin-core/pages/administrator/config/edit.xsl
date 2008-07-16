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
    
    <xsl:import href="plugin:core://stylesheets/widgets.xsl"/>
    
    <xsl:param name="contextPath"/>
    <xsl:param name="pluginName"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
    
    <xsl:variable name="workspaceContext"><xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/></xsl:variable>
    
    <xsl:variable name="resourcesPath"><xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources</xsl:variable>
    
    <xsl:template match="/">
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_TITLE"/></title>
                <LINK rel="stylesheet" href="{$workspaceContext}/resources/css/homepage.css" type="text/css"/>
                <LINK rel="stylesheet" href="{$resourcesPath}/css/administrator/config.css" type="text/css"/>
            </head>
            
            <body>
                <form name="form">
                    <table class="admin_index_main_table" style="width: 720px; height: 450px; table-layout: fixed;">
                        <colgroup>
                            <col width="233px"/>
                            <col/>
                        </colgroup>
                        <tr>
                            <td id="actionset"/>
                            <td  style="border: 1px inset #efebde; padding: 5px;">
                               <div style="overflow: auto; height: 430px; width: 470px;">
                                    <table id="view" style="width: 469px; height: 429px; table-layout: fixed"/>
                               </div>
                               
                               <xsl:for-each select="/config/categories/category">
                                    <div id="category_{position()}" style="overflow: auto; height: 100%; width: 460px; display: none;">
    
                                        <xsl:for-each select="groups/group">
                                            <table class="config" style="width: 409px; margin: 10px;">
                                                <caption><i18n:text i18n:catalogue="{@catalogue}" i18n:key="{@label}"/></caption>
                                                
                                                <colgroup>
                                                    <col width="246px"/>
                                                    <col width="173px"/>
                                                </colgroup>
                                                
                                                <tr>
                                                    <td colspan="2" style="height: 5px"/>
                                                </tr>
                                                
                                                <xsl:for-each select="*">
                                                    <xsl:sort select="order" data-type="number" order="ascending"/>
                                                    
                                                    <xsl:variable name="style">font-family: verdana; font-size: 10px; width: 167px;</xsl:variable>
                                                    
                                                    <tr>
                                                        <td class="label">
                                                            <xsl:attribute name="style">height: <xsl:choose><xsl:when test="type='password'">45px;</xsl:when><xsl:otherwise>25px;</xsl:otherwise></xsl:choose></xsl:attribute>

                                                            <i18n:text i18n:catalogue="{label/@catalogue}" i18n:key="{label}"/>
                                                            <img src="{$resourcesPath}/img/administrator/config/help.gif" i18n:attr="title" title="{description/@catalogue}:{description}"/>
                                                        </td>
                                                        <td style="vertical-align: middle">
                                                        
                                                            <xsl:choose>
                                                                <xsl:when test="enumeration">
                                                                    <xsl:call-template name="select-i18n-input">
                                                                        <xsl:with-param name="name" select="local-name(.)"/>
                                                                        <xsl:with-param name="values" select="enumeration"/>
                                                                        <xsl:with-param name="value" select="value"/>
                                                                        <xsl:with-param name="style" select="$style"/>
                                                                    </xsl:call-template>
                                                                </xsl:when>
                                                                
                                                                <xsl:when test="type='double'">
                                                                    <xsl:call-template name="double-input">
                                                                        <xsl:with-param name="name" select="local-name(.)"/>
                                                                        <xsl:with-param name="value" select="value"/>
                                                                        <xsl:with-param name="style" select="$style"/>
                                                                    </xsl:call-template>
                                                                </xsl:when>
                                                                <xsl:when test="type='boolean'">
                                                                    <xsl:call-template name="boolean-input">
                                                                        <xsl:with-param name="name" select="local-name(.)"/>
                                                                        <xsl:with-param name="value" select="value"/>
                                                                    </xsl:call-template>
                                                                </xsl:when>
                                                                <xsl:when test="type='date'">
                                                                    <xsl:call-template name="calendar-input">
                                                                        <xsl:with-param name="name" select="local-name(.)"/>
                                                                        <xsl:with-param name="value" select="value"/>
                                                                        <xsl:with-param name="style" select="$style"/>
                                                                    </xsl:call-template>
                                                                </xsl:when>
                                                                <xsl:when test="type='long'">
                                                                    <xsl:call-template name="long-input">
                                                                        <xsl:with-param name="name" select="local-name(.)"/>
                                                                        <xsl:with-param name="value" select="value"/>
                                                                        <xsl:with-param name="style" select="$style"/>
                                                                    </xsl:call-template>
                                                                </xsl:when>
                                                                <xsl:when test="type='password'">
                                                                    <xsl:call-template name="password-input">
                                                                        <xsl:with-param name="name" select="local-name(.)"/>
                                                                        <xsl:with-param name="value" select="value"/>
                                                                        <xsl:with-param name="style" select="concat($style, '; width: 150px')"/>
                                                                        <xsl:with-param name="styleTable" select="'height: 40px'"/>
                                                                        <xsl:with-param name="styleText" select="'font-family: verdana; font-size: 9px; font-style: italic; height: 20px'"/>
                                                                    </xsl:call-template>
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <xsl:call-template name="string-input">
                                                                        <xsl:with-param name="name" select="local-name(.)"/>
                                                                        <xsl:with-param name="value" select="value"/>
                                                                        <xsl:with-param name="style" select="$style"/>
                                                                    </xsl:call-template>
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                            
                                                        </td>
                                                    </tr>
                                                </xsl:for-each>
                                                                            
                                            </table>
                                        </xsl:for-each>
                                        
                                    </div>
                               </xsl:for-each>
                            </td>
                        </tr>
                    </table>
                </form>

                <script src="{$resourcesPath}/js/widgets.js.i18n">// empty</script>

                <script>
                    function goBack()
                    {
                        document.location.href = context.workspaceContext;
                    }
                    
                    function save()
                    {
                        var url = getPluginDirectUrl("<xsl:value-of select="$pluginName"/>") + "/administrator/config/set";
                        var args = Tools.buildQueryString(document.forms["form"], "");
                        
                        var result = Tools.postFromUrl(url, args);
                        if (result == null)
                        {
                            alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_FATALERROR"/>");
                            return;
                        }
                        
                        var error = Tools.getFromXML(result, "error");
                        if (error != null &amp;&amp; error != "")
                        {
                            alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_ERROR"/>" + error)
                            return;
                        }
                        
                        alert("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_OK"/>");
                        window.setTimeout(goBack, 100); // pour laisser au serveur le temps de redémarrer
                    }
                
                    var sp = new SContextualPanel("actionset");
                    var handle = sp.addCategory("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HANDLE"/>");
                        handle.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HANDLE_SAVE"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/config/save.gif", save);
                        handle.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HANDLE_QUIT"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/config/nosave.gif", goBack);
                    var help = sp.addCategory("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HELP"/>");
                        help.addElement("&lt;div style='font-size: 11px; color: #000000'&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HELP_TEXT"/>&lt;/div&gt;");
                    sp.paint();
                    
                    var myTab = new STab("view");
                    <xsl:for-each select="/config/categories/category">
                        myTab.addTab("<i18n:text i18n:catalogue="{@catalogue}" i18n:key="{@label}"/>", "category_<xsl:value-of select="position()"/>");
                    </xsl:for-each>
                    myTab.paint();
                </script>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>