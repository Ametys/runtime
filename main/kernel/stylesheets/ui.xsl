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
    xmlns:exslt="http://exslt.org/common"
    xmlns:csscomponent="org.ametys.runtime.plugins.core.ui.css.AllCSSComponent"
    xmlns:jscomponent="org.ametys.runtime.plugins.core.ui.js.AllJSComponent">

	<xsl:param name="max-upload-size"/>

    <!-- +
         | Load and initialize all scripts for UI
         + -->
    <xsl:template name="ui-load">
        <xsl:param name="pluginsDirectContext"/>
        <xsl:param name="pluginsWrappedContext"/>
        
        <script type="text/javascript">
            <xsl:comment>
                /* Load context */
                var context = {};
                context.contextPath = "<xsl:value-of select="$contextPath"/>";
                context.workspaceName = "<xsl:value-of select="$workspaceName"/>";
                context.workspaceContext = "<xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/>";
                context.maxUploadSize = "<xsl:value-of select="$max-upload-size"/>";
                
                function getPluginDirectUrl(plugin)
                {
                    return "<xsl:value-of select="$pluginsDirectContext"/>/" + plugin;
                }
    
                function getPluginResourcesUrl(plugin)
                {
                    return context.contextPath + "/plugins/" + plugin + "/resources";
                }
    
                function getPluginWrappedUrl(plugin)
                {
                    return "<xsl:value-of select="$pluginsWrappedContext"/>/" + plugin;
                }
      
                function runtimeRedirectTo(link, mode, plugin)
                {
                    function _checkLink (link)
                    {
                        if (link.substring(0, 1) != '/')
                        {
                            link = '/' + link;
                        }
                        return link;
                    }
    			  	
                    switch (mode)
                    {
                        case 'absolute': 
                            break;
                        case 'context':
                            link = context.contextPath + _checkLink(link);
                            break;
                        case 'workspace':
                            link = context.workspaceContext + _checkLink(link);
                            break;
                        case 'plugin-direct':
                            link = getPluginDirectUrl(plugin) + _checkLink(link);
                            break;
                        case 'plugin-wrapped':
                        default:
                            link = getPluginWrappedUrl(plugin) + _checkLink(link);
                    }
    			
                    window.location.href = link;
                }
            </xsl:comment>
        </script>
    </xsl:template>
    
    <xsl:template name="ui-tools-load">
        <xsl:param name="bad-navigator-redirection"/>
        <xsl:param name="accept-ie-6">false</xsl:param>
        <xsl:param name="accept-ie-7">false</xsl:param>
        <xsl:param name="accept-ie-8">false</xsl:param>
        <xsl:param name="accept-ie-9">false</xsl:param>
        <xsl:param name="accept-ff-1.0">false</xsl:param>
        <xsl:param name="accept-ff-1.5">false</xsl:param>
        <xsl:param name="accept-ff-2.0">false</xsl:param>
        <xsl:param name="accept-ff-3.0">false</xsl:param>
        <xsl:param name="accept-ff-3.5">false</xsl:param>
        <xsl:param name="accept-ff-3.6">false</xsl:param>
        <xsl:param name="accept-ff-4.0">false</xsl:param>
        <xsl:param name="accept-ff-5.0">false</xsl:param>
        <xsl:param name="accept-ff-6.0">false</xsl:param>
        <xsl:param name="accept-ff-7.0">false</xsl:param>
        <xsl:param name="accept-ff-8.0">false</xsl:param>
        <xsl:param name="accept-ff-9.0">false</xsl:param>
        <xsl:param name="accept-ff-10.0">false</xsl:param>
        <xsl:param name="accept-ff-11.0">false</xsl:param>
        <xsl:param name="accept-ff-12.0">false</xsl:param>
        <xsl:param name="accept-ff-13.0">false</xsl:param>
        <xsl:param name="accept-ff-14.0">false</xsl:param>
        <xsl:param name="accept-ff-15.0">false</xsl:param>
        <xsl:param name="accept-ff-16.0">false</xsl:param>
        <xsl:param name="accept-ff-17.0">false</xsl:param>
        <xsl:param name="accept-sa-3">false</xsl:param>
        <xsl:param name="accept-sa-4">false</xsl:param>
        <xsl:param name="accept-sa-5.0">false</xsl:param>
        <xsl:param name="accept-sa-5.1">false</xsl:param>
        <xsl:param name="accept-op-9">false</xsl:param>
        <xsl:param name="accept-op-10">false</xsl:param>
        <xsl:param name="accept-op-11">false</xsl:param>
        <xsl:param name="accept-ch-1">false</xsl:param>
        <xsl:param name="accept-ch-2">false</xsl:param>
        <xsl:param name="accept-ch-3">false</xsl:param>
        <xsl:param name="accept-ch-4">false</xsl:param>
        <xsl:param name="accept-ch-5">false</xsl:param>
        <xsl:param name="accept-ch-6">false</xsl:param>
        <xsl:param name="accept-ch-7">false</xsl:param>
        <xsl:param name="accept-ch-8">false</xsl:param>
        <xsl:param name="accept-ch-9">false</xsl:param>
        <xsl:param name="accept-ch-10">false</xsl:param>
        <xsl:param name="accept-ch-11">false</xsl:param>
        <xsl:param name="accept-ch-12">false</xsl:param>
        <xsl:param name="accept-ch-13">false</xsl:param>
        <xsl:param name="accept-ch-14">false</xsl:param>
        <xsl:param name="accept-ch-15">false</xsl:param>
        <xsl:param name="accept-ch-16">false</xsl:param>
        <xsl:param name="accept-ch-17">false</xsl:param>
        <xsl:param name="accept-ch-18">false</xsl:param>
        <xsl:param name="accept-ch-19">false</xsl:param>
        <xsl:param name="accept-ch-20">false</xsl:param>
        <xsl:param name="accept-ch-21">false</xsl:param>
        <xsl:param name="accept-ch-22">false</xsl:param>
        <xsl:param name="accept-ch-23">false</xsl:param>
        <xsl:param name="debug-mode">false</xsl:param>
        <xsl:param name="load-cb"/>
        <xsl:param name="use-css-component">true</xsl:param>
        <xsl:param name="reuse-css-component">false</xsl:param>
        <xsl:param name="use-js-component">true</xsl:param>
        <xsl:param name="reuse-js-component">false</xsl:param>
		
		<xsl:variable name="extjs-debug-suffix"><xsl:if test="$debug-mode = 'true'">-debug</xsl:if></xsl:variable>
		<xsl:variable name="scripts-to-load-raw">
			<script>/kernel/resources/js/mozxpath.js</script>
	    	<script>/plugins/extjs/resources/js/adapter/ext/ext-base<xsl:value-of select="$extjs-debug-suffix"/>.js</script>
			<script>/plugins/extjs/resources/js/ext-all<xsl:value-of select="$extjs-debug-suffix"/>.js</script>
	        <script>/kernel/resources/js/org/ametys/runtime/Runtime_InteractionActionLibrary.js</script>
	        <script>/kernel/resources/js/org/ametys/DialogBox.js</script>
	        <script>/kernel/resources/js/org/ametys/ListView.js</script>
	        <script>/kernel/resources/js/org/ametys/EditorListView.js</script>
	        <script>/kernel/resources/js/org/ametys/Tree.js</script>
	        <script>/kernel/resources/js/org/ametys/Utility.js</script>
	        <script>/kernel/resources/js/org/ametys/Ext-param.i18n.js</script>
	        <script>/kernel/resources/js/org/ametys/HtmlContainer.js</script>
	 		<script>/plugins/extjs/resources/ux/js/XmlTreeLoader.js</script>
	        <script>/plugins/extjs/resources/ux/js/fileuploadfield/FileUploadField.js</script>
	        <script>/plugins/extjs/resources/ux/js/MultiSelect.js</script>
	        <script>/kernel/resources/js/org/ametys/form/TextField.js</script>
	        <script>/kernel/resources/js/org/ametys/form/TextAreaField.js</script>
	        <script>/kernel/resources/js/org/ametys/form/LongField.js</script>
	        <script>/kernel/resources/js/org/ametys/form/DoubleField.js</script>
	        <script>/kernel/resources/js/org/ametys/form/BooleanField.js</script>
	        <script>/kernel/resources/js/org/ametys/form/PasswordField.js</script>
	        <script>/kernel/resources/js/org/ametys/form/PasswordCreationField.i18n.js</script>
	        <script>/kernel/resources/js/org/ametys/form/DateField.js</script>
	        <script>/kernel/resources/js/org/ametys/form/TimeField.js</script>
	        <script>/kernel/resources/js/org/ametys/form/ComboField.js</script>
	        <script>/kernel/resources/js/org/ametys/form/RadioGroupField.js</script>
	        <script>/kernel/resources/js/org/ametys/form/MultiSelectField.js</script>
	        <script>/kernel/resources/js/org/ametys/form/FileUploadField.i18n.js</script>
	 		<script>/kernel/resources/js/org/ametys/tree/XmlTreeLoader.js</script>
			<script>/kernel/resources/js/org/ametys/servercomm/TimeoutDialog.i18n.js</script>
			<script>/kernel/resources/js/org/ametys/servercomm/DirectComm.i18n.js</script>
			<script>/kernel/resources/js/org/ametys/servercomm/ServerComm.i18n.js</script>
			<script>/kernel/resources/js/org/ametys/servercomm/ServerMessage.js</script>
			<script>/kernel/resources/js/org/ametys/log/LoggerManager.js</script>
			<script>/kernel/resources/js/org/ametys/log/LoggerEntry.js</script>
			<script>/kernel/resources/js/org/ametys/msg/ErrorDialog.i18n.js</script>
			<script>/kernel/resources/js/org/ametys/msg/Mask.i18n.js</script>
		</xsl:variable>
		<xsl:variable name="scripts-to-load" select="exslt:node-set($scripts-to-load-raw)"/>

        <script type="text/javascript">
            <xsl:comment>
            	var userAgent = navigator.userAgent.toLowerCase();
                <xsl:text>if (!(</xsl:text>
                    <xsl:if test="$accept-ie-6   = 'true'">(userAgent.indexOf("msie 6") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ie-7   = 'true'">(userAgent.indexOf("msie 7") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ie-8   = 'true'">(userAgent.indexOf("msie 8") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ie-9   = 'true'">(userAgent.indexOf("msie 9") >= 0) ||</xsl:if>
                    
                    <xsl:if test="$accept-ff-1.0 = 'true'">(userAgent.indexOf("firefox/1.0") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-1.5 = 'true'">(userAgent.indexOf("firefox/1.5") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-2.0 = 'true'">(userAgent.indexOf("firefox/2.0") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-3.0 = 'true'">(userAgent.indexOf("firefox/3.0") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-3.5 = 'true'">(userAgent.indexOf("firefox/3.5") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-3.6 = 'true'">(userAgent.indexOf("firefox/3.6") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-4.0 = 'true'">(userAgent.indexOf("firefox/4.0") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-5.0 = 'true'">(userAgent.indexOf("firefox/5.0") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-6.0 = 'true'">(userAgent.indexOf("firefox/6.0") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-7.0 = 'true'">(userAgent.indexOf("firefox/7.0") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-8.0 = 'true'">(userAgent.indexOf("firefox/8.0") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-9.0 = 'true'">(userAgent.indexOf("firefox/9.0") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-10.0 = 'true'">(userAgent.indexOf("firefox/10.0") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-11.0 = 'true'">(userAgent.indexOf("firefox/11.0") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-12.0 = 'true'">(userAgent.indexOf("firefox/12.0") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-13.0 = 'true'">(userAgent.indexOf("firefox/13.0") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-14.0 = 'true'">(userAgent.indexOf("firefox/14.0") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-15.0 = 'true'">(userAgent.indexOf("firefox/15.0") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-16.0 = 'true'">(userAgent.indexOf("firefox/16.0") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ff-17.0 = 'true'">(userAgent.indexOf("firefox/17.0") >= 0) ||</xsl:if>
                    
                    <xsl:if test="$accept-sa-3   = 'true'">(userAgent.indexOf("safari/") >= 0 &amp;&amp; userAgent.indexOf("version/3.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-sa-4   = 'true'">(userAgent.indexOf("safari/") >= 0 &amp;&amp; userAgent.indexOf("version/4.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-sa-5.0 = 'true'">(userAgent.indexOf("safari/") >= 0 &amp;&amp; userAgent.indexOf("version/5.0") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-sa-5.1 = 'true'">(userAgent.indexOf("safari/") >= 0 &amp;&amp; userAgent.indexOf("version/5.1") >= 0) ||</xsl:if>
                    
                    <xsl:if test="$accept-op-9   = 'true'">(userAgent.indexOf("opera/9.") >= 0 &amp;&amp; userAgent.indexOf("version/10.") == -1) ||</xsl:if>
                    <xsl:if test="$accept-op-10  = 'true'">(userAgent.indexOf("opera/9.8") >= 0 &amp;&amp; userAgent.indexOf("version/10.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-op-11  = 'true'">(userAgent.indexOf("opera/9.8") >= 0 &amp;&amp; userAgent.indexOf("version/11.") >= 0) ||</xsl:if>
                    
                    <xsl:if test="$accept-ch-1   = 'true'">(userAgent.indexOf("chrome/1.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-2   = 'true'">(userAgent.indexOf("chrome/2.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-3   = 'true'">(userAgent.indexOf("chrome/3.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-4   = 'true'">(userAgent.indexOf("chrome/4.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-5   = 'true'">(userAgent.indexOf("chrome/5.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-6   = 'true'">(userAgent.indexOf("chrome/6.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-7   = 'true'">(userAgent.indexOf("chrome/7.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-8   = 'true'">(userAgent.indexOf("chrome/8.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-9   = 'true'">(userAgent.indexOf("chrome/9.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-10  = 'true'">(userAgent.indexOf("chrome/10.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-11  = 'true'">(userAgent.indexOf("chrome/11.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-12  = 'true'">(userAgent.indexOf("chrome/12.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-13  = 'true'">(userAgent.indexOf("chrome/13.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-14  = 'true'">(userAgent.indexOf("chrome/14.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-15  = 'true'">(userAgent.indexOf("chrome/15.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-16  = 'true'">(userAgent.indexOf("chrome/16.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-17  = 'true'">(userAgent.indexOf("chrome/17.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-18  = 'true'">(userAgent.indexOf("chrome/18.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-19  = 'true'">(userAgent.indexOf("chrome/19.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-20  = 'true'">(userAgent.indexOf("chrome/20.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-21  = 'true'">(userAgent.indexOf("chrome/21.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-22  = 'true'">(userAgent.indexOf("chrome/22.") >= 0) ||</xsl:if>
                    <xsl:if test="$accept-ch-23  = 'true'">(userAgent.indexOf("chrome/23.") >= 0) ||</xsl:if>
                    <xsl:text>1 == 0))</xsl:text>
                {
            		<!-- Check the cookie for forcing non supported navigators -->
                    var matcher = document.cookie.match("(^|;) ?ametys\.accept\.non\.supported\.navigators=([^;]*)");
                    if (!matcher || matcher[2] != "on")
                    {
                        window.location.href = "<xsl:value-of select="$bad-navigator-redirection"/>?uri=" + encodeURIComponent(window.location.href);
                    }
                }
            </xsl:comment>
        </script>

		<!-- LOAD JS -->
		<xsl:choose>
			<xsl:when test="$use-css-component != 'true'">
					<xsl:for-each select="$scripts-to-load/script">
						<script src="{$contextPath}{.}"/>
						<xsl:copy-of select="$load-cb"/>
			      	</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
					<xsl:if test="$reuse-js-component = 'false'">
						<xsl:value-of select="jscomponent:resetJSFilesList()"/>
					</xsl:if>
					
					<xsl:for-each select="$scripts-to-load/script">
			      		<xsl:value-of select="jscomponent:addJSFile(.)"/>
			      	</xsl:for-each>

					<xsl:if test="$reuse-js-component = 'false'">
				        <script type="text/javascript" src="{$contextPath}{$workspaceURI}/plugins/core/jsfilelist/{jscomponent:getHashCode()}-{$debug-mode}.js"></script>
						<xsl:copy-of select="$load-cb"/>
					</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
		
		<!--  LOAD CSS -->		
		<xsl:choose>
			<xsl:when test="$use-css-component != 'true'">
					<link rel="stylesheet" href="{$contextPath}/kernel/resources/css/all.css" type="text/css"/>
					<xsl:copy-of select="$load-cb"/>
			</xsl:when>
			<xsl:otherwise>
					<xsl:if test="$reuse-css-component = 'false'">
						<xsl:value-of select="csscomponent:resetCSSFilesList()"/>
					</xsl:if>

			      	<xsl:value-of select="csscomponent:addCSSFile('/kernel/resources/css/all.css')"/>
			      	
					<xsl:if test="$reuse-css-component = 'false'">
						<xsl:call-template name="ui-load-css">
							<xsl:with-param name="load-cb" select="$load-cb"/>
							<xsl:with-param name="debug-mode" select="$debug-mode"/>
						</xsl:call-template>
					</xsl:if>
			</xsl:otherwise>
		</xsl:choose>

    </xsl:template>

    <xsl:template name="ui-load-css">
        <xsl:param name="load-cb"/>
        <xsl:param name="debug-mode"/>
        
        	<xsl:choose>
        		<xsl:when test="$debug-mode = 'true'">
        			<xsl:variable name="hashcode" select="csscomponent:getHashCode()"/>
        		
				    <xsl:call-template name="ui-load-css-recurse">
					    <xsl:with-param name="max" select="csscomponent:getNumberOfParts($hashcode)"/>
					    <xsl:with-param name="hashcode" select="$hashcode"/>
				    </xsl:call-template>
				    
					<xsl:copy-of select="$load-cb"/>
        		</xsl:when>
        		<xsl:otherwise>
		        	<link rel="stylesheet" type="text/css" href="{$contextPath}{$workspaceURI}/plugins/core/cssfilelist/{csscomponent:getHashCode()}.css"/>
					<xsl:copy-of select="$load-cb"/>
        		</xsl:otherwise>
        	</xsl:choose>
        
	</xsl:template>

    <xsl:template name="ui-load-css-recurse">
        <xsl:param name="num" select="0"/>
        <xsl:param name="max"/>
        <xsl:param name="hashcode"/>
    
    	<xsl:if test="$num &lt; $max">
        	<link rel="stylesheet" type="text/css" href="{$contextPath}{$workspaceURI}/plugins/core/cssfilelist/debug/{$hashcode}/{$num}.css"/>
		    <xsl:call-template name="ui-load-css-recurse">
			    <xsl:with-param name="num" select="$num + 1"/>
			    <xsl:with-param name="max" select="$max"/>
			    <xsl:with-param name="hashcode" select="$hashcode"/>
		    </xsl:call-template>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>