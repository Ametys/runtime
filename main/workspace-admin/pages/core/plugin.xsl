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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
	
	<xsl:import href="template.xsl"/>
	<xsl:import href="resource://org/ametys/runtime/kernel/stylesheets/plugins.xsl"/>
	
    <xsl:template name="workspace-title"><xsl:copy-of select="/Plugins/html/head/title/node()"/></xsl:template>

    <xsl:template name="workspace-head"><xsl:copy-of select="/Plugins/html/head/*[local-name(.) != 'title']"/></xsl:template>

    <xsl:template name="workspace-body">
    	<xsl:copy-of select="/Plugins/html/body/node()"/>
    </xsl:template>
    
    <xsl:template name="workspace-script">
    	<xsl:call-template name="plugins-load">
            <xsl:with-param name="scripts" select="/Plugins/Desktop/category/DesktopItem/scripts/file"/>
            <xsl:with-param name="css" select="/Plugins/Desktop/category/DesktopItem/css/file"/>
        </xsl:call-template>
        
    	<script type="text/javascript">
    		Ext.namespace('org.ametys.runtime.administrator');
    		
    		org.ametys.runtime.administrator.Panel = function ()
    		{
    		}
    		
    		org.ametys.runtime.administrator.Panel.createPanel = function ()
    		{
    			return new org.ametys.HtmlContainer ({
    				region: 'center',
					html: '&lt;p&gt;&lt;i&gt;Override the &lt;b&gt;org.ametys.runtime.administrator.Panel.createPanel&lt;/b&gt; function to create your own administration tool here ...&lt;/i&gt;&lt;/p&gt;'
				});
    		}
    		
    		org.ametys.runtime.administrator.Panel._tpl = new Ext.Template (
	    			'&lt;div id="admin-top-panel"&gt;',
	    			'&lt;div id="admin-path"&gt;',
	    			'&lt;a href="{workspaceContext}"&gt;{homeTitle}&lt;/a&gt; &gt; {title}',
	    			'&lt;/div&gt;',
	    			'&lt;h2 class="admin-panel-title"&gt;{title}&lt;/h2&gt;',
	    			'&lt;/div&gt;'
    		);
    		org.ametys.runtime.administrator.Panel._tpl.compile();
    			
		    org.ametys.runtime.HomePage.createPanel = function ()
			{
				<xsl:variable name="title"><xsl:call-template name="workspace-title"/></xsl:variable>
				
				return new Ext.Panel({
					id: 'admin-panel',
					baseCls: 'admin-panel',
					border: false,
					layout: 'border',
					autoScroll: false,
					height: 'auto',
					
					items : [
						// Administration tool title
		    			new org.ametys.HtmlContainer (
						{
		   					border: false,
		   					region:'north',
		   					height: 43,
		   					baseCls: '',
		   					html : '',
		   					listeners: {
						        'render' : function(p) {
						        	org.ametys.runtime.administrator.Panel._tpl.overwrite(p.getEl(), { 
										workspaceContext : "<xsl:value-of select="$workspaceContext"/>",
										homeTitle : "<i18n:text i18n:key="WORKSPACE_ADMIN_HOME" i18n:catalogue="workspace.{$workspaceName}" />",
										title: "<xsl:copy-of select="$title"/>"
									});
						        }
		    				}
		
						}),
		    			// Administration tool panel
		    			org.ametys.runtime.administrator.Panel.createPanel ()
					]
				});
			}
			
			org.ametys.runtime.administrator.Panel.drawPaddle = function ()
			{
				<xsl:if test="/Plugins/Desktop/category">
					var items = [];
					
					<xsl:for-each select="/Plugins/Desktop/category">
						<xsl:for-each select="DesktopItem">
							var item = new org.ametys.DockItem ({
								tooltip: org.ametys.AdminTools.DockTooltipFormater("<xsl:copy-of select="action/param[@name='label']/node()"/>", "<xsl:value-of select="$contextPath"/><xsl:value-of select="action/param[@name='icon-large']"/>", "<xsl:copy-of select="action/param[@name='default-description']/node()"/>"),
								icon: "<xsl:value-of select="$contextPath"/><xsl:value-of select="action/param[@name='icon-small']"/>"
							 	<xsl:if test="../CurrentUIItem/@position = position()">, pressed: true</xsl:if>
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
						<xsl:if test="position() != last()">
							var tile = new org.ametys.HtmlContainer ({
									cls: 'dock-tile'
							});
							items.push(tile);
						</xsl:if>
					</xsl:for-each>
					
					var dock = new Ext.Panel({
						baseCls: 'paddle',
						renderTo: 'content_left',
						items: [new org.ametys.HtmlContainer ({
									cls: 'dock-top'
								}), 
								new Ext.Panel ({
									baseCls: 'dock-center',
									items: items
								}), 
								new org.ametys.HtmlContainer ({
									cls: 'dock-bottom'
								})
						]
					});
					
					/** IE 6 */
					if (window.pngFix)
					{
						pngFix.fixAllByTagName(["button"]);
					}
				</xsl:if>
			}
			
			Ext.onReady(org.ametys.runtime.administrator.Panel.drawPaddle);
		</script>
		
   		<xsl:copy-of select="/Plugins/html/script/node()"/>
    </xsl:template>
    
</xsl:stylesheet>