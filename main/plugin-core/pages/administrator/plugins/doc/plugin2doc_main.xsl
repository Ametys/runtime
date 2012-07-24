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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:plugin="http://www.ametys.org/schema/plugin">
 
    <xsl:import href="utils.xsl"/>
    <xsl:import href="configuration.xsl"/>
    <xsl:import href="extensions.xsl"/>
    <xsl:import href="features.xsl"/>

    <xsl:param name="currentPluginName"/>
 
	<xsl:template match="/plugins[$currentPluginName='']">
        <html>   
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <meta content="Ametys" name="generator"/>
                <title>Ametys</title>
                <link rel="stylesheet" type="text/css" href="{$resourcesPath}/css/administrator/plugindoc.css" title="Style"/>
            </head>
            <body class="head">
		        <h1 class="head">
		        	<img src="{$resourcesPath}/img/administrator/plugins/doc/runtime.jpg" style="float: left"/>
		            <a name="top">All Plugins</a>
		            <br/>
	            	<xsl:for-each select="plugin:plugin">
	            		<xsl:sort select="@name"/>
	            		<a href="#{@name}"><xsl:value-of select="@name"/></a>
	            		<xsl:if test="position() != last()"><xsl:text>, </xsl:text></xsl:if>
	            	</xsl:for-each>.
		        </h1>
            	
            	<xsl:for-each select="plugin:plugin">
                     <xsl:sort select="@name"/>

					<h2><a name="{@name}" href="{@name}_main.html" style="color: #ffffff !important"><xsl:value-of select="@name"/></a></h2>
			        <div class="content">
			            <xsl:call-template name="comment"/>   
			        </div>
                </xsl:for-each>
            </body>
        </html>
 	</xsl:template>
 	
	<xsl:template match="/plugins[$currentPluginName!='']">
 		<xsl:apply-templates select="plugin:plugin[@name=$currentPluginName]"/>
 	</xsl:template>

    <xsl:template match="plugin:plugin">
        <html>   
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <meta content="Ametys" name="generator"/>
                <title>Ametys</title>
                <link rel="stylesheet" type="text/css" href="{$resourcesPath}/css/administrator/plugindoc.css" title="Style"/>
            </head>
            <body class="head">
                <!-- MAIN INFORMATION -->
     
                <xsl:call-template name="main-information"/>
                
                <!-- SUMMARY -->
                
                <xsl:call-template name="configuration-summary"/>

                <xsl:call-template name="extension-points-summary"/>

                <xsl:call-template name="features-summary"/>
 
            </body>
        </html>
    </xsl:template>
    
    <!-- +
         | Display the main information
         + --> 
    <xsl:template name="main-information">
        <h1 class="head">
        	<img src="{$resourcesPath}/img/administrator/plugins/doc/runtime.jpg" style="float: left"/>
            <a name="top">Plugin <xsl:value-of select="$currentPluginName"/></a>
        </h1>
        
        <p>
            Version: 
            <xsl:choose>
                <xsl:when test="@version and @version != ''"><xsl:value-of select="@version"/></xsl:when>
                <xsl:otherwise>Not specified</xsl:otherwise>
            </xsl:choose>
            <br/>
            <br/>
            <a href="main.html">Go back to all plugins</a>
        </p>
        
        <h2>Description</h2>
        <div class="content">
            <xsl:call-template name="comment"/>   
        </div>
    </xsl:template>
    
</xsl:stylesheet>