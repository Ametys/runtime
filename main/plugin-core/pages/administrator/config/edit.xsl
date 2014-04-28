<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2014 Anyware Services

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
                xmlns:escape="org.apache.commons.lang.StringEscapeUtils"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                exclude-result-prefixes="escape">
    
    <xsl:param name="contextPath"/>
    <xsl:param name="pluginName"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
    
    <xsl:variable name="workspaceContext"><xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/></xsl:variable>
    
    <xsl:variable name="resourcesPath"><xsl:value-of select="$contextPath"/>/plugins/core/resources</xsl:variable>
    
    <xsl:template match="/">
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_TITLE"/></title>
                <link rel="stylesheet" href="{$resourcesPath}/css/administrator/config.css" type="text/css"/>
            </head>
            
            <script>
               <script type="text/javascript" src="{$contextPath}/plugins/{$pluginName}/resources/js/Ametys/plugins/core/administration/Config.i18n.js"/>
               
               <script type="text/javascript">
               
               		Ametys.plugins.core.administration.Config.initialize ("<xsl:value-of select="$pluginName"/>");
               		
					Ametys.plugins.core.administration.Config._navItems = [];
					
					<xsl:for-each select="config/categories/category">
						<xsl:sort select="label/node()" data-type="text" order="ascending"/>

						Ametys.plugins.core.administration.Config._navItems.push({
							id: "<xsl:value-of select="generate-id()"/>",
							label: "<xsl:copy-of select="label/node()"/>"
						});
					</xsl:for-each>
					
					// Create the tool panel
					var panel = Ametys.plugins.core.administration.Config.createPanel ();
					
					Ametys.plugins.core.administration.Config._fields = [];
					
					<xsl:for-each select="config/categories/category">
						<xsl:sort select="label/node()" data-type="text" order="ascending"/>
						
						var fieldSet = Ametys.plugins.core.administration.Config.createFieldSet ("<xsl:value-of select="generate-id()"/>", "<xsl:copy-of select="label/node()"/>");
							
						<xsl:for-each select="groups/group">
							Ametys.plugins.core.administration.Config.addGroupCategory (fieldSet, "<xsl:copy-of select="label/node()"/>"
							        <xsl:if test="group-switch">
							             <xsl:text>, </xsl:text>
							             <xsl:call-template name="create-config"><xsl:with-param name="config" select="parameters/*[local-name() = ../../group-switch]"/></xsl:call-template>
                                         <xsl:text>, </xsl:text>
                                         [ <xsl:for-each select="parameters/*[not(local-name() = ../../group-switch)]">
                                            <xsl:if test="position() != 1">, </xsl:if> "<xsl:value-of select="local-name()"/>"
                                           </xsl:for-each> ]
							         </xsl:if>
							);
							
                            <xsl:for-each select="parameters/*[not(local-name() = ../../group-switch)]">
                                    <xsl:variable name="switch-value" select="../*[local-name() = ../../group-switch]/value"/>
									Ametys.plugins.core.administration.Config.addInputField (fieldSet, <xsl:call-template name="create-config"><xsl:with-param name="config" select="."/></xsl:call-template>
									   <xsl:if test="$switch-value != ''">, "<xsl:value-of select="escape:escapeJavaScript($switch-value)"/>"</xsl:if>
									);
							</xsl:for-each>
							Ametys.plugins.core.administration.Config._form.add(fieldSet);
						</xsl:for-each>
					</xsl:for-each>
					function createPanel()
					{
						return panel;
					}
               </script>
			</script>
			<body>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template name="create-config">
        <xsl:param name="config" select="."/>
        {
            type: "<xsl:value-of select="$config/type"/>",
            name: "<xsl:value-of select="local-name($config)"/>",            
            <xsl:choose> 
	            <xsl:when test="$config/value">
	                value: "<xsl:value-of select="escape:escapeJavaScript($config/value)"/>",
	            </xsl:when>
	            <xsl:otherwise>
	               value: "<xsl:value-of select="escape:escapeJavaScript($config/defaultValue)"/>",
	            </xsl:otherwise>
            </xsl:choose>
            label: "<xsl:copy-of select="$config/label/node()"/>", 
            description: "<xsl:copy-of select="$config/description/node()"/>",
            enumeration: <xsl:call-template name="enumeration"><xsl:with-param name="config" select="$config"/></xsl:call-template>,
            widget: "<xsl:value-of select="$config/widget"/>",
            mandatory: "<xsl:value-of select="$config/validation/mandatory"/>",
            regexp: "<xsl:value-of select="escape:escapeJavaScript($config/validation/regexp)"/>"
            <xsl:if test="$config/validation/invalidText != ''">, invalidText: "<xsl:value-of select="$config/validation/invalidText"/>"</xsl:if>
        }
    </xsl:template>
    
    <xsl:template name="enumeration">
        <xsl:param name="config" select="."/>
        
    	<xsl:choose>
    		<xsl:when test="$config/enumeration">
    			<xsl:text>[</xsl:text> 
    			<xsl:for-each select="$config/enumeration/option">
    				<xsl:if test="position() != 1">, </xsl:if>
    				<xsl:text>["</xsl:text><xsl:value-of select="escape:escapeJavaScript(@value)"/><xsl:text>", "</xsl:text><xsl:copy-of select="node()"/><xsl:text>"]</xsl:text>
    			</xsl:for-each>
    			<xsl:text>]</xsl:text> 
    		</xsl:when>
    		
    		<xsl:otherwise>
    			<xsl:text>null</xsl:text>
    		</xsl:otherwise>
    	</xsl:choose>
    </xsl:template>
</xsl:stylesheet>