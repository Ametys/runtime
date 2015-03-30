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
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
    xmlns:exslt="http://exslt.org/common"
    xmlns:ametys="org.ametys.runtime.plugins.core.ui.AmetysXSLTHelper">
	
	<xsl:import href="kernel-browsers.xsl"/>
	
    <!-- +
         | Load and initialize all scripts for UI
         | @param {String} plugins-direct-prefix=/plugins Prefix for direct url to plugins (used for AJAX connections) with leading '/' nor context path. e.g. '/plugins' 
         | @param {String} plugins-wrapped-prefix=/_plugins Prefix for wrapped url to plugins (used for redirections) with leading '/' nor context path. e.g. '/_plugins'
         | @param {String} authorized-browsers Optionnaly, you can provide such a string :
		 | 					{
  		 |                     'supported': { 'ie': '8-0', 'ff': '12-0', 'sa': '6-0', 'ch': '18-0', 'op': '12-0' },         // List of supported browsers with their versions
		 | 					   'not-supported': { 'ie': '0-7', 'ff': '0-11', 'sa': '0-5', 'ch': '0-17', 'op': '0-11' },	    // List of non supported browsers with their versions
 		 | 					   'warning-redirection': "browser-warning.html",			                                    // Redirection when the browser may be supported
		 | 					   'failure-redirection': "browser-failure.html"                                                // Redirection when the browser is not supported 
		 |                  }			
		 |					If window.ametys_authorized_browsers is empty, no browser test will be done.<br/>
 		 | 					When a supported browser is detected, everything goes on normally.<br/>
		 | 					When a not-supported browser is detected, the application is redirected to the failure rediction url. Where a message should indicates to use a supported browser.<br/>
		 | 					When another browser is detected, the application is redirected to the warning redirection url. Where a message should indicated to use a supported browser, but the user could enforce the navigation (setting a cookie 'ametys.accept.non.supported.navigators' to 'on').<br/>
		 |					Keys for browsers are 'ie' (Microsoft Internet Explorer), 'ff' (Mozilla Firefox), 'ch' (Google Chrome), 'sa' (Apple Safari) and 'op' (Opera).<br/>
		 |					Versions can be a single number '3' or '3.5', or can be an interleave where 0 is the infinity '0-6' means all versions before 6 and 6 included, '6-0' means all versions after 6 and 6 included.
		 |                  The default value displayed above is the requirements of ExtJS version.
         |
         | @param {String} theme=neptune The ExtJS theme to load
		 | @param {String} load-cb= A callback js function to call after each js/css file is loaded
         + -->
    <xsl:template name="kernel-base">
        <xsl:param name="plugins-direct-prefix">/plugins</xsl:param>
        <xsl:param name="plugins-wrapped-prefix">/_plugins</xsl:param>
		<xsl:param name="authorized-browsers">
		     {
		          'supported': { 'ie': '8-0', 'ff': '12-0', 'sa': '6-0', 'ch': '18-0', 'op': '12-0' },
		          'not-supported': { 'ie': '0-7', 'ff': '0-11', 'sa': '0-5', 'ch': '0-17', 'op': '0-11' },
		          'warning-redirection': "browser-warning.html",
		          'failure-redirection': "browser-failure.html" 
		     }
		</xsl:param>
		<xsl:param name="theme">neptune</xsl:param>
        <xsl:param name="load-cb"/>
        
		<xsl:variable name="context-path" select="ametys:uriPrefix(false)"/>
		<xsl:variable name="workspace-name" select="ametys:workspaceName()"/>
		<xsl:variable name="workspace-prefix" select="ametys:workspacePrefix()"/>
		<xsl:variable name="language-code" select="ametys:translate('kernel:KERNEL_LANGUAGE_CODE')"/>
        <xsl:variable name="rtl" select="ametys:translate('kernel:KERNEL_LANGUAGE_RTL') = 'true'"/>
		<xsl:variable name="max-upload-size" select="ametys:config('runtime.upload.max-size')"/>
        <xsl:variable name="debug-mode" select="not(ametys:config('runtime.debug.ui') = 0)"/>

	    <xsl:variable name="uxtheme">
	       <xsl:choose>
	           <xsl:when test="$theme = 'neptune'">neptune</xsl:when>
               <xsl:when test="$theme = 'neptune-touch'">neptune</xsl:when>
               <xsl:when test="$theme = 'crisp'">crisp</xsl:when>
               <xsl:when test="$theme = 'crisp-touch'">crisp</xsl:when>
               <xsl:when test="$theme = 'classic'">classic</xsl:when>
               <xsl:when test="$theme = 'classic-touch'">classic</xsl:when>
               <xsl:when test="$theme = 'gray'">classic</xsl:when>
               <xsl:when test="$theme = 'aria'">classic</xsl:when>
               <xsl:otherwise>classic</xsl:otherwise>
	       </xsl:choose>
	    </xsl:variable>
		
	
		<xsl:call-template name="kernel-browsers">
			<xsl:with-param name="authorized-browsers" select="$authorized-browsers"/>
		</xsl:call-template>
	
		<script type="text/javascript">
			// Theses options are here to initialize the Ametys object.
			// Do not use theses since their are removed during Ametys initialization process
			window.ametys_opts = {
				"plugins-direct-prefix": "<xsl:value-of select='$plugins-direct-prefix'/>",
				"plugins-wrapped-prefix": "<xsl:value-of select='$plugins-wrapped-prefix'/>",
				"debug-mode": <xsl:value-of select='$debug-mode'/>,
				"context-path": "<xsl:value-of select='$context-path'/>",
				"workspace-name": "<xsl:value-of select='$workspace-name'/>",
				"workspace-prefix": "<xsl:value-of select='$workspace-prefix'/>",
				"max-upload-size": "<xsl:value-of select='$max-upload-size'/>",
				"language-code": "<xsl:copy-of select='$language-code'/>"
			}
        </script>
        
       	<xsl:variable name="scripts">
       		<script>/plugins/extjs5/resources/ext-all<xsl:if test="$rtl">-rtl</xsl:if><xsl:if test="$debug-mode">-debug</xsl:if>.js</script>
            <script>/plugins/extjs5/resources/packages/ext-locale/build/ext-locale-<xsl:value-of select="$language-code"/><xsl:if test="$debug-mode">-debug</xsl:if>.js</script>
            
            <xsl:if test="$theme = 'aria'">
                <script>/plugins/extjs5/resources/packages/ext-aria/build/ext-aria<xsl:if test="$debug-mode">-debug</xsl:if>.js</script>
            </xsl:if>
            <script>/plugins/extjs5/resources/packages/ext-theme-<xsl:value-of select="$theme"/>/build/ext-theme-<xsl:value-of select="$theme"/><xsl:if test="$debug-mode">-debug</xsl:if>.js</script>

            <script>/plugins/extjs5/resources/packages/ext-ux/build/ext-ux<xsl:if test="$debug-mode">-debug</xsl:if>.js</script>
            
<!-- 			<script>/kernel/resources/js/Ext.fixes.js</script> -->
<!--             <script>/kernel/resources/js/Ext.enhancements.js</script> -->

			<script>/kernel/resources/js/Ametys.js</script>
			
	        <script>/kernel/resources/js/Ametys/log/Logger/Entry.js</script>
            <script>/kernel/resources/js/Ametys/log/LoggerFactory.js</script>
			<script>/kernel/resources/js/Ametys/log/Logger.js</script>
	        <script>/kernel/resources/js/Ametys/log/ErrorDialog.js</script>
	        
	        <script>/kernel/resources/js/Ametys/window/DialogBox.js</script>
            <script>/kernel/resources/js/Ametys/window/MessageBox.js</script>
            
	        <script>/kernel/resources/js/Ametys/form/AbstractField.js</script>
	        <script>/kernel/resources/js/Ametys/form/AbstractFieldsWrapper.js</script>
	        <script>/kernel/resources/js/Ametys/form/field/DateTime.js</script>
	        <script>/kernel/resources/js/Ametys/form/field/ChangePassword.js</script>
            <script>/kernel/resources/js/Ametys/form/field/ReferencedNumberField.js</script>
            <script>/kernel/resources/js/Ametys/form/field/RichText.js</script>
            <script>/kernel/resources/js/Ametys/form/field/Code.js</script>
            
	        <script>/kernel/resources/js/Ametys/data/ServerComm.js</script>
            <script>/kernel/resources/js/Ametys/data/ServerCommProxy.js</script>
	        <script>/kernel/resources/js/Ametys/data/ServerComm/TimeoutDialog.js</script>
	        
<!-- 	        <script>/kernel/resources/js/Ametys/tree/TreeEditor.js</script> -->
<!--             <script>/kernel/resources/js/Ametys/grid/plugin/Multisort.js</script> -->

<!--             <script>/kernel/resources/js/Ametys/ui/fluent/tip/Tooltip.js</script> -->

<!--             <script>/kernel/resources/js/Ametys/ui/fluent/ribbon/Ribbon.js</script> -->
<!--             <script>/kernel/resources/js/Ametys/ui/fluent/ribbon/TabPanel.js</script> -->
<!--             <script>/kernel/resources/js/Ametys/ui/fluent/ribbon/TabPanel/Header.js</script> -->
<!--             <script>/kernel/resources/js/Ametys/ui/fluent/ribbon/Panel.js</script> -->
<!--             <script>/kernel/resources/js/Ametys/ui/fluent/ribbon/Group.js</script> -->
<!--             <script>/kernel/resources/js/Ametys/ui/fluent/ribbon/GroupPart.js</script> -->
<!--             <script>/kernel/resources/js/Ametys/ui/fluent/ribbon/controls/Button.js</script> -->
<!--             <script>/kernel/resources/js/Ametys/ui/fluent/ribbon/controls/Toolbar.js</script> -->
<!--             <script>/kernel/resources/js/Ametys/ui/fluent/ribbon/controls/gallery/MenuGallery.js</script> -->
<!--             <script>/kernel/resources/js/Ametys/ui/fluent/ribbon/controls/gallery/MenuGalleryButton.js</script>       -->
	    </xsl:variable>
	    
		<xsl:variable name="css">
            <css>/plugins/extjs5/resources/packages/ext-theme-<xsl:value-of select="$theme"/>/build/resources/ext-theme-<xsl:value-of select="$theme"/>-all<xsl:if test="$rtl">-rtl</xsl:if><xsl:if test="$debug-mode">-debug</xsl:if>.css</css>
            <css>/plugins/extjs5/resources/packages/ext-ux//build/<xsl:value-of select="$uxtheme"/>/resources/ext-ux-all<xsl:if test="$rtl">-rtl</xsl:if><xsl:if test="$debug-mode">-debug</xsl:if>.css</css>
            <css>/kernel/resources/css/Ametys/gray/all.css</css>
            <css>/kernel/resources/css/ametys.css</css>
		</xsl:variable>

		<xsl:call-template name="kernel-load">
			<xsl:with-param name="scripts" select="exslt:node-set($scripts)/*"/>
			<xsl:with-param name="css" select="exslt:node-set($css)/*"/>
			<xsl:with-param name="load-cb" select="$load-cb"/>
		</xsl:call-template>
    </xsl:template>
 
    <!-- +
         | Load CSS and JS files. This template will ensure that each file is called once only.
         | @param {Node} scripts JS files to load . The node is a list of nodes with file url as text value. The url is relative to the server and should not contains context path.
         | @param {Node} css The same as scripts but for css files.
		 | @param {String} load-cb A callback js function to call after each js/css file is loaded
         + -->
    <xsl:template name="kernel-load">
        <xsl:param name="scripts"/>
        <xsl:param name="css"/>
        
		<xsl:param name="load-cb"/>
        
        <xsl:variable name="contextPath" select="ametys:uriPrefix(false)"/>
		 
		<!-- Load scripts -->
		<xsl:if test="$scripts">
	        <xsl:for-each select="$scripts">
	            <xsl:variable name="position" select="position()"/>
	            <xsl:variable name="value" select="."/>
	            
	            <!-- check that the src was not already loaded (by another plugin for example) -->
	            <xsl:if test="not($scripts[position() &lt; $position and . = $value])">
	            	<script type="text/javascript" src="{$contextPath}{.}"/>
	        	    <xsl:copy-of select="$load-cb"/>
	            </xsl:if>
	        </xsl:for-each>
	    </xsl:if>

		<!-- Load css -->
        <xsl:if test="$css">
	        <xsl:for-each select="$css">
	            <xsl:variable name="position" select="position()"/>
	            <xsl:variable name="value" select="."/>
	            
	            <!-- check that the src was not already loaded (by another plugin for example) -->
	            <xsl:if test="not($css[position() &lt; $position and . = $value])">
	                <link rel="stylesheet" type="text/css" href="{$contextPath}{.}"/>
	        	    <xsl:copy-of select="$load-cb"/>
	            </xsl:if>
	        </xsl:for-each>
	   </xsl:if>
    </xsl:template>
</xsl:stylesheet>
