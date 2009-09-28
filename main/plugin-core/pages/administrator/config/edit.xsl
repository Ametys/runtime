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
    
    <xsl:import href="plugin:core://stylesheets/widgets.xsl"/>
    
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
               <script type="text/javascript" src="{$contextPath}/plugins/{$pluginName}/resources/js/org/ametys/administration/Config.i18n.js"><xsl:comment>empty</xsl:comment></script>
               
               <script type="text/javascript">
               
               		org.ametys.administration.Config.initialize ("<xsl:value-of select="$pluginName"/>");
               		
					org.ametys.administration.Config._navItems = [];
					
					<xsl:for-each select="config/categories/category">
						org.ametys.administration.Config._navItems.push({
							id: "<xsl:value-of select="generate-id()"/>",
							label: "<xsl:copy-of select="label/node()"/>"
						});
					</xsl:for-each>
					
					// Create the tool panel
					var panel = org.ametys.administration.Config.createPanel ();
					
					org.ametys.administration.Config._fields = [];
					
					<xsl:for-each select="config/categories/category">
						
						var fieldSet = org.ametys.administration.Config.createFieldSet ("<xsl:value-of select="generate-id()"/>", "<xsl:copy-of select="label/node()"/>");
							
						var height = 0; // padding-bottom
						
						
						<xsl:for-each select="groups/group">
						
							org.ametys.administration.Config.addGroupCategory (fieldSet, "<xsl:copy-of select="label/node()"/>");
							height += 24;
							
							<xsl:for-each select="parameters/*">
								<xsl:sort select="order"/>
									var input = org.ametys.administration.Config.addInputField (fieldSet, 
										"<xsl:value-of select="type"/>", 
										"<xsl:value-of select="local-name()"/>", 
										"<xsl:value-of select="value"/>", 
										"<xsl:copy-of select="label/node()"/>", 
										"<xsl:copy-of select="description/node()"/>");
									height += org.ametys.administration.Config.getInputHeight (input);
							</xsl:for-each>
							
							// Set the height
							fieldSet.setHeight(height);
								
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
</xsl:stylesheet>