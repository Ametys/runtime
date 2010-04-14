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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:plugin="http://www.ametys.org/schema/plugin">

    <xsl:param name="contextPath"/>
    <xsl:param name="pluginName"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
    
    <xsl:variable name="workspaceContext"><xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/></xsl:variable>
    
    <xsl:variable name="resourcesPath"><xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources</xsl:variable>
    
	<xsl:template match="/root/list">
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
                       		icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/root.png",
				            text: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_PLUGINS"/>",
				            children: [
                                <xsl:choose>
                                    <xsl:when test="count(plugins/plugin) = 0">
                                    	{
                                    		icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/plugins.png",
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
                       		icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/root.png",
				            text: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_EXTENSION_POINTS"/>",
				            children: [
                                <xsl:choose>
                                    <xsl:when test="count(extension-points/extension-point) = 0">
                                    	{
                                    		icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/extension-point.png",
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
       		icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/plugins.png",
            text: "<xsl:value-of select="@name"/>",
            type: "plugin",
            pluginName : "<xsl:value-of select="@name"/>",
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
			<xsl:variable name="featureName" select="@name"/>
			<xsl:variable name="pluginName" select="../@name"/>
		
			icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/plugin<xsl:if test="@inactive = 'true'">-inactive</xsl:if>.png",
			text: "<xsl:value-of select="@name"/><xsl:if test="@inactive = 'true'"> &lt;span style='font-style: italic; color: #7f7f7f'&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_INACTIVE_{@cause}"/>&lt;/span&gt;</xsl:if>",
			type: "feature",
            pluginName : "<xsl:value-of select="$pluginName"/>",
            featureName : "<xsl:value-of select="$featureName"/>",
                                                            
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
            	<xsl:when test="/root/plugins/plugin:plugin[@name=$pluginName]/plugin:feature[@name=$featureName]/*/*">
            		leaf: false,
            		children: [
						<xsl:for-each select="/root/plugins/plugin:plugin[@name=$pluginName]/plugin:feature[@name=$featureName]/plugin:components/plugin:component">
							<xsl:sort select="@role" />
							
							<xsl:if test="position() != 1">,</xsl:if>
							<xsl:call-template name="tree-inactive-component"/>
						</xsl:for-each>
						<xsl:for-each select="/root/plugins/plugin:plugin[@name=$pluginName]/plugin:feature[@name=$featureName]/plugin:extensions/plugin:extension[not(@point = preceding-sibling::*/@point)]">
							<xsl:sort select="@point" />
							
							<xsl:if test="position() != 1 or count(/root/plugins/plugin:plugin[@name=$pluginName]/plugin:feature[@name=$featureName]/plugin:components/plugin:component) != 0">,</xsl:if>
							<xsl:call-template name="tree-inactive-extensionpoint"/>
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
			icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/composant.png",
			text: "<xsl:value-of select="." />",
			type: "component",
			pluginName: "<xsl:value-of select="../../@name"/>",
			featureName: "<xsl:value-of select="../@name"/>",
			componentName: "<xsl:value-of select="." />",
			leaf: true
    	}
    </xsl:template>
        
    <xsl:template name="tree-inactive-component">
		{
			icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/composant-inactive.png",
			text: "<xsl:value-of select="@role" />",
			type: "component",
			pluginName: "<xsl:value-of select="../../../@name"/>",
			featureName: "<xsl:value-of select="../../@name"/>",
			componentName: "<xsl:value-of select="@role" />",
			leaf: true
    	}
    </xsl:template>
    
    <xsl:template name="tree-extensionpoint">
    	<xsl:variable name="name" select="@name"/>
		{
			icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/extension-point<xsl:if test="/root/list/extension-points/extension-point[@id = $name]">-multiple</xsl:if>.png",
			text: "<xsl:value-of select="$name" />",
			type: "extension-point",
			pluginName: "<xsl:value-of select="../../@name"/>",
			extensionPointName: "<xsl:value-of select="$name" />",
			
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
    
    <xsl:template name="tree-inactive-extensionpoint">
    	<xsl:variable name="name" select="@point"/>
		{
			icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/extension-point<xsl:if test="/root/list/extension-points/extension-point[@id = $name]">-multiple</xsl:if>.png",
			text: "<xsl:value-of select="$name" />",
			type: "extension-point",
			pluginName: "<xsl:value-of select="../../../@name"/>",
			extensionPointName: "<xsl:value-of select="$name" />",
			
       		leaf: false,
       		children: [
				<xsl:for-each select="../plugin:extension[@point = $name]">
					<xsl:sort select="@id" />
					
					<xsl:if test="position() != 1">,</xsl:if>
					<xsl:call-template name="tree-inactive-extension"/>
				</xsl:for-each>
       		]
    	}
    </xsl:template>

    <xsl:template name="tree-extension">
		{
			icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/extension.png",
			text: "<xsl:value-of select="." />",
			type: "extension",
			pluginName: "<xsl:value-of select="../../../@name"/>",
			featureName: "<xsl:value-of select="../../@name"/>",
			extensionId: "<xsl:value-of select="." />",
			leaf: true
    	}
    </xsl:template>

    <xsl:template name="tree-inactive-extension">
		{
			icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/extension-inactive.png",
			text: "<xsl:value-of select="@id" />",
			type: "extension",
			pluginName: "<xsl:value-of select="../../../@name"/>",
			featureName: "<xsl:value-of select="../../@name"/>",
			extensionId: "<xsl:value-of select="@id" />",
			leaf: true
    	}
    </xsl:template>
    
    <xsl:template name="tree2-extensionpoint">
        <xsl:variable name="name" select="@id"/>
		{
			icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/extension-point<xsl:if test="local-name() != 'single-extension-point'">-multiple</xsl:if>.png",
			text: "<xsl:value-of select="@id" />",
			type: "extension-point",
			pluginName: "<xsl:value-of select="/root/list/plugins/plugin[feature/extensionPoint[@name = $name]]/@name"/>",
			extensionPointName: "<xsl:value-of select="@id" />",

            <xsl:choose>
            	<xsl:when test="count(/root/plugins/plugin:plugin/plugin:feature/plugin:extensions/plugin:extension[@point = $name]) != 0">
            		leaf: false,
            		children: [
<!--						<xsl:for-each select="/root/list/plugins/plugin/feature/extensionPoint[@name = $name]/extension">-->
<!--							<xsl:sort select="." />-->
<!--							-->
<!--							<xsl:if test="position() != 1">,</xsl:if>-->
<!--							<xsl:call-template name="tree2-extension"/>-->
<!--						</xsl:for-each>-->
						<xsl:for-each select="/root/plugins/plugin:plugin/plugin:feature/plugin:extensions/plugin:extension[@point = $name]">
							<xsl:sort select="@point" />
							
							<xsl:if test="position() != 1">,</xsl:if>
							<xsl:variable name="id" select="@id"/>
							<xsl:choose>
								<xsl:when test="/root/list/plugins/plugin/feature/extensionPoint[@name = $name]/extension[. = $id]">
									<xsl:for-each select="/root/list/plugins/plugin/feature/extensionPoint[@name = $name]/extension[. = $id]">
										<xsl:call-template name="tree2-extension"/>
									</xsl:for-each>
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template name="tree2-inactive-extension"/>
								</xsl:otherwise>
							</xsl:choose>
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
			icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/extension.png",
			text: "<xsl:value-of select="." />",
			leaf: false,
			type: "extension",
			pluginName: "<xsl:value-of select="../../../@name"/>",
			featureName: "<xsl:value-of select="../../@name"/>",
			extensionId: "<xsl:value-of select="." />",
			children: [
				{
					icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/plugin.png",
					text: "<xsl:value-of select="../../@name"/>",
					type: "feature",
					pluginName: "<xsl:value-of select="../../../@name"/>",
					featureName: "<xsl:value-of select="../../@name"/>",
					leaf: false,
					children: [
						{
							icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/plugins.png",
							text: "<xsl:value-of select="../../../@name"/>",
							type: "plugin",
							pluginName: "<xsl:value-of select="../../../@name"/>",
							leaf: true
						}
					]
				}
			]
    	}
    </xsl:template>
    

    <xsl:template name="tree2-inactive-extension">
		{
			icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/extension-inactive.png",
			text: "<xsl:value-of select="@id" />",
			leaf: false,
			type: "extension",
			pluginName: "<xsl:value-of select="../../../@name"/>",
			featureName: "<xsl:value-of select="../../@name"/>",
			extensionId: "<xsl:value-of select="@id" />",
			children: [
				{
					icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/plugin-inactive.png",
					text: "<xsl:value-of select="../../@name"/>",
					type: "feature",
					pluginName: "<xsl:value-of select="../../../@name"/>",
					featureName: "<xsl:value-of select="../../@name"/>",
					leaf: false,
					children: [
						{
							icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/plugins.png",
							text: "<xsl:value-of select="../../../@name"/>",
							type: "plugin",
							pluginName: "<xsl:value-of select="../../../@name"/>",
							leaf: true
						}
					]
				}
			]
    	}
    </xsl:template>
    
    <xsl:template name="tree3-workspace">
		{
			icon: "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/workspace<xsl:if test="@inactive = 'true'">-inactive</xsl:if>.png",
			text: "<xsl:value-of select="." /><xsl:if test="@inactive = 'true'"> &lt;span style='font-style: italic; color: #7f7f7f'&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_INACTIVE_{@cause}"/>&lt;/span&gt;</xsl:if>",
			leaf: true
    	}
    </xsl:template>
    
</xsl:stylesheet>