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
         | Display the left summary of all extenesion points
         + -->
    <xsl:template name="extension-points-left-summary">
        <xsl:param name="target"/>
        
        <xsl:for-each select="plugin:plugin">
            <xsl:sort select="@name"/>

            <h1>Extensions points <a href="{@name}_main.html" target="{$target}"><xsl:value-of select="@name"/></a></h1>

			<xsl:choose>
				<xsl:when test="plugin:extension-points/plugin:single-extension-point|plugin:extension-points/plugin:extension-point">
			        <xsl:if test="plugin:extension-points/plugin:single-extension-point">
			            <b>Single extensions points :</b><br/>
			            <hr/>
			    
			            <xsl:for-each select="plugin:extension-points/plugin:single-extension-point">
			                 <xsl:sort select="@id"/>
			    
			                <a href="{../../@name}_extensions.html#extension_point_{@id}" style="display: block" target="{$target}" title="More details on {@id}"><xsl:value-of select="@id"/></a>
			            </xsl:for-each>
			            <br/>
			        </xsl:if>
			        
			        <xsl:if test="plugin:extension-points/plugin:extension-point">    
			            <b>Multiple extensions points :</b><br/>
			            <hr/>
			    
			            <xsl:for-each select="plugin:extension-points/plugin:extension-point">
			                 <xsl:sort select="@id"/>
			    
			                <a href="{../../@name}_extensions.html#extension_point_{@id}" style="display: block" target="{$target}" title="More details on {@id}"><xsl:value-of select="@id"/></a>
			            </xsl:for-each>
			        </xsl:if>
				</xsl:when>
				<xsl:otherwise>
					<p>n/a</p>
				</xsl:otherwise>
			</xsl:choose>
			<br/>
		</xsl:for-each>
    </xsl:template>
    
    <!-- +
         | Display the summary of the extension points
         + -->
    <xsl:template name="extension-points-summary">
        <xsl:if test="plugin:extension-points/plugin:single-extension-point|plugin:extension-points/plugin:extension-point">
            <h2>Extension points summary</h2>
            
            <xsl:if test="plugin:extension-points/plugin:single-extension-point">
                <h3>Single extension point</h3>
                <table>
                    <xsl:for-each select="plugin:extension-points/plugin:single-extension-point">
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
            
            <xsl:if test="plugin:extension-points/plugin:extension-point">
                <h3>Multiple extension point</h3>
                <table>
                    <xsl:for-each select="plugin:extension-points/plugin:extension-point">
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
        <xsl:if test="plugin:extension-points/plugin:single-extension-point|plugin:extension-points/plugin:extension-point">
            <h2>Extension points detail</h2>
            <xsl:if test="plugin:extension-points/plugin:single-extension-point">
                <h3>Single extension point</h3>
                <xsl:for-each select="plugin:extension-points/plugin:single-extension-point">
                    <h4><a name="extension_point_{@id}"><xsl:value-of select="@id"/></a></h4>
                    <div class="content">
                        <xsl:call-template name="comment"/>
                        <xsl:call-template name="extention-point-use"/>
                    </div>
                </xsl:for-each>
            </xsl:if>
            <xsl:if test="plugin:extension-points/plugin:extension-point">
                <h3>Multiple extension point</h3>
                <xsl:for-each select="plugin:extension-points/plugin:extension-point">
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
    
        <xsl:if test="/plugins/plugin:plugin/plugin:feature/plugin:extensions/plugin:extension[@point = $point-id]">
            <p style="text-indent: -20px; padding-left: 20px;">
                <b>Known implementations:</b><br/>
                
                <xsl:variable name="total" select="count(/plugins/plugin:plugin/plugin:feature/plugin:extensions/plugin:extension[@point = $point-id])"/>
                <xsl:for-each select="/plugins/plugin:plugin/plugin:feature/plugin:extensions[plugin:extension[@point = $point-id]]">
                    <xsl:variable name="last" select="position() = last()"/>
                    <xsl:for-each select="plugin:extension[@point = $point-id]">
                        <a href="{../../../@name}_features.html#feature_{../../@name}_extension_{@id}"><xsl:value-of select="@id"/></a>
                        <xsl:if test="position() != last() or not($last)"><xsl:text>, </xsl:text></xsl:if>
                    </xsl:for-each>
                </xsl:for-each>
            </p>
        </xsl:if>        
    </xsl:template>

</xsl:stylesheet>