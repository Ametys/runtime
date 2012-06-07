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
	
	<!-- +
	     | Returns value for the home template, parameter authorized-browsers
	     | @private
	     + -->
	<xsl:template name="authorized-browsers">{
  		    	'supported': { 'ie' : '7-9', ff: '3.6-12', ch: '10-19', op: '11-11.9', sa: '5.0-5.1'},
		  		'not-supported': { 'ie' : '0-6', ff: '0-3.5', ch : '0-9', op: '0-10.99', sa: '0-4.9'},
 		  		'warning-redirection': "<xsl:value-of select="$workspaceURI"/>/public/browser-unknown.html",
		  		'failure-redirection': "<xsl:value-of select="$workspaceURI"/>/public/browser-unsupported.html"}
	</xsl:template>
	
	<!-- +
	     | Returns value for the home template, parameter head-title
	     | @private
	     + -->
	<xsl:template name="head-title">
		<i18n:text i18n:catalogue="workspace.{$workspaceName}" i18n:key="WORKSPACE_ADMIN_LABEL_SHORT"/>
		<xsl:text> - </xsl:text>
		<i18n:text i18n:catalogue="application" i18n:key="APPLICATION_PRODUCT_LABEL"/>
	</xsl:template>
	
	<!-- +
	     | Returns value for the home template, parameter body-title
	     | @private
	     + -->
    <xsl:template name="body-title">
    	<img id="title-logo" alt="workspace.admin:WORKSPACE_ADMIN_LABEL_LONG" i18n:attr="alt"/>
    	<script type="text/javascript">
    		document.getElementById('title-logo').src = "<xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/>/resources/img/Admin_<i18n:text i18n:key="KERNEL_LANGUAGE_CODE" i18n:catalogue="kernel"/>.png";
    	</script>
	</xsl:template>

</xsl:stylesheet>
