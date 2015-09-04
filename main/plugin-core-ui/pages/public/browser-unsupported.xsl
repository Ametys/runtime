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
    
    <xsl:import href="plugin:core-ui://stylesheets/kernel.xsl"/>    
    
    <xsl:param name="doc"/>
    <xsl:param name="browser"/><!-- current browser -->
    <xsl:param name="browserversion"/><!-- current version -->
    <xsl:param name="supported"/><!-- the json list of supported browsers -->
    
    <xsl:variable name="contextPath" select="ametys:uriPrefix(false())"/>
    <xsl:variable name="workspaceURI" select="ametys:workspacePrefix()"/>
    <xsl:variable name="debug-mode" select="ametys:config('runtime.debug.ui')"/>
    
    <xsl:param name="authFailure">false</xsl:param>
    
    <xsl:template match="/">
        <html>
            <head>
                <title>Ametys - <i18n:text i18n:key='PLUGINS_CORE_UI_UNSUPPORTED_BROWSER_TITLE' i18n:catalogue='plugin.core-ui'/></title>
                <meta http-equiv="X-UA-Compatible" content="IE=10" />
                <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"/>
                
                <link rel="stylesheet" type="text/css" href="{$contextPath}/plugins/core-ui/resources/css/public.css"/>
            </head>
            <body>
                <noscript><i18n:text i18n:key="WORKSPACE_AMETYS_MAIN_ERROR_NOJS" i18n:catalogue="plugin.core-ui"/></noscript>
                
                <xsl:call-template name="kernel-base">
                    <xsl:with-param name="theme">triton</xsl:with-param>
                </xsl:call-template>
                
               <script type="text/javascript" src="{$contextPath}/plugins/core-ui/resources/js/Ametys/public/RedirectActionScreen.js"></script>
               <script type="text/javascript" src="{$contextPath}/plugins/core-ui/resources/js/Ametys/public/UnsupportedBrowserScreen.js"></script>
                
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
                                    items: Ext.create('Ametys.public.UnsupportedBrowserScreen', {
                                        text: "<i18n:text i18n:key="PLUGINS_CORE_UI_UNSUPPORTED_BROWSER_TITLE" i18n:catalogue='plugin.core-ui'/>",
                                        description: "<xsl:call-template name="description"/>",
                                        redirectText: "<i18n:text i18n:key="PLUGINS_CORE_UI_UNSUPPORTED_BROWSER_DOC" i18n:catalogue='plugin.core-ui'/>",
                                        btnText: "<i18n:text i18n:key="PLUGINS_CORE_UI_UNSUPPORTED_BROWSER_DOC_BTN" i18n:catalogue='plugin.core-ui'/>",
                                        redirectUrl: "<xsl:value-of select="$doc"/>",
                                        supported: <xsl:value-of select="$supported"/>,
                                        contextPath: "<xsl:value-of select="$contextPath"/>"
                                    })
                                });
                            }
                        })
                    });
                </script>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template name="description">
        <i18n:translate>
                <i18n:text i18n:key="PLUGINS_CORE_UI_UNSUPPORTED_BROWSER_TEXT1" i18n:catalogue='plugin.core-ui'/>
                <i18n:param><xsl:call-template name="browserName"/></i18n:param>
        </i18n:translate>
        <xsl:text>&lt;br/&gt;</xsl:text>
        <i18n:text i18n:key="PLUGINS_CORE_UI_UNSUPPORTED_BROWSER_TEXT2" i18n:catalogue='plugin.core-ui'/>
    </xsl:template>
    
    <xsl:template name="ametys-scripts"/>
</xsl:stylesheet>