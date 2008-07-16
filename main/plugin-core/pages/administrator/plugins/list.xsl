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
<xsl:stylesheet version="1.0" xmlns:i18n="http://apache.org/cocoon/i18n/2.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:param name="contextPath"/>
    <xsl:param name="pluginName"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
    
    <xsl:variable name="workspaceContext"><xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/></xsl:variable>
    <xsl:variable name="pluginResource"><xsl:value-of select="$contextPath"/>/plugins/<xsl:value-of select="$pluginName"/>/resources</xsl:variable>

	<xsl:template match="/list">
        <html>
            <head>
                <title><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_TITLE"/></title>
                <LINK rel="stylesheet" href="{$pluginResource}/css/homepage.css" type="text/css"/>
            </head>
            <body>
                <style>
                    .message
                    {
                        font-weight: bold;
                        font-size: 12px;
                        font-family: Verdana;
                        margin: 20px;
                    }
                </style>
            
				<table class="admin_index_main_table">
					<tr>
						<td id="actionset"/>
						<td  style="border-left: 1px solid #3b3a36; border-top: 1px solid #3b3a36; border-right: 1px solid #ece9d8; border-bottom: 1px solid #ece9d8; padding: 5px;">
							<table id="listview" style="width: 468px; height: 428px;"/>
						</td>
					</tr>
				</table>
                
               	<!-- Onglet vue par plugins -->
                <div id="plugin_tab" style="display: none; width: 452px; margin: 5px;">
                		<div id="admin_plugins_plugin_tree" style="display: none;  width: 452px; height: 390px;">
            				<img src="{$pluginResource}/img/administrator/plugins/root.gif"/>
                			<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_PLUGINS"/>
                			<!-- Noeud groupe de plugins -->
            				<ul>
                                <xsl:choose>
                                    <xsl:when test="count(plugin) = 0">
                                        <li style="font-style: italic; color: #7f7f7f"><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_NO_PLUGINS"/></li>
                                    </xsl:when>
                                    <xsl:otherwise>
                                		<xsl:for-each select="plugin">
                                		     <xsl:sort select="@name"/>
                                			<li><img src="{$pluginResource}/img/administrator/plugins/plugins.gif"/>
                                				<xsl:value-of select="@name"/>
                                				<xsl:if test="count(feature) != 0">
            	                    				<ul>
                            						<!-- Noeud plugin -->
            	                    				<xsl:for-each select="feature">
            	                    				<xsl:sort select="@name"/>
            	                    					<li>
            	                    						<xsl:choose>
            	                    							<xsl:when test="@inactive = 'true'">
            	                    								<img src="{$pluginResource}/img/administrator/plugins/plugin-inactive.gif"/>
            	                    							</xsl:when>
            	                    							<xsl:otherwise>
            	                    								<img src="{$pluginResource}/img/administrator/plugins/plugin.gif"/>
            	                    							</xsl:otherwise>
            	                    						</xsl:choose>
            	                    						<xsl:value-of select="@name"/>
                                                            <xsl:if test="@inactive = 'true'">
                                                                &#160;<span style="font-style: italic; color: #7f7f7f"><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_INACTIVE_{@cause}"/></span>
                                                            </xsl:if>
                                                            
                                                            <xsl:if test="count(component) > 0">
                                                                <ul>
                                                                    <xsl:for-each select="component">
                                                                        <xsl:sort select="."/>
                                                                        
                                                                        <li>
                                                                            <img src="{$pluginResource}/img/administrator/plugins/composant.gif"/>
                                                                            <xsl:value-of select="."/>
                                                                        </li>
                                                                                                                                                
                                                                    </xsl:for-each>
                                                                </ul>
                                                            </xsl:if>
                                                            
            					                    		<xsl:if test="count(extensionPoint) != 0">
                            										<!-- Noeud point d'extension -->
            					                    				<ul>
            					                    				<xsl:for-each select="extensionPoint">
            					                    				<xsl:sort select="@name"/>
            					                    					<li>
            																<xsl:variable name="name" select="@name"/>
            																<xsl:choose>
            																	<xsl:when test="/list/extension-points/single-extension-point[@id = $name]">
            																		<img src="{$pluginResource}/img/administrator/plugins/extension-point.gif"/>
            																	</xsl:when>
            																	<xsl:otherwise>
            																		<img src="{$pluginResource}/img/administrator/plugins/extension-point-multiple.gif"/>
            																	</xsl:otherwise>
            																</xsl:choose>
            																<xsl:value-of select="@name"/>
                            												<!-- Noeud extension -->
            																<ul>
            																<xsl:for-each select="extension">
            																<xsl:sort select="."/>
            																	<li>
            																		<img src="{$pluginResource}/img/administrator/plugins/extension.gif"/>
            																		<xsl:value-of select="."/>
            																	</li>
            																</xsl:for-each>
            															    </ul>
            															</li>
            					                    				</xsl:for-each>
            					                    				</ul>
            			                    				</xsl:if>
            	                    					</li>
            	                    				</xsl:for-each>
            	                    				</ul>
                                				</xsl:if>
                                			</li>
                                		</xsl:for-each>
                                    </xsl:otherwise>
                                </xsl:choose>
                    		</ul>
                    	</div>
        				<script language="JavaScript">
        					var treePlugin = new STree("admin_plugins_plugin_tree", null, 1);
        				</script>
                </div>
               	<!-- Onglet vue par point d'extensions -->
                <div id="extension_point_tab" style="display: none; width: 452px; margin: 5px;">
                	<div id="admin_plugins_extension_point_tree" style="display: none; height: 390px;">
			                	<img src="{$pluginResource}/img/administrator/plugins/root.gif"/>
			                	<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_EXTENSION_POINTS"/>
                				<!-- Noeud point d'extension -->
            					<ul>
                                <xsl:choose>
                                    <xsl:when test="count(extension-points/extension-point) = 0">
                                        <li style="font-style: italic; color: #7f7f7f"><i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_NO_EXTENSION"/></li>                                    
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:for-each select="extension-points/*">
                                             <xsl:sort select="@id"/>
                                            <li>
                                                <xsl:choose>
                                                    <xsl:when test="local-name() = 'single-extension-point'">
                                                        <img src="{$pluginResource}/img/administrator/plugins/extension-point.gif"/>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <img src="{$pluginResource}/img/administrator/plugins/extension-point-multiple.gif"/>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                                <xsl:value-of select="@id"/>
                                                <xsl:variable name="name" select="@id"/>
                                                <xsl:if test="count(/list/plugin/feature/extensionPoint[@name = $name]/extension) != 0">
                                                <!-- Noeud extension -->
                                                <ul>
                                                    <xsl:for-each select="/list/plugin/feature/extensionPoint[@name = $name]/extension">
                                                    <xsl:sort select="."/>
                                                        <li>
                                                            <img src="{$pluginResource}/img/administrator/plugins/extension.gif"/>
                                                            <xsl:value-of select="."/>
                                                            <!-- Noeud plugin -->
                                                            <ul>
                                                                <li>
                                                                    <img src="{$pluginResource}/img/administrator/plugins/plugin.gif"/>
                                                                    <xsl:value-of select="../../@name"/>
                                                                    <!-- Noeud groupe de plugins -->
                                                                    <ul>
                                                                        <li>
                                                                            <img src="{$pluginResource}/img/administrator/plugins/plugins.gif"/>
                                                                            <xsl:value-of select="../../../@name"/>
                                                                        </li>
                                                                    </ul>
                                                                </li>
                                                            </ul>
                                                            
                                                        </li>
                                                    </xsl:for-each>  
                                                </ul>
                                                </xsl:if>
                                            </li>
                                        </xsl:for-each>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </ul>
                            <script language="JavaScript">
                                var treeExtension = new STree("admin_plugins_extension_point_tree", null, 0);
                            </script>

	    			</div>
                </div>
            	<script language="JavaScript">
					function goBack()
					{
						document.location.href = '<xsl:value-of select="$workspaceContext"/>';
					}
				
					var sp = new SContextualPanel("actionset"); 
					var handle = sp.addCategory("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_HANDLE"/>");
						handle.addLink("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_CANCEL"/>", "<xsl:value-of select="$pluginResource"/>/img/administrator/plugins/quit.gif", goBack);
					var help = sp.addCategory("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_HELP"/>");
						help.addElement("&lt;div style='font-size: 11px; font-color: #000000'&gt;<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_HELP_TEXT"/>&lt;/div&gt;");
					sp.paint();

            		var tabs = new STab("listview");
					tabs.addTab("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_PLUGIN_VIEW"/>", "plugin_tab");
					tabs.addTab("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_PLUGINS_EXTENSION_POINT_VIEW"/>", "extension_point_tab");
					tabs.paint();
            	</script>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>