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
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
	
	<xsl:import href="kernel://pages/home/home.xsl"/>
	<xsl:import href="common/common.xsl"/>
	
	<xsl:param name="contextPath"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
    
	<xsl:template match="/">
    	<xsl:call-template name="home">
		    <xsl:with-param name="needs-kernel-ui" select="true()"/>
            <xsl:with-param name="debug-mode">true</xsl:with-param>
		    
		    <xsl:with-param name="context-path" select="$contextPath"/>
		    <xsl:with-param name="workspace-name" select="$workspaceName"/>
		    <xsl:with-param name="workspace-prefix" select="$workspaceURI"/>
		    
		    <xsl:with-param name="plugins-direct-prefix">/plugins</xsl:with-param>
		    <xsl:with-param name="plugins-wrapped-prefix">/_plugins</xsl:with-param>
		    
		    <xsl:with-param name="authorized-browsers"><xsl:call-template name="authorized-browsers"/></xsl:with-param>
		    
		    <xsl:with-param name="head-title">
		    		<xsl:if test="/Plugins/html/head/title/node()">
						<xsl:copy-of select="/Plugins/html/head/title/node()"/>
						<xsl:text> - </xsl:text>
					</xsl:if>
					<xsl:call-template name="head-title"/>
		    </xsl:with-param>
		    
		    <xsl:with-param name="head-meta">
		    	<xsl:call-template name="head-meta"/>
		        
                <link rel="stylesheet" href="{$contextPath}{$workspaceURI}/resources/css/wrapper.css" type="text/css"/>
		        
		        <xsl:copy-of select="/Plugins/html/head/*[local-name(.) != 'title']"/>
		        
		        <xsl:call-template name="workspace-scripts"/>	    
		    </xsl:with-param>

		    <xsl:with-param name="body-title"><xsl:call-template name="body-title"/></xsl:with-param>
		    
		    <xsl:with-param name="body-col-main">
		    	<xsl:copy-of select="/Plugins/html/body/node()"/>
		    </xsl:with-param>
    	</xsl:call-template>
    </xsl:template>
	
    <xsl:template name="workspace-scripts">
    	<script type="text/javascript">
    		function createTop()
    		{
  				return Ext.create('Ext.Component', {
					cls: 'admin-top-panel',
					border: false,
					height: 43, 
					html: "&lt;div&gt;"
			    		+ 	"&lt;ul&gt;"
			    		+ 		"&lt;li&gt;&lt;a href=\"<xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/>/index.html\"&gt;<i18n:text i18n:key="WORKSPACE_ADMIN_HOME" i18n:catalogue="workspace.{$workspaceName}" />&lt;/a&gt;&lt;/li&gt;"
			    		+ 		"&lt;li&gt;&gt; <xsl:copy-of select="/Plugins/html/head/title/node()"/>&lt;/li&gt;"
			    		+ 	"&lt;/ul&gt;"
			    		+ 	"&lt;h1&gt;<xsl:copy-of select="/Plugins/html/head/title/node()"/>&lt;/h1&gt;"
		 	    		+ "&lt;/div&gt;"
				});
    		}
    		
    		function createPanel()
    		{
    			return new Ext.Container ({
					html: '&lt;p&gt;&lt;em&gt;An error occured while loading this screen. The function &lt;b&gt;createPanel&lt;/b&gt; cannot be found.&lt;/em&gt;&lt;/p&gt;'
				});
    		}
    		
			function createDock() {
				<xsl:if test="/Plugins/Desktop/category">
					var items = [];
					
					<xsl:for-each select="/Plugins/Desktop/category">
						<xsl:for-each select="DesktopItem">
							<xsl:variable name="itemId" select="generate-id()"/>
							
							var itemCfg_<xsl:value-of select="$itemId"/> = <xsl:value-of select="action"/>;
							
							var item = new Ametys.workspace.admin.dock.DockItem ({
								tooltip: {
									title: itemCfg_<xsl:value-of select="$itemId"/>.label,
									image: Ametys.CONTEXT_PATH + itemCfg_<xsl:value-of select="$itemId"/>["icon-large"],
									text: itemCfg_<xsl:value-of select="$itemId"/>["default-description"]
								},
								icon: Ametys.CONTEXT_PATH + itemCfg_<xsl:value-of select="$itemId"/>["icon-small"]
							 	<xsl:if test="../CurrentUIItem/@position = position()">, pressed: true</xsl:if>
								
								<xsl:if test="not(@disabled)">
									,
									handler: function () { 
										<xsl:value-of select="action/@class"/>("<xsl:value-of select="@plugin"/>", itemCfg_<xsl:value-of select="$itemId"/>);
	 	                            }
								</xsl:if>
							});
							items.push(item);
						</xsl:for-each>
						<xsl:if test="position() != last()">
							var tile = new Ext.Component ({
									cls: 'dock-tile'
							});
							items.push(tile);
						</xsl:if>
					</xsl:for-each>
					
					var dock = new Ext.Panel({
						cls: 'paddle',
						border: false,
						frame: false,
						shadow: false,
						width: 44,
						renderTo: 'left',
						items: [new Ext.Component ({
									cls: 'dock-top'
								}), 
								new Ext.Panel ({
									baseCls: 'dock-center',
									items: items
								}), 
								new Ext.Component ({
									cls: 'dock-bottom'
								})
						]
					});
				</xsl:if>
			};
		</script>
		
		<script type="text/javascript" src="{$contextPath}{$workspaceURI}/resources/js/Ametys/admin/rightpanel/TextPanel.js"/>
		<script type="text/javascript" src="{$contextPath}{$workspaceURI}/resources/js/Ametys/admin/rightpanel/ActionPanel.js"/>
		<script type="text/javascript" src="{$contextPath}{$workspaceURI}/resources/js/Ametys/admin/rightpanel/ActionPanel/Action.js"/>
		<script type="text/javascript" src="{$contextPath}{$workspaceURI}/resources/js/Ametys/admin/rightpanel/NavigationPanel.js"/>
		<script type="text/javascript" src="{$contextPath}{$workspaceURI}/resources/js/Ametys/admin/rightpanel/NavigationPanel/NavigationItem.js"/>

		<xsl:if test="/Plugins/Desktop/category">
			<script type="text/javascript" src="{$contextPath}{$workspaceURI}/resources/js/Ametys/workspace/admin/dock/DockItem.js"/>
		</xsl:if>
		
   		<xsl:copy-of select="/Plugins/html/script/node()"/>
    </xsl:template>
    
</xsl:stylesheet>