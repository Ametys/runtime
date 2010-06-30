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
								html: '&lt;i&gt;Under construction...&lt;/i&gt;'
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