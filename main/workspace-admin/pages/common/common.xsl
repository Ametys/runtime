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
  		    	'supported': { 'ie' : '7-9', ff: '3.6-16', ch: '10-24', op: '11-11.9', sa: '5.0-5.1'},
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
	     | Returns value for the home template, parameter head-meta
	     | @private
	     + -->
    <xsl:template name="head-meta">
    	<xsl:call-template name="kernel-load">
            <xsl:with-param name="scripts" select="/*/Desktop/category/DesktopItem/scripts/file"/>
            <xsl:with-param name="css" select="/*/Desktop/category/DesktopItem/css/file"/>
            <xsl:with-param name="use-css-component">false</xsl:with-param>
            <xsl:with-param name="use-js-component">false</xsl:with-param>
            <xsl:with-param name="debug-mode">true</xsl:with-param>
        </xsl:call-template>	
        
		<link rel="stylesheet" href="{$contextPath}{$workspaceURI}/resources/css/common/common.css" type="text/css"/>

        <xsl:call-template name="common-script"/>	    
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
	
	<!-- +
	     | Creates a script tag to initialize ExtJs in the center panel of the home template.
	     | You have to have a createTop js function that will be called to the top part of the main part of the screen
	     | You have to have a createPanel js function that will be called to fill the center
	     | You have to have a createDock js function that will be called after
	     | @private
	     + -->
	<xsl:template name="common-script">
    	<script type="text/javascript">
			function redirect(plugin, params) {
	            function _checkLink (link)
	            {
	                if (link.substring(0, 1) != '/')
	                {
	                    link = '/' + link;
	                }
	                return link;
	            }
	            
	            window.location.href = Ametys.getPluginWrapperPrefix(plugin) + _checkLink(params.Link);
			}
    	
    		function createBottom()
    		{
	        	<xsl:if test="/*/Versions/Component|/Plugins/Versions/Component">
	              	var data = {versions : []};
	              	<xsl:for-each select="/*/Versions/Component|/Plugins/Versions/Component">
	              		data.versions.push({
	              			name : "<xsl:value-of select="Name"/>",
	              			version: "<xsl:choose><xsl:when test="Version"><xsl:value-of select="Version"/></xsl:when><xsl:otherwise><i18n:text i18n:key="WORKSPACE_ADMIN_VERSION_UNKNOWN" i18n:catalogue="workspace.{$workspaceName}"/></xsl:otherwise></xsl:choose>",
	              			date : "<xsl:if test="Date">&#160;<i18n:text i18n:key="WORKSPACE_ADMIN_VERSION_DATED" i18n:catalogue="workspace.{$workspaceName}"/>&#160;<xsl:value-of select="Date"/>&#160;<i18n:text i18n:key="WORKSPACE_ADMIN_VERSION_DATEDTIME" i18n:catalogue="workspace.{$workspaceName}"/>&#160;<xsl:value-of select="Time"/></xsl:if><xsl:if test="position() != last()">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</xsl:if>"
	              		});
	              	</xsl:for-each>
	              	
	              	var tplFooter = new Ext.XTemplate (
	              		'&lt;div id="versions"&gt;',
	              		'&lt;tpl for="versions"&gt;',
	              		'&lt;span class="title"&gt;{name}&#160;-&#160;&lt;/span&gt;',
	              		'{version}',
	              		'{date}',
	              		'&lt;/tpl&gt;',
	              		'&lt;/div&gt;'
	              	);
	              	tplFooter.compile();
	              	
	    			new Ext.Component({
	    				renderTo: 'footer',
	    				cls: 'footer',
	   					listeners: {
					        'render' : function(p) {
					        	tplFooter.overwrite(p.getEl(), data);
					        }
	    				}
	    			});
              	</xsl:if>
    		}
    	
    		Ext.application({
			    name: 'Ametys',
			
			    launch: function() {
		    		var items = [];
		    			var top = createTop();
		    			if (top != null)
		    			{
		    				items.push(Ext.apply(top, {region: 'north'}));
		    			}
		    			items.push(Ext.apply(createPanel(), {region: 'center'}));
		    	
			        var mainPanel = Ext.create('Ext.panel.Panel', {
			            autoScroll: false,
			            border: false,
			            bodyCls: 'admin-main-panel',
			            layout: 'border',
			            
			            items: items,
			            
			            listeners: {
			            	'render' : function() {
			            		this.setSize(Ext.get('main').getSize(true))
			            	}
			            },
			            renderTo: 'main'
			        });

					Ext.EventManager.onWindowResize(function() {
						this.setSize(null, 0);
			        	this.setSize(Ext.get('main').getSize(true))
					}, mainPanel);
					
					createDock();
					
					createBottom();
					
					if (typeof appReady == "function")
					{
						appReady();
					}
			    }
			});
    	</script>
	</xsl:template>

</xsl:stylesheet>
