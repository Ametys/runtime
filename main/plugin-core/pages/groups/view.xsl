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
                xmlns:escaper="org.apache.commons.lang.StringEscapeUtils"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
    <xsl:param name="contextPath"/>
    <xsl:param name="pluginName"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>

    <xsl:variable name="workspaceContext"><xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/></xsl:variable>    
    
    <xsl:variable name="resourcesPath"><xsl:value-of select="$contextPath"/>/plugins/core/resources</xsl:variable>    
    
	<xsl:template match="/GroupsManager">
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_CORE_GROUPS_LABEL"/></title>
                <link rel="stylesheet" href="{$resourcesPath}/css/groups/groups.css" type="text/css"/>      
                <link rel="stylesheet" href="{$resourcesPath}/css/users/selectuser.css" type="text/css"/>                     
            </head>
            
            <script>
            	<script type="text/javascript" src="{$resourcesPath}/js/users/selectuser.i18n.js"><xsl:comment>//emty</xsl:comment></script>
            	<script type="text/javascript" src="{$contextPath}/plugins/{$pluginName}/resources/js/org/ametys/administration/Groups.i18n.js"></script>
            	
            	<script type="text/javascript">
            		RUNTIME_Plugin_Runtime_SelectUser.initialize("<xsl:value-of select="$pluginName"/>");
            		
            		org.ametys.administration.Groups.initialize ("<xsl:value-of select="$pluginName"/>");
            		
            		<xsl:if test="Modifiable = 'true'">
						org.ametys.administration.Groups._modifiable = true;
					</xsl:if>
					
					function fillGroups ()
					{
						<xsl:for-each select="groups/group">
                        org.ametys.administration.Groups._listViewGp.addElement("<xsl:value-of select="escaper:escapeJavaScript(@id)"/>", 
                                {
                                    id: "<xsl:value-of select="escaper:escapeJavaScript(@id)"/>",
                                    name : "<xsl:value-of select="escaper:escapeJavaScript(label)"/>"
                                }, true);
                    	</xsl:for-each>	
					}
					var mainPanel = org.ametys.administration.Groups.createPanel ();
				    
				    org.ametys.administration.Groups._listViewGp.addListener('render', fillGroups, org.ametys.administration.Groups._listViewGp);         
					
					org.ametys.runtime.administrator.Panel.createPanel = function () 
					{
						return mainPanel;
					}
                    
                    org.ametys.administration.Groups.checkBeforeQuit = function()
                    {
                        if (org.ametys.administration.Groups._currentGroup != null &amp;&amp; org.ametys.administration.Groups._hasChanges)
                        {
                            if (confirm("<i18n:text i18n:key="PLUGINS_CORE_GROUPS_MODIFY_CONFIRM"/>"))
                            {
                                org.ametys.administration.Groups._saveConfirm('yes', org.ametys.administration.Groups._currentGroup);
                            }
                        }
                    }
                    
                    Ext.onReady( function () {
                        window.onbeforeunload = org.ametys.administration.Groups.checkBeforeQuit;
                    });
            	</script>
            </script>
            <body></body>
        </html>
    </xsl:template>
        
</xsl:stylesheet>
