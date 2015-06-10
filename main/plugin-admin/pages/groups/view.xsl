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
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1" 
                xmlns:escaper="org.apache.commons.lang.StringEscapeUtils"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
    <xsl:param name="contextPath"/>
    <xsl:param name="pluginName"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>

    <xsl:variable name="resourcesPath"><xsl:value-of select="$contextPath"/>/plugins/core/resources</xsl:variable>    
    
	<xsl:template match="/GroupsManager">
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_CORE_GROUPS_LABEL"/></title>
                <link rel="stylesheet" href="{$contextPath}/plugins/admin/resources/css/groups.css" type="text/css"/>      
                <link rel="stylesheet" href="{$contextPath}/plugins/core-ui/resources/css/selectuser.css" type="text/css"/>                     
                <link rel="stylesheet" href="{$contextPath}/plugins/core-ui/resources/css/selectgroup.css" type="text/css"/>                     
            </head>
            
            <script>
            	<script type="text/javascript" src="{$contextPath}/plugins/core-ui/resources/js/Ametys/helper/SelectUser.js"><xsl:comment>//emty</xsl:comment></script>
                <script type="text/javascript" src="{$contextPath}/plugins/core-ui/resources/js/Ametys/helper/SelectGroup.js"><xsl:comment>//emty</xsl:comment></script>
            	<script type="text/javascript" src="{$contextPath}/plugins/admin/resources/js/Ametys/plugins/admin/Groups.js"></script>
            	
            	<script type="text/javascript">
            		Ametys.plugins.admin.Groups.initialize ("<xsl:value-of select="$pluginName"/>");
            		<xsl:if test="Modifiable = 'true'">
					Ametys.plugins.admin.Groups._modifiable = true;
					</xsl:if>
					
					function fillGroups ()
					{
						<xsl:for-each select="groups/group">
                        Ametys.plugins.admin.Groups._listViewGp.getStore().addSorted(Ext.create('Ametys.plugins.admin.Groups.Group', {
                            'name': "<xsl:value-of select="escaper:escapeJavaScript(label)"/>",
                            'id': "<xsl:value-of select="escaper:escapeJavaScript(@id)"/>"
                        }));
                    	</xsl:for-each>	
					}
					var mainPanel = Ametys.plugins.admin.Groups.createPanel ();
				    Ametys.plugins.admin.Groups._listViewGp.addListener('render', fillGroups, Ametys.plugins.admin.Groups._listViewGp);         
					
					createPanel = function () 
					{
						return mainPanel;
					}
            	</script>
            </script>
            <body></body>
        </html>
    </xsl:template>
        
</xsl:stylesheet>
