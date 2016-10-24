<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2015 Anyware Services

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
                xmlns:stringutils="org.apache.commons.lang.StringUtils"
                exclude-result-prefixes="ex">

    <xsl:param name="contextPath"/>
    <xsl:param name="realPath" />
    <xsl:param name="code" />

    <xsl:template match="/">
        <html>
            <head> 
                <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
                <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"/>
                <title>Ametys - Fatal error</title>

                <link rel="icon" type="image/x-icon" href="{$contextPath}/favicon.ico" />
                <link rel="shortcut icon" type="image/x-icon" href="{$contextPath}/favicon.ico" />
                
                <style type="text/css">
                    html {
                        background-color: #812281;
                        background-size: cover;
                        font-family: 'Helvetica Neue', helvetica, arial, verdana, sans-serif;
                        font-size: 12px;
                        color: #efefef;
                        height: 100%;
                    }

                    body {
                        margin: 0;
                    }

                    /* The animation code **/
                    @keyframes smiley {
                        0% {
                            margin: 0 0; 
                            transform: rotate(0deg);   
                            -ms-transform: rotate(0deg); 
                            -webkit-transform: rotate(0deg); 
                            -moz-transform: rotate(0deg);     
                            -o-transform: rotate(0deg); 
                        }
                        25% {
                            margin: 0 0; 
                            transform: rotate(0deg); 
                            -ms-transform: rotate(0deg); 
                            -webkit-transform: rotate(0deg); 
                            -moz-transform: rotate(0deg);     
                            -o-transform: rotate(0deg);
                        }
                        100% {
                            transform: rotate(90deg); 
                            -ms-transform: rotate(90deg); 
                            -webkit-transform: rotate(90deg); 
                            -moz-transform: rotate(90deg);     
                            -o-transform: rotate(90deg);  
                        }
                    }

                    table {
                        table-layout: fixed;
                        width: 100%;
                        height: 100%;
                        border-collapse: collapse;
                    }
                    table tr.main td {
                        padding: 3em 2em 2em 10%;
                    }

                    .ametys-fatal-page-text {
                        font-size: 11em;
                        line-height: 1.3em;
                    }
                    .ametys-fatal-page-text span {
                        -ms-transform: rotate(90deg); 
                        -webkit-transform: rotate(90deg); 
                        -moz-transform: rotate(90deg);     
                        -o-transform: rotate(90deg);  
                        margin: 0 0.25em;  
                        display: inline-block; 
                        animation-name: smiley;
                        animation-duration: 4s;
                        animation-timing-function: ease;
                    }

                    .ametys-fatal-page-subtext
                    {
                        font-size: 2em;
                        line-height: 1.5em;
                    }

                    table tr.secondary {
                        height: 100%;
                    }
                    
                    table tr.secondary td {
                        padding: 0 1em 1em 10%;
                    }

                    .wrap {
                        height: 100%;
                        overflow: auto;
                    }

                    h1 {
                        font-size: 1em;
                    }

                    .ametys-fatal-page-details {
                        padding: 0.3em;
                        background: none;
                        border: none;
                        margin-top: 0.5em;
                    }

                    /**
                     * MOBILE
                     */

                    @media screen and (max-width: 960px), screen and (max-height: 360px)) {
                        html {
                            font-size: 9px;
                        }
                        
                        table tr.main td {
                            padding: 3em 2em 2em 2em;
                        }
                        
                        table tr.secondary td {
                            padding: 0 1em 1em 2em;
                        }
                        
                        .wrap {
                            height: auto;
                        }
                        
                        .ametys-fatal-page-text {
                            font-size: 7em;
                        }
                    }
                </style>
           </head>
           <body>
                <table>
                    <tr class="main">
                        <td>
                            <div class="ametys-fatal-page-text">Too bad <span>:(</span></div>
                            <div class="ametys-fatal-page-subtext">An error occurred.<br/>Please contact the administrator of the application.</div>
                        </td>
                    </tr>
                    <tr class="secondary">
                        <td>
                            <div class="wrap">
                                <xsl:call-template name="stacktrace">
                                    <xsl:with-param name="exception" select="/ex:exception-report"/>
                                        <xsl:with-param name="realPath" select="$realPath"/>
                                </xsl:call-template>
                            </div>
                        </td>
                    </tr>
                </table>
           </body>
        </html>
    </xsl:template>
    
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
            <pre><code><xsl:call-template name="escape-location"><xsl:with-param name="location" select="$exception/ex:stacktrace"/></xsl:call-template></code></pre>
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
