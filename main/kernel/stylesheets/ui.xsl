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
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:i18n="http://apache.org/cocoon/i18n/2.1">

    <!-- +
         | Load and initialize all scripts for UI
         + -->
    <xsl:template name="ui-load">
        <xsl:param name="pluginsDirectContext"/>
        <xsl:param name="pluginsWrappedContext"/>
        
        <script type="text/javascript">
            <xsl:comment>
                /* Load context */
                var context = {};
                context.contextPath = "<xsl:value-of select="$contextPath"/>";
                context.workspaceName = "<xsl:value-of select="$workspaceName"/>";
                context.workspaceContext = "<xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/>";
                
                function getPluginDirectUrl(plugin)
                {
                    return "<xsl:value-of select="$pluginsDirectContext"/>/" + plugin;
                }
    
                function getPluginResourcesUrl(plugin)
                {
                    return context.contextPath + "/plugins/" + plugin + "/resources";
                }
    
                function getPluginWrappedUrl(plugin)
                {
                    return "<xsl:value-of select="$pluginsWrappedContext"/>/" + plugin;
                }
      
    			function runtimeRedirectTo(link, mode, plugin)
    			{
    			  	function _checkLink (link)
    			  	{
    				  	if (link.substring(0, 1) != '/')
    				  	{
    				    	link = '/' + link;
    				  	}
    				  	return link;
    				}
    			  	
    			  	switch (mode)
    			  	{
    			  		case 'absolute': 
    			  			break;
    			  		case 'context':
    			  			link = context.contextPath + _checkLink(link);
    			  			break;
    			  		case 'workspace':
    			  			link = context.workspaceContext + _checkLink(link);
    			  			break;
    			  		case 'plugin-direct':
    			  			link = getPluginDirectUrl(plugin) + _checkLink(link);
    			  			break;
    			  		case 'plugin-wrapped':
    			  		default:
    			  			link = getPluginWrappedUrl(plugin) + _checkLink(link);
    			  	}
    			
    				window.location.href = link;
    			}
            </xsl:comment>
        </script>
    </xsl:template>
    
    <xsl:template name="ui-tools-load">
        <xsl:param name="bad-navigator-redirection"/>
        <xsl:param name="accept-ie-6">false</xsl:param>
        <xsl:param name="accept-ie-7">false</xsl:param>
        <xsl:param name="accept-ff-1.0">false</xsl:param>
        <xsl:param name="accept-ff-1.5">false</xsl:param>
        <xsl:param name="accept-ff-2.0">false</xsl:param>
        <xsl:param name="accept-ff-3.0">false</xsl:param>
    
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/Tools.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/mozxpath.js"><xsl:comment>empty</xsl:comment></script>
        
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/SUtilities/SUtilities.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/SUtilities/SProtectLayer.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/SUtilities/STooltip.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/SUtilities/SFullTable.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/SUtilities/SDragNDrop.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript">
            <xsl:comment>
                SUtilities.loadStyle("<xsl:value-of select="$contextPath"/>/kernel/resources/css/SUtilities/STooltip.css");
                SUtilities.loadStyle("<xsl:value-of select="$contextPath"/>/kernel/resources/css/SUtilities/SDragNDrop.css");
            </xsl:comment>
        </script>

        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/STools/STools.js"><xsl:comment>empty</xsl:comment></script>
        
        <script type="text/javascript">
            <xsl:comment>
                if (!(
                    <xsl:if test="$accept-ie-6 = 'true'">(STools.agt.indexOf("msie 6")) > 0 ||</xsl:if>
                    <xsl:if test="$accept-ie-7 = 'true'">(STools.agt.indexOf("msie 7")) > 0 ||</xsl:if>
                    <xsl:if test="$accept-ff-1.0 = 'true'">(STools.agt.indexOf("firefox/1.0")) > 0 ||</xsl:if>
                    <xsl:if test="$accept-ff-1.5 = 'true'">(STools.agt.indexOf("firefox/1.5")) > 0 ||</xsl:if>
                    <xsl:if test="$accept-ff-2.0 = 'true'">(STools.agt.indexOf("firefox/2.0")) > 0 ||</xsl:if>
                    <xsl:if test="$accept-ff-3.0 = 'true'">(STools.agt.indexOf("firefox/3.0")) > 0 ||</xsl:if>
                    1 == 0))
                {
                	// Check the cookie for forcing non supported navigators
                	var matcher = document.cookie.match("(^|;) ?ametys\.accept\.non\.supported\.navigators=([^;]*)");
                	if (!matcher || matcher[2] != "on")
                	{
                 	    window.location.href = "<xsl:value-of select="$bad-navigator-redirection"/>?uri=" + encodeURIComponent(window.location.href);
                	}
                }
            </xsl:comment>
        </script>
        
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/STools/SShortcut.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/STools/SToolBar.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/STools/SMenu.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/STools/SDialog.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/STools/STab.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/STools/SContextualMenu.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/STools/STree.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/STools/SContextualPanel.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/STools/SListView.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript">
            STools.Ressource = "<xsl:value-of select="$contextPath"/>/kernel/resources/img/STools/";
            SContextualPanel.css = "<xsl:value-of select="$contextPath"/>/kernel/resources/css/STools/";
            STree.css = "<xsl:value-of select="$contextPath"/>/kernel/resources/css/STools/";
            STree.img = "<xsl:value-of select="$contextPath"/>/kernel/resources/img/STools/";
            SListView.css = "<xsl:value-of select="$contextPath"/>/kernel/resources/css/STools/";
            SListView.viewerNoPreviewMessage = "<i18n:text i18n:key="KERNEL_UI_LISTVIEW_PREVIEW_NOTAVAILABLE" i18n:catalogue="kernel"/>";
        </script>
    </xsl:template>

    <xsl:template name="ui-text">
        <xsl:param name="text"/>
        
        <xsl:copy-of select="$text/node()"/>
    </xsl:template>
    
    <xsl:template name="ui-toolbar-place">
        <xsl:param name="id"/>

        <table id="runtime-toolbar-{$id}"/>
    </xsl:template>
    
    <xsl:template name="ui-toolbar-begin">
        <xsl:param name="id"/>
        <xsl:param name="height">18px</xsl:param>
        
        <script type="text/javascript">
            <xsl:comment>
                var toolBar_<xsl:value-of select="$id"/> = new SToolBar("runtime-toolbar-<xsl:value-of select="$id"/>");
                    toolBar_<xsl:value-of select="$id"/>.ui.config.toolBarStyle['height'] = "<xsl:value-of select="$height"/>";
            </xsl:comment>
        </script>
    </xsl:template>

    <xsl:template name="ui-toolbar-end">
        <xsl:param name="id"/>
        
        <script type="text/javascript">
            <xsl:comment>
                toolBar_<xsl:value-of select="$id"/>.paint();
            </xsl:comment>
        </script>
    </xsl:template>    

    <xsl:template name="ui-toolbar-addmenubar">
        <xsl:param name="toolbarId"/>
        <xsl:param name="menuData"/>

        <script type="text/javascript">
            <xsl:comment>
                var menuBar_<xsl:value-of select="$toolbarId"/> = toolBar_<xsl:value-of select="$toolbarId"/>.addMenuBar();
            </xsl:comment>
        </script>   
        <script type="text/javascript">
               <xsl:for-each select="$menuData/menu[.//UIItem]">
                    <xsl:variable name="menuVar">menu_<xsl:value-of select="$toolbarId"/>_<xsl:value-of select="position()"/></xsl:variable>
                    var <xsl:value-of select="$menuVar"/> = menuBar_<xsl:value-of select="$toolbarId"/>.addMenu("<xsl:call-template name="ui-text"><xsl:with-param name="text" select="label"/></xsl:call-template>");
                    
                    <xsl:call-template name="ui-toolbar-addmenubar-items">
                        <xsl:with-param name="menuVar" select="$menuVar"/>
                        <xsl:with-param name="itemsData" select="items"/>
                    </xsl:call-template>
                    
                </xsl:for-each>
        </script>
    </xsl:template>
    
	<xsl:template name="ui-toolbar-addmenubar-items">
        <xsl:param name="menuVar"/>
        <xsl:param name="itemsData"/>

        <xsl:for-each select="$itemsData/*">
       
            <xsl:choose>
                
                    <!-- ELEMENT -->
                    <xsl:when test="local-name() = 'UIItem'">
                        <xsl:value-of select="$menuVar"/>.addEntry(
                            "<xsl:call-template name="ui-text"><xsl:with-param name="text" select="Label"/></xsl:call-template>",
                            "<xsl:value-of select="$contextPath"/><xsl:value-of select="Icons/Small"/>",
                            { 
                                isEnabled: function (arg) {return <xsl:choose><xsl:when test="@disabled or not (Action)">false</xsl:when><xsl:otherwise>true</xsl:otherwise></xsl:choose>;}
                                <xsl:if test="Action">,
                                act : function () { 
                                    var params = {<xsl:for-each select="Action/Parameters/*">
                                                <xsl:text>"</xsl:text><xsl:value-of select="local-name()"/>" : "<xsl:value-of select="."/><xsl:text>"</xsl:text>
                                                <xsl:if test="position() != last()">, </xsl:if>
                                            </xsl:for-each>};
                                    <xsl:value-of select="Action/ClassName"/>.act("<xsl:value-of select="Action/@plugin"/>", params); }</xsl:if>
                            },
                            <xsl:choose>
                                <xsl:when test="Shortcut">
                                    "<xsl:if test="Shortcut/@CTRL">Ctrl+</xsl:if><xsl:if test="Shortcut/@ALT">Alt+</xsl:if><xsl:if test="Shortcut/@SHIFT">Shift+</xsl:if><xsl:value-of select="Shortcut"/>"
                                </xsl:when>
                                <xsl:otherwise>
                                null
                                </xsl:otherwise>
                            </xsl:choose>);
                    </xsl:when>
                    
                    <!-- SEPARATEUR non inutile -->
                    <xsl:when test="preceding-sibling::*[local-name() = 'UIItem' or (local-name() = 'menu' and .//UIItem)] and local-name() = 'separator' and following-sibling::*[position() = 1 and (local-name() = 'UIItem' or (local-name() = 'menu' and .//UIItem))]">
                        <xsl:value-of select="$menuVar"/>.addSeparator();
                    </xsl:when>

                    <!-- SOUS-MENU non vide -->
                    <xsl:when test="local-name() = 'menu' and .//UIItem">
                        <xsl:variable name="subMenuVar"><xsl:value-of select="$menuVar"/>_<xsl:value-of select="position()"/></xsl:variable>
                    
                        var <xsl:value-of select="$subMenuVar"/> = <xsl:value-of select="$menuVar"/>.addSubMenu("<xsl:call-template name="ui-text"><xsl:with-param name="text" select="label"/></xsl:call-template>");
                        <xsl:call-template name="ui-toolbar-addmenubar-items">
                            <xsl:with-param name="menuVar" select="$subMenuVar"/>
                            <xsl:with-param name="itemsData" select="items"/>
                        </xsl:call-template>
                    </xsl:when>
                    
                    <!-- OBJETS INUTILES -->
                    <xsl:otherwise/>
            </xsl:choose>
            
        </xsl:for-each>
        
    </xsl:template>
    
    
    <xsl:template name="ui-toolbar-addinfo">
        <xsl:param name="toolbarId"/>
        <xsl:param name="infoData"/>
        <xsl:param name="showLabel">true</xsl:param>

        <script type="text/javascript">
                <xsl:choose>
                    
                        <!-- ELEMENT -->
                        <xsl:when test="local-name() = 'UIItem'">
                            toolBar_<xsl:value-of select="$toolbarId"/>.addButton (
                                "<xsl:if test="$showLabel = 'true'"><xsl:call-template name="ui-text"><xsl:with-param name="text" select="Label"/></xsl:call-template></xsl:if>",
                                "<xsl:value-of select="$contextPath"/><xsl:value-of select="Icons/Small"/>",
                                <xsl:choose>
                                	<xsl:when test="Action">
    	                            { 
    	                                isEnabled: function (arg) {return <xsl:choose><xsl:when test="@disabled">false</xsl:when><xsl:otherwise>true</xsl:otherwise></xsl:choose>;},
    	                                act : function () { 
    	                                    var params = {<xsl:for-each select="Action/Parameters/*">
    	                                                <xsl:text>"</xsl:text><xsl:value-of select="local-name()"/>" : "<xsl:value-of select="."/><xsl:text>"</xsl:text>
    	                                                <xsl:if test="position() != last()">, </xsl:if>
    	                                            </xsl:for-each>};
    	                                    <xsl:value-of select="Action/ClassName"/>.act("<xsl:value-of select="Action/@plugin"/>", params); }
    	                            }
    	                            </xsl:when>
    	                            <xsl:otherwise>null</xsl:otherwise>
    	                        </xsl:choose>,
                                <xsl:choose>
                                    <xsl:when test="Shortcut">
                                        "<xsl:if test="Shortcut/@CTRL">Ctrl+</xsl:if><xsl:if test="Shortcut/@ALT">Alt+</xsl:if><xsl:if test="Shortcut/@SHIFT">Shift+</xsl:if><xsl:value-of select="Shortcut"/>"
                                    </xsl:when>
                                    <xsl:otherwise>
                                    null
                                    </xsl:otherwise>
                                </xsl:choose>,     
                                "<xsl:call-template name="ui-text"><xsl:with-param name="text" select="Description"/></xsl:call-template>",                       
                                null,
                                null
                            );
                        </xsl:when>
    
                        <!-- SEPARATOR -->
                        <xsl:when test="preceding-sibling::*[local-name() = 'UIItem'] and local-name() = 'separator' and following-sibling::*[position() = 1 and local-name() = 'UIItem']">
                            toolBar_<xsl:value-of select="$toolbarId"/>.addSeparator();
                        </xsl:when>
                        
                        <!-- OBJETS INUTILES -->
                        <xsl:otherwise/>
                        
                </xsl:choose>
        </script>
    </xsl:template>
    
</xsl:stylesheet>