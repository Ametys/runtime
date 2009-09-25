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
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:param name="contextPath"/>
    <xsl:param name="pluginName"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
    
    <xsl:variable name="workspaceContext"><xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/></xsl:variable>
    
    <xsl:variable name="resourcesPath"><xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources</xsl:variable>
    
	<xsl:template match="/list">
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_TITLE"/></title>
            </head>
				<script>
	            	<script type="text/javascript">
		               		function goBack()
		                    {
		                        document.location.href = context.workspaceContext;
		                    }   
		                    
							var handle = new org.ametys.ActionsPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_HANDLE"/>'});
							handle.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CANCEL"/>", "<xsl:value-of select="$resourcesPath"/>/img/administrator/plugins/quit.png", goBack);
            			
							var help = new org.ametys.TextPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_HELP"/>'});
							help.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_HELP_TEXT"/>");
            			
            				var rightPanel = new org.ametys.HtmlContainer({
									region:'east',
									border: false,
									cls: 'admin-right-panel',
									width: 277,
								    items: [handle, help]
							});
						
							var centerPanel = new Ext.Panel({
								region:'center',
								baseCls: 'transparent-panel',
								border: false,
								autoScroll : true,
								html: '&lt;i&gt;En construction ...&lt;/i&gt;'
							});		
						
							
							org.ametys.runtime.administrator.Panel.createPanel = function () 
							{
								return new Ext.Panel({
									region: 'center',
									baseCls: 'transparent-panel',
									border: false,
									layout: 'border',
									autoScroll: true,
									items: [centerPanel, rightPanel]
								});
							}						            			
	            			
	            	</script>
            </script>	
            
        </html>
    </xsl:template>
</xsl:stylesheet>