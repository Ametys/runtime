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
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">

	<xsl:import href="kernel://stylesheets/kernel.xsl"/>

	<!-- +
	     | Display the Ametys main screen
	     |
	     | @param {Boolean} needs-kernel-ui True to load all js/css for kernel display. (default if true). If false many the following parameters are not applyable.
         | @param {String} plugins-direct-prefix Prefix for direct url to plugins (used for AJAX connections) with leading '/' nor context path. e.g. '/_plugins' 
         | @param {String} plugins-wrapped-prefix Prefix for wrapped url to plugins (used for redirections) with leading '/' nor context path. e.g. '/plugins'
         | @param {Boolean} debug-mode Load JS files in debug mode when available.
         | @param {String} context-path The application context path. Can be empty for ROOT context path or should begin with / in other cases. E.g. '/MyContext'
         | @param {String} workspace-name The name of the current ametys workspace. Cannot be empty. E.g. 'admin'
         | @param {String} workspace-prefix The prefix of the current workspace (so not starting with the context path). If the workspace is the default one, this can be empty. E.g. '', '/_MyWorkspace'
         | @param {String} max-upload-size The parametrized max size for upadloding file to the server. In Bytes. E.g. 10670080 for 10 MB. Can be empty if unknown.
         | @param {String} language-code The language code supported when loading the application. E.g. 'en' or 'fr'. Defaults to the i18n value for key kernel:KERNEL_LANGUAGE_CODE.
         | @param {String} authorized-browsers Optionnaly, you can provide such a string :
		 | 					{
  		 |                     'supported': { 'ie' : '7-9'}, 					// List of supported browsers with their versions
		 | 					   'not-supported': { 'ie' : '0-6', ch : '10-0'},	// List of non supported browsers with their versions
 		 | 					   'warning-redirection': "warning.html",			// Redirection when the browser may be supported
		 | 					   'failure-redirection': "failure.html"			// Redirection when the browser is not supported
		 |					If window.ametys_authorized_browsers is undefined, no browser test will be done.<br/>
 		 | 					When a supported browser is detected, everything goes on normally.<br/>
		 | 					When a not-supported browser is detected, the application is redirected to the failure rediction url. Where a message should indicates to use a supported browser.<br/>
		 | 					When another browser is detected, the application is redirected to the warning redirection url. Where a message should indicated to use a supported browser, but the user could enforce the navigation (setting a cookie 'ametys.accept.non.supported.navigators' to 'on').<br/>
		 |					Keys for browsers are 'ie' (Microsoft Internet Explorer), 'ff' (Mozilla Firefox), 'ch' (Google Chrome), 'sa' (Apple Safari) and 'op' (Opera).<br/>
		 |					Versions can be a single number '3' or '3.5', or can be an interleave where 0 is the infinity '0-6' means all versions before 6 and 6 included, '6-0' means all versions after 6 and 6 included.
		 | @param {Boolean} use-js-css-component True to load JS/CSS using the js/css component to minimize css calls (true is the default value). Set to false, if this page should be displayed even if the server is misstarted (error page, config page...)
	     |
	     | @param {String/Node} head-title The title of the head tag. Can be an i18n tag. Default value is the i18n application:APPLICATION_PRODUCT_LABEL.
	     | @param {String} IE-compatibility-mode The internet explorer compatibility mode. Default value is 'IE=edge'. Sample: 'IE=9' or 'IE=EmultateIE7'.
	     | @param {Node} meta-favicon meta tags to display the fav icons. Default value is 2 links tag to display the ametys fav icon (in gif or ico formats).
	     | @param {Node} head-meta Additionnal head meta, script or css loading. If needs-kernel-ui is set to true and use-js-css-component is set to true, you can load js and css using component for performance purposes.
	     |
	     | @param {String/Node} body-title The title to display on the top part of the screen. Can be an i18n tag.
	     | @param {Node} body-col-left The content of the left column
	     | @param {Node} body-col-main The content of the main column
	     | @param {Node} body-col-right The content of the right column
	     | @param {Node} body-footer The content of the footer
	     + -->
	<xsl:template name="home">
        <xsl:param name="needs-kernel-ui" select="true()"/>

        <xsl:param name="plugins-direct-prefix"/>
        <xsl:param name="plugins-wrapped-prefix"/>
        <xsl:param name="debug-mode" select="false()"/>
		<xsl:param name="context-path"/>
		<xsl:param name="workspace-name"/>
		<xsl:param name="workspace-prefix"/>
		<xsl:param name="max-upload-size"/>
		<xsl:param name="language-code"><i18n:text i18n:key='KERNEL_LANGUAGE_CODE' i18n:catalogue='kernel'/></xsl:param>
		<xsl:param name="authorized-browsers"/>
        <xsl:param name="use-js-css-component">true</xsl:param>
		
		<xsl:param name="head-title"><i18n:text i18n:catalogue="application" i18n:key="APPLICATION_PRODUCT_LABEL"/></xsl:param>
		<xsl:param name="IE-compatibility-mode">IE=edge</xsl:param>
		<xsl:param name="meta-favicon">
                <link rel="icon" type="image/gif" href="{$contextPath}/kernel/resources/img/runtime_favico.gif" />
                <link rel="shortcut icon" type="image/x-icon" href="{$contextPath}/kernel/resources/img/runtime_favico.ico" />
        </xsl:param>
		<xsl:param name="head-meta"/>
        
		<xsl:param name="body-title"/>
		<xsl:param name="body-col-left"/>
		<xsl:param name="body-col-main"/>
		<xsl:param name="body-col-right"/>
		<xsl:param name="body-footer"/>
	
		<html>
			<!-- ****** HEAD ****** -->
			<head> 
				<!-- This tag has to one of the first of the head section -->
				<meta http-equiv="X-UA-Compatible" content="{$IE-compatibility-mode}"/>
				
				<title><xsl:copy-of select="$head-title"/></title>

				<xsl:copy-of select="$meta-favicon"/>
                
                <xsl:if test="$needs-kernel-ui">
	           		<xsl:call-template name="kernel-base">
			            <xsl:with-param name="plugins-direct-prefix" select="$plugins-direct-prefix"/>
			            <xsl:with-param name="plugins-wrapped-prefix" select="$plugins-wrapped-prefix"/>
			            <xsl:with-param name="debug-mode" select="$debug-mode"/>
			            <xsl:with-param name="context-path" select="$context-path"/>
			            <xsl:with-param name="workspace-name" select="$workspace-name"/>
			            <xsl:with-param name="workspace-prefix" select="$workspace-prefix"/>
			            <xsl:with-param name="max-upload-size" select="$max-upload-size"/>
			            <xsl:with-param name="language-code" select="$language-code"/>
			            <xsl:with-param name="authorized-browsers" select="$authorized-browsers"/>
			            <xsl:with-param name="use-css-component" select="$use-js-css-component"/>
			            <xsl:with-param name="use-js-component" select="$use-js-css-component"/>
			        </xsl:call-template>
		        </xsl:if>
		               
		        <!-- TODO load this CSS using the component if activated -->
                <link rel="stylesheet" href="{$contextPath}/kernel/resources/css/home.css" type="text/css"/>
                
                <xsl:copy-of select="$head-meta"/>
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
									<xsl:copy-of select="$body-title"/>
								</div>
							</div>
						</td>
						<td/>
					</tr>
					<tr class="home-main">
						<td class="home-col-left"><xsl:copy-of select="$body-col-left"/></td>
						<td class="home-col-main"><xsl:copy-of select="$body-col-main"/></td>
						<td class="home-col-right"><xsl:copy-of select="$body-col-right"/></td>
					</tr>
					<tr class="home-footer">
						<td colspan="3"><xsl:copy-of select="$body-footer"/></td>
					</tr>
				</table>
			</body>
		</html>  
	</xsl:template>
                
</xsl:stylesheet>
