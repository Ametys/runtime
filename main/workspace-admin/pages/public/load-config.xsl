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
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
	
	<xsl:import href="../core/template.xsl"/>
    
    <xsl:param name="redirect"/>
    <xsl:param name="contextPath"/>
    <xsl:param name="workspaceURI"/>
	
    <xsl:template name="workspace-script">
           	<script type="text/javascript">
           		var item = new Ext.ametys.DesktopItem ({
	           			text: "<i18n:text i18n:key="WORKSPACE_ADMIN_CONFIG_ACT"/>",
						desc: "<i18n:text i18n:key="WORKSPACE_ADMIN_CONFIG_DESCRIPTION"/>",
						icon: "<xsl:value-of select="$contextPath"/>/plugins/core/resources/img/administrator/config/icon_large.png",
						iconOver: "<xsl:value-of select="$contextPath"/>/plugins/core/resources/img/administrator/config/icon_large_over.png",
						plugin: "core",
	                    actionFunction : Runtime_InteractionActionLibrary_Link.act,
	                    actionParams : {"Link" : "administrator/config/edit.html", "Mode" : "plugin-wrapped"}
						
				});
				
				var top = new Ext.ametys.HtmlContainer({
					region: 'north',
					contentEl : 'top-panel',
					baseCls: 'top-panel',
					border: false
				}); 	
				
				var center = new Ext.Panel({
					region: 'center',
					baseCls: 'center-panel',
					border: false,
					items : [item]
				}); 	
				
           		function workspaceBody ()
           		{
           			return new Ext.Panel({
           				border: false,
           				layout: 'border',
           				autoScroll: true,
           				baseCls: 'transparent-panel',
						items : [top, center]
					});
           		}
           	</script>
           	
           	<style>
	           		.top-panel {
	           			background: none !important;
	           			text-align: center;
    					color: #666; 
    					padding-top: 30px;
	           		}
	           		.center-panel-body {
	           			background: none !important;
	           			text-align: center !important;
	           			padding-top: 10px;
	           			padding-bottom: 20px;
	           		}
	           		
    				.top-panel .title {
    					font-size: 1.4em; 
    					font-weight: bold; 
    					color: #1C58A0; 
    					text-transform: uppercase;
    				}
    				
    				.desktop-item {
						font-weight: bold;
						margin-left:auto;
						margin-right:auto;
						float: none !important;
					}
    		</style>
    </xsl:template>
    
    <xsl:template name="workspace-body">
    	<div id="top-panel"> 
			<div class="title">
                <i18n:text i18n:key="WORKSPACE_ADMIN_CONFIG_TITLE"/>
            </div>
                  
			<br/><br/>
                  
			<div>
                <i18n:text i18n:key="WORKSPACE_ADMIN_CONFIG_TEXT"/>
            </div>
                  
			<br/><br/>
            
		</div>
    </xsl:template>

</xsl:stylesheet>
