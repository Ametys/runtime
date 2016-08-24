<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2015 Anyware Services

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
                xmlns:ametys="org.ametys.core.util.AmetysXSLTHelper"
                xmlns:ex="http://apache.org/cocoon/exception/1.0" 
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
    
    <xsl:import href="common.xsl"/>    
    
    <xsl:template name="head"><i18n:text i18n:key="PLUGINS_CORE_UI_ERROR_403_HEAD" i18n:catalogue="plugin.core-ui"/></xsl:template>
    <xsl:template name="title"><i18n:text i18n:key="PLUGINS_CORE_UI_ERROR_403_TITLE" i18n:catalogue="plugin.core-ui"/></xsl:template>
    <xsl:template name="text"><i18n:text i18n:key="PLUGINS_CORE_UI_ERROR_403_TEXT" i18n:catalogue="plugin.core-ui"/></xsl:template>
    
    <xsl:template name="main-after">
        <xsl:variable name="user" select="ametys:user()/user"/>
        <div class="a-fluent-user-card-wrapper">
			<div class="a-fluent-user-card">
				<div class="photo">
					<img src="{$contextPath}/plugins/core-ui/current-user/image_64" />
				</div>
				<div class="main">
					<div class="name-wrapper">
						<div class="name"><xsl:value-of select="$user/fullname" /></div>
						<div class="login"><xsl:value-of select="$user/@login" /><xsl:text> / </xsl:text><xsl:value-of select="$user/populationLabel" /></div>
					</div>
					<xsl:if test="$user/email">
						<div class="email" title="{$user/email}"><xsl:value-of select="$user/email"/></div>
					</xsl:if>
				</div>
				
				<script type="text/javascript">
				    function logout()
				    {
				        var iframe = document.createElement("iframe");
				        iframe.src = "<xsl:value-of select='$contextPath'/>/plugins/core-ui/servercomm/messages.xml?content={%220%22:{%22pluginOrWorkspace%22:%22core-ui%22,%22responseType%22:%22text%22,%22url%22:%22client-call%22,%22parameters%22:{%22role%22:%22org.ametys.core.ui.RibbonControlsManager%22,%22id%22:%22org.ametys.core.ui.user.logout%22,%22methodName%22:%22logout%22,%22parameters%22:[]}}}";
				        iframe.style.position = "absolute";
				        iframe.style.top = "-10000px";
                        iframe.style.left = "-10000px";
                        iframe.style.width = "1px";
                        iframe.style.height = "1px";
                        iframe.onload = function() {
                            if (iframe.contentWindow.document)
                            {
                                // We do not test iframe.contentWindow.document.children[0].children[0].innerHTML == "true" since we do not know what we could do if it was false :(
				                window.location.reload();
                            }
                        }
                        document.body.appendChild(iframe);
				    }
				</script>
				
				<xsl:if test="$user/@logoutable">
			        <xsl:call-template name="button">
			            <xsl:with-param name="text"><i18n:text i18n:key='PLUGINS_CORE_UI_USER_LOGOUT_LABEL' i18n:catalogue='plugin.core-ui'/></xsl:with-param>
                        <xsl:with-param name="title"><i18n:text i18n:key='ui:PLUGINS_CORE_UI_USER_LOGOUT_DESCRIPTION' i18n:catalogue='plugin.core-ui'/></xsl:with-param>
			            <xsl:with-param name="action" select="'logout()'"/>
			        </xsl:call-template>
				</xsl:if>
			</div>
			
	    </div>
    </xsl:template>
		
    
</xsl:stylesheet>