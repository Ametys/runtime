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

    <!-- +
         | Display the left summary of all configuration parameters
         + -->
    <xsl:template name="configuration-left-summary">
        <xsl:param name="target"/>
        
        <h1>        
        <a href="{$pluginName}_main.html" target="{$target}">Plugin <xsl:value-of select="$pluginName"/></a>
        <xsl:text> - </xsl:text>
        <a href="{$pluginName}_configuration.html" target="{$target}" title="See main configuration parameters">Main configuration parameters</a>
        </h1>

        <xsl:for-each select="plugin:config/plugin:param">
             <xsl:sort select="@id"/>

            <a href="{$pluginName}_configuration.html#config_{@id}" target="{$target}" style="display: block" title="More details on {@id}"><xsl:value-of select="@id"/></a>
        </xsl:for-each>
    </xsl:template>

    <!-- +
         | Display the summary of all configuration parameters
         + -->
    <xsl:template name="configuration-summary">
        <xsl:if test="plugin:config/plugin:param|plugin:feature/plugin:config/plugin:param[*]">
            <h2>Configuration parameters summary</h2>
            <table>
                <xsl:for-each select="plugin:config/plugin:param|plugin:feature/plugin:config/plugin:param[*]">
                     <xsl:sort select="@id"/>

                    <tr class="row_{position() mod 2}">
                        <td>
                            <xsl:choose>
                                <xsl:when test="local-name(../..) = 'feature'">
                                    <a href="{$pluginName}_features.html#config_{@id}" title="More details on {@id}"><xsl:value-of select="@id"/></a>
                                </xsl:when>
                                <xsl:otherwise>
                                    <a href="{$pluginName}_configuration.html#config_{@id}" title="More details on {@id}"><xsl:value-of select="@id"/></a>
                                </xsl:otherwise>
                            </xsl:choose>
                        </td>
                        <td>
                            <div style="height: 20px; overflow: hidden">
                                <xsl:call-template name="comment-content"/>
                            </div>
                        </td>
                    </tr>
                </xsl:for-each>
            </table>
        </xsl:if>
    </xsl:template>
    
    <!-- +
         | Display the detail of the main configuration parameters
         + -->
    <xsl:template name="main-configuration-detail">
        <xsl:if test="plugin:config/plugin:param">
            <h2>Main configuration parameters detail</h2>
            <xsl:for-each select="plugin:config/plugin:param">
                <xsl:sort select="@id"/>
                
                <h4><a name="config_{@id}"><xsl:value-of select="@id"/></a></h4>
                <xsl:call-template name="configuration-detail"/>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <!-- +
         | Display the detail of one configuration parameter in the current xpath
         + -->
    <xsl:template name="configuration-detail">
	   <xsl:param name="known-uses" select="true()"/>

        <div class="content">
            <xsl:call-template name="comment"/>
            
            <xsl:if test="$known-uses and /plugin:plugin/plugin:feature[plugin:config/plugin:param-ref/@id = current()/@id]">
                <p style="text-indent: -20px; padding-left: 20px;">
                    <b>Konwn uses:</b>
                    <br/>
                    <xsl:for-each select="/plugin:plugin/plugin:feature[plugin:config/plugin:param-ref/@id = current()/@id]">
                        <a href="{$pluginName}_features.html#feature_{@name}"><xsl:value-of select="@name"/></a>
                        <xsl:if test="position() != last()"><xsl:text>, </xsl:text></xsl:if>
                    </xsl:for-each>
                </p>
            </xsl:if>

            <p>
                <b>Configuration:</b>
                <br/>
                <code><xsl:call-template name="configuration"/></code>
            </p>
            
            <xsl:if test="/plugin:plugin/plugin:config/plugin:param[plugin:Category=current()/plugin:Category and plugin:Group = current()/plugin:Group and @id != current()/@id]|/plugin:plugin/plugin:feature/plugin:config/plugin:param[plugin:Category=current()/plugin:Category and plugin:Group = current()/plugin:Group and @id != current()/@id]">
                <p style="text-indent: -20px; padding-left: 20px;">
                    <b>In the same group:</b>
                    <br/>
                    <xsl:for-each select="/plugin:plugin/plugin:config/plugin:param[plugin:Category=current()/plugin:Category and plugin:Group = current()/plugin:Group and @id != current()/@id]|/plugin:plugin/plugin:feature/plugin:config/plugin:param[plugin:Category=current()/plugin:Category and plugin:Group = current()/plugin:Group and @id != current()/@id]">
                        <xsl:choose>
                            <xsl:when test="local-name(../..) = 'feature'">
                                <a href="{$pluginName}_features.html#config_{@id}"><xsl:value-of select="@id"/></a>
                            </xsl:when>
                            <xsl:otherwise>
                                <a href="{$pluginName}_configuration.html#config_{@id}"><xsl:value-of select="@id"/></a>
                            </xsl:otherwise>
                        </xsl:choose>

                        <xsl:if test="position() != last()"><xsl:text>, </xsl:text></xsl:if>
                    </xsl:for-each>
                    <br/>
                </p>
            </xsl:if>

            <xsl:if test="/plugin:plugin/plugin:config/plugin:param[plugin:Category=current()/plugin:Category and plugin:Group != current()/plugin:Group and @id != current()/@id]|/plugin:plugin/plugin:feature/plugin:config/plugin:param[plugin:Category=current()/plugin:Category and plugin:Group != current()/plugin:Group and @id != current()/@id]">
                <p style="text-indent: -20px; padding-left: 20px;">
                    <b>In the same category:</b>
                    <br/>
                    <xsl:for-each select="/plugin:plugin/plugin:config/plugin:param[plugin:Category=current()/plugin:Category and plugin:Group != current()/plugin:Group and @id != current()/@id]|/plugin:plugin/plugin:feature/plugin:config/plugin:param[plugin:Category=current()/plugin:Category and Group != current()/plugin:Group and @id != current()/@id]">
                        <xsl:choose>
                            <xsl:when test="local-name(../..) = 'feature'">
                                <a href="{$pluginName}_features.html#config_{@id}"><xsl:value-of select="@id"/></a>
                            </xsl:when>
                            <xsl:otherwise>
                                <a href="{$pluginName}_configuration.html#config_{@id}"><xsl:value-of select="@id"/></a>
                            </xsl:otherwise>
                        </xsl:choose>

                        <xsl:if test="position() != last()"><xsl:text>, </xsl:text></xsl:if>
                    </xsl:for-each>
                    <br/>
                </p>
            </xsl:if>
        </div>
    </xsl:template>
    
</xsl:stylesheet>