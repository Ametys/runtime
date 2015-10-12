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
    
    <xsl:param name="doc" select="ametys:requestParameter('doc')"/><!-- absolute link to online doc -->
    <xsl:param name="browser" select="ametys:requestParameter('browser')"/><!-- current browser -->
    <xsl:param name="browserversion" select="ametys:requestParameter('browserversion')"/><!-- current version -->
    <xsl:param name="supported" select="ametys:requestParameter('supported')"/><!-- the json list of supported browsers -->
    
    <xsl:variable name="contextPath" select="ametys:uriPrefix(false())"/>
    
    <xsl:template match="/">
        <html>
            <head>
                <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
                <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1"/>
                <title>Ametys - <i18n:text i18n:key="PLUGINS_CORE_UI_UNSUPPORTED_BROWSER_HEAD" i18n:catalogue="plugin.core-ui"/></title>

                <link rel="icon" type="image/x-icon" href="{$contextPath}/kernel/resources/favicon.ico" />
                <link rel="shortcut icon" type="image/x-icon" href="{$contextPath}/kernel/resources/favicon.ico" />
                
                <link rel="stylesheet" type="text/css" href="{$contextPath}/plugins/core-ui/resources/css/special/browsers.css"/>
            </head>
            <body>
                    <table class="main">
                        <thead>
                           <tr><td><i18n:text i18n:catalogue='application' i18n:key='APPLICATION_PRODUCT_LABEL'/></td></tr>
                        </thead>
                        <tbody>
                            <tr class="main">
                                <td class="main">
                                    <div class="wrap">
                                        <h1 class="long">
                                            <i18n:text i18n:key="PLUGINS_CORE_UI_UNSUPPORTED_BROWSER_TITLE" i18n:catalogue="plugin.core-ui"/>
                                        </h1>
                                        <p class="text">
									        <i18n:translate>
									                <i18n:text i18n:key="PLUGINS_CORE_UI_UNSUPPORTED_BROWSER_TEXT1" i18n:catalogue='plugin.core-ui'/>
									                <i18n:param><xsl:call-template name="browserName"/></i18n:param>
									        </i18n:translate>
									        <br/>
									        
									        <xsl:if test="$supported">
									           <i18n:text i18n:key="PLUGINS_CORE_UI_UNSUPPORTED_BROWSER_TEXT2" i18n:catalogue='plugin.core-ui'/>
									        </xsl:if>
                                        </p>
                                        <xsl:if test="$supported">
									        <div class="browsers">
									            <xsl:call-template name="showBrowsers"/>
									            <div class="browsers-end"></div>
									        </div>
									    </xsl:if>
								        
								        <p class="bottomtext">
								           <i18n:text i18n:key="PLUGINS_CORE_UI_UNSUPPORTED_BROWSER_DOC" i18n:catalogue='plugin.core-ui'/>
								        </p>
								        <xsl:if test="$doc != ''">
								            <xsl:call-template name="button">
								                <xsl:with-param name="text"><i18n:text i18n:key='PLUGINS_CORE_UI_UNSUPPORTED_BROWSER_DOC_BTN' i18n:catalogue='plugin.core-ui'/></xsl:with-param>
								                <xsl:with-param name="href" select="$doc"/>
								            </xsl:call-template>
								        </xsl:if>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                        <tfoot>
                           <tr>
                                <td>
				                        <p>
				                            <i18n:text i18n:key="PLUGINS_CORE_UI_WORKSPACE_AMETYS_SPLASHSCREEN_APP_AMETYS_VERSION" i18n:catalogue="plugin.core-ui"/>
				                            <xsl:variable name="Versions" select="ametys:versions()"/>
				                            <xsl:if test="$Versions/Component[Name='Ametys' and Version]">
				                                &#160;<xsl:value-of select="Component[Name='Ametys']/Version"/>
				                            </xsl:if>
				                        </p>
                                </td>
                           </tr>
                        </tfoot>
                    </table>            
            </body>
        </html>
    </xsl:template>
    
    <xsl:template name="button">
        <xsl:param name="text"/>
        <xsl:param name="href">javascript:void(0)</xsl:param>
        
        <p class="additionnal">
            <a href="{$href}"><xsl:copy-of select="$text"/> ></a>
        </p>    
    </xsl:template>
    
    <xsl:template name="browserName">
        <xsl:if test="string-length($browser) > 0">
	        <xsl:text> (</xsl:text>
	        <i18n:text i18n:catalogue='plugin.core-ui'>PLUGINS_CORE_UI_UNSUPPORTED_BROWSER_NAME_<xsl:value-of select="$browser"/></i18n:text>
	        <xsl:text> </xsl:text>
	        <xsl:value-of select="$browserversion"/>
	        <xsl:text>)</xsl:text>
	    </xsl:if>
    </xsl:template>
    
    <xsl:template name="showBrowsers">
        <xsl:param name="supported" select="$supported"/>
        
        <xsl:variable name="browserRaw">
	        <xsl:choose>
	            <xsl:when test="contains($supported, ',')"><xsl:value-of select="substring-before($supported, ',')"/></xsl:when>
	            <xsl:otherwise><xsl:value-of select="$supported"/></xsl:otherwise>
	        </xsl:choose>
	    </xsl:variable>
	    
	    <xsl:variable name="separator">'</xsl:variable>
	    <xsl:variable name="browserCode" select="substring-before(substring-after($browserRaw, $separator), $separator)"/>
        <xsl:variable name="browserMin" select="substring-before(substring-after(substring-after(substring-after($browserRaw, $separator), $separator), $separator), '-')"/>
        <xsl:variable name="browserMax" select="substring-before(substring-after(substring-after(substring-after(substring-after($browserRaw, $separator), $separator), $separator), '-'), $separator)"/>
	    
        <div class="browser">
            <div class="browser-name">
                <img src="{$contextPath}/plugins/core-ui/resources/img/special/browsers/{$browserCode}_48.png" alt=""/>
                <br/>
                <i18n:text i18n:key="PLUGINS_CORE_UI_UNSUPPORTED_BROWSER_NAME_{$browserCode}" i18n:catalogue='plugin.core-ui'/>
            </div>
            <div class="browser-version">
                <xsl:choose>
                    <xsl:when test="$browserMin = '0' and $browserMax = '0'">
                        <i18n:key i18n:text="PLUGINS_CORE_UI_SUPPORTED_BROWSER_VERSIONS_ALL" i18n:catalogue='plugin.core-ui'/>
                    </xsl:when>
                    <xsl:when test="$browserMin = '0'">
                        <i18n:translate>
                            <i18n:text i18n:key="PLUGINS_CORE_UI_SUPPORTED_BROWSER_VERSIONS_LOWER" i18n:catalogue='plugin.core-ui'/>
                            <i18n:param><xsl:value-of select="$browserMax"/></i18n:param>
                        </i18n:translate>               
                    </xsl:when>
                    <xsl:when test="$browserMax = '0'">
                        <i18n:translate>
                            <i18n:text i18n:key="PLUGINS_CORE_UI_SUPPORTED_BROWSER_VERSIONS_ABOVE" i18n:catalogue='plugin.core-ui'/>
                            <i18n:param><xsl:value-of select="$browserMin"/></i18n:param>
                        </i18n:translate>               
                    </xsl:when>
                    <xsl:otherwise>
                        <i18n:translate >
                            <i18n:text i18n:key="PLUGINS_CORE_UI_SUPPORTED_BROWSER_VERSIONS_BETWEEN" i18n:catalogue='plugin.core-ui'/>
                            <i18n:param><xsl:value-of select="$browserMin"/></i18n:param>
                            <i18n:param><xsl:value-of select="$browserMax"/></i18n:param>
                        </i18n:translate>               
                    </xsl:otherwise>
                </xsl:choose>
            </div>
        </div>
	    
	    <xsl:if test="contains($supported, ',')">
	       <xsl:call-template name="showBrowsers">
	           <xsl:with-param name="supported" select="substring-after($supported, ',')"/>
	       </xsl:call-template>
	    </xsl:if>
    </xsl:template>
        
</xsl:stylesheet>