<?xml version="1.0" encoding="UTF-8"?>
<!--+
    | Copyright (c) 2007 Anyware Technologies and others.
    | All rights reserved. This program and the accompanying materials
    | are made available under the terms of the Eclipse Public License v1.0
    | which accompanies this distribution, and is available at
    | http://www.opensource.org/licenses/eclipse-1.0.php
    | 
    | Contributors:
    |     Anyware Technologies - initial API and implementation
    +-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:plugin="http://www.ametys.org/schema/plugin">
 
    <xsl:import href="utils.xsl"/>
    <xsl:import href="configuration.xsl"/>
    <xsl:import href="extensions.xsl"/>
    <xsl:import href="features.xsl"/>

    <xsl:param name="pluginName">unknown</xsl:param>
 
    <xsl:template match="/plugin:plugin">
        <html>   
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <meta content="Ametys" name="generator"/>
                <title>Ametys Runtime 1.1 - <xsl:value-of select="$pluginName"/></title>
                <link rel="stylesheet" type="text/css" href="resources/css/plugindoc.css" title="Style"/>
                <link rel="stylesheet" type="text/css" href="resources/css/plugindoc_main.css" title="Style"/>
            </head>
            <body>  
              
                <xsl:call-template name="features-left-summary">
                    <xsl:with-param name="target">Navigation</xsl:with-param>
                </xsl:call-template>
                  
            </body>
        </html>
    </xsl:template>
         
</xsl:stylesheet>