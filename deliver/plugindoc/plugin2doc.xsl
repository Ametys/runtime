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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:plugin="http://www.ametys.org/schema/plugin">
 
    <xsl:import href="utils.xsl"/>
    <xsl:import href="configuration.xsl"/>
    <xsl:import href="extensions.xsl"/>
    <xsl:import href="features.xsl"/>

    <xsl:param name="pluginName"/>
 
    <xsl:template match="/plugins">
        <xsl:variable name="config" select="boolean(plugin:plugin/plugin:config/plugin:param|plugin:plugin/plugin:feature/plugin:config/plugin:param[*])"/>
        <xsl:variable name="extension" select="boolean(plugin:plugin/plugin:extension-points/plugin:single-extension-point|plugin:plugin/plugin:extension-points/plugin:extension-point)"/>
        <xsl:variable name="feature" select="boolean(plugin:plugin/plugin:feature)"/>

        <html>   
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <meta content="Ametys" name="generator"/>
                <title>Ametys Plugin Doc</title>
            </head>
            <frameset cols="350px,*">
                <xsl:attribute name="col"><xsl:if test="$config or $extension or $feature">350px,</xsl:if>*</xsl:attribute>
            
                
                <xsl:if test="$config or $extension or $feature">
                    <frameset>
                        <xsl:attribute name="rows">
                            <xsl:choose>
                                <xsl:when test="($config and not($extension) and not($feature)) or (not($config) and $extension and not($feature)) or (not($config) and not($extension) and $feature)"><xsl:text>100%</xsl:text></xsl:when>
                                <xsl:when test="$config and $extension and $feature">33%,33%,34%</xsl:when>
                                <xsl:otherwise><xsl:text>50%,50%</xsl:text></xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                    
                        <xsl:if test="plugin:plugin/plugin:config/plugin:param|plugin:plugin/plugin:feature/plugin:config/plugin:param[*]">
                            <frame src="main_configuration.html" name="Configuration"/>
                        </xsl:if>
                        <xsl:if test="plugin:plugin/plugin:extension-points/plugin:single-extension-point|plugin:plugin/plugin:extension-points/plugin:extension-point">
                            <frame src="main_extensions.html" name="Extensions"/>
                        </xsl:if>
                        <xsl:if test="plugin:plugin/plugin:feature">
                            <frame src="main_features.html" name="Features"/>
                        </xsl:if>
                    </frameset>
                </xsl:if>
                <frame src="main.html" name="Navigation"/>
            </frameset>
        </html>
    </xsl:template>
    
</xsl:stylesheet>