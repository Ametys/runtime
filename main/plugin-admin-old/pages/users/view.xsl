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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
    <xsl:param name="contextPath"/>
    <xsl:param name="pluginName"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>

    <xsl:variable name="workspaceContext"><xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/></xsl:variable>    
    
    <xsl:variable name="resourcesPath"><xsl:value-of select="$contextPath"/>/plugins/admin-old/resources</xsl:variable>    
    
	<xsl:template match="/UsersView">
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_CORE_USERS_TITLE" i18n:catalogue="plugin.core"/></title>
                <link rel="stylesheet" href="{$resourcesPath}/css/users.css" type="text/css"/>
            </head>
            
            <script>
            	<script type="text/javascript" src="{$resourcesPath}/js/Ametys/plugins/admin/UserEdit.js"><xsl:comment>//empty</xsl:comment></script>
            	<script type="text/javascript" src="{$resourcesPath}/js/Ametys/plugins/admin/Users.js"><xsl:comment>//empty</xsl:comment></script>
            	
            	<script type="text/javascript">
            			Ametys.plugins.admin.Users.initialize("<xsl:value-of select="$pluginName"/>");
            			
            			<xsl:if test="Model/@Modifiable = 'true'">
            				Ametys.plugins.admin.Users._modifiable = true;
                			Ametys.plugins.admin.UserEdit.initialize("<xsl:value-of select="$pluginName"/>", <xsl:value-of select="count(Model/*)+count(Model/*[type='password'])"/>);
	                        <xsl:for-each select="Model/node()">
	                            Ametys.plugins.admin.UserEdit.addInputField (
	                                "<xsl:value-of select="local-name()"/>",
	                                "<xsl:value-of select="type"/>",
	                                "field_<xsl:value-of select="local-name()"/>",
	                                "<xsl:value-of select="label/node()"/>",
	                                "<xsl:value-of select="description/node()"/>"
	                            );
	                        </xsl:for-each>     
            			</xsl:if>
            			var mainPanel = Ametys.plugins.admin.Users.createPanel ();

						createPanel = function () 
						{
							return mainPanel;
						}
            	</script>
            </script>
            <body>
				
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>