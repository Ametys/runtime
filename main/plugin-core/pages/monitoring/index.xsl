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
					var help = new org.ametys.TextPanel({title: '<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_HELP"/>'});
					help.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_HELP_TEXT"/>");
          			
          			var rightPanel = new org.ametys.HtmlContainer({
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
            	<div id="samples">
                <table>
                <xsl:for-each select="samples/sample">
                	<tr>
                		<td>
                			:: <xsl:value-of select="name"/><br/>
                			<a href="monitoring/{name}.html" title="Details">
                				<img src="monitoring/graph/{name}/hour.png" title="{name}"/>
                			</a>
                			<span>&#160;&#160;</span>
                			<a href="monitoring/{name}.html" title="Details">
                				<img src="monitoring/graph/{name}/day.png" title="{name}"/>
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