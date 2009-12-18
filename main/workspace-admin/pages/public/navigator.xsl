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
	
	<xsl:import href="../core/template.xsl"/>
    
   <xsl:param name="redirect"/>
    <xsl:param name="contextPath"/>
    <xsl:param name="workspaceURI"/>
    
	<xsl:template name="workspace-script">
		<link type="text/css" href="{$contextPath}{$workspaceURI}/resources/css/public.css" rel="stylesheet" />
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
    </xsl:template>
    
     <xsl:template name="workspace-body">
     	<div id="wrapper" class="" style="width: 930px; margin-right: auto; margin-left: auto;">
     		<div id="content_left" style="width: 44px;"><xsl:comment></xsl:comment></div>
     		
     		<div id="content_center" style="width: 930px;">
     			<div id="top" style="height: 90px; width: 930px;">
					<div id="logo"><xsl:comment></xsl:comment></div>
				</div>
				
				<div id="main">
					<div id="top-panel" class="top-panel"> 
						<div class="title">
			               <i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_TITLE"/>
			            </div>
                  
						<br/><br/>
                  
						<div>
			               <i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_TEXT"/>
			                
			                <br/><br/>
			                <br/><br/>
			                 
			                <a href="{$redirect}" onclick="javascript:forceNonSupportedNavigators();">
			                   	<i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_FORCE_LINK"/>
			                </a>
			                
			                 <br/><br/>
			               	 <br/><br/>
			            </div>
                  
						<br/><br/>
            
					</div>
				</div>
				
				<div id="footer" style="height: 35px; width: 930px;">
     			</div>
     		</div>
     	</div>
     	<div id="column-right" style="width: 100%;"/>
    </xsl:template>
    
    <xsl:template name="ui-tools-load"/>
    <xsl:template name="ui-load"/>
    
</xsl:stylesheet>
