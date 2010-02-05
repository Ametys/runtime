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
					<script type="text/javascript" src="{$resourcesPath}/js/org/ametys/administration/Plugins.i18n.js"></script>
	            	<script type="text/javascript">
						org.ametys.administration.Plugins.initialize ("<xsl:value-of select="$pluginName"/>");
						
						var pluginTreeNode = new Ext.tree.AsyncTreeNode({
				            expanded: true,
                       		icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/root.gif",
				            text: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_PLUGINS"/>",
				            children: [
                                <xsl:choose>
                                    <xsl:when test="count(plugins/plugin) = 0">
                                    	{
                                    		icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/plugins.gif",
                                    		text: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_NO_PLUGINS"/>",
                                    		leaf: true
                                    	}
                                    </xsl:when>
                                 	<xsl:otherwise>
							            <xsl:for-each select="plugins/plugin">
							             	<xsl:sort select="@name"/>
							             	
							             	<xsl:if test="position() != 1">,</xsl:if> 
							             	<xsl:call-template name="tree-plugin"/>
							            </xsl:for-each>
					            	</xsl:otherwise>
					            </xsl:choose>
				            ]
				        });

						var pluginTreeNode2 = new Ext.tree.AsyncTreeNode({
				            expanded: true,
                       		icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/root.gif",
				            text: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_EXTENSION_POINTS"/>",
				            children: [
                                <xsl:choose>
                                    <xsl:when test="count(extension-points/extension-point) = 0">
                                    	{
                                    		icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/extension-point.gif",
                                    		text: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_NO_EXTENSION"/>",
                                    		leaf: true
                                    	}
                                    </xsl:when>
                                 	<xsl:otherwise>
							            <xsl:for-each select="extension-points/*">
							             	<xsl:sort select="@id"/>
							             	
							             	<xsl:if test="position() != 1">,</xsl:if> 
							             	<xsl:call-template name="tree2-extensionpoint"/>
							            </xsl:for-each>
					            	</xsl:otherwise>
					            </xsl:choose>
				            ]
				        });
						
						var pluginTreeNode3 = new Ext.tree.AsyncTreeNode({
				            expanded: true,
                       		icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/root.gif",
				            text: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_WORKSPACE_VIEW"/>",
				            children: [
                                <xsl:choose>
                                    <xsl:when test="count(workspaces/workspace) = 0">
                                    	{
                                    		icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/workspace.gif",
                                    		text: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_NO_WORKSPACE"/>",
                                    		leaf: true
                                    	}
                                    </xsl:when>
                                 	<xsl:otherwise>
							            <xsl:for-each select="workspaces/workspace">
							             	<xsl:sort select="."/>
							             	
							             	<xsl:if test="position() != 1">,</xsl:if> 
							             	<xsl:call-template name="tree3-workspace"/>
							            </xsl:for-each>
					            	</xsl:otherwise>
					            </xsl:choose>
				            ]
				        });
						
						var mainPanel = org.ametys.administration.Plugins.createPanel (pluginTreeNode, pluginTreeNode2, pluginTreeNode3);
							
						org.ametys.runtime.administrator.Panel.createPanel = function () 
						{
							return mainPanel;
						}						            			
	            	</script>
            </script>	
            
        </html>
    </xsl:template>
    
    <xsl:template name="tree-plugin">
        {
       		icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/plugins.gif",
            text: "<xsl:value-of select='@name'/>",
            <xsl:choose>
            	<xsl:when test="count(feature) != 0">
            		leaf: false,
            		children: [
            			<xsl:for-each select="feature"> 
            				<xsl:sort select="@name"/>

							<xsl:if test="position() != 1">,</xsl:if>
							<xsl:call-template name="tree-feature"/>            				
            			</xsl:for-each>
            		]
            	</xsl:when>
            	<xsl:otherwise>
            		leaf: true
            	</xsl:otherwise>
            </xsl:choose>
        }
    </xsl:template>
    
    <xsl:template name="tree-feature">
		{
			icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/plugin<xsl:if test="@inactive = 'true'">-inactive</xsl:if>.gif",
			text: "<xsl:value-of select='@name'/><xsl:if test="@inactive = 'true'"> &lt;span style='font-style: italic; color: #7f7f7f'&gt;<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_PLUGINS_INACTIVE_{@cause}'/>&lt;/span&gt;</xsl:if>",
                                                            
            <xsl:choose>
            	<xsl:when test="count(component) != 0 or count(extensionPoint) != 0">
            		leaf: false,
            		children: [
						<xsl:for-each select="component">
							<xsl:sort select="." />
							
							<xsl:if test="position() != 1">,</xsl:if>
							<xsl:call-template name="tree-component"/>
						</xsl:for-each>
						<xsl:for-each select="extensionPoint">
							<xsl:sort select="@name" />
							
							<xsl:if test="position() != 1 or count(../component) != 0">,</xsl:if>
							<xsl:call-template name="tree-extensionpoint"/>
						</xsl:for-each>
            		]
            	</xsl:when>
            	<xsl:otherwise>
            		leaf: true
            	</xsl:otherwise>
            </xsl:choose>
		}
    
    </xsl:template>
    
    <xsl:template name="tree-component">
		{
			icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/composant.gif",
			text: "<xsl:value-of select='.' />",
			leaf: true
    	}
    </xsl:template>
    
    <xsl:template name="tree-extensionpoint">
    	<xsl:variable name="name" select="@name"/>
		{
			icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/extension-point<xsl:if test="/list/extension-points/extension-point[@id = $name]">-multiple</xsl:if>.gif",
			text: "<xsl:value-of select='@name' />",
            <xsl:choose>
            	<xsl:when test="count(extension) != 0">
            		leaf: false,
            		children: [
						<xsl:for-each select="extension">
							<xsl:sort select="." />
							
							<xsl:if test="position() != 1">,</xsl:if>
							<xsl:call-template name="tree-extension"/>
						</xsl:for-each>
            		]
            	</xsl:when>
            	<xsl:otherwise>
            		leaf: true
            	</xsl:otherwise>
            </xsl:choose>
    	}
    </xsl:template>

    <xsl:template name="tree-extension">
		{
			icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/extension.gif",
			text: "<xsl:value-of select='.' />",
			leaf: true
    	}
    </xsl:template>
    
    <xsl:template name="tree2-extensionpoint">
    	<xsl:variable name="name" select="@name"/>
		{
			icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/extension-point<xsl:if test="local-name() != 'single-extension-point'">-multiple</xsl:if>.gif",
			text: "<xsl:value-of select='@id' />",

            <xsl:variable name="name" select="@id"/>
            <xsl:choose>
            	<xsl:when test="count(/list/plugins/plugin/feature/extensionPoint[@name = $name]/extension) != 0">
            		leaf: false,
            		children: [
						<xsl:for-each select="/list/plugins/plugin/feature/extensionPoint[@name = $name]/extension">
							<xsl:sort select="." />
							
							<xsl:if test="position() != 1">,</xsl:if>
							<xsl:call-template name="tree2-extension"/>
						</xsl:for-each>
            		]
            	</xsl:when>
            	<xsl:otherwise>
            		leaf: true
            	</xsl:otherwise>
            </xsl:choose>
    	}
    </xsl:template>

    <xsl:template name="tree2-extension">
		{
			icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/extension.gif",
			text: "<xsl:value-of select='.' />",
			leaf: false,
			children: [
				{
					icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/plugin.gif",
					text: "<xsl:value-of select="../../@name"/>",
					leaf: false,
					children: [
						{
							icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/plugins.gif",
							text: "<xsl:value-of select="../../../@name"/>",
							leaf: true
						}
					]
				}
			]
    	}
    </xsl:template>
    
    <xsl:template name="tree3-workspace">
		{
			icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/workspace<xsl:if test="@inactive = 'true'">-inactive</xsl:if>.gif",
			text: "<xsl:value-of select='.' /><xsl:if test="@inactive = 'true'"> &lt;span style='font-style: italic; color: #7f7f7f'&gt;<i18n:text i18n:key='PLUGINS_CORE_ADMINISTRATOR_PLUGINS_INACTIVE_{@cause}'/>&lt;/span&gt;</xsl:if>",
			leaf: true
    	}
    </xsl:template>
    
</xsl:stylesheet>