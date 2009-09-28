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
                <title><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_TITLE"/></title>                
                <link rel="stylesheet" href="{$resourcesPath}/css/administrator/logs.css" type="text/css"/>                  
            </head>
            
            <script>
            	 <script type="text/javascript" src="{$contextPath}/plugins/{$pluginName}/resources/js/org/ametys/administration/Logs.i18n.js"><xsl:comment>empty</xsl:comment></script>
            	 
            	<script type="text/javascript">
	                    
	                    org.ametys.administration.Logs.initialize ("<xsl:value-of select="$pluginName"/>");
					
						var panel = org.ametys.administration.Logs.createPanel ();
						
						var logs = [
							<xsl:for-each select="Logs/Log/file">
								['<xsl:value-of select="location" />',
								 '<xsl:value-of select="lastModified"/>',
								'<xsl:value-of select="size"/>',
                                 	'<xsl:choose>
                                    	<xsl:when test="../@name != location"><xsl:value-of select="../@name"/></xsl:when>
                                       <xsl:otherwise><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_LOGS_GROUP_OTHERS"/></xsl:otherwise>
                                   </xsl:choose>']
								<xsl:if test="not(position()=last())">
									<xsl:text>,</xsl:text>
								</xsl:if>								
							</xsl:for-each>						
						];	
						
						org.ametys.administration.Logs.load(logs);
						              
						org.ametys.runtime.administrator.Panel.createPanel = function () 
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
