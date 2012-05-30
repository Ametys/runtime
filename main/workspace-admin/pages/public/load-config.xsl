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
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
	
	<xsl:import href="kernel://pages/home/home.xsl"/>
    
    <xsl:param name="redirect"/>
    <xsl:param name="contextPath"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
	
    <xsl:template match="/">
    	<xsl:call-template name="home">
		    <xsl:with-param name="needs-kernel-ui" select="false()"/>
		    <xsl:with-param name="context-path" select="$contextPath"/>
		    <xsl:with-param name="workspace-name" select="$workspaceName"/>
		    <xsl:with-param name="workspace-prefix" select="$workspaceURI"/>
		    
		    <xsl:with-param name="head-title">
					<i18n:text i18n:catalogue="workspace.{$workspaceName}" i18n:key="WORKSPACE_ADMIN_LABEL_SHORT"/>
					<xsl:text> </xsl:text>
					<i18n:text i18n:catalogue="application" i18n:key="APPLICATION_PRODUCT_LABEL"/>
		    </xsl:with-param>
		    
		    <xsl:with-param name="head-meta">
		    	<link rel="stylesheet" href="{$contextPath}{$workspaceURI}/resources/css/load-config.css" type="text/css"/>
		    </xsl:with-param>

		    <xsl:with-param name="body-title">
		    	<img id="title-logo" alt="WORKSPACE_ADMIN_LABEL_LONG" i18n:attr="alt"/>
		    	<script type="text/javascript">
		    		document.getElementById('title-logo').src = "<xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/>/resources/img/Admin_<i18n:text i18n:key="KERNEL_LANGUAGE_CODE" i18n:catalogue="kernel"/>.png";
		    	</script>
			</xsl:with-param>
		    
		    <xsl:with-param name="body-col-main">
		    	<h1>
		    		<i18n:text i18n:key="WORKSPACE_ADMIN_CONFIG_TITLE"/>
		    	</h1>
		    	
		    	<p class="hint">
                	<i18n:text i18n:key="WORKSPACE_ADMIN_CONFIG_TEXT"/>
                </p>
                
                <div class="link">
                	<a href="{$contextPath}{$workspaceURI}/plugins/{$redirect}" title="WORKSPACE_ADMIN_CONFIG_DESCRIPTION" i18n:attr="title">
                		<i18n:text i18n:key="WORKSPACE_ADMIN_CONFIG_ACT"/>
                	</a>
                </div>
		    </xsl:with-param>
    	</xsl:call-template>
    </xsl:template>

</xsl:stylesheet>
