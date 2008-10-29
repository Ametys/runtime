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
				<link rel="stylesheet" href="{$workspaceContext}/resources/css/workspace.css" type="text/css"/>
				
                <xsl:call-template name="workspace-head"/>
			</head>
		
			<!-- ****** BODY ****** -->
			<body style="padding: 0px; margin: 0px;">
				<!-- loading ui library -->
                <xsl:call-template name="ui-load">
                    <xsl:with-param name="pluginsDirectContext" select="concat($workspaceContext, '/plugins')"/>
                    <xsl:with-param name="pluginsWrappedContext" select="concat($workspaceContext, '/_plugins')"/>
                </xsl:call-template>
                <xsl:call-template name="ui-tools-load">
                    <xsl:with-param name="bad-navigator-redirection"><xsl:value-of select="$workspaceContext"/>/public/navigator.html</xsl:with-param>
                    <xsl:with-param name="accept-ff-3.0">true</xsl:with-param>
                </xsl:call-template>

				<table class="admin_head" style="width: 100%; height: 100%; background-image: url({$contextPath}/kernel/resources/img/bg_top.gif); background-repeat: repeat-x; background-position: top; background-attachment: fixed;">
					<!-- En-tête -->
					<tr height="119px">
						<td>
							<img src="{$contextPath}/kernel/resources/img/runtime.jpg"/>
						</td>
                        <td class="admin_head">
                            <span class="admin_head"><i18n:text i18n:catalogue="workspace.{$workspaceName}" i18n:key="WORKSPACE_ADMIN_LABEL_LONG"/></span>
                        </td>
					</tr>
					<!-- Contenu -->
					<tr>
						<td colspan="2" class="admin_main_area">
						
							<table class="admin_main_border" style="width: 720px; height: 450px;">
								<tr>
									<th class="admin_main">
                                        <xsl:variable name="title"><xsl:call-template name="workspace-title"/></xsl:variable>
										<xsl:if test="xalan:nodeset($title)/node()">
											<xsl:copy-of select="$title"/>
										</xsl:if>
									</th>
								</tr>
								<tr>
									<td class="admin_main_border">
										
											<table class="admin_main" style="width: 720px; height: 450px;">
												<tr>
													<td>
															<!-- Contenu de la page d'administration -->							
															<xsl:call-template name="workspace-body"/>
													</td>
												</tr>
											</table>
									
									</td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
                
                <!-- scripts -->
                <xsl:call-template name="workspace-script"/>
			</body>
		</html>		
	</xsl:template>
    
    <xsl:template name="workspace-head"/>
    <xsl:template name="workspace-title"/>
    <xsl:template name="workspace-body"/>
    <xsl:template name="workspace-script"/>
	
</xsl:stylesheet>
	