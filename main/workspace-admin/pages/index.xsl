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
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
	
	<xsl:import href="kernel://stylesheets/plugins.xsl"/>
    <xsl:import href="core/template.xsl"/>
    
    <xsl:param name="workspaceName"/>
	
    <xsl:template name="workspace-title"/>
    
    <xsl:template name="workspace-head">
	   <xsl:call-template name="plugins-load">
            <xsl:with-param name="scripts" select="/Admin/Desktop/category/DesktopItem/scripts/file"/>
            <xsl:with-param name="css" select="/Admin/Desktop/category/DesktopItem/css/file"/>
            <xsl:with-param name="use-css-component">false</xsl:with-param>
            <xsl:with-param name="use-js-component">false</xsl:with-param>
            <xsl:with-param name="debug-mode">true</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="workspace-script">
    	<script type="text/javascript">
    		org.ametys.runtime.HomePage.createPanel = function ()
    		{
	    		var items = []
	    		
	    		var linkData = {links : []};
	    		linkData.links.push ({
	            	text : "<i18n:text i18n:key="WORKSPACE_ADMIN_LINK_WEBSITE" i18n:catalogue="workspace.{$workspaceName}"/>",
	                url: "http://www.ametys.org",
	                name : "ametys.org",
	                width: 42 // ie6
	            });
	            linkData.links.push ({
	            	text : "<i18n:text i18n:key="WORKSPACE_ADMIN_LINK_DOCUMENTATION" i18n:catalogue="workspace.{$workspaceName}"/>",
	                url: "http://wiki.ametys.org",
	                name : "wiki.ametys.org",
	                width: 75 // ie 6
	            });
	            linkData.links.push ({
	            	text : "<i18n:text i18n:key="WORKSPACE_ADMIN_LINK_ISSUE" i18n:catalogue="workspace.{$workspaceName}"/>",
	                url: "https://issues.ametys.org",
	                name : "issues.ametys.org",
	                width: 80// ie6
	            });
	            
	            var tplLink = new Ext.XTemplate (
	                		'&lt;div id="links"&gt;',
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
	           	var links = new org.ametys.HtmlContainer ({
   					border: false,
   					html : '',
   					listeners: {
				        'render' : function(p) {
				        	tplLink.overwrite(p.getEl(), linkData);
				        }
    				}
				});
	    		
	    		items.push(links);
	    		
				<xsl:for-each select="/Admin/Desktop/category">
						var category = new org.ametys.DesktopCategory ({
								text: "<i18n:text i18n:key="{@name}" i18n:catalogue="application"/>"
						});
						items.push(category);
						
						<xsl:for-each select="DesktopItem">
							var item = new org.ametys.DesktopItem ({
								text: "<xsl:copy-of select="action/param[@name='label']/node()"/>",
								desc: "<xsl:copy-of select="action/param[@name='default-description']/node()"/>",
								icon: "<xsl:value-of select="$contextPath"/><xsl:value-of select="action/param[@name='icon-large']"/>",
								iconOver: "<xsl:value-of select="$contextPath"/><xsl:value-of select="substring-before(action/param[@name='icon-large'], '.')"/>_over.<xsl:value-of select="substring-after(action/param[@name='icon-large'], '.')"/>"
								<xsl:if test="not(@disabled)">
	                            	, 
	                                "plugin" : "<xsl:value-of select="@plugin"/>",
	                                "actionFunction" : <xsl:value-of select="action/@class"/>,
	                                "actionParams" : {<xsl:for-each select="action/param">
	                                	<xsl:text>"</xsl:text><xsl:value-of select="@name"/>" : "<xsl:copy-of select="./node()"/><xsl:text>"</xsl:text>
	                                    <xsl:if test="position() != last()">, </xsl:if>
	                                    </xsl:for-each>}
	                                </xsl:if>
							});
							items.push(item);
						</xsl:for-each>
				</xsl:for-each>
				
				return new org.ametys.DesktopPanel({
					items: items
				});
			}
			
		</script>
    </xsl:template>
    
</xsl:stylesheet>
