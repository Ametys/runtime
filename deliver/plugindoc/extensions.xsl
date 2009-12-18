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

    <!-- +
         | Display the left summary of all extenesion points
         + -->
    <xsl:template name="extension-points-left-summary">
        <xsl:param name="target"/>
        
        <h1>        
        <a href="{$pluginName}_main.html" target="{$target}">Plugin <xsl:value-of select="$pluginName"/></a>
        <xsl:text> - </xsl:text>
        <a href="{$pluginName}_extensions.html" target="{$target}" title="See main configuration parameters">Extension points</a>
        </h1>

        <xsl:if test="extension-points/single-extension-point">
            <b>Single extensions points :</b><br/>
            <hr/>
    
            <xsl:for-each select="extension-points/single-extension-point">
                 <xsl:sort select="@id"/>
    
                <a href="{$pluginName}_extensions.html#extension_point_{@id}" style="display: block" target="{$target}" title="More details on {@id}"><xsl:value-of select="@id"/></a>
            </xsl:for-each>
            <br/>
        </xsl:if>
        
        <xsl:if test="extension-points/extension-point">    
            <b>Multiple extensions points :</b><br/>
            <hr/>
    
            <xsl:for-each select="extension-points/extension-point">
                 <xsl:sort select="@id"/>
    
                <a href="{$pluginName}_extensions.html#extension_point_{@id}" style="display: block" target="{$target}" title="More details on {@id}"><xsl:value-of select="@id"/></a>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>
    
    <!-- +
         | Display the summary of the extension points
         + -->
    <xsl:template name="extension-points-summary">
        <xsl:if test="extension-points/single-extension-point|extension-points/extension-point">
            <h2>Extension points summary</h2>
            
            <xsl:if test="extension-points/single-extension-point">
                <h3>Single extension point</h3>
                <table>
                    <xsl:for-each select="extension-points/single-extension-point">
                        <tr class="row_{position() mod 2}">
                            <td><a href="{$pluginName}_extensions.html#extension_point_{@id}" title="More details on {@id}"><xsl:value-of select="@id"/></a></td>
                            <td>
                                <div style="height: 20px; overflow: hidden">
                                    <xsl:call-template name="comment-content"/>
                                </div>
                            </td>
                        </tr>                                        
                    </xsl:for-each>
                </table>
            </xsl:if>
            
            <xsl:if test="extension-points/extension-point">
                <h3>Multiple extension point</h3>
                <table>
                    <xsl:for-each select="extension-points/extension-point">
                        <tr class="row_{position() mod 2}">
                            <td><a href="{$pluginName}_extensions.html#extension_point_{@id}" title="More details on {@id}"><xsl:value-of select="@id"/></a></td>
                            <td>
                                <div style="height: 20px; overflow: hidden">
                                    <xsl:call-template name="comment-content"/>
                                </div>
                            </td>
                        </tr>                                        
                    </xsl:for-each>
                </table>
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <!-- +
         | Display the detail of the extension points
         + -->
    <xsl:template name="extension-points-detail">    
        <xsl:if test="extension-points/single-extension-point|extension-points/extension-point">
            <h2>Extension points detail</h2>
            <xsl:if test="extension-points/single-extension-point">
                <h3>Single extension point</h3>
                <xsl:for-each select="extension-points/single-extension-point">
                    <h4><a name="extension_point_{@id}"><xsl:value-of select="@id"/></a></h4>
                    <div class="content">
                        <xsl:call-template name="comment"/>
                        <xsl:call-template name="extention-point-use"/>
                    </div>
                </xsl:for-each>
            </xsl:if>
            <xsl:if test="extension-points/extension-point">
                <h3>Multiple extension point</h3>
                <xsl:for-each select="extension-points/extension-point">
                    <h4><a name="extension_point_{@id}"><xsl:value-of select="@id"/></a></h4>
                    <div class="content">
                        <xsl:call-template name="comment"/>
                        <xsl:call-template name="extention-point-use"/>
                    </div>
                </xsl:for-each>
            </xsl:if>
        </xsl:if>
    </xsl:template>                
    
    <!-- +
         | Display in which feature an extension point is used 
         + -->
    <xsl:template name="extention-point-use">
        <xsl:variable name="point-id" select="current()/@id"/>
    
        <xsl:if test="/plugin/feature/extensions/extension[@point = $point-id]">
            <p style="text-indent: -20px; padding-left: 20px;">
                <b>Known implementations:</b><br/>
                
                <xsl:variable name="total" select="count(/plugin/feature/extensions/extension[@point = $point-id])"/>
                <xsl:for-each select="/plugin/feature/extensions[extension[@point = $point-id]]">
                    <xsl:variable name="last" select="position() = last()"/>
                    <xsl:for-each select="extension[@point = $point-id]">
                        <a href="{$pluginName}_features.html#feature_{../../@name}_extension_{@id}"><xsl:value-of select="@id"/></a>
                        <xsl:if test="position() != last() or not($last)"><xsl:text>, </xsl:text></xsl:if>
                    </xsl:for-each>
                </xsl:for-each>
            </p>
        </xsl:if>        
    </xsl:template>

</xsl:stylesheet>