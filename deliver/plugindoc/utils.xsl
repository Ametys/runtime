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

    <xsl:import href="utils-comment.xsl"/>
    <xsl:import href="utils-link.xsl"/>

    <!-- +
         | Display the current path with color highlight
         + -->
    <xsl:template name="configuration">
       <div class="code">
            <span class="code_tag">
                <xsl:text>&lt;</xsl:text>
                <xsl:value-of select="name()"/>
            </span>
            <xsl:for-each select="@*">
                <xsl:text> </xsl:text><span class="code_attr"><xsl:value-of select="name()"/>=</span><span class="code_attr2">"<xsl:value-of select="."/><xsl:text>"</xsl:text></span>
            </xsl:for-each>
            
            <xsl:choose>
                <xsl:when test="* or string-length(text()) != 0">
                    <span class="code_tag"><xsl:text>&gt;</xsl:text></span>
                    
                    <xsl:choose>
                        <xsl:when test="*">
                                <xsl:for-each select="*">
                                    <xsl:call-template name="configuration"/>
                                </xsl:for-each>
                        </xsl:when>
                        <xsl:otherwise>
                            <span class="code_text"><xsl:value-of select="."/></span>
                        </xsl:otherwise>
                    </xsl:choose>
                    
                    <span class="code_tag">
                        <xsl:text>&lt;/</xsl:text>
                        <xsl:value-of select="name()"/><xsl:text>&gt;</xsl:text>
                    </span>
                </xsl:when>
                <xsl:otherwise>
                    <span class="code_tag">
                        <xsl:text>/&gt;</xsl:text>
                    </span>
                </xsl:otherwise>
            </xsl:choose>
        </div>
    </xsl:template>    

</xsl:stylesheet>