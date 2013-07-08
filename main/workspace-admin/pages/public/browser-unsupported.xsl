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
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
                xmlns:parsebrowsers="org.ametys.runtime.workspaces.admin.browsers.ParseBrowsers">
	
	<xsl:import href="kernel://pages/home/home.xsl"/>
    
    <xsl:param name="redirect"/><!-- uri for enforcement -->
    <xsl:param name="doc"/><!-- uri for doc -->
    <xsl:param name="browser"/><!-- current browser -->
    <xsl:param name="browserversion"/><!-- current version -->
    <xsl:param name="supported"/><!-- the json list of supported browsers -->
    
    <xsl:param name="contextPath"/>
  	<xsl:param name="workspaceName"/>    
    <xsl:param name="workspaceURI"/>
    
    
    <xsl:template match="/">
    	<xsl:call-template name="home">
		    <xsl:with-param name="needs-kernel-ui" select="false()"/>
		    <xsl:with-param name="context-path" select="$contextPath"/>
		    <xsl:with-param name="workspace-name" select="$workspaceName"/>
		    <xsl:with-param name="workspace-prefix" select="$workspaceURI"/>
		    
		    <xsl:with-param name="head-title">
					<i18n:text i18n:catalogue="workspace.{$workspaceName}" i18n:key="WORKSPACE_ADMIN_LABEL_SHORT"/>
					<xsl:text> </xsl:text>
					<i18n:text i18n:catalogue="application" i18n:key="APPLICATION_PRODUCT_LABEL"/>
		    </xsl:with-param>
		    
		    <xsl:with-param name="head-meta">
                <link rel="stylesheet" href="{$contextPath}/kernel/resources/css/home-text.css" type="text/css"/>
                <link rel="stylesheet" href="{$contextPath}{$workspaceURI}/resources/css/public/browsers-issue.css" type="text/css"/>
		    </xsl:with-param>

		    <xsl:with-param name="body-col-main">
		    	<h1>
		    		<i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_UNSUPPORTED_TITLE"/>
		    	</h1>
		    	
		    	<p class="hint">
					<i18n:translate>
	                	<i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_UNSUPPORTED_TEXT"/>
	                	<i18n:param><xsl:call-template name="browserName"/></i18n:param>
	                </i18n:translate>
                </p>

				<xsl:call-template name="showBrowsers"/>

		    	<p class="hint">
		    		<xsl:choose>
			    		<xsl:when test="$doc != ''">
		                	<a href="{$doc}" target="_blank">
	        	        		<i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_UNSUPPORTED_TEXT2"/>
	            	    	</a>
	            	    </xsl:when>
	            	    <xsl:otherwise>
							<i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_UNSUPPORTED_TEXT2"/>
	            	    </xsl:otherwise>
            	    </xsl:choose>
                </p>
		    </xsl:with-param>
    	</xsl:call-template>
    </xsl:template>
    
    <xsl:template name="browserName">
    	<strong>
	   		<xsl:choose>
	   			<xsl:when test="$browser = 'ie'"><xsl:text> </xsl:text>(Microsoft Internet Explorer<xsl:text> </xsl:text><xsl:value-of select="$browserversion"/>)</xsl:when>
	   			<xsl:when test="$browser = 'ff'"><xsl:text> </xsl:text>(Mozilla Firefox<xsl:text> </xsl:text><xsl:value-of select="$browserversion"/>)</xsl:when>
	   			<xsl:when test="$browser = 'ch'"><xsl:text> </xsl:text>(Google Chrome<xsl:text> </xsl:text><xsl:value-of select="$browserversion"/>)</xsl:when>
	   			<xsl:when test="$browser = 'sa'"><xsl:text> </xsl:text>(Apple Safari<xsl:text> </xsl:text><xsl:value-of select="$browserversion"/>)</xsl:when>
	   			<xsl:when test="$browser = 'op'"><xsl:text> </xsl:text>(Opera<xsl:text> </xsl:text><xsl:value-of select="$browserversion"/>)</xsl:when>
	   		</xsl:choose>
   		</strong>
    </xsl:template>
    
    <xsl:template name="showBrowsers">
		<div class="browsers">
			<xsl:call-template name="parseBrowser">
		    	<xsl:with-param name="browserCode" select="'ie'"/>
		    	<xsl:with-param name="browserName" select="'Microsoft Internet Explorer'"/>
		    	<xsl:with-param name="browserIcon" select="'ie_48.png'"/>
		    	<xsl:with-param name="supported" select="$supported"/>
			</xsl:call-template>
			<xsl:call-template name="parseBrowser">
		    	<xsl:with-param name="browserCode" select="'ff'"/>
		    	<xsl:with-param name="browserName" select="'Mozilla Firefox'"/>
		    	<xsl:with-param name="browserIcon" select="'firefox_48.png'"/>
		    	<xsl:with-param name="supported" select="$supported"/>
			</xsl:call-template>
			<xsl:call-template name="parseBrowser">
		    	<xsl:with-param name="browserCode" select="'ch'"/>
		    	<xsl:with-param name="browserName" select="'Google Chrome'"/>
		    	<xsl:with-param name="browserIcon" select="'chrome_48.png'"/>
		    	<xsl:with-param name="supported" select="$supported"/>
			</xsl:call-template>
			<xsl:call-template name="parseBrowser">
		    	<xsl:with-param name="browserCode" select="'sa'"/>
		    	<xsl:with-param name="browserName" select="'Apple Safari'"/>
		    	<xsl:with-param name="browserIcon" select="'safari_48.png'"/>
		    	<xsl:with-param name="supported" select="$supported"/>
			</xsl:call-template>
			<xsl:call-template name="parseBrowser">
		    	<xsl:with-param name="browserCode" select="'op'"/>
		    	<xsl:with-param name="browserName" select="'Opera'"/>
		    	<xsl:with-param name="browserIcon" select="'opera_48.png'"/>
		    	<xsl:with-param name="supported" select="$supported"/>
			</xsl:call-template>
    		<div class="browsers-end"/>
		</div>    
    </xsl:template>
    
    <xsl:template name="parseBrowser">
    	<xsl:param name="browserCode"/>
    	<xsl:param name="browserName"/>
    	<xsl:param name="browserIcon"/>
    	<xsl:param name="supported"/>
    
    	<xsl:variable name="browserCodeWithQuots" select="substring-after($supported, $browserCode)"/>
    	<xsl:if test="$browserCodeWithQuots != ''">
    		<xsl:variable name="c">'</xsl:variable>


    		<xsl:variable name="v" select="substring-before(substring-after(substring-after($browserCodeWithQuots, ':'), $c), $c)"/>
    		<div class="browser">
	    		<div class="browser-name">
	    			<img src="{$contextPath}{$workspaceURI}/resources/img/browsers/{$browserIcon}" alt=""/>
		    		<span><xsl:value-of select="$browserName"/></span>
	    		</div>
	    		<div class="browser-version">
		    		<xsl:choose>
		    			<xsl:when test="$v = '0-0'">
		    				<i18n:key i18n:text="WORKSPACE_ADMIN_NAVIGATOR_VERSION_ALL"/>
		    			</xsl:when>
		    			<xsl:when test="substring-after($v, '0-') != ''">
							<i18n:translate>
								<i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_VERSION_UPTO"/>
							  	<i18n:param><xsl:value-of select="substring-after($v, '0-')"/></i18n:param>
							</i18n:translate>    			
		    			</xsl:when>
		    			<xsl:when test="substring-before($v, '-0') != ''">
							<i18n:translate>
								<i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_VERSION_AFTER"/>
							  	<i18n:param><xsl:value-of select="substring-before($v, '-0')"/></i18n:param>
							</i18n:translate>    			
		    			</xsl:when>
		    			<xsl:otherwise>
							<i18n:translate >
								<i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_VERSION_BETWEEN"/>
							  	<i18n:param><xsl:value-of select="substring-before($v, '-')"/></i18n:param>
							  	<i18n:param><xsl:value-of select="substring-after($v, '-')"/></i18n:param>
							</i18n:translate>    			
		    			</xsl:otherwise>
		    		</xsl:choose>
		    	</div>
	    		<div class="browser-end"/>
		    </div>
    	</xsl:if>
    </xsl:template>
</xsl:stylesheet>
