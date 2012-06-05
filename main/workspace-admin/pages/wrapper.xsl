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
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
	
	<xsl:import href="kernel://pages/home/home.xsl"/>
	
	<xsl:param name="contextPath"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
    
	<xsl:template match="/">
    	<xsl:call-template name="home">
		    <xsl:with-param name="needs-kernel-ui" select="true()"/>
		    <xsl:with-param name="use-js-css-component" select="'false'"/>
            <xsl:with-param name="debug-mode">true</xsl:with-param>
		    
		    <xsl:with-param name="context-path" select="$contextPath"/>
		    <xsl:with-param name="workspace-name" select="$workspaceName"/>
		    <xsl:with-param name="workspace-prefix" select="$workspaceURI"/>
		    
		    <xsl:with-param name="plugins-direct-prefix">/plugins</xsl:with-param>
		    <xsl:with-param name="plugins-wrapped-prefix">/_plugins</xsl:with-param>
		    
		    <xsl:with-param name="authorized-browsers">{
  		    	'supported': { 'ie' : '7-9', ff: '3.6-12', ch: '10-19', op: '11-11.9', sa: '5.0-5.1'},
		  		'not-supported': { 'ie' : '0-6', ff: '0-3.5', ch : '0-9', op: '0-10.99', sa: '0-4.9'},
 		  		'warning-redirection': "<xsl:value-of select="$workspaceURI"/>/public/browser-unknown.html",
		  		'failure-redirection': "<xsl:value-of select="$workspaceURI"/>/public/browser-unsupported.html"}
		    </xsl:with-param>
		    
		    <xsl:with-param name="head-title">
		    		<xsl:if test="/Plugins/html/head/title/node()">
						<xsl:copy-of select="/Plugins/html/head/title/node()"/>
						<xsl:text> - </xsl:text>
					</xsl:if>
					<i18n:text i18n:catalogue="workspace.{$workspaceName}" i18n:key="WORKSPACE_ADMIN_LABEL_SHORT"/>
					<xsl:text> </xsl:text>
					<i18n:text i18n:catalogue="application" i18n:key="APPLICATION_PRODUCT_LABEL"/>
		    </xsl:with-param>
		    
		    <xsl:with-param name="head-meta">
		    	<xsl:call-template name="kernel-load">
		            <xsl:with-param name="scripts" select="/Plugins/Desktop/category/DesktopItem/scripts/file"/>
		            <xsl:with-param name="css" select="/Plugins/Desktop/category/DesktopItem/css/file"/>
		            <xsl:with-param name="use-css-component">false</xsl:with-param>
		            <xsl:with-param name="use-js-component">false</xsl:with-param>
		            <xsl:with-param name="debug-mode">true</xsl:with-param>
		        </xsl:call-template>	
		        
		        <!-- TODO load this CSS/JS using the component if activated -->
                <link rel="stylesheet" href="{$contextPath}{$workspaceURI}/resources/css/wrapper.css" type="text/css"/>
		        
		        <xsl:copy-of select="/Plugins/html/head/*[local-name(.) != 'title']"/>
		        
		        <xsl:call-template name="workspace-scripts"/>	    
		    </xsl:with-param>

		    <xsl:with-param name="body-title">
		    	<img id="title-logo" alt="workspace.admin:WORKSPACE_ADMIN_LABEL_LONG" i18n:attr="alt"/>
		    	<script type="text/javascript">
		    		document.getElementById('title-logo').src = "<xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/>/resources/img/Admin_<i18n:text i18n:key="KERNEL_LANGUAGE_CODE" i18n:catalogue="kernel"/>.png";
		    	</script>
			</xsl:with-param>
		    
		    <xsl:with-param name="body-col-main">
		    	<xsl:copy-of select="/Plugins/html/body/node()"/>
		    </xsl:with-param>
    	</xsl:call-template>
    </xsl:template>
	
    <xsl:template name="workspace-scripts">
    	<script type="text/javascript">
    		Ext.application({
			    name: 'Ametys',
			
			    launch: function() {
			        var mainPanel = Ext.create('Ext.panel.Panel', {
			            autoScroll: false,
			            border: false,
			            bodyCls: 'admin-main-panel',
			            layout: 'border',
			            
			            items: [
							Ext.create('Ext.Component', {
								region: 'north',
								cls: 'admin-top-panel',
								border: false,
								height: 43, 
								html: '&lt;div&gt;'
						    		+ 	'&lt;ul&gt;'
						    		+ 		'&lt;li&gt;&lt;a href="<xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/>/index.html"&gt;<i18n:text i18n:key="WORKSPACE_ADMIN_HOME" i18n:catalogue="workspace.{$workspaceName}" />&lt;/a&gt;&lt;/li&gt;'
						    		+ 		'&lt;li&gt;&gt; <xsl:copy-of select="/Plugins/html/head/title/node()"/>&lt;/li&gt;'
						    		+ 	'&lt;/ul&gt;'
						    		+ 	'&lt;h1&gt;<xsl:copy-of select="/Plugins/html/head/title/node()"/>&lt;/h1&gt;'
					 	    		+ '&lt;/div&gt;'
							}),
							Ext.apply(createPanel(), {region: 'center'})
			            ],
			            
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
					
					_createDock();
			    }
			});
    	</script>
    	<script type="text/javascript">
    		function createPanel()
    		{
    			return new Ext.Container ({
					html: '&lt;p&gt;&lt;em&gt;An error occured while loading this screen. The function &lt;b&gt;createPanel&lt;/b&gt; cannot be found.&lt;/em&gt;&lt;/p&gt;'
				});
    		}
    		
			function _createDock() {
				<xsl:if test="/Plugins/Desktop/category">
					var items = [];
					
					<xsl:for-each select="/Plugins/Desktop/category">
						<xsl:for-each select="DesktopItem">
							var item = new org.ametys.DockItem ({
								tooltip: org.ametys.AdminTools.DockTooltipFormater("<xsl:copy-of select="action/param[@name='label']/node()"/>", "<xsl:value-of select="$contextPath"/><xsl:value-of select="action/param[@name='icon-large']"/>", "<xsl:copy-of select="action/param[@name='default-description']/node()"/>"),
								icon: "<xsl:value-of select="$contextPath"/><xsl:value-of select="action/param[@name='icon-small']"/>"
							 	<xsl:if test="../CurrentUIItem/@position = position()">, pressed: true</xsl:if>
								<xsl:if test="not(@disabled)">
	                            	, 
	                                "plugin" : "<xsl:value-of select="@plugin"/>",
	                                "actionFunction" : <xsl:value-of select="action/@class"/>,
	                                "actionParams" : {<xsl:for-each select="action/param">
	                                	<xsl:text>"</xsl:text><xsl:value-of select="@name"/>" : "<xsl:copy-of select="./node()"/><xsl:text>"</xsl:text>
	                                    <xsl:if test="position() != last()">, </xsl:if>
	                                    </xsl:for-each>}
	                                </xsl:if>
							});
							items.push(item);
						</xsl:for-each>
						<xsl:if test="position() != last()">
							var tile = new org.ametys.HtmlContainer ({
									cls: 'dock-tile'
							});
							items.push(tile);
						</xsl:if>
					</xsl:for-each>
					
					var dock = new Ext.Panel({
						baseCls: 'paddle',
						renderTo: 'home-col-left',
						items: [new Ext.Container ({
									cls: 'dock-top'
								}), 
								new Ext.Panel ({
									baseCls: 'dock-center',
									items: items
								}), 
								new Ext.Container ({
									cls: 'dock-bottom'
								})
						]
					});
				</xsl:if>
			};
		</script>
		
		<script type="text/javascript" src="{$contextPath}{$workspaceURI}/resources/js/Ametys/workspace/admin/Action.js"/>
		<script type="text/javascript" src="{$contextPath}{$workspaceURI}/resources/js/Ametys/workspace/admin/NavigationItem.js"/>
		<script type="text/javascript" src="{$contextPath}{$workspaceURI}/resources/js/Ametys/workspace/admin/NavigationPanel.js"/>
		
   		<xsl:copy-of select="/Plugins/html/script/node()"/>
    </xsl:template>
    
</xsl:stylesheet>