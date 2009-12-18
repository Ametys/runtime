<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2009 Anyware Services

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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 
    <xsl:import href="utils.xsl"/> 
    <xsl:import href="configuration.xsl"/>
    <xsl:import href="extensions.xsl"/>
    <xsl:import href="features.xsl"/>

    <xsl:param name="pluginName">unknown</xsl:param>
 
    <xsl:template match="/plugin">    
        <html>   
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <meta content="Ametys" name="generator"/>
                <title>Ametys Runtime 1.1 - <xsl:value-of select="$pluginName"/></title>
                <link rel="stylesheet" type="text/css" href="resources/css/plugindoc.css" title="Style"/>
                <link rel="stylesheet" type="text/css" href="resources/css/plugindoc_main.css" title="Style"/>
            </head>
            <body>
                <!-- SUMMARY -->
                
                <xsl:call-template name="configuration-left-summary">
                    <xsl:with-param name="target">Navigation</xsl:with-param>
                </xsl:call-template>

            </body>
        </html>
    </xsl:template> 
    
</xsl:stylesheet>