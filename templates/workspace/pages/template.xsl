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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
     
    <xsl:import href="resource://org/ametys/runtime/kernel/stylesheets/ui.xsl"/>
    
    <xsl:param name="contextPath"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
    
    <xsl:template match="/">
        <HTML>
            <!-- ****** HEAD ****** -->
            <HEAD> 
                <TITLE>
                    <xsl:call-template name="workspace-title"/>
                    <xsl:text> </xsl:text>
                    <i18n:text i18n:catalogue="workspace.{$workspaceName}" i18n:key="WORKSPACE_APPLICATION_LABEL_SHORT"/>
                    <xsl:text> </xsl:text>
                    <i18n:text i18n:catalogue="application" i18n:key="APPLICATION_PRODUCT_LABEL"/>
                </TITLE>  
                
                <META http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                
                <LINK rel="icon" type="image/gif" href="{$contextPath}/kernel/resources/img/runtime_favico.gif" />
                <link rel="shortcut icon" type="image/x-icon" href="{$contextPath}/kernel/resources/img/runtime_favico.ico" />

                <LINK rel="stylesheet" href="{$contextPath}{$workspaceURI}/resources/css/workspace.css" type="text/css"/>
                
                <xsl:call-template name="workspace-head"/>
            </HEAD>
        
            <!-- ****** BODY ****** -->
            <BODY style="padding: 0px; margin: 0px; background-image: url('{$contextPath}{$workspaceURI}/resources/img/back.jpg')">
                <!-- loading ui library -->
                <xsl:call-template name="ui-load">
                    <xsl:with-param name="pluginsDirectContext" select="concat($contextPath, $workspaceURI, '/plugins')"/>
                    <xsl:with-param name="pluginsWrappedContext" select="concat($contextPath, $workspaceURI, '/_plugins')"/>
                </xsl:call-template>
                <xsl:call-template name="ui-tools-load">
                	<xsl:with-param name="bad-navigator-redirection"><xsl:value-of select="$contextPath"/>/_admin/public/navigator.html</xsl:with-param>
                    <!-- STools supported browser -->
                    <xsl:with-param name="accept-ie-6">true</xsl:with-param>
                    <xsl:with-param name="accept-ie-7">true</xsl:with-param>
                    <xsl:with-param name="accept-ff-1.0">true</xsl:with-param>
                    <xsl:with-param name="accept-ff-1.5">true</xsl:with-param>
                    <xsl:with-param name="accept-ff-2.0">true</xsl:with-param>
                </xsl:call-template>

                <table width="100%" height="100%">
                
                    <!-- TOOLBAR DECLARATION -->
                    <!-- tr>
                        <td height="30px">
                            <xsl:call-template name="ui-toolbar-place">
                                <xsl:with-param name="id">Runtime_Toolbar</xsl:with-param>
                            </xsl:call-template>
                        </td>
                    </tr-->
                    <tr>
                        <td>
                            <xsl:call-template name="workspace-body"/>
                        </td>
                    </tr>
                </table>
            </BODY>
                
            <!-- SCRIPT TOOLBAR with menu and buttons -->
            <!-- xsl:call-template name="ui-toolbar-begin">
                <xsl:with-param name="id">Runtime_Toolbar</xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="ui-toolbar-addmenubar">
                <xsl:with-param name="toolbarId">Runtime_Toolbar</xsl:with-param>
                <xsl:with-param name="menuData" select="/*/Menus"/>
            </xsl:call-template>
            <xsl:for-each select="/*/Toolbar/*">
                <xsl:call-template name="ui-toolbar-addinfo">
                    <xsl:with-param name="toolbarId">Runtime_Toolbar</xsl:with-param>
                    <xsl:with-param name="infoData" select="."/>
                </xsl:call-template>
            </xsl:for-each>
            <xsl:call-template name="ui-toolbar-end">
                <xsl:with-param name="id">Runtime_Toolbar</xsl:with-param>
            </xsl:call-template-->

            <!-- scripts -->
            <xsl:call-template name="workspace-script"/>
        </HTML>     
    </xsl:template>
    
    <xsl:template name="workspace-head"/>
    <xsl:template name="workspace-title"/>
    <xsl:template name="workspace-body"/>
    <xsl:template name="workspace-script"/>
        
</xsl:stylesheet>