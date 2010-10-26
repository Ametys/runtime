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
                xmlns:jsencoder="org.ametys.runtime.ui.JavascriptEncoder" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
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
               <script type="text/javascript" src="{$contextPath}/plugins/{$pluginName}/resources/js/org/ametys/administration/Config.i18n.js"/>
               
               <script type="text/javascript">
               
               		org.ametys.administration.Config.initialize ("<xsl:value-of select="$pluginName"/>");
               		
					org.ametys.administration.Config._navItems = [];
					
					<xsl:for-each select="config/categories/category">
						<xsl:sort select="label/node()" data-type="text" order="ascending"/>

						org.ametys.administration.Config._navItems.push({
							id: "<xsl:value-of select="generate-id()"/>",
							label: "<xsl:copy-of select="label/node()"/>"
						});
					</xsl:for-each>
					
					// Create the tool panel
					var panel = org.ametys.administration.Config.createPanel ();
					
					org.ametys.administration.Config._fields = [];
					
					<xsl:for-each select="config/categories/category">
						<xsl:sort select="label/node()" data-type="text" order="ascending"/>
						
						var fieldSet = org.ametys.administration.Config.createFieldSet ("<xsl:value-of select="generate-id()"/>", "<xsl:copy-of select="label/node()"/>");
							
						var height = 0; // padding-bottom
						
						
						<xsl:for-each select="groups/group">
							org.ametys.administration.Config.addGroupCategory (fieldSet, "<xsl:copy-of select="label/node()"/>");
							height += 24;
							
							<xsl:for-each select="parameters/*">
								<xsl:sort select="order" data-type="number"/>
									var input = org.ametys.administration.Config.addInputField (fieldSet, 
										"<xsl:value-of select="type"/>", 
										"<xsl:value-of select="local-name()"/>", 
										"<xsl:value-of select="jsencoder:encode(value)"/>", 
										"<xsl:copy-of select="label/node()"/>", 
										"<xsl:copy-of select="description/node()"/>",
										<xsl:call-template name="enumeration"/>,
										"<xsl:value-of select="widget"/>");
									height += org.ametys.administration.Config.getInputHeight (input);
							</xsl:for-each>
							
							// Set the height
							//fieldSet.setHeight(height);
								
							org.ametys.administration.Config._form.add(fieldSet);
						</xsl:for-each>
					</xsl:for-each>
						
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
    
    <xsl:template name="enumeration">
    	<xsl:choose>
    		<xsl:when test="enumeration">
    			<xsl:text>[</xsl:text> 
    			<xsl:for-each select="enumeration/option">
    				<xsl:if test="position() != 1">, </xsl:if>
    				<xsl:text>["</xsl:text><xsl:value-of select="jsencoder:encode(@value)"/><xsl:text>", "</xsl:text><xsl:copy-of select="node()"/><xsl:text>"]</xsl:text>
    			</xsl:for-each>
    			<xsl:text>]</xsl:text> 
    		</xsl:when>
    		
    		<xsl:otherwise>
    			<xsl:text>null</xsl:text>
    		</xsl:otherwise>
    	</xsl:choose>
    </xsl:template>
</xsl:stylesheet>