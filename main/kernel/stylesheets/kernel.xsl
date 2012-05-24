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
    xmlns:exslt="http://exslt.org/common"
    xmlns:csscomponent="org.ametys.runtime.plugins.core.ui.css.AllCSSComponent"
    xmlns:jscomponent="org.ametys.runtime.plugins.core.ui.js.AllJSComponent">
	
	<xsl:import href="kernel-browsers.xsl"/>
	
    <!-- +
         | Load and initialize all scripts for UI
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
		 | @param {String} load-cb A callback js function to call after each js load
		 | @param {Boolean} use-css-component True to load CSS using the css component to minimize css calls (true is the default value)
		 | @param {Boolean} reuse-css-component True to avoid css component initialization and css tag creation (false is the default value). Usefull if you want to add others css file to the minimizer, but you will need to call the resetCSS and the link tag by your own. Only used when use-css-component is to true. 
		 | @param {Boolean} use-js-component The same as use-css-component for js files
		 | @param {Boolean} reuse-js-component The same as reuse-css-component for js files 
         + -->
    <xsl:template name="kernel-base">
        <xsl:param name="plugins-direct-prefix"/>
        <xsl:param name="plugins-wrapped-prefix"/>
        <xsl:param name="debug-mode" select="false()"/>
		<xsl:param name="context-path"/>
		<xsl:param name="workspace-name"/>
		<xsl:param name="workspace-prefix"/>
		<xsl:param name="max-upload-size"/>
		<xsl:param name="language-code"><i18n:text i18n:key='KERNEL_LANGUAGE_CODE' i18n:catalogue='kernel'/></xsl:param>
		<xsl:param name="authorized-browsers">undefined</xsl:param>

        <xsl:param name="load-cb"/>
        <xsl:param name="use-css-component">true</xsl:param>
        <xsl:param name="reuse-css-component">false</xsl:param>
        <xsl:param name="use-js-component">true</xsl:param>
        <xsl:param name="reuse-js-component">false</xsl:param>

	
		<xsl:call-template name="kernel-browsers">
			<xsl:with-param name="authorized-browsers" select="$authorized-browsers"/>
			<xsl:with-param name="context-path" select="$context-path"/>
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
       		<script>/plugins/extjs4/resources/js/ext-all<xsl:if test="$debug-mode">-debug</xsl:if>.js</script>
       		<script>/plugins/extjs4/resources/ux/js/form/MultiSelect.js</script>
       		<script>/plugins/extjs4/resources/ux/js/form/ItemSelector.js</script>

			<script>/kernel/resources/js/Ametys.i18n.js</script>
	        <script>/kernel/resources/js/Ametys/window/DialogBox.js</script>
	        <script>/kernel/resources/js/Ametys/form/AbstractFieldWrapper.js</script>
	        <script>/kernel/resources/js/Ametys/form/field/DateTime.js</script>
	        <script>/kernel/resources/js/Ametys/form/field/ChangePassword.i18n.js</script>
	        <script>/kernel/resources/js/Ametys/data/ServerComm.i18n.js</script>
	        <script>/kernel/resources/js/Ametys/data/ServerComm/TimeoutDialog.i18n.js</script>
	        <script>/kernel/resources/js/Ametys/log/Logger.js</script>
	        <script>/kernel/resources/js/Ametys/log/Logger/Entry.js</script>
	        <script>/kernel/resources/js/Ametys/log/ErrorDialog.i18n.js</script>
	    </xsl:variable>
		
		<xsl:variable name="css">
			<css>/kernel/resources/css/gray/all.css</css>
		</xsl:variable>

		<xsl:call-template name="kernel-load">
			<xsl:with-param name="scripts" select="exslt:node-set($scripts)/*"/>
			<xsl:with-param name="css" select="exslt:node-set($css)/*"/>
			<xsl:with-param name="debug-mode" select="$debug-mode"/>
			<xsl:with-param name="context-path" select="$context-path"/>
			<xsl:with-param name="plugins-direct-prefix" select="$plugins-direct-prefix"/>
			<xsl:with-param name="workspace-prefix" select="$workspace-prefix"/>
			<xsl:with-param name="load-cb" select="$load-cb"/>
			<xsl:with-param name="use-css-component" select="$use-css-component"/>
			<xsl:with-param name="reuse-css-component" select="$reuse-css-component"/>
			<xsl:with-param name="use-js-component" select="$use-js-component"/>
			<xsl:with-param name="reuse-js-component" select="$reuse-js-component"/>
		</xsl:call-template>
    </xsl:template>
 
    <!-- +
         | Load CSS and JS files. This template will ensure that each file is called once only.
         | @param {Node} scripts JS files to load . The node is a list of nodes with file url as text value. The url is relative to the server and should not contains context path.
         | @param {Node} css The same as scripts but for css files.
         | @param {Boolean} debug-mode Load JS files in debug mode when available.
         | @param {String} context-path The application context path. Can be empty for ROOT context path or should begin with / in other cases. E.g. '/MyContext'
         | @param {String} plugins-direct-prefix Prefix for direct url to plugins (used for AJAX connections) with leading '/' nor context path. e.g. '/_plugins' 
		 | @param {String} load-cb A callback js function to call after each js load
		 | @param {Boolean} use-css-component True to load CSS using the css component to minimize css calls (true is the default value)
		 | @param {Boolean} reuse-css-component True to avoid css component initialization and css tag creation (false is the default value). Usefull if you want to add others css file to the minimizer, but you will need to call the resetCSS and the link tag by your own. Only used when use-css-component is to true. 
		 | @param {Boolean} use-js-component The same as use-css-component for js files
		 | @param {Boolean} reuse-js-component The same as reuse-css-component for js files 
         + -->
    <xsl:template name="kernel-load">
        <xsl:param name="scripts"/>
        <xsl:param name="css"/>
        
        <xsl:param name="debug-mode">false</xsl:param>
		<xsl:param name="context-path"/>
        <xsl:param name="plugins-direct-prefix"/>
        <xsl:param name="workspace-prefix"/>
        
		<xsl:param name="load-cb"/>
        <xsl:param name="use-css-component">true</xsl:param>
		<xsl:param name="reuse-css-component">false</xsl:param>
		<xsl:param name="use-js-component">true</xsl:param>
        <xsl:param name="reuse-js-component">false</xsl:param>
		 
		<xsl:variable name="plugin-core-url" select="concat($context-path, $workspace-prefix, $plugins-direct-prefix, '/core')"/>
		 
		<!-- Load scripts -->
		<xsl:if test="$scripts">
			<xsl:choose>
				<xsl:when test="$use-js-component != 'true'">
				        <xsl:for-each select="$scripts">
				            <xsl:variable name="position" select="position()"/>
				            <xsl:variable name="value" select="."/>
				            
				            <!-- check that the src was not already loaded (by another plugin for example) -->
				            <xsl:if test="not($scripts[position() &lt; $position and . = $value])">
				            	<script type="text/javascript" src="{$contextPath}{.}"/>
				        	    <xsl:copy-of select="$load-cb"/>
				            </xsl:if>
				        </xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
						<xsl:if test="$reuse-js-component = 'false'">
							<xsl:value-of select="jscomponent:resetJSFilesList()"/>
						</xsl:if>
				        <xsl:for-each select="$scripts">
				            <xsl:variable name="position" select="position()"/>
				            <xsl:variable name="value" select="."/>
				            
				            <!-- check that the src was not already loaded (by another plugin for example) -->
				            <xsl:if test="not($scripts[position() &lt; $position and . = $value])">
				            	<xsl:value-of select="jscomponent:addJSFile(.)"/>
				            </xsl:if>
				        </xsl:for-each>
						<xsl:if test="$reuse-js-component = 'false'">
			            	<script type="text/javascript" src="{$plugin-core-url}/jsfilelist/{jscomponent:getHashCode()}-{$debug-mode}.js"/>
			        	    <xsl:copy-of select="$load-cb"/>
			        	</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>

		<!-- Load css -->
		<xsl:if test="$css">
			<xsl:choose>
				<xsl:when test="$use-css-component != 'true'">
				        <xsl:for-each select="$css">
				            <xsl:variable name="position" select="position()"/>
				            <xsl:variable name="value" select="."/>
				            
				            <!-- check that the src was not already loaded (by another plugin for example) -->
				            <xsl:if test="not($css[position() &lt; $position and . = $value])">
				                <link rel="stylesheet" type="text/css" href="{$contextPath}{.}"/>
				        	    <xsl:copy-of select="$load-cb"/>
				            </xsl:if>
				        </xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
						<xsl:if test="$reuse-css-component = 'false'">
							<xsl:value-of select="csscomponent:resetCSSFilesList()"/>
						</xsl:if>
				        <xsl:for-each select="$css">
				            <xsl:variable name="position" select="position()"/>
				            <xsl:variable name="value" select="."/>
				            
				            <!-- check that the src was not already loaded (by another plugin for example) -->
				            <xsl:if test="not($css[position() &lt; $position and . = $value])">
				            	<xsl:value-of select="csscomponent:addCSSFile(.)"/>
				            </xsl:if>
				        </xsl:for-each>
						<xsl:if test="$reuse-css-component = 'false'">
							<xsl:call-template name="kernel-base-load-css">
								<xsl:with-param name="load-cb" select="$load-cb"/>
								<xsl:with-param name="debug-mode" select="$debug-mode"/>
							    <xsl:with-param name="plugin-core-url" select="$plugin-core-url"/>
							</xsl:call-template>
			        	</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
    </xsl:template>
    
    <!-- +
	     | @private
	     | css load is not just a call to a link tag (as it is for js), because we need to cut it in several files for IE 
	     + -->
    <xsl:template name="kernel-base-load-css">
        <xsl:param name="load-cb"/>
        <xsl:param name="debug-mode"/>
        <xsl:param name="plugin-core-url"/>
        
        	<xsl:choose>
        		<xsl:when test="$debug-mode = 'true'">
        			<xsl:variable name="hashcode" select="csscomponent:getHashCode()"/>
        		
				    <xsl:call-template name="kernel-base-load-css-recurse">
					    <xsl:with-param name="max" select="csscomponent:getNumberOfParts($hashcode)"/>
					    <xsl:with-param name="hashcode" select="$hashcode"/>
					    <xsl:with-param name="plugin-core-url" select="$plugin-core-url"/>
				    </xsl:call-template>
				    
					<xsl:copy-of select="$load-cb"/>
        		</xsl:when>
        		<xsl:otherwise>
		        	<link rel="stylesheet" type="text/css" href="{$plugin-core-url}/cssfilelist/{csscomponent:getHashCode()}.css"/>
					<xsl:copy-of select="$load-cb"/>
        		</xsl:otherwise>
        	</xsl:choose>
	</xsl:template>

	<!-- +
	     | @private
	     + -->
    <xsl:template name="kernel-base-load-css-recurse">
        <xsl:param name="num" select="0"/>
        <xsl:param name="max"/>
        <xsl:param name="hashcode"/>
        <xsl:param name="plugin-core-url"/>
    
    	<xsl:if test="$num &lt; $max">
        	<link rel="stylesheet" type="text/css" href="{$plugin-core-url}/cssfilelist/debug/{$hashcode}/{$num}.css"/>
		    <xsl:call-template name="kernel-base-load-css-recurse">
			    <xsl:with-param name="num" select="$num + 1"/>
			    <xsl:with-param name="max" select="$max"/>
			    <xsl:with-param name="hashcode" select="$hashcode"/>
			    <xsl:with-param name="plugin-core-url" select="$plugin-core-url"/>
		    </xsl:call-template>
        </xsl:if>
    </xsl:template>    
</xsl:stylesheet>
