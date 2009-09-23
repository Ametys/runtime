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
        <xsl:param name="accept-ie-8">false</xsl:param>
        <xsl:param name="accept-ff-1.0">false</xsl:param>
        <xsl:param name="accept-ff-1.5">false</xsl:param>
        <xsl:param name="accept-ff-2.0">false</xsl:param>
        <xsl:param name="accept-ff-3.0">false</xsl:param>
        <xsl:param name="accept-ff-3.5">false</xsl:param>
        <xsl:param name="accept-sa-3">false</xsl:param>
        <xsl:param name="accept-op-9">false</xsl:param>
        <xsl:param name="accept-ch-1">false</xsl:param>
        <xsl:param name="debug-mode">false</xsl:param>
		
        <script type="text/javascript">
            <xsl:comment>
            	var userAgent = navigator.userAgent.toLowerCase();
                <xsl:text>if (!(</xsl:text>
                    <xsl:if test="$accept-ie-6 = 'true'">(userAgent.indexOf("msie 6") > 0) ||</xsl:if>
                    <xsl:if test="$accept-ie-7 = 'true'">(userAgent.indexOf("msie 7") > 0) ||</xsl:if>
                    <xsl:if test="$accept-ie-8 = 'true'">(userAgent.indexOf("msie 8") > 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-1.0 = 'true'">(userAgent.indexOf("firefox/1.0") > 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-1.5 = 'true'">(userAgent.indexOf("firefox/1.5") > 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-2.0 = 'true'">(userAgent.indexOf("firefox/2.0") > 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-3.0 = 'true'">(userAgent.indexOf("firefox/3.0") > 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-3.5 = 'true'">(userAgent.indexOf("firefox/3.5") > 0) ||</xsl:if>
                    <xsl:if test="$accept-sa-3 = 'true'">(userAgent.indexOf("safari/522") > 0) ||</xsl:if>
                    <xsl:if test="$accept-sa-3 = 'true'">(userAgent.indexOf("safari/525") > 0) ||</xsl:if>
                    <xsl:if test="$accept-op-9 = 'true'">(userAgent.indexOf("opera/9") > 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-1 = 'true'">(userAgent.indexOf("chrome/1") > 0) ||</xsl:if>
                    <xsl:text>1 == 0))</xsl:text>
                {
            		<!-- Check the cookie for forcing non supported navigators -->
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

		<xsl:variable name="extjs-debug-suffix"><xsl:if test="$debug-mode = 'true'">-debug</xsl:if></xsl:variable>
    	<script type="text/javascript" src="{$contextPath}/plugins/extjs/resources/js/adapter/ext/ext-base{$extjs-debug-suffix}.js"><xsl:comment>empty</xsl:comment></script>
		<script type="text/javascript" src="{$contextPath}/plugins/extjs/resources/js/ext-all{$extjs-debug-suffix}.js"><xsl:comment>empty</xsl:comment></script>
		
		<script type="text/javascript">
			<xsl:comment>
				Date.patterns = {
				    ISO8601Long:"Y-m-d\\TH:i:s.uP",
				    ISO8601Short:"Y-m-d",
				    ShortDate: "n/j/Y",
				    LongDate: "l, F d, Y",
				    FullDateTime: "l, F d, Y g:i:s A",
				    MonthDay: "F d",
				    ShortTime: "g:i A",
				    LongTime: "g:i:s A",
				    SortableDateTime: "Y-m-d\\TH:i:s",
				    UniversalSortableDateTime: "Y-m-d H:i:sO",
				    YearMonth: "F, Y"
				};
			</xsl:comment>
		</script>
		
		<script type="text/javascript">
            Tools.loadScript(document, "<xsl:value-of select="$contextPath"/>/plugins/extjs/resources/js/locale/ext-lang-<i18n:text i18n:key="KERNEL_LANGUAGE_CODE" i18n:catalogue="kernel"/>.js");
		</script>
	
		<link rel="stylesheet" type="text/css" href="{$contextPath}/plugins/extjs/resources/css/ext-all.css" />

        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/org/ametys/DialogBox.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/org/ametys/ListView.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/org/ametys/EditorListView.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/org/ametys/Tree.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/org/ametys/Utility.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/org/ametys/DesktopPanel.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/org/ametys/DesktopCategory.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/org/ametys/DesktopItem.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/org/ametys/HtmlContainer.js"><xsl:comment></xsl:comment></script>
        
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/org/ametys/form/TextField.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/org/ametys/form/TextAreaField.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/org/ametys/form/LongField.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/org/ametys/form/DoubleField.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/org/ametys/form/BooleanField.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/org/ametys/form/PasswordField.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/org/ametys/form/PasswordWidget.i18n.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/org/ametys/form/DateField.js"><xsl:comment>empty</xsl:comment></script>
        <script type="text/javascript" src="{$contextPath}/kernel/resources/js/org/ametys/form/ComboField.js"><xsl:comment>empty</xsl:comment></script>
		       
		<link rel="stylesheet" href="{$contextPath}/kernel/resources/css/import.css" type="text/css"/>
    </xsl:template>

</xsl:stylesheet>