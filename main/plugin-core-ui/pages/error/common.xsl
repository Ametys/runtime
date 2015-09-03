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
                xmlns:ex="http://apache.org/cocoon/exception/1.0" 
                xmlns:escape="org.apache.commons.lang.StringEscapeUtils"
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
    
    <xsl:import href="plugin:core-ui://stylesheets/kernel.xsl"/>    
    
    <xsl:param name="realpath" />
    
    <xsl:variable name="contextPath" select="ametys:uriPrefix(false())"/>
    <xsl:variable name="workspaceURI" select="ametys:workspacePrefix()"/>
    <xsl:variable name="debug-mode" select="ametys:config('runtime.debug.ui')"/>
    
    <xsl:variable name="backslashedRealpath" select="translate($realpath, '\', '/')" />
    
    <xsl:template match="/">
        <html>
            <head>
                <title>Ametys - <xsl:call-template name="description"/></title>
                <meta http-equiv="X-UA-Compatible" content="IE=10" />
                <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"/>
                
                <link rel="stylesheet" type="text/css" href="{$contextPath}/plugins/core-ui/resources/css/error.css"/>
            </head>
            <body>
                <noscript><xsl:comment><xsl:copy-of select="/"/></xsl:comment></noscript>
                <noscript><i18n:text i18n:key="WORKSPACE_AMETYS_MAIN_ERROR_NOJS" i18n:catalogue="plugin.core-ui"/></noscript>
                
                <xsl:call-template name="kernel-base">
                    <xsl:with-param name="theme">triton</xsl:with-param>
                </xsl:call-template>
                
                <script type="text/javascript" src="{$contextPath}/plugins/core-ui/resources/js/Ametys/error/ErrorPage.js"/>
                
                <script>
                    Ext.onReady(function ()
                    {
                        Ext.application({
                            requires: ['Ext.container.Viewport'],
                            name: 'AmetysLogin',
                            
                            appFolder: 'ametys',
                            enableQuickTips: false,
                            launch: function() {
                                Ext.create('Ext.container.Viewport', {
                                    layout: 'fit',
                                    items: Ext.create('Ametys.error.ErrorPage', {
                                        text: "<xsl:call-template name="text"/>",
                                        description: "<xsl:call-template name="description"/>",
                                        message: "<xsl:call-template name="message"/>"
                                        details: "<xsl:call-template name="details"/>"
                                    })
                                });
                            }
                        })
                    });
                </script>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template name="ametys-scripts"/>
    
    <xsl:template name="text"/>
    <xsl:template name="description"/>
    
    <xsl:template name="message">
        <xsl:if test="@class"><xsl:value-of select="/ex:exception-report/@class" /></xsl:if>
        <xsl:if test="string-length (/ex:exception-report/ex:message) != 0">
            <xsl:if test="@class">:</xsl:if><xsl:value-of select="/ex:exception-report/ex:message" />
            <xsl:if test="/ex:exception-report/ex:location">
                <br />
                <span style="font-weight: normal">
                    <xsl:apply-templates select="/ex:exception-report/ex:location" />
                </span>
            </xsl:if>
        </xsl:if>
    </xsl:template>
    <xsl:template name="details"><xsl:value-of select="escape:escapeJavaScript(escape:escapeHtml(/ex:exception-report/ex:stacktrace))"/></xsl:template>
    
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