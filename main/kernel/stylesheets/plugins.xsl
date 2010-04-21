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
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
    xmlns:csscomponent="org.ametys.runtime.plugins.core.ui.css.AllCSSComponent">

    <xsl:template name="plugins-load">
        <xsl:param name="scripts"/>
        <xsl:param name="css"/>
        <xsl:param name="actions"/>
        <xsl:param name="debug-mode">false</xsl:param>

		<!-- Load scripts -->
		<xsl:if test="$scripts">
	        <xsl:for-each select="$scripts">
	            <xsl:variable name="position" select="position()"/>
	            <xsl:variable name="value" select="."/>
	            
	            <!-- check that the src was not already loaded (by another plugin for example) -->
	            <xsl:if test="not($scripts[position() &lt; $position and . = $value])">
	                <script src="{$contextPath}{.}"></script>
	            </xsl:if>
	        </xsl:for-each>
		</xsl:if>

		<!-- Load css -->
		<xsl:if test="$css">
			<xsl:value-of select="csscomponent:resetCSSFilesList()"/>
	        <xsl:for-each select="$css">
	            <xsl:variable name="position" select="position()"/>
	            <xsl:variable name="value" select="."/>
	            
	            <!-- check that the src was not already loaded (by another plugin for example) -->
	            <xsl:if test="not($css[position() &lt; $position and . = $value])">
	            	<xsl:value-of select="csscomponent:addCSSFile(.)"/>
	            </xsl:if>
	        </xsl:for-each>
            <link rel="stylesheet" type="text/css" href="{$contextPath}/plugins/core/cssfilelist/{csscomponent:getHashCode()}-{$debug-mode}.css"/>
		</xsl:if>
    </xsl:template>
    
</xsl:stylesheet>