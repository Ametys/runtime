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
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
    
    <xsl:variable name="contextPath" select="ametys:uriPrefix(false())"/>
    
    <xsl:template match="/">
        <html class="ametys-common">
            <head>
                <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
                <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"/>
                <title><xsl:call-template name="head-title"/></title>
                
                <script type="text/javascript">
                    var useragent = navigator.userAgent.toLowerCase();
                    
                    if (/compatible; msie ([0-9.]+);/.test(useragent) || /trident.*rv:([0-9.]+)/.test(useragent))
                    {
                        document.getElementsByTagName("html")[0].classList.add("x-ie");
                    }                
                </script>
                    

                <link rel="icon" type="image/x-icon" href="{$contextPath}/favicon.ico" />
                <link rel="shortcut icon" type="image/x-icon" href="{$contextPath}/favicon.ico" />
                
                <xsl:call-template name="head-css"/>
                
                <xsl:call-template name="head-more"/>
            </head>
            
            <body>
                <xsl:call-template name="body-start"/>
            
                <xsl:call-template name="body"/>
                
                <xsl:call-template name="body-end"/>
            </body>
        </html>
    </xsl:template>

    <xsl:template name="body-impl">
            <div class="head"><header><i18n:text i18n:catalogue='application' i18n:key='APPLICATION_PRODUCT_LABEL'/></header></div>
            <div class="foot">
                <footer>
                    <i18n:text i18n:key="PLUGINS_CORE_UI_WORKSPACE_AMETYS_SPLASHSCREEN_APP_AMETYS_VERSION" i18n:catalogue="plugin.core-ui"/>
                    <xsl:variable name="Versions" select="ametys:versions()"/>
                    <xsl:if test="$Versions/Component[Name='Ametys' and Version]">
                        &#160;<xsl:value-of select="$Versions/Component[Name='Ametys' and Version]/Version"/>
                    </xsl:if>
                </footer>
            </div>                
            
            <div class="main">
                <section>
                    <div class="scroll">
                        <table class="inner">
                            <tr>
                                <td class="inner">
                                    <xsl:call-template name="main"/>
						            <xsl:call-template name="main-after"/>
                                </td>
                            </tr>
                        </table>
                   </div>
                </section>
            </div>
    </xsl:template>
    
    <xsl:template name="main-after"/>
    <xsl:template name="main">
        <div>
            <xsl:attribute name="class"><xsl:call-template name="wrapper-class"/></xsl:attribute>
        
	       <h1>
	           <xsl:attribute name="class"><xsl:call-template name="title-class"/></xsl:attribute>
	           <xsl:call-template name="title"/>
	       </h1>
	       <p class="text">
	           <xsl:call-template name="text"/>
	       </p>
	       <xsl:call-template name="text-additionnal"/>
        </div>
        <xsl:call-template name="div-additionnal"/>
    </xsl:template>
    
    <xsl:template name="head-title">Ametys - <xsl:call-template name="head"/></xsl:template>
    <xsl:template name="head-css"><xsl:call-template name="head-css-impl"/></xsl:template>
    <xsl:template name="head-css-impl">
        <link id="common-css" rel="stylesheet" type="text/css">
            <xsl:attribute name="href"><xsl:value-of select="$contextPath"/><xsl:call-template name="css-file"/></xsl:attribute>
        </link>
        <link rel="stylesheet" href="{$contextPath}/plugins/core-ui/resources/font/ametys/AmetysIcon.css"/>
    </xsl:template>
    <xsl:template name="css-file"><xsl:value-of select="ametys:workspaceThemeURL()"/>/sass/special/common.scss</xsl:template>
    <xsl:template name="head"/>
    <xsl:template name="head-more"/>
    <xsl:template name="body-start"/>
    <xsl:template name="body"><xsl:call-template name="body-impl"/></xsl:template>
    <xsl:template name="body-end"/>
    <xsl:template name="wrapper-class">wrap</xsl:template>
    <xsl:template name="title"/>
    <xsl:template name="title-class"/>
    <xsl:template name="text"/>
    <xsl:template name="text-additionnal"/>
    <xsl:template name="div-additionnal"/>
    
    <xsl:template name="button">
        <xsl:param name="text"/>
        <xsl:param name="title"/>
        <xsl:param name="action"/>
        <xsl:param name="href">javascript:void(0)</xsl:param>
        <xsl:param name="condition"/>
        <xsl:param name="type">load</xsl:param>

        <script type="text/javascript">
            <xsl:if test="$condition">
                if (<xsl:copy-of select="$condition"/>)
                {
            </xsl:if>
            document.write("&lt;p class='additionnal'>\
                          &lt;a <xsl:if test="$title">title=\"<xsl:value-of select='$title'/>\"</xsl:if> href=\"<xsl:value-of select='$href'/>\" onclick=\"<xsl:if test="$type = 'toggle'">this.className = (this.className == '') ? 'toggled' : ''; </xsl:if><xsl:if test="$type = 'load'">this.parentNode.remove(this); document.body.className = 'done'; document.getElementById('pulse').style.display = '';</xsl:if> <xsl:copy-of select='$action'/>\">\
                          <xsl:copy-of select="$text"/>\
                          &lt;/a>\
                          &lt;/p>\
                          <xsl:if test="$type = 'load'">\
                          &lt;div class=\"la-ball-pulse la-dark la-2x\" id=\"pulse\" style=\"display: none\">\
                          &lt;div>&lt;/div>\
                          &lt;div>&lt;/div>\
                          &lt;div>&lt;/div>\
                          &lt;/div>\
                          </xsl:if>\
                          ");
            <xsl:if test="$condition">
                }
            </xsl:if>
        </script>

    </xsl:template>
    
</xsl:stylesheet>
