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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:exslt="http://exslt.org/common" 
                              extension-element-prefixes="exslt">

    <!-- + 
         | Create a link to the javadoc of the class given in parameter.
         | Use the javadoc.xml to determine where are the javadoc of a given package
         + -->
    <xsl:template name="link-to-javadoc">
        <xsl:param name="content"/>
        
        <xsl:variable name="href" select="document('javadoc.xml')/*/javadoc[starts-with($content, concat(package, '.'))]/@url"/>
        
        <xsl:choose>
            <xsl:when test="$href != ''">
                        <a title="Javadoc" class="javadoc" href="{$href}/{translate($content, '.', '/')}.html">
                            <xsl:value-of select="$content"/>
                        </a>
            </xsl:when>
            <xsl:otherwise>
                        <span title="Unknown javadoc location" class="javadoc"><xsl:value-of select="$content"/></span>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- +
         | Create a link to the plugindoc of the link given in parameter (e.g. plugin{/PLUGINNAME/feature/FEATURENAME )
         + -->
    <xsl:template name="link-to-plugindoc">
        <xsl:param name="content"/>

        <xsl:choose>
            <xsl:when test="starts-with($content, '/')">
            
                <xsl:variable name="sub-content" select="substring-after($content, '/')"/>
                <xsl:choose>
                    <xsl:when test="contains($sub-content, '/')">
                        <xsl:call-template name="link-to-plugindoc-plugin">
                            <xsl:with-param name="plugin" select="substring-before($sub-content, '/')"/>
                            <xsl:with-param name="content" select="substring-after($sub-content, '/')"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="link-to-plugindoc-plugin">
                            <xsl:with-param name="plugin" select="$sub-content"/>
                            <xsl:with-param name="content" select="''"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
                
            </xsl:when>
            <xsl:otherwise>
            
                <xsl:call-template name="link-to-plugindoc-plugin">
                    <xsl:with-param name="content" select="$content"/>
                </xsl:call-template>

            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- Internale template for link-to-plugindoc -->
    <xsl:template name="link-to-plugindoc-plugin">
        <xsl:param name="plugin"/>
        <xsl:param name="content"/>
        
        <xsl:variable name="pluginRef"><xsl:value-of select="$plugin"/><xsl:if test="$plugin = ''"><xsl:value-of select="$currentPluginName"/></xsl:if>.html</xsl:variable>
        
        <xsl:choose>
            <xsl:when test="$content != ''">
            
                <xsl:variable name="type" select="substring-before($content, '/')"/>
                <xsl:variable name="sub-content" select="substring-after($content, '/')"/>
                
                <xsl:choose>
                    <xsl:when test="$type = 'config'">
                        <a title="Configuration parameter" href="{$pluginRef}_configuration.html#config_{$sub-content}"><xsl:value-of select="$sub-content"/></a>
                    </xsl:when>
                    <xsl:when test="$type = 'single-extension-point'">
                        <a title="Single extension point" href="{$pluginRef}_extensions.html#extension_point_{$sub-content}"><xsl:value-of select="$sub-content"/></a>
                    </xsl:when>
                    <xsl:when test="$type = 'extension-point'">
                        <a title="Multiple extension point" href="{$pluginRef}_extensions.html#extension_point_{$sub-content}"><xsl:value-of select="$sub-content"/></a>
                    </xsl:when>
                    <xsl:when test="$type = 'feature'">
                    
                        <xsl:call-template name="link-to-plugindoc-plugin-feature">
                            <xsl:with-param name="plugin" select="$plugin"/>
                            <xsl:with-param name="pluginRef" select="$pluginRef"/>
                            <xsl:with-param name="content" select="$sub-content"/>
                        </xsl:call-template>
                    
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="$plugin = ''"><xsl:value-of select="$currentPluginName"/></xsl:when>
                            <xsl:otherwise><xsl:value-of select="$plugin"/></xsl:otherwise>
                        </xsl:choose>
                        <xsl:text> </xsl:text><xsl:value-of select="$content"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <a title="Plugin" href="{$pluginRef}#top"><xsl:value-of select="$plugin"/><xsl:if test="$plugin = ''"><xsl:value-of select="$currentPluginName"/></xsl:if></a>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- Internal template for link-to-plugindoc-plugin -->
    <xsl:template name="link-to-plugindoc-plugin-feature">
        <xsl:param name="plugin"/>
        <xsl:param name="pluginRef"/>
        <xsl:param name="content"/>

        <xsl:choose>
            <xsl:when test="contains($content, '/')">
            
                <xsl:variable name="featureName" select="substring-before($content, '/')"/>
                <xsl:variable name="sub-content" select="substring-after($content, '/')"/>
                
                <xsl:variable name="type" select="substring-before($sub-content, '/')"/>
                <xsl:variable name="type-param" select="substring-after($sub-content, '/')"/>
                
                <xsl:choose>
                    <xsl:when test="$type = 'config'">
                        <a title="Configuration parameter of feature {$featureName}" href="{$pluginRef}_features.html#config_{$type-param}"><xsl:value-of select="$content"/></a>
                    </xsl:when>
                    <xsl:when test="$type = 'component'">
                        <a title="Component of feature {$featureName}" href="{$pluginRef}_features.html#feature_{$featureName}_component_{$type-param}"><xsl:value-of select="$type-param"/></a>
                    </xsl:when>
                    <xsl:when test="$type = 'extension'">
                        <a title="Extension of feature {$featureName}" href="{$pluginRef}_features.html#feature_{$featureName}_extension_{$type-param}"><xsl:value-of select="$type-param"/></a>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="$plugin = ''"><xsl:value-of select="$currentPluginName"/></xsl:when>
                            <xsl:otherwise><xsl:value-of select="$plugin"/></xsl:otherwise>
                        </xsl:choose>
                        <xsl:value-of select="$featureName"/>
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="$sub-content"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <a href="{$pluginRef}_features.html#feature_{$content}"><xsl:value-of select="$content"/></a>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>  
      
</xsl:stylesheet>