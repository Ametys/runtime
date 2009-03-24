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
    
     <xsl:variable name="resourcesPath"><xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources</xsl:variable>
     
    <xsl:template match="/">
        <html>
            <head>
                <title>Monitoring</title>
                <style>
                	a img {border-width: 0}
                </style>
            </head>
            <script>
            	<script type="text/javascript">
					var help = new Ext.ametys.TextPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_HELP"/>'});
					help.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_HELP_TEXT"/>");
          			
          			var rightPanel = new Ext.ametys.HtmlContainer({
						region:'east',
						border: false,
						baseCls: 'admin-right-panel',
						width: 277,
					    items: [help]
					});
				
					var centerPanel = new Ext.Panel({
						region:'center',
						baseCls: 'transparent-panel',
						border: false,
						autoScroll : true,
						contentEl : 'samples'
					});		
							
            		function _getAdminPanel ()
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
            	<div id="samples">
                <table>
                <xsl:for-each select="samples/sample">
                	<tr>
                		<td>
                			:: <xsl:value-of select="name"/><br/>
                			<a href="monitoring/{name}.html" title="Details">
                				<img src="{$resourcesPath}/img/monitoring/graph/{name}/hour.png" title="{name}"/>
                			</a>
                			<span>&#160;&#160;</span>
                			<a href="monitoring/{name}.html" title="Details">
                				<img src="{$resourcesPath}/img/monitoring/graph/{name}/day.png" title="{name}"/>
                			</a>
                		</td>
                	</tr>
                </xsl:for-each>
               	</table>
               	</div>
            </body>
        </html>
    </xsl:template>    
    
</xsl:stylesheet>