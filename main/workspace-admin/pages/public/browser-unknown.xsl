<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2012 Anyware Services

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
                xmlns:parsebrowsers="org.ametys.runtime.workspaces.admin.browsers.ParseBrowsers">
	
	<xsl:import href="browser-unsupported.xsl"/>
    
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
		    	<link rel="stylesheet" href="{$contextPath}{$workspaceURI}/resources/css/public/browsers-issue.css" type="text/css"/>
		        <script type="text/javascript">
					function forceNonSupportedNavigators()
					{
						// Create cookie which will expires when the browser is closed down
						document.cookie = "ametys.accept.non.supported.navigators=on; path=<xsl:value-of select="$contextPath"/>/;max-age=2592000"; // 30 days
						
						// Redirect
						return true;
					}
				</script>
		    </xsl:with-param>

		    <xsl:with-param name="body-col-main">
		    	<h1>
		    		<i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_UNKNOWN_TITLE"/>
		    	</h1>
		    	
				<p class="hint">
					<i18n:translate>
	                	<i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_UNKNOWN_TEXT"/>
	                	<i18n:param><xsl:call-template name="browserName"/></i18n:param>
	                </i18n:translate>
                </p>

				<xsl:call-template name="showBrowsers"/>

		    	<p class="hint">
		    		<xsl:choose>
			    		<xsl:when test="$doc != ''">
		                	<a href="{$doc}" target="_blank">
	        	        		<i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_UNKNOWN_TEXT2"/>
	            	    	</a>
	            	    </xsl:when>
	            	    <xsl:otherwise>
							<i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_UNKNOWN_TEXT2"/>
	            	    </xsl:otherwise>
            	    </xsl:choose>
                </p>
		    	<p class="hint">
                	<i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_UNKNOWN_TEXT3"/>
                </p>
                
                <div class="link">
	                <a href="{$redirect}" class="button" onclick="return forceNonSupportedNavigators();">
	                   	<i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_FORCE_LINK"/>
	                </a>
			    </div>	
		    </xsl:with-param>
    	</xsl:call-template>
    </xsl:template>
</xsl:stylesheet>
