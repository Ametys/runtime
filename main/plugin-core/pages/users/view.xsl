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
    
    <xsl:variable name="resourcesPath"><xsl:value-of select="$contextPath"/>/plugins/core/resources</xsl:variable>    
    
	<xsl:template match="/UsersView">
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_CORE_USERS_TITLE"/></title>
                <link rel="stylesheet" href="{$resourcesPath}/css/users/view.css" type="text/css"/>
            </head>
            
            <script>
            	<script type="text/javascript" src="{$resourcesPath}/js/users/user.i18n.js"><xsl:comment>//empty</xsl:comment></script>
            	<script type="text/javascript" src="{$contextPath}/plugins/{$pluginName}/resources/js/org/ametys/administration/Users.i18n.js"><xsl:comment>empty</xsl:comment></script>
            	
            	<script type="text/javascript">
            			RUNTIME_Plugin_Runtime_EditUser.initialize("<xsl:value-of select="$pluginName"/>", <xsl:value-of select="count(Model/*)+count(Model/*[type='password'])"/>);
            			
            			org.ametys.administration.Users.initialize("<xsl:value-of select="$pluginName"/>");
            			
            			<xsl:if test="Model/@Modifiable = 'true'">
            				org.ametys.administration.Users._modifiable = true;
            			</xsl:if>
            			
            			var mainPanel = org.ametys.administration.Users.createPanel ();
            			
	                  	var formInputs = [];
	                  	<xsl:for-each select="Model/node()">
	                  		
	                  		var input = org.ametys.administration.Users.addInputField (
	                  			"<xsl:value-of select="type"/>",
	                  			"field_<xsl:value-of select="local-name()"/>",
	                  			"<xsl:value-of select="label/node()"/>",
	                  			"<xsl:value-of select="description/node()"/>"
	                  		);
	                  		
							formInputs.push(input);																								
						</xsl:for-each>		
						
					     
						org.ametys.runtime.administrator.Panel.createPanel = function () 
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