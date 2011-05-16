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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
                xmlns:xalan="http://xml.apache.org/xalan"
                exclude-result-prefixes="xalan">
	
    <xsl:import href="resource://org/ametys/runtime/kernel/stylesheets/ui.xsl"/>
    
    <xsl:param name="contextPath"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
    
    <xsl:variable name="workspaceContext"><xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/></xsl:variable>
    
    <xsl:template name="administrator-css">
		<style type="text/css">
			#top { background-image: url('<xsl:value-of select="$workspaceContext"/>/resources/img/top_<i18n:text i18n:key="KERNEL_LANGUAGE_CODE" i18n:catalogue="kernel"/>.jpg'); } 
		</style>
    </xsl:template>

	<xsl:template match="/">
		<html>
			<!-- ****** HEAD ****** -->
			<head> 
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
				<meta http-equiv="X-UA-Compatible" content="IE=8" />
				<title>
					<i18n:text i18n:catalogue="workspace.{$workspaceName}" i18n:key="WORKSPACE_ADMIN_LABEL_SHORT"/>
					<xsl:text> </xsl:text>
					<i18n:text i18n:catalogue="application" i18n:key="APPLICATION_PRODUCT_LABEL"/>
				</title>  
				
                <link rel="icon" type="image/gif" href="{$contextPath}/kernel/resources/img/runtime_favico.gif" />
                <link rel="shortcut icon" type="image/x-icon" href="{$contextPath}/kernel/resources/img/runtime_favico.ico" />
                <link rel="stylesheet" href="{$contextPath}/kernel/resources/css/homepage/view.css" type="text/css"/>
				<link rel="stylesheet" href="{$workspaceContext}/resources/css/admin.css" type="text/css"/>
				<link rel="stylesheet" href="{$workspaceContext}/resources/css/panel.css" type="text/css"/>
		        
		        <xsl:call-template name="administrator-css"/>
				
				<!-- loading ui library -->
                <xsl:call-template name="ui-load">
                    <xsl:with-param name="pluginsDirectContext" select="concat($workspaceContext, '/plugins')"/>
                    <xsl:with-param name="pluginsWrappedContext" select="concat($workspaceContext, '/_plugins')"/>
                </xsl:call-template>
                <xsl:call-template name="ui-tools-load">
                    <xsl:with-param name="bad-navigator-redirection"><xsl:value-of select="$workspaceContext"/>/public/navigator.html</xsl:with-param>

                    <xsl:with-param name="accept-ie-6">true</xsl:with-param>
                    <xsl:with-param name="accept-ie-7">true</xsl:with-param>
                    <xsl:with-param name="accept-ie-8">true</xsl:with-param>
                    <xsl:with-param name="accept-ie-9">true</xsl:with-param>
                    <xsl:with-param name="accept-ff-1.0">false</xsl:with-param>
                    <xsl:with-param name="accept-ff-1.5">true</xsl:with-param>
                    <xsl:with-param name="accept-ff-2.0">true</xsl:with-param>
                    <xsl:with-param name="accept-ff-3.0">true</xsl:with-param>
                    <xsl:with-param name="accept-ff-3.5">true</xsl:with-param>
                    <xsl:with-param name="accept-ff-3.6">true</xsl:with-param>
                    <xsl:with-param name="accept-ff-4.0">true</xsl:with-param>
	                <xsl:with-param name="accept-sa-3">true</xsl:with-param>
	                <xsl:with-param name="accept-sa-4">true</xsl:with-param>
	                <xsl:with-param name="accept-sa-5">true</xsl:with-param>
	                <xsl:with-param name="accept-op-9">true</xsl:with-param>
	                <xsl:with-param name="accept-op-10">true</xsl:with-param>
	                <xsl:with-param name="accept-ch-1">true</xsl:with-param>
	                <xsl:with-param name="accept-ch-2">true</xsl:with-param>
	                <xsl:with-param name="accept-ch-3">true</xsl:with-param>
	                <xsl:with-param name="accept-ch-4">true</xsl:with-param>
	                <xsl:with-param name="accept-ch-5">true</xsl:with-param>
	                <xsl:with-param name="accept-ch-6">true</xsl:with-param>
	                <xsl:with-param name="accept-ch-7">true</xsl:with-param>
	                <xsl:with-param name="accept-ch-8">true</xsl:with-param>
	                <xsl:with-param name="accept-ch-9">true</xsl:with-param>
	                <xsl:with-param name="accept-ch-10">true</xsl:with-param>
	                <xsl:with-param name="accept-ch-11">true</xsl:with-param>
	                <xsl:with-param name="use-css-component">false</xsl:with-param>
	                <xsl:with-param name="use-js-component">false</xsl:with-param>
	                <xsl:with-param name="debug-mode">true</xsl:with-param>
                </xsl:call-template>
                
                <xsl:call-template name="workspace-head"/>
                
                <script type="text/javascript" src="{$workspaceContext}/resources/js/org/ametys/Fieldset.js"></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/org/ametys/ActionsPanel.js"></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/org/ametys/Action.js"></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/org/ametys/TextPanel.js"></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/org/ametys/NavigationPanel.js"></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/org/ametys/NavigationItem.js"></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/org/ametys/DesktopPanel.js"></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/org/ametys/DesktopCategory.js"></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/org/ametys/DesktopItem.js"></script>
		        
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/org/ametys/DockItem.js"></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/org/ametys/AdminTools.js"></script>
                <script type="text/javascript" src="{$contextPath}/kernel/resources/js/org/ametys/runtime/HomePage.js"><xsl:comment>//empty</xsl:comment></script>
                
                <script type="text/javascript">
                	<xsl:if test="/Admin/Versions/Component|/Plugins/Versions/Component">
	                	
	                	var data = {versions : []};
	                	<xsl:for-each select="/Admin/Versions/Component|/Plugins/Versions/Component">
	                		data.versions.push({
	                			name : "<xsl:value-of select="Name"/>",
	                			version: "<xsl:choose><xsl:when test="Version"><xsl:value-of select="Version"/></xsl:when><xsl:otherwise><i18n:text i18n:key="WORKSPACE_ADMIN_VERSION_UNKNOWN" i18n:catalogue="workspace.{$workspaceName}"/></xsl:otherwise></xsl:choose>",
	                			date : "<xsl:if test="Date">&#160;<i18n:text i18n:key="WORKSPACE_ADMIN_VERSION_DATED" i18n:catalogue="workspace.{$workspaceName}"/>&#160;<xsl:value-of select="Date"/>&#160;<i18n:text i18n:key="WORKSPACE_ADMIN_VERSION_DATEDTIME" i18n:catalogue="workspace.{$workspaceName}"/>&#160;<xsl:value-of select="Time"/></xsl:if><xsl:if test="position() != last()">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</xsl:if>"
	                		});
	                	</xsl:for-each>
	                	org.ametys.runtime.HomePage._tplFooter = new Ext.XTemplate (
	                		'&lt;div id="versions"&gt;',
	                		'&lt;tpl for="versions"&gt;',
	                		'&lt;span class="title"&gt;{name}&#160;-&#160;&lt;/span&gt;',
	                		'{version}',
	                		'{date}',
	                		'&lt;/tpl&gt;',
	                		'&lt;/div&gt;'
	                	);
	                	
	                	org.ametys.runtime.HomePage._tplFooter.compile();
	                	
	              		org.ametys.runtime.HomePage.drawFooterPanel = function ()
						{
							return new org.ametys.HtmlContainer (
							{
			   					border: false,
			   					html : '',
			   					listeners: {
							        'render' : function(p) {
							        	org.ametys.runtime.HomePage._tplFooter.overwrite(p.getEl(), data);
							        }
			    				}
			
							});
						}
					</xsl:if>
                </script>
                
                <xsl:comment>[if lt IE 7]&gt;
						&lt;script defer="defer" type="text/javascript" src="<xsl:value-of select="$workspaceContext"/>/resources/js/pngfix.js">&lt;/script&gt;
				&lt;![endif]</xsl:comment>

				<xsl:call-template name="workspace-script"/>
			</head>
		
			<!-- ****** BODY ****** -->
			<body>
				<xsl:call-template name="workspace-body"/>
				
				<noscript><div id="no-script"><i18n:text i18n:key="WORKSPACE_ADMIN_ERROR_NOJS" i18n:catalogue="workspace.{$workspaceName}"/></div></noscript>
			</body>
		</html>		
	</xsl:template>
    
    <xsl:template name="workspace-head"/>
    <xsl:template name="workspace-title"/>
    <xsl:template name="workspace-body"/>
    <xsl:template name="workspace-script"/>
	
</xsl:stylesheet>
	