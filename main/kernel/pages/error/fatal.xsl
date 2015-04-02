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
            <!-- ****** HEAD ****** -->
            <head> 
                <!-- This tag has to one of the first of the head section -->
                <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
                
                <title><xsl:value-of select="$pageTitle" /></title>

                <link rel="icon" type="image/gif" href="{$contextPath}/kernel/resources/img/runtime_favico.gif" />
                <link rel="shortcut icon" type="image/x-icon" href="{$contextPath}/kernel/resources/img/runtime_favico.ico" />
                
                <link rel="stylesheet" href="{$contextPath}/kernel/resources/css_old/home.css" type="text/css"/>
                <link rel="stylesheet" href="{$contextPath}/kernel/resources/css_old/home-text.css" type="text/css"/>
                <link rel="stylesheet" href="{$contextPath}/kernel/resources/css_old/error.css" type="text/css"/>
                
                <script type="text/javascript">
                    <xsl:comment>
                        function toggleDetails(start)
                        {
                            var button = document.getElementById('details-button');
                            var text = document.getElementById('details-place');
                            
                            if (text.style.display == 'none' &amp;&amp; start !== true)
                            {
                                button.innerHTML = 'Hide details ';
                                text.style.display = '';
                                document.body.childNodes[0].style.height = "";
                            }
                            else
                            {
                                button.href = "javascript: toggleDetails();";
                                button.innerHTML = 'Show details';
                                text.style.display = 'none';
                                document.body.childNodes[0].style.height = "auto";
                            }
                        }
                    </xsl:comment>
                </script>
            </head>
            
            <!-- ****** BODY ****** -->
            <body>
                <table class="home-wrapper">
                    <tr class="home-header">
                        <td/>
                        <td>
                            <div class="home-col-main">
                                <div class="ametys-logo-wrapper">
                                    <img src="{$contextPath}/kernel/resources/img/home/ametys.gif" alt="ametys"/>
                                </div>
                                <div class="title-wrapper">
                                    <!-- empty -->
                                </div>
                            </div>
                        </td>
                        <td/>
                    </tr>
                    <tr class="home-main">
                        <td class="home-col-left" id="left"><!-- empty --></td>
                        <td class="home-col-main" id="main">
                            <table class="error">
                                <tr>
                                    <td>
                                        <h1>
                                            <xsl:value-of select="$pageTitle" />
                                        </h1>
                        
                                        <p class="message">
                                            <xsl:if test="@class">
                                                <xsl:value-of select="@class" />
                                            </xsl:if>
                                            <xsl:if test="string-length (ex:message) != 0">
                                                <xsl:if test="@class">
                                                    :
                                                </xsl:if>
                                                <xsl:value-of select="ex:message" />
                                                <xsl:if test="ex:location">
                                                    <br />
                                                    <span style="font-weight: normal">
                                                        <xsl:apply-templates select="ex:location" />
                                                    </span>
                                                </xsl:if>
                                            </xsl:if>
                                        </p>
                                            
                                        <p class="details">
                                            <a id="details-button"></a>
                                        </p>
                                    </td>
                                </tr>
                                <tr class="details">
                                    <td>
                                        <div class="details-place" id="details-place">
                                            <script type="text/javascript">
                                                <xsl:comment>
                                                    toggleDetails(true);
                                                </xsl:comment>
                                            </script>
                                            <p>
                                                <xsl:value-of select="/" />
                                            </p>
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                        <td class="home-col-right" id="right"><!-- empty --></td>
                    </tr>
                    <tr class="home-footer">
                        <td/>
                        <td id="footer"><!-- empty --></td>
                        <td/>
                    </tr>
                </table>
            </body>
        </html>  
    </xsl:template>
    
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
