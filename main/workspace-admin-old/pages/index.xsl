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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:escaper="org.apache.commons.lang.StringEscapeUtils"
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
	
	<xsl:import href="kernel://pages/home/home.xsl"/>
    <xsl:import href="common/common.xsl"/>
    
	<xsl:param name="contextPath"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>
    
	<xsl:template match="/">
    	<xsl:call-template name="home">
		    <xsl:with-param name="needs-kernel-ui" select="true()"/>
            <xsl:with-param name="debug-mode">true</xsl:with-param>
		    
		    <xsl:with-param name="context-path" select="$contextPath"/>
		    <xsl:with-param name="workspace-name" select="$workspaceName"/>
		    <xsl:with-param name="workspace-prefix" select="$workspaceURI"/>
		    
		    <xsl:with-param name="plugins-direct-prefix">/plugins</xsl:with-param>
		    <xsl:with-param name="plugins-wrapped-prefix">/_plugins</xsl:with-param>
		    
		    <xsl:with-param name="authorized-browsers"><xsl:call-template name="authorized-browsers"/></xsl:with-param>
		    
		    <xsl:with-param name="head-title"><xsl:call-template name="head-title"/></xsl:with-param>
		    
		    <xsl:with-param name="head-meta">
		    	<xsl:call-template name="head-meta"/>
		        
                <link rel="stylesheet" href="{$contextPath}{$workspaceURI}/resources/css/index.css" type="text/css"/>
		        
		        <xsl:call-template name="workspace-scripts"/>	    
		    </xsl:with-param>

		    <xsl:with-param name="body-title"><xsl:call-template name="body-title"/></xsl:with-param>
		    
		    <xsl:with-param name="body-col-main">
		    </xsl:with-param>
    	</xsl:call-template>
    </xsl:template>
	
    <xsl:template name="workspace-scripts">
    	<script type="text/javascript">
    		createTop = function ()
    		{
    			return null;
    		}
    		
    		createPanel = function ()
    		{
	    		var items = []
	    		
	    		var linkData = {links : []};
	    		linkData.links.push ({
	            	text : "<i18n:text i18n:key="WORKSPACE_ADMIN_LINK_WEBSITE" i18n:catalogue="workspace.{$workspaceName}"/>",
	                url: "http://www.ametys.org",
	                name : "ametys.org"
	            });
	            linkData.links.push ({
	            	text : "<i18n:text i18n:key="WORKSPACE_ADMIN_LINK_DOCUMENTATION" i18n:catalogue="workspace.{$workspaceName}"/>",
	                url: "http://wiki.ametys.org",
	                name : "wiki.ametys.org"
	            });
	            linkData.links.push ({
	            	text : "<i18n:text i18n:key="WORKSPACE_ADMIN_LINK_ISSUE" i18n:catalogue="workspace.{$workspaceName}"/>",
	                url: "https://issues.ametys.org",
	                name : "issues.ametys.org"
	            });
	            
	            var tplLink = new Ext.XTemplate (
	                		'&lt;div class="links"&gt;',
	                		'&lt;tpl for="links"&gt;',
	                			'&lt;div class="link"&gt;',
	                				'&lt;div class="label"&gt;',
	                					'&lt;div class="left"&gt;&lt;!----&gt;&lt;/div&gt;',
	                					'&lt;div class="text" style="width:{width}px"&gt;{text}&lt;/div&gt;',
	                				'&lt;/div&gt;',
	                				'&lt;a href="{url}" target="_blank"&gt;{name}&lt;/a&gt;',
	                			'&lt;/div&gt;',
	                		'&lt;/tpl&gt;',
	                		'&lt;/div&gt;'
	            );
	            tplLink.compile();
	            
	           	var links = new Ext.Component ({
   					border: false,
   					html : '',
   					listeners: {
				        'render' : function(p) {
				        	tplLink.overwrite(p.getEl(), linkData);
				        }
    				}
				});
	    		items.push(links);
	    		
	    		function _desktop_item_over() {
	    			var i = this.icon.lastIndexOf(".");
	    			this.getEl().query(".x-btn-icon-el")[0].style.backgroundImage = "url('" + this.icon.substring(0, i) + "_over" + this.icon.substring(i) + "')";
	    		}
	    		function _desktop_item_out() {
	    			var i = this.icon.lastIndexOf(".");
	    			this.getEl().query(".x-btn-icon-el")[0].style.backgroundImage = "url('" + this.icon + "')";
	    		}
				<xsl:for-each select="/Admin/Desktop/category">
						var category = new Ext.Component ({
								cls: 'desktop-category-title',
								html: "&lt;h2&gt;<i18n:text i18n:key="{@name}" i18n:catalogue="application"/>&lt;/h2&gt;"
						});
						items.push(category);
						
						<xsl:for-each select="DesktopItem">
							<xsl:variable name="btnId" select="generate-id()"/>
							
							var btnConfig_<xsl:value-of select="$btnId"/> = <xsl:value-of select="action"/>;
							   
							var item = new Ext.button.Button({
								border: false,
								cls: 'desktop-item',
								
								icon: Ametys.CONTEXT_PATH + btnConfig_<xsl:value-of select="$btnId"/>["icon-large"],
								iconAlign: "top",
								scale: 'large',
								
								text: btnConfig_<xsl:value-of select="$btnId"/>["label"],
								tooltip: {
									title: btnConfig_<xsl:value-of select="$btnId"/>["label"],
									text: btnConfig_<xsl:value-of select="$btnId"/>["default-description"]
								},
								
								<xsl:if test="not(@disabled)">
									handler: function () { 
										<xsl:value-of select="action/@class"/>("<xsl:value-of select="@plugin"/>", btnConfig_<xsl:value-of select="$btnId"/>);
	 	                            },
								</xsl:if>
								
								listeners: { 
									'mouseover': _desktop_item_over,
									'mouseout': _desktop_item_out
								},
											
								width: 144,
								height: 109
							});
							items.push(item);
						</xsl:for-each>
				</xsl:for-each>
				
				return new Ext.Container({
					cls: 'desktop',
					border: false,
					autoScroll: true,
					
					items: items
				});
			}
			
			function createDock() {};
		</script>
    </xsl:template>
    
</xsl:stylesheet>
