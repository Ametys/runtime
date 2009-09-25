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
    
    <xsl:template match="/ViewRights">
        <html>
            <head>
                <title>
                    <i18n:text i18n:key="PLUGINS_CORE_RIGHTS_LABELLONG" i18n:catalogue="plugin.{$pluginName}"/>
                </title>
            </head>
				<script>
	            	<script type="text/javascript">
		               		function goBack()
		                    {
		                        document.location.href = context.workspaceContext;
		                    }   
		                    
		                    var store;
		                    var listView;
		                    
							//Actions
							var handle = new org.ametys.ActionsPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ACT_CATEGORY"/>'});
							handle.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ACT_ADDUSER"/>", "<xsl:value-of select="$resourcesPath"/>/img/rights/add_user.png", goBack);
							handle.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ACT_ADDGROUP"/>", "<xsl:value-of select="$resourcesPath"/>/img/rights/add_group.png", goBack);
							handle.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ACT_REMOVE"/>", "<xsl:value-of select="$resourcesPath"/>/img/rights/delete.png", goBack);
							handle.addAction("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_HANDLE_QUIT"/>", "<xsl:value-of select="$resourcesPath"/>/img/rights/quit.png", goBack);
							//handle.addText("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_CONTEXTS_APPLICATION_HELP_AFFECT"/>");
            			
							//Create the contextual panel
							var rightPanel = new org.ametys.HtmlContainer({
									region:'east',
									border: false,
									baseCls: 'admin-right-panel',
									width: 277,
								    items: [handle]
							});
							
							var centerPanel = new Ext.Panel({
								region:'center',
								baseCls: 'transparent-panel',
								border: false,
								autoScroll : true,
								html: '&lt;i&gt;En constructions...&lt;/i&gt;'
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
            <body>

            </body>            
        </html>
    </xsl:template>
</xsl:stylesheet>