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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 
    <xsl:import href="utils.xsl"/>
    <xsl:import href="configuration.xsl"/>
    <xsl:import href="extensions.xsl"/>
    <xsl:import href="features.xsl"/>

    <xsl:param name="pluginName">unknown</xsl:param>
 
    <xsl:template match="/plugin">
        <xsl:variable name="config" select="boolean(config/param|feature/config/param[*])"/>
        <xsl:variable name="extension" select="boolean(extension-points/single-extension-point|extension-points/extension-point)"/>
        <xsl:variable name="feature" select="boolean(feature)"/>

        <html>   
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <meta content="Ametys" name="generator"/>
                <title>Ametys Runtime 1.1 - <xsl:value-of select="$pluginName"/></title>
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
                    
                        <xsl:if test="config/param|feature/config/param[*]">
                            <frame src="{$pluginName}_main_configuration.html" name="Configuration"/>
                        </xsl:if>
                        <xsl:if test="extension-points/single-extension-point|extension-points/extension-point">
                            <frame src="{$pluginName}_main_extensions.html" name="Extensions"/>
                        </xsl:if>
                        <xsl:if test="feature">
                            <frame src="{$pluginName}_main_features.html" name="Features"/>
                        </xsl:if>
                    </frameset>
                </xsl:if>
                <frame src="{$pluginName}_main.html" name="Navigation"/>
            </frameset>
        </html>
    </xsl:template>
    
</xsl:stylesheet>