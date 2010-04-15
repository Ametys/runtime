<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2010 Anyware Services

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

    <xsl:import href="utils-comment.xsl"/>
    <xsl:import href="utils-link.xsl"/>

	<xsl:param name="contextPath"/>
	<xsl:param name="pluginName"/>
	<xsl:param name="workspaceName"/>
	<xsl:param name="workspaceURI"/>
    
    <xsl:variable name="resourcesPath"><xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources</xsl:variable>

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
                <xsl:when test="not(not(*)) or (string-length(text()) != 0)">
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