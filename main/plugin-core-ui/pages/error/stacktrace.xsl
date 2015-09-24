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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ametys="org.ametys.core.util.AmetysXSLTHelper"
                xmlns:stringutils="org.apache.commons.lang.StringUtils"                
                xmlns:ex="http://apache.org/cocoon/exception/1.0" 
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">

    <xsl:template name="stacktrace">
        <xsl:param name="exception"/>
        <xsl:param name="realPath"/>

        <xsl:variable name="backslashedRealpath" select="translate($realPath, '\', '/')" />
        
        <div class="stacktrace">
            <h1>
               <xsl:if test="@class"><xsl:value-of select="$exception/@class" /></xsl:if>
               <xsl:if test="string-length ($exception/ex:message) != 0">
                   <xsl:if test="@class">:</xsl:if><xsl:value-of select="$exception/ex:message" />
                   <xsl:if test="$exception/ex:location">
                       <br/>
                       <xsl:apply-templates select="$exception/ex:location">
                            <xsl:with-param name="backslashedRealpath" select="$backslashedRealpath"/>
                       </xsl:apply-templates>
                   </xsl:if>
               </xsl:if>
            </h1>
            <pre><code><xsl:call-template name="escape-location"><xsl:with-param name="backslashedRealpath" select="$backslashedRealpath"/><xsl:with-param name="location" select="$exception/ex:stacktrace"/></xsl:call-template></code></pre>
        </div>
    </xsl:template>
    
    <xsl:template match="ex:location">
        <xsl:param name="backslashedRealpath"/>
        
        <xsl:if test="string-length(.) > 0">
            <em>
                <xsl:value-of select="." />
            </em>
            <xsl:text> - </xsl:text>
        </xsl:if>
        <xsl:call-template name="escape-location"><xsl:with-param name="location" select="@uri"/><xsl:with-param name="backslashedRealpath" select="$backslashedRealpath"/></xsl:call-template>
    </xsl:template>
    
    <xsl:template name="escape-location">
        <xsl:param name="location"/>
        <xsl:param name="backslashedRealpath"/>

        <xsl:value-of select="stringutils:replace($location, concat('file:/', $backslashedRealpath), 'context:/')"/>
    </xsl:template>

</xsl:stylesheet>    
