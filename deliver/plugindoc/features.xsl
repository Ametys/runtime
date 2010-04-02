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

   <xsl:param name="pluginName"/>
 
     <!-- +
         | Display the left summary of the features in the current xpath
         + -->
    <xsl:template name="features-left-summary">
        <xsl:param name="target"/>
        
        <xsl:for-each select="plugin:plugin">
            <xsl:sort select="@name"/>

            <h1>Features <a href="{@name}_main.html" target="{$target}"><xsl:value-of select="@name"/></a></h1>

			<xsl:choose>
				<xsl:when test="plugin:feature">
			        <xsl:for-each select="plugin:feature">
			            <xsl:sort select="@name"/>
			             
			            <a href="{../@name}_features.html#feature_{@name}" target="{$target}" style="display: block" title="More details on {@name}"><xsl:value-of select="../@name"/>/<xsl:value-of select="@name"/></a>
			    	</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<p>n/a</p>
				</xsl:otherwise>
			</xsl:choose>
			
			<br/>
        </xsl:for-each>
    </xsl:template>
    
    <!-- +
         | Display the summary of the features in the current xpath
         + -->
    <xsl:template name="features-summary">
            <xsl:if test="plugin:feature">
                <h2>Features summary</h2>
                <table>
                    <xsl:for-each select="plugin:feature">
                        <tr class="row_{position() mod 2}">
                            <td><a href="{$pluginName}_features.html#feature_{@name}" title="More details on {@name}"><xsl:value-of select="$pluginName"/>/<xsl:value-of select="@name"/></a></td>
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
         | Display the detail of the summary in the current xpath
         + -->    
    <xsl:template name="features-detail">
        <xsl:if test="plugin:feature">
            <h2>Features detail</h2>
            <xsl:for-each select="plugin:feature">
                <h4><a name="feature_{@name}"><xsl:value-of select="$pluginName"/>/<xsl:value-of select="@name"/></a></h4>
                    <div class="content">
                        <xsl:call-template name="comment"/>
                        
                        <xsl:call-template name="feature-depends"/>
                        
                        <xsl:call-template name="feature-config-link"/>
                        
                        <xsl:call-template name="feature-config"/>

                        <xsl:call-template name="feature-components"/>
                        
                        <xsl:call-template name="feature-extensions"/>
                    </div>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>         

    <!-- +
         | Display the dependant features of a feautre
         + -->
    <xsl:template name="feature-depends">
    	<xsl:if test="@depends">
            <p style="text-indent: -20px; padding-left: 20px;">
                <b>Depends on:</b><br/>
                <xsl:choose>
                    <xsl:when test="contains(@depends, '/')">
                        <a href="{substring-before(@depends, '/')}_features.html#{substring-after(@depends, '/')}"><xsl:value-of select="substring-after(@depends, '/')"/></a>
                    </xsl:when>
                    <xsl:otherwise>
                        <a href="{$pluginName}_features.html#feature_{@depends}"><xsl:value-of select="$pluginName"/>/<xsl:value-of select="@depends"/></a>
                    </xsl:otherwise>
                </xsl:choose>
           </p>
    	</xsl:if>
    </xsl:template>
    
    <!-- +
         | Display the list of the main (shared) configuration parameters used
         + -->
    <xsl:template name="feature-config-link">
        <xsl:if test="plugin:config/plugin:param-ref">
            <p style="text-indent: -20px; padding-left: 20px;">
                <b>Use shared configuration parameters:</b><br/>
                <xsl:for-each select="plugin:config/plugin:param-ref">
                    <a href="{$pluginName}_configuration.html#config_{@id}"><xsl:value-of select="@id"/></a>
                    <xsl:if test="position() != last()"><xsl:text>, </xsl:text></xsl:if>
                </xsl:for-each>
            </p>
        </xsl:if>
    </xsl:template>

    <!-- +
         | Display the configuration parameters specific to the current feature
         + -->
    <xsl:template name="feature-config">
        <xsl:if test="plugin:config/plugin:param">
            <p style="text-indent: -20px; padding-left: 20px;">
                <b>Specific configuration parameters detail:</b><br/>
            </p>
            <xsl:for-each select="plugin:config/plugin:param">
                <h5><a name="config_{@id}"><xsl:value-of select="@id"/></a></h5>
                <xsl:call-template name="configuration-detail">
                    <xsl:with-param name="known-uses" select="false()"/>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>
    
    <!-- +
         | Display the components of the current feature
         + -->
    <xsl:template name="feature-components">
        <xsl:if test="plugin:components/plugin:component">
            <p style="text-indent: -20px; padding-left: 20px;">
                <b>Components:</b><br/>
            </p>
            
            <xsl:variable name="feature" select="@name"/>
            
            <xsl:for-each select="plugin:components/plugin:component">
                <h5><a name="feature_{$feature}_component_{@role}"><xsl:value-of select="@role"/></a></h5>
                <div class="content">
                    <xsl:call-template name="comment"/>

                    <p>
                        <b>Configuration:</b>
                        <br/>
                        <code><xsl:call-template name="configuration"/></code>
                    </p>
                </div>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>
    
    <!-- +
         | Display the extensions of the current feature
         + -->
    <xsl:template name="feature-extensions">
        <xsl:if test="plugin:extensions/plugin:extension">
            <p style="text-indent: -20px; padding-left: 20px;">
                <b>Extensions:</b><br/>
            </p>
            
            <xsl:variable name="feature" select="@name"/>
            
            <xsl:for-each select="plugin:extensions/plugin:extension">
                <h5><a name="feature_{$feature}_extension_{@id}"><xsl:value-of select="@id"/></a></h5>
                <div class="content">
                    <xsl:call-template name="comment"/>

                    <p style="text-indent: -20px; padding-left: 20px;">
                        <b>Extension point extended:</b><br/>
                        <a>
                            <xsl:attribute name="href"><xsl:value-of select="$pluginName"/>_extensions.html#extension_point_<xsl:value-of select="@point"/></xsl:attribute>
                            <xsl:value-of select="@point"/>
                        </a>
                    </p>

                    <p>
                        <b>Configuration:</b>
                        <br/>
                        <code><xsl:call-template name="configuration"/></code>
                    </p>
                </div>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>