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
	
	<xsl:import href="../core/template.xsl"/>
    
    <xsl:param name="contextPath"/>
    <xsl:param name="workspaceURI"/>
    <xsl:param name="redirect"/>
    
	
    <xsl:template name="workspace-title"></xsl:template>
    <xsl:template name="workspace-head"></xsl:template>
    <xsl:template name="workspace-script"></xsl:template>
    
    <xsl:template name="workspace-body">
		<!-- BODY -->
		<table style="width: 100%; height: 100%; background-color: #efebde;">
			<tr>
				<td style="padding: 30px"> 

					<div style="vertical-align: middle; text-align: center; FONT-SIZE: 14px; FONT-WEIGHT: BOLD; COLOR: orange;">
                        <img src="{$contextPath}{$workspaceURI}/resources/img/icon/warning.gif" style="margin-right: 5px; vertical-align: middle;"/>
                        <i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_TITLE"/>
                    </div>
                    
					<br/><br/>
                    
					<div style="text-align: center">
                        <i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_TEXT"/>
                    </div>
                    
					<br/><br/>
					
					<script type="text/javascript">
						<xsl:comment>
							function forceNonSupportedNavigators()
							{
								// Create cookie which will expires when the browser is closed down
								document.cookie = "ametys.accept.non.supported.navigators=on; path=<xsl:value-of select="$contextPath"/>/;";
								
								// Redirect
								return true;
							}
						</xsl:comment>
					</script>
                    
					<div style="text-align: center">
						<i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_FORCE_TEXT"/>
					</div>
					<div style="text-align: center; margin-top: 20px;">
						<style>
							a.runtime_link, a.runtime_link:visited, a.runtime_link:hover, a.runtime_link:linked, a.runtime_link:hover
							{
								font-weight: bold;
								color: #00005f;
							}
							a.runtime_link:hover
							{
								text-decoration: underline;
							}
						</style>
                        <a class="runtime_link" href="{$redirect}" onclick="javascript:forceNonSupportedNavigators();">
                            <img src="{$contextPath}{$workspaceURI}/resources/img/icon/warning.gif" border="0" style="margin-right: 5px; vertical-align: middle;"/>
                            <i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_FORCE_LINK"/>
                        </a>
                    </div>
                    
				</td>
			</tr>
		</table>
    </xsl:template>
    
    <xsl:template name="ui-tools-load"/>
    <xsl:template name="ui-load"/>
    
</xsl:stylesheet>
