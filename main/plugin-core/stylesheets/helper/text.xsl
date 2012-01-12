<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2011 Anyware Services

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
    xmlns:func="http://exslt.org/functions"
    xmlns:text="http://ametys.org/text/3.0"
    extension-element-prefixes="func text">
    
    <func:function name="text:lowercase">
        <xsl:param name="str"/>
        <func:result select="translate($str, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
    </func:function>
    
    <func:function name="text:uppercase">
        <xsl:param name="str"/>
        <func:result select="translate($str, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
    </func:function>
    
    <func:function name="text:noaccent">
        <xsl:param name="str"/>
        <func:result select="translate($str, 'àáâãäåçèéêëìíîïñòóôõöøùúûüýÿÀÁÂÃÄÅÇÈÉÊËÌÍÎÏÑÒÓÔÕÖØÙÚÛÜÝŸ', 'aaaaaaceeeeiiiinoooooouuuuyyAAAAAACEEEEIIIINOOOOOOUUUUYY')"/>
    </func:function>
    
    <func:function name="text:cleanstring">
        <xsl:param name="str"/>
        <func:result select="text:lowercase(text:noaccent($str))"/>
    </func:function>
    
    <func:function name="text:ends-with">
        <xsl:param name="str"/>
        <xsl:param name="test"/>
        <func:result select="substring($str, string-length($str) - string-length($test) + 1) = $test"/>
    </func:function>
    
    <func:function name="text:isNotEmpty">
        <xsl:param name="string"/>
        <func:result select="$string and normalize-space($string) != ''"/>
    </func:function>
    
    <func:function name="text:substring-after-last">
        <xsl:param name="str"/>
        <xsl:param name="delim"/>
        <func:result>
            <xsl:choose>
                <xsl:when test="contains($str, $delim)">
                    <xsl:value-of select="text:substring-after-last(substring-after($str, $delim), $delim)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$str"/>
                </xsl:otherwise>
            </xsl:choose>
        </func:result>
    </func:function>
    
    <!-- Newline to br tag. -->
    <xsl:template name="text.nl2br">
        <xsl:param name="input"/>
        <xsl:param name="forjs" select="false()"/>
        
        <xsl:param name="s1"><xsl:text>
</xsl:text></xsl:param>
        <xsl:choose>
            <xsl:when test="contains($input, $s1)">
                <xsl:variable name="rest">
                    <xsl:call-template name="text.nl2br">
                        <xsl:with-param name="input" select="substring-after($input, $s1)"/>
                        <xsl:with-param name="s1" select="$s1"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:value-of select="substring-before($input, $s1)"/>
                <xsl:choose>
                	<xsl:when test="$forjs"><xsl:text>&lt;br/&gt;</xsl:text></xsl:when>
                	<xsl:otherwise><br/></xsl:otherwise>
                </xsl:choose>
                <xsl:copy-of select="$rest"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$input"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
</xsl:stylesheet>
