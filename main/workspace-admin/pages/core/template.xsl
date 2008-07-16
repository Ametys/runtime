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
    xmlns:xalan="http://xml.apache.org/xalan">
	
    <xsl:import href="resource://org/ametys/runtime/kernel/stylesheets/ui.xsl"/>
    
    <xsl:param name="contextPath"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
    
    <xsl:variable name="workspaceContext"><xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/></xsl:variable>

	<xsl:template match="/">
		<HTML>
			<!-- ****** HEAD ****** -->
			<HEAD> 
				<TITLE>
					<i18n:text i18n:catalogue="workspace.{$workspaceName}" i18n:key="WORKSPACE_ADMIN_LABEL_SHORT"/>
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
			<BODY style="padding: 0px; margin: 0px;">
				<!-- loading ui library -->
                <xsl:call-template name="ui-load">
                    <xsl:with-param name="pluginsDirectContext" select="concat($contextPath, $workspaceURI, '/plugins')"/>
                    <xsl:with-param name="pluginsWrappedContext" select="concat($contextPath, $workspaceURI, '/_plugins')"/>
                </xsl:call-template>
                <xsl:call-template name="ui-tools-load">
                    <xsl:with-param name="bad-navigator-redirection"><xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/>/public/navigator.html</xsl:with-param>
                </xsl:call-template>

				<TABLE class="admin_head" style="width: 100%; height: 100%; background-image: url({$contextPath}/kernel/resources/img/bg_top.gif); background-repeat: repeat-x; background-position: top; background-attachment: fixed;">
					<!-- En-tête -->
					<TR height="119px">
						<TD>
							<IMG src="{$contextPath}/kernel/resources/img/runtime.jpg"/>
						</TD>
                        <TD class="admin_head">
                            <span class="admin_head"><i18n:text i18n:catalogue="workspace.{$workspaceName}" i18n:key="WORKSPACE_ADMIN_LABEL_LONG"/></span>
                        </TD>
					</TR>
					<!-- Contenu -->
					<TR>
						<TD colspan="2" class="admin_main_area">
						
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
						</TD>
					</TR>
				</TABLE>
                
                <!-- scripts -->
                <xsl:call-template name="workspace-script"/>
			</BODY>
		</HTML>		
	</xsl:template>
    
    <xsl:template name="workspace-head"/>
    <xsl:template name="workspace-title"/>
    <xsl:template name="workspace-body"/>
    <xsl:template name="workspace-script"/>
	
</xsl:stylesheet>
	