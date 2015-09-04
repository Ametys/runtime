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
                xmlns:escape="org.apache.commons.lang.StringEscapeUtils"
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
    
    <xsl:param name="doc"/>
    <xsl:param name="browser"/><!-- current browser -->
    <xsl:param name="browserversion"/><!-- current version -->
    <xsl:param name="supported"/><!-- the json list of supported browsers -->
    
    <xsl:variable name="contextPath" select="ametys:uriPrefix(false())"/>
    <xsl:variable name="workspaceURI" select="ametys:workspacePrefix()"/>
    <xsl:variable name="debug-mode" select="ametys:config('runtime.debug.ui')"/>
    
    <xsl:template match="/">
        <html>
            <head>
                <title>Ametys - <i18n:text i18n:key='PLUGINS_CORE_UI_UNSUPPORTED_BROWSER_TITLE' i18n:catalogue='plugin.core-ui'/></title>
                <meta http-equiv="X-UA-Compatible" content="IE=10" />
                <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"/>
                
                <link rel="stylesheet" type="text/css" href="{$contextPath}/plugins/core-ui/resources/css/unsupported-browser.css"/>
            </head>
            <body>
                    <div class="unsupported-browser-header"><i18n:text i18n:catalogue='application' i18n:key='APPLICATION_PRODUCT_LABEL'/></div>
                    <table class="unsupported-browser-inner-container" width="900px" height="100%" align="center">
                        <tr>
                            <td valign="middle">
                                <div class="text-container">
                                    <div class="unsupported-browser-text"><i18n:text i18n:key="PLUGINS_CORE_UI_UNSUPPORTED_BROWSER_TITLE" i18n:catalogue='plugin.core-ui'/></div>
                            
                                    <div class="unsupported-browser-desc">
                                        <xsl:call-template name="description"/>
                                    </div>                    
                                    
                                    <xsl:call-template name="showBrowsers"/>
                                    
                                    <div class="unsupported-browser-redirect-text">
                                        <xsl:choose>
                                            <xsl:when test="$doc != ''">
                                                <a href="{$doc}" target="_blank">
                                                    <i18n:text i18n:key="PLUGINS_CORE_UI_UNSUPPORTED_BROWSER_DOC" i18n:catalogue='plugin.core-ui'/>
                                                </a>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <i18n:text i18n:key="PLUGINS_CORE_UI_UNSUPPORTED_BROWSER_DOC" i18n:catalogue='plugin.core-ui'/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                     </div>
                                </div>
                            </td>
                        </tr>
                    </table>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template name="description">
        <i18n:translate>
                <i18n:text i18n:key="PLUGINS_CORE_UI_UNSUPPORTED_BROWSER_TEXT1" i18n:catalogue='plugin.core-ui'/>
                <i18n:param><xsl:call-template name="browserName"/></i18n:param>
        </i18n:translate>
        <br/>
        <i18n:text i18n:key="PLUGINS_CORE_UI_UNSUPPORTED_BROWSER_TEXT2" i18n:catalogue='plugin.core-ui'/>
    </xsl:template>
    
    <xsl:template name="browserName">
        <xsl:choose>
            <xsl:when test="$browser = 'ie'"><xsl:text> </xsl:text>(Microsoft Internet Explorer<xsl:text> </xsl:text><xsl:value-of select="$browserversion"/>)</xsl:when>
            <xsl:when test="$browser = 'ff'"><xsl:text> </xsl:text>(Mozilla Firefox<xsl:text> </xsl:text><xsl:value-of select="$browserversion"/>)</xsl:when>
            <xsl:when test="$browser = 'ch'"><xsl:text> </xsl:text>(Google Chrome<xsl:text> </xsl:text><xsl:value-of select="$browserversion"/>)</xsl:when>
            <xsl:when test="$browser = 'sa'"><xsl:text> </xsl:text>(Apple Safari<xsl:text> </xsl:text><xsl:value-of select="$browserversion"/>)</xsl:when>
            <xsl:when test="$browser = 'op'"><xsl:text> </xsl:text>(Opera<xsl:text> </xsl:text><xsl:value-of select="$browserversion"/>)</xsl:when>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="showBrowsers">
        <div class="browsers">
            <xsl:call-template name="parseBrowser">
                <xsl:with-param name="browserCode" select="'ie'"/>
                <xsl:with-param name="browserName" select="'Internet Explorer'"/>
                <xsl:with-param name="browserIcon" select="'ie_48.png'"/>
                <xsl:with-param name="supported" select="$supported"/>
            </xsl:call-template>
            <xsl:call-template name="parseBrowser">
                <xsl:with-param name="browserCode" select="'ff'"/>
                <xsl:with-param name="browserName" select="'Mozilla Firefox'"/>
                <xsl:with-param name="browserIcon" select="'firefox_48.png'"/>
                <xsl:with-param name="supported" select="$supported"/>
            </xsl:call-template>
            <xsl:call-template name="parseBrowser">
                <xsl:with-param name="browserCode" select="'ch'"/>
                <xsl:with-param name="browserName" select="'Google Chrome'"/>
                <xsl:with-param name="browserIcon" select="'chrome_48.png'"/>
                <xsl:with-param name="supported" select="$supported"/>
            </xsl:call-template>
            <xsl:call-template name="parseBrowser">
                <xsl:with-param name="browserCode" select="'sa'"/>
                <xsl:with-param name="browserName" select="'Apple Safari'"/>
                <xsl:with-param name="browserIcon" select="'safari_48.png'"/>
                <xsl:with-param name="supported" select="$supported"/>
            </xsl:call-template>
            <xsl:call-template name="parseBrowser">
                <xsl:with-param name="browserCode" select="'op'"/>
                <xsl:with-param name="browserName" select="'Opera'"/>
                <xsl:with-param name="browserIcon" select="'opera_48.png'"/>
                <xsl:with-param name="supported" select="$supported"/>
            </xsl:call-template>
            <div class="browsers-end"/>
        </div>    
    </xsl:template>
    
    <xsl:template name="parseBrowser">
        <xsl:param name="browserCode"/>
        <xsl:param name="browserName"/>
        <xsl:param name="browserIcon"/>
        <xsl:param name="supported"/>
    
        <xsl:variable name="browserCodeWithQuots" select="substring-after($supported, $browserCode)"/>
        <xsl:if test="$browserCodeWithQuots != ''">
            <xsl:variable name="c">'</xsl:variable>


            <xsl:variable name="v" select="substring-before(substring-after(substring-after($browserCodeWithQuots, ':'), $c), $c)"/>
            <div class="browser">
                <div class="browser-name">
                    <img src="{$contextPath}/plugins/core-ui/resources/img/browsers/{$browserIcon}" alt=""/>
                    <br/><xsl:value-of select="$browserName"/>
                </div>
                <div class="browser-version">
                    <xsl:choose>
                        <xsl:when test="$v = '0-0'">
                            <i18n:key i18n:text="PLUGINS_CORE_UI_SUPPORTED_BROWSER_VERSIONS_ALL" i18n:catalogue='plugin.core-ui'/>
                        </xsl:when>
                        <xsl:when test="substring-after($v, '0-') != ''">
                            <i18n:translate>
                                <i18n:text i18n:key="PLUGINS_CORE_UI_SUPPORTED_BROWSER_VERSIONS_LOWER" i18n:catalogue='plugin.core-ui'/>
                                <i18n:param><xsl:value-of select="substring-after($v, '0-')"/></i18n:param>
                            </i18n:translate>               
                        </xsl:when>
                        <xsl:when test="substring-before($v, '-0') != ''">
                            <i18n:translate>
                                <i18n:text i18n:key="PLUGINS_CORE_UI_SUPPORTED_BROWSER_VERSIONS_ABOVE" i18n:catalogue='plugin.core-ui'/>
                                <i18n:param><xsl:value-of select="substring-before($v, '-0')"/></i18n:param>
                            </i18n:translate>               
                        </xsl:when>
                        <xsl:otherwise>
                            <i18n:translate >
                                <i18n:text i18n:key="PLUGINS_CORE_UI_SUPPORTED_BROWSER_VERSIONS_BETWEEN" i18n:catalogue='plugin.core-ui'/>
                                <i18n:param><xsl:value-of select="substring-before($v, '-')"/></i18n:param>
                                <i18n:param><xsl:value-of select="substring-after($v, '-')"/></i18n:param>
                            </i18n:translate>               
                        </xsl:otherwise>
                    </xsl:choose>
                </div>
                <div class="browser-end"></div>
            </div>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>