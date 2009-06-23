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
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
                xmlns:xalan="http://xml.apache.org/xalan"
                exclude-result-prefixes="xalan">
	
    <xsl:import href="resource://org/ametys/runtime/kernel/stylesheets/ui.xsl"/>
    
    <xsl:param name="contextPath"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
    
    <xsl:variable name="workspaceContext"><xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/></xsl:variable>

	<xsl:template match="/">
		<html>
			<!-- ****** HEAD ****** -->
			<head> 
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
				<title>
					<i18n:text i18n:catalogue="workspace.{$workspaceName}" i18n:key="WORKSPACE_ADMIN_LABEL_SHORT"/>
					<xsl:text> </xsl:text>
					<i18n:text i18n:catalogue="application" i18n:key="APPLICATION_PRODUCT_LABEL"/>
				</title>  
				
                <link rel="icon" type="image/gif" href="{$contextPath}/kernel/resources/img/runtime_favico.gif" />
                <link rel="shortcut icon" type="image/x-icon" href="{$contextPath}/kernel/resources/img/runtime_favico.ico" />
				<link rel="stylesheet" href="{$workspaceContext}/resources/css/admin.css" type="text/css"/>
				<link rel="stylesheet" href="{$workspaceContext}/resources/css/panel.css" type="text/css"/>
		        <link rel="stylesheet" href="{$workspaceContext}/resources/css/form.css" type="text/css"/>
		        
				<!-- loading ui library -->
                <xsl:call-template name="ui-load">
                    <xsl:with-param name="pluginsDirectContext" select="concat($workspaceContext, '/plugins')"/>
                    <xsl:with-param name="pluginsWrappedContext" select="concat($workspaceContext, '/_plugins')"/>
                </xsl:call-template>
                <xsl:call-template name="ui-tools-load">
                    <xsl:with-param name="bad-navigator-redirection"><xsl:value-of select="$workspaceContext"/>/public/navigator.html</xsl:with-param>

                    <xsl:with-param name="accept-ie-6">true</xsl:with-param>
                    <xsl:with-param name="accept-ie-7">true</xsl:with-param>
                    <xsl:with-param name="accept-ff-1.0">true</xsl:with-param>
                    <xsl:with-param name="accept-ff-1.5">true</xsl:with-param>
                    <xsl:with-param name="accept-ff-2.0">true</xsl:with-param>
                    <xsl:with-param name="accept-ff-3.0">true</xsl:with-param>
                </xsl:call-template>
                
                <xsl:call-template name="workspace-head"/>
                
                <script type="text/javascript" src="{$workspaceContext}/resources/js/Ext/ametys/Fieldset.js"><xsl:comment>empty</xsl:comment></script>
                <script type="text/javascript" src="{$workspaceContext}/resources/js/Ext/ametys/form/TextField.js"><xsl:comment>empty</xsl:comment></script>
                <script type="text/javascript" src="{$workspaceContext}/resources/js/Ext/ametys/form/TextAreaField.js"><xsl:comment>empty</xsl:comment></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/Ext/ametys/form/LongField.js"><xsl:comment>empty</xsl:comment></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/Ext/ametys/form/DoubleField.js"><xsl:comment>empty</xsl:comment></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/Ext/ametys/form/BooleanField.js"><xsl:comment>empty</xsl:comment></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/Ext/ametys/form/PasswordField.js"><xsl:comment>empty</xsl:comment></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/Ext/ametys/form/PasswordWidget.i18n.js"><xsl:comment>empty</xsl:comment></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/Ext/ametys/form/DateField.js"><xsl:comment>empty</xsl:comment></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/Ext/ametys/form/ComboField.js"><xsl:comment>empty</xsl:comment></script>
		       
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/Ext/ametys/ActionsPanel.js"><xsl:comment>empty</xsl:comment></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/Ext/ametys/Action.js"><xsl:comment>empty</xsl:comment></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/Ext/ametys/TextPanel.js"><xsl:comment>empty</xsl:comment></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/Ext/ametys/NavigationPanel.js"><xsl:comment>empty</xsl:comment></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/Ext/ametys/NavigationItem.js"><xsl:comment>empty</xsl:comment></script>
		        
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/Ext/ametys/DockItem.js"><xsl:comment>empty</xsl:comment></script>
		        <script type="text/javascript" src="{$workspaceContext}/resources/js/Ext/ametys/AdminTools.js"><xsl:comment>empty</xsl:comment></script>
                <script type="text/javascript" src="{$workspaceContext}/resources/js/workspace.js"><xsl:comment>//empty</xsl:comment></script>
                
                <xsl:comment>[if lt IE 7]&gt;
						&lt;script defer="defer" type="text/javascript" src="<xsl:value-of select="$workspaceContext"/>/resources/js/pngfix.js">&lt;/script&gt;
				&lt;![endif]</xsl:comment>
				<xsl:call-template name="workspace-script"/>
			</head>
		
			<!-- ****** BODY ****** -->
			<body>
				<div id="logo"><xsl:comment></xsl:comment></div>
				
				<div id="versions">
					<xsl:for-each select="/Admin/Versions/Component|/Plugins/Versions/Component">
						<span class="title"><xsl:value-of select="Name"/>&#160;-&#160;</span>
						<xsl:value-of select="Version"/>
						<xsl:if test="Date">&#160;du <xsl:value-of select="Date"/> à <xsl:value-of select="Time"/></xsl:if>
						<xsl:if test="position() != last()">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</xsl:if>
					</xsl:for-each>
				</div>
				
				<xsl:call-template name="workspace-body"/>
			</body>
		</html>		
	</xsl:template>
    
    <xsl:template name="workspace-head"/>
    <xsl:template name="workspace-title"/>
    <xsl:template name="workspace-body"/>
    <xsl:template name="workspace-script"/>
	
</xsl:stylesheet>
	