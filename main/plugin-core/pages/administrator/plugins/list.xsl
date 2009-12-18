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
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:param name="contextPath"/>
    <xsl:param name="pluginName"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
    
    <xsl:variable name="workspaceContext"><xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/></xsl:variable>
    
    <xsl:variable name="resourcesPath"><xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources</xsl:variable>
    
	<xsl:template match="/list">
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_TITLE"/></title>
            </head>
				<script>
					<script type="text/javascript" src="{$contextPath}/plugins/{$pluginName}/resources/js/org/ametys/administration/Plugins.i18n.js"><xsl:comment>empty</xsl:comment></script>
	            	<script type="text/javascript">
						org.ametys.administration.Plugins.initialize ("<xsl:value-of select="$pluginName"/>");
						
						var mainPanel = org.ametys.administration.Plugins.createPanel ();
							
						org.ametys.runtime.administrator.Panel.createPanel = function () 
						{
							return mainPanel;
						}						            			
	            	</script>
            </script>	
            
        </html>
    </xsl:template>
</xsl:stylesheet>