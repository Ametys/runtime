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
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
	
	<xsl:import href="../core/template.xsl"/>
    
    <xsl:param name="redirect"/>
    <xsl:param name="contextPath"/>
    <xsl:param name="workspaceURI"/>
	
    <xsl:template name="workspace-script">
           	<script type="text/javascript">
           		var item = new org.ametys.DesktopItem ({
	           			text: "<i18n:text i18n:key="WORKSPACE_ADMIN_CONFIG_ACT"/>",
						desc: "<i18n:text i18n:key="WORKSPACE_ADMIN_CONFIG_DESCRIPTION"/>",
						icon: "<xsl:value-of select="$contextPath"/>/plugins/core/resources/img/administrator/config/icon_large.png",
						iconOver: "<xsl:value-of select="$contextPath"/>/plugins/core/resources/img/administrator/config/icon_large_over.png",
						plugin: "core",
	                    actionFunction : org.ametys.runtime.Link,
	                    actionParams : {"Link" : "administrator/config/edit.html", "Mode" : "plugin-wrapped"}
						
				});

				var toping = new org.ametys.HtmlContainer({
					region: 'north',
					contentEl : 'top-panel',
					cls: 'top-panel',
					border: false
				}); 	

				var center = new Ext.Panel({
					region: 'center',
					baseCls: 'center-panel',
					border: false,
					items : [item]
				}); 	
				
           		org.ametys.runtime.HomePage.createPanel = function ()
           		{
           			return new Ext.Panel({
           				border: false,
           				layout: 'border',
           				autoScroll: true,
           				baseCls: 'transparent-panel',
						items : [toping, center]
					});
           		}
           	</script>
           	<link type="text/css" href="{$contextPath}{$workspaceURI}/resources/css/public.css" rel="stylesheet" />
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
