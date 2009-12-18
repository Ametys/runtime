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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:exslt="http://exslt.org/common" 
                              extension-element-prefixes="exslt">

    <xsl:variable name="Constants_See">@see:</xsl:variable>
  
    <!-- +
         | Display fully the comment of the current xpath.
         + -->    
    <xsl:template name="comment">
        <xsl:call-template name="comment-content"/>

        <xsl:call-template name="comment-see"/>
        
        <xsl:call-template name="comment-class"/>
    </xsl:template>

    <!-- +
         | Display only the text content of the comment of the current xpath (or of the parameter).
         + -->    
    <xsl:template name="comment-content">
        <xsl:param name="content" select="comment()"/>
        
        <xsl:choose>
            <xsl:when test="contains($content, $Constants_See)">
                <xsl:value-of select="substring-before($content, $Constants_See)" disable-output-escaping="yes"/>
                <xsl:call-template name="comment-content">
                    <xsl:with-param name="content" select="substring-after(substring-after($content, $Constants_See), '}')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$content" disable-output-escaping="yes"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- +
         | Display only the java class of the comment of the current xpath (or of the parameter).
         + -->    
    <xsl:template name="comment-class">
        <xsl:param name="content" select="comment()"/>
        
        <xsl:if test="@class">
            <p style="text-indent: -20px; padding-left: 20px;">
                <b>Associated java class:</b><br/>
                <xsl:call-template name="link-to-javadoc">
                    <xsl:with-param name="content" select="@class"/>
                </xsl:call-template>
            </p>
        </xsl:if>
    </xsl:template>
    
    <!-- +
         | Display only the @see of the comment of the current xpath (or of the parameter).
         + -->    
    <xsl:template name="comment-see">
        <xsl:param name="content" select="comment()"/>

        <xsl:if test="contains($content, $Constants_See)">
            <p style="text-indent: -20px; padding-left: 20px;">
                <b>See also:</b><br/>
                <xsl:call-template name="comment-see-content">
                    <xsl:with-param name="content" select="$content"/>
                </xsl:call-template>
            </p>
        </xsl:if>
    </xsl:template>
    <!-- Internal template used by comment-see -->
    <xsl:template name="comment-see-content">
        <xsl:param name="content"/>
        
        <xsl:variable name="see" select="substring-before(substring-after($content, $Constants_See), '}')"/>
        
        <xsl:choose>
            <xsl:when test="starts-with($see, 'java{')">
                <!-- link to javadoc -->
                <xsl:call-template name="link-to-javadoc">
                    <xsl:with-param name="content" select="substring-after($see, 'java{')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="starts-with($see, 'plugin{')">
                <!-- link to plugindoc -->
                <xsl:call-template name="link-to-plugindoc">
                    <xsl:with-param name="content" select="substring-after($see, 'plugin{')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="substring-after($see, '{')"/>
            </xsl:otherwise>
        </xsl:choose>
        
        <!-- continue -->
        <xsl:variable name="after" select="substring-after(substring-after($content, $Constants_See), '}')"/>

        <xsl:if test="contains($after, $Constants_See)">
            <xsl:text>, </xsl:text>
            <xsl:call-template name="comment-see-content">
                <xsl:with-param name="content" select="$after"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>    
    
</xsl:stylesheet>