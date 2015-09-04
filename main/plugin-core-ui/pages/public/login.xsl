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
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
    
    <xsl:import href="plugin:core-ui://stylesheets/kernel.xsl"/>    
    
    <xsl:variable name="contextPath" select="ametys:uriPrefix(false())"/>
    <xsl:variable name="workspaceURI" select="ametys:workspacePrefix()"/>
    <xsl:variable name="debug-mode" select="ametys:config('runtime.debug.ui')"/>
    
    <xsl:param name="authFailure">false</xsl:param>
    
    <xsl:template match="/LoginForm">
        <html>
            <head>
                <title>Ametys - <i18n:text i18n:key='PLUGINS_CORE_UI_LOGIN_SCREEN_TITLE' i18n:catalogue='plugin.core-ui'/></title>
                <meta http-equiv="X-UA-Compatible" content="IE=10" />
                <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"/>
                
                <link rel="stylesheet" type="text/css" href="{$contextPath}/plugins/core-ui/resources/css/login.css"/>
            </head>
            <body>
                <noscript><i18n:text i18n:key="WORKSPACE_AMETYS_MAIN_ERROR_NOJS" i18n:catalogue="plugin.core-ui"/></noscript>
                
                <xsl:call-template name="kernel-base">
                    <xsl:with-param name="theme">triton</xsl:with-param>
                </xsl:call-template>
                
                <script type="text/javascript" src="{$contextPath}/plugins/core-ui/resources/js/Ametys/public/LoginScreen.js"/>
                
                <xsl:call-template name="twitter-scripts"/>
                
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
                                    items: Ext.create('Ametys.public.LoginScreen', {
                                        loginFieldName: 'Username',
                                        pwdFieldName: 'Password',
                                        
                                        authFailure: <xsl:value-of select="$authFailure"/>,
                                        rememberMe: <xsl:value-of select="rememberMe"/>,
                                        forgotPassword: false
                                    })
                                });
                            }
                        })
                    });
                </script>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template name="twitter-scripts">
        <script>
        !function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+"://platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);}}(document,"script","twitter-wjs");
        </script>
    </xsl:template>
    
    <!-- Do not load Ametys scripts -->
    <xsl:template name="ametys-scripts"/>
    
    <!-- Ignore browser compatibility -->
    <xsl:template name="kernel-browsers"/>
</xsl:stylesheet>