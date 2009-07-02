<?xml version="1.0" encoding="UTF-8"?>
<!--+
    | Copyright (c) 2007 Anyware Technologies and others.
    | All rights reserved. This program and the accompanying materials
    | are made available under the terms of the Eclipse Public License v1.0
    | which accompanies this distribution, and is available at
    | http://www.opensource.org/licenses/eclipse-1.0.php
    | 
    | Contributors:
    |     Anyware Technologies - initial API and implementation
    +-->
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
	
	<xsl:import href="template.xsl"/>
	<xsl:import href="resource://org/ametys/runtime/kernel/stylesheets/plugins.xsl"/>
	
    <xsl:template name="workspace-title"><xsl:copy-of select="/Plugins/html/head/title/node()"/></xsl:template>

    <xsl:template name="workspace-head"><xsl:copy-of select="/Plugins/html/head/*[local-name(.) != 'title']"/></xsl:template>

    <xsl:template name="workspace-body">
    	<!-- Fil ariane et titre -->
    	<div id="admin-top-panel">
			<xsl:variable name="title"><xsl:call-template name="workspace-title"/></xsl:variable>
			<div id="admin-path">
				<xsl:if test="xalan:nodeset($title)/node()">
					<a href="{$workspaceContext}"><i18n:text i18n:key="WORKSPACE_ADMIN_HOME" i18n:catalogue="workspace.{$workspaceName}" /></a> &gt; <xsl:copy-of select="$title"/>
				</xsl:if>
			</div>
			<h2 class="admin-panel-title"><xsl:copy-of select="$title"/></h2>
		</div>
				
    	<xsl:copy-of select="/Plugins/html/body/node()"/>
    </xsl:template>
    
    <xsl:template name="workspace-script">
    	<xsl:call-template name="plugins-load">
            <xsl:with-param name="scripts" select="/Plugins/Desktop/category/UIItem/Action/Imports/Import"/>
            <xsl:with-param name="actions" select="/Plugins/Desktop/category/UIItem/Action/ClassName"/>
        </xsl:call-template>
        
    	<script type="text/javascript">
    		
	    	var topPanel = new Ext.ametys.HtmlContainer ({
		    					border: false,
		    					region:'north',
		    					height: 43,
		    					baseCls: '',
		    					contentEl: 'admin-top-panel'
		    });
		    
		    function workspaceBody () 
			{
				
				var globalPanel =  new Ext.Panel({
					id: 'admin-panel',
					baseCls: 'admin-panel',
					border: false,
					layout: 'border',
					autoScroll: false,
					height: 'auto'
				});
				
				globalPanel.add(topPanel);
				globalPanel.add(_getAdminPanel ());
										
				return globalPanel;
			}
			
			function showPaddle() 
			{
				var dockTop = new Ext.ametys.HtmlContainer ({
					cls: 'dock-top'
				});
				var dockBottom = new Ext.ametys.HtmlContainer ({
					cls: 'dock-bottom'
				});
				
				var items = [];
				
				<xsl:for-each select="/Plugins/Desktop/category">
					<xsl:for-each select="UIItem">
						var item = new Ext.ametys.DockItem ({
							tooltip: Ext.ametys.AdminTools.DockTooltipFormater("<xsl:copy-of select="Label/node()"/>", "<xsl:value-of select="$contextPath"/><xsl:value-of select="Icons/Large"/>", "<xsl:copy-of select="Description/node()"/>"),
						 	icon : "<xsl:value-of select="$contextPath"/><xsl:value-of select="Icons/Small"/>"
						 	<xsl:if test="../CurrentUIItem/@position = position()">,pressed: true</xsl:if>
						 	<xsl:if test="not(@disabled)">
                            	, 
                                "plugin" : "<xsl:value-of select="Action/@plugin"/>",
                                "actionFunction" : <xsl:value-of select="Action/ClassName"/>.act,
                                "actionParams" : {<xsl:for-each select="Action/Parameters/*">
                                	<xsl:text>"</xsl:text><xsl:value-of select="local-name()"/>" : "<xsl:value-of select="."/><xsl:text>"</xsl:text>
                                    <xsl:if test="position() != last()">, </xsl:if>
                                    </xsl:for-each>}
	                       </xsl:if>
						});
						items.push(item);
					</xsl:for-each>
					<xsl:if test="position() != last()">
						var tile = new Ext.ametys.HtmlContainer ({
								cls: 'dock-tile'
						});
						items.push(tile);
					</xsl:if>
				</xsl:for-each>
				
				var dockCenter = new Ext.Panel ({
					baseCls: 'dock-center',
					items: items
				})
				
				var dock = new Ext.Panel({
					baseCls: 'paddle',
					renderTo: 'content_left',
					items: [dockTop, dockCenter, dockBottom]
				});
			}
			Ext.onReady(showPaddle);
		</script>
		
   		<xsl:copy-of select="/Plugins/html/script/node()"/>
    </xsl:template>
    
</xsl:stylesheet>