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
				var center = new org.ametys.HtmlContainer({
					region: 'center',
					contentEl : 'top-panel',
					cls: 'top-panel',
					border: false
				}); 	
				
           		function workspaceBody ()
           		{
           			return new Ext.Panel({
           				border: false,
           				layout: 'border',
           				autoScroll: true,
           				baseCls: 'transparent-panel',
						items : [center]
					});
           		}
		</script>
           	
        <script type="text/javascript">
			<xsl:comment>
				function forceNonSupportedNavigators()
				{
					// Create cookie which will expires when the browser is closed down
					document.cookie = "ametys.accept.non.supported.navigators=on; path=<xsl:value-of select="$contextPath"/>/;";
					
					// Redirect
					return true;
				}
			</xsl:comment>
		</script>
     	<link type="text/css" href="{$contextPath}{$workspaceURI}/resources/css/public.css" rel="stylesheet" />
    </xsl:template>
    
     <xsl:template name="workspace-body">
    	<div id="top-panel"> 
			<div class="title">
               <i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_TITLE"/>
            </div>
                  
			<br/><br/>
                  
			<div>
               TOTO <i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_TEXT"/>
                
                <a href="{$redirect}" onclick="javascript:forceNonSupportedNavigators();">
                	<img src="{$contextPath}{$workspaceURI}/resources/img/icon/warning.gif" border="0" style="margin-right: 5px; vertical-align: middle;"/>
                   	<i18n:text i18n:key="WORKSPACE_ADMIN_NAVIGATOR_FORCE_LINK"/>
                </a>
            </div>
                  
			<br/><br/>
            
		</div>
    </xsl:template>
    
    <xsl:template name="ui-tools-load"/>
    <xsl:template name="ui-load"/>
    
</xsl:stylesheet>
