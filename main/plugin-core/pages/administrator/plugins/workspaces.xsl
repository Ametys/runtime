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
    
    <xsl:template match="/">
        <workspaces>
	        {
		        children:
		        {
		            expanded: true,
		            icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/root.png",
		            text: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_WORKSPACE_VIEW"/>",
		            children: [
		                <xsl:choose>
		                    <xsl:when test="count(workspaces/workspace) = 0">
		                        {
		                            icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/workspace.png",
		                            text: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_NO_WORKSPACE"/>",
		                            leaf: true
		                        }
		                    </xsl:when>
		                    <xsl:otherwise>
		                        <xsl:for-each select="workspaces/workspace">
		                            <xsl:sort select="."/>
		                            
		                            <xsl:if test="position() != 1">,</xsl:if> 
		                            <xsl:call-template name="tree-workspace"/>
		                        </xsl:for-each>
		                    </xsl:otherwise>
		                </xsl:choose>
		            ]
		        }
	        }
        </workspaces>
    </xsl:template>
   
    <xsl:template name="tree-workspace">
        {
            icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/workspace<xsl:if test="@inactive = 'true'">-inactive</xsl:if>.png",
            text: "<xsl:value-of select="." /><xsl:if test="@inactive = 'true'"> &lt;span style='font-style: italic; color: #7f7f7f'&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_INACTIVE_{@cause}"/>&lt;/span&gt;</xsl:if><xsl:if test=". = ../@default"> &lt;span style='font-style: italic; color: #7f7f7f'&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_WORKSPACE_DEFAULT"/>&lt;/span&gt;</xsl:if>",
            leaf: true
        }
    </xsl:template>
    
</xsl:stylesheet>