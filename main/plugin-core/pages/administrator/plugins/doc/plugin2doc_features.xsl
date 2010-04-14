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

    <xsl:param name="currentPluginName"/>
 
 	<xsl:template match="/plugins">
 		<xsl:apply-templates select="plugin:plugin[@name=$currentPluginName]"/>
 	</xsl:template>
 	
    <xsl:template match="plugin:plugin">
        <html>   
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <meta content="Ametys" name="generator"/>
                <title>Ametys Runtime 1.1 - <xsl:value-of select="$currentPluginName"/></title>
                <link rel="stylesheet" type="text/css" href="{$resourcesPath}/css/administrator/plugindoc.css" title="Style"/>
            </head>
            <body class="head">  
                <xsl:call-template name="main-information"/>            

                <xsl:call-template name="features-detail"/>            
            </body>
        </html>
    </xsl:template>
         
    <!-- +
         | Display the main information
         + --> 
    <xsl:template name="main-information">
        <h1 class="head">
            <img src="{$resourcesPath}/img/administrator/plugins/doc/runtime.jpg" style="float: left"/>
            <a href="{$currentPluginName}_main.html">Plugin <xsl:value-of select="$currentPluginName"/></a>
        </h1>
                
        <p>
            Version: 
            <xsl:choose>
                <xsl:when test="@version and @version != ''"><xsl:value-of select="@version"/></xsl:when>
                <xsl:otherwise>Not specified</xsl:otherwise>
            </xsl:choose>
        </p>
    </xsl:template>
</xsl:stylesheet>