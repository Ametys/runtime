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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:plugin="http://www.ametys.org/schema/plugin">

    <xsl:param name="contextPath"/>
    <xsl:param name="pluginName"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
    
    <xsl:variable name="workspaceContext"><xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/></xsl:variable>
    
    <xsl:variable name="resourcesPath"><xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources</xsl:variable>
    
    <xsl:template match="/">
        <plugins-by-extension-point>
            {
                children:
                {
                   expanded: true,
                   icon: "<xsl:value-of select="$resourcesPath"/>/img/plugins/root.png",
                   text: "<i18n:text i18n:key="PLUGINS_ADMIN_PLUGINS_EXTENSION_POINTS"/>",
                   children: [
                       <xsl:choose>
                           <xsl:when test="count(root/list/extension-points/*) = 0">
                               {
                                   icon: "<xsl:value-of select="$resourcesPath"/>/img/plugins/extension-point.png",
                                   text: "<i18n:text i18n:key="PLUGINS_ADMIN_PLUGINS_NO_EXTENSION"/>",
                                   leaf: true
                               }
                           </xsl:when>
                           <xsl:otherwise>
                               {
                                   icon: "<xsl:value-of select="$resourcesPath"/>/img/plugins/composant.png",
                                   text: "<i18n:text i18n:key="PLUGINS_ADMIN_PLUGINS_CHANGES_CMP"/>",
                                   leaf: <xsl:value-of select="count(root/list/components/component)"/> == 0,
                                   children: [
                                       <xsl:for-each select="root/list/components/component">
                                           <xsl:sort select="@id"/>
                                           
                                           <xsl:if test="position() != 1">,</xsl:if> 
                                           <xsl:call-template name="tree-child"/>
                                       </xsl:for-each>
                                   ]
                               }
                               ,
                               {
                                   icon: "<xsl:value-of select="$resourcesPath"/>/img/plugins/extension-point-multiple.png",
                                   text: "<i18n:text i18n:key="PLUGINS_ADMIN_PLUGINS_CHANGES_EP"/>",
                                   leaf: <xsl:value-of select="count(root/list/extension-points/extension-point)"/> == 0,
                                   children: [
                                       <xsl:for-each select="root/list/extension-points/extension-point">
                                           <xsl:sort select="@id"/>
                                           
                                           <xsl:if test="position() != 1">,</xsl:if> 
                                           <xsl:call-template name="tree-child"/>
                                       </xsl:for-each>
                                   ]
                               }
                           </xsl:otherwise>
                       </xsl:choose>
                   ]
               }
           }
        </plugins-by-extension-point>
    </xsl:template>
    
    <xsl:template name="tree-child">
        <xsl:variable name="name" select="@id"/>
        {
            icon: "<xsl:value-of select="$resourcesPath"/>/img/plugins/<xsl:choose><xsl:when test="local-name() = 'component'">composant</xsl:when><xsl:otherwise>extension-point-multiple</xsl:otherwise></xsl:choose>.png",
            text: "<xsl:value-of select="@id" />",
            isMultiple: "<xsl:value-of select="local-name() != 'component'"/>",
            type: <xsl:choose><xsl:when test="local-name() = 'component'">"component-role"</xsl:when><xsl:otherwise>"extension-point"</xsl:otherwise></xsl:choose>,
            pluginName: "<xsl:value-of select="/root/list/plugins/plugin[feature/extensionPoint[@name = $name] or feature/component[@role = $name]]/@name"/>",
            name: "<xsl:value-of select="@id" />",

            <xsl:choose>
                <xsl:when test="count(/root/plugins/plugin:plugin/plugin:feature/plugin:extensions/plugin:extension[@point = $name]) != 0">
                    leaf: false,
                    children: [
                        <xsl:for-each select="/root/plugins/plugin:plugin/plugin:feature/plugin:extensions/plugin:extension[@point = $name]">
                            <xsl:sort select="@point" />
                            
                            <xsl:if test="position() != 1">,</xsl:if>
                            <xsl:variable name="id" select="@id"/>
                            <xsl:choose>
                                <xsl:when test="/root/list/plugins/plugin/feature/extensionPoint[@name = $name]/extension[. = $id]">
                                    <xsl:for-each select="/root/list/plugins/plugin/feature/extensionPoint[@name = $name]/extension[. = $id]">
                                        <xsl:call-template name="tree-extension"/>
                                    </xsl:for-each>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:call-template name="tree-inactive-extension"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each>
                    ]
                </xsl:when>
                <xsl:when test="count(/root/plugins/plugin:plugin/plugin:feature/plugin:components/plugin:component[@role = $name]) != 0">
                    leaf: false,
                    children: [
                        <xsl:for-each select="/root/plugins/plugin:plugin/plugin:feature/plugin:components/plugin:component[@role = $name]">
                            <xsl:sort select="@role" />
                            
                            <xsl:if test="position() != 1">,</xsl:if>
                            <xsl:variable name="id" select="@id"/>
                            <xsl:variable name="pluginName" select="../../../@name"/>
                            <xsl:variable name="featureName" select="../../@name"/>
                            <xsl:choose>
                                <xsl:when test="/root/list/plugins/plugin[@name = $pluginName]/feature[@name = $featureName]/component[. = $id]">
                                    <xsl:for-each select="/root/list/plugins/plugin[@name = $pluginName]/feature[@name = $featureName]/component[. = $id]">
                                        <xsl:call-template name="tree-component"/>
                                    </xsl:for-each>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:call-template name="tree-inactive-component"/>
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

    <xsl:template name="tree-extension">
        <xsl:if test="position() != 1">,</xsl:if>
    
        {
            icon: "<xsl:value-of select="$resourcesPath"/>/img/plugins/extension.png",
            text: "<xsl:value-of select="." />",
            leaf: false,
            type: "extension",
            pluginName: "<xsl:value-of select="../../../@name"/>",
            featureName: "<xsl:value-of select="../../@name"/>",
            extensionId: "<xsl:value-of select="." />",
            children: [
                {
                    icon: "<xsl:value-of select="$resourcesPath"/>/img/plugins/plugin.png",
                    text: "<xsl:value-of select="../../@name"/>",
                    type: "feature",
                    active: true,
                    pluginName: "<xsl:value-of select="../../../@name"/>",
                    featureName: "<xsl:value-of select="../../@name"/>",
                    leaf: false,
                    children: [
                        {
                            icon: "<xsl:value-of select="$resourcesPath"/>/img/plugins/plugins.png",
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

    <xsl:template name="tree-inactive-extension">
        <xsl:variable name="pluginName" select="../../../@name"/>
        <xsl:variable name="featureName" select="../../@name"/>
        <xsl:variable name="cause" select="/root/list/plugins/plugin[@name = $pluginName]/feature[@name = $featureName]/@cause"/>
        {
            icon: "<xsl:value-of select="$resourcesPath"/>/img/plugins/extension-inactive.png",
            text: "<xsl:value-of select="@id" />",
            leaf: false,
            type: "extension",
            pluginName: "<xsl:value-of select="$pluginName"/>",
            featureName: "<xsl:value-of select="$featureName"/>",
            extensionId: "<xsl:value-of select="@id" />",
            children: [
                {
                    icon: "<xsl:value-of select="$resourcesPath"/>/img/plugins/plugin-inactive.png",
                    text: "<xsl:value-of select="$featureName"/> &lt;span style='font-style: italic; color: #7f7f7f'&gt;<i18n:text i18n:key="PLUGINS_ADMIN_PLUGINS_INACTIVE_{$cause}"/>&lt;/span&gt;",
                    type: "feature",
                    active: false,
                    cause: "<xsl:value-of select="$cause"/>",
                    pluginName: "<xsl:value-of select="$pluginName"/>",
                    featureName: "<xsl:value-of select="$featureName"/>",
                    leaf: false,
                    children: [
                        {
                            icon: "<xsl:value-of select="$resourcesPath"/>/img/plugins/plugins.png",
                            text: "<xsl:value-of select="$pluginName"/>",
                            type: "plugin",
                            pluginName: "<xsl:value-of select="$pluginName"/>",
                            leaf: true
                        }
                    ]
                }
            ]
        }
    </xsl:template>
    
    <xsl:template name="tree-component">
        <xsl:if test="position() != 1">,</xsl:if>
    
        {
            icon: "<xsl:value-of select="$resourcesPath"/>/img/plugins/composant.png",
            text: "<xsl:value-of select="." />",
            leaf: true,
            type: "component",
            pluginName: "<xsl:value-of select="../../@name"/>",
            featureName: "<xsl:value-of select="../@name"/>",
            componentId: "<xsl:value-of select="." />"
        }
    </xsl:template>

    <xsl:template name="tree-inactive-component">
        <xsl:variable name="pluginName" select="../../../@name"/>
        <xsl:variable name="featureName" select="../../@name"/>
        <xsl:variable name="cause" select="/root/list/plugins/plugin[@name = $pluginName]/feature[@name = $featureName]/@cause"/>
        {
            icon: "<xsl:value-of select="$resourcesPath"/>/img/plugins/composant-inactive.png",
            text: "<xsl:value-of select="@id" /> &lt;span style='font-style: italic; color: #7f7f7f'&gt;<i18n:text i18n:key="PLUGINS_ADMIN_PLUGINS_INACTIVE_{$cause}"/>&lt;/span&gt;",
            leaf: true,
            type: "component",
            pluginName: "<xsl:value-of select="$pluginName"/>",
            featureName: "<xsl:value-of select="$featureName"/>",
            componentId: "<xsl:value-of select="@id" />",
            active: false,
            cause: "<xsl:value-of select="$cause"/>"
        }
    </xsl:template>
</xsl:stylesheet>