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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
	
	<xsl:import href="kernel://pages/home/home.xsl"/>
	
	<xsl:param name="contextPath"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
    
	<xsl:template match="/">
    	<xsl:call-template name="home">
		    <xsl:with-param name="needs-kernel-ui" select="true()"/>
		    <xsl:with-param name="use-js-css-component" select="'false'"/>
            <xsl:with-param name="debug-mode">true</xsl:with-param>
		    
		    <xsl:with-param name="context-path" select="$contextPath"/>
		    <xsl:with-param name="workspace-name" select="$workspaceName"/>
		    <xsl:with-param name="workspace-prefix" select="$workspaceURI"/>
		    
		    <xsl:with-param name="head-title">
					<i18n:text i18n:catalogue="workspace.{$workspaceName}" i18n:key="WORKSPACE_ADMIN_LABEL_SHORT"/>
					<xsl:text> </xsl:text>
					<i18n:text i18n:catalogue="application" i18n:key="APPLICATION_PRODUCT_LABEL"/>
		    </xsl:with-param>
		    
		    <xsl:with-param name="head-meta">
		    	<xsl:call-template name="kernel-load">
		            <xsl:with-param name="scripts" select="/Plugins/Desktop/category/DesktopItem/scripts/file"/>
		            <xsl:with-param name="css" select="/Plugins/Desktop/category/DesktopItem/css/file"/>
		            <xsl:with-param name="use-css-component">false</xsl:with-param>
		            <xsl:with-param name="use-js-component">false</xsl:with-param>
		            <xsl:with-param name="debug-mode">true</xsl:with-param>
		        </xsl:call-template>	
		        <xsl:copy-of select="/Plugins/html/head/*[local-name(.) != 'title']"/>
		        <xsl:call-template name="workspace-scripts"/>	    
		    </xsl:with-param>

		    <xsl:with-param name="body-title">
		    	<img id="title-logo" alt="workspace.admin:WORKSPACE_ADMIN_LABEL_LONG" i18n:attr="alt"/>
		    	<script type="text/javascript">
		    		document.getElementById('title-logo').src = "<xsl:value-of select="$contextPath"/><xsl:value-of select="$workspaceURI"/>/resources/img/Admin_<i18n:text i18n:key="KERNEL_LANGUAGE_CODE" i18n:catalogue="kernel"/>.png";
		    	</script>
			</xsl:with-param>
		    
		    <xsl:with-param name="body-col-main">
		    	<xsl:copy-of select="/Plugins/html/body/node()"/>
		    </xsl:with-param>
    	</xsl:call-template>
    </xsl:template>
	
    <xsl:template name="workspace-scripts">
    	<script type="text/javascript">
    		Ext.application({
			    name: 'Ametys',
			
			    launch: function() {
			        var mainPanel = Ext.create('Ext.panel.Panel', {
			            html : 'Todo',
			            renderTo: 'main',
			            
			            listeners: {
			            	'resize' : function() {
			            		this.setSize(Ext.get('main').getSize(true))
			            	}
			            }
			        });

					Ext.EventManager.onWindowResize(function() {
						this.setSize(null, 0);
			        	this.setSize(Ext.get('main').getSize(true))
					}, mainPanel);
			    }
			});
    	</script>
    	<script type="text/javascript">
<!--     		Ext.namespace('org.ametys.runtime.administrator'); -->
    		
<!--     		org.ametys.runtime.administrator.Panel = function () -->
<!--     		{ -->
<!--     		} -->
    		
<!--     		org.ametys.runtime.administrator.Panel.createPanel = function () -->
<!--     		{ -->
<!--     			return new org.ametys.HtmlContainer ({ -->
<!--     				region: 'center', -->
<!-- 					html: '&lt;p&gt;&lt;i&gt;Override the &lt;b&gt;org.ametys.runtime.administrator.Panel.createPanel&lt;/b&gt; function to create your own administration tool here ...&lt;/i&gt;&lt;/p&gt;' -->
<!-- 				}); -->
<!--     		} -->
    		
<!--     		org.ametys.runtime.administrator.Panel._tpl = new Ext.Template ( -->
<!-- 	    			'&lt;div id="admin-top-panel"&gt;', -->
<!-- 	    			'&lt;div id="admin-path"&gt;', -->
<!-- 	    			'&lt;a href="{workspaceContext}"&gt;<i18n:text i18n:key="WORKSPACE_ADMIN_HOME" i18n:catalogue="workspace.{$workspaceName}" />&lt;/a&gt; &gt; {title}', -->
<!-- 	    			'&lt;/div&gt;', -->
<!-- 	    			'&lt;h2 class="admin-panel-title"&gt;<xsl:copy-of select="/Plugins/html/head/title/node()"/>&lt;/h2&gt;', -->
<!-- 	    			'&lt;/div&gt;' -->
<!--     		); -->
<!--     		org.ametys.runtime.administrator.Panel._tpl.compile(); -->
    			
<!-- 		    org.ametys.runtime.HomePage.createPanel = function () -->
<!-- 			{ -->
<!-- 				<xsl:variable name="title"><xsl:copy-of select="/Plugins/html/head/title/node()"/></xsl:variable> -->
				
<!-- 				return new Ext.Panel({ -->
<!-- 					id: 'admin-panel', -->
<!-- 					baseCls: 'admin-panel', -->
<!-- 					border: false, -->
<!-- 					layout: 'border', -->
<!-- 					autoScroll: false, -->
<!-- 					height: 'auto', -->
					
<!-- 					items : [ -->
<!-- 						// Administration tool title -->
<!-- 		    			new org.ametys.HtmlContainer ( -->
<!-- 						{ -->
<!-- 		   					border: false, -->
<!-- 		   					region:'north', -->
<!-- 		   					height: 43, -->
<!-- 		   					baseCls: '', -->
<!-- 		   					html : '', -->
<!-- 		   					listeners: { -->
<!-- 						        'render' : function(p) { -->
<!-- 						        	org.ametys.runtime.administrator.Panel._tpl.overwrite(p.getEl(), {  -->
<!-- 										workspaceContext : Ametys.WORKSPACE_URI -->
<!-- 									}); -->
<!-- 						        } -->
<!-- 		    				} -->
		
<!-- 						}), -->
<!-- 		    			// Administration tool panel -->
<!-- 		    			org.ametys.runtime.administrator.Panel.createPanel () -->
<!-- 					] -->
<!-- 				}); -->
<!-- 			} -->
			
<!-- 			org.ametys.runtime.administrator.Panel.drawPaddle = function () -->
<!-- 			{ -->
<!-- 				<xsl:if test="/Plugins/Desktop/category"> -->
<!-- 					var items = []; -->
					
<!-- 					<xsl:for-each select="/Plugins/Desktop/category"> -->
<!-- 						<xsl:for-each select="DesktopItem"> -->
<!-- 							var item = new org.ametys.DockItem ({ -->
<!-- 								tooltip: org.ametys.AdminTools.DockTooltipFormater("<xsl:copy-of select="action/param[@name='label']/node()"/>", "<xsl:value-of select="$contextPath"/><xsl:value-of select="action/param[@name='icon-large']"/>", "<xsl:copy-of select="action/param[@name='default-description']/node()"/>"), -->
<!-- 								icon: "<xsl:value-of select="$contextPath"/><xsl:value-of select="action/param[@name='icon-small']"/>" -->
<!-- 							 	<xsl:if test="../CurrentUIItem/@position = position()">, pressed: true</xsl:if> -->
<!-- 								<xsl:if test="not(@disabled)"> -->
<!-- 	                            	,  -->
<!-- 	                                "plugin" : "<xsl:value-of select="@plugin"/>", -->
<!-- 	                                "actionFunction" : <xsl:value-of select="action/@class"/>, -->
<!-- 	                                "actionParams" : {<xsl:for-each select="action/param"> -->
<!-- 	                                	<xsl:text>"</xsl:text><xsl:value-of select="@name"/>" : "<xsl:copy-of select="./node()"/><xsl:text>"</xsl:text> -->
<!-- 	                                    <xsl:if test="position() != last()">, </xsl:if> -->
<!-- 	                                    </xsl:for-each>} -->
<!-- 	                                </xsl:if> -->
<!-- 							}); -->
<!-- 							items.push(item); -->
<!-- 						</xsl:for-each> -->
<!-- 						<xsl:if test="position() != last()"> -->
<!-- 							var tile = new org.ametys.HtmlContainer ({ -->
<!-- 									cls: 'dock-tile' -->
<!-- 							}); -->
<!-- 							items.push(tile); -->
<!-- 						</xsl:if> -->
<!-- 					</xsl:for-each> -->
					
<!-- 					var dock = new Ext.Panel({ -->
<!-- 						baseCls: 'paddle', -->
<!-- 						renderTo: 'content_left', -->
<!-- 						items: [new org.ametys.HtmlContainer ({ -->
<!-- 									cls: 'dock-top' -->
<!-- 								}),  -->
<!-- 								new Ext.Panel ({ -->
<!-- 									baseCls: 'dock-center', -->
<!-- 									items: items -->
<!-- 								}),  -->
<!-- 								new org.ametys.HtmlContainer ({ -->
<!-- 									cls: 'dock-bottom' -->
<!-- 								}) -->
<!-- 						] -->
<!-- 					}); -->
<!-- 				</xsl:if> -->
<!-- 			} -->
			
<!-- 			Ext.onReady(org.ametys.runtime.administrator.Panel.drawPaddle); -->
		</script>
		
   		<xsl:copy-of select="/Plugins/html/script/node()"/>
    </xsl:template>
    
</xsl:stylesheet>