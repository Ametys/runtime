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
    
    	<script type="text/javascript" src="{$contextPath}/kernel/resources/js/ExtJS/ext-base.js"><xsl:comment>empty</xsl:comment></script>
		<script type="text/javascript" src="{$contextPath}/kernel/resources/js/ExtJS/ext-core.js"><xsl:comment>empty</xsl:comment></script>
		<script type="text/javascript" src="{$contextPath}/kernel/resources/js/ExtJS/ext-all.js"><xsl:comment>empty</xsl:comment></script>
	
		<link rel="stylesheet" type="text/css" href="{$contextPath}/kernel/resources/css/ExtJS/ext-all.css" />
		
        <script type="text/javascript">
            <xsl:comment>
            	if (! (Ext.isIE6 || Ext.isIE7 || Ext.isOpera || Ext. isSafari || Ext. isSafari2 || Ext. isSafari3 || Ext.isGecko || Ext.isGecko2 || Ext.isGecko3))
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
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/Tools.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/Runtime_InteractionActionLibrary.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/mozxpath.js"><xsl:comment>empty</xsl:comment></script>
        
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/ametys/Ext.ametys.ActionPanel.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/ametys/Ext.ametys.CategoryPanel.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/ametys/Ext.ametys.ContextualPanel.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/ametys/Ext.ametys.DialogBox.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/ametys/Ext.ametys.Form.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/ametys/Ext.ametys.Field.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/ametys/Ext.ametys.ListView.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/ametys/Ext.ametys.EditorListView.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/ametys/Ext.ametys.Tree.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/ametys/Ext.ametys.Utility.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/ametys/Ext.ametys.DesktopPanel.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/ametys/Ext.ametys.DesktopCategory.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/ametys/Ext.ametys.DesktopItem.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/ametys/Ext.ametys.HtmlContainer.js"><xsl:comment></xsl:comment></script>
        
        <link rel="stylesheet" href="{$contextPath}/kernel/resources/css/dialog.css" type="text/css"/>
       	<link rel="stylesheet" href="{$contextPath}/kernel/resources/css/form.css" type="text/css"/>
        <link rel="stylesheet" href="{$contextPath}/kernel/resources/css/grid.css" type="text/css"/>
        <link rel="stylesheet" href="{$contextPath}/kernel/resources/css/panel.css" type="text/css"/>
        <link rel="stylesheet" href="{$contextPath}/kernel/resources/css/desktop.css" type="text/css"/> 
    </xsl:template>

    <xsl:template name="ui-text">
        <xsl:param name="text"/>
        
        <xsl:copy-of select="$text/node()"/>
    </xsl:template>
    
    <xsl:template name="ui-toolbar-place">
    </xsl:template>
    
    <xsl:template name="ui-toolbar-begin">
    </xsl:template>

    <xsl:template name="ui-toolbar-end">
    </xsl:template>    

    <xsl:template name="ui-toolbar-addmenubar">
    </xsl:template>
    
	<xsl:template name="ui-toolbar-addmenubar-items">
    </xsl:template>
    
    
    <xsl:template name="ui-toolbar-addinfo">
    </xsl:template>
    
</xsl:stylesheet>