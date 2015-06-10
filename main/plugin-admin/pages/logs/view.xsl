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
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
    <xsl:param name="contextPath"/>
    <xsl:param name="pluginName"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
    
    <xsl:variable name="workspaceContext"><xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/></xsl:variable>
    
    <xsl:variable name="resourcesPath"><xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources</xsl:variable>
    
    <xsl:template match="/Logger">
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_ADMIN_LOGS_TITLE"/></title>                
                <link rel="stylesheet" href="{$resourcesPath}/css/administrator/logs.css" type="text/css"/>                  
            </head>
            
            <script>
            	 <script type="text/javascript" src="{$contextPath}/plugins/{$pluginName}/resources/js/Ametys/plugins/core/administration/Logs.js"></script>
            	 
            	<script type="text/javascript">
	                    
	                    Ametys.plugins.admin.Logs.initialize ("<xsl:value-of select="$pluginName"/>");
					
						function createCategory(categories, catName, level) 
						{
							var i = catName.indexOf("."); 
							if (i != -1)
							{
								var parentCat = catName.substring(0, i);
								var childCat = catName.substring(i + 1);
								
								if (categories[parentCat] == null)
								{
									categories[parentCat] = {child: {}, level: "inherit"};
								}
								
								createCategory(categories[parentCat].child, childCat, level);
							}
							else
							{
								categories[catName] = {child: {}, level: level};
							}
						}
						
						var logcategories = {child: {}, level: "<xsl:value-of select="LogLevels/logger[@category='root']/@priority"/>"};
						<xsl:for-each select="LogLevels/logger[@category != 'root']">
							<xsl:sort select="@category"/>
						<xsl:text></xsl:text>createCategory(logcategories.child, "<xsl:value-of select="@category"/>", "<xsl:value-of select="@priority"/>");
						</xsl:for-each>
												              
						var panel = Ametys.plugins.admin.Logs.createPanel ();
						
						var logs = [
							<xsl:for-each select="Logs/Log/file">
								['<xsl:value-of select="location" />',
								 '<xsl:value-of select="lastModified"/>',
								'<xsl:value-of select="size"/>',
                                 	'<xsl:choose>
                                    	<xsl:when test="../@name != location"><xsl:value-of select="../@name"/></xsl:when>
                                       <xsl:otherwise><i18n:text i18n:key="PLUGINS_ADMIN_LOGS_GROUP_OTHERS"/></xsl:otherwise>
                                   </xsl:choose>']
								<xsl:if test="not(position()=last())">
									<xsl:text>,</xsl:text>
								</xsl:if>								
							</xsl:for-each>						
						];	
						Ametys.plugins.admin.Logs.load(logs);
						
						createPanel = function () 
						{
							return panel;
						}
						            			
            	</script>
            </script>
            <body>
				
            </body>
        </html>
    </xsl:template>
        
</xsl:stylesheet>
