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
   
<!--
    This stylesheets is only used by the kernel for displaying an error occurring during startup.
    For runtime errors, you should use error.xsl instead, which is more powerful.
-->
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:ex="http://apache.org/cocoon/exception/1.0" 
                exclude-result-prefixes="ex">

    <xsl:param name="pageTitle">An error has occurred</xsl:param>
    <xsl:param name="contextPath" />
    <xsl:param name="realpath" />

    <xsl:variable name="backslashedRealpath" select="translate($realpath, '\', '/')" />

    <xsl:template match="/ex:exception-report">
        <html>
            <head> 
                <meta http-equiv="X-UA-Compatible" content="IE=10" />
                <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"/>
                
                <link rel="stylesheet" type="text/css" href="{$contextPath}/kernel/resources/css/fatal.css"/>
           </head>
           <body>
                <div class="ametys-fatal-page">
                
                    <div class="ametys-fatal-page-text">Too bad <span>:(</span></div>
                    
                    <div class="ametys-fatal-page-desc"><xsl:call-template name="message"/></div>
                    
                    <div class="ametys-fatal-page-details">
                        <pre>
                            <code>
                                <xsl:call-template name="details"/>
                            </code>
                        </pre>
                    </div>
                </div>
           </body>
        </html>
    </xsl:template>
    
    <xsl:template name="message">
        <xsl:if test="@class"><xsl:value-of select="@class" /></xsl:if>
        <xsl:if test="string-length (ex:message) != 0">
            <xsl:if test="@class">:</xsl:if><xsl:value-of select="ex:message" />
            <xsl:if test="ex:location">
                <br />
                <span style="font-weight: normal">
                    <xsl:apply-templates select="ex:location" />
                </span>
            </xsl:if>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="details"><xsl:value-of select="ex:stacktrace"/></xsl:template>
    
    <xsl:template match="ex:location">
        <xsl:if test="string-length(.) > 0">
            <em>
                <xsl:value-of select="." />
            </em>
            <xsl:text> - </xsl:text>
        </xsl:if>
        <xsl:call-template name="print-location" />
    </xsl:template>

    <xsl:template name="print-location">
        <xsl:choose>
            <xsl:when test="contains(@uri, $backslashedRealpath)">
                <xsl:text>context:/</xsl:text>
                <xsl:value-of select="substring-after(@uri, $backslashedRealpath)" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@uri" />
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text> - </xsl:text><xsl:value-of select="@line" /> : <xsl:value-of select="@column" />
    </xsl:template>
    
</xsl:stylesheet>
