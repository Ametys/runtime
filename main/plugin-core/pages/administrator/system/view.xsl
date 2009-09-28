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
    
    <xsl:template match="/System">
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_TITLE"/></title>
                <link rel="stylesheet" href="{$resourcesPath}/css/administrator/system.css" type="text/css"/>                  
            </head>
            
	        <script>
	        	<script type="text/javascript" src="{$contextPath}/plugins/{$pluginName}/resources/js/org/ametys/administration/System.i18n.js"><xsl:comment>empty</xsl:comment></script>
	        	     
				<script type="text/javascript">
		                   
               		org.ametys.administration.System.initialize ("<xsl:value-of select="$pluginName"/>");
                   
                   	var mainPanel = org.ametys.administration.System.createPanel ();
                   
                   	<xsl:if test="announcements/@state != 'on'">
                   		org.ametys.administration.System._fieldSet.collapse();
                   	</xsl:if>
		                   
					<xsl:choose>
	                	<xsl:when test="/System/announcements">
	                    	<xsl:for-each select="/System/announcements/announcement">
	                        	org.ametys.administration.System._listView.addElement(null, 
	                                                    { lang : "<xsl:choose><xsl:when test="@lang"><xsl:value-of select="@lang"/></xsl:when><xsl:otherwise>*</xsl:otherwise></xsl:choose>",
	                                                      message : "<xsl:value-of select="."/>"});
							</xsl:for-each>
						</xsl:when>
	                    <xsl:otherwise>
	                    	org.ametys.administration.System._listView.addElement(null, { lang : "*", message : "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SYSTEM_DEFAULTMESSAGE"/>"});
						</xsl:otherwise>
					</xsl:choose>
                    
					org.ametys.runtime.administrator.Panel.createPanel = function () 
					{
						return mainPanel;
					}
				</script>
	        </script>
	            
	            	
            <body></body>
        </html>
    </xsl:template>    
    
</xsl:stylesheet>